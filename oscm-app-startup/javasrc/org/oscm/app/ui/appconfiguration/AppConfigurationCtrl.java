/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *
 *  Creation Date: 13 Mar 2014                                       
 *
 *******************************************************************************/
package org.oscm.app.ui.appconfiguration;

import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.ui.BaseCtrl;
import org.oscm.app.ui.SessionConstants;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.ControllerLookupException;
import org.oscm.app.v2_0.exceptions.ServiceNotReachableException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.types.constants.Configuration;
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
 * <p>
 * Mao
 */
@ViewScoped
@ManagedBean
public class AppConfigurationCtrl extends BaseCtrl {

    private static final Logger logger = LoggerFactory
            .getLogger(AppConfigurationCtrl.class.getName());

    private static final String ERROR_PING_UNSUPPORTED = "app.message.error.controller.not.pingable";
    private static final String ERROR_PING_FAILED = "app.message.error.controller.unreachable";
    private static final String INFO_PING_SUCCEEDED = "app.message.info.controller.reachable";
    private static final String DETAILED_MESSAGE_SUFFIX = ".detailed";

    private APPConfigurationServiceBean appConfigService;
    private APPTimerServiceBean timerService;

    private Map<String, APPlatformController> controllerInstanceMap = new ConcurrentHashMap<>();

    @ManagedProperty(value = "#{appConfigurationModel}")
    private AppConfigurationModel model;

    public String getInitialize() {
        logger.error(">>>>> TEST LOG");
        AppConfigurationModel model = getModel();
        try {
            if (model == null) {
                model = new AppConfigurationModel();
            }
            if (!model.isInitialized()) {
                model.setInitialized(true);
                model.setItems(initControllerOrganizations());
                model.setKeys(initItemKeys());
                model.setControllerIds(new HashSet<String>(model.getItems()
                        .keySet()));
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
        for(String key : keys) {
            invokeCanPing(key);
        }
    }

    public String getLoggedInUserId() {
        FacesContext facesContext = getFacesContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);
        if (session != null) {
            String loggedInUserId = ""
                    + session.getAttribute(SessionConstants.SESSION_USER_ID);
            return loggedInUserId;
        }
        return null;
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    private HashMap<String, String> initControllerOrganizations() {
        HashMap<String, String> map = getAppConfigService()
                .getControllerOrganizations();
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

    /**
     * Save modified values
     */
    public String save() {
        if (!model.isTokenValid()) {
            return OUTCOME_SAMEPAGE;
        }
        try {
            if (!checkAddController()) {
                return OUTCOME_SAMEPAGE;
            }
            HashMap<String, String> store = new HashMap<String, String>(
                    model.getItems());
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
            model.getItems().put(newControllerId.trim(),
                    newOrganizationId.trim());
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

    public void invokeCanPing(String controllerId) {
        Map<String, Boolean> updatedMap = new HashMap<>();

        try {
            if (Configuration.pingableControllersList.contains(controllerId)) {
                APPlatformController controller = getControllerInstance(controllerId);
                updatedMap.put(controllerId, controller != null && controller.canPing());
            }
        } catch (ControllerLookupException | ConfigurationException e) {
            displayError(ERROR_PING_UNSUPPORTED, Arrays.toString(e.getStackTrace()), true);
        }

        updatePingButtonVisibilityMap(updatedMap);
    }

    private void updatePingButtonVisibilityMap(Map<String, Boolean> updatedMap) {
        model.getPingButtonVisibilityMap().putAll(updatedMap);
    }

    public void invokePing(String controllerId) {
        try {
            APPlatformController controller = getControllerInstance(controllerId);

            if (controller == null) displayError(ERROR_PING_UNSUPPORTED, null, false);
            else if (controller.ping(controllerId)) addMessage(INFO_PING_SUCCEEDED);
            else displayError(ERROR_PING_FAILED, null, false);

        } catch (ControllerLookupException | ServiceNotReachableException e) {
            displayError(ERROR_PING_FAILED, Arrays.toString(e.getStackTrace()), true);
            displayError(ERROR_PING_FAILED, "STACK2", false);
            displayError(ERROR_PING_FAILED, "STACK3", true);
            displayError(ERROR_PING_FAILED, "STACK4", true);
            displayError(ERROR_PING_FAILED, "STACK5", true);
        }

    }

    private APPlatformController getControllerInstance(String controllerId) throws ControllerLookupException {
        if (!controllerInstanceMap.containsKey(controllerId))
            controllerInstanceMap.put(
                    controllerId, APPlatformControllerFactory.getInstance(controllerId));

        return controllerInstanceMap.get(controllerId);
    }

    private void displayError(String message, String stackTrace, boolean enableDetailsPopup) {
        if(enableDetailsPopup) message += DETAILED_MESSAGE_SUFFIX;
        addError(message);
//        model.setDetailedExceptionInfo(stackTrace);
//        model.setEnableExceptionInfo(enableDetailsPopup);
    }
}