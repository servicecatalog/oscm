/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Enes Sejfi                                                      
 *                                                                              
 *  Creation Date: 09.07.2012                                                      
 *                                                                              
 *  Completion Time: 09.07.2012                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.BSRDConverter;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;

/**
 * Data Container for the BillingSharesResult domain object.
 * 
 * @author Enes Sejfi
 */
@Embeddable
public class BillingSharesResultData extends DomainDataContainer {

    private static final long serialVersionUID = -6564335035061574985L;

    /**
     * The time when the billing shares result was generated.
     */
    @Column(nullable = false)
    private long creationTime;

    /**
     * The time the billing period started at.
     */
    @Column(nullable = false)
    private long periodStartTime;

    /**
     * The time the billing period ended at.
     */
    @Column(nullable = false)
    private long periodEndTime;

    /**
     * The technical key of the organization this billing shares result belongs
     * to. The reference is not a foreign key constraint, but just an additional
     * information (organization might already have been deleted).
     */
    @Column(nullable = false)
    private long organizationTKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingSharesResultType resultType;

    /**
     * The result of the billing shares calculation as XML string.
     */
    @Column(nullable = false)
    private String resultXML;

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getPeriodStartTime() {
        return periodStartTime;
    }

    public void setPeriodStartTime(long periodStartTime) {
        this.periodStartTime = periodStartTime;
    }

    public long getPeriodEndTime() {
        return periodEndTime;
    }

    public void setPeriodEndTime(long periodEndTime) {
        this.periodEndTime = periodEndTime;
    }

    public long getOrganizationTKey() {
        return organizationTKey;
    }

    public void setOrganizationTKey(long organizationTKey) {
        this.organizationTKey = organizationTKey;
    }

    public BillingSharesResultType getResultType() {
        return resultType;
    }

    public void setResultType(BillingSharesResultType resultType) {
        this.resultType = resultType;
    }

    public String getResultXML() {
        return resultXML;
    }

    public void setResultXML(String resultXML) {
        this.resultXML = resultXML;
    }
}
