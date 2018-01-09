/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
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
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Domain object for table DISCOUNT.
 * 
 * @author Aleh Khomich.
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "ORGANIZATIONREFERENCE_TKEY" }))
@NamedQueries({
        @NamedQuery(name = "Discount.findForOrganizationAndPeriod", query = "SELECT c FROM Discount c WHERE c.organizationReference.target = :organization AND c.organizationReference.dataContainer.referenceType = org.oscm.domobjects.enums.OrganizationReferenceType.SUPPLIER_TO_CUSTOMER AND ((c.dataContainer.endTime >=:bS AND c.dataContainer.endTime <= :bE) OR (c.dataContainer.startTime >=:bS AND c.dataContainer.startTime <= :bE) OR (c.dataContainer.startTime <=:bS AND c.dataContainer.endTime >= :bS AND c.dataContainer.startTime <=:bE AND c.dataContainer.endTime >= :bE))"),
        @NamedQuery(name = "Discount.findForOrganizationAndSupplier", query = "SELECT c FROM Discount c WHERE c.organizationReference.target = :organization AND c.organizationReference.dataContainer.referenceType = org.oscm.domobjects.enums.OrganizationReferenceType.SUPPLIER_TO_CUSTOMER AND c.organizationReference.source = :supplier") })
public class Discount extends DomainObjectWithHistory<DiscountData> {

    /**
     * ID.
     */
    private static final long serialVersionUID = 6373227371951477776L;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationReference organizationReference;

    /**
     * Default constructor.
     */
    public Discount() {
        super();
        dataContainer = new DiscountData();
    }

    /**
     * Setter for organization.
     * 
     * @param organizationReference
     */
    public void setOrganizationReference(
            OrganizationReference organizationReference) {
        this.organizationReference = organizationReference;
    }

    /**
     * Getter for organization.
     * 
     * @return Organization.
     */
    public OrganizationReference getOrganizationReference() {
        return organizationReference;
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
     * @return Start time for the discount.
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
