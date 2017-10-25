package org.oscm.dbtask;
/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                 
 *  Creation Date: 29.06.2017                                                      
 *                                                                              
 *******************************************************************************/

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * @author stavreva
 *
 */
public class MigrateSubscriptions extends DatabaseUpgradeTask {

    private Connection conn;
    private static final String QUERY_SELECT_ALL = "SELECT tkey FROM subscription;";
    private static final String QUERY_ADD_UUID = "UPDATE subscription SET uuid='";

    @Override
    public void execute() throws Exception {
        conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(QUERY_SELECT_ALL);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            long key = rs.getLong("tkey");
            String query = QUERY_ADD_UUID
                    + UUID.randomUUID().toString() + "' WHERE tkey='" + String.valueOf(key)+"';";
            PreparedStatement pstmtUpdate = conn.prepareStatement(query);
            pstmtUpdate.executeUpdate();
        }
    }

}
