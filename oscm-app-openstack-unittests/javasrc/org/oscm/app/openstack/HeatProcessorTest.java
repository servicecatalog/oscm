/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Aug 1, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.openstack.controller.StackStatus;
import org.oscm.app.openstack.data.Stack;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.data.Template;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.intf.APPTemplateService;

/**
 * @author farmaki
 * 
 */
public class HeatProcessorTest {

    private final HashMap<String, Setting> parameters = new HashMap<>();
    private final HashMap<String, Setting> configSettings = new HashMap<>();
    private final ProvisioningSettings settings = new ProvisioningSettings(
            parameters, configSettings, "en");
    private final PropertyHandler paramHandler = new PropertyHandler(settings);

    private final MockURLStreamHandler streamHandler = new MockURLStreamHandler();

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
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
        paramHandler.setStackId("sID");
    }

    @Test
    public void createStack() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        givenHeatProcessor().createStack(paramHandler);

        // then
        assertEquals("idValue", paramHandler.getStackId());
        assertTrue(paramHandler.getStackName().startsWith(instanceName));
    }

    @Test
    public void createStack_https() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "https");

        // when
        givenHeatProcessor().createStack(paramHandler);

        // then
        assertEquals("idValue", paramHandler.getStackId());
        assertTrue(paramHandler.getStackName().startsWith(instanceName));
    }

    @Test(expected = AbortException.class)
    public void createStack_https_notTemplate() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "https");
        configSettings.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL,
                "estfarmaki2:8880/templates/"));
        // when
        givenAnyHeatProcessor_NoTemplate().createStack(paramHandler);
    }

    @Test()
    public void createStack_https_localTemplate() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "https");
        configSettings.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL,
                "estfarmaki2:8880/templates/"));

        // when
        givenHeatProcessor().createStack(paramHandler);

    }

    @Test
    public void createStack_https_withHttpsTemplate() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "https");
        configSettings.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL,
                "https://objectstorage/v1/templates/"));

        Template t = new Template();
        t.setContent(MockURLStreamHandler.respTemplatesFosi_v2().getBytes());

        final APPTemplateService service = Mockito
                .mock(APPTemplateService.class);
        when(
                service.getTemplate(anyString(), anyString(),
                        any(PasswordAuthentication.class))).thenReturn(t);

        // when
        new HeatProcessor() {
            @Override
            protected APPTemplateService getTemplateService()
                    throws NamingException {
                return service;
            }
        }.createStack(paramHandler);

        // then
        assertEquals("idValue", paramHandler.getStackId());
        assertTrue(paramHandler.getStackName().startsWith(instanceName));
    }

    @Test(expected = HeatException.class)
    public void createStack_connectionFailed() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/stacks", connection);
        streamHandler.put("/stacks/Instance4-1905561714", connection);

        // when
        givenHeatProcessor().createStack(paramHandler);
    }

    @Test
    public void getStackDetails() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        Stack result = givenHeatProcessor().getStackDetails(paramHandler);

        // then
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
        assertEquals("ID", result.getId());
        assertEquals("SSR", result.getStatusReason());
    }

    @Test
    public void getStackDetails_WithoutStatusReason() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put("/stacks/" + instanceName, new MockHttpURLConnection(
                200, MockURLStreamHandler.respStacksInstanceName(null, false)));

        // when
        Stack result = new HeatProcessor().getStackDetails(paramHandler);

        // then
        assertEquals("ID", result.getId());
        assertEquals("n/a", result.getStatusReason());
    }

    @Test
    public void getStackDetails_JSONException() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put("/stacks/" + instanceName, new MockHttpURLConnection(
                200, " "));

        // when
        try {
            givenHeatProcessor().getStackDetails(paramHandler);
            assertTrue("Test must fail with HeatException!", false);
        } catch (HeatException ex) {
            // then
            // then
            assertEquals(
                    "A JSONObject text must begin with '{' at character 0 of ",
                    ex.getMessage());
        }
    }

    @Test
    public void resumeStack_notSuspended() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        boolean result = givenHeatProcessor().resumeStack(paramHandler);

        // then
        assertFalse(result);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void resumeStack() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put(
                "/stacks/" + instanceName,
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_COMPLETE,
                                false)));

        // when
        boolean result = givenHeatProcessor().resumeStack(paramHandler);

        // then
        assertTrue(result);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void resumeStackWithNovaServer() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1");
        streamHandler.put(
                "/stacks/" + instanceName,
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_COMPLETE,
                                false)));
        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.NOVA.getString())));

        // when
        boolean result = givenHeatProcessor().resumeStack(paramHandler);

        // then
        assertTrue(result);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void resumeStackWithTroveInstance() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1");
        streamHandler.put(
                "/stacks/" + instanceName,
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_COMPLETE,
                                false)));
        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.TROVE.getString())));

        // when
        boolean result = givenHeatProcessor().resumeStack(paramHandler);

        // then
        assertTrue(result);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test(expected = InstanceNotAliveException.class)
    public void resumeStack_InstanceNotAliveException_serverIDError()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put(
                "/stacks/" + instanceName,
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_COMPLETE,
                                false)));
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler.respServer(
                        "test", "servId")));

        // when
        givenHeatProcessor().resumeStack(paramHandler);
    }

    @Test(expected = InstanceNotAliveException.class)
    public void resumeStack_InstanceNotAliveException_serverIDNotFoundError()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        List<String> serverNames = new LinkedList<>();
        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        // when
        givenHeatProcessor().resumeStack(paramHandler);
    }

    @Test(expected = HeatException.class)
    public void resumeStack_connectionFailedToGetStack() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/stacks/" + instanceName, connection);

        // when
        givenHeatProcessor().resumeStack(paramHandler);
    }

    @Test(expected = HeatException.class)
    public void resumeStack_connectionFailedToServerDetail() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/servers/0-Instance-server1", connection);

        // when
        givenHeatProcessor().resumeStack(paramHandler);

    }

    @Test(expected = HeatException.class)
    public void resumeStack_connectionFailedToStackResource() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/stacks/" + instanceName + "/resources", connection);

        // when
        givenHeatProcessor().resumeStack(paramHandler);
    }

    @Test(expected = HeatException.class)
    public void resumeStack_connectionFailedToAction() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put(
                "/stacks/" + instanceName,
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_COMPLETE,
                                false)));
        streamHandler.put("/stacks/" + instanceName + "/sID/actions",
                connection);

        // when
        givenHeatProcessor().resumeStack(paramHandler);
    }

    @Test(expected = InstanceNotAliveException.class)
    public void suspendStack_InstanceNotAliveException_serverMissing()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        final List<String> serverNames = new ArrayList<>();
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put(
                "/stacks/Instance4/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        // when
        givenHeatProcessor().suspendStack(paramHandler);
    }

    @Test
    public void suspendStack() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        boolean result = givenHeatProcessor().suspendStack(paramHandler);

        // then
        assertTrue(result);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void suspendStack_alredySuspended() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        streamHandler.put(
                "/stacks/" + instanceName,
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksInstanceName(StackStatus.SUSPEND_COMPLETE,
                                false)));

        // when
        boolean result = givenHeatProcessor().suspendStack(paramHandler);

        // then
        assertFalse(result);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test(expected = RuntimeException.class)
    public void suspendStack_InstanceNotAliveException_serverIdError()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        final List<String> serverNames = Arrays.asList("serv");
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put(
                "/stacks/Instance4/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        // when
        givenHeatProcessor().suspendStack(paramHandler);
    }

    @Test(expected = HeatException.class)
    public void suspendStack_connectionFailed() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());

        streamHandler.put("/stacks/" + instanceName, connection);

        // when
        givenHeatProcessor().suspendStack(paramHandler);
    }

    @Test(expected = HeatException.class)
    public void suspendStack_connectionFailedToAction() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());

        streamHandler.put("/stacks/" + instanceName + "/sID/actions",
                connection);

        // when
        givenHeatProcessor().suspendStack(paramHandler);
    }

    @Test
    public void createStack_JSONException() throws Exception {
        // given
        final String instanceName = "Instance4";
        streamHandler.put("/stacks", new MockHttpURLConnection(200,
                "no valid JSON"));
        streamHandler.put("/stacks/" + instanceName + "-1905561714",
                new MockHttpURLConnection(400, ""));
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        try {
            givenHeatProcessor().createStack(paramHandler);
            assertTrue("Test must fail with HeatException!", false);
        } catch (HeatException ex) {
            // then
            assertEquals(
                    "A JSONObject text must begin with '{' at character 1 of no valid JSON",
                    ex.getMessage());
        }
    }

    @Test
    public void updateStack() throws Exception {
        // given
        createBasicParameters("instanceName", "fosi_v2.json", "http");

        // when
        givenHeatProcessor().updateStack(paramHandler);

        // then
    }

    @Test
    public void updateStack_connectionFailed500() throws Exception {
        // given
        createBasicParameters("instanceName", "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(500,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/stacks/instanceName", connection);

        // when
        HeatProcessor hp = givenAnyHeatProcessor_Error();

        try {
            hp.updateStack(paramHandler);

            fail();
        } catch (HeatException e) {
            // then
            assertEquals(500, e.getResponseCode());
        }
    }

    @Test
    public void updateStack_connectionFailed400() throws Exception {
        // given
        createBasicParameters("instanceName", "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/stacks/instanceName", connection);

        // when
        HeatProcessor hp = givenAnyHeatProcessor_Error();

        try {
            hp.updateStack(paramHandler);

            fail();
        } catch (HeatException e) {
            // then
            assertEquals(400, e.getResponseCode());
        }
    }

    @Test
    public void deleteStack() throws Exception {
        // given
        createBasicParameters("instanceName", "fosi_v2.json", "http");

        // when
        givenHeatProcessor().createStack(paramHandler);
    }

    @Test
    public void deleteStack_connectionFailed() throws Exception {
        // given
        createBasicParameters("instanceName", "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(400,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/stacks/instanceName", connection);

        // when
        HeatProcessor hp = givenAnyHeatProcessor_Error();

        try {
            hp.deleteStack(paramHandler);

            fail();
        } catch (HeatException e) {
            // then
            assertEquals(400, e.getResponseCode());
        }
    }

    private void createBasicParameters(String instanceName,
            String templateName, String httpMethod) {
        parameters.put(PropertyHandler.STACK_NAME, new Setting(
                PropertyHandler.STACK_NAME, instanceName));
        parameters.put(PropertyHandler.TEMPLATE_NAME, new Setting(
                PropertyHandler.TEMPLATE_NAME, templateName));
        parameters.put(PropertyHandler.TEMPLATE_PARAMETER_PREFIX + "KeyName",
                new Setting(PropertyHandler.TEMPLATE_PARAMETER_PREFIX
                        + "KeyName", "key"));
        if (httpMethod == "https") {
            configSettings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                    PropertyHandler.KEYSTONE_API_URL,
                    "https://keystone:8080/v3/auth"));
        } else {

            configSettings.put(PropertyHandler.KEYSTONE_API_URL, new Setting(
                    PropertyHandler.KEYSTONE_API_URL,
                    "http://keystone:8080/v3/auth"));
        }
        configSettings.put(PropertyHandler.DOMAIN_NAME, new Setting(
                PropertyHandler.DOMAIN_NAME, "testDomain"));
        configSettings.put(PropertyHandler.TENANT_ID, new Setting(
                PropertyHandler.TENANT_ID, "testTenantID"));
        configSettings.put(PropertyHandler.API_USER_NAME, new Setting(
                PropertyHandler.API_USER_NAME, "api_user"));
        configSettings.put(PropertyHandler.API_USER_PWD, new Setting(
                PropertyHandler.API_USER_PWD, "secret"));
        configSettings.put(PropertyHandler.TEMPLATE_BASE_URL, new Setting(
                PropertyHandler.TEMPLATE_BASE_URL,
                "http://estfarmaki2:8880/templates/"));
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

    private HeatProcessor givenAnyHeatProcessor_NoTemplate()
            throws AuthenticationException, APPlatformException {
        Template t = new Template();
        t.setContent(MockURLStreamHandler.respTemplatesFosi_v2().getBytes());

        final APPTemplateService service = Mockito
                .mock(APPTemplateService.class);
        when(
                service.getTemplate(anyString(), anyString(),
                        any(PasswordAuthentication.class))).thenReturn(null);

        return new HeatProcessor() {
            @Override
            protected APPTemplateService getTemplateService()
                    throws NamingException {
                return service;
            }
        };
    }

    private HeatProcessor givenAnyHeatProcessor_Error()
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
