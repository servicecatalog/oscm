/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.statemachine;

import java.util.Collections;

import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.VM;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.statemachine.api.StateMachineAction;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.remote.vmware.VMClientPool;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.TaskInfo;

public class DeleteActions extends Actions {

    private static final Logger logger = LoggerFactory
            .getLogger(DeleteActions.class);

    private static final String EVENT_DELETING = "deleting";

    @StateMachineAction
    public String checkVMExists(String instanceId,
            ProvisioningSettings settings, InstanceStatus result) {

        logger.debug("instance: " + instanceId);
        String eventId = "exists";
        VMPropertyHandler ph = new VMPropertyHandler(settings);
        String instanceName = null;
        try {
            instanceName = ph.getInstanceName();
        } catch (APPlatformException e1) {
            logger.error("Failed to retrieve instancename");
        }

        String vcenter = ph
                .getServiceSetting(VMPropertyHandler.TS_TARGET_VCENTER_SERVER);
        VMwareClient vmClient = null;
        try {
            vmClient = VMClientPool.getInstance().getPool()
                    .borrowObject(vcenter);
            VM vm = new VM(vmClient, instanceName);
            vm.isRunning();
            eventId = "exists";
        } catch (Exception e) {
            eventId = "not exists";
        } finally {
            if (vmClient != null) {
                try {
                    VMClientPool.getInstance().getPool().returnObject(vcenter,
                            vmClient);
                } catch (Exception e) {
                    logger.error("Failed to return VMware client into pool", e);
                }
            }
        }

        logger.debug("instance " + instanceName + " " + eventId);

        return eventId;
    }

    @StateMachineAction
    public String releaseIPaddress(String instanceId,
            ProvisioningSettings settings, InstanceStatus result) {
        String eventId = EVENT_FAILED;
        VMPropertyHandler ph = new VMPropertyHandler(settings);

        try {
            ph.releaseManuallyDefinedIPAddresses();
            eventId = EVENT_SUCCESS;
        } catch (Exception e) {
            logger.error(
                    "Failed to release IP address for instance " + instanceId,
                    e);
            String message = Messages.get(ph.getLocale(), "error_release_ip",
                    new Object[] { instanceId });
            ph.setSetting(VMPropertyHandler.SM_ERROR_MESSAGE, message);
        }

        return eventId;
    }

    @StateMachineAction
    public String deleteVM(String instanceId, ProvisioningSettings settings,
            @SuppressWarnings("unused") InstanceStatus result) {

        VMPropertyHandler ph = new VMPropertyHandler(settings);
        String vcenter = ph
                .getServiceSetting(VMPropertyHandler.TS_TARGET_VCENTER_SERVER);
        VMwareClient vmClient = null;
        try {
            vmClient = VMClientPool.getInstance().getPool()
                    .borrowObject(vcenter);
            VM vm = new VM(vmClient, ph.getInstanceName());
            TaskInfo taskInfo = vm.delete();
            ph.setTask(taskInfo);
            return EVENT_DELETING;
        } catch (Exception e) {
            logger.error("Failed to delete VM for instance " + instanceId, e);
            String message = Messages.get(ph.getLocale(), "error_delete_vm",
                    new Object[] { instanceId });
            ph.setSetting(VMPropertyHandler.SM_ERROR_MESSAGE, message);
            return EVENT_FAILED;
        } finally {
            if (vmClient != null) {
                try {
                    VMClientPool.getInstance().getPool().returnObject(vcenter,
                            vmClient);
                } catch (Exception e) {
                    logger.error("Failed to return VMware client into pool", e);
                }
            }

        }
    }

    @StateMachineAction
    public String notifyAdministrator(String instanceId,
            ProvisioningSettings settings, InstanceStatus result) {
        logger.debug("instance: " + instanceId);
        String eventId = EVENT_FAILED;

        VMPropertyHandler ph = new VMPropertyHandler(settings);
        String mailRecipient = ph
                .getServiceSetting(VMPropertyHandler.TS_MAIL_FOR_COMPLETION);
        if (mailRecipient == null || mailRecipient.trim().isEmpty()) {
            logger.debug("mailRecipient is not defined.");
            return EVENT_SUCCESS;
        }

        try {
            sendEmail(ph, instanceId, mailRecipient);
            eventId = EVENT_SUCCESS;
        } catch (Exception e) {
            logger.error("Failed to send mail after deleting the VM instance "
                    + instanceId, e);
            String message = Messages.get(ph.getLocale(),
                    "error_pause_after_creation", new Object[] { instanceId });
            ph.setSetting(VMPropertyHandler.SM_ERROR_MESSAGE, message);
        }

        return eventId;

    }

    private void sendEmail(VMPropertyHandler paramHandler, String instanceId,
            String mailRecipient) throws Exception {
        logger.debug("instanceId: " + instanceId + " mailRecipient: "
                + mailRecipient);
        String subject = Messages.get(paramHandler.getSettings().getLocale(),
                "mail_delete_vm.subject",
                new Object[] { paramHandler.getInstanceName() });
        String details = paramHandler.getConfigurationAsString(
                paramHandler.getSettings().getLocale());
        details += paramHandler.getResponsibleUserAsString(
                paramHandler.getSettings().getLocale());
        String text = Messages.get(paramHandler.getSettings().getLocale(),
                "mail_delete_vm.text",
                new Object[] { paramHandler.getInstanceName(), paramHandler
                        .getServiceSetting(VMPropertyHandler.REQUESTING_USER),
                        details });

        platformService.sendMail(Collections.singletonList(mailRecipient),
                subject, text);
    }
}
