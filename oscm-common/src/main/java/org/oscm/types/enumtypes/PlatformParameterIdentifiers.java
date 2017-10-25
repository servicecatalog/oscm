/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 13.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Interface representing the parameter identifiers the platform supports. This
 * object is in fact a kind of an enumeration, but as we cannot put the
 * parameter identifiers specified by the products in an enumeration, we must
 * keep the objects holding the identifiers supporting by the platform
 * compatible as strings.
 * 
 * @author Mike J&auml;ger
 * 
 */
public interface PlatformParameterIdentifiers {

    /**
     * The maximum number of concurrent users for a subscription.
     */
    public static final String CONCURRENT_USER = "CONCURRENT_USER";
    /**
     * The maximum number of registered users for a subscription.
     */
    public static final String NAMED_USER = "NAMED_USER";
    /**
     * The number of milliseconds a subscription can be used.
     */
    public static final String PERIOD = "PERIOD";
}
