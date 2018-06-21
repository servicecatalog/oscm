package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.DatacenterParser;
import org.oscm.app.vmware.parser.model.Datacenter;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class DatacenterImporter implements Importer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VLANImporter.class);

    private final DataAccessService das;

    private void save(Datacenter datacenter) {
        // TODO save/update vcenter
    }

    DatacenterImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try(DatacenterParser parser = new DatacenterParser(csvFile)) {
            Datacenter datacenter;
            while((datacenter = parser.readNextObject()) != null) {
                this.save(datacenter);
            }
        }
    }
}
