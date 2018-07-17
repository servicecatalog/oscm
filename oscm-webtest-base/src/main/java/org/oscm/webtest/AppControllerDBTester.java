/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: 10 7, 2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.webtest;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;

public class AppControllerDBTester {

    private static final String DB_DRIVER = "db.driver.class";
    private static final String DB_TYPE = "db.type";
    private static final String DB_PORT = "db.port";
    private static final String DB_HOST = "db.host";
    private static final String DB_NAME = "db.name";
    private String DB_CONNECTION_URL = "jdbc:%s://%s:%s/%s";
    private static final String DB_USERNAME = "db.user";
    private static final String DB_PASSWORD = "db.pwd";
    private static String filePath = "../oscm-devruntime/javares/local/%s/db-app.properties";
    public static final int IV_BYTES = 16;
    public static final int KEY_BYTES = 16;
    // private static SecretKey key;

    private IDatabaseTester databaseTester;
    protected static final Logger logger = Logger.getLogger(WebTester.class);
    protected Properties prop;

    public AppControllerDBTester() throws Exception {

        loadPropertiesFile();
        DB_CONNECTION_URL = String.format(DB_CONNECTION_URL,
                getPropertie(DB_TYPE), getPropertie(DB_HOST),
                getPropertie(DB_PORT), getPropertie(DB_NAME));
        databaseTester = new JdbcDatabaseTester(getPropertie(DB_DRIVER),
                DB_CONNECTION_URL, getPropertie(DB_USERNAME),
                getPropertie(DB_PASSWORD));
    }

    private void loadPropertiesFile() throws Exception {

        Map<String, String> env = System.getenv();
        String localhost = env.get("HOSTNAME");
        if (StringUtils.isEmpty(localhost)) {
            localhost = InetAddress.getLocalHost().getHostName();
        }
        filePath = String.format(filePath, localhost);

        prop = new Properties();
        FileInputStream fis = new FileInputStream(filePath);
        prop.load(fis);
        fis.close();

    }

    public void close() throws Exception {
        databaseTester.getConnection().close();
    }

    public void clearSetting(String controllerId)
            throws SQLException, Exception {
        String query = "Select * FROM configurationsetting where controllerid = '"
                + controllerId + "';";
        Statement stmt = databaseTester.getConnection().getConnection()
                .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(query);
        if (countRows(rs) > 0) {
            deleteSetting(controllerId);
        }
    }

    private int countRows(ResultSet rs) throws SQLException {
        int totalRows = 0;
        try {
            rs.last();
            totalRows = rs.getRow();
            rs.beforeFirst();
        } catch (Exception ex) {
            return 0;
        }
        return totalRows;
    }

    public void insertSetting(String settingKey, String settingValue,
            String controllerId) throws SQLException, Exception {

        String query = "INSERT INTO configurationsetting VALUES ('" + settingKey
                + "','" + settingValue + "', '" + controllerId + "');";
        Statement stmt = databaseTester.getConnection().getConnection()
                .createStatement();
        stmt.executeUpdate(query);
    }

    public void deleteSetting(String controllerId)
            throws SQLException, Exception {
        String query = "DELETE FROM configurationsetting WHERE controllerid = '"
                + controllerId + "';";
        Statement stmt = databaseTester.getConnection().getConnection()
                .createStatement();
        stmt.executeUpdate(query);
    }

    public void updateEncryptPWDasAdmin(String controllerId)
            throws SQLException, Exception {
        String query = "UPDATE configurationsetting SET settingvalue = "
                + "(SELECT settingvalue FROM configurationsetting WHERE controllerid = 'PROXY' AND settingkey = 'BSS_USER_PWD') "
                + "WHERE controllerid = '" + controllerId
                + "' AND settingkey = 'BSS_USER_PWD';";
        Statement stmt = databaseTester.getConnection().getConnection()
                .createStatement();
        stmt.executeUpdate(query);
    }

    public String getPropertie(String propertie) {
        return prop.getProperty(propertie);
    }

    public void log(String msg) {
        logger.info(msg);
    }
}
