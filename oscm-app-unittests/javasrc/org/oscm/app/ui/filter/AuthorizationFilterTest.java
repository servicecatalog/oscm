/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.ui.filter;

/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) FUJITSU LIMITED - ALL RIGHTS RESERVED.                  
 *                                                                              
 *  Creation Date: 28.11.2014                                                      
 *                                                                              
 *******************************************************************************/

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.service.APPAuthenticationServiceBean;
import org.oscm.vo.VOUserDetails;

/**
 * Unit test of authorization filter
 */
public class AuthorizationFilterTest {
    private AuthorizationFilter filter;
    private FilterChain chain;
    private FilterConfig config;
    private HttpServletRequest req;
    private HttpServletResponse resp;
    private HttpSession session;

    @Before
    public void setup() throws Exception {
        filter = new AuthorizationFilter();
        filter.authService = mock(APPAuthenticationServiceBean.class);

        VOUserDetails user = new VOUserDetails();
        user.setLocale("de");
        doReturn(user).when(filter.authService).authenticateAdministrator(
                any(PasswordAuthentication.class));

        chain = mock(FilterChain.class);
        config = mock(FilterConfig.class);
        req = mock(HttpServletRequest.class);
        resp = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);

        when(req.getSession()).thenReturn(session);
        when(req.getLocale()).thenReturn(new Locale("en"));
        when(req.getServletPath()).thenReturn("/xxx");
        when(config.getInitParameter(any(String.class))).thenReturn("css");
    }

    @Test
    public void testAuthenticateLoggedIn() throws Exception {
        String credentials = "user1:password1";
        String credentialsEncoded = Base64.encodeBase64String(credentials
                .getBytes());
        when(req.getHeader(eq("Authorization"))).thenReturn(
                "Basic " + credentialsEncoded);

        filter.init(config);
        filter.doFilter(req, resp, chain);

        // Check whether request has been forwarded
        verify(session).setAttribute(eq("loggedInUserLocale"), eq("de"));
        verify(chain).doFilter(eq(req), eq(resp));
    }

    @Test
    public void testAuthenticateLogin_JA() throws Exception {
        Mockito.when(session.getAttribute(Matchers.eq("loggedInUserId")))
                .thenReturn(null);

        Mockito.when(req.getHeader(Matchers.eq("Authorization"))).thenReturn(
                null);
        Mockito.when(req.getLocale()).thenReturn(new Locale("ja"));
        // given
        filter.init(config);

        // when
        filter.doFilter(req, resp, chain);

        // then
        Mockito.verify(resp).setStatus(Matchers.eq(401));
        Mockito.verify(resp)
                .setHeader(
                        Matchers.eq("WWW-Authenticate"),
                        Matchers.startsWith("Basic realm=\"Please log in as an organization administrator\""));

    }

}
