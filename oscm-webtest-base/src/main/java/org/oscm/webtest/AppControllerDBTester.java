/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018                                           
 *                                                                                                                                 
 *  Creation Date: 10 7, 2018                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.webtest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;

public class AppControllerDBTester {

    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_TYPE = "postgresql";
    private static final String DB_PORT = "5432";
    private static final String DB_HOST = "localhost";
    private static final String DB_NAME = "bssapp";
    private static final String DB_USERNAME = "bssappuser";
    private static final String DB_PASSWORD = "secret";
    
    private String DB_CONNECTION_URL = "jdbc:%s://%s:%s/%s";

    public static final int IV_BYTES = 16;
    public static final int KEY_BYTES = 16;

    private IDatabaseTester databaseTester;
    protected static final Logger logger = Logger.getLogger(WebTester.class);
    protected Properties prop;

    public AppControllerDBTester() throws Exception {

        DB_CONNECTION_URL = String.format(DB_CONNECTION_URL,
                DB_TYPE, DB_HOST,
               DB_PORT, DB_NAME);
        databaseTester = new JdbcDatabaseTester(DB_DRIVER,
                DB_CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
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

    public void log(String msg) {
        logger.info(msg);
    }
}
