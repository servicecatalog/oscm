/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import javax.naming.InitialContext;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oscm.app.aws.EC2Communication;
import org.oscm.app.aws.EC2Mockup;
import org.oscm.app.aws.data.FlowState;
import org.oscm.app.aws.data.Operation;
import org.oscm.app.aws.i18n.Messages;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class AWSControllerIT extends EJBTestBase {

    private static final String INSTANCE_ID = "INSTANCE-123";
    private static final String IMAGE_ID = "IMAGE-123";

    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> configSettings;
    private ProvisioningSettings settings;
    private APPlatformController aws;
    private APPlatformService platformService;
    public static final String SUBNET = "subnet";
    public static final String SECURITY_GROUP_NAMES = "security_group1,security_group2";
    private EC2Mockup ec2mock;
    private AmazonEC2Client ec2;

    @Override
    protected void setup(TestContainer container) throws Exception {
        platformService = mock(APPlatformService.class);
        enableJndiMock();
        InitialContext context = new InitialContext();
        context.bind(APPlatformService.JNDI_NAME, platformService);
        parameters = new HashMap<>();
        configSettings = new HashMap<>();
        settings = new ProvisioningSettings(parameters, configSettings,
                Messages.DEFAULT_LOCALE);

        container.addBean(new AWSController());
        aws = container.get(APPlatformController.class);

        ec2mock = new EC2Mockup();
        ec2 = ec2mock.getEC2();
        EC2Communication.useMock(ec2);

        PropertyHandler ph = new PropertyHandler(settings) {
            @Override
            public <T> T getWebService(java.lang.Class<T> serviceClass)
                    throws Exception {
                return mock(serviceClass);
            };
        };
        PropertyHandler.useMock(ph);
    }

    @Test
    public void createInstance() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.CREATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_CREATION.name()));

        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);

        ec2mock.addDescribeInstancesResult(INSTANCE_ID, "pending", null);
        ec2mock.addDescribeInstanceStatusResult(INSTANCE_ID, "pending",
                "initializing", "initializing");

        ec2mock.addDescribeInstancesResult(INSTANCE_ID, "running", null);
        ec2mock.addDescribeInstanceStatusResult(INSTANCE_ID, "running", "ok",
                "ok");

        ec2mock.addDescribeInstancesResult(INSTANCE_ID, "running",
                "2aws-1-2-3-4");
        ec2mock.addDescribeInstancesResult(INSTANCE_ID, "ok", "1.2.3.4");

        runUntilReady();

        ArgumentCaptor<DescribeImagesRequest> argCapImages = ArgumentCaptor
                .forClass(DescribeImagesRequest.class);
        verify(ec2).describeImages(argCapImages.capture());
    }

    @Test(expected = SuspendException.class)
    public void createInstance_Authfailed() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.CREATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_CREATION.name()));

        AmazonServiceException ase = new AmazonServiceException("Test message");
        ase.setErrorCode("AuthFailure");
        ec2mock.createDescribeImagesException(ase);

        runUntilReady();
    }

    @Test(expected = SuspendException.class)
    public void createInstance_AuthorizationFailed() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.CREATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_CREATION.name()));

        AmazonServiceException ase = new AmazonServiceException("Test message");
        ase.setErrorCode("UnauthorizedOperation");
        ec2mock.createDescribeImagesException(ase);

        runUntilReady();
    }

    @Test(expected = AbortException.class)
    public void createInstance_AmazonServiceException() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.CREATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_CREATION.name()));

        AmazonServiceException ase = new AmazonServiceException("Test message");
        ase.setErrorCode("Unknown1234");
        ec2mock.createDescribeImagesException(ase);

        runUntilReady();
    }

    // currently modify is not implemented
    @Ignore
    @Test(expected = AbortException.class)
    public void modifyInstance_AmazonServiceException() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.MODIFICATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_MODIFICATION.name()));

        AmazonServiceException ase = new AmazonServiceException("Test message");
        ase.setErrorCode("Unknown1234");
        ec2mock.createDescribeImagesException(ase);

        runUntilReady();
    }

    @Test(expected = SuspendException.class)
    public void createInstance_AmazonClientException() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.CREATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_CREATION.name()));

        AmazonClientException ase = new AmazonClientException("Test message");
        ec2mock.createDescribeImagesException(ase);

        runUntilReady();
    }

    @Test(expected = AbortException.class)
    public void executeServiceOperation_AmazonServiceException()
            throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE, new Setting(
                PropertyHandler.FLOW_STATE, FlowState.START_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_OPERATION.name()));

        AmazonServiceException ase = new AmazonServiceException("Test message");
        ase.setErrorCode("Unknown1234");
        ec2mock.createStartInstanceException(ase);

        runUntilReady();
    }

    @Test
    public void deleteInstance() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.DELETION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_DELETION.name()));

        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);

        ec2mock.addDescribeInstancesResult(INSTANCE_ID, "pending",
                "2aws-1-2-3-4");
        ec2mock.addDescribeInstanceStatusResult(INSTANCE_ID, "pending",
                "initializing", "initializing");

        ec2mock.addDescribeInstancesResult(INSTANCE_ID, "terminated",
                "2aws-1-2-3-4");
        ec2mock.addDescribeInstanceStatusResult(INSTANCE_ID, "terminated",
                "ok", "ok");

        ec2mock.addDescribeInstancesResult(INSTANCE_ID, "terminated", null);

        runUntilReady();

        ArgumentCaptor<TerminateInstancesRequest> argCapImages = ArgumentCaptor
                .forClass(TerminateInstancesRequest.class);
        verify(ec2).terminateInstances(argCapImages.capture());

    }

    private void runUntilReady() throws Exception {
        LinkedList<InstanceStatus> status = new LinkedList<>();
        boolean ready = false;
        int i = 0;
        do {
            status.add(getInstanceStatus());
            ready = status.getLast().isReady();
        } while (!ready && i++ < 20);
        assertTrue(ready);
    }

    @Test
    public void modifyInstance() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.MODIFICATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_MODIFICATION.name()));

        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);

        runUntilReady();

    }

    @Test
    public void activateInstance() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.ACTIVATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_ACTIVATION.name()));

        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);

        runUntilReady();

        verify(ec2).startInstances(any(StartInstancesRequest.class));

    }

    @Test
    public void deactivateInstance() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE,
                new Setting(PropertyHandler.FLOW_STATE,
                        FlowState.DEACTIVATION_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_ACTIVATION.name()));

        ec2mock.createDescribeInstancesResult("instance1", "stopped", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);

        runUntilReady();

        verify(ec2).stopInstances(any(StopInstancesRequest.class));

    }

    @Test
    public void executeServiceOperation_Start() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE, new Setting(
                PropertyHandler.FLOW_STATE, FlowState.START_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_OPERATION.name()));

        ec2mock.createDescribeInstancesResult("instance1", "running", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);

        runUntilReady();

        verify(ec2).startInstances(any(StartInstancesRequest.class));

    }

    @Test
    public void executeServiceOperation_Stop() throws Exception {

        parameters.put(PropertyHandler.FLOW_STATE, new Setting(
                PropertyHandler.FLOW_STATE, FlowState.STOP_REQUESTED.name()));
        parameters.put(PropertyHandler.OPERATION, new Setting(
                PropertyHandler.OPERATION, Operation.EC2_OPERATION.name()));

        ec2mock.createDescribeInstancesResult("instance1", "stopped", "1.2.3.4");
        ec2mock.createDescribeInstanceStatusResult("instance1", "ok", "ok",
                "ok");
        ec2mock.createDescribeImagesResult(IMAGE_ID);
        ec2mock.createRunInstancesResult(INSTANCE_ID);

        runUntilReady();

        verify(ec2).stopInstances(any(StopInstancesRequest.class));

    }

    private InstanceStatus getInstanceStatus() throws Exception {
        InstanceStatus result = runTX(new Callable<InstanceStatus>() {

            @Override
            public InstanceStatus call() throws Exception {
                return aws.getInstanceStatus(INSTANCE_ID, settings);
            }
        });
        return result;
    }

}
