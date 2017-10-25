/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import java.util.List;

import org.oscm.billingservice.service.model.BillingInput;

/**
 * Provides the required data to perform a billing run per subscription.
 * 
 * @author muenz
 * 
 */
abstract class DataProvider {

    protected long periodStart;
    protected long periodEnd;
    protected List<BillingInput> billingInputList;

    /**
     * Returns the start date of the billing period in milliseconds. Please
     * note, the start time is usually midnight, but can also be any time of the
     * day. The period length is typically one month, but also can be more or
     * less.
     * 
     * @return long
     */
    public long getPeriodStart() {
        return periodStart;
    }

    /**
     * Returns the end date of the billing period in milliseconds. Please note,
     * the end time is usually midnight, but can also be any time of the day.
     * The period length is typically one month, but also can be more or less.
     * 
     * @return long
     */
    public long getPeriodEnd() {
        return periodEnd;
    }

    /**
     * Returns a list of billing input data objects. For each BillingInput a
     * billing run will create one BillingResult.
     * 
     * @return list
     */
    public List<BillingInput> getBillingInput() {
        return billingInputList;
    }

}
