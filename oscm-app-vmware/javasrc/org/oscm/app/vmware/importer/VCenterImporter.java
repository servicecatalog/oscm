package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.parser.VCenterParser;
import org.oscm.app.vmware.parser.model.VCenter;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class VCenterImporter implements Importer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VCenterImporter.class);

    private final DataAccessService das;

    private void save(VCenter vCenter) {
        // TODO save/update vcenter
    }

    VCenterImporter(DataAccessService das) {
        this.das = das;
    }

    @Override
    public void load(InputStream csvFile) {
        try(VCenterParser parser = new VCenterParser(csvFile)) {
            VCenter vCenter;
            while((vCenter = parser.readNextObject()) != null) {
                this.save(vCenter);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
