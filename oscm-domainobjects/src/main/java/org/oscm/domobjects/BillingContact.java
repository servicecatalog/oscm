/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
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
 * Holds all information used in the payment process of an subscription.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "organization_tkey", "billingContactId" }))
@NamedQueries({ @NamedQuery(name = "BillingContact.findByBusinessKey", query = "select bc from BillingContact bc where bc.dataContainer.billingContactId=:billingContactId and bc.organization_tkey=:organization_tkey"),
    @NamedQuery(name = "BillingContact.findByOrgAndAddress", query = "select bc from BillingContact bc where bc.dataContainer.address=:address and bc.organization_tkey=:organization_tkey and bc.dataContainer.email=:email and bc.dataContainer.orgAddressUsed='TRUE'")})
@BusinessKey(attributes = { "billingContactId", "organization_tkey" })
public class BillingContact extends DomainObjectWithHistory<BillingContactData> {

    private static final long serialVersionUID = -2152506613278753483L;

    @Column(name = "organization_tkey", insertable = false, updatable = false, nullable = false)
    private long organization_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_tkey")
    private Organization organization;

    @OneToMany(mappedBy = "billingContact", fetch = FetchType.LAZY)
    private Set<Subscription> subscriptions = new HashSet<Subscription>();

    public BillingContact() {
        super();
        dataContainer = new BillingContactData();
    }

    public String getEmail() {
        return dataContainer.getEmail();
    }

    public void setEmail(String email) {
        dataContainer.setEmail(email);
    }

    public String getCompanyName() {
        return dataContainer.getCompanyName();
    }

    public void setCompanyName(String companyName) {
        dataContainer.setCompanyName(companyName);
    }

    public String getAddress() {
        return dataContainer.getAddress();
    }

    public void setAddress(String companyAddress) {
        dataContainer.setAddress(companyAddress);
    }

    public boolean isOrgAddressUsed() {
        return dataContainer.isOrgAddressUsed();
    }

    public void setOrgAddressUsed(boolean orgAddressUsed) {
        dataContainer.setOrgAddressUsed(orgAddressUsed);
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
        if (organization != null) {
            setOrganization_tkey(organization.getKey());
        }
    }

    public void setSubscriptions(Set<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Set<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setBillingContactId(String billingContactId) {
        dataContainer.setBillingContactId(billingContactId);
    }

    public String getBillingContactId() {
        return dataContainer.getBillingContactId();
    }

    public void setOrganization_tkey(long organization_tkey) {
        this.organization_tkey = organization_tkey;
    }

    public long getOrganization_tkey() {
        return organization_tkey;
    }

}
