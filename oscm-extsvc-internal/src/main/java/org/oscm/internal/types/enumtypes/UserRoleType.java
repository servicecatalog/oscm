/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-08-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

import java.io.Serializable;

/**
 * Specifies the roles a user can have within an organization.
 * 
 */
public enum UserRoleType implements Serializable {

    /**
     * Service manager.
     */
    SERVICE_MANAGER,

    /**
     * Technology manager.
     */
    TECHNOLOGY_MANAGER,

    /**
     * Administrator of the organization.
     */
    ORGANIZATION_ADMIN,

    /**
     * Operator.
     */
    PLATFORM_OPERATOR,

    /**
     * Marketplace manager.
     */
    MARKETPLACE_OWNER,

    /**
     * Broker.
     */
    BROKER_MANAGER,

    /**
     * Reseller.
     */
    RESELLER_MANAGER,

    /**
     * Subscription manager.
     */
    SUBSCRIPTION_MANAGER,

    /**
     * Unit administrator.
     */
    UNIT_ADMINISTRATOR;

    /**
     * Check if the given role is a manager role, granting access to the
     * administration portal.
     * 
     * @return <code>true</code> if it is a manager role, otherwise
     *         <code>false</code>
     */
    public boolean isManagerRole() {
        switch (this) {
        case SERVICE_MANAGER:
        case TECHNOLOGY_MANAGER:
        case MARKETPLACE_OWNER:
        case BROKER_MANAGER:
        case RESELLER_MANAGER:
        case PLATFORM_OPERATOR:
            return true;
        default:
            return false;
        }
    }

    /**
     * Check if the given role is organization specific role. For example only
     * BROKER organization can have BROKER_MANAGER user role. *
     * 
     * @return <code>true</code> if it is an organization specific role,
     *         otherwise <code>false</code>
     */

    public boolean isOrganizationSpecificRole() {
        switch (this) {
        case ORGANIZATION_ADMIN:
        case SUBSCRIPTION_MANAGER:
            return false;
        default:
            return true;
        }
    }

    /**
     * Check if the given role is an unit role.
     * 
     * @return <code>true</code> if it is an unit role,
     *         otherwise <code>false</code>
     */
    public boolean isUnitRole() {
        switch (this) {
        case UNIT_ADMINISTRATOR:
            return true;
        default:
            return false;
        }
    }

    /**
     * Check if the given role is an organization role.
     * 
     * @return <code>true</code> if it is an organization role,
     *         otherwise <code>false</code>
     */
    public boolean isOrganizationRole() {
        switch (this) {
        case SERVICE_MANAGER:
        case TECHNOLOGY_MANAGER:
        case ORGANIZATION_ADMIN:
        case PLATFORM_OPERATOR:
        case MARKETPLACE_OWNER:
        case BROKER_MANAGER:
        case RESELLER_MANAGER:
        case SUBSCRIPTION_MANAGER:
            return true;
        default:
            return false;
        }
    }
}
