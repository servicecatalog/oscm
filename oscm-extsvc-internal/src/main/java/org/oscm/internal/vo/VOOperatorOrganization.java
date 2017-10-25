/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-02-02                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.vo;

import java.util.ArrayList;
import java.util.List;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPaymentType;

/**
 * Represents a platform operator organization and provides data required for
 * such an organization.
 * 
 */
public class VOOperatorOrganization extends VOOrganization {

    private static final long serialVersionUID = 3372778007655456804L;

    private List<OrganizationRoleType> organizationRoles = new ArrayList<OrganizationRoleType>();
    private List<VOPaymentType> paymentTypes = new ArrayList<VOPaymentType>();

    /**
     * Retrieves the roles that can be assigned to another organization by the
     * operator organization.
     * 
     * @return the organization roles
     */
    public List<OrganizationRoleType> getOrganizationRoles() {
        return organizationRoles;
    }

    /**
     * Sets the roles that can be assigned to another organization by the
     * operator organization.
     * 
     * @param organizationRoles
     *            the organization roles
     */
    public void setOrganizationRoles(
            List<OrganizationRoleType> organizationRoles) {
        this.organizationRoles = organizationRoles;
    }

    /**
     * Retrieves the payment types that can be made available to suppliers by
     * the operator organization.
     * 
     * @return the payment types
     */
    public List<VOPaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    /**
     * Sets the payment types that can be made available to suppliers by the
     * operator organization.
     * 
     * @param paymentTypes
     *            the payment types
     */
    public void setPaymentTypes(List<VOPaymentType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

}
