/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-2-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.serviceinstance;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.app.ui.BaseModel;

/**
 * Model of manage service instance page
 * 
 * @author zhaohang
 * 
 */
@ViewScoped
@ManagedBean
public class ManageServiceInstanceModel extends BaseModel {

    private static final long serialVersionUID = -1884953013778507106L;

    private boolean initialized;

    private List<ServiceInstanceRow> serviceInstanceRows;

    private ServiceInstanceRow selectedInstanceRow;

    private String selectedInstanceId;

    private String controllerId;

    private String timePattern;

    private String loggedInUserId;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public List<ServiceInstanceRow> getServiceInstanceRows() {
        return serviceInstanceRows;
    }

    public void setServiceInstanceRows(
            List<ServiceInstanceRow> serviceInstanceRows) {
        this.serviceInstanceRows = serviceInstanceRows;
    }

    public String getSelectedInstanceId() {
        return selectedInstanceId;
    }

    public void setSelectedInstanceId(String selectedInstanceId) {
        this.selectedInstanceId = selectedInstanceId;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public ServiceInstanceRow getSelectedInstanceRow() {
        return selectedInstanceRow;
    }

    public void setSelectedInstanceRow(ServiceInstanceRow selectedInstanceRow) {
        this.selectedInstanceRow = selectedInstanceRow;
    }

    public String getTimePattern() {
        return timePattern;
    }

    public void setTimePattern(String timePattern) {
        this.timePattern = timePattern;
    }

    public String getLoggedInUserId() {
        return loggedInUserId;
    }

    public void setLoggedInUserId(String loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }
}
