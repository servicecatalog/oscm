/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                     
 *                                                                              
 *  Creation Date: 14.01.2010                                             
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Base class to perform a SQL query.
 * 
 */
abstract public class AbstractKeyQuery{

    private DataSource ds;
    private long key;
    
    AbstractKeyQuery(DataSource ds) {
        this.ds = ds;
    }

    /**
     * Perform the SQL query.
     * 
     * @throws SQLException
     *             if the SQL query fails
     */
    void execute() throws SQLException {
        Connection c = null;
        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            c = ds.getConnection();
            p = c.prepareStatement(getStatement());
            setParameters(p);
            rs = p.executeQuery();
            while (rs.next()) {
                mapResult(rs);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (p != null) {
                try {
                    p.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }

    }
    
    public long getKey(){
        return this.key;
    }
    
    /**
     * Processes one row of the result set.
     * 
     * @param the
     *            result set.
     * @throws SQLException
     *             if a database access error occurs.
     */
    public void mapResult(ResultSet rs) throws SQLException{
        this.key = rs.getLong(1);  
    }
    
    /**
     * Returns the SQL statement that should be performed.
     * 
     * @return the SQL statement that should be performed.
     */
    abstract protected String getStatement();

    /**
     * Sets all parameters of the prepared statement.
     * 
     * @param the
     *            prepared statement
     * @throws SQLException
     *             if a database access error occurs.
     */
    abstract protected void setParameters(PreparedStatement p)
            throws SQLException;

}
