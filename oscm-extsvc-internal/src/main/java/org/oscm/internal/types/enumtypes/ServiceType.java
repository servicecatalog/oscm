/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-02-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the statuses a service can take on.
 * 
 */
public enum ServiceType {
    /**
     * The template a supplier creates
     */
    TEMPLATE,

    /**
     * Resale permissions granted to resellers and brokers
     */
    PARTNER_TEMPLATE,

    /**
     * Customer specific copy
     */
    CUSTOMER_TEMPLATE,

    /**
     * Subscription to a supplier service
     */
    SUBSCRIPTION,

    /**
     * Subscription to a partner service
     */
    PARTNER_SUBSCRIPTION,

    /**
     * Subscription to a customer specific service
     */
    CUSTOMER_SUBSCRIPTION;

    public static boolean isSubscription(ServiceType type) {
        return type == CUSTOMER_SUBSCRIPTION || type == PARTNER_SUBSCRIPTION
                || type == SUBSCRIPTION;
    }

    public static boolean isPartnerSubscription(ServiceType type) {
        return type == PARTNER_SUBSCRIPTION;
    }

    public static boolean isTemplate(ServiceType type) {
        return !isSubscription(type);
    }

    public static boolean isTemplateOrPartnerTemplate(ServiceType type) {
        return type == TEMPLATE || type == PARTNER_TEMPLATE;
    }

    public static boolean isCustomerTemplate(ServiceType type) {
        return type == CUSTOMER_TEMPLATE;
    }

}
