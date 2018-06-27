/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.parser;

import org.oscm.app.vmware.parser.model.Cluster;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClusterParser extends CSVParser<Cluster> {
    enum Columns {
        VCENTER("VCenter"),
        DATACENTER("Datacenter"),
        CLUSTER_NAME("Clustername");

        private final String text;

        Columns(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return this.text;
        }
    }

    public ClusterParser(InputStream stream) throws Exception {
        super(stream);
    }

    @Override
    public List<String> getRequiredColumns() {
        return Arrays.stream(Columns.values())
                .map(Columns::toString)
                .collect(Collectors.toList());
    }

    @Override
    public Cluster readNextObject() throws Exception {
        Cluster result = new Cluster();
        Map<String, String> entries = this.readNext();

        if(entries == null) {
            return null;
        }

        result.vCenter = entries.get(Columns.VCENTER.toString());
        result.datacenter = entries.get(Columns.DATACENTER.toString());
        result.clusterName = entries.get(Columns.CLUSTER_NAME.toString());

        return result;
    }
}
