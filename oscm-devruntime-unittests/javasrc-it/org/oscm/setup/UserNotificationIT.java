/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.setup;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;

import org.junit.Assert;
import org.apache.geronimo.javamail.store.pop3.POP3Store;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.db.TestDatabase;
import org.oscm.setup.UserNotificationHandler.UserData;
import org.oscm.internal.types.exception.MailOperationException;

@SuppressWarnings("boxing")
public class UserNotificationIT {

    private UserNotificationHandler userNotification;
    private static TestDatabase testDatabase;

    private final static String testmailPropertiesFilePath = "testmail.properties";
    private final static String unPropertiesFilePath = "un.properties";
    private String testMailAddress;
    private String testMailServer;
    private String testMailPasswd;
    private int testMailDelay;

    @BeforeClass
    public static void initDb() throws Exception {
        testDatabase = new TestDatabase();
        testDatabase.initDatabase();
    }

    @Before
    public void setUp() throws Exception {
        userNotification = new UserNotificationHandler();
        testDatabase.insertData(
                getClass().getResource("/setup_UserNotification.xml"));
        initMail();
        injectTestMailAdress();
        cleanInbox();
    }

    @After
    public void tearDown() throws Exception {
        testDatabase.clean();
    }

    @AfterClass
    public static void cleanupDB() throws Exception {
        testDatabase.updateDBSchemaToLatestVersion();
    }

    /**
     * Initializes the test mail system with the values of the
     * testmail.properrties.
     */
    private void initMail() throws Exception, IOException {

        Properties mailProperties = getProperties(
                getProperiesForComputerName(testmailPropertiesFilePath));

        InetAddress localMachine = InetAddress.getLocalHost();
        String localHostName = localMachine.getHostName();
        int i = localHostName.indexOf(".");
        if (i > -1) {
            localHostName = localHostName.substring(0, i);
        }
        Assert.assertNotNull(localHostName);
        String username = mailProperties.getProperty("mail.username");
        Assert.assertNotNull(username);
        testMailAddress = username.replace("${env.HOSTNAME}", localHostName);

        testMailServer = mailProperties.getProperty("mail.server");
        testMailPasswd = mailProperties.getProperty("mail.password");
        testMailDelay = Integer
                .parseInt(mailProperties.getProperty("mail.delay"));
    }

    public String getProperiesForComputerName(String propertyPath) {
        String path = propertyPath;
        if (retrieveComputerName().equals("ctmg2")) {
            path = "ctmg2/" + propertyPath;
        }
        return path;
    }

    /**
     * Execute getuserdata with valid data.
     */
    @Test
    public void testGetUserData_Ok() throws Exception {
        List<UserData> userData = userNotification
                .getUserData(testDatabase.getDBconnection());
        Assert.assertNotNull(userData);
        Assert.assertEquals(2, userData.size());
        for (int i = 0; i < userData.size(); i++) {
            Assert.assertNotNull(userData.get(i).email);
            Assert.assertNotNull(userData.get(i).userid);
            Assert.assertNotNull(userData.get(i).olduserid);
        }
    }

    /**
     * Execute getuserdata, without any records in the db.
     */
    @Test
    public void testGetUserData_noRecords() throws Exception {
        testDatabase.clean();
        List<UserData> userData = userNotification
                .getUserData(testDatabase.getDBconnection());
        Assert.assertNotNull(userData);
        Assert.assertEquals(0, userData.size());
    }

    /**
     * Execute send mail with valid data.
     */
    @Test
    public void testSendMail_Ok() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        UserData ud = new UserData();
        ud.email = testMailAddress;
        ud.userid = "newid";
        ud.olduserid = "oldid";
        userData.add(ud);

        UserData ud2 = new UserData();
        ud2.email = testMailAddress;
        ud2.userid = "newid2";
        ud2.olduserid = "oldid2";
        userData.add(ud2);

        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        userNotification.sendMail(userData, unProperties);
        assertNewMailCount(2);
    }

    private String retrieveComputerName() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName.indexOf('.') > 0) {
                hostName = hostName.substring(0, hostName.indexOf('.'));
            }
            return hostName;
        } catch (UnknownHostException e) {
            return "";
        }
    }

    /**
     * Execute send mail with valid data.
     */
    @Test
    public void testSendMail_OkWithPortAndPassword() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        UserData ud = new UserData();
        ud.email = testMailAddress;
        ud.userid = "newid";
        ud.olduserid = "oldid";
        userData.add(ud);

        UserData ud2 = new UserData();
        ud2.email = testMailAddress;
        ud2.userid = "newid2";
        ud2.olduserid = "oldid2";
        userData.add(ud2);

        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));

        unProperties.setProperty(HandlerUtils.MAIL_PORT, "25");
        unProperties.setProperty(HandlerUtils.MAIL_USER_PWD, testMailPasswd);
        unProperties.setProperty(HandlerUtils.MAIL_USER, testMailAddress);

        userNotification.sendMail(userData, unProperties);
        assertNewMailCount(2);
    }

    /**
     * Execute send mail without a valid server.
     */
    @Test(expected = MailOperationException.class)
    public void testSendMail_invServer() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        UserData ud = new UserData();
        ud.email = testMailAddress;
        ud.userid = "newid";
        ud.olduserid = "oldid";
        userData.add(ud);

        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));

        unProperties.setProperty(HandlerUtils.MAIL_SERVER, "invalidServer");
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Execute send mail without a valid "to". address.
     */
    @Test
    public void testSendMail_invToAdr() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        UserData ud = new UserData();
        ud.email = "@invalid.de";
        ud.userid = "newid";
        ud.olduserid = "oldid";
        userData.add(ud);

        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));

        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Execute send mail without a valid responce adress.
     */
    @Test(expected = MailOperationException.class)
    public void testSendMail_invRespAdress() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        UserData ud = new UserData();
        ud.email = "test@asd.de";
        ud.userid = "newid";
        ud.olduserid = "oldid";
        userData.add(ud);

        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));

        unProperties.setProperty(HandlerUtils.MAIL_RESPONSE_ADDRESS,
                "@invalid");
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Execute send mail without a valid userdata.
     */
    @Test
    public void testSendMail_invUserData() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        UserData ud = new UserData();
        ud.email = null;
        ud.userid = "newid";
        ud.olduserid = "oldid";
        userData.add(ud);
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Execute send mail without a valid "from" address.
     */
    @Test(expected = RuntimeException.class)
    public void testSendMail_nullRespAdress() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        userData.add(new UserData());
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(HandlerUtils.MAIL_RESPONSE_ADDRESS);
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Execute send mail without a valid server.
     */
    @Test(expected = RuntimeException.class)
    public void testSendMail_nullServer() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        userData.add(new UserData());
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(HandlerUtils.MAIL_SERVER);
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Execute with a missing body text in the default locale.
     */
    @Test(expected = RuntimeException.class)
    public void testPrepareMessageText_nullBodyDef() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(HandlerUtils.MAIL_BODY + setLocale(Locale.ENGLISH));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Execute with a missing body text in the additional locale.
     */
    @Test
    public void testPrepareMessageText_nullBody() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(HandlerUtils.MAIL_BODY + setLocale(Locale.GERMAN));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Check the function if the subject is not in the properties. (default
     * locale)
     */
    @Test(expected = RuntimeException.class)
    public void testSendMail_nullSubject() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        userData.add(new UserData());
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties
                .remove(HandlerUtils.MAIL_SUBJECT + setLocale(Locale.ENGLISH));
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Check the function if the subject is not in the properties. (additional
     * locale)
     */
    @Test
    public void testSendMail_nullSubjectAdd() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        userData.add(new UserData());
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties
                .remove(HandlerUtils.MAIL_SUBJECT + setLocale(Locale.GERMAN));
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Check the function if the footer is not in the properties. (default
     * locale)
     */
    @Test(expected = RuntimeException.class)
    public void testPrepareMessageText_nullFooter() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties
                .remove(HandlerUtils.MAIL_FOOTER + setLocale(Locale.ENGLISH));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Check the function if the footer is not in the properties. (additional
     * locale)
     */
    @Test
    public void testPrepareMessageText_nullFooterAdd() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties
                .remove(HandlerUtils.MAIL_FOOTER + setLocale(Locale.GERMAN));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Check the function if the description of the new userid is not in the
     * properties. (default locale)
     */
    @Test(expected = RuntimeException.class)
    public void testPrepareMessageText_nullNewIdDess() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(
                HandlerUtils.MAIL_BODY_NEWID + setLocale(Locale.ENGLISH));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Check the function if the description of the new userid is not in the
     * properties. (additional locale)
     */
    @Test
    public void testPrepareMessageText_nullNewIdDessAdd() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(
                HandlerUtils.MAIL_BODY_NEWID + setLocale(Locale.GERMAN));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Check the function if the description of the old userid is not in the
     * properties. (deault locale)
     */
    @Test(expected = RuntimeException.class)
    public void testPrepareMessageText_nullOldIdDess() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(
                HandlerUtils.MAIL_BODY_OLDID + setLocale(Locale.ENGLISH));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Check the function if the description of the old userid is not in the
     * properties. (additional locale)
     */
    @Test
    public void testPrepareMessageText_nullOldIdDessAdd() throws Exception {
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        unProperties.remove(
                HandlerUtils.MAIL_BODY_OLDID + setLocale(Locale.GERMAN));
        userNotification.prepareMessageText(unProperties);
    }

    /**
     * Check the function if there are no addresses to send mails to.
     */
    @Test
    public void testSendMail_noAdresses() throws Exception {
        List<UserData> userData = new ArrayList<UserData>();
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        userNotification.sendMail(userData, unProperties);
    }

    /**
     * Check the removal function when the columns are not existing.
     */
    @Test
    public void testCleanTable() throws Exception {
        assertColumn("platformuser", "useridcnt", true);
        assertColumn("platformuser", "olduserid", true);
        userNotification.cleanTable(testDatabase.getDBconnection());
        assertColumn("platformuser", "useridcnt", false);
        assertColumn("platformuser", "olduserid", false);
        initDb();
    }

    /**
     * Check the notify function which accumulates the getUsers, sendemail and
     * cleanDB function.
     */
    @Test
    public void testNotifyUsers() throws Exception {
        assertColumn("platformuser", "useridcnt", true);
        assertColumn("platformuser", "olduserid", true);
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        userNotification.notifyUsers(testDatabase.getDBconnection(),
                unProperties);
        assertColumn("platformuser", "useridcnt", false);
        assertColumn("platformuser", "olduserid", false);

        // Two user in the test db have a ueridcnt > 1
        assertNewMailCount(2);
        initDb();
    }

    /**
     * Check if the testSettings method works properly
     * 
     * @throws Exception
     */
    @Test
    public void testTestSettings() throws Exception {
        assertColumn("platformuser", "useridcnt", true);
        assertColumn("platformuser", "olduserid", true);
        Properties unProperties = getProperties(
                getProperiesForComputerName(unPropertiesFilePath));
        userNotification.testSettings(testDatabase.getDBconnection(),
                unProperties, testMailAddress);
        assertColumn("platformuser", "useridcnt", true);
        assertColumn("platformuser", "olduserid", true);
        assertNewMailCount(1);

    }

    /**
     * Small helper function to determine if a specific column of a table exists
     * or not exists.
     * 
     * @param tableName
     *            the name of the table which holds the column to check.
     * @param columnName
     *            the name of the column to check.
     * @param exists
     *            pass <code>true</code> to test if the column exists or
     *            <code>false</code> to check if the column does not exists.
     * 
     */
    private void assertColumn(String tableName, String columnName,
            boolean exists) throws SQLException {
        Statement stmt = testDatabase.getDBconnection().createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
                        + tableName + "' AND  COLUMN_NAME = '" + columnName
                        + "'");
        boolean resultAvailable = rs.next();
        stmt.close();
        rs.close();
        Assert.assertEquals(exists, resultAvailable);
    }

    /**
     * Returns the file object for a resource.
     * 
     * @param name
     *            the name of the resource located in the resource directory.
     * @return the file object for the passed resource name.
     */
    private static File getFile(String name) {
        System.out.println("get file: " + name);

        URL resource = UserNotificationIT.class.getResource("/" + name);

        Assert.assertNotNull(resource);
        return new File(resource.getFile());
    }

    /**
     * Sets the default locale and returns the matching postfix.
     * 
     * @param locale
     *            new default locale.
     * @return matching postfix.
     */
    private String setLocale(Locale locale) {
        Locale.setDefault(locale);
        return "_" + locale.getLanguage();
    }

    /**
     * Access the mailbox by using the parameter of the initMail function.
     * 
     * @param cleanInbox
     *            if set to true all messages of the mailbox will be deleted.
     * @param assertCount
     *            if set to true, the count of email in the mailbox will be
     *            checked again the passed value.
     * @param count
     *            the number of email which is expected to bin in the inbox.
     * 
     */
    private void checkInbox(boolean cleanInbox, boolean assertCount,
            int count) {
        try {
            Properties properties = new Properties();
            properties.put("mail.pop3.host", testMailServer);
            Session emailSession = Session.getDefaultInstance(properties);

            POP3Store emailStore = (POP3Store) emailSession.getStore("pop3");
            emailStore.connect(testMailAddress, testMailPasswd);

            Folder emailFolder = emailStore.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);

            Message[] messages = emailFolder.getMessages();
            if (cleanInbox) {
                for (int i = 0; i < messages.length; i++) {
                    messages[i].setFlag(Flags.Flag.DELETED, true);
                }
            }
            emailFolder.close(true);
            emailStore.close();

            if (assertCount) {
                Assert.assertEquals(count, messages.length);
            }
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method to call checkInbox for the propose to delete alle
     * messages in the inbox.
     */
    private void cleanInbox() {
        checkInbox(true, false, -1);
    }

    /**
     * Convenience method to call checkInbox for the propose check the number of
     * email in the inbox (the inbox will be cleaned afterwards)
     */
    private void assertNewMailCount(int count) throws Exception {
        Thread.sleep(testMailDelay);
        checkInbox(true, true, count);
    }

    /**
     * Small helper to update the email addresses in the test database.
     */
    private void injectTestMailAdress() throws Exception {
        Statement stmt = testDatabase.getDBconnection().createStatement();
        stmt.execute(
                "UPDATE platformuser SET email = '" + testMailAddress + "'");
    }

    /**
     * Read properties from the file denoted by given file name.
     * <p>
     * This method also checks for local settings which must be stored in a
     * corresponding local folder in the same directory.
     * 
     * @param filePath
     *            - given file name
     * @return the filled properties
     * @throws IOException
     *             if a problem occurred when reading the file
     */
    protected static Properties getProperties(String filePath)
            throws IOException {
        File propFile = getFile(filePath);
        Properties properties = HandlerUtils
                .readProperties(propFile.getAbsolutePath());

        File localFolder = new File(propFile.getParentFile(), "local");
        if (localFolder.exists()) {
            propFile = new File(localFolder, propFile.getName());
            if (propFile.canRead()) {
                Properties localProperties = HandlerUtils
                        .readProperties(propFile.getAbsolutePath());
                if (localProperties != null) {
                    properties.putAll(localProperties);
                }
            }
        }
        return properties;

    }
}
