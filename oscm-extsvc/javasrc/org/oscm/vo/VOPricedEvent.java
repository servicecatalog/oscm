/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-05-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.oscm.types.exceptions.IllegalArgumentException;

/**
 * Represents the information on events that is relevant for price models.
 * 
 */
public class VOPricedEvent extends BaseVO  {

    private static final long serialVersionUID = -8260878341502255890L;

    /**
     * Default constructor.
     */
    public VOPricedEvent() {

    }

    /**
     * The stepped prices for users.
     */
    private List<VOSteppedPrice> steppedPrices = new ArrayList<VOSteppedPrice>();

    /**
     * Constructs a priced event object based on the specified event definition.
     * 
     * @param eventDefinition
     *            the event definition
     */
    public VOPricedEvent(VOEventDefinition eventDefinition) {
        if (eventDefinition == null) {
            throw new IllegalArgumentException(
                    "VOEventDefinition must not be null");
        }
        this.eventDefinition = eventDefinition;
    }

    /**
     * The event definition this priced event is based on.
     */
    private VOEventDefinition eventDefinition;

    /**
     * The price that the organization will be charged for each occurrence of
     * the event.
     */
    private BigDecimal eventPrice = BigDecimal.ZERO;

    /**
     * Retrieves the price defined for the event.
     * 
     * @return the price
     */
    public BigDecimal getEventPrice() {
        return eventPrice;
    }

    /**
     * Sets the price for the event.
     * 
     * @param eventPrice
     *            the event price
     */
    public void setEventPrice(BigDecimal eventPrice) {
        this.eventPrice = eventPrice;
    }

    /**
     * Retrieves the definition of the event.
     * 
     * @return the event definition
     */
    public VOEventDefinition getEventDefinition() {
        return eventDefinition;
    }

    /**
     * Sets the definition of the event.
     * 
     * @param eventDefinition
     *            the event definition
     */
    public void setEventDefinition(VOEventDefinition eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    /**
     * Sets the price steps for the event if different prices are to be applied
     * depending on the number of occurrences of the event.
     * 
     * @param steppedUserPrices
     *            the price steps
     */
    public void setSteppedPrices(List<VOSteppedPrice> steppedUserPrices) {
        this.steppedPrices = steppedUserPrices;
    }

    /**
     * Retrieves the price steps for the event if different prices are applied
     * depending on the number of occurrences of the event.
     * 
     * @return the price steps
     */
    public List<VOSteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }
}
