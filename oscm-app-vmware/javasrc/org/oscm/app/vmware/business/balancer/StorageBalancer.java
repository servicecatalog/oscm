/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.util.ArrayList;
import java.util.List;

import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.model.VMwareStorage;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public void setConfiguration(Node node) {
        if (node != null) {

            NodeList storages = node.getOwnerDocument()
                    .getElementsByTagName("storage");

            if (storages == null || storages.getLength() == 0) {
                throw new IllegalArgumentException(
                        "No storage reference defined for balancer");
            }

            for (int i = 1; i < storages.getLength(); i++) {
                Node s = storages.item(i);
                datastoreNames
                        .add(XMLHelper.getAttributeValue(s, "name").trim());
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
    public boolean isValid(VMwareStorage storage,
            VMPropertyHandler properties) {
        return storage != null && storage.isEnabled()
        // add memory because respective swap space will be required
                && storage.checkLimit(properties.getTemplateDiskSpaceMB()
                        + properties.getConfigMemoryMB());
    }
}