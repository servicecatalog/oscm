/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-06-08                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the types of events that are gathered by the platform.
 * 
 */
public enum EventType {

    /**
     * The event is gathered by the platform but has been generated externally
     * and does not match any event type the platform can interpret.
     */
    SERVICE_EVENT,
    /**
     * The event is gathered by the platform and has been generated internally,
     * by the platform itself.
     */
    PLATFORM_EVENT;

}
