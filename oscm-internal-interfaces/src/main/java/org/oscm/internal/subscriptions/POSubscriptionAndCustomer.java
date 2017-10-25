/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.subscriptions;

import java.io.Serializable;

/**
 * @author Mao
 * 
 */
public class POSubscriptionAndCustomer implements Serializable {

    private static final long serialVersionUID = -8906674861646856983L;

    private String customerName;

    private String customerId;

    private String subscriptionId;

    private String activation;

    private String serviceId;
    
    private String serviceName;
    
    private long tkey;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getActivation() {
        return activation;
    }

    public void setActivation(String activation) {
        this.activation = activation;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public long getTkey() {
        return tkey;
    }

    public void setTkey(long tkey) {
        this.tkey = tkey;
    }

    public Long getActivationTimeInMillis() {
        Long retVal = null;
        if (activation != null) {
            retVal = Long.valueOf(activation);
        }
        return retVal;
    }

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
