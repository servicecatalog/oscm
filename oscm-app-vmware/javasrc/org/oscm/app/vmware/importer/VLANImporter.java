/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.VLANParser;
import org.oscm.app.vmware.parser.model.VLAN;
import org.oscm.app.vmware.persistence.DataAccessService;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VLANImporter implements Importer {
    private final DataAccessService das;

    private void save(VLAN vlan) throws Exception {
        String query = "insert into vlan (TKEY, NAME, GATEWAY, SUBNET_MASK, DNSSERVER, DNSSUFFIX, ENABLED, CLUSTER_TKEY) values (DEFAULT,?,?,?,?,?,?,?)";
        int clusterKey = this.getClusterKey(vlan.cluster, vlan.datacenter, vlan.vCenter);
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, vlan.name);
            stmt.setString(2, vlan.gateway);
            stmt.setString(3, vlan.subnetMask);
            stmt.setString(4, vlan.dnsServer);
            stmt.setString(5, vlan.dnsSuffix);
            stmt.setBoolean(6, Boolean.getBoolean(vlan.enabled));
            stmt.setInt(7, clusterKey);
            stmt.execute();
        }
    }

    private int getClusterKey(String cluster, String datacenter, String vCenter) throws Exception {
        String query = "select tkey from cluster where name = ? and datacenter_tkey = (select tkey from datacenter where name = ? and vcenter_tkey = (select tkey from vcenter where name = ?))";
        try(PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, cluster);
            stmt.setString(2, datacenter);
            stmt.setString(3, vCenter);
            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) {
                throw new Exception("Cluster " + cluster + " not found");
            }

            return rs.getInt(1);
        }
    }

    VLANImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try(VLANParser parser = new VLANParser(csvFile)) {
            VLAN vlan;
            while((vlan = parser.readNextObject()) != null) {
                this.save(vlan);
            }
        }
    }
}
