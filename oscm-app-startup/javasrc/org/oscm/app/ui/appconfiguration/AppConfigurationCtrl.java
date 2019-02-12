/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                           
 *                                                                              
 *  Creation Date: 13 Mar 2014                                       
 *                                                                              
 *******************************************************************************/
package org.oscm.app.ui.appconfiguration;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.oscm.app.ui.BaseCtrl;
import org.oscm.app.ui.SessionConstants;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.types.constants.Configuration;

/**
 * This controller of manage app configuration settings.
 * 
 * Mao
 */
@ViewScoped
@ManagedBean
public class AppConfigurationCtrl extends BaseCtrl {

    private static final Logger log = Logger.getLogger(AppConfigurationCtrl.class.getName());

    private APPConfigurationServiceBean appConfigService;
    private APPTimerServiceBean timerService;
    private boolean pingButtonVisibility = false;
    
    @ManagedProperty(value="#{appConfigurationModel}")
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
                model.setControllerIds(new HashSet<String>(model.getItems()
                        .keySet()));
                model.setLoggedInUserId(getLoggedInUserId());
                model.setRestartRequired(isAPPSuspend());
                setModel(model);
            }
        } catch (Exception e) {
            addError(e);
        }
        return "";
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

    public void invokeCanPing(String controllerId) throws APPlatformException {


        Map<String, Boolean> updatedMap = new HashMap<>();
        boolean canPing = false;

        switch (controllerId) {
            case Configuration.VMWARE_CONTROLLER_ID:
                //TODO: Execute canPingImplementation
                updatedMap.put(Configuration.VMWARE_CONTROLLER_ID, true);
//                updatedMap.put(Configuration.VMWARE_CONTROLLER_ID, canPing);
                break;
            case Configuration.AWS_CONTROLLER_ID:
                updatedMap.put(Configuration.AWS_CONTROLLER_ID, true);
                break;
            default:
                addErrorMessage("Unsupported controller ID");
        }

        updatePingButtonVisibilityMap(updatedMap);
    }

    private void updatePingButtonVisibilityMap(Map<String, Boolean> updatedMap) {
        model.getPingButtonVisibilityMap().putAll(updatedMap);
        Map<String, Boolean> lookup = model.getPingButtonVisibilityMap();
    }

    public void invokePing(String controllerId) throws APPlatformException {
        switch (controllerId) {
            case Configuration.VMWARE_CONTROLLER_ID:
                break;
            default:
                addErrorMessage("Unsupported controller ID");

        }
    }
}
