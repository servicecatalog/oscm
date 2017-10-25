/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015-02-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.context;

/**
 * Specifies the keys that may occur in the context defining the element for
 * which a price model is to be returned.
 * 
 */
public enum ContextKey {

    /**
     * The identifier of the organization owning a subscription. Only required
     * for pushing price models to subscriptions.
     */
    TENANT_ID,

    /**
     * The name of the organization owning a subscription.
     */
    TENANT_NAME,

    /**
     * The identifier of the customer organization for a customer-specific price
     * model.
     */
    CUSTOMER_ID,

    /**
     * The name of the customer organization for a customer-specific price
     * model.
     */
    CUSTOMER_NAME,

    /**
     * The identifier of a service.
     */
    SERVICE_ID,

    /**
     * The name of a service.
     */
    SERVICE_NAME,

    /**
     * The parameters of a service.
     */
    SERVICE_PARAMETERS,

    /**
     * The identifier of the subscription for a subscription price model.
     */
    SUBSCRIPTION_ID
}
