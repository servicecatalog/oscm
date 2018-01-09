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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage balancer implementation selecting the least used storage from all
 * available storages to equally distribute the storage usage in the system.
 * 
 * @author Dirk Bernsau
 * 
 */
public class EquipartitionStorageBalancer extends StorageBalancer {

    private static final Logger logger = LoggerFactory
            .getLogger(EquipartitionStorageBalancer.class);

    @Override
    public VMwareStorage next(VMPropertyHandler properties)
            throws APPlatformException {

        VMwareStorage selectedStorage = null;
        for (VMwareStorage storage : getElements()) {
            if (isValid(storage, properties)) {
                logger.debug("Checking level of valid storage: " + storage
                        + " level: " + storage.getLevel());
                if (selectedStorage == null) {
                    selectedStorage = storage;
                } else if (selectedStorage.getLevel() > storage.getLevel()) {
                    selectedStorage = storage;
                }
            } else {
                logger.debug("Ignoring invalid storage " + storage);
            }
        }
        if (selectedStorage != null) {
            return selectedStorage;
        }

        throw new APPlatformException(Messages.getAll("error_outof_storage"));
    }
}
