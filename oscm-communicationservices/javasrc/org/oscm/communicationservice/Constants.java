/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 21.09.16 08:07
 *
 ******************************************************************************/

package org.oscm.communicationservice;

/**
 * Class which holds constants values for the communicationservices module.
 * Authored by dawidch
 */
public class Constants {
    public static final String TENANT_ID = "tenantID";
    public static final String ENCODING = "UTF-8";
    public static final String MAIL_PROTOCOL_SMTP = "smtp";

    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_PASSWORD = "mail.smtp.password";
    public static final String MAIL_USER = "mail.user";
    public static final String MAIL_SMTP_USER = "mail.smtp.user";
    public static final String MAIL_TLS_ENABLED = "mail.smtp.starttls.enable";


    public static final String RESOURCE_TEXT_HEADER = "text.header";
    public static final String RESOURCE_TEXT_FOOTER = "text.footer";
    public static final String RESOURCE_SUBJECT = ".subject";
    public static final String RESOURCE_TEXT = ".text";

    public static final String MAIL_RESOURCE = "java:openejb/Resource/OSCMMail";


    private Constants() {
    }
}
