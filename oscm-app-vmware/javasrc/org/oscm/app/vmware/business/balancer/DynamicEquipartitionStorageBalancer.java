/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.oscm.app.vmware.i18n.Messages;

/**
 * Storage balancer implementation dynamically selecting the least used storage
 * from vSphere.
 * 
 * @author Oliver Petrovski
 * 
 */
public class DynamicEquipartitionStorageBalancer extends StorageBalancer {

    @Override
    public VMwareStorage next(VMPropertyHandler properties)
            throws APPlatformException {
        VMwareStorage selectedStorage = null;
        String targetHost = properties
                .getServiceSetting(VMPropertyHandler.TS_TARGET_HOST);

        if (targetHost == null) {
            throw new APPlatformException(
                    Messages.getAll("error_target_host_not_defined"));
        }

        double maxFreeSpace = 0.0;
        for (VMwareStorage storage : inventory.getStorageByHost(targetHost)) {
            if (storage.getFree() > maxFreeSpace) {
                selectedStorage = storage;
                maxFreeSpace = storage.getFree();
            }
        }

        if (selectedStorage != null)

        {
            return selectedStorage;
        }

        throw new APPlatformException(Messages.getAll("error_outof_storage"));
    }
}
