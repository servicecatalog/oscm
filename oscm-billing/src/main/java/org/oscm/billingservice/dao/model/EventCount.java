/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 25.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents the information on which billing events occurred how often. Used
 * to determine billing costs.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class EventCount {

    /**
     * The identifier of the event.
     */
    private String eventIdentifier;

    /**
     * The amount of occurrences of the event.
     */
    private long numberOfOccurrences;

    /** Price for one event. */
    private BigDecimal priceForOneEvent;

    /** Price for one event. */
    private BigDecimal priceForEventsWithEventIdentifier;

    /** Event stepped price. */
    private List<SteppedPriceData> eventSteppedPrice;

    /** The key of the respective event. */
    private long eventKey;

    public String getEventIdentifier() {
        return eventIdentifier;
    }

    public long getNumberOfOccurrences() {
        return numberOfOccurrences;
    }

    public void setEventIdentifier(String eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }

    public void setNumberOfOccurrences(long numberOfOccurrences) {
        this.numberOfOccurrences = numberOfOccurrences;
    }

    /**
     * @param priceForOneEvent
     *            the priceForOneEvent to set
     */
    public void setPriceForOneEvent(BigDecimal priceForOneEvent) {
        this.priceForOneEvent = priceForOneEvent;
    }

    /**
     * @return the priceForOneEvent
     */
    public BigDecimal getPriceForOneEvent() {
        if (priceForOneEvent == null) {
            return BigDecimal.ZERO;
        }
        return priceForOneEvent;
    }

    /**
     * @param priceForEventsWithEventIdentifier
     *            the priceForEventsWithEventIdentifier to set
     */
    public void setPriceForEventsWithEventIdentifier(
            BigDecimal priceForEventsWithEventIdentifier) {
        this.priceForEventsWithEventIdentifier = priceForEventsWithEventIdentifier;
    }

    /**
     * @return the priceForEventsWithEventIdentifier
     */
    public BigDecimal getPriceForEventsWithEventIdentifier() {
        if (priceForEventsWithEventIdentifier == null) {
            return BigDecimal.ZERO;
        }
        return priceForEventsWithEventIdentifier;
    }

    /**
     * @param eventSteppedPrice
     *            the eventSteppedPrice to set
     */
    public void setEventSteppedPrice(List<SteppedPriceData> eventSteppedPrice) {
        this.eventSteppedPrice = eventSteppedPrice;
    }

    /**
     * @return the eventSteppedPrice
     */
    public List<SteppedPriceData> getEventSteppedPrice() {
        return eventSteppedPrice;
    }

    /**
     * @return the eventKey
     */
    public long getEventKey() {
        return eventKey;
    }

    /**
     * @param key
     *            the event key to set
     */
    public void setEventKey(long key) {
        this.eventKey = key;
    }

}
