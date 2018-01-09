/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 13 Mar 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.appconfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.app.ui.BaseModel;

/**
 * @author Mao
 * 
 */
@ViewScoped
@ManagedBean
public class AppConfigurationModel extends BaseModel {

    private static final long serialVersionUID = -3308378616927999618L;
    private boolean initialized;
    private HashMap<String, String> items;
    private Set<String> controllerIds;
    private List<String> keys;
    private String selectedControllerId;
    private String newControllerId;
    private String newOrganizationId;
    private boolean pageDirty;
    private String loggedInUserId;
    private boolean restartRequired = false;
    
    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isDirty() {
        return pageDirty;
    }

    public void setDirty(boolean dirty) {
        pageDirty = dirty;
    }

    public String getSelectedControllerId() {
        return selectedControllerId;
    }

    public void setSelectedControllerId(String controllerId) {
        selectedControllerId = controllerId;
    }

    public String getNewControllerId() {
        return newControllerId;
    }

    public void setNewControllerId(String controllerId) {
        newControllerId = controllerId;
    }

    public String getNewOrganizationId() {
        return newOrganizationId;
    }

    public void setNewOrganizationId(String controllerId) {
        newOrganizationId = controllerId;
    }

    public HashMap<String, String> getItems() {
        return items;
    }

    public Set<String> getControllerIds() {
        return controllerIds;
    }

    public boolean isPageDirty() {
        return pageDirty;
    }

    public void setItems(HashMap<String, String> items) {
        this.items = items;
    }

    public void setControllerIds(Set<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    public void setPageDirty(boolean pageDirty) {
        this.pageDirty = pageDirty;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public String getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public boolean isRestartRequired() {
        return restartRequired;
    }

    public void setRestartRequired(boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

}
