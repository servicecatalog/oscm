/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine;

import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.statemachine.api.StateMachineAction;
import org.oscm.app.vmware.remote.vmware.VMClientPool;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.ManagedObjectReference;

/**
 * @author kulle
 * 
 */
public class RestoreActions extends Actions {

    private static final Logger logger = LoggerFactory
            .getLogger(RestoreActions.class);

    private static final String EVENT_RUN = "run";

    @StateMachineAction
    public String restoreSnapshot(String instanceId,
            ProvisioningSettings settings,
            @SuppressWarnings("unused") InstanceStatus result) {

        VMPropertyHandler ph = new VMPropertyHandler(settings);
        String vcenter = ph
                .getServiceSetting(VMPropertyHandler.TS_TARGET_VCENTER_SERVER);
        VMwareClient client = null;
        try {
            client = VMClientPool.getInstance().getPool().borrowObject(vcenter);

            ManagedObjectReference snapshot = client.findSnapshot(
                    ph.getInstanceName(),
                    ph.getServiceSetting(VMPropertyHandler.SNAPSHOT_ID));

            if (snapshot == null) {
                logger.info("Found no snapshot to restore for instance "
                        + instanceId + ", virtual system '"
                        + ph.getInstanceName() + "' and snapshot id "
                        + ph.getServiceSetting(VMPropertyHandler.SNAPSHOT_ID));
                return EVENT_SUCCESS;
            }

            ManagedObjectReference targetHost = null;
            boolean suppressPowerOn = false;
            ManagedObjectReference task = client.getService()
                    .revertToSnapshotTask(snapshot, targetHost,
                            suppressPowerOn);

            ph.setTask(client.retrieveTaskInfo(task));
            return EVENT_RUN;
        } catch (Exception e) {
            String message = "Failed to restore snapshot for instance "
                    + instanceId;
            logger.error(message, e);
            ph.setSetting(VMPropertyHandler.SM_ERROR_MESSAGE, message);
            return EVENT_ERROR;
        } finally {
            if (client != null) {
                try {
                    VMClientPool.getInstance().getPool().returnObject(vcenter,
                            client);
                } catch (Exception e) {
                    logger.error("Failed to return VMware client into pool", e);
                }
            }
        }
    }

    @StateMachineAction
    @Override
    public String finish(String instanceId, ProvisioningSettings settings,
            InstanceStatus result) {

        result.setIsReady(true);
        VMPropertyHandler ph = new VMPropertyHandler(settings);
        logger.info("Restored snapshot for instance " + instanceId
                + " with snapshot id "
                + ph.getServiceSetting(VMPropertyHandler.SNAPSHOT_ID));
        return EVENT_SUCCESS;
    }

}