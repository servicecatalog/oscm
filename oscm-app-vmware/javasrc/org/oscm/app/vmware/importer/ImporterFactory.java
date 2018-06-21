package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.persistence.DataAccessService;

public class ImporterFactory {
    private ImporterFactory() {}

    public static Importer getImporter(String tableName, DataAccessService das) {
        switch(tableName) {
            case "Cluster":
                return new ClusterImporter(das);
            case "Datacenter":
                return new DatacenterImporter(das);
            case "IPPool":
                return new IPPoolImporter(das);
            case "VCenter":
                return new VCenterImporter(das);
            case "VLAN":
                return new VLANImporter(das);
            default:
                throw new UnsupportedOperationException(tableName + " import not supported.");
        }
    }
}
