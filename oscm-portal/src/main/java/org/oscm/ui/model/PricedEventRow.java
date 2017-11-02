/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                        
 *                                                                              
 *  Creation Date: 16.07.2010                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oscm.ui.common.SteppedPriceComparator;
import org.oscm.ui.common.VOFinder;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * PricedEvent table row which either contains only a priced event or a priced
 * event with a stepped priced
 * 
 */
public class PricedEventRow implements Serializable {

    private static final long serialVersionUID = 1L;
    private VOPricedEvent pricedEvent;
    private VOSteppedPrice steppedPrice;

    /**
     * Create a list with PricedEventRow objects for the given service.
     * 
     * @param service
     *            the service for which the list is created
     * 
     * @returns the created list.
     */
    public static List<PricedEventRow> createPricedEventRowList(
            VOServiceDetails service) {

        List<PricedEventRow> result = new ArrayList<PricedEventRow>();
        for (VOEventDefinition event : service.getTechnicalService()
                .getEventDefinitions()) {
            VOPricedEvent pricedEvent = null;
            if (service.getPriceModel() != null) {
                pricedEvent = VOFinder.findPricedEvent(service.getPriceModel()
                        .getConsideredEvents(), event);
            }
            if (pricedEvent == null) {
                pricedEvent = new VOPricedEvent(event);
            }

            PricedEventRow row;
            if (pricedEvent.getSteppedPrices().isEmpty()) {
                row = new PricedEventRow();
                row.setPricedEvent(pricedEvent);
                result.add(row);
            } else {
                Collections.sort(pricedEvent.getSteppedPrices(),
                        new SteppedPriceComparator());
                for (VOSteppedPrice sp : pricedEvent.getSteppedPrices()) {
                    row = new PricedEventRow();
                    row.setPricedEvent(pricedEvent);
                    row.setSteppedPrice(sp);
                    result.add(row);
                }
            }
        }

        return result;
    }

    public VOPricedEvent getPricedEvent() {
        return pricedEvent;
    }

    public void setPricedEvent(VOPricedEvent pricedEvent) {
        this.pricedEvent = pricedEvent;
    }

    public BigDecimal getEventPrice() {
        return pricedEvent.getEventPrice();
    }

    public void setEventPrice(BigDecimal eventPrice) {
        pricedEvent.setEventPrice(eventPrice);
    }

    public String getEventDescription() {
        if (isEmptyOrFirstSteppedPrice()) {
            return pricedEvent.getEventDefinition().getEventDescription();
        }
        return null;
    }

    public VOSteppedPrice getSteppedPrice() {
        return steppedPrice;
    }

    public void setSteppedPrice(VOSteppedPrice steppedPrice) {
        this.steppedPrice = steppedPrice;
    }

    public Long getLimit() {
        return steppedPrice.getLimit();
    }

    public void setLimit(Long limit) {
        steppedPrice.setLimit(limit);
    }

    public BigDecimal getPrice() {
        return steppedPrice.getPrice();
    }

    public void setPrice(BigDecimal price) {
        steppedPrice.setPrice(price);
    }

    public boolean isFirstSteppedPrice() {
        if (steppedPrice == null) {
            return false;
        }
        return steppedPrice == getPricedEvent().getSteppedPrices().get(0);
    }

    public boolean isEmptyOrFirstSteppedPrice() {
        if (steppedPrice == null) {
            return true;
        }
        return steppedPrice == getPricedEvent().getSteppedPrices().get(0);
    }

    public boolean isLastSteppedPrice() {
        if (steppedPrice == null) {
            return false;
        }
        return steppedPrice == getPricedEvent().getSteppedPrices().get(
                getPricedEvent().getSteppedPrices().size() - 1);
    }

}
