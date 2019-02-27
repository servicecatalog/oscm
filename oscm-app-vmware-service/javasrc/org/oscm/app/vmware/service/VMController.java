/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.service;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.*;
import org.oscm.app.v2_0.exceptions.*;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.model.VCenter;
import org.oscm.app.vmware.business.statemachine.CreateActions;
import org.oscm.app.vmware.business.statemachine.StateMachine;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.persistence.VMwareCredentials;
import org.oscm.app.vmware.remote.bes.Credentials;
import org.oscm.app.vmware.remote.vmware.VMwareClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Controller implementation for integration of VMWare.
 *
 * @author soehnges
 */
@Stateless(mappedName = "bss/app/controller/" + Controller.ID)
@Remote(APPlatformController.class)
public class VMController implements APPlatformController {

        private static final Logger logger = LoggerFactory
                .getLogger(CreateActions.class);

        private static final String OPERATION_RESTART = "RESTART_VM";
        private static final String OPERATION_START = "START_VM";
        private static final String OPERATION_STOP = "STOP_VM";
        private static final String OPERATION_SNAPSHOT = "SNAPSHOT_VM";
        private static final String OPERATION_RESTORE = "RESTORE_VM";

        protected APPlatformService platformService;

        private VMwareControllerAccess controllerAccess;
        private VMwareCredentials cachedCredentials;
        private VMwareClient client;

        @PostConstruct
        public void initialize() {
                try {
                        platformService = APPlatformServiceFactory
                                .getInstance();
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
                        VMPropertyHandler ph = new VMPropertyHandler(settings);
                        ph.setRequestingUser(settings.getRequestingUser());

                        InstanceDescription id = new InstanceDescription();
                        id.setInstanceId(
                                Long.toString(System.currentTimeMillis()));
                        id.setChangedParameters(settings.getParameters());

                        if (platformService
                                .exists(Controller.ID, id.getInstanceId())) {
                                logger.error(
                                        "Other instance with same name already registered in CTMG: ["
                                                + id.getInstanceId() + "]");
                                throw new APPlatformException(
                                        Messages.getAll("error_instance_exists",
                                                id.getInstanceId()));
                        }

                        validateParameters(null, ph,
                                settings.getOrganizationId(),
                                id.getInstanceId());

                        logger.info("createInstance({})",
                                LogAndExceptionConverter
                                        .getLogText(id.getInstanceId(),
                                                settings));

                        StateMachine.initializeProvisioningSettings(settings,
                                "create_vm.xml");
                        return id;
                } catch (Exception e) {
                        throw LogAndExceptionConverter
                                .createAndLogPlatformException(e,
                                        Context.CREATION);
                }
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public InstanceStatus deleteInstance(String instanceId,
                ProvisioningSettings settings) throws APPlatformException {

                logger.info("deleteInstance({})",
                        LogAndExceptionConverter
                                .getLogText(instanceId, settings));
                try {
                        StateMachine.initializeProvisioningSettings(settings,
                                "delete_vm.xml");
                        InstanceStatus result = new InstanceStatus();
                        result.setChangedParameters(settings.getParameters());
                        return result;
                } catch (Exception e) {
                        throw LogAndExceptionConverter
                                .createAndLogPlatformException(e,
                                        Context.DELETION);
                }
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public InstanceStatus modifyInstance(String instanceId,
                ProvisioningSettings currentSettings,
                ProvisioningSettings newSettings) throws APPlatformException {

                try {
                        VMPropertyHandler currentParameters = new VMPropertyHandler(
                                currentSettings);
                        VMPropertyHandler newParameters = new VMPropertyHandler(
                                newSettings);

                        validateParameters(currentParameters, newParameters,
                                currentSettings.getOrganizationId(),
                                instanceId);

                        newParameters.setTask(null);
                        newParameters.setRequestingUser(
                                newSettings.getRequestingUser());
                        newParameters.setImportOfExistingVM(false);

                        StateMachine.initializeProvisioningSettings(
                                newParameters.getSettings(), "modify_vm.xml");

                        InstanceStatus result = new InstanceStatus();
                        result.setChangedParameters(
                                newSettings.getParameters());
                        return result;
                } catch (Exception e) {
                        throw LogAndExceptionConverter
                                .createAndLogPlatformException(e,
                                        Context.MODIFICATION);
                }
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public InstanceStatus getInstanceStatus(String instanceId,
                ProvisioningSettings settings) throws APPlatformException {
                logger.debug("{}",
                        LogAndExceptionConverter
                                .getLogText(instanceId, settings));

                try {
                        VMPropertyHandler ph = new VMPropertyHandler(settings);
                        InstanceStatus status = new InstanceStatus();
                        StateMachine stateMachine = new StateMachine(settings);
                        stateMachine
                                .executeAction(settings, instanceId, status);
                        updateProvisioningSettings(ph, stateMachine,
                                instanceId);
                        status.setChangedParameters(settings.getParameters());
                        return status;
                } catch (SuspendException e) {
                        throw e;
                } catch (Throwable t) {
                        logger.error(
                                "Failed to get instance status for instance "
                                        + instanceId,
                                t);
                        throw new SuspendException(
                                "Failed to get instance status for instance "
                                        + instanceId,
                                new APPlatformException(t.getMessage()));
                }
        }

        private void updateProvisioningSettings(VMPropertyHandler ph,
                StateMachine stateMachine, String instanceId)
                throws Exception {

                String nextState = stateMachine.getStateId();
                switch (nextState) {
                case "REPEAT_FAILED_STATE":
                        String failedState = stateMachine
                                .loadPreviousStateFromHistory(
                                        ph.getProvisioningSettings());
                        ph.setSetting(VMPropertyHandler.TASK_KEY, "");
                        ph.setSetting(VMPropertyHandler.TASK_STARTTIME, "");
                        ph.setSetting(VMPropertyHandler.SM_STATE, failedState);
                        Credentials cred = ph.getTPUser();
                        platformService
                                .storeServiceInstanceDetails(Controller.ID,
                                        instanceId,
                                        ph.getProvisioningSettings(),
                                        cred.toPasswordAuthentication());
                        String errorMessage = ph
                                .getServiceSetting(
                                        VMPropertyHandler.SM_ERROR_MESSAGE);
                        throw new SuspendException(errorMessage);
                case "ERROR":
                        errorMessage = ph
                                .getServiceSetting(
                                        VMPropertyHandler.SM_ERROR_MESSAGE);
                        throw new SuspendException(errorMessage);
                default:
                        ph.setSetting(VMPropertyHandler.SM_STATE_HISTORY,
                                stateMachine.getHistory());
                        ph.setSetting(VMPropertyHandler.SM_STATE, nextState);
                        break;
                }
        }

        /**
         * Validates the given parameters before contacting VMware API. When both
         * oldParams and newParams are set, also modification rules (e.g. no disk
         * reduce) are checked.
         *
         * @param currentParameters the existing parameters (optional)
         * @param newParameters     the requested parameters
         * @throws APPlatformException thrown when validation fails
         */
        private void validateParameters(VMPropertyHandler currentParameters,
                VMPropertyHandler newParameters, String customerOrgId,
                String instanceId) throws APPlatformException {

                validateMemory(newParameters);
                validateDataDiskMountPoints(newParameters);

                if (currentParameters == null) {
                        return;
                }
                validateDataDiskSizeReduction(currentParameters, newParameters);
                validateSystemDiskSizeReduction(currentParameters,
                        newParameters);
                validateWindowsSettings(newParameters);
        }

        void validateDataDiskMountPoints(VMPropertyHandler newParameters)
                throws APPlatformException {

                for (String mointPointKey : newParameters
                        .getDataDiskMountPointParameterKeys()) {

                        String mountPoint = newParameters
                                .getServiceSetting(mointPointKey);
                        String validationPattern = newParameters
                                .getMountPointValidationPattern(mointPointKey);

                        if (validationPattern == null
                                || validationPattern.length() == 0) {
                                continue;
                        }

                        if (mountPoint == null || !mountPoint
                                .matches(validationPattern)) {
                                logger.debug(
                                        "Invalid data disk mount point: {}, {}, {}",
                                        mointPointKey, mountPoint,
                                        validationPattern);
                                throw new APPlatformException(
                                        Messages.getAll(
                                                "error_invalid_data_disk_mount_point",
                                                mointPointKey,
                                                mountPoint,
                                                validationPattern));
                        }
                }
        }

        private void validateMemory(VMPropertyHandler newParameters)
                throws APPlatformException {

                long memory = newParameters.getConfigMemoryMB();
                if (memory % 4 != 0) {
                        logger.debug(
                                "Validation error on memory size [" + memory
                                        + "MB]");
                        throw new APPlatformException(
                                Messages.getAll("error_invalid_memory",
                                        Long.valueOf(memory)));
                }
        }

        private void validateDataDiskSizeReduction(
                VMPropertyHandler currentParameters,
                VMPropertyHandler newParameters) throws APPlatformException {

                boolean diskSizeReduction = false;
                Double[] oldDataDisksMB = currentParameters.getDataDisksMB();
                Double[] newDataDisksMB = newParameters.getDataDisksMB();
                if (oldDataDisksMB.length > newDataDisksMB.length) {
                        logger.warn(
                                "Reducing the number of data disks is not possible. instanceId: "
                                        + currentParameters.getInstanceName()
                                        + " old number: "
                                        + currentParameters
                                        .getDataDisksMB().length
                                        + " new number: "
                                        + newParameters
                                        .getDataDisksMB().length);
                        diskSizeReduction = true;
                } else if (oldDataDisksMB.length >= newDataDisksMB.length) {
                        for (int i = 0; i < oldDataDisksMB.length; i++) {
                                Double dataDiskMB = oldDataDisksMB[i];
                                if (dataDiskMB.longValue() > newDataDisksMB[i]
                                        .longValue()) {
                                        diskSizeReduction = true;
                                        logger.error(
                                                "Data disk size reduction is not possible. instanceId: "
                                                        + currentParameters
                                                        .getInstanceName()
                                                        + " old size: "
                                                        + currentParameters
                                                        .getConfigDiskSpaceMB()
                                                        + " new size: "
                                                        + newParameters
                                                        .getConfigDiskSpaceMB());
                                        break;
                                }
                        }
                }
                if (diskSizeReduction) {
                        throw new APPlatformException(
                                Messages.getAll(
                                        "error_invalid_diskspacereduction"));
                }
        }

        private void validateSystemDiskSizeReduction(
                VMPropertyHandler currentParameters,
                VMPropertyHandler newParameters) throws APPlatformException {
                if (currentParameters.getConfigDiskSpaceMB() > newParameters
                        .getConfigDiskSpaceMB()) {
                        logger.error(
                                "System disk size reduction is not possible. old size: "
                                        + currentParameters
                                        .getConfigDiskSpaceMB()
                                        + " new size: "
                                        + newParameters.getConfigDiskSpaceMB());
                        throw new APPlatformException(
                                Messages.getAll(
                                        "error_invalid_diskspacereduction"));
                }
        }

        private void validateWindowsSettings(VMPropertyHandler params)
                throws APPlatformException {

                boolean isDomainJoin = params
                        .isServiceSettingTrue(
                                VMPropertyHandler.TS_WINDOWS_DOMAIN_JOIN);
                boolean domainName = params
                        .getServiceSetting(VMPropertyHandler.TS_DOMAIN_NAME)
                        != null;
                boolean admin = params.getServiceSetting(
                        VMPropertyHandler.TS_WINDOWS_DOMAIN_ADMIN) != null;
                boolean adminPwd = params.getServiceSetting(
                        VMPropertyHandler.TS_WINDOWS_DOMAIN_ADMIN_PWD) != null;
                logger.debug("isDomainJoin: " + isDomainJoin);

                if (isDomainJoin && !domainName) {
                        throw new APPlatformException(
                                Messages.getAll("error_missing_domain"));
                }
                if (isDomainJoin && !admin) {
                        throw new APPlatformException(
                                Messages.getAll("error_missing_domain_admin"));
                }
                if (isDomainJoin && !adminPwd) {
                        throw new APPlatformException(
                                Messages.getAll(
                                        "error_missing_domain_admin_pwd"));
                }
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public InstanceStatus notifyInstance(String instanceId,
                ProvisioningSettings settings, Properties properties)
                throws APPlatformException {

                logger.info("notifyInstance({})",
                        LogAndExceptionConverter
                                .getLogText(instanceId, settings));
                InstanceStatus status = null;
                if (instanceId == null || settings == null
                        || properties == null) {
                        return status;
                }
                try {
                        if ("finish".equals(properties.get("command"))) {
                                status = new InstanceStatus();
                                status.setIsReady(false);
                                status.setRunWithTimer(true);
                                status.setChangedParameters(
                                        settings.getParameters());
                                logger.debug(
                                        "Received finish event. Instance provisioning will be continued for instance "
                                                + instanceId);
                        }
                        return status;
                } catch (Throwable t) {
                        throw LogAndExceptionConverter
                                .createAndLogPlatformException(t,
                                        Context.STATUS);
                }
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public InstanceStatus activateInstance(String instanceId,
                ProvisioningSettings settings) throws APPlatformException {
                logger.info("activateInstance({})",
                        LogAndExceptionConverter
                                .getLogText(instanceId, settings));
                try {
                        StateMachine.initializeProvisioningSettings(settings,
                                "activate_vm.xml");
                        InstanceStatus result = new InstanceStatus();
                        result.setChangedParameters(settings.getParameters());
                        return result;
                } catch (Throwable t) {
                        throw LogAndExceptionConverter
                                .createAndLogPlatformException(t,
                                        Context.ACTIVATION);
                }
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public InstanceStatus deactivateInstance(String instanceId,
                ProvisioningSettings settings) throws APPlatformException {
                logger.info("deactivateInstance({})",
                        LogAndExceptionConverter
                                .getLogText(instanceId, settings));
                try {
                        StateMachine.initializeProvisioningSettings(settings,
                                "deactivate_vm.xml");
                        InstanceStatus result = new InstanceStatus();
                        result.setChangedParameters(settings.getParameters());
                        return result;
                } catch (Throwable t) {
                        throw LogAndExceptionConverter
                                .createAndLogPlatformException(t,
                                        Context.DEACTIVATION);
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
        public InstanceStatus executeServiceOperation(String userId,
                String instanceId, String transactionId, String operationId,
                List<OperationParameter> parameters,
                ProvisioningSettings settings)
                throws APPlatformException {

                try {
                        switch (operationId) {
                        case OPERATION_RESTART:
                                StateMachine.initializeProvisioningSettings(
                                        settings,
                                        "restart_vm.xml");
                                break;
                        case OPERATION_START:
                                StateMachine.initializeProvisioningSettings(
                                        settings,
                                        "start_vm.xml");
                                break;
                        case OPERATION_STOP:
                                StateMachine.initializeProvisioningSettings(
                                        settings,
                                        "stop_vm.xml");
                                break;
                        case OPERATION_SNAPSHOT:
                                StateMachine.initializeProvisioningSettings(
                                        settings,
                                        "snapshot_vm.xml");
                                break;
                        case OPERATION_RESTORE:
                                StateMachine.initializeProvisioningSettings(
                                        settings,
                                        "restore_vm.xml");
                                break;
                        }
                        InstanceStatus result = new InstanceStatus();
                        result.setChangedParameters(settings.getParameters());
                        return result;
                } catch (Exception t) {
                        logger.debug("Failed to execute service operation "
                                + operationId
                                + " for instance " + instanceId + " and user "
                                + userId);
                        throw LogAndExceptionConverter
                                .createAndLogPlatformException(t,
                                        Context.OPERATION);
                }

        }

        @Override
        public boolean ping(String controllerId)
                throws ServiceNotReachableException {

                boolean pingResult = false;
                ServiceNotReachableException exception;
                client = getClient();

                try {
                        client.connect();
                        pingResult = client.isConnected();
                } catch (Exception e) {
                        logger.error("Unable to connect to VMware service", e);
                        exception = new ServiceNotReachableException(
                                "Unable to connect to VMWare service");
                        exception.setStackTrace(e.getStackTrace());
                        throw exception;
                }

                return pingResult;
        }

        @Override
        public boolean canPing() throws ConfigurationException {
                HashMap<String, Setting> controllerSettings = getControllerSettings();
                VCenter vCenter = getVCenter(controllerSettings);
                boolean areParamsValid = false;

                if (vCenter != null)
                        areParamsValid = validateControllerParams(vCenter);

                if (areParamsValid)
                        cacheCredentials(vCenter);
                return areParamsValid;
        }

        /**
         * Fetches controller setting from platformService.
         * Throws an error when config is unable to obtain or is incomplete
         *
         * @return Controller settings map
         * @throws ConfigurationException
         */
        private HashMap<String, Setting> getControllerSettings()
                throws ConfigurationException {

                ConfigurationException exception;
                HashMap<String, Setting> controllerSettings = null;

                try {
                        PasswordAuthentication tpUser = getPasswordAuthentication();
                        controllerSettings = platformService
                                .getControllerSettings(
                                        Controller.ID, tpUser);
                } catch (APPlatformException e) {
                        exception = new ConfigurationException(
                                "Unable to get controller settings");
                        exception.setStackTrace(e.getStackTrace());
                        throw exception;
                }

                if (controllerSettings == null) {
                        exception = new ConfigurationException(
                                "Unable to get controller settings. Controller settings are null");
                        throw exception;
                } else if (controllerSettings.isEmpty()) {
                        exception = new ConfigurationException(
                                "Unable to get controller settings. Controller settings map is empty");
                        throw exception;
                }

                return controllerSettings;
        }

        /**
         * Gets password authentication data for current user from session data
         *
         * @return Password Authentication object
         */
        private PasswordAuthentication getPasswordAuthentication() {
                FacesContext facesContext = getContext();
                HttpSession session = (HttpSession) facesContext
                        .getExternalContext()
                        .getSession(false);
                Object userId = session.getAttribute("loggedInUserId");
                Object password = session
                        .getAttribute("loggedInUserPassword");

                return new PasswordAuthentication(
                        userId.toString(), password.toString());
        }

        /**
         * Gets first available VCenter config
         *
         * @param controllerSettings
         * @return VCenter
         * @throws ConfigurationException
         */
        private VCenter getVCenter(
                HashMap<String, Setting> controllerSettings)
                throws ConfigurationException {

                ConfigurationException exception;

                List<VCenter> vCenters = getVCenterList(controllerSettings);

                if (vCenters == null) {
                        exception = new ConfigurationException(
                                "Unable to get controller settings. VCenter list is null");
                        throw exception;
                } else if (vCenters.isEmpty()) {
                        exception = new ConfigurationException(
                                "Unable to get controller settings. VCenter list is empty");
                        throw exception;
                }

                return vCenters.get(0);
        }

        /**
         * Fetches list of available vCenters for provided controller settings
         *
         * @param controllerSettings
         * @return
         */
        protected List<VCenter> getVCenterList(
                HashMap<String, Setting> controllerSettings) {
                ProvisioningSettings settings = new ProvisioningSettings(
                        new HashMap<>(), controllerSettings,
                        Messages.DEFAULT_LOCALE);

                VMPropertyHandler propertyHandler = new VMPropertyHandler(
                        settings);

                return propertyHandler.getTargetVCenter();
        }

        private boolean validateControllerParams(VCenter vCenter)
                throws ConfigurationException {

                if (checkIfParamsAreNull(vCenter)) {
                        return checkIfParamsAreEmpty(vCenter);

                } else
                        return false;

        }

        /**
         * Checks if config params for provided VCenter are not null
         *
         * @param vCenter
         * @return true - if checks passes
         * @throws ConfigurationException
         */

        private boolean checkIfParamsAreNull(VCenter vCenter)
                throws ConfigurationException {
                if (vCenter.getUrl() == null
                        || vCenter.getUserid() == null
                        || vCenter.getPassword() == null) {
                        throw new ConfigurationException(
                                "One of controller config parameters is null");
                } else
                        return true;
        }

        /**
         * Checks if config params for provided VCenter are not empty
         *
         * @param vCenter
         * @return
         * @throws ConfigurationException
         */
        private boolean checkIfParamsAreEmpty(VCenter vCenter)
                throws ConfigurationException {
                if (vCenter.getUrl().isEmpty()
                        || vCenter.getUserid().isEmpty()
                        || vCenter.getPassword().isEmpty()) {
                        throw new ConfigurationException(
                                "One of controller config parameters is empty");
                } else {
                        return true;
                }
        }

        /**
         * Copies acquired vCenter credentials to external variable
         *
         * @param vCenter
         */
        private void cacheCredentials(VCenter vCenter) {
                cachedCredentials = new VMwareCredentials(
                        vCenter.getUrl(),
                        vCenter.getUserid(),
                        vCenter.getPassword()
                );
        }

        /**
         * Gets current instance of FacesContext
         *
         * @return Current FacesContext
         */
        protected FacesContext getContext() {
                return FacesContext.getCurrentInstance();
        }

        /**
         * Gets VMware client instance for provided credentials
         *
         * @return VMWare Client
         */
        protected VMwareClient getClient() {
                if (client == null)
                        return new VMwareClient(cachedCredentials);
                else
                        return client;
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public List<LocalizedText> getControllerStatus(ControllerSettings arg0)
                throws APPlatformException {
                return null;
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public List<OperationParameter> getOperationParameters(String arg0,
                String arg1, String arg2, ProvisioningSettings arg3)
                throws APPlatformException {
                return null;
        }

        @Override
        public void setControllerSettings(ControllerSettings settings) {
                if (controllerAccess != null) {
                        controllerAccess.storeSettings(settings);
                }
        }

        @Inject
        public void setControllerAccess(final ControllerAccess access) {
                this.controllerAccess = (VMwareControllerAccess) access;
        }
}