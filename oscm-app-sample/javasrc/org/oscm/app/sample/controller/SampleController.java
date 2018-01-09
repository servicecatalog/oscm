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

package org.oscm.app.sample.controller;

import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.app.sample.i18n.Messages;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.data.InstanceDescription;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.InstanceStatusUsers;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.OperationParameter;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample implementation of a service controller based on the Asynchronous
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
@Stateless(mappedName = "bss/app/controller/ess.sample")
@Remote(APPlatformController.class)
public class SampleController implements APPlatformController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SampleController.class);

    public static final String ID = "ess.sample";

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
        LOGGER.debug("SampleController @PostConstruct");
        try {
            platformService = APPlatformServiceFactory.getInstance();
        } catch (IllegalStateException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
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
    public InstanceDescription createInstance(ProvisioningSettings settings)
            throws APPlatformException {

        // Set status to store for application instance
        PropertyHandler paramHandler = new PropertyHandler(settings);
        paramHandler.setState(Status.CREATION_REQUESTED);

        // Return generated instance information
        InstanceDescription id = new InstanceDescription();
        id.setInstanceId("Instance_" + System.currentTimeMillis());
        id.setChangedParameters(settings.getParameters());
        id.setChangedAttributes(settings.getAttributes());
        return id;
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

        // Set status to store for application instance
        PropertyHandler paramHandler = new PropertyHandler(settings);
        paramHandler.setState(Status.DELETION_REQUESTED);

        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(settings.getParameters());
        result.setChangedAttributes(settings.getAttributes());
        return result;
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

        PropertyHandler paramHandler = new PropertyHandler(newSettings);

        // Validate new settings
        validateParameters(paramHandler);

        // Set status to store for application instance
        paramHandler.setState(Status.MODIFICATION_REQUESTED);

        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(newSettings.getParameters());
        result.setChangedAttributes(newSettings.getAttributes());
        return result;
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

        PropertyHandler paramHandler = new PropertyHandler(settings);

        // Get and check instance status
        Dispatcher dp = new Dispatcher(platformService, instanceId,
                paramHandler);
        InstanceStatus status = dp.dispatch();

        return status;
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
        return null;
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

        // Set status to store for application instance
        PropertyHandler paramHandler = new PropertyHandler(settings);
        paramHandler.setState(Status.ACTIVATION_REQUESTED);

        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(settings.getParameters());
        result.setChangedAttributes(settings.getAttributes());
        return result;
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

        // Set status to store for application instance
        PropertyHandler paramHandler = new PropertyHandler(settings);
        paramHandler.setState(Status.DEACTIVATION_REQUESTED);

        InstanceStatus result = new InstanceStatus();
        result.setChangedParameters(settings.getParameters());
        result.setChangedAttributes(settings.getAttributes());
        return result;
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
        return null;
    }

    /**
     * Validates the given service parameters and configuration settings.
     * 
     * @param newParams
     *            the parameters to check
     * @throws APPlatformException
     */
    private void validateParameters(PropertyHandler newParams)
            throws APPlatformException {

        if (newParams.getEMail() == null || newParams.getEMail().length() < 5)
            throw new APPlatformException(
                    Messages.getAll("error_missing_email"));

        if (newParams.getMessage() == null
                || newParams.getMessage().length() < 5)
            throw new APPlatformException(
                    Messages.getAll("error_missing_message"));

        if (newParams.getUser() == null || newParams.getUser().length() < 5)
            throw new APPlatformException(
                    Messages.getAll("error_missing_user"));

        if (newParams.getPassword() == null
                || newParams.getPassword().length() < 5)
            throw new APPlatformException(
                    Messages.getAll("error_missing_password"));

    }

    @Override
    public List<LocalizedText> getControllerStatus(ControllerSettings settings)
            throws APPlatformException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<OperationParameter> getOperationParameters(String userId,
            String instanceId, String operationId,
            ProvisioningSettings settings) throws APPlatformException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InstanceStatus executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters, ProvisioningSettings settings)
            throws APPlatformException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setControllerSettings(ControllerSettings settings) {
        // not applicable
    }

}
