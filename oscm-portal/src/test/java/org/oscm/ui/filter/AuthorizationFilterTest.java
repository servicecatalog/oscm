/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 8, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;

import javax.faces.application.ViewExpiredException;
import javax.security.auth.login.LoginException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ServiceAccess;

/**
 * Unit testing of {@link AuthorizationFilter}.
 * 
 * @author barzu
 */
public class AuthorizationFilterTest {

    private AuthorizationFilter authFilter;

    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;
    private FilterChain chainMock;
    private AuthorizationRequestData authReqDataMock;
    private AuthenticationSettings authSettingsMock;
    private IdentityService identityServiceMock;
    private ServiceAccess serviceAccessMock;
    private VOUserDetails userDetails;

    @Before
    public void setup() throws Exception {

        requestMock = mock(HttpServletRequest.class);
        responseMock = mock(HttpServletResponse.class);
        chainMock = mock(FilterChain.class);
        authReqDataMock = mock(AuthorizationRequestData.class);
        authSettingsMock = mock(AuthenticationSettings.class);

        authFilter = spy(new AuthorizationFilter());
        authFilter.authSettings = authSettingsMock;

        doReturn(authReqDataMock).when(authFilter).initializeRequestDataObject(
                any(HttpServletRequest.class));

        userDetails = createCustomer();
        identityServiceMock = mock(IdentityService.class);
        serviceAccessMock = mock(ServiceAccess.class);
        doReturn(userDetails).when(identityServiceMock).getCurrentUserDetails();
        doReturn(identityServiceMock).when(serviceAccessMock).getService(
                IdentityService.class);
        doNothing().when(authFilter).forward(any(String.class),
                any(ServletRequest.class), any(ServletResponse.class));

    }

    @Test
    public void doFilter_SessionTimeoutRelogin() throws Exception {
        // given
        ServletException se = new ServletException(new ViewExpiredException());
        doThrow(se).when(authFilter).handleProtectedUrlAndChangePwdCase(
                eq(chainMock), any(HttpServletRequest.class), eq(responseMock),
                eq(authReqDataMock));
        doNothing().when(authFilter).reLogginUserIfRequired(
                any(HttpServletRequest.class), eq(responseMock),
                eq(authReqDataMock), any(StringBuffer.class));
        doReturn("admin").when(requestMock).getParameter(
                eq(AuthorizationFilter.PARAM_LOGIN_USER_ID));

        // when
        authFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(authFilter, times(1)).reLogginUserIfRequired(
                any(HttpServletRequest.class), eq(responseMock),
                eq(authReqDataMock), any(StringBuffer.class));
        verify(responseMock, times(1)).sendRedirect(anyString());
    }

    @Test
    public void doFilter_loginWithWrongPassword() throws Exception {
        // given
        prepareLogin();
        doReturn(Boolean.FALSE).when(authFilter).loginUser(eq(chainMock),
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(VOUserDetails.class), any(AuthorizationRequestData.class),
                eq(identityServiceMock));

        // when
        authFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(identityServiceMock, never()).getCurrentUserDetails();

    }

    @Test
    public void doFilter_loginWithCorrectPassword() throws Exception {
        // given
        prepareLogin();
        doReturn(Boolean.TRUE).when(authFilter).loginUser(eq(chainMock),
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(VOUserDetails.class), any(AuthorizationRequestData.class),
                eq(identityServiceMock));
        doReturn(userDetails).when(identityServiceMock).getCurrentUserDetails();
        doReturn(userDetails).when(authReqDataMock).getUserDetails();
        doNothing().when(authFilter).redirectToPrimarilyRequestedUrl(
                any(FilterChain.class), any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(ServiceAccess.class),
                any(AuthorizationRequestData.class));

        // when
        authFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(identityServiceMock, times(1)).getCurrentUserDetails();

    }

    @Test
    public void doFilter_SAMLSP_LoginPage() throws Exception {
        // given
        prepareLogin_SAMLSP();
        doReturn(Boolean.TRUE).when(authFilter).loginUser(eq(chainMock),
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(VOUserDetails.class), any(AuthorizationRequestData.class),
                eq(identityServiceMock));
        doReturn(userDetails).when(identityServiceMock).getCurrentUserDetails();
        doReturn(userDetails).when(authReqDataMock).getUserDetails();
        doNothing().when(authFilter).redirectToPrimarilyRequestedUrl(
                any(FilterChain.class), any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(ServiceAccess.class),
                any(AuthorizationRequestData.class));

        doReturn(authFilter.loginPage).when(requestMock).getServletPath();

        // when
        authFilter.doFilter(requestMock, responseMock, chainMock);

        doNothing().when(authFilter).sendRedirect(
                any(HttpServletRequest.class), any(HttpServletResponse.class),
                anyString());

        // then
        verify(authFilter, times(1)).sendRedirect(
                any(HttpServletRequest.class), eq(responseMock),
                eq(MenuBean.LINK_DEFAULT));

    }

    @Test
    public void doFilter_SessionTimeoutPublicAccess() throws Exception {
        // given
        ServletException se = new ServletException(new ViewExpiredException());
        doThrow(se).when(authFilter).handleProtectedUrlAndChangePwdCase(
                eq(chainMock), any(HttpServletRequest.class), eq(responseMock),
                eq(authReqDataMock));
        doNothing().when(authFilter).reLogginUserIfRequired(
                any(HttpServletRequest.class), eq(responseMock),
                eq(authReqDataMock), any(StringBuffer.class));

        // when
        authFilter.doFilter(requestMock, responseMock, chainMock);

        // then
        verify(authFilter, times(1)).reLogginUserIfRequired(
                any(HttpServletRequest.class), eq(responseMock),
                eq(authReqDataMock), any(StringBuffer.class));
        verify(responseMock, times(1)).sendRedirect(anyString());
    }

    @Test
    public void loginUser_accessToServiceUrl() throws Exception {
        // given
        createSessionMock(serviceAccessMock, "/opt/2af8/", true);

        doReturn(Boolean.FALSE).when(authReqDataMock).isMarketplace();
        doReturn(Boolean.TRUE).when(authReqDataMock).isAccessToServiceUrl();
        doReturn("").when(authReqDataMock).getMarketplaceId();

        // when
        boolean result = authFilter
                .loginUser(chainMock, requestMock, responseMock, userDetails,
                        authReqDataMock, identityServiceMock);

        // then
        assertTrue("Login successful, no forward performed", result);
    }

    @Test
    public void loginUser_customerAccessBluePortal_noMP() throws Exception {
        // given
        createSessionMock(serviceAccessMock, "/oscm-portal/", false);

        doReturn(Boolean.FALSE).when(authReqDataMock).isMarketplace();
        doReturn(Boolean.FALSE).when(authReqDataMock).isAccessToServiceUrl();
        doReturn("").when(authReqDataMock).getMarketplaceId();

        // when
        boolean result = authFilter
                .loginUser(chainMock, requestMock, responseMock, userDetails,
                        authReqDataMock, identityServiceMock);

        // then
        assertFalse("Forward must be performed", result);

        verify(authFilter, times(1)).forward(BaseBean.MARKETPLACE_ERROR_PAGE,
                requestMock, responseMock);
    }

    @Test
    public void loginUser_customerAccessBluePortal_MP() throws Exception {
        // given
        createSessionMock(serviceAccessMock, "/oscm-portal/", false);

        doReturn(Boolean.FALSE).when(authReqDataMock).isMarketplace();
        doReturn(Boolean.FALSE).when(authReqDataMock).isAccessToServiceUrl();
        doReturn("MyMP").when(authReqDataMock).getMarketplaceId();

        // when
        boolean result = authFilter
                .loginUser(chainMock, requestMock, responseMock, userDetails,
                        authReqDataMock, identityServiceMock);

        // then
        assertFalse("Forward must be performed", result);

        verify(authFilter, times(1)).forward(BaseBean.MARKETPLACE_START_SITE,
                requestMock, responseMock);
    }

    @Test
    public void loginUser_UserLocked_BluePortal() throws Exception {
        // given
        VOUser voUser = createVoUser("userId", UserAccountStatus.LOCKED);
        doReturn(voUser).when(identityServiceMock).getUser(any(VOUser.class));

        createSessionMock(serviceAccessMock, "/oscm-portal/", false);

        doReturn("").when(authReqDataMock).getMarketplaceId();
        doThrow(new LoginException()).when(serviceAccessMock).login(
                any(VOUser.class), anyString(), any(HttpServletRequest.class),
                any(HttpServletResponse.class));

        // when
        boolean result = authFilter
                .loginUser(chainMock, requestMock, responseMock, userDetails,
                        authReqDataMock, identityServiceMock);

        // then
        assertFalse("Forward must be performed", result);
        verify(requestMock, times(1)).setAttribute(
                Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_USER_LOCKED);
        verify(authFilter, times(1)).forward(BaseBean.ERROR_PAGE, requestMock,
                responseMock);
    }

    @Test
    public void loginUser_customerAccessBluePortal_Redirect() throws Exception {
        // given
        createSessionMock(serviceAccessMock, "/oscm-portal/", false);
        doReturn(Boolean.FALSE).when(authReqDataMock).isMarketplace();
        doReturn(Boolean.FALSE).when(authReqDataMock).isAccessToServiceUrl();
        doReturn("").when(authReqDataMock).getMarketplaceId();

        final String mpRedirect = "http://www.fujitsu.com/global/";
        doReturn(mpRedirect).when(authFilter).getRedirectMpUrlHttp(
                any(ConfigurationService.class));
        doReturn(mpRedirect).when(authFilter).getRedirectMpUrlHttps(
                any(ConfigurationService.class));
        doReturn(new StringBuffer(BaseBean.MARKETPLACE_START_SITE)).when(
                requestMock).getRequestURL();
        doReturn(BaseBean.MARKETPLACE_START_SITE).when(requestMock)
                .getRequestURI();

        // when
        boolean result = authFilter
                .loginUser(chainMock, requestMock, responseMock, userDetails,
                        authReqDataMock, identityServiceMock);

        // then
        assertFalse("Forward must be performed", result);
        verify(responseMock, times(1)).sendRedirect(eq(mpRedirect));
        verify(authReqDataMock, times(1)).setUserDetails(
                any(VOUserDetails.class));

        verify(authFilter, never()).forward(BaseBean.MARKETPLACE_ERROR_PAGE,
                requestMock, responseMock);
        verify(authFilter, never()).forward(BaseBean.MARKETPLACE_START_SITE,
                requestMock, responseMock);
    }

    @Test
    public void handleChangeUserPasswordRequest_errorAccountNotLocked()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(authReqDataMock).isRequestedToChangePwd();
        doReturn("secret").when(authReqDataMock).getNewPassword();
        doReturn("secret").when(authReqDataMock).getNewPassword2();
        doReturn(userDetails).when(identityServiceMock).getUser(
                any(VOUser.class));
        doReturn("error.changePassword").when(requestMock).getAttribute(
                Constants.REQ_ATTR_ERROR_KEY);

        // when
        boolean result = authFilter
                .handleChangeUserPasswordRequest(chainMock, requestMock,
                        responseMock, authReqDataMock, identityServiceMock);

        // then
        assertFalse("Unsuccessful password change", result);
        verify(authFilter, never()).sendRedirect(any(HttpServletRequest.class),
                any(HttpServletResponse.class), anyString());
        verify(chainMock, times(2)).doFilter(any(ServletRequest.class),
                any(ServletResponse.class));
    }

    @Test
    public void handleChangeUserPasswordRequest_errorAccountLocked()
            throws Exception {
        // given
        doReturn(Boolean.TRUE).when(authReqDataMock).isRequestedToChangePwd();
        doReturn("secret").when(authReqDataMock).getNewPassword();
        doReturn("secret").when(authReqDataMock).getNewPassword2();
        userDetails.setStatus(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
        doReturn(userDetails).when(identityServiceMock).getUser(
                any(VOUser.class));
        doReturn("error.changePassword").when(requestMock).getAttribute(
                Constants.REQ_ATTR_ERROR_KEY);

        // when
        boolean result = authFilter
                .handleChangeUserPasswordRequest(chainMock, requestMock,
                        responseMock, authReqDataMock, identityServiceMock);

        // then
        assertFalse("Unsuccessful password change", result);
        verify(chainMock, times(1)).doFilter(any(ServletRequest.class),
                any(ServletResponse.class));
        verify(requestMock, times(1)).setAttribute(
                Constants.REQ_ATTR_ERROR_KEY, BaseBean.ERROR_USER_LOCKED);
        verify(authFilter, times(1)).sendRedirect(requestMock, responseMock,
                "/public/error.jsf");
    }

    private VOUserDetails createCustomer() {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails
                .setOrganizationRoles(new HashSet<OrganizationRoleType>(
                        Arrays.asList(new OrganizationRoleType[] { OrganizationRoleType.CUSTOMER })));
        return userDetails;
    }

    private void createSessionMock(ServiceAccess serviceAccessMock,
            final String forwardUrl, final boolean serviceLogin) {
        HttpSession sessionMock = mock(HttpSession.class);
        doReturn(forwardUrl).when(sessionMock).getAttribute(
                Constants.SESS_ATTR_FORWARD_URL);
        doReturn(Boolean.valueOf(serviceLogin)).when(sessionMock).getAttribute(
                Constants.SESS_ATTR_ONLY_SERVICE_LOGIN);
        doReturn(serviceAccessMock).when(sessionMock).getAttribute(
                "ADM_SERVICE_ACCESS");
        doReturn(sessionMock).when(requestMock).getSession();
        doReturn(sessionMock).when(requestMock).getSession(anyBoolean());
    }

    private VOUser createVoUser(String userid, UserAccountStatus status) {
        VOUser voUser = new VOUser();
        voUser.setKey(10011L);
        voUser.setUserId(userid);
        voUser.setStatus(status);
        return voUser;
    }

    private void prepareLogin_SAMLSP() throws Exception {
        userDetails.setStatus(UserAccountStatus.ACTIVE);
        createSessionMock(serviceAccessMock, "/oscm-portal/", false);
        doReturn("userId").when(authReqDataMock).getUserId();
        doReturn(Boolean.TRUE).when(authReqDataMock).isPasswordSet();
        doReturn(userDetails).when(authFilter).readTechnicalUserFromDb(
                any(IdentityService.class), eq(authReqDataMock));
        doReturn(Boolean.TRUE).when(authFilter).loginUser(
                any(FilterChain.class), any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(VOUserDetails.class),
                any(AuthorizationRequestData.class), eq(identityServiceMock));
        doReturn(Boolean.TRUE).when(authSettingsMock).isServiceProvider();
    }

    private void prepareLogin() throws Exception {
        userDetails.setStatus(UserAccountStatus.ACTIVE);
        createSessionMock(serviceAccessMock, "/oscm-portal/", false);
        doReturn("userId").when(authReqDataMock).getUserId();
        doReturn(Boolean.TRUE).when(authReqDataMock).isPasswordSet();
        doReturn(Boolean.FALSE).when(authFilter).handleLoggedInUser(
                any(FilterChain.class), any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(ServiceAccess.class),
                any(AuthorizationRequestData.class));
        doReturn(userDetails).when(authFilter).readTechnicalUserFromDb(
                any(IdentityService.class), eq(authReqDataMock));
        doReturn(Boolean.TRUE).when(authFilter).loginUser(
                any(FilterChain.class), any(HttpServletRequest.class),
                any(HttpServletResponse.class), any(VOUserDetails.class),
                any(AuthorizationRequestData.class), eq(identityServiceMock));
    }

}
