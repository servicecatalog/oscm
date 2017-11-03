/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRequiredException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.Status;
import javax.transaction.Transaction;

/**
 * Wrapper for session bean instances to emulate EJB container behavior
 * regarding transaction handling and exceptions. See EJB 3.0 Core Spec, 16.6.2
 * and 14.3.1.
 * 
 * @author hoffmann
 */
final class TransactionInvocationHandlers {

    /**
     * Internal handler in case we don't have to consider any transaction.
     */
    static final IInvocationHandler HANDLER_NOTX = new IInvocationHandler() {

        public Object call(Callable<Object> callable,
                IInvocationHandler.IInvocationCtx ctx) throws Exception {
            try {
                return callable.call();
            } catch (Exception ex) {
                if (ctx.isApplicationException(ex)) {
                    throw ex;
                } else {
                    throw setCause(new EJBException(ex));
                }
            }
        }
    };

    /**
     * Internal handler in case we work within a existing transaction.
     */
    static final IInvocationHandler HANDLER_WITHINTX = new IInvocationHandler() {

        public Object call(Callable<Object> callable,
                IInvocationHandler.IInvocationCtx ctx) throws Exception {
            try {
                return callable.call();
            } catch (Exception ex) {
                if (ctx.isApplicationException(ex)) {
                    if (hasRollbackAnnotation(ex)) {
                        ctx.getTransactionManager().setRollbackOnly();
                    }
                    throw ex;
                } else {
                    ctx.getTransactionManager().setRollbackOnly();
                    throw setCause(new EJBTransactionRolledbackException(
                            "Rollback due to exception.", ex));
                }
            }
        }
    };

    /**
     * Internal handler in case we need to create a new transaction.
     */
    static final IInvocationHandler HANDLER_NEWTX = new IInvocationHandler() {

        public Object call(Callable<Object> callable,
                IInvocationHandler.IInvocationCtx ctx) throws Exception {
            final Object result;
            ctx.getTransactionManager().begin();
            try {
                result = callable.call();
            } catch (Exception ex) {
                if (ctx.isApplicationException(ex)) {
                    if (hasRollbackAnnotation(ex)
                            || ctx.getTransactionManager().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                        ctx.getTransactionManager().rollback();
                    } else {
                        commit(ctx);
                    }
                    throw ex;
                }
                ctx.getTransactionManager().rollback();
                throw setCause(new EJBException(ex));
            } catch (Error err) {
                // Required for AssertErrors thrown by JUnit4 assertions:
                ctx.getTransactionManager().rollback();
                throw err;
            }
            if (ctx.getTransactionManager().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ctx.getTransactionManager().rollback();
                throw new EJBTransactionRolledbackException();
            }
            commit(ctx);
            return result;
        }

        private void commit(IInvocationHandler.IInvocationCtx ctx) {
            try {
                ctx.getTransactionManager().commit();
            } catch (Exception ex) {
                throw setCause(new EJBTransactionRolledbackException(
                        "Commit failed.", ex));
            }
        }
    };

    private static EJBException setCause(EJBException ex) {
        return ex;
    }

    private static boolean hasRollbackAnnotation(Exception ex) {
        ApplicationException annotation = ex.getClass().getAnnotation(
                ApplicationException.class);
        return annotation != null && annotation.rollback();
    }

    /**
     * Transaction mode NOT_SUPPORTED.
     */
    public static final IInvocationHandler TX_NOT_SUPPORTED = suspend(HANDLER_NOTX);

    /**
     * Transaction mode REQUIRED.
     */
    public static final IInvocationHandler TX_REQUIRED = new HandlerSwitch(
            HANDLER_WITHINTX, HANDLER_NEWTX);

    /**
     * Transaction mode REQUIRED.
     */
    public static final IInvocationHandler TX_SUPPORTS = new HandlerSwitch(
            HANDLER_WITHINTX, HANDLER_NOTX);

    /**
     * Transaction mode REQUIRES_NEW.
     */
    public static final IInvocationHandler TX_REQUIRES_NEW = suspend(HANDLER_NEWTX);

    /**
     * Transaction mode MANDATORY.
     */
    public static final IInvocationHandler TX_MANDATORY = new HandlerSwitch(
            HANDLER_WITHINTX, new IInvocationHandler() {
                public Object call(Callable<Object> callable, IInvocationCtx ctx)
                        throws Exception {
                    throw new EJBTransactionRequiredException(
                            "Transaction required (MANDATORY).");
                }
            });

    /**
     * Transaction mode NEWER.
     */
    public static final IInvocationHandler TX_NEVER = new HandlerSwitch(
            new IInvocationHandler() {
                public Object call(Callable<Object> callable, IInvocationCtx ctx)
                        throws Exception {
                    throw new EJBException("Transaction not allowed (NEVER).");
                }
            }, HANDLER_NOTX);

    /**
     * Handler that delegates to one of two other handler depending whether a
     * transaction is active or not.
     */
    private static class HandlerSwitch implements IInvocationHandler {

        private final IInvocationHandler withTx, withoutTx;

        HandlerSwitch(IInvocationHandler withTx, IInvocationHandler withoutTx) {
            this.withTx = withTx;
            this.withoutTx = withoutTx;
        }

        public Object call(Callable<Object> callable, IInvocationCtx ctx)
                throws Exception {
            if (ctx.getTransactionManager().getStatus() == Status.STATUS_ACTIVE) {
                return withTx.call(callable, ctx);
            } else {
                return withoutTx.call(callable, ctx);
            }
        }

    }

    /**
     * Suspends the current transaction (if any) during the given handler is
     * called.
     * 
     * @param delegate
     * @return
     */
    private static IInvocationHandler suspend(final IInvocationHandler delegate) {
        return new IInvocationHandler() {

            public Object call(Callable<Object> callable,
                    IInvocationHandler.IInvocationCtx ctx) throws Exception {
                final Transaction suspended = ctx.getTransactionManager()
                        .suspend();
                try {
                    return delegate.call(callable, ctx);
                } finally {
                    if (suspended != null) {
                        ctx.getTransactionManager().resume(suspended);
                    }
                }
            }
        };
    }

    private static final Map<TransactionAttributeType, IInvocationHandler> TYPE2HANDLERS = new HashMap<TransactionAttributeType, IInvocationHandler>();

    static {
        TYPE2HANDLERS.put(TransactionAttributeType.NOT_SUPPORTED,
                TX_NOT_SUPPORTED);
        TYPE2HANDLERS.put(TransactionAttributeType.REQUIRED, TX_REQUIRED);
        TYPE2HANDLERS.put(TransactionAttributeType.SUPPORTS, TX_SUPPORTS);
        TYPE2HANDLERS.put(TransactionAttributeType.REQUIRES_NEW,
                TX_REQUIRES_NEW);
        TYPE2HANDLERS.put(TransactionAttributeType.MANDATORY, TX_MANDATORY);
        TYPE2HANDLERS.put(TransactionAttributeType.NEVER, TX_NEVER);
    }

    /**
     * Returns the handler suitable for the given transaction type.
     * 
     * @param type
     * @return
     */
    public static IInvocationHandler getHandlerFor(TransactionAttributeType type) {
        return TYPE2HANDLERS.get(type);
    }

    /**
     * Returns the handler suitable for the given bean implementation class.
     * 
     * @param beanClass
     * @return
     */
    public static IInvocationHandler getHandlerFor(Class<?> beanClass) {
        TransactionAttribute attr = beanClass
                .getAnnotation(TransactionAttribute.class);
        if (attr != null) {
            return getHandlerFor(attr.value());
        }
        // Default is REQUIRED (see EJB 3.0 Core Spec, 13.3.7)
        return TX_REQUIRED;
    }

    /**
     * Returns the handler suitable for the given bean implementation class and
     * method.
     * 
     * @param beanClass
     * @return
     */
    public static IInvocationHandler getHandlerFor(Class<?> beanClass,
            Method beanMethod) {
        TransactionAttribute attr = beanMethod
                .getAnnotation(TransactionAttribute.class);
        if (attr != null) {
            return getHandlerFor(attr.value());
        }
        // Default is class scope:
        return getHandlerFor(beanClass);
    }

}
