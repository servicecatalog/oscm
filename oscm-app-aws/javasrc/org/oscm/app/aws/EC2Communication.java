/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-10-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.oscm.app.aws.controller.PropertyHandler;
import org.oscm.app.aws.i18n.Messages;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceSpecification;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class EC2Communication {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EC2Communication.class);

    // EC2 client stub for unit testing
    private static AmazonEC2Client ec2_stub;

    private final PropertyHandler ph;
    private final AWSCredentialsProvider credentialsProvider;
    private AmazonEC2Client ec2;

    private static final String ENDPOINT_PREFIX = "ec2.";
    private static final String ENDPOINT_SUFFIX = ".amazonaws.com";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String HTTPS_PROXY_USER = "https.proxyUser";
    private static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    /**
     * Constructor
     * 
     * @param ph
     */
    public EC2Communication(PropertyHandler ph) {
        this.ph = ph;
        final String secretKey = ph.getSecretKey();
        final String accessKeyId = ph.getAccessKeyId();

        credentialsProvider = new AWSCredentialsProvider() {

            @Override
            public void refresh() {
            }

            @Override
            public AWSCredentials getCredentials() {

                return new AWSCredentials() {

                    @Override
                    public String getAWSSecretKey() {
                        return secretKey;
                    }

                    @Override
                    public String getAWSAccessKeyId() {
                        return accessKeyId;
                    }
                };
            }
        };
    }

    /**
     * Return amazon interface
     * 
     * @return AmazonEC2 client ec2
     */

    AmazonEC2 getEC2() {
        if (ec2 == null) {
            String endpoint = ENDPOINT_PREFIX + ph.getRegion()
                    + ENDPOINT_SUFFIX;
            String proxyHost = System.getProperty(HTTPS_PROXY_HOST);
            String proxyPort = System.getProperty(HTTPS_PROXY_PORT);
            String proxyUser = System.getProperty(HTTPS_PROXY_USER);
            String proxyPassword = System.getProperty(HTTPS_PROXY_PASSWORD);

            int proxyPortInt = 0;
            try {
                proxyPortInt = Integer.parseInt(proxyPort);
            } catch (NumberFormatException e) {
                // ignore
            }
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            if (!isNonProxySet(endpoint)) {
                if (proxyHost != null) {
                    clientConfiguration.setProxyHost(proxyHost);
                }
                if (proxyPortInt > 0) {
                    clientConfiguration.setProxyPort(proxyPortInt);
                }
                if (proxyUser != null && proxyUser.length() > 0) {
                    clientConfiguration.setProxyUsername(proxyUser);
                }
                if (proxyPassword != null && proxyPassword.length() > 0) {
                    clientConfiguration.setProxyPassword(proxyPassword);
                }
            }
            ec2 = getEC2(credentialsProvider, clientConfiguration);
            ec2.setEndpoint(endpoint);
        }
        return ec2;
    }

    /**
     * Define AWS mockup for unit tests
     * 
     * @param ec2
     */
    public static void useMock(AmazonEC2Client ec2) {
        ec2_stub = ec2;
    }

    /**
     * Allow mocking of EC2 client by having it in separate creation method
     * 
     * @param credentialsProvider
     * @param clientConfiguration
     */
    AmazonEC2Client getEC2(AWSCredentialsProvider credentialsProvider,
            ClientConfiguration clientConfiguration) {
        if (ec2 == null) {
            ec2 = (ec2_stub != null) ? ec2_stub : new AmazonEC2Client(
                    credentialsProvider, clientConfiguration);
        }
        return ec2;
    }

    /**
     * Checks whether system proxy settings tell to omit proxying for given
     * endpoint.
     * 
     * @param endpoint
     * @return <code>true</code> if the endpoint matches one of the nonProxy
     *         settings
     */
    boolean isNonProxySet(String endpoint) {
        String nonProxy = System.getProperty(HTTP_NON_PROXY_HOSTS);
        if (nonProxy != null) {
            String[] split = nonProxy.split("\\|");
            for (int i = 0; i < split.length; i++) {
                String np = split[i].trim();
                if (np.length() > 0) {
                    boolean wcStart = np.startsWith("*");
                    boolean wcEnd = np.endsWith("*");
                    if (wcStart) {
                        np = np.substring(1);
                    }
                    if (wcEnd) {
                        np = np.substring(0, np.length() - 1);
                    }
                    if (wcStart && wcEnd && endpoint.contains(np)) {
                        return true;
                    }
                    if (wcStart && endpoint.endsWith(np)) {
                        return true;
                    }
                    if (wcEnd && endpoint.startsWith(np)) {
                        return true;
                    }
                    if (np.equals(endpoint)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether image is present.
     * 
     * @param amiID
     * @return <code>Image </code> if the matches one of the amiID
     * 
     */
    public Image resolveAMI(String amiID) throws APPlatformException {
        LOGGER.debug("resolveAMI('{}') entered", amiID);
        DescribeImagesRequest dir = new DescribeImagesRequest();
        dir.withImageIds(amiID);
        DescribeImagesResult describeImagesResult = getEC2()
                .describeImages(dir);

        List<Image> images = describeImagesResult.getImages();
        for (Image image : images) {
            LOGGER.debug(image.getImageId() + "==" + image.getImageLocation()
                    + "==" + image.getName());
            return image;
        }
        throw new APPlatformException(Messages.getAll("error_invalid_image")
                + amiID);
    }

    /**
     * Checks whether exiting Subnet is present.
     * 
     * @param subnetString
     * @return <code>Subnet </code> if the matches one of the subnetString
     * 
     */
    public Subnet resolveSubnet(String subnetString) throws APPlatformException {
        DescribeSubnetsRequest request = new DescribeSubnetsRequest();
        DescribeSubnetsResult result = getEC2().describeSubnets(
                request.withSubnetIds(subnetString));
        List<Subnet> subnets = result.getSubnets();
        if (!subnets.isEmpty()) {
            LOGGER.debug(" number of subnets found: " + subnets.size());
            for (Subnet subnet : subnets) {
                LOGGER.debug("return subnet with id " + subnet.getSubnetId());
                return subnet;
            }

        }
        throw new APPlatformException(
                Messages.getAll("error_invalid_subnet_id") + subnetString);

    }

    /**
     * Checks whether exiting SecurityGroups is present.
     * 
     * @param securityGroupNames
     * @param vpcId
     *            The ID of the VPC the subnet is in.A virtual private cloud
     *            (VPC) is a virtual network dedicated to your AWS account. It
     *            is logically isolated from other virtual networks in the AWS
     *            cloud. You can launch your AWS resources, such as Amazon EC2
     *            instances, into your VPC.
     * @return <code>Collection<String> </code> if the matches one of the
     *         securityGroupNames and vpcId
     * 
     */
    public Collection<String> resolveSecurityGroups(
            Collection<String> securityGroupNames, String vpcId)
            throws APPlatformException {
        Collection<String> input = new HashSet<String>();
        Collection<String> result = new HashSet<String>();
        if (vpcId != null && vpcId.trim().length() == 0) {
            vpcId = null;
        }
        if (securityGroupNames != null && !securityGroupNames.isEmpty()) {
            input.addAll(securityGroupNames);
            DescribeSecurityGroupsResult securityGroups = getEC2()
                    .describeSecurityGroups();
            LOGGER.debug("Search for securityGroups"
                    + securityGroupNames.toString());
            for (SecurityGroup group : securityGroups.getSecurityGroups()) {
                boolean vpcMatch = false;
                if (vpcId == null) {
                    vpcMatch = isNullOrEmpty(group.getVpcId());
                } else {
                    vpcMatch = vpcId.equals(group.getVpcId());
                }
                if (vpcMatch && input.contains(group.getGroupName())) {
                    result.add(group.getGroupId());
                    input.remove(group.getGroupName());
                }
            }
            if (!input.isEmpty()) {
                StringBuffer sb = new StringBuffer();
                for (String name : input) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(name);
                }
                throw new APPlatformException(
                        Messages.getAll("error_invalid_security_group")
                                + sb.toString());
            }
        }
        LOGGER.debug("Done with Searching for securityGroups " + result);
        return result;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * create virtual machine on AWS
     * 
     * @param image
     * 
     * 
     */
    public void createInstance(Image image) throws APPlatformException {

        LOGGER.debug("createInstance('{}') entered", image.getImageId());
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withInstanceType(ph.getInstanceType())
                .withImageId(image.getImageId())
                .withMinCount(Integer.valueOf(1))
                .withMaxCount(Integer.valueOf(1))
                .withKeyName(ph.getKeyPairName());
        InstanceNetworkInterfaceSpecification networkInterface = new InstanceNetworkInterfaceSpecification();
        String subnetId = null;
        LOGGER.debug("runInstancesRequest : " + " image ID : "
                + image.getImageId() + " insatance type : "
                + ph.getInstanceType());

        Collection<String> securityGroupNames = ph.getSecurityGroups();
        if (ph.getSubnet() != null && ph.getSubnet().trim().length() > 0) {
            Subnet subnet = resolveSubnet(ph.getSubnet());
            subnetId = subnet.getSubnetId();
            LOGGER.debug("Subnet: " + subnetId);
            Collection<String> securityGroupIds = resolveSecurityGroups(
                    securityGroupNames, subnet.getVpcId());
            if (securityGroupIds.size() > 0) {
                for (String secGroup : securityGroupIds) {
                    LOGGER.debug("SecurityGroup: " + secGroup);
                }
                networkInterface.setGroups(securityGroupIds);
            }
        }
        LOGGER.debug("public IP for VM : " + ph.getPublicIp());
        if (ph.getPublicIp() != null) {
            if (Boolean.parseBoolean(ph.getPublicIp().trim())) {
                LOGGER.debug("Set public IP for VM as true ");
                networkInterface.setAssociatePublicIpAddress(true);
            } else {
                LOGGER.debug("Set public IP for VM as false ");
                networkInterface.setAssociatePublicIpAddress(false);
            }
        }
        // Here we attached network Interface to the instance request as we want
        // to set the public ip or subnet and securityGroups
        if ((ph.getSubnet() != null && ph.getSubnet().trim().length() > 0)
                || (ph.getPublicIp() != null && ph.getPublicIp().trim()
                        .length() > 0)) {
            networkInterface.setDeviceIndex(Integer.valueOf(0));
            networkInterface.setSubnetId(subnetId);
            networkInterface.setDeleteOnTermination(Boolean.TRUE);
            runInstancesRequest.withNetworkInterfaces(networkInterface);
            LOGGER.info("add networkInterface parameters in instacne request");
        }

        // if disk size is defined change the disk size of the new instance
        if (ph.getDiskSize() != null) {
            Integer diskSize = Integer.parseInt(ph.getDiskSize().trim());
            if (diskSize.intValue() >= 0) {
                List<BlockDeviceMapping> mappings = image
                        .getBlockDeviceMappings();
                for (BlockDeviceMapping bdm : mappings) {
                    EbsBlockDevice ebs = bdm.getEbs();
                    String rootDeviceName = image.getRootDeviceName();
                    if (rootDeviceName != null
                            && rootDeviceName.equals(bdm.getDeviceName())) {

                        if (diskSize.intValue() < ebs.getVolumeSize()
                                .intValue()) {
                            diskSize = ebs.getVolumeSize().intValue();
                        }
                        ebs.setVolumeSize(diskSize);
                        ebs.setEncrypted(null);
                        ebs.setDeleteOnTermination(true);
                        LOGGER.info(">>SNAPSHOT ID : " + ebs.getSnapshotId());
                        ph.setSnapshotId(ebs.getSnapshotId());
                        runInstancesRequest.setBlockDeviceMappings(mappings);
                        break;
                    }
                }
            }
        }

        LOGGER.info("disk type Done");
        String userData = ph.getUserData();
        if (userData != null && userData.trim().length() > 0) {
            runInstancesRequest.setUserData(getTextBASE64(userData));
        }
        LOGGER.info("runInstancesRequest :: " + runInstancesRequest.toString());
        RunInstancesResult result = getEC2().runInstances(runInstancesRequest);

        List<Instance> reservedInstances = result.getReservation()
                .getInstances();
        LOGGER.info("RunInstancesResult type Done");
        if (reservedInstances.size() == 0) {
            throw new APPlatformException(
                    Messages.getAll("error_no_reserved_instance"));
        }

        for (Instance instance : reservedInstances) {
            String instanceId = instance.getInstanceId();
            ph.setAwsInstanceId(instanceId);
            LOGGER.info("setAwsInstanceId : " + instanceId);
            createTags(ph);
        }
        if (image.getPlatform() != null) {
            ph.setInstancePlatform(image.getPlatform());
        }
        String publicDNS = this.getPublicDNS(ph.getAwsInstanceId());
        ph.setInstancePublicDNS(publicDNS);
    }

    public void modifyInstance() throws APPlatformException {
        createTags(ph);
    }

    public void terminateInstance(String instanceId) {
        getEC2().terminateInstances(
                new TerminateInstancesRequest().withInstanceIds(instanceId));
    }

    public void startInstance(String instanceId) {
        getEC2().startInstances(
                new StartInstancesRequest().withInstanceIds(instanceId));
    }

    public void stopInstance(String instanceId) {
        getEC2().stopInstances(
                new StopInstancesRequest().withInstanceIds(instanceId));
    }

    public String getInstanceState(String instanceId) {
        LOGGER.debug("getInstanceState('{}') entered", instanceId);
        DescribeInstancesResult result = getEC2().describeInstances(
                new DescribeInstancesRequest().withInstanceIds(instanceId));
        List<Reservation> reservations = result.getReservations();
        Set<Instance> instances = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
            if (instances.size() > 0) {
                String state = instances.iterator().next().getState().getName();
                LOGGER.debug("  InstanceState: {}", state);
                return state;
            }
        }
        LOGGER.debug("getInstanceState('{}') left", instanceId);
        return null;
    }

    public boolean isInstanceReady(String instanceId) {
        LOGGER.debug("isInstanceReady('{}') entered", instanceId);
        DescribeInstanceStatusResult result = getEC2()
                .describeInstanceStatus(
                        new DescribeInstanceStatusRequest()
                                .withInstanceIds(instanceId));
        List<InstanceStatus> statusList = result.getInstanceStatuses();
        boolean instanceStatus = false;
        boolean systemStatus = false;

        for (InstanceStatus status : statusList) {
            LOGGER.debug("  InstanceState:    {}", status.getInstanceState());
            LOGGER.debug("  InstanceStatus:   {}", status.getInstanceStatus()
                    .getStatus());
            LOGGER.debug("  SystemStatus:     {}", status.getSystemStatus()
                    .getStatus());
            LOGGER.debug("  AvailabilityZone: {}", status.getAvailabilityZone());

            instanceStatus = ("ok".equals(status.getInstanceStatus()
                    .getStatus()));
            systemStatus = ("ok".equals(status.getSystemStatus().getStatus()));
        }
        LOGGER.debug("isInstanceReady('{}') left", instanceId);
        return instanceStatus && systemStatus;
    }

    public String getPublicDNS(String instanceId) {
        DescribeInstancesResult result = getEC2().describeInstances(
                new DescribeInstancesRequest().withInstanceIds(instanceId));
        List<Reservation> reservations = result.getReservations();
        Set<Instance> instances = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
            if (instances.size() > 0) {
                return instances.iterator().next().getPublicDnsName();
            }
        }
        return null;
    }

    public List<Instance> getInstance(String instanceId) {
        DescribeInstancesResult result = getEC2().describeInstances(
                new DescribeInstancesRequest().withInstanceIds(instanceId));
        List<Reservation> reservations = result.getReservations();
        List<Instance> instances = new ArrayList<>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }
        return instances;
    }

    private String getTextBASE64(String url) throws APPlatformException {
        InputStream cin = null;
        try {
            URL source = new URL(url);
            URLConnection connection = source.openConnection();
            cin = connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(cin));

            StringBuilder response = new StringBuilder();
            char[] buffer = new char[1024];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                response.append(buffer, 0, n);
            }

            in.close();
            LOGGER.debug(response.toString());
            return Base64.encodeBase64String(response.toString().getBytes(
                    "UTF-8"));

        } catch (MalformedURLException e) {
            throw new APPlatformException("Reading userdata failed: "
                    + e.getMessage());
        } catch (IOException e) {
            throw new APPlatformException("Reading userdata failed: "
                    + e.getMessage());
        } finally {
            if (cin != null) {
                try {
                    cin.close();
                } catch (IOException e) {
                    // ignore, wanted to close anyway
                }

            }
        }
    }

    private void createTags(PropertyHandler ph) throws APPlatformException {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(new Tag(PropertyHandler.TAG_NAME, ph.getInstanceName()));
        tags.add(new Tag(PropertyHandler.TAG_SUBSCRIPTION_ID, ph.getSettings()
                .getSubscriptionId()));
        tags.add(new Tag(PropertyHandler.TAG_ORGANIZATION_ID, ph.getSettings()
                .getOrganizationId()));
        CreateTagsRequest ctr = new CreateTagsRequest();
        LOGGER.debug("attach tags to resource " + ph.getAWSInstanceId());
        ctr.withResources(ph.getAWSInstanceId()).setTags(tags);
        getEC2().createTags(ctr);
    }
}
