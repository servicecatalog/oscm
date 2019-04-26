/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 26-04-2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.vmware.importer;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.oscm.app.vmware.parser.DistributedVirtualSwitchParser;
import org.oscm.app.vmware.parser.model.DistributedVirtualSwitch;
import org.oscm.app.vmware.persistence.DataAccessService;

public class DistributedVirtualSwitchImporter implements Importer {

  private final DataAccessService das;

  private void save(DistributedVirtualSwitch dvs) throws Exception {
    String query =
        "INSERT INTO distributedvirtualswitch (tkey, uuid, name, cluster_tkey) VALUES (DEFAULT,?,?,?)";
    int clusterKey = this.getClusterKey(dvs.cluster, dvs.datacenter, dvs.vCenter);
    try (PreparedStatement stmt =
        this.das.getDatasource().getConnection().prepareStatement(query)) {
      stmt.setString(1, dvs.uuid);
      stmt.setString(2, dvs.name);
      stmt.setInt(3, clusterKey);
      stmt.execute();
    }
  }

  private int getClusterKey(String cluster, String datacenter, String vCenter) throws Exception {
    String query =
        ""
            + "select tkey from cluster where name = ? and datacenter_tkey = "
            + "(select tkey from datacenter where name = ? and vcenter_tkey = "
            + "(select tkey from vcenter where name = ?))";
    try (PreparedStatement stmt =
        this.das.getDatasource().getConnection().prepareStatement(query)) {
      stmt.setString(1, cluster);
      stmt.setString(2, datacenter);
      stmt.setString(3, vCenter);
      ResultSet rs = stmt.executeQuery();

      if (!rs.next()) {
        throw new Exception("Cluster " + cluster + " not found");
      }

      return rs.getInt(1);
    }
  }

  DistributedVirtualSwitchImporter(DataAccessService das) {
    this.das = das;
  }

  @Override
  public void load(InputStream csvFile) throws Exception {
    try (DistributedVirtualSwitchParser parser = new DistributedVirtualSwitchParser(csvFile)) {
      DistributedVirtualSwitch dvs;
      while ((dvs = parser.readNextObject()) != null) {
        this.save(dvs);
      }
    }
  }
}
