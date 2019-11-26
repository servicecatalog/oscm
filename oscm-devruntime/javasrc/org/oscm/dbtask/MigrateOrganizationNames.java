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
import java.util.HashMap;
import java.util.Map;

/**
 * @author goebel
 *
 */
public class MigrateOrganizationNames extends DatabaseUpgradeTask {

    private Connection conn;
    private static final String QUERY_ALL_ANONYMOUS_ORGS = "SELECT * FROM organization WHERE name = NULL or name = '';";
    private static final String QUERY_SET_NAME = "UPDATE organization SET name='%s' WHERE tkey=%s;";
    
    @Override
    public void execute() throws Exception {
        conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(QUERY_ALL_ANONYMOUS_ORGS);
        
        Map<String, String> toUpdate = new HashMap<String, String>();
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {

            String name = rs.getString("name");
            String tkey = String.valueOf(rs.getLong("tkey"));
            String orgid = rs.getString("organizationid");
            if (name == null || name.trim().length() == 0) {
                String query = String.format(QUERY_SET_NAME, orgid, tkey);
                PreparedStatement pstmtUpdate = conn.prepareStatement(query);
                pstmtUpdate.executeUpdate();
            }
        }

    }

}
