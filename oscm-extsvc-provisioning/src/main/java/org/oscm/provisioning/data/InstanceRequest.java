/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.provisioning.data;

import java.util.List;

/**
 * Provides information on a subscription and customer organization for which an
 * application instance is to be created.
 * 
 */
public class InstanceRequest {

    /**
     * The ID of the customer organization which created the subscription.
     * 
     */
    private String organizationId;
    /**
     * The name of the customer organization which created the subscription.
     */
    private String organizationName;
    /**
     * The name specified by the customer to identify the subscription.
     */
    private String subscriptionId;
    /**
     * The ID specified by the customer to refer the subscription.
     */
    private String referenceId;
    /**
     * The language used by default for interacting with the users of the
     * customer organization.
     */
    private String defaultLocale;
    /**
     * The platform's login page to which the application can redirect users who
     * need to log in. This value is set for the <code>LOGIN</code> access type.
     * <p>
     * Typically, the login page is displayed when a user tries to directly
     * access the application before logging in to the platform first, or when a
     * user needs to log in again because his previous session has timed out.
     */
    private String loginUrl;
    /**
     * The service parameters, which the application can evaluate, for example,
     * in order to activate or deactivate a specific feature.
     */
    private List<ServiceParameter> parameterValue;

    /**
     * The custom attributes for subscriptions, which the application can
     * use, for example, to overrule configuration settings.
     */
    private List<ServiceAttribute> attributeValue;

    /**
     * Retrieves the identifier of the customer organization which created the
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
     * Retrieves the name of the customer organization which created the
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
     * Retrieves the name specified by the customer to identify the
     * subscription.
     * 
     * @return the subscription identifier
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the name specified by the customer to identify the subscription
     * 
     * @param subscriptionId
     *            the subscription identifier
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Retrieves the ID specified by the customer to refer to the subscription.
     * 
     * @return the subscription reference
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the ID specified by the customer to refer to the subscription.
     * 
     * @param referenceId
     *            the subscription reference
     */
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * Retrieves the language used by default for interacting with the users of
     * the customer organization.
     * 
     * @return the language code
     */
    public String getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Sets the language used by default for interacting with the users of the
     * customer organization.
     * 
     * @param defaultLocale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Retrieves the platform's login page to which the application can redirect
     * users who need to log in. This value is set for the <code>LOGIN</code>
     * access types.
     * <p>
     * Typically, the login page is displayed when a user tries to directly
     * access the application before logging in to the platform first, or when a
     * user needs to log in again because his previous session has timed out.
     * 
     * @return the URL of the platform's login page
     */
    public String getLoginUrl() {
        return loginUrl;
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
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    /**
     * Retrieves the service parameters, which the application can evaluate, for
     * example, in order to activate or deactivate a specific feature.
     * 
     * @return the list of service parameters
     */
    public List<ServiceParameter> getParameterValue() {
        return parameterValue;
    }

    /**
     * Sets the service parameters, which the application can evaluate, for
     * example, in order to activate or deactivate a specific feature.
     * 
     * @param parameterValue
     *            the list of service parameters
     */
    public void setParameterValue(List<ServiceParameter> parameterValue) {
        this.parameterValue = parameterValue;
    }

    /**
     * Retrieves the custom attributes for subscriptions, which the
     * application can use, for example, to overrule configuration settings.
     * 
     * @return the list of customer attributes
     */
    public List<ServiceAttribute> getAttributeValue() {
        return attributeValue;
    }

    /**
     * Sets the custom attributes for subscriptions, which the application
     * can use, for example, to overrule configuration settings.
     * 
     * @param attributeValue
     *            the list of customer attributes
     */
    public void setAttributeValue(List<ServiceAttribute> attributeValue) {
        this.attributeValue = attributeValue;
    }

}
