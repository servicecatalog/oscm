/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.persistence;

import org.oscm.app.vmware.business.model.Cluster;
import org.oscm.app.vmware.business.model.VCenter;
import org.oscm.app.vmware.business.model.VLAN;
import org.oscm.app.vmware.encryption.AESEncrypter;
import org.oscm.app.vmware.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Provides data access to the vmware controller database.
 */
public class DataAccessService {

    private static final Logger logger = LoggerFactory
            .getLogger(DataAccessService.class);

    /**
     * The value must match a datasource definition in the Glassfish domain
     * where the controller is installed. It points to a controller specific
     * database with information about vCenter API access, datacenters and
     * clusters.
     */
    private static final String DATASOURCE = "BSSAppVMwareDS";

    private DataSource ds = null;

    private String locale = "en";

    public DataAccessService(String locale) {
        this.locale = locale;
    }

    public String getNextSequenceNumber(int numDigits, String vcenterId)
            throws Exception {
        logger.debug("vcenterId: " + vcenterId);

        if (vcenterId == null) {
            throw new IllegalArgumentException(
                    "DataAccessService.getNextSequenceNumber() vCenter cannot be null.");
        }

        String seqName = "vcenter_" + vcenterId.trim() + "_seq";
        String seq = null;
        String query = "SELECT nextval('" + seqName + "')";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Integer seqnum = new Integer(rs.getInt("nextval"));
                seq = String.format("%0" + numDigits + "d", seqnum);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve value from sequence " + seqName,
                    e);
            String message = Messages.get(locale, "error_db_seq", seqName);
            throw new Exception(message);
        }

        return seq;
    }

    public String getVCenterIdentifier(String vcenter) throws Exception {
        logger.debug("vcenter: " + vcenter);
        String identifier = null;
        String query = "SELECT identifier FROM vcenter WHERE name = ?";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, vcenter);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                identifier = rs.getString("identifier");
            }
        } catch (SQLException e) {
            logger.error(
                    "Failed to retrieve identifier from vcenter " + vcenter, e);
            String message = Messages.get(locale, "error_db_vcenter", vcenter);
            throw new Exception(message);
        }

        if (identifier == null) {
            logger.error(
                    "Failed to retrieve identifier from vcenter " + vcenter);
            String message = Messages.get(locale, "error_db_vcenter", vcenter);
            throw new Exception(message);
        }
        return identifier;
    }

    /**
     * Save vCenter API settings and load balancer configuration.
     */
    public void setVCenter(VCenter vcenter) throws Exception {
        logger.debug("vcenter: " + vcenter.name);
        try (Connection con = getDatasource().getConnection()) {
            String query1 = "UPDATE vcenter SET url = ?, userid = ?, password = ? WHERE tkey = ?";
            try (PreparedStatement stmt = con.prepareStatement(query1)) {
                stmt.setString(1, vcenter.getUrl());
                stmt.setString(2, vcenter.getUserid());
                stmt.setString(3,
                        AESEncrypter.encrypt(vcenter.getPassword()));
                stmt.setInt(4, vcenter.tkey);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Failed to save controller configuration", e);
            throw new Exception(Messages.get(locale, "error_db_save_conf"));
        }
    }

    public List<VCenter> getVCenter() throws Exception {
        logger.debug("");
        List<VCenter> vcenter = new ArrayList<>();
        String query = "SELECT tkey,name,identifier,url,userid,password FROM vcenter";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VCenter vc = new VCenter();
                vc.name = rs.getString("name");
                vc.identifier = rs.getString("identifier");
                vc.setUrl(rs.getString("url"));
                vc.setUserid(rs.getString("userid"));
                vc.setPassword(AESEncrypter.decrypt(rs.getString("password")));
                vc.tkey = rs.getInt("tkey");
                vcenter.add(vc);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve vCenter server list", e);
            throw e;
        }

        return vcenter;
    }

    public VMwareCredentials getCredentials(String vcenter) throws Exception {
        logger.debug("vcenter=" + vcenter);
        String query = "SELECT url,userid,password FROM vcenter WHERE name = ?";
        VMwareCredentials credentials = new VMwareCredentials(null, null, null);
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                credentials = new VMwareCredentials(
                        rs.getString("url"),
                        rs.getString("userid"),
                        AESEncrypter.decrypt(rs.getString("password")));
            }
        } catch (SQLException e) {
            logger.error(
                    "Failed to retrieve credentials from vcenter: " + vcenter,
                    e);
            throw e;
        }

        if (credentials.getURL() == null
                || credentials.getURL().length() == 0) {
            throw new Exception("No URL defined for vcenter: " + vcenter);
        }

        if (credentials.getUserId() == null
                || credentials.getUserId().length() == 0) {
            throw new Exception("No userid defined for vcenter: " + vcenter);
        }

        if (credentials.getPassword() == null
                || credentials.getPassword().length() == 0) {
            throw new Exception("No password defined for vcenter: " + vcenter);
        }

        return credentials;
    }

    public String getDatacenterId(String vcenter, String datacenter)
            throws Exception {
        logger.debug("vcenter=" + vcenter + " datacenter=" + datacenter);
        String query = "select identifier from datacenter where name = ? and vcenter_tkey=(select tkey from vcenter where name = ?)";
        String datacenterId = null;
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, datacenter);
            stmt.setString(2, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                datacenterId = rs.getString("identifier");
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve datacenter id from vcenter: "
                    + vcenter + " datacenter: " + datacenter, e);
            throw e;
        }

        if (datacenterId == null) {
            logger.error("No datacenter id defined for vcenter: " + vcenter
                    + " datacenter: " + datacenter);
        }

        return datacenterId;
    }

    public String getHostLoadBalancerConfig(String vcenter, String datacenter,
            String cluster) {
        logger.debug("vcenter: " + vcenter + " datacenter: " + datacenter
                + " cluster: " + cluster);
        String query = "SELECT load_balancer FROM cluster WHERE name = ? AND datacenter_tkey = (SELECT tkey FROM datacenter WHERE name = ? and vcenter_tkey = (SELECT TKEY FROM vcenter WHERE NAME = ?))";
        String xml = "";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, cluster);
            stmt.setString(2, datacenter);
            stmt.setString(3, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                xml = rs.getString("load_balancer");
            }
        } catch (Exception e) {
            logger.error(
                    "Failed to retrieve load balancer configuration for cluster "
                            + cluster,
                    e);
        }

        return xml;

    }

    public List<VLAN> getVLANs(Cluster cluster) throws Exception {
        List<VLAN> vlans = new ArrayList<>();
        logger.debug(
                "cluster: " + cluster.name + " cluster_tkey: " + cluster.tkey
                        + " datacenter_tkey: " + cluster.datacenter_tkey);

        String query = "SELECT tkey,name,enabled FROM vlan WHERE cluster_tkey = ?";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setInt(1, cluster.tkey);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VLAN vlan = new VLAN();
                vlan.setName(rs.getString("name"));
                vlan.setEnabled(rs.getBoolean("enabled"));
                vlan.setTKey(rs.getInt("tkey"));
                vlan.setClusterTKey(cluster.tkey);
                vlans.add(vlan);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve VLANs for cluster " + cluster.name,
                    e);
            throw e;
        }

        return vlans;
    }

    public int addVLAN(VLAN vlan) throws Exception {
        logger.debug("name: " + vlan.getName());
        String query = "INSERT INTO VLAN VALUES (DEFAULT,?,?,?)";
        String query2 = "SELECT CURRVAL('vlan_tkey_seq') AS TKEY";
        int tkey = -1;

        try (Connection con = getDatasource().getConnection();) {
            try (PreparedStatement stmt = con.prepareStatement(query);) {
                stmt.setString(1, vlan.getName());
                stmt.setBoolean(2, vlan.isEnabled());
                stmt.setInt(3, vlan.getClusterTKey());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = con.prepareStatement(query2);) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    tkey = rs.getInt("TKEY");
                    logger.debug("added VLAN " + vlan.getName() + " with tkey "
                            + tkey);
                }
            }
        }

        return tkey;
    }

    public void updateVLANs(List<VLAN> vlans) throws Exception {
        String query = "UPDATE VLAN SET ENABLED = ?, NAME = ? WHERE TKEY = ?";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            for (VLAN vlan : vlans) {
                logger.debug(
                        "name: " + vlan.getName() + " tkey: " + vlan.getTKey());
                stmt.setBoolean(1, vlan.isEnabled());
                stmt.setString(2, vlan.getName());
                stmt.setInt(3, vlan.getTKey());
                stmt.addBatch();
            }
            int[] affectedRecords = stmt.executeBatch();
            logger.debug("number of records updated: " + affectedRecords);
        }
    }

    public void deleteVLAN(VLAN vlan) throws Exception {
        logger.debug("name: " + vlan.getName() + " tkey: " + vlan.getTKey());
        String query = "DELETE FROM VLAN WHERE TKEY = ?";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setInt(1, vlan.getTKey());
            int affectedRows = stmt.executeUpdate();
            logger.debug("number of records deleted: " + affectedRows);
        }
    }

    public VMwareNetwork getNetworkSettings(String vcenter, String datacenter,
            String cluster, String vlan) throws Exception {
        VMwareNetwork network = new VMwareNetwork();
        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster + "  vlan: " + vlan);
        String query = "SELECT SUBNET_MASK,GATEWAY,DNSSERVER,DNSSUFFIX FROM VLAN WHERE NAME = ? AND CLUSTER_TKEY = ?";
        try (Connection con = getDatasource().getConnection();) {
            int cluster_tkey = getClusterTKey(con, vcenter, datacenter,
                    cluster);
            if (cluster_tkey == -1) {
                throw new SQLException(
                        "Failed to retrieve network settings. Unknown cluster "
                                + cluster);
            }

            boolean foundVLAN = false;
            try (PreparedStatement stmt = con.prepareStatement(query);) {
                stmt.setString(1, vlan);
                stmt.setInt(2, cluster_tkey);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    network.setSubnetMask(rs.getString("SUBNET_MASK"));
                    network.setGateway(rs.getString("GATEWAY"));
                    network.setDnsServer(rs.getString("DNSSERVER"));
                    network.setDnsSuffix(rs.getString("DNSSUFFIX"));
                    foundVLAN = true;
                }
            }
            if (!foundVLAN) {
                throw new SQLException(
                        "Failed to retrieve network settings. Unknown vlan "
                                + vlan);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve network settings for VLAN " + vlan,
                    e);
            throw e;
        }

        return network;
    }

    public String getVLANwithMostIPs(String vcenter, String datacenter,
            String cluster) {

        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster);

        StringBuffer vlans = new StringBuffer();
        String query = "SELECT tkey FROM vlan WHERE cluster_tkey = (SELECT tkey from cluster where name = ? and datacenter_tkey = (SELECT tkey FROM datacenter WHERE name = ? and vcenter_tkey = (SELECT TKEY FROM vcenter WHERE NAME = ?)))";
        try (Connection con = getDatasource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, cluster);
            stmt.setString(2, datacenter);
            stmt.setString(3, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (vlans.length() > 0) {
                    vlans.append(",");
                }
                vlans.append(rs.getInt("tkey"));
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve vlans for vcenter: " + vcenter
                    + "  datacenter: " + datacenter + "  cluster: " + cluster,
                    e);
            return null;
        }

        if (vlans.length() == 0) {
            logger.error("No vlans found for vcenter: " + vcenter
                    + "  datacenter: " + datacenter + "  cluster: " + cluster);
            return null;
        }

        query = "SELECT name,COUNT(ip_address) AS NumIPs FROM vlan INNER JOIN ippool ON vlan.tkey in ("
                + vlans.toString()
                + ") and vlan.tkey=ippool.vlan_tkey and in_use = false GROUP BY name ORDER BY NumIPs DESC LIMIT 1";
        String vlan = null;
        int numFreeIPs = 0;

        try (Connection con = getDatasource().getConnection();) {
            try (PreparedStatement stmt = con.prepareStatement(query);) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    vlan = rs.getString("name");
                    numFreeIPs = rs.getInt("NumIPs");
                }
            }

            logger.debug("retrieved vlan " + vlan
                    + " with number of free ip addresses: " + numFreeIPs);
            if (numFreeIPs == 0) {
                logger.error(
                        "Failed to retrieve vlan. No free IP address available in VLAN "
                                + vlan);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve vlans for vcenter: " + vcenter
                    + "  datacenter: " + datacenter + "  cluster: " + cluster,
                    e);
            return null;
        }

        logger.debug("retrieved vlan: " + vlan);
        return vlan;
    }

    /**
     * Retrieves an IP address that is associated to the given cluster. The IP
     * address is marked as reserved and will therefore not be available for new
     * VMs.
     */
    public String reserveIPAddress(String vcenter, String datacenter,
            String cluster, String vlan) throws Exception {

        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster);

        if (vcenter == null) {
            throw new Exception("vCenter not defined");
        }
        if (datacenter == null) {
            throw new Exception("datacenter not defined");
        }
        if (cluster == null) {
            throw new Exception("cluster not defined");
        }
        if (vlan == null) {
            throw new Exception("vlan not defined");
        }

        String ipaddress = null;
        String query2 = "SELECT IP_ADDRESS,VLAN_TKEY FROM IPPOOL WHERE VLAN_TKEY = (SELECT TKEY from VLAN WHERE NAME = ? AND CLUSTER_TKEY = ?) AND IN_USE = FALSE LIMIT 1";
        String query3 = "UPDATE IPPOOL SET IN_USE = TRUE WHERE IP_ADDRESS = ? AND VLAN_TKEY = ?";

        try (Connection con = getDatasource().getConnection();) {
            int cluster_tkey = getClusterTKey(con, vcenter, datacenter,
                    cluster);
            if (cluster_tkey == -1) {
                logger.error("Failed to reserve IP address. Unknown cluster "
                        + cluster);
                String message = Messages.get(locale,
                        "error_db_reserve_ip_unknown_cluster", cluster);
                throw new Exception(message);
            }

            logger.debug("retrieved tkey " + cluster_tkey + " for cluster "
                    + cluster);
            int vlanTKey = -1;
            try (PreparedStatement stmt = con.prepareStatement(query2);) {
                stmt.setString(1, vlan);
                stmt.setInt(2, cluster_tkey);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ipaddress = rs.getString("IP_ADDRESS");
                    vlanTKey = rs.getInt("VLAN_TKEY");
                }
            }
            logger.debug("retrieved vlan tkey " + vlanTKey + " and ip address: "
                    + ipaddress);
            if (vlanTKey == -1) {
                logger.error("Failed to reserve IP address for vcenter: "
                        + vcenter + "  datacenter: " + datacenter
                        + "  cluster: " + cluster + " vlan: " + vlan);
                String message = Messages.get(locale,
                        "error_db_reserve_ip_unknown_vlan", vlan);
                throw new Exception(message);
            }

            if (ipaddress == null) {
                logger.error(
                        "Failed to reserve IP address. No free IP address available in VLAN "
                                + vlan);
                String message = Messages.get(locale,
                        "error_db_no_free_ipaddress", vlan);
                throw new Exception(message);
            } else {
                try (PreparedStatement stmt = con.prepareStatement(query3);) {
                    stmt.setString(1, ipaddress);
                    stmt.setInt(2, vlanTKey);
                    stmt.executeUpdate();
                }
            }
        }

        logger.debug("reserved IP address: " + ipaddress + " in VLAN " + vlan);
        return ipaddress;
    }

    /**
     * The given IP address is marked as used in the VMware Controller database.
     * This functionality is used when VMs are imported.
     */
    public boolean markIPAddressAsUsed(String vcenter, String datacenter,
            String cluster, String vlan, String ipAddress) {
        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster + "  vlan: " + vlan + "  ipAddress: "
                + ipAddress);
        String query2 = "SELECT IN_USE FROM IPPOOL WHERE VLAN_TKEY = (SELECT TKEY from VLAN WHERE NAME = ? AND CLUSTER_TKEY = ?) AND IP_ADDRESS = ?";
        String query3 = "UPDATE IPPOOL SET IN_USE = TRUE WHERE IP_ADDRESS = ? AND VLAN_TKEY = (SELECT TKEY from VLAN WHERE NAME = ? AND CLUSTER_TKEY = ?)";

        if (vcenter == null) {
            logger.error(
                    "Failed to mark IP address as used. vCenter not defined.");
            return false;
        }
        if (datacenter == null) {
            logger.error(
                    "Failed to mark IP address as used. Datacenter not defined.");
            return false;
        }
        if (cluster == null) {
            logger.error(
                    "Failed to mark IP address as used. Cluster not defined.");
            return false;
        }
        if (vlan == null) {
            logger.error(
                    "Failed to mark IP address as used. VLAN not defined.");
            return false;
        }
        if (ipAddress == null) {
            logger.error(
                    "Failed to mark IP address as used. IP address not defined.");
            return false;
        }

        boolean success = true;

        try (Connection con = getDatasource().getConnection();) {
            int cluster_tkey = getClusterTKey(con, vcenter, datacenter,
                    cluster);
            if (cluster_tkey == -1) {
                throw new SQLException("Unknown cluster " + cluster);
            }

            logger.debug("retrieved tkey " + cluster_tkey + " for cluster "
                    + cluster);
            boolean inUse = false;
            boolean foundIPAddress = false;
            try (PreparedStatement stmt = con.prepareStatement(query2);) {
                stmt.setString(1, vlan);
                stmt.setInt(2, cluster_tkey);
                stmt.setString(3, ipAddress);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    inUse = rs.getBoolean("IN_USE");
                    foundIPAddress = true;
                }
            }
            if (!foundIPAddress) {
                throw new SQLException(
                        "IP address " + ipAddress + " is not registered.");
            }
            if (inUse) {
                throw new SQLException("IP address " + ipAddress
                        + " is already marked as used.");
            }

            try (PreparedStatement stmt = con.prepareStatement(query3);) {
                stmt.setString(1, ipAddress);
                stmt.setString(2, vlan);
                stmt.setInt(3, cluster_tkey);
                stmt.executeUpdate();
                logger.debug("IP address " + ipAddress
                        + " has been marked as used.");
            }
        } catch (Exception e) {
            logger.error("Failed to mark IP address as used.", e);
            success = false;
        }

        return success;
    }

    /**
     * Check if the given IP address is in use.
     */
    public boolean isIPAddressInUse(String vcenter, String datacenter,
            String cluster, String vlan, String ipAddress) throws Exception {
        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster + "  vlan: " + vlan + "  ipAddress: "
                + ipAddress);

        if (vcenter == null) {
            throw new Exception("vCenter not defined");
        }
        if (datacenter == null) {
            throw new Exception("datacenter not defined");
        }
        if (cluster == null) {
            throw new Exception("cluster not defined");
        }
        if (vlan == null) {
            throw new Exception("vlan not defined");
        }
        if (ipAddress == null) {
            throw new Exception("ipaddress not defined");
        }

        boolean inUse = true;
        String query1 = "SELECT TKEY from VLAN WHERE NAME = ? AND CLUSTER_TKEY = ?";
        String query2 = "SELECT IN_USE FROM IPPOOL WHERE IP_ADDRESS = ? AND VLAN_TKEY = ?";
        try (Connection con = getDatasource().getConnection();) {
            int cluster_tkey = getClusterTKey(con, vcenter, datacenter,
                    cluster);
            if (cluster_tkey == -1) {
                logger.error("Failed to validate IP address. Unknown cluster "
                        + cluster);
                String message = Messages.get(locale,
                        "error_db_validate_ip_unknown_cluster", cluster);
                throw new Exception(message);
            }
            logger.debug("retrieved tkey " + cluster_tkey + " for cluster "
                    + cluster);

            int vlan_tkey = -1;
            try (PreparedStatement stmt = con.prepareStatement(query1);) {
                stmt.setString(1, vlan);
                stmt.setInt(2, cluster_tkey);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    vlan_tkey = rs.getInt("TKEY");
                }
            }
            if (vlan_tkey == -1) {
                logger.error(
                        "Failed to validate IP address. Unknown VLAN " + vlan);
                String message = Messages.get(locale,
                        "error_db_validate_ip_unknown_vlan", vlan);
                throw new Exception(message);
            }
            logger.debug("retrieved tkey " + vlan_tkey + " for VLAN " + vlan);

            boolean foundEntry = false;
            try (PreparedStatement stmt = con.prepareStatement(query2);) {
                stmt.setString(1, ipAddress);
                stmt.setInt(2, vlan_tkey);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    inUse = rs.getBoolean("IN_USE");
                    foundEntry = true;
                }
            }
            if (!foundEntry) {
                logger.error("Failed to validate IP address " + ipAddress
                        + ". Not found in VLAN " + vlan);
                String message = Messages.get(locale,
                        "error_db_validate_ip_not_found",
                        new Object[] { ipAddress, vlan });
                throw new Exception(message);
            }
        }
        logger.debug(
                "In-Use-Status of IP address: " + ipAddress + " is " + inUse);

        return inUse;
    }

    /**
     * Check if the given IP address is present in the database.
     */
    public boolean isIPAddressPresent(String vcenter, String datacenter,
            String cluster, String vlan, String ipAddress) throws Exception {
        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster + "  vlan: " + vlan + "  ipAddress: "
                + ipAddress);

        if (vcenter == null) {
            throw new Exception("vCenter not defined");
        }
        if (datacenter == null) {
            throw new Exception("datacenter not defined");
        }
        if (cluster == null) {
            throw new Exception("cluster not defined");
        }
        if (vlan == null) {
            throw new Exception("vlan not defined");
        }
        if (ipAddress == null) {
            throw new Exception("ipaddress not defined");
        }

        boolean foundEntry = false;
        String query1 = "SELECT TKEY from VLAN WHERE NAME = ? AND CLUSTER_TKEY = ?";
        String query2 = "SELECT IN_USE FROM IPPOOL WHERE IP_ADDRESS = ? AND VLAN_TKEY = ?";
        try (Connection con = getDatasource().getConnection();) {
            int cluster_tkey = getClusterTKey(con, vcenter, datacenter,
                    cluster);
            if (cluster_tkey == -1) {
                logger.error("Failed to validate IP address. Unknown cluster "
                        + cluster);
                String message = Messages.get(locale,
                        "error_db_validate_ip_unknown_cluster", cluster);
                throw new Exception(message);
            }
            logger.debug("retrieved tkey " + cluster_tkey + " for cluster "
                    + cluster);

            int vlan_tkey = -1;
            try (PreparedStatement stmt = con.prepareStatement(query1);) {
                stmt.setString(1, vlan);
                stmt.setInt(2, cluster_tkey);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    vlan_tkey = rs.getInt("TKEY");
                }
            }
            if (vlan_tkey == -1) {
                logger.error(
                        "Failed to validate IP address. Unknown VLAN " + vlan);
                String message = Messages.get(locale,
                        "error_db_validate_ip_unknown_vlan", vlan);
                throw new Exception(message);
            }
            logger.debug("retrieved tkey " + vlan_tkey + " for VLAN " + vlan);

            try (PreparedStatement stmt = con.prepareStatement(query2);) {
                stmt.setString(1, ipAddress);
                stmt.setInt(2, vlan_tkey);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    foundEntry = true;
                }
            }
        }
        logger.debug("IP address: " + ipAddress + " is present: " + foundEntry);
        return foundEntry;
    }

    /**
     * Add the given IP address to the database and mark it as unused.
     */
    public void addIPAddress(String vcenter, String datacenter, String cluster,
            String vlan, String ipAddress) throws Exception {
        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster + "  vlan: " + vlan + "  ipaddress: "
                + ipAddress);

        if (vcenter == null) {
            throw new Exception("vCenter not defined");
        }
        if (datacenter == null) {
            throw new Exception("datacenter not defined");
        }
        if (cluster == null) {
            throw new Exception("cluster not defined");
        }
        if (vlan == null) {
            throw new Exception("vlan not defined");
        }
        if (ipAddress == null) {
            throw new Exception("ipaddress not defined");
        }

        String query = "INSERT INTO IPPOOL VALUES (DEFAULT,?,?,?)";

        try (Connection con = getDatasource().getConnection();) {
            int vlanTkey = getVLANTKey(con, vcenter, datacenter, cluster, vlan);

            if (vlanTkey == -1) {
                throw new SQLException(
                        "Failed to add IP address. Unknown VLAN " + vlan);
            }

            try (PreparedStatement stmt = con.prepareStatement(query);) {
                stmt.setString(1, ipAddress);
                stmt.setBoolean(2, false);
                stmt.setInt(3, vlanTkey);
                stmt.execute();
            }
        }
    }

    /**
     * Marks the given IP address as available for new VMs.
     */
    public void releaseIPAddress(String vcenter, String datacenter,
            String cluster, String vlan, String ipaddress) throws Exception {

        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster + "  vlan: " + vlan + "  ipaddress: "
                + ipaddress);

        if (vcenter == null) {
            throw new Exception("vCenter not defined");
        }
        if (datacenter == null) {
            throw new Exception("datacenter not defined");
        }
        if (cluster == null) {
            throw new Exception("cluster not defined");
        }
        if (vlan == null) {
            throw new Exception("vlan not defined");
        }
        if (ipaddress == null) {
            throw new Exception("ipaddress not defined");
        }

        String query = "UPDATE IPPOOL SET IN_USE = FALSE WHERE IP_ADDRESS = ? AND VLAN_TKEY = (SELECT TKEY FROM VLAN WHERE CLUSTER_TKEY = ? AND NAME = ?)";

        try (Connection con = getDatasource().getConnection();) {
            int cluster_tkey = getClusterTKey(con, vcenter, datacenter,
                    cluster);

            if (cluster_tkey == -1) {
                throw new SQLException(
                        "Failed to release IP address. Unknown cluster "
                                + cluster);
            }

            try (PreparedStatement stmt = con.prepareStatement(query);) {
                stmt.setString(1, ipaddress);
                stmt.setInt(2, cluster_tkey);
                stmt.setString(3, vlan);
                stmt.execute();
            }
        }
    }

    private int getVLANTKey(Connection con, String vcenter, String datacenter,
            String cluster, String vlan) throws Exception {
        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster + "  vlan: " + vlan);
        int vlan_tkey = -1;
        String query = "SELECT tkey FROM vlan WHERE name = ? AND cluster_tkey = (SELECT tkey from cluster WHERE name = ? AND datacenter_tkey = (SELECT tkey from datacenter WHERE name = ? AND vcenter_tkey = (SELECT tkey FROM vcenter WHERE name = ?)))";

        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, vlan);
            stmt.setString(2, cluster);
            stmt.setString(3, datacenter);
            stmt.setString(4, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vlan_tkey = rs.getInt("tkey");
            }
        }
        return vlan_tkey;
    }

    private int getClusterTKey(Connection con, String vcenter,
            String datacenter, String cluster) throws Exception {
        logger.debug("vcenter: " + vcenter + "  datacenter: " + datacenter
                + "  cluster: " + cluster);
        int cluster_tkey = -1;
        String query = "SELECT tkey FROM cluster WHERE name = ? AND datacenter_tkey = (SELECT tkey from datacenter WHERE name = ? AND vcenter_tkey = (SELECT tkey FROM vcenter WHERE name = ?))";

        try (PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, cluster);
            stmt.setString(2, datacenter);
            stmt.setString(3, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cluster_tkey = rs.getInt("tkey");
            }
        }
        return cluster_tkey;
    }

    public DataSource getDatasource() throws Exception {
        if (ds == null) {
            try {
                Properties p = new Properties();
                p.put(Context.INITIAL_CONTEXT_FACTORY,"org.apache.openejb.core.OpenEJBInitialContextFactory");
                Context context = new InitialContext(p);
                ds = (DataSource) context.lookup(DATASOURCE);
            } catch (Exception e) {
                throw new Exception("Datasource " + DATASOURCE + " not found.",
                        e);
            }
        }

        return ds;
    }

}
