/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 30.04.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

/**
 * Data object, which represents a subscription that has to be billed
 * 
 * @author baumann
 *
 */
public class BillingSubscriptionData {

    private long subscriptionKey;
    private long activationDate;
    private int cutOffDay;
    private Long endOfLastBilledPeriod;

    /**
     * @return the subscriptionKey
     */
    public long getSubscriptionKey() {
        return subscriptionKey;
    }

    /**
     * @param subscriptionKey
     *            the subscriptionKey to set
     */
    public void setSubscriptionKey(long subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    /**
     * @return the activationDate
     */
    public long getActivationDate() {
        return activationDate;
    }

    /**
     * @param activationDate
     *            the activationDate to set
     */
    public void setActivationDate(long activationDate) {
        this.activationDate = activationDate;
    }

    /**
     * @return the cutOffDay
     */
    public int getCutOffDay() {
        return cutOffDay;
    }

    /**
     * @param cutOffDay
     *            the cutOffDay to set
     */
    public void setCutOffDay(int cutOffDay) {
        this.cutOffDay = cutOffDay;
    }

    /**
     * @return the endOfLastBilledPeriod
     */
    public Long getEndOfLastBilledPeriod() {
        return endOfLastBilledPeriod;
    }

    /**
     * @param endOfLastBilledPeriod
     *            the endOfLastBilledPeriod to set
     */
    public void setEndOfLastBilledPeriod(Long endOfLastBilledPeriod) {
        this.endOfLastBilledPeriod = endOfLastBilledPeriod;
    }

}
