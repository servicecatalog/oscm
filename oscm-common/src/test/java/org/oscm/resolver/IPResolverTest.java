/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                
 *                                                                              
 *  Creation Date: Sep 14, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 14, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.resolver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.junit.Before;
import org.junit.Test;

/**
 * @author tokoda
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class IPResolverTest {

    HttpServletRequest httpRequestMock;
    WebServiceContext wsContextMock;

    @Before
    public void setup() {

        httpRequestMock = mock(HttpServletRequest.class);
        when(httpRequestMock.getHeader("x-forwarded-for"))
                .thenReturn("1.1.1.1");
        when(httpRequestMock.getHeader("X-Forwarded-For"))
                .thenReturn("1.1.1.2");
        when(httpRequestMock.getRemoteAddr()).thenReturn("255.255.255.255");
        wsContextMock = mock(WebServiceContext.class);
        MessageContext msgContextMock = mock(MessageContext.class);
        when(msgContextMock.get(MessageContext.SERVLET_REQUEST)).thenReturn(
                httpRequestMock);
        when(wsContextMock.getMessageContext()).thenReturn(msgContextMock);
    }

    @Test
    public void testConstructor() {
        new IPResolver();
    }

    @Test
    public void testResolveIpAddressFromWebServiceContext() {
        Enumeration headerEnumMock = mock(Enumeration.class);
        when(Boolean.valueOf(headerEnumMock.hasMoreElements())).thenReturn(
                Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        when(headerEnumMock.nextElement()).thenReturn("something",
                "x-forwarded-for");
        when(httpRequestMock.getHeaderNames()).thenReturn(headerEnumMock);

        String ipAddress = IPResolver.resolveIpAddress(wsContextMock);
        assertEquals("1.1.1.1", ipAddress);
    }

    @Test
    public void testResolveIpAddressFromHeader() {
        Enumeration headerEnumMock = mock(Enumeration.class);
        when(Boolean.valueOf(headerEnumMock.hasMoreElements())).thenReturn(
                Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        when(headerEnumMock.nextElement()).thenReturn("something",
                "x-forwarded-for");
        when(httpRequestMock.getHeaderNames()).thenReturn(headerEnumMock);

        String ipAddress = IPResolver.resolveIpAddress(httpRequestMock);
        assertEquals("1.1.1.1", ipAddress);
    }

    @Test
    public void testResolveIpAddressFromHeaderUpperCase() {
        Enumeration headerEnumMock = mock(Enumeration.class);
        when(Boolean.valueOf(headerEnumMock.hasMoreElements())).thenReturn(
                Boolean.TRUE);
        when(headerEnumMock.nextElement()).thenReturn("X-Forwarded-For");
        when(httpRequestMock.getHeaderNames()).thenReturn(headerEnumMock);

        String ipAddress = IPResolver.resolveIpAddress(httpRequestMock);
        assertEquals("1.1.1.2", ipAddress);
    }

    @Test
    public void testResolveIpAddressFromRemoteAddr() {
        Enumeration headerEnumMock = mock(Enumeration.class);
        when(Boolean.valueOf(headerEnumMock.hasMoreElements())).thenReturn(
                Boolean.FALSE);
        when(httpRequestMock.getHeaderNames()).thenReturn(headerEnumMock);

        String ipAddress = IPResolver.resolveIpAddress(httpRequestMock);
        assertEquals("255.255.255.255", ipAddress);
    }

}
