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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.enumtypes.EventType;

/**
 * An event is an identifier for the events a technical product supports. The
 * event itself, in the scope of the technical product, is identified by the
 * event identifier.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "technicalProduct_tkey", "eventIdentifier", "eventType" }))
@NamedQueries({
        @NamedQuery(name = "Event.getPlatformEvent", query = "select c from Event c where c.technicalProduct is NULL AND c.dataContainer.eventType=:eventType AND c.dataContainer.eventIdentifier=:eventIdentifier ORDER BY c.key ASC"),
        @NamedQuery(name = "Event.getAllPlatformEvents", query = "select c from Event c where c.technicalProduct is NULL AND c.dataContainer.eventType=:eventType ORDER BY c.key ASC"),
        @NamedQuery(name = "Event.findByBusinessKey", query = "select c from Event c where c.dataContainer.eventIdentifier=:eventIdentifier AND c.dataContainer.eventType=:eventType AND c.technicalProduct_tkey=:technicalProduct_tkey") })
@BusinessKey(attributes = { "technicalProduct_tkey", "eventIdentifier",
        "eventType" })
public class Event extends DomainObjectWithHistory<EventData> {

    private static final long serialVersionUID = -2321303407897558512L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(LocalizedObjectTypes.EVENT_DESC));

    @Column(name = "technicalProduct_tkey", insertable = false, updatable = false, nullable = true)
    private Long technicalProduct_tkey;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "event", fetch = FetchType.LAZY)
    @OrderBy
    private List<PricedEvent> pricedEvents = new ArrayList<PricedEvent>();

    /**
     * product events belong to a technical product (platform events do not)
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "technicalProduct_tkey")
    private TechnicalProduct technicalProduct;

    public Event() {
        setDataContainer(new EventData());
    }

    public List<PricedEvent> getPricedEvents() {
        return pricedEvents;
    }

    public void setPricedEvents(List<PricedEvent> events) {
        this.pricedEvents = events;
    }

    public Long getTechnicalProduct_tkey() {
        return technicalProduct_tkey;
    }

    public void setTechnicalProduct_tkey(Long technicalProduct_tkey) {
        this.technicalProduct_tkey = technicalProduct_tkey;
    }

    /**
     * Refer to {@link EventData#eventIdentifier}
     */
    public String getEventIdentifier() {
        return dataContainer.getEventIdentifier();
    }

    /**
     * Refer to {@link EventData#eventIdentifier}
     */
    public void setEventIdentifier(String eventIdentifier) {
        dataContainer.setEventIdentifier(eventIdentifier);
    }

    /**
     * Refer to {@link EventData#eventType}
     */
    public EventType getEventType() {
        return dataContainer.getEventType();
    }

    /**
     * Refer to {@link EventData#eventType}
     */
    public void setEventType(EventType eventType) {
        dataContainer.setEventType(eventType);
    }

    public TechnicalProduct getTechnicalProduct() {
        return technicalProduct;
    }

    public void setTechnicalProduct(TechnicalProduct technicalProduct) {
        this.technicalProduct = technicalProduct;
        if (null != technicalProduct) {
            setTechnicalProduct_tkey(Long.valueOf(technicalProduct.getKey()));
        }
    }

    @Override
    String toStringAttributes() {
        return String.format(", eventIdentifier='%s'", getEventIdentifier());
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

}
