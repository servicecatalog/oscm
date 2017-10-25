/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2011-11-03                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the sorting criteria for lists which are, for example, the result
 * of a search.
 */
public enum Sorting {

    /**
     * The results are sorted by their name in ascending order.
     */
    NAME_ASCENDING,

    /**
     * The results are sorted by their name in descending order.
     */
    NAME_DESCENDING,

    /**
     * The results are sorted by their activation date and time in ascending
     * order.
     */
    ACTIVATION_ASCENDING,

    /**
     * The results are sorted by their activation date and time in descending
     * order.
     */
    ACTIVATION_DESCENDING,

    /**
     * The results are sorted by their rating in ascending order.
     */
    RATING_ASCENDING,

    /**
     * The results are sorted by their rating in descending order.
     */
    RATING_DESCENDING;
}
