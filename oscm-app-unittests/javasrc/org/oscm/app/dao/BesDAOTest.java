/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 17.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.business.exceptions.BESNotificationException;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.ControllerConfigurationKey;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.intf.IdentityService;
import org.oscm.intf.SubscriptionService;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAuthoritiesException;
import org.oscm.types.exceptions.OrganizationRemovedException;
import org.oscm.types.exceptions.SubscriptionStateException;
import org.oscm.types.exceptions.TechnicalServiceNotAliveException;
import org.oscm.types.exceptions.TechnicalServiceOperationException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOInstanceInfo;
import org.oscm.vo.VOLocalizedText;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

public class BesDAOTest {

    private final static String USER = "user";
    private final static String PASSWORD = "pass";
    private final static String USER_ID = "userId";
    private final static String USER_KEY = "userKey";
    private final static String USER_PWD = "userPwd";
    private final static String USER_ID_TM = "userId";
    private final static String USER_KEY_TM = "userKey";
    private final static String USER_PWD_TM = "userPwd";
    private final static String USER_TM_TechSvc = "user";
    private final static String USER_PWD_TM_TechSvc = "userPwd";

    private final BesDAO besDAO = spy(new BesDAO());
    private final EnhancedIdentityService idServ = mock(EnhancedIdentityService.class);
    private final SubscriptionService subServ = mock(SubscriptionService.class);
    private final APPConfigurationServiceBean confServ = mock(APPConfigurationServiceBean.class);

    /**
     * Internal interface combining IdentityService with BindingProvider to
     * allow easy mocking.
     */
    private static interface EnhancedIdentityService extends IdentityService,
            BindingProvider {
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws APPlatformException, MalformedURLException {
        besDAO.configService = confServ;
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(idServ).when(besDAO).getServicePort(eq(IdentityService.class),
                anyMap());
        doReturn(subServ).when(besDAO).getServicePort(
                eq(SubscriptionService.class), anyMap());
        doNothing().when(besDAO).validateVersion(anyString());
        Binding mockBinding = mock(Binding.class);
        when(idServ.getBinding()).thenReturn(mockBinding);
    }

    @Test
    public void isSsoMode_true() {
        // given
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");

        // when
        Boolean actual = Boolean.valueOf(besDAO.isSsoMode(settings));

        // then
        assertEquals(Boolean.valueOf(true), actual);
    }

    @Test
    public void isSsoMode_false() {
        // given
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");

        // when
        Boolean actual = Boolean.valueOf(besDAO.isSsoMode(settings));

        // then
        assertEquals(Boolean.valueOf(false), actual);
    }

    @Test
    public void getWsdlUrl_INTERNAL() throws MalformedURLException {
        // given
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");

        // when
        URL actual = besDAO.getWsdlUrl(IdentityService.class, settings);

        // then
        assertEquals(
                "https://localhost:8181/oscm/v1.9/IdentityService/BASIC?wsdl",
                actual.toString());
    }

    @Test
    public void getWsdlUrl_SAML_SP() throws MalformedURLException {
        // given
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");

        // when
        URL actual = besDAO.getWsdlUrl(IdentityService.class, settings);

        // then
        assertEquals(
                "https://localhost:8181/oscm/v1.9/IdentityService/STS?wsdl",
                actual.toString());
    }

    @Test
    public void getPortSuffix_INTERNAL() {
        // given
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");

        // when
        String actual = besDAO.getPortSuffix(settings);

        // then
        assertEquals("PortBASIC", actual);
    }

    @Test
    public void getPortSuffix_SAML_SP() {
        // given
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");

        // when
        String actual = besDAO.getPortSuffix(settings);

        // then
        assertEquals("PortSTS", actual);
    }

    @Test
    public void getPasswordConstant_INTERNAL() {
        // given
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");

        // when
        String actual = besDAO.getPasswordConstant(settings);

        // then
        assertEquals(BindingProvider.PASSWORD_PROPERTY, actual);
    }

    @Test
    public void getPasswordConstant_SAML_SP() {
        // given
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");

        // when
        String actual = besDAO.getPasswordConstant(settings);

        // then
        assertEquals("password", actual);
    }

    @Test
    public void getUsernameConstant_INTERNAL() {
        // given
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");

        // when
        String actual = besDAO.getUsernameConstant(settings);

        // then
        assertEquals(BindingProvider.USERNAME_PROPERTY, actual);
    }

    @Test
    public void getUsernameConstant_SAML_SP() {
        // given
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");

        // when
        String actual = besDAO.getUsernameConstant(settings);

        // then
        assertEquals("username", actual);
    }

    @Test
    public void setUserCredentialsInContext_INTERNAL() {
        // given
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");
        BindingProvider client = Mockito.mock(BindingProvider.class);
        Map<String, String> context = new HashMap<>();
        Mockito.doReturn(context).when(client).getRequestContext();

        // when
        besDAO.setUserCredentialsInContext(client, USER, PASSWORD, settings);

        // then
        assertNull(client.getRequestContext().get(
                "username"));
        assertNull(client.getRequestContext().get(
                "password"));
        assertEquals(
                USER,
                client.getRequestContext().get(
                        BindingProvider.USERNAME_PROPERTY));
        assertEquals(
                PASSWORD,
                client.getRequestContext().get(
                        BindingProvider.PASSWORD_PROPERTY));
    }

    @Test
    public void setUserCredentialsInContext_SAML_SP() {
        // given
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");
        BindingProvider client = Mockito.mock(BindingProvider.class);
        Map<String, String> context = new HashMap<>();
        Mockito.doReturn(context).when(client).getRequestContext();

        // when
        besDAO.setUserCredentialsInContext(client, USER, PASSWORD, settings);

        // then
        assertNull(client.getRequestContext().get(
                BindingProvider.USERNAME_PROPERTY));
        assertNull(client.getRequestContext().get(
                BindingProvider.PASSWORD_PROPERTY));
        assertEquals(USER,
                client.getRequestContext().get("username"));
        assertEquals(PASSWORD,
                client.getRequestContext().get("password"));
    }

    @Test
    public void getServicePort() throws MalformedURLException {
        // given
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");
        IdentityService idSvcMock = Mockito.mock(IdentityService.class);
        Service serviceMock = Mockito.mock(Service.class);
        doReturn(serviceMock).when(besDAO).createWebService(any(URL.class),
                any(QName.class));
        when(
                serviceMock.getPort(Matchers.any(QName.class),
                        eq(IdentityService.class))).thenReturn(idSvcMock);

        // when
        IdentityService idSvc = besDAO.getServicePort(IdentityService.class,
                settings);

        // then
        assertTrue(IdentityService.class.isAssignableFrom(idSvc.getClass()));
    }

    @Test
    public void getTechnologyProviderManagers() throws APPlatformException {
        // given
        List<VOUserDetails> users = new ArrayList<>();
        users.add(givenUser(UserRoleType.TECHNOLOGY_MANAGER, "mail"));
        users.add(givenUser(UserRoleType.MARKETPLACE_OWNER, "mail"));
        users.add(givenUser(UserRoleType.ORGANIZATION_ADMIN, ""));
        IdentityService is = Mockito.mock(IdentityService.class);
        Mockito.doReturn(is)
                .when(besDAO)
                .getBESWebService(Matchers.eq(IdentityService.class),
                        Matchers.any(ServiceInstance.class), any(Optional.class));
        Mockito.doReturn(users).when(is).getUsersForOrganization();

        // when
        List<VOUserDetails> admins = besDAO
                .getBESTechnologyManagers(new ServiceInstance());

        // then
        assertEquals(1, admins.size());
        assertTrue(admins.get(0).getUserRoles()
                .contains(UserRoleType.TECHNOLOGY_MANAGER));
        assertEquals("mail", admins.get(0).getEMail());
    }

    @Test
    public void getTechnologyProviderManagers_Exception()
            throws APPlatformException {
        // given
        Mockito.doThrow(new RuntimeException())
                .when(besDAO)
                .getBESWebService(Matchers.eq(IdentityService.class),
                        Matchers.any(ServiceInstance.class), any(Optional.class));

        // when
        List<VOUserDetails> admins = besDAO
                .getBESTechnologyManagers(new ServiceInstance());

        // then
        assertEquals(0, admins.size());
    }

    @Test
    public void getInstanceInfo() {
        // given
        ServiceInstance si = new ServiceInstance();
        si.setInstanceId("instanceId");
        InstanceResult ir = givenInstanceResult(0);

        // when
        VOInstanceInfo info = besDAO.getInstanceInfo(si, ir);

        // then
        Assert.assertEquals(si.getInstanceId(), info.getInstanceId());
        Assert.assertEquals(ir.getInstance().getAccessInfo(),
                info.getAccessInfo());
        Assert.assertEquals(ir.getInstance().getBaseUrl(), info.getBaseUrl());
        Assert.assertEquals(ir.getInstance().getLoginPath(),
                info.getLoginPath());
    }

    @Test(expected = AuthenticationException.class)
    public void getUser_ObjectNotFound() throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException,
            APPlatformException {
        // given
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));
        doThrow(new ObjectNotFoundException()).when(idServ).getUser(
                any(VOUser.class));

        // when
        besDAO.getUser(new ServiceInstance(), new VOUser(), Optional.empty());
    }

    @Test(expected = AuthenticationException.class)
    public void getUser_OperationNotPermitted() throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException,
            APPlatformException {
        // given
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));
        doThrow(new OperationNotPermittedException()).when(idServ).getUser(
                any(VOUser.class));

        // when
        besDAO.getUser(new ServiceInstance(), new VOUser(), Optional.empty());
    }

    @Test(expected = AuthenticationException.class)
    public void getUser_OrganizationRemovedException()
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationRemovedException, APPlatformException {
        // given
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));
        doThrow(new OrganizationRemovedException()).when(idServ).getUser(
                any(VOUser.class));

        // when
        besDAO.getUser(new ServiceInstance(), new VOUser(), Optional.empty());
    }

    @Test(expected = APPlatformException.class)
    public void getUser_APPlatformException() throws APPlatformException {
        // given
        doThrow(new APPlatformException(Arrays.asList(new LocalizedText())))
                .when(besDAO).getBESWebService(eq(IdentityService.class),
                        any(ServiceInstance.class), any(Optional.class));
        // when
        besDAO.getUser(new ServiceInstance(), new VOUser(), Optional.empty());
    }

    @Test(expected = APPlatformException.class)
    public void getUserDetails_APPlatformException() throws APPlatformException {
        // given
        doThrow(new APPlatformException(Arrays.asList(new LocalizedText())))
                .when(besDAO).getBESWebService(eq(IdentityService.class),
                        any(ServiceInstance.class), any(Optional.class));
        // when
        besDAO.getUserDetails(new ServiceInstance(), null, null, Optional.empty());
    }

    @Test
    public void getUser() throws ObjectNotFoundException,
            OperationNotPermittedException, OrganizationRemovedException,
            APPlatformException {
        // given
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));
        VOUser user = new VOUser();

        // when
        besDAO.getUser(new ServiceInstance(), user, Optional.empty());

        // then
        verify(idServ).getUser(user);
    }

    @Test
    public void getUserDetails_currentUser() throws APPlatformException {
        // given
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));

        // when
        besDAO.getUserDetails(new ServiceInstance(), null, null, Optional.empty());

        // then
        verify(idServ).getCurrentUserDetails();
    }

    @Test
    public void getUserDetails_givenUser_INTERNAL() throws APPlatformException {
        // given
        besDAO.configService = confServ;
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");
        doReturn(settings).when(besDAO.configService)
                .getAllProxyConfigurationSettings();
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));
        VOUser user = givenUser(null, "mail");

        // when
        besDAO.getUserDetails(new ServiceInstance(), user, "password", Optional.empty());

        // then
        verify(besDAO).setUserCredentialsInContext(any(BindingProvider.class),
                eq(Long.valueOf(user.getKey()).toString()), eq("password"),
                eq(settings));
        verify(idServ).getCurrentUserDetails();
    }

    @Test
    public void getUserDetails_givenUser_SSO() throws APPlatformException {
        // given
        besDAO.configService = confServ;
        Map<String, Setting> settings = getSettingsForMode("SAML_SP");
        doReturn(settings).when(besDAO.configService)
                .getAllProxyConfigurationSettings();
        doReturn(idServ).when(besDAO).getBESWebService(
                eq(IdentityService.class), any(ServiceInstance.class), any(Optional.class));
        VOUser user = givenUser(null, "mail");

        // when
        besDAO.getUserDetails(new ServiceInstance(), user, "password", Optional.empty());

        // then
        verify(besDAO).setUserCredentialsInContext(any(BindingProvider.class),
                eq(user.getUserId()), eq("password"), eq(settings));
        verify(idServ).getCurrentUserDetails();
    }

    @Test
    public void notifyAsyncSubscription_completion()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        VOInstanceInfo info = new VOInstanceInfo();
        doReturn(info).when(besDAO).getInstanceInfo(any(ServiceInstance.class),
                any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then
        verify(subServ).completeAsyncSubscription(si.getSubscriptionId(),
                si.getOrganizationId(), info);
    }

    @Test
    public void notifyAsyncModifySubscription_completion()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        VOInstanceInfo info = new VOInstanceInfo();
        doReturn(info).when(besDAO).getInstanceInfo(any(ServiceInstance.class),
                any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then
        verify(subServ).completeAsyncModifySubscription(si.getSubscriptionId(),
                si.getOrganizationId(), info);
    }

    @Test
    public void notifyAsyncUpgradeSubscription_completion()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        VOInstanceInfo info = new VOInstanceInfo();
        doReturn(info).when(besDAO).getInstanceInfo(any(ServiceInstance.class),
                any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then
        verify(subServ).completeAsyncUpgradeSubscription(
                si.getSubscriptionId(), si.getOrganizationId(), info);
    }

    @Test(expected = BESNotificationException.class)
    public void notifyAsyncSubscription_completion_OperationNotPermited()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new OperationNotPermittedException()).when(subServ)
                .completeAsyncSubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));
    }

    @Test(expected = BESNotificationException.class)
    public void notifyAsyncModifySubscription_completion_OperationNotPermited()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new OperationNotPermittedException()).when(subServ)
                .completeAsyncModifySubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), true,
                new APPlatformException(""));
    }

    @Test(expected = BESNotificationException.class)
    public void notifyAsyncUpgradeSubscription_completion_OperationNotPermited()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new OperationNotPermittedException()).when(subServ)
                .completeAsyncUpgradeSubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));
    }

    @Test(expected = BESNotificationException.class)
    public void notifyAsyncSubscription_abortion_OperationNotPermited()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new OperationNotPermittedException()).when(subServ)
                .abortAsyncSubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), false,
                new APPlatformException(""));
    }

    @Test(expected = BESNotificationException.class)
    public void notifyAsyncModifySubscription_abortion_OperationNotPermited()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new OperationNotPermittedException()).when(subServ)
                .abortAsyncModifySubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), false,
                new APPlatformException(""));
    }

    @Test(expected = BESNotificationException.class)
    public void notifyAsyncUpgradeSubscription_abortion_OperationNotPermited()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new OperationNotPermittedException()).when(subServ)
                .abortAsyncUpgradeSubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), false,
                new APPlatformException(""));
    }

    @Test
    public void notifyAsyncSubscription_completion_ObjectNotFound()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new ObjectNotFoundException()).when(subServ)
                .completeAsyncSubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncModifySubscription_completion_ObjectNotFound()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new ObjectNotFoundException()).when(subServ)
                .completeAsyncModifySubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncUpgradeSubscription_completion_ObjectNotFound()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new ObjectNotFoundException()).when(subServ)
                .completeAsyncUpgradeSubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncSubscription_abortion_ObjectNotFound()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new ObjectNotFoundException()).when(subServ)
                .abortAsyncSubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), false,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncModifySubscription_abortion_ObjectNotFound()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new ObjectNotFoundException()).when(subServ)
                .abortAsyncModifySubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), false,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncUpgradeSubscription_abortion_ObjectNotFound()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new ObjectNotFoundException()).when(subServ)
                .abortAsyncUpgradeSubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), false,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncSubscription_abortion_SubscriptionState()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new SubscriptionStateException()).when(subServ)
                .abortAsyncSubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));
        doNothing().when(besDAO).handleSubscriptionStateException(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(SubscriptionStateException.class));

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), false,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncModifySubscription_abortion_SubscriptionState()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new SubscriptionStateException()).when(subServ)
                .abortAsyncModifySubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));
        doNothing().when(besDAO).handleSubscriptionStateException(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(SubscriptionStateException.class));

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), false,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncUpgradeSubscription_abortion_SubscriptionState()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new SubscriptionStateException()).when(subServ)
                .abortAsyncUpgradeSubscription(anyString(), anyString(),
                        anyListOf(VOLocalizedText.class));
        doNothing().when(besDAO).handleSubscriptionStateException(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(SubscriptionStateException.class));

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), false,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncSubscription_completion_SubscriptionState()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new SubscriptionStateException()).when(subServ)
                .completeAsyncSubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));
        doNothing().when(besDAO).handleSubscriptionStateException(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(true), any(SubscriptionStateException.class));

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncModifySubscription_completion_SubscriptionState()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new SubscriptionStateException()).when(subServ)
                .completeAsyncModifySubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));
        doNothing().when(besDAO).handleSubscriptionStateException(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(true), any(SubscriptionStateException.class));

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncUpgradeSubscription_completion_SubscriptionState()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doReturn(new VOInstanceInfo()).when(besDAO).getInstanceInfo(
                any(ServiceInstance.class), any(InstanceResult.class));
        ServiceInstance si = givenServiceInstance(false);
        doThrow(new SubscriptionStateException()).when(subServ)
                .completeAsyncUpgradeSubscription(anyString(), anyString(),
                        any(VOInstanceInfo.class));
        doNothing().when(besDAO).handleSubscriptionStateException(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(true), any(SubscriptionStateException.class));

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), true,
                new APPlatformException(""));

        // then no exception
    }

    @Test
    public void notifyAsyncSubscription_abortion() throws APPlatformException,
            BESNotificationException, ObjectNotFoundException,
            SubscriptionStateException, OrganizationAuthoritiesException,
            OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        VOInstanceInfo info = new VOInstanceInfo();
        doReturn(info).when(besDAO).getInstanceInfo(any(ServiceInstance.class),
                any(InstanceResult.class));
        List<VOLocalizedText> besText = new ArrayList<>();
        besText.add(new VOLocalizedText("de", "text"));
        doReturn(besText).when(besDAO).toBES(anyListOf(LocalizedText.class));
        ServiceInstance si = givenServiceInstance(false);
        APPlatformException cause = new APPlatformException("");

        // when
        besDAO.notifyAsyncSubscription(si, new InstanceResult(), false, cause);

        // then
        verify(subServ).abortAsyncSubscription(eq(si.getSubscriptionId()),
                eq(si.getOrganizationId()), eq(besText));
    }

    @Test
    public void notifyAsyncModifySubscription_abortion()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        VOInstanceInfo info = new VOInstanceInfo();
        doReturn(info).when(besDAO).getInstanceInfo(any(ServiceInstance.class),
                any(InstanceResult.class));
        List<VOLocalizedText> besText = new ArrayList<>();
        besText.add(new VOLocalizedText("de", "text"));
        doReturn(besText).when(besDAO).toBES(anyListOf(LocalizedText.class));
        ServiceInstance si = givenServiceInstance(false);
        APPlatformException cause = new APPlatformException("");

        // when
        besDAO.notifyAsyncModifySubscription(si, new InstanceResult(), false,
                cause);

        // then
        verify(subServ).abortAsyncModifySubscription(
                eq(si.getSubscriptionId()), eq(si.getOrganizationId()),
                eq(besText));
    }

    @Test
    public void notifyAsyncUpgradeSubscription_abortion()
            throws APPlatformException, BESNotificationException,
            ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        VOInstanceInfo info = new VOInstanceInfo();
        doReturn(info).when(besDAO).getInstanceInfo(any(ServiceInstance.class),
                any(InstanceResult.class));
        List<VOLocalizedText> besText = new ArrayList<>();
        besText.add(new VOLocalizedText("de", "text"));
        doReturn(besText).when(besDAO).toBES(anyListOf(LocalizedText.class));
        ServiceInstance si = givenServiceInstance(false);
        APPlatformException cause = new APPlatformException("");

        // when
        besDAO.notifyAsyncUpgradeSubscription(si, new InstanceResult(), false,
                cause);

        // then
        verify(subServ).abortAsyncUpgradeSubscription(
                eq(si.getSubscriptionId()), eq(si.getOrganizationId()),
                eq(besText));
    }

    private ServiceInstance givenServiceInstance(boolean isDeleted) {
        ServiceInstance si = new ServiceInstance();
        if (isDeleted) {
            si.setSubscriptionId("subId#123");
        } else {
            si.setSubscriptionId("subId");
        }
        si.setOrganizationId("orgId");
        return si;
    }

    @Test(expected = BESNotificationException.class)
    public void notifyAsyncSubscription_APPlatformException()
            throws APPlatformException, BESNotificationException {
        // given
        doThrow(new APPlatformException(Arrays.asList(new LocalizedText())))
                .when(besDAO).getBESWebService(eq(IdentityService.class),
                        any(ServiceInstance.class), any(Optional.class));

        // when
        besDAO.notifyAsyncSubscription(givenServiceInstance(false),
                new InstanceResult(), true, new APPlatformException(""));
    }

    @Test
    public void getEnglishOrFirst_null() {
        assertNull(BesDAO.getEnglishOrFirst(null));
    }

    @Test
    public void getEnglishOrFirst_emptyList() {
        assertNull(BesDAO.getEnglishOrFirst(new ArrayList<LocalizedText>()));
    }

    @Test
    public void getEnglishOrFirst_englishFisrt() {
        // when
        String result = BesDAO.getEnglishOrFirst(Arrays.asList(
                givenText("en", "en_text"), givenText("de", "de_text")));

        // then
        assertEquals("en_text", result);
    }

    @Test
    public void getEnglishOrFirst_englishSecond() {
        // when
        String result = BesDAO.getEnglishOrFirst(Arrays.asList(
                givenText("de", "de_text"), givenText("en", "en_text")));

        // then
        assertEquals("en_text", result);
    }

    @Test
    public void getEnglishOrFirst_noEnglish() {
        // when
        String result = BesDAO.getEnglishOrFirst(Arrays.asList(
                givenText("de", "de_text"), givenText("ja", "ja_text")));

        // then
        assertEquals("de_text", result);
    }

    @Test
    public void notifyOnProvisioningStatusUpdate_noInfo()
            throws BESNotificationException {
        // when
        besDAO.notifyOnProvisioningStatusUpdate(new ServiceInstance(), null);

        // then
        verifyZeroInteractions(subServ);
    }

    @Test
    public void notifyOnProvisioningStatusUpdate_info()
            throws BESNotificationException, ObjectNotFoundException,
            SubscriptionStateException, OrganizationAuthoritiesException,
            OperationNotPermittedException {
        // given
        List<LocalizedText> list = new ArrayList<>();
        list.add(new LocalizedText("de", "text"));
        doNothing().when(subServ).updateAsyncSubscriptionProgress(anyString(),
                anyString(), anyListOf(VOLocalizedText.class));

        // when
        besDAO.notifyOnProvisioningStatusUpdate(givenServiceInstance(false),
                list);

        // then
        verify(subServ).updateAsyncSubscriptionProgress(eq("subId"),
                eq("orgId"), anyListOf(VOLocalizedText.class));
    }

    @Test
    public void getClientForBESAdmin_INTERNAL() throws APPlatformException,
            MalformedURLException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL");
        BesDAO besDAO = mockWebServiceSetup(proxySettings, null);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                null, Optional.empty());

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_KEY, USER_PWD, proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESAdmin_INTERNAL_noUserPwd()
            throws ConfigurationException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL",
                true, true, false);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForAPPAdmin(proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESAdmin_INTERNAL_noUserKey()
            throws ConfigurationException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL",
                true, false, true);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForAPPAdmin(proxySettings);
    }

    @Test
    public void getClientForBESAdmin_SSO() throws APPlatformException,
            MalformedURLException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP");
        BesDAO besDAO = mockWebServiceSetup(proxySettings, null);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                null, Optional.empty());

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_ID, USER_PWD, proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESAdmin_SSO_noUserPwd()
            throws ConfigurationException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP",
                true, true, false);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForAPPAdmin(proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESAdmin_SSO_noUserId()
            throws ConfigurationException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP",
                false, true, true);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForAPPAdmin(proxySettings);
    }

    @Test
    public void getClientForBESTechnologyManager_INTERNAL_userInConfig()
            throws MalformedURLException, APPlatformException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL");
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                true, true);
        BesDAO besDAO = mockWebServiceSetup(proxySettings, controllerSettings);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                new ServiceInstance(), Optional.empty());

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_KEY_TM, USER_PWD_TM,
                proxySettings);
    }

    @Test
    public void getClientForBESTechnologyManager_INTERNAL_userKeyNotInConfig_userInTS()
            throws MalformedURLException, BadResultException,
            APPlatformException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL");
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                false, true);
        BesDAO besDAO = mockWebServiceSetup(proxySettings, controllerSettings);
        ServiceInstance si = getServiceInstanceWithParameters(true, true);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                si, Optional.empty());

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_TM_TechSvc, USER_PWD_TM_TechSvc,
                proxySettings);
    }

    @Test
    public void getClientForBESTechnologyManager_INTERNAL_userPwdNotInConfig_userInTS()
            throws MalformedURLException, BadResultException,
            APPlatformException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL");
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                true, false);
        BesDAO besDAO = mockWebServiceSetup(proxySettings, controllerSettings);
        ServiceInstance si = getServiceInstanceWithParameters(true, true);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                si, Optional.empty());

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_TM_TechSvc, USER_PWD_TM_TechSvc,
                proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESTechnologyManager_INTERNAL_userKeyNotInConfig_userNotInTS()
            throws ConfigurationException, BadResultException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL");
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                false, true);
        doReturn(controllerSettings).when(confServ)
                .getControllerConfigurationSettings(anyString());
        ServiceInstance si = getServiceInstanceWithParameters(false, true);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForBESTechnologyManager(null, si,
                        proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESTechnologyManager_INTERNAL_userKeyNotInConfig_pwdNotInTS()
            throws ConfigurationException, BadResultException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL");
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                false, true);
        doReturn(controllerSettings).when(confServ)
                .getControllerConfigurationSettings(anyString());
        ServiceInstance si = getServiceInstanceWithParameters(true, false);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForBESTechnologyManager(null, si,
                        proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getAuthenticationForBESTechnologyManager_INTERNAL_nothingInConfig()
            throws ConfigurationException, BadResultException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("INTERNAL");
        final HashMap<String, Setting> controllerSettings = getControllerSettings(
                false, false, false);

        // when
        APPConfigurationServiceBean configurationServiceBean = new APPConfigurationServiceBean() {
            @Override
            public HashMap<String, Setting> getControllerConfigurationSettings(
                    String controllerId) throws ConfigurationException {
                return controllerSettings;
            }
        };
        configurationServiceBean.getAuthenticationForBESTechnologyManager(
                "ess.sample", null, proxySettings);
    }

    @Test
    public void getClientForBESTechnologyManager_SSO_userInConfig()
            throws MalformedURLException, APPlatformException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP");
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                true, true);
        BesDAO besDAO = mockWebServiceSetup(proxySettings, controllerSettings);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                new ServiceInstance(), Optional.empty());

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_ID_TM, USER_PWD_TM,
                proxySettings);
    }

    @Test
    public void getClientForBESTechnologyManager_SSO_userIdNotInConfig_userInTS()
            throws MalformedURLException, BadResultException,
            APPlatformException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP");
        Map<String, Setting> controllerSettings = getControllerSettings(false,
                true, true);
        BesDAO besDAO = mockWebServiceSetup(proxySettings, controllerSettings);
        ServiceInstance si = getServiceInstanceWithParameters(true, true);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                si, Optional.empty());

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_TM_TechSvc, USER_PWD_TM_TechSvc,
                proxySettings);
    }

    @Test
    public void getClientForBESTechnologyManager_SSO_userPwdNotInConfig_userInTS()
            throws MalformedURLException, BadResultException,
            APPlatformException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP");
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                true, false);
        BesDAO besDAO = mockWebServiceSetup(proxySettings, controllerSettings);
        ServiceInstance si = getServiceInstanceWithParameters(true, true);

        // when
        IdentityService client = besDAO.getBESWebService(IdentityService.class,
                si, Optional.of(""));

        // then
        verify(besDAO, times(1)).setUserCredentialsInContext(
                (BindingProvider) client, USER_TM_TechSvc, USER_PWD_TM_TechSvc,
                proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESTechnologyManager_SSO_userIdNotInConfig_userNotInTS()
            throws ConfigurationException, BadResultException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP");
        Map<String, Setting> controllerSettings = getControllerSettings(false,
                true, true);
        doReturn(controllerSettings).when(confServ)
                .getControllerConfigurationSettings(anyString());
        ServiceInstance si = getServiceInstanceWithParameters(false, true);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForBESTechnologyManager(null, si,
                        proxySettings);
    }

    @Test(expected = ConfigurationException.class)
    public void getClientForBESTechnologyManager_SSO_userKeyNotInConfig_pwdNotInTS()
            throws ConfigurationException, BadResultException {
        // given
        Map<String, Setting> proxySettings = getSettingsForMode("SAML_SP");
        Map<String, Setting> controllerSettings = getControllerSettings(false,
                true, true);
        doReturn(controllerSettings).when(confServ)
                .getControllerConfigurationSettings(anyString());
        ServiceInstance si = getServiceInstanceWithParameters(true, false);

        // when
        new APPConfigurationServiceBean()
                .getAuthenticationForBESTechnologyManager(null, si,
                        proxySettings);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getBESWebService_nullInstance() throws MalformedURLException,
            APPlatformException {
        // given
        BesDAO besDAO = spy(new BesDAO());
        besDAO.configService = spy(new APPConfigurationServiceBean());
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");
        doReturn(settings).when(besDAO.configService)
                .getAllProxyConfigurationSettings();
        Service serviceMock = Mockito.mock(Service.class);
        doReturn(serviceMock).when(besDAO).createWebService(any(URL.class),
                any(QName.class));
        doReturn(idServ).when(besDAO).getServicePort(eq(IdentityService.class),
                anyMap());
        doReturn(new PasswordAuthentication("user", "password")).when(
                besDAO.configService).getAuthenticationForAPPAdmin(anyMap());

        // when
        besDAO.getBESWebService(IdentityService.class, null, Optional.empty());

        // then
        verify(besDAO.configService, times(1)).getAuthenticationForAPPAdmin(
                anyMap());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getBESWebService_withInstance() throws MalformedURLException,
            APPlatformException {
        // given
        BesDAO besDAO = spy(new BesDAO());
        besDAO.configService = spy(new APPConfigurationServiceBean());
        Map<String, Setting> settings = getSettingsForMode("INTERNAL");
        doReturn(settings).when(besDAO.configService)
                .getAllProxyConfigurationSettings();
        Map<String, Setting> controllerSettings = getControllerSettings(true,
                true, true);
        doReturn(controllerSettings).when(besDAO.configService)
                .getControllerConfigurationSettings(anyString());
        doReturn(mock(Service.class)).when(besDAO).createWebService(
                any(URL.class), any(QName.class));
        doReturn(idServ).when(besDAO).getServicePort(eq(IdentityService.class),
                anyMap());
        doReturn(new PasswordAuthentication("user", "password")).when(
                besDAO.configService).getAuthenticationForAPPAdmin(anyMap());
        ServiceInstance si = new ServiceInstance();

        // when
        besDAO.getBESWebService(IdentityService.class, si, Optional.empty());

        // then
        verify(besDAO.configService, times(1))
                .getAuthenticationForBESTechnologyManager(anyString(), eq(si),
                        anyMap());
    }

    @Test
    public void getWebServiceAuthentication_withoutProxySettings()
            throws ConfigurationException {
        // given
        BesDAO besDAO = spy(new BesDAO());
        besDAO.configService = spy(new APPConfigurationServiceBean());
        doReturn(getSettingsForMode("INTERNAL")).when(besDAO.configService)
                .getAllProxyConfigurationSettings();

        // when
        besDAO.configService.getWebServiceAuthentication(null, null, Optional.empty());

        // then
        verify(besDAO.configService).getAllProxyConfigurationSettings();

    }

    @SuppressWarnings("unchecked")
    @Test(expected = ConfigurationException.class)
    public void getBESWebService_malformedURL() throws MalformedURLException,
            APPlatformException {
        // given
        BesDAO besDAO = spy(new BesDAO());
        besDAO.configService = mock(APPConfigurationServiceBean.class);
        doReturn(new HashMap<>()).when(besDAO.configService)
                .getAllProxyConfigurationSettings();
        doThrow(new MalformedURLException()).when(besDAO).getServicePort(
                eq(IdentityService.class), anyMap());

        // when
        besDAO.getBESWebService(IdentityService.class, null, Optional.empty());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = APPlatformException.class)
    public void getBESWebService_configExceprion() throws APPlatformException {
        // given
        BesDAO besDAO = spy(new BesDAO());
        besDAO.configService = mock(APPConfigurationServiceBean.class);
        doReturn(new HashMap<>()).when(besDAO.configService)
                .getAllProxyConfigurationSettings();
        doThrow(new ConfigurationException("")).when(besDAO.configService)
                .getAuthenticationForAPPAdmin(anyMap());

        // when
        besDAO.getBESWebService(IdentityService.class, null, Optional.empty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notifyOnProvisioningStatusUpdate_deletedInstance()
            throws BESNotificationException, APPlatformException {

        List<LocalizedText> list = new ArrayList<>();
        list.add(new LocalizedText("de", "text"));

        // when
        besDAO.notifyOnProvisioningStatusUpdate(givenServiceInstance(true),
                list);

        // then
        verify(besDAO, times(0)).getBESWebService(any(Class.class),
                any(ServiceInstance.class), any(Optional.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notifyAsyncSubscription_deletedInstance()
            throws BESNotificationException, APPlatformException {
        // when
        besDAO.notifyAsyncSubscription(givenServiceInstance(true),
                new InstanceResult(), true, new APPlatformException(""));

        // then
        verify(besDAO, times(0)).getBESWebService(any(Class.class),
                any(ServiceInstance.class), any(Optional.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notifyAsyncModifySubscription_deletedInstance()
            throws BESNotificationException, APPlatformException {
        // when
        besDAO.notifyAsyncModifySubscription(givenServiceInstance(true),
                new InstanceResult(), true, new APPlatformException(""));

        // then
        verify(besDAO, times(0)).getBESWebService(any(Class.class),
                any(ServiceInstance.class), any(Optional.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void notifyAsyncUpgradeSubscription_deletedInstance()
            throws BESNotificationException, APPlatformException {
        // when
        besDAO.notifyAsyncUpgradeSubscription(givenServiceInstance(true),
                new InstanceResult(), true, new APPlatformException(""));

        // then
        verify(besDAO, times(0)).getBESWebService(any(Class.class),
                any(ServiceInstance.class), any(Optional.class));
    }

    @Test
    public void isBESAvalible_false() throws Exception {
        // given
        doThrow(new APPlatformException("", new ConnectException())).when(
                besDAO).getBESWebService(eq(IdentityService.class),
                any(ServiceInstance.class), any(Optional.class));
        // when
        boolean result = besDAO.isBESAvalible();

        // then
        assertFalse(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void isBESAvalible_true() throws Exception {
        // when
        boolean result = besDAO.isBESAvalible();

        // then
        verify(besDAO, times(1)).getBESWebService(any(Class.class),
                any(ServiceInstance.class), any(Optional.class));
        assertTrue(result);
    }

    @Test
    public void terminateSubscription_successful() throws APPlatformException,
            ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthoritiesException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            ConcurrentModificationException, SubscriptionStateException,
            BESNotificationException {
        // given
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));

        ServiceInstance si = givenServiceInstance(false);
        String locale = "en";

        // when
        besDAO.terminateSubscription(si, locale);

        // then
        verify(subServ, times(1)).getSubscriptionForCustomer(
                si.getOrganizationId(), si.getSubscriptionId());
        verify(subServ, times(1)).terminateSubscription(
                any(VOSubscription.class), anyString());

    }

    @Test
    public void terminateSubscription_NoService_unsuccessful()
            throws APPlatformException, ObjectNotFoundException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ConcurrentModificationException {
        // given
        doThrow(new APPlatformException("")).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));

        ServiceInstance si = givenServiceInstance(false);
        String locale = "en";

        // when
        try {
            besDAO.terminateSubscription(si, locale);
        } catch (Exception e) {
            // do Nothing
        }
        // then
        verify(subServ, times(0)).getSubscriptionForCustomer(
                si.getOrganizationId(), si.getSubscriptionId());
        verify(subServ, times(0)).terminateSubscription(
                any(VOSubscription.class), anyString());

    }

    @Test
    public void terminateSubscription_NoSubscription_unsuccessful()
            throws APPlatformException, ObjectNotFoundException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ConcurrentModificationException {
        // given
        ServiceInstance si = givenServiceInstance(false);
        String locale = "en";
        doReturn(subServ).when(besDAO).getBESWebService(
                eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doThrow(new ObjectNotFoundException("")).when(subServ)
                .getSubscriptionForCustomer(eq(si.getOrganizationId()),
                        eq(si.getSubscriptionId()));

        // when
        try {
            besDAO.terminateSubscription(si, locale);
        } catch (Exception e) {
            // do Nothing
        }
        // then
        verify(subServ, times(1)).getSubscriptionForCustomer(
                si.getOrganizationId(), si.getSubscriptionId());
        verify(subServ, times(0)).terminateSubscription(
                any(VOSubscription.class), anyString());

    }

    @Test
    public void testNotifySubscriptionAboutVmsNumber()
        throws BESNotificationException, APPlatformException, ObjectNotFoundException, OperationNotPermittedException {
        //given
        ServiceInstance si = givenServiceInstance(false);
        doReturn(subServ).when(besDAO).getBESWebService(
            eq(SubscriptionService.class), any(ServiceInstance.class), any(Optional.class));
        doNothing().when(subServ).notifySubscriptionAboutVmsNumber(anyString(), anyString(), any(VOInstanceInfo.class));
        //when
        besDAO.notifySubscriptionAboutVmsNumber(si);

        //then
        verify(subServ, times(1)).notifySubscriptionAboutVmsNumber(anyString(), anyString(), any(VOInstanceInfo.class));
    }

    private LocalizedText givenText(String locale, String text) {
        LocalizedText voText = new LocalizedText();
        voText.setLocale(locale);
        voText.setText(text);
        return voText;
    }

    private VOUserDetails givenUser(UserRoleType role, String mail) {
        VOUserDetails user = new VOUserDetails();
        user.setEMail(mail);
        if (role != null) {
            user.addUserRole(role);
        }
        user.setKey(10L);
        user.setUserId("userId");
        return user;

    }

    @SuppressWarnings("unchecked")
    private BesDAO mockWebServiceSetup(
            final Map<String, Setting> proxySettings,
            final Map<String, Setting> controllerSettings)
            throws APPlatformException, MalformedURLException {
        BesDAO besDAO = spy(new BesDAO());
        besDAO.configService = spy(new APPConfigurationServiceBean());
        doReturn(proxySettings).when(besDAO.configService)
                .getAllProxyConfigurationSettings();
        if (controllerSettings != null) {
            doReturn(controllerSettings).when(besDAO.configService)
                    .getControllerConfigurationSettings(anyString());
        }
        doReturn(mock(Service.class)).when(besDAO).createWebService(
                any(URL.class), any(QName.class));
        doReturn(idServ).when(besDAO).getServicePort(eq(IdentityService.class),
                anyMap());
        doReturn(subServ).when(besDAO).getServicePort(
                eq(SubscriptionService.class), anyMap());
        return besDAO;
    }

    private Map<String, Setting> getSettingsForMode(String mode) {
        return getSettingsForMode(mode, true, true, true);
    }

    private Map<String, Setting> getSettingsForMode(String mode,
            boolean userId, boolean userKey, boolean userPwd) {
        Map<String, Setting> settings = new HashMap<>();
        if ("SAML_SP".equals(mode)) {
            settings.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(),
                    new Setting(PlatformConfigurationKey.BSS_AUTH_MODE.name(),
                            "SAML_SP"));
        } else {
            settings.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(),
                    new Setting(PlatformConfigurationKey.BSS_AUTH_MODE.name(),
                            "INTERNAL"));
        }
        if (userId) {
            settings.put(PlatformConfigurationKey.BSS_USER_ID.name(),
                    new Setting(PlatformConfigurationKey.BSS_USER_ID.name(),
                            USER_ID));
        }
        if (userKey) {
            settings.put(PlatformConfigurationKey.BSS_USER_KEY.name(),
                    new Setting(PlatformConfigurationKey.BSS_USER_KEY.name(),
                            USER_KEY));
        }
        if (userPwd) {
            settings.put(PlatformConfigurationKey.BSS_USER_PWD.name(),
                    new Setting(PlatformConfigurationKey.BSS_USER_PWD.name(),
                            USER_PWD));
        }
        settings.put(PlatformConfigurationKey.BSS_WEBSERVICE_URL.name(),
                new Setting(PlatformConfigurationKey.BSS_WEBSERVICE_URL.name(),
                        "https://localhost:8181/{SERVICE}/BASIC?wsdl"));
        settings.put(
                PlatformConfigurationKey.BSS_STS_WEBSERVICE_URL.name(),
                new Setting(PlatformConfigurationKey.BSS_STS_WEBSERVICE_URL
                        .name(), "https://localhost:8181/{SERVICE}/STS?wsdl"));

        settings.put(
                PlatformConfigurationKey.BSS_WEBSERVICE_WSDL_URL.name(),
                new Setting(
                        PlatformConfigurationKey.BSS_STS_WEBSERVICE_WSDL_URL
                                .name(),
                        "https://localhost:8181/oscm/v1.9/{SERVICE}/BASIC?wsdl"));
        settings.put(PlatformConfigurationKey.BSS_STS_WEBSERVICE_WSDL_URL
                .name(), new Setting(
                PlatformConfigurationKey.BSS_STS_WEBSERVICE_WSDL_URL.name(),
                "https://localhost:8181/oscm/v1.9/{SERVICE}/STS?wsdl"));
        return settings;
    }

    private HashMap<String, Setting> getControllerSettings(boolean userId,
            boolean userKey, boolean userPwd) {
        HashMap<String, Setting> settings = new HashMap<>();
        if (userId) {
            settings.put(ControllerConfigurationKey.BSS_USER_ID.name(),
                    new Setting(ControllerConfigurationKey.BSS_USER_ID.name(),
                            USER_ID_TM));
        }
        if (userKey) {
            settings.put(ControllerConfigurationKey.BSS_USER_KEY.name(),
                    new Setting(ControllerConfigurationKey.BSS_USER_KEY.name(),
                            USER_KEY_TM));
        }
        if (userPwd) {
            settings.put(ControllerConfigurationKey.BSS_USER_PWD.name(),
                    new Setting(ControllerConfigurationKey.BSS_USER_PWD.name(),
                            USER_PWD_TM));
        }
        return settings;
    }

    private ServiceInstance getServiceInstanceWithParameters(boolean user,
            boolean userPwd) throws BadResultException {
        ServiceInstance si = new ServiceInstance();
        List<InstanceParameter> list = new ArrayList<>();
        if (user) {
            InstanceParameter p = spy(new InstanceParameter());
            p.setParameterKey(InstanceParameter.BSS_USER);
            p.setParameterValue(USER_ID_TM);
            list.add(p);
            doReturn(USER_TM_TechSvc).when(p).getDecryptedValue();
        }
        if (userPwd) {
            InstanceParameter p = spy(new InstanceParameter());
            p.setParameterKey(InstanceParameter.BSS_USER_PWD);
            p.setParameterValue(USER_PWD_TM);
            list.add(p);
            doReturn(USER_PWD_TM_TechSvc).when(p).getDecryptedValue();
        }
        si.setInstanceParameters(list);

        return si;
    }

    private InstanceResult givenInstanceResult(int returnCode) {
        InstanceResult instanceResult = new InstanceResult();
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAccessInfo("TSReturnAccessInfo");
        instanceInfo.setBaseUrl("TS_RETURN_BASE_URL");
        instanceInfo.setInstanceId("TSReturnInstanceId");
        instanceInfo.setLoginPath("TSReturnLoginPath");
        instanceResult.setInstance(instanceInfo);
        instanceResult.setRc(returnCode);
        return instanceResult;
    }
}
