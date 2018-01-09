/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  OpenStack controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2013-11-29                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.oscm.app.openstack.HeatProcessor;
import org.oscm.app.openstack.NovaProcessor;
import org.oscm.app.openstack.data.FlowState;
import org.oscm.app.openstack.data.Server;
import org.oscm.app.openstack.data.Stack;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.openstack.i18n.Messages;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Dispatcher {

    private static final Logger logger = LoggerFactory
            .getLogger(Dispatcher.class);

    private final String instanceId;
    private final PropertyHandler properties;
    private final APPlatformService platformService;

    /**
     * Constructs a new dispatcher.
     * 
     * @param platformService
     *            an <code>APPlatformService</code> instance which provides
     *            helper methods for accessing common APP utilities, for
     *            example, send emails
     * @param instanceId
     *            the ID of the application instance in question
     * @param paramHandler
     *            a property handler for reading and writing service parameters
     *            and controller configuration settings
     */
    public Dispatcher(APPlatformService platformService, String instanceId,
            PropertyHandler paramHandler) {
        this.platformService = platformService;
        this.instanceId = instanceId;
        this.properties = paramHandler;
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
    public InstanceStatus dispatch() throws APPlatformException {

        logger.debug("dispatch('{}', VMwarePropertyHandler) entered",
                instanceId);

        InstanceStatus result = new InstanceStatus();

        // Get and trace current internal status of the operation
        FlowState currentState = properties.getState();
        logger.debug("  currentState=" + currentState.toString());
        FlowState newState = null;
        Stack stack;
        String status;
        String statusReason;
        List<Server> servers = new ArrayList<Server>();
        HashMap<String, Boolean> operationStatuses;
        List<Server> successServers = new ArrayList<Server>();
        List<Server> errorServers;
        List<LocalizedText> messages = null;
        String mail = properties.getMailForCompletion();
        try {
            // Dispatch next step depending on current internal status
            switch (currentState) {
            case CREATION_REQUESTED:
                getHeatProcessor().createStack(properties);
                newState = FlowState.CREATING_STACK;
                break;

            case MODIFICATION_REQUESTED:
                getHeatProcessor().updateStack(properties);
                newState = FlowState.UPDATING;
                break;

            case DELETION_REQUESTED:
                getHeatProcessor().deleteStack(properties);
                newState = FlowState.DELETING_STACK;
                break;

            case START_REQUESTED:
                operationStatuses = new NovaProcessor()
                        .startInstances(properties);
                if (operationStatuses.containsValue(Boolean.TRUE)) {
                    newState = FlowState.STARTING;
                    messages = Messages.getAll("status_" + currentState);
                } else {
                    properties.setStartTime("Timeout");
                    stack = getHeatProcessor().getStackDetails(properties);
                    result.setAccessInfo(getAccessInfo(stack));
                    throw new APPlatformException(
                            Messages.getAll("error_starting_failed"));
                }
                break;

            case STARTING:
                servers = new NovaProcessor().getServersDetails(properties,
                        false);
                errorServers = new ArrayList<Server>();
                for (Server server : servers) {
                    if (server.getStatus().equals(
                            ServerStatus.ACTIVE.toString())) {
                        successServers.add(server);
                    }
                    if (server.getStatus()
                            .equals(ServerStatus.ERROR.toString())
                            || server.getStatus().equals(
                                    ServerStatus.UNKNOWN.toString())) {
                        errorServers.add(server);
                    }
                }

                logger.debug(Integer.toString(successServers.size()) + " of "
                        + Integer.toString(servers.size()) + " VMs started");
                logger.debug(Integer.toString(errorServers.size())
                        + " VMs are ERROR or UNKNOWN status");
                if (errorServers.size() == 0) {
                    if (successServers.size() == servers.size()) {
                        stack = getHeatProcessor().getStackDetails(properties);
                        result.setAccessInfo(getAccessInfo(stack));
                        newState = FlowState.FINISHED;
                    } else {
                        logger.info(FlowState.STARTING
                                + " servers are not yet ready. "
                                + Integer.toString(servers.size()
                                        - successServers.size())
                                + " VMs are not started. Nothing will be done.");

                        messages = Messages.getAll(
                                "status_" + properties.getState(),
                                Integer.toString(successServers.size()),
                                Integer.toString(servers.size()));
                    }
                } else {
                    properties.setStartTime("Timeout");
                    throw new APPlatformException(
                            Messages.getAll("error_starting_failed"));
                }
                break;

            case STOP_REQUESTED:
                operationStatuses = new NovaProcessor()
                        .stopInstances(properties);
                if (operationStatuses.containsValue(Boolean.TRUE)) {
                    newState = FlowState.STOPPING;
                    messages = Messages.getAll("status_" + currentState);
                } else {
                    stack = getHeatProcessor().getStackDetails(properties);
                    result.setAccessInfo(getAccessInfo(stack));
                    throw new APPlatformException(
                            Messages.getAll("error_stopping_failed"));
                }
                break;

            case STOPPING:
                servers = new NovaProcessor().getServersDetails(properties,
                        false);
                errorServers = new ArrayList<Server>();
                for (Server server : servers) {
                    if (server.getStatus().equals(
                            ServerStatus.SHUTOFF.toString())) {
                        successServers.add(server);
                    }
                    if (server.getStatus()
                            .equals(ServerStatus.ERROR.toString())
                            || server.getStatus().equals(
                                    ServerStatus.UNKNOWN.toString())) {
                        errorServers.add(server);
                    }
                }

                logger.debug(Integer.toString(successServers.size()) + " of "
                        + Integer.toString(servers.size()) + " VMs stopped");
                logger.debug(Integer.toString(errorServers.size())
                        + " VMs are ERROR or UNKNOWN status");
                if (successServers.size() == servers.size()) {
                    stack = getHeatProcessor().getStackDetails(properties);
                    result.setAccessInfo(getAccessInfo(stack));
                    newState = FlowState.FINISHED;
                } else {
                    logger.info(FlowState.STOPPING
                            + " Servers is not yet ready"
                            + Integer.toString(servers.size()
                                    - successServers.size())
                            + "VMs are not stopped. Nothing will be done.");

                    messages = Messages.getAll(
                            "status_" + properties.getState(),
                            Integer.toString(successServers.size()),
                            Integer.toString(servers.size()));
                }
                break;

            case ACTIVATION_REQUESTED:
                boolean resuming = getHeatProcessor().resumeStack(properties);
                newState = resuming ? FlowState.ACTIVATING : FlowState.FINISHED;
                if (resuming) {
                    result.setAccessInfo(Messages.get(
                            properties.getCustomerLocale(),
                            "accessInfo_NOT_AVAILABLE"));
                } else {
                    stack = getHeatProcessor().getStackDetails(properties);
                    result.setAccessInfo(getAccessInfo(stack));
                }
                break;

            case ACTIVATING:
                stack = getHeatProcessor().getStackDetails(properties);
                status = stack.getStatus();
                statusReason = stack.getStatusReason();
                logger.debug("Status of stack is: " + status);
                if (StackStatus.RESUME_COMPLETE.name().equals(status)) {
                    result.setAccessInfo(getAccessInfo(stack));
                    newState = FlowState.FINISHED;
                } else if (StackStatus.RESUME_FAILED.name().equals(status)
                        && statusReason.contains("Failed to find instance")) {
                    throw new InstanceNotAliveException(
                            Messages.getAll("error_activating_failed_instance_not_found"));
                } else if (StackStatus.RESUME_FAILED.name().equals(status)) {
                    throw new SuspendException(Messages.getAll(
                            "error_activating_failed", stack.getStatusReason()));
                } else {
                    logger.info(FlowState.ACTIVATING
                            + " Instance is not yet ready, status: " + status
                            + ". Nothing will be done.");
                }
                break;

            case DEACTIVATION_REQUESTED:
                boolean suspending = getHeatProcessor()
                        .suspendStack(properties);
                newState = suspending ? FlowState.DEACTIVATING
                        : FlowState.FINISHED;
                result.setAccessInfo(Messages.get(
                        properties.getCustomerLocale(),
                        "accessInfo_NOT_AVAILABLE"));
                break;

            case DEACTIVATING:
                stack = getHeatProcessor().getStackDetails(properties);
                status = stack.getStatus();
                statusReason = stack.getStatusReason();
                logger.debug("Status of stack is: " + status);
                if (StackStatus.SUSPEND_COMPLETE.name().equals(status)) {
                    result.setAccessInfo(Messages.get(
                            properties.getCustomerLocale(),
                            "accessInfo_NOT_AVAILABLE"));
                    newState = FlowState.FINISHED;
                } else if (StackStatus.SUSPEND_FAILED.name().equals(status)
                        && statusReason.contains("Failed to find instance")) {
                    throw new InstanceNotAliveException(
                            Messages.getAll("error_deactivating_failed_instance_not_found"));
                } else if (StackStatus.SUSPEND_FAILED.name().equals(status)) {
                    throw new SuspendException(Messages.getAll(
                            "error_deactivating_failed",
                            stack.getStatusReason()));
                } else {
                    logger.info(FlowState.DEACTIVATING
                            + " Instance is not yet ready, status: " + status
                            + ". Nothing will be done.");
                }
                break;

            case CREATING_STACK:
                stack = getHeatProcessor().getStackDetails(properties);
                status = stack.getStatus();
                if (StackStatus.CREATE_COMPLETE.name().equals(status)) {
                    result.setAccessInfo(getAccessInfo(stack));
                    if (mail != null) {
                        newState = dispatchManualOperation(instanceId,
                                properties, mail, StackStatus.CREATE_COMPLETE);
                    } else {
                        newState = FlowState.FINISHED;
                    }
                } else if (StackStatus.CREATE_FAILED.name().equals(status)) {
                    throw new AbortException(
                            Messages.getAll("error_create_failed_customer"),
                            Messages.getAll("error_create_failed_provider",
                                    stack.getStatusReason()));
                } else {
                    logger.info(FlowState.CREATING_STACK
                            + " Instance is not yet ready, status: " + status
                            + ". Nothing will be done.");
                }
                break;

            case UPDATING:
                stack = getHeatProcessor().getStackDetails(properties);
                status = stack.getStatus();
                if (StackStatus.UPDATE_COMPLETE.name().equals(status)) {
                    result.setAccessInfo(getAccessInfo(stack));
                    if (mail != null) {
                        newState = dispatchManualOperation(instanceId,
                                properties, mail, StackStatus.UPDATE_COMPLETE);
                    } else {
                        newState = FlowState.FINISHED;
                    }
                } else if (StackStatus.UPDATE_FAILED.name().equals(status)) {
                    throw new AbortException(
                            Messages.getAll("error_update_failed_customer"),
                            Messages.getAll("error_update_failed_provider",
                                    stack.getStatusReason()));
                } else {
                    logger.info(FlowState.UPDATING
                            + " Instance is not yet ready, status: " + status
                            + ". Nothing will be done.");
                }
                break;

            case DELETING_STACK:
                try {
                    stack = getHeatProcessor().getStackDetails(properties);
                    status = stack.getStatus();
                    statusReason = stack.getStatusReason();
                    if (StackStatus.DELETE_COMPLETE.name().equals(status)
                            || (StackStatus.DELETE_FAILED.name().equals(status) && statusReason
                                    .contains("Failed to find instance"))) {
                        if (mail != null) {
                            newState = dispatchManualOperation(instanceId,
                                    properties, mail,
                                    StackStatus.DELETE_COMPLETE);
                        } else {
                            newState = FlowState.DESTROYED;
                        }
                    } else if (StackStatus.DELETE_FAILED.name().equals(status)) {
                        throw new SuspendException(Messages.getAll(
                                "error_deleting_stack_failed",
                                stack.getStatusReason()));
                    } else {
                        logger.info(FlowState.DELETING_STACK
                                + " Instance is not yet ready, status: "
                                + status + ". Nothing will be done.");
                    }

                } catch (HeatException e) {
                    if (e.getResponseCode() != 404) {
                        throw e;
                    } else {
                        // Stack is no longer available, since it has been
                        // deleted.
                        if (mail != null) {
                            newState = dispatchManualOperation(instanceId,
                                    properties, mail,
                                    StackStatus.DELETE_COMPLETE);
                        } else {
                            newState = FlowState.DESTROYED;
                        }
                    }
                }

                break;
            case FINISHED:
                stack = getHeatProcessor().getStackDetails(properties);
                status = stack.getStatus();
                if (StackStatus.CREATE_COMPLETE.name().equals(status)) {
                    result.setAccessInfo(getAccessInfo(stack));
                }
                break;

            default:
            }
        } catch (APPlatformException e) {
            logger.warn("OpenStack platform reported error", e);
            throw e;
        } catch (HeatException e) {
            if (e.getResponseCode() < 0 || e.getResponseCode() == 401
                    || e.getResponseCode() == 504) {
                throw new SuspendException(e.getMessage(), e.getResponseCode());
            }
            if (e.getResponseCode() == 404) {
                if (FlowState.DEACTIVATION_REQUESTED == currentState
                        || FlowState.DEACTIVATING == currentState) {
                    throw new InstanceNotAliveException(
                            Messages.getAll("error_deactivating_failed_instance_not_found"));
                } else if (FlowState.ACTIVATION_REQUESTED == currentState
                        || FlowState.ACTIVATING == currentState) {
                    throw new InstanceNotAliveException(
                            Messages.getAll("error_activating_failed_instance_not_found"));
                }
                throw new AbortException(Messages.getAll(
                        "error_heat_resource_not_found", e.getMessage()),
                        Messages.getAll("error_heat_resource_not_found",
                                e.getMessage()));

            }
            throw new APPlatformException(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal error while dispatching to OpenStack", e);
            throw new APPlatformException(e.getMessage());
        }

        if (newState != null) {
            // Set the next internal status for the provisioning operation.
            // The status is stored as a controller configuration setting.
            logger.debug("  newState=" + newState.toString());
            properties.setState(newState);
        }

        // Set the overall status of the application instance.
        // The instance is ready if the internal status of the
        // provisioning operation is FINISHED or DESTROYED. If this
        // is the case, APP stops polling for the instance status.
        result.setIsReady(properties.getState() == FlowState.FINISHED
                || properties.getState() == FlowState.DESTROYED);

        result.setRunWithTimer(properties.getState() != FlowState.MANUAL);
        // Update the description of the instance status.
        // This description is displayed to users for a pending
        // subscription.
        if (messages == null) {
            messages = Messages.getAll("status_" + properties.getState());
        }
        result.setDescription(messages);

        // Return the current parameters and settings to APP.
        // They are stored in the APP database.
        result.setChangedParameters(properties.getSettings().getParameters());

        return result;
    }

    protected HeatProcessor getHeatProcessor() throws AuthenticationException,
            APPlatformException {
        return new HeatProcessor();
    }

    private String getAccessInfo(Stack stack) {
        String accessInfo = properties.getAccessInfoPattern();
        for (String key : stack.getOutput().keySet()) {
            String value = stack.getOutput(key);
            if (value != null) {
                accessInfo = accessInfo.replace("{" + key + "}", value);
            }
        }
        return accessInfo;
    }

    private FlowState dispatchManualOperation(String instanceId,
            PropertyHandler properties, String mail, StackStatus status)
            throws APPlatformException, UnsupportedEncodingException,
            HeatException {
        String subscriptionId = properties.getSettings()
                .getOriginalSubscriptionId();
        User user = platformService.authenticate(OpenStackController.ID,
                properties.getTPAuthentication());
        String locale = user.getLocale();
        if (StackStatus.CREATE_COMPLETE.equals(status)) {
            StringBuffer eventLink = new StringBuffer(
                    platformService.getEventServiceUrl());
            eventLink.append("?sid=").append(
                    URLEncoder.encode(instanceId, "UTF-8"));
            eventLink.append("&controllerid=").append(OpenStackController.ID);
            eventLink.append("&command=finish");
            String subject = Messages.get(locale,
                    "mail_openstack_manual_completion.subject", new Object[] {
                            instanceId, subscriptionId });
            String details = properties.getStackConfigurationAsString();
            String text = Messages.get(locale,
                    "mail_openstack_manual_completion.text",
                    new Object[] { instanceId, subscriptionId, details,
                            eventLink.toString() });
            platformService.sendMail(Collections.singletonList(mail), subject,
                    text);
            return FlowState.MANUAL;
        } else if (StackStatus.UPDATE_COMPLETE.equals(status)) {
            String subject = Messages.get(locale,
                    "mail_openstack_manual_modification.subject", new Object[] {
                            instanceId, subscriptionId });
            String details = properties.getStackConfigurationAsString();
            String text = Messages.get(locale,
                    "mail_openstack_manual_modification.text", new Object[] {
                            instanceId, subscriptionId, details });
            platformService.sendMail(Collections.singletonList(mail), subject,
                    text);
            return FlowState.FINISHED;
        } else if (StackStatus.DELETE_COMPLETE.equals(status)) {
            String subject = Messages.get(locale,
                    "mail_openstack_manual_delete.subject", new Object[] {
                            instanceId, subscriptionId });
            String text = Messages.get(locale,
                    "mail_openstack_manual_delete.text", new Object[] {
                            instanceId, subscriptionId });
            platformService.sendMail(Collections.singletonList(mail), subject,
                    text);
            return FlowState.DESTROYED;
        }
        return null;
    }

}
