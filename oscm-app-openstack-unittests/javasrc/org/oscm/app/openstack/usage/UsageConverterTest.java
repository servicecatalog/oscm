/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                          
 *                                                                                                                                 
 *  Creation Date: 16.10.2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.usage;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.intf.EventService;
import org.oscm.vo.VOGatheredEvent;

/**
 * @author kulle
 *
 */
public class UsageConverterTest {

    private UsageConverter converter;
    private PropertyHandler ph;

    @Before
    public void before() {
        converter = spy(new UsageConverter());
        converter.ph = ph = mock(PropertyHandler.class);

    }

    /**
     * No event should be generated if the multiplier is less than 0.
     */
    @Test
    public void submit_invalidMultiplier1() throws Exception {
        // given
        long multiplier = 0;
        String eventId = "id";
        String occurence = "2011-12-03T10:15:30";
        EventService svc = mock(EventService.class);
        doReturn(svc).when(ph).getWebService(EventService.class);

        // when
        converter.submit(eventId, multiplier, occurence);

        // then
        verifyZeroInteractions(svc);
    }

    /**
     * No event should be generated if the multiplier is less than 0.
     */
    @Test
    public void submit_invalidMultiplier2() throws Exception {
        // given
        long multiplier = -1;
        String eventId = "id";
        String occurence = "2011-12-03T10:15:30";
        EventService svc = mock(EventService.class);
        doReturn(svc).when(ph).getWebService(EventService.class);

        // when
        converter.submit(eventId, multiplier, occurence);

        // then
        verifyZeroInteractions(svc);
    }

    @Test
    public void submit() throws Exception {
        // given
        long multiplier = 1;
        String eventId = "eventId";
        String occurence = "1970-01-01T00:00:01";

        EventService svc = mock(EventService.class);
        doReturn(svc).when(ph).getWebService(EventService.class);

        PasswordAuthentication auth = mock(PasswordAuthentication.class);
        doReturn("user").when(auth).getUserName();
        doReturn(auth).when(ph).getTPAuthentication();

        ArgumentCaptor<VOGatheredEvent> event = forClass(VOGatheredEvent.class);

        // when
        converter.submit(eventId, multiplier, occurence);

        // then
        verify(svc).recordEventForInstance(anyString(), anyString(),
                event.capture());
        assertEquals(1L, event.getValue().getMultiplier());
        assertEquals(1000L, event.getValue().getOccurrenceTime());
        assertEquals("eventId_1970-01-01T00:00:01",
                event.getValue().getUniqueId());
    }

}
