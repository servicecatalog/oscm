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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPPoolImport extends GenericImport {

    private static final Logger logger = LoggerFactory
            .getLogger(IPPoolImport.class);

    public static void main(String args[]) throws Exception {
        if (args.length < 5 || args.length > 6) {
            throw new RuntimeException(
                    "Usage: VMImport <driverClass> <driverURL> <userName> <userPwd> <csvFile>");
        }

        IPPoolImport ipImport = new IPPoolImport(args[0], args[1], args[2],
                args[3], args[4]);
        ipImport.execute();
    }

    public IPPoolImport(String driverClass, String driverURL, String userName,
            String userPwd, String csvFile) {
        super(driverClass, driverURL, userName, userPwd, csvFile);
    }

    public void execute() throws Exception {
        IPPoolCSV csv = null;

        try (Connection conn = getConnection();
                InputStream in = getFileInputStream();) {
            csv = new IPPoolCSV(in);
            Map<String, String> line = csv.readNext();
            while (line != null) {
                String vcenter = line.get(IPPoolCSV.COL_VCENTER);
                String datacenter = line.get(IPPoolCSV.COL_DATACENTER);
                String cluster = line.get(IPPoolCSV.COL_CLUSTER);
                String vlan = line.get(IPPoolCSV.COL_VLAN);
                String ipaddress = line.get(IPPoolCSV.COL_IPADDRESS);
                try {
                    int vlanTkey = getVlanTkey(vcenter, datacenter, cluster,
                            vlan);
                    addTableRow(conn, ipaddress, vlanTkey);
                } catch (Exception e) {
                    logger.error("Failed to add row: " + vcenter + " "
                            + datacenter + " " + cluster + " " + vlan + " "
                            + ipaddress);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
                try {
                    line = csv.readNext();
                } catch (Exception e) {
                    logger.error("Failed to read line from CSV file after row: "
                            + vcenter + " " + datacenter + " " + cluster + " "
                            + vlan + " " + ipaddress);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("Failed to import IP pool. " + e.getMessage());
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

    private void addTableRow(Connection con, String ipaddress, int vlanKey)
            throws Exception {
        String query = "insert into ippool (tkey, ip_address, in_use, vlan_tkey) values (DEFAULT,?,?,?)";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, ipaddress);
            stmt.setBoolean(2, false);
            stmt.setInt(3, vlanKey);
            stmt.execute();
        }
    }

}
