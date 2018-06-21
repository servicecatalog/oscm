package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.VLANParser;
import org.oscm.app.vmware.parser.model.VLAN;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class VLANImporter implements Importer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VLANImporter.class);

    private final DataAccessService das;

    private void save(VLAN vlan) {
        // TODO save/update vcenter
    }

    VLANImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) throws Exception {
        try(VLANParser parser = new VLANParser(csvFile)) {
            VLAN vlan;
            while((vlan = parser.readNextObject()) != null) {
                this.save(vlan);
            }
        }
    }
}
