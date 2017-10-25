/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.10.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class UserKeyForOrganizationQuery extends AbstractKeyQuery{
    
    private String orgId;
    private String userId;
    
    UserKeyForOrganizationQuery(DataSource ds, String userId, String orgId) {
        super(ds);
        this.userId = userId;
        this.orgId = orgId;
    }

    @Override
    protected String getStatement() {
        String query = "SELECT u.tkey FROM PlatformUser u "
                + "LEFT JOIN Organization o ON u.organizationkey=o.tkey "
                + "WHERE u.userid=? AND o.organizationid=?";

        return query;
    }

    @Override
    protected void setParameters(PreparedStatement p) throws SQLException {
        p.setString(1, this.userId);
        p.setString(2, this.orgId);    
    }

}
