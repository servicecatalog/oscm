/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 15.07.2011                                                      
 *                                                                              
 *  Completion Time: 15.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;

/**
 * @author weiser
 * 
 */
public class VOPaymentData implements Serializable {

    private static final long serialVersionUID = 7069711557729772771L;

    private String accountNumber;
    private String identification;
    private String paymentInfoId;
    private long paymentInfoKey;
    private long paymentTypeKey;
    private long organizationKey;
    private String provider;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public long getPaymentInfoKey() {
        return paymentInfoKey;
    }

    public void setPaymentInfoKey(long paymentInfoKey) {
        this.paymentInfoKey = paymentInfoKey;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return the paymentInfoId
     */
    public String getPaymentInfoId() {
        return paymentInfoId;
    }

    /**
     * @param paymentInfoId
     *            the paymentInfoId to set
     */
    public void setPaymentInfoId(String paymentInfoId) {
        this.paymentInfoId = paymentInfoId;
    }

    /**
     * @return the paymentTypeKey
     */
    public long getPaymentTypeKey() {
        return paymentTypeKey;
    }

    /**
     * @param paymentTypeKey
     *            the paymentTypeKey to set
     */
    public void setPaymentTypeKey(long paymentTypeKey) {
        this.paymentTypeKey = paymentTypeKey;
    }

    /**
     * @return the organizationKey
     */
    public long getOrganizationKey() {
        return organizationKey;
    }

    /**
     * @param organizationKey
     *            the organizationKey to set
     */
    public void setOrganizationKey(long organizationKey) {
        this.organizationKey = organizationKey;
    }

}
