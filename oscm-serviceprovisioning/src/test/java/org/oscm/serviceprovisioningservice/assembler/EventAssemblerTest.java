/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.10.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Event;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOEventDefinition;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOSteppedPrice;

/**
 * Tests for the event assembler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class EventAssemblerTest {

    private VOPricedEvent evt;
    private PricedEvent doEvt;
    private LocalizerFacade facade;
    private PricedEvent pricedEvent;
    private Event event;
    private SteppedPrice steppedPrice;

    @Before
    public void setUp() throws Exception {
        evt = new VOPricedEvent();
        evt.setKey(1);
        evt.setEventPrice(BigDecimal.valueOf(123L));

        doEvt = new PricedEvent();
        doEvt.setKey(1);

        facade = new LocalizerFacade(new LocalizerServiceStub() {
            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "";
            }

            @Override
            public String getLocalizedTextFromBundle(
                    LocalizedObjectTypes objectType, Marketplace shop,
                    String localeString, String key) {
                return "";
            }
        }, "de");

        event = new Event();
        event.setKey(1);
        event.setEventIdentifier("eventIdentifier");
        event.setEventType(EventType.PLATFORM_EVENT);

        pricedEvent = new PricedEvent();
        pricedEvent.setKey(123);
        pricedEvent.setEvent(event);
        pricedEvent.setEventPrice(BigDecimal.valueOf(999));
        pricedEvent.setPriceModelKey(333);
        List<SteppedPrice> steppedPrices = new ArrayList<SteppedPrice>();
        steppedPrice = new SteppedPrice();
        steppedPrice.setKey(2);
        steppedPrices.add(steppedPrice);
        pricedEvent.setSteppedPrices(steppedPrices);
    }

    @Test
    public void constructor() throws Exception {
        // only for coverage
        EventAssembler result = new EventAssembler();
        assertNotNull(result);
    }

    @Test
    public void toPricedEvent() throws Exception {
        PricedEvent pe = EventAssembler.toPricedEvent(evt);
        assertEquals(0, pe.getVersion());
        assertEquals(BigDecimal.valueOf(123), pe.getEventPrice());
    }

    @Test
    public void updatePricedEvent() throws Exception {
        PricedEvent updatedPE = EventAssembler.updatePricedEvent(evt, doEvt);
        assertEquals(evt.getEventPrice(), updatedPE.getEventPrice());
    }

    @Test(expected = SaaSSystemException.class)
    public void updatePricedEvent_WrongKey() throws Exception {
        doEvt.setKey(2);
        EventAssembler.updatePricedEvent(evt, doEvt);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void updatePricedEvent_WrongVersion() throws Exception {
        evt.setVersion(-1);
        EventAssembler.updatePricedEvent(evt, doEvt);
    }

    @Test
    public void toVOPricedEvent() throws Exception {
        VOPricedEvent result = EventAssembler.toVOPricedEvent(pricedEvent,
                facade);
        validatePricedEvent(result);
    }

    @Test
    public void toVOPricedEvent_List() throws Exception {
        List<PricedEvent> events = new ArrayList<PricedEvent>();
        events.add(pricedEvent);
        events.add(pricedEvent);
        List<VOPricedEvent> voEvents = EventAssembler.toVOPricedEvent(events,
                facade);
        assertEquals(2, voEvents.size());
        validatePricedEvent(voEvents.get(0));
        validatePricedEvent(voEvents.get(1));
    }

    @Test
    public void toVOEventDefinitions_EmptyInput() throws Exception {
        List<Event> events = new ArrayList<Event>();
        List<VOEventDefinition> result = EventAssembler.toVOEventDefinitions(
                events, events, facade);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void toVOEventDefinitions_EmptyPlatformEvents() throws Exception {
        List<Event> pEvents = new ArrayList<Event>();
        List<Event> events = new ArrayList<Event>();
        events.add(event);
        List<VOEventDefinition> result = EventAssembler.toVOEventDefinitions(
                pEvents, events, facade);
        assertNotNull(result);
        assertEquals(1, result.size());
        VOEventDefinition evt = result.get(0);
        validateEventDefinition(evt);
    }

    @Test
    public void toVOEventDefinitions_EmptyServiceEvents() throws Exception {
        List<Event> pEvents = new ArrayList<Event>();
        List<Event> events = new ArrayList<Event>();
        pEvents.add(event);
        List<VOEventDefinition> result = EventAssembler.toVOEventDefinitions(
                pEvents, events, facade);
        assertNotNull(result);
        assertEquals(1, result.size());
        VOEventDefinition evt = result.get(0);
        validateEventDefinition(evt);
    }

    @Test
    public void toVOEventDefinitions() throws Exception {
        List<Event> pEvents = new ArrayList<Event>();
        List<Event> events = new ArrayList<Event>();
        pEvents.add(event);
        events.add(event);
        List<VOEventDefinition> result = EventAssembler.toVOEventDefinitions(
                pEvents, events, facade);
        assertNotNull(result);
        assertEquals(2, result.size());
        VOEventDefinition evt = result.get(0);
        validateEventDefinition(evt);
        evt = result.get(1);
        validateEventDefinition(evt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullEventDefinitions() throws Exception {
        new VOPricedEvent(null);
    }

    @Test(expected = ValidationException.class)
    public void validatePricedEvent_scaleTooLong() throws Exception {
        // given
        evt.setEventPrice(BigDecimal.TEN.setScale(60));

        // when
        EventAssembler.validatePricedEvent(evt);

        // then validation exception expected
    }

    // ---------------------------------------------------------------
    // internal methods

    /**
     * Validates the settings of the assembled event definition.
     * 
     * @param evt
     *            The result of the assembling to be checked.
     */
    private void validateEventDefinition(VOEventDefinition evt) {
        assertEquals(1, evt.getKey());
        assertEquals(0, evt.getVersion());
        assertEquals("", evt.getEventDescription());
        assertEquals("eventIdentifier", evt.getEventId());
        assertEquals(EventType.PLATFORM_EVENT, evt.getEventType());
    }

    /**
     * Validates the settings of the assembled VO priced event.
     * 
     * @param result
     *            The result of the assembling to be checked.
     */
    private void validatePricedEvent(VOPricedEvent result) {
        assertNotNull(result);
        assertEquals(123, result.getKey());
        assertEquals(0, result.getVersion());
        assertEquals(1, result.getEventDefinition().getKey());
        assertEquals("", result.getEventDefinition().getEventDescription());
        assertEquals("eventIdentifier", result.getEventDefinition()
                .getEventId());
        assertEquals(BigDecimal.valueOf(999), result.getEventPrice());
        List<VOSteppedPrice> steppedPrices = result.getSteppedPrices();
        assertEquals(1, steppedPrices.size());
        assertEquals(2, steppedPrices.get(0).getKey());
    }

}
