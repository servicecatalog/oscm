/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 26.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLStreamHandler;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.oscm.app.iaas.data.DiskImage;
import org.oscm.app.iaas.data.FWPolicy;
import org.oscm.app.iaas.data.FWPolicy.Action;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.NATRule;
import org.oscm.app.iaas.data.Network;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.data.ResourceType;
import org.oscm.app.iaas.data.VNIC;
import org.oscm.app.iaas.data.VServerConfiguration;
import org.oscm.app.iaas.data.VServerStatus;
import org.oscm.app.iaas.data.VSystemConfiguration;
import org.oscm.app.iaas.data.VSystemStatus;
import org.oscm.app.iaas.data.VSystemTemplateConfiguration;
import org.oscm.app.iaas.exceptions.IaasException;
import org.oscm.app.iaas.exceptions.MissingResourceException;
import org.oscm.app.iaas.exceptions.PolicyConfigurationException;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.iaas.intf.FWCommunication;
import org.oscm.app.iaas.intf.VServerCommunication;
import org.oscm.app.iaas.intf.VSystemCommunication;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;

/**
 * @author malhotra
 * 
 */
@Stateless
@LocalBean
public class VSystemProcessorBean extends BaseProvisioningProcessor {

    private static final Logger logger = LoggerFactory
            .getLogger(VSystemProcessorBean.class);

    private static URLStreamHandler streamHandler;

    /**
     * Sets the URL stream handler. <b>Should only be used for unit testing!</b>
     * 
     * @param streamHandler
     */
    public static void setURLStreamHandler(URLStreamHandler streamHandler) {
        VSystemProcessorBean.streamHandler = streamHandler;
    }

    @EJB(beanInterface = VSystemCommunication.class)
    protected VSystemCommunication vsysComm;

    @EJB(beanInterface = VServerCommunication.class)
    protected VServerCommunication vserverComm;

    @EJB(beanInterface = FWCommunication.class)
    protected FWCommunication fwComm;

    protected VServerProcessorBean serverProcessor;

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

    public void setDelegate(VServerProcessorBean delegate) {
        serverProcessor = delegate;
    }

    @Override
    public void process(String controllerId, String instanceId,
            PropertyHandler paramHandler) throws Exception {
        if (paramHandler.isVirtualSystemProvisioning()) {
            try {
                validateParameters(paramHandler);
                dispatch(controllerId, instanceId, paramHandler);
            } catch (MissingResourceException e) {
                ResourceType resourceType = e.getResouceType();
                if (ResourceType.VSYSTEM.equals(resourceType)
                        && Operation.VSYSTEM_DELETION.equals(paramHandler
                                .getOperation())
                        && paramHandler.getVsysId() != null
                        && paramHandler.getVsysId().equals(e.getResouceId())) {
                    logger.warn(
                            "Ignoring missing resource since virtual system {} is to be deleted anyway",
                            paramHandler.getVsysId());
                    // adapt internal knowledge about the instance an try to
                    // finalize the deletion process
                    paramHandler.setState(FlowState.VSYSTEM_DELETING);
                    paramHandler.setVsysId("");
                    dispatch(controllerId, instanceId, paramHandler);
                } else if (ResourceType.VSYSTEM.equals(resourceType)
                        && Operation.VSYSTEM_OPERATION.equals(paramHandler
                                .getOperation())) {
                    throw new InstanceNotAliveException(Messages.getAll(
                            "error_suspend_instance_error", e.getMessage()));
                } else {
                    throw e.getSuspendException();
                }
            }
        } else {
            throw new RuntimeException("Wrong processoer");
        }
    }

    /**
     * Dispatches next virtual system operation according to current
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
        logger.debug("Dispatching in VSystemProcessor with OperationState="
                + operationState + " and FlowState= " + flowState);
        FlowState newState = null;
        String vSystemState = VSystemStatus.NORMAL;
        if (paramHandler.getVsysId() != null
                && paramHandler.getVsysId().length() != 0) {
            vSystemState = vsysComm.getVSystemState(paramHandler);
            if (!VSystemStatus.NORMAL.equals(vSystemState)) {
                logger.debug(
                        "Virtual system is currently in state '{}'. This might cause waiting for NORMAL state.",
                        vSystemState);
            }
            if (VSystemStatus.ERROR.equals(vSystemState)) {
                newState = FlowState.FAILED;
                disableExclusiveProcessing(controllerId, instanceId,
                        paramHandler);
                return;
            }
        }
        paramHandler.getIaasContext().setVSystemStatus(vSystemState);
        // => Dispatch next operation
        switch (operationState) {
        case VSYSTEM_CREATION:
            // pass on to modification (basically same steps)
        case VSYSTEM_MODIFICATION:
            newState = manageModificationProcess(controllerId, instanceId,
                    paramHandler, flowState);
            break;
        case VSYSTEM_DELETION:
            newState = manageDeletionProcess(controllerId, instanceId,
                    paramHandler, flowState);
            break;
        case VSYSTEM_ACTIVATION:
            newState = manageActivationProcess(controllerId, instanceId,
                    paramHandler, flowState);
            break;
        case VSYSTEM_OPERATION:
            newState = manageOperations(controllerId, instanceId, paramHandler,
                    flowState);
            break;
        default:
        }
        if (newState != null) { // update changed flow state
            paramHandler.setState(newState);
            logger.debug("Dispatch in VSystemProcessor returns new FlowState="
                    + newState);
        } else {
            logger.debug("Dispatch in VSystemProcessor leaves FlowState unchanged");
        }
    }

    FlowState manageActivationProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState)
            throws Exception, APPlatformException {

        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());

        if (!vSysInNormalState) {
            return null;
        }

        FlowState newState = null;
        switch (flowState) {
        case VSYSTEM_ACTIVATION_REQUESTED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVERS_STARTING, paramHandler)) {
                // start only the servers that have previously been stopped by
                // deactivation
                vsysComm.startVServers(paramHandler);
                paramHandler.setVserverToBeStarted(new ArrayList<String>());
                newState = FlowState.FINISHED;
            }
            break;
        case VSYSTEM_DEACTIVATION_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVERS_STOPPING, paramHandler)) {
                List<String> stoppedServers = vsysComm
                        .stopAllVServers(paramHandler);
                paramHandler.setVserverToBeStarted(stoppedServers);
                newState = FlowState.VSERVERS_STOPPING;
            }
            break;
        case VSERVERS_STOPPING:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                if (vsysComm.getCombinedVServerState(paramHandler,
                        VSystemStatus.STOPPED)) {
                    newState = FlowState.FINISHED;
                }
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageOperations(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState)
            throws Exception, APPlatformException {

        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());

        if (!vSysInNormalState) {
            return null;
        }

        FlowState newState = null;
        switch (flowState) {
        case VSYSTEM_START_REQUESTED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVERS_STARTING, paramHandler)) {
                if (vsysComm.startAllEFMs(paramHandler)) {
                    vsysComm.startAllVServers(paramHandler);
                    newState = FlowState.VSERVERS_STARTING;
                }
            }
            break;
        case VSERVERS_STARTING:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                if (vsysComm.getCombinedVServerState(paramHandler,
                        VSystemStatus.RUNNING)) {
                    newState = FlowState.FINISHED;
                }
            }
            break;
        case VSYSTEM_STOP_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSERVERS_STOPPING, paramHandler)) {
                vsysComm.stopAllVServers(paramHandler);
                newState = FlowState.VSERVERS_STOPPING;
            }
            break;
        case VSERVERS_STOPPING:
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                if (vsysComm.getCombinedVServerState(paramHandler,
                        VSystemStatus.STOPPED)) {
                    newState = FlowState.FINISHED;
                }
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageModificationProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState)
            throws Exception, APPlatformException {

        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());

        FlowState newState = null;
        FlowState targetState = null; // temporary logic state
        boolean skipUpdateFW = false;
        boolean skipWaitForUpdateFW = false;
        switch (flowState) {
        case VSYSTEM_CREATION_REQUESTED:
            if (checkNextStatus(controllerId, instanceId,
                    FlowState.VSYSTEM_CREATING, paramHandler)) {
                vsysComm.createVSystem(paramHandler);
                newState = FlowState.VSYSTEM_CREATING;
            }
            break;
        case VSYSTEM_CREATING:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VSERVERS_STARTING, paramHandler)) {
                if (vsysComm.startAllEFMs(paramHandler)) {
                    vsysComm.startAllVServers(paramHandler);
                    newState = FlowState.VSERVERS_STARTING;
                }
            }
            break;
        case VSERVERS_STARTING:
            if (vsysComm.getCombinedVServerState(paramHandler,
                    VServerStatus.RUNNING)) {
                targetState = checkConfiguredServers(controllerId, instanceId,
                        paramHandler);
                if (targetState == null) {
                    targetState = checkNetworkSettings(paramHandler);
                }
                if (targetState == null) {
                    targetState = determineScalingAndSizing(paramHandler);
                }
                if (checkNextStatus(controllerId, instanceId, targetState,
                        paramHandler)) {
                    newState = targetState;
                }
            }
            break;
        case VSYSTEM_MODIFICATION_REQUESTED:
        case VSYSTEM_SUBPROCESS_SERVERS:
            targetState = checkConfiguredServers(controllerId, instanceId,
                    paramHandler);
            skipWaitForUpdateFW = true;
        case VSYSTEM_UPDATE_FW_WAITING:
            if (!skipWaitForUpdateFW) {
                if (!vSysInNormalState) {
                    break;
                }
                skipUpdateFW = true;
            }
        case VSYSTEM_UPDATE_FW:
            if (targetState == null && !skipUpdateFW) {
                targetState = checkNetworkSettings(paramHandler);
            }
            if (targetState == null) {
                targetState = determineScalingAndSizing(paramHandler);
            }
            if (checkNextStatus(controllerId, instanceId, targetState,
                    paramHandler)) {
                newState = targetState;
            }
            break;
        case VSYSTEM_RETRIEVEGUEST:
            String mail = paramHandler.getMailForCompletion();
            if (mail != null) {
                newState = dispatchVSystemManualOperation(controllerId,
                        instanceId, paramHandler, mail);
            } else if (checkNextStatus(controllerId, instanceId,
                    FlowState.FINISHED, paramHandler)) {
                newState = FlowState.FINISHED;
            }
            break;
        default:
        }
        newState = manageScaling(controllerId, instanceId, paramHandler,
                flowState, newState);
        if (FlowState.FINISHED.equals(newState)) {
            paramHandler.resetTouchedVservers();
        }
        return newState;
    }

    private FlowState checkConfiguredServers(String controllerId,
            String instanceId, PropertyHandler paramHandler) throws Exception {
        SubPropertyHandler[] vservers = paramHandler.getVserverList();
        for (int i = 0; i < vservers.length; i++) {
            Set<String> touched = paramHandler.getVserversTouched();
            SubPropertyHandler nextServer = vservers[i];
            if (nextServer.isEnabled()
                    && (Operation.UNKNOWN.equals(nextServer.getOperation()) || FlowState.DESTROYED
                            .equals(nextServer.getState()))) {
                nextServer.setOperation(Operation.VSERVER_CREATION);
                nextServer.setState(FlowState.VSERVER_CREATION_REQUESTED);
                // FIXME parameter check like in ProcessManagerBean?
            } else {
                logger.debug("Touched servers are: " + touched);
                if (nextServer.getVserverIdIfPresent() != null) {
                    if (nextServer.isEnabled()) {
                        if (Operation.VSYSTEM_MODIFICATION.equals(paramHandler
                                .getOperation())
                                && !isActiveState(nextServer.getState())
                                && !touched.contains(nextServer.getVserverId())) {
                            logger.debug("Marking server {} for modification",
                                    nextServer.getVserverId());
                            nextServer
                                    .setOperation(Operation.VSERVER_MODIFICATION);
                            nextServer
                                    .setState(FlowState.VSERVER_MODIFICATION_REQUESTED);
                            paramHandler.addTouchedVserver(nextServer
                                    .getVserverId());
                        }
                    } else {
                        if (!Operation.VSERVER_DELETION.equals(nextServer
                                .getOperation())) {
                            logger.debug("Marking server {} for deletion",
                                    nextServer.getVserverId());
                            nextServer.setOperation(Operation.VSERVER_DELETION);
                            nextServer
                                    .setState(FlowState.VSERVER_DELETION_REQUESTED);
                        }
                    }
                }
            }
            if (isActiveState(nextServer.getState())) {
                logger.debug("Delegating handling of server to VServerProcessor");
                serverProcessor.process(controllerId, instanceId, nextServer);
                if (nextServer.getVserverIdIfPresent() != null) {
                    paramHandler.addTouchedVserver(nextServer.getVserverId());
                }
                if (isActiveState(nextServer.getState())) {
                    return FlowState.VSYSTEM_SUBPROCESS_SERVERS;
                }
            }
        }
        return null;
    }

    private FlowState checkNetworkSettings(PropertyHandler ph) throws Exception {
        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(ph
                .getIaasContext().getVSystemStatus());
        Set<FWPolicy> policies = new HashSet<FWPolicy>(ph.getFirewallPolicies());
        SubPropertyHandler[] vsList = ph.getVserverList();
        for (SubPropertyHandler subPh : vsList) {
            policies.addAll(subPh.getFirewallPolicies());
        }

        // check whether firewall rules are configured or managed elements are
        // still present
        if (policies.isEmpty() && ph.getManagedFirewallPolicies().isEmpty()
                && ph.getManagedPublicIPs().isEmpty()) {
            return null; // nothing to do
        }

        if (!vSysInNormalState) {
            return FlowState.VSYSTEM_UPDATE_FW;
        }

        HashMap<String, String> hostToZone_PUBLIC = new HashMap<String, String>();
        HashMap<String, String> hostToPrivate_PUBLIC = new HashMap<String, String>();
        HashMap<String, String> hostToZone_PRIVATE = new HashMap<String, String>();
        HashMap<String, String> hostToPrivate_PRIVATE = new HashMap<String, String>();
        HashMap<String, String> privateToPublic = new HashMap<String, String>();

        // divide FW rules by use of public/private IPs
        // this is required for proper replacement of names
        Set<FWPolicy> ruleToPublic = new HashSet<FWPolicy>();
        Set<FWPolicy> ruleToPrivate = new HashSet<FWPolicy>();
        Set<FWPolicy> ruleFromPrivate = new HashSet<FWPolicy>();

        // Zones that have FW rules to INTERNET defined
        Set<String> snaptZones = new HashSet<String>();

        // in case only outgoing rules are defined we need one IP for SNAPT
        String sparePrivateIP = null;

        boolean snaptRequired = false;
        boolean dnatRequired = false;
        boolean snaptPresent = false;
        boolean updateNatRequired = false;
        for (FWPolicy policy : policies) {
            if (policy.getAction() == null) {
                policy.setAction(Action.Accept);
            }
            if (FWPolicy.ZONE_INTERNET.equals(policy.getSrcZone())) {
                if (policy.getDst() == null) {
                    // TODO NLS
                    throw new PolicyConfigurationException(
                            "Incoming internet rule requires defined destination server name!");
                }
                addHostToZone(hostToZone_PUBLIC, hostToZone_PRIVATE,
                        policy.getDst(), policy.getDstZone());
                ruleToPublic.add(policy); // public IP in "to" part
                snaptRequired = true;
                dnatRequired = true;
            } else if (FWPolicy.ZONE_INTERNET.equals(policy.getDstZone())) {
                snaptRequired = true;
                snaptZones.add(policy.getSrcZone());
                if (policy.getSrc() != null) {
                    addHostToZone(hostToZone_PRIVATE, hostToZone_PUBLIC,
                            policy.getSrc(), policy.getSrcZone());
                    ruleFromPrivate.add(policy); // private IP in "from" part
                }
            } else {
                // internal policy (no INTERNET) => resolve everything that is
                // not INTRANET
                if (!FWPolicy.ZONE_INTRANET.equals(policy.getSrcZone())
                        && policy.getSrc() != null) {
                    addHostToZone(hostToZone_PRIVATE, hostToZone_PUBLIC,
                            policy.getSrc(), policy.getSrcZone());
                    ruleFromPrivate.add(policy); // private IP in "from" part
                }
                if (!FWPolicy.ZONE_INTRANET.equals(policy.getDstZone())
                        && policy.getDst() != null) {
                    addHostToZone(hostToZone_PRIVATE, hostToZone_PUBLIC,

                    policy.getDst(), policy.getDstZone());
                    ruleToPrivate.add(policy); // private IP in "to" part
                }
            }
        }
        VSystemConfiguration vsysConf = vsysComm.getConfiguration(ph);
        String networkPrefix = vsysConf.getNetworkPrefix();

        for (VServerConfiguration vservConf : vsysConf.getVServers()) {
            String zone_PUBLIC = hostToZone_PUBLIC.get(vservConf
                    .getServerName());
            String zone_PRIVATE = hostToZone_PRIVATE.get(vservConf
                    .getServerName());
            for (VNIC nic : vservConf.getVirtualNICs()) {
                String networkId = nic.getNetworkId();
                if (networkId != null) {
                    if (networkId.startsWith(networkPrefix)) {
                        networkId = networkId.substring(networkPrefix.length());
                    }
                    if (networkId.equals(zone_PUBLIC)) {
                        hostToPrivate_PUBLIC.put(vservConf.getServerName(),
                                nic.getPrivateIP());
                    }
                    if (networkId.equals(zone_PRIVATE)) {
                        hostToPrivate_PRIVATE.put(vservConf.getServerName(),
                                nic.getPrivateIP());
                    }
                    if (sparePrivateIP == null
                            && snaptZones.contains(networkId)
                            && !vservConf.isEFM()) {
                        sparePrivateIP = nic.getPrivateIP();
                    }
                }
            }
        }
        Set<String> unassignedPrivateIPs = new HashSet<String>(
                hostToPrivate_PUBLIC.values());
        Set<String> availablePublicIPs = new HashSet<String>(
                vsysComm.getPublicIps(ph));

        // first add all public IPs, remove available ones later
        Set<String> disabledPublicIPs = new HashSet<String>(availablePublicIPs);
        // public IPs that are no longer assigned
        Set<String> freedPublicIPs = new HashSet<String>();
        Set<NATRule> oldNatRules = fwComm.getNATSetting(ph);
        Set<NATRule> newNatRules = new HashSet<NATRule>();
        // store rules that have only the public IP set
        Set<NATRule> publicOnlyRules = new HashSet<NATRule>();
        logger.debug((oldNatRules.size() > 0) ? "Existing NAT rules: "
                : "No existing NAT rules");
        Set<String> ruleBasedPrivateIPs = new HashSet<String>(
                hostToPrivate_PUBLIC.values());
        Set<String> managedPublicIPs = new HashSet<String>(
                ph.getManagedPublicIPs());
        for (NATRule rule : oldNatRules) {
            logger.debug("    " + rule.toString());
            if (rule.getInternalIP() != null) {
                unassignedPrivateIPs.remove(rule.getInternalIP());
                if (ruleBasedPrivateIPs.contains(rule.getInternalIP())) {
                    availablePublicIPs.remove(rule.getExternalIP());
                    privateToPublic.put(rule.getInternalIP(),
                            rule.getExternalIP());
                } else if (managedPublicIPs.contains(rule.getExternalIP())) {
                    if (snaptRequired && !dnatRequired && rule.isSnapt()) {
                        logger.debug(rule + " is required for outgoing traffic");
                    } else {
                        // a NATRule managed by us is no longer backed by an
                        // according FW policy
                        rule.setInternalIP(null);
                        rule.setSnapt(false);
                        publicOnlyRules.add(rule);
                        freedPublicIPs.add(rule.getExternalIP());
                        logger.debug("  => rule no longer required");
                    }
                }
                if (rule.isSnapt()) {
                    snaptPresent = true;
                }
            } else {
                // this rule is not actually active and should be ignored in
                // further processing
                publicOnlyRules.add(rule);
            }
            disabledPublicIPs.remove(rule.getExternalIP());
        }
        // do not handle rules that are for information only
        oldNatRules.removeAll(publicOnlyRules);

        logger.debug("Checking network settings:");
        logger.debug("  Unassigned private IPs: " + unassignedPrivateIPs);
        logger.debug("  Available public IPs: " + availablePublicIPs);
        if (unassignedPrivateIPs.size() > availablePublicIPs.size()) {
            if (vSysInNormalState) {
                logger.debug("  => Allocating new public IP");
                vsysComm.allocatePublicIP(ph);
            }
            return FlowState.VSYSTEM_UPDATE_FW;
        }

        while (unassignedPrivateIPs.size() > 0 && availablePublicIPs.size() > 0) {
            NATRule newRule = new NATRule();
            newRule.setExternalIP(availablePublicIPs.iterator().next());
            availablePublicIPs.remove(newRule.getExternalIP());
            newRule.setInternalIP(unassignedPrivateIPs.iterator().next());
            unassignedPrivateIPs.remove(newRule.getInternalIP());
            if (!snaptPresent) {
                newRule.setSnapt(true);
                snaptPresent = true;
            }
            privateToPublic.put(newRule.getInternalIP(),
                    newRule.getExternalIP());
            managedPublicIPs.add(newRule.getExternalIP());
            newNatRules.add(newRule);
            logger.debug("  Adding rule " + newRule);
            updateNatRequired = true;
        }
        if (snaptRequired && !snaptPresent) {
            // TODO check whether simply taking the first is sufficient
            if (oldNatRules.size() > 0) {
                NATRule natRule = oldNatRules.iterator().next();
                natRule.setSnapt(true);
                logger.debug("SNAPT required. Activating it on arbitrary rule "
                        + natRule);
                newNatRules.add(natRule);
            } else {
                if (sparePrivateIP == null) {
                    // TODO NLS
                    throw new SuspendException(
                            "Outgoing FW rules defined, but no server is available to be assigned in NAT rule");
                }
                logger.debug("Creating new NAT rule on arbitrary private IP to allow outgoing traffic: ");
                if (availablePublicIPs.isEmpty()) {
                    if (vSysInNormalState) {
                        logger.debug("  => Allocating new public IP");
                        vsysComm.allocatePublicIP(ph);
                    }
                    return FlowState.VSYSTEM_UPDATE_FW;
                }
                NATRule newRule = new NATRule();
                newRule.setExternalIP(availablePublicIPs.iterator().next());
                availablePublicIPs.remove(newRule.getExternalIP());
                newRule.setInternalIP(sparePrivateIP);
                newRule.setSnapt(true);
                privateToPublic.put(newRule.getInternalIP(),
                        newRule.getExternalIP());
                managedPublicIPs.add(newRule.getExternalIP());
                newNatRules.add(newRule);
                logger.debug("  Adding rule " + newRule);
            }
            updateNatRequired = true;
        }
        // generate remove rules for all freed IPs
        HashSet<String> unusedPublicIPs = new HashSet<String>(
                availablePublicIPs);
        unusedPublicIPs.retainAll(managedPublicIPs);
        freedPublicIPs.addAll(unusedPublicIPs);
        if (freedPublicIPs.size() > 0) {
            if (!vSysInNormalState
                    || vsysComm.freePublicIPs(ph, freedPublicIPs) != null) {
                return FlowState.VSYSTEM_UPDATE_FW;
            }
            for (String freedIP : freedPublicIPs) {
                NATRule freeRule = new NATRule();
                freeRule.setExternalIP(freedIP);
                newNatRules.add(freeRule);
                managedPublicIPs.remove(freedIP);
                updateNatRequired = true;
            }
        }
        if (updateNatRequired) {

            if (!vSysInNormalState) {
                return FlowState.VSYSTEM_UPDATE_FW;
            }

            // no need to activate IPs that are not used in any NAT rules
            disabledPublicIPs.removeAll(availablePublicIPs);
            if (disabledPublicIPs.size() > 0) {
                logger.debug("  => Activating public IPs: " + disabledPublicIPs);
                vsysComm.activatePublicIPs(ph, disabledPublicIPs);
            }
            logger.debug("  => updating NAT settings");
            fwComm.updateNATSetting(ph, newNatRules);
            ph.setManagedPublicIPs(managedPublicIPs);
            return FlowState.VSYSTEM_UPDATE_FW;
        }
        // replace host names in policies
        if (policies.size() > 0) {
            for (FWPolicy policy : ruleToPublic) {
                String srvName = policy.getDst();
                String privIP = hostToPrivate_PUBLIC.get(srvName.trim());
                if (privIP == null) {
                    throw new PolicyConfigurationException(
                            "Server name cannot be matched to private IP: "
                                    + srvName);
                }
                String pubIP = privateToPublic.get(privIP.trim());
                if (pubIP == null) {
                    throw new PolicyConfigurationException(
                            "Private IP cannot be mapped to public IP: "
                                    + privIP);
                }
                policy.setDst(pubIP);
            }
            for (FWPolicy policy : ruleToPrivate) {
                String srvName = policy.getDst();
                String privIP = hostToPrivate_PRIVATE.get(srvName.trim());
                if (privIP == null) {
                    throw new PolicyConfigurationException(
                            "Server name cannot be matched to private IP: "
                                    + srvName);
                }
                policy.setDst(privIP);
            }
            for (FWPolicy policy : ruleFromPrivate) {
                String srvName = policy.getSrc();
                String privIP = hostToPrivate_PRIVATE.get(srvName.trim());
                if (privIP == null) {
                    throw new PolicyConfigurationException(
                            "Server name cannot be matched to private IP: "
                                    + srvName);
                }
                policy.setSrc(privIP);
            }
            Set<String> managedPolicies = fwComm.updateFirewallSetting(ph,
                    policies, ph.getManagedFirewallPolicies());
            ph.setManagedFirewallPolicies(managedPolicies);
            return FlowState.VSYSTEM_UPDATE_FW_WAITING;
        }
        return null;
    }

    private boolean isActiveState(FlowState state) {
        return !(FlowState.DESTROYED.equals(state)
                || FlowState.FINISHED.equals(state) || FlowState.FAILED
                    .equals(state));
    }

    FlowState determineScalingAndSizing(PropertyHandler paramHandler)
            throws Exception {
        int slaveClusterSize = 0;
        int slaveServersPresent = 0;
        if (paramHandler.isClusterDefined()) {
            slaveClusterSize = getSlaveClusterSize(paramHandler);
            List<String> serverIds = vsysComm.getVServersForTemplate(
                    paramHandler.getSlaveTemplateId(), paramHandler);
            slaveServersPresent = serverIds.size();
        }
        if (slaveClusterSize > slaveServersPresent) {
            return FlowState.VSYSTEM_SCALE_UP;
        }

        if (slaveClusterSize < slaveServersPresent) {
            return FlowState.VSYSTEM_SCALE_DOWN;
        }

        if (isResizingRequired(paramHandler) != null) {
            return FlowState.VSYSTEM_RESIZE_VSERVERS;
        }
        return FlowState.VSYSTEM_SCALING_COMPLETED;
    }

    /**
     * Compare current and required sizing information of affected VMs. Returns
     * the server ID of the first server that is to be changed or
     * <code>null</code> if no server is to be changed.
     */
    String isResizingRequired(PropertyHandler paramHandler) throws Exception {
        String countCPU = paramHandler.getCountCPU();
        try {
            Integer.valueOf(countCPU);
        } catch (NumberFormatException e) {
            return null;
        }
        // only apply to VMs that are from respective templates
        String masterTemplateId = paramHandler.getMasterTemplateId();
        String slaveTemplateId = paramHandler.getSlaveTemplateId();
        VSystemConfiguration configuration = getVSystemConfiguration(paramHandler);
        for (VServerConfiguration server : configuration.getVServers()) {
            String existingCPU = server.getNumOfCPU();
            String diskImageId = server.getDiskImageId();
            if (existingCPU != null && !existingCPU.equals(countCPU)) {
                if (diskImageId != null
                        && (diskImageId.equals(masterTemplateId) || diskImageId
                                .equals(slaveTemplateId))) {
                    logger.debug("Server " + server.getServerId()
                            + " to be resized to CPU# " + countCPU);
                    SubPropertyHandler subPropertyHandler = paramHandler
                            .getTemporaryVserver(server);
                    subPropertyHandler.setCountCPU(countCPU);
                    subPropertyHandler
                            .setState(FlowState.VSERVER_MODIFICATION_REQUESTED);
                    subPropertyHandler
                            .setOperation(Operation.VSERVER_MODIFICATION);
                    paramHandler.setState(FlowState.VSYSTEM_RESIZE_VSERVERS);
                    return server.getServerId();
                }
            }
        }
        return null;
    }

    FlowState manageDeletionProcess(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState)
            throws APPlatformException, Exception {

        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());

        FlowState newState = null;
        switch (flowState) {
        case VSYSTEM_DELETION_REQUESTED:
            if (paramHandler.getControllerWaitTime() != 0) {
                paramHandler.suspendProcessInstanceFor(paramHandler
                        .getControllerWaitTime());
                newState = FlowState.WAITING_BEFORE_STOP;
                break;
            }
        case WAITING_BEFORE_STOP:
            if (vSysInNormalState
                    && checkNextStatus(controllerId, instanceId,
                            FlowState.VNET_DELETING, paramHandler)) {
                vsysComm.freePublicIPs(paramHandler, null);
                newState = FlowState.VNET_DELETING;
            }
            break;
        case VNET_DELETING:
            if (vSysInNormalState) {
                String ipState = vsysComm.freePublicIPs(paramHandler, null);
                if (ipState == null) { // null means everything is deleted
                    if (checkNextStatus(controllerId, instanceId,
                            FlowState.VSERVERS_STOPPING, paramHandler)) {
                        try {
                            vsysComm.stopAllVServers(paramHandler);
                            newState = FlowState.VSERVERS_STOPPING;
                        } catch (Exception e) {
                            if (!(e instanceof IaasException)
                                    || !((IaasException) e).isIllegalState()) {
                                throw e;
                            }
                        }
                    }
                }
            }
            break;
        case VSERVERS_STOPPING:
            if (vsysComm.getCombinedVServerState(paramHandler,
                    VSystemStatus.STOPPED)) {
                if (checkNextStatus(controllerId, instanceId,
                        FlowState.VSYSTEM_DELETING, paramHandler)) {
                    vsysComm.destroyVSystem(paramHandler);
                    newState = FlowState.VSYSTEM_DELETING;
                    paramHandler.setVsysId("");
                }
            }
            break;
        case VSYSTEM_DELETING:
            String mail = paramHandler.getMailForCompletion();
            if (mail != null) {
                newState = dispatchVSystemManualOperation(controllerId,
                        instanceId, paramHandler, mail);
            } else if (checkNextStatus(controllerId, instanceId,
                    FlowState.DESTROYED, paramHandler)) {
                newState = FlowState.DESTROYED;
            }
            break;
        default:
        }
        return newState;
    }

    FlowState manageScaling(String controllerId, String instanceId,
            PropertyHandler paramHandler, FlowState flowState,
            FlowState newState) throws APPlatformException, Exception {

        logger.debug("Manage scaling in VSystemProcessor with FlowState="
                + flowState + " and NewState=" + newState);

        boolean vSysInNormalState = VSystemStatus.NORMAL.equals(paramHandler
                .getIaasContext().getVSystemStatus());
        try {
            String masterTemplateId = paramHandler.getMasterTemplateId();
            String slaveTemplateId = paramHandler.getSlaveTemplateId();
            switch (flowState) {
            case VSYSTEM_SCALE_UP:
                if (vSysInNormalState) {
                    newState = scaleUp(paramHandler, newState,
                            masterTemplateId, slaveTemplateId,
                            vSysInNormalState);
                }
                break;
            case VSYSTEM_SCALE_UP_WAIT_BEFORE_NOTIFICATION:
                if (vSysInNormalState) {
                    paramHandler.suspendProcessInstanceFor(120);
                    newState = FlowState.VSYSTEM_SCALE_UP_NOTIFY_ADMIN_AGENT;
                }
                break;
            case VSYSTEM_SCALE_UP_NOTIFY_ADMIN_AGENT:
                if (vSysInNormalState) {
                    // inform the admin agent to add new system to the cluster
                    notifyAdminAgent(true, paramHandler);
                    // return to scale up base state to check whether further
                    // steps are required
                    newState = FlowState.VSYSTEM_SCALE_UP;
                }
                break;
            case VSYSTEM_SCALE_DOWN:
                newState = scaleDown(paramHandler, newState, slaveTemplateId,
                        vSysInNormalState);
                break;
            case VSYSTEM_SCALE_DOWN_STOP_SERVER:
                if (paramHandler.getControllerWaitTime() != 0) {
                    if (vSysInNormalState) {
                        paramHandler.suspendProcessInstanceFor(paramHandler
                                .getControllerWaitTime());
                        newState = FlowState.WAITING_BEFORE_STOP;
                        break;
                    }
                }
            case WAITING_BEFORE_STOP:
                if (vSysInNormalState) {
                    String status = vserverComm.getVServerStatus(paramHandler);
                    if (VServerStatus.STARTING.equals(status)) {
                        // this case is rare but cannot be excluded for sure
                        paramHandler.suspendProcessInstanceFor(30);
                    } else {
                        if (VServerStatus.RUNNING.equals(status)) {
                            vserverComm.stopVServer(paramHandler);
                        }
                        newState = FlowState.VSYSTEM_SCALE_DOWN_DESTROY_SERVER;
                    }
                }
                break;
            case VSYSTEM_SCALE_DOWN_DESTROY_SERVER:
                if (vSysInNormalState) {
                    String status = vserverComm
                            .getNonErrorVServerStatus(paramHandler);
                    if (VServerStatus.STOPPED.equals(status)) {
                        vserverComm.destroyVServer(paramHandler);
                        // finally check for more resources to be scaled down
                        newState = FlowState.VSYSTEM_SCALE_DOWN;
                    }
                }
                break;
            case VSERVERS_STARTING:
                if (vSysInNormalState
                        && checkNextStatus(controllerId, instanceId,
                                FlowState.VSYSTEM_SCALING_COMPLETED,
                                paramHandler)) {
                    if (vsysComm.getCombinedVServerState(paramHandler,
                            VSystemStatus.STOPPED)) {
                        if (vsysComm.startAllEFMs(paramHandler)) {
                            vsysComm.startAllVServers(paramHandler);
                            newState = FlowState.VSYSTEM_SCALING_COMPLETED;
                        }
                    }
                }
                break;
            case VSYSTEM_RESIZE_VSERVERS:
                if (vSysInNormalState) {
                    SubPropertyHandler serverProperties = paramHandler
                            .getTemporaryVserver(null);
                    if (!FlowState.FINISHED.equals(serverProperties.getState())) {
                        serverProcessor.process(controllerId, instanceId,
                                serverProperties);
                    }
                    if (FlowState.FINISHED.equals(serverProperties.getState())) {
                        paramHandler.removeTemporaryVserver();
                        String serverId = isResizingRequired(paramHandler);
                        if (serverId == null) {
                            newState = determineScalingAndSizing(paramHandler);
                        }
                    }
                }
                break;
            case VSYSTEM_SCALING_COMPLETED:
                if (vSysInNormalState) {
                    if (checkNextStatus(controllerId, instanceId,
                            FlowState.VSYSTEM_RETRIEVEGUEST, paramHandler)) {
                        newState = FlowState.VSYSTEM_RETRIEVEGUEST;
                    }
                }
                break;
            default:
            }
        } finally {
            logger.debug("Manage scaling returns NewState=" + newState);
        }
        return newState;
    }

    FlowState scaleUp(PropertyHandler paramHandler, FlowState newStatus,
            String masterTemplateId, String slaveTemplateId,
            boolean vSysInNormalState) throws Exception {

        try {
            int slaveClusterSize = getSlaveClusterSize(paramHandler);
            List<String> serverIds = vsysComm.getVServersForTemplate(
                    slaveTemplateId, paramHandler);
            if (slaveClusterSize > serverIds.size()) {
                // scale up the virtual system
                if (vSysInNormalState) {
                    String vServerId = vsysComm.scaleUp(masterTemplateId,
                            slaveTemplateId, paramHandler);
                    // store id of server to be able to communicate to admin
                    // agent
                    paramHandler.setVserverId(vServerId);
                    String restURL = paramHandler.getAdminRestURL();
                    if (restURL == null) {
                        return FlowState.VSYSTEM_SCALE_UP; // no agent => no
                                                           // call and no wait
                    }
                    return FlowState.VSYSTEM_SCALE_UP_WAIT_BEFORE_NOTIFICATION;
                }
            }
            if (slaveClusterSize == serverIds.size()) {
                // actual scaling completed, check whether re-sizing is still
                // necessary
                newStatus = determineScalingAndSizing(paramHandler);
            }
            logger.debug("newStatus=" + newStatus);
        } catch (Exception e) {
            logger.error("Problem while scaling up:", e);
            throw e;
        }
        return newStatus;
    }

    FlowState scaleDown(PropertyHandler paramHandler, FlowState newStatus,
            String slaveTemplateId, boolean vsysInNormalState)
            throws Exception, APPlatformException {

        int slaveClusterSize = getSlaveClusterSize(paramHandler);
        List<String> serverIds = vsysComm.getVServersForTemplate(
                slaveTemplateId, paramHandler);
        if (slaveClusterSize < serverIds.size() && serverIds.size() > 0) {
            java.util.Collections.sort(serverIds, Collator.getInstance());
            paramHandler.setVserverId(serverIds.get(serverIds.size() - 1));
            if (vsysInNormalState) {
                // inform the admin agent to remove from cluster
                notifyAdminAgent(false, paramHandler);
                // synchronous execution is assumed here
                // if no error occurred, we can stop the server now
                newStatus = FlowState.VSYSTEM_SCALE_DOWN_STOP_SERVER;
            }
        } else if (slaveClusterSize == serverIds.size()) {
            // actual scaling completed, check whether re-sizing is still
            // necessary
            newStatus = determineScalingAndSizing(paramHandler);
        }
        logger.debug("newStatus=" + newStatus);
        return newStatus;
    }

    FlowState dispatchVSystemManualOperation(String controllerId,
            String instanceId, PropertyHandler paramHandler, String mail)
            throws Exception {
        String subscriptionId = paramHandler.getSettings()
                .getOriginalSubscriptionId();
        String locale = getTechnicalProviderLocale(controllerId, paramHandler);
        if (Operation.VSYSTEM_CREATION.equals(paramHandler.getOperation())) {
            if (checkNextStatus(controllerId, instanceId, FlowState.MANUAL,
                    paramHandler)) {
                StringBuffer eventLink = new StringBuffer(
                        platformService.getEventServiceUrl());
                eventLink.append("?sid=").append(
                        URLEncoder.encode(instanceId, "UTF-8"));
                eventLink.append("&controllerid=").append(controllerId);
                eventLink.append("&command=finish");
                String subject = Messages.get(locale,
                        "mail_vsystem_manual_completion.subject", new Object[] {
                                instanceId, subscriptionId });
                String details = paramHandler.getVSystemConfigurationAsString();
                String text = Messages.get(locale,
                        "mail_vsystem_manual_completion.text",
                        new Object[] { instanceId, subscriptionId, details,
                                eventLink.toString() });
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
                return FlowState.MANUAL;
            }
        } else if (Operation.VSYSTEM_MODIFICATION.equals(paramHandler
                .getOperation())) {
            if (checkNextStatus(controllerId, instanceId, FlowState.FINISHED,
                    paramHandler)) {
                String subject = Messages.get(locale,
                        "mail_vsystem_manual_modification.subject",
                        new Object[] { instanceId, subscriptionId });
                String details = paramHandler.getVSystemConfigurationAsString();
                String text = Messages.get(locale,
                        "mail_vsystem_manual_modification.text", new Object[] {
                                instanceId, subscriptionId, details });
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
                return FlowState.FINISHED;
            }
        } else if (Operation.VSYSTEM_DELETION.equals(paramHandler
                .getOperation())) {
            if (checkNextStatus(controllerId, instanceId, FlowState.DESTROYED,
                    paramHandler)) {
                String subject = Messages.get(locale,
                        "mail_vsystem_manual_disposal.subject", new Object[] {
                                instanceId, subscriptionId });
                String text = Messages.get(locale,
                        "mail_vsystem_manual_disposal.text", new Object[] {
                                instanceId, subscriptionId });
                platformService.sendMail(Collections.singletonList(mail),
                        subject, text);
                return FlowState.DESTROYED;
            }
        } else if (checkNextStatus(controllerId, instanceId,
                FlowState.FINISHED, paramHandler)) {
            return FlowState.FINISHED;
        }
        return null;
    }

    void notifyAdminAgent(boolean scaleUp, PropertyHandler paramHandler)
            throws Exception {

        String restURL = paramHandler.getAdminRestURL();
        if (restURL == null || restURL.trim().length() == 0) {
            return; // no agent => no call
        }
        String vsysId = paramHandler.getVsysId();
        String masterTemplateId = paramHandler.getMasterTemplateId();
        String slaveServerId = paramHandler.getVserverId();
        List<String> serverIds = vsysComm.getVServersForTemplate(
                masterTemplateId, paramHandler);

        if (serverIds == null || serverIds.isEmpty()) {
            throw new SuspendException(Messages.getAll(
                    "error_vsys_no_master_instance_available",
                    new Object[] { vsysId }));
        }
        if (serverIds.size() > 1) {
            logger.debug("Virtual system " + vsysId
                    + " contains more than one instance of image "
                    + masterTemplateId
                    + ". Using the first instance to communicate.");
        }
        String masterServerId = serverIds.get(0);
        String masterIp = null;
        try {
            paramHandler.setVserverId(masterServerId);
            masterIp = vserverComm.getInternalIp(paramHandler);
        } finally {
            // reset anyway
            paramHandler.setVserverId(slaveServerId);
        }
        String slaveIp = vserverComm.getInternalIp(paramHandler);
        restURL = restURL.replaceAll("\\{MASTER_IP\\}", masterIp);
        restURL = restURL.replaceAll("\\{SLAVE_IP\\}", slaveIp);

        URL url = new URL(null, restURL, streamHandler);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(scaleUp ? "PUT" : "DELETE");
        // sync processing => wait up to two minutes
        connection.setReadTimeout(120000);
        try {
            logger.debug("Sending " + connection.getRequestMethod()
                    + " to REST-API " + restURL);
            connection.connect();
            logger.debug("  ResponseCode was " + connection.getResponseCode());
        } catch (Exception e) {
            logger.error("REST-API failed with message: " + e.getMessage());
            throw e;
        } finally { // clean up
            try {
                connection.disconnect();
            } catch (Exception e) {
                // best effort pattern
            }
        }
    }

    void validateParameters(PropertyHandler paramHandler) throws Exception {
        FlowState currentState = paramHandler.getState();
        boolean isCreation = FlowState.VSYSTEM_CREATION_REQUESTED
                .equals(currentState);
        boolean isModification = FlowState.VSYSTEM_MODIFICATION_REQUESTED
                .equals(currentState);
        if (isCreation || isModification) {
            // validation of parameters for creation, modification
            logger.debug("Validating parameters in state "
                    + currentState.name());
            if (!isAdminAgentReachable()) {
                throw new SuspendException(Messages.getAll(
                        "error_vsys_admin_agent_connection_error",
                        new Object[] { paramHandler.getVsysId() }));
            }

            VSystemTemplateConfiguration templateConfiguration = null;
            if (isCreation) {
                try {
                    templateConfiguration = vsysComm
                            .getVSystemTemplateConfiguration(paramHandler);
                } catch (MissingResourceException e) {
                    throw new AbortException(
                            Messages.getAll("error_user_config_template_mismatch"),
                            Messages.getAll(
                                    "error_provider_system_template_notfound",
                                    paramHandler.getSystemTemplateId()));
                }
            }

            if (paramHandler.isClusterDefined()) {
                if (paramHandler.getIaasContext().getDiskImages() == null) {
                    // fill cache to avoid multiple calls to back-end system
                    paramHandler.getIaasContext().setDiskImages(
                            vsysComm.getDiskImages(paramHandler));
                }
                DiskImage masterImage = serverProcessor.isDiskImageIdValid(
                        paramHandler.getMasterTemplateId(), paramHandler);
                if (masterImage == null) {
                    throw new AbortException(
                            Messages.getAll("error_user_config_template_mismatch"),
                            Messages.getAll(
                                    "error_provider_master_image_notfound",
                                    paramHandler.getMasterTemplateId()));
                }
                DiskImage slaveImage = serverProcessor.isDiskImageIdValid(
                        paramHandler.getSlaveTemplateId(), paramHandler);
                if (slaveImage == null) {
                    throw new AbortException(
                            Messages.getAll("error_user_config_template_mismatch"),
                            Messages.getAll(
                                    "error_provider_slave_image_notfound",
                                    paramHandler.getSlaveTemplateId()));
                }

                if (!"".equalsIgnoreCase(masterImage.getMaxCpuCount())
                        && !isFloatValueOK(paramHandler.getCountCPU(),
                                masterImage.getMaxCpuCount(),
                                PropertyHandler.COUNT_CPU)) {
                    throw new AbortException(
                            Messages.getAll("error_user_config_template_mismatch"),
                            Messages.getAll("error_provider_cpucount_limit",
                                    paramHandler.getCountCPU(),
                                    "[CLUSTER MASTER]",
                                    paramHandler.getMasterTemplateId(),
                                    masterImage.getMaxCpuCount()));
                }

                if (!"".equalsIgnoreCase(slaveImage.getMaxCpuCount())
                        && !isFloatValueOK(paramHandler.getCountCPU(),
                                slaveImage.getMaxCpuCount(),
                                PropertyHandler.COUNT_CPU)) {
                    throw new AbortException(
                            Messages.getAll("error_user_config_template_mismatch"),
                            Messages.getAll("error_provider_cpucount_limit",
                                    paramHandler.getCountCPU(),
                                    "[CLUSTER SLAVES]",
                                    paramHandler.getSlaveTemplateId(),
                                    slaveImage.getMaxCpuCount()));
                }

                validateClusterSize(paramHandler, isCreation, isModification,
                        templateConfiguration);

            }
        }
    }

    /**
     * @param paramHandler
     * @param isCreation
     * @param isModification
     * @param templateConfiguration
     * @throws Exception
     * @throws AbortException
     */
    protected void validateClusterSize(PropertyHandler paramHandler,
            boolean isCreation, boolean isModification,
            VSystemTemplateConfiguration templateConfiguration)
            throws Exception, AbortException {
        List<Network> networks = new ArrayList<>();
        List<? extends VServerConfiguration> vServers = new ArrayList<>();
        if (isCreation && templateConfiguration != null) {
            networks = templateConfiguration.getNetworks();
            vServers = templateConfiguration.getVServers();
        } else if (isModification) {
            VSystemConfiguration systemConfiguration = getVSystemConfiguration(paramHandler);
            networks = systemConfiguration.getNetworks();
            vServers = systemConfiguration.getVServers();
        }

        HashMap<String, HashSet<String>> net2Server = new HashMap<>();
        int countedMasters = 0;
        int countedSlaves = 0;
        String masterNetId = "";
        for (VServerConfiguration sConf : vServers) {
            String networkId = sConf.getNetworkId();
            if (!net2Server.containsKey(networkId)) {
                net2Server.put(networkId, new HashSet<String>());
            }
            net2Server.get(networkId).add(
                    sConf.getDiskImageId() + "###" + sConf.getServerName());
            if (paramHandler.getMasterTemplateId().equals(
                    sConf.getDiskImageId())) {
                countedMasters++;
                masterNetId = sConf.getNetworkId();
            } else if (paramHandler.getSlaveTemplateId().equals(
                    sConf.getDiskImageId())) {
                countedSlaves++;
            }
        }
        int presentClusterNodes = countedMasters + countedSlaves;
        int clusterSize = Integer.parseInt(paramHandler.getClusterSize());
        boolean missingNetwork = true;
        if (networks != null) {
            for (Network network : networks) {
                int numOfMaxVm = network.getNumOfMaxVm();
                if (net2Server.containsKey(network.getId())) {
                    missingNetwork = false;
                    int add = 0;
                    if (network.getId().equals(masterNetId)) {
                        add = clusterSize - presentClusterNodes;
                        if (add < 0) {
                            add = 0;
                        }
                    }
                    if (net2Server.get(network.getId()).size() + add > numOfMaxVm) {
                        throw new AbortException(
                                Messages.getAll("error_user_config_template_mismatch"),
                                Messages.getAll(
                                        "error_provider_servercount_limit",
                                        paramHandler.getClusterSize()));
                    }
                }
            }
        }
        if (missingNetwork) {
            throw new AbortException(
                    Messages.getAll("error_user_config_template_mismatch"),
                    Messages.getAll("error_missing_network",
                            paramHandler.getClusterSize()));
        }
    }

    boolean isAdminAgentReachable() {
        // TODO clarify how this should be checked in the future
        return true;
    }

    boolean isFloatValueOK(String targetValue, String maxValue,
            @SuppressWarnings("unused") String fieldName) {
        float target = 0;
        if (targetValue != null && !targetValue.equals("")) {
            target = Float.parseFloat(targetValue);
        }
        float max = 0;
        if (maxValue != null && !maxValue.equals("")) {
            max = Float.parseFloat(maxValue);
        }
        return target <= max;
    }

    /**
     * Calculates the number of slave nodes required to achieve desired cluster
     * size. If master and slave template IDs are the same, the number returned
     * is the actual cluster size.
     * 
     * @param parameters
     *            the provisioning parameters
     * @return the required number of slave nodes
     */
    int getSlaveClusterSize(PropertyHandler parameters) {
        int reduceByMaster = 1;
        String slaveTemplateId = parameters.getSlaveTemplateId();
        if (slaveTemplateId != null
                && slaveTemplateId.equals(parameters.getMasterTemplateId())) {
            reduceByMaster = 0;
        }
        String clusterSize = parameters.getClusterSize();
        return Math.max(0, Integer.parseInt(clusterSize) - reduceByMaster);
    }

    private void addHostToZone(HashMap<String, String> hostToZone,
            HashMap<String, String> hostToZone_alt, String hostname, String zone)
            throws PolicyConfigurationException {
        String currentZone = hostToZone.get(hostname);
        if (currentZone != null && !currentZone.equals(zone)) {
            // TODO NLS
            throw new PolicyConfigurationException(
                    "Firewall policies can not address one server within different zones: "
                            + currentZone + "(" + hostname + ") vs. " + zone
                            + "(" + hostname + ")");
        }
        currentZone = hostToZone_alt.get(hostname);
        if (currentZone != null && !currentZone.equals(zone)) {
            // TODO NLS
            throw new PolicyConfigurationException(
                    "Firewall policies can not address one server within different zones: "
                            + currentZone + "(" + hostname + ") vs. " + zone
                            + "(" + hostname + ")");
        }
        hostToZone.put(hostname, zone);
    }

    /**
     * Tries to retrieve the virtual system configuration from either the
     * IaaSContext cache or back-end system.
     */
    VSystemConfiguration getVSystemConfiguration(PropertyHandler paramHandler)
            throws Exception {
        VSystemConfiguration result = paramHandler.getIaasContext()
                .getVSystemConfiguration();
        if (result == null) {
            result = vsysComm.getConfiguration(paramHandler);
            paramHandler.getIaasContext().add(result);
        }
        return result;
    }

}
