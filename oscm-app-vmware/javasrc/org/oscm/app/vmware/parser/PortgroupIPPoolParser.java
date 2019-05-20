/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 26-04-2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.vmware.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.oscm.app.vmware.parser.model.PortgroupIPPool;

public class PortgroupIPPoolParser extends CSVParser<PortgroupIPPool> {
  enum Columns {
    VCENTER("VCenter"),
    DATACENTER("Datacenter"),
    CLUSTER("Cluster"),
    DISTRIBUTEDVIRTUALSWITCH("DistributedVirtualSwitch"),
    PORTGROUP("Portgroup"),
    IP_ADDRESS("IPAddress");
    ;

    private final String text;

    Columns(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return this.text;
    }
  }

  public PortgroupIPPoolParser(InputStream stream) throws Exception {
    super(stream);
  }

  @Override
  public List<String> getRequiredColumns() {
    return Arrays.stream(Columns.values()).map(Columns::toString).collect(Collectors.toList());
  }

  @Override
  public PortgroupIPPool readNextObject() throws Exception {
    PortgroupIPPool result = new PortgroupIPPool();
    Map<String, String> entries = this.readNext();

    if (entries == null) {
      return null;
    }

    result.vCenter = entries.get(Columns.VCENTER.toString());
    result.datacenter = entries.get(Columns.DATACENTER.toString());
    result.cluster = entries.get(Columns.CLUSTER.toString());
    result.portgroup = entries.get(Columns.PORTGROUP.toString());
    result.distributedVirtualSwitch = entries.get(Columns.DISTRIBUTEDVIRTUALSWITCH.toString());
    result.ipAddress = entries.get(Columns.IP_ADDRESS.toString());

    return result;
  }
}
