/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;

/**
 * Interface for deployment balancer.
 * 
 * @author soehnges
 */
public interface VMwareBalancer<T> {

    /**
     * Sets property container for this balancer.
     */
    public void setConfiguration(HierarchicalConfiguration xmlConfig);

    /**
     * Returns next element to process.
     * 
     * @throws APPlatformException
     *             in case no host, storage etc. is available
     */
    public T next(VMPropertyHandler properties) throws APPlatformException;

    /**
     * Sets the inventory information describing available resources in the data
     * center.
     * 
     * @param inventory
     *            the resources
     */
    public void setInventory(VMwareDatacenterInventory inventory);

    /**
     * Returns a list of all configured elements that are also present in the
     * inventory.
     * 
     * @return the list of elements - may be empty but not <code>null</code>
     */
    public List<T> getElements();
}
