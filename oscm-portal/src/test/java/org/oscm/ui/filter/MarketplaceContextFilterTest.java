/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Peter Pock                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: <date>                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Any;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.ServiceAccess;

/**
 * Tests for correct behavior of MarketplaceContext Filter which is supposed to
 * determine the marketplace context (either since we actually need to know the
 * current marketplace or of brand picking) and/or perform redirects according
 * to the context of the given request.
 * 
 * @author groch
 * 
 */
public class MarketplaceContextFilterTest {

    static final String MARKETPLACE_CHANGEPASSWORD_URL = "/oscm-portal/marketplace/changePassword.jsf";

    static final String BLUE_PORTAL_URL = "/oscm-portal";

    static final String BLUE_PORTAL_CHANGEPASSWORD_URL = "/oscm-portal/public/changePassword.jsf";
    final static String INVALID_URL_ENCODED_TOKEN = "Y3VfY3UmMTM2NDQ1NTM0MDEwMCZjY2YzN2JiZiY%3";
    private MarketplaceContextFilter mpCtxFilter;
    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private FilterChain chainMock;
    private PrintWriter writerMock;

    private HttpSession sessionMock;
    private MarketplaceService mpSvcMock;
    private ConfigurationService confSvcMock;
    private VOUserDetails voUserMock;
    private AuthorizationRequestData rdoMock;

    private static String forwardUrl;
    private static String sessionMid;
    private static String errorPage = BaseBean.MARKETPLACE_ERROR_PAGE;
    private static String requestUri;
    private static Map<String, String> cookieMap;

    @Before
    public void setUp() throws Exception {

        cookieMap = new HashMap<String, String>();

        requestMock = mock(HttpServletRequest.class);
        // default servlet path avoids NPE, override for specific tests
        when(requestMock.getServletPath()).thenReturn("");
        when(requestMock.getParameter(Constants.REQ_PARAM_MARKETPLACE_ID))
                .thenReturn("");
        when(requestMock.getContextPath()).thenReturn("xxxx");
        when(requestMock.getRequestURL()).thenReturn(
                new StringBuffer("http://thisisaurl/xxxx")
                        .append(BaseBean.MARKETPLACE_START_SITE));
        requestUri = "xxxx" + BaseBean.MARKETPLACE_START_SITE;

        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);
        when(requestMock.getSession(true)).thenReturn(sessionMock);

        // fake dynamic set/get session behavior
        sessionMid = null;
        doAnswer(((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            assertEquals(2, args.length);
            String attrKey = (String) args[0];
            if (Constants.REQ_PARAM_MARKETPLACE_ID.equals(attrKey)) {
                sessionMid = (String) args[1];
            }
            return null;
        })).when(sessionMock).setAttribute(
                Matchers.eq(Constants.REQ_PARAM_MARKETPLACE_ID),
                any(String.class));
        doAnswer(((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            String attrKey = (String) args[0];
            if (Constants.REQ_PARAM_MARKETPLACE_ID.equals(attrKey)) {
                return sessionMid;
            }
            return null;
        })).when(sessionMock).getAttribute(
                Matchers.eq(Constants.REQ_PARAM_MARKETPLACE_ID));
        voUserMock = new VOUserDetails();

        doReturn(voUserMock).when(sessionMock).getAttribute(
                Constants.SESS_ATTR_USER);
        rdoMock = mock(AuthorizationRequestData.class);
        responseMock = mock(HttpServletResponse.class);

        chainMock = mock(FilterChain.class);

        writerMock = mock(PrintWriter.class);
        when(responseMock.getWriter()).thenReturn(writerMock);

        mpSvcMock = mock(MarketplaceService.class);
        confSvcMock = mock(ConfigurationService.class);

        mpCtxFilter = spy(new MarketplaceContextFilter());
        mpCtxFilter.authSettings = mock(AuthenticationSettings.class);
        doReturn(mpSvcMock).when(mpCtxFilter).getMarketplaceService(
                any(HttpSession.class));
        doReturn(confSvcMock).when(mpCtxFilter).getConfigurationService(
                any(HttpServletRequest.class));
        forwardUrl = null;
        doAnswer(((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            assertEquals(3, args.length);
            forwardUrl = (String) args[0];
            return null;
        })).when(mpCtxFilter).forward(any(String.class),
                any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    private VOUserDetails fakeUserLogin(final String orgId) {
        VOUserDetails userMock = mock(VOUserDetails.class);
        userMock.setOrganizationId(orgId);
        when(sessionMock.getAttribute(Constants.SESS_ATTR_USER)).thenReturn(
                userMock);
        when(userMock.getOrganizationId()).thenReturn(orgId);
        return userMock;
    }

    private final void fakeSetSessionMid(String mId) {
        sessionMid = mId;
        when((sessionMock).getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID))
                .thenReturn(mId);
    }

    private final String fakeSetCookieMid(final String cookieAttr,
            final String cookieValue) {
        if (cookieValue == null) {
            cookieMap.remove(cookieAttr);
        } else {
            cookieMap.put(cookieAttr, cookieValue);
        }
        Set<Cookie> cookieSet = new HashSet<Cookie>();
        for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
            cookieSet.add(new Cookie(entry.getKey(), entry.getValue()));
        }
        when(requestMock.getCookies()).thenReturn(
                cookieSet.toArray(new Cookie[] {}));

        doAnswer((new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String attrKey = (String) args[1];
                if (cookieMap.containsKey(attrKey)) {
                    return cookieMap.get(attrKey);
                }
                return null;
            }
        })).when(mpCtxFilter).getCookieValue(any(HttpServletRequest.class),
                any(String.class));

        return cookieValue;
    }

    private void fakeRequest(final String mId, final String servletPath) {
        if (mId != null) {
            when(requestMock.getParameter(Constants.REQ_PARAM_MARKETPLACE_ID))
                    .thenReturn(mId);
        }
        if (servletPath != null) {
            when(requestMock.getServletPath()).thenReturn(servletPath);
        }
    }

    private void fakeCreateMarketplace(final String mId)
            throws ObjectNotFoundException {
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId(mId);
        when(mpSvcMock.getMarketplaceById(mId)).thenReturn(mp);
    }

    @Test
    public void doFilter_pwdChangeRedirect() throws Exception {
        final String mId = "FUJITSU";
        final String servletPath = "/marketplace/services.jsf";
        fakeRequest(mId, servletPath);
        fakeCreateMarketplace(mId);
        voUserMock.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);

        doReturn(
                new VOConfigurationSetting(ConfigurationKey.BASE_URL,
                        "anyString", "http://idp.de/openam/SSORedirect")).when(
                confSvcMock).getVOConfigurationSetting(
                any(ConfigurationKey.class), anyString());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);
        verify(sessionMock, times(1)).setAttribute(
                Constants.SESS_ATTR_FORWARD_URL, servletPath);
        verify(requestMock, times(1)).setAttribute(
                Constants.REQ_ATTR_LOGIN_REDIRECT_TARGET, servletPath);
        verify(responseMock, times(1)).sendRedirect(any(String.class));

    }

    @Test
    public void doFilter_pwdChangeWithToken() throws Exception {
        final String mId = "FUJITSU";
        final String servletPath = "/marketplace/services.jsf";
        fakeRequest(mId, servletPath);
        fakeCreateMarketplace(mId);
        doReturn("token").when(requestMock).getParameter(
                BesServletRequestReader.REQ_PARAM_PASSWORD_CHANGE_TOKEN);
        voUserMock.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);
        verify(sessionMock, never()).setAttribute(
                eq(Constants.SESS_ATTR_FORWARD_URL), any(String.class));
        verify(requestMock, never())
                .setAttribute(eq(Constants.REQ_ATTR_LOGIN_REDIRECT_TARGET),
                        any(String.class));
        verify(responseMock, never()).sendRedirect(any(String.class));
    }

    @Test
    public void doFilter_pwdChangeOnLoginPage() throws Exception {
        final String mId = "FUJITSU";
        final String servletPath = BaseBean.MARKETPLACE_LOGIN_PAGE;
        fakeRequest(mId, servletPath);
        fakeCreateMarketplace(mId);
        doReturn("token").when(requestMock).getParameter(
                BesServletRequestReader.REQ_PARAM_PASSWORD_CHANGE_TOKEN);
        voUserMock.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);
        verify(sessionMock, never()).setAttribute(
                eq(Constants.SESS_ATTR_FORWARD_URL), any(String.class));
        verify(requestMock, never())
                .setAttribute(eq(Constants.REQ_ATTR_LOGIN_REDIRECT_TARGET),
                        any(String.class));
        verify(responseMock, never()).sendRedirect(any(String.class));
    }

    /* tests with given url, focus on validation of given url */

    @Test
    public void doFilter_noMidInGivenUrl() throws Exception {
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(mpSvcMock, never()).getMarketplaceById(any(String.class));
        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, Any.class);
    }

    @Test
    public void doFilter_givenUrlMidIsValid() throws Exception {
        final String mId = "fdfb4933";
        fakeRequest(mId, null);
        when(mpSvcMock.getMarketplaceById(mId)).thenReturn(new VOMarketplace());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, times(1)).setAttribute(
                Matchers.eq(Constants.REQ_PARAM_MARKETPLACE_ID),
                Matchers.eq(mId));
        // no MPL portal request, thus no redirect required
        verify(responseMock, never()).sendRedirect(
                Matchers.contains(Marketplace.PUBLIC_CATALOG_SITE));
        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();
        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, Any.class);

        assertEquals(mId, sessionMid);
    }

    @Test
    // this also verifies the "mId=DEFAULT_NAME" case
    public void doFilter_givenUrlMidIsInvalid_AdministrationPortalContext()
            throws Exception {
        final String mId = "fdfb4933";
        fakeRequest(mId, null);
        when(mpSvcMock.getMarketplaceById(mId)).thenThrow(
                new ObjectNotFoundException());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, atLeastOnce()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, null);
        verify(mpCtxFilter, never()).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

        assertEquals(null, sessionMid);
    }

    @Test
    // this also verifies the "mId=DEFAULT_NAME" case
    public void doFilter_givenUrlMidIsInvalid_MPLcontext() throws Exception {
        final String mId = "FUJITSU";
        final String servletPath = "/marketplace/index.jsf";
        fakeRequest(mId, servletPath);
        final String defaultUrl = "http://myDefaultUrl.com";

        when(mpSvcMock.getMarketplaceById(mId)).thenThrow(
                new ObjectNotFoundException());
        doReturn(defaultUrl).when(mpCtxFilter).getDefaultUrl(
                any(ServiceAccess.class), any(AuthorizationRequestData.class),
                any(HttpServletRequest.class));

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, atLeastOnce()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, null);

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

        // expected redirect to default page in blue portal
        assertEquals(defaultUrl, forwardUrl);
        verify(mpCtxFilter, times(1)).forward(Matchers.eq(defaultUrl),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
        assertEquals(null, sessionMid);
    }

    @Test
    public void doFilter_givenUrlMidIsInvalid_MPLErrorPage() throws Exception {
        final String mId = "FUJITSU";
        final String servletPath = "/marketplace/errorPage.jsf";
        fakeRequest(mId, servletPath);

        when(mpSvcMock.getMarketplaceById(mId)).thenThrow(
                new ObjectNotFoundException());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, atLeastOnce()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, null);

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

        // shouldn't forward any other page
        verify(mpCtxFilter, never()).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
        assertEquals(null, sessionMid);
    }

    /* tests where mId is not given in URL, focus on fallback */

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_noUserLoggedIn()
            throws Exception {
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // fallback is not invoked since no mId can be resolved
        verify(sessionMock, times(1)).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, null);

        assertEquals(null, sessionMid);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_mIdStoredInCookie()
            throws Exception {
        final String cookieId = fakeSetCookieMid(
                Constants.REQ_PARAM_MARKETPLACE_ID, "myMpId");
        fakeUserLogin("myOrgId");
        fakeCreateMarketplace(cookieId);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, atLeastOnce()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, cookieId);

        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, Any.class);

        assertEquals(cookieId, sessionMid);
    }

    @Test
    public void doFilter_mIdInGivenUrl_previousmIdStoredInCookie()
            throws Exception {
        // Test objective:
        // ensure that if mId is given explicitly in URL, it must not be
        // retrieved via fallback (via subscription key or cookie)
        final String cookieId = fakeSetCookieMid(
                Constants.REQ_PARAM_MARKETPLACE_ID, "myCookieId");
        final String mId = "fdfb4933";
        fakeRequest(mId, null);
        when(mpSvcMock.getMarketplaceById(mId)).thenReturn(new VOMarketplace());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);
        (verify(requestMock, times(1))).getCookies();
        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, cookieId);

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

    }

    @Test
    public void doFilter_MpRedirect() throws Exception {
        when(requestMock.getServletPath()).thenReturn(
                BaseBean.MARKETPLACE_REDIRECT);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // MPL portal request, thus no redirect required
        verify(responseMock, times(1)).sendRedirect(
                Matchers.contains(Marketplace.MARKETPLACE_ROOT));
        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();
        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, Any.class);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_previousmIdStoredInCookieButMpDeletedMeanwhile()
            throws Exception {
        final String cookieId = fakeSetCookieMid(
                Constants.REQ_PARAM_MARKETPLACE_ID, "myMpId");
        fakeUserLogin("myOrgId");
        fakeCreateMarketplace(cookieId);
        when(mpSvcMock.getMarketplaceById(cookieId)).thenThrow(
                new ObjectNotFoundException());
        when(mpSvcMock.getMarketplaceById("myOrgId")).thenThrow(
                new ObjectNotFoundException());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, cookieId);

        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, Any.class);

        assertEquals(null, sessionMid);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_MPLcontext()
            throws Exception {
        fakeUserLogin("myOrgId");
        fakeRequest(null, "/marketplace/index.jsf");

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, Any.class);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_loggedInUserOrgHasNoLocalMp()
            throws Exception {
        String orgId = "myOrgId";
        VOUserDetails userMock = fakeUserLogin(orgId);
        // since there is no marketplace matching the organization id,
        // getMarketplaceById() will throw an ObjectNotFoundException
        when(mpSvcMock.getMarketplaceById(orgId)).thenThrow(
                new ObjectNotFoundException(ClassEnum.MARKETPLACE, orgId));

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(userMock, times(1)).getOrganizationId();
        // mId will be reset to null on invalid mId
        verify(sessionMock, times(1)).setAttribute(
                Matchers.eq(Constants.REQ_PARAM_MARKETPLACE_ID), eq(null));

        assertEquals(null, sessionMid);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_nUserLoggedIn_sIdGiven()
            throws Exception {
        final String sId = "c3723223";
        fakeCreateMarketplace(sId);

        // login user and verify that fallback for currently logged-in user does
        // not apply since sId is given
        VOUserDetails userMock = fakeUserLogin("myOrgId");

        when(requestMock.getParameter(Constants.REQ_PARAM_SUPPLIER_ID))
                .thenReturn(sId);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, times(1)).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, sId);
        verify(userMock, never()).getOrganizationId();

        assertEquals(sId, sessionMid);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_serviceAccess()
            throws Exception {
        final String subKey = "12345678";
        final String mpId = "myMpId";
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId(mpId);

        when(requestMock.getServletPath())
                .thenReturn("/opt/" + subKey + "/xyz");
        when(
                mpSvcMock.getMarketplaceForSubscription(
                        ADMStringUtils.parseUnsignedLong(subKey), "en"))
                .thenReturn(mp);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, null);
        assertEquals(null, sessionMid);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_serviceAccessThrowsException()
            throws Exception {
        final String subKey = "12345678";
        final String mpId = "myMpId";
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId(mpId);

        when(requestMock.getServletPath())
                .thenReturn("/opt/" + subKey + "/xyz");
        when(
                mpSvcMock.getMarketplaceForSubscription(
                        ADMStringUtils.parseUnsignedLong(subKey), "en"))
                .thenThrow(new ObjectNotFoundException());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, mpId);

        assertEquals(null, sessionMid);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_serviceAccessViaRequestParam()
            throws Exception {
        final String subKey = "12345678";
        final String mpId = "myMpId";
        VOMarketplace mp = new VOMarketplace();
        mp.setMarketplaceId(mpId);

        when(requestMock.getServletPath()).thenReturn("/opt//xyz");
        when(requestMock.getParameter(Constants.REQ_PARAM_SUB_KEY)).thenReturn(
                subKey);
        when(
                mpSvcMock.getMarketplaceForSubscription(
                        ADMStringUtils.parseUnsignedLong(subKey), "en"))
                .thenReturn(mp);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(sessionMock, never()).setAttribute(
                Constants.REQ_PARAM_MARKETPLACE_ID, null);

        assertEquals(null, sessionMid);
    }

    @Test
    public void doFilter_redirectToMPErrorPage() throws Exception {
        // given
        String mpRedirect = "http://thisisaurl/xxxx"
                + BaseBean.MARKETPLACE_START_SITE;
        when(requestMock.getServletPath()).thenReturn(
                BaseBean.MARKETPLACE_START_SITE);
        when(requestMock.getRequestURI()).thenReturn(
                "xxxx" + BaseBean.MARKETPLACE_START_SITE);
        doReturn(mpRedirect).when(mpCtxFilter).getRedirectMpUrlHttp(
                any(ConfigurationService.class));
        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);
        // then
        verify(mpCtxFilter, times(1)).sendRedirect(
                any(HttpServletRequest.class), Matchers.eq(responseMock),
                anyString());
    }

    @Test
    public void doFilter_redirectToConfiguredMPErrorRedirectPage()
            throws Exception {
        // given
        String mpRedirect = "http://thisisaurl/xxxx/marketplace/?mId=aaa";
        when(requestMock.getServletPath()).thenReturn(
                BaseBean.MARKETPLACE_START_SITE);
        when(requestMock.getRequestURI()).thenReturn(
                "xxxx" + BaseBean.MARKETPLACE_START_SITE);
        doReturn(mpRedirect).when(mpCtxFilter).getRedirectMpUrlHttp(
                any(ConfigurationService.class));
        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);
        // then
        verify(responseMock, times(1)).sendRedirect(Matchers.eq(mpRedirect));
        verify(sessionMock, never()).invalidate();
        verify(mpCtxFilter, never()).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void doFilter_MPLogin() throws Exception {
        final String redirect = "abc";

        when(requestMock.getServletPath()).thenReturn(
                BaseBean.MARKETPLACE_LOGIN_PAGE);
        when(
                requestMock.getSession().getAttribute(
                        Constants.SESS_ATTR_FORWARD_URL)).thenReturn(redirect);
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(requestMock, never()).setAttribute(
                Constants.REQ_ATTR_SERVICE_LOGIN_TYPE, redirect);
        verify(requestMock, times(1)).setAttribute(
                Constants.REQ_ATTR_LOGIN_REDIRECT_TARGET, redirect);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_serviceAccess_sessionMidSet()
            throws Exception {
        final String subKey = "12345678";

        String initialSessionMid = "sessionMid";
        fakeSetSessionMid(initialSessionMid);
        when(requestMock.getServletPath())
                .thenReturn("/opt/" + subKey + "/xyz");

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());

        assertEquals(initialSessionMid, sessionMid);
    }

    @Test
    public void doFilter_noValidMidInGivenUrl_noSessionMid_loggedInUserOrgHasLocalMp()
            throws Exception {
        String orgId = "myOrgId";
        VOUserDetails userMock = fakeUserLogin(orgId);
        fakeCreateMarketplace(orgId);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(userMock, times(1)).getOrganizationId();
        verify(sessionMock, times(1)).setAttribute(
                Matchers.eq(Constants.REQ_PARAM_MARKETPLACE_ID),
                Matchers.eq(orgId));

        assertEquals(orgId, sessionMid);
    }

    @Test
    public void doFilter_mIdIsGivenInUrl_noSessionMid_loggedInUserOrgHasLocalMp()
            throws Exception {
        // Test objective:
        // ensure that if mId is given explicitly in URL, it must not be
        // retrieved via fallback (local mp of org of logged-in user)
        final String mId = "fdfb4933";
        fakeRequest(mId, null);
        fakeCreateMarketplace(mId);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // no MPL portal request, thus no redirect required
        verify(responseMock, never()).sendRedirect(
                Matchers.contains(Marketplace.PUBLIC_CATALOG_SITE));

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

        assertEquals(mId, sessionMid);
    }

    @Test
    public void doFilter_accessGlobalMPinMPLcontext() throws Exception {
        // accessing a global MP in the MPL brand is fine, NO redirect
        final String mId = "FUJITSU";
        final String servletPath = "/marketplace/index.jsf";
        fakeRequest(mId, servletPath);
        fakeCreateMarketplace(mId);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // MPL portal request and trying to access global MP, thus no redirect
        verify(responseMock, never()).sendRedirect(
                Matchers.contains(Marketplace.PUBLIC_CATALOG_SITE));

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

        assertEquals(mId, sessionMid);
    }

    @Test
    public void doFilter_illegalChractersInParamerter_Bug8830()
            throws Exception {

        // Test a redirect with illegal parameter in query string
        // (Return and line feed characters are forbidden in cookies values and
        // redirect URLs to avoid HTTP header manipulation vulnerability
        // (response splitting).
        final String forbiddenURL = "http://www.xyz.de?forbidden=Test\n\rNo!";
        try {
            JSFUtils.sendRedirect(responseMock, forbiddenURL);
            fail(IllegalArgumentException.class.getName() + " expected.");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().indexOf(forbiddenURL) > -1);
        }
        verify(responseMock, never()).sendRedirect(Matchers.contains("\n"));
        verify(responseMock, never()).sendRedirect(Matchers.contains("\r"));
    }

    @Test
    public void doFilter_accessGlobalMPinAdministrationPortalContext()
            throws Exception {
        // accessing a global MP in the Administration Portal is fine, NO
        // redirect
        final String mId = "OSCM";
        fakeRequest(mId, null);
        fakeCreateMarketplace(mId);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // MPL portal request and trying to access global MP, thus no redirect
        verify(responseMock, never()).sendRedirect(
                Matchers.contains(Marketplace.PUBLIC_CATALOG_SITE));

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

        assertEquals(mId, sessionMid);
    }

    @Test
    public void doFilter_accessLocalMPinAdministrationPortalContext()
            throws Exception {
        // accessing a global MP in the MPL brand is fine, NO redirect
        final String mId = "fdfb4933";
        fakeRequest(mId, null);
        fakeCreateMarketplace(mId);

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // MPL portal request and trying to access global MP, thus no redirect
        verify(responseMock, never()).sendRedirect(
                Matchers.contains(Marketplace.PUBLIC_CATALOG_SITE));

        // if mId given, do not use fallback via subscription key or cookie
        verify(mpSvcMock, never()).getMarketplaceForSubscription(
                Matchers.anyLong(), Matchers.anyString());
        verify(requestMock, times(1)).getCookies();

        assertEquals(mId, sessionMid);
    }

    /*
     * tests where mId has been resolved, focus on special global MP handling
     */

    @Test
    public void doFilter_accessGlobalMPwithoutMIdSet() throws Exception {
        // accessing a global MP in the MPL brand is fine, NO redirect
        final String servletPath = "/marketplace/index.jsf";
        fakeRequest(null, servletPath);
        final String defaultUrl = "http://myDefaultUrl.com";

        when(sessionMock.getAttribute(Constants.REQ_PARAM_MARKETPLACE_ID))
                .thenReturn(null);
        doReturn(defaultUrl).when(mpCtxFilter).getDefaultUrl(
                any(ServiceAccess.class), any(AuthorizationRequestData.class),
                any(HttpServletRequest.class));
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // expected redirect to default page in blue portal
        assertEquals(defaultUrl, forwardUrl);
        verify(mpCtxFilter, times(1)).forward(Matchers.eq(defaultUrl),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void doFilter_marketplaceServiceCalledOnlyOnce() throws Exception {
        final String mId = "fdfb4933";
        fakeRequest(mId, null);
        when(mpSvcMock.getMarketplaceById(mId)).thenReturn(new VOMarketplace());

        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        verify(mpSvcMock, times(1)).getMarketplaceById(mId);
    }

    @SuppressWarnings("boxing")
    @Test
    public void hasInvalidChangePasswordToken_invalid() throws Exception {

        // given
        HttpServletRequest request = givenChangePasswordRequestInvalidParameter();

        // when
        boolean result = mpCtxFilter.hasInvalidChangePasswordToken(request);

        // then
        assertEquals(Boolean.TRUE, result);

    }

    @SuppressWarnings("boxing")
    @Test
    public void hasInvalidChangePasswordToken_valid() throws Exception {
        // given
        HttpServletRequest request = givenChangePasswordRequestValidParameter();

        // when
        boolean result = mpCtxFilter.hasInvalidChangePasswordToken(request);

        // then
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void Portal_PasswordRecovery_ValidRequestURI_ValidQueryString()
            throws Exception {
        // given
        when(requestMock.getRequestURI()).thenReturn(
                BLUE_PORTAL_CHANGEPASSWORD_URL);

        when(requestMock.getQueryString())
                .thenReturn(
                        "mId=f7ce314e&token=Y3VzdG9tZXJfMTM2NTc0OTUzMjA3OCYxMzY1NzUxMDg1NjE4JmY3Y2UzMTRlJg%3D%3D&et");

        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(mpCtxFilter, times(0)).handleInvalidURL(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthorizationRequestData.class));
        verify(mpCtxFilter, times(0)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void PasswordRecovery_ValidRequestURI_ValidQueryString()
            throws Exception {
        // given
        when(requestMock.getRequestURI()).thenReturn(
                MARKETPLACE_CHANGEPASSWORD_URL);

        when(requestMock.getQueryString())
                .thenReturn(
                        "mId=f7ce314e&token=Y3VzdG9tZXJfMTM2NTc0OTUzMjA3OCYxMzY1NzUxMDg1NjE4JmY3Y2UzMTRlJg%3D%3D&et");

        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(mpCtxFilter, times(0)).handleInvalidURL(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthorizationRequestData.class));
        verify(mpCtxFilter, times(0)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void PasswordRecovery_InValidRequestURI_ValidQueryString()
            throws Exception {
        // given
        when(requestMock.getRequestURI()).thenReturn(BLUE_PORTAL_URL);

        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(mpCtxFilter, times(0)).handleInvalidURL(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthorizationRequestData.class));
        verify(mpCtxFilter, times(0)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void handleInvalidURLCase_uriIsNull() throws Exception {
        // given
        setUpForHandleInvalidURLCase(errorPage, null);
        // when
        mpCtxFilter.handleInvalidURL(requestMock, responseMock, rdoMock);
        // then
        verify(mpCtxFilter, times(1)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void handleInvalidURLCase_uriEqualsDefaultPage() throws Exception {
        // given
        final String uri = "oscm-portal/marketplace/index.jsf";
        final String defaultUrl = "/marketplace/index.jsf";
        when(requestMock.getContextPath()).thenReturn("oscm-portal");
        setUpForHandleInvalidURLCase(defaultUrl, uri);
        // when
        mpCtxFilter.handleInvalidURL(requestMock, responseMock, rdoMock);
        // then
        verify(mpCtxFilter, times(1)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), eq(errorPage));
    }

    @Test
    public void handleInvalidURLCase_uriNotEqualsDefaultPage() throws Exception {
        // given
        final String uri = "oscm-portal/marketplace/changePassword.jsf?mId=c28e9ea5&token=Y3VfY3UmMT";
        setUpForHandleInvalidURLCase(errorPage, uri);
        // when
        mpCtxFilter.handleInvalidURL(requestMock, responseMock, rdoMock);
        // then
        verify(mpCtxFilter, times(1)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    private void setUpForHandleInvalidURLCase(String defaultUrl,
            String requestUri) {
        doReturn(defaultUrl).when(mpCtxFilter).getDefaultUrl(
                any(ServiceAccess.class), any(AuthorizationRequestData.class),
                any(HttpServletRequest.class));
        when(requestMock.getRequestURI()).thenReturn(requestUri);
    }

    @Test
    public void doFilter_ChangePasswordRequestInvalidParameter()
            throws Exception {
        // given
        requestMock = givenChangePasswordRequestInvalidParameter();

        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(mpCtxFilter, times(1)).handleInvalidURL(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthorizationRequestData.class));
        verify(mpCtxFilter, times(1)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void doFilter_ChangePasswordRequestValidParameter() throws Exception {
        // given
        requestMock = givenChangePasswordRequestValidParameter();

        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(mpCtxFilter, times(0)).handleInvalidURL(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthorizationRequestData.class));
        verify(mpCtxFilter, times(0)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void doFilter_AnyRequestValidParameter() throws Exception {
        // given
        requestMock = givenAnyRequestWithValidParameter();

        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(mpCtxFilter, times(0)).handleInvalidURL(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthorizationRequestData.class));
        verify(mpCtxFilter, times(0)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void doFilter_AnyRequestInValidParameter() throws Exception {
        // given
        requestMock = givenAnyRequestWithInvalidParameter();

        // when
        mpCtxFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(mpCtxFilter, times(0)).handleInvalidURL(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthorizationRequestData.class));
        verify(mpCtxFilter, times(0)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock), anyString());
    }

    @Test
    public void getRedirectMpUrlHttp_OK() throws Exception {
        // given
        String value = "http://thisisaurl/?mId=aaa";
        when(
                confSvcMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.MP_ERROR_REDIRECT_HTTP),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.MP_ERROR_REDIRECT_HTTP, value));
        // when
        String result = mpCtxFilter.getRedirectMpUrlHttp(confSvcMock);
        // then
        assertEquals(value, result);
    }

    @Test
    public void getRedirectMpUrlHttp_Null() throws Exception {
        // given
        when(
                confSvcMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.MP_ERROR_REDIRECT_HTTP),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.MP_ERROR_REDIRECT_HTTP, null));
        // when
        String result = mpCtxFilter.getRedirectMpUrlHttp(confSvcMock);
        // then
        assertEquals(null, result);
    }

    @Test
    public void getRedirectMpUrlHttps_OK() throws Exception {
        // given
        String value = "https://thisisaurl/?mId=aaa";
        when(
                confSvcMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.MP_ERROR_REDIRECT_HTTPS),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.MP_ERROR_REDIRECT_HTTPS, value));
        // when
        String result = mpCtxFilter.getRedirectMpUrlHttps(confSvcMock);
        // then
        assertEquals(value, result);
    }

    @Test
    public void getRedirectMpUrlHttps_Null() throws Exception {
        // given
        when(
                confSvcMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.MP_ERROR_REDIRECT_HTTPS),
                        anyString())).thenReturn(
                createSetting(ConfigurationKey.MP_ERROR_REDIRECT_HTTPS, null));
        // when
        String result = mpCtxFilter.getRedirectMpUrlHttps(confSvcMock);
        // then
        assertEquals(null, result);
    }

    @Test
    public void handleWrongMarketplaceIdCase_redirectToDefaultPage()
            throws Exception {
        // given
        final String servletPath = BaseBean.MARKETPLACE_START_SITE;
        fakeRequest(null, servletPath);
        final String defaultUrl = "http://myDefaultUrl.com";

        setUpForHandleWrongMarketplaceIdCase(defaultUrl, requestUri, null);
        // when
        mpCtxFilter.handleWrongMarketplaceIdCase(requestMock, responseMock,
                rdoMock);
        // then
        verify(mpCtxFilter, times(1)).forward(Matchers.eq(defaultUrl),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void handleWrongMarketplaceIdCase_redirectToMPErrorPage()
            throws Exception {
        // given
        final String servletPath = BaseBean.MARKETPLACE_START_SITE;
        fakeRequest(null, servletPath);
        final String mpRedirect = "http://thisisaurl/xxxx" + servletPath;

        setUpForHandleWrongMarketplaceIdCase(errorPage, requestUri, mpRedirect);
        // when
        mpCtxFilter.handleWrongMarketplaceIdCase(requestMock, responseMock,
                rdoMock);
        // then
        verify(mpCtxFilter, times(1)).forward(Matchers.eq(errorPage),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void handleWrongMarketplaceIdCase_redirectToConfiguredRedirectPage()
            throws Exception {
        // given
        final String servletPath = BaseBean.MARKETPLACE_START_SITE;
        fakeRequest(null, servletPath);
        final String defaultUrl = BaseBean.MARKETPLACE_START_SITE;
        final String mpRedirect = "http://thisisaurl/xxxx/marketplace/?mId=aaa";

        setUpForHandleWrongMarketplaceIdCase(defaultUrl, requestUri, mpRedirect);
        // when
        mpCtxFilter.handleWrongMarketplaceIdCase(requestMock, responseMock,
                rdoMock);
        // then
        verify(responseMock, times(1)).sendRedirect(Matchers.eq(mpRedirect));
        verify(mpCtxFilter, never()).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void handleWrongMarketplaceIdCase_uriIsNull() throws Exception {
        // given
        final String servletPath = BaseBean.MARKETPLACE_START_SITE;
        fakeRequest(null, servletPath);
        setUpForHandleWrongMarketplaceIdCase(errorPage, null, null);
        // when
        mpCtxFilter.handleWrongMarketplaceIdCase(requestMock, responseMock,
                rdoMock);
        // then
        verify(responseMock, never()).sendRedirect(anyString());
        verify(mpCtxFilter, times(1)).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void handleWrongMarketplaceIdCase_hasRequestQueryString()
            throws Exception {
        // given
        final String mId = "FUJITSU";
        final String servletPath = BaseBean.MARKETPLACE_START_SITE;
        fakeRequest(mId, servletPath);
        final String uri = "xxxx" + servletPath + mId;
        final String mpRedirect = "http://thisisaurl/oscm-portal";
        setUpForHandleWrongMarketplaceIdCase(errorPage, uri, mpRedirect);
        when(requestMock.getQueryString()).thenReturn("mId=FUJITSU");
        // when
        mpCtxFilter.handleWrongMarketplaceIdCase(requestMock, responseMock,
                rdoMock);
        // then
        verify(responseMock, times(1)).sendRedirect(Matchers.eq(mpRedirect));
        verify(mpCtxFilter, never()).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void handleWrongMarketplaceIdCase_requesetUrlIsHttps()
            throws Exception {
        // given
        final String servletPath = BaseBean.MARKETPLACE_START_SITE;
        fakeRequest(null, servletPath);
        final String defaultUrl = BaseBean.MARKETPLACE_START_SITE;
        final String mpRedirect = "https://thisisaurl/oscm-portal";

        setUpForHandleWrongMarketplaceIdCase(defaultUrl, requestUri, mpRedirect);
        when(requestMock.getRequestURL())
                .thenReturn(
                        new StringBuffer("https://thisisaurl/xxxx")
                                .append(servletPath));
        doReturn(mpRedirect).when(mpCtxFilter).getRedirectMpUrlHttps(
                any(ConfigurationService.class));
        // when
        mpCtxFilter.handleWrongMarketplaceIdCase(requestMock, responseMock,
                rdoMock);
        // then
        verify(responseMock, times(1)).sendRedirect(Matchers.eq(mpRedirect));
        verify(mpCtxFilter, never()).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    @Test
    public void handleWrongMarketplaceIdCase_RedirectUrlIsNull()
            throws Exception {
        // given
        final String servletPath = BaseBean.MARKETPLACE_START_SITE;
        fakeRequest(null, servletPath);
        final String defaultUrl = BaseBean.MARKETPLACE_START_SITE;

        setUpForHandleWrongMarketplaceIdCase(defaultUrl, requestUri, null);
        // when
        mpCtxFilter.handleWrongMarketplaceIdCase(requestMock, responseMock,
                rdoMock);
        // then
        verify(responseMock, times(1)).sendRedirect(anyString());
        verify(mpCtxFilter, never()).forward(any(String.class),
                any(HttpServletRequest.class), Matchers.eq(responseMock));
    }

    private static VOConfigurationSetting createSetting(ConfigurationKey key,
            String value) {
        return new VOConfigurationSetting(key, "global", value);
    }

    private void setUpForHandleWrongMarketplaceIdCase(String defaultUrl,
            String requestUri, String mpRedirect) {
        doReturn(defaultUrl).when(mpCtxFilter).getDefaultUrl(
                any(ServiceAccess.class), any(AuthorizationRequestData.class),
                any(HttpServletRequest.class));
        when(requestMock.getRequestURI()).thenReturn(requestUri);
        doReturn(mpRedirect).when(mpCtxFilter).getRedirectMpUrlHttp(
                any(ConfigurationService.class));
    }

    private HttpServletRequest givenChangePasswordRequestInvalidParameter() {
        when(requestMock.getRequestURI()).thenReturn(
                "/marketplace/changePassword.jsf");
        when(requestMock.getQueryString()).thenReturn(
                "token=" + INVALID_URL_ENCODED_TOKEN);
        doReturn(INVALID_URL_ENCODED_TOKEN).when(requestMock).getParameter(
                eq("token"));
        return requestMock;
    }

    private HttpServletRequest givenChangePasswordRequestValidParameter() {
        when(requestMock.getRequestURI()).thenReturn(
                "/marketplace/changePassword.jsf");
        when(requestMock.getQueryString()).thenReturn("token=\"1234\"");
        doReturn("1234").when(requestMock).getParameter(eq("token"));

        return requestMock;
    }

    private HttpServletRequest givenAnyRequestWithInvalidParameter() {
        when(requestMock.getRequestURI()).thenReturn(
                BLUE_PORTAL_URL + "/test.jsf");
        when(requestMock.getQueryString()).thenReturn(
                "token=" + INVALID_URL_ENCODED_TOKEN);
        doReturn(INVALID_URL_ENCODED_TOKEN).when(requestMock).getParameter(
                eq("token"));
        return requestMock;
    }

    private HttpServletRequest givenAnyRequestWithValidParameter() {
        when(requestMock.getRequestURI()).thenReturn(
                BLUE_PORTAL_URL + "/test.jsf");
        when(requestMock.getQueryString()).thenReturn("token=\"1234\"");
        doReturn("1234").when(requestMock).getParameter(eq("token"));
        return requestMock;
    }
}
