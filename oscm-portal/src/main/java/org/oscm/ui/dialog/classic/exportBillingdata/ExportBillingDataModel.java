/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.exportBillingdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.internal.types.enumtypes.BillingSharesResultType;

@ViewScoped
@ManagedBean(name="exportBillingDataModel")
public class ExportBillingDataModel {

    boolean initialized;
    private Date fromDate;
    private Date toDate;
    private String failedDateComponentId = null;

    // Select options
    List<Customer> customers;
    List<SelectItem> billingDataTypeOptions;
    List<SelectItem> sharesResultTypeOptions;
    String anyCustomerSelected = "0";

    public String getAnyCustomerSelected() {
        return anyCustomerSelected;
    }

    public void setAnyCustomerSelected(String anyCustomerSelected) {
        this.anyCustomerSelected = anyCustomerSelected;
    }

    // current user
    boolean platformOperator = false;
    boolean supplierOrReseller = false;

    public List<SelectItem> getSharesResultTypeOptions() {
        return sharesResultTypeOptions;
    }

    public void setSharesResultTypeOptions(
            List<SelectItem> sharesResultTypeOptions) {
        this.sharesResultTypeOptions = sharesResultTypeOptions;
    }

    // chosen options
    private List<BillingSharesResultType> billingSharesResultTypes;

    BillingSharesResultType selectedSharesResultType;

    BillingDataType selectedBillingDataType;

    // xml result
    byte[] billingData;

    // getter and setter

    public byte[] getBillingData() {
        return billingData;
    }

    public void setBillingData(byte[] billingData) {
        this.billingData = billingData;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public Date getFromDate() {
        /*
         * BUG #8350: no initialization
         */
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public BillingDataType getSelectedBillingDataType() {
        return selectedBillingDataType;
    }

    public void setSelectedBillingDataType(
            BillingDataType selectedBillingDataType) {
        this.selectedBillingDataType = selectedBillingDataType;
    }

    public List<SelectItem> getBillingDataTypeOptions() {
        return billingDataTypeOptions;
    }

    public void setBillingDataTypeOptions(
            List<SelectItem> billingDataTypeOptions) {

        this.billingDataTypeOptions = billingDataTypeOptions;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customerOrgs) {
        this.customers = customerOrgs;
    }

    public BillingSharesResultType getSelectedSharesResultType() {
        return selectedSharesResultType;
    }

    public void setSelectedSharesResultType(
            BillingSharesResultType selectedSharesResultType) {
        this.selectedSharesResultType = selectedSharesResultType;
    }

    public List<String> getSelectedOrganizationIds() {
        List<String> selectedOrganizationIds = new ArrayList<String>();
        for (Customer customer : customers) {
            if (customer.isSelected()) {
                selectedOrganizationIds.add(customer.getOrganizationId());
            }
        }
        return selectedOrganizationIds;
    }

    public List<BillingSharesResultType> getBillingSharesResultTypes() {
        return billingSharesResultTypes;
    }

    public void setBillingSharesResultTypes(
            List<BillingSharesResultType> billingSharesResultTypes) {
        this.billingSharesResultTypes = billingSharesResultTypes;
    }

    public boolean isPlatformOperator() {
        return platformOperator;
    }

    public void setPlatformOperator(boolean platformOperator) {
        this.platformOperator = platformOperator;
    }

    public boolean isSupplierOrReseller() {
        return supplierOrReseller;
    }

    public void setSupplierOrReseller(boolean supplierOrReseller) {
        this.supplierOrReseller = supplierOrReseller;
    }

    public void setFailedDateComponentId(String failedDateComponentId) {
        this.failedDateComponentId = (failedDateComponentId == null) ? ""
                : failedDateComponentId;
    }

    public String getFailedDateComponentId() {
        return this.failedDateComponentId;
    }
}
