/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides the billing data to be used by a payment service provider (PSP) for
 * charging a customer.
 */
public class ChargingData implements Serializable {
    private static final long serialVersionUID = -7651377504781822518L;

    private BigDecimal netAmount;
    private BigDecimal grossAmount;
    private String vat;
    private BigDecimal vatAmount;
    private BigDecimal netDiscount;

    private Long sellerKey;
    private Long customerKey;

    private Date periodStartTime;
    private Date periodEndTime;

    private String currency;
    private String externalIdentifier;
    private String address;
    private String email;

    private String subscriptionId;
    private String pon; // purchaseOrderNumber

    private long transactionId;

    private List<PriceModelData> priceModelData = new ArrayList<PriceModelData>();

    /**
     * Returns the gross amount the customer has to pay.
     * 
     * @return the gross amount
     */
    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    /**
     * Sets the gross amount the customer has to pay.
     * 
     * @param overallCosts
     *            the gross amount
     */
    public void setGrossAmount(BigDecimal overallCosts) {
        this.grossAmount = overallCosts;
    }

    /**
     * Returns the VAT the customer has to pay.
     * 
     * @return the VAT
     */
    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    /**
     * Sets the VAT the customer has to pay.
     * 
     * @param vat
     *            the VAT
     */
    public void setVatAmount(BigDecimal vat) {
        this.vatAmount = vat;
    }

    /**
     * Returns the net discount granted to the customer.
     * 
     * @return the net discount
     */
    public BigDecimal getNetDiscount() {
        return netDiscount;
    }

    /**
     * Sets the net discount granted to the customer.
     * 
     * @param netDiscount
     *            the net discount
     */
    public void setNetDiscount(BigDecimal netDiscount) {
        this.netDiscount = netDiscount;
    }

    /**
     * Returns the numeric key of the supplier or reseller organization the
     * customer is related to.
     * 
     * @return the key
     */
    public Long getSellerKey() {
        return sellerKey;
    }

    /**
     * Sets the numeric key of the supplier or reseller organization the
     * customer is related to.
     * 
     * @param sellerKey
     *            the key
     */
    public void setSellerKey(Long sellerKey) {
        this.sellerKey = sellerKey;
    }

    /**
     * Returns the VAT rate used for charging the customer.
     * 
     * @return the VAT rate
     */
    public String getVat() {
        return vat;
    }

    /**
     * Sets the VAT rate used for charging the customer.
     * 
     * @param vatPercent
     *            the VAT rate
     */
    public void setVat(String vatPercent) {
        this.vat = vatPercent;
    }

    /**
     * Returns the currency used for charging the customer.
     * 
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency used for charging the customer.
     * 
     * @param currency
     *            the currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Returns the identifier provided by the PSP for the applicable payment
     * information of the customer to be charged.
     * 
     * @return the identifier
     */
    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    /**
     * Sets the identifier provided by the PSP for the applicable payment
     * information of the customer to be charged.
     * 
     * @param externalIdentifier
     *            the identifier
     */
    public void setExternalIdentifier(String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    /**
     * Returns the postal address of the customer organization.
     * 
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the postal address of the customer organization.
     * 
     * @param address
     *            the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the email address of the customer organization.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the customer organization.
     * 
     * @param email
     *            the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the start time of the period for which the customer is to be
     * charged.
     * 
     * @return the start time
     */
    public Date getPeriodStartTime() {
        return periodStartTime;
    }

    /**
     * Sets the start time of the period for which the customer is to be
     * charged.
     * 
     * @param periodStartTime
     *            the start time
     */
    public void setPeriodStartTime(Date periodStartTime) {
        this.periodStartTime = periodStartTime;
    }

    /**
     * Returns the end time of the period for which the customer is to be
     * charged.
     * 
     * @return the end time
     */
    public Date getPeriodEndTime() {
        return periodEndTime;
    }

    /**
     * Sets the end time of the period for which the customer is to be charged.
     * 
     * @param periodEndTime
     *            the end time
     */
    public void setPeriodEndTime(Date periodEndTime) {
        this.periodEndTime = periodEndTime;
    }

    /**
     * Returns the numeric key of the customer organization.
     * 
     * @return the organization key
     */
    public Long getCustomerKey() {
        return customerKey;
    }

    /**
     * Sets the numeric key of the customer organization.
     * 
     * @param customerKey
     *            the organization key
     */
    public void setCustomerKey(Long customerKey) {
        this.customerKey = customerKey;
    }

    /**
     * Returns the identifier of the service subscription for which the customer
     * is to be charged.
     * 
     * @return the identifier
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the identifier of the service subscription for which the customer is
     * to be charged.
     * 
     * @param subscriptionId
     *            the identifier
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Returns the reference number the customer specified for the service
     * subscription.
     * 
     * @return the reference number
     */
    public String getPon() {
        return pon;
    }

    /**
     * Sets the reference number the customer specified for the service
     * subscription.
     * 
     * @param pon
     *            the reference number
     */
    public void setPon(String pon) {
        this.pon = pon;
    }

    /**
     * Returns the identifier of the transaction.
     * 
     * @return the identifier
     */
    public long getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the identifier for the transaction.
     * 
     * @param transactionId
     *            the identifier
     */
    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Returns the net amount the customer has to pay.
     * 
     * @return the net amount
     */
    public BigDecimal getNetAmount() {
        return netAmount;
    }

    /**
     * Sets the net amount the customer has to pay.
     * 
     * @param netAmount
     *            the net amount
     */
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    /**
     * Returns the billing data for the price models of the individual
     * subscriptions covered by the current charging request. One price model
     * data object corresponds to one position of the invoice sent to the
     * customer.
     * 
     * @return the price model data
     */
    public List<PriceModelData> getPriceModelData() {
        return priceModelData;
    }

    /**
     * Sets the billing data for the price models of the individual
     * subscriptions covered by the current charging request. One price model
     * data object corresponds to one position of the invoice sent to the
     * customer.
     * 
     * @param priceModelData
     *            the price model data
     */
    public void setPriceModelData(List<PriceModelData> priceModelData) {
        this.priceModelData = priceModelData;
    }

}
