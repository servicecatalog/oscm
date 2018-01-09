/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.data;

import java.util.List;

/**
 * Represents the context for a price model
 *
 */
public class PriceModelContext {

    private static final String SERVICE_PARAMETERS_PREFIX = "SERVICE_PARAMETERS_";
    private static final String SERVICE_PAR_INSTANCE_TYPE = "INSTANCE_TYPE";
    private static final String SERVICE_PAR_REGION = "REGION";
    private static final String SERVICE_PAR_OS = "OS";
    private static final String PAR_CUSTOMER_ID = "CUSTOMER_ID";
    private static final String PAR_SUBSCRIPTION_ID = "SUBSCRIPTION_ID";

    private String instanceType = null;
    private String region = null;
    private String os = null;
    private String customerId = null;
    private String subscriptionId = null;

    /**
     * Evaluate the context keys and values from the HTTP request and store the
     * context in a new price model context object
     * 
     * @param contextKeys
     *            the context keys
     * @param contextValues
     *            the context values
     * @return a price model context object containing the context
     */
    public static PriceModelContext create(List<String> contextKeys,
            List<String> contextValues) {

        PriceModelContext pmContext = new PriceModelContext();

        for (int i = 0; i < contextKeys.size(); i++) {
            String key = contextKeys.get(i);
            if (key.startsWith(SERVICE_PARAMETERS_PREFIX)) {
                key = key.substring(SERVICE_PARAMETERS_PREFIX.length());
                switch (key) {
                case SERVICE_PAR_INSTANCE_TYPE:
                    pmContext.setInstanceType(contextValues.get(i));
                    break;
                case SERVICE_PAR_REGION:
                    pmContext.setRegion(contextValues.get(i));
                    break;
                case SERVICE_PAR_OS:
                    pmContext.setOs(contextValues.get(i));
                    break;
                default:
                    break;
                }
            } else if (key.equals(PAR_CUSTOMER_ID)) {
                pmContext.setCustomerId(contextValues.get(i));
            } else if (key.equals(PAR_SUBSCRIPTION_ID)) {
                pmContext.setSubscriptionId(contextValues.get(i));
            }
        }

        return pmContext;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * @return <code>true</code> if this is a service price model context
     */
    public boolean isServicePriceModelContext() {
        return (instanceType != null && region != null && os != null);
    }

    /**
     * @return <code>true</code> if this is a subscription price model context
     */
    public boolean isSubscriptionPriceModelContext() {
        return (subscriptionId != null);
    }
}
