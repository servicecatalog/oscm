/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 14.10.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices.handler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.junit.Before;
import org.junit.Test;

public class LoginHandlerTest {

    private LoginHandler loginHandler;
    private SOAPMessageContext context; 

    @Before
    public void setup() throws Exception {

        loginHandler = spy(new LoginHandler());
        context = mock(SOAPMessageContext.class);       
    }

    @Test
    public void testOutbundTypeOk() {

        // given
        when(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))
                .thenReturn(true);

        // when
        boolean result = loginHandler.handleMessage(context);

        // then
        assertTrue(result);
    }
}
