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
import org.oscm.app.vmware.parser.model.DistributedVirtualSwitch;

public class DistributedVirtualSwitchParser extends CSVParser<DistributedVirtualSwitch> {

  enum Columns {
    VCENTER("VCenter"),
    DATACENTER("Datacenter"),
    CLUSTER("Cluster"),
    UUID("Uuid"),
    NAME("Name");
    private final String text;

    Columns(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return this.text;
    }
  }

  public DistributedVirtualSwitchParser(InputStream stream) throws Exception {
    super(stream);
  }

  @Override
  public List<String> getRequiredColumns() {
    return Arrays.stream(Columns.values()).map(Columns::toString).collect(Collectors.toList());
  }

  @Override
  public DistributedVirtualSwitch readNextObject() throws Exception {
    DistributedVirtualSwitch result = new DistributedVirtualSwitch();
    Map<String, String> entries = this.readNext();

    if (entries == null) {
      return null;
    }
    result.cluster = entries.get(Columns.CLUSTER.toString());
    result.uuid = entries.get(Columns.UUID.toString());
    result.name = entries.get(Columns.NAME.toString());
    result.vCenter = entries.get(Columns.VCENTER.toString());
    result.datacenter = entries.get(Columns.DATACENTER.toString());
    result.cluster = entries.get(Columns.CLUSTER.toString());
    return result;
  }
}
