/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-08-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

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
    SUBSCRIPTION_MANAGER;

}
