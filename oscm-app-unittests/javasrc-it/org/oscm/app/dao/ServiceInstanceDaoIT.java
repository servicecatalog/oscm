/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.junit.Test;

import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;

public class ServiceInstanceDaoIT extends EJBTestBase {

    private static ServiceInstanceDAO instanceDAO = spy(new ServiceInstanceDAO());
    private static EntityManager em;
    private ServiceInstance si1;
    private static final String INSTANCEID_1 = "instanceId_1";
    private static final String INSTANCEID_2 = "instanceId_2";
    private static final String INSTANCEID_3 = "instanceId_3";
    private static final String CONTROLLERID = "PROXY";

    @Override
    protected void setup(TestContainer container) throws Exception {
        em = container.getPersistenceUnit("oscm-app");
        instanceDAO.em = em;
        si1 = createInstance(INSTANCEID_1);
        createInstance(INSTANCEID_2);
        createInstance(INSTANCEID_3);
    }

    @Test
    public void getInstanceById() throws Exception {
        // when
        ServiceInstance siRetrieved = getInstanceById(INSTANCEID_1);

        // then
        assertEquals(si1.getTkey(), siRetrieved.getTkey());
    }

    @Test
    public void getInstanceBySubscriptionAndOrganization() throws Exception {
        // when
        ServiceInstance siRetrieved = getInstanceBySubscriptionAndOrganization(
                si1.getSubscriptionId(), si1.getOrganizationId());

        // then
        assertEquals(si1.getTkey(), siRetrieved.getTkey());
    }

    @Test
    public void deleteInstance() throws Exception {
        // when
        deleteInstance(si1);

        // then
        List<ServiceInstance> serviceInstances = getInstancesForController(CONTROLLERID);
        assertEquals(2, serviceInstances.size());
    }

    @Test
    public void resumeInstance() throws Exception {
        // given
        ServiceInstance si2 = getInstanceById(INSTANCEID_2);

        // when
        resumeInstance(si2);
        ServiceInstance siRetrieved = getInstanceById(INSTANCEID_2);

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(siRetrieved.getRunWithTimer()));
    }

    @Test
    public void suspendInstance() throws Exception {
        // given
        ServiceInstance si2 = getInstanceById(INSTANCEID_2);

        // when
        suspendInstance(si2);
        ServiceInstance siRetrieved = getInstanceById(INSTANCEID_2);

        // then
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(siRetrieved.getRunWithTimer()));
    }

    @Test
    public void abortPendingInstance() throws Exception {
        // given
        ServiceInstance si2 = getInstanceById(INSTANCEID_2);

        // when
        suspendInstance(si2);
        abortPendingInstance(si2);
        ServiceInstance siRetrieved = getInstanceById(INSTANCEID_2);

        // then
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(siRetrieved.getRunWithTimer()));
    }

    @Test
    public void unlockInstance() throws Exception {
        // given
        ServiceInstance si2 = getInstanceById(INSTANCEID_2);

        // when
        unlockInstance(si2);
        ServiceInstance siRetrieved = getInstanceById(INSTANCEID_2);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(siRetrieved.isLocked()));
    }

    @Test
    public void prepareDeletionAndRestoreInstance() throws Exception {

        // given
        ServiceInstance si3 = getInstanceById(INSTANCEID_3);

        // when
        prepareDeletionInstance(si3);

        // then
        si3 = getInstanceById(INSTANCEID_3);
        assertTrue(si3.getSubscriptionId().contains("#"));

        // when
        restoreInstance(si3);

        // then
        si3 = getInstanceById(INSTANCEID_3);
        assertFalse(si3.getSubscriptionId().contains("#"));
    }

    ServiceInstance createInstance(final String id) throws Exception {
        return runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                ServiceInstance si = new ServiceInstance();
                si.setInstanceId(id);
                si.setServiceBaseURL("baseURL");
                si.setBesLoginURL("loginUrl");
                si.setControllerId(CONTROLLERID);
                si.setDefaultLocale("en");
                si.setOrganizationId(id + "_orgId");
                si.setSubscriptionId(id + "_subId");
                si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
                si.setServiceAccessInfo("serviceAccessInfo");
                si.setServiceBaseURL("serviceBaseURL");
                si.setServiceLoginPath("serviceLoginPath");
                si.setSuspendedByApp(true);
                em.persist(si);
                em.flush();
                return si;
            }
        });
    }

    ServiceInstance getInstanceById(final String instanceId) throws Exception {
        return runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                return instanceDAO.getInstanceById(instanceId);
            }
        });
    }

    ServiceInstance getInstanceBySubscriptionAndOrganization(
            final String subscriptionId, final String organizationId)
            throws Exception {
        return runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                return instanceDAO.getInstanceBySubscriptionAndOrganization(
                        subscriptionId, organizationId);
            }
        });
    }

    void deleteInstance(final ServiceInstance instance) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                instanceDAO.deleteInstance(instance);
                return null;
            }
        });
    }

    void resumeInstance(final ServiceInstance instance) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                instanceDAO.resumeInstance(instance);
                return null;
            }
        });
    }

    void suspendInstance(final ServiceInstance instance) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                instanceDAO.suspendInstance(instance);
                return null;
            }
        });
    }

    void abortPendingInstance(final ServiceInstance instance) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                instanceDAO.abortPendingInstance(instance);
                return null;
            }
        });
    }

    void unlockInstance(final ServiceInstance instance) throws Exception {
        runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                instanceDAO.unlockInstance(instance);
                return null;
            }
        });
    }

    void prepareDeletionInstance(final ServiceInstance instance)
            throws Exception {
        runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                instanceDAO.markAsDeleted(instance);
                return null;
            }
        });
    }

    void restoreInstance(final ServiceInstance instance) throws Exception {
        runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                instanceDAO.restoreInstance(instance);
                return null;
            }
        });
    }

    List<ServiceInstance> getInstancesForController(final String controllerId)
            throws Exception {
        return runTX(new Callable<List<ServiceInstance>>() {
            @Override
            public List<ServiceInstance> call() throws Exception {
                List<ServiceInstance> serviceInstances = instanceDAO
                        .getInstancesForController(controllerId);
                return serviceInstances;
            }
        });
    }

    @Test
    public void getInstancesSuspendedbyApp_OK() throws Exception {
        // when
        List<ServiceInstance> instances = getInstancesSuspendedbyApp();

        // then
        assertEquals(3, instances.size());
    }

    List<ServiceInstance> getInstancesSuspendedbyApp() throws Exception {
        return runTX(new Callable<List<ServiceInstance>>() {
            @Override
            public List<ServiceInstance> call() throws Exception {
                return instanceDAO.getInstancesSuspendedbyApp();
            }
        });
    }

    // public void prepareDeletionInstance(ServiceInstance serviceInstance)
    // throws ServiceInstanceException {
    // ServiceInstance newDbInstance = null;
    // try {
    // newDbInstance = getInstanceById(serviceInstance.getInstanceId());
    // newDbInstance.setSubscriptionId(newDbInstance.getSubscriptionId()
    // + "#" + UUID.randomUUID());
    // // em.persist(newDbInstance);
    // em.flush();
    // } catch (BadResultException ex) {
    // throw new ServiceInstanceException(ex);
    // }
    // }
    //
    // public void restoreInstance(ServiceInstance serviceInstance)
    // throws ServiceInstanceException {
    // try {
    // ServiceInstance dbInstance = getInstanceById(serviceInstance
    // .getInstanceId());
    // // dbInstance.unmarkForDeletion();
    // int index = dbInstance.getSubscriptionId().indexOf("#");
    // if (index >= 0) {
    // dbInstance.setSubscriptionId(dbInstance.getSubscriptionId()
    // .substring(0, index));
    // }
    // // dbInstance = em.merge(dbInstance);
    // em.flush();
    // } catch (BadResultException ex) {
    // throw new ServiceInstanceException(ex);
    // }
    //
    // }

}
