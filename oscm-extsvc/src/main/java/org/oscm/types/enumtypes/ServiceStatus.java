/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-02-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the statuses a service can take on.
 * 
 */
public enum ServiceStatus {
    /**
     * The service is active and customers can subscribe to it.
     */
    ACTIVE,

    /**
     * The service has become obsolete, for example, because the relation
     * between the supplier and technology provider no longer exists. The
     * service cannot be used and is not shown to customers any longer.
     */
    OBSOLETE,

    /**
     * The service has been marked as deleted. It cannot be used for new for new
     * subscriptions and no longer appears in lists.
     */
    DELETED,

    /**
     * The service is inactive, for example, due to a required update. Inactive
     * services are not visible to customers.
     */
    INACTIVE,

    /**
     * The service was suspended by the marketplace owner due to legal issues.
     */
    SUSPENDED;
}
