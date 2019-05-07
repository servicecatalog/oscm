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
import org.oscm.app.vmware.parser.PortgroupParser;
import org.oscm.app.vmware.parser.model.Portgroup;
import org.oscm.app.vmware.persistence.DataAccessService;

public class PortgroupImporter implements Importer {

  private final DataAccessService das;

  PortgroupImporter(DataAccessService das) {
      this.das = das;
  }
  
  private void save(Portgroup portgroup) throws Exception {
    String query =
        "INSERT INTO portgroup (tkey, uuid, name, distributedvirtualswitch_cluster_fk) VALUES (DEFAULT,?,?,?)";
    int dvsKey =
        this.getDVSKey(
            portgroup.vCenter,
            portgroup.datacenter,
            portgroup.cluster,
            portgroup.distributedVirtualSwitch);
    try (PreparedStatement stmt =
        this.das.getDatasource().getConnection().prepareStatement(query)) {
      stmt.setString(1, portgroup.uuid);
      stmt.setString(2, portgroup.name);
      stmt.setInt(3, dvsKey);
      stmt.execute();
    }
  }

  private int getDVSKey(
      String vCenter, String datacenter, String cluster, String distributedvirtualswitch)
      throws Exception {
    String query =
        ""
            + "select tkey from distributedvirtualswitch where name = ? and cluster_tkey = "
            + "(select tkey from cluster where name = ? and datacenter_tkey = "
            + "(select tkey from datacenter where name = ? and vcenter_tkey = "
            + "(select tkey from vcenter where name = ?)))";
    try (PreparedStatement stmt =
        this.das.getDatasource().getConnection().prepareStatement(query)) {
      stmt.setString(1, distributedvirtualswitch);
      stmt.setString(2, cluster);
      stmt.setString(3, datacenter);
      stmt.setString(4, vCenter);
      ResultSet rs = stmt.executeQuery();

      if (!rs.next()) {
        throw new Exception("distributedvirtualswitch " + distributedvirtualswitch + " not found");
      }

      return rs.getInt(1);
    }
  }


  @Override
  public void load(InputStream csvFile) throws Exception {
    try (PortgroupParser parser = new PortgroupParser(csvFile)) {
      Portgroup portgroup;
      while ((portgroup = parser.readNextObject()) != null) {
        this.save(portgroup);
      }
    }
  }
}
