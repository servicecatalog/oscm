/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author weiser
 * 
 */
@Embeddable
public class BillingContactData extends DomainDataContainer {

    private static final long serialVersionUID = -3200195731711309039L;

    @Column(nullable = false)
    private String email;

    private String companyName;

    private String address;

    @Column(nullable = false)
    private boolean orgAddressUsed;

    @Column(nullable = false)
    private String billingContactId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isOrgAddressUsed() {
        return orgAddressUsed;
    }

    public void setOrgAddressUsed(boolean orgAddressUsed) {
        this.orgAddressUsed = orgAddressUsed;
    }

    public void setBillingContactId(String billingContactId) {
        this.billingContactId = billingContactId;
    }

    public String getBillingContactId() {
        return billingContactId;
    }
}
