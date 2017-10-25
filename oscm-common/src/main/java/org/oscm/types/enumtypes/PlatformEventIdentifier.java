/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 13.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Interface representing the event identifier the platform supports. This
 * object is in fact kind of an enumeration, but as we cannot put the event
 * identifiers specified by the products in an enumeration, we must keep the
 * objects holding the identifiers supporting by the platform compatible as
 * strings.
 * 
 * @author Mike J&auml;ger
 * 
 */
public interface PlatformEventIdentifier {

    /**
     * Indicates that a user stopped using a service, what is indicated either
     * by directly logging out from it or by a time-out.
     */
    public static final String USER_LOGOUT_FROM_SERVICE = "USER_LOGOUT_FROM_SERVICE";

    /**
     * Indicates that a user logged in to the system and is now actively using a
     * provided service (product running on the platform).
     */
    public static final String USER_LOGIN_TO_SERVICE = "USER_LOGIN_TO_SERVICE";
}
