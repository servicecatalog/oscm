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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * PaymentInfo is the base class for the payment methods provided by the
 * platform. Subclasses of PaymentInfo will represent a certain payment method
 * (e.g. direct debit, credit card etc.). Each subscription holds (for the
 * moment one specific) PaymentInfo to be used for billing.
 * 
 * @author schmid
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "organization_tkey", "paymentInfoId" }))
@NamedQueries({ @NamedQuery(name = "PaymentInfo.findByBusinessKey", query = "select pi from PaymentInfo pi where pi.dataContainer.paymentInfoId=:paymentInfoId and pi.organization_tkey=:organization_tkey") })
@BusinessKey(attributes = { "paymentInfoId", "organization_tkey" })
public class PaymentInfo extends DomainObjectWithHistory<PaymentInfoData> {

    private static final long serialVersionUID = 1L;

    public PaymentInfo() {
        dataContainer = new PaymentInfoData();
    }

    public PaymentInfo(long creationTime) {
        this();
        setCreationTime(creationTime);
    }

    @OneToMany(mappedBy = "paymentInfo", fetch = FetchType.LAZY)
    private Set<Subscription> subscriptions = new HashSet<Subscription>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PaymentType paymentType;

    @Column(name = "organization_tkey", insertable = false, updatable = false, nullable = false)
    private long organization_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_tkey")
    private Organization organization;

    /**
     * Refer to {@link PaymentInfoData#externalIdentifier}
     */
    public String getExternalIdentifier() {
        return dataContainer.getExternalIdentifier();
    }

    /**
     * Refer to {@link PaymentInfoData#externalIdentifier}
     */
    public void setExternalIdentifier(String identifier) {
        dataContainer.setExternalIdentifier(identifier);
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    protected void setCreationTime(long creationTime) {
        dataContainer.setCreationTime(creationTime);
    }

    protected long getCreationTime() {
        return dataContainer.getCreationTime();
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null) {
            setOrganization_tkey(organization.getKey());
        }
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getPaymentInfoId() {
        return dataContainer.getPaymentInfoId();
    }

    public void setPaymentInfoId(String paymentInfoId) {
        dataContainer.setPaymentInfoId(paymentInfoId);
    }

    public String getProviderName() {
        return dataContainer.getProviderName();
    }

    public void setProviderName(String providerName) {
        dataContainer.setProviderName(providerName);
    }

    public String getAccountNumber() {
        return dataContainer.getAccountNumber();
    }

    public void setAccountNumber(String accountNumber) {
        dataContainer.setAccountNumber(accountNumber);
    }

    public void setOrganization_tkey(long organization_tkey) {
        this.organization_tkey = organization_tkey;
    }

    public long getOrganization_tkey() {
        return organization_tkey;
    }

}
