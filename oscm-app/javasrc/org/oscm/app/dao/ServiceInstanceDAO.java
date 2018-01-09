/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-02-25                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.dao;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.app.business.exceptions.ServiceInstanceNotFoundException;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.types.enumtypes.LogMessageIdentifier;

@Stateless
@LocalBean
public class ServiceInstanceDAO {

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    public EntityManager em;
    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(ServiceInstanceDAO.class);

    public ServiceInstance getInstanceById(String instanceId)
            throws ServiceInstanceNotFoundException {
        if (Strings.isEmpty(instanceId)) {
            throw new ServiceInstanceNotFoundException(
                    "Service instance ID not set or empty.");
        }

        Query query = em.createNamedQuery("ServiceInstance.getForKey");
        query.setParameter("key", instanceId);
        try {
            final ServiceInstance instance = (ServiceInstance) query
                    .getSingleResult();
            return instance;
        } catch (NoResultException e) {
            throw new ServiceInstanceNotFoundException(
                    "Service instance with ID '%s' not found.", instanceId);
        }
    }

    public ServiceInstance getInstance(String instanceId,
            String subscriptionId, String organizationId)
            throws ServiceInstanceNotFoundException {
        if (!Strings.isEmpty(instanceId)) {
            return getInstanceById(instanceId);
        }

        return getInstanceBySubscriptionAndOrganization(subscriptionId,
                organizationId);
    }

    public ServiceInstance getInstanceBySubscriptionAndOrganization(
            String subscriptionId, String organizationId)
            throws ServiceInstanceNotFoundException {
        if (Strings.isEmpty(subscriptionId) || Strings.isEmpty(organizationId)) {
            throw new ServiceInstanceNotFoundException(
                    "Subscription or organization ID not set or empty.");
        }
        Query query = em
                .createNamedQuery("ServiceInstance.getForSubscriptionAndOrg");
        query.setParameter("subscriptionId", subscriptionId);
        query.setParameter("organizationId", organizationId);
        try {
            final ServiceInstance instance = (ServiceInstance) query
                    .getSingleResult();
            return instance;
        } catch (NoResultException e) {
            throw new ServiceInstanceNotFoundException(
                    "Service instance for subscription '%s' and organization '%s' not found.",
                    subscriptionId, organizationId);
        }
    }

    public ServiceInstance getInstanceById(String controllerId,
            String instanceId) throws ServiceInstanceNotFoundException {
        if (Strings.isEmpty(controllerId) || Strings.isEmpty(instanceId)) {
            throw new ServiceInstanceNotFoundException(
                    "Service instance or controller ID not set or empty.");
        }
        ServiceInstance result = null;
        Query query = em.createNamedQuery("ServiceInstance.getForCtrlKey");
        query.setParameter("key", instanceId);
        query.setParameter("cid", controllerId);
        try {
            result = (ServiceInstance) query.getSingleResult();
        } catch (NoResultException e) {
            throw new ServiceInstanceNotFoundException(
                    "Service instance with ID '%s' for controller '%s' not found.",
                    instanceId, controllerId);
        }
        return result;
    }

    public ServiceInstance getLockedInstanceForController(String controllerId) {
        ServiceInstance instance = null;
        Query query = em.createNamedQuery("ServiceInstance.getLockedService");
        query.setParameter("cid", controllerId);
        query.setMaxResults(1);
        try {
            instance = (ServiceInstance) query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e, LogMessageIdentifier.ERROR);
        }
        return instance;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void deleteInstance(ServiceInstance serviceInstance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = find(serviceInstance);
        em.remove(dbInstance);
        em.flush();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public ServiceInstance markAsDeleted(ServiceInstance instance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = find(instance);
        dbInstance.markForDeletion();
        em.flush();
        return dbInstance;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void restoreInstance(ServiceInstance instance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = find(instance);
        dbInstance.unmarkForDeletion();
        em.flush();

    }

    public ServiceInstance resumeInstance(ServiceInstance instance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = find(instance);
        dbInstance.setRunWithTimer(true);
        em.flush();
        return dbInstance;
    }

    public ServiceInstance abortPendingInstance(ServiceInstance instance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = find(instance);
        dbInstance.setRunWithTimer(true);
        em.flush();
        return dbInstance;
    }

    public void suspendInstance(ServiceInstance instance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = find(instance);
        dbInstance.setRunWithTimer(false);
        dbInstance.setLocked(false);
        em.flush();

    }

    public void unlockInstance(ServiceInstance instance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = find(instance);
        dbInstance.setLocked(false);
        em.flush();

    }

    public ServiceInstance find(ServiceInstance instance)
            throws ServiceInstanceNotFoundException {
        ServiceInstance dbInstance = em.find(ServiceInstance.class,
            instance.getTkey());
        if (dbInstance == null) {
            dbInstance = getInstanceById(instance.getInstanceId());
        }
        return dbInstance;
    }

    /**
     * Fetch all instances for the given controller.
     */
    public List<ServiceInstance> getInstancesForController(String controllerId) {
        if (controllerId == null) {
            throw new IllegalArgumentException();
        }
        Query query = em.createNamedQuery("ServiceInstance.getAllForCtrl");
        query.setParameter("cid", controllerId);
        @SuppressWarnings("unchecked")
        List<ServiceInstance> resultList = query.getResultList();
        return resultList;
    }

    /**
     * Fetch all instance parameter for given service instance.
     */
    public List<InstanceParameter> getInstanceParameters(
            ServiceInstance serviceInstance) {
        if (serviceInstance == null) {
            throw new IllegalArgumentException();
        }
        Query query = em
                .createNamedQuery("InstanceParameter.getAllForInstanceId");
        query.setParameter("sid", serviceInstance.getInstanceId());
        query.setParameter("cid", serviceInstance.getControllerId());
        @SuppressWarnings("unchecked")
        List<InstanceParameter> result = query.getResultList();
        return result;
    }

    public List<ServiceInstance> getInstancesInWaitingState() {
        Query query = em
                .createNamedQuery("ServiceInstance.getForStatusWithTimer");
        query.setParameter("status", ProvisioningStatus.getWaiting());
        @SuppressWarnings("unchecked")
        List<ServiceInstance> result = query.getResultList();
        return result;
    }

    public boolean exists(String controllerId, String instanceId) {
        Query query = em.createNamedQuery("ServiceInstance.getForCtrlKey");
        query.setParameter("key", instanceId);
        query.setParameter("cid", controllerId);
        try {
            query.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    public boolean exists(String instanceId) {
        Query query = em.createNamedQuery("ServiceInstance.getForKey");
        query.setParameter("key", instanceId);
        try {
            query.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
        return true;
    }

    public List<ServiceInstance> getInstancesSuspendedbyApp() {
        Query query = em
            .createNamedQuery("ServiceInstance.getForSuspendedByApp");
        return query.getResultList();
    }

    public List<ServiceInstance> getInstances() {
        Query query = em
            .createNamedQuery("ServiceInstance.getAll");
        return query.getResultList();
    }


    public ServiceInstance updateVmsNumber(ServiceInstance serviceInstance, Integer vmsNumber) {
//        Query query = em.createNamedQuery("ServiceInstance.getForKey");
//        query.setParameter("key", serviceInstance.getInstanceId());
//        ServiceInstance si = (ServiceInstance) query.getSingleResult();
//        si.setVmsNumber(vmsNumber);
//        return em.merge(si);
        return serviceInstance;
    }
}
