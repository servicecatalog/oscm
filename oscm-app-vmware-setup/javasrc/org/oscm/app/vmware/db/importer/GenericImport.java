/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.db.importer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericImport {

    private static final Logger logger = LoggerFactory
            .getLogger(GenericImport.class);

    private String driverURL;
    private String userName;
    private String userPwd;
    private String csvFile;

    public GenericImport(String driverClass, String driverURL, String userName,
            String userPwd, String csvFile) {

        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "DriverClass '" + driverClass + "' could not be found");
        }

        this.driverURL = driverURL;
        this.userName = userName;
        this.userPwd = userPwd;
        this.csvFile = csvFile;
    }

    protected Connection getConnection() throws Exception {
        try {
            Connection conn = DriverManager.getConnection(driverURL, userName,
                    userPwd);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to create database connection.", e);
            throw new RuntimeException("Could not connect to the database");
        }
    }

    protected InputStream getFileInputStream() throws Exception {
        try {
            return new FileInputStream(csvFile);
        } catch (FileNotFoundException e) {
            logger.error("Failed to create file input stream.", e);
            throw new RuntimeException(
                    "Failed to create file input stream '" + csvFile + "'.");
        }
    }

    protected int getVlanTkey(String vcenter, String datacenter, String cluster,
            String vlan) throws Exception {
        String query = "select tkey from vlan where name = ? and cluster_tkey = (select tkey from cluster where name = ? and datacenter_tkey = (select tkey from datacenter where name = ? and vcenter_tkey = (select tkey from vcenter where name = ?)))";
        int tkey = -1;
        try (Connection con = getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, vlan);
            stmt.setString(2, cluster);
            stmt.setString(3, datacenter);
            stmt.setString(4, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tkey = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve tkey for VLAN " + vlan
                    + " datacenter: " + datacenter + " vcenter: " + vcenter
                    + " cluster: " + cluster, e);
            throw e;
        }

        if (tkey == -1) {
            throw new Exception("Failed to retrieve tkey for VLAN " + vlan
                    + " datacenter: " + datacenter + " vcenter: " + vcenter
                    + " cluster: " + cluster);
        }

        return tkey;
    }

    protected int getClusterTkey(String vcenter, String datacenter,
            String cluster) throws Exception {
        String query = "select tkey from cluster where name = ? and datacenter_tkey = (select tkey from datacenter where name = ? and vcenter_tkey = (select tkey from vcenter where name = ?))";
        int tkey = -1;
        try (Connection con = getConnection();
                PreparedStatement stmt = con.prepareStatement(query);) {
            stmt.setString(1, cluster);
            stmt.setString(2, datacenter);
            stmt.setString(3, vcenter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tkey = rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Failed to retrieve tkey for cluster " + cluster
                    + " datacenter: " + datacenter + " vcenter: " + vcenter, e);
            throw e;
        }

        if (tkey == -1) {
            throw new Exception("Failed to retrieve tkey for cluster " + cluster
                    + " datacenter: " + datacenter + " vcenter: " + vcenter);
        }

        return tkey;
    }

}
