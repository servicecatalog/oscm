/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.parser;

import org.oscm.app.vmware.parser.model.VLAN;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VLANParser extends CSVParser<VLAN> {
    enum Columns {
        VCENTER("VCenter"),
        DATACENTER("Datacenter"),
        CLUSTER("Cluster"),
        NAME("Name"),
        GATEWAY("Gateway"),
        SUBNET_MASK("SubnetMask"),
        DNS_SERVER("DnsServer"),
        DNS_SUFFIX("DnsSuffix"),
        ENABLED("Enabled");

        private final String text;

        Columns(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    public VLANParser(InputStream stream) throws Exception {
        super(stream);
    }

    @Override
    public List<String> getRequiredColumns() {
        return Arrays.stream(Columns.values())
                .map(Columns::toString)
                .collect(Collectors.toList());
    }

    @Override
    public VLAN readNextObject() throws Exception {
        VLAN result = new VLAN();
        Map<String, String> entries = this.readNext();

        if(entries == null) {
            return null;
        }

        result.vCenter = entries.get(Columns.VCENTER.toString());
        result.datacenter = entries.get(Columns.DATACENTER.toString());
        result.cluster = entries.get(Columns.CLUSTER.toString());
        result.name = entries.get(Columns.NAME.toString());
        result.gateway = entries.get(Columns.GATEWAY.toString());
        result.subnetMask = entries.get(Columns.SUBNET_MASK.toString());
        result.dnsServer = entries.get(Columns.DNS_SERVER.toString());
        result.dnsSuffix = entries.get(Columns.DNS_SUFFIX.toString());
        result.enabled = entries.get(Columns.ENABLED.toString());

        return result;
    }
}
