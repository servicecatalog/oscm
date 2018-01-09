/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 05.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.ETConverter;
import org.oscm.internal.types.enumtypes.EventType;

@Embeddable
public class GatheredEventData extends DomainDataContainer {

    private static final long serialVersionUID = 7530357604669711333L;

    /**
     * The time the event was created at.
     */
    @Column(nullable = false)
    private long occurrenceTime;

    /**
     * The technical key of the referenced subscription.
     */
    @Column(nullable = false)
    private long subscriptionTKey;

    /**
     * The actor that caused the event. For any user this information will be
     * the user Id.
     */
    private String actor;

    /**
     * The type of the event, based on the currently supported set of types.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType type;

    /**
     * The identifier of the event. This can be any String that identifies the
     * kind of event in the context of an application (as e.g. defined for the
     * technical product).
     */
    @Column(nullable = false)
    private String eventIdentifier;

    /**
     * Multiplier for gathered event occurrence.
     */
    @Column(nullable = false)
    private long multiplier = 1L; // default value is 1

    /**
     * Unique ID of gathered event for subscription. Can be null. This is a
     * situation, when event has no id form service. Service does not interested
     * in unique id.
     */
    @Column(nullable = true)
    private String uniqueId;

    public long getOccurrenceTime() {
        return occurrenceTime;
    }

    public long getSubscriptionTKey() {
        return subscriptionTKey;
    }

    public String getActor() {
        return actor;
    }

    public EventType getType() {
        return type;
    }

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public void setOccurrenceTime(long occurrenceTime) {
        this.occurrenceTime = occurrenceTime;
    }

    public void setSubscriptionTKey(long subscriptionTKey) {
        this.subscriptionTKey = subscriptionTKey;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public void setEventIdentifier(String eventId) {
        this.eventIdentifier = eventId;
    }

    /**
     * Multiplier getter.
     * 
     * @return Multiplier for the gathered event. Not null, default value is 1.
     */
    public long getMultiplier() {
        return multiplier;
    }

    /**
     * Multiplier setter.
     * 
     * @param multiplier
     *            Multiplier for the gathered event.
     */
    public void setMultiplier(long multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * @param uniqueId
     *            the uniqueId to set
     */
    public void setUniqueId(String pUniqueId) {
        this.uniqueId = pUniqueId;
    }

    /**
     * @return the uniqueId
     */
    public String getUniqueId() {
        return uniqueId;
    }
}
