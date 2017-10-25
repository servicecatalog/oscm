/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.model.VMwareStorage;

/**
 * Common superclass for all storage balancers.
 * 
 * @author Dirk Bernsau
 * 
 */
public abstract class StorageBalancer implements VMwareBalancer<VMwareStorage> {

    protected List<String> datastoreNames = new ArrayList<String>();
    protected VMwareDatacenterInventory inventory;

    @Override
    public void setConfiguration(HierarchicalConfiguration config) {
        if (config != null) {
            String storages = config.getString("[@storage]");
            if (storages == null || storages.trim().length() == 0) {
                throw new IllegalArgumentException(
                        "No storage reference defined for balancer");
            }
            String[] elms = storages.split(",");
            for (String x : elms) {
                datastoreNames.add(x.toString().trim());
            }
        }
    }

    @Override
    public abstract VMwareStorage next(VMPropertyHandler properties)
            throws APPlatformException;

    @Override
    public void setInventory(VMwareDatacenterInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public List<VMwareStorage> getElements() {
        List<VMwareStorage> result = new ArrayList<VMwareStorage>();
        if (inventory != null) {
            for (String ds : datastoreNames) {
                VMwareStorage storage = inventory.getStorage(ds);
                if (storage != null) {
                    result.add(storage);
                }
            }
        }
        return result;
    }

    /**
     * Checks whether the given storage is capable of hosting the requested
     * configuration.
     * 
     * @param storage
     *            the storage instance in question
     * @param properties
     *            the properties defining the requested instance
     * @return <code>true</code> when all conditions and limits are met
     */
    public boolean isValid(VMwareStorage storage, VMPropertyHandler properties) {
        return storage != null
                && storage.isEnabled()
                // add memory because respective swap space will be required
                && storage.checkLimit(properties.getTemplateDiskSpaceMB()
                        + properties.getConfigMemoryMB());
    }
}
