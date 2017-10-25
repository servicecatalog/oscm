/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-02-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the statuses a subscription can take on.
 * 
 */
public enum SubscriptionStatus {
    /**
     * The subscription is ready to handle user requests. Users can work with
     * the underlying application(s).
     */
    ACTIVE,

    /**
     * The subscription has been created but is still waiting for the tenant ID
     * of the associated application instance.
     */
    PENDING,

    /**
     * The subscription was used but has been deactivated explicitly. User
     * requests are not handled.
     */
    DEACTIVATED,

    /**
     * The subscription was active for a defined period but has been deactivated
     * automatically.
     */
    EXPIRED,

    /**
     * The subscription cannot be used because there are problems in fulfilling
     * its conditions and constraints (e.g. the application instance cannot be
     * provided).
     */
    INVALID,

    /**
     * The subscription cannot be used because the supplier or reseller has
     * disabled the payment type for which the customer specified his payment
     * information. The subscription keeps this status until valid payment
     * information is provided.
     */
    SUSPENDED,

    /**
     * The subscription has been modified but is still waiting for the
     * provisioning service of the associated application to confirm the
     * modification.
     */
    PENDING_UPD,

    /**
     * The subscription is suspended due to missing payment information and, at
     * the same time, waiting for the confirmation of a modification from the
     * provisioning service of the associated application.
     */
    SUSPENDED_UPD;
}
