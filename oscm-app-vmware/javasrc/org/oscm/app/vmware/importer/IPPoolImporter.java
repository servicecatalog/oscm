package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.IPPoolParser;
import org.oscm.app.vmware.parser.model.IPPool;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class IPPoolImporter implements Importer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VLANImporter.class);

    private final DataAccessService das;

    private void save(IPPool ipPool) {
        // TODO save/update vcenter
    }

    IPPoolImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try(IPPoolParser parser = new IPPoolParser(csvFile)) {
            IPPool ipPool;
            while((ipPool = parser.readNextObject()) != null) {
                this.save(ipPool);
            }
        }
    }
}
