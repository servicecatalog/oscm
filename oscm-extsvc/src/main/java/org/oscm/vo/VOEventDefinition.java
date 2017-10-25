/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import org.oscm.types.enumtypes.EventType;

/**
 * Represents the definition of an event.
 * 
 */
public class VOEventDefinition extends BaseVO {

    private static final long serialVersionUID = 3216257594108168576L;

    /**
     * The type of the event.
     */
    private EventType eventType;

    /**
     * The identifier of the event.
     */
    private String eventId;

    /**
     * The description of the event in the locale set for the user who performs
     * the operation.
     */
    private String eventDescription;

    /**
     * Retrieves the type of the event.
     * 
     * @return the event type
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Retrieves the identifier of the event.
     * 
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Retrieves the text describing the event in the locale set for the calling
     * user.
     * 
     * @return the event description
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * Sets the type of the event.
     * 
     * @param eventType
     *            the event type
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Sets the identifier of the event.
     * 
     * @param eventId
     *            the event ID
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Sets the text describing the event.
     * 
     * @param eventDescription
     *            the event description
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

}
