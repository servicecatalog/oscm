/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.05.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the billing input data for a chunk of subscriptions, that has to be
 * processed for a given billing period.
 * 
 * @author baumann
 */
public class BillingSubscriptionChunk {
    private long billingPeriodStart;
    private long billingPeriodEnd;
    private List<BillingInput> billingInputList;

    public BillingSubscriptionChunk(long billingPeriodStart,
            long billingPeriodEnd) {
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.billingInputList = new ArrayList<BillingInput>();
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
     * @return the billingInputList
     */
    public List<BillingInput> getBillingInputList() {
        return billingInputList;
    }

    /**
     * @param billingInputList
     *            the billingInputList to set
     */
    public void setBillingInputList(List<BillingInput> billingInputList) {
        this.billingInputList = billingInputList;
    }

    public void addBillingInput(BillingInput billingInput) {
        billingInputList.add(billingInput);
    }
}
