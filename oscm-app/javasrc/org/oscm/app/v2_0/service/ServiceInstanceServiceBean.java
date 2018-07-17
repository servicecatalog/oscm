/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-02-25                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.v2_0.service;

import java.util.EnumSet;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.oscm.app.business.AsynchronousProvisioningProxyImpl;
import org.oscm.app.business.UserMapper;
import org.oscm.app.business.exceptions.BESNotificationException;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceException;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.dao.BesDAO;
import org.oscm.app.dao.OperationDAO;
import org.oscm.app.dao.ServiceInstanceDAO;
import org.oscm.app.domain.InstanceOperation;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.i18n.Messages;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.provisioning.data.InstanceInfo;
import org.oscm.provisioning.data.InstanceResult;
import org.oscm.provisioning.data.User;
import org.oscm.types.enumtypes.OperationStatus;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.slf4j.Logger;

/**
 * @author goebel
 */
@Stateless
@LocalBean
public class ServiceInstanceServiceBean {

    private static final String ALLOWED_STATUS_ABORT_COMPLETE = ProvisioningStatus.WAITING_FOR_SYSTEM_CREATION
            .name() + ", "
            + ProvisioningStatus.WAITING_FOR_SYSTEM_MODIFICATION.name() + ", "
            + ProvisioningStatus.WAITING_FOR_SYSTEM_UPGRADE.name();

    @Inject
    protected transient Logger logger;

    @Inject
    private AsynchronousProvisioningProxyImpl appImpl;

    @EJB
    protected ServiceInstanceDAO dao;

    @EJB
    protected BesDAO besDao;

    @EJB
    protected APPTimerServiceBean timerServcie;

    @EJB
    protected OperationServiceBean opBean;

    @EJB
    protected OperationDAO opDao;

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    public EntityManager em;

    /**
     * Fetch the list of service instances for the given controller.
     * 
     * @param controllerId
     *            - the Id of the controller managing the instances
     * 
     * @return a list of SerivceInstance objects
     */
    public List<ServiceInstance> getInstancesForController(String controllerId)
            throws ServiceInstanceException {
        try {
            return dao.getInstancesForController(controllerId);
        } catch (IllegalArgumentException ex) {
            throw new ServiceInstanceException(Messages.get(
                    Messages.DEFAULT_LOCALE, "error_missing_controller_id"));
        }
    }

    public List<ServiceInstance> getInstances() {
        return dao.getInstances();
    }

    public ServiceInstance find(ServiceInstance instance, String locale)
            throws ServiceInstanceException {
        try {
            return dao.find(instance);
        } catch (ServiceInstanceNotFoundException e) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_service_instance_not_found"));
        }
    }

    /**
     * Fetch the list of instance parameters for the given instance.
     * 
     * @param instance
     *            - the instance to read the parameters from
     * 
     * @return a list of InstanceParameter objects
     */
    public List<InstanceParameter> getInstanceParameters(
            ServiceInstance serviceInstance, String locale)
            throws ServiceInstanceException {
        try {
            return dao.getInstanceParameters(serviceInstance);
        } catch (IllegalArgumentException ex) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_instance_not_exist"),
                    "[serviceInstance is null]");
        }
    }

    /**
     * Execute the given operation.
     * 
     * @param serviceInstance
     *            - the service instance object
     * @param operation
     *            - the operation to execute
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void executeOperation(ServiceInstance serviceInstance,
            ServiceUser user, InstanceOperation operation)
            throws ServiceInstanceException {

        String locale = user.getLocale();
        if (serviceInstance == null) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_instance_not_exist"),
                    "[serviceInstance is null]");
        }

        serviceInstance = em.getReference(ServiceInstance.class,
                Long.valueOf(serviceInstance.getTkey()));

        switch (operation) {

        case RESUME:
            resumeInstance(serviceInstance, locale);
            break;

        case SUSPEND:
            suspendInstance(serviceInstance, locale);
            break;

        case UNLOCK:
            unlockInstance(serviceInstance, locale);
            break;

        case DELETE:
            deleteServiceInstance(serviceInstance, locale);
            break;

        case DEPROVISION:
            deprovision(serviceInstance, user);
            break;

        case ABORT_PENDING:
            abortPendingInstance(serviceInstance, locale);
            break;

        case COMPLETE_PENDING:
            completePendingInstance(serviceInstance, locale);
            break;

        default:
            break;
        }

    }

    public void resumeInstance(ServiceInstance serviceInstance, String locale)
            throws ServiceInstanceException {
        try {
            ServiceInstance dbInstance = dao.resumeInstance(serviceInstance);
            timerServcie.initTimers();

            if (dbInstance.getProvisioningStatus().isWaitingForOperation()) {
                Operation operation = opDao
                        .getOperationByInstanceId(dbInstance.getInstanceId());
                if (operation != null) {
                    besDao.notifyAsyncOperationStatus(dbInstance,
                            operation.getTransactionId(),
                            OperationStatus.RUNNING, null);
                }
            }
        } catch (ServiceInstanceNotFoundException e) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_instance_not_exist",
                            serviceInstance.getInstanceId()));
        } catch (BESNotificationException e) {
            throw new ServiceInstanceException(getMessage(e));
        }
    }

    /**
     * Deprovisions all service instance related resources, which means in
     * detail:<br />
     * 1. Deletes the OSCM subscription<br />
     * 2. Deletes the APP service instance<br />
     * 3. Deletes controller back-end resources
     * <p>
     * In contrast to the deleteServiceInstance method, back-end resources are
     * deleted too.
     */
    private void deprovision(ServiceInstance serviceInstance, ServiceUser user)
            throws ServiceInstanceException {
        try {
            ServiceInstance dbServiceInstance = dao.find(serviceInstance);
            besDao.terminateSubscription(dbServiceInstance, user.getLocale());
        } catch (ServiceInstanceNotFoundException e) {
            throw new ServiceInstanceException(
                    Messages.get(user.getLocale(), "error_instance_not_exist",
                            serviceInstance.getInstanceId()));
        } catch (BESNotificationException e) {
            if (e.getCause() instanceof ObjectNotFoundException) {
                try {
                    User provisioningUser = UserMapper.toProvisioningUser(user);
                    appImpl.deleteInstance(serviceInstance, provisioningUser);
                } catch (BadResultException | APPlatformException e1) {
                    throw new ServiceInstanceException(getMessage(e1));
                }
            } else {
                throw new ServiceInstanceException(getMessage(e));
            }
        }
    }

    /**
     * 1. Deletes the OSCM subscription<br />
     * 2. Deletes the APP service instance<br />
     * <p>
     * In contrast to the deprovision method no back-end resources are deleted.
     * <p>
     * Implementation Idea:<br />
     * Open a new transaction and mark the service instance as deleted. Then
     * terminate the subscription on OSCM side via a web-service call, finally
     * the APP is called but the APP does nothing because the service instance
     * is already marked as deleted. The final step of this method will then
     * actually delete the service instance from the APP database.
     */
    private void deleteServiceInstance(ServiceInstance serviceInstance,
            String locale) throws ServiceInstanceException {

        ServiceInstance changedSI = null;
        try {
            changedSI = dao.markAsDeleted(serviceInstance);
            besDao.terminateSubscription(changedSI, locale);
        } catch (ServiceInstanceNotFoundException e) {
            logger.warn("The service instance '{}' doesn't exist any more.",
                    serviceInstance.getInstanceId());
        } catch (BESNotificationException e) {
            if (!(e.getCause() instanceof ObjectNotFoundException)) {
                try {
                    dao.restoreInstance(changedSI);
                } catch (ServiceInstanceNotFoundException e1) {
                    throw new ServiceInstanceException(
                            Messages.get(locale, "error_instance_not_exist",
                                    serviceInstance.getInstanceId()));
                }
                throw new ServiceInstanceException(getMessage(e));
            }
        }

        try {
            dao.deleteInstance(changedSI);
        } catch (ServiceInstanceNotFoundException e1) {
            logger.warn("The service instance '{}' doesn't exist any more.",
                    serviceInstance.getInstanceId());
        }
    }

    private String getMessage(Exception e) {
        String message = e.getMessage();
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            message += ": " + e.getCause().getMessage();
        }
        return message;
    }

    private void suspendInstance(ServiceInstance serviceInstance, String locale)
            throws ServiceInstanceException {
        try {
            dao.suspendInstance(serviceInstance);
        } catch (ServiceInstanceNotFoundException e) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_instance_not_exist",
                            serviceInstance.getInstanceId()));
        }
    }

    private void unlockInstance(ServiceInstance serviceInstance, String locale)
            throws ServiceInstanceException {
        try {
            dao.unlockInstance(serviceInstance);
        } catch (ServiceInstanceNotFoundException e) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_instance_not_exist",
                            serviceInstance.getInstanceId()));
        }
    }

    private InstanceResult createInstanceResult(ServiceInstance currentSI) {

        InstanceInfo instance = new InstanceInfo();
        instance.setInstanceId(currentSI.getSubscriptionId());
        instance.setAccessInfo(currentSI.getServiceAccessInfo());
        instance.setBaseUrl(currentSI.getServiceBaseURL());
        instance.setLoginPath(currentSI.getServiceLoginPath());
        InstanceResult instanceResult = new InstanceResult();
        instanceResult.setInstance(instance);

        return instanceResult;
    }

    void abortPendingInstance(ServiceInstance serviceInstance, String locale)
            throws ServiceInstanceException {
        try {
            ServiceInstance dbInstance = dao
                    .abortPendingInstance(serviceInstance);

            switch (dbInstance.getProvisioningStatus()) {
            case WAITING_FOR_SYSTEM_CREATION:
                besDao.notifyAsyncSubscription(dbInstance,
                        createInstanceResult(dbInstance), false,
                        new APPlatformException(
                                Messages.getAll("abort_pending_subscription")));
                break;
            case WAITING_FOR_SYSTEM_MODIFICATION:
                if (dbInstance.getRollbackParameters() != null) {
                    try {
                        dbInstance.rollbackServiceInstance(em);
                    } catch (BadResultException e) {
                        throw new ServiceInstanceException(
                                e.getLocalizedMessage());
                    }
                }
                besDao.notifyAsyncModifySubscription(dbInstance,
                        createInstanceResult(dbInstance), false,
                        new APPlatformException(
                                Messages.getAll("abort_pending_subscription")));
                break;
            case WAITING_FOR_SYSTEM_UPGRADE:
                if (dbInstance.getRollbackParameters() != null) {
                    try {
                        dbInstance.rollbackServiceInstance(em);
                    } catch (BadResultException e) {
                        throw new ServiceInstanceException(
                                e.getLocalizedMessage());
                    }
                }
                besDao.notifyAsyncUpgradeSubscription(dbInstance,
                        createInstanceResult(dbInstance), false, null);
                break;
            case WAITING_FOR_SYSTEM_OPERATION:
                Operation operation = opDao
                        .getOperationByInstanceId(dbInstance.getInstanceId());
                if (operation != null) {
                    besDao.notifyAsyncOperationStatus(dbInstance,
                            operation.getTransactionId(), OperationStatus.ERROR,
                            Messages.getAll("abort_system_operation"));
                    em.remove(operation);
                }
                break;
            default:
                throw new ServiceInstanceException(
                        Messages.get(locale, "error_instance_status_wrong",
                                ALLOWED_STATUS_ABORT_COMPLETE));
            }
            dbInstance.setProvisioningStatus(ProvisioningStatus.COMPLETED);
            dbInstance.setControllerReady(true);
        } catch (ServiceInstanceNotFoundException e) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_instance_not_exist"),
                    serviceInstance.getInstanceId());
        } catch (BESNotificationException e) {
            throw new ServiceInstanceException(getMessage(e));
        }
    }

    void completePendingInstance(ServiceInstance serviceInstance, String locale)
            throws ServiceInstanceException {
        try {
            ServiceInstance dbInstance = dao.find(serviceInstance);
            if (dbInstance.isControllerReady()) {
                switch (dbInstance.getProvisioningStatus()) {
                case WAITING_FOR_SYSTEM_CREATION:
                    besDao.notifyAsyncSubscription(dbInstance,
                            createInstanceResult(dbInstance), true, null);
                    break;
                case WAITING_FOR_SYSTEM_MODIFICATION:
                    besDao.notifyAsyncModifySubscription(dbInstance,
                            createInstanceResult(dbInstance), true, null);
                    break;
                case WAITING_FOR_SYSTEM_UPGRADE:
                    besDao.notifyAsyncUpgradeSubscription(dbInstance,
                            createInstanceResult(dbInstance), true, null);
                    break;
                case WAITING_FOR_SYSTEM_OPERATION:
                    Operation operation = opDao.getOperationByInstanceId(
                            dbInstance.getInstanceId());
                    if (operation != null) {
                        besDao.notifyAsyncOperationStatus(dbInstance,
                                operation.getTransactionId(),
                                OperationStatus.COMPLETED, null);
                        em.remove(operation);
                    }
                    break;
                default:
                    throw new ServiceInstanceException(
                            Messages.get(locale, "error_instance_status_wrong",
                                    ALLOWED_STATUS_ABORT_COMPLETE));
                }

            } else {
                throw new ServiceInstanceException(
                        Messages.get(locale, "error_bes_notification",
                                serviceInstance.getInstanceId()));
            }
            dbInstance.setProvisioningStatus(ProvisioningStatus.COMPLETED);
        } catch (ServiceInstanceNotFoundException e) {
            throw new ServiceInstanceException(
                    Messages.get(locale, "error_instance_not_exist"),
                    serviceInstance.getInstanceId());
        } catch (BESNotificationException e) {
            throw new ServiceInstanceException(getMessage(e));
        }
    }

    /**
     * List all available operations for the given service instance.
     * 
     * @param serviceInstance
     *            - the service instance object
     * @return a set of instance operations
     */
    public EnumSet<InstanceOperation> listOperationsForInstance(
            ServiceInstance serviceInstance) {
        return EnumSet.allOf(InstanceOperation.class);
    }

    public ServiceInstance updateVmsNumber(ServiceInstance serviceInstance,
            Integer vmsNumber) {
        return dao.updateVmsNumber(serviceInstance, vmsNumber);
    }

    public void notifySubscriptionAboutVmsNumber(
            ServiceInstance serviceInstance) {
        try {
            besDao.notifySubscriptionAboutVmsNumber(serviceInstance);
        } catch (BESNotificationException e) {
            logger.error(e.getMessage());
        }
    }

    public void updateLastUsageFetch(ServiceInstance serviceInstance,
            String endTime) {
        dao.updateParam(serviceInstance, endTime, "LAST_USAGE_FETCH");
    }

}
