/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 06.05.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.DHOConverter;
import org.oscm.domobjects.converters.ETConverter;
import org.oscm.internal.types.enumtypes.EventType;

/**
 * Data container to hold the information on each event.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class EventData extends DomainDataContainer {

    private static final long serialVersionUID = -8925197991236724376L;

    /**
     * The identifier of the event.
     */
    @Column(nullable = false, updatable = false)
    private String eventIdentifier;

    /**
     * The type of the event.
     */
    @Column(nullable = false, updatable = false, columnDefinition = "varchar")
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public void setEventIdentifier(String eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

}
