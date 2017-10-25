/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 12.10.2010                                                      
 *                                                                              
 *  Completion Time: 12.10.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import java.util.Collections;
import java.util.Set;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * The enumeration defining the possible target types for UDAs (entities where
 * UDAs can be defined for).
 * 
 * @author weiser
 * 
 */
public enum UdaTargetType {

    /**
     * A supplier can define UDAs for his customers
     */
    CUSTOMER(Collections.singleton(OrganizationRoleType.SUPPLIER)),

    /**
     * A supplier can define UDAs for his customers' subscriptions
     */
    CUSTOMER_SUBSCRIPTION(Collections.singleton(OrganizationRoleType.SUPPLIER));

    private Set<OrganizationRoleType> roles;

    /**
     * @param allowedRoles
     *            the roles an organization must have to define UDAs of this
     *            type.
     */
    private UdaTargetType(Set<OrganizationRoleType> allowedRoles) {
        roles = allowedRoles;
    }

    public Set<OrganizationRoleType> getRoles() {
        return roles;
    }

    /**
     * Checks if an organization with roles of the passed
     * {@link OrganizationRoleType}s can create/save UDA definition of this
     * {@link UdaTargetType}.
     * 
     * @param types
     *            the role types of the organization
     * @return <code>true</code> if at least one of the required roles is
     *         passed, <code>false</code> otherwise
     */
    public boolean canSaveDefinition(Set<OrganizationRoleType> types) {
        for (OrganizationRoleType type : types) {
            if (roles.contains(type)) {
                return true;
            }
        }
        return false;
    }
}
