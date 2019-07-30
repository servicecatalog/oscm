/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 30.07.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.internal.cache.MarketplaceConfiguration;

/**
 * @author goebel
 *
 */
public class OidcFilterTest {

    OidcFilter filter = spy(new OidcFilter());

    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private HttpSession httpSessionMock;
    private FilterChain chainMock;

    ArgumentCaptor<String> ac;

    @Before
    public void setup() throws IOException, ServletException {
        final String exclPattern = "(.*/a4j/.*|.*/img/.*|.*/css/.*|.*/fonts/.*|.*/scripts/.*|.*/faq/.*|^/slogout.jsf|^/public/.*|^/marketplace/terms/.*|^/marketplace/[^/\\?#]*([\\?#].*)?)";
        FilterConfig cfgMock = mock(FilterConfig.class);
        doReturn(exclPattern).when(cfgMock)
                .getInitParameter(eq("exclude-url-pattern"));

        filter.init(cfgMock);

        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        chainMock = mock(FilterChain.class);

        httpSessionMock = mock(HttpSession.class);

        doReturn("oscm-portal/marketplace/").when(requestMock).getServletPath();

        doReturn(httpSessionMock).when(requestMock).getSession();
        doReturn("tenanatMPL").when(httpSessionMock).getAttribute(eq("mId"));
        MarketplaceConfiguration mfc = mock(MarketplaceConfiguration.class);
        doReturn(mfc).when(filter).getConfig(anyString());
        doReturn("tenantXY").when(mfc).getTenantId();
        doNothing().when(chainMock).doFilter(any(), any());

        ac = ArgumentCaptor.forClass(String.class);

        doAnswer((new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                StringBuffer url = requestMock.getRequestURL();
                try {
                    return new URL(url.toString()).getQuery();
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        })).when(requestMock).getQueryString();
    }
   

    @Test
    public void doFilter() throws IOException, ServletException {
        // given
        givenMPLoginWithoutParameter();

        // when
        filter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(responseMock, times(1)).sendRedirect(ac.capture());

        assertRedirectUrl(ac.getValue());
    }

    @Test
    public void doFilter_noMPLLogin() throws IOException, ServletException {
        // given
        givenMPLoginWithoutParameter();

        // when
        filter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(responseMock, times(1)).sendRedirect(ac.capture());

        assertRedirectUrl(ac.getValue());
    }

    @Test
    public void doFilter_withLoginParameter()
            throws IOException, ServletException {
        // given
        givenMPLoginWithParameter();

        // when
        filter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(responseMock, times(1)).sendRedirect(ac.capture());

        assertRedirectUrl(ac.getValue());

        assertTenantId(ac.getValue());
    }

    protected void assertRedirectUrl(String urls) throws MalformedURLException {
        URL url = new URL(urls);

        assertEquals("oscmhost", url.getHost());

        assertEquals("http", url.getProtocol());

        assertEquals("/oscm-identity/login", url.getPath());
    }

    private void assertTenantId(String value) throws MalformedURLException {
        URL url = new URL(value);
        assertTenant(value);
    }

    protected void assertTenant(String anUrl) throws MalformedURLException {
        URL url = new URL(anUrl);

        String query = url.getQuery();
        
        assertNotNull(query);

        assertEquals(query, Boolean.TRUE,
                Boolean.valueOf(query.contains("tenantID=tenantXY")));
    }

    private StringBuffer sb(String val) {
        StringBuffer sb = new StringBuffer();
        sb.append(val);
        return sb;
    }

    String enc(String val) {
        try {
            return URLEncoder.encode(val, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void givenMPLoginWithoutParameter() {
        doReturn(sb("https://oscmhost:8081/oscm-portal/marketplace/index.jsf"))
                .when(requestMock).getRequestURL();
    }

    private void givenMPLoginWithParameter() {
        doReturn(sb(
                "https://oscmhost:8081/oscm-portal/marketplace/index.jsf?mId="
                        + enc("\"SampleMP\""))).when(requestMock)
                                .getRequestURL();
    }

    private void givenNoMPLogin() {
        doReturn(sb("https://oscmhost:8081/oscm-portal/index.jsf?mId="
                + enc("\"SampleMP\""))).when(requestMock).getRequestURL();
    }

}
