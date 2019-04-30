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
import org.oscm.app.vmware.parser.PortgroupIPPoolParser;
import org.oscm.app.vmware.parser.model.PortgroupIPPool;
import org.oscm.app.vmware.persistence.DataAccessService;

public class PortgroupIPPoolImporter implements Importer {

  private final DataAccessService das;

  private void save(PortgroupIPPool ipPool) throws Exception {
    String query =
        "INSERT INTO portgroup_ippool (tkey, ip_address, in_use, portgroup_fk) VALUES (DEFAULT,?,?,?)";
    int portgroupKey =
        this.getPortgroupTKey(
            ipPool.vCenter,
            ipPool.datacenter,
            ipPool.cluster,
            ipPool.portgroup,
            ipPool.distributedVirtualSwitch);
    try (PreparedStatement stmt =
        this.das.getDatasource().getConnection().prepareStatement(query)) {
      stmt.setString(1, ipPool.ipAddress);
      stmt.setBoolean(2, false);
      stmt.setInt(3, portgroupKey);
      stmt.execute();
    }
  }

  private int getPortgroupTKey(
      String vCenter, String datacenter, String cluster, String portgroup, String dvs)
      throws Exception {
    String query =
        ""
            + "select tkey from portgroup where name = ? and distributedvirtualswitch_cluster_fk = "
            + "(select tkey from distributedvirtualswitch where name = ? and cluster_tkey = "
            + "(select tkey from cluster where name = ? and datacenter_tkey = "
            + "(select tkey from datacenter where name = ? and vcenter_tkey = "
            + "(select tkey from vcenter where name = ?))))";
    try (PreparedStatement stmt =
        this.das.getDatasource().getConnection().prepareStatement(query)) {
      stmt.setString(1, portgroup);
      stmt.setString(2, dvs);
      stmt.setString(3, cluster);
      stmt.setString(4, datacenter);
      stmt.setString(5, vCenter);
      ResultSet rs = stmt.executeQuery();

      if (!rs.next()) {
        throw new Exception("Portgroup " + portgroup + " not found");
      }

      return rs.getInt(1);
    }
  }

  PortgroupIPPoolImporter(DataAccessService das) {
    this.das = das;
  }

  @Override
  public void load(InputStream csvFile) throws Exception {
    try (PortgroupIPPoolParser parser = new PortgroupIPPoolParser(csvFile)) {
      PortgroupIPPool ipPool;
      while ((ipPool = parser.readNextObject()) != null) {
        this.save(ipPool);
      }
    }
  }
}
