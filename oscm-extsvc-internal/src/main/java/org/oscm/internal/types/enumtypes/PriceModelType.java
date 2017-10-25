/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-11-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the cost calculation type of price models.
 * 
 */
public enum PriceModelType {

    /**
     * The price model is free of charge; subscriptions based on it are not
     * considered in the cost calculation.
     */
    FREE_OF_CHARGE,

    /**
     * The costs are calculated based on milliseconds and exact usage periods.
     */
    PRO_RATA,

    /**
     * The costs are calculated based on full time units, such as full months or
     * days, for which the service was used.
     */
    PER_UNIT,

    /**
     * The price model type is unknown, it comes from external billing system.
     */
    UNKNOWN;
}
