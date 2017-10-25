/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.12.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.model.billing;

import org.oscm.reportingservice.business.model.RDO;

/**
 * @author kulle
 * 
 */
public class RDOSubscription extends RDO {

    private static final long serialVersionUID = -3984223322067775113L;

    private String grossAmount;
    private String amount;
    private String currency;
    private String vat;
    private String vatAmount;
    private String discount;
    private String discountAmount;
    private String pon;
    private String subscriptionId;
    private String netAmountBeforeDiscount;
    private String purchaseOrderNumber;

    public String getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(String grossAmount) {
        this.grossAmount = grossAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(String vatAmount) {
        this.vatAmount = vatAmount;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPon() {
        return pon;
    }

    public void setPon(String pon) {
        this.pon = pon;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getNetAmountBeforeDiscount() {
        return netAmountBeforeDiscount;
    }

    public void setNetAmountBeforeDiscount(String netAmountBeforeDiscount) {
        this.netAmountBeforeDiscount = netAmountBeforeDiscount;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

}
