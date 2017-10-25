/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.communicationservice.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;

import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;

import static org.oscm.communicationservice.Constants.ENCODING;
import static org.oscm.communicationservice.Constants.MAIL_PASSWORD;
import static org.oscm.communicationservice.Constants.MAIL_PROTOCOL_SMTP;
import static org.oscm.communicationservice.Constants.MAIL_RESOURCE;
import static org.oscm.communicationservice.Constants.MAIL_SMTP_AUTH;
import static org.oscm.communicationservice.Constants.MAIL_SMTP_USER;
import static org.oscm.communicationservice.Constants.MAIL_TLS_ENABLED;
import static org.oscm.communicationservice.Constants.RESOURCE_SUBJECT;
import static org.oscm.communicationservice.Constants.RESOURCE_TEXT;
import static org.oscm.communicationservice.Constants.RESOURCE_TEXT_FOOTER;
import static org.oscm.communicationservice.Constants.RESOURCE_TEXT_HEADER;
import static org.oscm.communicationservice.Constants.TENANT_ID;

/**
 * Session Bean implementation class CommunicationServiceBean
 */
@Stateless
@Local(CommunicationServiceLocal.class)
public class CommunicationServiceBean implements CommunicationServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(CommunicationServiceBean.class);

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    ConfigurationServiceLocal confSvc;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    /**
     * Default constructor.
     */
    public CommunicationServiceBean() {
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public SendMailStatus<PlatformUser> sendMail(EmailType type,
            Object[] params, Marketplace marketplace,
            PlatformUser... recipients) {

        SendMailStatus<PlatformUser> sendMailStatus = new SendMailStatus<PlatformUser>();
        for (PlatformUser recipient : recipients) {
            try {
                sendMail(recipient, type, params, marketplace);
                sendMailStatus.addMailStatus(recipient);

            } catch (MailOperationException e) {
                sendMailStatus.addMailStatus(recipient, e);
            }
        }

        return sendMailStatus;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void sendMail(PlatformUser recipient, EmailType type,
            Object[] params, Marketplace marketplace)
            throws MailOperationException {
        

        String mail = recipient.getEmail();
        if (mail == null || mail.trim().length() == 0) {
            logger.logInfo(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_NO_EMAIL_ADDRESS_SPECIFIED_USER,
                    recipient.getUserId());
            
            return;
        }

        // get mail subject
        final String locale = recipient.getLocale();
        String subject = getText(locale, type.toString() + RESOURCE_SUBJECT,
                params, marketplace);

        // get mail text
        String text = null;
        if (recipient.getSalutation() != null
                && recipient.getLastName() != null
                && recipient.getLastName().length() > 0) {
            String key = RESOURCE_TEXT_HEADER + "."
                    + recipient.getSalutation().toString();
            text = getText(locale, key, new Object[] { recipient.getUserId(),
                    recipient.getFirstName(), recipient.getAdditionalName(),
                    recipient.getLastName() }, marketplace);
            if (key.equals(text)) {
                text = null;
            }
        }
        if (text == null) {
            text = getText(
                    locale,
                    RESOURCE_TEXT_HEADER,
                    new Object[] { recipient.getUserId(),
                            recipient.getFirstName(),
                            recipient.getAdditionalName(),
                            recipient.getLastName() }, marketplace);
        }
        text += getText(locale, type.toString() + RESOURCE_TEXT, params,
                marketplace);
        text += getText(locale, RESOURCE_TEXT_FOOTER, null, marketplace);

        // send mail
        List<String> to = new ArrayList<String>();
        to.add(recipient.getEmail());
        sendMail(to, subject, text, locale);

        
    }

    /**
     * Send mail to organization.
     * 
     * @param organization
     *            Organization to getting mail.
     * @param type
     *            Mail type.
     * @param params
     *            Mail parameters,
     * @throws MailOperationException
     *             On error mail sending.
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void sendMail(Organization organization, EmailType type,
            Object[] params, Marketplace marketplace)
            throws MailOperationException {
        
        String mail = organization.getEmail();
        if (mail == null || mail.trim().length() == 0) {
            logger.logInfo(
                    Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.INFO_NO_EMAIL_ADDRESS_SPECIFIED_ORGANIZATION,
                    organization.getOrganizationId());
            
            return;
        }
        // get mail subject
        final String locale = organization.getLocale();
        String subject = getText(locale, type.toString() + RESOURCE_SUBJECT,
                params, marketplace);

        // get mail text
        String text = getText(locale, RESOURCE_TEXT_HEADER, params, marketplace);
        text += getText(locale, type.toString() + RESOURCE_TEXT, params,
                marketplace);
        text += getText(locale, RESOURCE_TEXT_FOOTER, null, marketplace);

        // send mail
        List<String> to = new ArrayList<String>();
        to.add(mail);
        sendMail(to, subject, text, locale);
        
    }

    /**
     * Send an email of given type to the given address.
     * 
     * @param emailAddress
     *            The address will mail be send to.
     * @param type
     *            Mail type.
     * @param params
     *            Mail parameters.
     * @param marketplace
     *            Marketplace of subscription.
     * @param locale
     *            Locale information of mail receiver.
     * @throws MailOperationException
     *             On error mail sending.
     * @throws ValidationException
     *             if the format of the email address is not valid
     */
    public void sendMail(String emailAddress, EmailType type, Object[] params,
            Marketplace marketplace, String locale)
            throws MailOperationException, ValidationException {
        

        BLValidator.isEmail("emailAddress", emailAddress, true);

        // get mail subject
        String subject = getText(locale, type.toString() + RESOURCE_SUBJECT,
                params, marketplace);

        // get mail text
        String text = getText(locale, type.toString() + RESOURCE_TEXT, params,
                marketplace);

        // send mail
        List<String> to = new ArrayList<String>();
        to.add(emailAddress);
        sendMail(to, subject, text, locale);
        
    }

    public SendMailStatus<Organization> sendMail(EmailType type,
            Object[] params, Marketplace marketplace,
            Organization... organizations) {

        SendMailStatus<Organization> sendMailStatus = new SendMailStatus<Organization>();
        for (Organization organization : organizations) {
            try {
                sendMail(organization, type, params, marketplace);
                sendMailStatus.addMailStatus(organization);

            } catch (MailOperationException e) {
                sendMailStatus.addMailStatus(organization, e);
            }
        }

        return sendMailStatus;
    }

    public String getBaseUrlWithTenant(String tenantId) throws MailOperationException {
        StringBuffer url = new StringBuffer();
        try {
            url.append(getBaseUrl());
            if (StringUtils.isNotBlank(tenantId)) {
                removeTrailingSlashes(url);
                url.append("?" + TENANT_ID + "=");
                url.append(URLEncoder.encode(tenantId.trim(), ENCODING));
            }
            return url.toString();
        } catch (UnsupportedEncodingException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ENCODE_ORGANIZATION_ID_FAILED);
            MailOperationException mof = new MailOperationException(
                    "Tenant URL creation failed!", e);
            throw mof;
        }
    }

    public String getBaseUrlHttpsWithTenant(String tenantId) throws MailOperationException {
        StringBuffer url = new StringBuffer();
        try {
            url.append(getBaseUrlHttps());
            if (StringUtils.isNotBlank(tenantId)) {
                removeTrailingSlashes(url);
                url.append("?" + TENANT_ID + "=");
                url.append(URLEncoder.encode(tenantId.trim(), ENCODING));
            }
            return url.toString();
        } catch (UnsupportedEncodingException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ENCODE_ORGANIZATION_ID_FAILED);
            MailOperationException mof = new MailOperationException(
                    "Tenant URL creation failed!", e);
            throw mof;
        }
    }

    public String getMarketplaceUrl(String marketplaceId)
            throws MailOperationException {
        // send acknowledge e-mail
        StringBuffer url = new StringBuffer();
        try {
            url.append(getBaseUrl());
            if (marketplaceId != null && marketplaceId.trim().length() > 0) {
                removeTrailingSlashes(url);
                url.append(org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT);
                url.append("?mId=");
                url.append(URLEncoder.encode(marketplaceId.trim(), ENCODING));
            }
            return url.toString();
        } catch (UnsupportedEncodingException e) {
            // log exception
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ENCODE_ORGANIZATION_ID_FAILED);
            MailOperationException mof = new MailOperationException(
                    "Marketplace URL creation failed!", e);
            throw mof;
        }
    }

    public String getMarketplaceUrlHttps(String marketplaceId)
            throws MailOperationException {
        // send acknowledge e-mail
        StringBuffer url = new StringBuffer();
        try {
            url.append(getBaseUrlHttps());
            if (marketplaceId != null && marketplaceId.trim().length() > 0) {
                removeTrailingSlashes(url);
                url.append(org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT);
                url.append("?mId=");
                url.append(URLEncoder.encode(marketplaceId.trim(), ENCODING));
            }
            return url.toString();
        } catch (UnsupportedEncodingException e) {
            // log exception
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_ENCODE_ORGANIZATION_ID_FAILED);
            MailOperationException mof = new MailOperationException(
                    "Marketplace URL creation failed!", e);
            throw mof;
        }
    }

    public String getBaseUrl() {
        return confSvc.getBaseURL();
    }

    public String getBaseUrlHttps() {
        return confSvc.getBaseUrlHttps();
    }

    private Session lookupMailResource() {
        Session session = null;
        try {
            Context context = new InitialContext();
            Object resource = context.lookup(MAIL_RESOURCE);
            if (resource instanceof Session) {
                session = (Session) resource;
                if (MAIL_PROTOCOL_SMTP.equalsIgnoreCase(session.getProperty("mail.transport.protocol"))) {
                    Properties properties = session.getProperties();
                    properties.putIfAbsent(MAIL_SMTP_AUTH, "true");
                    properties.putIfAbsent(MAIL_TLS_ENABLED, "true");
                    String username = session.getProperty(MAIL_SMTP_USER);
                    String password = session.getProperty(MAIL_PASSWORD);
                    session = Session.getInstance(session.getProperties(), new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
                }
            }
        } catch (NamingException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "The registered JavaMail resource " + MAIL_RESOURCE
                            + " is not configured properly.", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_MAILING_FAILURE);
            throw se;
        }
        return session;
    }

    protected void sendMail(List<String> mailAddresses, String subject,
            String text, String locale) throws MailOperationException {
        

        Session session = lookupMailResource();
        MimeMessage msg = new MimeMessage(session);

        final String encoding;
        if (Locale.JAPANESE.getLanguage().equals(locale)) {
            encoding = confSvc.getConfigurationSetting(
                    ConfigurationKey.MAIL_JA_CHARSET,
                    Configuration.GLOBAL_CONTEXT).getValue();
        } else {
            encoding = ENCODING;
        }

        try {
            Address from = new InternetAddress(session.getProperty("mail.smtp.from"));
            msg.setFrom(from);
            msg.setReplyTo(new Address[] { from });
            msg.setSubject(subject, encoding);
            msg.setText(text, encoding);
        } catch (AddressException e) {
            // parsing the configuration setting for the system mail address
            // failed, preventing correct working behavior of the platform. A
            // system exception will be thrown
            SaaSSystemException se = new SaaSSystemException(
                    "Invalid mail address in configuration setting", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_MAILING_FAILURE);
            throw se;
        } catch (MessagingException e) {
            MailOperationException mof = new MailOperationException(
                    "Mail could not be initialized.", e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_MAILING_FAILURE);
            throw mof;
        }

        try {
            Address[] to = new InternetAddress[mailAddresses.size()];
            int pos = 0;
            for (String recipient : mailAddresses) {
                to[pos] = new InternetAddress(recipient);
                pos++;
            }
            msg.addRecipients(Message.RecipientType.TO, to);
        } catch (AddressException e) {
            MailOperationException mof = new MailOperationException(
                    "Invalid recipient address.", e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_MAILING_FAILURE);
            throw mof;
        } catch (MessagingException e) {
            // actually this must not happen, but still throw a checked
            // exception
            MailOperationException mof = new MailOperationException(
                    "Recipient address could not be set.", e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_MAILING_FAILURE);
            throw mof;
        }

        try {
            Transport.send(msg);
        } catch (MessagingException e) {
            MailOperationException mof = new MailOperationException(
                    "Mail could not be sent.", e);
            logger.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_MAILING_FAILURE);
            throw mof;
        }
        
    }

    /**
     * Gets a text string for the given key from the mail resource bundle. If
     * the params array is set, placeholders in this string will be replaced by
     * the elements of the array.
     * 
     * @param localeString
     *            the string representation of the locale which is used to
     *            access the resource bundle
     * @param key
     *            the key for the desired string.
     * @param params
     *            an array of objects to be formatted and substituted.
     */
    private String getText(String localeString, String key, Object[] params,
            Marketplace marketplace) {
        
        String text = null;
        text = localizer.getLocalizedTextFromBundle(
                LocalizedObjectTypes.MAIL_CONTENT, marketplace, localeString,
                key);

        if (params != null) {
            MessageFormat mf = new MessageFormat(text, new Locale(localeString));
            text = mf.format(params, new StringBuffer(), null).toString();
        }

        
        return text;
    }

    void removeTrailingSlashes(StringBuffer url) {
        while (url.length() > 0 && url.charAt(url.length() - 1) == '/') {
            url.replace(url.length() - 1, url.length(), "");
        }
    }
}
