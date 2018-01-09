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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VCenterImport extends GenericImport {

    private static final Logger logger = LoggerFactory
            .getLogger(VCenterImport.class);

    public static void main(String args[]) throws Exception {
        if (args.length < 5 || args.length > 6) {
            throw new RuntimeException(
                    "Usage: VCenterImport <driverClass> <driverURL> <userName> <userPwd> <csvFile>");
        }

        VCenterImport vcImport = new VCenterImport(args[0], args[1], args[2],
                args[3], args[4]);
        vcImport.execute();
    }

    public VCenterImport(String driverClass, String driverURL, String userName,
            String userPwd, String csvFile) {
        super(driverClass, driverURL, userName, userPwd, csvFile);
    }

    public void execute() throws Exception {
        VCenterCSV csv = null;

        try (Connection conn = getConnection();
                InputStream in = getFileInputStream();) {
            csv = new VCenterCSV(in);
            Map<String, String> line = csv.readNext();
            while (line != null) {
                String vcenterId = line.get(VCenterCSV.COL_VCENTER_IDENTIFIER);
                String vcenterName = line.get(VCenterCSV.COL_VCENTER_NAME);
                String url = line.get(VCenterCSV.COL_URL);
                String userid = line.get(VCenterCSV.COL_USER_ID);
                String password = line.get(VCenterCSV.COL_PASSWORD);
                String tkey = line.get(VCenterCSV.COL_TKEY);
                boolean rowExists = false;
                String oldVcId = null;
                
                try {
                    rowExists = rowExists(conn, tkey);
                    if(rowExists){
                        oldVcId = getVCenterId(conn, tkey);
                        updateTableRow(conn, vcenterId, vcenterName, url, userid, password, tkey);
                    } else{
                        addTableRow(conn, vcenterId, vcenterName, url, userid, password, tkey);
                    }
                } catch (Exception e) {
                    logger.error("failed to add row:  " + tkey + " " + vcenterId
                            + " " + vcenterName);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
                try {
                    if (rowExists) {
                        if (!vcenterId.equals(oldVcId)) {
                            updateSequence(conn, oldVcId, vcenterId);
                        }
                    } else {
                        createSequence(conn, vcenterId);
                    }
                } catch (Exception e) {
                    logger.error("failed to create sequence for   " + tkey + " "
                            + vcenterId + " " + vcenterName);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
                try {
                    line = csv.readNext();
                } catch (Exception e) {
                    conn.rollback();
                    logger.error("Failed to read line from CSV file after row: "
                            + tkey + " " + vcenterId + " " + vcenterName);
                    logger.error(e.getMessage());
                    throw e;
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("failed to import vcenter settings.", e);
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
    
    
    private void createSequence(Connection con, String vcenterId)
            throws Exception {
        String query = "create sequence vcenter_" + vcenterId.trim()
                + "_seq increment 1 minvalue 1 maxvalue 10000 start 1 cycle";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.execute();
        }
    }

    private void addTableRow(Connection con, String vcId, String vcName,
            String url, String userid, String password, String tkey)
                    throws Exception {

        String query = "insert into vcenter (TKEY, NAME, IDENTIFIER, URL, USERID, PASSWORD) values(?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setInt(1, Integer.parseInt(tkey));
            stmt.setString(2, vcName);
            stmt.setString(3, vcId);
            stmt.setString(4, url);
            stmt.setString(5, userid);
            stmt.setString(6, password);
            stmt.execute();
        }
    }
    
    private void updateTableRow(Connection con, String vcId, String vcName,
            String url, String userid, String password, String tkey)
                    throws Exception {

        String query = "UPDATE vcenter set NAME=?, IDENTIFIER=?, URL=?, USERID=?, PASSWORD=? WHERE TKEY=?";

        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, vcName);
            stmt.setString(2, vcId);
            stmt.setString(3, url);
            stmt.setString(4, userid);
            stmt.setString(5, password);
            stmt.setInt(6, Integer.parseInt(tkey));
            stmt.execute();
        }
    }
    
    private void updateSequence(Connection con, String oldVcId, String newVcId)
            throws Exception {
        String query = "alter sequence vcenter_" + oldVcId.trim()
                + "_seq rename to vcenter_" + newVcId.trim() + "_seq";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.execute();
        }
    }
    
    private boolean rowExists(Connection con, String tkey) throws Exception {
        String query = "select count(*) as count FROM vcenter WHERE TKEY=?";

        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setInt(1, Integer.parseInt(tkey));
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt("count");
                if (count > 0) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
    }
    
    private String getVCenterId(Connection con, String tkey) {

        String query = "SELECT identifier FROM vcenter WHERE tkey = ?";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setInt(1, Integer.parseInt(tkey));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve vcenter " + tkey,
                    e);
        }
        return null;
    }
}
