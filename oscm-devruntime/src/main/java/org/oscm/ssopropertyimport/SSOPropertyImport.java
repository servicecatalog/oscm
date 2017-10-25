/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ssopropertyimport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.oscm.setup.HandlerUtils;

/**
 * @author Qiu
 * 
 */
public class SSOPropertyImport {
    private static final String TABLE_NAME = "platformuser";
    private static final String AUTH_MODE_SAML_SP = "SAML_SP";
    private static final String AUTH_MODE = "AUTH_MODE";
    private static final int argNum = 6;
    private static final String ADMIN_USER_ID = "ADMIN_USER_ID";

    public static void main(String args[]) throws Exception {

        if (args.length != argNum) {
            throw new RuntimeException(
                    "Usage: java PropertyImport <driverClass> <driverURL> <userName> <userPwd> <configPropertyFile> <ssoPropertyFile>");
        }

        SSOPropertyImport propertyImport = new SSOPropertyImport(args[0],
                args[1], args[2], args[3], args[4], args[5]);
        propertyImport.execute();
    }

    private String driverURL;
    private String userName;
    private String userPwd;
    private String configPropertyFile;
    private String ssoPropertyFile;

    public SSOPropertyImport() {
    }

    public SSOPropertyImport(String driverClass, String driverURL,
            String userName, String userPwd, String configPropertyFile,
            String ssoPropertyFile) {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("DriverClass '" + driverClass
                    + "' could not be found");
        }

        this.driverURL = driverURL;
        this.userName = userName;
        this.userPwd = userPwd;
        this.configPropertyFile = configPropertyFile;
        this.ssoPropertyFile = ssoPropertyFile;

    }

    public void execute() throws Exception {
        if (ifImportNeeded()) {
            updateUserId();
        }
    }

    private boolean ifImportNeeded() throws Exception {
        String authMode = getProperty(configPropertyFile, AUTH_MODE);
        if (AUTH_MODE_SAML_SP.equals(authMode)) {
            return true;
        }
        return false;
    }

    String getProperty(String propertyFile, String propertyName)
            throws FileNotFoundException, IOException {
        Properties property = HandlerUtils.readProperties(propertyFile);
        String propertyValue = property.getProperty(propertyName);
        return propertyValue;
    }

    void updateUserId() throws FileNotFoundException, IOException, SQLException {
        String adminUserId = getProperty(ssoPropertyFile, ADMIN_USER_ID);
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnetion();
            stmt = conn.createStatement();
            stmt.executeUpdate(String.format(
                    "UPDATE %s SET userid = '%s' WHERE tkey = 1000",
                    TABLE_NAME, adminUserId));
            stmt.close();
            conn.close();

        } finally {
            closeStatement(stmt);
            closeConnection(conn);

        }
    }

    private void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not close resources");
            }
        }

    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not close resources");
            }
        }

    }

    protected Connection getConnetion() throws SQLException {
        return DriverManager.getConnection(driverURL, userName, userPwd);
    }
}
