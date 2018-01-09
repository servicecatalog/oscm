/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceStatusSummary;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.Subnet;

/**
 * Helper class for EC2 mockups
 */
public class EC2Mockup {
    private FifoAnswer<DescribeInstancesResult> answerDescribeInstances;
    private FifoAnswer<DescribeInstanceStatusResult> answerDescribeInstanceStatus;

    private AmazonEC2Client ec2;

    public EC2Mockup() {
        ec2 = mock(AmazonEC2Client.class);

        answerDescribeInstances = new FifoAnswer<DescribeInstancesResult>();
        doAnswer(answerDescribeInstances).when(ec2)
                .describeInstances(any(DescribeInstancesRequest.class));

        answerDescribeInstanceStatus = new FifoAnswer<DescribeInstanceStatusResult>();
        doAnswer(answerDescribeInstanceStatus).when(ec2).describeInstanceStatus(
                any(DescribeInstanceStatusRequest.class));

    }

    public AmazonEC2Client getEC2() {
        return ec2;
    }

    public void createDescribeImagesResult(String... imageIds) {
        Collection<Image> images = new ArrayList<Image>();
        for (int i = 0; i < imageIds.length; i++) {
            images.add(new Image().withImageId(imageIds[i]));
        }
        DescribeImagesResult imagesResult = new DescribeImagesResult()
                .withImages(images);
        doReturn(imagesResult).when(ec2)
                .describeImages(any(DescribeImagesRequest.class));
    }

    public void createDescribeImagesException(AmazonClientException ase) {
        doThrow(ase).when(ec2).describeImages(any(DescribeImagesRequest.class));
    }

    public void createStartInstanceException(AmazonClientException ase) {
        doThrow(ase).when(ec2).startInstances(any(StartInstancesRequest.class));
    }

    public void createDescribeInstancesResult(String instanceId,
            String stateName, String publicDnsName) {
        InstanceState state = new InstanceState().withName(stateName);
        Instance instance = new Instance().withInstanceId(instanceId)
                .withState(state).withPublicDnsName(publicDnsName);
        Reservation reservation = new Reservation().withInstances(instance);
        DescribeInstancesResult instancesResult = new DescribeInstancesResult()
                .withReservations(reservation);
        doReturn(instancesResult).when(ec2)
                .describeInstances(any(DescribeInstancesRequest.class));
    }

    public void createDescribeInstanceStatusResult(String instanceId,
            String stateName, String instanceStatusName,
            String systemStatusName) {
        InstanceState state = new InstanceState().withName(stateName);
        InstanceStatusSummary instanceSummary = new InstanceStatusSummary()
                .withStatus(instanceStatusName);
        InstanceStatusSummary systemSummary = new InstanceStatusSummary()
                .withStatus(systemStatusName);
        InstanceStatus instanceStatus = new com.amazonaws.services.ec2.model.InstanceStatus()
                .withInstanceId(instanceId).withInstanceState(state)
                .withInstanceStatus(instanceSummary)
                .withSystemStatus(systemSummary);
        DescribeInstanceStatusResult instanceStatusResult = new DescribeInstanceStatusResult()
                .withInstanceStatuses(instanceStatus);
        doReturn(instanceStatusResult).when(ec2).describeInstanceStatus(
                any(DescribeInstanceStatusRequest.class));
    }

    public void createRunInstancesResult(String... instanceIds) {
        Collection<Instance> instances = new ArrayList<Instance>();
        for (int i = 0; i < instanceIds.length; i++) {
            instances.add(new Instance().withInstanceId(instanceIds[i]));
        }
        Reservation reservation = new Reservation().withInstances(instances);
        RunInstancesResult result = new RunInstancesResult()
                .withReservation(reservation);
        doReturn(result).when(ec2).runInstances(any(RunInstancesRequest.class));
    }

    public void addDescribeInstancesResult(String instanceId, String stateName,
            String publicDnsName) {
        InstanceState state = new InstanceState().withName(stateName);
        Instance instance = new Instance().withInstanceId(instanceId)
                .withState(state).withPublicDnsName(publicDnsName);
        Reservation reservation = new Reservation().withInstances(instance);
        DescribeInstancesResult instancesResult = new DescribeInstancesResult()
                .withReservations(reservation);
        answerDescribeInstances.add(instancesResult);
    }

    public void addDescribeInstanceStatusResult(String instanceId,
            String stateName, String instanceStatusName,
            String systemStatusName) {
        InstanceState state = new InstanceState().withName(stateName);
        InstanceStatusSummary instanceSummary = new InstanceStatusSummary()
                .withStatus(instanceStatusName);
        InstanceStatusSummary systemSummary = new InstanceStatusSummary()
                .withStatus(systemStatusName);
        com.amazonaws.services.ec2.model.InstanceStatus instanceStatus = new com.amazonaws.services.ec2.model.InstanceStatus()
                .withInstanceId(instanceId).withInstanceState(state)
                .withInstanceStatus(instanceSummary)
                .withSystemStatus(systemSummary);
        DescribeInstanceStatusResult instanceStatusResult = new DescribeInstanceStatusResult()
                .withInstanceStatuses(instanceStatus);
        answerDescribeInstanceStatus.add(instanceStatusResult);
    }

    private class FifoAnswer<T> implements Answer<T> {
        private LinkedList<T> list = new LinkedList<T>();

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            return list.pollFirst();
        }

        public void add(T element) {
            list.addLast(element);
        }
    }

    public void createDescribeSubnetsResult(String... subnetIds) {
        Collection<Subnet> subnets = new ArrayList<Subnet>();
        for (int i = 0; i < subnetIds.length; i++) {
            subnets.add(new Subnet().withSubnetId(subnetIds[i])
                    .withVpcId(subnetIds[i]));
        }
        DescribeSubnetsResult subnetResult = new DescribeSubnetsResult()
                .withSubnets(subnets);
        doReturn(subnetResult).when(ec2)
                .describeSubnets(any(DescribeSubnetsRequest.class));
    }

    public void createDescribeSecurityGroupResult(String vpcId,
            String SecurityGroupIds) {
        Collection<SecurityGroup> securityGroup = new ArrayList<SecurityGroup>();
        for (int i = 0; i < SecurityGroupIds.split(",").length; i++) {
            securityGroup.add(new SecurityGroup()
                    .withGroupId(SecurityGroupIds.split(",")[i])
                    .withGroupName(SecurityGroupIds.split(",")[i])
                    .withVpcId(vpcId));
        }
        DescribeSecurityGroupsResult securityGroupResult = new DescribeSecurityGroupsResult()
                .withSecurityGroups(securityGroup);
        doReturn(securityGroupResult).when(ec2).describeSecurityGroups();
    }

}
