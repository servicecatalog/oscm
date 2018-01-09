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

public class VLANImport extends GenericImport {

    private static final Logger logger = LoggerFactory
            .getLogger(VLANImport.class);

    public static void main(String args[]) throws Exception {
        if (args.length < 5 || args.length > 6) {
            throw new RuntimeException(
                    "Usage: VLANImport <driverClass> <driverURL> <userName> <userPwd> <csvFile>");
        }
        VLANImport ipImport = new VLANImport(args[0], args[1], args[2], args[3],
                args[4]);
        ipImport.execute();
    }

    public VLANImport(String driverClass, String driverURL, String userName,
            String userPwd, String csvFile) {
        super(driverClass, driverURL, userName, userPwd, csvFile);
    }

    public void execute() throws Exception {
        VLANCSV csv = null;

        try (Connection conn = getConnection();
                InputStream in = getFileInputStream();) {
            csv = new VLANCSV(in);
            Map<String, String> line = csv.readNext();
            while (line != null) {
                String vcenter = line.get(VLANCSV.COL_VCENTER);
                String datacenter = line.get(VLANCSV.COL_DATACENTER);
                String cluster = line.get(VLANCSV.COL_CLUSTER);
                String vlan = line.get(VLANCSV.COL_NAME);
                String gateway = line.get(VLANCSV.COL_GATEWAY);
                String subnetMask = line.get(VLANCSV.COL_SUBNET_MASK);
                String dnsServer = line.get(VLANCSV.COL_DNSSERVER);
                String dnsSuffix = line.get(VLANCSV.COL_DNSSUFFIX);
                String flag = line.get(VLANCSV.COL_ENABLED);
                boolean enabled = "true".equalsIgnoreCase(flag) ? true : false;

                try {
                    int clusterTkey = getClusterTkey(vcenter, datacenter,
                            cluster);
                    addTableRow(conn, vlan, gateway, subnetMask, dnsServer,
                            dnsSuffix, enabled, clusterTkey);
                } catch (Exception e) {
                    logger.error("failed to add row: " + vcenter + " "
                            + datacenter + " " + cluster + " " + vlan + " "
                            + dnsServer + " " + dnsSuffix);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
                try {
                    line = csv.readNext();
                } catch (Exception e) {
                    logger.error("Failed to read line from CSV file after row: "
                            + vcenter + " " + datacenter + " " + cluster + " "
                            + vlan + " " + dnsServer + " " + dnsSuffix);
                    logger.error(e.getMessage());
                    conn.rollback();
                    throw e;
                }
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("Failed to import VLANs.", e);
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

    private void addTableRow(Connection con, String vlan, String gateway,
            String subnetMask, String dnsServer, String dnsSuffix,
            boolean enabled, int clusterTkey) throws Exception {
        String query = "insert into vlan (TKEY, NAME, GATEWAY, SUBNET_MASK, DNSSERVER, DNSSUFFIX, ENABLED, CLUSTER_TKEY) values (DEFAULT,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, vlan);
            stmt.setString(2, gateway);
            stmt.setString(3, subnetMask);
            stmt.setString(4, dnsServer);
            stmt.setString(5, dnsSuffix);
            stmt.setBoolean(6, enabled);
            stmt.setInt(7, clusterTkey);
            stmt.execute();
        }
    }
}
