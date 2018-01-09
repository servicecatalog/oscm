/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Florian Walker
 *                                                                              
 *  Creation Date: 05.04.2011                                                      
 *                                                                              
 *  Completion Time: 05.04.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.setup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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

import org.oscm.internal.types.exception.MailOperationException;

/**
 * This class is responsible for sending notification email to all platformusers
 * which user ID was changed in the context of making the globally unique. user
 * IDs in BES.
 * 
 * @author Florian Walker
 * 
 */
public class UserNotificationHandler {

    /**
     * Defines the encoding of the notification emails.
     */
    private static final String MAIL_CHARSET = "UTF-8";

    /**
     * Defines the default locale
     */
    private static final Locale defaultLocale = Locale.ENGLISH;

    /**
     * The patters which are used to exchange the old and new user name in the
     * message body.
     */
    private static final String MARKER_OLDID = "%OLD_ID%";
    private static final String MARKER_NEWID = "%NEW_ID%";

    /**
     * Main entry point of the program.
     * 
     * @param args
     *            1. the properties file which defined the database settings, 2.
     *            the properties file which defines the notification properties.
     * 
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out
                    .println("Usage: java org.oscm.setup.UserNotification <db-properties-file-path> <notification-properties-file-path> [<test email address>]");
            System.exit(-1);
        }
        final String dbPropertiesFile = args[0];
        final String unPropertiesFile = args[1];
        String testEmailAddress = null;
        if (args.length == 3 && !"".equals(args[2])) {
            testEmailAddress = args[2];
        }

        Properties dbProperties = HandlerUtils.readProperties(dbPropertiesFile);
        Properties unProperties = HandlerUtils.readProperties(unPropertiesFile);

        UserNotificationHandler notificationHandler = new UserNotificationHandler();
        Connection dbConnection = null;
        try {
            dbConnection = HandlerUtils
                    .establishDatabaseConnection(dbProperties);
        } catch (Exception e) {
            System.out.println("Failed to esablish the"
                    + " database connection. Reason:\n" + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            if (testEmailAddress == null) {
                notificationHandler.notifyUsers(dbConnection, unProperties);
            } else {
                notificationHandler.testSettings(dbConnection, unProperties,
                        testEmailAddress);
            }
        } catch (SQLException e) {
            System.out
                    .println("Failed to retrieve data from"
                            + " the table \"platformuser\". Reason:\n"
                            + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        } catch (MailOperationException e) {
            System.out.println("Failed to send notification emails. Reason:\n"
                    + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * This function accumulates the functionality to: get the affected users
     * from the database, send them a notification mail and clean up the
     * database.
     * 
     * @param dbConnection
     *            the database connection.
     * @param unProperties
     *            the properties for the notification.
     * @throws SQLException
     *             thrown in case there are problems accessing or updating the
     *             database.
     * @throws MailOperationException
     *             thrown if there are problems sending notifications
     */
    protected void notifyUsers(Connection dbConnection, Properties unProperties)
            throws SQLException, MailOperationException {
        List<UserData> userData = getUserData(dbConnection);
        sendMail(userData, unProperties);
        cleanTable(dbConnection);
    }

    /**
     * This method executes the same steps as notifyUsers() with a test email
     * address to validate the settings. It does not cleans the database.
     * 
     * @param dbConnection
     *            connection info for the db.
     * @param unProperties
     *            notification properties.
     * @param testEmailAddress
     *            teh test email address
     * @throws SQLException
     *             thrown if there are any problems accessing the db
     * @throws MailOperationException
     *             thrown if there are any problems sending the email.
     */
    protected void testSettings(Connection dbConnection,
            Properties unProperties, String testEmailAddress)
            throws SQLException, MailOperationException {
        System.out.println("Trying to retrieve data from the database ...");
        List<UserData> userData = getUserData(dbConnection);
        System.out.println("Found " + userData.size()
                + " to notify. The following users will be notified:");
        for (UserData user : userData) {
            System.out
                    .println("Old UserID: " + user.olduserid + "; new UserID: "
                            + user.userid + "; Email: " + user.email);
        }
        System.out.println("Trying to send test email ...");
        userData = new ArrayList<UserData>();
        UserData ud = new UserData();
        ud.email = testEmailAddress;
        ud.olduserid = "olduserid";
        ud.userid = "newuserid";
        userData.add(ud);
        sendMail(userData, unProperties);
        // Do not clean the db
    }

    /**
     * Retrieves the user data from the database for all users which user ID
     * have been change.
     * 
     * @param dbConnection
     *            the connection to the database.
     * @return a list of data of all users which ID have was changed.
     * @throws SQLException
     *             thrown if there are problems accessing the database.
     */
    protected List<UserData> getUserData(Connection dbConnection)
            throws SQLException {
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = stmt
                .executeQuery("SELECT userid, email, olduserid FROM platformuser WHERE useridcnt > 1");

        List<UserData> userDataList = new ArrayList<UserData>();
        while (rs.next()) {
            UserData ud = new UserData();
            ud.userid = rs.getString("userid");
            ud.email = rs.getString("email");
            ud.olduserid = rs.getString("olduserid");
            userDataList.add(ud);
        }
        rs.close();
        stmt.close();
        return userDataList;
    }

    /**
     * Removes the temporal columns "useridcnt" and "olduserid" from the table
     * "platformuser".
     * 
     * @param dbConnection
     *            the connection to the database.
     * @throws SQLException
     *             thrown if there are any problems accessing the database.
     */
    protected void cleanTable(Connection dbConnection) throws SQLException {
        Statement stmt = dbConnection.createStatement();
        stmt.execute("ALTER TABLE platformuser DROP COLUMN useridcnt");
        stmt.execute("ALTER TABLE platformuser DROP COLUMN olduserid");
        stmt.close();
    }

    /**
     * Small helper to retrieve the "localized" property key. E.g. key:
     * MAIL_SERVER ==> localized version: MAIL_SERVER_en
     * 
     * @param propertyKey
     *            the property key to be localized.
     * @param useDefaultLanguage
     *            <code>pass true to use the default locale.</code>
     * @return the "localized" version of the property key.
     */
    private String getLocalizedKey(String propertyKey,
            boolean useDefaultLanguage) {
        String language;
        if (useDefaultLanguage) {
            language = defaultLocale.getLanguage();
        } else {
            Locale locale = Locale.getDefault();
            language = locale.getLanguage();
        }
        return propertyKey + "_" + language;
    }

    /**
     * Retrieves a property matching to the current locale.
     * 
     * @param propertyKey
     *            the key of the property to get.
     * @param unProperties
     *            the properties which holds localized version of the property
     *            identified by the property key.
     * @return the value of the property matching to the current locale.
     */
    private String getLocalizedProperty(String propertyKey,
            Properties unProperties) {
        String value = unProperties.getProperty(getLocalizedKey(propertyKey,
                false));
        if (value == null) {
            value = unProperties
                    .getProperty(getLocalizedKey(propertyKey, true));
        }
        return value;
    }

    /**
     * Assembles the message text for given userdata and the prepared text.
     * 
     * @param preparedMessage
     *            the prepared message.
     * @param userData
     *            the userdata for which the message should be "personalized"
     * @return the "personalized" message text.
     */
    private String assembleMessageText(final String preparedMessage,
            final UserData userData) {
        String assembledMessage;
        assembledMessage = preparedMessage.replace(MARKER_NEWID,
                userData.userid);
        assembledMessage = assembledMessage.replace(MARKER_OLDID,
                userData.olduserid);
        return assembledMessage;
    }

    /**
     * Assembles the message text pattern for which will be "personalized" for
     * every user depending on the individual user data.
     * 
     * @param unProperties
     *            the properties which contains the single building blocks of
     *            the message.
     * @return the non "personalized" version of the message text.
     */
    protected String prepareMessageText(Properties unProperties) {
        String body = getLocalizedProperty(HandlerUtils.MAIL_BODY, unProperties);
        String footer = getLocalizedProperty(HandlerUtils.MAIL_FOOTER,
                unProperties);
        String nameNewId = getLocalizedProperty(HandlerUtils.MAIL_BODY_NEWID,
                unProperties);
        String nameOldId = getLocalizedProperty(HandlerUtils.MAIL_BODY_OLDID,
                unProperties);

        HandlerUtils.checkNotNull(body, HandlerUtils.MAIL_BODY);
        HandlerUtils.checkNotNull(footer, HandlerUtils.MAIL_FOOTER);
        HandlerUtils.checkNotNull(nameNewId, HandlerUtils.MAIL_BODY_NEWID);
        HandlerUtils.checkNotNull(nameOldId, HandlerUtils.MAIL_BODY_OLDID);

        StringBuilder sb = new StringBuilder();
        sb.append(body);
        sb.append("\n\n");
        sb.append(nameOldId);
        sb.append(": ");
        sb.append(MARKER_OLDID);
        sb.append("\n");
        sb.append(nameNewId);
        sb.append(": ");
        sb.append(MARKER_NEWID);
        sb.append("\n");
        sb.append(footer);
        return sb.toString();
    }

    /**
     * Sends notification emails to the passed list of users. The message text
     * is defined in the passed properties.
     * 
     * @param userData
     *            a list of userdata for which the emails should be send to.
     * @param unProperties
     *            the properties which defines the message text and email
     *            settings.
     * @throws MailOperationException
     *             thrown there are any problems sending the emails.
     */
    protected void sendMail(List<UserData> userData, Properties unProperties)
            throws MailOperationException {

        // If there are data, just return.
        if (userData == null || userData.size() < 1) {
            return;
        }

        String subject = getLocalizedProperty(HandlerUtils.MAIL_SUBJECT,
                unProperties);
        HandlerUtils.checkNotNull(subject, HandlerUtils.MAIL_SUBJECT);

        String preparedMessageText = prepareMessageText(unProperties);
        Session session = getEmailSession(unProperties);

        for (UserData recipient : userData) {
            if (recipient.email == null || recipient.email.length() == 0
                    || recipient.olduserid == null) {
                // Insufficient user data data to send the email
                System.out
                        .println("Insufficient user data data to send the email to user \""
                                + recipient.userid + "\".");
                continue;
            }

            MimeMessage msg = new MimeMessage(session);
            final String encoding = MAIL_CHARSET;
            try {
                Address from = new InternetAddress(
                        session.getProperty("mail.smtp.from"));
                msg.setFrom(from);
                msg.setReplyTo(new Address[] { from });
                msg.setSubject(subject, encoding);

                String text = assembleMessageText(preparedMessageText,
                        recipient);

                msg.setText(text, encoding);
            } catch (AddressException e) {
                // parsing the configuration setting for the system mail address
                // failed.
                MailOperationException mof = new MailOperationException(
                        "Invalid mail address in configuration setting", e);
                throw mof;
            } catch (MessagingException e) {
                MailOperationException mof = new MailOperationException(
                        "Mail could not be initialized.", e);
                throw mof;
            }

            try {
                Address to = new InternetAddress(recipient.email);
                msg.addRecipient(Message.RecipientType.TO, to);
            } catch (AddressException e) {
                // The to address is not valid, log it and continue with the
                // next data record.
                System.out.println("Invalid recipient address for "
                        + recipient.userid + "; email: " + recipient.email);
                continue;
            } catch (MessagingException e) {
                // actually this must not happen, but still throw a checked
                // exception
                MailOperationException mof = new MailOperationException(
                        "Recipient address could not be set.", e);
                throw mof;
            }

            try {
                Transport.send(msg);
                System.out.println("Notification was send to user: "
                        + recipient.userid + "; email: " + recipient.email);
            } catch (MessagingException e) {
                MailOperationException mof = new MailOperationException(
                        "Mail could not be sent.", e);
                throw mof;
            }
        }
    }

    /**
     * Retrieves a email session based on the passed properties.
     * 
     * @param unProperties
     *            define the settings of the session.
     * @return a email session for the passed properties.
     */
    private Session getEmailSession(Properties unProperties) {
        String mailServer = unProperties.getProperty(HandlerUtils.MAIL_SERVER);
        String returnAddress = unProperties
                .getProperty(HandlerUtils.MAIL_RESPONSE_ADDRESS);

        HandlerUtils.checkNotNull(mailServer, HandlerUtils.MAIL_SERVER);
        HandlerUtils.checkNotNull(returnAddress,
                HandlerUtils.MAIL_RESPONSE_ADDRESS);

        String mailPort = unProperties.getProperty(HandlerUtils.MAIL_PORT);
        String username = unProperties.getProperty(HandlerUtils.MAIL_USER);
        String password = unProperties.getProperty(HandlerUtils.MAIL_USER_PWD);

        Properties emailProperties = new Properties();
        emailProperties.setProperty("mail.smtp.host", mailServer);
        emailProperties.put("mail.smtp.from", returnAddress);

        if ((mailPort != null) && mailPort.length() > 0) {
            emailProperties.put("mail.smtp.port", mailPort);
        }

        SMTPAuthenticator authenticator;

        if (username != null && username.length() > 0 && password != null) {
            authenticator = new SMTPAuthenticator(username, password);
            emailProperties.put("mail.smtp.auth", "true");
        } else {
            authenticator = null;
        }

        return Session.getInstance(emailProperties, authenticator);
    }

    /**
     * Standard helper class for the SMTP authentication.
     */
    public static class SMTPAuthenticator extends Authenticator {
        private String username;
        private String password;

        public SMTPAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    /**
     * Struct to organize the userdata.
     */
    public static class UserData {
        public String userid;
        public String email;
        public String olduserid;
    }
}
