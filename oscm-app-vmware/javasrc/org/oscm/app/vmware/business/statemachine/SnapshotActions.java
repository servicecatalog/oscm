/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine;

import java.util.Date;

import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.statemachine.api.StateMachineAction;
import org.oscm.app.vmware.remote.vmware.VMClientPool;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.oscm.app.vmware.remote.vmware.VMwareClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.TaskInfo;

/**
 * @author kulle
 * 
 */
public class SnapshotActions extends Actions {

    private static final Logger logger = LoggerFactory
            .getLogger(SnapshotActions.class);

    private static final String TASK_NAME_CREATE_SNAPSHOT = "CreateSnapshot_Task";
    private static final String SNAPSHOT_NAME = "backup";
    private static final String EVENT_RUN = "run";

    @StateMachineAction
    public String deleteSnapshots(String instanceId,
            ProvisioningSettings settings,
            @SuppressWarnings("unused") InstanceStatus result) {

        VMPropertyHandler ph = new VMPropertyHandler(settings);
        try (VMwareClient client = new VMwareClientFactory("en")
                .getInstance(ph);) {

            ManagedObjectReference snapshot = client.findSnapshot(
                    ph.getInstanceName(),
                    ph.getServiceSetting(VMPropertyHandler.SNAPSHOT_ID));
            if (snapshot == null) {
                logger.info("Found no old snapshot to delete for instance "
                        + instanceId + ", virtual system '"
                        + ph.getInstanceName() + "' and snapshot id "
                        + ph.getServiceSetting(VMPropertyHandler.SNAPSHOT_ID));
                return EVENT_SUCCESS;
            }

            boolean removeChildren = false;
            boolean consolidate = true;
            ManagedObjectReference removeTask = client.getService()
                    .removeSnapshotTask(snapshot, removeChildren, consolidate);

            ph.setTask(client.retrieveTaskInfo(removeTask));
            return EVENT_RUN;
        } catch (Exception e) {
            String message = "Failed to delete old snapshot for instance "
                    + instanceId + " and snapshot id "
                    + ph.getServiceSetting(VMPropertyHandler.SNAPSHOT_ID);
            logger.error(message, e);
            ph.setSetting(VMPropertyHandler.SM_ERROR_MESSAGE, message.concat(e.getMessage()));
            return EVENT_FAILED;
        }
    }

    @StateMachineAction
    public String createSnapshot(String instanceId,
            ProvisioningSettings settings,
            @SuppressWarnings("unused") InstanceStatus result) {

        VMPropertyHandler ph = new VMPropertyHandler(settings);
        String vcenter = ph
                .getServiceSetting(VMPropertyHandler.TS_TARGET_VCENTER_SERVER);
        VMwareClient client = null;
        try {
            client = VMClientPool.getInstance().getPool().borrowObject(vcenter);

            ManagedObjectReference vm = client
                    .getVirtualMachine(ph.getInstanceName());
            String description = "Instance ID: " + instanceId
                    + ", Snapshot date: " + (new Date());
            boolean dumpMemory = false;
            boolean quiesceFileSystem = true;
            ManagedObjectReference task = client.getService()
                    .createSnapshotTask(vm, SNAPSHOT_NAME, description,
                            dumpMemory, quiesceFileSystem);

            ph.setTask(client.retrieveTaskInfo(task));
            return EVENT_RUN;
        } catch (Exception e) {
            String message = "Failed to create snapshot for instance "
                    + instanceId;
            logger.error(message, e);
            ph.setSetting(VMPropertyHandler.SM_ERROR_MESSAGE, message.concat(e.getMessage()));
            return EVENT_FAILED;
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
    protected String successfulTask(TaskInfo taskInfo, VMPropertyHandler ph) {
        ph.setSetting(VMPropertyHandler.GUEST_READY_TIMEOUT_REF,
                String.valueOf(System.currentTimeMillis()));
        if (TASK_NAME_CREATE_SNAPSHOT.equals(taskInfo.getName())) {
            ManagedObjectReference mor = (ManagedObjectReference) taskInfo
                    .getResult();
            ph.setSetting(VMPropertyHandler.SNAPSHOT_ID, mor.getValue());
        }
        return EVENT_SUCCESS;
    }

    @StateMachineAction
    @Override
    public String finish(String instanceId, ProvisioningSettings settings,
            InstanceStatus result) {

        result.setIsReady(true);
        VMPropertyHandler ph = new VMPropertyHandler(settings);
        logger.info("Created snapshot for instance " + instanceId
                + " with snapshot id "
                + ph.getServiceSetting(VMPropertyHandler.SNAPSHOT_ID));
        return EVENT_SUCCESS;
    }

}