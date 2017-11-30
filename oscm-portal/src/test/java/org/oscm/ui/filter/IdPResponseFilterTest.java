/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 09.07.2013                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.types.exception.SessionIndexNotFoundException;
import org.oscm.saml2.api.SAMLResponseExtractor;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.Constants;

/**
 * Unit testing of {@link IdPResponseFilter}.
 * 
 * @author stavreva
 */
public class IdPResponseFilterTest {

    private static final String EMPTY_STRING = "  ";
    private static final String NOT_BLANK = "idp_url";
    private static final String RELAY_STATE = "relay_state";
    private static final String RELAY_STATE_MARKETPLACE = "/marketplace/index.jsf";
    private static final String RELAY_STATE_REGISTRATION = "/marketplace/registration.jsf";

    private IdPResponseFilter idpFilter;

    private HttpServletRequest requestMock;
    private AuthenticationSettings authSettingsMock;
    private HttpSession sessionMock;

    @Before
    public void setup() throws Exception {

        requestMock = mock(HttpServletRequest.class);
        authSettingsMock = mock(AuthenticationSettings.class);

        idpFilter = spy(new IdPResponseFilter());
        idpFilter.setAuthSettings(authSettingsMock);

        sessionMock = mock(HttpSession.class);
        doReturn(sessionMock).when(requestMock).getSession();

    }

    @Test
    public void getForwardUrl() throws Exception {
        // given
        doReturn(null).when(requestMock)
                .getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        // when
        String result = idpFilter.getForwardUrl(requestMock, RELAY_STATE);
        // then
        assertEquals(RELAY_STATE, result);
    }

    @Test
    public void getForwardUrl_marketplaceLogin() throws Exception {
        // given
        doReturn(null).when(requestMock)
                .getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        // when
        String result = idpFilter.getForwardUrl(requestMock,
                RELAY_STATE_MARKETPLACE);
        // then
        verify(idpFilter, times(1)).setRequestAttributesForAutosubmit(
                eq(requestMock), eq(RELAY_STATE_MARKETPLACE));
        assertEquals(BaseBean.MARKETPLACE_START_SITE, result);
    }

    @Test
    public void getForwardUrl_serviceLogin() throws Exception {
        // given
        doReturn(Constants.REQ_ATTR_LOGIN_TYPE_MPL).when(requestMock)
                .getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        // when
        String result = idpFilter.getForwardUrl(requestMock, RELAY_STATE);
        // then
        verify(idpFilter, times(1)).setRequestAttributesForAutosubmit(
                eq(requestMock), eq(RELAY_STATE));
        assertEquals(BaseBean.MARKETPLACE_START_SITE, result);
    }

    @Test
    public void getForwardUrl_selfRegistration() throws Exception {
        // given
        doReturn(null).when(requestMock)
                .getAttribute(Constants.REQ_ATTR_SERVICE_LOGIN_TYPE);
        // when
        String result = idpFilter.getForwardUrl(requestMock,
                RELAY_STATE_REGISTRATION);
        // then
        verify(idpFilter, times(1)).setRequestAttributesForSelfRegistration(
                eq(requestMock), eq(RELAY_STATE_REGISTRATION));
        assertEquals(RELAY_STATE_REGISTRATION, result);
    }

    @Test
    public void containsSAMLResponse_notServiceProvider() throws Exception {
        // given
        doReturn(Boolean.FALSE).when(authSettingsMock).isServiceProvider();

        // then
        assertFalse(idpFilter.containsSamlResponse(requestMock));
    }

    @Test
    public void testFilter() throws Exception {

        // given
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        FilterChain mockChain = mock(FilterChain.class);
        RequestRedirector mockRedirector = mock(RequestRedirector.class);
        AuthenticationSettings mockSettings = mock(
                AuthenticationSettings.class);
        idpFilter.setExcludeUrlPattern("servletPathOther");
        doReturn(mockSettings).when(idpFilter).getAuthenticationSettings();
        SessionBean sessionBean = mock(SessionBean.class);
        doReturn(sessionBean).when(idpFilter).getSessionBean();
        FilterConfig filterConfig = mock(FilterConfig.class);
        doReturn("exclude pattern").when(filterConfig)
                .getInitParameter("exclude-url-pattern");
        doReturn(mockSession).when(mockRequest).getSession();
        doReturn(true).when(mockSettings).isServiceProvider();
        doReturn("someSamlResponse").when(mockRequest)
                .getParameter("SAMLResponse");
        doReturn("someRelayState").when(mockRequest).getParameter("RelayState");
        doReturn("someServletPath").when(mockRequest).getServletPath();
        doReturn("someForwardURL").when(idpFilter).getForwardUrl(mockRequest,
                "someRelayState");
        SAMLResponseExtractor mockExtractor = mock(SAMLResponseExtractor.class);
        doReturn(mockExtractor).when(idpFilter).getSamlResponseExtractor();
        doReturn("").when(idpFilter).getLogoutURL();
        doReturn("").when(idpFilter).getKeyAlias();
        doReturn("").when(idpFilter).getKeystorePath();
        doReturn("").when(idpFilter).getKeystorePass();
        doReturn("").when(idpFilter).getIssuer();
        doReturn("someSAMLSessionId").when(mockExtractor)
                .getSessionIndex("someSamlResponse");
        doReturn(true).when(mockExtractor).isFromLogin("someSamlResponse");
        doReturn("").when(mockExtractor).getUserId("someSamlResponse");
        idpFilter.init(filterConfig);
        idpFilter.setRedirector(mockRedirector);
        doNothing().when(idpFilter).buildSAMLLogoutRequestAndStoreInSession(any(HttpServletRequest.class), any(String.class));
        // when
        idpFilter.doFilter(mockRequest, mockResponse, mockChain);
        // then
        verify(mockRedirector, times(1)).forward(mockRequest, mockResponse,
                "someForwardURL");
    }

    @Test
    public void testFilterSessionIndexNotFound() throws Exception {

        // given
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        doReturn("").when(mockRequest)
                .getAttribute(Constants.REQ_ATTR_ERROR_KEY);
        HttpSession mockSession = mock(HttpSession.class);
        FilterChain mockChain = mock(FilterChain.class);
        RequestRedirector mockRedirector = mock(RequestRedirector.class);
        AuthenticationSettings mockSettings = mock(
                AuthenticationSettings.class);
        SAMLResponseExtractor mockExtractor = mock(SAMLResponseExtractor.class);
        idpFilter.setExcludeUrlPattern("servletPathOther");
        doReturn(mockSettings).when(idpFilter).getAuthenticationSettings();
        SessionBean sessionBean = mock(SessionBean.class);
        doReturn(sessionBean).when(idpFilter).getSessionBean();
        FilterConfig filterConfig = mock(FilterConfig.class);
        doReturn("exclude pattern").when(filterConfig)
                .getInitParameter("exclude-url-pattern");
        doReturn(mockSession).when(mockRequest).getSession();
        doReturn(true).when(mockSettings).isServiceProvider();
        doReturn("someSamlResponse").when(mockRequest)
                .getParameter("SAMLResponse");
        doReturn("someServletPath").when(mockRequest).getServletPath();
        doReturn(mockExtractor).when(idpFilter).getSamlResponseExtractor();
        doReturn(true).when(mockExtractor).isFromLogin("someSamlResponse");
        doThrow(new SessionIndexNotFoundException()).when(mockExtractor)
                .getSessionIndex("someSamlResponse");
        idpFilter.init(filterConfig);
        idpFilter.setRedirector(mockRedirector);

        // when
        idpFilter.doFilter(mockRequest, mockResponse, mockChain);

        // verify
        verify(mockRedirector, times(1)).forward(any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(String.class));
        verify(mockRequest, times(1)).setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_INVALID_SAML_RESPONSE);
    }

    @Test
    public void testIssuerDoesNotMatch() throws Exception {

        // given
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        doReturn("").when(mockRequest)
                .getAttribute(Constants.REQ_ATTR_ERROR_KEY);
        HttpSession mockSession = mock(HttpSession.class);
        FilterChain mockChain = mock(FilterChain.class);
        RequestRedirector mockRedirector = mock(RequestRedirector.class);
        AuthenticationSettings mockSettings = mock(
                AuthenticationSettings.class);
        SAMLResponseExtractor mockExtractor = mock(SAMLResponseExtractor.class);
        idpFilter.setExcludeUrlPattern("servletPathOther");
        doReturn(mockSettings).when(idpFilter).getAuthenticationSettings();
        SessionBean sessionBean = mock(SessionBean.class);
        doReturn(sessionBean).when(idpFilter).getSessionBean();
        FilterConfig filterConfig = mock(FilterConfig.class);
        doReturn("exclude pattern").when(filterConfig)
                .getInitParameter("exclude-url-pattern");
        doReturn(mockSession).when(mockRequest).getSession();
        doReturn(true).when(mockSettings).isServiceProvider();
        doReturn("someSamlResponse").when(mockRequest)
                .getParameter("SAMLResponse");
        doReturn("someServletPath").when(mockRequest).getServletPath();
        doReturn(mockExtractor).when(idpFilter).getSamlResponseExtractor();
        doReturn(true).when(mockExtractor).isFromLogin("someSamlResponse");
        doReturn("bad").when(mockExtractor)
                .getIssuer("someSamlResponse");
        idpFilter.init(filterConfig);
        idpFilter.setRedirector(mockRedirector);

        // when
        idpFilter.doFilter(mockRequest, mockResponse, mockChain);

        // verify
        verify(mockRedirector, times(1)).forward(any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(String.class));
        verify(mockRequest, times(1)).setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_INVALID_SAML_RESPONSE);
    }

}
