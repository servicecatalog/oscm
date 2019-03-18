/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Unit test.
 *       
 *  Creation Date: 2013-11-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.controller;

import junit.framework.Assert;
import org.junit.Test;
import org.oscm.app.openstack.*;
import org.oscm.app.openstack.data.FlowState;
import org.oscm.app.openstack.exceptions.OpenStackConnectionException;
import org.oscm.app.v2_0.data.*;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.ServiceNotReachableException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.ofInstant;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 
 */
public class OpenStackControllerTest extends EJBTestBase {


    private OpenStackController controller;
    private APPlatformService platformService;

    private final HashMap<String, Setting> parameters = new HashMap<String, Setting>();
    private final HashMap<String, Setting> configSettings = new HashMap<String, Setting>();
    private final ProvisioningSettings settings = new ProvisioningSettings(
            parameters, configSettings, "en");
    private InitialContext context;
    private final MockURLStreamHandler streamHandler = new MockURLStreamHandler();

    private HttpSession httpSession;
    private FacesContext facesContext;
    private ExternalContext externalContext;
    private OpenStackConnection openStackConnection;
    private KeystoneClient keystoneClient;
    private PasswordAuthentication passwordAuthentication;

    @Override
    protected void setup(TestContainer container) throws Exception {

        platformService = mock(APPlatformService.class);
        enableJndiMock();
        context = new InitialContext();
        context.bind(APPlatformService.JNDI_NAME, platformService);
        container.addBean(new OpenStackController());

        controller = spy(container.get(OpenStackController.class));
        httpSession = mock(HttpSession.class);
        facesContext = mock(FacesContext.class);
        externalContext = mock(ExternalContext.class);
        openStackConnection = mock(OpenStackConnection.class);
        keystoneClient = mock(KeystoneClient.class);
        passwordAuthentication = mock(PasswordAuthentication.class);
    }

    private void setupContext() {
        doReturn(httpSession).when(controller).getSession(any());
        doReturn(openStackConnection).when(controller).getOpenstackConnection();
        when(controller.getFacesContext()).thenReturn(facesContext);
        when(controller.getKeystoneClient(any())).thenReturn(keystoneClient);
        when(externalContext.getSession(anyBoolean())).thenReturn(httpSession);
        when(externalContext.getContext()).thenReturn(passwordAuthentication);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(httpSession.getAttribute(anyString())).thenReturn("string");
    }

    @Test
    public void ping_success() {
        //given
        setupContext();
        HashMap<String, Setting> settings = getCompleteSettings();

        //when
        try {
            when(platformService.getControllerSettings(any(), any())).thenReturn(settings);
            controller.ping(anyString());
        } catch (APPlatformException e) {
            fail("Keystone authentication failed");
        }

        //then
        //no exception is thrown
    }

    @Test
    public void canPing_failWhenGettingOpenStackSettings() {
        //given
        setupContext();
        HashMap<String, Setting> settings = getCompleteSettings();
        Exception exception = null;

        //when
        try {
            doThrow(new APPlatformException("Could not get Openstack Settings."))
                    .when(platformService).getControllerSettings(any(), any());
            controller.canPing();
        } catch (APPlatformException e) {
            exception = e;
        }

        //then
        Assert.assertNotNull("Exception is null.", exception);
    }

    @Test
    public void ping_failWhenGettingOpenStackSettings() {
        //given
        setupContext();
        HashMap<String, Setting> settings = getCompleteSettings();
        Exception exception = null;

        //when
        try {
            doThrow(new APPlatformException("Could not get Openstack Settings."))
                    .when(platformService).getControllerSettings(any(), any());
            controller.ping(anyString());
        } catch (APPlatformException e) {
            exception = e;
        }

        //then
        Assert.assertNotNull("Exception is null.", exception);
    }

    @Test
    public void ping_failWhenAuthenticatingWithKeystone() {
        //given
        setupContext();
        HashMap<String, Setting> settings = getCompleteSettings();
        Exception exception = null;

        //when
        try {
            doThrow(new OpenStackConnectionException("Could not authenticate."))
                    .when(keystoneClient)
                    .authenticate(anyString(), anyString(), anyString(), anyString());
            when(platformService.getControllerSettings(any(), any())).thenReturn(settings);
            controller.ping(anyString());
        } catch (ServiceNotReachableException | OpenStackConnectionException e) {
            exception = e;
        } catch (APPlatformException e) {
            fail("Wrong exception is thrown");
        }

        //then
        Assert.assertNotNull("Exception is null.", exception);
    }

    @Test
    public void canPing_success() {
        //given
        setupContext();
        HashMap<String, Setting> settings = getCompleteSettings();

        //when
        boolean result = false;
        try {
            when(platformService.getControllerSettings(any(), any())).thenReturn(settings);
            result = controller.canPing();
        } catch (APPlatformException e) {
            fail(e.getMessage());
        }

        //then
        Assert.assertTrue("canPing fail", result);
    }

    @Test
    public void canPing_failure() {
        //given
        setupContext();
        HashMap<String, Setting> settings = getEmptySettings();
        Exception exception = null;

        //when
        try {
            when(platformService.getControllerSettings(any(), any())).thenReturn(settings);
            controller.canPing();
        } catch (APPlatformException e) {
            exception = e;
        }

        //then
        Assert.assertNotNull("Exception is null", exception);
    }

    private HashMap<String, Setting> getCompleteSettings() {
        HashMap<String, Setting> settings = new HashMap<>();
        settings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(PropertyHandler.KEYSTONE_API_URL, "http://keystone:8080/v3"));
        settings.put(PropertyHandler.API_USER_NAME, new Setting(PropertyHandler.API_USER_NAME, "username"));
        settings.put(PropertyHandler.API_USER_PWD, new Setting(PropertyHandler.API_USER_PWD, "password"));
        settings.put(PropertyHandler.DOMAIN_NAME, new Setting(PropertyHandler.DOMAIN_NAME, "default"));
        settings.put(PropertyHandler.TENANT_ID, new Setting(PropertyHandler.TENANT_ID, "admin"));
        return settings;
    }

    private HashMap<String, Setting> getEmptySettings() {
        HashMap<String, Setting> settings = new HashMap<>();
        settings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(PropertyHandler.KEYSTONE_API_URL, ""));
        settings.put(PropertyHandler.API_USER_NAME, new Setting(PropertyHandler.API_USER_NAME, ""));
        settings.put(PropertyHandler.API_USER_PWD, new Setting(PropertyHandler.API_USER_PWD, ""));
        settings.put(PropertyHandler.DOMAIN_NAME, new Setting(PropertyHandler.DOMAIN_NAME, ""));
        settings.put(PropertyHandler.TENANT_ID, new Setting(PropertyHandler.TENANT_ID, ""));
        return settings;
    }

    @Test
    public void unbind_noPlatformService() throws Exception {
        context.unbind(APPlatformService.JNDI_NAME);
    }

    @Test
    public void createInstance() throws Exception {

        settings.getParameters().put(PropertyHandler.STACK_NAME,
                new Setting(PropertyHandler.STACK_NAME, "xyz"));

        InstanceDescription instance = createInstanceInternal();
        assertNotNull(instance);
        assertNotNull(instance.getInstanceId());
        assertTrue(instance.getInstanceId().startsWith("stack-"));

        assertEquals(FlowState.CREATION_REQUESTED.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());

    }

    @Test
    public void executeServiceOperation_settingsNull() throws Exception {
        controller.executeServiceOperation("userId", "instanceId",
                "transactionId", "operationId", null, null);
    }

    @Test
    public void executeServiceOperation_startSystem() throws Exception {
        ProvisioningSettings startSettings = new ProvisioningSettings(
                new HashMap<String, Setting>(), null, "en");
        controller.executeServiceOperation("userId", "instanceId",
                "transactionId", "START_VIRTUAL_SYSTEM", null, startSettings);

        assertTrue(Long.parseLong(startSettings.getParameters()
                .get("START_TIME").getValue()) > 0);
    }

    @Test
    public void executeServiceOperation_stopSystem() throws Exception {
        controller.executeServiceOperation("userId", "instanceId",
                "transactionId", "STOP_VIRTUAL_SYSTEM", null,
                new ProvisioningSettings(new HashMap<String, Setting>(), null,
                        "en"));
    }

    @Test
    public void executeServiceOperation_resumeSystem() throws Exception {
        controller.executeServiceOperation("userId", "instanceId",
                "transactionId", "RESUME_VIRTUAL_SYSTEM", null,
                new ProvisioningSettings(new HashMap<String, Setting>(), null,
                        "en"));
    }

    @Test
    public void executeServiceOperation_suspendSystem() throws Exception {
        controller.executeServiceOperation("userId", "instanceId",
                "transactionId", "SUSPEND_VIRTUAL_SYSTEM", null,
                new ProvisioningSettings(new HashMap<String, Setting>(), null,
                        "en"));
    }

    @Test(expected = APPlatformException.class)
    public void createInstance_stackNameEmpty() throws Exception {
        // given
        settings.getParameters().put(PropertyHandler.STACK_NAME,
                new Setting(PropertyHandler.STACK_NAME, " "));

        // when
        createInstanceInternal();
    }

    @Test(expected = APPlatformException.class)
    public void createInstance_stackNameIllegal() throws Exception {
        // given
        settings.getParameters().put(PropertyHandler.STACK_NAME,
                new Setting(PropertyHandler.STACK_NAME, "!§$%"));

        // when
        createInstanceInternal();
    }

    @Test(expected = APPlatformException.class)
    public void createInstance_stackNameBeginsWithNumber() throws Exception {
        // given
        settings.getParameters().put(PropertyHandler.STACK_NAME,
                new Setting(PropertyHandler.STACK_NAME, "0a"));

        // when
        createInstanceInternal();
    }

    @Test(expected = APPlatformException.class)
    public void createInstance_runtime() throws Exception {
        createInstanceInternal();
    }

    @Test
    public void deleteInstance() throws Exception {

        final String workloadId = "98345";
        InstanceStatus instanceStatus = deleteInstance(workloadId);
        assertNotNull(instanceStatus);
        assertTrue(instanceStatus.getRunWithTimer());

        assertEquals(FlowState.DELETION_REQUESTED.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
    }

    @Test
    public void activateInstance() throws Exception {

        final String workloadId = "98345";
        InstanceStatus instanceStatus = activateInstance(workloadId);
        assertNotNull(instanceStatus);
        assertTrue(instanceStatus.getRunWithTimer());

        assertEquals(FlowState.ACTIVATION_REQUESTED.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
    }

    @Test
    public void deactivateInstance() throws Exception {

        final String workloadId = "98345";
        InstanceStatus instanceStatus = deactivateInstance(workloadId);
        assertNotNull(instanceStatus);
        assertTrue(instanceStatus.getRunWithTimer());

        assertEquals(FlowState.DEACTIVATION_REQUESTED.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
    }

    @Test
    public void notifyInstance() throws Exception {

        String oldStatus = parameters.get(PropertyHandler.STATUS) == null ? null
                : parameters.get(PropertyHandler.STATUS).getValue();
        InstanceStatus status = notifyInstance("123", new Properties());
        assertNull(status);
        if (oldStatus == null) {
            assertNull(parameters.get(PropertyHandler.STATUS));
        } else {
            assertEquals(oldStatus,
                    parameters.get(PropertyHandler.STATUS).getValue());
        }
    }

    @Test
    public void modifyInstance() throws Exception {

        modifyInstance("123");
        assertEquals(FlowState.MODIFICATION_REQUESTED.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
    }

    @Test
    public void getInstanceStatus() throws Exception {

        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.MANUAL.toString()));
        InstanceStatus status = getInstanceStatus("123");
        assertEquals(FlowState.MANUAL.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
        assertFalse(status.isReady());
    }

    @Test(expected = APPlatformException.class)
    public void getInstanceStatus_startOperationTimeoutOccureed()
            throws Exception {

        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STARTING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "10"));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME,
                        String.valueOf(System.currentTimeMillis())));
        Thread.sleep(20);
        getInstanceStatus("123");
    }

    @Test
    public void getInstanceStatus_startOperationNotTimeout() throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STARTING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "1000000"));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME,
                        String.valueOf(System.currentTimeMillis())));
        streamHandler.put("/servers/0-Instance-server1",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respServerDetail("server1",
                                "0-Instance-server1", ServerStatus.SHUTOFF,
                                "testTenantID")));

        // when
        Thread.sleep(20);
        InstanceStatus status = getInstanceStatus("123");

        // then
        assertEquals(FlowState.STARTING.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
        assertFalse(status.isReady());
    }

    @Test
    public void getInstanceStatus_startOperationReadyTimeoutNotSet()
            throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STARTING.toString()));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME,
                        String.valueOf(System.currentTimeMillis())));
        streamHandler.put("/servers/0-Instance-server1",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respServerDetail("server1",
                                "0-Instance-server1", ServerStatus.SHUTOFF,
                                "testTenantID")));

        // when
        Thread.sleep(20);
        InstanceStatus status = getInstanceStatus("123");

        // then
        assertEquals(FlowState.STARTING.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
        assertFalse(status.isReady());
    }

    @Test(expected = APPlatformException.class)
    public void getInstanceStatus_startOperationAlreadyTimeout()
            throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STARTING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "1000000"));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME, "Timeout"));
        streamHandler.put("/servers/0-Instance-server1",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respServerDetail("server1",
                                "0-Instance-server1", ServerStatus.SHUTOFF,
                                "testTenantID")));

        // when
        getInstanceStatus("123");
    }

    @Test(expected = APPlatformException.class)
    public void getInstanceStatus_stopOperationTimeoutOccureed()
            throws Exception {

        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STOPPING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "10"));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME,
                        String.valueOf(System.currentTimeMillis())));
        Thread.sleep(20);
        getInstanceStatus("123");
    }

    @Test
    public void getInstanceStatus_stopOperationNotTimeout() throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STOPPING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "1000000"));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME,
                        String.valueOf(System.currentTimeMillis())));
        streamHandler.put("/servers/0-Instance-server1",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respServerDetail("server1",
                                "0-Instance-server1", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        Thread.sleep(20);
        InstanceStatus status = getInstanceStatus("123");

        // then
        assertEquals(FlowState.STOPPING.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
        assertFalse(status.isReady());
    }

    @Test
    public void getInstanceStatus_stopOperationReadyTimeoutNotSet()
            throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STOPPING.toString()));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME,
                        String.valueOf(System.currentTimeMillis())));
        streamHandler.put("/servers/0-Instance-server1",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respServerDetail("server1",
                                "0-Instance-server1", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        Thread.sleep(20);
        InstanceStatus status = getInstanceStatus("123");

        // then
        assertEquals(FlowState.STOPPING.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
        assertFalse(status.isReady());
    }

    @Test(expected = APPlatformException.class)
    public void getInstanceStatus_stopOperationAlreadyTimeout()
            throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.STOPPING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "1000000"));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME, "Timeout"));
        streamHandler.put("/servers/0-Instance-server1",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respServerDetail("server1",
                                "0-Instance-server1", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        getInstanceStatus("123");
    }

    @Test
    public void getInstanceStatus_activating() throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.ACTIVATING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "1000000"));
        parameters.put(PropertyHandler.START_TIME,
                new Setting(PropertyHandler.START_TIME,
                        String.valueOf(System.currentTimeMillis())));

        // when
        Thread.sleep(20);
        InstanceStatus status = getInstanceStatus("123");

        // then
        assertEquals(FlowState.ACTIVATING.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
        assertFalse(status.isReady());
    }

    @Test
    public void getInstanceStatus_activatingWithStartTime() throws Exception {
        // given
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        createBasicParameters("Instance4", "fosi_v2.json");
        parameters.put(PropertyHandler.STATUS, new Setting(
                PropertyHandler.STATUS, FlowState.ACTIVATING.toString()));
        configSettings.put(PropertyHandler.READY_TIMEOUT,
                new Setting(PropertyHandler.READY_TIMEOUT, "1000000"));

        // when
        Thread.sleep(20);
        InstanceStatus status = getInstanceStatus("123");

        // then
        assertEquals(FlowState.ACTIVATING.toString(),
                parameters.get(PropertyHandler.STATUS).getValue());
        assertFalse(status.isReady());
    }

    @Test
    public void createUsers() throws Exception {
        InstanceStatusUsers statusUsers = createUsers("123", null);
        assertNull(statusUsers);
    }

    @Test
    public void updateUsers() throws Exception {
        InstanceStatus status = updateUsers("123", null);
        assertNull(status);
    }

    @Test
    public void deleteUsers() throws Exception {
        InstanceStatus status = deleteUsers("123", null);
        assertNull(status);
    }

    @Test
    public void gatherUsageData_AnyRessourceType() throws Exception {
        // given
        settings.getParameters().put(PropertyHandler.STACK_NAME,
                new Setting(PropertyHandler.STACK_NAME, "xyz"));

        InstanceDescription instance = createInstanceInternal();

        settings.getParameters().put(PropertyHandler.RESOURCE_TYPE,
                new Setting(PropertyHandler.RESOURCE_TYPE, ""));

        // when
        controller.gatherUsageData("controllerId", instance.getInstanceId(),
                getTime(), getTime(), settings);
    }

    @Test(expected = APPlatformException.class)
    public void gatherUsageData_ProjectResourceType() throws Exception {
        // given
        settings.getParameters().put(PropertyHandler.STACK_NAME,
                new Setting(PropertyHandler.STACK_NAME, "xyz"));

        InstanceDescription instance = createInstanceInternal();

        settings.getParameters().put(PropertyHandler.RESOURCE_TYPE, new Setting(
                PropertyHandler.RESOURCE_TYPE, "OS::Keystone::Project"));
        settings.getParameters().put(PropertyHandler.IS_CHARGING,
                new Setting(PropertyHandler.IS_CHARGING, "true"));

        configSettings.put(PropertyHandler.API_USER_NAME,
                new Setting(PropertyHandler.API_USER_NAME, "horst"));

        configSettings.put(PropertyHandler.API_USER_PWD,
                new Setting(PropertyHandler.API_USER_PWD, "123"));
        configSettings.put(PropertyHandler.KEYSTONE_API_URL,
                new Setting(PropertyHandler.KEYSTONE_API_URL,
                        "http://keystone:8080/v3/auth"));
        settings.setConfigSettings(configSettings);

        // when
        controller.gatherUsageData("controllerId", instance.getInstanceId(),
                getTime(), getTime(), settings);
    }

    String getTime() throws Exception {
        long requestTime = System.currentTimeMillis();
        return ofInstant(ofEpochMilli(requestTime), of("UTC"))
                .format(ISO_LOCAL_DATE_TIME);
    }

    private InstanceDescription createInstanceInternal() throws Exception {
        InstanceDescription instance = runTX(
                new Callable<InstanceDescription>() {

                    @Override
                    public InstanceDescription call() throws Exception {
                        return controller.createInstance(settings);
                    }
                });
        return instance;
    }

    private InstanceStatus modifyInstance(final String instanceId)
            throws Exception {
        return runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.modifyInstance(instanceId, settings,
                        settings);
            }
        });
    }

    private InstanceStatus deleteInstance(final String instanceId)
            throws Exception {
        return runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.deleteInstance(instanceId, settings);
            }
        });
    }

    private InstanceStatus activateInstance(final String instanceId)
            throws Exception {
        return runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.activateInstance(instanceId, settings);
            }
        });
    }

    private InstanceStatus deactivateInstance(final String instanceId)
            throws Exception {
        return runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.deactivateInstance(instanceId, settings);
            }
        });
    }

    private InstanceStatus notifyInstance(final String instanceId,
            final Properties properties) throws Exception {
        return runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.notifyInstance(instanceId, settings,
                        properties);
            }
        });
    }

    private InstanceStatus getInstanceStatus(final String instanceId)
            throws Exception {
        InstanceStatus result = runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.getInstanceStatus(instanceId, settings);
            }
        });
        return result;
    }

    private InstanceStatusUsers createUsers(final String instanceId,
            final List<ServiceUser> users) throws Exception {
        InstanceStatusUsers instance = runTX(
                new Callable<InstanceStatusUsers>() {

                    @Override
                    public InstanceStatusUsers call() throws Exception {
                        return controller.createUsers(instanceId, settings,
                                users);
                    }
                });
        return instance;
    }

    private InstanceStatus updateUsers(final String instanceId,
            final List<ServiceUser> users) throws Exception {
        return runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.updateUsers(instanceId, settings, users);
            }
        });
    }

    private InstanceStatus deleteUsers(final String instanceId,
            final List<ServiceUser> users) throws Exception {
        return runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return controller.deleteUsers(instanceId, settings, users);
            }
        });
    }

    private void createBasicParameters(String instanceName,
            String templateName) {
        parameters.put(PropertyHandler.STACK_ID,
                new Setting(PropertyHandler.STACK_ID, "sID"));
        parameters.put(PropertyHandler.STACK_NAME,
                new Setting(PropertyHandler.STACK_NAME, instanceName));
        parameters.put(PropertyHandler.TEMPLATE_NAME,
                new Setting(PropertyHandler.TEMPLATE_NAME, templateName));
        parameters.put(PropertyHandler.TEMPLATE_PARAMETER_PREFIX + "KeyName",
                new Setting(
                        PropertyHandler.TEMPLATE_PARAMETER_PREFIX + "KeyName",
                        "key"));
        parameters.put(PropertyHandler.ACCESS_INFO_PATTERN, new Setting(
                PropertyHandler.ACCESS_INFO_PATTERN, "access info"));
        configSettings.put(PropertyHandler.KEYSTONE_API_URL,
                new Setting(PropertyHandler.KEYSTONE_API_URL,
                        "http://keystone:8080/v3/auth"));
        configSettings.put(PropertyHandler.DOMAIN_NAME,
                new Setting(PropertyHandler.DOMAIN_NAME, "testDomain"));
        configSettings.put(PropertyHandler.TENANT_ID,
                new Setting(PropertyHandler.TENANT_ID, "testTenantID"));
        configSettings.put(PropertyHandler.API_USER_NAME,
                new Setting(PropertyHandler.API_USER_NAME, "api_user"));
        configSettings.put(PropertyHandler.API_USER_PWD,
                new Setting(PropertyHandler.API_USER_PWD, "secret"));
        configSettings.put(PropertyHandler.TEMPLATE_BASE_URL,
                new Setting(PropertyHandler.TEMPLATE_BASE_URL,
                        "http://estfarmaki2:8880/templates/"));
    }

}
