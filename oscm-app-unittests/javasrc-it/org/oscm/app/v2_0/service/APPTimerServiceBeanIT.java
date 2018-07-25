/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 17.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.business.ProductProvisioningServiceFactoryBean;
import org.oscm.app.business.exceptions.BESNotificationException;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.operation.data.OperationResult;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.ServiceAttribute;
import org.oscm.provisioning.data.ServiceParameter;
import org.oscm.provisioning.data.User;
import org.oscm.provisioning.intf.ProvisioningService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.types.enumtypes.OperationStatus;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.slf4j.LoggerFactory;

public class APPTimerServiceBeanIT extends EJBTestBase {

    private static final String CTRL_ID = "ess.vmware";
    private static final String TS_RETURN_BASE_URL = "http://1.2.3.4:8080";
    private final String ERROR_MESSAGE = "err_message";
    private final int RESPONSE_CODE = -1;
    private APPTimerServiceBean timerService;
    private APPTimerServiceBean timerService1;
    private Timer timer;

    private EntityManager em;

    private String instanceId;
    private APPlatformController controller;
    private APPCommunicationServiceBean mailService;
    private boolean requestInstanceProvisioning = false;
    private boolean setControllerReady = false;

    private APPConfigurationServiceBean configService;
    private ProvisioningService provService;
    private ProductProvisioningServiceFactoryBean provFactoryBean;
    private BesDAO besDAOMock;
    private OperationDAO operationDAOMock;
    private ServiceInstanceDAO instanceDAO;
    private VOUser defaultUser;
    private OperationServiceBean opBean;

    @Override
    protected void setup(TestContainer container) throws Exception {

        container.addBean(LoggerFactory.getLogger(APPTimerServiceBean.class));
        container.addBean(instanceDAO = new ServiceInstanceDAO());
        container.addBean(configService = Mockito
                .mock(APPConfigurationServiceBean.class));

        container.addBean(mock(APPConcurrencyServiceBean.class));
        container.addBean(provFactoryBean = Mockito
                .mock(ProductProvisioningServiceFactoryBean.class));

        provService = mock(ProvisioningService.class);
        doReturn(provService).when(provFactoryBean)
                .getInstance(any(ServiceInstance.class));

        container.addBean(
                mailService = Mockito.mock(APPCommunicationServiceBean.class));

        container.addBean(besDAOMock = mock(BesDAO.class));
        doReturn(Arrays.asList(new VOUserDetails())).when(besDAOMock)
                .getBESTechnologyManagers(any(ServiceInstance.class));
        operationDAOMock = mock(OperationDAO.class);
        container.addBean(operationDAOMock);
        container.addBean(opBean = mock(OperationServiceBean.class));
        container.addBean(mock(APPAuthenticationServiceBean.class));
        container.addBean(new APPlatformServiceBean());
        controller = mock(APPlatformController.class);
        InitialContext context = new InitialContext();
        context.bind("bss/app/controller/ess.vmware", controller);
        container.addBean(controller);

        timer = mock(Timer.class);

        defaultUser = new VOUser();
        defaultUser.setUserId("user");

        em = instanceDAO.em;
        container.addBean(timerService = spy(new APPTimerServiceBean()));
        container.addBean(timerService1 = spy(new APPTimerServiceBean()));
        timerService.em = em;
        timerService1.em = em;
        timerService.instanceDAO = instanceDAO;
        timerService1.instanceDAO = instanceDAO;
        timerService.configService = configService;
        timerService1.configService = configService;
        timerService.provServFact = provFactoryBean;
        timerService1.provServFact = provFactoryBean;
        timerService.besDAO = besDAOMock;
        timerService1.besDAO = besDAOMock;
        timerService.mailService = mailService;
        timerService1.mailService = mailService;
        timerService.operationDAO = operationDAOMock;
        timerService1.operationDAO = operationDAOMock;
        timerService.opBean = opBean;
        timerService1.opBean = opBean;
        timerService.appTimerServiceBean = timerService1;
    }

    private InstanceResult getInstanceResult(int returnCode) {
        InstanceResult instanceResult = new InstanceResult();
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAccessInfo("TSReturnAccessInfo");
        instanceInfo.setBaseUrl(TS_RETURN_BASE_URL);
        instanceInfo.setInstanceId("TSReturnInstanceId");
        instanceInfo.setLoginPath("TSReturnLoginPath");
        instanceResult.setInstance(instanceInfo);
        instanceResult.setRc(returnCode);
        return instanceResult;
    }

    @Test
    public void testHandleTimerNoStoredServiceInstance() throws Exception {
        // when
        handleTimer();

        // then
        verifyZeroInteractions(provFactoryBean);
        verify(timerService1).cancelTimers();
    }

    @Test
    public void testHandleTimerNoWaitingServiceInstances() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED);

        // when
        handleTimer();

        // then
        verifyZeroInteractions(provFactoryBean);
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        verify(timerService1).cancelTimers();
    }

    @Test
    public void testHandleTimerOneWaitingServiceInstance() throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
    }

    @Test
    public void testInstanceActivationError() throws Exception {
        // given
        requestInstanceProvisioning = false;
        setControllerReady = true;
        ServiceInstance si = createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .activateInstance(anyString(), any(User.class));
        doThrow(new APPlatformException(ERROR_MESSAGE)).when(controller)
                .getInstanceStatus(anyString(),
                        any(ProvisioningSettings.class));

        // when
        handleTimer();

        // then
        String subject = Messages.get("en", "mail_activation_error.subject",
                si.getSubscriptionId());
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        verify(mailService, times(1)).sendMail(anyListOf(String.class),
                eq(subject), anyString());
    }

    @Test
    public void testInstanceConnectServerError() throws Exception {
        // given
        requestInstanceProvisioning = false;
        setControllerReady = true;
        ServiceInstance si = createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .activateInstance(anyString(), any(User.class));
        doThrow(new SuspendException(ERROR_MESSAGE, RESPONSE_CODE))
                .when(controller).getInstanceStatus(anyString(),
                        any(ProvisioningSettings.class));

        // when
        handleTimer();

        // then
        String subject = Messages.get("en", "mail_server_connect_error.subject",
                si.getSubscriptionId());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        verify(mailService, times(1)).sendMail(anyListOf(String.class),
                eq(subject), anyString());
    }

    @Test
    public void testInstanceDeactivationError() throws Exception {
        // given
        requestInstanceProvisioning = false;
        setControllerReady = true;
        ServiceInstance si = createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DEACTIVATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .activateInstance(anyString(), any(User.class));
        doThrow(new APPlatformException("")).when(controller).getInstanceStatus(
                anyString(), any(ProvisioningSettings.class));

        // when
        handleTimer();

        // then
        String subject = Messages.get("en", "mail_deactivation_error.subject",
                si.getSubscriptionId());
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        verify(mailService, times(1)).sendMail(anyListOf(String.class),
                eq(subject), anyString());
    }

    @Test
    public void testHandleTimerOneWaitingServiceInstanceNotReady()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = false;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleTimerOneCreatingServiceInstance() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);

        InstanceStatus status = new InstanceStatus();
        List<LocalizedText> list = new ArrayList<>();
        list.add(new LocalizedText("en", "finished"));
        status.setDescription(list);
        status.setIsReady(true);
        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "4.3.2.1"));
        status.setChangedParameters(parameters);

        OperationResult operationResult = new OperationResult();
        operationResult.setErrorMessage(null);
        when(opBean.executeServiceOperationFromQueue(anyString()))
                .thenReturn(operationResult);
        when(controller.getInstanceStatus(matches("appInstanceId"),
                any(ProvisioningSettings.class))).thenReturn(status);

        // when
        handleTimer();

        // then
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
    }

    void handleTimer() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                timerService.handleTimer(timer);
                return null;
            }
        });
    }

    void raiseEvent(final String controllerId, final String instanceId,
            final Properties properties) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                timerService.raiseEvent(controllerId, instanceId, properties);
                return null;
            }
        });
    }

    @Test
    public void testHandleTimerOneCreatingServiceInstance_withInstProv()
            throws Exception {

        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);

        InstanceStatus status = new InstanceStatus();
        List<LocalizedText> list = new ArrayList<>();
        list.add(new LocalizedText("en", "finished"));
        status.setDescription(list);
        status.setIsReady(true);
        status.setChangedParameters(null);
        status.setInstanceProvisioningRequired(true);
        when(controller.getInstanceStatus(matches("appInstanceId"),
                any(ProvisioningSettings.class))).thenReturn(status);

        // when
        handleTimer();

        // then
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.TRUE },
                new Boolean[] { Boolean.TRUE });
    }

    @Test
    public void testHandleTimerOneModifyingServiceInstance_Ready()
            throws Exception {
        // given
        requestInstanceProvisioning = false;
        setControllerReady = false;
        createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION,
                InstanceParameter.PUBLIC_IP);
        InstanceStatus status1 = new InstanceStatus();
        status1.setIsReady(false);
        status1.setInstanceProvisioningRequired(true);
        InstanceStatus status2 = new InstanceStatus();
        status2.setIsReady(true);
        status2.setInstanceProvisioningRequired(false);

        when(controller.getInstanceStatus(anyString(),
                any(ProvisioningSettings.class))).thenReturn(status1, status2);

        // when
        handleTimer();
        handleTimer();
        handleTimer();

        // then
        verify(provFactoryBean, timeout(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.TRUE });
    }

    @Test
    public void testHandleTimerOneModifyingServiceInstance_NotReady()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = false;
        createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION,
                InstanceParameter.PUBLIC_IP);

        // when
        handleTimer();

        // then
        verify(provFactoryBean).getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.FALSE });
    }

    @Test
    public void testHandleTimerOneDeletingServiceInstance_Ready()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService).deleteInstance(
                anyString(), anyString(), anyString(), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.TRUE });
    }

    @Test
    public void testHandleTimerOneDeletingServiceInstance_NotReady()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = false;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION,
                InstanceParameter.PUBLIC_IP);

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.FALSE });
    }

    @Test
    public void testHandleTimerOneActivatingServiceInstance_Ready()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .activateInstance(anyString(), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.TRUE });
    }

    @Test
    public void testHandleTimerOneActivatingServiceInstance_NotReady()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = false;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION,
                InstanceParameter.PUBLIC_IP);

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.FALSE });
    }

    @Test
    public void testHandleTimerOneDeactivatingServiceInstance_Ready()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DEACTIVATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .deactivateInstance(anyString(), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.TRUE });
    }

    @Test
    public void testHandleTimerOneDeactivatingServiceInstance_NotReady()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = false;
        createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DEACTIVATION,
                InstanceParameter.PUBLIC_IP);

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DEACTIVATION);
        validateServiceInstanceFlags(null, new Boolean[] { Boolean.FALSE },
                new Boolean[] { Boolean.FALSE });
    }

    @Test
    public void testHandleTimer_IgnoreProvisiongOnUserOperations()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        Map<String, Setting> params = new HashMap<>();
        params.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params.put("subscriptionId", new Setting("subscriptionId", "sub1"));
        params.put("instanceId", new Setting("instanceId", "inst1"));
        createServiceInstance(ProvisioningStatus.WAITING_FOR_USER_CREATION,
                params);
        doReturn(getInstanceResult(0)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        params = new HashMap<>();
        params.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params.put("subscriptionId", new Setting("subscriptionId", "sub2"));
        params.put("instanceId", new Setting("instanceId", "inst2"));
        createServiceInstance(ProvisioningStatus.WAITING_FOR_USER_MODIFICATION,
                params);
        doReturn(getInstanceResult(0)).when(provService).modifySubscription(
                anyString(), anyString(), anyString(),
                anyListOf(ServiceParameter.class),
                anyListOf(ServiceAttribute.class), any(User.class));

        // when
        handleTimer();

        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        params = new HashMap<>();
        params.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params.put("subscriptionId", new Setting("subscriptionId", "sub3"));
        params.put("instanceId", new Setting("instanceId", "inst3"));
        createServiceInstance(ProvisioningStatus.WAITING_FOR_USER_DELETION,
                params);
        doReturn(getInstanceResult(0)).when(provService).deleteInstance(
                anyString(), anyString(), anyString(), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(3))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED,
                ProvisioningStatus.COMPLETED, ProvisioningStatus.COMPLETED);
        validateServiceInstanceFlags(null,
                new Boolean[] { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE },
                new Boolean[] { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE });
    }

    @Test
    public void testHandleTimerOneWaitingServiceInstanceNoResult()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = false;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleTimerOneWaitingServiceInstanceNoInstanceInfoInResult()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleTimerWaitingServiceInstances() throws Exception {
        // given
        Map<String, Setting> params1 = new HashMap<>();
        params1.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params1.put("subscriptionId", new Setting("subscriptionId", "sub1"));
        params1.put("instanceId", new Setting("instanceId", "inst1"));
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                params1);
        Map<String, Setting> params2 = new HashMap<>();
        params2.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params2.put("subscriptionId", new Setting("subscriptionId", "sub2"));
        params2.put("instanceId", new Setting("instanceId", "inst2"));
        requestInstanceProvisioning = false;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.COMPLETED, params2);
        Map<String, Setting> params3 = new HashMap<>();
        params3.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params3.put("subscriptionId", new Setting("subscriptionId", "sub3"));
        params3.put("instanceId", new Setting("instanceId", "inst3"));
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                params3);
        Map<String, Setting> params4 = new HashMap<>();
        params4.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params4.put("subscriptionId", new Setting("subscriptionId", "sub4"));
        params4.put("instanceId", new Setting("instanceId", "inst4"));
        requestInstanceProvisioning = false;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.COMPLETED, params4);

        doReturn(getInstanceResult(0)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(2))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED,
                ProvisioningStatus.COMPLETED, ProvisioningStatus.COMPLETED,
                ProvisioningStatus.COMPLETED);
    }

    @Test
    public void testHandleTimerWaitingServiceInstancesProcessingException()
            throws Exception {
        // given
        Map<String, Setting> params1 = new HashMap<>();
        params1.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params1.put("subscriptionId", new Setting("subscriptionId", "sub1"));
        params1.put("instanceId", new Setting("instanceId", "inst1"));
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                params1);
        Map<String, Setting> params2 = new HashMap<>();
        params2.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params2.put("subscriptionId", new Setting("subscriptionId", "sub2"));
        params2.put("instanceId", new Setting("instanceId", "inst2"));
        requestInstanceProvisioning = false;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.COMPLETED, params2);
        Map<String, Setting> params3 = new HashMap<>();
        params3.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params3.put("subscriptionId", new Setting("subscriptionId", "sub3"));
        params3.put("instanceId", new Setting("instanceId", "inst3"));
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                params3);
        Map<String, Setting> params4 = new HashMap<>();
        params4.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params4.put("subscriptionId", new Setting("subscriptionId", "sub4"));
        params4.put("instanceId", new Setting("instanceId", "inst4"));
        requestInstanceProvisioning = false;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.COMPLETED, params4);

        // 2 invocations for WAITING_FOR_SYSTEM_CREATION instances
        doReturn("").doThrow(new RuntimeException()).when(provService)
                .sendPing(anyString());
        // 1 invocation for first WAITING_FOR_SYSTEM_CREATION, no second
        // invocation because of the exception
        doReturn(getInstanceResult(0)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(2))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED,
                ProvisioningStatus.COMPLETED,
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                ProvisioningStatus.COMPLETED);
    }

    @Test
    public void testHandleTimerVerifyServiceCreation() throws Exception {
        // given
        requestInstanceProvisioning = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);

        // when
        handleTimer();

        // then
        verify(provService).createInstance(any(InstanceRequest.class),
                any(User.class));
    }

    @Test
    public void testHandleTimerVerifyServiceCreationWithParams()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                "param1", "param2");
        InstanceRequest ir = new InstanceRequest();
        doReturn(ir).when(timerService)
                .getInstanceRequest(any(ServiceInstance.class));

        // when
        handleTimer();

        // then
        verify(provService).createInstance(ir, null);
    }

    @Test
    public void testGetInstancerequestWithParamsFilteringRequired()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        ServiceInstance si = createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.APP_PARAM_KEY_PREFIX + "param1", "param2");

        // when
        InstanceRequest ir = timerService.getInstanceRequest(si);

        // then
        List<ServiceParameter> parameters = ir.getParameterValue();
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals("param2Value", parameters.get(0).getValue());
        Assert.assertEquals(si.getDefaultLocale(), ir.getDefaultLocale());
        Assert.assertEquals(si.getOrganizationId(), ir.getOrganizationId());
        Assert.assertEquals(si.getOrganizationName(), ir.getOrganizationName());
        Assert.assertEquals(si.getSubscriptionId(), ir.getSubscriptionId());
        Assert.assertEquals(si.getBesLoginURL(), ir.getLoginUrl());
    }

    @Test
    public void testHandleTimerVerifyBESNotificationOnCompletion()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(besDAOMock).notifyAsyncSubscription(any(ServiceInstance.class),
                any(InstanceResult.class), eq(true),
                any(APPlatformException.class));
    }

    @Test
    public void testHandleTimerVerifyBESNotificationOnAbort() throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(1)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(besDAOMock, times(1)).notifyAsyncSubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(APPlatformException.class));
    }

    @Test
    public void testHandleTimerVerifyBESNotificationOnMailSend()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        doReturn(new VOUserDetails()).when(configService).getAPPAdministrator();
        doThrow(new APPlatformException("")).when(controller).getInstanceStatus(
                anyString(), any(ProvisioningSettings.class));
        doThrow(new BESNotificationException("", new Throwable()))
                .when(timerService).notifyOnProvisioningAbortion(
                        any(ServiceInstance.class), any(InstanceResult.class),
                        any(APPlatformException.class));
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(1)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(mailService, times(1)).sendMail(anyListOf(String.class),
                anyString(), anyString());
    }

    @Test
    public void testHandleTimerVerifyBESNotificationOnStatusUpdate()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        doThrow(new RuntimeException()).when(provService).sendPing(anyString());
        List<LocalizedText> messages = new ArrayList<>();
        messages.add(new LocalizedText());
        doReturn(messages).when(timerService).getErrorMessages();

        // when
        handleTimer();

        // then
        verify(besDAOMock).notifyOnProvisioningStatusUpdate(
                any(ServiceInstance.class), eq(messages));
    }

    @Test
    public void testHandleTimerVerifyBESNotificationParameters()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        doReturn(getInstanceResult(0)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(besDAOMock).notifyAsyncSubscription(any(ServiceInstance.class),
                any(InstanceResult.class), eq(true),
                any(APPlatformException.class));
    }

    @Test
    public void testHandleTimerBESNotificationOnStatusUpdateException()
            throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);

        InstanceStatus status = new InstanceStatus();
        List<LocalizedText> list = new ArrayList<>();
        list.add(new LocalizedText("en", "nextStep"));
        status.setDescription(list);
        when(controller.getInstanceStatus(matches("appInstanceId"),
                any(ProvisioningSettings.class))).thenReturn(status);
        doReturn(new VOUserDetails()).when(configService).getAPPAdministrator();
        doThrow(new BESNotificationException("", new Throwable()))
                .when(besDAOMock)
                .notifyOnProvisioningStatusUpdate(any(ServiceInstance.class),
                        anyListOf(LocalizedText.class));

        ServiceInstance si = getServiceInstance();
        Assert.assertTrue(si.getRunWithTimer());

        // when
        handleTimer();

        // then
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
        si = getServiceInstance();
        Assert.assertFalse(si.getRunWithTimer());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleTimerVerifyServiceParameterPersistence()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        InstanceResult ir = getInstanceResult(0);
        doReturn(ir).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        ServiceInstance si = getServiceInstance();
        Assert.assertNotNull(si);
        Assert.assertEquals(ir.getInstance().getAccessInfo(),
                si.getServiceAccessInfo());
        Assert.assertEquals(ir.getInstance().getBaseUrl(),
                si.getServiceBaseURL());
        Assert.assertEquals(ir.getInstance().getLoginPath(),
                si.getServiceLoginPath());
    }

    @Test
    public void testHandleTimerReadyButNoPublicIP() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);

        InstanceStatus status = new InstanceStatus();
        status.setIsReady(true);
        status.setInstanceProvisioningRequired(true);
        HashMap<String, Setting> parameters = new HashMap<>();
        parameters.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, ""));
        status.setChangedParameters(parameters);
        List<LocalizedText> list = new ArrayList<>();
        list.add(new LocalizedText("en", "nextStep"));
        status.setDescription(list);
        when(controller.getInstanceStatus(anyString(),
                any(ProvisioningSettings.class))).thenReturn(status);

        ServiceInstance si = getServiceInstance();
        Assert.assertTrue(si.getRunWithTimer());

        // when
        handleTimer();

        // then
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
        si = getServiceInstance();
        Assert.assertFalse(si.getRunWithTimer());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleTimerSuspendDuringCreation() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        SuspendException ex = new SuspendException("suspended");
        when(controller.getInstanceStatus(anyString(),
                any(ProvisioningSettings.class))).thenThrow(ex);

        ServiceInstance si = getServiceInstance();
        Assert.assertTrue(si.getRunWithTimer());

        // when
        handleTimer();

        // then
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
        si = getServiceInstance();
        Assert.assertFalse(si.getRunWithTimer());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleTimerSuspendDuringUpdate() throws Exception {
        // given
        createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        SuspendException ex = new SuspendException("suspended");
        when(controller.getInstanceStatus(anyString(),
                any(ProvisioningSettings.class))).thenThrow(ex);

        ServiceInstance si = getServiceInstance();
        Assert.assertTrue(si.getRunWithTimer());

        // when
        handleTimer();

        // then
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
        si = getServiceInstance();
        Assert.assertFalse(si.getRunWithTimer());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
    }

    @Test
    public void testHandleTimerSuspendDuringDeletion() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION);
        SuspendException ex = new SuspendException("suspended");
        when(controller.getInstanceStatus(anyString(),
                any(ProvisioningSettings.class))).thenThrow(ex);

        ServiceInstance si = getServiceInstance();
        Assert.assertTrue(si.getRunWithTimer());

        // when
        handleTimer();

        // then
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
        si = getServiceInstance();
        Assert.assertFalse(si.getRunWithTimer());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION);
    }

    @Test
    public void testHandleTimerSuspendDuringActivation() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION);
        SuspendException ex = new SuspendException("suspended");
        when(controller.getInstanceStatus(anyString(),
                any(ProvisioningSettings.class))).thenThrow(ex);
        ServiceInstance si = getServiceInstance();
        Assert.assertTrue(si.getRunWithTimer());

        // when
        handleTimer();

        // then
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
        si = getServiceInstance();
        Assert.assertFalse(si.getRunWithTimer());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION);
    }

    @Test(expected = APPlatformException.class)
    public void testRaiseEventNoControllerId() throws Exception {
        // when
        raiseEvent(null, null, null);

        // then
        verifyZeroInteractions(provFactoryBean);
    }

    @Test(expected = APPlatformException.class)
    public void testRaiseEventNoStoredServiceInstance() throws Exception {
        // when
        raiseEvent(CTRL_ID, null, null);

        // then
        verifyZeroInteractions(provFactoryBean);
    }

    @Test(expected = APPlatformException.class)
    public void testRaiseEventWrongServiceInstance() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);

        // when
        raiseEvent(CTRL_ID, "invalidApp", null);
    }

    @Test(expected = APPlatformException.class)
    public void testRaiseEventWrongControllerInstance() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);

        // when
        raiseEvent("invalidCtrl", "appInstanceId", null);
    }

    @Test
    public void testRaiseEventOneServiceInstance() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.COMPLETED,
                InstanceParameter.PUBLIC_IP);

        // when
        raiseEvent(CTRL_ID, "appInstanceId", null);

        // then
        verify(controller, times(1)).notifyInstance(matches("appInstanceId"),
                any(ProvisioningSettings.class), any(Properties.class));
    }

    @Test
    public void testRaiseEventOneServiceInstanceDisableTimer()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);

        // Disable timer through event
        InstanceStatus rcStatus = new InstanceStatus();
        rcStatus.setRunWithTimer(false);
        when(controller.notifyInstance(matches("appInstanceId"),
                any(ProvisioningSettings.class), any(Properties.class)))
                        .thenReturn(rcStatus);

        // when
        raiseEvent(CTRL_ID, "appInstanceId", null);
        handleTimer();

        // then
        verifyZeroInteractions(provFactoryBean);
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleNoTimerOneWaitingServiceInstance() throws Exception {
        // given
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.PUBLIC_IP);
        setServiceInstanceRunWithTimer(instanceId, false);

        // when
        handleTimer();

        // then
        verifyZeroInteractions(provFactoryBean);
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    @Test
    public void testHandleNoTimerTwoWaitingServiceInstances() throws Exception {
        // given
        Map<String, Setting> params1 = new HashMap<>();
        params1.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.4"));
        params1.put("subscriptionId", new Setting("subscriptionId", "sub1"));
        params1.put("instanceId", new Setting("instanceId", "inst1"));
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                params1);
        String key1 = instanceId;
        Map<String, Setting> params2 = new HashMap<>();
        params2.put(InstanceParameter.PUBLIC_IP,
                new Setting(InstanceParameter.PUBLIC_IP, "1.2.3.5"));
        params2.put("subscriptionId", new Setting("subscriptionId", "sub2"));
        params2.put("instanceId", new Setting("instanceId", "inst2"));
        requestInstanceProvisioning = true;
        setControllerReady = true;
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                params2);
        String key2 = instanceId;
        setServiceInstanceRunWithTimer(key1, false);
        setServiceInstanceRunWithTimer(key2, true);

        doReturn(getInstanceResult(0)).when(provService)
                .createInstance(any(InstanceRequest.class), any(User.class));

        // when
        handleTimer();

        // then
        verify(provFactoryBean, times(1))
                .getInstance(any(ServiceInstance.class));
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                ProvisioningStatus.COMPLETED);
    }

    /**
     * Validates error handling on case of EJB exception during creation
     * (Bugzilla #9566)
     */
    @Test
    public void testHandleEJBErrorDuringCreation() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);

        // Throw EJB exception when creation status is invoked
        EJBException e = new EJBException("ejb_error");
        when(controller.getInstanceStatus(matches("appInstanceId"),
                any(ProvisioningSettings.class))).thenThrow(e);

        // when
        handleTimer();

        // then
        verify(besDAOMock, times(1)).notifyAsyncSubscription(
                any(ServiceInstance.class), any(InstanceResult.class),
                eq(false), any(APPlatformException.class));
    }

    /**
     * Validates error handling on case of EJB AND BES exception during creation
     * (Bugzilla #9566)
     */
    @Test
    public void testHandleEJBAndBESErrorDuringCreation() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);

        // Throw EJB exception when creation status is invoked
        EJBException e = new EJBException("ejb_error");
        when(controller.getInstanceStatus(matches("appInstanceId"),
                any(ProvisioningSettings.class))).thenThrow(e);

        doThrow(new BESNotificationException("", new Throwable()))
                .when(besDAOMock).notifyAsyncSubscription(
                        any(ServiceInstance.class), any(InstanceResult.class),
                        eq(false), any(APPlatformException.class));

        doReturn(new VOUserDetails()).when(configService).getAPPAdministrator();

        // when
        handleTimer();

        // then
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
        ServiceInstance si = getServiceInstance();
        Assert.assertFalse(si.getRunWithTimer());
        validateServiceInstanceStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
    }

    /**
     * Validates error mailing in case of deletion problems.
     */
    @Test
    public void testHandleTimerErrorDuringDeletion() throws Exception {
        // given
        createServiceInstance(ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION);

        // Throw exception when deletion status is invoked
        APPlatformException e = new APPlatformException("error");
        when(controller.getInstanceStatus(matches("appInstanceId"),
                any(ProvisioningSettings.class))).thenThrow(e);

        VOUserDetails admin = new VOUserDetails();
        admin.setEMail("sss");
        doReturn(Arrays.asList(admin)).when(besDAOMock)
                .getBESTechnologyManagers(any(ServiceInstance.class));

        // when
        handleTimer();

        // then
        // => instance must be removed but mail is sent to TP, mail set
        Assert.assertNull(getServiceInstance());
        verify(mailService).sendMail(anyListOf(String.class), anyString(),
                anyString());
    }

    /**
     * Validates error mailing in case of update problems.
     */
    @Test
    public void testHandleTimerErrorDuringUpdate() throws Exception {
        // given
        createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION);
        String baseUrl = "BASEURL";
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("NAME1", "VALUE1");
        parameters.put("NAME2", "VALUE2");

        // Throw exception when deletion status is invoked
        APPlatformException e = new APPlatformException("error");
        when(controller.getInstanceStatus(matches("appInstanceId"),
                any(ProvisioningSettings.class))).thenThrow(e);
        doReturn(baseUrl).when(configService).getProxyConfigurationSetting(
                PlatformConfigurationKey.APP_BASE_URL);
        // when
        handleTimer();

        // then
        // => instance must be set to COMPLETED but mail is sent to TP
        validateServiceInstanceStatus(ProvisioningStatus.COMPLETED);
        validateRollbackedInstanceParameters(parameters);
        verify(timerService, times(1))
                .generateLinkForControllerUI(any(ServiceInstance.class));
        verify(timerService).sendActionMail(eq(true),
                any(ServiceInstance.class), anyString(),
                any(APPlatformException.class),
                eq(baseUrl + "/controller?controllerid=ess.vmware"), eq(false));
    }

    /**
     * Returns the first service instance entry found in the database.
     * 
     * @return A service instance.
     */
    private ServiceInstance getServiceInstance() throws Exception {
        return runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                Query query = em
                        .createQuery("SELECT si FROM ServiceInstance si");
                List<?> resultList = query.getResultList();
                if (resultList.isEmpty()) {
                    return null;
                } else {
                    return (ServiceInstance) resultList.get(0);
                }
            }
        });
    }

    /**
     * Reads the existing service instances and validates that their status
     * matches the ones specified (in the given order).
     * 
     * @param expectedStatus
     *            The status to check for.
     */
    private void validateServiceInstanceStatus(
            final ProvisioningStatus... expectedStatus) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = em.createQuery(
                        "SELECT si FROM ServiceInstance si ORDER BY si.tkey ASC");
                List<?> result = query.getResultList();
                Assert.assertEquals(expectedStatus.length, result.size());
                for (int i = 0; i < result.size(); i++) {
                    ServiceInstance currentEntry = (ServiceInstance) result
                            .get(i);
                    Assert.assertEquals("Wrong status at index " + i + " => ",
                            expectedStatus[i],
                            currentEntry.getProvisioningStatus());
                }
                return null;
            }
        });
    }

    private void validateRollbackedInstanceParameters(
            final HashMap<String, String> parameterMap) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = em.createQuery(
                        "SELECT si FROM ServiceInstance si ORDER BY si.tkey ASC");
                List<?> result = query.getResultList();
                for (int i = 0; i < result.size(); i++) {
                    ServiceInstance currentEntry = (ServiceInstance) result
                            .get(i);
                    Assert.assertNull(currentEntry.getRollbackParameters());
                    List<InstanceParameter> params = currentEntry
                            .getInstanceParameters();
                    Assert.assertEquals(parameterMap.size(), params.size());

                    for (InstanceParameter tempParameter : params) {
                        assertTrue(parameterMap
                                .containsKey(tempParameter.getParameterKey()));
                        Assert.assertEquals(
                                parameterMap
                                        .get(tempParameter.getParameterKey()),
                                tempParameter.getParameterValue());
                    }
                }
                return null;
            }
        });
    }

    /**
     * Reads the existing service instances and validates that their flags
     * against the ones specified (in the given order).
     */
    private void validateServiceInstanceFlags(final Boolean[] isRunWithTimer,
            final Boolean[] isInstanceProvisioning,
            final Boolean[] isControllerReady) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = em.createQuery(
                        "SELECT si FROM ServiceInstance si ORDER BY si.tkey ASC");
                List<?> result = query.getResultList();
                assertTrue(isRunWithTimer == null
                        || result.size() == isRunWithTimer.length);
                assertTrue(isInstanceProvisioning == null
                        || result.size() == isInstanceProvisioning.length);
                assertTrue(isControllerReady == null
                        || result.size() == isControllerReady.length);
                for (int i = 0; i < result.size(); i++) {
                    ServiceInstance currentEntry = (ServiceInstance) result
                            .get(i);
                    if (isRunWithTimer != null) {
                        assertTrue(
                                "Instance '" + currentEntry.getInstanceId()
                                        + "' should "
                                        + (isRunWithTimer[i].booleanValue() ? ""
                                                : "not ")
                                        + "have runWithTimer flag set to TRUE",
                                isRunWithTimer[i].booleanValue() == currentEntry
                                        .getRunWithTimer());
                    }
                    if (isInstanceProvisioning != null) {
                        assertTrue(
                                "Instance '" + currentEntry.getInstanceId()
                                        + "' should "
                                        + (isInstanceProvisioning[i]
                                                .booleanValue() ? "" : "not ")
                                        + "have instanceProvisioning flag set to TRUE",
                                isInstanceProvisioning[i]
                                        .booleanValue() == currentEntry
                                                .isInstanceProvisioning());
                    }
                    if (isControllerReady != null) {
                        assertTrue(
                                "Instance '" + currentEntry.getInstanceId()
                                        + "' should "
                                        + (isControllerReady[i].booleanValue()
                                                ? "" : "not ")
                                        + "have controllerReady flag set to TRUE",
                                isControllerReady[i]
                                        .booleanValue() == currentEntry
                                                .isControllerReady());
                    }
                }
                return null;
            }
        });
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
                si.setRollbackParameters(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"param1.name\">NAME1</entry>\r\n<entry key=\"param1.value\">VALUE1</entry>\r\n<entry key=\"param1.encryption\">false</entry>\r\n<entry key=\"ROLLBACK_SUBSCRIPTIONID\">"
                                + si.getSubscriptionId()
                                + "</entry>\r\n<entry key=\"param2.name\">NAME2</entry>\r\n<entry key=\"param2.value\">VALUE2</entry>\r\n<entry key=\"param2.encryption\">false</entry>\r\n</properties>\r\n");
                si.setRollbackInstanceAttributes(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\r\n<properties>\r\n<entry key=\"att1.name\">NAME1</entry>\r\n<entry key=\"att1.value\">VALUE1</entry>\r\n<entry key=\"att1.encryption\">false</entry>\r\n<entry key=\"ROLLBACK_SUBSCRIPTIONID\">"
                                + si.getSubscriptionId()
                                + "</entry>\r\n<entry key=\"att2.name\">NAME2</entry>\r\n<entry key=\"att2.value\">VALUE2</entry>\r\n<entry key=\"att2.encryption\">false</entry>\r\n</properties>\r\n");
                em.persist(si);
                em.flush();
                instanceId = si.getInstanceId();
                return si;
            }
        });
    }

    /**
     * Updates the "runWithTimer" flag for the given service
     * 
     * @param si
     *            the service which should be updated
     * @param runWithTimer
     *            the new value of the runWithTimer flag
     */
    private void setServiceInstanceRunWithTimer(final String instanceKey,
            final boolean runWithTimer) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = em.createQuery(
                        "SELECT si FROM ServiceInstance si WHERE si.instanceId = :key");
                query.setParameter("key", instanceKey);
                List<?> resultList = query.getResultList();
                if (resultList.size() != 1) {
                    return null;
                }

                ServiceInstance si = (ServiceInstance) resultList.get(0);
                si.setRunWithTimer(runWithTimer);
                em.persist(si);
                em.flush();
                return null;
            }
        });
    }

    @Test
    public void filterListTest() {
        // given
        EnumSet<ProvisioningStatus> enumSet = EnumSet.of(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION);
        ServiceInstance si_creation = new ServiceInstance();
        si_creation.setProvisioningStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        ServiceInstance si_deletion = new ServiceInstance();
        si_deletion.setProvisioningStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DELETION);
        ServiceInstance si_activation = new ServiceInstance();
        si_activation.setProvisioningStatus(
                ProvisioningStatus.WAITING_FOR_SYSTEM_ACTIVATION);
        List<?> result = Arrays.asList(si_creation, si_deletion, si_activation);

        // when
        List<ServiceInstance> filteredList = timerService.filterList(result,
                enumSet);

        // then
        assertTrue(filteredList.contains(si_creation));
        assertTrue(filteredList.contains(si_deletion));
        assertFalse(filteredList.contains(si_activation));
    }

    @Test
    public void doHandleControllerProvisioning_NamingException()
            throws Exception {
        // given
        requestInstanceProvisioning = true;
        ServiceInstance si = createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION,
                InstanceParameter.APP_PARAM_KEY_PREFIX + "param1", "param2");
        si.setControllerId("ess.ror");
        org.slf4j.Logger logger = mock(org.slf4j.Logger.class);
        doReturn(logger).when(timerService).getLogger();

        // when
        timerService.doHandleControllerProvisioning(si);

        // then
        verify(logger, times(0)).error(
                "The application could not be contacted. Please try again later.");

    }

    @Test
    public void doHandleControllerProvisioning_bug11379() throws Exception {
        // given
        requestInstanceProvisioning = true;
        ServiceInstance si = createServiceInstance(
                ProvisioningStatus.WAITING_FOR_SYSTEM_DEACTIVATION,
                InstanceParameter.APP_PARAM_KEY_PREFIX + "param1", "param2");

        doThrow(new SuspendException(ERROR_MESSAGE, RESPONSE_CODE))
                .when(controller).getInstanceStatus(anyString(),
                        any(ProvisioningSettings.class));
        Operation op = new Operation();
        op.setTransactionId("transactionId");
        doReturn(op).when(operationDAOMock)
                .getOperationByInstanceId(anyString());
        doNothing().when(timerService).handleSuspendException(
                any(ServiceInstance.class), any(ProvisioningStatus.class),
                any(SuspendException.class));
        // when
        timerService.doHandleControllerProvisioning(si);

        // then
        verify(besDAOMock, times(1)).notifyAsyncOperationStatus(eq(si),
                eq(op.getTransactionId()), eq(OperationStatus.ERROR),
                anyListOf(LocalizedText.class));
    }
}
