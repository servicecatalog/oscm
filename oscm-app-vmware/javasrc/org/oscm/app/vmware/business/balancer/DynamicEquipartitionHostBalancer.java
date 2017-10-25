/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.util.Collection;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.oscm.app.vmware.i18n.Messages;

/**
 * Balancer implementation determining dynamically from vSphere the host with
 * the lowest number of VMs running on it.
 * 
 * @author Oliver Petrovski
 * 
 */
public class DynamicEquipartitionHostBalancer extends HostBalancer {

    @Override
    public void setConfiguration(HierarchicalConfiguration xmlConfig) {
        super.setConfiguration(xmlConfig);
    }

    @Override
    public VMwareHost next(VMPropertyHandler properties)
            throws APPlatformException {

        VMwareHost selectedHost = null;
        int minVM = Integer.MAX_VALUE;

        Collection<VMwareHost> hosts = inventory.getHosts();
        for (VMwareHost host : hosts) {
            int numVM = host.getAllocatedVMs();
            if (numVM < minVM) {
                selectedHost = host;
                minVM = numVM;
            }
        }

        if (selectedHost == null) {
            throw new APPlatformException(Messages.getAll("error_outof_host"));
        }

        return selectedHost;
    }
}
