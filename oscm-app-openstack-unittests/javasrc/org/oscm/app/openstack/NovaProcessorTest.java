/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016/10/06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.openstack.controller.PropertyHandler;
import org.oscm.app.openstack.controller.ServerStatus;
import org.oscm.app.openstack.data.Server;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;

/**
 * @author tateiwamext
 * 
 */
public class NovaProcessorTest {

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
        paramHandler.setStackId("sID");
    }

    @Test
    public void getServersDetails() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        List<Server> result = new NovaProcessor()
                .getServersDetails(paramHandler, false);

        // then
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getId(), "0-Instance-server1");
        assertEquals(result.get(0).getName(), "server1");
        assertEquals(result.get(0).getStatus(), ServerStatus.ACTIVE.name());
    }

    @Test
    public void getServersDetails_moreInfo() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        List<Server> result = new NovaProcessor()
                .getServersDetails(paramHandler, true);

        // then
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
        assertEquals(1, result.size());
        assertEquals("0-Instance-server1", result.get(0).getId());
        assertEquals("server1", result.get(0).getName());
        assertEquals(ServerStatus.ACTIVE.name(), result.get(0).getStatus());
        assertEquals("S-1", result.get(0).getType());
        assertEquals(Arrays.asList("133.162.161.216"),
                result.get(0).getPublicIP());
        assertEquals(Arrays.asList("192.168.0.4"),
                result.get(0).getPrivateIP());
    }

    @Test
    public void getServersDetails_withMultiVms() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        List<Server> result = new NovaProcessor()
                .getServersDetails(paramHandler, false);

        // then
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), "0-Instance-server1");
        assertEquals(result.get(0).getName(), "server1");
        assertEquals(result.get(0).getStatus(), ServerStatus.ACTIVE.name());
        assertEquals(result.get(1).getId(), "1-Instance-otherserver2");
        assertEquals(result.get(1).getName(), "otherserver2");
        assertEquals(result.get(1).getStatus(), ServerStatus.ACTIVE.name());
    }

    @Test
    public void getServersDetails_withMultiVms_faild401_and_retry()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(401,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2", connection2);
        streamHandler.put(
                "/servers/1-Instance-otherserver2/2",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        List<Server> result = new NovaProcessor()
                .getServersDetails(paramHandler, false);

        // then
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), "0-Instance-server1");
        assertEquals(result.get(0).getName(), "server1");
        assertEquals(result.get(0).getStatus(), ServerStatus.ACTIVE.name());
        assertEquals(result.get(1).getId(), "1-Instance-otherserver2");
        assertEquals(result.get(1).getName(), "otherserver2");
        assertEquals(result.get(1).getStatus(), ServerStatus.ACTIVE.name());
    }

    @Test
    public void getServersDetails_withMultiVms_faild401_twice()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(401,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2", connection2);

        // when
        List<Server> result = new NovaProcessor()
                .getServersDetails(paramHandler, false);

        // then
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), "0-Instance-server1");
        assertEquals(result.get(0).getName(), "server1");
        assertEquals(result.get(0).getStatus(), ServerStatus.ACTIVE.name());
        assertEquals(result.get(1).getId(), "1-Instance-otherserver2");
        assertEquals(result.get(1).getName(), "");
        assertEquals(result.get(1).getStatus(),
                ServerStatus.UNKNOWN.toString());
    }

    @Test(expected = InstanceNotAliveException.class)
    public void getServersDetails_InstanceNotAliveException_serverMissing()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = new LinkedList<>();

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));

        // when
        new NovaProcessor().getServersDetails(paramHandler, false);
    }

    @Test
    public void getServersDetails_missingVM() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerDetail(null, null, null, null));
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2", connection2);

        // when
        List<Server> result = new NovaProcessor()
                .getServersDetails(paramHandler, false);

        // then
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getId(), "0-Instance-server1");
        assertEquals(result.get(0).getName(), "server1");
        assertEquals(result.get(0).getStatus(), ServerStatus.ACTIVE.name());
        assertEquals(result.get(1).getId(), "1-Instance-otherserver2");
        assertEquals(result.get(1).getName(), "");
        assertEquals(result.get(1).getStatus(),
                ServerStatus.UNKNOWN.toString());
    }

    @Test
    public void startInstances() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE)); // All values are TRUE
        assertEquals(1, result.size());
        assertTrue(result.containsKey("0-Instance-server1"));
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void startInstances_withMultiVMs() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put(
                "/servers/1-Instance-otherserver2/action",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerActions()));
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

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE)); // All values are TRUE
        assertEquals(2, result.size());
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void startInstances_lastVMFaild() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
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
        MockHttpURLConnection connection2 = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2/action",
                connection2);

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-otherserver2"), Boolean.FALSE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void startInstances_secoundVMFaild() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "missingServer2", "otherServer");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));
        streamHandler.put(
                "/servers/1-Instance-missingServer2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("missingServer2", "missingServer2",
                                ServerStatus.SHUTOFF, "testTenantID")));
        streamHandler.put(
                "/servers/2-Instance-otherServer",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherServer",
                                "2-Instance-otherServer", ServerStatus.SHUTOFF,
                                "testTenantID")));

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-missingServer2/action",
                connection2);
        streamHandler.put(
                "/servers/2-Instance-otherServer/action",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerActions()));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 3);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-missingServer2"));
        assertTrue(result.containsKey("2-Instance-otherServer"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-missingServer2"), Boolean.FALSE);
        assertEquals(result.get("2-Instance-otherServer"), Boolean.TRUE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void startInstances_lastVMFaild401_and_retry() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(401,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());
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
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2/action",
                connection2);

        streamHandler.put(
                "/servers/1-Instance-otherserver2/action/2",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerActions()));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-otherserver2"), Boolean.TRUE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void startInstances_lastVMFaild401_twice() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(401,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());
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
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2/action",
                connection2);

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-otherserver2"), Boolean.FALSE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void startInstancesWithNovaServer() throws Exception {
        // given
        final String instanceName = "Instance4";
        final List<String> serverNames = Arrays.asList("server1");
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.NOVA.getString())));
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));
        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE)); // All values are TRUE
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void startInstancesWithTroveInstance() throws Exception {
        // given
        final String instanceName = "Instance4";
        final List<String> serverNames = Arrays.asList("server1");
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.TROVE.getString())));
        streamHandler.put(
                "/servers/0-Instance-server1",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("server1", "0-Instance-server1",
                                ServerStatus.SHUTOFF, "testTenantID")));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .startInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE)); // All values are TRUE
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test(expected = InstanceNotAliveException.class)
    public void startInstances_InstanceNotAliveException_serverMissing()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        List<String> serverNames = new LinkedList<>();
        streamHandler
                .put("/stacks/" + instanceName + "/resources",
                        new MockHttpURLConnection(200, MockURLStreamHandler
                                .respStacksResources(serverNames,
                                        "AWS::EC2::Instance")));

        // when
        new NovaProcessor().startInstances(paramHandler);
    }

    @Test(expected = HeatException.class)
    public void startInstances_connectionFailedToGetServerIds()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        MockHttpURLConnection connection = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection.setIOException(new IOException());
        streamHandler.put("/stacks/" + instanceName + "/resources", connection);

        // when
        new NovaProcessor().startInstances(paramHandler);
    }

    @Test(expected = APPlatformException.class)
    public void startInstances_APPlatformException_serverAllStarted()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        new NovaProcessor().startInstances(paramHandler);
    }

    @Test(expected = InstanceNotAliveException.class)
    public void stopInstances_InstanceNotAliveException_serverMissing()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        List<String> serverNames = new LinkedList<>();
        streamHandler
                .put("/stacks/" + instanceName + "/resources",
                        new MockHttpURLConnection(200, MockURLStreamHandler
                                .respStacksResources(serverNames,
                                        "AWS::EC2::Instance")));
        // when
        new NovaProcessor().stopInstances(paramHandler);
    }

    @Test
    public void stopInstances() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .stopInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE)); // All values are TRUE
        assertEquals(1, result.size());
        assertTrue(result.containsKey("0-Instance-server1"));
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void stopInstances_withMultiVMs() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put(
                "/servers/1-Instance-otherserver2/action",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerActions()));
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .stopInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE)); // All values are TRUE
        assertEquals(2, result.size());
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void stopInstances_lastVMFaild() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2/action",
                connection2);
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ERROR,
                                "testTenantID")));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .stopInstances(paramHandler);

        // then
        assertTrue(result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-otherserver2"), Boolean.FALSE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void stopInstances_secoundVMFaild() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "missingServer2", "otherServer");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(404,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-missingServer2/action",
                connection2);
        streamHandler.put(
                "/servers/2-Instance-otherServer/action",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerActions()));
        streamHandler.put(
                "/servers/1-Instance-missingServer2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("missingServer2",
                                "1-Instance-missingServer2",
                                ServerStatus.ACTIVE, "testTenantID")));
        streamHandler.put(
                "/servers/2-Instance-otherServer",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherServer",
                                "2-Instance-otherServer", ServerStatus.ACTIVE,
                                "testTenantID")));
        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .stopInstances(paramHandler);

        // then
        assertTrue(result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 3);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-missingServer2"));
        assertTrue(result.containsKey("2-Instance-otherServer"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-missingServer2"), Boolean.FALSE);
        assertEquals(result.get("2-Instance-otherServer"), Boolean.TRUE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void stopInstances_lastVMFaild401_and_retry() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(401,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2/action",
                connection2);

        streamHandler.put(
                "/servers/1-Instance-otherserver2/action/2",
                new MockHttpURLConnection(202, MockURLStreamHandler
                        .respServerActions()));
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .stopInstances(paramHandler);

        // then
        assertTrue(!result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-otherserver2"), Boolean.TRUE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test
    public void stopInstances_lastVMFaild401_twice() throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");
        MockHttpURLConnection connection2 = new MockHttpURLConnection(401,
                MockURLStreamHandler.respServerActions());
        connection2.setIOException(new IOException());

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
        streamHandler.put("/servers/1-Instance-otherserver2/action",
                connection2);
        streamHandler.put(
                "/servers/1-Instance-otherserver2",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respServerDetail("otherserver2",
                                "1-Instance-otherserver2", ServerStatus.ACTIVE,
                                "testTenantID")));

        // when
        HashMap<String, Boolean> result = new NovaProcessor()
                .stopInstances(paramHandler);

        // then
        assertTrue(result.containsValue(Boolean.FALSE));
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("0-Instance-server1"));
        assertTrue(result.containsKey("1-Instance-otherserver2"));
        assertEquals(result.get("0-Instance-server1"), Boolean.TRUE);
        assertEquals(result.get("1-Instance-otherserver2"), Boolean.FALSE);
        assertEquals("sID", paramHandler.getStackId());
        assertEquals(instanceName, paramHandler.getStackName());
    }

    @Test(expected = APPlatformException.class)
    public void stopInstances_APPlatformException_serverAllStopped()
            throws Exception {
        // given
        final String instanceName = "Instance4";
        createBasicParameters(instanceName, "fosi_v2.json", "http");
        final List<String> serverNames = Arrays.asList("server1",
                "otherserver2");

        streamHandler.put(
                "/stacks/" + instanceName + "/resources",
                new MockHttpURLConnection(200, MockURLStreamHandler
                        .respStacksResources(serverNames,
                                InstanceType.EC2.getString())));
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

        // when
        new NovaProcessor().stopInstances(paramHandler);
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
}
