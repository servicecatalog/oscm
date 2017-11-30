/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.04.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.faces.application.FacesMessage;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.TenantService;
import org.oscm.internal.types.enumtypes.AuthenticationMode;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.common.saml2.AuthenticationHandler;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.filter.AuthenticationSettings;
import org.oscm.ui.model.User;
import org.oscm.ui.model.UserRole;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIViewRootStub;

/**
 * Unit tests for testing the UserBean.
 * 
 * @author weiser
 */
public class UserBeanTest {

    private UserBean userBean;
    private User user;
    private VOUserDetails loggedInUser;
    private final String OUTCOME_CANCEL = "cancel";
    private final String OUTCOME_SHOW_REGISTRATION = "showRegistration";
    private final String ERROR_COMPLETE_REGISTRATION = "error.complete.registration";
    private final String BASE_URL = "http://localhost:8180/oscm-portal";
    private final String SUBSCRIPTION_ADD_PAGE = "/marketplace/subscriptions/creation/add.jsf";

    private HttpServletRequest requestMock;
    private HttpServletResponse responseMock;

    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();
    private TableState ts;
    private IdentityService idServiceMock;
    private ConfigurationService cfgMock;
    private ServiceAccess serviceAccessMock;
    private List<UserRole> roles;
    private ApplicationBean appBean;
    private AuthenticationSettings authSettingsMock;
    private AuthenticationHandler authHandlerMock;
    private HttpSession sessionMock;
    private UserGroupService userGroupService = mock(UserGroupService.class);
    private MarketplaceService marketplaceService = mock(MarketplaceService.class);
    private static final String ISSUER = "CT-MG";
    private static final String RECIPIENT = "http://www.bes-portal.de";
    private static final String IDP = "http://www.idp.de/openam/SSORedirect/request";
    private static final String KEYSTORE_PATH = "/openam/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "changeit";
    private static final String OUTCOME_SAMLSP_REDIRECT = "redirectToIdp";
    static final String OUTCOME_ADD_USER = "addUser";

    private String errorCodeValue = null;
    private TenantService tenantService;

    @Before
    public void setup() throws Exception {
        tenantService = mock(TenantService.class);
        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };
        UIViewRootStub vrStub = new UIViewRootStub() {
            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            };
        };
        contextStub.setViewRoot(vrStub);

        userBean = spy(new UserBean());

        // Create a user
        user = new User(new VOUserDetails());
        user.setUserId("myUserId");

        // Mock all necessary methods of userBean
        requestMock = mock(HttpServletRequest.class);
        doReturn("/oscm-portal").when(requestMock).getContextPath();
        doReturn(new StringBuffer("https://something.de")).when(requestMock)
                .getRequestURL();
        mockErrorAttribute(requestMock);
        doReturn(requestMock).when(userBean).getRequest();

        doReturn(user).when(userBean).getUserFromSession();
        doReturn("myUserId2").when(userBean).getMyUserId();
        doNothing().when(userBean).resetUIInputChildren();

        userBean.setSessionBean(new SessionBean());
        userBean.ui = mock(UiDelegate.class);

        ts = mock(TableState.class);
        when(userBean.ui.findBean(TableState.BEAN_NAME)).thenReturn(ts);

        UserRole role = new UserRole();
        role.setSelected(true);
        role.setUserRoleType(UserRoleType.MARKETPLACE_OWNER);
        roles = Arrays.asList(role);
        userBean.setUserRolesForNewUser(roles);
        userBean.setToken(userBean.getToken());
        userBean.getNewUser();

        idServiceMock = mock(IdentityService.class);
        doReturn(idServiceMock).when(userBean).getIdService();

        appBean = mock(ApplicationBean.class);
        when(userBean.ui.findBean(eq(UserBean.APPLICATION_BEAN))).thenReturn(
                appBean);
        loggedInUser = mock(VOUserDetails.class);
        doReturn(loggedInUser).when(userBean).getLoggedInUser();

        authSettingsMock = mock(AuthenticationSettings.class);

        authHandlerMock = mock(AuthenticationHandler.class);
        doReturn(OUTCOME_SAMLSP_REDIRECT).when(authHandlerMock)
                .handleAuthentication(true, sessionMock);

        responseMock = mock(HttpServletResponse.class);
        doReturn(responseMock).when(userBean).getResponse();
        sessionMock = mock(HttpSession.class);
        doReturn(sessionMock).when(userBean).getSession();

        doReturn("").when(userBean).getLoginRedirect(
                any(HttpServletRequest.class), any(HttpSession.class),
                anyBoolean());

        doReturn(sessionMock).when(requestMock).getSession();
        serviceAccessMock = mock(ServiceAccess.class);
        doReturn(serviceAccessMock).when(sessionMock).getAttribute(
                eq(ServiceAccess.SESS_ATTR_SERVICE_ACCESS));
        doReturn(idServiceMock).when(serviceAccessMock).getService(
                eq(IdentityService.class));

        doReturn(userGroupService).when(userBean).getUserGroupService();

        when(userBean.ui.findBean(eq(UserBean.APPLICATION_BEAN))).thenReturn(
            appBean);

        doReturn(marketplaceService).when(userBean).getMarketplaceService();
        doReturn(true).when(marketplaceService).doesOrganizationHaveAccessMarketplace(anyString(), anyString());
        userBean.getSessionBean().setTenantID("1");
    }

    void mockErrorAttribute(HttpServletRequest requestMock) {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                errorCodeValue = (String) invocation.getArguments()[1];
                return null;
            }
        }).when(requestMock).setAttribute(eq(Constants.REQ_ATTR_ERROR_KEY),
                anyString());

        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return errorCodeValue;
            }
        }).when(requestMock).getAttribute(eq(Constants.REQ_ATTR_ERROR_KEY));
    }

    @Test
    public void addUser() {
        assertEquals(UserBean.OUTCOME_ADD_USER, userBean.addUser());
    }

    @Test
    public void addUser_Ldap() {
        user.setRemoteLdapActive(true);

        assertEquals(UserBean.OUTCOME_IMPORT_USER, userBean.addUser());
    }

    @Test
    public void getAddUserLabelKey() {
        assertEquals(UserBean.LABEL_ADD_USER, userBean.getAddUserLabelKey());
    }

    @Test
    public void getUserId() {
        // given
        String userIdGiven = "xyzäüöß\"´`*~!§$%&/()";
        doReturn(userIdGiven).when(requestMock).getParameter(
                Constants.REQ_PARAM_USER_ID);

        // when
        String userId = userBean.getUserId();

        // then
        assertEquals(userIdGiven, userId);
    }

    @Test
    public void getAddUserLabelKey_Ldap() {
        user.setRemoteLdapActive(true);

        assertEquals(UserBean.LABEL_IMPORT_USER, userBean.getAddUserLabelKey());
    }

    @Test
    public void isSelfRegistrationAllowed() {
        setupConfigurationMockForRegistrationenablement(true);
        assertNull(userBean.getSessionBean().getSelfRegistrationEnabled());

        assertTrue(userBean.isSelfRegistrationAllowed());
        assertEquals(Boolean.TRUE, userBean.getSessionBean()
                .getSelfRegistrationEnabled());
    }

    @Test
    public void isSelfRegistrationAllowed_Cached() {
        userBean.getSessionBean().setSelfRegistrationEnabled(Boolean.FALSE);
        ConfigurationService cs = setupConfigurationMockForRegistrationenablement(true);

        assertFalse(userBean.isSelfRegistrationAllowed());

        verifyZeroInteractions(cs);
    }

    @Test
    public void createClassic_ResetuserRolesForNewUser() throws Exception {
        // given
        facesMessages = new ArrayList<FacesMessage>();
        doReturn(new VOUserDetails()).when(idServiceMock).createUser(
                any(VOUserDetails.class), anyListOf(UserRoleType.class),
                anyString());
        // when
        userBean.createClassic();
        // then
        assertEquals(1, userBean.getUserRolesForNewUser().size());
        for (UserRole userRole : roles) {
            if (userRole.getUserRoleType().equals(
                    UserRoleType.MARKETPLACE_OWNER)) {
                assertFalse(userRole.isSelected());
            }
        }
        assertEquals(1, facesMessages.size());
        verify(ts).resetActivePages();
    }

    @Test
    public void createClassic_Pending() throws Exception {
        // given
        facesMessages = new ArrayList<FacesMessage>();
        doReturn(null).when(idServiceMock).createUser(any(VOUserDetails.class),
                anyListOf(UserRoleType.class), anyString());
        // when
        String result = userBean.createClassic();
        // then
        assertEquals(OUTCOME_ADD_USER, result);
    }

    @Test
    public void cancel() {
        String result = userBean.cancel();
        assertEquals(OUTCOME_CANCEL, result);
    }

    @Test
    public void isServiceProvider_True() {

        setupConfigurationMockForAuthentication(AuthenticationMode.SAML_SP);

        assertTrue(userBean.isServiceProvider());

    }

    @Test
    public void isServiceProvider_False() {

        setupConfigurationMockForAuthentication(AuthenticationMode.INTERNAL);

        assertFalse(userBean.isServiceProvider());

    }

    @Test
    public void isInternalAuthMode() {
        // given
        when(Boolean.valueOf(appBean.isInternalAuthMode())).thenReturn(
                Boolean.TRUE);
        // when
        boolean result = userBean.isInternalAuthMode();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isInternalAuthMode_notInternalAuthMode() {
        // given
        when(Boolean.valueOf(appBean.isInternalAuthMode())).thenReturn(
                Boolean.FALSE);
        // when
        boolean result = userBean.isInternalAuthMode();
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void showRegistration_NotInternalMode() throws Exception {
        // given
        doReturn(mockConfigurationService(AuthenticationMode.SAML_SP.name()))
                .when(userBean).getConfigurationService();
        doReturn(authHandlerMock).when(userBean).getAuthenticationHandler();

        // when
        userBean.showRegistration();

        // then
        verify(authHandlerMock, times(1)).handleAuthentication(true,
                sessionMock);
    }

    @Test
    public void showRegistration_INTERNAL() throws Exception {
        // given
        doReturn(mockConfigurationService(AuthenticationMode.INTERNAL.name()))
                .when(userBean).getConfigurationService();

        // when
        String result = userBean.showRegistration();

        // then
        assertEquals(OUTCOME_SHOW_REGISTRATION, result);
    }

    @Test
    public void redirectToIDP_OK() throws Exception {
        // given
        String redirect = "/marketplace/index.jsf";
        userBean.setConfirmedRedirect(redirect);
        doReturn(authSettingsMock).when(userBean).getAuthenticationSettings();
        doReturn(authHandlerMock).when(userBean).getAuthenticationHandler();

        // when
        userBean.redirectToIDP();

        // then
        verify(authHandlerMock, times(1)).handleAuthentication(true,
                sessionMock);
    }

    @Test
    public void redirectToIDP_SelfRegistration() throws Exception {
        // given
        userBean.setConfirmedRedirect("/marketplace/registration.jsf");
        doReturn(authSettingsMock).when(userBean).getAuthenticationSettings();

        // when
        String outcome = userBean.redirectToIDP();

        // then
        verify(userBean.ui, times(1)).handleError(anyString(),
                eq(ERROR_COMPLETE_REGISTRATION));
        assertEquals(OUTCOME_SHOW_REGISTRATION, outcome);
    }

    @Test
    public void getAuthSettings() throws Exception {
        // given
        ConfigurationService csMock = mockConfigurationService(AuthenticationMode.SAML_SP
                .name());
        AuthenticationSettings authSettings = new AuthenticationSettings(tenantService, csMock);
        doReturn(authSettings).when(userBean).getAuthenticationSettings();
        VOTenant tenant = new VOTenant();
        tenant.setTenantId("tenantID");
        doReturn(tenant).when(tenantService).getTenantByTenantId(anyString());
        // when
        AuthenticationSettings result = userBean.getAuthenticationSettings();
        // then
        assertEquals(authSettings.getIssuer(), result.getIssuer());
        assertEquals(authSettings.getIdentityProviderURL(),
                result.getIdentityProviderURL());
        assertEquals(authSettings.getIdentityProviderURLContextRoot(),
                result.getIdentityProviderURLContextRoot());
    }

    @Test
    public void isLoggedInAndSubscriptionManager_NullUser() {
        // given
        doReturn(null).when(userBean).getUserFromSessionWithoutException();

        // when
        boolean isNotSubscriptionManager = userBean
                .isLoggedInAndSubscriptionManager();

        // then
        assertFalse(isNotSubscriptionManager);
    }

    @Test
    public void isLoggedInAndSubscriptionManager_SubscriptionManager() {

        // given
        VOUserDetails subscriptionManager = new VOUserDetails();
        HashSet<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        subscriptionManager.setUserRoles(userRoles);
        doReturn(subscriptionManager).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean isSubscriptionManager = userBean
                .isLoggedInAndSubscriptionManager();

        // then
        assertTrue(isSubscriptionManager);
    }

    @Test
    public void isLoggedInAndSubscriptionManager_NotSubscriptionManager() {
        // given
        VOUserDetails subscriptionManager = new VOUserDetails();
        HashSet<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.BROKER_MANAGER);
        subscriptionManager.setUserRoles(userRoles);
        doReturn(subscriptionManager).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean isNotSubscriptionManager = userBean
                .isLoggedInAndSubscriptionManager();

        // then
        assertFalse(isNotSubscriptionManager);
    }

    @Test
    public void isLoggedInAndAllowedToSubscribe_NullUser() {
        // given
        doReturn(null).when(userBean).getUserFromSessionWithoutException();

        // when
        boolean result = userBean.isLoggedInAndAllowedToSubscribe();

        // then
        assertFalse(result);
    }

    @Test
    public void isLoggedInAndAllowedToSubscribe_NotAllowed() {
        // given
        VOUserDetails subscriptionManager = new VOUserDetails();
        HashSet<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.BROKER_MANAGER);
        subscriptionManager.setUserRoles(userRoles);
        doReturn(subscriptionManager).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean result = userBean.isLoggedInAndAllowedToSubscribe();

        // then
        assertFalse(result);
    }

    @Test
    public void isLoggedInAndAllowedToSubscribe_SubscriptionManager() {

        // given
        VOUserDetails subscriptionManager = new VOUserDetails();
        HashSet<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        subscriptionManager.setUserRoles(userRoles);
        doReturn(subscriptionManager).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean result = userBean.isLoggedInAndAllowedToSubscribe();

        // then
        assertTrue(result);
    }

    @Test
    public void isLoggedInAndAllowedToSubscribe_Admin() {
        // given
        VOUserDetails subscriptionManager = new VOUserDetails();
        HashSet<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        subscriptionManager.setUserRoles(userRoles);
        doReturn(subscriptionManager).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean result = userBean.isLoggedInAndAllowedToSubscribe();

        // then
        assertTrue(result);
    }

    @Test
    public void isLoggedInAndAllowedToSubscribe_AdminAndSubMgr() {
        // given
        VOUserDetails subscriptionManager = new VOUserDetails();
        HashSet<UserRoleType> userRoles = new HashSet<UserRoleType>();
        userRoles.add(UserRoleType.ORGANIZATION_ADMIN);
        userRoles.add(UserRoleType.SUBSCRIPTION_MANAGER);
        subscriptionManager.setUserRoles(userRoles);
        doReturn(subscriptionManager).when(userBean)
                .getUserFromSessionWithoutException();

        // when
        boolean result = userBean.isLoggedInAndAllowedToSubscribe();

        // then
        assertTrue(result);
    }

    @Test
    public void login_UserLocked_Internal() throws Exception {
        VOUser voUser = createVoUser("userId", UserAccountStatus.LOCKED);
        userBean.setUserId("userId");
        doReturn(Boolean.FALSE).when(userBean).isServiceProvider();
        doReturn(voUser).when(idServiceMock).getUser(any(VOUser.class));
        doThrow(new LoginException()).when(serviceAccessMock).login(eq(voUser),
                anyString(), any(HttpServletRequest.class),
                any(HttpServletResponse.class));
        doReturn(BaseBean.MARKETPLACE_START_SITE).when(requestMock)
                .getServletPath();
        // when
        String result = userBean.login();
        // then
        verify(requestMock, times(1)).setAttribute(
                eq(Constants.REQ_ATTR_ERROR_KEY),
                eq(BaseBean.ERROR_USER_LOCKED));
        assertEquals(BaseBean.OUTCOME_STAY_ON_PAGE, result);
    }

    @Test
    public void login_UserLocked_SAMLSP() throws Exception {
        VOUser voUser = createVoUser("userId", UserAccountStatus.LOCKED);
        doReturn("userId").when(requestMock).getParameter(
                eq(UserBean.SAMPSP_FORM + Constants.REQ_PARAM_USER_ID));
        doReturn(Boolean.TRUE).when(userBean).isServiceProvider();
        doReturn(voUser).when(idServiceMock).getUser(any(VOUser.class));
        doThrow(new LoginException()).when(serviceAccessMock).login(eq(voUser),
                anyString(), any(HttpServletRequest.class),
                any(HttpServletResponse.class));
        doReturn(BaseBean.SAML_SP_LOGIN_AUTOSUBMIT_PAGE).when(requestMock)
                .getServletPath();
        // when
        String result = userBean.login();
        // then
        verify(requestMock, times(1)).setAttribute(
                eq(Constants.REQ_ATTR_ERROR_KEY),
                eq(BaseBean.ERROR_USER_LOCKED));
        assertEquals(BaseBean.OUTCOME_MARKETPLACE_ERROR_PAGE, result);
    }

    @Test
    public void login_NonExistingUser_Internal() throws Exception {
        userBean.setUserId("userId");
        doReturn(Boolean.FALSE).when(userBean).isServiceProvider();
        doThrow(new LoginException()).when(serviceAccessMock).login(
                any(VOUser.class), anyString(), any(HttpServletRequest.class),
                any(HttpServletResponse.class));
        doThrow(new ObjectNotFoundException()).when(idServiceMock).getUser(
                any(VOUser.class));
        // when
        String result = userBean.login();
        // then
        verify(requestMock, times(1)).setAttribute(
                eq(Constants.REQ_ATTR_ERROR_KEY), eq(BaseBean.ERROR_LOGIN));
        assertEquals(BaseBean.OUTCOME_STAY_ON_PAGE, result);
    }

    @Test
    public void login_NonExistingUser_SAMLSP() throws Exception {
        doReturn("userId").when(requestMock).getParameter(
                eq(UserBean.SAMPSP_FORM + Constants.REQ_PARAM_USER_ID));
        doReturn(Boolean.TRUE).when(userBean).isServiceProvider();
        doThrow(new ObjectNotFoundException()).when(idServiceMock).getUser(
                any(VOUser.class));
        // when
        String result = userBean.login();
        // then
        verify(requestMock, times(1)).setAttribute(
                eq(Constants.REQ_ATTR_ERROR_KEY), eq(BaseBean.ERROR_LOGIN));
        assertEquals(BaseBean.OUTCOME_MARKETPLACE_ERROR_PAGE, result);
    }

    @Test
    public void login_SAMLSP() throws ObjectNotFoundException,
            OrganizationRemovedException, OperationNotPermittedException,
            ValidationException {
        VOUser voUser = createVoUser("userId", UserAccountStatus.ACTIVE);
        doReturn("userId").when(requestMock).getParameter(
                eq(UserBean.SAMPSP_FORM + Constants.REQ_PARAM_USER_ID));
        doReturn(Boolean.TRUE).when(userBean).isServiceProvider();
        doReturn(voUser).when(idServiceMock).getUser(any(VOUser.class));
        // when
        userBean.login();
        // then
        verify(userBean, times(1)).getLoginRedirect(
                any(HttpServletRequest.class), eq(sessionMock), eq(false));
    }

    /**
     * Try to trigger import user action without uploaded file (actually cannot
     * happen)
     */
    @Test
    public void importUsers() {
        // when
        userBean.importUsersOnPortal();

        // then
        verify(userBean.ui, times(1)).handleException(
                any(SaaSApplicationException.class));
    }

    @Test
    public void createInt_MailOperationException_Internal() throws Exception {
        // given
        String mId = "mId1";
        doReturn(mockConfigurationService(AuthenticationMode.INTERNAL.name()))
                .when(userBean).getConfigurationService();
        when(
                idServiceMock.createUser(any(VOUserDetails.class),
                        anyListOf(UserRoleType.class), anyString())).thenThrow(
                new MailOperationException());
        when(Boolean.valueOf(authSettingsMock.isInternal())).thenReturn(
                Boolean.TRUE);

        // when
        String outcome = userBean.createInt(mId);

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, outcome);
        verify(userBean, times(1)).addMessage((String) isNull(),
                eq(FacesMessage.SEVERITY_ERROR),
                eq(BaseBean.ERROR_USER_CREATE_MAIL));
    }

    @Test
    public void createInt_MailOperationException_NotInternal() throws Exception {
        // given
        String mId = "mId1";
        doReturn(mockConfigurationService(AuthenticationMode.SAML_SP.name()))
                .when(userBean).getConfigurationService();
        when(
                idServiceMock.createUser(any(VOUserDetails.class),
                        anyListOf(UserRoleType.class), anyString())).thenThrow(
                new MailOperationException());
        when(Boolean.valueOf(authSettingsMock.isInternal())).thenReturn(
                Boolean.FALSE);

        // when
        String outcome = userBean.createInt(mId);

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, outcome);
        verify(userBean, times(1)).addMessage((String) isNull(),
                eq(FacesMessage.SEVERITY_ERROR),
                eq(BaseBean.ERROR_USER_CREATE_MAIL_NOT_INTERNAL));
    }

    @Test
    public void getIsOrganizationAdmin() {
        // given
        doReturn(Boolean.TRUE).when(idServiceMock).isCallerOrganizationAdmin();

        // when
        boolean result = userBean.getIsOrganizationAdmin();

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void getIsOrganizationAdmin_Not() {
        // given
        doReturn(Boolean.FALSE).when(idServiceMock).isCallerOrganizationAdmin();

        // when
        boolean result = userBean.getIsOrganizationAdmin();

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void checkAddSubscription_NullUser() {
        // given
        final String REDIRECT = "some redirect";
        userBean.setRequestedRedirect(REDIRECT);

        // when
        userBean.checkAddSubacription(null);

        // then
        assertEquals(REDIRECT, userBean.getRequestedRedirect());
    }

    @Test
    public void checkAddSubscription_addSubscription_NoAccess()
            throws ObjectNotFoundException {
        // given
        final long SERVICE_KEY = 12000L;
        userBean.setRequestedRedirect(SUBSCRIPTION_ADD_PAGE);
        userBean.getSessionBean().setSelectedServiceKeyForCustomer(SERVICE_KEY);
        VOUserDetails user = new VOUserDetails();
        user.setKey(1000L);
        List<Long> invisibleServices = Arrays.asList(Long.valueOf(SERVICE_KEY));
        doReturn(invisibleServices).when(userGroupService)
                .getInvisibleProductKeysForUser(user.getKey());

        // when
        userBean.checkAddSubacription(user);

        // then
        assertEquals(BaseBean.MARKETPLACE_ACCESS_DENY_PAGE,
                userBean.getRequestedRedirect());
    }

    @Test
    public void checkAddSubscription_addSubscription_Access()
            throws ObjectNotFoundException {
        // given
        final long SERVICE_KEY = 12000L;
        userBean.setRequestedRedirect(SUBSCRIPTION_ADD_PAGE);
        userBean.getSessionBean().setSelectedServiceKeyForCustomer(SERVICE_KEY);
        VOUserDetails user = new VOUserDetails();
        user.setKey(1000L);
        List<Long> invisibleServices = Arrays.asList(Long.valueOf(110L));
        doReturn(invisibleServices).when(userGroupService)
                .getInvisibleProductKeysForUser(user.getKey());

        // when
        userBean.checkAddSubacription(user);

        // then
        assertEquals(SUBSCRIPTION_ADD_PAGE, userBean.getRequestedRedirect());
    }

    @Test
    public void testLoginForClosedMarketplace() throws LoginException, ValidationException, OperationNotPermittedException, ObjectNotFoundException, OrganizationRemovedException {
        //given
        doReturn(false).when(userBean).isServiceProvider();
        VOUser mockUser = mock(VOUser.class);
        doReturn(mockUser).when(idServiceMock).getUser(any(VOUser.class));
        doReturn(false).when(marketplaceService).doesOrganizationHaveAccessMarketplace(anyString(), anyString());
        userBean.setUserId("ID");
        //when
        userBean.login();
        //then
        verify(requestMock, times(1)).setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_LOGIN_TO_CLOSED_MARKETPLACE);
    }

    @Test
    public void testLoginForClosedMarketplace_SAML() throws LoginException, ValidationException, OperationNotPermittedException, ObjectNotFoundException, OrganizationRemovedException {
        //given
        doReturn(true).when(userBean).isServiceProvider();
        userBean.setUserId("ID");
        doReturn(false).when(marketplaceService).doesOrganizationHaveAccessMarketplace(anyString(), anyString());
        when(requestMock.getParameter("samlSPForm:" + Constants.REQ_PARAM_USER_ID)).thenReturn("UserId");
        when(requestMock.getParameter("samlSPForm:" + Constants.REQ_ATTR_PASSWORD)).thenReturn("Password");
        VOUser mockUser = mock(VOUser.class);
        doReturn(mockUser).when(idServiceMock).getUser(any(VOUser.class));
        //when
        userBean.login();
        //then
        verify(requestMock, times(1)).setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                BaseBean.ERROR_ACCESS_TO_CLOSED_MARKETPLACE);
    }

    private ConfigurationService setupConfigurationMockForRegistrationenablement(
            boolean enabled) {
        ConfigurationService csMock = mock(ConfigurationService.class);
        when(
                csMock.getVOConfigurationSetting(
                        eq(ConfigurationKey.CUSTOMER_SELF_REGISTRATION_ENABLED),
                        anyString())).thenReturn(
                createSetting(
                        ConfigurationKey.CUSTOMER_SELF_REGISTRATION_ENABLED,
                        String.valueOf(enabled)));

        doReturn(csMock).when(userBean).getConfigurationService();
        return csMock;
    }

    private ConfigurationService setupConfigurationMockForAuthentication(
            AuthenticationMode mode) {
        ConfigurationService csMock = mock(ConfigurationService.class);
        doReturn(csMock).when(userBean).getConfigurationService();
        if (AuthenticationMode.SAML_SP.equals(mode)) {
            doReturn(Boolean.TRUE).when(csMock).isServiceProvider();
        } else {
            doReturn(Boolean.FALSE).when(csMock).isServiceProvider();
        }
        return csMock;
    }

    private ConfigurationService mockConfigurationService(String mode) {
        cfgMock = mock(ConfigurationService.class);
        doReturn(
                new VOConfigurationSetting(ConfigurationKey.AUTH_MODE,
                        Configuration.GLOBAL_CONTEXT, mode)).when(cfgMock)
                .getVOConfigurationSetting(ConfigurationKey.AUTH_MODE,
                        Configuration.GLOBAL_CONTEXT);
        doReturn(
                new VOConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, RECIPIENT)).when(cfgMock)
                .getVOConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT);
        doReturn(
                new VOConfigurationSetting(ConfigurationKey.SSO_ISSUER_ID,
                        Configuration.GLOBAL_CONTEXT, ISSUER)).when(cfgMock)
                .getVOConfigurationSetting(ConfigurationKey.SSO_ISSUER_ID,
                        Configuration.GLOBAL_CONTEXT);
        doReturn(
                new VOConfigurationSetting(ConfigurationKey.SSO_IDP_URL,
                        Configuration.GLOBAL_CONTEXT, IDP)).when(cfgMock)
                .getVOConfigurationSetting(ConfigurationKey.SSO_IDP_URL,
                        Configuration.GLOBAL_CONTEXT);
        doReturn(
                new VOConfigurationSetting(ConfigurationKey.SSO_IDP_TRUSTSTORE,
                        Configuration.GLOBAL_CONTEXT, KEYSTORE_PATH)).when(
                cfgMock).getVOConfigurationSetting(
                ConfigurationKey.SSO_IDP_TRUSTSTORE,
                Configuration.GLOBAL_CONTEXT);

        doReturn(
                new VOConfigurationSetting(
                        ConfigurationKey.SSO_IDP_TRUSTSTORE_PASSWORD,
                        Configuration.GLOBAL_CONTEXT, KEYSTORE_PASSWORD)).when(
                cfgMock).getVOConfigurationSetting(
                ConfigurationKey.SSO_IDP_TRUSTSTORE_PASSWORD,
                Configuration.GLOBAL_CONTEXT);
        doReturn(cfgMock).when(userBean).getConfigurationService();
        return cfgMock;
    }

    private static VOConfigurationSetting createSetting(ConfigurationKey key,
            String value) {
        return new VOConfigurationSetting(key, "global", value);
    }

    private VOUser createVoUser(String userid, UserAccountStatus status) {
        VOUser voUser = new VOUser();
        voUser.setKey(10011L);
        voUser.setUserId(userid);
        voUser.setStatus(status);
        return voUser;
    }
}
