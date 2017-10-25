/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.10.2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class UserKeyForTenantQuery extends AbstractKeyQuery {

    private String userId;
    private String tenantId;

    UserKeyForTenantQuery(DataSource ds, String userId, String tenantId) {
        super(ds);
        this.userId = userId;
        this.tenantId = tenantId;
    }

    @Override
    protected String getStatement() {

        String query = "SELECT u.tkey FROM PlatformUser u "
                + "LEFT JOIN Organization o ON u.organizationkey=o.tkey "
                + "LEFT JOIN Tenant t ON o.tenant_tkey=t.tkey "
                + "WHERE u.userid=? AND t.tenantid=?";

        return query;
    }

    @Override
    protected void setParameters(PreparedStatement p) throws SQLException {
        p.setString(1, this.userId);
        p.setString(2, this.tenantId);
    }
}
