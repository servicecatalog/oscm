/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import org.apache.commons.lang3.StringUtils;
import org.oscm.app.azure.data.FlowState;
import org.oscm.app.v2_0.BSSWebServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Helper class to handle service parameters and controller configuration
 * settings.
 * <p>
 * The underlying <code>ProvisioningSettings</code> object of APP provides all
 * the specified service parameters and controller configuration settings
 * (key/value pairs). The settings are stored in the APP database and therefore
 * available even after restarting the application server.
 */
public class PropertyHandler {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(PropertyHandler.class);

    /**
     * ProvisioningSettings.
     */
    private final ProvisioningSettings settings;

    /*(ConfigSettings) */

    /**
     * Azure
     */
    public static final String API_USER_NAME = "API_USER_NAME";

    /**
     * Azure‚
     */
    public static final String API_USER_PWD = "API_USER_PWD";

    /**
     *
     */
    public static final String TEMPLATE_BASE_URL = "TEMPLATE_BASE_URL";

    /**
     *
     */
    public static final String READY_TIMEOUT = "READY_TIMEOUT";

    /* (Parameters) */

    /**
     *
     */
    public static final String MAIL_FOR_COMPLETION = "MAIL_FOR_COMPLETION";

    /**
     * Azure‚
     */
    public static final String SUBSCRIPTION_ID = "SUBSCRIPTION_ID";

    /**
     * Azure‚
     */
    public static final String TENANT_ID = "TENANT_ID";

    /**
     * Azure‚
     */
    public static final String CLIENT_ID = "CLIENT_ID";

    /**
     * Azure
     */
    public static final String CLIENT_SECRET = "CLIENT_SECRET";

    /**
     * Azure
     */
    public static final String REGION = "REGION";

    /**
     * Azure
     */
    public static final String RESOURCE_GROUP_NAME = "RESOURCE_GROUP_NAME";

    /**
     * Azure
     */
    public static final String VIRTUAL_NETWORK = "VIRTUAL_NETWORK";

    /**
     * Azure
     */
    public static final String SUBNET = "SUBNET";

    /**
     * Azure
     */
    public static final String STORAGE_ACCOUNT = "STORAGE_ACCOUNT";

    /**
     * Azure
     */
    public static final String INSTANCE_COUNT = "INSTANCE_COUNT";

    /**
     * Azure
     */
    public static final String VIRTUAL_MACHINE_IMAGE_ID = "VIRTUAL_MACHINE_IMAGE_ID";

    /**
     * Azure
     */
    public static final String VM_NAME = "VM_NAME";

    /**
     * Azure
     */
    public static final String INSTANCE_NAME = "INSTANCE_NAME";

    /**
     * Azure
     */
    public static final String TEMPLATE_NAME = "TEMPLATE_NAME";

    /**
     * Azure
     */
    public static final String TEMPLATE_PARAMETERS_NAME = "TEMPLATE_PARAMETERS_NAME";

    /**
     * Azure
     */
    public static final String DEPLOYMENT_NAME = "DEPLOYMENT_NAME";

    /**
     *
     */
    public static final String FLOW_STATUS = "FLOW_STATUS";

    /**
     *
     */
    public static final String START_TIME = "START_TIME";

    /**
     * Default constructor.
     *
     * @param settings a <code>ProvisioningSettings</code> object specifying the
     *                 service parameters and configuration settings
     */
    public PropertyHandler(ProvisioningSettings settings) {
        this.settings = settings;
    }

    /**
     * Reads the requested property from the available parameters. If no value
     * can be found, a RuntimeException will be thrown.
     *
     * @param sourceProps The property object to take the settings from
     * @param key         The key to retrieve the setting for
     * @return the parameter value corresponding to the provided key
     */
    protected static String getValidatedProperty(Map<String, Setting> sourceProps,
                                                 String key) {
        String value = sourceProps.get(key).getValue();
        if (value == null) {
            String message = String.format("No value set for property '%s'",
                    key);
            logger.error(message);
            throw new RuntimeException(message);
        }
        return value;
    }

    /**
     * Returns the current service parameters and controller configuration
     * settings.
     *
     * @return a <code>ProvisioningSettings</code> object specifying the
     * parameters and settings
     */
    public ProvisioningSettings getSettings() {
        return settings;
    }

    /**
     * Azure
     *
     * @return Azure
     */
    public String getUserName() {
        return getValidatedProperty(settings.getConfigSettings(), API_USER_NAME);
    }

    /**
     * Azure
     */
    public String getPassword() {
        return getValidatedProperty(settings.getConfigSettings(), API_USER_PWD);
    }

    /**
     * Azure
     */
    public String getTemplateBaseUrl() {
        return getValidatedProperty(settings.getParameters(), TEMPLATE_BASE_URL);
    }

    /**
     * Azure
     */
    public long getReadyTimeout() {
        return Long.parseLong(getValidatedProperty(
                settings.getConfigSettings(), READY_TIMEOUT));
    }

    /**
     * Azure
     */
    public String getMailForCompletion() {
        String value = settings.getParameters().get(MAIL_FOR_COMPLETION).getValue();
        return StringUtils.isBlank(value) ? null : value;
    }

    /**
     * Azure
     */
    public String getSubscriptionId() {
        return getValidatedProperty(settings.getParameters(), SUBSCRIPTION_ID);
    }

    /**
     * Azure
     */
    public String getTenantId() {
        return getValidatedProperty(settings.getParameters(), TENANT_ID);
    }

    /**
     * Azure
     */
    public String getClientId() {
        return getValidatedProperty(settings.getParameters(), CLIENT_ID);
    }

    /**
     * Azure
     */
    public String getClientSecret() {
        return settings.getParameters().get(CLIENT_SECRET).getValue();
    }

    /**
     * Azure
     */
    public String getRegion() {
        return getValidatedProperty(settings.getParameters(), REGION);
    }

    /**
     * Azure
     */
    public String getResourceGroupName() {

        return getValidatedProperty(settings.getParameters(),
                RESOURCE_GROUP_NAME);
    }


    public String getVirtualNetwork() {
        return getValidatedProperty(settings.getParameters(),
                VIRTUAL_NETWORK);
    }

    public String getSubnet() {
        return getValidatedProperty(settings.getParameters(),
                SUBNET);
    }

    public String getStorageAccount() {
        return getValidatedProperty(settings.getParameters(),
                STORAGE_ACCOUNT);
    }

    public String getInstanceCount() {
        return getValidatedProperty(settings.getParameters(),
                INSTANCE_COUNT);
    }

    public String getVirtualMachineImageID() {
        return getValidatedProperty(settings.getParameters(),
                VIRTUAL_MACHINE_IMAGE_ID);
    }

    public String getVMName() {
        return getValidatedProperty(settings.getParameters(),
                VM_NAME);
    }

    public String getInstanceName() {
        return getValidatedProperty(settings.getParameters(),
                INSTANCE_NAME);
    }

    /**
     * Azure
     */
    public void setResourceGroupName(String value) {
        settings.getParameters().put(RESOURCE_GROUP_NAME, new Setting(RESOURCE_GROUP_NAME, value));
    }

    /**
     * Azure
     */
    protected String getTemplateName() {
        return getValidatedProperty(settings.getParameters(), TEMPLATE_NAME);

    }

    /**
     * Azure
     */
    public String getTemplateUrl() {
        try {
            String url = new URL(new URL(getTemplateBaseUrl()), getTemplateName()).toExternalForm();
            return url;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot generate template URL: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Azure
     */
    protected String getTemplateParametersName() {
        String value = settings.getParameters().get(TEMPLATE_PARAMETERS_NAME).getValue();
        return StringUtils.isBlank(value) ? null : value;
    }

    /**
     * Azure
     */
    public String getTemplateParametersUrl() {
        String fileName = getTemplateParametersName();
        if (fileName == null) {
            return null;
        }
        try {
            return new URL(new URL(getTemplateBaseUrl()), fileName)
                    .toExternalForm();
        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "Cannot generate template parameters URL: "
                            + e.getMessage(), e);
        }
    }

    /**
     * Azure
     */
    public String getDeploymentName() {
        return settings.getParameters().get(DEPLOYMENT_NAME).getValue();
    }

    /**
     * Azure
     */
    public void setDeploymentName(String value) {
        settings.getParameters().put(DEPLOYMENT_NAME, new Setting(DEPLOYMENT_NAME, value));

    }

    /**
     * Azure
     */
    public FlowState getFlowState() {
        String status = settings.getParameters().get(FLOW_STATUS).getValue();
        return (status != null) ? FlowState.valueOf(status) : FlowState.FAILED;
    }

    /**
     * Azure
     */
    public void setFlowState(FlowState value) {
        settings.getParameters().put(FLOW_STATUS, new Setting(FLOW_STATUS, value.toString()));
    }

    /**
     * Azure
     */
    public String getStartTime() {
        return settings.getParameters().get(START_TIME).getValue();
    }

    /**
     * Azure
     */
    public void setStartTime(String value) {
        settings.getParameters().put(START_TIME, new Setting(START_TIME, value));
    }

    /**
     * Returns service interfaces for BSS web service calls.
     */
    public <T> T getWebService(Class<T> serviceClass) throws Exception {
        return BSSWebServiceFactory.getBSSWebService(serviceClass,
                settings.getAuthentication());
    }

    /**
     * Returns the instance or controller specific technology manager
     * authentication.
     */
    public PasswordAuthentication getTPAuthentication() {
        return settings.getAuthentication();
    }

    /**
     * Returns the locale set as default for the customer organization.
     *
     * @return the customer locale
     */
    public String getCustomerLocale() {
        String locale = settings.getLocale();
        if (StringUtils.isBlank(locale)) {
            locale = "en";
        }
        return locale;
    }

    /**
     * Azure
     */
    public String getConfigurationAsString() {
        StringBuffer details = new StringBuffer();
        details.append("\t\r\nAPIUserName: ");
        details.append(getUserName());
        details.append("\t\r\nTenantId: ");
        details.append(getTenantId());
        details.append("\t\r\nResourceGroupName: ");
        details.append(getResourceGroupName());
        details.append("\t\r\nTemplateUrl: ");
        details.append(getTemplateUrl());
        details.append("\t\r\nTemplateParametersUrl: ");
        details.append(StringUtils.defaultString(getTemplateParametersUrl()));
        details.append("\t\r\nRegion: ");
        details.append(getRegion());
        details.append("\t\r\n");
        return details.toString();
    }
}
