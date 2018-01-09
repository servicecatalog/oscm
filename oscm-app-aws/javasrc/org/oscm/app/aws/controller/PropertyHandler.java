/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Sample controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2012-09-06                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.aws.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.oscm.app.aws.data.FlowState;
import org.oscm.app.aws.data.Operation;
import org.oscm.app.v2_0.BSSWebServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;

/**
 * Helper class to handle service parameters and controller configuration
 * settings. The implementation shows how the settings can be managed in a
 * centralized way.
 * <p>
 * The underlying <code>ProvisioningSettings</code> object of APP provides all
 * the specified service parameters and controller configuration settings
 * (key/value pairs). The settings are stored in the APP database and therefore
 * available even after restarting the application server.
 */
public class PropertyHandler {

    private final ProvisioningSettings settings;
    private static PropertyHandler mockHandler;

    /**
     * The internal status of a provisioning operation as set by the controller
     * or the status dispatcher.
     */
    public static final String OPERATION = "OPERATION";
    public static final String FLOW_STATE = "FLOW_STATE";

    public static final String SECRET_KEY_PWD = "SECRET_KEY_PWD";
    public static final String ACCESS_KEY_ID_PWD = "ACCESS_KEY_ID_PWD";

    public static final String KEY_PAIR_NAME = "KEY_PAIR_NAME";
    public static final String REGION = "REGION";
    public static final String IMAGE_NAME = "IMAGE_NAME";
    public static final String INSTANCE_TYPE = "INSTANCE_TYPE";
    public static final String SECURITY_GROUP_NAMES = "SECURITY_GROUP_NAMES";
    public static final String USERDATA_URL = "USERDATA_URL";

    public static final String INSTANCENAME = "INSTANCENAME";
    public static final String INSTANCENAME_PATTERN = "INSTANCENAME_PATTERN";
    public static final String INSTANCENAME_PREFIX = "INSTANCENAME_PREFIX";

    // Subscription-related tag keys for AWS resources
    public static final String TAG_NAME = "Name";
    public static final String TAG_SUBSCRIPTION_ID = "SubscriptionId";
    public static final String TAG_ORGANIZATION_ID = "OrganizationId";

    public static final String AWS_INSTANCE_ID = "AWS_INSTANCE_ID";
    public static final String MAIL_FOR_COMPLETION = "MAIL_FOR_COMPLETION";

    // new
    public static final String SUBNET = "SUBNET";
    public static final String PUBLIC_IP = "PUBLIC_IP";
    public static final String DISK_SIZE = "DISK_SIZE";
    public static final String INSTANCE_PLATFORM = "INSTANCE_PLATFORM";
    public static final String EAI_INSTANCE_PUBLIC_DNS = "EAI_INSTANCE_PUBLIC_DNS";
    public static final String SNAPSHOT_ID = "SNAPSHOT_ID";

    /**
     * Default factory method.
     * 
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     */
    public static PropertyHandler withSettings(ProvisioningSettings settings) {
        return mockHandler == null ? new PropertyHandler(settings)
                : mockHandler;
    }

    public static void useMock(PropertyHandler mock) {
        mockHandler = mock;
    }

    /**
     * Internal constructor.
     * 
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     */
    protected PropertyHandler(ProvisioningSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns the internal state of the current provisioning operation as set
     * by the controller or the processor.
     * 
     * @return the current state
     */
    public FlowState getState() {
        Setting status = settings.getParameters().get(FLOW_STATE);
        return (status != null && status.getValue() != null)
                ? FlowState.valueOf(status.getValue()) : FlowState.FAILED;
    }

    /**
     * Changes the internal state for the current provisioning operation.
     * 
     * @param newState
     *            the new status to set
     */
    public void setState(FlowState newState) {
        setValue(FLOW_STATE, newState.toString(), settings.getParameters());
    }

    /**
     * Returns the current service parameters and controller configuration
     * settings.
     * 
     * @return a <code>ProvisioningSettings</code> object specifying the
     *         parameters and settings
     */
    public ProvisioningSettings getSettings() {
        return settings;
    }

    /**
     * @return the configured AWS secret key
     */
    public String getSecretKey() {
        return getValue(SECRET_KEY_PWD, settings.getConfigSettings());
    }

    /**
     * @return the configured AWS access key ID
     */
    public String getAccessKeyId() {
        return getValue(ACCESS_KEY_ID_PWD, settings.getConfigSettings());
    }

    /**
     * @return the configured AWS key pair name
     */
    public String getKeyPairName() {
        return getValue(KEY_PAIR_NAME, settings.getParameters());
    }

    /**
     * @return the region the instance is (to be) deployed to
     */
    public String getRegion() {
        return getValue(REGION, settings.getParameters());
    }

    /**
     * @return the AWS generated instance ID
     */
    public String getAWSInstanceId() {
        return getValue(AWS_INSTANCE_ID, settings.getParameters());
    }

    /**
     * Set the AWS generated instance ID.
     * 
     * @param instanceId
     *            the instance ID
     */
    public void setAWSInstanceId(String instanceId) {
        setValue(AWS_INSTANCE_ID, instanceId, settings.getParameters());
    }

    /**
     * Return the value of the image name parameter. The name is used to resolve
     * the AMI for instance creation.
     * 
     * @return the image name
     */
    public String getImageName() {
        return getValue(IMAGE_NAME, settings.getParameters());
    }

    /**
     * Returns the value of the instance type parameter.
     * 
     * @return the instance type
     */
    public String getInstanceType() {
        return getValue(INSTANCE_TYPE, settings.getParameters());
    }

    private String getInstanceNamePrefix() {
        return getValue(INSTANCENAME_PREFIX, settings.getParameters());
    }

    /**
     * Returns the name pattern to be used for instance name validation.
     * 
     * @return the validation pattern
     */
    public String getInstanceNamePattern() {
        return getValue(INSTANCENAME_PATTERN, settings.getParameters());
    }

    private String getInstanceNameRaw() {
        return getValue(INSTANCENAME, settings.getParameters());
    }

    /**
     * Returns the full name of the new instance (including the prefix).
     * 
     * @return the full name of the new instance
     */
    public String getInstanceName() {
        StringBuffer b = new StringBuffer();
        String prefix = getInstanceNamePrefix();
        if (prefix != null) {
            b.append(prefix);
        }
        String name = getInstanceNameRaw();
        if (name != null) {
            b.append(name);
        }
        return b.toString();
    }

    /**
     * Returns the current operation.
     */
    public Operation getOperation() {
        Setting operation = settings.getParameters().get(OPERATION);
        return (operation != null && operation.getValue() != null)
                ? Operation.valueOf(operation.getValue()) : Operation.UNKNOWN;
    }

    /**
     * Updates the current operation.
     */
    public void setOperation(Operation newState) {
        setValue(OPERATION, newState.toString(), settings.getParameters());
    }

    /**
     *  
     */
    public String getUserData() {
        return getValue(USERDATA_URL, settings.getParameters());
    }

    /**
     * Returns the security groups the instance is to be assigned to.
     * 
     * @return a collection of security group names - may be empty but not
     *         <code>null</code>
     */
    public Collection<String> getSecurityGroups() {
        Collection<String> result = new HashSet<>();
        String value = getValue(SECURITY_GROUP_NAMES, settings.getParameters());
        if (value != null) {
            String[] split = value.split(",");
            for (int i = 0; i < split.length; i++) {
                result.add(split[i].trim());
            }
            result.remove("");
        }
        return result;
    }

    /**
     * Returns service interfaces for BSS web service calls.
     */
    public <T> T getWebService(Class<T> serviceClass) throws Exception {
        return BSSWebServiceFactory.getBSSWebService(serviceClass,
                settings.getAuthentication());
    }

    /**
     * Returns the mail address to be used for completion events (provisioned,
     * deleted). If not set, no events are required.
     * 
     * @return the mail address or <code>null</code> if no events are required
     */
    public String getMailForCompletion() {
        String mail = getValue(MAIL_FOR_COMPLETION, settings.getParameters());
        return isNullOrEmpty(mail) ? null : mail;
    }

    /**
     * Returns the instance or controller specific technology manager
     * authentication.
     */
    public PasswordAuthentication getTPAuthentication() {
        return settings.getAuthentication();
    }

    /**
     * Returns a printable virtual system configuration string.
     */
    public String getAWSConfigurationAsString() {
        StringBuffer details = new StringBuffer();
        details.append("\t\r\nImageName: ");
        details.append(getImageName());
        details.append("\t\r\nAWSInstanceId: ");
        details.append(getAWSInstanceId());
        details.append("\t\r\nInstanceType: ");
        details.append(getInstanceType());
        details.append("\t\r\nInstanceName: ");
        details.append(getInstanceName());
        details.append("\t\r\nRegion: ");
        details.append(getRegion());
        details.append("\t\r\n");
        return details.toString();
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Returns the locale set as default for the customer organization.
     * 
     * @return the customer locale
     */
    public String getCustomerLocale() {
        String locale = settings.getLocale();
        if (isNullOrEmpty(locale)) {
            locale = "en";
        }
        return locale;
    }

    public String getSubnet() {
        return getValue(SUBNET, settings.getParameters());
    }

    public String getTagName() {
        return getValue(TAG_NAME, settings.getParameters());
    }

    public String getPublicIp() {
        return getValue(PUBLIC_IP, settings.getParameters());
    }

    public String getDiskSize() {
        return getValue(DISK_SIZE, settings.getParameters());
    }

    public String getAwsInstanceId() {
        return getValue(AWS_INSTANCE_ID, settings.getParameters());
    }

    public void setAwsInstanceId(String awsInstanceId) {
        setValue(AWS_INSTANCE_ID, awsInstanceId, settings.getParameters());
    }

    public void setInstancePlatform(String instancePlatform) {
        setValue(INSTANCE_PLATFORM, instancePlatform, settings.getParameters());
    }

    public void setInstancePublicDNS(String publicDNS) {
        setValue(EAI_INSTANCE_PUBLIC_DNS, publicDNS, settings.getParameters());
    }

    public void setSnapshotId(String snapshotId) {
        setValue(SNAPSHOT_ID, snapshotId, settings.getParameters());
    }

    private String getValue(String key, Map<String, Setting> source) {
        Setting setting = source.get(key);
        return setting != null ? setting.getValue() : null;
    }

    private void setValue(String key, String value,
            Map<String, Setting> target) {
        target.put(key, new Setting(key, value));
    }

}
