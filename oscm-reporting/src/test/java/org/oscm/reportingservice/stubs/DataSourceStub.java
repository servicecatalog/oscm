/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.02.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.stubs;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * @author Mike J&auml;ger
 * 
 */
public class DataSourceStub implements DataSource {

    private Connection conn;

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        return conn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection(java.lang.String,
     * java.lang.String)
     */
    @Override
    public Connection getConnection(String username, String password)
            throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getLoginTimeout()
     */
    @Override
    public int getLoginTimeout() throws SQLException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {

        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {

        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {

        return false;
    }

}
