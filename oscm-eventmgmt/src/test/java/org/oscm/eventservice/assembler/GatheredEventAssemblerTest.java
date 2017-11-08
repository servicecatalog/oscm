/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: brandstetter                                                
 *                                                                              
 *  Creation Date: 10.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.eventservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.domobjects.GatheredEvent;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOGatheredEvent;

public class GatheredEventAssemblerTest {

    private static VOGatheredEvent event;
    public static int STRING_LENGTH = 512;
    public static String TOO_LONG_STRING = "";

    // -------------------------------------------------------------------------
    private void checkEvent(GatheredEvent gatheredEvent) {
        assertNotNull(gatheredEvent);
        assertEquals(event.getActor(), gatheredEvent.getActor());
        assertEquals(event.getOccurrenceTime(),
                gatheredEvent.getOccurrenceTime());
        assertEquals(event.getEventId(), gatheredEvent.getEventId());
        assertEquals(event.getMultiplier(), gatheredEvent.getMultiplier());
        assertEquals(event.getUniqueId(), gatheredEvent.getUniqueId());
    }

    // -------------------------------------------------------------------------
    @BeforeClass
    public static void setup() throws Exception {
        // create string
        for (int i = 0; i < STRING_LENGTH; i++) {
            TOO_LONG_STRING = TOO_LONG_STRING + "x";
        }
    }

    // -------------------------------------------------------------------------
    @Before
    public void before() throws Exception {
        Random random = new Random();

        event = new VOGatheredEvent();
        event.setActor("anyUser");
        event.setOccurrenceTime(1111);
        event.setEventId("eventId");
        event.setMultiplier(1L);
        event.setUniqueId(Long.toString(random.nextLong())
                + Long.toString(System.currentTimeMillis()));
    }

    // -------------------------------------------------------------------------
    @Test
    public void testToGatheredEvent_GoodCase() throws Exception {

        GatheredEvent gatheredEvent = GatheredEventAssembler
                .toGatheredEvent(event);

        checkEvent(gatheredEvent);
    }

    // -------------------------------------------------------------------------
    @Test(expected = ValidationException.class)
    public void testToGatheredEvent_TooLongActor() throws Exception {

        event.setActor(TOO_LONG_STRING);

        try {
            GatheredEventAssembler.toGatheredEvent(event);
        } catch (ValidationException e) {

            assertTrue(Arrays.asList(e.getMember()).contains("actor"));
            assertTrue(Arrays.asList(e.getMessageParams()).contains(
                    TOO_LONG_STRING));
            assertEquals(ReasonEnum.LENGTH, e.getReason());
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    @Test(expected = ValidationException.class)
    public void testToGatheredEvent_OccurrenceTimeWrong() throws Exception {

        event.setOccurrenceTime(-1);

        try {
            GatheredEventAssembler.toGatheredEvent(event);
        } catch (ValidationException e) {
            assertTrue(Arrays.asList(e.getMember()).contains("occurrenceTime"));
            assertTrue(Arrays.asList(e.getMessageParams()).contains("-1"));
            assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE, e.getReason());
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    @Test
    public void testToGatheredEvent_TooLongEventId() throws Exception {

        event.setEventId(TOO_LONG_STRING);

        GatheredEvent gatheredEvent = GatheredEventAssembler
                .toGatheredEvent(event);

        // not checked here
        checkEvent(gatheredEvent);
    }

    // -------------------------------------------------------------------------
    @Test(expected = ValidationException.class)
    public void testToGatheredEvent_MultiplierWrong() throws Exception {

        // "old"(unchanged) v1.1 setter sets the multiplier to "1"
        // the multiplier is set to "-1" via reflection
        Field declaredField = VOGatheredEvent.class
                .getDeclaredField("multiplier");
        declaredField.setAccessible(true);
        declaredField.setLong(event, -1);

        try {
            GatheredEventAssembler.toGatheredEvent(event);
        } catch (ValidationException e) {
            assertTrue(Arrays.asList(e.getMember()).contains("multiplier"));
            assertTrue(Arrays.asList(e.getMessageParams()).contains("-1"));
            assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE, e.getReason());
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    @Test(expected = ValidationException.class)
    public void testToGatheredEvent_TooLongUniqueId() throws Exception {

        event.setUniqueId(TOO_LONG_STRING);

        try {
            GatheredEventAssembler.toGatheredEvent(event);
        } catch (ValidationException e) {
            assertTrue(Arrays.asList(e.getMember()).contains("uniqueId"));
            assertTrue(Arrays.asList(e.getMessageParams()).contains(
                    TOO_LONG_STRING));
            assertEquals(ReasonEnum.LENGTH, e.getReason());
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    @Test
    public void testToGatheredEvent_EventNull() throws Exception {
        GatheredEvent gatheredEvent = GatheredEventAssembler
                .toGatheredEvent(null);
        assertNull(gatheredEvent);
    }

    // -------------------------------------------------------------------------
    @Test
    public void testToGatheredEvent_ActorNull() throws Exception {

        event.setActor(null);
        GatheredEvent gatheredEvent = GatheredEventAssembler
                .toGatheredEvent(event);
        checkEvent(gatheredEvent);
    }

    // -------------------------------------------------------------------------
    @Test
    public void testToGatheredEvent_EventIdNull() throws Exception {

        event.setEventId(null);
        GatheredEvent gatheredEvent = GatheredEventAssembler
                .toGatheredEvent(event);
        checkEvent(gatheredEvent);
    }

    // -------------------------------------------------------------------------
    @Test
    public void testToGatheredEvent_UniqueIdNull() throws Exception {

        event.setUniqueId(null);
        GatheredEvent gatheredEvent = GatheredEventAssembler
                .toGatheredEvent(event);
        checkEvent(gatheredEvent);
    }
}
