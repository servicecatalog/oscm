/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  OpenStack controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2013-11-29                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.oscm.app.openstack.data.FlowState;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.v2_0.BSSWebServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PropertyHandler.class);

    private final ProvisioningSettings settings;

    public static final String STACK_NAME = "STACK_NAME";
    public static final String STACK_ID = "STACK_ID";
    public static final String STACK_NAME_PATTERN = "STACK_NAME_PATTERN";

    // Name (not id) of the domain (if omitted, it is taken from
    // controller configuration)
    public static final String DOMAIN_NAME = "DOMAIN_NAME";

    // Default name of Domain
    private static final String DEFAULT_DOMAIN = "default";

    // URL of Heat template
    public static final String TEMPLATE_NAME = "TEMPLATE_NAME";

    // AccessInfo creation pattern
    public static final String ACCESS_INFO_PATTERN = "ACCESS_INFO_PATTERN";

    // Base URL of Heat templates
    public static final String TEMPLATE_BASE_URL = "TEMPLATE_BASE_URL";

    // Prefix for template parameters
    public static final String TEMPLATE_PARAMETER_PREFIX = "TP_";

    public static final String KEYSTONE_API_URL = "KEYSTONE_API_URL";
    public static final String API_USER_NAME = "API_USER_NAME";
    public static final String API_USER_PWD = "API_USER_PWD";

    /**
     * Defines whether manual steps are required before activation the
     * deployment for the customer.
     */
    public static final String MAIL_FOR_COMPLETION = "MAIL_FOR_COMPLETION";

    /**
     * The internal status of a provisioning operation as set by the controller
     * or the status dispatcher.
     */
    public static final String STATUS = "STATUS";

    // ID of the tenant/project
    public static final String TENANT_ID = "TENANT_ID";

    // Timeout for status check (msec)
    public static final String READY_TIMEOUT = "READY_TIMEOUT";

    // Start time of operation
    public static final String START_TIME = "START_TIME";

    public static final String TEMPLATE_PARAMETER_ARRAY_PREFIX = "TP_ARRAY_";

    /**
     * Default constructor.
     * 
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * 
     */
    public PropertyHandler(ProvisioningSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns the internal state of the current provisioning operation as set
     * by the controller or the dispatcher.
     * 
     * @return the current status
     */
    public FlowState getState() {
        String status = getValue(STATUS, settings.getParameters());
        return (status != null) ? FlowState.valueOf(status) : FlowState.FAILED;
    }

    /**
     * Changes the internal state for the current provisioning operation.
     * 
     * @param newState
     *            the new state to set
     */
    public void setState(FlowState newState) {
        setValue(STATUS, newState.toString(), settings.getParameters());
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
     * Returns the name of the stack (=instance identifier).
     * 
     * @return the name of the stack
     */
    public String getStackName() {
        return getValidatedProperty(settings.getParameters(), STACK_NAME);
    }

    public void setStackName(String stackName) {
        setValue(STACK_NAME, stackName, settings.getParameters());
    }

    /**
     * Returns the regex for the stack name
     * 
     * @return the regular expression
     */
    public String getStackNamePattern() {
        return getValue(STACK_NAME_PATTERN, settings.getParameters());
    }

    /**
     * Returns the heat specific id of the stack.
     * 
     * @return the id of the stack
     */
    public String getStackId() {
        return getValue(STACK_ID, settings.getParameters());
    }

    public void setStackId(String stackId) {
        setValue(STACK_ID, stackId, settings.getParameters());
    }

    /**
     * Returns the access information pattern used to created the instance
     * access information using the output parameters of the created stack.
     * 
     * @return the access information pattern
     */
    public String getAccessInfoPattern() {
        return getValidatedProperty(settings.getParameters(),
                ACCESS_INFO_PATTERN);
    }

    /**
     * Returns the name of the template to be used for provisioning.
     * 
     * @return the template name
     */
    public String getTemplateName() {
        return getValidatedProperty(settings.getParameters(), TEMPLATE_NAME);
    }

    /**
     * Returns the URL of the template to be used for provisioning.
     * 
     * @return the template URL
     */
    public String getTemplateUrl() throws HeatException {

        try {
            String url = getValidatedProperty(settings.getParameters(),
                    TEMPLATE_NAME);

            String baseUrl = getValue(TEMPLATE_BASE_URL,
                    settings.getParameters());
            if (baseUrl == null || baseUrl.trim().length() == 0) {
                baseUrl = getValidatedProperty(settings.getConfigSettings(),
                        TEMPLATE_BASE_URL);
            }
            return new URL(new URL(baseUrl), url).toExternalForm();
        } catch (MalformedURLException e) {
            throw new HeatException(
                    "Cannot generate template URL: " + e.getMessage());
        }
    }

    /**
     * Returns the domain name that defines the context for the provisioning. It
     * can either be defined within the controller settings of as instance
     * parameter. When present, the service parameter is preferred.
     * 
     * @return the domain name
     */
    public String getDomainName() {
        String domain = getValue(DOMAIN_NAME, settings.getParameters());
        if (domain == null || domain.trim().length() == 0) {
            domain = getValue(DOMAIN_NAME, settings.getConfigSettings());
            if (domain == null || domain.trim().length() == 0) {
                domain = DEFAULT_DOMAIN;
            }
        }
        return domain;
    }

    public JSONObject getTemplateParameters() {
        JSONObject parameters = new JSONObject();
        // created security Group array , user can add security group separated
        // by comma
        JSONArray securityGroupSecurityGroup = new JSONArray();
        Set<String> keySet = settings.getParameters().keySet();
        String securityGroup = null;
        try {

            for (String key : keySet) {
                if (key.startsWith(TEMPLATE_PARAMETER_PREFIX)) {
                    if (key.startsWith(TEMPLATE_PARAMETER_ARRAY_PREFIX)) {
                        // below if execute only if technical service parameter
                        // have a
                        // security group parameters
                        securityGroup = key.substring(
                                TEMPLATE_PARAMETER_ARRAY_PREFIX.length());
                        String securityGroupArray[] = settings.getParameters()
                                .get(key).getValue().split(",");
                        for (String groupName : securityGroupArray) {
                            securityGroupSecurityGroup.put(groupName);
                        }
                        parameters.put(securityGroup,
                                securityGroupSecurityGroup);

                    } else {
                        parameters.put(
                                key.substring(
                                        TEMPLATE_PARAMETER_PREFIX.length()),
                                settings.getParameters().get(key).getValue());
                    }
                }

            }
            // remove the empty parameter from object
            parameters.remove("");
        } catch (JSONException e) {
            // should not happen with Strings
            throw new RuntimeException(
                    "JSON error when collection template parameters", e);
        }
        return parameters;
    }

    /**
     * Reads the requested property from the available parameters. If no value
     * can be found, a RuntimeException will be thrown.
     * 
     * @param sourceProps
     *            The property object to take the settings from
     * @param key
     *            The key to retrieve the setting for
     * @return the parameter value corresponding to the provided key
     */
    private String getValidatedProperty(Map<String, Setting> sourceProps,
            String key) {
        String value = getValue(key, sourceProps);
        if (value == null) {
            String message = String.format("No value set for property '%s'",
                    key);
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
        return value;
    }

    /**
     * Return the URL of the Keystone API which acts as entry point to all other
     * API endpoints.
     * 
     * @return the Keystone URL
     */
    public String getKeystoneUrl() {

        String keystoneURL = getValue(KEYSTONE_API_URL,
                settings.getParameters());
        if (keystoneURL == null || keystoneURL.trim().length() == 0) {
            keystoneURL = getValidatedProperty(settings.getConfigSettings(),
                    KEYSTONE_API_URL);
        }
        return keystoneURL;
    }

    /**
     * Returns the configured password for API usage.
     * 
     * @return the password
     */
    public String getPassword() {
        return getValidatedProperty(settings.getConfigSettings(), API_USER_PWD);
    }

    /**
     * Returns the configured user name for API usage.
     * 
     * @return the user name
     */
    public String getUserName() {
        return getValidatedProperty(settings.getConfigSettings(),
                API_USER_NAME);
    }

    /**
     * Returns the mail address to be used for completion events (provisioned,
     * deleted). If not set, no events are required.
     * 
     * @return the mail address or <code>null</code> if no events are required
     */
    public String getMailForCompletion() {
        String value = getValue(MAIL_FOR_COMPLETION, settings.getParameters());
        if (value == null || value.trim().length() == 0) {
            value = null;
        }
        return value;
    }

    public String getStackConfigurationAsString() throws HeatException {
        StringBuffer details = new StringBuffer();
        details.append("\t\r\nStackName: ");
        details.append(getStackName());
        details.append("\t\r\nStackId: ");
        details.append(getStackId());
        details.append("\t\r\nAPIUserName: ");
        details.append(getUserName());
        details.append("\t\r\nKeystoneAPIUrl: ");
        details.append(getKeystoneUrl());
        details.append("\t\r\nTenantID: ");
        details.append(getTenantId());
        details.append("\t\r\nDomainName: ");
        details.append(getDomainName());
        details.append("\t\r\nTemplateUrl: ");
        details.append(getTemplateUrl());
        details.append("\t\r\nAccessInfoPattern: ");
        details.append(getAccessInfoPattern());
        details.append("\t\r\n");
        return details.toString();
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
        if (locale == null || locale.trim().length() == 0) {
            locale = "en";
        }
        return locale;
    }

    /**
     * Returns the tenant id that defines the context for the provisioning.
     * 
     * @return the tenant id
     */
    public String getTenantId() {
        String tenant = getValue(TENANT_ID, settings.getParameters());
        if (tenant == null || tenant.trim().length() == 0) {
            tenant = getValidatedProperty(settings.getConfigSettings(),
                    TENANT_ID);
        }
        return tenant;
    }

    /**
     * Set start time of operation
     * 
     * @param time
     */
    public void setStartTime(String time) {
        setValue(START_TIME, time, settings.getParameters());
    }

    /**
     * Return the start time of operation
     * 
     * @return the start time of string
     */
    public String getStartTime() {
        return getValue(START_TIME, settings.getParameters());
    }

    /**
     * Return the ready timeout which is waiting time of status changing If
     * number is not corrected, return 0
     * 
     * @return timeout value of long
     */
    public long getReadyTimeout() {
        String readyTimeout = getValue(READY_TIMEOUT,
                settings.getConfigSettings());
        if (readyTimeout == null || readyTimeout.trim().length() == 0) {
            LOGGER.warn("'READY_TIMEOUT' is not set and therefore ignored");
            return 0;
        }
        try {
            return Long.parseLong(
                    getValue(READY_TIMEOUT, settings.getConfigSettings()));
        } catch (NumberFormatException ex) {
            LOGGER.warn(
                    "Wrong value set for property 'READY_TIMEOUT' and therefore ignored");
        }
        return 0;

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
