/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-01-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the time periods available for recurring charges and for the cost
 * calculation based on time units in price models. For example, a price model
 * could define an amount of money that a customer has to pay per DAY for each
 * user or subscription.
 */
public enum PricingPeriod {
    /**
     * The period is a month.
     */
    MONTH,

    /**
     * The period is a week.
     */
    WEEK,

    /**
     * The period is a day.
     */
    DAY,

    /**
     * The period is an hour.
     */
    HOUR;
}
