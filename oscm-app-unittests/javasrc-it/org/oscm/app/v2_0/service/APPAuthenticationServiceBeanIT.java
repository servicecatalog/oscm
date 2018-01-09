/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 17.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.app.business.ProductProvisioningServiceFactoryBean;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.intf.IdentityService;
import org.oscm.intf.SubscriptionService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationRemovedException;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;



public class APPAuthenticationServiceBeanIT extends EJBTestBase {
    private static final String CTRL_ID = "ess.vmware";
    private APPAuthenticationServiceBean authService;
    private BesDAO besDAO;
    private EntityManager em;
    private APPlatformController controller;
    private boolean requestInstanceProvisioning = false;
    private boolean setControllerReady = false;

    private EnhancedIdentityService identityService;
    private APPConfigurationServiceBean configService;
    private SubscriptionService subcriptionService;
    private APPlatformService platformService;
    private PasswordAuthentication defaultAuth;
    private Service serviceMock;
    private HashMap<String, String> proxyConfigSettings;
    private HashMap<String, String> controllerConfigSettings;

    @SuppressWarnings("unchecked")
    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();

        container.addBean(configService = Mockito
                .mock(APPConfigurationServiceBean.class));
        container.addBean(Mockito.mock(ServiceInstanceDAO.class));

        container.addBean(Mockito.mock(APPConcurrencyServiceBean.class));
        container.addBean(
                Mockito.mock(ProductProvisioningServiceFactoryBean.class));
        container.addBean(Mockito.mock(APPCommunicationServiceBean.class));

        serviceMock = Mockito.mock(Service.class);
        besDAO = Mockito.mock(BesDAO.class);
        subcriptionService = Mockito.mock(SubscriptionService.class);
        identityService = Mockito.mock(EnhancedIdentityService.class);
        Mockito.doReturn(Arrays.asList(new VOUserDetails())).when(besDAO)
                .getBESTechnologyManagers(any(ServiceInstance.class));

        Mockito.doReturn(identityService).when(besDAO).getBESWebService(
                Matchers.eq(IdentityService.class),
                any(ServiceInstance.class), any(Optional.class));

        Mockito.doNothing().when(besDAO).setUserCredentialsInContext(
                any(BindingProvider.class), anyString(),
                anyString(), Matchers.anyMap());

        Mockito.doReturn(subcriptionService).when(besDAO).getBESWebService(
                Matchers.eq(SubscriptionService.class),
                any(ServiceInstance.class), any(Optional.class));

        Mockito.doNothing().when(subcriptionService).completeAsyncSubscription(
                anyString(), anyString(),
                any(VOInstanceInfo.class));
        Mockito.doNothing().when(subcriptionService).abortAsyncSubscription(
                anyString(), anyString(),
                Matchers.anyListOf(VOLocalizedText.class));
        Mockito.doReturn(subcriptionService).when(serviceMock).getPort(
                any(QName.class),
                Matchers.eq(SubscriptionService.class));

        Mockito.doReturn(serviceMock).when(besDAO).createWebService(
                any(URL.class), any(QName.class));

        Mockito.doReturn(identityService).when(serviceMock).getPort(
                any(QName.class), Matchers.eq(IdentityService.class));

        container.addBean(besDAO);
        container.addBean(Mockito.mock(OperationDAO.class));

        container.addBean(Mockito.mock(ServiceInstanceDAO.class));
        container.addBean(Mockito.mock(OperationServiceBean.class));

        container.addBean(
                authService = Mockito.spy(new APPAuthenticationServiceBean()));
        container.addBean(Mockito.mock(OperationServiceBean.class));

        container.addBean(new APPlatformServiceBean());
        controller = Mockito.mock(APPlatformController.class);
        InitialContext context = new InitialContext();
        context.bind("bss/app/controller/ess.vmware", controller);
        container.addBean(controller);

        besDAO = container.get(BesDAO.class);

        platformService = container.get(APPlatformServiceBean.class);

        em = container.getPersistenceUnit("oscm-app");

        defaultAuth = new PasswordAuthentication("user", "password");
    }

    void authenticateTMForInstance(final String controllerId,
            final String instanceId, final String user, final String password)
            throws Exception {
        authenticateTMForInstance(controllerId, instanceId,
                new PasswordAuthentication(user, password));
    }

    void authenticateTMForInstance(final String controllerId,
            final String instanceId, final long key, final String password)
            throws Exception {
        authenticateTMForInstance(controllerId, instanceId,
                new PasswordAuthentication(Long.toString(key), password));
    }

    void authenticateTMForInstance(final String controllerId,
            final String instanceId, final PasswordAuthentication auth)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                authService.authenticateTMForInstance(controllerId, instanceId,
                        auth);
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateAdministrator_NullUser() throws Throwable {
        authService.authenticateAdministrator(
                new PasswordAuthentication(null, "admin123"));
    }

    @Test
    public void testAuthenticateAdministrator_Params() throws Throwable {
        // given
        VOUserDetails admin = createVOUserDetails(1000, "admin", "app");
        admin.setUserRoles(
                Collections.singleton(UserRoleType.ORGANIZATION_ADMIN));
        Mockito.doReturn(new VOUserDetails()).when(authService)
                .authenticateUser(any(ServiceInstance.class),
                        anyString(),
                        any(PasswordAuthentication.class),
                        any(UserRoleType.class), any(Optional.class));

        // when
        authService.authenticateAdministrator(defaultAuth);

        // then
        verify(authService).authenticateUser(null, null, defaultAuth,
                UserRoleType.ORGANIZATION_ADMIN, Optional.empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateTMForController_NullController()
            throws Throwable {
        authService.authenticateTMForController(null, defaultAuth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateTMForController_NullAuth() throws Throwable {
        authService.authenticateTMForController("controllerId", null);
    }

    @Test(expected = ConfigurationException.class)
    public void testAuthenticateTMForController_NullOrganization()
            throws Throwable {
        authService.authenticateTMForController("controllerId", defaultAuth);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateTMForInstance_NullInstance() throws Throwable {
        authService.authenticateTMForInstance(CTRL_ID, null, defaultAuth);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateTMForInstance_NullUser() throws Throwable {
        authService.authenticateTMForInstance(CTRL_ID, "service1", null);
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticateTMForInstance_NullUserDetails()
            throws Throwable {
        // applyDefaultTMCredentials();
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);
        VOUserDetails userToGet = createVOUserDetails(1000, "user", "customer");

        identityService = Mockito.mock(EnhancedIdentityService.class);

        Mockito.doReturn(null).doReturn(userToGet).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(userToGet).when(besDAO).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));

        authenticateTMForInstance(CTRL_ID, "appInstanceId", defaultAuth);
    }

    void authenticateTMForController(final String controllerId, String user,
            String password) throws Exception {
        authenticateTMForController(controllerId,
                new PasswordAuthentication(user, password));
    }

    void authenticateTMForController(final String controllerId, long key,
            String password) throws Exception {
        authenticateTMForController(controllerId,
                new PasswordAuthentication(Long.toString(key), password));
    }

    void authenticateTMForController(final String controllerId,
            final PasswordAuthentication auth) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                authService.authenticateTMForController(controllerId, auth);
                return null;
            }
        });
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticateTMForInstance_NullOrganizationId()
            throws Throwable {

        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);
        VOUserDetails userToGet = createVOUserDetails(1000, "user", null);

        Mockito.doReturn(new VOUserDetails()).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(userToGet).when(besDAO).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));

        authenticateTMForInstance(CTRL_ID, "appInstanceId", defaultAuth);
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticateTMForInstance_wrongOrg() throws Throwable {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);
        VOUserDetails org = createVOUserDetails(10000, "supplier", "tp123");
        VOUserDetails wrongOrg = createVOUserDetails(20001, "user", "customer");
        Mockito.doReturn(org).doReturn(wrongOrg).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId",
                new PasswordAuthentication(wrongOrg.getUserId(), "secret"));

    }

    @SuppressWarnings("unchecked")
    @Test(expected = AuthenticationException.class)
    public void testAuthenticateTMForInstance_UserNotFound() throws Throwable {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);
        VOUserDetails currentUserDetails = createVOUserDetails(1000, "userid",
                "orgid");
        Mockito.doReturn(currentUserDetails).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication(
                currentUserDetails.getUserId(), "pass")).when(configService)
                .getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doThrow(new AuthenticationException("not found")).when(besDAO)
                .getUser(any(ServiceInstance.class),
                        any(VOUser.class), any(Optional.class));

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId",
                new PasswordAuthentication("someone", "secret"));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = APPlatformException.class)
    public void testAuthenticateTMForInstance_OperationNotPermitted()
            throws Throwable {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);
        VOUserDetails currentUserDetails = createVOUserDetails(1000,
                defaultAuth.getUserName(), "orgid");
        Mockito.doReturn(currentUserDetails).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication(
                currentUserDetails.getUserId(), "pass")).when(configService)
                .getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doThrow(new APPlatformException("any")).when(besDAO).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId", defaultAuth);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AuthenticationException.class)
    public void testAuthenticateTMForInstance_noRoles() throws Throwable {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);
        VOUserDetails user = createVOUserDetails(10000, "supplier", "tp123");
        Mockito.doReturn(user).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId",
                new PasswordAuthentication("supplier", "secret"));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = AuthenticationException.class)
    public void testAuthenticateTMForInstance_wrongRole() throws Throwable {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);
        VOUserDetails user = createVOUserDetails(10000, "supplier", "tp123");
        user.addUserRole(UserRoleType.MARKETPLACE_OWNER);
        Mockito.doReturn(user).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId",
                new PasswordAuthentication("supplier", "secret"));
    }

    @Ignore
    // TODO in besDAO
    @Test(expected = AuthenticationException.class)
    public void testAuthenticateTMForInstance_wrongCreds() throws Throwable {

        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);

        VOUserDetails user = createVOUserDetails(10000, "supplier", "tp123");

        Mockito.doReturn(user).when(identityService).getCurrentUserDetails();
        Mockito.doReturn(user).when(identityService)
                .getUser(any(VOUser.class));

        authenticateTMForInstance(CTRL_ID, "appInstanceId",
                new PasswordAuthentication("supplier", "wrong"));
    }

    @Ignore
    // test in besDAO
    @Test(expected = ConfigurationException.class)
    public void testAuthenticateTMForInstance_EmptyUser() throws Throwable {

        Map<String, Setting> parameters = new HashMap<>();
        parameters.put(InstanceParameter.BSS_USER,
                new Setting(InstanceParameter.BSS_USER, ""));
        createServiceInstance(ProvisioningStatus.COMPLETED, parameters);

        // identityService = new UserBase().mockIdentityService();

        authenticateTMForInstance(CTRL_ID, "appInstanceId", defaultAuth);
    }

    @Ignore
    // TODO test in besDAO
    @Test(expected = ConfigurationException.class)
    public void testAuthenticate_MalformedUrl() throws Throwable {

        // applyDefaultAPPCredentials();
        proxyConfigSettings.put(
                PlatformConfigurationKey.BSS_WEBSERVICE_URL.name(), "http");
        proxyConfigSettings.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(),
                "INTERNAL");
        try {
            besDAO.getBESWebService(IdentityService.class, null, Optional.empty());
        } catch (ConfigurationException e) {
            assertEquals(PlatformConfigurationKey.BSS_WEBSERVICE_URL.name(),
                    e.getAffectedKey());
            throw e;
        }
    }

    @Test(expected = ConfigurationException.class)
    public void testAuthenticateTMForController_noOrgConfigured()
            throws Throwable {
        authService.authenticateTMForController("ess.test", defaultAuth);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateTMForInstance_Internal() throws Throwable {
        // given
        Map<String, String> settings = getProxySettingsForMode("INTERNAL");
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.BSS_USER, InstanceParameter.BSS_USER_PWD);
        VOUserDetails manager = createVOUserDetails(10000, "user", "tp123");
        manager.setUserRoles(
                Collections.singleton(UserRoleType.TECHNOLOGY_MANAGER));
        Mockito.doReturn(manager).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doReturn(settings).when(configService)
                .getAllProxyConfigurationSettings();

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId", manager.getKey(),
                "pass");

        // then
        verify(besDAO, Mockito.times(0)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
        verify(besDAO, Mockito.times(2)).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateTMForInstance_Internal_noUserKey()
            throws Throwable {
        // given
        Map<String, String> settings = getProxySettingsForMode("INTERNAL");
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.BSS_USER, InstanceParameter.BSS_USER_PWD);
        VOUserDetails manager = createVOUserDetails(0, "user", "tp123");
        manager.setUserRoles(
                Collections.singleton(UserRoleType.TECHNOLOGY_MANAGER));
        Mockito.doReturn(manager).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doReturn(settings).when(configService)
                .getAllProxyConfigurationSettings();

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId", 0, "pass");

        // then
        verify(besDAO, Mockito.times(1)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
        verify(besDAO, Mockito.times(2)).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateTMForInstance_SSO() throws Throwable {
        // given
        Map<String, String> settings = getProxySettingsForMode("SAML_SP");
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.BSS_USER, InstanceParameter.BSS_USER_PWD);
        VOUserDetails manager = createVOUserDetails(10000, "user", "tp123");
        manager.setUserRoles(
                Collections.singleton(UserRoleType.TECHNOLOGY_MANAGER));
        Mockito.doReturn(manager).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doReturn(settings).when(configService)
                .getAllProxyConfigurationSettings();

        // when
        authenticateTMForInstance(CTRL_ID, "appInstanceId", manager.getUserId(),
                "pass");

        // then
        verify(besDAO, Mockito.times(0)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
        verify(besDAO, Mockito.times(2)).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateTMForInstance_SSO_noUserId() throws Throwable {
        authenticateTMForInstance(CTRL_ID, "appInstanceId", null, "pass");
    }

    @Ignore
    // TODO test in besDAO
    @Test(expected = ConfigurationException.class)
    public void testAuthenticateTMForInstance_SSO_withoutConfiguredPassword()
            throws Throwable {

        proxyConfigSettings.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(),
                "SAML_SP");
        VOUserDetails supplier = createVOUserDetails(10000, "supplier",
                "tp123");
        controllerConfigSettings
                .put(ControllerConfigurationKey.BSS_USER_PWD.name(), null);
        // do not add instance specific TP credentials
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);

        authenticateTMForInstance(CTRL_ID, "appInstanceId",
                supplier.getUserId(), "secret");
        UserBase userBase = new UserBase();
        userBase.addUser(supplier, "secret");
        identityService = userBase.mockIdentityService();

    }

    @Test
    public void testAuthenticateTMForController_Internal() throws Throwable {

        // given
        Map<String, String> proxySettings = getProxySettingsForMode("INTERNAL");
        Map<String, Setting> controlleSettings = getControllerSettingsForOrg(
                "tp123");

        VOUserDetails manager = createVOUserDetails(10001, "user", "tp123");
        manager.setUserRoles(
                Collections.singleton(UserRoleType.TECHNOLOGY_MANAGER));

        Mockito.doReturn(proxySettings).when(configService)
                .getAllProxyConfigurationSettings();
        Mockito.doReturn(controlleSettings).when(configService)
                .getControllerConfigurationSettings(anyString());
        Mockito.doReturn(manager).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));

        // when
        authenticateTMForController(CTRL_ID, manager.getKey(), "pass");

        // then
        verify(besDAO, Mockito.times(0)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
        verify(besDAO, Mockito.times(1)).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
    }

    @Test
    public void testAuthenticateTMForController_Internal_noUserKey()
            throws Throwable {

        // given
        Map<String, String> proxySettings = getProxySettingsForMode("INTERNAL");
        Map<String, Setting> controlleSettings = getControllerSettingsForOrg(
                "tp123");

        VOUserDetails manager = createVOUserDetails(0, "user", "tp123");
        manager.setUserRoles(
                Collections.singleton(UserRoleType.TECHNOLOGY_MANAGER));
        Mockito.doReturn(proxySettings).when(configService)
                .getAllProxyConfigurationSettings();
        Mockito.doReturn(controlleSettings).when(configService)
                .getControllerConfigurationSettings(anyString());
        Mockito.doReturn(manager).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));

        // when
        authenticateTMForController(CTRL_ID, manager.getKey(), "pass");

        // then
        verify(besDAO, Mockito.times(1)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
        verify(besDAO, Mockito.times(1)).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
    }

    @Test
    public void testAuthenticateTMForController_SSO() throws Throwable {

        // given
        Map<String, String> proxySettings = getProxySettingsForMode("SAML_SP");
        Map<String, Setting> controlleSettings = getControllerSettingsForOrg(
                "tp123");

        VOUserDetails manager = createVOUserDetails(10001, "user", "tp123");
        manager.setUserRoles(
                Collections.singleton(UserRoleType.TECHNOLOGY_MANAGER));

        Mockito.doReturn(proxySettings).when(configService)
                .getAllProxyConfigurationSettings();
        Mockito.doReturn(controlleSettings).when(configService)
                .getControllerConfigurationSettings(anyString());
        Mockito.doReturn(manager).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));

        // when
        authenticateTMForController(CTRL_ID, manager.getUserId(), "pass");

        // then
        verify(besDAO, Mockito.times(0)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
        verify(besDAO, Mockito.times(1)).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateTMForController_noUserId() throws Throwable {

        // given
        Map<String, Setting> controlleSettings = getControllerSettingsForOrg(
                "tp123");
        Mockito.doReturn(controlleSettings).when(configService)
                .getControllerConfigurationSettings(anyString());

        // when
        authenticateTMForController(CTRL_ID, null, "pass");
    }

    @Test(expected = ConfigurationException.class)
    public void testAuthenticateTMForController_noOrgId() throws Throwable {

        // given
        Map<String, String> controllerSettings = new HashMap<>();
        VOUserDetails manager = createVOUserDetails(10000, "user", "tp123");
        Mockito.doReturn(controllerSettings).when(configService)
                .getControllerConfigurationSettings(anyString());

        // when
        authenticateTMForController(CTRL_ID, manager.getUserId(), "pass");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateTMForController_noControllerSettings()
            throws Throwable {

        // given
        VOUserDetails manager = createVOUserDetails(10000, "user", "tp123");
        Mockito.doThrow(new ConfigurationException("test")).when(configService)
                .getAuthenticationForBESTechnologyManager(anyString(),
                        any(ServiceInstance.class), Matchers.anyMap());
        Mockito.doReturn(null).when(authService)
                .getAuthenticatedTMForController(anyString(),
                        any(PasswordAuthentication.class));
        ArgumentCaptor<PasswordAuthentication> ac = ArgumentCaptor
                .forClass(PasswordAuthentication.class);

        // when
        authenticateTMForController(CTRL_ID, manager.getUserId(), "pass");

        // then
        verify(authService).getAuthenticatedTMForController(
                anyString(), ac.capture());
        assertEquals(manager.getUserId(), ac.getValue().getUserName());
        assertEquals("pass", ac.getValue().getPassword());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateTMForController_controllerSettingsMatch()
            throws Throwable {

        // given
        Map<String, String> proxySettings = getProxySettingsForMode("INTERNAL");
        Map<String, Setting> controlleSettings = getControllerSettingsForOrg(
                "tp123");

        Mockito.doReturn(proxySettings).when(configService)
                .getAllProxyConfigurationSettings();
        Mockito.doReturn(controlleSettings).when(configService)
                .getControllerConfigurationSettings(anyString());
        Mockito.doReturn(new PasswordAuthentication("user", "pass"))
                .when(configService)
                .getAuthenticationForBESTechnologyManager(anyString(),
                        any(ServiceInstance.class), Matchers.anyMap());

        Mockito.doReturn(null).when(authService)
                .getAuthenticatedTMForController(anyString(),
                        any(PasswordAuthentication.class));

        // when
        authenticateTMForController(CTRL_ID, "user", "pass");

        // then
        verify(authService, Mockito.times(0))
                .getAuthenticatedTMForController(anyString(),
                        any(PasswordAuthentication.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateAdministrator_Internal() throws Throwable {
        // given
        Map<String, String> settings = getProxySettingsForMode("INTERNAL");
        VOUserDetails admin = createVOUserDetails(1000, "admin", "org");
        admin.setUserRoles(
                Collections.singleton(UserRoleType.ORGANIZATION_ADMIN));

        Mockito.doReturn(admin).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doReturn(settings).when(configService)
                .getAllProxyConfigurationSettings();

        // when
        authService.authenticateAdministrator(
                new PasswordAuthentication("1000", "admin123"));

        // then
        verify(besDAO, Mockito.times(0)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateAdministrator_Internal_noKey()
            throws Throwable {
        // given
        Map<String, String> settings = getProxySettingsForMode("INTERNAL");
        VOUserDetails admin = createVOUserDetails(0, "admin", "org");
        admin.setUserRoles(
                Collections.singleton(UserRoleType.ORGANIZATION_ADMIN));

        Mockito.doReturn(admin).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doReturn(settings).when(configService)
                .getAllProxyConfigurationSettings();

        // when
        authService.authenticateAdministrator(
                new PasswordAuthentication("0", "admin123"));

        // then
        verify(besDAO, Mockito.times(1)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticateAdministrator_SSO() throws Throwable {
        // given
        Map<String, String> settings = getProxySettingsForMode("SAML_SP");
        VOUserDetails admin = createVOUserDetails(1000, "admin", "org");
        admin.setUserRoles(
                Collections.singleton(UserRoleType.ORGANIZATION_ADMIN));

        Mockito.doReturn(admin).when(besDAO).getUserDetails(
                any(ServiceInstance.class), any(VOUser.class),
                anyString(), any(Optional.class));
        Mockito.doReturn(new PasswordAuthentication("nobody", ""))
                .when(configService).getWebServiceAuthentication(
                        any(ServiceInstance.class), Matchers.anyMap(), any(Optional.class));
        Mockito.doReturn(settings).when(configService)
                .getAllProxyConfigurationSettings();

        // when
        authService.authenticateAdministrator(
                new PasswordAuthentication(admin.getUserId(), "admin123"));

        // then
        verify(besDAO, Mockito.times(0)).getUser(
                any(ServiceInstance.class),
                any(VOUser.class), any(Optional.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateAdministrator_noUserId() throws Throwable {
        authService.authenticateAdministrator(
                new PasswordAuthentication(null, "admin123"));
    }

    Map<String, String> getProxySettingsForMode(String authMode) {
        Map<String, String> settings = new HashMap<>();
        settings.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(), authMode);
        return settings;
    }

    Map<String, Setting> getControllerSettingsForOrg(String orgId) {
        Map<String, Setting> settings = new HashMap<>();
        settings.put(ControllerConfigurationKey.BSS_ORGANIZATION_ID.name(),
                new Setting(
                        ControllerConfigurationKey.BSS_ORGANIZATION_ID.name(),
                        orgId));
        return settings;
    }

    @Ignore
    // TODO test in besDAO
    @Test(expected = ConfigurationException.class)
    public void testAuthenticateAdministrator_SSO_WithoutConfiguredPassword()
            throws Throwable {

        proxyConfigSettings.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(),
                "SAML_SP");
        proxyConfigSettings.put(PlatformConfigurationKey.BSS_USER_PWD.name(),
                null);

        authService.authenticateAdministrator(
                new PasswordAuthentication("admin", "admin123"));
    }

    @Test
    public void testGetEventServiceUrl_NoSlash() throws Exception {
        // when
        String baseUrl = "http://127.0.0.1:8080/test";
        Mockito.doReturn(baseUrl).when(configService)
                .getProxyConfigurationSetting(
                        PlatformConfigurationKey.APP_BASE_URL);
        // when
        String eventServiceUrl = platformService.getEventServiceUrl();

        // then
        assertEquals(baseUrl + "/notify", eventServiceUrl);
    }

    @Test
    public void testGetEventServiceUrl_WithSlash() throws Exception {
        // given
        String baseUrl = "http://127.0.0.1:8080/test";
        Mockito.doReturn(baseUrl + "/").when(configService)
                .getProxyConfigurationSetting(
                        PlatformConfigurationKey.APP_BASE_URL);
        // when
        String eventServiceUrl = platformService.getEventServiceUrl();

        // then
        assertEquals(baseUrl + "/notify", eventServiceUrl);
    }

    @Test
    public void testGetBSSWebServiceUrl() throws Exception {
        // given
        String baseUrl = "http://127.0.0.1:8080/test";
        Mockito.doReturn(baseUrl).when(configService)
                .getProxyConfigurationSetting(
                        PlatformConfigurationKey.BSS_WEBSERVICE_URL);
        Mockito.doReturn("INTERNAL").when(configService)
                .getProxyConfigurationSetting(
                        PlatformConfigurationKey.BSS_AUTH_MODE);
        // when
        String webServiceUrl = platformService.getBSSWebServiceUrl();

        // then
        assertEquals(baseUrl, webServiceUrl);
    }

    @Test
    public void testGetBSSWebServiceUrl_SAML_SP() throws Exception {
        // given
        String baseUrl = "http://127.0.0.1:8080/test";
        Mockito.doReturn(baseUrl).when(configService)
                .getProxyConfigurationSetting(
                        PlatformConfigurationKey.BSS_STS_WEBSERVICE_URL);
        Mockito.doReturn("SAML_SP").when(configService)
                .getProxyConfigurationSetting(
                        PlatformConfigurationKey.BSS_AUTH_MODE);
        // when
        String webServiceUrl = platformService.getBSSWebServiceUrl();

        // then
        assertEquals(baseUrl, webServiceUrl);
    }

    /**
     * Creates and persists a service instance object.
     * 
     * @param status
     *            the status to be set for the service instance
     * @param parameters
     *            the keys for the parameters to be created
     */
    private ServiceInstance createServiceInstance(
            final ProvisioningStatus status, final String... parameter)
            throws Exception {

        // Use linked hash map to keep order of entries (for asserts)
        Map<String, Setting> parameters = new LinkedHashMap<>();
        for (String parameterKey : parameter) {
            if (InstanceParameter.PUBLIC_IP.equals(parameterKey)) {
                parameters.put(InstanceParameter.PUBLIC_IP,
                        new Setting(InstanceParameter.PUBLIC_IP, "4.3.2.1"));
            } else {
                String pValue = parameterKey + "Value";
                Setting setting = new Setting(parameterKey, pValue);
                if (parameterKey.endsWith("_PWD")) {
                    pValue = AESEncrypter.encrypt(pValue);
                    setting.setEncrypted(true);
                }
                parameters.put(parameterKey, setting);
            }
        }
        return createServiceInstance(status, parameters);
    }

    /**
     * Creates and persists a service instance object.
     * 
     * @param status
     *            the status to be set for the service instance
     * @param instanceProvisioning
     *            whether or not to set the instanceProvisioningRequest flag
     * @param parameters
     *            the keys and values for the parameters to be created
     */
    private ServiceInstance createServiceInstance(
            final ProvisioningStatus status,
            final Map<String, Setting> parameters) throws Exception {
        return runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                ServiceInstance si = new ServiceInstance();
                si.setServiceBaseURL("baseURL");
                si.setBesLoginURL("besLoginURL");
                si.setDefaultLocale("de");
                si.setOrganizationId("orgId");
                si.setInstanceProvisioning(requestInstanceProvisioning);
                si.setControllerReady(setControllerReady);
                if (parameters != null
                        && parameters.get("subscriptionId") != null) {
                    si.setSubscriptionId(
                            parameters.get("subscriptionId").getValue());
                } else {
                    si.setSubscriptionId("subId");
                }
                if (parameters != null
                        && parameters.get("instanceId") != null) {
                    si.setInstanceId(parameters.get("instanceId").getValue());
                } else {
                    si.setInstanceId("appInstanceId");
                }

                si.setControllerId("ess.vmware");
                si.setProvisioningStatus(status);
                if (parameters != null) {
                    for (String parameterKey : parameters.keySet()) {
                        if (parameterKey == null) {
                            throw new RuntimeException(
                                    "parameter key must not be null");
                        }
                        InstanceParameter ip = new InstanceParameter();
                        ip.setParameterKey(parameterKey);
                        ip.setParameterValue(
                                parameters.get(parameterKey).getValue());
                        ip.setServiceInstance(si);
                        si.getInstanceParameters().add(ip);
                    }
                }
                em.persist(si);
                em.flush();
                // instanceId = si.getInstanceId();
                return si;
            }
        });
    }

    /**
     * Creates VOUserDetails with given key, userId, orgId.
     */
    private VOUserDetails createVOUserDetails(long key, String id,
            String orgId) {
        VOUserDetails user = new VOUserDetails();
        user.setUserId(id);
        user.setKey(key);
        user.setOrganizationId(orgId);
        return user;
    }

    /**
     * Internal interface combining IdentityService with BindingProvider to
     * allow easy mocking.
     */
    private static interface EnhancedIdentityService
            extends IdentityService, BindingProvider {
    }

    private static class UserBase {

        private boolean useSSO;

        private HashMap<Long, VOUserDetails> byKey = new HashMap<>();
        private HashMap<String, VOUserDetails> byId = new HashMap<>();
        private HashMap<Long, String> passwordsByKey = new HashMap<>();

        final Map<String, Object> stackRequestContext = new HashMap<>();
        final Map<String, Object> requestContext = new HashMap<>();

        public VOUserDetails authenticate(Map<String, Object> requestContext) throws Exception {
            Object user = requestContext
                    .get(useSSO ? "username"
                            : BindingProvider.USERNAME_PROPERTY);
            Object pwd = requestContext
                    .get(useSSO ? "password"
                            : BindingProvider.PASSWORD_PROPERTY);
            if (user != null) {
                Long userKey = null;
                if (useSSO) {
                    VOUserDetails userDetails = byId.get(user);
                    if (userDetails != null) {
                        userKey = Long.valueOf(userDetails.getKey());
                    } else {
                        throw new Exception(
                                new RuntimeException());
                    }
                } else {
                    userKey = Long.valueOf(user.toString());
                }
                if (passwordsByKey.get(userKey) != null
                        && passwordsByKey.get(userKey).equals(pwd)) {
                    return byKey.get(userKey);
                }
            }
            throw new Exception(new RuntimeException());
        }

        private void addUser(VOUserDetails user, String password) {
            if (user != null) {
                if (user.getKey() != 0 && user.getUserId() != null) {
                    byId.put(user.getUserId(), user);
                    byKey.put(Long.valueOf(user.getKey()), user);
                    passwordsByKey.put(Long.valueOf(user.getKey()), password);
                } else {
                    throw new IllegalArgumentException(
                            "User must have ID and key!");
                }
            }
        }

        private VOUserDetails get(VOUser user) throws ObjectNotFoundException {
            if (user.getKey() != 0) {
                VOUserDetails voUserDetails = byKey
                        .get(Long.valueOf(user.getKey()));
                if (voUserDetails != null) {
                    return voUserDetails;
                }
            }
            VOUserDetails voUserDetails = byId.get(user.getUserId());
            if (voUserDetails != null) {
                return voUserDetails;
            }
            throw new ObjectNotFoundException();
        }

        private EnhancedIdentityService mockIdentityService()
                throws ObjectNotFoundException, OperationNotPermittedException,
                OrganizationRemovedException {
            EnhancedIdentityService idSvc = Mockito
                    .mock(EnhancedIdentityService.class);
            Answer<VOUserDetails> answerGetCurrentUserDetails = new Answer<VOUserDetails>() {

                @Override
                public VOUserDetails answer(InvocationOnMock invocation)
                        throws Throwable {
                    for (String key : requestContext.keySet()) {
                        stackRequestContext.put(key, requestContext.get(key));
                    }
                    return authenticate(requestContext);
                }
            };
            Answer<VOUser> answerGetUser = new Answer<VOUser>() {

                @Override
                public VOUser answer(InvocationOnMock invocation)
                        throws Throwable {
                    Object[] arguments = invocation.getArguments();
                    VOUser userParameter = (VOUser) arguments[0];
                    authenticate(requestContext);
                    return get(userParameter);
                }
            };

            Mockito.doAnswer(answerGetCurrentUserDetails).when(idSvc)
                    .getCurrentUserDetails();
            Mockito.doAnswer(answerGetUser).when(idSvc)
                    .getUser(any(VOUser.class));
            Mockito.when((idSvc).getRequestContext())
                    .thenReturn(requestContext);
            return idSvc;
        }
    }
}
