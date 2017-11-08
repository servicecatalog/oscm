/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.junit.Test;

/**
 * Unit tests for {@link TransactionInvocationHandlers}.
 * 
 * @author hoffmann
 */
public class TransactionInvocationHandlersTest {

    private final List<String> stubCalls = new ArrayList<String>();

    private boolean failCommit = false;

    private final TransactionManager tmStub = new TransactionManager() {

        private int status = Status.STATUS_NO_TRANSACTION;

        @Override
        public void begin() {
            status = Status.STATUS_ACTIVE;
            stubCalls.add("begin()");
        }

        @Override
        public void commit() {
            status = Status.STATUS_NO_TRANSACTION;
            stubCalls.add("commit()");
            if (failCommit) {
                throw new RollbackException();
            }
        }

        @Override
        public void rollback() throws RollbackException {
            status = Status.STATUS_NO_TRANSACTION;
            stubCalls.add("rollback()");
        }

        @Override
        public void setRollbackOnly() {
            status = Status.STATUS_MARKED_ROLLBACK;
            stubCalls.add("setRollbackOnly()");
        }

        @Override
        public Transaction suspend() {
            stubCalls.add("suspend()");
            if (status == Status.STATUS_NO_TRANSACTION) {
                return null;
            }
            return new Transaction() {

                @Override
                public void setRollbackOnly() {
                    fail();
                }

                @Override
                public void rollback() {
                    fail();
                }

                @Override
                public void registerSynchronization(Synchronization arg0) {
                    fail();
                }

                @Override
                public int getStatus() {
                    fail();
                    return 0;
                }

                @Override
                public boolean enlistResource(XAResource arg0) {
                    fail();
                    return false;
                }

                @Override
                public boolean delistResource(XAResource arg0, int arg1) {
                    fail();
                    return false;
                }

                @Override
                public void commit() {
                    fail();
                }
            };
        }

        @Override
        public void resume(Transaction t) {
            assertNotNull(t);
            stubCalls.add("resume()");
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public Transaction getTransaction() {
            fail();
            return null;
        }

        @Override
        public void setTransactionTimeout(int arg0) {
            fail();
        }

    };

    private boolean ctxStubIsApplicationException = false;

    private final IInvocationHandler.IInvocationCtx ctxStub = new IInvocationHandler.IInvocationCtx() {

        @Override
        public boolean isApplicationException(Exception e) {
            return ctxStubIsApplicationException;
        }

        @Override
        public TransactionManager getTransactionManager() {
            return tmStub;
        }
    };

    private Callable<Object> callableReturns(final Object value) {
        return new Callable<Object>() {
            @Override
            public Object call() {
                stubCalls.add("call()");
                return value;
            }
        };
    }

    private Callable<Object> callableMarkRollback() {
        return new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                stubCalls.add("call()");
                tmStub.setRollbackOnly();
                return null;
            }
        };
    }

    private Callable<Object> callableException(final Exception ex) {
        return new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                stubCalls.add("call()");
                throw ex;
            }
        };
    }

    private Callable<Object> callableError(final Error err) {
        return new Callable<Object>() {
            @Override
            public Object call() {
                stubCalls.add("call()");
                throw err;
            }
        };
    }

    private Callable<Object> callableExceptionAndMarkRollback(final Exception ex) {
        return new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                stubCalls.add("call()");
                tmStub.setRollbackOnly();
                throw ex;
            }
        };
    }

    @ApplicationException(rollback = true)
    private static class ExceptionWithRollback extends IOException {

        private static final long serialVersionUID = 1L;

    }

    @ApplicationException(rollback = false)
    private static class ExceptionWithoutRollback extends IOException {

        private static final long serialVersionUID = 1L;

    }

    // =========================================================================
    // Internal Handlers
    // =========================================================================

    @Test
    public void testHANDLER_NOTX_Positive() throws Exception {
        final Object v = new Object();
        assertSame(v, TransactionInvocationHandlers.HANDLER_NOTX.call(
                callableReturns(v), ctxStub));
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testHANDLER_NOTX_ApplicationException() throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new UnsupportedOperationException();
        try {
            TransactionInvocationHandlers.HANDLER_NOTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (UnsupportedOperationException ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testHANDLER_NOTX_Exception() throws Exception {
        final Exception root = new IOException();
        try {
            TransactionInvocationHandlers.HANDLER_NOTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (EJBException ejbex) {
            assertSame(root, ejbex.getCause());
            assertSame(root, ejbex.getCausedByException());
        }
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testHANDLER_WITHINTX_Positive() throws Exception {
        final Object v = new Object();
        assertSame(v, TransactionInvocationHandlers.HANDLER_WITHINTX.call(
                callableReturns(v), ctxStub));
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testHANDLER_WITHINTX_ApplicationException() throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new UnsupportedOperationException();
        try {
            TransactionInvocationHandlers.HANDLER_WITHINTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (UnsupportedOperationException ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testHANDLER_WITHINTX_ApplicationExceptionWithRollback()
            throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new ExceptionWithRollback();
        try {
            TransactionInvocationHandlers.HANDLER_WITHINTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (ExceptionWithRollback ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("call()", "setRollbackOnly()"), stubCalls);
    }

    @Test
    public void testHANDLER_WITHINTX_ApplicationExceptionWithoutRollback()
            throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new ExceptionWithoutRollback();
        try {
            TransactionInvocationHandlers.HANDLER_WITHINTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (ExceptionWithoutRollback ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testHANDLER_WITHINTX_Exception() throws Exception {
        final Exception root = new IOException();
        try {
            TransactionInvocationHandlers.HANDLER_WITHINTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (EJBTransactionRolledbackException ejbex) {
            assertSame(root, ejbex.getCause());
            assertSame(root, ejbex.getCausedByException());
        }
        assertEquals(Arrays.asList("call()", "setRollbackOnly()"), stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_Positive() throws Exception {
        final Object v = new Object();
        assertSame(v, TransactionInvocationHandlers.HANDLER_NEWTX.call(
                callableReturns(v), ctxStub));
        assertEquals(Arrays.asList("begin()", "call()", "commit()"), stubCalls);

    }

    @Test
    public void testHANDLER_NEWTX_Positive_MarkedRollback() throws Exception {
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableMarkRollback(), ctxStub);
            fail("Exception expected");
        } catch (EJBTransactionRolledbackException ejbex) {
            assertNull(ejbex.getCause());
            assertNull(ejbex.getCausedByException());
        }
        assertEquals(Arrays.asList("begin()", "call()", "setRollbackOnly()",
                "rollback()"), stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_ApplicationException() throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new IOException();
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (IOException ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("begin()", "call()", "commit()"), stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_ApplicationExceptionWithRollback()
            throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new ExceptionWithRollback();
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (ExceptionWithRollback ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("begin()", "call()", "rollback()"),
                stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_ApplicationExceptionWithoutRollback()
            throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new ExceptionWithoutRollback();
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (ExceptionWithoutRollback ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("begin()", "call()", "commit()"), stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_ApplicationException_MarkedRollback()
            throws Exception {
        ctxStubIsApplicationException = true;
        Exception root = new IOException();
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableExceptionAndMarkRollback(root), ctxStub);
            fail("Exception expected");
        } catch (IOException ex) {
            assertSame(root, ex);
        }
        assertEquals(Arrays.asList("begin()", "call()", "setRollbackOnly()",
                "rollback()"), stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_Exception() throws Exception {
        final Exception root = new IOException();
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (EJBException ejbex) {
            assertSame(root, ejbex.getCause());
            assertSame(root, ejbex.getCausedByException());
        }
        assertEquals(Arrays.asList("begin()", "call()", "rollback()"),
                stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_Error() throws Exception {
        final Error root = new Error();
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableError(root), ctxStub);
            fail("Exception expected");
        } catch (Error err) {
            assertSame(root, err);
        }
        assertEquals(Arrays.asList("begin()", "call()", "rollback()"),
                stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_CommitFailed1() throws Exception {
        failCommit = true;
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableReturns(new Object()), ctxStub);
            fail("Exception expected");
        } catch (EJBTransactionRolledbackException ejbex) {
        }
        assertEquals(Arrays.asList("begin()", "call()", "commit()"), stubCalls);
    }

    @Test
    public void testHANDLER_NEWTX_CommitFailed2() throws Exception {
        failCommit = true;
        ctxStubIsApplicationException = true;
        Exception root = new IOException();
        try {
            TransactionInvocationHandlers.HANDLER_NEWTX.call(
                    callableException(root), ctxStub);
            fail("Exception expected");
        } catch (EJBTransactionRolledbackException ex) {
        }
        assertEquals(Arrays.asList("begin()", "call()", "commit()"), stubCalls);
    }

    // =========================================================================
    // Transaction Modes
    // =========================================================================

    @Test
    public void testNOT_SUPPORTED_NoTx() throws Exception {
        TransactionInvocationHandlers.TX_NOT_SUPPORTED.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("suspend()", "call()"), stubCalls);
    }

    @Test
    public void testNOT_SUPPORTED_WithinTx() throws Exception {
        tmStub.begin();
        stubCalls.clear();
        TransactionInvocationHandlers.TX_NOT_SUPPORTED.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("suspend()", "call()", "resume()"),
                stubCalls);
    }

    @Test
    public void testNOT_SUPPORTED_WithinTx_Exception() throws Exception {
        tmStub.begin();
        stubCalls.clear();
        try {
            TransactionInvocationHandlers.TX_NOT_SUPPORTED.call(
                    callableException(new IOException()), ctxStub);
            fail("Exception expected");
        } catch (EJBException e) {
            //expected
        }
        assertEquals(Arrays.asList("suspend()", "call()", "resume()"),
                stubCalls);
    }

    @Test
    public void testREQUIRED_NoTx() throws Exception {
        TransactionInvocationHandlers.TX_REQUIRED.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("begin()", "call()", "commit()"), stubCalls);
    }

    @Test
    public void testREQUIRED_WithinTx() throws Exception {
        tmStub.begin();
        stubCalls.clear();
        TransactionInvocationHandlers.TX_REQUIRED.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testSUPPORTS_NoTx() throws Exception {
        TransactionInvocationHandlers.TX_SUPPORTS.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testSUPPORTS_WithinTx() throws Exception {
        tmStub.begin();
        stubCalls.clear();
        TransactionInvocationHandlers.TX_SUPPORTS.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testREQUIRES_NEW_NoTx() throws Exception {
        TransactionInvocationHandlers.TX_REQUIRES_NEW.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(
                Arrays.asList("suspend()", "begin()", "call()", "commit()"),
                stubCalls);
    }

    @Test
    public void testREQUIRES_NEW_WithinTx() throws Exception {
        tmStub.begin();
        stubCalls.clear();
        TransactionInvocationHandlers.TX_REQUIRES_NEW.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("suspend()", "begin()", "call()",
                "commit()", "resume()"), stubCalls);
    }

    @Test(expected = EJBTransactionRequiredException.class)
    public void testMANDATORY_NoTx() throws Exception {
        TransactionInvocationHandlers.TX_MANDATORY.call(
                callableReturns(new Object()), ctxStub);
    }

    @Test
    public void testMANDATORY_WithinTx() throws Exception {
        tmStub.begin();
        stubCalls.clear();
        TransactionInvocationHandlers.TX_MANDATORY.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test
    public void testNEVER_NoTx() throws Exception {
        TransactionInvocationHandlers.TX_NEVER.call(
                callableReturns(new Object()), ctxStub);
        assertEquals(Arrays.asList("call()"), stubCalls);
    }

    @Test(expected = EJBException.class)
    public void testNEVER_WithinTx() throws Exception {
        tmStub.begin();
        TransactionInvocationHandlers.TX_NEVER.call(
                callableReturns(new Object()), ctxStub);
    }

    @Test
    public void testGetHandlerForType() {
        assertSame(TransactionInvocationHandlers.TX_NOT_SUPPORTED,
                TransactionInvocationHandlers
                        .getHandlerFor(TransactionAttributeType.NOT_SUPPORTED));
        assertSame(TransactionInvocationHandlers.TX_REQUIRED,
                TransactionInvocationHandlers
                        .getHandlerFor(TransactionAttributeType.REQUIRED));
        assertSame(TransactionInvocationHandlers.TX_SUPPORTS,
                TransactionInvocationHandlers
                        .getHandlerFor(TransactionAttributeType.SUPPORTS));
        assertSame(TransactionInvocationHandlers.TX_REQUIRES_NEW,
                TransactionInvocationHandlers
                        .getHandlerFor(TransactionAttributeType.REQUIRES_NEW));
        assertSame(TransactionInvocationHandlers.TX_MANDATORY,
                TransactionInvocationHandlers
                        .getHandlerFor(TransactionAttributeType.MANDATORY));
        assertSame(TransactionInvocationHandlers.TX_NEVER,
                TransactionInvocationHandlers
                        .getHandlerFor(TransactionAttributeType.NEVER));
    }

    @Test
    public void testGetHandlerForBeanClass1() {
        @TransactionAttribute(TransactionAttributeType.NEVER)
        class Bean {
        }
        assertSame(TransactionInvocationHandlers.TX_NEVER,
                TransactionInvocationHandlers.getHandlerFor(Bean.class));
    }

    @Test
    public void testGetHandlerForBeanClass2() {
        class Bean {
        }
        assertSame(TransactionInvocationHandlers.TX_REQUIRED,
                TransactionInvocationHandlers.getHandlerFor(Bean.class));
    }

    @Test
    public void testGetHandlerForBeanClassAndMethod1() throws Exception {
        @TransactionAttribute(TransactionAttributeType.SUPPORTS)
        class Bean implements Runnable {
            @Override
            @TransactionAttribute(TransactionAttributeType.MANDATORY)
            public void run() {
            }
        }
        assertSame(TransactionInvocationHandlers.TX_MANDATORY,
                TransactionInvocationHandlers.getHandlerFor(Bean.class,
                        Bean.class.getMethod("run")));
    }

    @Test
    public void testGetHandlerForBeanClassAndMethod2() throws Exception {
        @TransactionAttribute(TransactionAttributeType.SUPPORTS)
        class Bean implements Runnable {
            @Override
            public void run() {
            }
        }
        assertSame(TransactionInvocationHandlers.TX_SUPPORTS,
                TransactionInvocationHandlers.getHandlerFor(Bean.class,
                        Bean.class.getMethod("run")));
    }

    @Test
    public void testGetHandlerForBeanClassAndMethod3() throws Exception {
        class Bean implements Runnable {
            @Override
            public void run() {
            }
        }
        assertSame(TransactionInvocationHandlers.TX_REQUIRED,
                TransactionInvocationHandlers.getHandlerFor(Bean.class,
                        Bean.class.getMethod("run")));
    }

}
