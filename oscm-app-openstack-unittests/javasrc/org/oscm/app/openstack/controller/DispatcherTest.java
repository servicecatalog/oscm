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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.oscm.app.openstack.HeatProcessor;
import org.oscm.app.openstack.MockHttpURLConnection;
import org.oscm.app.openstack.MockHttpsURLConnection;
import org.oscm.app.openstack.MockURLStreamHandler;
import org.oscm.app.openstack.OpenStackConnection;
import org.oscm.app.openstack.data.FlowState;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.data.Template;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPTemplateService;
import org.oscm.app.v2_0.intf.APPlatformService;

/**
 * @author Dirk Bernsau
 * 
 */
public class DispatcherTest {

    private PropertyHandler paramHandler;
    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private Dispatcher dispatcher;
    private final MockURLStreamHandler streamHandler = new MockURLStreamHandler();
    private APPlatformService platformService;
    @Captor
    private ArgumentCaptor<String> subject;
    @Captor
    private ArgumentCaptor<String> text;
    private static final String ACCESS_INFO = "access";
    private static final String ACCESS_INFO_NOT_AVAILABLE = "Access information currently not available";

    enum InstanceType {
        NOVA("OS::Nova::Server"), EC2("AWS::EC2::Instance"), TROVE(
                "OS::Trove::Instance");

        private final String text;

        private InstanceType(final String text) {
            this.text = text;
        }

        public String getString() {
            return this.text;
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        configSettings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                PropertyHandler.KEYSTONE_API_URL,
                "http://keystone:8080/v3/auth"));
        configSettings.put(PropertyHandler.API_USER_NAME, new Setting(
                PropertyHandler.API_USER_NAME, "api_user"));
        configSettings.put(PropertyHandler.API_USER_PWD, new Setting(
                PropertyHandler.API_USER_PWD, "secret"));
        configSettings.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, "testTenantID"));
        configSettings.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, "demo"));
        configSettings.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL,
                "http://estfarmaki2:8880/templates/"));
        settings = new ProvisioningSettings(parameters, configSettings, "en");
        settings.setSubscriptionId("subscriptionId");
        settings.getParameters().put(PropertyHandler.ACCESS_INFO_PATTERN,
                new Setting(PropertyHandler.ACCESS_INFO_PATTERN, ACCESS_INFO));
        settings.getParameters().put(
                PropertyHandler.TEMPLATE_NAME,
                new Setting(PropertyHandler.TEMPLATE_NAME,
                        "/templates/fosi_v2.json"));
        paramHandler = spy(new PropertyHandler(settings));
        paramHandler.setStackId("sID");
        platformService = mock(APPlatformService.class);
        doNothing().when(platformService).sendMail(anyListOf(String.class),
                subject.capture(), text.capture());
        User user = new User();
        user.setLocale("de");
        doReturn(user).when(platformService).authenticate(anyString(),
                any(PasswordAuthentication.class));
        dispatcher = new Dispatcher(platformService, "123", paramHandler) {
            @Override
            protected HeatProcessor getHeatProcessor()
                    throws AuthenticationException, APPlatformException {
                return givenHeatProcessor();
            }
        };
        paramHandler.setStackName("Instance4");
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
    }

    @Test
    public void creationRequested() throws Exception {
        // given
        paramHandler.setState(FlowState.CREATION_REQUESTED);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.CREATING_STACK.toString(), status);
        assertFalse(result.isReady());
    }

    @Test
    public void activationRequested() throws Exception {
        // given
        paramHandler.setState(FlowState.ACTIVATION_REQUESTED);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertEquals(ACCESS_INFO, result.getAccessInfo());
        assertTrue(result.isReady());
    }

    @Test
    public void creationRequested_AbortException() throws Exception {
        // given
        String url = "/templates/xyz.json";
        paramHandler.setState(FlowState.CREATION_REQUESTED);
        settings.getParameters().put(PropertyHandler.TEMPLATE_NAME,
                new Setting(PropertyHandler.TEMPLATE_NAME, url));

        try {

            dispatcher = new Dispatcher(platformService, "123", paramHandler) {
                @Override
                protected HeatProcessor getHeatProcessor()
                        throws AuthenticationException, APPlatformException {
                    return givenHeatProcessor_Error();
                }
            };
            // when
            dispatcher.dispatch();
            assertTrue("Test must fail at this point!", false);
        } catch (AbortException ex) {
            // then
            assertTrue(ex.getProviderMessages().get(0).getText().indexOf(url) > 0);
        }
    }

    @Test
    public void startRequested() throws Exception {
        // given
        paramHandler.setState(FlowState.START_REQUESTED);
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.STARTING.toString(), status);
        assertFalse(result.isReady());
    }

    @Test(expected = APPlatformException.class)
    public void startRequested_allServersFaild() throws Exception {
        // given
        paramHandler.setState(FlowState.START_REQUESTED);
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2",
                                ServerStatus.SHUTOFF, "testTenantID")));

        streamHandler.put(
                "/stacks/" + paramHandler.getStackName() + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/0-Instance-server1/action", connection);
        streamHandler
                .put("/servers/1-Instance-otherserver2/action", connection);

        // when
        dispatcher.dispatch();
    }

    @Test
    public void stopRequested() throws Exception {
        // given
        paramHandler.setState(FlowState.STOP_REQUESTED);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.STOPPING.toString(), status);
        assertFalse(result.isReady());
    }

    @Test(expected = APPlatformException.class)
    public void stopRequested_allServersFaild() throws Exception {
        // given
        paramHandler.setState(FlowState.STOP_REQUESTED);
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ACTIVE,
                                "testTenantID")));

        streamHandler.put(
                "/stacks/" + paramHandler.getStackName() + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/0-Instance-server1/action", connection);
        streamHandler
                .put("/servers/1-Instance-otherserver2/action", connection);

        // when
        dispatcher.dispatch();
    }

    @Test
    public void deactivationRequested() throws Exception {
        // given
        paramHandler.setState(FlowState.DEACTIVATION_REQUESTED);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.DEACTIVATING.toString(), status);
        assertEquals(ACCESS_INFO_NOT_AVAILABLE, result.getAccessInfo());
        assertFalse(result.isReady());
    }

    @Test
    public void activating() throws Exception {
        // given
        paramHandler.setState(FlowState.ACTIVATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.RESUME_COMPLETE,
                                true)));

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertEquals(ACCESS_INFO, result.getAccessInfo());
        assertTrue(result.isReady());
    }

    @Test(expected = SuspendException.class)
    public void activating_FAILED() throws Exception {
        // given
        paramHandler.setState(FlowState.ACTIVATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respStacksInstanceName(
                                StackStatus.RESUME_FAILED, true)));

        // when
        dispatcher.dispatch();
    }

    @Test
    public void activating_wrongStatus() throws Exception {
        // given
        paramHandler.setState(FlowState.ACTIVATING);

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test
    public void starting() throws Exception {
        // given
        paramHandler.setState(FlowState.STARTING);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertEquals(ACCESS_INFO, result.getAccessInfo());
        assertTrue(result.isReady());
    }

    @Test(expected = APPlatformException.class)
    public void starting_FAILED() throws Exception {
        // given
        paramHandler.setState(FlowState.STARTING);
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.ERROR, "testTenantID")));

        // when
        dispatcher.dispatch();
    }

    @Test
    public void starting_stillStopped() throws Exception {
        // given
        paramHandler.setState(FlowState.STARTING);
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test
    public void creatingStack() throws Exception {
        // given
        paramHandler.setState(FlowState.CREATING_STACK);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertEquals(ACCESS_INFO, result.getAccessInfo());
        assertTrue(result.isReady());
    }

    @Test(expected = AbortException.class)
    public void creatingStack_FAILED() throws Exception {
        // given
        paramHandler.setState(FlowState.CREATING_STACK);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respStacksInstanceName(
                                StackStatus.CREATE_FAILED, true)));

        // when
        dispatcher.dispatch();
    }

    @Test
    public void creatingStack_wrongStatus() throws Exception {
        // given
        paramHandler.setState(FlowState.CREATING_STACK);
        streamHandler.put("/stacks/Instance4", new MockHttpURLConnection(200,
                MockURLStreamHandler.respStacksInstanceName(null, true)));

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test
    public void creating_sendMail() throws Exception {
        // given
        configSettings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                PropertyHandler.KEYSTONE_API_URL,
                "https://keystone:8080/v3/auth"));
        configSettings.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, "domain1"));
        configSettings.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, "098765"));
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@mail.com"));
        paramHandler.setState(FlowState.CREATING_STACK);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpsURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.CREATE_COMPLETE,
                                true)));
        doReturn("test").when(platformService).getEventServiceUrl();
        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.MANUAL.toString(), status);
        assertFalse(result.isReady());
        assertFalse(result.getRunWithTimer());
        assertTrue(subject.getValue().contains("subscriptionId"));
        assertTrue(text.getValue().contains("subscriptionId"));
        assertTrue(text.getValue().contains("domain1"));
        assertTrue(text.getValue().contains("098765"));
    }

    @Test
    public void updating() throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@mail.com"));
        configSettings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                PropertyHandler.KEYSTONE_API_URL,
                "https://keystone:8080/v3/auth"));
        configSettings.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, "domain1"));
        configSettings.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, "87654"));
        paramHandler.setState(FlowState.UPDATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpsURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.UPDATE_COMPLETE,
                                true)));

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertEquals(ACCESS_INFO, result.getAccessInfo());
        assertTrue(result.isReady());
        assertTrue(text.getValue().contains("domain1"));
        assertTrue(text.getValue().contains("87654"));
    }

    @Test
    public void updating_sendMail() throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@mail.com"));
        paramHandler.setState(FlowState.UPDATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.UPDATE_COMPLETE,
                                true)));
        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertTrue(result.isReady());
        assertTrue(result.getRunWithTimer());
    }

    @Test(expected = AbortException.class)
    public void updating_FAILED() throws Exception {
        // given
        paramHandler.setState(FlowState.UPDATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respStacksInstanceName(
                                StackStatus.UPDATE_FAILED, true)));

        // when
        dispatcher.dispatch();
    }

    @Test
    public void updating_wrongStatus() throws Exception {
        // given
        paramHandler.setState(FlowState.UPDATING);

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test
    public void deletingStack() throws Exception {
        // given already deleted stack
        paramHandler.setStackName("InstanceDeleted");
        MockHttpURLConnection mockUrlConnection = new MockHttpURLConnection(
                404, MockURLStreamHandler.respStackDeleted());
        mockUrlConnection.setIOException(new IOException());
        streamHandler.put("/stacks/InstanceDeleted", mockUrlConnection);
        paramHandler.setState(FlowState.DELETING_STACK);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.DESTROYED.toString(), status);
        assertTrue(result.isReady());
    }

    @Test
    public void deletingStack_sendMail() throws Exception {
        // given
        parameters.put(PropertyHandler.MAIL_FOR_COMPLETION, new Setting(
                PropertyHandler.MAIL_FOR_COMPLETION, "test@mail.com"));
        paramHandler.setState(FlowState.DELETING_STACK);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.DELETE_COMPLETE,
                                true)));
        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.DESTROYED.toString(), status);
        assertTrue(result.isReady());
        assertTrue(result.getRunWithTimer());
    }

    @Test
    public void deletingStack_COMPLETE() throws Exception {
        // given
        paramHandler.setState(FlowState.DELETING_STACK);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.DELETE_COMPLETE,
                                true)));

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.DESTROYED.toString(), status);
        assertTrue(result.isReady());
    }

    @Test(expected = SuspendException.class)
    public void deletingStack_FAILED() throws Exception {
        // given
        paramHandler.setState(FlowState.DELETING_STACK);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200,
                        MockURLStreamHandler.respStacksInstanceName(
                                StackStatus.DELETE_FAILED, true)));

        // when
        dispatcher.dispatch();
    }

    @Test(expected = SuspendException.class)
    public void deletingStack_FAILED_401() throws Exception {
        // given
        paramHandler.setState(FlowState.DELETING_STACK);
        MockHttpURLConnection muc = new MockHttpURLConnection(401,
                MockURLStreamHandler.respStacksInstanceName(
                        StackStatus.DELETE_FAILED, true));
        muc.setIOException(new IOException("test"));
        streamHandler.put("/stacks/Instance4", muc);

        // when
        dispatcher.dispatch();
    }

    @Test(expected = AbortException.class)
    public void creatingStack_FAILED_404() throws Exception {
        // given
        paramHandler.setState(FlowState.CREATING_STACK);
        MockHttpURLConnection muc = new MockHttpURLConnection(404,
                MockURLStreamHandler.respStacksInstanceName(
                        StackStatus.DELETE_FAILED, true));
        muc.setIOException(new IOException("test"));
        streamHandler.put("/stacks/Instance4", muc);

        // when
        dispatcher.dispatch();
    }

    @Test(expected = APPlatformException.class)
    public void creatingStack_FAILED_RuntimeException() throws Exception {
        // given
        paramHandler.setState(FlowState.CREATING_STACK);
        MockHttpURLConnection muc = new MockHttpURLConnection(404,
                MockURLStreamHandler.respStacksInstanceName(
                        StackStatus.DELETE_FAILED, true));
        muc.setRuntimeException(new RuntimeException("test"));
        streamHandler.put("/stacks/Instance4", muc);

        // when
        dispatcher.dispatch();
    }

    @Test(expected = APPlatformException.class)
    public void creatingStack_FAILED_301() throws Exception {
        // given
        paramHandler.setState(FlowState.CREATING_STACK);
        MockHttpURLConnection muc = new MockHttpURLConnection(301,
                MockURLStreamHandler.respStacksInstanceName(
                        StackStatus.DELETE_FAILED, true));
        muc.setIOException(new IOException("test"));
        streamHandler.put("/stacks/Instance4", muc);

        // when
        dispatcher.dispatch();
    }

    @Test
    public void deletingStack_wrongStatus() throws Exception {
        // given
        paramHandler.setState(FlowState.DELETING_STACK);

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test(expected = SuspendException.class)
    public void deactivating_FAILED() throws Exception {
        // given
        paramHandler.setState(FlowState.DEACTIVATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_FAILED,
                                true)));

        // when
        dispatcher.dispatch();
    }

    @Test(expected = InstanceNotAliveException.class)
    public void deactivating_FAILED_InstanceNotFound() throws Exception {
        // given
        paramHandler.setState(FlowState.DEACTIVATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_FAILED,
                                true, "Failed to find instance example ")));

        // when
        dispatcher.dispatch();
    }

    @Test
    public void deactivating_wrongStatus() throws Exception {
        // given
        paramHandler.setState(FlowState.DEACTIVATING);

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test
    public void deactivating() throws Exception {
        // given
        paramHandler.setState(FlowState.DEACTIVATING);
        streamHandler.put(
                "/stacks/Instance4",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_COMPLETE,
                                true)));

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertEquals(ACCESS_INFO_NOT_AVAILABLE, result.getAccessInfo());
        assertTrue(result.isReady());
    }

    @Test
    public void stopping_stillError() throws Exception {
        // stop function can be executed even if server status is ERROR

        // given
        paramHandler.setState(FlowState.STOPPING);
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.ERROR, "testTenantID")));

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test
    public void stopping_stillActive() throws Exception {
        // given
        paramHandler.setState(FlowState.STOPPING);
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.ACTIVE, "testTenantID")));

        // when
        dispatcher.dispatch();

        // then
        assertFalse(FlowState.FINISHED.toString().equals(
                parameters.get(PropertyHandler.STATUS)));
    }

    @Test
    public void stopping() throws Exception {
        // given
        paramHandler.setState(FlowState.STOPPING);
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.FINISHED.toString(), status);
        assertEquals(ACCESS_INFO, result.getAccessInfo());
        assertTrue(result.isReady());
    }

    @Test
    public void modificationRequested() throws Exception {
        // given
        paramHandler.setState(FlowState.MODIFICATION_REQUESTED);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.UPDATING.toString(), status);
        assertFalse(result.isReady());

    }

    @Test
    public void deletionRequested() throws Exception {
        // given
        paramHandler.setState(FlowState.DELETION_REQUESTED);

        // when
        InstanceStatus result = dispatcher.dispatch();

        // then
        String status = parameters.get(PropertyHandler.STATUS).getValue();
        assertEquals(FlowState.DELETING_STACK.toString(), status);
        assertFalse(result.isReady());

    }

    private HeatProcessor givenHeatProcessor() throws AuthenticationException,
            APPlatformException {
        Template t = new Template();
        t.setContent(MockURLStreamHandler.respTemplatesFosi_v2().getBytes());

        final APPTemplateService service = mock(APPTemplateService.class);
        when(
                service.getTemplate(anyString(), anyString(),
                        any(PasswordAuthentication.class))).thenReturn(t);

        return new HeatProcessor() {
            @Override
            protected APPTemplateService getTemplateService()
                    throws NamingException {
                return service;
            }
        };
    }

    private HeatProcessor givenHeatProcessor_Error()
            throws AuthenticationException, APPlatformException {
        Template t = new Template();
        t.setContent(MockURLStreamHandler.respTemplatesFosi_v2().getBytes());

        final APPTemplateService service = Mockito
                .mock(APPTemplateService.class);

        doThrow(new APPlatformException("")).when(service).getTemplate(
                anyString(), anyString(), any(PasswordAuthentication.class));

        return new HeatProcessor() {
            @Override
            protected APPTemplateService getTemplateService()
                    throws NamingException {
                return service;
            }
        };
    }
}
