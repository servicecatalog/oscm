package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.ClusterParser;
import org.oscm.app.vmware.parser.model.Cluster;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class ClusterImporter implements Importer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VLANImporter.class);

    private final DataAccessService das;

    private void save(Cluster cluster) {
        // TODO save/update vcenter
    }

    private int getDatacenterKey(String vCenter, String datacenter) {
        return 0;
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
