/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 09.07.15 11:27
 *
 *******************************************************************************/

package org.oscm.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementBuilder {

    protected PreparedStatement statement;
    private Connection connection;

    public PreparedStatementBuilder(Connection connection) throws SQLException {
        this.connection = connection;
    }

    public PreparedStatementBuilder setQuery(String query) throws SQLException {
        statement = connection.prepareStatement(query);
        return this;
    }

    public PreparedStatementBuilder addLongParam(int paramIndex, long param)
            throws SQLException {
        statement.setLong(paramIndex, param);
        return this;
    }

    public PreparedStatementBuilder addStringParam(int paramindex, String param)
            throws SQLException {
        statement.setString(paramindex, param);
        return this;
    }

    public PreparedStatement build() {
        return statement;
    }
}
