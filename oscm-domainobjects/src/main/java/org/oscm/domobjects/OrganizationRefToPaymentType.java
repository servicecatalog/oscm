/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Relation entity for saving the configured available and default payment types
 * for customers and suppliers. The organization role is used to specify for
 * which role of the organization the payment types are referenced. E. g. an
 * organization can be supplier and customer and for both roles there should be
 * different sets of available payment types.
 * 
 * @author weiser
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "ORGANIZATIONREFERENCE_TKEY", "PAYMENTTYPE_TKEY" }))
public class OrganizationRefToPaymentType extends
        DomainObjectWithVersioning<OrganizationRefToPaymentTypeData> {

    private static final long serialVersionUID = 1270445836927142522L;

    /**
     * The organization a payment type is available for
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationReference organizationReference;

    /**
     * The available payment type
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PaymentType paymentType;

    /**
     * The role context for the organization
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationRole organizationRole;

    public OrganizationRefToPaymentType() {
        super();
        dataContainer = new OrganizationRefToPaymentTypeData();
    }

    public OrganizationReference getOrganizationReference() {
        return organizationReference;
    }

    public void setOrganizationReference(
            OrganizationReference organizationReference) {
        this.organizationReference = organizationReference;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public boolean isUsedAsDefault() {
        return dataContainer.isUsedAsDefault();
    }

    public void setUsedAsDefault(boolean usedAsDefault) {
        dataContainer.setUsedAsDefault(usedAsDefault);
    }

    public boolean isUsedAsServiceDefault() {
        return dataContainer.isUsedAsServiceDefault();
    }

    public void setUsedAsServiceDefault(boolean usedAsServiceDefault) {
        dataContainer.setUsedAsServiceDefault(usedAsServiceDefault);
    }

    public void setOrganizationRole(OrganizationRole organizationRole) {
        this.organizationRole = organizationRole;
    }

    public OrganizationRole getOrganizationRole() {
        return organizationRole;
    }

    /**
     * Returns the organization this payment type is allowed for.
     * 
     * @return The organization that is allowed to use this payment type.
     */
    public Organization getAffectedOrganization() {
        return organizationReference.getTarget();
    }

    /**
     * Returns the organization that enabled this payment type.
     * 
     * @return The defining organization.
     */
    public Organization getDefiningOrganization() {
        return organizationReference.getSource();
    }
}
