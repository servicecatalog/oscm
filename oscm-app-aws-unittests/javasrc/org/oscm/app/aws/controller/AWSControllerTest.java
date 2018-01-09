/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Properties;

import javax.naming.InitialContext;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.app.aws.EC2Communication;
import org.oscm.app.aws.EC2Mockup;
import org.oscm.app.aws.data.FlowState;
import org.oscm.app.aws.data.Operation;
import org.oscm.app.v2_0.data.InstanceDescription;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

import com.amazonaws.services.ec2.AmazonEC2Client;

public class AWSControllerTest extends EJBTestBase {

    private static final String EXCEPTION_MESSAGE = "Exception-Code2309875";
    private static final String INSTANCE_ID = "INSTANCE_1";
    private static final String IMAGE_ID = "IMAGE_1";
    public static final String SUBNET = "subnet";
    public static final String SECURITY_GROUP_NAMES = "security_group1,security_group2";
    public static final String DISK_SIZE = "3";
    public static final String INSTANCE_PLATFORM = "instancePlatform";
    public static final String EAI_INSTANCE_PUBLIC_DNS = "instancePublicDns";
    public static final String SNAPSHOT_ID = "snapshotId";

    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private AWSController aws;
    private AmazonEC2Client ec2;
    private EC2Mockup ec2mock;
    private APPlatformService platformService;

    @Override
    public void setup(TestContainer container) throws Exception {
        platformService = mock(APPlatformService.class);
        enableJndiMock();
        InitialContext context = new InitialContext();
        context.bind(APPlatformService.JNDI_NAME, platformService);

        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings, "en");
        aws = Mockito.spy(new AWSController());

        ec2mock = new EC2Mockup();
        ec2 = ec2mock.getEC2();
        EC2Communication.useMock(ec2);
        PropertyHandler.useMock(null);

        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);
        // new mock
        ec2mock.createDescribeSubnetsResult(SUBNET);
        ec2mock.createDescribeSecurityGroupResult(SUBNET, SECURITY_GROUP_NAMES);
        ec2mock.createDescribeInstancesResult(INSTANCE_ID, "ok", "1.2.3.4");
    }

    private void setValidParameters() {
        parameters.put(PropertyHandler.INSTANCENAME, new Setting(
                PropertyHandler.INSTANCENAME, "name"));
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, "[a-z]{2,25}"));
        parameters.put(PropertyHandler.KEY_PAIR_NAME, new Setting(
                PropertyHandler.KEY_PAIR_NAME, "mySecret"));
        parameters.put(PropertyHandler.IMAGE_NAME, new Setting(
                PropertyHandler.IMAGE_NAME, "ami-12345"));
        parameters.put(PropertyHandler.INSTANCE_TYPE, new Setting(
                PropertyHandler.INSTANCE_TYPE, "m1.small"));
        // new data
        parameters.put(PropertyHandler.DISK_SIZE, new Setting(
                PropertyHandler.DISK_SIZE, DISK_SIZE));
        parameters.put(PropertyHandler.SUBNET, new Setting(
                PropertyHandler.SUBNET, SUBNET));
        parameters.put(PropertyHandler.SECURITY_GROUP_NAMES, new Setting(
                PropertyHandler.SECURITY_GROUP_NAMES, SECURITY_GROUP_NAMES));

    }

    private void mockSettingsWithRuntimeException() {
        settings = mock(ProvisioningSettings.class);
        doThrow(new RuntimeException(EXCEPTION_MESSAGE)).when(settings)
                .getParameters();
    }

    @Test
    public void testCreateUsers() throws Exception {

        assertNull(aws.createUsers(null, settings, null));
    }

    @Test
    public void testUpdateUsers() throws Exception {

        assertNull(aws.updateUsers(null, settings, null));
    }

    @Test
    public void testDeleteUsers() throws Exception {

        assertNull(aws.deleteUsers(null, settings, null));
    }

    @Test
    public void testNotifyInstance() throws Exception {

        assertNull(aws.notifyInstance(null, settings, null));
    }

    @Test
    public void testGetControllerStatus() throws Exception {

        assertNull(aws.getControllerStatus(settings));
    }

    @Test
    public void testGetOperationParameters() throws Exception {

        assertNull(aws.getOperationParameters(null, null, null, null));
    }

    @Test
    public void testCreateInstance() throws Exception {

        // given
        parameters.put(PropertyHandler.INSTANCENAME, new Setting(
                PropertyHandler.INSTANCENAME, "myInstance"));
        parameters.put(PropertyHandler.KEY_PAIR_NAME, new Setting(
                PropertyHandler.KEY_PAIR_NAME, "myKey"));
        parameters.put(PropertyHandler.IMAGE_NAME, new Setting(
                PropertyHandler.IMAGE_NAME, "myImage"));
        parameters.put(PropertyHandler.INSTANCE_TYPE, new Setting(
                PropertyHandler.INSTANCE_TYPE, "m1.tiny"));
        // new data
        parameters.put(PropertyHandler.DISK_SIZE, new Setting(
                PropertyHandler.DISK_SIZE, "3"));
        parameters.put(PropertyHandler.SUBNET, new Setting(
                PropertyHandler.SUBNET, "subnet-a77430d0"));
        parameters.put(PropertyHandler.SECURITY_GROUP_NAMES, new Setting(
                PropertyHandler.SECURITY_GROUP_NAMES,
                "security_group1,security_group2"));

        // when
        InstanceDescription instanceDescription = aws.createInstance(settings);

        // then
        assertNotNull(instanceDescription);
        assertNotNull(instanceDescription.getInstanceId());
        assertTrue(instanceDescription.getRunWithTimer());
        HashMap<String, Setting> changedParameters = instanceDescription
                .getChangedParameters();
        assertNotNull(changedParameters);
        String state = changedParameters.get(PropertyHandler.FLOW_STATE)
                .getValue();
        assertEquals(FlowState.CREATION_REQUESTED.name(), state);
        String operation = changedParameters.get(PropertyHandler.OPERATION)
                .getValue();
        assertEquals(Operation.EC2_CREATION.name(), operation);

        // when
        InstanceStatus status = aws.getInstanceStatus(INSTANCE_ID, settings);
        assertTrue(status.getRunWithTimer());
    }

    @Test(expected = APPlatformException.class)
    public void testCreateInstance_validateNameFail() throws Exception {

        // given
        setValidParameters();
        parameters.put(PropertyHandler.INSTANCENAME, new Setting(
                PropertyHandler.INSTANCENAME, "NAME"));
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, "[a-z]{2,25}"));

        // when
        try {
            aws.createInstance(settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertFalse(e.getLocalizedMessage().startsWith("!"));
            throw e;
        }
    }

    @Test
    public void testCreateInstance_validateOK() throws Exception {

        // given
        setValidParameters();

        // when
        InstanceDescription instanceDescription = aws.createInstance(settings);

        // then
        assertNotNull(instanceDescription);
        assertNotNull(instanceDescription.getInstanceId());
    }

    @Test
    public void testCreateInstance_validateNameEmptyPattern() throws Exception {

        // given
        parameters.put(PropertyHandler.INSTANCENAME, new Setting(
                PropertyHandler.INSTANCENAME, "NAME"));
        parameters.put(PropertyHandler.INSTANCENAME_PATTERN, new Setting(
                PropertyHandler.INSTANCENAME_PATTERN, ""));
        parameters.put(PropertyHandler.KEY_PAIR_NAME, new Setting(
                PropertyHandler.KEY_PAIR_NAME, "myKey"));
        parameters.put(PropertyHandler.IMAGE_NAME, new Setting(
                PropertyHandler.IMAGE_NAME, "myImage"));
        parameters.put(PropertyHandler.INSTANCE_TYPE, new Setting(
                PropertyHandler.INSTANCE_TYPE, "m1.tiny"));
        parameters.put(PropertyHandler.DISK_SIZE, new Setting(
                PropertyHandler.DISK_SIZE, "3"));
        parameters.put(PropertyHandler.SUBNET, new Setting(
                PropertyHandler.SUBNET, "subnettest"));

        // when
        InstanceDescription instanceDescription = aws.createInstance(settings);

        // then
        assertNotNull(instanceDescription);
        assertNotNull(instanceDescription.getInstanceId());
    }

    @Test(expected = APPlatformException.class)
    public void testCreateInstance_validateEmptyName() throws Exception {

        // given
        setValidParameters();
        parameters.remove(PropertyHandler.INSTANCENAME);

        // when
        try {
            aws.createInstance(settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertFalse(e.getLocalizedMessage().startsWith("!"));
            throw e;
        }
    }

    @Test(expected = APPlatformException.class)
    public void testCreateInstance_validateEmptyKeypair() throws Exception {

        // given
        setValidParameters();
        parameters.remove(PropertyHandler.KEY_PAIR_NAME);

        // when
        try {
            aws.createInstance(settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertFalse(e.getLocalizedMessage().startsWith("!"));
            throw e;
        }
    }

    @Test(expected = APPlatformException.class)
    public void testCreateInstance_validateEmptyImageName() throws Exception {

        // given
        setValidParameters();
        parameters.remove(PropertyHandler.IMAGE_NAME);

        // when
        try {
            aws.createInstance(settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertFalse(e.getLocalizedMessage().startsWith("!"));
            throw e;
        }
    }

    @Test(expected = APPlatformException.class)
    public void testCreateInstance_validateEmptyInstanceType() throws Exception {

        // given
        setValidParameters();
        parameters.remove(PropertyHandler.INSTANCE_TYPE);

        // when
        try {
            aws.createInstance(settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertFalse(e.getLocalizedMessage().startsWith("!"));
            throw e;
        }
    }

    @Test
    public void testModifyInstance() throws Exception {

        // when
        InstanceStatus status = aws.modifyInstance(INSTANCE_ID, settings,
                settings);

        // then
        assertNotNull(status);
        assertTrue(status.getRunWithTimer());
        HashMap<String, Setting> changedParameters = status
                .getChangedParameters();
        assertNotNull(changedParameters);
        String state = changedParameters.get(PropertyHandler.FLOW_STATE)
                .getValue();
        assertEquals(FlowState.MODIFICATION_REQUESTED.name(), state);
        String operation = changedParameters.get(PropertyHandler.OPERATION)
                .getValue();
        assertEquals(Operation.EC2_MODIFICATION.name(), operation);
    }

    @Test(expected = APPlatformException.class)
    public void testModfifyInstance_Exception() throws Exception {

        // given
        mockSettingsWithRuntimeException();

        // when
        try {
            aws.modifyInstance(INSTANCE_ID, settings, settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertTrue(e.getLocalizedMessage().contains(EXCEPTION_MESSAGE));
            throw e;
        }
    }

    @Test
    public void testDeleteInstance() throws Exception {

        // when
        InstanceStatus status = aws.deleteInstance(INSTANCE_ID, settings);

        // then
        assertNotNull(status);
        assertTrue(status.getRunWithTimer());
        HashMap<String, Setting> changedParameters = status
                .getChangedParameters();
        assertNotNull(changedParameters);
        String state = changedParameters.get(PropertyHandler.FLOW_STATE)
                .getValue();
        assertEquals(FlowState.DELETION_REQUESTED.name(), state);
        String operation = changedParameters.get(PropertyHandler.OPERATION)
                .getValue();
        assertEquals(Operation.EC2_DELETION.name(), operation);
    }

    @Test(expected = APPlatformException.class)
    public void testDeleteInstance_Exception() throws Exception {

        // given
        mockSettingsWithRuntimeException();

        // when
        try {
            aws.deleteInstance(INSTANCE_ID, settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertTrue(e.getLocalizedMessage().contains(EXCEPTION_MESSAGE));
            throw e;
        }
    }

    @Test
    public void testActivateInstance() throws Exception {

        // when
        InstanceStatus status = aws.activateInstance(INSTANCE_ID, settings);

        // then
        assertNotNull(status);
        assertTrue(status.getRunWithTimer());
        HashMap<String, Setting> changedParameters = status
                .getChangedParameters();
        assertNotNull(changedParameters);
        String state = changedParameters.get(PropertyHandler.FLOW_STATE)
                .getValue();
        assertEquals(FlowState.ACTIVATION_REQUESTED.name(), state);
        String operation = changedParameters.get(PropertyHandler.OPERATION)
                .getValue();
        assertEquals(Operation.EC2_ACTIVATION.name(), operation);
    }

    @Test(expected = APPlatformException.class)
    public void testActivateInstance_Exception() throws Exception {

        // given
        mockSettingsWithRuntimeException();

        // when
        try {
            aws.activateInstance(INSTANCE_ID, settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertTrue(e.getLocalizedMessage().contains(EXCEPTION_MESSAGE));
            throw e;
        }
    }

    @Test
    public void testDeactivateInstance() throws Exception {

        // when
        InstanceStatus status = aws.deactivateInstance(INSTANCE_ID, settings);

        // then
        assertNotNull(status);
        assertTrue(status.getRunWithTimer());
        HashMap<String, Setting> changedParameters = status
                .getChangedParameters();
        assertNotNull(changedParameters);
        String state = changedParameters.get(PropertyHandler.FLOW_STATE)
                .getValue();
        assertEquals(FlowState.DEACTIVATION_REQUESTED.name(), state);
        String operation = changedParameters.get(PropertyHandler.OPERATION)
                .getValue();
        assertEquals(Operation.EC2_ACTIVATION.name(), operation);
    }

    @Test(expected = APPlatformException.class)
    public void testDeactivateInstance_Exception() throws Exception {

        // given
        mockSettingsWithRuntimeException();

        // when
        try {
            aws.deactivateInstance(INSTANCE_ID, settings);
        } catch (APPlatformException e) {

            // then
            assertNotNull(e.getLocalizedMessage());
            assertTrue(e.getLocalizedMessage().contains(EXCEPTION_MESSAGE));
            throw e;
        }
    }

    @Test
    public void testExecuteOperation_unknown() throws Exception {

        // when
        assertNull(aws.executeServiceOperation(null, INSTANCE_ID, "124",
                "unknown", null, settings));
    }

    @Test
    public void testExecuteOperation_start() throws Exception {

        // when
        InstanceStatus status = aws.executeServiceOperation(null, INSTANCE_ID,
                "124", "START_VIRTUAL_SYSTEM", null, settings);

        // then
        assertNotNull(status);
        assertTrue(status.getRunWithTimer());
        HashMap<String, Setting> changedParameters = status
                .getChangedParameters();
        assertNotNull(changedParameters);
        String state = changedParameters.get(PropertyHandler.FLOW_STATE)
                .getValue();
        assertEquals(FlowState.START_REQUESTED.name(), state);
        String operation = changedParameters.get(PropertyHandler.OPERATION)
                .getValue();
        assertEquals(Operation.EC2_OPERATION.name(), operation);
    }

    @Test
    public void testExecuteOperation_stop() throws Exception {

        // when
        InstanceStatus status = aws.executeServiceOperation(null, INSTANCE_ID,
                "124", "STOP_VIRTUAL_SYSTEM", null, settings);

        // then
        assertNotNull(status);
        assertTrue(status.getRunWithTimer());
        HashMap<String, Setting> changedParameters = status
                .getChangedParameters();
        assertNotNull(changedParameters);
        String state = changedParameters.get(PropertyHandler.FLOW_STATE)
                .getValue();
        assertEquals(FlowState.STOP_REQUESTED.name(), state);
        String operation = changedParameters.get(PropertyHandler.OPERATION)
                .getValue();
        assertEquals(Operation.EC2_OPERATION.name(), operation);
    }

    @Test(expected = APPlatformException.class)
    public void testExecuteOperation_Exception() throws Exception {
        // given
        mockSettingsWithRuntimeException();

        // when
        aws.executeServiceOperation(null, INSTANCE_ID, "124",
                "STOP_VIRTUAL_SYSTEM", null, settings);
    }

    @Test
    public void testExecuteOperation_nulls() throws Exception {

        // instanceID NULL
        assertNull(aws.executeServiceOperation(null, null, "124", "unknown",
                null, settings));
        // operationID NULL
        assertNull(aws.executeServiceOperation(null, INSTANCE_ID, "124", null,
                null, settings));
        // settings NULL
        assertNull(aws.executeServiceOperation(null, INSTANCE_ID, "124",
                "unknown", null, null));
    }

    @Test
    public void notifyInstance_Manual() throws Exception {
        // given
        Properties properties = new Properties();
        properties.put("command", "finish");
        settings.getParameters().put(
                PropertyHandler.OPERATION,
                new Setting(PropertyHandler.OPERATION, Operation.EC2_CREATION
                        .toString()));
        settings.getParameters().put(
                PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE, FlowState.MANUAL
                        .toString()));

        // when
        InstanceStatus result = aws.notifyInstance(INSTANCE_ID, settings,
                properties);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.isReady()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.getRunWithTimer()));
        assertEquals(settings.getParameters(), result.getChangedParameters());
    }

    @Test(expected = APPlatformException.class)
    public void notifyInstance_APPlatformException() throws Exception {
        // given
        Properties properties = new Properties();
        properties.put("command", "finish");
        settings.getParameters().put(
                PropertyHandler.OPERATION,
                new Setting(PropertyHandler.OPERATION, Operation.EC2_CREATION
                        .toString()));
        settings.getParameters().put(
                PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE, FlowState.CREATING
                        .toString()));

        // when
        aws.notifyInstance(INSTANCE_ID, settings, properties);
    }

}
