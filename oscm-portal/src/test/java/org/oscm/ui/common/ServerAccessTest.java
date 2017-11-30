/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.stubs.HttpSessionStub;
import org.oscm.internal.intf.SessionService;

public class ServerAccessTest {

    HttpSessionStub session;

    @Before
    public void setUp() throws Exception {

        session = new HttpSessionStub(Locale.ENGLISH) {
            Map<String, Object> attributes = new HashMap<String, Object>();

            @Override
            public Object getAttribute(String key) {
                return attributes.get(key);
            }

            @Override
            public void setAttribute(String key, Object val) {
                attributes.put(key, val);
            }

        };
    }

    @Test
    public void testSessionLogin() throws Exception {
        final String sessionid = "hjfdksh";
        SessionService sessionServiceMock = mock(SessionService.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                Assert.assertEquals(sessionid, args[0]);
                return null;
            }
        }).when(sessionServiceMock).createPlatformSession(anyString());

        ServiceAccess serviceAccessMock = mock(ServiceAccess.class);
        when(serviceAccessMock.getService(eq(SessionService.class)))
                .thenReturn(sessionServiceMock);

        session.setAttribute(ServiceAccess.SESS_ATTR_SERVICE_ACCESS,
                serviceAccessMock);
        session.setId(sessionid);

        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        when(requestMock.getSession()).thenReturn(session);

        new EJBServiceAccess().createPlatformSession(requestMock);
    }
}
