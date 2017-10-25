/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import org.oscm.converter.CharConverter;
import org.oscm.reportingservice.business.model.RDO;

/**
 * Note: holds some information redundant, like the service name from the price
 * model, due to simpler report template creation.
 **/
public class RDOSummary extends RDO {

    private static final long serialVersionUID = 1269462373532220673L;

    // organization information
    private String organizationName;
    private String organizationAddress;
    private String supplierName;
    private String supplierAddress;
    private String paymentType;

    // subscription information
    private String subscriptionId;
    private String userGroupName;
    private String userGroupReferenceId;
    private String purchaseOrderNumber;

    // price model information
    private String serviceName;
    private String priceModelStartDate;
    private String priceModelEndDate;

    private String billingDate; // creation time of billing result
    private RDOPriceModel priceModel;

    // These data also exist in the price model, but we need them here
    // for the display in the reports
    private String amount;
    private String grossAmount;
    private String currency;
    private String vat;
    private String vatAmount;
    private String discount;
    private String discountAmount;
    private String netAmountBeforeDiscount;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String id) {
        this.subscriptionId = id;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(String billingDate) {
        this.billingDate = billingDate;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String orgName) {
        this.organizationName = CharConverter.convertToSBC(orgName);
    }

    public String getOrganizationAddress() {
        return organizationAddress;
    }

    public void setOrganizationAddress(String orgAddress) {
        this.organizationAddress = CharConverter.convertToSBC(orgAddress);
    }

    public String getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(String vatAmount) {
        this.vatAmount = vatAmount;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setGrossAmount(String grossAmount) {
        this.grossAmount = grossAmount;
    }

    public String getGrossAmount() {
        return grossAmount;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = CharConverter.convertToSBC(supplierName);
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = CharConverter.convertToSBC(supplierAddress);
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public RDOPriceModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(RDOPriceModel priceModel) {
        this.priceModel = priceModel;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPriceModelStartDate() {
        return priceModelStartDate;
    }

    public void setPriceModelStartDate(String priceModelstartDate) {
        this.priceModelStartDate = priceModelstartDate;
    }

    public String getPriceModelEndDate() {
        return priceModelEndDate;
    }

    public void setPriceModelEndDate(String priceModelEndDate) {
        this.priceModelEndDate = priceModelEndDate;
    }

    public String getNetAmountBeforeDiscount() {
        return netAmountBeforeDiscount;
    }

    public void setNetAmountBeforeDiscount(String netAmountBeforeDiscount) {
        this.netAmountBeforeDiscount = netAmountBeforeDiscount;
    }

    public String getUserGroupName() {
        return userGroupName;
    }

    public void setUserGroupName(String userGroupName) {
        this.userGroupName = userGroupName;
    }

    public String getUserGroupReferenceId() {
        return userGroupReferenceId;
    }

    public void setUserGroupReferenceId(String userGroupReferenceId) {
        this.userGroupReferenceId = userGroupReferenceId;
    }
}
