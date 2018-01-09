/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.ejb.Timer;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.app.business.AsynchronousProvisioningProxyImpl;
import org.oscm.app.business.ProductProvisioningServiceFactoryBean;
import org.oscm.app.business.ProvisioningResults;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.data.InstanceDescription;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.InstanceStatusUsers;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.service.APPAuthenticationServiceBean;
import org.oscm.app.v2_0.service.APPCommunicationServiceBean;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.app.v2_0.service.AsynchronousProvisioningProxy;
import org.oscm.app.v2_0.service.OperationServiceBean;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.data.UserResult;
import org.oscm.provisioning.intf.ProvisioningService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link AsynchronousProvisioningProxy}.
 * 
 * @author hoffmann
 */
public class AsynchronousProvisioningProxyIT extends EJBTestBase {

    private AsynchronousProvisioningProxy proxy;
    private EntityManager em;
    private ProductProvisioningServiceFactoryBean provisioningFactory;
    private APPlatformController controllerMock;
    private APPConfigurationServiceBean configService;
    private APPTimerServiceBean timerService;
    private APPTimerServiceBean timerService1;
    private APPAuthenticationServiceBean authService;
    private InstanceRequest basicInstanceRequest;
    private ProvisioningService provServiceMock;
    private ServiceInstanceDAO instanceDAO;
    private final String organizationId = "org123";
    private final String subscriptionId = "sub123";

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.addBean(LoggerFactory
                .getLogger(AsynchronousProvisioningProxy.class));
        container.addBean(provisioningFactory = Mockito
                .mock(ProductProvisioningServiceFactoryBean.class));

        provServiceMock = mock(ProvisioningService.class);
        doReturn(provServiceMock).when(provisioningFactory).getInstance(
                any(ServiceInstance.class));
        container.addBean(configService = Mockito
                .mock(APPConfigurationServiceBean.class));

        container.addBean(mock(APPCommunicationServiceBean.class));
        container.addBean(mock(BesDAO.class));
        container.addBean(mock(OperationDAO.class));
        container.addBean(instanceDAO = new ServiceInstanceDAO());
        em = instanceDAO.em;
        container.addBean(mock(OperationServiceBean.class));
        container.addBean(authService = Mockito
                .mock(APPAuthenticationServiceBean.class));
        container.addBean(timerService = Mockito
                .mock(APPTimerServiceBean.class));
        container.addBean(timerService1 = Mockito
                .mock(APPTimerServiceBean.class));

        timerService.em = em;
        timerService1.em = em;
        timerService.instanceDAO = instanceDAO;
        timerService1.instanceDAO = instanceDAO;
        timerService.configService = configService;
        timerService1.configService = configService;
        timerService.appTimerServiceBean = timerService1;

        doThrow(new AuthenticationException("NoAuth")).when(authService)
                .authenticateTMForInstance(anyString(), anyString(),
                        any(PasswordAuthentication.class));
        doThrow(new AuthenticationException("NoAuth")).when(authService)
                .authenticateTMForController(anyString(),
                        any(PasswordAuthentication.class));
        doThrow(new AuthenticationException("NoAuth")).when(authService)
                .authenticateAdministrator(any(PasswordAuthentication.class));
        doNothing().when(timerService).raiseEvent(anyString(), anyString(),
                any(Properties.class));
        doNothing().when(timerService).handleTimer(any(Timer.class));

        container.addBean(new ProvisioningResults());
        AsynchronousProvisioningProxyImpl bean = new AsynchronousProvisioningProxyImpl();
        container.addBean(bean);
        container.addBean(proxy = Mockito
                .spy(new AsynchronousProvisioningProxy()));
        proxy.em = em;
        proxy.timerService = timerService;
        proxy.configService = configService;
        proxy.instanceDAO = instanceDAO;
        proxy.provisioningFactory = provisioningFactory;
        proxy.appImpl = bean;

        InitialContext context = new InitialContext();
        controllerMock = mock(APPlatformController.class);
        context.bind(APPlatformController.JNDI_PREFIX + "test.controller",
                controllerMock);

        // create instance request with test controller configured
        final ServiceParameter p = new ServiceParameter();
        p.setParameterId(InstanceParameter.CONTROLLER_ID);
        p.setValue("test.controller");

        final ServiceAttribute a = new ServiceAttribute();
        a.setAttributeId("attrId");
        a.setValue("value");

        basicInstanceRequest = new InstanceRequest();
        basicInstanceRequest
                .setParameterValue(new ArrayList<ServiceParameter>());
        basicInstanceRequest
                .setAttributeValue(new ArrayList<ServiceAttribute>());
        basicInstanceRequest.getParameterValue().add(p);
        basicInstanceRequest.getAttributeValue().add(a);
        basicInstanceRequest.setDefaultLocale("en");
        basicInstanceRequest.setReferenceId("referenceId");

        final HashMap<String, Setting> controllerSetting = new HashMap<>();
        controllerSetting.put("BSS_ORGANIZATION_ID", new Setting(
                "BSS_ORGANIZATION_ID", "testorg"));
        when(
                configService
                        .getControllerConfigurationSettings("test.controller"))
                .thenReturn(controllerSetting);
        Answer<ProvisioningSettings> answer = new Answer<ProvisioningSettings>() {
            @Override
            public ProvisioningSettings answer(InvocationOnMock invocation)
                    throws Throwable {
                Object[] args = invocation.getArguments();
                ServiceInstance instance = (ServiceInstance) args[0];
                return new ProvisioningSettings(instance.getParameterMap(),
                        instance.getAttributeMap(),
                        new HashMap<String, Setting>(), controllerSetting, "en");
            }
        };
        doAnswer(answer).when(configService).getProvisioningSettings(
                any(ServiceInstance.class), any(ServiceUser.class));
    }

    @Test
    public void testAsyncCreateInstanceAppException() throws Exception {

        when(
                controllerMock.createInstance(Matchers
                        .any(ProvisioningSettings.class))).thenThrow(
                new APPlatformException("APP Fault"));

        final BaseResult result = runTX(new Callable<BaseResult>() {

            @Override
            public BaseResult call() {
                return proxy.asyncCreateInstance(basicInstanceRequest, null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals("APP Fault", result.getDesc());
    }

    @Ignore
    @Test
    public void testAsyncCreateTimerInitFailure() throws Exception {
        final InstanceDescription descr = new InstanceDescription();
        descr.setInstanceId("appId123");
        descr.setBaseUrl("http://here/");
        when(
                controllerMock.createInstance(Matchers
                        .any(ProvisioningSettings.class))).thenReturn(descr);

        doThrow(new APPlatformException("TimerInitFailure")).when(timerService)
                .initTimers();
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                final InstanceRequest rq = basicInstanceRequest;
                rq.setOrganizationId("org123");
                rq.setSubscriptionId("sub123");
                rq.setDefaultLocale("de");
                rq.setLoginUrl("http://bes/");
                return proxy.asyncCreateInstance(rq, null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals("TimerInitFailure", result.getDesc());
    }

    @Test
    public void testAsyncCreateInstance_noControllerSet() {
        final InstanceRequest rq = new InstanceRequest();
        rq.setOrganizationId("org123");
        rq.setOrganizationName("Fujitsu");
        rq.setSubscriptionId("sub123");
        rq.setDefaultLocale("de");
        rq.setLoginUrl("http://bes/");
        final ServiceParameter param = new ServiceParameter();
        param.setParameterId("appParam1");
        param.setValue("appValue1");
        rq.setParameterValue(new ArrayList<ServiceParameter>());
        rq.getParameterValue().add(param);
        BaseResult result = proxy.asyncCreateInstance(rq, null);
        assertFalse(result.getRc() == 0);
    }

    @Test
    public void testAsyncCreateInstance_noParameters() {
        final InstanceRequest rq = new InstanceRequest();
        rq.setOrganizationId("org123");
        rq.setOrganizationName("Fujitsu");
        rq.setSubscriptionId("sub123");
        rq.setDefaultLocale("de");
        rq.setLoginUrl("http://bes/");
        BaseResult result = proxy.asyncCreateInstance(rq, null);
        assertFalse(result.getRc() == 0);
    }

    @Test
    public void testAsyncCreateInstance() throws Exception {

        doNothing().when(timerService).initTimers();
        final InstanceRequest rq = basicInstanceRequest;
        rq.setOrganizationId("org123");
        rq.setOrganizationName("Fujitsu");
        rq.setSubscriptionId("sub123");
        rq.setDefaultLocale("de");
        rq.setLoginUrl("http://bes/");
        final ServiceParameter param = new ServiceParameter();
        param.setParameterId("appParam1");
        param.setValue("appValue1");
        rq.getParameterValue().add(param);
        final ServiceParameter param2 = new ServiceParameter();
        param2.setParameterId(null); // mappers must be able to handle this
        param2.setValue("nullKey");
        rq.getParameterValue().add(param2);

        final InstanceDescription descr = new InstanceDescription();
        descr.setInstanceId("appId123");
        descr.setBaseUrl("http://here/");
        HashMap<String, Setting> map = new HashMap<>();
        map.put(param.getParameterId(), new Setting(param.getParameterId(),
                param.getValue()));
        map.put(null, new Setting(null, "null")); // check error resistance
        descr.setChangedParameters(map);

        List<LocalizedText> msgs = Arrays.asList(new LocalizedText("en",
                "enMsg"), new LocalizedText("de", "deMsg"), new LocalizedText(
                "ja", "ja"));
        descr.setDescription(msgs);

        when(
                controllerMock.createInstance(Matchers
                        .any(ProvisioningSettings.class))).thenReturn(descr);

        doReturn("en").when(proxy).getLocale(any(User.class));

        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncCreateInstance(rq, null);
            }
        });

        assertEquals(0, result.getRc());
        assertEquals("enMsg", result.getDesc());

        final List<ServiceInstance> services = runTX(new Callable<List<ServiceInstance>>() {
            @Override
            public List<ServiceInstance> call() {
                final Query q = em
                        .createQuery("SELECT s FROM ServiceInstance s");
                @SuppressWarnings("unchecked")
                List<ServiceInstance> services = q.getResultList();
                ServiceInstance service = services.get(0);
                load(service.getInstanceParameters());
                return services;
            }
        });

        assertEquals(1, services.size());
        final ServiceInstance service = services.get(0);
        assertEquals("org123", service.getOrganizationId());
        assertEquals("Fujitsu", service.getOrganizationName());
        assertEquals("sub123", service.getSubscriptionId());
        assertEquals("de", service.getDefaultLocale());
        assertEquals("http://bes/", service.getBesLoginURL());
        assertEquals("appId123", service.getInstanceId());
        assertEquals("http://here/", service.getServiceBaseURL());
        assertTrue(service.getRequestTime() != 0);
        assertTrue(System.currentTimeMillis() >= service.getRequestTime());

        final List<InstanceParameter> params = service.getInstanceParameters();
        assertEquals(1, params.size());
        final InstanceParameter resultParam = params.get(0);
        assertEquals("appParam1", resultParam.getParameterKey());
        assertEquals("appValue1", resultParam.getParameterValue());
        assertEquals(service, resultParam.getServiceInstance());

        verify(timerService).initTimers();
        resultParam.setTkey(123);
        assertEquals(123, resultParam.getTkey());
    }

    @Test
    public void testAsyncCreateInstanceDuplicateId() throws Exception {
        // First we create a normal service
        testAsyncCreateInstance();

        // Now create another one with same instance id
        final InstanceRequest rq = basicInstanceRequest;
        rq.setOrganizationId("org123");
        rq.setOrganizationName("Fujitsu");
        rq.setSubscriptionId("sub123");
        rq.setDefaultLocale("de");
        rq.setLoginUrl("http://bes/");

        final InstanceDescription descr = new InstanceDescription();
        descr.setInstanceId("appId123");
        descr.setBaseUrl("http://here/");
        HashMap<String, Setting> map = new HashMap<>();
        descr.setChangedParameters(map);
        when(
                controllerMock.createInstance(Matchers
                        .any(ProvisioningSettings.class))).thenReturn(descr);

        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncCreateInstance(rq, null);
            }
        });

        // Error expected! Duplicate ID
        assertEquals(1, result.getRc());
        assertTrue(result.getDesc().contains("appId123"));

    }

    @Test
    public void testAsyncCreateInstanceEmptyId1() throws Exception {
        final InstanceRequest rq = basicInstanceRequest;
        rq.setOrganizationId("org123");
        rq.setOrganizationName("Fujitsu");
        rq.setSubscriptionId("sub123");
        rq.setDefaultLocale("de");
        rq.setLoginUrl("http://bes/");

        final InstanceDescription descr = new InstanceDescription();
        descr.setInstanceId("");
        descr.setBaseUrl("http://here/");
        HashMap<String, Setting> map = new HashMap<>();
        descr.setChangedParameters(map);
        when(
                controllerMock.createInstance(Matchers
                        .any(ProvisioningSettings.class))).thenReturn(descr);

        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncCreateInstance(rq, null);
            }
        });

        // Error expected! Empty ID
        assertEquals(1, result.getRc());
    }

    @Test
    public void testAsyncCreateInstanceEmptyId2() throws Exception {
        final InstanceRequest rq = basicInstanceRequest;
        rq.setOrganizationId("org123");
        rq.setOrganizationName("Fujitsu");
        rq.setSubscriptionId("sub123");
        rq.setDefaultLocale("de");
        rq.setLoginUrl("http://bes/");

        final InstanceDescription descr = new InstanceDescription();
        descr.setInstanceId(null);
        descr.setBaseUrl("http://here/");
        HashMap<String, Setting> map = new HashMap<>();
        descr.setChangedParameters(map);
        when(
                controllerMock.createInstance(Matchers
                        .any(ProvisioningSettings.class))).thenReturn(descr);

        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncCreateInstance(rq, null);
            }
        });

        // Error expected! Empty ID
        assertEquals(1, result.getRc());
    }

    @Test
    public void testPing() {
        assertEquals("Hello Proxy!", proxy.sendPing("Hello Proxy!"));
    }

    @Test
    public void testCreateInstance() {
        final InstanceResult result = proxy.createInstance(
                new InstanceRequest(), null);
        assertEquals(1, result.getRc());
        assertEquals(Messages.get("en", "error_synchronous_provisioning"),
                result.getDesc());
    }

    @Test
    public void testModifySubscription() {
        final BaseResult result = proxy.modifySubscription("instanceId",
                "subscriptionId", "referenceId",
                new ArrayList<ServiceParameter>(),
                new ArrayList<ServiceAttribute>(), null);
        assertEquals(1, result.getRc());
        assertEquals(Messages.get("en", "error_synchronous_provisioning"),
                result.getDesc());
    }

    @Test
    public void testUpgradeSubscription() {
        final BaseResult result = proxy.upgradeSubscription("instanceId",
                "subscriptionId", "referenceId",
                new ArrayList<ServiceParameter>(),
                new ArrayList<ServiceAttribute>(), null);
        assertEquals(1, result.getRc());
        assertEquals(Messages.get("en", "error_synchronous_provisioning"),
                result.getDesc());
    }

    @Test
    public void testCreateUsersInvalidInstanceId1() throws Exception {
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                final List<User> users = new ArrayList<>();
                return proxy.createUsers("unknown", users, null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals(
                Messages.get("en", "error_instance_not_exists", "unknown"),
                result.getDesc());
    }

    @Test
    public void testCreateUsersInvalidInstanceId2() throws Exception {
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                final List<User> users = new ArrayList<>();
                return proxy.createUsers("999999", users, null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals(Messages.get("en", "error_instance_not_exists", "999999"),
                result.getDesc());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateUsers() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final UserResult result = new UserResult();
        result.setDesc("Ok");

        doReturn(result).when(provServiceMock).createUsers(instanceId, users,
                null);

        InstanceStatusUsers mockStatus = new InstanceStatusUsers();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.createUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(mockStatus);

        UserResult ur = runTX(new Callable<UserResult>() {
            @Override
            public UserResult call() {
                return proxy.createUsers(instanceId, users, null);
            }
        });

        Assert.assertEquals(result.getDesc(), ur.getDesc());
        Assert.assertEquals(result.getRc(), ur.getRc());

        verify(provServiceMock).createUsers(instanceId, users, null);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateUsersWithUsers1() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setUserId("user1");
        User user2 = new User();
        user2.setUserId("user2");
        users.add(user1);
        users.add(user2);
        final UserResult result = new UserResult();
        result.setDesc("Ok");

        doReturn(result).when(provServiceMock).createUsers(instanceId, users,
                null);

        final List<ServiceUser> svcUsers = new ArrayList<>();
        ServiceUser svcUser1 = new ServiceUser();
        svcUser1.setUserId("user1");
        svcUser1.setApplicationUserId("user1app");
        ServiceUser svcUser2 = new ServiceUser();
        svcUser2.setUserId("user2");
        svcUser2.setApplicationUserId("user2app");
        svcUsers.add(svcUser1);
        svcUsers.add(svcUser2);

        InstanceStatusUsers mockStatus = new InstanceStatusUsers();
        mockStatus.setInstanceProvisioningRequired(true);
        mockStatus.setChangedUsers(svcUsers);
        when(
                controllerMock.createUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(mockStatus);

        UserResult ur = runTX(new Callable<UserResult>() {
            @Override
            public UserResult call() {
                return proxy.createUsers(instanceId, users, null);
            }
        });
        Assert.assertEquals(result.getDesc(), ur.getDesc());
        Assert.assertEquals(result.getRc(), ur.getRc());

        verify(provServiceMock).createUsers(instanceId, users, null);
        Assert.assertEquals(2, ur.getUsers().size());
        Assert.assertEquals("user1app", ur.getUsers().get(0)
                .getApplicationUserId());
        Assert.assertEquals("user2app", ur.getUsers().get(1)
                .getApplicationUserId());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateUsersWithUsers1Invalid() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setUserId("user1");
        User user2 = new User();
        user2.setUserId("user2");
        users.add(user1);
        users.add(user2);
        final UserResult result = new UserResult();
        result.setDesc("Ok");

        doReturn(result).when(provServiceMock).createUsers(instanceId, users,
                null);

        final List<ServiceUser> svcUsers = new ArrayList<>();
        ServiceUser svcUser1 = new ServiceUser();
        svcUser1.setUserId("user1");
        svcUser1.setApplicationUserId("user1app");
        ServiceUser svcUser2 = new ServiceUser();
        svcUser2.setUserId("user3"); // wrong user
        svcUser2.setApplicationUserId("user3app");
        svcUsers.add(svcUser1);
        svcUsers.add(svcUser2);

        InstanceStatusUsers mockStatus = new InstanceStatusUsers();
        mockStatus.setInstanceProvisioningRequired(true);
        mockStatus.setChangedUsers(svcUsers);
        when(
                controllerMock.createUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(mockStatus);

        UserResult ur = runTX(new Callable<UserResult>() {
            @Override
            public UserResult call() {
                return proxy.createUsers(instanceId, users, null);
            }
        });
        Assert.assertEquals(result.getDesc(), ur.getDesc());
        Assert.assertEquals(result.getRc(), ur.getRc());

        verify(provServiceMock).createUsers(instanceId, users, null);
        Assert.assertEquals(2, ur.getUsers().size());
        Assert.assertEquals("user1app", ur.getUsers().get(0)
                .getApplicationUserId());
        Assert.assertNull(ur.getUsers().get(1).getApplicationUserId());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateUsersWithUsers2() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setUserId("user1");
        User user2 = new User();
        user2.setUserId("user2");
        users.add(user1);
        users.add(user2);
        final UserResult result = new UserResult();
        result.setDesc("Ok");

        final List<User> svcUsers = new ArrayList<>();
        User svcUser1 = new User();
        svcUser1.setUserId("user1");
        svcUser1.setApplicationUserId("user1app");
        User svcUser2 = new User();
        svcUser2.setUserId("user2");
        svcUser2.setApplicationUserId("user2app");
        svcUsers.add(svcUser1);
        svcUsers.add(svcUser2);
        result.setUsers(svcUsers);

        doReturn(result).when(provServiceMock).createUsers(instanceId, users,
                null);

        InstanceStatusUsers mockStatus = new InstanceStatusUsers();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.createUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(mockStatus);

        UserResult ur = runTX(new Callable<UserResult>() {
            @Override
            public UserResult call() {
                return proxy.createUsers(instanceId, users, null);
            }
        });
        Assert.assertEquals(result.getDesc(), ur.getDesc());
        Assert.assertEquals(result.getRc(), ur.getRc());
        verify(provServiceMock).createUsers(instanceId, users, null);
        Assert.assertEquals(2, ur.getUsers().size());
        Assert.assertEquals("user1app", ur.getUsers().get(0)
                .getApplicationUserId());
        Assert.assertEquals("user2app", ur.getUsers().get(1)
                .getApplicationUserId());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateUsers_NoProvOnInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final UserResult result = new UserResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).createUsers(instanceId, users,
                null);

        when(
                controllerMock.createUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(new InstanceStatusUsers());

        UserResult ur = runTX(new Callable<UserResult>() {
            @Override
            public UserResult call() {
                return proxy.createUsers(instanceId, users, null);
            }
        });
        verifyZeroInteractions(provServiceMock);

        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, ur.getRc());
    }

    @Test
    public void testCreateUsers_NullStatus() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final UserResult result = new UserResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).createUsers(instanceId, users,
                null);

        UserResult ur = runTX(new Callable<UserResult>() {
            @Override
            public UserResult call() {
                return proxy.createUsers(instanceId, users, null);
            }
        });

        verifyZeroInteractions(provServiceMock);

        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, ur.getRc());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateUsers_NoProvOnInstanceWithUsers() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setUserId("user1");
        User user2 = new User();
        user2.setUserId("user2");
        users.add(user1);
        users.add(user2);

        final UserResult result = new UserResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).createUsers(instanceId, users,
                null);
        when(
                controllerMock.createUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(new InstanceStatusUsers());

        UserResult ur = runTX(new Callable<UserResult>() {
            @Override
            public UserResult call() {
                return proxy.createUsers(instanceId, users, null);
            }
        });

        verifyZeroInteractions(provServiceMock);

        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, ur.getRc());
        Assert.assertEquals(2, ur.getUsers().size());

    }

    @Test
    public void testUpdateUsersInvalidInstanceId() throws Exception {
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                final List<User> users = new ArrayList<>();
                return proxy.updateUsers("unknown", users, null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals(
                Messages.get("en", "error_instance_not_exists", "unknown"),
                result.getDesc());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateUsers() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final BaseResult result = new BaseResult();
        result.setDesc("Ok");

        doReturn(result).when(provServiceMock).updateUsers(instanceId, users,
                null);

        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.updateUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(mockStatus);

        BaseResult br = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.updateUsers(instanceId, users, null);
            }
        });
        Assert.assertEquals(result.getDesc(), br.getDesc());
        Assert.assertEquals(result.getRc(), br.getRc());
        verify(provServiceMock).updateUsers(instanceId, users, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateUsers_NoProvOnInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final BaseResult result = new BaseResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).updateUsers(instanceId, users,
                null);
        when(
                controllerMock.updateUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(new InstanceStatus());

        BaseResult ur = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.updateUsers(instanceId, users, null);
            }
        });
        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, ur.getRc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void testUpdateUsers_NullStatus() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final BaseResult result = new BaseResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).updateUsers(instanceId, users,
                null);
        BaseResult ur = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.updateUsers(instanceId, users, null);
            }
        });
        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, ur.getRc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void testDeleteUsersInvalidInstanceId() throws Exception {
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                final List<User> users = new ArrayList<>();
                return proxy.deleteUsers("unknown", users, null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals(
                Messages.get("en", "error_instance_not_exists", "unknown"),
                result.getDesc());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteUsers() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final BaseResult result = new BaseResult();
        result.setDesc("Ok");

        doReturn(result).when(provServiceMock).deleteUsers(instanceId, users,
                null);

        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.deleteUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(mockStatus);

        BaseResult br = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteUsers(instanceId, users, null);
            }
        });
        Assert.assertEquals(result.getDesc(), br.getDesc());
        Assert.assertEquals(result.getRc(), br.getRc());
        verify(provServiceMock).deleteUsers(instanceId, users, null);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteUsers_NoProvOnInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final BaseResult result = new BaseResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).deleteUsers(instanceId, users,
                null);
        when(
                controllerMock.deleteUsers(anyString(),
                        any(ProvisioningSettings.class), any(List.class)))
                .thenReturn(new InstanceStatus());

        BaseResult ur = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteUsers(instanceId, users, null);
            }
        });
        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, ur.getRc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void testDeleteUsers_NullStatus() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final List<User> users = new ArrayList<>();
        final BaseResult result = new BaseResult();
        result.setRc(1);
        doReturn(result).when(provServiceMock).deleteUsers(instanceId, users,
                null);
        BaseResult ur = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteUsers(instanceId, users, null);
            }
        });
        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, ur.getRc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void modifySubscription_CreationConflict() throws Exception {
        createService();
        final String subscriptionId = "";
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                // Invoke modify although creation is just active
                return proxy.asyncModifySubscription(null, subscriptionId,
                        "referenceId", new ArrayList<ServiceParameter>(),
                        new ArrayList<ServiceAttribute>(), null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals(Messages.get("en", "error_instance_not_exists", "null"),
                result.getDesc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void modifySubscription_ConcurrencyConflict() throws Exception {
        final String instanceId = createService(ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        final String subscriptionId = "newId";
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                // Invoke modify although modification is just active
                return proxy.asyncModifySubscription(instanceId,
                        subscriptionId, "referenceId",
                        new ArrayList<ServiceParameter>(),
                        new ArrayList<ServiceAttribute>(), null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals(Messages.get("en", "error_parallel_service_processing",
                "sub123", instanceId), result.getDesc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void modifySubscription_appException() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        doReturn(new BaseResult()).when(provServiceMock)
                .asyncModifySubscription(instanceId, "", "referenceId",
                        new ArrayList<ServiceParameter>(),
                        new ArrayList<ServiceAttribute>(), null);

        when(
                controllerMock.modifyInstance(anyString(),
                        any(ProvisioningSettings.class),
                        any(ProvisioningSettings.class))).thenThrow(
                new APPlatformException("app Fault"));
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncModifySubscription(instanceId, "",
                        "referenceId", new ArrayList<ServiceParameter>(),
                        new ArrayList<ServiceAttribute>(), null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals("app Fault", result.getDesc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void modifySubscription_InvalidInstanceId() throws Exception {
        final String subscriptionId = "";
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncModifySubscription("unknown", subscriptionId,
                        "referenceId", new ArrayList<ServiceParameter>(),
                        new ArrayList<ServiceAttribute>(), null);
            }
        });
        assertEquals(1, result.getRc());
        assertEquals(
                Messages.get("en", "error_instance_not_exists", "unknown"),
                result.getDesc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void modifySubscription_ProductFailure() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final String subscriptionId = "";
        final BaseResult result = new BaseResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).modifySubscription(instanceId,
                subscriptionId, "referenceId",
                new ArrayList<ServiceParameter>(),
                new ArrayList<ServiceAttribute>(), null);

        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.modifyInstance(anyString(),
                        any(ProvisioningSettings.class),
                        any(ProvisioningSettings.class)))
                .thenReturn(mockStatus);

        BaseResult br = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncModifySubscription(instanceId,
                        subscriptionId, "referenceId",
                        new ArrayList<ServiceParameter>(),
                        new ArrayList<ServiceAttribute>(), null);
            }
        });
        Assert.assertEquals(result.getDesc(), br.getDesc());
        Assert.assertEquals(result.getRc(), br.getRc());
        verify(provServiceMock).modifySubscription(instanceId, subscriptionId,
                "referenceId", new ArrayList<ServiceParameter>(),
                new ArrayList<ServiceAttribute>(), null);
    }

    @Test
    public void modifySubscription_NoProvOnInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final String subscriptionId = "";
        final BaseResult result = new BaseResult();
        result.setRc(1);

        doReturn(result).when(provServiceMock).modifySubscription(instanceId,
                subscriptionId, "referenceId",
                new ArrayList<ServiceParameter>(),
                new ArrayList<ServiceAttribute>(), null);

        when(
                controllerMock.modifyInstance(anyString(),
                        any(ProvisioningSettings.class),
                        any(ProvisioningSettings.class))).thenReturn(
                new InstanceStatus());

        BaseResult br = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncModifySubscription(instanceId,
                        subscriptionId, "referenceId",
                        new ArrayList<ServiceParameter>(),
                        new ArrayList<ServiceAttribute>(), null);
            }
        });
        // OK result because provisioning on instance has never been called
        Assert.assertEquals(0, br.getRc());
        verifyZeroInteractions(provServiceMock);
    }

    @Test
    public void modifySubscription() throws Exception {
        final List<ServiceParameter> parameters = new ArrayList<>();
        final List<ServiceAttribute> attributes = new ArrayList<>();

        ServiceParameter p1 = new ServiceParameter();
        p1.setParameterId("param1");
        p1.setValue("value1");
        parameters.add(p1);
        ServiceParameter p2 = new ServiceParameter();
        p2.setParameterId("APP_param2");
        p2.setValue("xxxx");
        parameters.add(p2);

        ServiceAttribute a1 = new ServiceAttribute();
        a1.setAttributeId("attr1");
        a1.setValue("value1");
        attributes.add(a1);

        final List<ServiceParameter> filteredParameters = new ArrayList<>();
        filteredParameters.add(p1);

        final String subscriptionId = "subscriptionId_new";
        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(true);

        final String instanceId = createService(ProvisioningStatus.COMPLETED,
                "param1", "1", "APP_param2", "xyz");

        doReturn(new BaseResult()).when(provServiceMock).modifySubscription(
                instanceId, subscriptionId, "referenceId", filteredParameters,
                attributes, null);

        when(
                controllerMock.modifyInstance(anyString(),
                        any(ProvisioningSettings.class),
                        any(ProvisioningSettings.class)))
                .thenReturn(mockStatus);
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncModifySubscription(instanceId,
                        subscriptionId, "referenceId", parameters, attributes,
                        null);
            }
        });

        final ServiceInstance si = runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call()
                    throws ServiceInstanceNotFoundException {
                return instanceDAO.getInstanceById(instanceId);
            }
        });
        assertEquals(subscriptionId, si.getSubscriptionId());
        assertEquals(0, result.getRc());
        assertEquals("Ok", result.getDesc());

        verify(provServiceMock).modifySubscription(instanceId, subscriptionId,
                "referenceId", filteredParameters, attributes, null);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor
                .forClass(String.class);
        ArgumentCaptor<ProvisioningSettings> oldCaptor = ArgumentCaptor
                .forClass(ProvisioningSettings.class);
        ArgumentCaptor<ProvisioningSettings> newCaptor = ArgumentCaptor
                .forClass(ProvisioningSettings.class);
        verify(controllerMock, times(1)).modifyInstance(stringCaptor.capture(),
                oldCaptor.capture(), newCaptor.capture());
        assertEquals("appId123", stringCaptor.getValue());
        final Map<String, String> expectedNew = new HashMap<>();
        expectedNew.put("param1", "value1");
        expectedNew.put("APP_param2", "xxxx");
        Map<String, Setting> params = newCaptor.getValue().getParameters();
        assertEquals("value1", params.get("param1").getValue());
        assertEquals("xxxx", params.get("APP_param2").getValue());
        final Map<String, String> expectedOld = new HashMap<>();
        expectedOld.put("param1", "1");
        expectedOld.put("APP_param2", "xyz");
        params = oldCaptor.getValue().getParameters();
        assertEquals("1", params.get("param1").getValue());
        assertEquals("xyz", params.get("APP_param2").getValue());
    }

    @Test
    public void upgradeSubscription() throws Exception {
        final List<ServiceParameter> parameters = new ArrayList<>();
        final List<ServiceAttribute> attributes = new ArrayList<>();

        ServiceParameter p1 = new ServiceParameter();
        p1.setParameterId("param1");
        p1.setValue("value1");
        parameters.add(p1);
        ServiceParameter p2 = new ServiceParameter();
        p2.setParameterId("APP_param2");
        p2.setValue("xxxx");
        parameters.add(p2);

        ServiceAttribute a1 = new ServiceAttribute();
        a1.setAttributeId("attr1");
        a1.setValue("value1");
        attributes.add(a1);

        final List<ServiceParameter> filteredParameters = new ArrayList<>();
        filteredParameters.add(p1);

        final String subscriptionId = "subscriptionId_new";
        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(false);

        final String instanceId = createService(ProvisioningStatus.COMPLETED,
                "param1", "1", "APP_param2", "xyz");

        when(
                controllerMock.modifyInstance(anyString(),
                        any(ProvisioningSettings.class),
                        any(ProvisioningSettings.class)))
                .thenReturn(mockStatus);
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.asyncUpgradeSubscription(instanceId,
                        subscriptionId, "referenceId", parameters, attributes,
                        null);
            }
        });

        final ServiceInstance si = runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call()
                    throws ServiceInstanceNotFoundException {
                return instanceDAO.getInstanceById(instanceId);
            }
        });
        assertEquals(subscriptionId, si.getSubscriptionId());
        assertEquals(0, result.getRc());
        assertEquals("Ok", result.getDesc());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor
                .forClass(String.class);
        ArgumentCaptor<ProvisioningSettings> oldCaptor = ArgumentCaptor
                .forClass(ProvisioningSettings.class);
        ArgumentCaptor<ProvisioningSettings> newCaptor = ArgumentCaptor
                .forClass(ProvisioningSettings.class);
        verify(controllerMock, times(1)).modifyInstance(stringCaptor.capture(),
                oldCaptor.capture(), newCaptor.capture());
        assertEquals("appId123", stringCaptor.getValue());
        final Map<String, String> expectedNew = new HashMap<>();
        expectedNew.put("param1", "value1");
        expectedNew.put("APP_param2", "xxxx");
        Map<String, Setting> params = newCaptor.getValue().getParameters();
        assertEquals("value1", params.get("param1").getValue());
        assertEquals("xxxx", params.get("APP_param2").getValue());
        final Map<String, String> expectedOld = new HashMap<>();
        expectedOld.put("param1", "1");
        expectedOld.put("APP_param2", "xyz");
        params = oldCaptor.getValue().getParameters();
        assertEquals("1", params.get("param1").getValue());
        assertEquals("xyz", params.get("APP_param2").getValue());
    }

    @Test
    public void testDeleteInstanceConcurrencyNoConflict() throws Exception {
        final String instanceId = createService(ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        doReturn(new BaseResult()).when(provServiceMock).deleteInstance(
                eq(instanceId), anyString(), anyString(), any(User.class));

        doReturn(null).when(controllerMock).deleteInstance(anyString(),
                any(ProvisioningSettings.class));
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteInstance(instanceId, organizationId,
                        subscriptionId, null);
            }
        });

        verify(controllerMock).deleteInstance(eq(instanceId),
                any(ProvisioningSettings.class));
        assertEquals(0, result.getRc());
    }

    @Test
    public void testActivateInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final BaseResult result = new BaseResult();
        result.setDesc("Ok");

        doReturn(result).when(provServiceMock).activateInstance(instanceId,
                null);

        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.activateInstance(anyString(),
                        any(ProvisioningSettings.class)))
                .thenReturn(mockStatus);

        BaseResult br = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.activateInstance(instanceId, null);
            }
        });
        verify(provServiceMock).activateInstance(instanceId, null);
        Assert.assertEquals(result.getDesc(), br.getDesc());
        Assert.assertEquals(result.getRc(), br.getRc());
    }

    @Test
    public void testActivateInstance_NoProvOnInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED,
                "param1", "value1");
        doReturn(new BaseResult()).when(provServiceMock).activateInstance(
                instanceId, null);

        when(
                controllerMock.activateInstance(anyString(),
                        any(ProvisioningSettings.class))).thenReturn(null);
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.activateInstance(instanceId, null);
            }
        });
        assertEquals(0, result.getRc());
        assertEquals("Ok", result.getDesc());

        verifyZeroInteractions(provServiceMock);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor
                .forClass(String.class);
        ArgumentCaptor<ProvisioningSettings> settings = ArgumentCaptor
                .forClass(ProvisioningSettings.class);

        verify(controllerMock, times(1)).activateInstance(
                stringCaptor.capture(), settings.capture());
        assertEquals("appId123", stringCaptor.getValue());
        Map<String, Setting> params = settings.getValue().getParameters();
        assertEquals("value1", params.get("param1").getValue());
    }

    @Test
    public void testDeactivateInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final BaseResult result = new BaseResult();
        result.setDesc("Ok");
        doReturn(result).when(provServiceMock).deactivateInstance(instanceId,
                null);

        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.deactivateInstance(anyString(),
                        any(ProvisioningSettings.class)))
                .thenReturn(mockStatus);

        BaseResult br = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deactivateInstance(instanceId, null);
            }
        });
        verify(provServiceMock).deactivateInstance(instanceId, null);
        Assert.assertEquals(result.getDesc(), br.getDesc());
        Assert.assertEquals(result.getRc(), br.getRc());
    }

    @Test
    public void testDeactivateInstance_NoProvOnInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED,
                "param1", "value1");

        when(
                controllerMock.deactivateInstance(anyString(),
                        any(ProvisioningSettings.class))).thenReturn(null);
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deactivateInstance(instanceId, null);
            }
        });

        verifyZeroInteractions(provServiceMock);
        assertEquals(0, result.getRc());
        assertEquals("Ok", result.getDesc());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor
                .forClass(String.class);
        ArgumentCaptor<ProvisioningSettings> settings = ArgumentCaptor
                .forClass(ProvisioningSettings.class);

        verify(controllerMock, times(1)).deactivateInstance(
                stringCaptor.capture(), settings.capture());
        assertEquals("appId123", stringCaptor.getValue());
        Map<String, Setting> params = settings.getValue().getParameters();
        assertEquals("value1", params.get("param1").getValue());
    }

    @Test
    public void testDeleteInstanceAppException() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        when(
                controllerMock.deleteInstance(anyString(),
                        any(ProvisioningSettings.class))).thenThrow(
                new APPlatformException("APP Fault"));
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteInstance(instanceId, organizationId,
                        subscriptionId, null);
            }
        });
        verifyZeroInteractions(provServiceMock);
        assertEquals(1, result.getRc());
        assertEquals("APP Fault", result.getDesc());
    }

    @Test
    public void testDeleteInstanceInvalidInstanceId() throws Exception {
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteInstance("unknown", organizationId,
                        subscriptionId, null);
            }
        });
        // BSS sometimes sends delete instance again, therefore we should not
        // return RC1 on second delete
        verifyZeroInteractions(provServiceMock);
        assertEquals(0, result.getRc());
    }

    @Test
    public void testDeleteInstance_NoProvOnInstance() throws Exception {
        final String instanceId = createService(ProvisioningStatus.COMPLETED,
                "param1", "value1");

        when(
                controllerMock.deleteInstance(anyString(),
                        any(ProvisioningSettings.class))).thenReturn(null);
        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteInstance(instanceId, organizationId,
                        subscriptionId, null);
            }
        });
        verifyZeroInteractions(provServiceMock);

        assertEquals(0, result.getRc());
        assertEquals("Ok", result.getDesc());

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor
                .forClass(String.class);
        ArgumentCaptor<ProvisioningSettings> settings = ArgumentCaptor
                .forClass(ProvisioningSettings.class);

        verify(controllerMock, times(1)).deleteInstance(stringCaptor.capture(),
                settings.capture());
        assertEquals("appId123", stringCaptor.getValue());
        Map<String, Setting> params = settings.getValue().getParameters();
        assertEquals("value1", params.get("param1").getValue());
    }

    @Test
    public void testDeleteInstance() throws Exception {
        // given
        final String instanceId = createService(ProvisioningStatus.COMPLETED);
        final BaseResult result = new BaseResult();
        result.setDesc("Ok");
        doReturn(result).when(provServiceMock).deleteInstance(eq(instanceId),
                anyString(), anyString(), any(User.class));

        InstanceStatus mockStatus = new InstanceStatus();
        mockStatus.setInstanceProvisioningRequired(true);
        when(
                controllerMock.deleteInstance(anyString(),
                        any(ProvisioningSettings.class)))
                .thenReturn(mockStatus);

        // when
        BaseResult br = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.deleteInstance(instanceId, organizationId,
                        subscriptionId, null);
            }
        });

        // then
        verify(provServiceMock).deleteInstance(instanceId, organizationId,
                subscriptionId, null);
        assertEquals(result.getRc(), br.getRc());
        assertEquals(result.getDesc(), br.getDesc());
    }

    private String createService(final String... paramKeyValues)
            throws Exception {
        return createService(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                paramKeyValues);
    }

    private String createService(final ProvisioningStatus status,
            final String... paramKeyValues) throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() {
                ServiceInstance si = new ServiceInstance();
                si.setOrganizationId("org123");
                si.setSubscriptionId("sub123");
                si.setServiceBaseURL("http://localhost/");
                si.setBesLoginURL("http://localhost/");
                si.setDefaultLocale("en");
                si.setInstanceId("appId123");
                si.setControllerId("test.controller");
                si.setProvisioningStatus(status);
                final List<InstanceParameter> params = new ArrayList<>();
                final Iterator<String> i = Arrays.asList(paramKeyValues)
                        .iterator();
                while (i.hasNext()) {
                    final InstanceParameter p = new InstanceParameter();
                    p.setParameterKey(i.next());
                    p.setParameterValue(i.next());
                    p.setServiceInstance(si);
                    params.add(p);
                }
                si.setInstanceParameters(params);
                em.persist(si);
                return si.getInstanceId();
            }
        });
    }

    @Test
    public void testSaveAttributes() throws Exception {

        final ArrayList<ServiceAttribute> list = new ArrayList<>();

        ServiceAttribute attr = new ServiceAttribute();
        attr.setAttributeId("TEST");
        attr.setValue("value");
        list.add(attr);

        final BaseResult result = runTX(new Callable<BaseResult>() {
            @Override
            public BaseResult call() {
                return proxy.saveAttributes("abc", list, null);
            }
        });

        System.out.println(result.getDesc());
        assertEquals(0, result.getRc());
    }

}
