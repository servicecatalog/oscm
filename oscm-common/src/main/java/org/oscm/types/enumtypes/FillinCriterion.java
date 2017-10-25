/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-06-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the criterion for filling up the landing page (home page) of a
 * marketplace if not enough featured services are available.
 */
public enum FillinCriterion {

    /**
     * No services are added to the landing page; only the featured services are
     * displayed.
     */
    NO_FILLIN,

    /**
     * Services are added to the landing page in alphabetically ascending order
     * (A-Z).
     */
    NAME_ASCENDING,

    /**
     * Services are added to the landing page by their activation date and time,
     * starting with one activated most recently.
     */
    ACTIVATION_DESCENDING,

    /**
     * Services are added to the landing page by their rating, starting with the
     * one rated best.
     */
    RATING_DESCENDING;

	public static FillinCriterion getDefault() {
		return ACTIVATION_DESCENDING;
	}
}
