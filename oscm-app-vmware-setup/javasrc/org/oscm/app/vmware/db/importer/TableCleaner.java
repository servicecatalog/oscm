/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-13
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableCleaner extends GenericImport {

    private static final Logger logger = LoggerFactory
            .getLogger(TableCleaner.class);

    public TableCleaner(String driverClass, String driverURL, String userName,
            String userPwd) {
        super(driverClass, driverURL, userName, userPwd, null);
    }

    public static void main(String args[]) throws Exception {

        if (args.length < 4 || args.length > 4) {
            throw new RuntimeException(
                    "Usage: deleteAll <driverClass> <driverURL> <userName> <userPwd>");
        }

        TableCleaner tableCleaner = new TableCleaner(args[0], args[1], args[2],
                args[3]);
        tableCleaner.execute();
    }

    public void execute() throws Exception {

        try (Connection conn = getConnection();) {

            try {
                deleteTableContent(conn, "ippool");
                deleteTableContent(conn, "vlan");
                deleteTableContent(conn, "cluster");
                deleteTableContent(conn, "datacenter");
                deleteVcenterSequences(conn);
                deleteTableContent(conn, "vcenter");
            } catch (Exception e) {
                logger.error("failed to delete content");
                logger.error(e.getMessage());
                conn.rollback();
                throw e;
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("failed to delete content.", e);
            throw e;
        }
    }

    private void deleteTableContent(Connection con, String tableName)
            throws Exception {
        String query = "delete from " + tableName;
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.execute();
        }
    }
    
    private void deleteVcenterSequences(Connection con) {
        String query = "SELECT identifier FROM vcenter";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString(1);
                String sequenceName = "vcenter_" + id + "_seq";
                deleteSequence(con, sequenceName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to remove vcentre sequences");
        }
    }

    private void deleteSequence(Connection con, String name)
            throws SQLException {
        String query = "drop sequence if exists " + name;
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.execute();
        }
    }
}
