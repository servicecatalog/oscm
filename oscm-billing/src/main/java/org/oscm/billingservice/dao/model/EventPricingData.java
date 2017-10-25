/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 21.07.2010.                                                      
 *                                                                              
 *  Completion Time: 21.07.2010.                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Transfer object. Contains event price and stepped price for the event.
 * 
 * @author Aleh Khomich.
 * 
 */
public class EventPricingData {

    /** Event price. Used for billing, when no stepped prices are defined. */
    private BigDecimal price;

    /**
     * Event key. It is stored by the billing processing in eventPricingData in
     * order to retrieve during the export of the billing result the value of
     * the localized event description that corresponds to the given event key.
     */
    private long eventKey;
    /**
     * Event stepped prices. Can be null, in this case for billing just price
     * has to be used.
     */
    private List<SteppedPriceData> eventSteppedPrice;

    /**
     * Setter for event price.
     * 
     * @param price
     *            price to set.
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Getter for event price.
     * 
     * @return Event price.
     */
    public BigDecimal getPrice() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price;
    }

    /**
     * Setter for event stepped price list.
     * 
     * @param eventSteppedPrice
     *            Event stepped price list to set.
     */
    public void setEventSteppedPrice(List<SteppedPriceData> eventSteppedPrice) {
        this.eventSteppedPrice = eventSteppedPrice;
    }

    /**
     * Getter for event stepped price list.
     * 
     * @return Event stepped price list.
     */
    public List<SteppedPriceData> getEventSteppedPrice() {
        return eventSteppedPrice;
    }

    /**
     * Getter for event key.
     * 
     * @return the key of the respective event.
     */
    public long getEventKey() {
        return eventKey;
    }

    /**
     * Setter for event key.
     * 
     * @param key
     *            the event key to set.
     */
    public void setEventKey(long key) {
        this.eventKey = key;
    }

}
