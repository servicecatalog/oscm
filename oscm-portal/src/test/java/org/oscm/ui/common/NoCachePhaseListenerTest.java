/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-4-8                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import javax.faces.event.PhaseEvent;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.stubs.ExternalContextStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletResponseStub;

/**
 * @author Mao
 * 
 */
public class NoCachePhaseListenerTest {
    NoCachePhaseListener noCachePhaseListener;
    HttpServletResponseStub response;
    ExternalContextStub externalContext;
    FacesContextStub facesContext;
    PhaseEvent event;

    @Before
    public void setup() throws Exception {
        response = new HttpServletResponseStub() {
            private HashMap<String, String> map = new HashMap<String, String>();

            @Override
            public String getHeader(String arg0) {
                return map.get(arg0);
            }

            @Override
            public void addHeader(String key, String value) {
                map.put(key, value);
            }
        };
        event = mock(PhaseEvent.class);
        facesContext = mock(FacesContextStub.class);
        externalContext = mock(ExternalContextStub.class);

        noCachePhaseListener = new NoCachePhaseListener() {
            private static final long serialVersionUID = 4585977494122628808L;
        };

        doReturn(facesContext).when(event).getFacesContext();
        doReturn(externalContext).when(facesContext).getExternalContext();
        doReturn(response).when(externalContext).getResponse();

    }

    @Test
    public void beforePhase() throws Exception {

        // when
        noCachePhaseListener.beforePhase(event);

        // then
        assertEquals("no-cache", response.getHeader("Pragma"));
        assertEquals("no-cache,no-store,must-revalidate",
                response.getHeader("Cache-Control"));
        assertEquals("0", response.getHeader("Expires"));
    }
}
