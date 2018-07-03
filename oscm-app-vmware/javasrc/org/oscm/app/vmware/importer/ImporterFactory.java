/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.importer;

import org.oscm.app.vmware.importer.model.ConfigurationType;
import org.oscm.app.vmware.persistence.DataAccessService;

public class ImporterFactory {
    private ImporterFactory() {}

    public static Importer getImporter(ConfigurationType ct, DataAccessService das) {
        switch(ct) {
            case CLUSTER:
                return new ClusterImporter(das);
            case DATACENTER:
                return new DatacenterImporter(das);
            case IPPOOL:
                return new IPPoolImporter(das);
            case VCENTER:
                return new VCenterImporter(das);
            case VLAN:
                return new VLANImporter(das);
            default:
                throw new UnsupportedOperationException(ct.getTableName()
                        + " import not supported.");
        }
    }
}
