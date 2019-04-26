package org.oscm.app.vmware.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.oscm.app.vmware.parser.model.Portgroup;

public class PortgroupParser extends CSVParser<Portgroup>{
    enum Columns {
        VCENTER("VCenter"),
        DATACENTER("Datacenter"),
        CLUSTER("Cluster"),
        DISTRIBUTEDVIRTUALSWITCH("DistributedVirtualSwitch"),
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

    public PortgroupParser(InputStream stream) throws Exception {
        super(stream);
    }

    @Override
    public List<String> getRequiredColumns() {
        return Arrays.stream(Columns.values())
                .map(Columns::toString)
                .collect(Collectors.toList());
    }

    @Override
    public Portgroup readNextObject() throws Exception {
        Portgroup result = new Portgroup();
        Map<String, String> entries = this.readNext();

        if(entries == null) {
            return null;
        }
        
        result.distributedVirtualSwitch = entries.get(Columns.DISTRIBUTEDVIRTUALSWITCH.toString());
        result.uuid = entries.get(Columns.UUID.toString());
        result.name = entries.get(Columns.NAME.toString());
        result.vCenter = entries.get(Columns.VCENTER.toString());
        result.datacenter = entries.get(Columns.DATACENTER.toString());
        result.cluster = entries.get(Columns.CLUSTER.toString());
        
        return result;
    }
}
