/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Sample controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2012-09-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.oscm.app.aws.EC2Communication;
import org.oscm.app.aws.data.FlowState;
import org.oscm.app.aws.data.Operation;
import org.oscm.app.aws.data.Server;
import org.oscm.app.aws.i18n.Messages;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;

/**
 * Dispatcher for triggering the next step in a provisioning operation depending
 * on the current internal status.
 * <p>
 * The controller methods for creating, updating, and deleting instances set
 * their own, internal status. This status is evaluated and handled by the
 * dispatcher, which is invoked at regular intervals by APP through the
 * <code>getInstanceStatus</code> method of the <code>SampleController</code>
 * class. The dispatcher sets the next internal status and returns the
 * corresponding overall instance status to APP through the controller.
 * <p>
 * In this sample implementation, the dispatcher simply moves from one step to
 * the next and sends emails at specific status transitions. The recipient and
 * contents of the emails are set as service parameters in the technical service
 * definition of the sample.
 * 
 */
public class EC2Processor {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EC2Processor.class);

    private APPlatformService platformService;

    private final PropertyHandler ph;
    private final String instanceId;
    private final String KEY_PAIR_NAME = " Key pair name: ";

    /**
     * Constructs a new dispatcher.
     * 
     * @param paramHandler
     *            a property handler for reading and writing service parameters
     *            and controller configuration settings
     */
    public EC2Processor(PropertyHandler paramHandler, String instanceId) {
        this.ph = paramHandler;
        this.instanceId = instanceId;
        try {
            this.platformService = APPlatformServiceFactory.getInstance();
        } catch (IllegalStateException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    /**
     * Triggers the next step of a provisioning operation depending on the
     * operation's current internal status, and returns the overall instance
     * status. The internal status is set by the service controller or the
     * dispatcher itself. At specific status transitions, emails are sent to the
     * recipient specified as a service parameter in the technical service
     * definition.
     * <p>
     * In real-life scenarios, the dispatcher might also check the current
     * status or the result of operations triggered from the outside.
     * 
     * @return an <code>InstanceStatus</code> instance with the overall status
     *         of the application instance
     * @throws APPlatformException
     */
    public InstanceStatus process() throws APPlatformException {

        Operation operationState = ph.getOperation();
        FlowState flowState = ph.getState();
        LOGGER.debug(
                "Dispatching in EC2Processor with OperationState={} and FlowState={}",
                operationState, flowState);

        FlowState newState = null;
        InstanceStatus result = new InstanceStatus();

        try {
            switch (operationState) {
            case EC2_CREATION:
            case EC2_MODIFICATION:
            case EC2_DELETION:
                newState = manageProvisioningProcess(flowState, result);
                break;
            case EC2_ACTIVATION:
                newState = manageActivationProcess(flowState, result);
                break;
            case EC2_OPERATION:
                newState = manageOperationProcess(flowState, result);
                break;
            default:
                break;
            }
        } catch (AmazonServiceException e) {
            String ec = e.getErrorCode();
            int statusCode = e.getStatusCode();
            if ("AuthFailure".equals(ec)
                    || "UnauthorizedOperation".equals(ec)) {
                throw new SuspendException(Messages
                        .getAll("error_suspend_awscode_" + ec, e.getMessage()));
            }
            if (("IncorrectInstanceState".equals(ec)
                    || "Unsupported".equals(ec)) && 400 == statusCode) {
                throw new InstanceNotAliveException(Messages.getAll(
                        "error_suspend_instance_error", e.getMessage()));
            }
            throwPlatformException(operationState,
                    Messages.getAll("error_aws_general", e.toString()));
        } catch (AmazonClientException e) {
            // assuming that client exceptions have local reasons, not logical
            // reasons regarding AWS, the technology manger would be able to
            // fix the issue and continue processing
            throw new SuspendException(
                    Messages.getAll("error_aws_general", e.getMessage()));
        }

        if (newState != null) {
            LOGGER.debug("Process in EC2Processor changes FlowState to {}",
                    newState.toString());
            ph.setState(newState);
        } else {
            LOGGER.debug("Process in EC2Processor leaves FlowState unchanged");
        }

        // Set the overall status of the application instance.
        // The instance is ready if the internal status of the
        // provisioning operation is FINISHED or DESTROYED. If this
        // is the case, APP stops polling for the instance status.
        result.setIsReady(ph.getState() == FlowState.FINISHED
                || ph.getState() == FlowState.DESTROYED);

        result.setRunWithTimer(ph.getState() != FlowState.MANUAL);

        // Update the description of the instance status.
        // This description is displayed to users for a pending subscription.
        result.setDescription(Messages.getAll("status_" + ph.getState()));

        // Return the current parameters and settings to APP.
        result.setChangedParameters(ph.getSettings().getParameters());
        return result;
    }

    public List<Server> getServerDetails() {
        EC2Communication ec2comm = new EC2Communication(ph);
        List<Instance> instances = ec2comm.getInstance(instanceId);
        return convertInstanceToServer(instances);
    }

    private void throwPlatformException(Operation operationState,
            List<LocalizedText> providerMessages) throws APPlatformException {
        if (Operation.EC2_CREATION.equals(operationState)) {
            throw new AbortException(
                    Messages.getAll("error_abort_creation_customer"),
                    providerMessages);
        } else if (Operation.EC2_MODIFICATION.equals(operationState)) {
            throw new AbortException(
                    Messages.getAll("error_abort_modification_customer"),
                    providerMessages);
        } else if (Operation.EC2_OPERATION.equals(operationState)) {
            throw new AbortException(
                    Messages.getAll("error_abort_operation_customer"),
                    providerMessages);
        }
        throw new SuspendException(providerMessages);
    }

    /**
     * Manage provisioning process.
     * 
     * @param flowState
     *            flowState
     * @param result
     *            result
     * @return FlowState newState
     * @throws APPlatformException
     */
    FlowState manageProvisioningProcess(FlowState flowState,
            InstanceStatus result) throws APPlatformException {

        EC2Communication ec2comm = new EC2Communication(ph);
        String instanceState;
        FlowState newState = null;
        String mail = ph.getMailForCompletion();
        String accessInfo = null;
        // Dispatch next step depending on current internal status
        switch (flowState) {
        case CREATION_REQUESTED:
            // get available image
            Image imageId = ec2comm.resolveAMI(ph.getImageName());
            ec2comm.createInstance(imageId);
            newState = FlowState.CREATING;
            break;

        case MODIFICATION_REQUESTED:
            ec2comm.modifyInstance();
            newState = FlowState.UPDATING;
            break;

        case DELETION_REQUESTED:
            ec2comm.terminateInstance(ph.getAWSInstanceId());
            newState = FlowState.DELETING;
            break;

        case CREATING:
            LOGGER.info("Trying to check instance status...");
            if (isInstanceRunning(ec2comm)) {
                accessInfo = Messages.get(ph.getCustomerLocale(),
                        "accessInfo_DNS", new Object[] {
                                ec2comm.getPublicDNS(ph.getAWSInstanceId()) });
                result.setAccessInfo(
                        accessInfo + KEY_PAIR_NAME + ph.getKeyPairName());
                if (mail != null) {
                    newState = dispatchManualOperation(AWSController.ID,
                            instanceId, ph, mail);
                } else {
                    newState = FlowState.FINISHED;
                }
            }
            break;

        case UPDATING:
            accessInfo = Messages.get(ph.getCustomerLocale(), "accessInfo_DNS",
                    new Object[] {
                            ec2comm.getPublicDNS(ph.getAWSInstanceId()) });
            if (accessInfo == null || accessInfo.trim().length() == 0) {
                accessInfo = Messages.get(ph.getCustomerLocale(),
                        "accessInfo_STOPPED");
            }
            result.setAccessInfo(
                    accessInfo + KEY_PAIR_NAME + ph.getKeyPairName());
            if (mail != null) {
                newState = dispatchManualOperation(AWSController.ID, instanceId,
                        ph, mail);
            } else {
                newState = FlowState.FINISHED;
            }
            break;

        case DELETING:
            instanceState = ec2comm.getInstanceState(ph.getAWSInstanceId());
            ec2comm.isInstanceReady(ph.getAWSInstanceId());
            if ("terminated".equals(instanceState)) {
                if (mail != null) {
                    newState = dispatchManualOperation(AWSController.ID,
                            instanceId, ph, mail);
                } else {
                    newState = FlowState.DESTROYED;
                }
            }
            break;
        case FINISHED:
            if (isInstanceRunning(ec2comm)) {
                accessInfo = Messages.get(ph.getCustomerLocale(),
                        "accessInfo_DNS", new Object[] {
                                ec2comm.getPublicDNS(ph.getAWSInstanceId()) });
                result.setAccessInfo(
                        accessInfo + KEY_PAIR_NAME + ph.getKeyPairName());
            }
            break;

        default:
        }
        return newState;
    }

    private boolean isInstanceRunning(EC2Communication ec2comm) {
        String instanceState = ec2comm.getInstanceState(ph.getAWSInstanceId());
        boolean ready = ec2comm.isInstanceReady(ph.getAWSInstanceId());
        LOGGER.debug("  Instance state: " + instanceState);
        LOGGER.debug("  Instance ready: " + ready);
        return "running".equals(instanceState) && ready;
    }

    private boolean isInstanceStopped(EC2Communication ec2comm) {
        String instanceState = ec2comm.getInstanceState(ph.getAWSInstanceId());
        boolean ready = ec2comm.isInstanceReady(ph.getAWSInstanceId());
        LOGGER.debug("  Instance state: " + instanceState);
        LOGGER.debug("  Instance ready: " + ready);
        return "stopped".equals(instanceState);
    }

    /**
     * Manage operation process.
     * 
     * @param flowState
     *            flowState
     * @param result
     *            result
     * @return FlowState newState
     */
    FlowState manageOperationProcess(FlowState flowState,
            InstanceStatus result) {
        EC2Communication ec2comm = new EC2Communication(ph);
        FlowState newState = null;
        // Dispatch next step depending on current internal status
        switch (flowState) {
        case START_REQUESTED:
            ec2comm.startInstance(ph.getAWSInstanceId());
            result.setDescription(Messages.getAll("accessInfo_STARTING"));
            result.setAccessInfo(Messages.get(ph.getCustomerLocale(),
                    "accessInfo_NOT_AVAILABLE"));
            newState = FlowState.STARTING;
            break;
        case STARTING:
            if (isInstanceRunning(ec2comm)) {
                String accessInfo = Messages.get(ph.getCustomerLocale(),
                        "accessInfo_DNS", new Object[] {
                                ec2comm.getPublicDNS(ph.getAWSInstanceId()) });
                result.setAccessInfo(
                        accessInfo + KEY_PAIR_NAME + ph.getKeyPairName());
                newState = FlowState.FINISHED;
            }
            break;
        case STOP_REQUESTED:
            ec2comm.stopInstance(ph.getAWSInstanceId());
            result.setDescription(Messages.getAll("accessInfo_STOPPING"));
            result.setAccessInfo(Messages.get(ph.getCustomerLocale(),
                    "accessInfo_NOT_AVAILABLE"));
            newState = FlowState.STOPPING;
            break;
        case STOPPING:
            if (isInstanceStopped(ec2comm)) {
                result.setAccessInfo(Messages.get(ph.getCustomerLocale(),
                        "accessInfo_NOT_AVAILABLE"));
                newState = FlowState.FINISHED;
            }
            break;
        default:
        }
        return newState;
    }

    /**
     * Manage activation process.
     * 
     * @param flowState
     *            flowState
     * @param flowState
     *            result
     * @return FlowState newState
     * @throws APPlatformException
     */
    FlowState manageActivationProcess(FlowState flowState,
            InstanceStatus result) throws APPlatformException {
        FlowState newState = null;
        // Dispatch next step depending on current internal status
        switch (flowState) {
        case ACTIVATION_REQUESTED:
            newState = manageOperationProcess(FlowState.START_REQUESTED,
                    result);
            break;
        case DEACTIVATION_REQUESTED:
            newState = manageOperationProcess(FlowState.STOP_REQUESTED, result);
            break;
        default:
            newState = manageOperationProcess(flowState, result);
        }
        return newState;
    }

    FlowState dispatchManualOperation(String controllerId, String instanceId,
            PropertyHandler paramHandler, String mail)
            throws APPlatformException {
        String subscriptionId = paramHandler.getSettings()
                .getOriginalSubscriptionId();
        User user = platformService.authenticate(AWSController.ID,
                paramHandler.getTPAuthentication());
        String locale = user.getLocale();
        if (Operation.EC2_CREATION.equals(paramHandler.getOperation())) {
            StringBuffer eventLink = new StringBuffer(
                    platformService.getEventServiceUrl());
            try {
                eventLink.append("?sid=")
                        .append(URLEncoder.encode(instanceId, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new APPlatformException(e.getMessage());
            }
            eventLink.append("&controllerid=").append(controllerId);
            eventLink.append("&command=finish");
            String subject = Messages.get(locale,
                    "mail_aws_manual_completion.subject",
                    new Object[] { instanceId, subscriptionId });
            String details = paramHandler.getAWSConfigurationAsString();
            String text = Messages.get(locale,
                    "mail_aws_manual_completion.text",
                    new Object[] { instanceId, subscriptionId, details,
                            eventLink.toString() });
            platformService.sendMail(Collections.singletonList(mail), subject,
                    text);
            return FlowState.MANUAL;
        } else if (Operation.EC2_MODIFICATION
                .equals(paramHandler.getOperation())) {
            String subject = Messages.get(locale,
                    "mail_aws_manual_modification.subject",
                    new Object[] { instanceId, subscriptionId });
            String details = paramHandler.getAWSConfigurationAsString();
            String text = Messages.get(locale,
                    "mail_aws_manual_modification.text",
                    new Object[] { instanceId, subscriptionId, details });
            platformService.sendMail(Collections.singletonList(mail), subject,
                    text);
            return FlowState.FINISHED;

        } else if (Operation.EC2_DELETION.equals(paramHandler.getOperation())) {
            String subject = Messages.get(locale,
                    "mail_aws_manual_disposal.subject",
                    new Object[] { instanceId, subscriptionId });
            String text = Messages.get(locale, "mail_aws_manual_disposal.text",
                    new Object[] { instanceId, subscriptionId });
            platformService.sendMail(Collections.singletonList(mail), subject,
                    text);
            return FlowState.DESTROYED;
        }
        return null;
    }

    private List<Server> convertInstanceToServer(List<Instance> instances) {
        List<Server> servers = new ArrayList<>();
        for (Instance instance : instances) {
            Server server = new Server(instance.getInstanceId());
            for (Tag tag : instance.getTags()) {
                if (tag != null && tag.getKey() != null
                        && tag.getKey().equals("Name")) {
                    server.setName(tag.getValue());
                }
            }
            server.setStatus(instance.getState().getName());
            server.setType(instance.getInstanceType());
            server.setPublicIP(Arrays.asList(instance.getPublicIpAddress()));
            server.setPrivateIP(Arrays.asList(instance.getPrivateIpAddress()));
            servers.add(server);
        }
        return servers;
    }
}
