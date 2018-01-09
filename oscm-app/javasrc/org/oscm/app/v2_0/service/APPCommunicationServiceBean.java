/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 03.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;

/**
 * @author Dirk Bernsau
 * 
 */
@Stateless
public class APPCommunicationServiceBean {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(APPCommunicationServiceBean.class);

    private static final String MAIL_CHARSET = "UTF-8";
    public static final String DEFAULT_MAIL_RESOURCE = "APPMail";

    @EJB
    protected APPConfigurationServiceBean configService;

    public void sendMail(List<String> mailAddresses, String subject, String text)
            throws APPlatformException {

        MimeMessage msg = composeMessage(mailAddresses, subject, text);

        try {
            LOGGER.debug("Send message.");
            transportMail(msg);
        } catch (Exception e) {
            APPlatformException pe = new APPlatformException(
                    "Mail could not be sent.", e);
            LOGGER.warn(pe.getMessage() + " [Cause: " + e.getMessage() + "]");
            throw pe;
        }
    }

    MimeMessage composeMessage(List<String> mailAddresses, String subject,
            String text) throws APPlatformException {

        if (mailAddresses == null || mailAddresses.isEmpty()) {
            APPlatformException pe = new APPlatformException(
                    "No mail recipients specified.");
            LOGGER.warn(pe.getMessage());
            throw pe;
        }
        for (String recipient : mailAddresses) {
            if (recipient == null) {
                APPlatformException pe = new APPlatformException(
                        "Invalid recipient address [null].");
                LOGGER.warn(pe.getMessage());
                throw pe;
            }
        }

        mailAddresses = removeDuplicates(mailAddresses);

        Session session = lookupMailResource();
        MimeMessage msg = null;

        final String encoding = MAIL_CHARSET;

        try {
            msg = getMimeMessage(session);
            Address from = new InternetAddress(session.getProperty("mail.smtp.from"));
            msg.setFrom(from);
            msg.setReplyTo(new Address[] { from });
            msg.setSubject(subject, encoding);
            msg.setText(text, encoding);

            LOGGER.debug("MailFrom: {}", from.toString());
            LOGGER.debug("MailSubject: {}", subject);

        } catch (AddressException e) {
            // parsing the configuration setting for the system mail address
            // failed, preventing correct working behavior of the platform. A
            // system exception will be thrown
            APPlatformException pe = new APPlatformException(
                    "Invalid mail address in configuration setting [Address: "
                            + session.getProperty("mail.smtp.from") + "]", e);
            LOGGER.error(pe.getMessage() + " [Cause: " + e.getMessage() + "]");
            throw pe;
        } catch (Exception e) {
            APPlatformException pe = new APPlatformException(
                    "Mail could not be initialized.", e);
            LOGGER.warn(pe.getMessage() + " [Cause: " + e.getMessage() + "]");
            throw pe;
        }

        try {
            Address[] to = new InternetAddress[mailAddresses.size()];
            int pos = 0;
            for (String recipient : mailAddresses) {
                to[pos++] = new InternetAddress(recipient);
                LOGGER.debug("MailReceiver: {}", recipient);
            }
            msg.addRecipients(Message.RecipientType.TO, to);
        } catch (AddressException e) {
            APPlatformException pe = new APPlatformException(
                    "Invalid recipient address.", e);
            LOGGER.warn(pe.getMessage() + " [Cause: " + e.getMessage() + "]");
            throw pe;
        } catch (Exception e) {
            // actually this must not happen, but still throw a checked
            // exception
            APPlatformException pe = new APPlatformException(
                    "Recipient address could not be set.", e);
            LOGGER.warn(pe.getMessage() + " [Cause: " + e.getMessage() + "]");
            throw pe;
        }
        return msg;
    }

    /**
     * Getter for unit tests.
     */
    MimeMessage getMimeMessage(Session session) {
        MimeMessage msg = new MimeMessage(session);
        return msg;
    }

    /**
     * Sends the mail.
     * <p>
     * This method is invoked within a separate method, for supporting the JUnit
     * test framework (static calls can't be mocked).
     */
    protected void transportMail(MimeMessage msg) throws MessagingException {
        LOGGER.debug("Send message.");
        Transport.send(msg);
    }

    private Session lookupMailResource() throws APPlatformException {

        String mailResource = null;

        try {
            mailResource = configService
                    .getProxyConfigurationSetting(PlatformConfigurationKey.APP_MAIL_RESOURCE);
        } catch (ConfigurationException e) {
            // no configured resource found => using default
        }

        if (mailResource == null || mailResource.trim().length() == 0) {
            mailResource = DEFAULT_MAIL_RESOURCE;
        }

        Object resource = null;
        try {
            Context context = new InitialContext();
            resource = context.lookup("java:openejb/Resource/" + mailResource);
        } catch (Exception e) {
            APPlatformException se = new APPlatformException(
                    "The configured JavaMail resource " + mailResource
                            + " is not available.", e);
            LOGGER.error(se.getMessage() + " [Cause: " + e.getMessage() + "]");
            throw se;
        }

        return (Session) resource;
    }

    List<String> removeDuplicates(List<String> recipients) {
        List<String> list = new ArrayList<String>();
        for (String recipient : recipients) {
            if (!list.contains(recipient)) {
                list.add(recipient);
            }
        }
        return list;
    }
}
