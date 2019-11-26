/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2019                                           
 *                                                                                                                                 
 *  Creation Date: 26.11.2019                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Migrate organizations without name.
 * <p>
 * 
 * @author goebel
 */
public class MigrateOrganizationNames extends DatabaseUpgradeTask {
    private static final String QUERY_ALL_ANONYMOUS_ORGS = "SELECT * FROM organization WHERE name = NULL or name = '';";
    private static final String QUERY_SET_NAME = "UPDATE organization SET name='%s' WHERE tkey=%s;";
    
    @Override
    public void execute() throws Exception {
        
        final Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(QUERY_ALL_ANONYMOUS_ORGS);
        
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            String name = rs.getString("name");
            String tkey = String.valueOf(rs.getLong("tkey"));
            String orgid = rs.getString("organizationid");
            if (isBlank(name)) {
                String query = String.format(QUERY_SET_NAME, orgid, tkey);
                PreparedStatement ps = conn.prepareStatement(query);
                ps.executeUpdate();
            }
        }
    }
    
    private boolean isBlank(String name) {
        return (name == null || name.trim().length() == 0);
    }
}
