/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                    
 *                                                                              
 *  Creation Date: 03.06.2011                                                      
 *                                                                              
 *  Completion Time: 03.06.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.base;

import java.io.IOException;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.oscm.test.setup.PropertiesReader;

/**
 * @author tokoda
 * 
 */
public class MailReader {

    private String mailProviderType;
    private String mailUser;
    private String mailPassword;
    private String mailServer;
    private String mailAddress;

    private final static String MAIL_INBOX = "INBOX";
    protected final static String MAIL_SUBJECT_USER_ACCOUNT_CREATED_EN = "Account created";
    protected final static String MAIL_BODY_PASSWORD_PATTERN_EN = "password is:";
    protected final static String MAIL_BODY_USERKEY_PATTERN_EN = "Web service access:";
    protected final static String MAIL_BODY_USERNAME_PATTERN_EN = "Your user ID is: ";

    // Set if we establish a JavaMail session and connect to server.
    private Store store;

    public MailReader() throws Exception {
        initialize();
    }

    private void initialize() throws Exception {
        PropertiesReader reader = new PropertiesReader();
        Properties props = reader.load();
        mailProviderType = props.getProperty("mail.servertype");
        mailUser = props.getProperty("mail.username");
        mailPassword = props.getProperty("mail.password");
        mailServer = props.getProperty("mail.server");
        mailAddress = props.getProperty("mail.address");
    }

    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    private Store getStore() throws MessagingException {
        if (store == null) {
            // This is the JavaMail session.
            Session session;

            // Initialize JavaMail session.
            session = Session.getDefaultInstance(new Properties(), null);

            // Connect to e-mail server
            store = session.getStore(mailProviderType);
            store.connect(mailServer, mailUser, mailPassword);
        }
        return store;
    }

    /**
     * Get the content of a mail message.
     * 
     * @param message
     *            the mail message
     * @return the content of the mail message
     */
    private String getMessageContent(Message message) throws MessagingException {
        try {
            Object content = message.getContent();
            if (content instanceof Multipart) {
                StringBuffer messageContent = new StringBuffer();
                Multipart multipart = (Multipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    Part part = multipart.getBodyPart(i);
                    if (part.isMimeType("text/plain")) {
                        messageContent.append(part.getContent().toString());
                    }
                }
                return messageContent.toString();
            }
            return content.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getMailAddress() {
        if (mailAddress == null || mailAddress.trim().length() == 0) {
            return mailUser;
        }
        return mailAddress;
    }

    /**
     * Connect to the mail server and delete all mails.
     */
    public void deleteMails() throws MessagingException {
        Folder folder = getStore().getFolder(MAIL_INBOX);
        folder.open(Folder.READ_WRITE);

        // Get folder's list of messages.
        Message[] messages = folder.getMessages();

        // Retrieve message headers for each message in folder.
        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.ENVELOPE);
        folder.fetch(messages, profile);

        for (Message message : messages) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        folder.close(true);

    }

    /**
     * Get the content of the last message with the given subject.
     *
     * @param subject
     *            the subject
     * @return the content of the last message with the given subject
     */
    public String getLastMailContentWithSubject(String subject)
            throws MessagingException {
        // Download message headers from server.
        int retries = 0;
        String content = null;
        while (retries < 40) {

            // Open main "INBOX" folder.
            Folder folder = getStore().getFolder(MAIL_INBOX);
            folder.open(Folder.READ_WRITE);

            // Get folder's list of messages.
            Message[] messages = folder.getMessages();

            // Retrieve message headers for each message in folder.
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, profile);

            for (Message message : messages) {
                if (message.getSubject().equals(subject)) {
                    content = getMessageContent(message);
                }
            }
            folder.close(true);

            if (content != null) {
                return content;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }
            retries++;
        }

        return "";
    }

    public String[] readPassAndKeyFromEmail(boolean delete, String userName)
            throws MessagingException {
        // Download message headers from server.
        int retries = 0;
        String userPass = null;
        String userKey = null;
        while (retries < 40 && userPass == null) {

            // Open main "INBOX" folder.
            Folder folder = getStore().getFolder(MAIL_INBOX);
            folder.open(Folder.READ_WRITE);

            // Get folder's list of messages.
            Message[] messages = folder.getMessages();

            // Retrieve message headers for each message in folder.
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, profile);

            for (Message message : messages) {
                if (message.getSubject().equals(MAIL_SUBJECT_USER_ACCOUNT_CREATED_EN)) {
                    String content = getMessageContent(message);
                    String userNameFromEmail = readInformationFromGivenMail(MAIL_BODY_USERNAME_PATTERN_EN, content);
                    if (userName.equals(userNameFromEmail)) {
                        userKey = readInformationFromGivenMail(MAIL_BODY_USERKEY_PATTERN_EN, content);
                        userPass = readInformationFromGivenMail(MAIL_BODY_PASSWORD_PATTERN_EN, content);
                        if (delete) {
                            message.setFlag(Flag.DELETED, true);
                        }
                        break;
                    }
                }
            }
            folder.close(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }
            retries++;
        }

        return new String[] {userKey, userPass};
    }
    
    public String readKeyFromEmail(boolean delete, String userName)
            throws MessagingException {
        // Download message headers from server.
        int retries = 0;
        String userKey = null;
        while (retries < 40 && userKey == null) {

            // Open main "INBOX" folder.
            Folder folder = getStore().getFolder(MAIL_INBOX);
            folder.open(Folder.READ_WRITE);

            // Get folder's list of messages.
            Message[] messages = folder.getMessages();

            // Retrieve message headers for each message in folder.
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages, profile);

            for (Message message : messages) {
                if (message.getSubject().equals(MAIL_SUBJECT_USER_ACCOUNT_CREATED_EN)) {
                    String content = getMessageContent(message);
                    String userNameFromEmail = readInformationFromGivenMail(MAIL_BODY_USERNAME_PATTERN_EN, content);
                    if (userName.equals(userNameFromEmail)) {
                        userKey = readInformationFromGivenMail(MAIL_BODY_USERKEY_PATTERN_EN, content);
                        if (delete) {
                            message.setFlag(Flag.DELETED, true);
                        }
                        break;
                    }
                }
            }
            folder.close(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }
            retries++;
        }

        return userKey;
    }

    private String readInformationFromMail(String subject, String pattern)
            throws MessagingException {

        String information = getLastMailContentWithSubject(subject);
        return readInformationFromGivenMail(pattern, information);
    }

    private String readInformationFromGivenMail(String pattern, String mailContent)
            throws MessagingException {

        String information = mailContent;
        int idx = information.indexOf(pattern);
        if (idx >= 0 && idx + pattern.length() < information.length()) {
            idx += pattern.length();
            information = information.substring(idx).trim();
            idx = 0;
            while (idx < information.length()
                    && !Character.isWhitespace(information.charAt(idx))) {
                idx++;
            }
            if (idx < information.length()) {
                information = information.substring(0, idx);
            }
        }
        return information;
    }

    /**
     * Read the password from the last "Account created" mail from the server.
     * @deprecated Use {@link #readPassAndKeyFromEmail} instead
     */
    @Deprecated
    public String readPasswordFromMail() throws MessagingException {
        return readInformationFromMail(MAIL_SUBJECT_USER_ACCOUNT_CREATED_EN,
                MAIL_BODY_PASSWORD_PATTERN_EN);
    }

    /**
     * Read the user key from the last "Account created" mail from the server.
     * @deprecated Use {@link #readPassAndKeyFromEmail} instead
     */
    @Deprecated
    public String readUserKeyFromMail() throws MessagingException {
        return readInformationFromMail(MAIL_SUBJECT_USER_ACCOUNT_CREATED_EN,
                MAIL_BODY_USERKEY_PATTERN_EN);
    }

    /**
     * Read the user key from the last "Account created" mail from the server.
     * @return Array which contains at index 0 userKey and at index 1 userPassword.
     * @param userName
     */
    public String[] readPassAndKeyFromEmail(String userName) throws MessagingException {
        return readPassAndKeyFromEmail(true, userName);
    }

}
