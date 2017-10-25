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

package org.oscm.paymentservice.constants;

import java.io.PrintStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

/**
 * Represents the different types of configuration keys available in the
 * platform.
 * 
 */
public enum HeidelpayConfigurationKey {
    @Doc({ "The proxy to be used for HTTP connections, if any." })
    HTTP_PROXY,

    @Doc({ "The proxy port to be used for HTTP connections, if any." })
    @Example("1080")
    HTTP_PROXY_PORT,

    @Doc({ "The base URL to access the platform's landing pages, especially",
            "required to create the URL for accessing the confirmation page." })
    @Example("http://<your server>:<your port>/oscm-portal")
    BASE_URL,

    @Doc({ "The URL of the POST interface of the payment service provider (PSP)." })
    @Example("<post interface url>")
    PSP_POST_URL,

    @Doc({ "The URL of the XML integrator of the PSP." })
    @Example("<xml integrator url>")
    PSP_XML_URL,

    @Doc({ "The PSP security sender identifier." })
    @Example("<sender identifier>")
    PSP_SECURITY_SENDER,

    @Doc({ "The transaction channel identifier as provided by the PSP." })
    @Example("<channel identifier>")
    PSP_TRANSACTION_CHANNEL,

    @Doc({ "The user name used to connect to the PSP." })
    @Example("<user name>")
    PSP_USER_LOGIN,

    @Doc({ "The PSP login user's password." })
    @Example("<password>")
    PSP_USER_PWD,

    @Doc({ "The brands of supported credit cards." })
    @Example("Visa, Master")
    PSP_SUPPORTED_CC_BRANDS(),

    @Doc({ "The countries for which direct debit is supported." })
    @Example("DE,AT,ES,NL")
    PSP_SUPPORTED_DD_COUNTRIES(),

    @Doc({ "The mode to be used for sending transactions to the PSP." })
    @Example("INTEGRATOR_TEST")
    PSP_TXN_MODE(),

    @Doc({ "The URL of the PSP response servlet. This usually points to a ",
            "secure resource." })
    @Example("https://<yourserver>:<yourport>/oscm-psp-heidelpay/HeidelpayResponseServlet")
    PSP_RESPONSE_SERVLET_URL,

    @Doc({
            "The URL of the platform's payment registration Web service WSDL. This is a WSDL ",
            "and must point to a secure resource. It is a OSCM service." })
    @Example("https://<yourserver>:<yourport>/oscm/PaymentRegistrationService/CLIENTCERT?wsdl")
    PSP_PAYMENT_REGISTRATION_WSDL,

    @Doc({
            "The URL of the platform's payment registration Web service. This is a endpoint ",
            "and must point to a secure resource. It is a OSCM service." })
    @Example("https://<yourserver>:<yourport>/PaymentRegistrationService/CLIENTCERT")
    PSP_PAYMENT_REGISTRATION_ENDPOINT,

    @Doc({ "The URL of the PSP frontend javascript path." })
    @Example("https://<yourserver>:<yourport>/oscm-psp-heidelpay/scripts/pspViewConfig.js")
    PSP_FRONTEND_JS_PATH;

    @Retention(RetentionPolicy.RUNTIME)
    private @interface Doc {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface Example {
        String value();
    }

    /**
     * Prints a documented configuration file populated with all config keys and
     * their default values.
     * 
     * @param out
     * @throws Exception
     */
    public static void printExampleConfig(PrintStream out) throws Exception {
        for (final HeidelpayConfigurationKey key : HeidelpayConfigurationKey
                .values()) {
            final Field f = HeidelpayConfigurationKey.class
                    .getDeclaredField(key.name());
            if (f.getType().isEnum()) {
                final Doc doc = f.getAnnotation(Doc.class);
                if (doc != null) {
                    for (String line : doc.value()) {
                        out.printf("# %s%n", line);
                    }
                    final Example example = f.getAnnotation(Example.class);
                    if (example != null) {
                        out.printf("# %s=%s%n%n", f.getName(), example.value());
                    }
                }
            }
        }
    }

}
