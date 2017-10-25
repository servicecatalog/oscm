/*******************************************************************************
 * 
 *  Copyright FUJITSU LIMITED 2017
 * 
 *  Author: Mike J&auml;ger
 * 
 *  Creation Date: 22.01.2009
 * 
 *  Completion Time: 17.06.2009
 * 
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

import java.io.PrintStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import org.oscm.internal.types.constants.HiddenUIConstants;

/**
 * Represents the different types configuration keys available in the platform.
 * 
 */
public enum ConfigurationKey {

    @Doc({ "Optional: Special character encoding for emails sent in japanese",
            "locale. Default is UTF-8." })
    MAIL_JA_CHARSET(false, "UTF-8", "string"),

    @Doc({ "The maximum number of login attempts. If a user does not log in",
            "successfully with this number of attempts, his account is locked.",
            "The default is 3." })
    MAX_NUMBER_LOGIN_ATTEMPTS(false, "3", "long", Long.valueOf(1L), null, false),

    @Doc({ "The maximum number of entries that will be returned by an LDAP",
            "query. The default is 100." })
    LDAP_SEARCH_LIMIT(false, "100", "long", Long.valueOf(1L), null, false),

    @Doc({ "The base URL to access the platform's landing pages, especially",
            "required to create the URL for accessing the confirmation page." })
    @Example("http://<your server>:<your port>/oscm-portal")
    BASE_URL(false, null, "url"),

    @Doc({ "The base URL for HTTPS. Required to create the URL for accessing",
            " services via HTTPS" })
    @Example("https://<your server>:<your port>/oscm-portal")
    BASE_URL_HTTPS(true, null, "url"),

    @Doc({ "URL to specify a Web page that is to be displayed in case the HTTP",
            "protocol is used and a customer tries to access a marketplace",
            "without a valid marketplace ID." })
    @Example("http://<your server>:<your port>/oscm-portal/marketplace/index.jsf?mId=8f78f156")
    MP_ERROR_REDIRECT_HTTP(false, null, "url"),

    @Doc({ "URL to specify a Web page that is to be displayed in case the HTTPS",
            "protocol is used and a customer tries to access a marketplace",
            "without a valid marketplace ID." })
    @Example("https://<your server>:<your port>/oscm-portal/marketplace/index.jsf?mId=8f78f156")
    MP_ERROR_REDIRECT_HTTPS(false, null, "url"),

    @Doc({ "The log level valid for the entire application." })
    LOG_LEVEL(false, "INFO", "string"),

    @Doc({ "The path to the log files." })
    @Example("../logs")
    LOG_FILE_PATH(true, "../logs", "string"),

    @Doc({ "The path to the log4j configuration file." })
    LOG_CONFIG_FILE(false, "./log4j.properties", "string"),

    @Doc({ "The path to the file containing the key required for encrypting and",
           "decrypting service parameters and custom attributes." })
    @Example("./key")
    KEY_FILE_PATH(false, "./key", "string", false),

    @Doc({ "The number of decimal places for prices. Values between 2 and 6 are supported." })
    @Example("4")
    DECIMAL_PLACES(false, "2", "long", Long.valueOf(2L), Long.valueOf(6L), false),

    @Doc({ "Specifies whether the customer self-registration is used for the",
            "current environment." })
    @Example("false")
    CUSTOMER_SELF_REGISTRATION_ENABLED(false, "true", "boolean"),

    @Doc({ "The maximum time until an organization's initial administrative",
            "account must be confirmed. After this time has passed, the",
            "account may be removed. The value is in milliseconds, the default",
            "is seven days." })
    PERMITTED_PERIOD_UNCONFIRMED_ORGANIZATIONS(false, "604800000", "long", Long.valueOf(1L), null, false),

    @Doc({ "The time interval at which tasks related to organizations are",
            "executed. The value is in milliseconds. A value of 0 indicates",
            "that this timer is disabled." })
    TIMER_INTERVAL_ORGANIZATION(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "The offset of the timer for organization-related tasks.",
            "The value is in milliseconds, based on January 1, 00:00." })
    TIMER_INTERVAL_ORGANIZATION_OFFSET(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "The time interval at which a check for expired subscriptions",
            "is executed. The value is in milliseconds, the default is 24 hours.",
            "The value must be greater than 0." })
    TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION(false, "86400000", "long", Long.valueOf(1L), null, false),

    @Doc({ "The offset of the timer for subscription expiration checks.",
            "The value is in milliseconds, based on January 1, 00:00." })
    TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION_OFFSET(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "The time interval at which a check for timed-out pending",
            "subscriptions is executed. The value is in milliseconds.",
            "A value of 0 indicates that this timer is disabled." })
    TIMER_INTERVAL_TENANT_PROVISIONING_TIMEOUT(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "The offset of the timer for pending subscription checks.",
            "The value is in milliseconds, based on January 1, 00:00." })
    TIMER_INTERVAL_TENANT_PROVISIONING_TIMEOUT_OFFSET(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "The offset of the timer for terminating the discounts",
            "for all organizations. The timer interval is one day.",
            "The offset is in milliseconds, based on January 1, 00:00." })
    TIMER_INTERVAL_DISCOUNT_END_NOTIFICATION_OFFSET(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "The URL template of the report engine. If this value is not",
            "configured, reporting will not be available.",
            "It supports http and https to show report.",
            "But your server name must use fully qualified domain name when using https to show report." })
    @Example("http://<your server>:<your port>/birt/frameset?__report=${reportname}" + ".rptdesign&SessionId=${sessionid}&__locale=${locale}&WSDLURL=${wsdlurl}&SOAPEndPoint=${soapendpoint}&wsname=Report&wsport=ReportPort or https://<your server>:<your " + "port>/birt/frameset?__report=${reportname}.rptdesign&SessionId=${sessionid}&__locale=${locale}&WSDLURL=${wsdlurl}&SOAPEndPoint=${soapendpoint}&wsname=ReportSecure" + "&wsport=ReportSecurePort")
    REPORT_ENGINEURL(false, "", "url"),

    @Doc({ "The WSDL link to the reporting Web service. This value",
            "has to be configured when REPORT_ENGINEURL is set.",
            "It supports http and https to show report.",
            "But your server name must use fully qualified domain name when using https to show report." })
    @Example("http://<your server>:<your port>/Report/ReportingServiceBean?wsdl or https://<your server>:<your port>/ReportSecure/ReportingServiceSecureBean?wsdl")
    REPORT_WSDLURL(false, "", "url"),

    @Doc({ "The SOAP end point of the reporting Web service. This value",
            "has to be configured when REPORT_ENGINEURL is set.",
            "It supports http and https to show report.",
            "But your server name must use fully qualified domain name when using https to show report." })
    @Example("http://<your server>:<your port>/Report/ReportingServiceBean or https://<your server>:<your port>/ReportSecure/ReportingServiceSecureBean")
    REPORT_SOAP_ENDPOINT(false, "", "url"),

    @Doc({ "The proxy to be used for HTTP connections, if any." })
    HTTP_PROXY(false, "", "string"),

    @Doc({ "The proxy port to be used for HTTP connections, if any." })
    HTTP_PROXY_PORT(false, "1080", "long", Long.valueOf(1L), Long.valueOf(65535L), false),

    @Doc({ "Specifies whether the PSP integration is used for the",
            "current environment." })
    @Example("false")
    PSP_USAGE_ENABLED(true, "false", "boolean"),

    @Doc({ "The billing run offset and the timer's expiration time.",
            "The value is configured in milliseconds.",
            "Values greater then 28 days will be set to 28 days with expiration time 00:00:00.0000",
            "The milliseconds are converted into day, hours, minutes, seconds, milliseconds.",
            "To determine the billing-run-offset the day-value will be used.",
            "To determine the billing-run-start-time the hour-, minute-, second-, millisecond-values will be used.",
            "The default billing-run-offset is 4 days, i.e. billing starts 4 days after period end.",
            "The default billing-run-start-time is 00:00:00.0000" })
    TIMER_INTERVAL_BILLING_OFFSET(false, "345600000", "long", Long.valueOf(0L), null, false),

    @Doc({ "The list of menus and groups of fields in dialogs, which are",
            "hidden. These values are allowed:",
            HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PROFILE,
            HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PAYMENT,
            HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_SUBSCRIPTIONS,
            HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_USERS,
            HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_REPORTS,
            HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_PROCESSES,
            HiddenUIConstants.MARKETPLACE_MENU_ITEM_ACCOUNT_OPERATIONS,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_LINK,
            HiddenUIConstants.MENU_ITEM_OPERATOR_CREATE_ORGANIZATION,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_ORGANIZATIONS,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_OPERATOR_REVENUE_SHARE,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_BILLING_ADAPTERS,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_TRIGGERS,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_TIMERS,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CURRENCIES,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_CONFIGURATION,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_INDEXES,
            HiddenUIConstants.MENU_ITEM_OPERATOR_MANAGE_TENANTS,
            HiddenUIConstants.MENU_GROUP_NAVIGATION_MYACCOUNT,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_EDIT,
            HiddenUIConstants.MENU_ITEM_USER_PWD,
            HiddenUIConstants.MENU_ITEM_USER_ADD,
            HiddenUIConstants.MENU_ITEM_USER_IMPORT,
            HiddenUIConstants.MENU_ITEM_USER_LIST,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_PAYMENT,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_REPORT,
            HiddenUIConstants.MENU_ITEM_TRIGGER_PROCESS_LIST,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_SUPPLIERS,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_UDAS,
            HiddenUIConstants.MENU_GROUP_NAVIGATION_MARKETPLACE,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_SUPPLIERS,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_CREATE,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_UPDATE,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_DELETE,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_BROKER_REVENUE_SHARE,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_MANAGE_RESELLER_REVENUE_SHARE,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_EDIT_SKIN,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_TRANSLATION,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_EDIT_STAGE,
            HiddenUIConstants.MENU_ITEM_MARKETPLACE_CUSTOMIZE_BRAND,
            HiddenUIConstants.MENU_GROUP_NAVIGATION_CUSTOMER,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_ADD_CUSTOMER,
            HiddenUIConstants.MENU_ITEM_EDIT_CUSTOMER,
            HiddenUIConstants.MENU_ITEM_VIEW_CUSTOMER,
            HiddenUIConstants.MENU_ITEM_MANAGE_COUNTRIES,
            HiddenUIConstants.MENU_ITEM_MANAGE_VAT,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_MANAGE_PAYMENT_ENABLEMENT,
            HiddenUIConstants.MENU_ITEM_ORGANIZATION_EXPORT_BILLING_DATA,
            HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_VIEW,
            HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_EDIT_UDAS,
            HiddenUIConstants.MENU_ITEM_SUBSCRIPTION_TERMINATE,
            HiddenUIConstants.MENU_GROUP_NAVIGATION_TECHSERVICE,
            HiddenUIConstants.MENU_ITEM_TECHSERVICE_ADD,
            HiddenUIConstants.MENU_ITEM_TECHSERVICE_IMPORT,
            HiddenUIConstants.MENU_ITEM_TECHSERVICE_EDIT,
            HiddenUIConstants.MENU_ITEM_TECHSERVICE_EXPORT,
            HiddenUIConstants.MENU_ITEM_TECHSERVICE_DELETE,
            HiddenUIConstants.MENU_ITEM_TECHSERVICE_VIEW_ADAPTERS,
            HiddenUIConstants.MENU_GROUP_NAVIGATION_SERVICE,
            HiddenUIConstants.MENU_ITEM_SERVICE_ADD,
            HiddenUIConstants.MENU_ITEM_SERVICE_EDIT,
            HiddenUIConstants.MENU_ITEM_SERVICE_COPY,
            HiddenUIConstants.MENU_ITEM_SERVICE_DELETE,
            HiddenUIConstants.MENU_ITEM_SERVICE_VIEW,
            HiddenUIConstants.MENU_ITEM_SERVICE_MANAGE,
            HiddenUIConstants.MENU_ITEM_SERVICE_UPGRADE_OPTIONS,
            HiddenUIConstants.MENU_ITEM_SERVICE_ACTIVATION,
            HiddenUIConstants.MENU_ITEM_SERVICE_PUBLISH,
            HiddenUIConstants.MENU_GROUP_NAVIGATION_PRICE_MODEL,
            HiddenUIConstants.MENU_ITEM_PRICE_MODEL_SERVICE,
            HiddenUIConstants.MENU_ITEM_PRICE_MODEL_CUSTOMER,
            HiddenUIConstants.MENU_ITEM_PRICE_MODEL_DELETE,
            HiddenUIConstants.MENU_ITEM_PRICE_MODEL_SUBSCRIPTION,
            HiddenUIConstants.PANEL_ORGANIZATION_EDIT_ORGANIZATIONDATA,
            HiddenUIConstants.PANEL_ORGANIZATION_EDIT_USERPROFILE,
            HiddenUIConstants.PANEL_USER_LIST_SUBSCRIPTIONS, })
    @Example("operator.manageBillingAdapters,techService.viewBillingAdapters")
    HIDDEN_UI_ELEMENTS(false, "", "string"),

    @Doc({ "Specifies whether the supplier sets the payment type invoice as default payment info for his customers." })
    SUPPLIER_SETS_INVOICE_AS_DEFAULT(false, "false", "boolean"),

    @Doc({ "If a time zone different to 'GMT' should be used for displaying purposes, use this setting to specify the wanted time zone id." })
    TIME_ZONE_ID(false, "", "string"),

    @Doc({ "The maximum number of tags composing the tag cloud." })
    @Example("20")
    TAGGING_MAX_TAGS(true, null, "long", Long.valueOf(0L), Long.valueOf(Integer.MAX_VALUE), false),

    @Doc({ "A threshold can be set so that tags that don't reach a certain score will not be shown in the tag cloud." })
    @Example("1")
    TAGGING_MIN_SCORE(true, null, "long", Long.valueOf(1L), Long.valueOf(Integer.MAX_VALUE), false),

    @Doc({ "The maximum time until an inactive on-behalf  user will be removed from the system."
            + "The value is in milliseconds, the default is seven days." })
    PERMITTED_PERIOD_INACTIVE_ON_BEHALF_USERS(false, "604800000", "long", Long.valueOf(1L), null, false),

    @Doc({ "The time interval at which a check for inactive on-behalf users",
            "is executed. The value is in milliseconds. A value of 0 indicates",
            "that this timer is disabled." })
    TIMER_INTERVAL_INACTIVE_ON_BEHALF_USERS(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "The offset of the timer for removing inactive on-behalf users.",
            "The offset is in milliseconds, based on January 1, 00:00." })
    TIMER_INTERVAL_INACTIVE_ON_BEHALF_USERS_OFFSET(false, "0", "long", Long.valueOf(0L), null, false),

    @Doc({ "Defines the timeout for outgoing JAX-WS calls. After the time intervall is passed a timeout exception is thrown by the JAX-WS framework." })
    @Example("30000")
    WS_TIMEOUT(true, "30000", "long", Long.valueOf(1L), null, false),

    @Doc({ "The authentication mode defines whether the bes internal authentication is used",
            "or an external authentication service, such as OpenAM.",
            "Default value is INTERNAL. Allowed values are: INTERNAL, SAML_SP" })
    @Example("INTERNAL")
    AUTH_MODE(true, "INTERNAL", "string", true),

    @Doc({ "URL to the SAML Identity Provider (IdP) used by CT-MG. To be considered only if the configuration setting AUTH_MODE is set ",
            "to any other option than INTERNAL." })
    @Example("https://<host>:<port>/<RedirectServiceEndpoint>")
    SSO_IDP_URL(false, null, "url"),

    @Doc({ "The http method (GET or POST) used for the authentication request to the SAML Identity Provider (IdP)." })
    @Example("POST")
    SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD(false, "POST", "string"),

    @Doc({ "Path to the trusstore holding the IdP certificate." })
    @Example("<path>/cacerts.jks")
    SSO_IDP_TRUSTSTORE(false, null, "string"),

    @Doc({ "Password of the IdP truststore." })
    @Example("changeit")
    SSO_IDP_TRUSTSTORE_PASSWORD(false, null, "password"),

    @Doc({ "Unique identifier of the requesting service provider for the SAML Identity Provider (IdP)." })
    @Example("CT_MG")
    SSO_ISSUER_ID(false, null, "string"),

    @Doc({ "Encryption key length for STS." })
    @Example("128")
    SSO_STS_ENCKEY_LEN(false, null, "long", Long.valueOf(1L), Long.valueOf(Integer.MAX_VALUE), false),

    @Doc({ "Specifies whether the audit logging is enabled" })
    @Example("false")
    AUDIT_LOG_ENABLED(true, "false", "boolean"),

    @Doc({ "Specifies the batch size for audit logging" })
    @Example("100")
    AUDIT_LOG_MAX_ENTRIES_RETRIEVED(true, "1000", "long", Long.valueOf(1), Long.valueOf(1000), false),

    @Doc({ "URL of STS service" })
    @Example("https://<host>:<port>/<ServiceEndpoint>")
    SSO_STS_URL(false, null, "url"),

    @Doc({ "MetadataReference URL of STS service" })
    @Example("https://<host>:<port>/<MEXAddress>")
    SSO_STS_METADATA_URL(false, null, "url"),

    @Doc({ "URL of the Identity Provider's single logout service" })
    @Example("https://<host>:<port>/<LogoutServiceEndpoint>")
    SSO_LOGOUT_URL(false, null, "url", false),

    @Doc({ "The maximum number of users that can be registered on the platform" })
    @Example("10")
    MAX_NUMBER_ALLOWED_USERS(true, "10", "long", Long.valueOf(1L), Long.valueOf(9223372036854775807L), false),

    @Doc({ "The time interval in milliseconds at which the amount of user registered on the platform is checked" })
    @Example("43200000")
    TIMER_INTERVAL_USER_COUNT(true, "43200000", "long", Long.valueOf(1L), Long.valueOf(9223372036854775807L), false),

    @Doc({ "Specifies whether payment information is required for subscribing to services ",
            "that use the native billing system and are not free of charge. ",
            "Once set, the value cannot be changed." })
    @Example("false")
    HIDE_PAYMENT_INFORMATION(false, null, "boolean", true),

    @Doc({ "The alias of the private key to be used for signing SAML and custom tab requests" })
    @Example("s1as")
    SSO_SIGNING_KEY_ALIAS(false, "s1as", "string"),

    @Doc({ "The password for the keystore containing the private key for signing SAML and custom tab requests" })
    @Example("changeit")
    SSO_SIGNING_KEYSTORE_PASS(false, "changeit", "password"),

    @Doc({ "The path of the keystore holding the private key for signing SAML and custom tab requests." })
    @Example("<path>/keystore.jks")
    SSO_SIGNING_KEYSTORE(false, "./keystore.jks", "string"),

    @Doc({ "The ID of the default tenant for user authentication in SAML_SP mode. ",
           "The value must consist of 8 alphanumeric characters." })
    @Example("8f96dede")
    SSO_DEFAULT_TENANT_ID(false, "8f96dede", "string", false, 8L),

    @Doc({ "The unique entity ID of the IdP system used for authentication in SAML_SP mode. ", 
           "The ID is used for verifying that the response to an authentication request ",
           "is returned by the correct IdP." })
    @Example("IDP_ID")
    SSO_IDP_SAML_ASSERTION_ISSUER_ID(false, "default", "string", false),

    @Doc({ "Hashing algorithm used in signing SAML communication" })
    @Example("SHA1")
    SSO_SIGNING_ALGORITHM(false, SigningAlgorithmType.SHA1.name(), "string", false),

    @Doc({ "The Kafka configuration property bootstrap.servers." })
    @Example("localhost:9092")
    KAFKA_BOOTSTRAP_SERVERS(false, "", "string");

    // ///////////////////////////////////////////////////////////////////////////////////

    private final boolean isMandatory;
    private final boolean isReadonly;

    private final String fallbackValue;

    private final String type;

    public final static String TYPE_LONG = "long";
    public final static String TYPE_STRING = "string";
    public final static String TYPE_URL = "url";
    public final static String TYPE_MAIL = "mail";
    public final static String TYPE_BOOLEAN = "boolean";
    private Long length;
    public final static String TYPE_PASSWORD = "password";

    /**
     * The minimum value allowed for a long field.
     */
    private Long minValue;

    /**
     * The maximum value allowed for a long field.
     */
    private Long maxValue;

    ConfigurationKey(boolean isMandatory, String fallbackValue, String type) {
        this(isMandatory, fallbackValue, type, null, null, false);
    }

    ConfigurationKey(boolean isMandatory, String fallbackValue, String type,
            boolean isReadonly) {
        this(isMandatory, fallbackValue, type, null, null, isReadonly);
    }

    ConfigurationKey(boolean isMandatory, String fallbackValue, String type,
            boolean isReadonly, Long length) {
        this(isMandatory, fallbackValue, type, null, null, isReadonly);
        if (!TYPE_STRING.equals(type) && (length != null)) {
            throw new IllegalArgumentException(
                    "length is only allowed for configuration properties of type "
                            + TYPE_STRING);
        }
        this.length = length;
    }

    /**
     * @throws IllegalArgumentException
     *             if the field is not of type 'long' and any of
     *             {@link #minValue} or {@link #maxValue} is set.
     */
    ConfigurationKey(boolean isMandatory, String fallbackValue, String type,
            Long minValue, Long maxValue, boolean isReadonly) {
        if (!TYPE_LONG.equals(type) && (minValue != null || maxValue != null)) {
            throw new IllegalArgumentException(
                    "minValue and maxValue are only allowed for configuration properties of type "
                            + TYPE_LONG);
        }
        this.isMandatory = isMandatory;
        this.fallbackValue = fallbackValue;
        this.type = type;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.isReadonly = isReadonly;
    }

    /**
     * Returns the type for a certain configuration key.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the fallback value for a certain configuration key.
     * 
     * @return the fallback value
     */
    public String getFallBackValue() {
        return fallbackValue;
    }

    /**
     * Returns whether the configuration setting is a mandatory setting or not.
     * 
     * @return <code>true</code> in case the setting is mandatory,
     *         <code>false</code> otherwise.
     */
    public boolean isMandatory() {
        return isMandatory;
    }

    /**
     * Returns whether the configuration setting is readonly or not.
     * 
     * @return <code>true</code> in case the setting is readonly,
     *         <code>false</code> otherwise.
     */

    public boolean isReadonly() {
        return isReadonly;
    }

    /**
     * Retrieves the minimum value allowed for a long field.
     * 
     * @return The minimum value if set, <code>null</code> if not set or if the
     *         field is not of type long.
     */
    public Long getMinValue() {
        return minValue;
    }

    /**
     * Retrieves the maximum value allowed for a long field.
     * 
     * @return The maximum value if set, <code>null</code> if not set or if the
     *         field is not of type long.
     */
    public Long getMaxValue() {
        return maxValue;
    }

    /**
     * Returns the String representation of the key for the configuration
     * setting. This one may only be used to display information.
     * 
     * @return the configuration key as string
     */
    public String getKeyName() {
        return name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface Doc {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface Example {
        String value();
    }

    /**
     * Retrieves the length value allowed for a string field.
     * 
     * @return The length value if set, <code>null</code> if not set or if the
     *         field is not of type long.
     */
    public Long getLength() {
        return length;
    }

    /**
     * Prints a documented configuration file populated with all config keys and
     * their default values.
     * 
     * @param out
     * @throws Exception
     */
    public static void printExampleConfig(PrintStream out) throws Exception {
        for (final ConfigurationKey key : ConfigurationKey.values()) {
            final Field f = ConfigurationKey.class.getDeclaredField(key.name());
            if (f.getType().isEnum()) {
                final Doc doc = f.getAnnotation(Doc.class);
                if (doc != null) {
                    for (String line : doc.value()) {
                        out.printf("# %s\r\n", line);
                    }
                    final Example example = f.getAnnotation(Example.class);
                    if (key.isMandatory()) {
                        if (example == null) {
                            throw new AssertionError(
                                    "Mandatory field must declare example: "
                                            + key);
                        }
                        out.printf("%s=%s\r\n\r\n", f.getName(),
                                example.value());
                    } else {
                        String exampleValue = key.getFallBackValue();
                        if (example != null) {
                            exampleValue = example.value();
                        }
                        out.printf("# %s=%s\r\n\r\n", f.getName(),
                                exampleValue);
                    }
                }
            }
        }
    }

}
