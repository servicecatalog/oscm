/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.business.balancer.XMLHelper;
import org.oscm.app.vmware.parser.ClusterParser;
import org.oscm.app.vmware.parser.model.Cluster;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.xml.sax.SAXParseException;


import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClusterImporter implements Importer {
    private final DataAccessService das;

    private void save(Cluster cluster) throws Exception {
        int datacenterKey = this.getDatacenterKey(cluster.vCenter, cluster.datacenter);
        
        String query = "INSERT INTO cluster (TKEY, NAME, LOAD_BALANCER, DATACENTER_TKEY) VALUES (DEFAULT, ?, ?, ?)";

        try(PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            XMLHelper.convertToDocument(cluster.loadBalancer);
            stmt.setString(1, cluster.clusterName);
            stmt.setString(2, cluster.loadBalancer);
            stmt.setInt(3, datacenterKey);
            stmt.execute();
        } catch (SAXParseException saxe) {
            throw new Exception("Failed to import cluster " + cluster.clusterName + ". " + saxe.getMessage());
        } catch (Exception e) {
            throw new Exception("Failed to import cluster " + cluster.clusterName, e);
        }
    }

    private int getDatacenterKey(String vCenter, String datacenter) throws Exception {
        String query = "SELECT tkey from datacenter WHERE name = ? and vcenter_tkey = (SELECT tkey FROM vcenter WHERE name = ?)";
        try(PreparedStatement stmt = this.das.getDatasource().getConnection().prepareStatement(query)) {
            stmt.setString(1, datacenter);
            stmt.setString(2, vCenter);
            ResultSet rs = stmt.executeQuery();

            if(!rs.next()) {
                throw new Exception("Datacenter " + datacenter + " not found");
            }
            return rs.getInt(1);
        }
    }

    ClusterImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try(ClusterParser parser = new ClusterParser(csvFile)) {
            Cluster cluster;
            while((cluster = parser.readNextObject()) != null) {
                this.save(cluster);
            }
        }
    }
}
