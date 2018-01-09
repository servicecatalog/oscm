/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 14.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.oscm.app.aws.controller.PropertyHandler;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class EC2CommunicationTest {

    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private PropertyHandler ph;
    private EC2Communication ec2comm;
    private EC2Mockup ec2mock;
    private AmazonEC2Client ec2;
    private AWSCredentialsProvider credProvider;
    private ClientConfiguration clientConfig;
    private final String PROXYUSER = "user";
    private final String PROXYPWD = "password";

    @Before
    public void setUp() throws Exception {
        // Reset settings from previous tests
        EC2Communication.useMock(null);
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("https.proxyPassword");
        System.clearProperty("https.proxyUser");

        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.proxyPassword");
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.nonProxyHosts");

        // Define controller settings
        configSettings = new HashMap<>();
        configSettings.put(PropertyHandler.SECRET_KEY_PWD, new Setting(
                PropertyHandler.SECRET_KEY_PWD, "secret_key"));
        configSettings.put(PropertyHandler.ACCESS_KEY_ID_PWD, new Setting(
                PropertyHandler.ACCESS_KEY_ID_PWD, "access_key"));

        // Define parameters
        parameters = new HashMap<>();
        parameters.put(PropertyHandler.REGION, new Setting(
                PropertyHandler.REGION, "test"));
        parameters.put(PropertyHandler.KEY_PAIR_NAME, new Setting(
                PropertyHandler.KEY_PAIR_NAME, "key_pair"));
        parameters.put(PropertyHandler.INSTANCE_TYPE, new Setting(
                PropertyHandler.INSTANCE_TYPE, "type1"));
        parameters.put(PropertyHandler.INSTANCENAME, new Setting(
                PropertyHandler.INSTANCENAME, "name1"));
        // new data
        parameters.put(PropertyHandler.DISK_SIZE, new Setting(
                PropertyHandler.DISK_SIZE, "3"));
        parameters.put(PropertyHandler.SUBNET, new Setting(
                PropertyHandler.SUBNET, "subnet-a77430d0"));
        parameters.put(PropertyHandler.SECURITY_GROUP_NAMES, new Setting(
                PropertyHandler.SECURITY_GROUP_NAMES,
                "security_group1,security_group2"));

        settings = new ProvisioningSettings(parameters, configSettings, "en");
        settings.setOrganizationId("orgId");
        settings.setSubscriptionId("subId");

        PropertyHandler.useMock(null);
        ph = PropertyHandler.withSettings(settings);
        ec2mock = new EC2Mockup();
        ec2 = ec2mock.getEC2();
        ec2comm = new EC2Communication(ph) {
            @Override
            AmazonEC2Client getEC2(AWSCredentialsProvider credentialsProvider,
                    ClientConfiguration clientConfiguration) {
                // Remember given values for asserts
                credProvider = credentialsProvider;
                clientConfig = clientConfiguration;
                return ec2;
            }

        };

    }

    @Test
    public void testNonProxyMatcher() throws Exception {

        String endpoint = "ec2." + ph.getRegion() + ".amazonaws.com";
        testNonProxy(endpoint, null, false);
        testNonProxy(endpoint, "", false);
        testNonProxy(endpoint, "localhost", false);
        testNonProxy(endpoint, "localhost|127.0.0.1", false);
        testNonProxy(endpoint, endpoint, true);
        testNonProxy(endpoint, "ec2*", true);
        testNonProxy(endpoint, "eb2*", false);
        testNonProxy(endpoint, "*.amazonaws.com", true);
        testNonProxy(endpoint, "*.amazonaws.com| ec2.*", true);
        testNonProxy(endpoint, "*.amazonaws.*", true);
        testNonProxy(endpoint, "*.fujitsu.com", false);
        testNonProxy(endpoint, "*.fujitsu.*", false);
    }

    private void testNonProxy(String endpoint, String setting,
            boolean expectation) {

        // given
        setNonProxy(setting);

        // when
        boolean result = ec2comm.isNonProxySet(endpoint);

        // then
        if (expectation) {
            assertTrue(
                    "Expected, that " + setting + " would match " + endpoint,
                    result);
        } else {
            assertFalse("Expected, that " + setting + " would not match "
                    + endpoint, result);
        }
    }

    private void setNonProxy(String value) {
        if (value != null) {
            System.setProperty("http.nonProxyHosts", value);
        } else {
            System.clearProperty("http.nonProxyHosts");
        }
    }

    @Test
    public void testEC2SetupNoProxy() throws Exception {
        // Ask for connection
        AmazonEC2 client = ec2comm.getEC2();
        assertNotNull(client);
        assertNotNull(credProvider);
        assertNotNull(clientConfig);

        assertEquals(-1, clientConfig.getProxyPort());
        assertNull(clientConfig.getProxyHost());
        verify(ec2).setEndpoint("ec2.test.amazonaws.com");

        // Test again for validating cached value
        AmazonEC2 newClient = ec2comm.getEC2();
        assertTrue(client == newClient);

    }

    @Test
    public void testEC2SetupProxy() throws Exception {
        // Define proxy
        System.setProperty("https.proxyHost", "proxy");
        System.setProperty("https.proxyPort", "8080");

        // Ask for connection
        AmazonEC2 client = ec2comm.getEC2();
        assertNotNull(client);
        assertNotNull(credProvider);
        assertNotNull(clientConfig);

        assertEquals(8080, clientConfig.getProxyPort());
        assertEquals("proxy", clientConfig.getProxyHost());

        assertTrue(clientConfig.getProxyUsername() == null
                || clientConfig.getProxyUsername().isEmpty());
        assertTrue(clientConfig.getProxyPassword() == null
                || clientConfig.getProxyPassword().isEmpty());
        verify(ec2).setEndpoint("ec2.test.amazonaws.com");

    }

    @Test
    public void testEC2SetupProxyWithCredentials() throws Exception {
        // Define proxy
        System.setProperty("https.proxyHost", "proxy");
        System.setProperty("https.proxyPort", "8080");
        System.setProperty("https.proxyUser", PROXYUSER);
        System.setProperty("https.proxyPassword", PROXYPWD);

        // Ask for connection
        AmazonEC2 client = ec2comm.getEC2();
        assertNotNull(client);
        assertNotNull(credProvider);
        assertNotNull(clientConfig);
        clientConfig.getProxyUsername();
        assertEquals(8080, clientConfig.getProxyPort());
        assertEquals("proxy", clientConfig.getProxyHost());
        assertEquals(PROXYUSER, clientConfig.getProxyUsername());
        assertEquals(PROXYPWD, clientConfig.getProxyPassword());
        verify(ec2).setEndpoint("ec2.test.amazonaws.com");

    }

    @Test
    public void testEC2SetupProxyWithEmptyCredentials() throws Exception {
        // Define proxy
        System.setProperty("https.proxyHost", "proxy");
        System.setProperty("https.proxyPort", "8080");
        System.setProperty("https.proxyUser", "");
        System.setProperty("https.proxyPassword", "");

        // Ask for connection
        AmazonEC2 client = ec2comm.getEC2();
        assertNotNull(client);
        assertNotNull(credProvider);
        assertNotNull(clientConfig);

        assertEquals(8080, clientConfig.getProxyPort());
        assertEquals("proxy", clientConfig.getProxyHost());

        assertTrue(clientConfig.getProxyUsername() == null
                || clientConfig.getProxyUsername().isEmpty());
        assertTrue(clientConfig.getProxyPassword() == null
                || clientConfig.getProxyPassword().isEmpty());
        verify(ec2).setEndpoint("ec2.test.amazonaws.com");

    }

    @Test
    public void testEC2SetupNonProxy() throws Exception {
        // Define proxy
        System.setProperty("https.proxyHost", "proxy");
        System.setProperty("https.proxyPort", "8080");
        // But set endpoint as non proxy
        System.setProperty("http.nonProxyHosts", "*.amazonaws.com");

        // Ask for connection
        AmazonEC2 client = ec2comm.getEC2();
        assertNotNull(client);
        assertNotNull(credProvider);
        assertNotNull(clientConfig);

        assertEquals(8080, clientConfig.getProxyPort());
        assertTrue(clientConfig.getProxyUsername() == null
                || clientConfig.getProxyUsername().isEmpty());
        assertTrue(clientConfig.getProxyPassword() == null
                || clientConfig.getProxyPassword().isEmpty());
        verify(ec2).setEndpoint("ec2.test.amazonaws.com");

    }

    @Test
    public void testEC2SetupMock() throws Exception {
        // Set stub
        EC2Communication.useMock(ec2);

        // Get client
        EC2Communication realEC2 = new EC2Communication(ph);

        // Ask for connection
        AmazonEC2 client = realEC2.getEC2();

        // Stub returned?
        assertTrue(client == ec2);
    }

    @Test
    public void testAWSCredentials() throws Exception {
        // Ask for connection
        AmazonEC2 client = ec2comm.getEC2();
        assertNotNull(client);
        assertNotNull(credProvider);

        AWSCredentials credentials = credProvider.getCredentials();
        assertEquals("access_key", credentials.getAWSAccessKeyId());
        assertEquals("secret_key", credentials.getAWSSecretKey());

        credProvider.refresh();
        credentials = credProvider.getCredentials();
        assertEquals("access_key", credentials.getAWSAccessKeyId());
        assertEquals("secret_key", credentials.getAWSSecretKey());

    }

    @Test
    public void testResolveAMIFound() throws Exception {
        ec2mock.createDescribeImagesResult("image1");
        Image result = ec2comm.resolveAMI("image1");
        assertEquals("image1", result.getImageId());

        ArgumentCaptor<DescribeImagesRequest> argCaptor = ArgumentCaptor
                .forClass(DescribeImagesRequest.class);
        verify(ec2).describeImages(argCaptor.capture());
        DescribeImagesRequest dir = argCaptor.getValue();
        for (Filter filter : dir.getFilters()) {
            if (filter.getName().equals("name")) {
                assertEquals("image1", filter.getValues().get(0));
            }
        }
    }

    @Test(expected = APPlatformException.class)
    public void testResolveAMINotFound() throws Exception {
        ec2mock.createDescribeImagesResult();
        ec2comm.resolveAMI("image1");
    }

    @Test
    public void testCreateInstance() throws Exception {
        ec2mock.createRunInstancesResult("instance1");
        ec2mock.createDescribeImagesResult("image1");
        ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
        ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                "security_group1,security_group2");
        ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
        Image image = ec2comm.resolveAMI("image1");
        ec2comm.createInstance(image);
        String result = ph.getAWSInstanceId();
        assertEquals("instance1", result);
        ArgumentCaptor<RunInstancesRequest> arg1 = ArgumentCaptor
                .forClass(RunInstancesRequest.class);
        verify(ec2).runInstances(arg1.capture());
        RunInstancesRequest rir = arg1.getValue();
        assertEquals("image1", rir.getImageId());
        assertEquals("type1", rir.getInstanceType());
        assertEquals("key_pair", rir.getKeyName());
        assertEquals(1, rir.getMinCount().intValue());
        assertEquals(1, rir.getMaxCount().intValue());

        ArgumentCaptor<CreateTagsRequest> arg2 = ArgumentCaptor
                .forClass(CreateTagsRequest.class);
        verify(ec2).createTags(arg2.capture());
        CreateTagsRequest ctr = arg2.getValue();
        for (Tag t : ctr.getTags()) {
            if (t.getKey().equalsIgnoreCase("Name")) {
                assertEquals("name1", t.getValue());
            } else if (t.getKey().equalsIgnoreCase("SubscriptionId")) {
                assertEquals("subId", t.getValue());
            } else if (t.getKey().equalsIgnoreCase("OrganizationId")) {
                assertEquals("orgId", t.getValue());
            }
        }

        parameters.put("USERDATA_URL", new Setting("USERDATA_URL", "userdata"));

    }

    @Test(expected = APPlatformException.class)
    public void testCreateInstanceInvalid() throws Exception {
        // Missing instance id
        ec2mock.createRunInstancesResult();
        ec2mock.createDescribeImagesResult("image1");
        ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
        ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                "security_group1, security_group2");
        Image image = ec2comm.resolveAMI("image1");
        ec2comm.createInstance(image);
    }

    @Test
    public void testCreateInstanceUserData() throws Exception {
        String userData = "line1\nline2\n";
        String userDataBase64 = Base64.encodeBase64String(userData.getBytes());
        File myFile = createUserDataFile(userData);
        try {
            URL fileUrl = myFile.toURI().toURL();
            parameters.put(PropertyHandler.USERDATA_URL, new Setting(
                    PropertyHandler.USERDATA_URL, fileUrl.toString()));
            ec2mock.createRunInstancesResult("instance2");
            ec2mock.createDescribeImagesResult("image1");
            ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
            ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                    "security_group1,security_group2");
            ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
            Image image = ec2comm.resolveAMI("image1");

            ec2comm.createInstance(image);
            String result = ph.getAWSInstanceId();
            assertEquals("instance2", result);
            ArgumentCaptor<RunInstancesRequest> arg1 = ArgumentCaptor
                    .forClass(RunInstancesRequest.class);
            verify(ec2).runInstances(arg1.capture());
            RunInstancesRequest rir = arg1.getValue();
            assertEquals(userDataBase64, rir.getUserData());
        } finally {
            myFile.delete();
        }
    }

    private File createUserDataFile(String content) throws IOException {
        File tmpFile = File.createTempFile("userdata", ".tmp");
        FileWriter fw = new FileWriter(tmpFile);
        try {
            fw.write(content);
        } finally {
            fw.close();
        }
        return tmpFile;
    }

    @Test
    public void testCreateInstanceUserDataMalformedUrl() throws Exception {
        try {
            parameters.put(PropertyHandler.USERDATA_URL, new Setting(
                    PropertyHandler.USERDATA_URL, "MALFORMED_URL"));
            ec2mock.createRunInstancesResult("instance2");
            ec2mock.createDescribeImagesResult("image1");
            ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
            ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                    "security_group1,security_group2");
            ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
            Image image = ec2comm.resolveAMI("image1");
            ec2comm.createInstance(image);

            assertTrue("Exception expected", false);

        } catch (APPlatformException ape) {
            assertTrue(ape.getMessage(),
                    ape.getMessage().contains("MALFORMED_URL"));
        }
    }

    @Test
    public void testCreateInstanceUserDataEmptyUrl() throws Exception {
        parameters.put(PropertyHandler.USERDATA_URL, new Setting(
                PropertyHandler.USERDATA_URL, ""));
        ec2mock.createRunInstancesResult("instance3");
        ec2mock.createDescribeImagesResult("image1");
        ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
        ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                "security_group1,security_group2");
        ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
        Image image = ec2comm.resolveAMI("image1");
        ec2comm.createInstance(image);
        String result = ph.getAWSInstanceId();
        assertEquals("instance3", result);
    }

    @Test
    public void testCreateInstanceUserDataInvalidPath() throws Exception {
        File myFile = createUserDataFile("test123");
        try {
            URL fileUrl = myFile.toURI().toURL();
            parameters.put(PropertyHandler.USERDATA_URL, new Setting(
                    PropertyHandler.USERDATA_URL, fileUrl.toString()
                            + "_notexisting"));
            ec2mock.createRunInstancesResult("instance2");
            ec2mock.createDescribeImagesResult("image1");
            ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
            ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                    "security_group1,security_group2");
            ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
            Image image = ec2comm.resolveAMI("image1");
            ec2comm.createInstance(image);
            assertTrue("Exception expected", false);

        } catch (APPlatformException ape) {
            assertTrue("Error message not as expected", ape.getMessage()
                    .contains("cannot find the file")
                    || ape.getMessage().contains("No such file"));
        } finally {
            myFile.delete();
        }
    }

    @Test
    public void testCreateInstanceSecurityGroups() throws Exception {
        parameters.put(PropertyHandler.SECURITY_GROUP_NAMES, new Setting(
                PropertyHandler.SECURITY_GROUP_NAMES, "security_group"));
        ec2mock.createRunInstancesResult("instance3");

        ec2mock.createDescribeImagesResult("image1");
        ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
        ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                "security_group");
        ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
        Image image = ec2comm.resolveAMI("image1");
        ec2comm.createInstance(image);
        String result = ph.getAWSInstanceId();
        assertEquals("instance3", result);

        ArgumentCaptor<RunInstancesRequest> arg1 = ArgumentCaptor
                .forClass(RunInstancesRequest.class);
        verify(ec2).runInstances(arg1.capture());
        RunInstancesRequest rir = arg1.getValue();
        // Network interfaces and an instance-level security groups may not be
        // specified on the same request..
        assertEquals(1, rir.getNetworkInterfaces().get(0).getGroups().size());
        /*
         * assertEquals("security_group",
         * rir.getNetworkInterfaces().get(0).getGroups().get(0));
         */
    }

    @Test
    public void testCreateInstanceSecurityGroupsMultiple() throws Exception {
        parameters.put(PropertyHandler.SECURITY_GROUP_NAMES, new Setting(
                PropertyHandler.SECURITY_GROUP_NAMES,
                "security_group1, security_group2"));
        ec2mock.createRunInstancesResult("instance3");
        ec2mock.createDescribeImagesResult("image1");
        ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
        ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                "security_group1,security_group2");
        ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
        Image image = ec2comm.resolveAMI("image1");
        ec2comm.createInstance(image);
        String result = ph.getAWSInstanceId();
        assertEquals("instance3", result);

        ArgumentCaptor<RunInstancesRequest> arg1 = ArgumentCaptor
                .forClass(RunInstancesRequest.class);
        verify(ec2).runInstances(arg1.capture());
        RunInstancesRequest rir = arg1.getValue();
        // Network interfaces and an instance-level security groups may not be
        // specified on the same request..
        assertEquals(2, rir.getNetworkInterfaces().get(0).getGroups().size());
        // assertEquals("security_group1", rir.getSecurityGroups().get(0));
        // assertEquals("security_group2", rir.getSecurityGroups().get(1));
    }

    @Test
    public void testTerminateInstance() throws Exception {
        ec2comm.terminateInstance("instance1");
        ArgumentCaptor<TerminateInstancesRequest> arg1 = ArgumentCaptor
                .forClass(TerminateInstancesRequest.class);
        verify(ec2).terminateInstances(arg1.capture());
        TerminateInstancesRequest val = arg1.getValue();

        assertEquals(1, val.getInstanceIds().size());
        assertEquals("instance1", val.getInstanceIds().get(0));
    }

    @Test
    public void testStartInstance() throws Exception {
        ec2comm.startInstance("instance1");
        ArgumentCaptor<StartInstancesRequest> arg1 = ArgumentCaptor
                .forClass(StartInstancesRequest.class);
        verify(ec2).startInstances(arg1.capture());
        StartInstancesRequest val = arg1.getValue();

        assertEquals(1, val.getInstanceIds().size());
        assertEquals("instance1", val.getInstanceIds().get(0));
    }

    @Test
    public void testStopInstance() throws Exception {
        ec2comm.stopInstance("instance1");
        ArgumentCaptor<StopInstancesRequest> arg1 = ArgumentCaptor
                .forClass(StopInstancesRequest.class);
        verify(ec2).stopInstances(arg1.capture());
        StopInstancesRequest val = arg1.getValue();

        assertEquals(1, val.getInstanceIds().size());
        assertEquals("instance1", val.getInstanceIds().get(0));
    }

    @Test
    public void testGetInstanceState() throws Exception {
        ec2mock.createDescribeInstancesResult("instance1", "ok", "1.2.3.4");
        String state = ec2comm.getInstanceState("instance1");
        assertEquals("ok", state);

        String dns = ec2comm.getPublicDNS("instance1");
        assertEquals("1.2.3.4", dns);
    }

    @Test
    public void testGetInstanceStateEmpty() throws Exception {
        DescribeInstancesResult instancesResult = new DescribeInstancesResult();
        doReturn(instancesResult).when(ec2).describeInstances(
                Matchers.any(DescribeInstancesRequest.class));
        String state = ec2comm.getInstanceState("instance1");
        assertNull(state);

        String dns = ec2comm.getPublicDNS("instance1");
        assertNull(dns);

    }

    @Test
    public void testGetInstanceStateEmptyReservation() throws Exception {
        DescribeInstancesResult instancesResult = new DescribeInstancesResult()
                .withReservations(new Reservation());
        doReturn(instancesResult).when(ec2).describeInstances(
                Matchers.any(DescribeInstancesRequest.class));
        String state = ec2comm.getInstanceState("instance1");
        assertNull(state);

        String dns = ec2comm.getPublicDNS("instance1");
        assertNull(dns);
    }

    @Test
    public void testIsInstanceReady() throws Exception {
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        boolean ready = ec2comm.isInstanceReady("instance1");
        assertTrue(ready);
    }

    @Test
    public void testIsInstanceNotReady1() throws Exception {
        ec2mock.createDescribeInstanceStatusResult("instance1", "nok", "nok",
                "ok");
        boolean ready = ec2comm.isInstanceReady("instance1");
        assertFalse(ready);
    }

    @Test
    public void testIsInstanceNotReady2() throws Exception {
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "nok");
        boolean ready = ec2comm.isInstanceReady("instance1");
        assertFalse(ready);
    }

    @Test
    public void testResolveSubnet() throws Exception {
        ec2mock.createDescribeSubnetsResult("subnet-a77430d0");
        Subnet subnet = ec2comm.resolveSubnet("subnet-a77430d0");
        assertNotNull(subnet);
        assertEquals("subnet-a77430d0", subnet.getSubnetId());
    }

    @Test
    public void testSubnentNotFound() throws Exception {
        ec2mock.createDescribeSubnetsResult("Testsubnet-a77430d0");
        Subnet subnet = ec2comm.resolveSubnet("subnet-a77430d0");
        assertFalse("subnet-a77430d0".equals(subnet.getSubnetId()));
    }

    @Test
    public void testResolveSecurityGroups() throws Exception {
        ec2mock.createDescribeSecurityGroupResult("subnet-a77430d0",
                "security_group1,security_group2");
        Collection<String> securityGroupNames = ph.getSecurityGroups();
        Collection<String> groupId = ec2comm.resolveSecurityGroups(
                securityGroupNames, "subnet-a77430d0");
        assertNotNull(groupId);

    }

}
