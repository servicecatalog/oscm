/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-01-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the periods available for recurring charges and for price models
 * that charge for a service per time unit. For example, a price model could
 * define an amount of money that a customer has to pay per DAY for each user or
 * subscription.
 */
public enum PricingPeriod {

    /**
     * The period is a month.
     */
    MONTH(2678400000L),

    /**
     * The period is a week.
     */
    WEEK(604800000L),

    /**
     * The period is a day.
     */
    DAY(86400000L),

    /**
     * The period is an hour.
     */
    HOUR(3600000L);

    private long milliseconds;

    PricingPeriod(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    /**
     * Returns the period in milliseconds. Note that a <code>MONTH</code> is
     * always 31 days, and a <code>DAY</code> is always 24 hours.
     * 
     * @return the number of milliseconds
     */
    public long getMilliseconds() {
        return milliseconds;
    }
}
