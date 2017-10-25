/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 05.06.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.internal.types.enumtypes.EventType;

/**
 * JPA managed object containing all event related information.
 * 
 * 
 * @author Mike J&auml;ger
 * 
 */
@NamedQueries( {
        @NamedQuery(name = "GatheredEvent.getEventsForSubAndPeriod", query = "SELECT evt.dataContainer.eventIdentifier, SUM(evt.dataContainer.multiplier) FROM GatheredEvent evt WHERE evt.dataContainer.occurrenceTime >= :startTime AND evt.dataContainer.occurrenceTime < :endTime AND evt.dataContainer.subscriptionTKey = :subscriptionKey GROUP BY evt.dataContainer.eventIdentifier"),
        @NamedQuery(name = "GatheredEvent.setResultReferenceForEventsForSubAndPeriod", query = "UPDATE GatheredEvent evt SET evt.billingResult = :billingResult WHERE evt.dataContainer.occurrenceTime >= :startTime AND evt.dataContainer.occurrenceTime < :endTime AND evt.dataContainer.subscriptionTKey = :subscriptionKey") })
@Entity
public class GatheredEvent extends
        DomainObjectWithVersioning<GatheredEventData> {

    private static final long serialVersionUID = -82675568384475008L;

    public GatheredEvent() {
        super();
        dataContainer = new GatheredEventData();
    }

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private BillingResult billingResult;

    public String getActor() {
        return dataContainer.getActor();
    }

    public String getEventId() {
        return dataContainer.getEventIdentifier();
    }

    public long getOccurrenceTime() {
        return dataContainer.getOccurrenceTime();
    }

    public long getSubscriptionTKey() {
        return dataContainer.getSubscriptionTKey();
    }

    public EventType getType() {
        return dataContainer.getType();
    }

    public void setActor(String actor) {
        dataContainer.setActor(actor);
    }

    public void setEventId(String eventId) {
        dataContainer.setEventIdentifier(eventId);
    }

    public void setOccurrenceTime(long occurrenceTime) {
        dataContainer.setOccurrenceTime(occurrenceTime);
    }

    public void setSubscriptionTKey(long subscriptionTKey) {
        dataContainer.setSubscriptionTKey(subscriptionTKey);
    }

    public void setType(EventType type) {
        dataContainer.setType(type);
    }

    /*
     * used in queries
     */
    public BillingResult getBillingResult() {
        return billingResult;
    }

    /*
     * used in queries
     */
    public void setBillingResult(BillingResult billingResult) {
        this.billingResult = billingResult;
    }

    /**
     * Multiplier getter.
     * 
     * @return Multiplier for the gathered event. Not null, default value is 1.
     */
    public long getMultiplier() {
        return dataContainer.getMultiplier();
    }

    /**
     * Multiplier setter.
     * 
     * @param multiplier
     *            for the gathered event. Not null, default value is 1.
     */
    public void setMultiplier(long multiplier) {
        dataContainer.setMultiplier(multiplier);
    }

    /**
     * Unique id getter.
     * 
     * @return Unique Id.
     */
    public String getUniqueId() {
        return dataContainer.getUniqueId();
    }

    /**
     * Unique id setter.
     * 
     * @param pUniqueId
     *            Unique ID for setting.
     */
    public void setUniqueId(String pUniqueId) {
        dataContainer.setUniqueId(pUniqueId);
    }

}
