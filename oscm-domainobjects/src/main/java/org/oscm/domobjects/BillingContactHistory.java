/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * @author weiser
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "BillingContactHistory.findByObject", query = "select c from BillingContactHistory c where c.objKey=:objKey order by objversion"),
        @NamedQuery(name = "BillingContactHistory.findForOrganizationKeyDescencing", query = "SELECT c FROM BillingContactHistory c ORDER BY objversion DESC") })
public class BillingContactHistory extends
        DomainHistoryObject<BillingContactData> {

    private static final long serialVersionUID = 4152600673062431998L;

    /**
     * The technical key of the organization this billing contact belongs to.
     */
    private long organizationObjKey;

    public BillingContactHistory() {
        super();
        dataContainer = new BillingContactData();
    }

    public BillingContactHistory(BillingContact billingContact) {
        super(billingContact);
        if (billingContact.getOrganization() != null) {
            organizationObjKey = billingContact.getOrganization().getKey();
        }
    }

    public boolean isOrgAddressUsed() {
        return dataContainer.isOrgAddressUsed();
    }

    public String getCompanyName() {
        return dataContainer.getCompanyName();
    }

    public String getAddress() {
        return dataContainer.getAddress();
    }

    public String getEmail() {
        return dataContainer.getEmail();
    }

    public long getOrganizationObjKey() {
        return organizationObjKey;
    }

    public void setOrganizationObjKey(long organizationObjKey) {
        this.organizationObjKey = organizationObjKey;
    }

}
