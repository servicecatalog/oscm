/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.iaas.data.DiskImage;
import org.oscm.app.iaas.data.FWStatus;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.data.ResourceType;
import org.oscm.app.iaas.data.VServerStatus;
import org.oscm.app.iaas.data.VSystemStatus;
import org.oscm.app.iaas.exceptions.MissingResourceException;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.iaas.intf.FWCommunication;
import org.oscm.app.iaas.intf.VDiskCommunication;
import org.oscm.app.iaas.intf.VServerCommunication;
import org.oscm.app.iaas.intf.VSystemCommunication;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.exceptions.SuspendException;

@Stateless
@LocalBean
public class VServerProcessorBean extends BaseProvisioningProcessor {

    private static final Logger logger = LoggerFactory
            .getLogger(VServerProcessorBean.class);

    @EJB(beanInterface = VServerCommunication.class)
    protected VServerCommunication vserverComm;

    @EJB(beanInterface = VSystemCommunication.class)
    protected VSystemCommunication vsysComm;

    @EJB(beanInterface = VDiskCommunication.class)
    protected VDiskCommunication vdiskInfo;

    @EJB(beanInterface = FWCommunication.class)
    protected FWCommunication fwComm;

    @PostConstruct
    public void initialize() {
        try {
            platformService = APPlatformServiceFactory.getInstance();
            setPlatformService(platformService);
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    @Override
    public void process(String controllerId, String instanceId,
            PropertyHandler paramHandler) throws Exception {
        if (paramHandler.isVirtualServerProvisioning()) {
            try {
                String vSystemState = paramHandler.getIaasContext()
                        .getVSystemStatus();
                if (vSystemState == null) {
                    vSystemState = vsysComm.getVSystemState(paramHandler);
                    paramHandler.getIaasContext()
                            .setVSystemStatus(vSystemState);
                }
                if (!VSystemStatus.NORMAL.equals(vSystemState)) {
                    logger.debug(
                            "Virtual system is currently in state '{}'. This might cause waiting for NORMAL state.",
                            vSystemState);
                }
                validateParameters(paramHandler);
                dispatch(controllerId, instanceId, paramHandler);
            } catch (MissingResourceException e) {
                if (ResourceType.VSERVER.equals(e.getResouceType())
                        && Operation.VSERVER_DELETION.equals(paramHandler
                                .getOperation())
                        && paramHandler.getVserverId() != null
                        && paramHandler.getVserverId().equals(e.getResouceId())) {
                    logger.warn(
                            "Ignoring missing resource since server {} is to be deleted anyway",
                            paramHandler.getVserverId());
                    // adapt internal knowledge about the instance an try to
                    // finalize the deletion process
                    paramHandler.setState(FlowState.VSERVER_DELETING);
                    paramHandler.setVserverId("");
                    dispatch(controllerId, instanceId, paramHandler);
                } else {
                    throw e.getSuspendException();
                }
            }

        } else {
            throw new RuntimeException("Wrong processor!");
        }
    }

    /**
     * Dispatches next operation for virtual server according to current
     * provisioning state and task state.
     * 
     * @param controllerId
     *            id of the controller
     * @param instanceId
     *            id of the instance
     * @param paramHandler
     *            entity which holds all properties of the instance.
     * @throws Exception
     */
    void dispatch(String controllerId, String instanceId,
            PropertyHandler paramHandler) throws Exception {

        Operation operationState = paramHandler.getOperation();
        FlowState flowState = paramHandler.getState();
        logger.debug("Dispatching in VServerProcessor with OperationState="
                + operationState + " and FlowState=" + flowState);
        FlowState newState = null;
        if (VSystemStatus.ERROR.equals(paramHandler.getIaasContext()
                .getVSystemStatus())) {
            newState = FlowState.FAILED;
            disableExclusiveProcessing(controllerId, instanceId, paramHandler);
        } else {
            switch (operationState) { // => Dispatch next operation
            case VSERVER_CREATION:
                newState = manageCreationProcess(controllerId, instanceId,
                        paramHandler, flowState, newState);
                break;
            case VSERVER_MODIFICATION:
                newState = manageModificationProcess(controllerId, instanceId,
                        paramHandler, flowState, newState);
                break;
            case VSERVER_MODIFICATION_VDISK_CREATION:
                newState = manageModificationVDiskCreation(controllerId,
                        instanceId, paramHandler, flowState, newState);
                break;
            case VSERVER_MODIFICATION_VDISK_DELETION:
                newState = manageModificationVDiskDeletion(controllerId,
                        instanceId, paramHandler, flowState, newState);
                break;
            case VSERVER_DELETION:
                newState = manageDeletionProcess(controllerId, instanceId,
                        paramHandler, flowState, newState);
                break;
            case VSERVER_ACTIVATION:
                newState = manageActivationProcess(controllerId, instanceId,
                        paramHandler, flowState, newState);
                break;
            case VSERVER_OPERATION:
                newState = manageOperationsProcess(controllerId, instanceId,
                        paramHandler, flowState, newState);
                break;
            default:
            }
        }
        if (newState != null) { // update changed flow state
            paramHandler.setState(newState);
            logger.debug("Dispatch in VServerProcessor returns new FlowState="
                    + newState);
        } else {
            logger.debug("Dispatch in VServerProcessor leaves FlowState unchanged");
        }
    }

    FlowState manageOperationsProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState,
            FlowState newStateParam) throws Exception {
        FlowState newState = newStateParam;
        switch (flowState) {
        case VSERVER_START_REQUESTED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTING, paramHandler)) {
                if (vserverComm.startVServer(paramHandler)) {
                    newState = FlowState.VSERVER_STARTING;
                }
            }
            break;
        case VSERVER_STARTING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTED, paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_STARTED;
                }
            }
            break;
        case VSERVER_STARTED:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                newState = FlowState.FINISHED;
            }
            break;
        case VSERVER_STOP_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVERS_STOPPING, paramHandler)) {
                String status = vserverComm.getVServerStatus(paramHandler);
                if (VServerStatus.RUNNING.equals(status)) {
                    vserverComm.stopVServer(paramHandler);
                    newState = FlowState.VSERVER_STOPPING;
                }
            }
            break;
        case VSERVER_STOPPING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STOPPED, paramHandler)) {
                String status = vserverComm.getVServerStatus(paramHandler);
                if (VServerStatus.STOPPED.equals(status)) {
                    newState = FlowState.VSERVER_STOPPED;
                }
            }
            break;
        case VSERVER_STOPPED:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                newState = FlowState.FINISHED;
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageActivationProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState,
            FlowState newState) throws Exception {
        switch (flowState) {
        case VSERVER_ACTIVATION_REQUESTED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTING, paramHandler)) {
                if (vserverComm.startVServer(paramHandler)) {
                    newState = FlowState.VSERVER_STARTING;
                }
            }
            break;
        case VSERVER_STARTING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTED, paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_STARTED;
                }
            }
            break;
        case VSERVER_STARTED:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                newState = FlowState.FINISHED;
            }
            break;
        case VSERVER_DEACTIVATION_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STOP_FOR_DEACTIVATION, paramHandler)) {
                vserverComm.stopVServer(paramHandler);
                if (VServerStatus.STOPPED.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_STOP_FOR_DEACTIVATION;
                }
            }
            break;
        case VSERVER_STOP_FOR_DEACTIVATION:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                newState = FlowState.FINISHED;
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageDeletionProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState,
            FlowState newState) throws Exception {
        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());
        String fwStatus = fwComm.getFirewallStatus(paramHandler);
        switch (flowState) {
        case VSERVER_DELETION_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STOPPED_FOR_DELETION, paramHandler)) {
                String status = vserverComm.getVServerStatus(paramHandler);
                if (VServerStatus.RUNNING.equals(status)) {
                    vserverComm.stopVServer(paramHandler);
                } else if (VServerStatus.STOPPED.equals(status)) {
                    newState = FlowState.VSERVER_STOPPED_FOR_DELETION;
                }
            }
            break;
        case VSERVER_STOPPED_FOR_DELETION:
            if (vdiskInfo.isAttachedVDisksFound(paramHandler)) {
                if (vSysInNormalState
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.VSDISK_DETACHING, paramHandler)) {
                    vdiskInfo.detachVDisks(paramHandler);
                    newState = FlowState.VSDISK_DETACHING;
                }
            } else {
                if (vSysInNormalState
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.VSERVER_DELETING, paramHandler)) {
                    // lock the destroyVServer operation by checking
                    // isVSysInNormalState. If VSYS is not in normal state, it
                    // means it either deploying a sever or configuring the
                    // other server or destroying the other server
                    if (FWStatus.RUNNING.equals(fwStatus)) {
                        String status = vserverComm
                                .getVServerStatus(paramHandler);
                        if (VServerStatus.STOPPED.equals(status)) {
                            vserverComm.destroyVServer(paramHandler);
                            newState = FlowState.VSERVER_DELETING;
                        }
                    } else {
                        if (checkNextStatus(controllerId, instanceId,
                                FlowState.FW_STARTING_FOR_VSERVER_DELETION,
                                paramHandler)) {
                            fwComm.startFirewall(paramHandler);
                            newState = FlowState.FW_STARTING_FOR_VSERVER_DELETION;
                        }
                    }
                }
            }
            break;
        case FW_STARTING_FOR_VSERVER_DELETION:
            if (FWStatus.RUNNING.equals(fwStatus)) {
                if (checkNextStatus(controllerId, instanceId,
                        FlowState.FW_STARTED_FOR_VSERVER_DELETION, paramHandler)) {
                    String status = vserverComm.getVServerStatus(paramHandler);
                    if (VServerStatus.STOPPED.equals(status)) {
                        vserverComm.destroyVServer(paramHandler);
                        newState = FlowState.VSERVER_DELETING;
                    }
                }
            }
            break;
        case VSERVER_DELETING:
            if (vserverComm.isVServerDestroyed(paramHandler)) {
                if (vSysInNormalState
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.DESTROYED, paramHandler)) {
                    newState = FlowState.DESTROYED;
                    String mail = paramHandler.getMailForCompletion();
                    String subscriptionId = paramHandler.getSettings()
                            .getOriginalSubscriptionId();
                    if (mail != null) {
                        String locale = getTechnicalProviderLocale(
                                controllerId, paramHandler);
                        String subject = Messages.get(locale,
                                "mail_VSERVER_manual_disposal.subject",
                                instanceId, subscriptionId);
                        String text = Messages.get(locale,
                                "mail_VSERVER_manual_disposal.text",
                                instanceId, subscriptionId);
                        platformService.sendMail(
                                Collections.singletonList(mail), subject, text);
                    }
                }
            }
            break;
        case VSDISK_DETACHING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_DETACHED, paramHandler)) {
                if (vdiskInfo.areVDisksDetached(paramHandler)) {
                    newState = FlowState.VSDISK_DETACHED;
                }
            }
            break;
        case VSDISK_DETACHED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSDISK_DELETING, paramHandler)) {
                vdiskInfo.destroyVDisks(paramHandler);
                newState = FlowState.VSDISK_DELETING;
            }
            break;
        case VSDISK_DELETING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_DESTROYED, paramHandler)) {
                if (vdiskInfo.areVDisksDestroyed(paramHandler)) {
                    newState = FlowState.VSDISK_DESTROYED;
                }
            }
            break;
        case VSDISK_DESTROYED:
            if (FWStatus.RUNNING.equals(fwStatus)) {
                if (vSysInNormalState
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.VSERVER_DELETING, paramHandler)) {
                    vserverComm.destroyVServer(paramHandler);
                    newState = FlowState.VSERVER_DELETING;
                }
            } else {
                if (checkNextStatus(controllerId, instanceId,
                        FlowState.FW_STARTING_FOR_VSERVER_DELETION,
                        paramHandler)) {
                    fwComm.startFirewall(paramHandler);
                    newState = FlowState.FW_STARTING_FOR_VSERVER_DELETION;
                }
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageModificationProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState,
            FlowState newState) throws Exception {
        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());
        switch (flowState) {
        case VSERVER_MODIFICATION_REQUESTED:
            if (vSysInNormalState) {
                newState = vserverComm.modifyVServerAttributes(paramHandler);
            }
            break;
        case VSERVER_STOPPING_FOR_MODIFICATION:
            if (vSysInNormalState
                    && VServerStatus.STOPPED.equals(vserverComm
                            .getNonErrorVServerStatus(paramHandler))) {
                if (checkNextStatus(controllerId, instanceId,
                        FlowState.VSERVER_UPDATING, paramHandler)) {
                    newState = vserverComm
                            .modifyVServerAttributes(paramHandler);

                }
            }
            break;
        case VSERVER_UPDATING:
            if (vSysInNormalState) {
                Set<String> toBeStarted = paramHandler.getVserversToBeStarted();
                if (toBeStarted.contains(paramHandler.getVserverId())
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.VSERVER_STARTING, paramHandler)) {
                    if (vserverComm.startVServer(paramHandler)) {
                        newState = FlowState.VSERVER_STARTING;
                    } else {
                        // release lock if present
                        checkNextStatus(controllerId, instanceId,
                                FlowState.VSERVER_STARTED, paramHandler);
                        newState = FlowState.VSERVER_STARTED;
                    }
                    // start request no longer necessary
                    paramHandler.removeVserverToBeStarted(paramHandler
                            .getVserverId());
                } else {
                    newState = FlowState.VSERVER_RETRIEVEGUEST;
                }
            }
            break;
        case VSERVER_UPDATED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSERVER_STARTING, paramHandler)) {
                if (vserverComm.startVServer(paramHandler)) {
                    newState = FlowState.VSERVER_STARTING;
                }
            }
            break;
        case VSERVER_STARTING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTED, paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_STARTED;
                }
            }
            break;
        case VSERVER_STARTED:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_RETRIEVEGUEST;
                }
            }
            break;
        case VSERVER_RETRIEVEGUEST:
            String mail = paramHandler.getMailForCompletion();
            if (mail != null) {
                // Check for manual operation
                newState = dispatchVServerManualOperation(controllerId,
                        instanceId, paramHandler, mail);
            } else if (checkNextStatus(controllerId, instanceId,
                    FlowState.FINISHED, paramHandler)) {
                // Simply finish if no mail is given
                newState = FlowState.FINISHED;
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageModificationVDiskCreation(String controllerId,
            String instanceId, PropertyHandler paramHandler,
            FlowState flowState, FlowState newStateParam) throws Exception {
        FlowState newState = newStateParam;
        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());
        switch (flowState) {
        case VSERVER_MODIFICATION_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STOPPED_FOR_MODIFICATION, paramHandler)) {
                String status = vserverComm.getVServerStatus(paramHandler);
                if (VServerStatus.RUNNING.equals(status)) {
                    vserverComm.stopVServer(paramHandler);
                } else if (VServerStatus.STOPPED.equals(status)) {
                    newState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
                }
            }
            break;
        case VSERVER_STOPPED_FOR_MODIFICATION:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSERVER_UPDATING, paramHandler)) {
                newState = FlowState.VSERVER_UPDATING;
                vserverComm.modifyVServerAttributes(paramHandler);
                if (vdiskInfo.isAdditionalDiskSelected(paramHandler)) {
                    newState = FlowState.VSDISK_CREATION_REQUESTED;
                }
            }
            break;
        case VSDISK_CREATION_REQUESTED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSDISK_CREATING, paramHandler)) {
                vdiskInfo.createVDisk(paramHandler);
                newState = FlowState.VSDISK_CREATING;
            }
            break;
        case VSDISK_CREATING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_CREATED, paramHandler)) {
                if (vdiskInfo.isVDiskDeployed(paramHandler)) {
                    newState = FlowState.VSDISK_CREATED;
                }
            }
            break;
        case VSDISK_CREATED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSDISK_ATTACHING, paramHandler)) {
                vdiskInfo.attachVDisk(paramHandler);
                newState = FlowState.VSDISK_ATTACHING;
            }
            break;
        case VSDISK_ATTACHING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_ATTACHED, paramHandler)) {
                if (vdiskInfo.isVDiskAttached(paramHandler)) {
                    newState = FlowState.VSDISK_ATTACHED;
                }
            }
            break;
        case VSDISK_ATTACHED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_UPDATING, paramHandler)) {
                newState = FlowState.VSERVER_UPDATING;
            }
            break;
        case VSERVER_UPDATING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_UPDATED, paramHandler)) {
                if (VServerStatus.STOPPED.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_UPDATED;
                }
            }
            break;
        case VSERVER_UPDATED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTING, paramHandler)) {
                if (vserverComm.startVServer(paramHandler)) {
                    newState = FlowState.VSERVER_STARTING;
                }
            }
            break;
        case VSERVER_STARTING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTED, paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_STARTED;
                }
            }
            break;
        case VSERVER_STARTED:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_RETRIEVEGUEST;
                }
            }
            break;
        case VSERVER_RETRIEVEGUEST:
            String mail = paramHandler.getMailForCompletion();
            if (mail != null) {
                // Check for manual operation
                newState = dispatchVServerManualOperation(controllerId,
                        instanceId, paramHandler, mail);
            } else if (checkNextStatus(controllerId, instanceId,
                    FlowState.FINISHED, paramHandler)) {
                // Simply finish if no mail is given
                newState = FlowState.FINISHED;
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageModificationVDiskDeletion(String controllerId,
            String instanceId, PropertyHandler paramHandler,
            FlowState flowState, FlowState newStateParam) throws Exception {
        FlowState newState = newStateParam;
        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());
        switch (flowState) {
        case VSERVER_MODIFICATION_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STOPPED_FOR_MODIFICATION, paramHandler)) {
                String status = vserverComm.getVServerStatus(paramHandler);
                if (VServerStatus.RUNNING.equals(status)) {
                    vserverComm.stopVServer(paramHandler);
                } else if (VServerStatus.STOPPED.equals(status)) {
                    newState = FlowState.VSERVER_STOPPED_FOR_MODIFICATION;
                }
            }
            break;
        case VSERVER_STOPPED_FOR_MODIFICATION:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSERVER_UPDATING, paramHandler)) {
                vserverComm.modifyVServerAttributes(paramHandler);
                newState = FlowState.VSERVER_UPDATING;
                if (vdiskInfo.isAttachedVDisksFound(paramHandler)) {
                    newState = FlowState.VSDISK_DELETION_REQUESTED;
                }
            }
            break;
        case VSDISK_DELETION_REQUESTED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSDISK_DETACHING, paramHandler)) {
                vdiskInfo.detachVDisks(paramHandler);
                newState = FlowState.VSDISK_DETACHING;
            }
            break;
        case VSDISK_DETACHING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_DETACHED, paramHandler)) {
                if (vdiskInfo.areVDisksDetached(paramHandler)) {
                    newState = FlowState.VSDISK_DETACHED;
                }
            }
            break;
        case VSDISK_DETACHED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSDISK_DELETING, paramHandler)) {
                vdiskInfo.destroyVDisks(paramHandler);
                newState = FlowState.VSDISK_DELETING;
            }
            break;
        case VSDISK_DELETING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_DESTROYED, paramHandler)) {
                if (vdiskInfo.areVDisksDestroyed(paramHandler)) {
                    newState = FlowState.VSERVER_UPDATING;
                }
            }
            break;
        case VSERVER_UPDATING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_UPDATED, paramHandler)) {
                if (VServerStatus.STOPPED.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_UPDATED;
                }
            }
            break;
        case VSERVER_UPDATED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTING, paramHandler)) {
                if (vserverComm.startVServer(paramHandler)) {
                    newState = FlowState.VSERVER_STARTING;
                }
            }
            break;
        case VSERVER_STARTING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTED, paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_STARTED;
                }
            }
            break;
        case VSERVER_STARTED:
            if (VServerStatus.RUNNING.equals(vserverComm
                    .getVServerStatus(paramHandler))) {
                if (paramHandler.getMailForCompletion() != null) {
                    if (checkNextStatus(controllerId, instanceId,
                            FlowState.VSERVER_RETRIEVEGUEST, paramHandler)) {
                        newState = FlowState.VSERVER_RETRIEVEGUEST;
                    }
                } else {
                    newState = FlowState.FINISHED;
                }
            }
            break;
        case VSERVER_RETRIEVEGUEST:
            String mail = paramHandler.getMailForCompletion();
            if (mail != null) {
                // Check for manual operation
                newState = dispatchVServerManualOperation(controllerId,
                        instanceId, paramHandler, mail);
            } else {
                // Simply finish if no mail is given
                newState = FlowState.FINISHED;
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageCreationProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState,
            FlowState newStateParam) throws Exception {
        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());
        FlowState newState = newStateParam;
        String fwStatus = fwComm.getFirewallStatus(paramHandler);
        switch (flowState) {
        case VSERVER_CREATION_REQUESTED:
            // lock the create server operation by checking isVSysInNormalState
            // If VSYS is not in normal state, it means it either deploying
            // other sever or configuring other server or destroying
            if (FWStatus.RUNNING.equals(fwStatus)) {
                if (vSysInNormalState
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.VSERVER_CREATING, paramHandler)) {
                    vserverComm.createVServer(paramHandler);
                    newState = FlowState.VSERVER_CREATING;
                }
            } else {
                if (checkNextStatus(controllerId, instanceId,
                        FlowState.FW_STARTING_FOR_VSERVER_CREATION,
                        paramHandler)) {
                    fwComm.startFirewall(paramHandler);
                    newState = FlowState.FW_STARTING_FOR_VSERVER_CREATION;
                }
            }
            break;

        case FW_STARTING_FOR_VSERVER_CREATION:
            if (FWStatus.RUNNING.equals(fwStatus)) {
                if (vSysInNormalState
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.FW_STARTED_FOR_VSERVER_CREATION,
                                paramHandler)) {
                    vserverComm.createVServer(paramHandler);
                    newState = FlowState.VSERVER_CREATING;
                }
            }
            break;
        case VSERVER_CREATING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_CREATED, paramHandler)) {
                String vServerStatus = vserverComm
                        .getVServerStatus(paramHandler);
                if (VServerStatus.STOPPED.equals(vServerStatus)
                        || VServerStatus.RUNNING.equals(vServerStatus)
                        || VServerStatus.STARTING.equals(vServerStatus)) {
                    newState = FlowState.VSERVER_CREATED;
                }
            }
            break;
        case VSERVER_CREATED:
            if (vdiskInfo.isAdditionalDiskSelected(paramHandler)) {
                if (checkNextStatus(controllerId, instanceId,
                        FlowState.VSDISK_CREATION_REQUESTED, paramHandler)) {
                    newState = FlowState.VSDISK_CREATION_REQUESTED;
                }
            } else {
                String vServerStatus = vserverComm
                        .getVServerStatus(paramHandler);
                if (VServerStatus.STOPPED.equals(vServerStatus)) {
                    if (checkNextStatus(controllerId, instanceId,
                            FlowState.VSERVER_STARTING, paramHandler)) {
                        vserverComm.startVServer(paramHandler);
                        newState = FlowState.VSERVER_STARTING;
                    }
                } else if (VServerStatus.STARTING.equals(vServerStatus)) {
                    newState = FlowState.VSERVER_STARTING;
                } else if (VServerStatus.RUNNING.equals(vServerStatus)) {
                    newState = FlowState.VSERVER_STARTED;
                }
            }
            break;
        case VSERVER_STARTING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTED, paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_STARTED;
                }
            }
            break;
        case VSERVER_STARTED:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                if (VServerStatus.RUNNING.equals(vserverComm
                        .getVServerStatus(paramHandler))) {
                    newState = FlowState.VSERVER_RETRIEVEGUEST;
                }
            }
            break;
        case VSERVER_RETRIEVEGUEST:
            String mail = paramHandler.getMailForCompletion();
            if (mail != null) {
                // Check for manual operation
                newState = dispatchVServerManualOperation(controllerId,
                        instanceId, paramHandler, mail);
            } else if (checkNextStatus(controllerId, instanceId,
                    FlowState.FINISHED, paramHandler)) {
                // Simply finish if no mail is given
                newState = FlowState.FINISHED;
            }
            break;
        case VSDISK_CREATION_REQUESTED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSDISK_CREATING, paramHandler)) {
                vdiskInfo.createVDisk(paramHandler);
                newState = FlowState.VSDISK_CREATING;
            }
            break;
        case VSDISK_CREATING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_CREATED, paramHandler)) {
                if (vdiskInfo.isVDiskDeployed(paramHandler)) {
                    newState = FlowState.VSDISK_CREATED;
                }
            }
            break;
        case VSDISK_CREATED:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSDISK_ATTACHING, paramHandler)) {
                vdiskInfo.attachVDisk(paramHandler);
                newState = FlowState.VSDISK_ATTACHING;
            }
            break;
        case VSDISK_ATTACHING:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSDISK_ATTACHED, paramHandler)) {
                if (vdiskInfo.isVDiskAttached(paramHandler)) {
                    newState = FlowState.VSDISK_ATTACHED;
                }
            }
            break;
        case VSDISK_ATTACHED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVER_STARTING, paramHandler)) {
                newState = FlowState.VSERVER_STARTING;
                vserverComm.startVServer(paramHandler);
            }
            break;
        default:
        }
        return newState;
    }

    FlowState dispatchVServerManualOperation(String controllerId,
            String instanceId, PropertyHandler paramHandler, String mail)
            throws Exception {
        String subscriptionId = paramHandler.getSettings()
                .getOriginalSubscriptionId();
        String locale = getTechnicalProviderLocale(controllerId, paramHandler);
        if (Operation.VSERVER_CREATION.equals(paramHandler.getOperation())) {
            if (checkNextStatus(controllerId, instanceId, FlowState.MANUAL,
                    paramHandler)) {
                StringBuffer eventLink = new StringBuffer(
                        platformService.getEventServiceUrl());
                eventLink.append("?sid=").append(
                        URLEncoder.encode(instanceId, "UTF-8"));
                eventLink.append("&controllerid=").append(controllerId);
                eventLink.append("&command=finish");
                String subject = Messages.get(locale,
                        "mail_VSERVER_manual_completion.subject", instanceId,
                        subscriptionId);
                String details = paramHandler.getConfigurationAsString();
                String text = Messages.get(locale,
                        "mail_VSERVER_manual_completion.text", instanceId,
                        subscriptionId, details, eventLink.toString());
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
                return FlowState.MANUAL;
            }
        } else if (Operation.VSERVER_MODIFICATION.equals(paramHandler
                .getOperation())) {
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                String subject = Messages.get(locale,
                        "mail_VSERVER_manual_modification.subject", instanceId,
                        subscriptionId);
                String details = paramHandler.getConfigurationAsString();
                String text = Messages.get(locale,
                        "mail_VSERVER_manual_modification.text", instanceId,
                        subscriptionId, details);
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
                return FlowState.FINISHED;
            }
        } else if (Operation.VSERVER_MODIFICATION_VDISK_CREATION
                .equals(paramHandler.getOperation())) {
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                String subject = Messages
                        .get(locale,
                                "mail_VSERVER_VDISK_CREATION_manual_modification.subject",
                                instanceId, subscriptionId);
                String details = paramHandler.getConfigurationAsString();
                String text = Messages.get(locale,
                        "mail_VSERVER_VDISK_CREATION_manual_modification.text",
                        instanceId, subscriptionId, details);
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
                return FlowState.FINISHED;
            }
        } else if (Operation.VSERVER_MODIFICATION_VDISK_DELETION
                .equals(paramHandler.getOperation())) {
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                String subject = Messages
                        .get(locale,
                                "mail_VSERVER_VDISK_DELETION_manual_modification.subject",
                                instanceId, subscriptionId);
                String details = paramHandler.getConfigurationAsString();
                String text = Messages.get(locale,
                        "mail_VSERVER_VDISK_DELETION_manual_modification.text",
                        instanceId, subscriptionId, details);
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
                return FlowState.FINISHED;
            }
        } else if (checkNextStatus(controllerId, instanceId,
                FlowState.FINISHED, paramHandler)) {
            return FlowState.FINISHED;
        }
        return null;
    }

    void validateParameters(PropertyHandler paramHandler) throws Exception {
        FlowState currentState = paramHandler.getState();
        FlowState[] VALIDABLE_STATES = { FlowState.VSERVER_CREATION_REQUESTED,
                FlowState.VSERVER_MODIFICATION_REQUESTED,
                FlowState.VSERVER_DELETION_REQUESTED };
        for (FlowState cflState : VALIDABLE_STATES) {
            if (cflState.equals(currentState)) {
                // validation of parameters for creation, modification, deletion
                logger.debug("VServerProcessor is validating parameters in state "
                        + currentState.name());
                if (VSystemStatus.ERROR.equals(paramHandler.getIaasContext()
                        .getVSystemStatus())) {
                    throw new SuspendException(Messages.getAll(
                            "error_vsys_notrunning",
                            new Object[] { paramHandler.getVsysId() }));
                }
                if (!vserverComm.isNetworkIdValid(paramHandler)) {
                    throw new SuspendException(Messages.getAll(
                            "error_invalid_networkid",
                            new Object[] { paramHandler.getNetworkId() }));
                }
                // validation of input parameters against the back end API
                if (!cflState.equals(FlowState.VSERVER_DELETION_REQUESTED)) {
                    if (!vserverComm.isVSysIdValid(paramHandler)) {
                        throw new SuspendException(Messages.getAll(
                                "error_invalid_sysid",
                                new Object[] { paramHandler.getVsysId() }));
                    }
                    if (isDiskImageIdValid(null, paramHandler) == null) {
                        throw new SuspendException(Messages.getAll(
                                "error_invalid_diskimageid",
                                new Object[] { paramHandler.getDiskImageId() }));
                    }
                    if (!vserverComm.isServerTypeValid(paramHandler)) {
                        throw new SuspendException(Messages.getAll(
                                "error_invalid_servertype",
                                new Object[] { paramHandler.getVserverType() }));
                    }
                }
                break;
            }
        }
    }

    public DiskImage isDiskImageIdValid(String diskImageId,
            PropertyHandler paramHandler) throws Exception {
        DiskImage diskImageForName = null;
        boolean diskImageIdUnique = false;
        String imageIdOrName = diskImageId != null ? diskImageId : paramHandler
                .getDiskImageId();
        List<DiskImage> diskImages = vsysComm.getDiskImages(paramHandler);
        for (DiskImage image : diskImages) {
            String imageId = image.getDiskImageId();
            String imageName = image.getDiskImageName();
            if (imageId.equalsIgnoreCase(imageIdOrName)) {
                return image;
            } else {
                if (imageName.equalsIgnoreCase(imageIdOrName)) {
                    if (diskImageForName == null) {
                        diskImageForName = image;
                        diskImageIdUnique = true;
                    } else {
                        diskImageIdUnique = false;
                    }
                }
            }
        }
        if (diskImageIdUnique) {
            logger.info(
                    "No disk image with referenced ID {} defined. Using disk image with same name and ID {}",
                    new Object[] { imageIdOrName,
                            diskImageForName.getDiskImageId() });
            return diskImageForName;
        }
        return null;
    }
}
