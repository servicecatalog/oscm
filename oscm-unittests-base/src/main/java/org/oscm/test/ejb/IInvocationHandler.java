/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;

/**
 * Abstraction for bean method invocation.
 */
interface IInvocationHandler {

    /**
     * Context of a method invocation.
     */
    interface IInvocationCtx {

        /**
         * @return transaction manager responsible for this invocation
         */
        TransactionManager getTransactionManager();

        /**
         * Determines whether the given Exception is an application exception in
         * this context.
         * 
         * @param e
         * @return <code>true</code>, if the exception is an application
         *         exception
         */
        boolean isApplicationException(Exception e);

    }

    /**
     * Implementations should call the given {@link Callable} and return its
     * return value. Additional steps might be performed before or after the
     * invocation and in case of exceptions.
     * 
     * @param callable
     * @param ctx
     * @return result of the {@link Callable}
     */
    public Object call(Callable<Object> callable,
            IInvocationHandler.IInvocationCtx ctx) throws Exception;

}
