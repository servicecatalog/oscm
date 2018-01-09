/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.model.VMwareHost;

/**
 * Implements a sequential host balancer filling the host systems in their
 * configured order.
 *
 * @author Dirk Bernsau
 */
public class SequentialHostBalancer extends HostBalancer {

    @Override
    public VMwareHost next(VMPropertyHandler properties) {
        for (VMwareHost host : getElements()) {
            if (isValid(host, properties)) {
                return host;
            }
        }
        return null;
    }
}
