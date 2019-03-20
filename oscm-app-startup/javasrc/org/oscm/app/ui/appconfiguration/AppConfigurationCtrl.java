/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 13 Mar 2014
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.ui.appconfiguration;

import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.ui.BaseCtrl;
import org.oscm.app.ui.SessionConstants;
import org.oscm.app.ui.i18n.Messages;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.ControllerLookupException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This controller of manage app configuration settings.
 *
 * <p>Mao
 */
@ViewScoped
@ManagedBean
public class AppConfigurationCtrl extends BaseCtrl {

  private final static Logger LOGGER = LoggerFactory.getLogger(AppConfigurationCtrl.class);

  private static final String ERROR_DETAILED_PING_UNSUPPORTED =
      "app.message.error.controller.not.pingable.detailed";
  private static final String ERROR_PING_FAILED = "app.message.error.controller.unreachable";
  private static final String ERROR_DETAILED_PING_FAILED =
      "app.message.error.controller.unreachable.detailed";
  private static final String INFO_PING_SUCCEEDED = "app.message.info.controller.reachable";

  private APPConfigurationServiceBean appConfigService;
  private APPTimerServiceBean timerService;

  private Map<String, APPlatformController> controllerInstanceMap = new ConcurrentHashMap<>();

  @ManagedProperty(value = "#{appConfigurationModel}")
  private AppConfigurationModel model;

  public String getInitialize() {

    AppConfigurationModel model = getModel();
    try {
      if (model == null) {
        model = new AppConfigurationModel();
      }
      if (!model.isInitialized()) {
        model.setInitialized(true);
        model.setItems(initControllerOrganizations());
        model.setKeys(initItemKeys());
        model.setControllerIds(new HashSet<String>(model.getItems().keySet()));
        model.setLoggedInUserId(getLoggedInUserId());
        model.setRestartRequired(isAPPSuspend());
        setModel(model);
      }

      invokeCanPingForControllers(model.getKeys());

    } catch (Exception e) {
      addError(e);
    }

    return "";
  }

  private void invokeCanPingForControllers(List<String> keys) {
    for (String key : keys) {
      invokeCanPing(key);
    }
  }

  public String getLoggedInUserId() {
    FacesContext facesContext = getFacesContext();
    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
    if (session != null) {
      String loggedInUserId = "" + session.getAttribute(SessionConstants.SESSION_USER_ID);
      return loggedInUserId;
    }
    return null;
  }

  @Override
  protected FacesContext getFacesContext() {
    return FacesContext.getCurrentInstance();
  }

  private HashMap<String, String> initControllerOrganizations() {
    HashMap<String, String> map = getAppConfigService().getControllerOrganizations();
    LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
    if (map != null) {
      List<String> sortedKeys = new ArrayList<String>(map.keySet());
      Collections.sort(sortedKeys);
      for (String key : sortedKeys) {
        result.put(key, map.get(key));
      }
    }
    return result;
  }

  private List<String> initItemKeys() {
    List<String> keys = new ArrayList<String>();
    HashMap<String, String> items = model.getItems();
    if (items != null) {
      keys.addAll(items.keySet());
    }
    return keys;
  }

  /** Save modified values */
  public String save() {
    if (!model.isTokenValid()) {
      return OUTCOME_SAMEPAGE;
    }
    try {
      if (!checkAddController()) {
        return OUTCOME_SAMEPAGE;
      }
      HashMap<String, String> store = new HashMap<String, String>(model.getItems());
      for (String controllerId : store.keySet()) {
        if (isEmpty(store.get(controllerId))) {
          addError(ERROR_ORGANIZATIONID_MANDATORY);
          return OUTCOME_SAMEPAGE;
        }
      }
      for (String controllerId : model.getControllerIds()) {
        if (!store.containsKey(controllerId)) {
          store.put(controllerId, null);
        }
      }
      getAppConfigService().storeControllerOrganizations(store);
      addMessage(SUCCESS_SAVED);
      model.setInitialized(false);
      model.resetToken();
      model.setNewControllerId(null);
      model.setNewOrganizationId(null);
    } catch (Exception e) {
      addError(e);
    }
    return OUTCOME_SUCCESS;
  }

  private boolean checkAddController() {
    String newControllerId = model.getNewControllerId();
    String newOrganizationId = model.getNewOrganizationId();
    if (notIsEmpty(newControllerId)) {
      if (model.getItems().containsKey(newControllerId.trim())) {
        addError(ERROR_CONTROLLERID_EXISTS);
        return false;
      }
    }
    if (notIsEmpty(newControllerId) && notIsEmpty(newOrganizationId)) {
      model.getItems().put(newControllerId.trim(), newOrganizationId.trim());
      return true;
    }
    if (notIsEmpty(newControllerId) || notIsEmpty(newOrganizationId)) {
      addError(ERROR_ADD_BOTH);
      return false;
    }
    return true;
  }

  public String deleteController() {
    if (!model.isTokenValid()) {
      return OUTCOME_SAMEPAGE;
    }
    model.getItems().remove(model.getSelectedControllerId());
    model.getKeys().remove(model.getSelectedControllerId());
    model.resetToken();
    return "";
  }

  public AppConfigurationModel getModel() {
    return model;
  }

  public void setModel(AppConfigurationModel model) {
    this.model = model;
  }

  public APPConfigurationServiceBean getAppConfigService() {
    if (appConfigService == null) {
      appConfigService = lookup(APPConfigurationServiceBean.class);
    }
    return appConfigService;
  }

  public APPTimerServiceBean getAPPTimerService() {
    if (timerService == null) {
      timerService = lookup(APPTimerServiceBean.class);
    }
    return timerService;
  }

  public String restart() {
    if (!model.isTokenValid()) {
      return OUTCOME_SAMEPAGE;
    }
    boolean result = getAPPTimerService().restart(false);
    if (result) {
      model.setRestartRequired(false);
      addMessage(RESTART_SUCCESS);
      return OUTCOME_SUCCESS;
    }
    model.resetToken();
    addMessage(RESTART_FAILURE);
    return OUTCOME_SUCCESS;
  }

  private boolean isAPPSuspend() {
    return getAppConfigService().isAPPSuspend();
  }

  /**
   * Tries to invoke controller's implementation of canPing() method. Checks if controller of
   * specified ID is properly configured. According to results, sets visibility of 'Check
   * connection' button
   *
   * @param controllerId ID of controller for which canPing() method has to be invoked
   */
  public void invokeCanPing(String controllerId) {
    Map<String, Boolean> updatedMap = new HashMap<>();

    try {
      if (Configuration.pingableControllersList.contains(controllerId)) {
        APPlatformController controller = getControllerInstance(controllerId);
        updatedMap.put(controllerId, controller == null ? false : controller.canPing());
      }
    } catch (ControllerLookupException | ConfigurationException e) {
//      logger.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR_PING_NOT_SUPPORTED);
      updatedMap.put(controllerId, false);
    }

    updatePingButtonVisibilityMap(updatedMap);
  }

  /**
   * Updates pingButtonVisibilityMap with 'Check connection' buttons visibility settings according
   * to the outcome of canPing() implementations
   *
   * @param updatedMap pingButtonVisibilityMap updated with new 'Check connection' buttons
   *     visibility settings
   */
  private void updatePingButtonVisibilityMap(Map<String, Boolean> updatedMap) {
    model.getPingButtonVisibilityMap().putAll(updatedMap);
  }

  /**
   * Tries to invoke controller's implementation of ping() method. Checks if it is possible to
   * connect to service instance and presents the outcome to the user.
   *
   * @param controllerId ID of controller for which ping() method has to be invoked
   */
  public void invokePing(String controllerId) {
          LOGGER.error("test error dupa");
          LOGGER.warn("test warn dupa");
          LOGGER.info("test info dupa");
          LOGGER.debug("test debug dupa");
    //                try {
    //                        APPlatformController controller = getControllerInstance(
    //                                controllerId);
    //
    //                        if (controller == null)
    //                                addError(ERROR_DETAILED_PING_UNSUPPORTED);
    //                        else if (controller.ping(controllerId))
    //                                addMessage(INFO_PING_SUCCEEDED);
    //                        else
    //                                addError(ERROR_PING_FAILED);
    //
    //                } catch (ControllerLookupException | ServiceNotReachableException e) {
    //                        logger.logError(Log4jLogger.SYSTEM_LOG, e,
    //                                LogMessageIdentifier.ERROR_PING_FAILED);
    //                        displayDetailedError(
    //                                getLocalizedErrorMessage(
    //                                        ERROR_DETAILED_PING_FAILED),
    //                                e.getMessage()+"\\n"+Arrays.toString(e.getStackTrace())
    //                        );
    //                }

  }

  /**
   * Retrieves localized message, based on provided property key and current user's locale settings
   *
   * @param messageKey property key of the message
   * @return localized message
   */
  private String getLocalizedErrorMessage(String messageKey) {
    String locale = readUserFromSession().getLocale();
    return Messages.get(locale, messageKey);
  }

  private APPlatformController getControllerInstance(String controllerId)
      throws ControllerLookupException {
    if (!controllerInstanceMap.containsKey(controllerId))
      controllerInstanceMap.put(
          controllerId, APPlatformControllerFactory.getInstance(controllerId));

    return controllerInstanceMap.get(controllerId);
  }

  /**
   * Constructs CanPingErrorWrapper and adds it to the set of canPing exception, that are further
   * presented to the user
   *
   * @param errorMessage message that is presented to the user at the top of the page
   * @param stackTrace stack trace content, which is presented in details popup
   */
  private void displayDetailedError(String errorMessage, String stackTrace) {
    model.addCanPingException(new CanPingErrorWrapper(errorMessage, stackTrace));
  }
}
