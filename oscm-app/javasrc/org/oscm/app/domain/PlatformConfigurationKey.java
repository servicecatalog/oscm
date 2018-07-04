/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.domain;

/**
 * Enumeration of all globally defined configuration keys.
 */
public enum PlatformConfigurationKey {

    /**
     * Defines the base URL of the asynchronous provisioning platform.
     */
    APP_BASE_URL("url"),

    /**
     * The timer interval to be used by the proxy to poll for the services. Unit
     * is milliseconds.
     */
    APP_TIMER_INTERVAL("long"),

    /**
     * The interval (in milliseconds) at which APP polls the status of instances
     * and updates respective subscription parameters, e.g. the number of VMs.
     */
    APP_TIMER_REFRESH_SUBSCRIPTIONS("long"),
    
    /**
     * The interval (in milliseconds) at which APP updates the usage data of instances
     */
    APP_TIMER_REFRESH_USAGEDATA(false, "long"),


    /**
     * Defines the mail resource to be used for communication.
     */
    APP_MAIL_RESOURCE(false, "string"),

    /**
     * The URL base to the BSS service endpoint locations.
     */
    BSS_WEBSERVICE_URL("url"),

    /**
     * The URL base to the BSS service WSDL locations.
     */
    BSS_WEBSERVICE_WSDL_URL("url"),

    /**
     * The key of the user to access BSS on behalf of the APP.
     */
    BSS_USER_KEY("string"),

    /**
     * The ID of the user to access BSS on behalf of the APP.
     */
    BSS_USER_ID(false, "string"),

    /**
     * The password of the user to access BSS on behalf of the APP.
     */
    BSS_USER_PWD("string"),

    /**
     * Defines the default e-mail address of the administrator which will
     * receive critical error mails.
     */
    APP_ADMIN_MAIL_ADDRESS("mail"),

    /**
     * The BSS authentication mode (INTERNAL, SAML_SP, SAML_IDP, OPENID_RP)
     */
    BSS_AUTH_MODE("string"),

    /**
     * The URL base to the BSS service endpoint locations for SAML_SP
     * authentication mode.
     */
    BSS_STS_WEBSERVICE_URL(false, "url"),

    /**
     * The URL base to the BSS service WSDL locations for SAML_SP authentication
     * mode.
     */
    BSS_STS_WEBSERVICE_WSDL_URL(false, "url"),

    /**
     * The keystore password.
     */
    APP_KEYSTORE_PASSWORD(false, "string"),

    /**
     * The location of the app truststore
     */
    APP_TRUSTSTORE("string"),

    /**
     * The alias of the public bss certificate
     */
    APP_TRUSTSTORE_BSS_ALIAS("string"),

    /**
     * The truststore password.
     */
    APP_TRUSTSTORE_PASSWORD("string"),

    /**
     * The suspend flag of APP.
     */
    APP_SUSPEND(false, "boolean"),

    /**
     * The path to the file with the encryption key
     */
    APP_KEY_PATH("string");

    private boolean isMandatory;

    private String type;

    public final static String TYPE_LONG = "long";
    public final static String TYPE_STRING = "string";
    public final static String TYPE_URL = "url";
    public final static String TYPE_MAIL = "mail";
    public final static String TYPE_BOOLEAN = "boolean";

    private PlatformConfigurationKey(String type) {
        this.isMandatory = true;
        this.type = type;
    }

    private PlatformConfigurationKey(boolean mandatory, String type) {
        this.isMandatory = mandatory;
        this.type = type;
    }

    /**
     * Indicates whether the configuration parameter is mandatory.
     * 
     * @return <code>true</code> if the configuration of the proxy is incomplete
     *         without setting this parameter
     */
    public boolean isMandatory() {
        return isMandatory;
    }

    public String getType() {
        return type;
    }
}
