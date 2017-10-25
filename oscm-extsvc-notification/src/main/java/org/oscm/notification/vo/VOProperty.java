/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-07-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.notification.vo;

import java.io.Serializable;

/**
 * Represents a property for use in the data of a notification.
 */
public class VOProperty implements Serializable {
    private static final long serialVersionUID = -3724074635157943539L;

    /**
     * The identifier of a subscription.
     */
    public final static String SUBSCRIPTION_SUBSCRIPTION_ID = "subscription.subscriptionId";
    
    /**
     * The identifier of the application instance for a subscription.
     */
    public final static String SUBSCRIPTION_SERVICE_INSTANCE_ID = "subscription.serviceInstanceId";
    
    /**
     * The identifier of the service underlying to a subscription.
     */
    public final static String SUBSCRIPTION_SERVICE_ID = "subscription.serviceId";
    
    /**
     * The numeric key of the service underlying to a subscription.
     */
    public final static String SUBSCRIPTION_SERVICE_KEY = "subscription.serviceKey";
    
    /**
     * The email address of a billing contact.
     */
    public final static String BILLING_CONTACT_EMAIL = "billingContact.email";
    
    /**
     * The company name of a billing contact.
     */
    public final static String BILLING_CONTACT_COMPANYNAME = "billingContact.companyName";
    
    /**
     * The postal address of a billing contact.
     */
    public final static String BILLING_CONTACT_ADDRESS = "billingContact.address";
    
    /**
     * The identifier of a marketplace.
     */
    public final static String MARKETPLACE_MARKETPLACE_ID = "marketplace.marketplaceId";

    /**
     * Name of the property.
     */
    private String name;

    /**
     * Value of the property.
     */
    private String value;

    /**
     * Retrieves the name of the property.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the property.
     * 
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the value of the property.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the property.
     * 
     * @param value
     *            the value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
