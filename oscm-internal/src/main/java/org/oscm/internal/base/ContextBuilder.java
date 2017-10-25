/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.02.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.base;

import java.util.HashMap;
import java.util.Map;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.context.ContextValueParameterMap;
import org.oscm.billing.external.context.ContextValueString;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * Builds a CT-MG context for the billing requests to the external billing
 * system.
 * 
 */
public class ContextBuilder {

    private Map<ContextKey, ContextValue<?>> context = new HashMap<>();

    public ContextBuilder() {
    }

    public ContextBuilder(String tenant_id, String tenant_name) {
        context.put(ContextKey.TENANT_ID, new ContextValueString(tenant_id));
        context.put(ContextKey.TENANT_NAME, new ContextValueString(tenant_name));
    }

    public ContextBuilder addService(VOService service) {
        context.put(ContextKey.SERVICE_ID,
                new ContextValueString(service.getServiceId()));
        context.put(ContextKey.SERVICE_NAME,
                new ContextValueString(service.getName()));
        return this;
    }

    public ContextBuilder addCustomer(VOOrganization customer) {
        context.put(ContextKey.CUSTOMER_ID,
                new ContextValueString(customer.getOrganizationId()));
        /*
         * context.put(ContextKey.CUSTOMER_NAME, new
         * ContextValueString(customer.getName()));
         */
        return this;
    }

    public ContextBuilder addServiceParameters(Map<String, String> parametersMap) {
        context.put(ContextKey.SERVICE_PARAMETERS,
                new ContextValueParameterMap(parametersMap));
        return this;
    }

    public Map<ContextKey, ContextValue<?>> build() {
        return context;
    }

    public ContextBuilder addSubscription(VOSubscriptionDetails subscription) {
        context.put(ContextKey.SUBSCRIPTION_ID, new ContextValueString(
                subscription.getSubscriptionId()));
        return this;
    }
}