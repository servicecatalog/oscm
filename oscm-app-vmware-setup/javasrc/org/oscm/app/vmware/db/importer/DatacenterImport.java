/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatacenterImport extends GenericImport {

    private static final Logger logger = LoggerFactory
            .getLogger(DatacenterImport.class);

    public static void main(String args[]) throws Exception {

        if (args.length < 5 || args.length > 6) {
            throw new RuntimeException(
                    "Usage: DatacenterImport <driverClass> <driverURL> <userName> <userPwd> <csvFile>");
        }

        DatacenterImport dcImport = new DatacenterImport(args[0], args[1],
                args[2], args[3], args[4]);
        dcImport.execute();
    }

    private final Map<String, Integer> vcenterCache = new HashMap<String, Integer>();

    public DatacenterImport(String driverClass, String driverURL,
            String userName, String userPwd, String csvFile) {
        super(driverClass, driverURL, userName, userPwd, csvFile);
    }

    public void execute() throws Exception {
        DatacenterCSV csv = null;
        try (Connection conn = getConnection();
                InputStream in = getFileInputStream();) {
            csv = new DatacenterCSV(in);
            Map<String, String> line = csv.readNext();
            while (line != null) {
                String vcenter = line.get(DatacenterCSV.COL_VCENTER);
                String datacenter = line.get(DatacenterCSV.COL_DATACENTER);
                String dcId = line.get(DatacenterCSV.COL_DATACENTER_ID);

                try {
                    addTableRow(conn, vcenter, datacenter, dcId);
                } catch (Exception e) {
                    logger.error("Failed to add row: " + vcenter + " "
                            + datacenter + " " + dcId);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
                try {
                    line = csv.readNext();
                } catch (Exception e) {
                    logger.error("Failed to read line from CSV file after row: "
                            + vcenter + " " + datacenter + " " + dcId);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("Failed to import datacenter. " + e.getMessage());
            throw e;
        } finally {
            try {
                if (csv != null) {
                    csv.close();
                }
            } catch (Exception e) {
                logger.error("Failed to close resources", e);
                throw e;
            }
        }
    }

    private void addTableRow(Connection con, String vcenter, String datacenter,
            String dcId) throws Exception {
        int vcenterKey = getVCenterKey(con, vcenter);
        String query = "insert into datacenter (TKEY, NAME, IDENTIFIER, VCENTER_TKEY) values (DEFAULT, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, datacenter);
            stmt.setString(2, dcId);
            stmt.setInt(3, vcenterKey);
            stmt.execute();
        }
    }

    private int getVCenterKey(Connection con, String vcenter) {
        int vcKey = -1;

        if (vcenterCache.containsKey(vcenter)) {
            return vcenterCache.get(vcenter).intValue();
        }

        String query = "SELECT tkey FROM vcenter WHERE name = ?";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vcKey = rs.getInt(1);
                vcenterCache.put(vcenter, Integer.valueOf(vcKey));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve vcenter " + vcenter,
                    e);
        }

        if (vcKey == -1) {
            throw new RuntimeException("Failed to retrieve vcenter " + vcenter);
        }
        return vcKey;
    }
}
