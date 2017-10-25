/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.05.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service.model;

import java.util.List;

/**
 * Contains the billing periods that have to be processed and the keys of the
 * subscriptions, that need to be calculated.
 * 
 * @author baumann
 */
public class BillingPeriodData {
    private long billingPeriodStart;
    private long billingPeriodEnd;
    private List<Long> subscriptionKeys;

    public BillingPeriodData(long billingPeriodStart, long billingPeriodEnd,
            List<Long> subscriptionKeys) {
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.subscriptionKeys = subscriptionKeys;
    }

    /**
     * @return the billingPeriodStart
     */
    public long getBillingPeriodStart() {
        return billingPeriodStart;
    }

    /**
     * @param billingPeriodStart
     *            the billingPeriodStart to set
     */
    public void setBillingPeriodStart(long billingPeriodStart) {
        this.billingPeriodStart = billingPeriodStart;
    }

    /**
     * @return the billingPeriodEnd
     */
    public long getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    /**
     * @param billingPeriodEnd
     *            the billingPeriodEnd to set
     */
    public void setBillingPeriodEnd(long billingPeriodEnd) {
        this.billingPeriodEnd = billingPeriodEnd;
    }

    /**
     * @return the subscriptionKeys
     */
    public List<Long> getSubscriptionKeys() {
        return subscriptionKeys;
    }

    /**
     * @param subscriptionKeys
     *            the subscriptionKeys to set
     */
    public void setSubscriptionKeys(List<Long> subscriptionKeys) {
        this.subscriptionKeys = subscriptionKeys;
    }

}
