/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                     
 *                                                                              
 *  Creation Date: 25.02.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects.enums;

import java.util.Set;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author weiser
 */
public enum OrganizationReferenceType {

    TECHNOLOGY_PROVIDER_TO_SUPPLIER,

    SUPPLIER_TO_CUSTOMER,

    PLATFORM_OPERATOR_TO_SUPPLIER,

    /**
     * Allow the paas (source organization) to make API calls in the name of a
     * third party to the target organization.
     */
    ON_BEHALF_ACTING,

    RESELLER_TO_CUSTOMER,

    PLATFORM_OPERATOR_TO_RESELLER,

    BROKER_TO_CUSTOMER;

    /**
     * Get the corresponding OrganizationReferenceType for the given roles of
     * the source organization
     * 
     * @param sourceRoles
     *            the granted roles of the source organization
     * @return
     */
    public static OrganizationReferenceType getOrgRefTypeForSourceRoles(
            Set<OrganizationRoleType> sourceRoles) {
        if (sourceRoles.contains(OrganizationRoleType.SUPPLIER)) {
            return OrganizationReferenceType.SUPPLIER_TO_CUSTOMER;
        } else if (sourceRoles.contains(OrganizationRoleType.RESELLER)) {
            return OrganizationReferenceType.RESELLER_TO_CUSTOMER;
        } else if (sourceRoles.contains(OrganizationRoleType.BROKER)) {
            return OrganizationReferenceType.BROKER_TO_CUSTOMER;
        } else {
            return null;
        }
    }

}
