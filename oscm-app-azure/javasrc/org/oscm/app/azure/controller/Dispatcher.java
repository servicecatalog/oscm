/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import org.apache.commons.lang3.StringUtils;
import org.oscm.app.azure.AzureCommunication;
import org.oscm.app.azure.data.AzureState;
import org.oscm.app.azure.data.FlowState;
import org.oscm.app.azure.exception.AzureClientException;
import org.oscm.app.azure.exception.AzureServiceException;
import org.oscm.app.azure.i18n.Messages;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

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
 */
public class Dispatcher {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory
            .getLogger(Dispatcher.class);

    /**
     * Reference to an APPlatformService instance.
     */
    private APPlatformService platformService = null;
    private AzureCommunication azureCom;

    FlowState flowState;

    /**
     * Instance id.
     */
    private final String instanceId;

    /**
     * PropertyHandler.
     */
    private final PropertyHandler ph;

    /**
     * Constructs a new dispatcher.
     *
     * @param platformService an <code>APPlatformService</code> instance which provides
     *                        helper methods for accessing common APP utilities, for
     *                        example, send emails
     * @param instanceId      the ID of the application instance in question
     * @param ph              a property handler for reading and writing service parameters
     *                        and controller configuration settings
     */
    public Dispatcher(APPlatformService platformService, String instanceId,
                      PropertyHandler ph) {
        this.platformService = platformService;
        this.instanceId = instanceId;
        this.ph = ph;
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
     * of the application instance
     * @throws APPlatformException
     */
    public InstanceStatus dispatch() throws APPlatformException {
        logger.debug("Dispatcher.dispatch entered");

        InstanceStatus status = new InstanceStatus();

        // Get and trace current internal status of the operation
        flowState = ph.getFlowState();
        logger.debug("Current FlowState is {}", flowState);
        FlowState nextFlowState = null;
        try {
            // Dispatch next step depending on current internal status
            switch (flowState) {
                case CREATION_REQUESTED:
                case CREATING:
                case MODIFICATION_REQUESTED:
                case UPDATING:
                case DELETION_REQUESTED:
                case DELETING:
                case FINISHED:
                    nextFlowState = dispatchProvisioningProcess(flowState, status);
                    break;

                case START_REQUESTED:
                case STARTING:
                case STOP_REQUESTED:
                case STOPPING:
                    nextFlowState = dispatchOperationProcess(flowState, status);
                    break;

                case ACTIVATION_REQUESTED:
                case DEACTIVATION_REQUESTED:
                    nextFlowState = dispatchActivationProcess(flowState, status);
                    break;

                default:

            }
        } catch (APPlatformException e) {
            logger.warn("Azure platform reported error", e);
            throw e;
        } catch (AzureServiceException e) {
            if ("ResourceGroupNotFound".equalsIgnoreCase(e.getErrorCode())) {
                throw new InstanceNotAliveException(Messages.getAll(
                        "error_resource_group_not_found", e.toString()));
            }
            throw new SuspendException(Messages.getAll("error_azure_general",
                    e.getMessage()));
        } catch (AzureClientException e) {
            throw new SuspendException(Messages.getAll("error_azure_general",
                    e.getMessage()));
        } catch (Exception e) {
            logger.error("Internal error while dispatching to Azure", e);
            throw new APPlatformException(e.getMessage());
        }

        if (nextFlowState != null) {
            // Set the next internal status for the provisioning operation.
            // The status is stored as a controller configuration setting.
            logger.debug("Next FlowState is {}", nextFlowState);
            ph.setFlowState(nextFlowState);
        }

        // Set the overall status of the application instance.
        // The instance is ready if the internal status of the
        // provisioning operation is FINISHED or DESTROYED. If this
        // is the case, APP stops polling for the instance status.
        status.setIsReady(ph.getFlowState() == FlowState.FINISHED
                || ph.getFlowState() == FlowState.DESTROYED);

        // Update the description of the instance status.
        // This description is displayed to users for a pending
        // subscription.
        List<LocalizedText> messages = Messages.getAll("status_"
                + ph.getFlowState());
        status.setDescription(messages);

        // Return the current parameters and settings to APP.
        // They are stored in the APP database.
        status.setChangedParameters(ph.getSettings().getParameters());

        logger.debug("InstanceStatus isReady: {}, runWithTimer: {}",
                status.isReady(), status.getRunWithTimer());

        return status;
    }

    /**
     * @param flowState current <code>FlowState</code>
     * @param status    an <code>InstanceStatus</code> instance with the overall
     *                  status of the application instance
     * @return next <code>FlowState</code>
     * @throws APPlatformException
     */
    protected FlowState dispatchProvisioningProcess(final FlowState flowState,
                                                    InstanceStatus status) throws APPlatformException {
        logger.debug("Dispatcher.dispatchProvisioningProcess entered");

        String mail = ph.getMailForCompletion();
        AzureCommunication azureCom = getAzureCommunication();
        AzureState azureState;
        FlowState nextFlowState = null;
        switch (flowState) {
            case CREATION_REQUESTED:
                azureCom.createInstance();
                //azureCom.test();
                nextFlowState = FlowState.CREATING;
                break;

            case CREATING:
                azureState = azureCom.getDeploymentState();
                if (azureState.isSucceeded()) {
                    status.setAccessInfo(azureCom.getAccessInfo("RUNNING").getOutput(
                            ph.getCustomerLocale()));
                    if (StringUtils.isNotEmpty(mail)) {
                        dispatchManualOperation(flowState);
                        nextFlowState = FlowState.MANUAL;
                    } else {
                        nextFlowState = FlowState.FINISHED;
                    }
                } else if (azureState.isFailed()) {
                    throw new AbortException(
                            Messages.getAll("error_create_failed_customer"),
                            Messages.getAll("error_create_failed_provider",
                                    azureState.getStatusCode()));
                } else {
                    logger.info(flowState
                            + " Instance is not yet ready, provisioning status: "
                            + azureState.getProvisioningState()
                            + ". Nothing will be done.");
                }
                break;

            case MODIFICATION_REQUESTED:
                azureCom.updateInstance();
                nextFlowState = FlowState.UPDATING;
                break;

            case UPDATING:
                azureState = azureCom.getDeploymentState();
                if (azureState.isSucceeded()) {
                    status.setAccessInfo(azureCom.getAccessInfo("RUNNING").getOutput(
                            ph.getCustomerLocale()));
                    if (StringUtils.isNotEmpty(mail)) {
                        dispatchManualOperation(flowState);
                        nextFlowState = FlowState.FINISHED;
                    } else {
                        nextFlowState = FlowState.FINISHED;
                    }
                } else if (azureState.isFailed()) {
                    throw new AbortException(
                            Messages.getAll("error_update_failed_customer"),
                            Messages.getAll("error_update_failed_provider",
                                    azureState.getStatusCode()));
                } else {
                    logger.info(flowState
                            + " Instance is not yet ready, provisioning status: "
                            + azureState.getProvisioningState()
                            + ". Nothing will be done.");
                }
                break;

            case DELETION_REQUESTED:
                azureCom.deleteInstance();
                nextFlowState = FlowState.DELETING;
                break;

            case DELETING:
                azureState = azureCom.getDeletingState();
                if (azureState.isDeleted()) {
                    if (StringUtils.isNotEmpty(mail)) {
                        dispatchManualOperation(flowState);
                        nextFlowState = FlowState.DESTROYED;
                    } else {
                        nextFlowState = FlowState.DESTROYED;
                    }
                } else {
                    logger.info(flowState
                            + " Instance is not yet ready, provisioning status: "
                            + azureState.getProvisioningState()
                            + ". Nothing will be done.");
                }
                break;

            case FINISHED:
                // CREATING -> mail'command=finish' -> FINISHED
                azureState = azureCom.getDeploymentState();
                if (azureState.isSucceeded()) {
                    status.setAccessInfo(azureCom.getAccessInfo("RUNNING").getOutput(
                            ph.getCustomerLocale()));
                }
                break;

            default:
        }
        logger.info("left dispatchProvisioningProcess :: nextFlowState-" + nextFlowState);
        return nextFlowState;
    }

    /**
     * @param flowState current <code>FlowState</code>
     * @param status    an <code>InstanceStatus</code> instance with the overall
     *                  status of the application instance
     * @return next <code>FlowState</code>
     */
    protected FlowState dispatchOperationProcess(final FlowState flowState,
                                                 InstanceStatus status) {
        logger.debug("Dispatcher.dispatchOperationProcess entered");
        azureCom = getAzureCommunication();
        AzureState azureState;
        FlowState nextFlowState = null;
        switch (flowState) {
            case START_REQUESTED:
                azureCom.startInstance();
                status.setAccessInfo(azureCom.getAccessInfo("STARTING").getOutput(
                        ph.getCustomerLocale()));
                nextFlowState = FlowState.STARTING;
                break;

            case STARTING:
                azureState = azureCom.getStartingState();
                if (azureState.isSucceeded()) {
                    status.setAccessInfo(azureCom.getAccessInfo("RUNNING").getOutput(
                            ph.getCustomerLocale()));
                    nextFlowState = FlowState.FINISHED;
                } else {
                    logger.info(flowState
                            + " Instance is not yet ready, provisioning status: "
                            + azureState.getProvisioningState()
                            + ". Nothing will be done.");
                }
                break;

            case STOP_REQUESTED:
                azureCom.stopInstance();
                status.setAccessInfo(azureCom.getAccessInfo("STOPPING").getOutput(
                        ph.getCustomerLocale()));
                nextFlowState = FlowState.STOPPING;
                break;

            case STOPPING:
                azureState = azureCom.getStoppingState();
                if (azureState.isSucceeded()) {
                    status.setAccessInfo(azureCom.getAccessInfo("STOPPED").getOutput(ph.getCustomerLocale()));
                    nextFlowState = FlowState.FINISHED;
                } else {
                    logger.info(flowState
                            + " Instance is not yet ready, provisioning status: "
                            + azureState.getProvisioningState()
                            + ". Nothing will be done.");
                }
                break;

            default:
        }
        return nextFlowState;
    }

    public AzureCommunication getAzureCommunication() {
        return new AzureCommunication(ph);
    }

    /**
     * @param flowState current <code>FlowState</code>
     * @param status    an <code>InstanceStatus</code> instance with the overall
     *                  status of the application instance
     * @return next <code>FlowState</code>
     */
    protected FlowState dispatchActivationProcess(final FlowState flowState,
                                                  InstanceStatus status) {
        logger.debug("Dispatcher.dispatchActivationProcess entered");

        FlowState nextFlowState = null;
        switch (flowState) {
            case ACTIVATION_REQUESTED:
                nextFlowState = dispatchOperationProcess(FlowState.START_REQUESTED,
                        status);
                break;

            case DEACTIVATION_REQUESTED:
                nextFlowState = dispatchOperationProcess(FlowState.STOP_REQUESTED,
                        status);
                break;

            default:
        }
        return nextFlowState;
    }

    /**
     * @param flowState current <code>FlowState</code>
     * @throws APPlatformException
     */
    private void dispatchManualOperation(final FlowState flowState)
            throws APPlatformException {
        logger.debug("Dispatcher.dispatchManualOperation entered");

        String subscriptionId = ph.getSettings().getOriginalSubscriptionId();
        String locale = platformService.authenticate(AzureController.ID,
                ph.getTPAuthentication()).getLocale();

        String subject;
        String details;
        String text;
        switch (flowState) {
            case CREATING:
                StringBuffer eventLink = new StringBuffer(
                        platformService.getEventServiceUrl());
                try {
                    eventLink.append("?sid=").append(
                            URLEncoder.encode(instanceId, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new APPlatformException(e.getMessage());
                }
                eventLink.append("&cid=").append(AzureController.ID);
                eventLink.append("&command=finish");
                subject = Messages.get(locale,
                        "mail_azure_manual_completion.subject", new Object[]{
                                instanceId, subscriptionId});
                details = ph.getConfigurationAsString();
                text = Messages.get(locale, "mail_azure_manual_completion.text",
                        new Object[]{instanceId, subscriptionId, details,
                                eventLink.toString()});
                break;

            case UPDATING:
                subject = Messages.get(locale,
                        "mail_azure_manual_modification.subject", new Object[]{
                                instanceId, subscriptionId});
                details = ph.getConfigurationAsString();
                text = Messages.get(locale, "mail_azure_manual_modification.text",
                        new Object[]{instanceId, subscriptionId, details});
                break;

            case DELETING:
                subject = Messages.get(locale, "mail_azure_manual_delete.subject",
                        new Object[]{instanceId, subscriptionId});
                text = Messages.get(locale, "mail_azure_manual_delete.text",
                        new Object[]{instanceId, subscriptionId});
                break;

            default:
                return;
        }

        platformService.sendMail(
                Collections.singletonList(ph.getMailForCompletion()), subject,
                text);
    }


    public void test(PropertyHandler ph) throws Exception {

        InstanceStatus instanceStatus;

        Dispatcher dispatcher = new Dispatcher(null, "ess.azureARM", ph);

        instanceStatus = dispatcher.dispatch();
        logger.debug("instanceStatus= " + instanceStatus.toString() + ",flowwstate= " + ph.getFlowState() + ",check= " + ph.getFlowState().equals(FlowState.FINISHED));
    }
}
