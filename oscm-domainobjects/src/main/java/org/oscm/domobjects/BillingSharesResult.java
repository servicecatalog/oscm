/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.internal.types.enumtypes.BillingSharesResultType;

@Entity
@NamedQueries({
        @NamedQuery(name = "BillingSharesResult.getSharesResultForOrganization", query = ""
                + " SELECT br FROM BillingSharesResult br"
                + "  WHERE br.dataContainer.organizationTKey = :orgKey"
                + "    AND br.dataContainer.resultType = :resultType"
                + "    AND br.dataContainer.periodStartTime >= :fromDate"
                + "    AND br.dataContainer.periodStartTime < :toDate"
                + " ORDER BY br.dataContainer.periodStartTime ASC"),
        @NamedQuery(name = "BillingSharesResult.getSharesResult", query = ""
                + " SELECT br FROM BillingSharesResult br"
                + "  WHERE br.dataContainer.resultType = :resultType"
                + "    AND br.dataContainer.periodStartTime >= :fromDate"
                + "    AND br.dataContainer.periodStartTime < :toDate"
                + " ORDER BY br.dataContainer.periodStartTime ASC") })
public class BillingSharesResult extends
        DomainObjectWithVersioning<BillingSharesResultData> {

    private static final long serialVersionUID = 7590756533796173272L;

    public BillingSharesResult() {
        super();
        dataContainer = new BillingSharesResultData();
    }

    public void setCreationTime(long creationTime) {
        dataContainer.setCreationTime(creationTime);
    }

    public long getCreationTime() {
        return dataContainer.getCreationTime();
    }

    public long getPeriodStartTime() {
        return dataContainer.getPeriodStartTime();
    }

    public void setPeriodStartTime(long periodStartTime) {
        dataContainer.setPeriodStartTime(periodStartTime);
    }

    public long getPeriodEndTime() {
        return dataContainer.getPeriodEndTime();
    }

    public void setPeriodEndTime(long periodEndTime) {
        dataContainer.setPeriodEndTime(periodEndTime);
    }

    // FIXME rename to aggregationTKey which is tkey of organization for
    // reseller,broker,supplier and tkey of marketplace for marketplace revenue
    // shares
    public long getOrganizationTKey() {
        return dataContainer.getOrganizationTKey();
    }

    public void setOrganizationTKey(long organizationTKey) {
        dataContainer.setOrganizationTKey(organizationTKey);
    }

    public BillingSharesResultType getResultType() {
        return dataContainer.getResultType();
    }

    public void setResultType(BillingSharesResultType billingSharesResultType) {
        dataContainer.setResultType(billingSharesResultType);
    }

    public String getResultXML() {
        return dataContainer.getResultXML();
    }

    public void setResultXML(String resultXML) {
        dataContainer.setResultXML(resultXML);
    }
}
