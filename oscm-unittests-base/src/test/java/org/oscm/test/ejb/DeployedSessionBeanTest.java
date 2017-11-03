/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.RollbackException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Test;

/**
 * Unit tests for {@link DeployedSessionBean}.
 * 
 * @author hoffmann
 */
public class DeployedSessionBeanTest {

    private final TransactionManager tmStub = new TransactionManager() {

        public void begin() {
            fail();
        }

        public void commit() {
            fail();
        }

        public void rollback() throws RollbackException {
            fail();
        }

        public void setRollbackOnly() {
            fail();
        }

        public Transaction suspend() {
            fail();
            return null;
        }

        public void resume(Transaction t) {
            fail();
        }

        public int getStatus() {
            return Status.STATUS_NO_TRANSACTION;
        }

        public Transaction getTransaction() {
            fail();
            return null;
        }

        public void setTransactionTimeout(int arg0) {
            fail();
        }

    };

    private final SessionContext sessionContext = new TestSessionContext(null,
            null);

    @Test(expected = IllegalArgumentException.class)
    public void testGetInterfaceNegative() {
        DeployedSessionBean bean = new DeployedSessionBean(tmStub,
                sessionContext, new Object());
        bean.getInterfaceOrClass(Runnable.class);
    }

    @Test
    public void testCall() throws Exception {
        final Object value = new Object();
        @TransactionAttribute(TransactionAttributeType.NEVER)
        class Bean implements Callable<Object> {
            public Object call() {
                return value;
            }
        }
        DeployedSessionBean bean = new DeployedSessionBean(tmStub,
                sessionContext, new Bean());
        assertSame(value, bean.getInterfaceOrClass(Callable.class).call());
    }

    @Test(expected = EJBException.class)
    public void testCallWithSystemException() {
        @TransactionAttribute(TransactionAttributeType.NEVER)
        class Bean implements Runnable {
            public void run() {
                throw new RuntimeException();
            }
        }
        DeployedSessionBean bean = new DeployedSessionBean(tmStub,
                sessionContext, new Bean());
        bean.getInterfaceOrClass(Runnable.class).run();
    }

    @ApplicationException
    class AppException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

    @Test(expected = AppException.class)
    public void testCallWithApplicationException1() {
        @TransactionAttribute(TransactionAttributeType.NEVER)
        class Bean implements Runnable {
            public void run() {
                throw new AppException();
            }
        }
        DeployedSessionBean bean = new DeployedSessionBean(tmStub,
                sessionContext, new Bean());
        bean.getInterfaceOrClass(Runnable.class).run();
    }

    @Test(expected = IOException.class)
    public void testCallWithApplicationException2() throws Exception {
        @TransactionAttribute(TransactionAttributeType.NEVER)
        class Bean implements Callable<Void> {
            public Void call() throws Exception {
                throw new IOException();
            }
        }
        DeployedSessionBean bean = new DeployedSessionBean(tmStub,
                sessionContext, new Bean());
        bean.getInterfaceOrClass(Callable.class).call();
    }
}
