/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2012-08-06                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper object for service parameters and configuration settings. Service
 * parameters are defined at the technical service for an application; values
 * for the parameters may be specified at subscriptions and evaluated by the
 * application. Configuration settings are the settings and values defined for
 * an application-specific service controller; they can also be evaluated by the
 * application.
 */
public class ProvisioningSettings extends ControllerSettings
        implements Serializable {

    private static final long serialVersionUID = -1572408823477281540L;

    private static final String BSS_PREFIX = "BSS_";

    private String locale;
    private HashMap<String, Setting> parameters;
    private HashMap<String, Setting> attributes;
    private HashMap<String, Setting> customAttributes;
    private String organizationId;
    private String organizationName;
    private String subscriptionId;
    private String referenceId;
    private String besLoginURL;
    private ServiceUser requestingUser;
    private String serviceAccessInfo;

    /**
     * Constructs a new provisioning settings instance with the given service
     * parameters and controller configuration settings. The specified locale is
     * used for language-dependent strings.
     * 
     * @param parameters
     *            the service parameters, consisting of a key and a value each
     * @param configSettings
     *            the configuration settings, consisting of a key and a value
     *            each
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public ProvisioningSettings(HashMap<String, Setting> parameters,
            HashMap<String, Setting> configSettings, String locale) {

        this(parameters, new HashMap<String, Setting>(),
                new HashMap<String, Setting>(), configSettings, locale);
    }

    /**
     * Constructs a new provisioning settings instance with the given service
     * parameters, attributes, custom attributes and controller configuration
     * settings. The specified locale is used for language-dependent strings.
     * 
     * @param parameters
     *            the service parameters, consisting of a key and a value each
     * @param attributes
     *            the service attributes, consisting of a key and a value each
     * @param customAttributes
     *            the custom attributes, consisting of a key and a value each
     * @param configSettings
     *            the configuration settings, consisting of a key and a value
     *            each
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public ProvisioningSettings(HashMap<String, Setting> parameters,
            HashMap<String, Setting> attributes,
            HashMap<String, Setting> customAttributes,
            HashMap<String, Setting> configSettings, String locale) {
        super(configSettings);
        this.parameters = parameters;
        this.locale = locale;
        this.attributes = attributes;
        this.customAttributes = customAttributes;
    }

    /**
     * Returns a list of the service parameters.
     * 
     * @return the service parameters, consisting of a key and a value each
     */
    public HashMap<String, Setting> getParameters() {
        return parameters;
    }

    /**
     * Sets the service parameters.
     * 
     * @param parameters
     *            the service parameters, consisting of a key and a value each
     */
    public void setParameters(HashMap<String, Setting> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns a list of the instance attributes.
     * 
     * @return the instance attributes, consisting of a key and a value each
     */
    public HashMap<String, Setting> getAttributes() {
        return attributes;
    }

    /**
     * Sets the instance attributes.
     * 
     * @param attributes
     *            the instance attributes, consisting of a key and a value each
     */
    public void setAttributes(HashMap<String, Setting> attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns a list of the custom attributes.
     * 
     * @return the custom attributes, consisting of a key and a value each
     */
    public HashMap<String, Setting> getCustomAttributes() {
        return customAttributes;
    }

    /**
     * Sets the custom attributes.
     * 
     * @param customAttributes
     *            the custom attributes, consisting of a key and a value each
     */
    public void setCustomAttributes(HashMap<String, Setting> customAttributes) {
        this.customAttributes = customAttributes;
    }

    /**
     * Returns the locale used for language-dependent strings.
     * 
     * @return the language code
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Returns the identifier of the customer organization which created the
     * subscription.
     * 
     * @return the organization ID
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the identifier of the customer organization which created the
     * subscription.
     * 
     * @param organizationId
     *            the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Returns the name of the customer organization which created the
     * subscription.
     * 
     * @return the organization name
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Sets the name of the customer organization which created the
     * subscription.
     * 
     * @param organizationName
     *            the organization name
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * Returns the name specified by the customer to identify the subscription.
     * 
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Returns the name specified by the customer to identify the subscription.
     * 
     * @return the subscription ID
     */
    public String getOriginalSubscriptionId() {
        int index = subscriptionId.indexOf("#");
        if (index >= 0) {
            return subscriptionId.substring(0, index);
        } else {
            return subscriptionId;
        }
    }

    /**
     * Sets the name specified by the customer to identify the subscription.
     * 
     * @param subscriptionId
     *            the subscription ID
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Returns the id specified by the customer to refer to the subscription.
     * 
     * @return the referenceId
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the id specified by the customer to refer to the subscription.
     * 
     * @param referenceId
     *            the referenceId to set
     */
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * Returns the platform's login page to which the application can redirect
     * users who need to log in. This value is set for the <code>LOGIN</code>
     * access types.
     * <p>
     * Typically, the login page is displayed when a user tries to directly
     * access the application before logging in to the platform first, or when a
     * user needs to log in again because his previous session has timed out.
     * 
     * @return the login URL
     */
    public String getBesLoginURL() {
        return besLoginURL;
    }

    /**
     * Sets the platform's login page to which the application can redirect
     * users who need to log in. This value is set for the <code>LOGIN</code>
     * access type.
     * <p>
     * Typically, the login page is displayed when a user tries to directly
     * access the application before logging in to the platform first, or when a
     * user needs to log in again because his previous session has timed out.
     * 
     * @param loginUrl
     *            the URL of the platform's login page
     */
    public void setBesLoginUrl(String loginUrl) {
        this.besLoginURL = loginUrl;
    }

    /**
     * <p>
     * If APP calls createInstance() of the controller, returns the user who
     * requested the current provisioning operation.
     * </p>
     * 
     * <p>
     * If a timer calls the controller to switch from one status to the other,
     * returns 'null'.
     * </p>
     * 
     * @return the user or 'null'
     */
    public ServiceUser getRequestingUser() {
        return requestingUser;
    }

    /**
     * Set the user who requested a the current provisioning operation.
     * 
     * @param user
     *            the user
     */
    public void setRequestingUser(ServiceUser user) {
        this.requestingUser = user;
    }

    /**
     * Returns the service access information for instance.
     * 
     * @return service access information for instance.
     */
    public String getServiceAccessInfo() {
        return serviceAccessInfo;
    }

    /**
     * Sets the service access information for instance.
     * 
     * @param serviceAccessInfo
     */
    public void setServiceAccessInfo(String serviceAccessInfo) {
        this.serviceAccessInfo = serviceAccessInfo;
    }

    /**
     * Replaces properties of configuration settings or parameters with values
     * from attributes or parameters. In the case of attributes a prefix is used
     * which is the controller id and an underscore. Properties will only be
     * overwritten if the source value is not null or empty.
     * 
     * @param controllerId
     *            the id of the controller the settings are for.
     */
    public void overwriteProperties(String controllerId) {

        overwriteProperties(getParameters(), Arrays.asList(getConfigSettings()),
                null);
        overwriteProperties(getCustomAttributes(),
                Arrays.asList(getParameters(), getConfigSettings()),
                controllerId);
        overwriteProperties(getAttributes(),
                Arrays.asList(getParameters(), getConfigSettings()),
                controllerId);
    }

    private void overwriteProperties(HashMap<String, Setting> source,
            List<HashMap<String, Setting>> targets, String controllerId) {

        for (Map<String, Setting> target : targets) {
            for (String key : source.keySet()) {

                if (key != null && target.containsKey(key)) {

                    Setting sourceSetting = source.get(key);

                    if (sourceSetting != null
                            && sourceSetting.getValue() != null
                            && sourceSetting.getValue().trim().length() > 0
                            && !sourceSetting.getValue().startsWith(BSS_PREFIX)
                            && (controllerId == null || controllerId
                                    .equals(sourceSetting.getControllerId()))) {

                        Setting newSetting = new Setting(key,
                                sourceSetting.getValue());

                        newSetting.setEncrypted(sourceSetting.isEncrypted());
                        target.put(key, newSetting);
                    }
                }
            }
        }

    }

}
