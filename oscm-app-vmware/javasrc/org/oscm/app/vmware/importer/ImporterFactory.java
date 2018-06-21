package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.persistence.DataAccessService;

public class ImporterFactory {
    private ImporterFactory() {}

    public static Importer getImporter(String tableName, DataAccessService das) {
        switch(tableName) {
            case "VCenter":
                return new VCenterImporter(das);
            default:
                throw new UnsupportedOperationException(tableName + " importer not supported.");
        }
    }
}
