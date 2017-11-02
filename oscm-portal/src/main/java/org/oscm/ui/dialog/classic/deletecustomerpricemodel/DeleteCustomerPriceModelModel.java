/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 15.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.deletecustomerpricemodel;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.ui.beans.BaseModel;

/**
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name="deleteCustomerPriceModelModel")
public class DeleteCustomerPriceModelModel extends BaseModel {

    private static final long serialVersionUID = -449702714364869007L;

    private boolean initialized;
    private boolean allSelected;
    private List<SelectItem> customers = new ArrayList<SelectItem>();
    private List<CustomerService> services = new ArrayList<CustomerService>();
    private String selectedOrgId;

    public boolean isAllSelected() {
        return allSelected;
    }

    public void setAllSelected(boolean allSelected) {
        this.allSelected = allSelected;
    }

    public List<SelectItem> getCustomers() {
        return customers;
    }

    public void setCustomers(List<SelectItem> customers) {
        this.customers = customers;
    }

    public List<CustomerService> getServices() {
        return services;
    }

    public void setServices(List<CustomerService> services) {
        this.services = services;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public void setSelectedOrgId(String selectedOrgId) {
        this.selectedOrgId = selectedOrgId;
    }

    public String getSelectedOrgId() {
        return selectedOrgId;
    }
}
