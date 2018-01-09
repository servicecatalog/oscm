/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-2-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.serviceinstance;

import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ServiceInstance;

/**
 * Row of ServiceInstance table
 * 
 * @author Gao
 * 
 */
public class ServiceInstanceRow {

    private ServiceInstance serviceInstance;

    private List<SelectItem> selectableOperations;

    private List<InstanceParameter> instanceParameters;

    private String selectedOperation;

    private boolean buttonDisabled = true;

    public ServiceInstanceRow(ServiceInstance serviceInstance,
            String selectedOperation) {
        this.serviceInstance = serviceInstance;
        this.selectedOperation = selectedOperation;
    }

    public ServiceInstanceRow(ServiceInstance serviceInstance,
            List<SelectItem> selectableOperations) {
        this.serviceInstance = serviceInstance;
        this.selectableOperations = selectableOperations;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public List<SelectItem> getSelectableOperations() {
        return selectableOperations;
    }

    public void setSelectableOperations(List<SelectItem> selectableOperations) {
        this.selectableOperations = selectableOperations;
    }

    public String getSelectedOperation() {
        return selectedOperation;
    }

    public void setSelectedOperation(String selectedOperation) {
        this.selectedOperation = selectedOperation;
    }

    public boolean isButtonDisabled() {
        return buttonDisabled;
    }

    public void setButtonDisabled(boolean buttonDisabled) {
        this.buttonDisabled = buttonDisabled;
    }

    public List<InstanceParameter> getInstanceParameters() {
        return instanceParameters;
    }

    public void setInstanceParameters(List<InstanceParameter> instanceParameters) {
        this.instanceParameters = instanceParameters;
    }

    public void operationChanged(ValueChangeEvent event) throws Exception {
        String operation = (String) event.getNewValue();
        this.setButtonDisabled(operation == null || operation.isEmpty() ? true
                : false);
    }
}
