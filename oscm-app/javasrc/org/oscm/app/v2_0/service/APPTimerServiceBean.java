/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;

import org.oscm.app.business.APPlatformControllerFactory;
import org.oscm.app.business.InstanceFilter;
import org.oscm.app.business.ProductProvisioningServiceFactoryBean;
import org.oscm.app.business.exceptions.BESNotificationException;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.PlatformConfigurationKey;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.data.InstanceStatus;
import org.oscm.app.v2_0.data.LocalizedText;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AbortException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.exceptions.ControllerLookupException;
import org.oscm.app.v2_0.exceptions.InstanceExistsException;
import org.oscm.app.v2_0.exceptions.InstanceNotAliveException;
import org.oscm.app.v2_0.exceptions.SuspendException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.operation.data.OperationResult;
import org.oscm.provisioning.data.BaseResult;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceRequest;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.intf.ProvisioningService;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.OperationStatus;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.vo.VOUserDetails;

/**
 * The timer service implementation
 * 
 * @author Mike J&auml;ger
 * 
 */
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Singleton
@LocalBean
public class APPTimerServiceBean implements Cloneable {

    private static final String EVENT_KEY_NOTIFY = "notify";
    private static final String EVENT_KEY_RESUME = "_resume";
    private static final String EVENT_VALUE_YES = "yes";
    private static final long DEFAULT_TIMER_INTERVAL = 15000;

    protected APPTimerServiceBean appTimerServiceBean = null;

    public Object TIMER_LOCK = new Object();

    /**
     * Used to identify the timer service
     */
    private static final String APP_TIMER_INFO = "d432dac0-5f81-11e4-9803-0800200c9a66";

    @Inject
    protected transient Logger logger;

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    @Resource
    protected TimerService timerService;

    @EJB
    protected APPConfigurationServiceBean configService;

    @EJB
    protected APPCommunicationServiceBean mailService;

    @EJB
    protected BesDAO besDAO;

    @EJB
    protected OperationServiceBean opBean;

    @EJB
    protected ServiceInstanceDAO instanceDAO;

    @EJB
    protected OperationDAO operationDAO;

    @EJB
    protected ProductProvisioningServiceFactoryBean provServFact;

    @PostConstruct
    public void init() {
        try {
            appTimerServiceBean = (APPTimerServiceBean) this.clone();
        } catch (CloneNotSupportedException e) {
            // ignore since class is cloneable
        }
    }

    public void initTimers() {
        synchronized (TIMER_LOCK) {
            logger.info("Timer initialization start");
            appTimerServiceBean.initTimers_internal();
            logger.info("Timer initialization finished");
        }
    }

    /**
     * Initialize the timer for polling for the services
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void initTimers_internal() {
        Collection<Timer> timers = timerService.getTimers();
        boolean appTimerExist = false;
        for (Timer timerAPP : timers) {
        	if (APP_TIMER_INFO.equals(timerAPP.getInfo())) {
        		appTimerExist = true;
        	}
        }
        if (!appTimerExist) {
            logger.info("Timer create.");
            try {
                String timerIntervalSetting = configService
                        .getProxyConfigurationSetting(
                                PlatformConfigurationKey.APP_TIMER_INTERVAL);
                long interval = Long.parseLong(timerIntervalSetting);
                timerService.createTimer(0, interval, APP_TIMER_INFO);
            } catch (ConfigurationException e) {
                timerService.createTimer(0, DEFAULT_TIMER_INTERVAL,
                        APP_TIMER_INFO);
                logger.info(
                        "Timer interval not set, switch to default 15 sec.");
            }
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cancelTimers() {
        Collection<Timer> timers = timerService.getTimers();
        for (Timer th : timers) {
            if (APP_TIMER_INFO.equals(th.getInfo())) {
                th.cancel();
                return;
            }
        }
    }

    /**
     * Handles the timers as soon as they are expired and the container invokes
     * this callback method. If the timer is not a periodic timer, it will also
     * be re-initialized.
     * 
     * @param timer
     *            The expired timer provided by the system.
     */
    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void handleTimer(Timer timer) {
        // must never throw an exception or throwable, as the timer will be
        // deactivated then.
        List<ServiceInstance> result;
        synchronized (TIMER_LOCK) {
            result = instanceDAO.getInstancesInWaitingState();
            // If no service is waiting, we can stop the timer
            if (result.isEmpty() || configService.isAPPSuspend()) {
                appTimerServiceBean.cancelTimers();
                logger.info("Timer canceled.");
                return;
            }
        }

        final String ERROR_TIMER = "Error occured during timer handling";
        try {
            doHandleSystems(result, ProvisioningStatus.getWaitingForCreation());
        } catch (Throwable e) {
            logger.error(ERROR_TIMER, e);
        }
        try {
            doHandleSystems(result,
                    ProvisioningStatus.getWaitingForModification());
        } catch (Throwable e) {
            logger.error(ERROR_TIMER, e);
        }

        try {
            doHandleSystems(result,
                    ProvisioningStatus.getWaitingForActivation());
        } catch (Throwable e) {
            logger.error(ERROR_TIMER, e);
        }

        try {
            doHandleSystems(result,
                    ProvisioningStatus.getWaitingForDeactivation());
        } catch (Throwable e) {
            logger.error(ERROR_TIMER, e);
        }

        try {
            doHandleSystems(result, ProvisioningStatus.getWaitingForDeletion());
        } catch (Throwable e) {
            logger.error(ERROR_TIMER, e);
        }

        try {
            doHandleSystems(result,
                    ProvisioningStatus.getWaitingForOperation());
        } catch (Throwable e) {
            logger.error(ERROR_TIMER, e);
        }

    }

    List<ServiceInstance> filterList(List<?> result,
            EnumSet<ProvisioningStatus> status) {
        List<ServiceInstance> filteredList = new ArrayList<>();
        for (Object entry : result) {
            ServiceInstance currentSI = (ServiceInstance) entry;
            if (status.contains(currentSI.getProvisioningStatus())) {
                filteredList.add(currentSI);
            }
        }
        return filteredList;
    }

    void doHandleSystems(List<?> result,
            EnumSet<ProvisioningStatus> provisioningStatus) {

        List<ServiceInstance> serviceInstanceList = filterList(result,
                provisioningStatus);

        for (ServiceInstance currentSI : serviceInstanceList) {
            if (configService.isAPPSuspend()) {
                return;
            } else {
                if (currentSI.isInstanceProvisioning()) {
                    doHandleInstanceProvisioning(currentSI);
                } else {
                    doHandleControllerProvisioning(currentSI);
                }
            }
        }

        return;
    }

    void doHandleControllerProvisioning(ServiceInstance serviceInstance) {
        final ProvisioningStatus provisioningStatus = serviceInstance
                .getProvisioningStatus();
        HashMap<String, Setting> changedParameters = new HashMap<>();

        try {
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(serviceInstance, null, true);
            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(serviceInstance.getControllerId());
            InstanceStatus instanceStatus = controller.getInstanceStatus(
                    serviceInstance.getInstanceId(), settings);
            em.refresh(serviceInstance);
            // run with timer status can be corrected in suspend case
            serviceInstance.updateStatus(em, instanceStatus);

            changedParameters = instanceStatus.getChangedParameters();
            if (changedParameters == null) {
                changedParameters = new HashMap<>();
            }

            if (!instanceStatus.isReady()) {
                if (provisioningStatus.isWaitingForCreation()) {
                    besDAO.notifyOnProvisioningStatusUpdate(serviceInstance,
                            instanceStatus.getDescription());
                } else if (provisioningStatus.isWaitingForOperation()) {
                    Operation operation = operationDAO.getOperationByInstanceId(
                            serviceInstance.getInstanceId());
                    if (operation != null) {
                        besDAO.notifyAsyncOperationStatus(serviceInstance,
                                operation.getTransactionId(),
                                OperationStatus.RUNNING,
                                instanceStatus.getDescription());
                    }
                }
            } else {
                if (provisioningStatus.isWaitingForDeletion()) {
                    em.remove(serviceInstance);
                    return;
                }

                if (instanceStatus.isInstanceProvisioningRequested()) {
                    Setting publicIp = null;
                    if (changedParameters
                            .containsKey(InstanceParameter.PUBLIC_IP)) {
                        publicIp = changedParameters
                                .get(InstanceParameter.PUBLIC_IP);
                    } else {
                        InstanceParameter publicIpParam = serviceInstance
                                .getParameterForKey(
                                        InstanceParameter.PUBLIC_IP);
                        publicIp = publicIpParam == null ? null
                                : new Setting(publicIpParam.getParameterKey(),
                                        publicIpParam.getDecryptedValue(),
                                        publicIpParam.isEncrypted());
                    }
                    if (publicIp == null
                            || Strings.isEmpty(publicIp.getValue())) {
                        // no IP for instance provisioning available
                        suspendServiceInstance(serviceInstance,
                                new IllegalStateException(
                                        "Instance status is ready and instance provisioning has been requested, but there is no public IP available for the instance."),
                                provisioningStatus.getSuspendMailMessage(),
                                true);
                        instanceStatus.setIsReady(false);
                    }
                } else {
                    InstanceResult instanceResult = createInstanceResult(
                            instanceStatus);
                    notifyOnProvisioningCompletion(serviceInstance,
                            instanceResult);
                    serviceInstance.setProvisioningStatus(
                            ProvisioningStatus.COMPLETED);
                }
                serviceInstance.setServiceBaseURL(instanceStatus.getBaseUrl());
                serviceInstance
                        .setServiceAccessInfo(instanceStatus.getAccessInfo());
                serviceInstance
                        .setServiceLoginPath(instanceStatus.getLoginPath());
            }

            serviceInstance.setInstanceParameters(changedParameters);
            serviceInstance.setControllerReady(instanceStatus.isReady());
            serviceInstance.setInstanceProvisioning(
                    instanceStatus.isInstanceProvisioningRequested());
            em.persist(serviceInstance);
            if (serviceInstance.getProvisioningStatus().isCompleted()) {
                OperationResult result = opBean
                        .executeServiceOperationFromQueue(
                                serviceInstance.getInstanceId());
                if (result.getErrorMessage() == null) {
                    try {
                        ServiceInstance si = instanceDAO.getInstanceById(
                                serviceInstance.getInstanceId());
                        if (si.getProvisioningStatus()
                                .isWaitingForOperation()) {
                            initTimers();
                        }
                    } catch (ServiceInstanceNotFoundException e) {
                        // could not happen, otherwise result cannot be null
                    }
                }
            }
        } catch (ControllerLookupException | EntityNotFoundException e) {
            logger.error(e.getMessage());
            return;
        } catch (BESNotificationException bne) {
            handleBESNotificationException(serviceInstance, provisioningStatus,
                    changedParameters, bne);
        } catch (InstanceNotAliveException inae) {
            handleInstanceNotAliveException(serviceInstance, changedParameters,
                    inae);

            Operation operation = operationDAO
                    .getOperationByInstanceId(serviceInstance.getInstanceId());
            if (operation != null) {
                try {
                    besDAO.notifyAsyncOperationStatus(serviceInstance,
                            operation.getTransactionId(), OperationStatus.ERROR,
                            inae.getLocalizedMessages());
                    besDAO.notifyInstanceStatusOfAsyncOperation(
                            serviceInstance);
                } catch (BESNotificationException bne) {
                    handleBESNotificationException(serviceInstance,
                            provisioningStatus, changedParameters, bne);
                }
            }
        } catch (InstanceExistsException inae) {
            sendInfoMail(true, serviceInstance, "mail_create_instance_exists",
                    inae);
            handleException(serviceInstance, provisioningStatus, inae);
        } catch (SuspendException se) {
            handleSuspendException(serviceInstance, provisioningStatus, se);
            Operation operation = operationDAO
                    .getOperationByInstanceId(serviceInstance.getInstanceId());
            if (operation != null) {
                try {
                    besDAO.notifyAsyncOperationStatus(serviceInstance,
                            operation.getTransactionId(), OperationStatus.ERROR,
                            se.getLocalizedMessages());
                } catch (BESNotificationException bne) {
                    handleBESNotificationException(serviceInstance,
                            provisioningStatus, changedParameters, bne);
                }
            }
        } catch (Exception e) {
            // TODO analyze exception
            handleException(serviceInstance, provisioningStatus, e);
        }
    }

    void handleInstanceNotAliveException(ServiceInstance currentSI,
            HashMap<String, Setting> changedParameters,
            InstanceNotAliveException bne) {

        updateParameterMapSafe(currentSI, changedParameters);

        // Disable timer of this service
        currentSI.setRunWithTimer(false);
        currentSI.setLocked(false);
        em.flush();

        sendInfoMail(true, currentSI, "mail_suspend_error_instance_not_found",
                bne);
    }

    void handleBESNotificationException(ServiceInstance currentSI,
            final ProvisioningStatus instanceProvStatus,
            HashMap<String, Setting> changedParameters,
            BESNotificationException bne) {
        // write the parameters back
        updateParameterMapSafe(currentSI, changedParameters);
        if (bne.getCause() instanceof ObjectNotFoundException) {
            logger.info("Subscription with id " + currentSI.getSubscriptionId()
                    + " for service instance " + currentSI.getInstanceId()
                    + " already terminated.");
            currentSI.markForDeletion();
        }
        // suspend process and inform APP admin with mail

        if (besDAO.isCausedByConnectionException(bne)) {
            suspendApp(currentSI, "mail_bes_notification_error_app_admin");

        } else {
            suspendServiceInstance(currentSI, bne.getCause(),
                    instanceProvStatus.getBesNotificationErrorMailMessage(),
                    true, true);
        }

    }

    void suspendApp(ServiceInstance currentSI, String msgKey) {
        currentSI.setSuspendedByApp(true);
        currentSI.setRunWithTimer(false);
        currentSI.setLocked(false);
        configService.setAPPSuspend(Boolean.TRUE.toString());
        sendMailToAppAdmin(msgKey);
    }

    void sendMailToAppAdmin(String msgKey) {
        StringBuffer eventLink = new StringBuffer();
        try {
            eventLink.append(configService.getProxyConfigurationSetting(
                    PlatformConfigurationKey.APP_BASE_URL));
        } catch (Exception e) {
            logger.error(
                    "Failure during generation of link for error mail with message '{}'",
                    e.getMessage());
            return;
        }
        sendActionMailToAppAdmin(msgKey, eventLink.toString());
    }

    void sendActionMailToAppAdmin(String msgKey, String actionLink) {
        List<VOUserDetails> mailUsers = new ArrayList<>();
        try {
            mailUsers.add(configService.getAPPAdministrator());
        } catch (ConfigurationException e) {
            logger.warn(
                    "APP administrator mail not configured. Update the configuration setting: "
                            + PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS
                                    .name());
        }
        if (!mailUsers.isEmpty()) {
            for (VOUserDetails user : mailUsers) {
                String locale = user.getLocale();
                String subject = getMailSubject(locale, msgKey, null);
                String body = "";
                if (actionLink == null) {
                    body = getMailBodyForInfo(locale, msgKey, null, null);
                } else {
                    body = getMailBodyForAction(locale, msgKey, null, null,
                            actionLink, false);
                }
                try {
                    mailService.sendMail(Arrays.asList(user.getEMail()),
                            subject, body);
                } catch (APPlatformException pe) {
                    String causeStr = "unknown";
                    if (pe.getCause() != null) {
                        causeStr = pe.getCause().getMessage();
                    }
                    logger.error(
                            "Failure during error mail notification with message '{}'",
                            pe.getMessage() + " [Cause: " + causeStr + "]");
                }
            }
        }
    }

    void handleException(ServiceInstance currentSI,
            final ProvisioningStatus instanceProvStatus, Exception e) {
        APPlatformException cause = getPlatformException(e);
        logger.error("handlingexception");
        logger.error(e.getMessage());
        logger.error(instanceProvStatus.name());
        logger.warn(
                "Failure during processing for service instance '{}' with message '{}'",
                currentSI.getIdentifier(), cause.getMessage());

        if (e.getCause() instanceof NamingException) {
            Logger logger = this.getLogger();
            logger.error(cause.getMessage());
            return;
        }

        if (instanceProvStatus.isWaitingForCreation()) {
            logger.error("waitingforcreation");
            InstanceResult instanceResult = new InstanceResult();
            instanceResult.setRc(0);
            instanceResult.setInstance(new InstanceInfo());
            try {
                notifyOnProvisioningAbortion(currentSI, instanceResult, cause);
                em.remove(currentSI);
            } catch (BESNotificationException bne) {
                handleBESNotificationException(currentSI, instanceProvStatus,
                        cause.getChangedParameters(), bne);
            }
            if (e instanceof AbortException) {
                sendInfoMail(true, currentSI,
                        instanceProvStatus.getErrorMailMessage(), cause);
            }
        } else if (instanceProvStatus.isWaitingForDeletion()) {
            logger.error("waitingfordeletion");
            em.remove(currentSI);
            sendInfoMail(true, currentSI,
                    instanceProvStatus.getErrorMailMessage(), cause);
        } else if (instanceProvStatus.isWaitingForModification()) {
            logger.error("waitingformodification");
            InstanceResult instanceResult = new InstanceResult();
            instanceResult.setRc(1);
            instanceResult.setInstance(new InstanceInfo());
            try {
                currentSI.rollbackServiceInstance(em);
                notifyOnProvisioningAbortion(currentSI, instanceResult, cause);
                currentSI.setProvisioningStatus(ProvisioningStatus.COMPLETED);
                em.persist(currentSI);
                String actionLink = "";
                try {
                    actionLink = generateLinkForControllerUI(currentSI)
                            .toString();
                } catch (Exception exception) {
                    logger.error(
                            "Failure during generation of link for errormail (service instance '{}' with message '{}')",
                            currentSI.getInstanceId(), exception.getMessage());
                }
                sendActionMail(true, currentSI,
                        instanceProvStatus.getErrorMailMessage(), cause,
                        actionLink, false);
            } catch (BESNotificationException bne) {
                handleBESNotificationException(currentSI, instanceProvStatus,
                        cause.getChangedParameters(), bne);
            } catch (BadResultException bre) {
                logger.warn(
                        "Failure during rollback of instance parameters for service instance '{}' with message '{}'",
                        Long.valueOf(currentSI.getTkey()), bre.getMessage());
                sendInfoMail(true, currentSI,
                        instanceProvStatus.getErrorMailMessage(), cause);
            }
        } else if (instanceProvStatus.isWaitingForActivation()
                || instanceProvStatus.isWaitingForDeactivation()) {
            // no BES notification yet
            logger.error("waitingforactiveordeactivation");
            currentSI.setProvisioningStatus(ProvisioningStatus.COMPLETED);
            em.persist(currentSI);
            sendInfoMail(true, currentSI,
                    instanceProvStatus.getErrorMailMessage(), cause);
        } else if (instanceProvStatus.isWaitingForOperation()) {
            logger.error("waitingforoperation");
            InstanceResult instanceResult = new InstanceResult();
            try {
                notifyOnProvisioningAbortion(currentSI, instanceResult, cause);
            } catch (BESNotificationException bne) {
                handleBESNotificationException(currentSI, instanceProvStatus,
                        cause.getChangedParameters(), bne);
            }
            currentSI.setProvisioningStatus(ProvisioningStatus.COMPLETED);
            em.persist(currentSI);
            sendInfoMail(true, currentSI,
                    instanceProvStatus.getErrorMailMessage(), cause);
        } else if (instanceProvStatus.isWaitingForUserCreation()) {
            logger.error("waitingforusercreation");
            InstanceResult instanceResult = new InstanceResult();
            try {
                notifyOnProvisioningAbortion(currentSI, instanceResult, cause);
            } catch (BESNotificationException bne) {
                handleBESNotificationException(currentSI, instanceProvStatus,
                        cause.getChangedParameters(), bne);
            }
            currentSI.setProvisioningStatus(ProvisioningStatus.COMPLETED);
            em.persist(currentSI);
            logger.error("sendinginfomail");
            sendInfoMail(true, currentSI,
                    instanceProvStatus.getErrorMailMessage(), cause);
        } else if (instanceProvStatus.isWaitingForUserDeletion()) {
            logger.error("waitingforuserdeletion");
            InstanceResult instanceResult = new InstanceResult();
            try {
                notifyOnProvisioningAbortion(currentSI, instanceResult, cause);
            } catch (BESNotificationException bne) {
                handleBESNotificationException(currentSI, instanceProvStatus,
                        cause.getChangedParameters(), bne);
            }
            currentSI.setProvisioningStatus(ProvisioningStatus.COMPLETED);
            em.persist(currentSI);
            logger.error("sendinginfomail");
            sendInfoMail(true, currentSI,
                    instanceProvStatus.getErrorMailMessage(), cause);
        }
        
    }

    void handleSuspendException(ServiceInstance currentSI,
            ProvisioningStatus instanceProvStatus, SuspendException se) {
        // TODO check no params set in SuspendException
        // write the parameters back
        updateParameterMapSafe(currentSI, se.getChangedParameters());
        // suspend process and inform TP with mail
        if (se.getCause() != null) {
            suspendServiceInstance(currentSI, se.getCause(),
                    instanceProvStatus.getSuspendMailMessage(), true);
            // server can not be connected
        } else if (se.getResponseCode() == -1) {
            suspendServiceInstance(currentSI, se, "mail_server_connect_error",
                    true);
        } else {
            suspendServiceInstance(currentSI, se,
                    instanceProvStatus.getSuspendMailMessage(), true);
        }
    }

    /**
     * Checks the service instances where the system is up and running but a
     * dedicated provisioning call into the instance has been requested.
     */
    private void doHandleInstanceProvisioning(ServiceInstance serviceInstance) {

        try {
            ProvisioningService provisioningService = provServFact
                    .getInstance(serviceInstance);
            provisioningService.sendPing("Availability check");
            // if available, start the actual provisioning

            // only call instance provisioning once
            serviceInstance.setInstanceProvisioning(false);
            em.persist(serviceInstance);

            String instanceId = serviceInstance.getInstanceId();
            String subscriptionId = serviceInstance.getSubscriptionId();
            String organizationId = serviceInstance.getOrganizationId();
            BaseResult baseResult = null;
            InstanceResult instanceResult = null;

            switch (serviceInstance.getProvisioningStatus()) {
            case WAITING_FOR_SYSTEM_CREATION:
                instanceResult = createServiceInstance(serviceInstance,
                        provisioningService);
                if (instanceResult == null) {
                    BadResultException be = new BadResultException(
                            "Service returned null as instance result");
                    throw be;
                }

                // persist the data retrieved from the service
                InstanceInfo instance = instanceResult.getInstance();
                if (instance == null) {
                    throw new BadResultException(String.format(
                            "Returned object of type InstanceInfo is null for service '%s'",
                            Long.valueOf(serviceInstance.getTkey())));
                }
                if (instance.getAccessInfo() != null) {
                    serviceInstance
                            .setServiceAccessInfo(instance.getAccessInfo());
                }
                if (instance.getBaseUrl() != null) {
                    String serviceBaseURL = getBaseUrlWithPublicIp(
                            serviceInstance, instance);
                    serviceInstance.setServiceBaseURL(serviceBaseURL);
                    instance.setBaseUrl(serviceBaseURL);
                }
                if (instance.getLoginPath() != null) {
                    serviceInstance
                            .setServiceLoginPath(instance.getLoginPath());
                }
                baseResult = instanceResult;
                break;
            case WAITING_FOR_SYSTEM_DELETION:
                baseResult = provisioningService.deleteInstance(instanceId,
                        organizationId, subscriptionId, null);
                break;
            case WAITING_FOR_SYSTEM_ACTIVATION:
                baseResult = provisioningService.activateInstance(instanceId,
                        null);
                break;
            case WAITING_FOR_SYSTEM_DEACTIVATION:
                baseResult = provisioningService.deactivateInstance(instanceId,
                        null);
                break;
            case WAITING_FOR_SYSTEM_MODIFICATION:
                baseResult = provisioningService.modifySubscription(instanceId,
                        serviceInstance.getSubscriptionId(),
                        serviceInstance.getReferenceId(),
                        InstanceFilter.getFilteredInstanceParametersForService(
                                serviceInstance),
                        InstanceFilter.getFilteredInstanceAttributeForService(
                                serviceInstance),
                        null);
                break;
            case WAITING_FOR_SYSTEM_UPGRADE:
                baseResult = provisioningService.upgradeSubscription(instanceId,
                        serviceInstance.getSubscriptionId(),
                        serviceInstance.getReferenceId(),
                        InstanceFilter.getFilteredInstanceParametersForService(
                                serviceInstance),
                        InstanceFilter.getFilteredInstanceAttributeForService(
                                serviceInstance),
                        null);
                break;
            case WAITING_FOR_USER_CREATION:
            case WAITING_FOR_USER_MODIFICATION:
            case WAITING_FOR_USER_DELETION:
                // no parameters available here => ignore
                baseResult = new BaseResult();
                baseResult.setRc(0);
                break;
            default:
                // e.g. COMPLETED instances
                return;
            }

            if (baseResult == null) {
                BadResultException be = new BadResultException(
                        "Service returned null as base result");
                throw be;
            }

            try {
                if (baseResult.getRc() == 0) {
                    if (serviceInstance.isControllerReady()) {
                        if (instanceResult != null) {
                            // if succeeded, inform BES about the completion
                            notifyOnProvisioningCompletion(serviceInstance,
                                    instanceResult);
                        }
                        serviceInstance.setProvisioningStatus(
                                ProvisioningStatus.COMPLETED);
                    }
                } else {
                    // otherwise abort
                    if (instanceResult != null) {
                        notifyOnProvisioningAbortion(serviceInstance,
                                instanceResult, new APPlatformException(
                                        instanceResult.getDesc()));
                    }
                    serviceInstance.setProvisioningStatus(
                            ProvisioningStatus.COMPLETED);
                }
            } catch (BESNotificationException bne) {
                // Suspend process and inform admin with an e-mail
                if (besDAO.isCausedByConnectionException(bne)) {
                    suspendApp(serviceInstance,
                            "mail_bes_notification_error_app_admin");
                } else {
                    suspendServiceInstance(serviceInstance, bne.getCause(),
                            "mail_create_beserror", false);
                }
            }

            // update the service instance entry
            em.persist(serviceInstance);

        } catch (BadResultException e) {
            logger.warn(
                    "Failure during processing for service instance '{}' with message '{}'",
                    Long.valueOf(serviceInstance.getTkey()), e.getMessage());

        } catch (Exception e) {

            // if the ping of the technical service failed, it cannot be
            // reached, so inform BSS on status of the service
            try {
                ProvisioningStatus instanceProvStatus = serviceInstance
                        .getProvisioningStatus();
                if (instanceProvStatus.isWaitingForCreation()) {
                    besDAO.notifyOnProvisioningStatusUpdate(serviceInstance,
                            getErrorMessages());
                }
            } catch (BESNotificationException bne) {
                // Suspend process and inform admin with an e-mail
                if (besDAO.isCausedByConnectionException(bne)) {
                    suspendApp(serviceInstance,
                            "mail_bes_notification_error_app_admin");
                } else {

                    suspendServiceInstance(serviceInstance, bne.getCause(),
                            "mail_create_beserror", false);
                }
            }
        }
    }

    List<LocalizedText> getErrorMessages() {
        return Messages.getAll("error_env_not_ready");
    }

    private String getBaseUrlWithPublicIp(ServiceInstance si, InstanceInfo ii)
            throws BadResultException {
        InstanceParameter publicIpParam = si
                .getParameterForKey(InstanceParameter.PUBLIC_IP);
        if (publicIpParam == null) {
            BadResultException bre = new BadResultException(
                    String.format("Parameter for key '%s' not found",
                            InstanceParameter.PUBLIC_IP));
            logger.warn(bre.getMessage(), bre);
            throw bre;
        }
        String ip = publicIpParam.getDecryptedValue();
        String baseUrl = ii.getBaseUrl();
        int idx = baseUrl.indexOf("//") + 2;
        String tmp = baseUrl.substring(0, idx) + ip
                + baseUrl.substring(baseUrl.indexOf(':', idx));
        return tmp;
    }

    /**
     * Performs the callback to the BES system to notify on the unsuccessful
     * provisioning of the technical service.
     * 
     * @param currentSI
     *            the service instance containing the details on the initial
     *            corresponding BES request
     * @param instanceResult
     *            the result of the provisioning. Must not be <code>null</code>
     * @param cause
     *            reason for abort or NULL if unknown
     * @throws BESNotificationException
     *             Thrown in case BES could not be notified.
     */
    void notifyOnProvisioningAbortion(ServiceInstance currentSI,
            InstanceResult instanceResult, APPlatformException cause)
            throws BESNotificationException {

        ProvisioningStatus provisioningStatus = currentSI
                .getProvisioningStatus();

        switch (provisioningStatus) {
        case WAITING_FOR_SYSTEM_CREATION:
            besDAO.notifyAsyncSubscription(currentSI, instanceResult, false,
                    cause);
            break;
        case WAITING_FOR_SYSTEM_MODIFICATION:
            besDAO.notifyAsyncModifySubscription(currentSI, instanceResult,
                    false, cause);
            break;
        case WAITING_FOR_SYSTEM_UPGRADE:
            besDAO.notifyAsyncUpgradeSubscription(currentSI, instanceResult,
                    false, cause);
            break;
        case WAITING_FOR_SYSTEM_OPERATION:
            Operation operation = operationDAO
                    .getOperationByInstanceId(currentSI.getInstanceId());
            if (operation != null) {
                besDAO.notifyAsyncOperationStatus(currentSI,
                        operation.getTransactionId(), OperationStatus.ERROR,
                        cause == null ? null : cause.getLocalizedMessages());
                em.remove(operation);
            }
            break;
        default:
            return;
        }

        if (currentSI.isDeleted()) {
            logger.info(
                    "Processing of service instance '{}' failed with return code '{}' and description '{}'. OSCM subscription is already terminated.",
                    new Object[] { Long.valueOf(currentSI.getTkey()),
                            Long.valueOf(instanceResult.getRc()),
                            instanceResult.getDesc() });
        } else {
            logger.info(
                    "Processing of service instance '{}' failed with return code '{}' and description '{}'. OSCM was informed on the abortion.",
                    new Object[] { Long.valueOf(currentSI.getTkey()),
                            Long.valueOf(instanceResult.getRc()),
                            instanceResult.getDesc() });
        }
    }

    /**
     * Performs the callback to the BES system to notify on the successful
     * provisioning of the technical service.
     * 
     * @param currentSI
     *            The service instance containing the details on the initial,
     *            corresponding BES request.
     * @param instanceResult
     *            The result of the provisioning. Must not be <code>null</code>.
     * @throws BESNotificationException
     *             Thrown in case BES could not be notified.
     */
    private void notifyOnProvisioningCompletion(ServiceInstance currentSI,
            InstanceResult instanceResult) throws BESNotificationException {

        ProvisioningStatus provisioningStatus = currentSI
                .getProvisioningStatus();

        switch (provisioningStatus) {
        case WAITING_FOR_SYSTEM_CREATION:
            besDAO.notifyAsyncSubscription(currentSI, instanceResult, true,
                    null);
            break;
        case WAITING_FOR_SYSTEM_MODIFICATION:
            besDAO.notifyAsyncModifySubscription(currentSI, instanceResult,
                    true, null);
            break;
        case WAITING_FOR_SYSTEM_UPGRADE:
            besDAO.notifyAsyncUpgradeSubscription(currentSI, instanceResult,
                    true, null);
            break;
        case WAITING_FOR_SYSTEM_OPERATION:
            Operation operation = operationDAO
                    .getOperationByInstanceId(currentSI.getInstanceId());
            if (operation != null) {
                besDAO.notifyAsyncOperationStatus(currentSI,
                        operation.getTransactionId(), OperationStatus.COMPLETED,
                        null);
                em.remove(operation);
            }
            break;
        default:
            return;
        }

        if (currentSI.isDeleted()) {
            logger.info(
                    "The processing of service instance '{}' was completed, but OSCM subscription is already terminated.",
                    Long.valueOf(currentSI.getTkey()));
        } else {
            logger.info(
                    "The processing of service instance '{}' was completed and OSCM has been notified accordingly",
                    Long.valueOf(currentSI.getTkey()));
        }

    }

    /**
     * Creates the instance of the technical service, running in the provisioned
     * environment.
     * 
     * @param currentSI
     *            The service instance containing the environment details.
     * @param provisioningService
     *            The ProvisioningService of the technical service.
     * @return The instance result object returned from the technical service.
     * @throws BadResultException
     */
    InstanceResult createServiceInstance(ServiceInstance currentSI,
            ProvisioningService provisioningService) throws BadResultException {
        InstanceRequest request = getInstanceRequest(currentSI);
        InstanceResult instanceResult = provisioningService
                .createInstance(request, null);
        return instanceResult;
    }

    InstanceRequest getInstanceRequest(ServiceInstance currentSI)
            throws BadResultException {
        InstanceRequest request = new InstanceRequest();
        request.setDefaultLocale(currentSI.getDefaultLocale());
        request.setOrganizationId(currentSI.getOrganizationId());
        request.setOrganizationName(currentSI.getOrganizationName());
        request.setSubscriptionId(currentSI.getSubscriptionId());
        request.setLoginUrl(currentSI.getBesLoginURL());
        request.setParameterValue(InstanceFilter
                .getFilteredInstanceParametersForService(currentSI));
        return request;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void raiseEvent(String controllerId, String instanceId,
            Properties properties) throws APPlatformException {

        ServiceInstance currentSI = null;
        try {
            currentSI = instanceDAO.getInstanceById(controllerId, instanceId);
        } catch (ServiceInstanceNotFoundException e) {
            logger.warn(e.getMessage(), e);
            throw new APPlatformException(e.getMessage());
        }

        try {
            // Get settings for this instance
            final ProvisioningSettings settings = configService
                    .getProvisioningSettings(currentSI, null, true);

            // Signal notification to instance
            final APPlatformController controller = APPlatformControllerFactory
                    .getInstance(currentSI.getControllerId());
            InstanceStatus newStatus = controller.notifyInstance(instanceId,
                    settings, properties);

            // Check for internal call
            if (properties != null && EVENT_VALUE_YES
                    .equals(properties.getProperty(EVENT_KEY_RESUME, ""))) {
                // Resume suspended process
                if (newStatus == null) {
                    newStatus = new InstanceStatus();
                }
                newStatus.setRunWithTimer(true);
            }

            // Update service status
            currentSI.updateStatus(em, newStatus);

            if (newStatus != null) {
                // Update changed parameters for the instance
                currentSI.setInstanceParameters(
                        newStatus.getChangedParameters());
                em.persist(currentSI);
                if (newStatus.getRunWithTimer()) {
                    // Ensure running timers
                    initTimers();
                }
            }

        } catch (APPlatformException e) {
            // Write back parameters
            updateParameterMapSafe(currentSI, e.getChangedParameters());
            logger.warn(
                    "Failure during notifying service instance '{}' with message '{}'",
                    instanceId, e.getMessage());
            throw e;
        } catch (BadResultException bre) {
            logger.warn(
                    "Failure during handling of instance parameters for service instance '{}' with message '{}'",
                    Long.valueOf(currentSI.getTkey()), bre.getMessage());
            throw getPlatformException(bre);
        }

    }

    /**
     * Convert given exception into a well designed platform exception.
     * 
     * @param ex
     *            the exception
     * @return the converted platform exception
     */
    private APPlatformException getPlatformException(Throwable ex) {
        if (ex instanceof EJBException) {
            if (ex.getCause() != null) {
                ex = ex.getCause();
            } else if (((EJBException) ex).getCausedByException() != null) {
                ex = ((EJBException) ex).getCausedByException();
            }
        }

        if (ex instanceof APPlatformException) {
            return (APPlatformException) ex;
        }

        String causeMessage = (ex.getMessage() != null) ? ex.getMessage()
                : ex.getClass().getName();
        return new APPlatformException(causeMessage, ex);
    }

    void sendActionMail(boolean informTechnologyManagers, ServiceInstance si,
            String msgKey, Throwable cause, String actionLink,
            boolean withProvStatus) {
        sendMail(informTechnologyManagers, si, msgKey, cause, actionLink,
                withProvStatus);
    }

    void sendInfoMail(boolean informTechnologyManagers, ServiceInstance si,
            String msgKey, Throwable cause) {
        sendMail(informTechnologyManagers, si, msgKey, cause, null, false);
    }

    void sendMail(boolean informTechnologyManagers, ServiceInstance si,
            String msgKey, Throwable cause, String actionLink,
            boolean withProvStatus) {
        List<VOUserDetails> mailUsers = new ArrayList<>();
        if (informTechnologyManagers) {
            mailUsers.addAll(besDAO.getBESTechnologyManagers(si));
            if (cause instanceof AbortException) {
                // abort exception carries two sorts of messages, take provider
                // side here
                cause = new APPlatformException(
                        ((AbortException) cause).getProviderMessages());
            }
        }

        if (mailUsers.isEmpty()) {
            try {
                mailUsers.add(configService.getAPPAdministrator());
            } catch (ConfigurationException e) {
                logger.warn(
                        "APP administrator mail not configured. Update the configuration setting: "
                                + PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS
                                        .name());
            }
        }

        if (!mailUsers.isEmpty()) {
            for (VOUserDetails user : mailUsers) {
                String locale = user.getLocale();
                String subject = getMailSubject(locale, msgKey,
                        si.getSubscriptionId());
                String body = "";
                if (actionLink == null) {
                    body = getMailBodyForInfo(locale, msgKey, si, cause);
                } else {
                    body = getMailBodyForAction(locale, msgKey, si, cause,
                            actionLink, withProvStatus);
                }
                try {
                    mailService.sendMail(Arrays.asList(user.getEMail()),
                            subject, body);
                } catch (APPlatformException pe) {
                    String causeStr = "unknown";
                    if (pe.getCause() != null) {
                        causeStr = pe.getCause().getMessage();
                    }
                    logger.error(
                            "Failure during error mail notification for service instance '{}' with message '{}'",
                            si.getInstanceId(),
                            pe.getMessage() + " [Cause: " + causeStr + "]");
                }
            }
        }
    }

    String getMailSubject(String locale, String msgKey, String subscriptionId) {
        String subject = Messages.get(locale, msgKey + ".subject",
                subscriptionId);
        return subject;
    }

    String getMailBodyForInfo(String locale, String msgKey, ServiceInstance si,
            Throwable cause) {
        String causeMsg = getCauseMessage(cause, locale);
        String body = Messages.get(locale, "mail_header.text")
                + Messages.get(locale, msgKey + ".text", si.getSubscriptionId(),
                        si.getOrganizationId(), si.getInstanceId(), causeMsg)
                + Messages.get(locale, "mail_footer.text");
        return body;
    }

    String getMailBodyForAction(String locale, String msgKey,
            ServiceInstance si, Throwable cause, String actionLink,
            boolean withProvStatus) {
        String causeMsg = getCauseMessage(cause, locale);
        msgKey = msgKey + ".text";
        if (msgKey.startsWith("mail_suspend_error")) {
            msgKey = "mail_suspend_error.text";
        }

        String body;
        if ("mail_inconsistent_instance_state.text".equals(msgKey)) {
            body = Messages.get(locale, "mail_header.text")
                    + Messages.get(locale, msgKey, si.getSubscriptionId(),
                            si.getOrganizationId(), si.getInstanceId(),
                            si.getProvisioningStatus(), causeMsg);
        } else if ("mail_server_connect_error.text".equals(msgKey)) {
            body = Messages.get(locale, "mail_header.text") + Messages.get(
                    locale, msgKey, si.getSubscriptionId(),
                    si.getOrganizationId(), si.getInstanceId(), causeMsg);
        } else if ("mail_bes_notification_connection_success.text"
                .equals(msgKey)) {
            body = Messages.get(locale, "mail_header.text") + Messages.get(
                    locale, msgKey, si.getSubscriptionId(),
                    si.getOrganizationId(), si.getInstanceId(), actionLink);
        } else if ("mail_bes_notification_error_app_admin.text"
                .equals(msgKey)) {
            body = Messages.get(locale, "mail_header.text")
                    + Messages.get(locale, msgKey, actionLink);
        } else {
            if (withProvStatus) {
                body = Messages.get(locale, "mail_header.text") + Messages.get(
                        locale, msgKey, si.getSubscriptionId(),
                        si.getOrganizationId(), si.getInstanceId(),
                        si.getProvisioningStatus(), causeMsg, actionLink);
            } else {
                body = Messages.get(locale, "mail_header.text")
                        + Messages.get(locale, msgKey, si.getSubscriptionId(),
                                si.getOrganizationId(), si.getInstanceId(),
                                causeMsg, actionLink);
            }
        }
        return body;
    }

    private void suspendServiceInstance(ServiceInstance si, Throwable cause,
            String msgKey, boolean informTechnologyManagers) {
        suspendServiceInstance(si, cause, msgKey, informTechnologyManagers,
                false);
    }

    /**
     * Suspends provisioning process and informs the technology managers with an
     * e-mail.
     * 
     * @param si
     *            the service instance which caused the problem
     * @param cause
     *            the exception which describes the details of the problem
     * @param msgKey
     *            the message key for the mail subject and body
     * @param informTechnologyManagers
     *            TRUE if the technology managers of the TP should be informed.
     *            If BES not available no exception is thrown, but the APP
     *            administrator is informed.
     */
    private void suspendServiceInstance(ServiceInstance si, Throwable cause,
            String msgKey, boolean informTechnologyManagers,
            boolean withProvStatus) {
        // Disable timer of this service
        si.setRunWithTimer(false);
        si.setLocked(false);
        em.persist(si);

        StringBuffer eventLink = new StringBuffer();
        try {
            if (cause instanceof ObjectNotFoundException) {
                eventLink = generateLinkForControllerUI(si);
            } else {
                eventLink
                        .append(configService.getProxyConfigurationSetting(
                                PlatformConfigurationKey.APP_BASE_URL))
                        .append("/").append(EVENT_KEY_NOTIFY).append("?")
                        .append("sid=")
                        .append(URLEncoder.encode(si.getInstanceId(), "UTF-8"))
                        .append('&').append("controllerid=")
                        .append(URLEncoder.encode(si.getControllerId(),
                                "UTF-8"))
                        .append('&').append(EVENT_KEY_RESUME).append('=')
                        .append(EVENT_VALUE_YES);
            }
        } catch (Exception e) {
            logger.error(
                    "Failure during error mail notification for service instance '{}' with message '{}'",
                    si.getInstanceId(), e.getMessage());
            return;
        }

        sendActionMail(informTechnologyManagers, si, msgKey, cause,
                eventLink.toString(), withProvStatus);
    }

    StringBuffer generateLinkForControllerUI(ServiceInstance si)
            throws ConfigurationException, UnsupportedEncodingException {
        StringBuffer eventLink = new StringBuffer();
        eventLink
                .append(configService.getProxyConfigurationSetting(
                        PlatformConfigurationKey.APP_BASE_URL))
                .append("/controller?controllerid=")
                .append(URLEncoder.encode(si.getControllerId(), "UTF-8"));
        return eventLink;
    }

    String getCauseMessage(Throwable cause, String locale) {
        String causeMsg;
        if (cause != null) {
            if (cause instanceof APPlatformException) {
                causeMsg = ((APPlatformException) cause)
                        .getLocalizedMessage(locale);
            } else {
                causeMsg = cause.getMessage();
            }
        } else {
            causeMsg = "<Unknown cause>";
        }
        return causeMsg;
    }

    /**
     * Update given parameters in a safe way (handling within error situations).
     * Exceptions will be catched and traced.
     */
    private void updateParameterMapSafe(ServiceInstance si,
            HashMap<String, Setting> changedParameters) {
        try {
            si.setInstanceParameters(changedParameters);
            em.persist(si);
        } catch (BadResultException bre) {
            logger.warn(
                    "Failure during storing of instance parameters for service instance '{}' with message '{}'",
                    Long.valueOf(si.getTkey()), bre.getMessage());
        }
    }

    /**
     * Creates an <code>InstanceResult</code> with given access information.
     * 
     * @param status
     *            the instance status to take access information from
     * @return the instance result object
     */
    private InstanceResult createInstanceResult(InstanceStatus status) {
        InstanceResult instanceResult = new InstanceResult();
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setAccessInfo(status.getAccessInfo());
        instanceInfo.setBaseUrl(status.getBaseUrl());
        instanceInfo.setLoginPath(status.getLoginPath());
        instanceResult.setRc(0);
        instanceResult.setInstance(instanceInfo);
        return instanceResult;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * If BES is available process failed serviceInstances and reset
     * APP_SUSPEND.
     * 
     * @param isRestartAPP
     *            if true the method invoked by restartAPP else invoked by
     *            ControllerUI
     * @return If true restart successfully else restart unsuccessfully
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean restart(boolean isRestartAPP) {
        final String messageKey = "mail_bes_notification_connection_success";
        boolean isSuspendedByApp = false;
        if (!besDAO.isBESAvalible()) {
            if (isRestartAPP) {
                sendMailToAppAdmin("mail_bes_notification_error_app_admin");
            }
            return false;
        }
        List<ServiceInstance> serviceInstances = instanceDAO
                .getInstancesSuspendedbyApp();
        for (ServiceInstance instance : serviceInstances) {
            String actionLink = getResumeLinkForInstance(instance);
            if (actionLink == null || actionLink.isEmpty()) {
                isSuspendedByApp = true;
                continue;
            }
            sendActionMail(true, instance, messageKey, null, actionLink, false);
            instance.setSuspendedByApp(false);
        }
        configService
                .setAPPSuspend(Boolean.valueOf(isSuspendedByApp).toString());
        return true;
    }

    private String getResumeLinkForInstance(ServiceInstance instance) {
        StringBuffer actionLink = new StringBuffer();
        try {
            actionLink
                    .append(configService.getProxyConfigurationSetting(
                            PlatformConfigurationKey.APP_BASE_URL))
                    .append("/").append(EVENT_KEY_NOTIFY).append("?")
                    .append("sid=")
                    .append(URLEncoder.encode(instance.getInstanceId(),
                            "UTF-8"))
                    .append('&').append("controllerid=")
                    .append(URLEncoder.encode(instance.getControllerId(),
                            "UTF-8"))
                    .append('&').append(EVENT_KEY_RESUME).append('=')
                    .append(EVENT_VALUE_YES);
        } catch (Exception e) {
            logger.error(
                    "Failure during error mail notification for service instance '{}' with message '{}'",
                    instance.getInstanceId(), e.getMessage());
            return "";
        }
        return actionLink.toString();

    }

}
