/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.12.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.constants;

/**
 * Interface providing all required POST parameters used in calls to the
 * heidelpay POST interface.
 * 
 * @author Mike J&auml;ger
 * 
 */
public interface HeidelpayPostParameter {

    public static final String CONTACT_EMAIL = "CONTACT.EMAIL";
    public static final String NAME_COMPANY = "NAME.COMPANY";
    public static final String NAME_FAMILY = "NAME.FAMILY";
    public static final String NAME_GIVEN = "NAME.GIVEN";
    public static final String ACCOUNT_REGISTRATION = "ACCOUNT.REGISTRATION";
    public static final String ACCOUNT_HOLDER = "ACCOUNT.HOLDER";
    public static final String PRESENTATION_USAGE = "PRESENTATION.USAGE";
    public static final String PRESENTATION_CURRENCY = "PRESENTATION.CURRENCY";
    public static final String PRESENTATION_AMOUNT = "PRESENTATION.AMOUNT";
    public static final String TRANSACTION_RESPONSE = "TRANSACTION.RESPONSE";
    public static final String FRONTEND_PM_3_ENABLED = "FRONTEND.PM.3.ENABLED";
    public static final String FRONTEND_PM_3_METHOD = "FRONTEND.PM.3.METHOD";
    public static final String FRONTEND_PM_2_SUBTYPES = "FRONTEND.PM.2.SUBTYPES";
    public static final String FRONTEND_PM_2_ENABLED = "FRONTEND.PM.2.ENABLED";
    public static final String FRONTEND_PM_2_METHOD = "FRONTEND.PM.2.METHOD";
    public static final String FRONTEND_PM_1_SUBTYPES = "FRONTEND.PM.1.SUBTYPES";
    public static final String FRONTEND_PM_1_ENABLED = "FRONTEND.PM.1.ENABLED";
    public static final String FRONTEND_PM_1_METHOD = "FRONTEND.PM.1.METHOD";
    public static final String FRONTEND_PM_DEFAULT_DISABLE_ALL = "FRONTEND.PM.DEFAULT_DISABLE_ALL";
    public static final String FRONTEND_ONEPAGE = "FRONTEND.ONEPAGE";
    public static final String FRONTEND_REDIRECT_TIME = "FRONTEND.REDIRECT_TIME";
    public static final String FRONTEND_REDIRECT_URL = "FRONTEND.REDIRECT_URL";
    public static final String FRONTEND_RESPONSE_URL = "FRONTEND.RESPONSE_URL";
    public static final String FRONTEND_LANGUAGE = "FRONTEND.LANGUAGE";
    public static final String FRONTEND_MODE = "FRONTEND.MODE";
    public static final String FRONTEND_POPUP = "FRONTEND.POPUP";
    public static final String FRONTEND_ENABLED = "FRONTEND.ENABLED";
    public static final String FRONTEND_JS_SCRIPT = "FRONTEND.JSCRIPT_PATH";
    public static final String FRONTEND_NEXT_TARGET = "FRONTEND.NEXT_TARGET";
    public static final String USER_PWD = "USER.PWD";
    public static final String USER_LOGIN = "USER.LOGIN";
    public static final String SECURITY_SENDER = "SECURITY.SENDER";
    public static final String TRANSACTION_CHANNEL = "TRANSACTION.CHANNEL";
    public static final String PAYMENT_CODE = "PAYMENT.CODE";
    public static final String IDENTIFICATION_TRANSACTIONID = "IDENTIFICATION.TRANSACTIONID";
    public static final String IDENTIFICATION_REFERENCEID = "IDENTIFICATION.REFERENCEID";
    public static final String TRANSACTION_MODE = "TRANSACTION.MODE";
    public static final String REQUEST_VERSION = "REQUEST.VERSION";
    public static final String USER_LAST_NAME = "NAME.FAMILY";
    public static final String USER_FIRST_NAME = "NAME.GIVEN";
    public static final String ADDRESS_STREET = "ADDRESS.STREET";
    public static final String ADDRESS_ZIP = "ADDRESS.ZIP";
    public static final String ADDRESS_CITY = "ADDRESS.CITY";
    public static final String ADDRESS_COUNTRY = "ADDRESS.COUNTRY";

    public static final String SUCCESS_RESULT = "ACK";
    public static final String FAILURE_RESULT = "NOK";

    public static final String PAYMENT_OPTION_CREDIT_CARD = "CC";
    public static final String PAYMENT_OPTION_DIRECT_DEBIT = "DD";

    public static final String PAYMENT_INFO_KEY = "CRITERION.BES_PAYMENT_INFO_KEY";
    public static final String PAYMENT_INFO_ID = "CRITERION.BES_PAYMENT_INFO_ID";
    public static final String PAYMENT_TYPE_KEY = "CRITERION.BES_PAYMENT_TYPE_KEY";
    public static final String ORGANIZATION_KEY = "CRITERION.BES_ORGANIZATION_KEY";
    public static final String USER_LOCALE = "CRITERION.USER_LOCALE";
    public static final String BASE_URL = "CRITERION.BES_BASE_URL";
    public static final String BES_PAYMENT_REGISTRATION_WSDL = "CRITERION.BES_PAYMENT_REGISTRATION_WSDL";
    public static final String BES_PAYMENT_REGISTRATION_ENDPOINT = "CRITERION.BES_PAYMENT_REGISTRATION_ENDPOINT";
}
