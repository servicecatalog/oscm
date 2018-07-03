/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.IPPoolParser;
import org.oscm.app.vmware.parser.model.IPPool;
import org.oscm.app.vmware.persistence.DataAccessService;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IPPoolImporter implements Importer {
    private final DataAccessService das;

    private void save(IPPool ipPool) throws Exception {
        String query = "INSERT INTO ippool (tkey, ip_address, in_use, vlan_tkey) VALUES (DEFAULT,?,?,?)";
        int vlanKey = this.getVLANTKey(ipPool.vCenter, ipPool.datacenter, ipPool.cluster, ipPool.vlan);
        try (PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, ipPool.ipAddress);
            stmt.setBoolean(2, false);
            stmt.setInt(3, vlanKey);
            stmt.execute();
        }
    }

    private int getVLANTKey(String vCenter, String datacenter, String cluster, String vlan) throws Exception {
        String query = "select tkey from vlan where name = ? and cluster_tkey = (select tkey from cluster where name = ? and datacenter_tkey = (select tkey from datacenter where name = ? and vcenter_tkey = (select tkey from vcenter where name = ?)))";
        try(PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, vlan);
            stmt.setString(2, cluster);
            stmt.setString(3, datacenter);
            stmt.setString(4, vCenter);
            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) {
                throw new Exception("VLAN " + vlan + " not found");
            }

            return rs.getInt(1);
        }
    }

    IPPoolImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try(IPPoolParser parser = new IPPoolParser(csvFile)) {
            IPPool ipPool;
            while((ipPool = parser.readNextObject()) != null) {
                this.save(ipPool);
            }
        }
    }
}
