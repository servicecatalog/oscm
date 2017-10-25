/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;

/**
 * Represents the information gathered when an event occurs. The information is
 * related to a specific event that is defined at a technical service or in the
 * platform.
 * 
 */
public class VOGatheredEvent implements Serializable {

    private static final long serialVersionUID = 4236856337043191556L;

    /**
     * The time the event was created (in milliseconds).
     */
    private long occurrenceTime;

    /**
     * The actor that caused the event. For users, this information is the user
     * ID.
     */
    private String actor;

    /**
     * The identifier of the event. This is the string that identifies the event
     * in the context of the relevant application. It is defined at the
     * corresponding technical service or in the platform.
     */
    private String eventId;

    /**
     * Multiplier for gathered event occurrences.
     * <p>
     * The multiplier specifies the number of occurrences of an event. Instead
     * of recording the same event each time it occurs, you can record it only
     * once and set the multiplier to the number of occurrences.
     * <p>
     * The billing services consider the multiplier. For example, one event with
     * a multiplier of 2 is handled in the same way as 2 events of the same
     * type, if both events occur in the same billing period.
     */
    private long multiplier = 1L;

    /**
     * Unique ID of a gathered event for the subscription. May be
     * <code>null</code>.<br>
     * It is not possible to record a gathered event if its used unique ID
     * already exists in the system.
     */
    private String uniqueId;

    /**
     * Retrieves the time the event occurred in Java time format (milliseconds).
     * 
     * @return the time
     */
    public long getOccurrenceTime() {
        return occurrenceTime;
    }

    /**
     * Retrieves the actor that caused the event. For users, this is the user
     * ID.
     * 
     * @return the actor
     */
    public String getActor() {
        return actor;
    }

    /**
     * Retrieves the identifier of the event. This is the string that identifies
     * the event in the context of the relevant application. It is defined at
     * the corresponding technical service or in the platform.
     * 
     * @return the event identifier
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the time the event occurred in Java time format (milliseconds).
     * 
     * @param occurrenceTime
     *            the time
     */
    public void setOccurrenceTime(long occurrenceTime) {
        this.occurrenceTime = occurrenceTime;
    }

    /**
     * Sets the actor that caused the event. For users, this is the user ID.
     * 
     * @param actor
     *            the actor
     */
    public void setActor(String actor) {
        this.actor = actor;
    }

    /**
     * Sets the identifier of the event. This is the string that identifies the
     * event in the context of the relevant application. It is defined at the
     * corresponding technical service or in the platform.
     * 
     * @param eventId
     *            the event identifier
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Retrieves the multiplier for the event.
     * <p>
     * The multiplier specifies the number of occurrences of an event. Instead
     * of recording the same event each time it occurs, you can record it only
     * once and set the multiplier to the number of occurrences.
     * <p>
     * The billing services consider the multiplier. For example, one event with
     * a multiplier of 2 is handled in the same way as 2 events of the same
     * type, if both events occur in the same billing period.
     * 
     * @return the multiplier; never <code>null</code>; the default is 1
     */
    public long getMultiplier() {
        return multiplier;
    }

    /**
     * Sets the multiplier for the event.
     * <p>
     * The multiplier specifies the number of occurrences of an event. Instead
     * of recording the same event each time it occurs, you can record it only
     * once and set the multiplier to the number of occurrences.
     * <p>
     * The billing services consider the multiplier. For example, one event with
     * a multiplier of 2 is handled in the same way as 2 events of the same
     * type, if both events occur in the same billing period.
     * 
     * @param multiplier
     *            the multiplier; must not be <code>null</code>; the default is
     *            1
     */
    public void setMultiplier(long multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Sets the unique ID of the event for the current subscription. It is not
     * possible to record the event if this ID already exists.
     * 
     * @param pUniqueId
     *            the unique ID; may be <code>null</code>
     */
    public void setUniqueId(String pUniqueId) {
        this.uniqueId = pUniqueId;
    }

    /**
     * Returns the unique ID of the event for the current subscription. It is
     * not possible to record the event if this ID already exists.
     * 
     * @return the unique ID; may be <code>null</code>
     */
    public String getUniqueId() {
        return uniqueId;
    }
}
