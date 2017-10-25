/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-19                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

import java.io.Serializable;

/**
 * Provides the response of a payment service provider (PSP) to a payment
 * information registration request in the format required by the platform.
 * <p>
 * For the registration of new payment information, the
 * <code>paymentInfoId</code>, <code>organizationKey</code>, and
 * <code>paymentTypeKey</code> must be set. For an update of exiting payment
 * information, the <code>paymentInfoKey</code> must be set. This information is
 * passed to the PSP integration adapter with the corresponding platform request
 * (<code>determineRegistrationLink</code> or
 * <code>determineReregistrationLink</code>). If supported by the PSP, the
 * information can be forwarded to the PSP and returned with the response, or it
 * can be handled within the PSP integration adapter itself. In any case, the
 * information must be passed back unchanged to the platform via the callback
 * component in the response to the registration request.
 */
public class RegistrationData implements Serializable {

    private static final long serialVersionUID = 3345412340521096092L;

    private String accountNumber;
    private String identification;
    private String provider;
    private long paymentInfoKey;
    private String paymentInfoId;
    private long organizationKey;
    private long paymentTypeKey;
    private Status status = Status.Success;

    /**
     * Enumeration of the possible results of the registration at the PSP.
     * 
     */
    public enum Status {
        Success, Failure, Canceled;
    }

    /**
     * Returns the account or card number set for the payment information. This
     * setting depends on the payment type. For payment by credit card, it is
     * typically the credit card number. For direct debit, it usually is the
     * account number.
     * 
     * @return the account or card number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the account or card number for the payment information. This setting
     * depends on the payment type. For payment by credit card, it is typically
     * the credit card number. For direct debit, it usually is the account
     * number.
     * 
     * @param accountNumber
     *            the account or card number
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Returns the identifier provided by the PSP for the payment information.
     * 
     * @return the identifier
     */
    public String getIdentification() {
        return identification;
    }

    /**
     * Sets the identifier provided by the PSP for the payment information.
     * 
     * @param identification
     *            the identifier
     */
    public void setIdentification(String identification) {
        this.identification = identification;
    }

    /**
     * Returns the provider set for the payment information. This setting
     * depends on the payment type. For payment by credit card, it is typically
     * the credit card provider. For direct debit, it could be the name or
     * routing code of the bank.
     * 
     * @return the provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Sets the provider for the payment information. This setting depends on
     * the payment type. For payment by credit card, it is typically the credit
     * card provider. For direct debit, it could be the name or routing code of
     * the bank.
     * 
     * @param provider
     *            the provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Returns the numeric key of the payment information object stored in the
     * platform. Note that this is not available with the first registration.
     * 
     * @return the payment information key
     */
    public long getPaymentInfoKey() {
        return paymentInfoKey;
    }

    /**
     * Sets the numeric key of the payment information object stored in the
     * platform. Note that this is not available with the first registration.
     * 
     * @param paymentInfoKey
     *            the payment information key
     */
    public void setPaymentInfoKey(long paymentInfoKey) {
        this.paymentInfoKey = paymentInfoKey;
    }

    /**
     * Returns the identifier of the payment information as specified by the
     * customer.
     * 
     * @return the payment information ID
     */
    public String getPaymentInfoId() {
        return paymentInfoId;
    }

    /**
     * Sets the identifier of the payment information as specified by the
     * customer.
     * 
     * @param paymentInfoId
     *            the payment information ID
     */
    public void setPaymentInfoId(String paymentInfoId) {
        this.paymentInfoId = paymentInfoId;
    }

    /**
     * Returns the numeric key of the customer organization.
     * 
     * @return the organization key
     */
    public long getOrganizationKey() {
        return organizationKey;
    }

    /**
     * Sets the numeric key of the customer organization.
     * 
     * @param organizationKey
     *            the organization key
     */
    public void setOrganizationKey(long organizationKey) {
        this.organizationKey = organizationKey;
    }

    /**
     * Returns the numeric key of the payment type selected by the customer.
     * 
     * @return the payment type key
     */
    public long getPaymentTypeKey() {
        return paymentTypeKey;
    }

    /**
     * Sets the numeric key of the payment type selected by the customer.
     * 
     * @param paymentTypeKey
     *            the payment type key
     */
    public void setPaymentTypeKey(long paymentTypeKey) {
        this.paymentTypeKey = paymentTypeKey;
    }

    /**
     * Returns the result of the registration at the PSP.
     * 
     * @return the result
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the result of the registration at the PSP.
     * 
     * @param status
     *            the result
     */
    public void setStatus(Status status) {
        this.status = status;
    }

}
