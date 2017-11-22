/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 04.05.2011                                                      
 *                                                                              
 *  Completion Time: 04.05.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * SQL query to read the roles of a PlatformUser.
 * 
 * @author cheld
 * 
 */
class RoleQuery extends AbstractQuery {

    private String userKey;

    private List<String> roleNames = new ArrayList<String>();


    RoleQuery(DataSource ds, String userKey) {
        super(ds);
        this.userKey = userKey;
    }

    @Override
    protected String getStatement() {
        return "SELECT r.rolename FROM RoleAssignment a, UserRole r WHERE a.userrole_tkey=r.tkey AND a.user_tkey=?";
    }

    @Override
    protected void setParameters(PreparedStatement p) throws SQLException {
        long queryUserKey = 0;
        try {
            queryUserKey = Long.parseLong(userKey);
            p.setLong(1, queryUserKey);
        } catch (NumberFormatException e) {
            p.setNull(1, Types.BIGINT);
        }
    }

    @Override
    protected void mapResult(ResultSet rs) throws SQLException {
        roleNames.add(rs.getString(1));
    }

    List<String> getRoleNames() {
        return roleNames;
    }

}
