/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import org.junit.Test;

import org.oscm.test.EJBTestBase;
import org.oscm.test.ejb.TestContainer;
import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ProvisioningStatus;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.v2_0.intf.APPlatformController;

public class OperationDaoIT extends EJBTestBase {

    private static OperationDAO opDAO = spy(new OperationDAO());
    private static EntityManager em;
    private static String USER_ID = "userId";
    private static String TX_ID = "transactionId";
    private static String OP_ID = "opId";

    @Override
    protected void setup(TestContainer container) throws Exception {
        em = container.getPersistenceUnit("oscm-app");
        opDAO.em = em;
    }

    @Test
    public void addOperationForQueue() throws Exception {
        // given
        ServiceInstance si = createInstance("instanceId");
        Properties prop = givenOperationDescription();
        String transactionId = "transactionId";

        // when
        Operation op = addOperationForQueue(si, prop, transactionId);

        // then
        Operation dbOp = retrieveOperation(op.getTkey());
        assertNotNull(dbOp);
        assertEquals(si, op.getServiceInstance());
        assertEquals(OP_ID, op.getOperationId());
        assertEquals(USER_ID, op.getUserId());
        assertEquals(TX_ID, op.getTransactionId());
        assertEquals(Boolean.valueOf(true), Boolean.valueOf(op.isForQueue()));
    }

    @Test
    public void addOperation() throws Exception {
        // given
        ServiceInstance si = createInstance("instanceId");
        Properties prop = givenOperationDescription();

        // when
        Operation op = this.addOperation(si, prop, TX_ID);

        // then
        Operation dbOp = retrieveOperation(op.getTkey());
        assertNotNull(dbOp);
        assertEquals(si, op.getServiceInstance());
        assertEquals(OP_ID, op.getOperationId());
        assertEquals(USER_ID, op.getUserId());
        assertEquals(TX_ID, op.getTransactionId());
        assertEquals(Boolean.valueOf(false), Boolean.valueOf(op.isForQueue()));
    }

    @Test
    public void getOperationFromQueue_emptyQueue() throws Exception {
        assertNull(getOperationFromQueue("instanceid"));
    }

    @Test
    public void getOperationFromQueue_oneInQueue() throws Exception {
        // given
        List<Operation> operations = createOperationsForQueue(1, 1);

        // when
        Operation dbOp = getOperationFromQueue(operations.get(0)
                .getServiceInstance().getInstanceId());

        // then
        assertNotNull(dbOp);
        assertEquals(operations.get(0).getTkey(), dbOp.getTkey());
        assertEquals(Boolean.valueOf(true), Boolean.valueOf(dbOp.isForQueue()));
    }

    @Test
    public void getOperation_oneNotInQueue() throws Exception {
        // given
        List<Operation> operations = createOperationsNotForQueue(1, 1);

        // when
        Operation dbOp = getOperationByInstanceId(operations.get(0)
                .getServiceInstance().getInstanceId());

        // then
        assertNotNull(dbOp);
        assertEquals(operations.get(0).getTkey(), dbOp.getTkey());
        assertEquals(Boolean.valueOf(false), Boolean.valueOf(dbOp.isForQueue()));
    }

    @Test
    public void getOperationFromQueue_moreInQueue() throws Exception {
        // given
        List<Operation> operations = createOperationsForQueue(3, 2);

        // when
        Operation dbOp = getOperationFromQueue(operations.get(0)
                .getServiceInstance().getInstanceId());

        // then
        assertNotNull(dbOp);
        assertEquals(operations.get(0).getTkey(), dbOp.getTkey());
        assertEquals(Boolean.valueOf(true), Boolean.valueOf(dbOp.isForQueue()));
    }

    @Test
    public void getOperation_moreNotInQueue() throws Exception {
        // given
        List<Operation> operations = createOperationsNotForQueue(3, 2);

        // when
        Operation dbOp = getOperationByInstanceId(operations.get(0)
                .getServiceInstance().getInstanceId());

        // then
        assertNotNull(dbOp);
        assertEquals(operations.get(0).getTkey(), dbOp.getTkey());
        assertEquals(Boolean.valueOf(false), Boolean.valueOf(dbOp.isForQueue()));
    }

    @Test
    public void clear_emptyQueue() throws Exception {
        // when
        clear();

        // then
        assertNull(getOperationFromQueue("instanceId"));
    }

    @Test
    public void clear_Queue() throws Exception {
        // given
        List<Operation> operations = createOperationsForQueue(3, 1);

        // when
        clear();

        // then
        assertNull(retrieveOperation(operations.get(0).getTkey()));
        assertNull(retrieveOperation(operations.get(1).getTkey()));
        assertNull(retrieveOperation(operations.get(2).getTkey()));
    }

    @Test
    public void clear_NotForQueue() throws Exception {
        // given
        List<Operation> operations = createOperationsNotForQueue(3, 1);

        // when
        clear();

        // then
        assertNotNull(retrieveOperation(operations.get(0).getTkey()));
        assertNotNull(retrieveOperation(operations.get(1).getTkey()));
        assertNotNull(retrieveOperation(operations.get(2).getTkey()));
    }

    @Test
    public void removeOperation_Queue() throws Exception {
        // given
        List<Operation> operations = createOperationsForQueue(3, 1);

        // when
        removeOperation(operations.get(0).getTkey());

        // then
        assertNull(retrieveOperation(operations.get(0).getTkey()));
        assertNotNull(retrieveOperation(operations.get(1).getTkey()));
        assertNotNull(retrieveOperation(operations.get(2).getTkey()));
    }

    ServiceInstance createInstance(final String id) throws Exception {
        return runTX(new Callable<ServiceInstance>() {
            @Override
            public ServiceInstance call() throws Exception {
                ServiceInstance si = new ServiceInstance();
                si.setInstanceId(id);
                si.setServiceBaseURL("baseURL");
                si.setBesLoginURL("loginUrl");
                si.setControllerId("PROXY");
                si.setDefaultLocale("en");
                si.setOrganizationId(id + "_orgId");
                si.setSubscriptionId(id + "_subId");
                si.setProvisioningStatus(ProvisioningStatus.COMPLETED);
                si.setServiceAccessInfo("serviceAccessInfo");
                si.setServiceBaseURL("serviceBaseURL");
                si.setServiceLoginPath("serviceLoginPath");
                em.persist(si);
                em.flush();
                return si;
            }
        });
    }

    Operation retrieveOperation(final long key) throws Exception {
        return runTX(new Callable<Operation>() {
            @Override
            public Operation call() throws Exception {
                Operation op = em.find(Operation.class, Long.valueOf(key));
                return op;
            }
        });
    }

    Operation addOperationForQueue(final ServiceInstance si,
            final Properties opDescription, final String transactionId)
            throws Exception {
        return runTX(new Callable<Operation>() {
            @Override
            public Operation call() throws Exception {
                return opDAO.addOperationForQueue(si, opDescription,
                        transactionId);
            }
        });
    }

    Operation addOperation(final ServiceInstance si,
            final Properties opDescription, final String transactionId)
            throws Exception {
        return runTX(new Callable<Operation>() {
            @Override
            public Operation call() throws Exception {
                Operation op = new Operation();
                op.setForQueue(false);
                op.setServiceInstance(si);
                op.setTransactionId(transactionId);
                op.setFromProperties(opDescription);
                em.persist(op);
                return op;
            }
        });
    }

    Operation getOperationFromQueue(final String instanceId) throws Exception {
        return runTX(new Callable<Operation>() {
            @Override
            public Operation call() throws Exception {
                return opDAO.getOperationFromQueue(instanceId);
            }
        });
    }

    Operation getOperationByInstanceId(final String instanceId)
            throws Exception {
        return runTX(new Callable<Operation>() {
            @Override
            public Operation call() throws Exception {
                return opDAO.getOperationByInstanceId(instanceId);
            }
        });
    }

    Void clear() throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                opDAO.clear();
                return null;
            }
        });
    }

    Void removeOperation(final long opKey) throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                opDAO.removeOperation(opKey);
                return null;
            }
        });
    }

    Properties givenOperationDescription() {
        Properties prop = new Properties();
        prop.put(APPlatformController.KEY_OPERATION_ID, OP_ID);
        prop.put(APPlatformController.KEY_OPERATION_USER_ID, USER_ID);
        return prop;
    }

    Properties givenOperationDescription(int counter) {
        Properties prop = new Properties();
        prop.put(APPlatformController.KEY_OPERATION_ID, OP_ID + counter);
        prop.put(APPlatformController.KEY_OPERATION_USER_ID, USER_ID + counter);
        return prop;
    }

    List<Operation> createOperationsForQueue(int numInst, int numOp)
            throws Exception {
        List<Operation> list = new ArrayList<Operation>();
        for (int i = 0; i < numInst; i++) {
            ServiceInstance si = createInstance("instanceId" + i);
            String transactionId = "transactionId" + i;
            for (int j = 0; j < numOp; j++) {
                Properties prop = givenOperationDescription(j);
                Operation op = addOperationForQueue(si, prop, transactionId);
                list.add(op);
            }
        }
        return list;
    }

    List<Operation> createOperationsNotForQueue(int numInst, int numOp)
            throws Exception {
        List<Operation> list = new ArrayList<Operation>();
        for (int i = 0; i < numInst; i++) {
            ServiceInstance si = createInstance("instanceId" + i);
            for (int j = 0; j < numOp; j++) {
                Properties prop = givenOperationDescription(j);
                Operation op = addOperation(si, prop, TX_ID + j);
                list.add(op);
            }
        }
        return list;
    }
}
