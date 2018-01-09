/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.dao;

import java.util.Properties;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.oscm.app.domain.Operation;
import org.oscm.app.domain.ServiceInstance;

@Stateless
public class OperationDAO {

    @PersistenceContext(name = "persistence/em", unitName = "oscm-app")
    protected EntityManager em;

    public Operation addOperationForQueue(ServiceInstance si,
            Properties opParameters, String transactionId) {
        Operation op = createOperationForQueue(si, opParameters, transactionId);
        em.persist(op);
        return op;
    }

    public Operation addOperation(ServiceInstance si, Properties opParameters,
            String transactionId) {
        Operation op = createOperation(si, opParameters, transactionId);
        em.persist(op);
        return op;
    }

    public Operation getOperationFromQueue(String instanceId) {
        Query query = em
                .createNamedQuery("Operation.getFirstOperationFromQueue");
        query.setParameter("id", instanceId);
        query.setMaxResults(1);
        try {
            return (Operation) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Operation getOperationByInstanceId(String instanceId) {
        Query query = em.createNamedQuery("Operation.getOperationByInstanceId");
        query.setParameter("id", instanceId);
        query.setMaxResults(1);
        try {
            return (Operation) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void removeOperation(long operationKey) {
        Query query = em.createNamedQuery("Operation.removeForKey");
        query.setParameter("key", Long.valueOf(operationKey));
        query.executeUpdate();
    }

    Operation createOperationForQueue(ServiceInstance si,
            Properties opParameters, String transactionId) {
        Operation op = new Operation();
        op.setForQueue(true);
        op.setServiceInstance(si);
        op.setTransactionId(transactionId);
        op.setFromProperties(opParameters);
        return op;
    }

    Operation createOperation(ServiceInstance si, Properties opParameters,
            String transactionId) {
        Operation op = new Operation();
        op.setForQueue(false);
        op.setServiceInstance(si);
        op.setTransactionId(transactionId);
        op.setFromProperties(opParameters);
        return op;
    }

    public void clear() {
        Query query = em.createNamedQuery("Operation.removeAll");
        query.executeUpdate();
    }

}
