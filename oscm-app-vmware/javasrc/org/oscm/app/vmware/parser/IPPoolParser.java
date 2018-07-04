/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.parser;

import org.oscm.app.vmware.parser.model.IPPool;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IPPoolParser extends CSVParser<IPPool> {
    enum Columns {
        VCENTER("VCenter"),
        DATACENTER("Datacenter"),
        CLUSTER("Cluster"),
        VLAN("VLAN"),
        IP_ADDRESS("IPAddress");

        private final String text;

        Columns(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    public IPPoolParser(InputStream stream) throws Exception {
        super(stream);
    }

    @Override
    public List<String> getRequiredColumns() {
        return Arrays.stream(Columns.values())
                .map(Columns::toString)
                .collect(Collectors.toList());
    }

    @Override
    public IPPool readNextObject() throws Exception {
        IPPool result = new IPPool();
        Map<String, String> entries = this.readNext();

        if(entries == null) {
            return null;
        }

        result.vCenter = entries.get(Columns.VCENTER.toString());
        result.datacenter = entries.get(Columns.DATACENTER.toString());
        result.cluster = entries.get(Columns.CLUSTER.toString());
        result.vlan = entries.get(Columns.VLAN.toString());
        result.ipAddress = entries.get(Columns.IP_ADDRESS.toString());

        return result;
    }
}
