/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 06.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws.base;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.oscm.test.setup.PropertiesReader;

/**
 * @author kulle
 * 
 */
public class AppDbClient implements Closeable {

    private Connection connection;
    private Properties dbProperties;

    public AppDbClient() throws Exception {
        dbProperties = loadAppDbProperties();
        connection = establishConnection();
    }

    private Properties loadAppDbProperties() throws Exception {
        PropertiesReader reader = new PropertiesReader();
        return reader.loadAppDbProperties();
    }

    public Connection establishConnection() throws SQLException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String serverName = dbProperties.getProperty("db.host");
        String portNumber = dbProperties.getProperty("db.port");
        String databaseName = dbProperties.getProperty("db.name");
        String userName = dbProperties.getProperty("db.user");
        String password = dbProperties.getProperty("db.pwd");

        String connectionUrl = "jdbc:postgresql://" + serverName + ":"
                + portNumber + "/" + databaseName;
        Connection connection = DriverManager.getConnection(connectionUrl,
                userName, password);
        return connection;
    }

    public void insertRorControllerMapping(String organizationId)
            throws SQLException {
        try (PreparedStatement insertStmt = connection
                .prepareStatement("INSERT INTO configurationsetting VALUES('BSS_ORGANIZATION_ID','"
                        + organizationId + "','ess.ror');");) {
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            try (PreparedStatement updtStmt = connection
                    .prepareStatement("UPDATE configurationsetting SET settingvalue='"
                            + organizationId
                            + "' WHERE settingkey='BSS_ORGANIZATION_ID';");) {
                updtStmt.executeUpdate();
            }
        }
    }

    public void insertIaasApiUri(String iaasApiUri) throws SQLException {
        try (PreparedStatement insertStmt = connection
                .prepareStatement("INSERT INTO configurationsetting VALUES('IAAS_API_URI','"
                        + iaasApiUri + "','ess.ror');");) {
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            try (PreparedStatement updtStmt = connection
                    .prepareStatement("UPDATE configurationsetting SET settingvalue='"
                            + iaasApiUri + "' WHERE settingkey='IAAS_API_URI';");) {
                updtStmt.executeUpdate();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }
}
