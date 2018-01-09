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
 * Implements a sequential storage balancer filling the storages in their
 * configured order.
 * 
 * @author soehnges
 */
public class SequentialStorageBalancer extends StorageBalancer {

    @Override
    public VMwareStorage next(VMPropertyHandler properties)
            throws APPlatformException {

        for (VMwareStorage storage : getElements()) {
            if (isValid(storage, properties)) {
                return storage;
            }
        }

        throw new APPlatformException(Messages.getAll("error_outof_storage"));
    }
}
