/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 13.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * shared handler for price information
 * used by serviceDetails page and subscriptionDetails page.
 * 
 */
public class SteppedPriceHandler {
    /**
     * @return true if any priced parameter row of the price model contains any
     *         stepped price.
     */
    public static boolean isParametersWithSteppedPrices(
            List<PricedParameterRow> serviceParameters) {
        if (serviceParameters == null) {
            return false;
        }
        for (PricedParameterRow row : serviceParameters) {
            if (row.getSteppedPrice() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPricedEventsWithSteppedPrices(
            List<PricedEventRow> serviceEvents) {
        if (serviceEvents == null) {
            return false;
        }
        for (PricedEventRow row : serviceEvents) {
            if (row.getSteppedPrice() != null) {
                return true;
            }
        }
        return false;
    }


    public static List<PricedEventRow> buildPricedEvents(
            List<VOPricedEvent> pricedEvents) {
        final List<PricedEventRow> serviceEvents = new ArrayList<PricedEventRow>();
        for (VOPricedEvent pricedEvent : pricedEvents) {
            PricedEventRow row;
            if (pricedEvent.getSteppedPrices().isEmpty()) {
                row = new PricedEventRow();
                row.setPricedEvent(pricedEvent);
                serviceEvents.add(row);
            } else {
                Collections.sort(pricedEvent.getSteppedPrices(),
                        new SteppedPriceComparator());
                for (VOSteppedPrice sp : pricedEvent.getSteppedPrices()) {
                    row = new PricedEventRow();
                    row.setPricedEvent(pricedEvent);
                    row.setSteppedPrice(sp);
                    serviceEvents.add(row);
                }
            }
        }
        return serviceEvents;
    }

}
