/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *******************************************************************************/
package org.oscm.test.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * A database client for an PostGreSQL DB running on localhost. This
 * implementation should normally not be used by unit tests but may help
 * debugging tests. Note that this implementation does not create or update the
 * schema.
 * 
 * @author hoffmann
 */
public class ClientTestDBPostGreSQL implements ITestDB {

    /**
     * Set this to false if the test databases' data should not be purged before
     * running unit tests.
     */
    private static final boolean purgeData = true;

    private final String scriptDir;
    private final String databaseName;
    private final String userName;
    private final String password;
    private final String serverName;
    private final int portNumber;
    private DataSource ds;

    public ClientTestDBPostGreSQL(final String scriptDir, String databaseName,
            String dbUser, String dbPwd) {
        this(scriptDir, databaseName, dbUser, dbPwd, "localhost", 5432);
    }

    public ClientTestDBPostGreSQL(final String scriptDir, String databaseName,
            String dbUser, String dbPwd, String serverName, int port) {
        this.scriptDir = scriptDir;
        this.databaseName = databaseName;
        this.userName = dbUser;
        this.password = dbPwd;
        this.serverName = serverName;
        this.portNumber = port;
        ds = new PostGreSQLDataSource();
    }

    @Override
    public DataSource getDataSource() {
        return ds;
    }

    @Override
    public void initialize() throws Exception {
        if (purgeData) {
            TestDBSetup.purgeInstanceData(ds, userName, true);
        }
    }

    @Override
    public void loadSchema() throws Exception {
        if (purgeData) {
            TestDBSetup.loadSchema(ds, scriptDir, DatabaseVersionInfo.MAX,
                    "postgresql");
        }
    }

    @Override
    public void loadSchema(DatabaseVersionInfo toVersion) throws Exception {
        if (purgeData) {
            TestDBSetup.loadSchema(ds, scriptDir, toVersion, "postgresql");
        }
    }

    @Override
    public void purgeSchema() throws Exception {
        if (purgeData) {
            TestDBSetup.purgeSchema(ds, userName);
        }
    }

    @Override
    public void clearBusinessData() throws Exception {
        if (purgeData) {
            TestDBSetup.purgeInstanceData(ds, userName, false);
        }
    }

    /**
     * Internal class for the PostGreSQL integration.
     * 
     * @author Mike J&auml;ger
     * 
     */
    private final class PostGreSQLDataSource implements DataSource {
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Connection getConnection(String username, String password)
                throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Connection getConnection() throws SQLException {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            Connection conn = DriverManager.getConnection("jdbc:postgresql://"
                    + serverName + ":" + portNumber + "/" + databaseName,
                    userName, password);
            return conn;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            throw new UnsupportedOperationException();
        }
    }

}
