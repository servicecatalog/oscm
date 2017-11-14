/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: Sep 9, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 9, 2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.intf.SessionService;

/**
 * @author tokoda
 * 
 */
public class SessionServiceWSTest {

    private SessionServiceWS serviceWS;
    private SessionService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(SessionService.class);
        serviceWS = new SessionServiceWS();
        serviceWS.wsContext = createWebServiceContextMock("127.0.0.1", "99999");
        serviceWS.delegate = serviceMock;
        PlatformUser user = mock(PlatformUser.class);
        DataService ds = mock(DataService.class);
        Mockito.when(ds.getCurrentUser()).thenReturn(user);
        serviceWS.ds = ds;
    }

    private WebServiceContext createWebServiceContextMock(String expectedIP,
            String expectedUser) {
        requestMock = mock(HttpServletRequest.class);
        when(requestMock.getRemoteAddr()).thenReturn(expectedIP);

        Principal principalMock = mock(Principal.class);
        when(principalMock.getName()).thenReturn(expectedUser);

        MessageContext msgContextMock = mock(MessageContext.class);
        when(msgContextMock.get(anyString())).thenReturn(requestMock);

        WebServiceContext wsContextMock = mock(WebServiceContext.class);
        when(wsContextMock.getUserPrincipal()).thenReturn(principalMock);
        when(wsContextMock.getMessageContext()).thenReturn(msgContextMock);

        return wsContextMock;
    }

    @Test
    public void testLogInjectionForSessionServiceWS() throws Exception {

        serviceWS.createPlatformSession(null);
        serviceWS.createServiceSession(0, null, null);
        serviceWS.deletePlatformSession(null);
        serviceWS.deleteServiceSession(0, null);
        serviceWS.deleteSessionsForSessionId(null);
        serviceWS.getSubscriptionKeysForSessionId(null);
        serviceWS.resolveUserToken(0, null, null);
        serviceWS.getNumberOfServiceSessions(0);
        serviceWS.deleteServiceSessionsForSubscription(0);

        verify(requestMock, times(9)).getRemoteAddr();

        verify(serviceMock, times(1)).createPlatformSession(null);
        verify(serviceMock, times(1)).createServiceSession(0, null, null);
        verify(serviceMock, times(1)).deletePlatformSession(null);
        verify(serviceMock, times(1)).deleteServiceSession(0, null);
        verify(serviceMock, times(1)).deleteSessionsForSessionId(null);
        verify(serviceMock, times(1)).getSubscriptionKeysForSessionId(null);
        verify(serviceMock, times(1)).resolveUserToken(0, null, null);
        verify(serviceMock, times(1)).getNumberOfServiceSessions(0);
        verify(serviceMock, times(1)).deleteServiceSessionsForSubscription(0);
    }

}
