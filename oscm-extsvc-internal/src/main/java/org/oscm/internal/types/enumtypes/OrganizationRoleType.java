/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-08-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Specifies the roles an organization can have.
 * 
 */
public enum OrganizationRoleType {

    /**
     * Marketplace owner.
     */
    MARKETPLACE_OWNER,

    /**
     * Platform operator.
     */
    PLATFORM_OPERATOR,

    /**
     * Technology provider.
     */
    TECHNOLOGY_PROVIDER,

    /**
     * Supplier.
     */
    SUPPLIER,

    /**
     * Customer.
     */
    CUSTOMER,

    /**
     * Broker.
     */
    BROKER,

    /**
     * Reseller.
     */
    RESELLER;

    /**
     * Returns the default user role which is assigned to users in an
     * organization with the current organization role.
     * <p>
     * The default user roles are the following:
     * <ul>
     * <li><code>PLATFORM_OPERATOR</code> organization:
     * <code>PLATFORM_OPERATOR</code>
     * <li>
     * <code>TECHNOLOGY_PROVIDER</code> organization:
     * <code>TECHNOLOGY_MANAGER</code>
     * <li><code>SUPPLIER</code> organization: <code>SERVICE_MANAGER</code>
     * <li><code>MARKETPLACE_OWNER</code> organization:
     * <code>MARKETPLACE_OWNER</code>
     * <li><code>BROKER</code> organization: <code>BROKER_MANAGER</code>
     * <li><code>RESELLER</code> organization: <code>RESELLER_MANAGER</code>
     * <li><code>CUSTOMER</code> organization: <code>null</code>
     * </ul>
     * 
     * @return the user role
     */
    public UserRoleType correspondingUserRole() {
        switch (this) {
        case PLATFORM_OPERATOR:
            return UserRoleType.PLATFORM_OPERATOR;
        case TECHNOLOGY_PROVIDER:
            return UserRoleType.TECHNOLOGY_MANAGER;
        case SUPPLIER:
            return UserRoleType.SERVICE_MANAGER;
        case MARKETPLACE_OWNER:
            return UserRoleType.MARKETPLACE_OWNER;
        case BROKER:
            return UserRoleType.BROKER_MANAGER;
        case RESELLER:
            return UserRoleType.RESELLER_MANAGER;
        default:
            return null;
        }
    }

    /**
     * Returns the default user roles for the given collection of organization
     * roles. The customer organization role is ignored, as there is no
     * equivalent default user role.
     * <p>
     * The default user roles for the individual organization roles are the
     * following:
     * <ul>
     * <li><code>PLATFORM_OPERATOR</code> organization:
     * <code>PLATFORM_OPERATOR</code>
     * <li>
     * <code>TECHNOLOGY_PROVIDER</code> organization:
     * <code>TECHNOLOGY_MANAGER</code>
     * <li><code>SUPPLIER</code> organization: <code>SERVICE_MANAGER</code>
     * <li><code>MARKETPLACE_OWNER</code> organization:
     * <code>MARKETPLACE_OWNER</code>
     * <li><code>BROKER</code> organization: <code>BROKER_MANAGER</code>
     * <li><code>RESELLER</code> organization: <code>RESELLER_MANAGER</code>
     * <li><code>CUSTOMER</code> organization: <code>null</code>
     * </ul>
     * 
     * @param orgRoles
     *            the organization roles for which to return the user roles
     * @return the user roles
     */
    public static Set<UserRoleType> correspondingUserRoles(
            Collection<OrganizationRoleType> orgRoles) {
        Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
        for (OrganizationRoleType orgRole : orgRoles) {
            UserRoleType userRole = orgRole.correspondingUserRole();
            if (userRole != null) {
                userRoles.add(userRole);
            }
        }
        return userRoles;
    }

    /**
     * Returns the organization role which is required to assign the given user
     * role to a user within the organization.
     * <p>
     * The organization roles required for the individual user roles are the
     * following:
     * <ul>
     * <li><code>PLATFORM_OPERATOR</code> user: <code>PLATFORM_OPERATOR</code>
     * <li>
     * <code>TECHNOLOGY_MANAGER</code> user: <code>TECHNOLOGY_PROVIDER</code>
     * <li><code>SERVICE_MANAGER</code> user: <code>SUPPLIER</code>
     * <li><code>MARKETPLACE_OWNER</code> user: <code>MARKETPLACE_OWNER</code>
     * <li><code>BROKER_MANAGER</code> user: <code>BROKER</code>
     * <li><code>RESELLER_MANAGER</code> user: <code>RESELLER</code>
     * </ul>
     * 
     * The <code>ORGANIZATION_ADMIN</code> and <code>SUBSCRIPTION_MANAGER</code>
     * user roles are not related to a specific organization role. They can be
     * assigned to a user within any organization.
     * 
     * @param userRole
     *            the user role for which to return the organization role
     * @return the organization role
     */
    public static OrganizationRoleType correspondingOrgRoleForUserRole(
            UserRoleType userRole) {
        switch (userRole) {
        case PLATFORM_OPERATOR:
            return PLATFORM_OPERATOR;
        case TECHNOLOGY_MANAGER:
            return TECHNOLOGY_PROVIDER;
        case SERVICE_MANAGER:
            return SUPPLIER;
        case MARKETPLACE_OWNER:
            return MARKETPLACE_OWNER;
        case BROKER_MANAGER:
            return BROKER;
        case RESELLER_MANAGER:
            return RESELLER;
        default:
            return null;
        }
    }
}
