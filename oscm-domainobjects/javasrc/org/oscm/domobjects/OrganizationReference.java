/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.OrganizationReferenceType;

/**
 * A technology provider can provide his technical product to several supplier
 * and a supplier can sell the products of different technology providers.
 * Technology provider and supplier are special organizations, so we have an
 * many to many relation between organizations. The default JPA managed approach
 * creates a join table, but does not take care of historization of the objects.
 * 
 * @author pock
 * 
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "OrganizationReference.findByBusinessKey", query = "SELECT c FROM OrganizationReference c WHERE c.sourceKey=:sourceKey AND c.targetKey=:targetKey AND c.dataContainer.referenceType = :referenceType"),
        @NamedQuery(name = "OrganizationReference.findOrganizationForDiscountEndNotification", query = "SELECT r FROM OrganizationReference r WHERE r.dataContainer.referenceType = org.oscm.domobjects.enums.OrganizationReferenceType.SUPPLIER_TO_CUSTOMER AND r.discount.dataContainer.endTime >= :firstMillis AND r.discount.dataContainer.endTime <= :lastMillis"),
        @NamedQuery(name = "OrganizationReference.getObsolete", query = "SELECT orgRef FROM OrganizationReference orgRef WHERE 0 = (SELECT COUNT(mp.key) FROM MarketingPermission mp WHERE mp.organizationReference.key = orgRef.key) AND orgRef.key IN (:refKeys)") })
@BusinessKey(attributes = { "sourceKey", "targetKey", "referenceType" })
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "sourceKey",
        "targetKey", "referenceType" }))
public class OrganizationReference extends
        DomainObjectWithHistory<OrganizationReferenceData> {

    private static final long serialVersionUID = -8910467535964023567L;

    @Column(name = "sourceKey", insertable = false, updatable = false, nullable = false)
    private long sourceKey;

    /**
     * Refers to the source organization of the reference.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sourceKey")
    private Organization source;

    @Column(name = "targetKey", insertable = false, updatable = false, nullable = false)
    private long targetKey;

    /**
     * Refers to the target organization of the reference.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "targetKey")
    private Organization target;

    @OneToOne(mappedBy = "organizationReference", cascade = CascadeType.ALL, optional = true, fetch = FetchType.LAZY)
    private Discount discount;

    @OneToMany(mappedBy = "organizationReference", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<OrganizationRefToPaymentType> paymentTypes = new ArrayList<OrganizationRefToPaymentType>();

    protected OrganizationReference() {
        dataContainer = new OrganizationReferenceData();
    }

    /**
     * Creates a reference between to organizations. The source organization
     * must refer to target organization otherwise an
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param referenceType
     *            the type of the organization reference TODO
     * @param technologyProvider
     *            The source organization of the reference.
     * @param supplier
     *            The target organization of the reference.
     */
    public OrganizationReference(Organization source, Organization target,
            OrganizationReferenceType referenceType) {
        this();
        this.source = source;
        this.target = target;
        sourceKey = source.getKey();
        targetKey = target.getKey();
        setReferenceType(referenceType);
    }

    public Organization getSource() {
        return source;
    }

    public Organization getTarget() {
        return target;
    }

    public long getSourceKey() {
        return sourceKey;
    }

    public long getTargetKey() {
        return targetKey;
    }

    public void setSourceKey(long sourceKey) {
        this.sourceKey = sourceKey;
    }

    public void setTargetKey(long targetKey) {
        this.targetKey = targetKey;
    }

    public void setReferenceType(OrganizationReferenceType referenceType) {
        dataContainer.setReferenceType(referenceType);
    }

    public OrganizationReferenceType getReferenceType() {
        return dataContainer.getReferenceType();
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }

    public List<OrganizationRefToPaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(List<OrganizationRefToPaymentType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    /**
     * Determines all payment references that have the specified payment type.
     * 
     * @param paymentType
     *            The payment type to filter the references for. Must not be
     *            <code>null</code>.
     * @return The available payment reference for the specified type.
     *         <code>null</code> in case none is found.
     */
    public OrganizationRefToPaymentType getPaymentReferenceForType(
            String paymentType) {
        for (OrganizationRefToPaymentType ortpt : getPaymentTypes()) {
            if (paymentType.equals(ortpt.getPaymentType().getPaymentTypeId())) {
                return ortpt;
            }
        }
        return null;
    }

}
