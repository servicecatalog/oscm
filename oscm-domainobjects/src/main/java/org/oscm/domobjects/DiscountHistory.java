/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                 
 *                                                                              
 *  Creation Date: 19.05.2010                                                     
 *                                                                              
 *  Completion Time: 19.05.2010                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
        @NamedQuery(name = "DiscountHistory.findByObject", query = "select c from DiscountHistory c where c.objKey=:objKey order by objversion"),
        // TODO this query might be not correct as the organization reference
        // existed in a different time than the discount
        @NamedQuery(name = "DiscountHistory.findForOrganizationAndPeriod", query = "SELECT c FROM DiscountHistory c, OrganizationReferenceHistory refH "
                + "WHERE c.organizationReferenceObjKey = refH.objKey AND refH.targetObjKey =:orgKey AND refH.sourceObjKey =:supplierKey "
                + "AND c.objVersion = (SELECT max(innerC.objVersion) FROM DiscountHistory innerC where c.objKey = "
            + "innerC.objKey) "
                + "AND ((c.dataContainer.endTime >=:bS AND c.dataContainer.endTime <= :bE) OR (c.dataContainer.startTime >=:bS AND c.dataContainer.startTime <= :bE) "
                + "OR (c.dataContainer.startTime <=:bS AND c.dataContainer.endTime >= :bE) OR (c.dataContainer.startTime <=:bE AND c.dataContainer.endTime IS NULL)) "
                + "ORDER BY c.objVersion DESC, c.modDate DESC") })
public class DiscountHistory extends DomainHistoryObject<DiscountData> {

    /**
     * ID.
     */
    private static final long serialVersionUID = 8214373677660508331L;

    /**
     * Organization key.
     */
    private long organizationReferenceObjKey;

    /**
     * Default constructor.
     */
    public DiscountHistory() {
        super();
        dataContainer = new DiscountData();
    }

    /**
     * Parameterized constructor.
     * 
     * @param discount
     */
    public DiscountHistory(Discount discount) {
        super(discount);
        if (discount.getOrganizationReference() != null) {
            setOrganizationReferenceObjKey(discount.getOrganizationReference()
                    .getKey());
        }
    }

    /**
     * Setter for organization.
     * 
     * @param organizationObjKey
     */
    public void setOrganizationReferenceObjKey(long organizationReferenceObjKey) {
        this.organizationReferenceObjKey = organizationReferenceObjKey;
    }

    /**
     * Getter for organization.
     * 
     * @return Organization key.
     */
    public long getOrganizationReferenceObjKey() {
        return organizationReferenceObjKey;
    }

    /**
     * Setting discount value.
     * 
     * @param value
     */
    public void setValue(BigDecimal value) {
        dataContainer.setValue(value);
    }

    /**
     * Getting discount value.
     * 
     * @return Discount value.
     */
    public BigDecimal getValue() {
        return dataContainer.getValue();
    }

    /**
     * Setting start date of discount.
     * 
     * @param startTime
     */
    public void setStartTime(Long startTime) {
        dataContainer.setStartTime(startTime);
    }

    /**
     * Getting start time.
     * 
     * @return Start time for the discount,
     */
    public Long getStartTime() {
        return dataContainer.getStartTime();
    }

    /**
     * Setting end time of discount.
     * 
     * @param endTime
     */
    public void setEndTime(Long endTime) {
        dataContainer.setEndTime(endTime);
    }

    /**
     * Getting end time of discount.
     * 
     * @return End time for the discount,
     */
    public Long getEndTime() {
        return dataContainer.getEndTime();
    }
}
