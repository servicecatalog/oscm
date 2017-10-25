/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.setup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.oscm.converter.PropertiesLoader;

public class HandlerUtils {

    /**
     * Property for the database user password.
     */
    public static final String DB_PWD = "db.pwd";
    /**
     * Property key for the database user name.
     */
    public static final String DB_USER = "db.user";
    /**
     * Property key for the database host name.
     */
    public static final String DB_HOST = "db.host";
    /**
     * Property key for the database port.
     */
    public static final String DB_PORT = "db.port";
    /**
     * Property key for the database name.
     */
    public static final String DB_NAME = "db.name";
    /**
     * Property key for the database URL.
     */
    public static final String DB_URL = "db.url";
    /**
     * Property key for the database driver class name.
     */
    public static final String DB_DRIVER_CLASS = "db.driver.class";
    /**
     * Property key for the database system type.
     */
    public static final String DB_TYPE = "db.type";

    /**
     * The name of the machine hosting the mail server.
     */
    public static final String MAIL_SERVER = "MAIL_SERVER";

    /**
     * The port of the mail server. The default is 25.
     */
    public static final String MAIL_PORT = "MAIL_PORT";

    /**
     * The mail address used by the server as the sender of emails.
     */
    public static final String MAIL_RESPONSE_ADDRESS = "MAIL_RESPONSE_ADDRESS";

    /**
     * The name of the user to be used for authentication against the SMTP mail
     * system.
     */
    public static final String MAIL_USER = "MAIL_USER";

    /**
     * The password of the mail user.
     */
    public static final String MAIL_USER_PWD = "MAIL_USER_PWD";

    /**
     * Text pattern for the notification email.
     */
    public static final String MAIL_BODY = "MAIL_BODY";

    /**
     * The subject of the email.
     */
    public static final String MAIL_SUBJECT = "MAIL_SUBJECT";

    /**
     * The footer of the email.
     */
    public static final String MAIL_FOOTER = "MAIL_FOOTER";

    /**
     * The old user id.
     */
    public static final String MAIL_BODY_OLDID = "MAIL_BODY_OLDID";

    /**
     * The new user id.
     */
    public static final String MAIL_BODY_NEWID = "MAIL_BODY_NEWID";

    /**
     * Establishes a connection to the database.
     * 
     * @param connectionProperties
     *            The database properties to be used.
     * @return The connection to the database.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static Connection establishDatabaseConnection(
            Properties connectionProperties) throws SQLException,
            ClassNotFoundException {
        String className = connectionProperties.getProperty(DB_DRIVER_CLASS);
        String dbUrl = "jdbc:" + connectionProperties.getProperty(DB_TYPE)
                + "://" + connectionProperties.getProperty(DB_HOST) + ":"
                + connectionProperties.getProperty(DB_PORT) + "/"
                + connectionProperties.getProperty(DB_NAME);
        String dbUser = connectionProperties.getProperty(DB_USER);
        String dbUserPwd = connectionProperties.getProperty(DB_PWD);

        checkNotNull(className, DB_DRIVER_CLASS);
        checkNotNull(dbUrl, DB_URL);
        checkNotNull(dbUser, DB_USER);
        checkNotNull(dbUserPwd, DB_PWD);

        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver class '" + className
                    + "' is not in the classpath. Please correct settings.");
            throw e;
        }

        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbUserPwd);
        } catch (SQLException e) {
            System.out
                    .println("Could not establish a connection to the database.");
            throw e;
        }
    }

    /**
     * Reads the properties from the (db.)properties file, required to establish
     * a database connection.
     * 
     * @return The properties as given in the file.
     * @throws FileNotFoundException
     *             Thrown in case the file could not be found.
     * @throws IOException
     *             Thrown in case it cannot be read from the file.
     */
    public static Properties readProperties(String dbPropertiesFile)
            throws FileNotFoundException, IOException {
        try {
            final InputStream in = new FileInputStream(dbPropertiesFile);
            return PropertiesLoader.loadProperties(in);
        } catch (FileNotFoundException e) {
            System.out
                    .println("Cannot find the file '"
                            + dbPropertiesFile
                            + "'! Please ensure it's in the same directory you call this class from.");
            throw e;
        }
    }

    /**
     * Checks if the value of the given String is null. If it is, it prompts a
     * message.
     */
    public static void checkNotNull(String valueToCheck, String key) {
        if (valueToCheck == null) {
            throw new RuntimeException("Could not find the setting for '" + key
                    + "'.");
        }
    }

}
