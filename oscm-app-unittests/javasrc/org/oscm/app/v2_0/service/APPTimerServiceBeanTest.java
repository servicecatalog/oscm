/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 28.11.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.business.ProductProvisioningServiceFactoryBean;
import org.oscm.app.business.exceptions.BESNotificationException;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.test.ejb.TestNamingContextFactoryBuilder;
import org.oscm.vo.VOUserDetails;
import org.slf4j.Logger;

public class APPTimerServiceBeanTest {
    private static final String CONTROLLER_ID = "ess.aws";
    private APPTimerServiceBean timerService;
    private Timer timer;
    protected TimerService ts;
    private EntityManager em;
    private ServiceInstanceDAO instanceDAO;
    private APPConfigurationServiceBean configService;
    private ProductProvisioningServiceFactoryBean provFactoryBean;
    private BesDAO besDAOMock;
    private APPCommunicationServiceBean mailService;
    private Logger logger;
    private APPlatformController controller;
    private APPTimerServiceBean timerBean;

    @Before
    public void setup() throws Exception {
        timerService = spy(new APPTimerServiceBean());
        timer = mock(Timer.class);
        em = mock(EntityManager.class);
        logger = mock(Logger.class);
        timerService.em = em;
        timerService.logger = logger;
        doNothing().when(em).persist(any(ServiceInstance.class));
        ts = mock(TimerService.class);
        mailService = Mockito.mock(APPCommunicationServiceBean.class);
        besDAOMock = mock(BesDAO.class);
        provFactoryBean = mock(ProductProvisioningServiceFactoryBean.class);
        configService = mock(APPConfigurationServiceBean.class);
        instanceDAO = mock(ServiceInstanceDAO.class);
        timerBean = mock(APPTimerServiceBean.class);
        timerService.instanceDAO = instanceDAO;
        timerService.configService = configService;
        timerService.mailService = mailService;
        timerService.besDAO = besDAOMock;
        timerService.timerService = ts;
        timerService.appTimerServiceBean = timerBean;
        Collection<Timer> timers = new ArrayList<Timer>();
        controller = mock(APPlatformController.class);

        doReturn(getResult()).when(instanceDAO).getInstancesInWaitingState();
        doReturn(timers).when(timerService.timerService).getTimers();

    }

    private List<ServiceInstance> getResult() {
        List<ServiceInstance> result = new ArrayList<ServiceInstance>();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance
                .setInstanceId("aws-4df6b429-910b-48aa-88f5-d20a08f4d67f");
        serviceInstance.setControllerId("ess.aws");
        result.add(serviceInstance);
        return result;
    }

    @Test
    public void handleTimer_APPSuspended() throws Exception {
        // given

        doReturn(Boolean.TRUE).when(configService).isAPPSuspend();

        // when
        timerService.handleTimer(timer);

        // then
        verify(timerBean, times(1)).cancelTimers();
    }

    @Test
    public void handleTimer_doHandleSystems_APPSuspended() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(configService).isAPPSuspend();

        // when
        timerService.doHandleSystems(getResult(),
                ProvisioningStatus.getWaitingForCreation());

        // then
        verify(provFactoryBean, times(0)).getInstance(
                any(ServiceInstance.class));
        verify(timerService, times(0)).doHandleControllerProvisioning(
                any(ServiceInstance.class));
    }

    @Test
    public void handleBESNotificationException() throws Exception {
        // given
        ServiceInstance serviceInstance = getResult().get(0);
        doReturn(Boolean.TRUE).when(besDAOMock).isCausedByConnectionException(
                any(Throwable.class));
        doNothing().when(timerService).suspendApp(any(ServiceInstance.class),
                anyString());

        // when
        timerService.handleBESNotificationException(serviceInstance,
                ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION, null,
                new BESNotificationException("connection refused",
                        new Throwable()));

        // then
        verify(timerService, times(1)).suspendApp(any(ServiceInstance.class),
                anyString());
    }

    @Test
    public void suspendApp() throws Exception {
        // given
        ServiceInstance serviceInstance = getResult().get(0);
        doNothing().when(timerService).sendMailToAppAdmin(anyString());
        doNothing().when(configService).setAPPSuspend(anyString());
        doReturn("APP_BASE_URL").when(configService)
                .getProxyConfigurationSetting(
                        eq(PlatformConfigurationKey.APP_BASE_URL));

        // when
        timerService.suspendApp(serviceInstance,
                "mail_bes_notification_error_app_admin");

        // then
        verify(timerService, times(1)).sendMailToAppAdmin(anyString());
    }

    @Test
    public void sendMailToAppAdmin() throws Exception {
        // given
        doNothing().when(timerService).sendActionMailToAppAdmin(anyString(),
                anyString());
        doReturn("APP_BASE_URL").when(configService)
                .getProxyConfigurationSetting(
                        eq(PlatformConfigurationKey.APP_BASE_URL));

        // when
        timerService
                .sendMailToAppAdmin("mail_bes_notification_error_app_admin");
        // then
        verify(timerService, times(1)).sendActionMailToAppAdmin(anyString(),
                anyString());
    }

    @Test
    public void sendActionMailToAppAdmin_Exception() throws Exception {
        // given
        VOUserDetails adminuser = new VOUserDetails();
        adminuser.setKey(1000L);
        Mockito.doThrow(new ConfigurationException("")).when(configService)
                .getAPPAdministrator();

        // when
        timerService.sendActionMailToAppAdmin(
                "mail_bes_notification_error_app_admin", "");
        // then
        verify(timerService, times(0)).getMailBodyForInfo(anyString(),
                anyString(), any(ServiceInstance.class), any(Throwable.class));

    }

    @Test
    public void sendActionMailToAppAdmin_linkNull() throws Exception {
        // given
        VOUserDetails adminuser = new VOUserDetails();
        adminuser.setKey(1000L);
        doReturn(adminuser).when(configService).getAPPAdministrator();

        doReturn("").when(timerService).getMailSubject(anyString(),
                anyString(), anyString());
        doReturn("").when(timerService).getMailBodyForInfo(anyString(),
                anyString(), any(ServiceInstance.class), any(Throwable.class));
        doNothing().when(mailService).sendMail(anyListOf(String.class),
                anyString(), anyString());

        // when
        timerService.sendActionMailToAppAdmin(
                "mail_bes_notification_error_app_admin", null);
        // then
        verify(timerService, times(1)).getMailBodyForInfo(anyString(),
                anyString(), any(ServiceInstance.class), any(Throwable.class));
        verify(timerService, times(0)).getMailBodyForAction(anyString(),
                anyString(), any(ServiceInstance.class), any(Throwable.class),
                anyString(), anyBoolean());
    }

    @Test
    public void sendActionMailToAppAdmin_linkNotNull() throws Exception {
        // given
        VOUserDetails adminuser = new VOUserDetails();
        adminuser.setKey(1000L);
        doReturn(adminuser).when(configService).getAPPAdministrator();

        doReturn("").when(timerService).getMailSubject(anyString(),
                anyString(), anyString());
        doReturn("").when(timerService).getMailBodyForAction(anyString(),
                anyString(), any(ServiceInstance.class), any(Throwable.class),
                anyString(), anyBoolean());
        doNothing().when(mailService).sendMail(anyListOf(String.class),
                anyString(), anyString());

        // when
        timerService.sendActionMailToAppAdmin(
                "mail_bes_notification_error_app_admin", "");
        // then
        verify(timerService, times(1)).getMailBodyForAction(anyString(),
                anyString(), any(ServiceInstance.class), any(Throwable.class),
                anyString(), anyBoolean());
        verify(timerService, times(0)).getMailBodyForInfo(anyString(),
                anyString(), any(ServiceInstance.class), any(Throwable.class));
    }

    @Test
    public void restart_BESNotAvailable() {
        // given
        doReturn(Boolean.valueOf(false)).when(besDAOMock).isBESAvalible();

        // when
        boolean result = timerService.restart(false);

        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void restart() throws Exception {
        // given
        List<ServiceInstance> instances = prepareInstance("instance ID");
        doReturn(Boolean.valueOf(true)).when(besDAOMock).isBESAvalible();
        doReturn(instances).when(instanceDAO).getInstancesSuspendedbyApp();

        doNothing().when(timerService).sendActionMail(anyBoolean(),
                any(ServiceInstance.class), anyString(), any(Throwable.class),
                anyString(), anyBoolean());

        doReturn("APP_BASE_URL").when(configService)
                .getProxyConfigurationSetting(
                        eq(PlatformConfigurationKey.APP_BASE_URL));
        // when
        boolean result = timerService.restart(false);

        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        verify(timerService, times(1)).sendActionMail(anyBoolean(),
                any(ServiceInstance.class), anyString(), any(Throwable.class),
                anyString(), anyBoolean());
    }

    @Test
    public void restart_NoMail() throws Exception {
        // given
        List<ServiceInstance> instances = prepareInstance(null);
        doReturn(Boolean.valueOf(true)).when(besDAOMock).isBESAvalible();
        doReturn(instances).when(instanceDAO).getInstancesSuspendedbyApp();

        // when
        boolean result = timerService.restart(false);

        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(instances.get(0).isSuspendedByApp()));
        verify(timerService, times(0)).sendActionMail(anyBoolean(),
                any(ServiceInstance.class), anyString(), any(Throwable.class),
                anyString(), anyBoolean());
    }

    private List<ServiceInstance> prepareInstance(String instanceId) {
        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        ServiceInstance instance = new ServiceInstance();
        instance.setInstanceId(instanceId);
        instance.setControllerId(CONTROLLER_ID);
        instances.add(instance);
        return instances;

    }

    @Test
    public void testHandleTimer_doHandleControllerProvisioning_bug11449()
            throws Exception {
        // given
        ServiceInstance instance = new ServiceInstance();
        instance.setInstanceId("123");
        instance.setSubscriptionId("subscriptionId");
        instance.setControllerId("ess.vmware");
        instance.setProvisioningStatus(ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION);
        InstanceStatus status = new InstanceStatus();
        status.setIsReady(false);

        when(
                controller.getInstanceStatus(anyString(),
                        any(ProvisioningSettings.class))).thenReturn(status);
        doNothing().when(besDAOMock).notifyOnProvisioningStatusUpdate(
                any(ServiceInstance.class), anyListOf(LocalizedText.class));
        if (!NamingManager.hasInitialContextFactoryBuilder()) {
            NamingManager
                    .setInitialContextFactoryBuilder(new TestNamingContextFactoryBuilder());
        }
        InitialContext context = new InitialContext();
        context.bind(APPlatformController.JNDI_PREFIX + "ess.vmware",
                controller);

        // when
        timerService.doHandleControllerProvisioning(instance);

        // then
        verify(em, times(1)).refresh(any(ServiceInstance.class));
    }
}
