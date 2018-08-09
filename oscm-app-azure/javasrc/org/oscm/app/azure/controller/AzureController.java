/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.app.azure.AzureCommunication;
import org.oscm.app.azure.data.FlowState;
import org.oscm.app.azure.i18n.Messages;

import org.oscm.app.v1_0.APPlatformServiceFactory;
import org.oscm.app.v1_0.data.ControllerSettings;
import org.oscm.app.v1_0.data.InstanceDescription;
import org.oscm.app.v1_0.data.InstanceStatus;
import org.oscm.app.v1_0.data.InstanceStatusUsers;
import org.oscm.app.v1_0.data.LocalizedText;
import org.oscm.app.v1_0.data.OperationParameter;
import org.oscm.app.v1_0.data.ProvisioningSettings;
import org.oscm.app.v1_0.data.ServiceUser;
import org.oscm.app.v1_0.exceptions.APPlatformException;
import org.oscm.app.v1_0.intf.APPlatformController;
import org.oscm.app.v1_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an OpenStack service controller based on the Asynchronous
 * Provisioning Platform (APP).
 * <p>
 * Whenever an application instance is to be created, updated, or deleted, the
 * corresponding method of the controller is called by APP. As long as the
 * provisioning operation is not finished, the controller returns the overall
 * instance status as "not ready". APP thus continues to poll the status at
 * regular intervals (<code>getInstanceStatus</code> method) until the instance
 * is reported as "ready".
 * <p>
 * The controller methods for creating, updating, and deleting instances set
 * their own, internal status. This status is evaluated and handled by a
 * dispatcher, which is invoked at regular intervals by APP through the
 * <code>getInstanceStatus</code> method. The dispatcher sets the next internal
 * status and returns the corresponding overall instance status to APP.
 */
@Stateless(mappedName = "bss/app/controller/" + AzureController.ID) //Custom
@Remote(APPlatformController.class)
public class AzureController implements APPlatformController {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(AzureController.class);

    /**
     * Azure Controller id.
     */
    public static final String ID = "ess.azureARM";

    /**
     * Instance id prefix.
     */
    private static final String INSTANCE_ID_PREFIX = "azure-";

    /**
     * Reference to an APPlatformService instance.
     */
    private APPlatformService platformService;

    /**
     * Retrieves an <code>APPlatformService</code> instance.
     * <p>
     * The <code>APPlatformService</code> provides helper methods by which the
     * service controller can access common APP utilities, for example, send
     * emails or lock application instances.
     */
    @PostConstruct
    public void initialize() {
        platformService = APPlatformServiceFactory.getInstance();
    }

    /**
     * Starts the creation of an application instance and returns the instance
     * ID.
     * <p>
     * The internal status <code>CREATION_REQUESTED</code> is stored as a
     * controller configuration setting. It is evaluated and handled by the
     * status dispatcher, which is invoked at regular intervals by APP through
     * the <code>getInstanceStatus</code> method.
     *
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceDescription</code> instance describing the
     *         application instance
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceDescription createInstance(ProvisioningSettings settings) throws APPlatformException
    {
        try
        {
        	logger.info("Inside createInstance");
            String instanceId = INSTANCE_ID_PREFIX
                    + UUID.randomUUID().toString();
            PropertyHandler ph = new PropertyHandler(settings);
            AzureCommunication azureComm = getAzureCommunication(ph);
            ProvisioningValidator.validateParameters(ph, azureComm);
            ph.setFlowState(FlowState.CREATION_REQUESTED);
            // Return generated instance information
            InstanceDescription id = new InstanceDescription();
            id.setInstanceId(instanceId);
            id.setChangedParameters(settings.getParameters());
            logger.info("exiting createInstance with id: "+id.getInstanceId());
            return id;
        }
        catch (Exception t)
        {
            throw t;
        }
    }

    public AzureCommunication getAzureCommunication(PropertyHandler ph) {
        return new AzureCommunication(ph);
    }

    /**
     * Starts the deletion of an application instance.
     * <p>
     * The internal status <code>DELETION_REQUESTED</code> is stored as a
     * controller configuration setting. It is evaluated and handled by the
     * status dispatcher, which is invoked at regular intervals by APP through
     * the <code>getInstanceStatus</code> method.
     *
     * @param instanceId
     *            the ID of the application instance to be deleted
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the overall status
     *         of the application instance
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus deleteInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
       /* logger.info("deleteInstance({})",
                LogAndExceptionConverter.getLogText(instanceId, settings));*/
        try {
            PropertyHandler ph = new PropertyHandler(settings);
            ph.setStartTime(String.valueOf(System.currentTimeMillis()));
            ph.setFlowState(FlowState.DELETION_REQUESTED);

            InstanceStatus status = new InstanceStatus();
            status.setChangedParameters(settings.getParameters());
            return status;
        } catch (Exception t) {
            throw t;
        }
    }

    /**
     * Starts the modification of an application instance.
     * <p>
     * The internal status <code>MODIFICATION_REQUESTED</code> is stored as a
     * controller configuration setting. It is evaluated and handled by the
     * status dispatcher, which is invoked at regular intervals by APP through
     * the <code>getInstanceStatus</code> method.
     *
     * @param instanceId
     *            the ID of the application instance to be modified
     * @param currentSettings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            current service parameters and configuration settings
     * @param newSettings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            modified service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the overall status
     *         of the application instance
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus modifyInstance(String instanceId,
            ProvisioningSettings currentSettings,
            ProvisioningSettings newSettings) throws APPlatformException {
        try {
            PropertyHandler ph = new PropertyHandler(newSettings);
            ph.setStartTime(String.valueOf(System.currentTimeMillis()));
            ph.setFlowState(FlowState.MODIFICATION_REQUESTED);
            ph.setResourceGroupName(new PropertyHandler(currentSettings)
                    .getResourceGroupName());

            InstanceStatus status = new InstanceStatus();
            status.setChangedParameters(newSettings.getParameters());
            return status;
        } catch (Exception t) {
            throw t;
        }
    }

    /**
     * Returns the current overall status of the application instance.
     * <p>
     * For retrieving the status, the method calls the status dispatcher with
     * the currently stored controller configuration settings. These settings
     * include the internal status set by the controller or the dispatcher
     * itself. The overall status of the instance depends on this internal
     * status.
     *
     * @param instanceId
     *            the ID of the application instance to be checked
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the overall status
     *         of the application instance
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus getInstanceStatus(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
    	logger.info("Inside getInstanceStatus");
        try {
            PropertyHandler ph = new PropertyHandler(settings);
            Dispatcher dp = new Dispatcher(platformService, instanceId, ph);
            InstanceStatus status = dp.dispatch();
            logger.info("exiting getInstanceStatus || isReady: "+status.isReady());
            return status;
        } catch (Exception t) {
            throw t;
        }
    }

    /**
     * Does not carry out specific actions in this implementation and always
     * returns <code>null</code>.
     *
     * @param instanceId
     *            the ID of the application instance
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param properties
     *            the events as properties consisting of a key and a value each
     * @return <code>null</code>
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus notifyInstance(String instanceId,
            ProvisioningSettings settings, Properties properties)
            throws APPlatformException {
        InstanceStatus status = null;
        if (instanceId == null || settings == null || properties == null) {
            return status;
        }
        PropertyHandler ph = new PropertyHandler(settings);
        ph.setStartTime(String.valueOf(System.currentTimeMillis()));

        if ("finish".equals(properties.get("command"))) {
            if (FlowState.MANUAL.equals(ph.getFlowState())) {
                ph.setFlowState(FlowState.FINISHED);
                status = new InstanceStatus();
                status.setIsReady(true);
                status.setRunWithTimer(true);
                status.setDescription(getProvisioningStatusText(ph));
                logger.debug("Got finish event => changing instance status to finished");
            } else {
                APPlatformException pe = new APPlatformException(
                        "Got finish event but instance is in state "
                                + ph.getFlowState() + " => nothing changed");
                throw pe;
            }
        }
        if (status != null) {
            status.setChangedParameters(settings.getParameters());
        }
        return status;
    }

    /**
     * Starts the activation of an application instance.
     * <p>
     * The internal status <code>ACTIVATION_REQUESTED</code> is stored as a
     * controller configuration setting. It is evaluated and handled by the
     * status dispatcher, which is invoked at regular intervals by APP through
     * the <code>getInstanceStatus</code> method.
     *
     * @param instanceId
     *            the ID of the application instance to be activated
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the overall status
     *         of the application instance
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus activateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
        try {
            // Set status to store for application instance
            PropertyHandler ph = new PropertyHandler(settings);
            ph.setStartTime(String.valueOf(System.currentTimeMillis()));
            ph.setFlowState(FlowState.ACTIVATION_REQUESTED);

            InstanceStatus status = new InstanceStatus();
            status.setChangedParameters(settings.getParameters());
            return status;
        } catch (Exception t) {
            throw t;
        }
    }

    /**
     * Starts the deactivation of an application instance.
     * <p>
     * The internal status <code>DEACTIVATION_REQUESTED</code> is stored as a
     * controller configuration setting. It is evaluated and handled by the
     * status dispatcher, which is invoked at regular intervals by APP through
     * the <code>getInstanceStatus</code> method.
     *
     * @param instanceId
     *            the ID of the application instance to be activated
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @return an <code>InstanceStatus</code> instance with the overall status
     *         of the application instance
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus deactivateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
        try {
            // Set status to store for application instance
            PropertyHandler ph = new PropertyHandler(settings);
            ph.setStartTime(String.valueOf(System.currentTimeMillis()));
            ph.setFlowState(FlowState.DEACTIVATION_REQUESTED);

            InstanceStatus status = new InstanceStatus();
            status.setChangedParameters(settings.getParameters());
            return status;
        } catch (Exception t) {
            throw t;
        }
    }

    @Override
    public InstanceStatus executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters, ProvisioningSettings settings)
            throws APPlatformException {
        InstanceStatus status = null;
        if (instanceId == null || operationId == null || settings == null) {
            return status;
        }

        try {
            PropertyHandler ph = new PropertyHandler(settings);
            ph.setStartTime(String.valueOf(System.currentTimeMillis()));

            boolean operationAccepted = false;
            if ("START_VIRTUAL_SYSTEM".equals(operationId)) {
                ph.setFlowState(FlowState.START_REQUESTED);
                operationAccepted = true;
            } else if ("STOP_VIRTUAL_SYSTEM".equals(operationId)) {
                ph.setFlowState(FlowState.STOP_REQUESTED);
                operationAccepted = true;
            }
            if (operationAccepted) {
                // when a valid operation has been requested, let the timer
                // handle the instance afterwards
                status = new InstanceStatus();
                status.setRunWithTimer(true);
                status.setIsReady(false);
                status.setChangedParameters(settings.getParameters());
            }
            return status;
        } catch (Throwable t) {
        	throw  t;
        }
    }

    /**
     * Does not carry out specific actions in this implementation and always
     * returns <code>null</code>.
     *
     * @param instanceId
     *            the ID of the application instance
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param users
     *            a list of users
     * @return <code>null</code>
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatusUsers createUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        // not applicable
        return null;
    }

    /**
     * Does not carry out specific actions in this implementation and always
     * returns <code>null</code>.
     *
     * @param instanceId
     *            the ID of the application instance
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param users
     *            a list of users
     * @return <code>null</code>
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus deleteUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        // not applicable
        return null;
    }

    /**
     * Does not carry out specific actions in this implementation and always
     * returns <code>null</code>.
     *
     * @param instanceId
     *            the ID of the application instance
     * @param settings
     *            a <code>ProvisioningSettings</code> object specifying the
     *            service parameters and configuration settings
     * @param users
     *            a list of users
     * @return <code>null</code>
     * @throws APPlatformException
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus updateUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        // not applicable
        return null;
    }

    @Override
    public List<LocalizedText> getControllerStatus(ControllerSettings settings)
            throws APPlatformException {
        // not yet implemented by APP => nothing to do
        return null;
    }

    @Override
    public List<OperationParameter> getOperationParameters(String userId,
            String instanceId, String operationId, ProvisioningSettings settings)
            throws APPlatformException {
        // not applicable
        return null;
    }

    @Override
    public void setControllerSettings(ControllerSettings settings) {
        // not applicable
    }

    /**
     * Returns a small status text for the current provisioning step.
     *
     * @param ph
     *            property handler containing the current status
     * @return short status text describing the current status
     */
    private static List<LocalizedText> getProvisioningStatusText(
            PropertyHandler ph) {
        List<LocalizedText> messages = Messages.getAll("status_"
                + ph.getFlowState());
        for (LocalizedText message : messages) {
            if (message.getText() == null
                    || (message.getText().startsWith("!") && message.getText()
                            .endsWith("!"))) {
                message.setText(Messages.get(message.getLocale(),
                        "status_INSTANCE_OVERALL"));
            }
        }
        return messages;
    }
}
