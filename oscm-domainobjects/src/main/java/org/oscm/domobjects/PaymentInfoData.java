/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * DataContainer for domain object PaymentInfo
 * 
 * @see org.oscm.domobjects.PaymentInfo
 * 
 * @author schmid
 */
@Embeddable
public class PaymentInfoData extends DomainDataContainer implements
        Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The identifier for the organization's payment information in the external
     * system, e.g. heidelpay.
     */
    private String externalIdentifier;

    @Column(nullable = false)
    private long creationTime;

    @Column(nullable = false)
    private String paymentInfoId;

    private String providerName;

    private String accountNumber;

    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
    }

    protected void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    protected long getCreationTime() {
        return creationTime;
    }

    public String getPaymentInfoId() {
        return paymentInfoId;
    }

    public void setPaymentInfoId(String paymentInfoId) {
        this.paymentInfoId = paymentInfoId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

}
