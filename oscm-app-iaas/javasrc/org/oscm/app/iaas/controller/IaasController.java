/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Nov 27, 2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.iaas.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.app.iaas.ProcessManagerBean;
import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.iaas.data.FlowState;
import org.oscm.app.iaas.data.Operation;
import org.oscm.app.iaas.i18n.Messages;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.data.InstanceDescription;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.InstanceStatusUsers;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.OperationParameter;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.InstanceExistsException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IaasController extends ProvisioningValidator implements
        APPlatformController {

    private static final Logger logger = LoggerFactory
            .getLogger(IaasController.class);
    private static final String RORControllerID = "ess.ror";
    private static final String RORInstancePrefix = "ror-";
    @EJB
    protected ProcessManagerBean processManager;

    protected APPlatformService platformService;

    protected abstract String getControllerID();

    @PostConstruct
    public void initialize() {
        try {
            platformService = APPlatformServiceFactory.getInstance();
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceDescription createInstance(ProvisioningSettings settings)
            throws APPlatformException {
        try {
            PropertyHandler paramHandler = new PropertyHandler(settings);
            defineOperation(Operation.CREATION, paramHandler);

            // Do some quick checks (before asynchronous provisioning starts;
            // e.g. naming rules)
            validateInstanceName(paramHandler);
            validateExistingInstance(platformService, getControllerID(),
                    paramHandler);
            validateDiskName(paramHandler);
            validateFirewallConfiguration(paramHandler);
            // validate parameters
            if (paramHandler.isVirtualSystemProvisioning()) {
                validateParametersForVsysProvisioning(null, paramHandler);
                paramHandler.setState(FlowState.VSYSTEM_CREATION_REQUESTED);
            } else {
                boolean isAdditionalDiskSelected = isAdditionalDiskSelected(paramHandler);
                validateParametersForVserverProvisioning(null, paramHandler,
                        isAdditionalDiskSelected);
                paramHandler.setState(FlowState.VSERVER_CREATION_REQUESTED);
            }
            // Return generated instance name
            InstanceDescription id = new InstanceDescription();
            id.setInstanceId(getInstancePrefix() + UUID.randomUUID().toString());
            id.setChangedParameters(settings.getParameters());
            id.setChangedAttributes(settings.getAttributes());
            return id;
        } catch (APPlatformException e) {
            // pass on
            throw e;
        } catch (Exception e) {
            logger.error("Error while scheduling VServer instance creation", e);
            APPlatformException exception = getPlatformException(e,
                    "error_provisioning_overall");
            throw exception;
        }
    }

    String getInstancePrefix() {
        String prefix = "";
        if (getControllerID().equals(RORControllerID)) {
            prefix = RORInstancePrefix;
        }
        return prefix;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus deleteInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {

        try {
            PropertyHandler paramHandler = new PropertyHandler(settings);
            defineOperation(Operation.DELETION, paramHandler);
            // Schedule instance deletion
            if (paramHandler.isVirtualSystemProvisioning()) {
                paramHandler.setState(FlowState.VSYSTEM_DELETION_REQUESTED);
            } else {
                paramHandler.setState(FlowState.VSERVER_DELETION_REQUESTED);
            }
            InstanceStatus result = new InstanceStatus();
            result.setChangedParameters(settings.getParameters());
            result.setChangedAttributes(settings.getAttributes());
            return result;
        } catch (Exception e) {
            logger.error("Error while scheduling VSERVER instance deletion", e);
            APPlatformException exception = getPlatformException(e,
                    "error_deletion_overall");
            throw exception;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus modifyInstance(String instanceId,
            ProvisioningSettings currentSettings,
            ProvisioningSettings newSettings) throws APPlatformException {
        try {
            PropertyHandler oldParams = new PropertyHandler(currentSettings);
            PropertyHandler newParams = new PropertyHandler(newSettings);
            validateFirewallConfiguration(newParams);
            defineOperation(Operation.MODIFICATION, oldParams, newParams);
            // Schedule instance modification
            if (newParams.isVirtualSystemProvisioning()) {
                validateParametersForVsysProvisioning(oldParams, newParams);
                newParams.setState(FlowState.VSYSTEM_MODIFICATION_REQUESTED);
            } else {
                boolean isAdditionalDiskSelected = isAdditionalDiskSelected(newParams);
                validateParametersForVserverProvisioning(oldParams, newParams,
                        isAdditionalDiskSelected);
                newParams.setState(FlowState.VSERVER_MODIFICATION_REQUESTED);
            }
            InstanceStatus result = new InstanceStatus();
            result.setChangedParameters(newSettings.getParameters());
            result.setChangedAttributes(newSettings.getAttributes());
            return result;
        } catch (APPlatformException e) {
            throw e;
        } catch (Exception e) {
            logger.error(
                    "Error while scheduling VSERVER instance modification", e);
            APPlatformException exception = getPlatformException(e,
                    "error_modification_overall");
            throw exception;
            /*
             * throw new APPlatformException(
             * Messages.getAll("error_modification_overall"), e);
             */
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus getInstanceStatus(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {
        try {
            PropertyHandler paramHandler = new PropertyHandler(settings);
            // Get & check instance state
            InstanceStatus status = processManager.getControllerInstanceStatus(
                    getControllerID(), instanceId, paramHandler);
            // Update provisioning status description
            status.setDescription(getProvisioningStatusText(paramHandler));
            status.setChangedParameters(settings.getParameters());
            status.setChangedAttributes(settings.getAttributes());
            return status;
        } catch (SuspendException | AbortException | InstanceNotAliveException
                | InstanceExistsException e) {
            // TODO Dirk check changed parameters
            throw e;
        } catch (Exception e) {
            filterStackTrace(e);
            logger.error("Error while checking instance status", e);
            if (logger.isDebugEnabled()) {
                debugHashMap("ConfigSettings", settings.getConfigSettings());
                debugHashMap("Parameters", settings.getParameters());
            }
            APPlatformException exception = getPlatformException(e,
                    "error_status_check");
            exception.setChangedParameters(settings.getParameters());
            throw exception;
        }
    }

    private void filterStackTrace(Exception e) {
        if (e != null) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            List<StackTraceElement> result = new LinkedList<>();
            if (stackTrace != null) {
                boolean startFiltering = false;
                for (StackTraceElement ste : stackTrace) {
                    if (isBss(ste)) {
                        startFiltering = true;
                        result.add(ste);
                    } else {
                        if (!startFiltering) {
                            result.add(ste);
                        }
                    }
                }
                StackTraceElement[] newStackTrace = result
                        .toArray(new StackTraceElement[0]);
                e.setStackTrace(newStackTrace);
            }
        }
    }

    private boolean isBss(StackTraceElement ste) {
        return ste != null && ste.getClassName().startsWith("org.oscm");
    }

    private void debugHashMap(String name, HashMap<String, Setting> map) {
        if (map == null) {
            logger.debug("Map is null: " + name);
            return;
        }
        logger.debug("Contents of map " + name);
        for (String key : map.keySet()) {
            Setting setting = map.get(key);
            String value = setting != null ? setting.getValue() : "";
            if (setting != null && setting.isEncrypted()) {
                logger.debug(key + " => " + value.replaceAll(".", "*"));
            } else {
                logger.debug(key + " => " + value);
            }
        }
    }

    /**
     * Convert given exception into a well designed platform exception.
     * 
     * @param ex
     *            the exception
     * @return the converted platform exception
     */
    private APPlatformException getPlatformException(Throwable ex,
            String messageType) {
        if (ex instanceof EJBException) { // Get real error cause
            if (ex.getCause() != null) {
                ex = ex.getCause();
            } else if (((EJBException) ex).getCausedByException() != null) {
                ex = ((EJBException) ex).getCausedByException();
            }
        }

        if (ex.getClass().getName().endsWith("OViSSException")) { // Get real
                                                                  // error cause
            if (ex.getCause() != null) {
                ex = ex.getCause();
            } else {
                String causeMessage = (ex.getMessage() != null) ? ex
                        .getMessage() : ex.getClass().getName();
                return new APPlatformException(Messages.getAll(messageType,
                        causeMessage));
            }
        }
        if (ex instanceof APPlatformException) {
            return (APPlatformException) ex;
        }
        if (ex instanceof SuspendException) {
            return (SuspendException) ex;
        }

        // Map to platform exception
        String causeMessage = (ex.getMessage() != null) ? ex.getMessage() : ex
                .getClass().getName();
        return new APPlatformException(Messages.getAll(messageType,
                causeMessage));
    }

    /**
     * Returns a small status text for the current provisioning step.
     * 
     * @param paramHandler
     *            property handler containing the current status
     * @return short status text describing the current status
     */
    private List<LocalizedText> getProvisioningStatusText(
            PropertyHandler paramHandler) {
        List<LocalizedText> messages = Messages.getAll("status_"
                + paramHandler.getState());
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

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus notifyInstance(String instanceId,
            ProvisioningSettings settings, Properties properties)
            throws APPlatformException {

        InstanceStatus status = null;
        if (instanceId == null || settings == null || properties == null) {
            return status;
        }
        PropertyHandler propertyHandler = new PropertyHandler(settings);

        if ("finish".equals(properties.get("command"))) {
            // since deletion currently does not have manual steps, the event
            // can be ignored
            if (!propertyHandler.getOperation().isDeletion()) {
                if (FlowState.MANUAL.equals(propertyHandler.getState())) {
                    propertyHandler.setState(FlowState.FINISHED);
                    status = setNotificationStatus(settings, propertyHandler);
                    logger.debug("Got finish event => changing instance status to finished");
                } else {
                    APPlatformException pe = new APPlatformException(
                            "Got finish event but instance is in state "
                                    + propertyHandler.getState()
                                    + " => nothing changed");
                    logger.debug(pe.getMessage());
                    throw pe;
                }
            }
        }
        return status;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus activateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {

        try {
            PropertyHandler paramHandler = new PropertyHandler(settings);
            if (paramHandler.isVirtualSystemProvisioning()) {
                paramHandler.setOperation(Operation.VSYSTEM_ACTIVATION);
                paramHandler.setState(FlowState.VSYSTEM_ACTIVATION_REQUESTED);
            } else {
                paramHandler.setOperation(Operation.VSERVER_ACTIVATION);
                paramHandler.setState(FlowState.VSERVER_ACTIVATION_REQUESTED);
            }
            InstanceStatus result = new InstanceStatus();
            result.setChangedParameters(settings.getParameters());
            result.setChangedAttributes(settings.getAttributes());
            return result;
        } catch (Exception e) {
            logger.error("Error while scheduling instance activation", e);
            throw getPlatformException(e, "error_activation_overall");
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus deactivateInstance(String instanceId,
            ProvisioningSettings settings) throws APPlatformException {

        try {
            PropertyHandler paramHandler = new PropertyHandler(settings);
            if (paramHandler.isVirtualSystemProvisioning()) {
                paramHandler.setOperation(Operation.VSYSTEM_ACTIVATION);
                paramHandler.setState(FlowState.VSYSTEM_DEACTIVATION_REQUESTED);
            } else {
                paramHandler.setOperation(Operation.VSERVER_ACTIVATION);
                paramHandler.setState(FlowState.VSERVER_DEACTIVATION_REQUESTED);
            }
            InstanceStatus result = new InstanceStatus();
            result.setChangedParameters(settings.getParameters());
            result.setChangedAttributes(settings.getAttributes());
            return result;
        } catch (Exception e) {
            logger.error("Error while scheduling instance deactivation", e);
            throw getPlatformException(e, "error_deactivation_overall");
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatusUsers createUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus deleteUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus updateUsers(String instanceId,
            ProvisioningSettings settings, List<ServiceUser> users)
            throws APPlatformException {
        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<LocalizedText> getControllerStatus(ControllerSettings settings)
            throws APPlatformException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<OperationParameter> getOperationParameters(String userId,
            String instanceId, String operationId, ProvisioningSettings settings)
            throws APPlatformException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public InstanceStatus executeServiceOperation(String userId,
            String instanceId, String transactionId, String operationId,
            List<OperationParameter> parameters, ProvisioningSettings settings)
            throws APPlatformException {
        InstanceStatus status = null;
        if (instanceId == null || operationId == null || settings == null) {
            return status;
        }
        PropertyHandler propertyHandler = new PropertyHandler(settings);

        boolean operationAccepted = false;
        switch (operationId) {
        case "START_VIRTUAL_SYSTEM":
            propertyHandler.setState(FlowState.VSYSTEM_START_REQUESTED);
            propertyHandler.setOperation(Operation.VSYSTEM_OPERATION);
            operationAccepted = true;
            break;
        case "STOP_VIRTUAL_SYSTEM":
            propertyHandler.setState(FlowState.VSYSTEM_STOP_REQUESTED);
            propertyHandler.setOperation(Operation.VSYSTEM_OPERATION);
            operationAccepted = true;
            break;
        case "START_VIRTUAL_SERVER":
            propertyHandler.setState(FlowState.VSERVER_START_REQUESTED);
            propertyHandler.setOperation(Operation.VSERVER_OPERATION);
            operationAccepted = true;
            break;
        case "STOP_VIRTUAL_SERVER":
            propertyHandler.setState(FlowState.VSERVER_STOP_REQUESTED);
            propertyHandler.setOperation(Operation.VSERVER_OPERATION);
            operationAccepted = true;
            break;
        default:
            break;
        }
        if (operationAccepted) {
            // when a valid operation has been requested, let the timer
            // handle the instance afterwards
            status = new InstanceStatus();
            status.setRunWithTimer(true);
            status.setIsReady(false);
            // settings changed in propertyHandler
            status.setChangedParameters(settings.getParameters());
            status.setChangedAttributes(settings.getAttributes());
        }
        return status;
    }

    @Override
    public void setControllerSettings(ControllerSettings settings) {
        // not applicable
    }

    private boolean isAdditionalDiskSelected(PropertyHandler paramHandler) {
        boolean selected = false;
        if ((paramHandler.getVDiskSize() != null)
                && (paramHandler.getVDiskSize().length() != 0)) {
            int size = Integer.parseInt(paramHandler.getVDiskSize());
            if (size != 0) {
                selected = true;
            }
        }
        return selected;
    }

    private static boolean isCreateVDiskFound(PropertyHandler oldParams,
            PropertyHandler newParams) {
        boolean selected = false;
        // create vdisk is allowed or not
        if ((newParams.getVDiskSize() != null)
                && (newParams.getVDiskSize().length() != 0)) {
            int size = Integer.parseInt(newParams.getVDiskSize());
            if (size != 0) {
                selected = true;
                if (oldParams.getVDiskSize().equals(newParams.getVDiskSize())) {
                    // do not create again the vdisk
                    selected = false;
                }
            }
        }
        return selected;
    }

    private static boolean isDeleteVDiskFound(PropertyHandler oldParams,
            PropertyHandler newParams) {
        boolean selected = false;
        // only delete virtual disk, if it was created before and new size is
        // zero
        if ((newParams.getVDiskSize() != null)
                && (newParams.getVDiskSize().length() != 0)) {
            int sizeNew = Integer.parseInt(newParams.getVDiskSize());
            if (sizeNew == 0) {
                int sizeOld = Integer.parseInt(oldParams.getVDiskSize());
                if (sizeOld != 0) {
                    // checks if it was created before
                    selected = true;
                }
            }
        }
        return selected;
    }

    public static void defineOperation(Operation type, PropertyHandler params) {
        defineOperation(type, null, params);
    }

    public static void defineOperation(Operation type,
            PropertyHandler oldParams, PropertyHandler newParams) {

        if (Operation.CREATION.equals(type)) {
            if (newParams.isVirtualSystemProvisioning()) {
                newParams.setOperation(Operation.VSYSTEM_CREATION);
            } else {
                newParams.setOperation(Operation.VSERVER_CREATION);
            }
        } else if (Operation.MODIFICATION.equals(type)) {
            if (newParams.isVirtualSystemProvisioning()) {
                newParams.setOperation(Operation.VSYSTEM_MODIFICATION);
            } else {
                newParams.setOperation(Operation.VSERVER_MODIFICATION);
            }
            if (isCreateVDiskFound(oldParams, newParams)) {
                newParams
                        .setOperation(Operation.VSERVER_MODIFICATION_VDISK_CREATION);
            }
            if (isDeleteVDiskFound(oldParams, newParams)) {
                newParams
                        .setOperation(Operation.VSERVER_MODIFICATION_VDISK_DELETION);
            }
        } else if (Operation.DELETION.equals(type)) {
            if (newParams.isVirtualSystemProvisioning()) {
                newParams.setOperation(Operation.VSYSTEM_DELETION);
            } else {
                newParams.setOperation(Operation.VSERVER_DELETION);
            }
        }
    }

    private InstanceStatus setNotificationStatus(ProvisioningSettings settings,
            PropertyHandler propertyHandler) {
        InstanceStatus status;
        status = new InstanceStatus();
        status.setIsReady(true);
        status.setRunWithTimer(true);
        status.setDescription(getProvisioningStatusText(propertyHandler));
        status.setChangedParameters(settings.getParameters());
        status.setChangedAttributes(settings.getAttributes());
        return status;
    }

    private void handleServiceParameters(HashMap<String, Setting> allParams,
            HashMap<String, Setting> serviceParams) {
        for (String paraKey : allParams.keySet()) {
            if (!serviceParams.containsKey(paraKey)) {
                serviceParams.put(paraKey, allParams.get(paraKey));
            }
        }
    }
}
