/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 14.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOSteppedPrice;

public class SteppedPriceHandlerTest {
    @Test
    public void setEvents() {
        // given
        List<VOPricedEvent> pricedEvents = givenPricedEvents();

        // when
        List<PricedEventRow> result = SteppedPriceHandler
                .buildPricedEvents(pricedEvents);

        // then
        assertEquals(pricedEvents.size(), result.size());
        assertTrue(pricedEvents.get(0) == result.get(0).getPricedEvent());
    }

    private List<VOPricedEvent> givenPricedEvents() {
        List<VOPricedEvent> pricedEvents = new LinkedList<VOPricedEvent>();

        VOPricedEvent event = new VOPricedEvent();
        event.setSteppedPrices(new LinkedList<VOSteppedPrice>());
        pricedEvents.add(new VOPricedEvent());

        return pricedEvents;
    }

}
