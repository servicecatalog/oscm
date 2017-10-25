/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ejb.EJBException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.BillingAdapterConnectionException;
import org.oscm.internal.types.exception.BillingApplicationException;

/**
 * @author baumann
 *
 */
public abstract class BasicBillingProxy {

    public static final long ADAPTER_TIMEOUT_IN_SECONDS = 30L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(BasicBillingProxy.class);

    private BillingAdapter billingAdapter;

    public BasicBillingProxy(BillingAdapter billingAdapter) {
        this.billingAdapter = billingAdapter;
    }

    <T> T locateBillingAdapterService(Class<T> serviceInterface)
            throws BillingApplicationException {
        try {
            return PluginServiceFactory.getPluginService(serviceInterface,
                    billingAdapter);
        } catch (BillingApplicationException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_BILLING_ADAPTER_NOT_FOUND);
            throw e;
        }
    }

    <T> Future<T> submitAdapterCall(Callable<T> callable)
            throws BillingApplicationException {
        ExecutorService executor = getSingleThreadExecutor();

        Future<T> future = null;
        try {
            future = executor.submit(callable);
        } catch (RejectedExecutionException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_EXECUTION_OF_BILLING_APPLICATION_TASK_REJECTED);
            throw new BillingApplicationException(
                    "Call to Billing Adapter failed",
                    new BillingAdapterConnectionException(
                            "The execution of the billing application task was rejected"));
        }

        return future;
    }

    ExecutorService getSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    <T> T getAdapterResult(Future<T> future)
            throws BillingApplicationException {
        try {
            // TODO timeout as config setting
            return (future.get(ADAPTER_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_CALL_TO_BILLING_APPLICATION_TIMEOUT);
            throw new BillingApplicationException(
                    "Call to Billing Adapter failed",
                    new BillingAdapterConnectionException(
                            "Timeout occurred when calling billing application"));
        } catch (InterruptedException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_CALL_TO_BILLING_APPLICATION_INTERRUPTED);
            throw new BillingApplicationException(
                    "Call to Billing Adapter failed",
                    new BillingAdapterConnectionException(
                            "Thread calling billing application was interrupted"));
        } catch (CancellationException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_CALL_TO_BILLING_APPLICATION_CANCELLED);
            throw new BillingApplicationException(
                    "Call to Billing Adapter failed",
                    new BillingAdapterConnectionException(
                            "Thread calling billing application was cancelled"));
        } catch (ExecutionException e) {
            Throwable cause = e;
            if (e.getCause() != null) {
                cause = e.getCause();
            }

            if (cause instanceof EJBException) {
                if (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                // EJBException's are already logged by the container!
                throw new BillingApplicationException(cause.getMessage());
            } else if (cause instanceof BillingApplicationException) {
                logger.logError(Log4jLogger.SYSTEM_LOG, cause,
                        LogMessageIdentifier.ERROR_CONNECTION_TO_BILLING_ADAPTER_FAILED);
                throw (BillingApplicationException) cause;
            } else {
                // Exception from Adapter
                logger.logError(Log4jLogger.SYSTEM_LOG, cause,
                        LogMessageIdentifier.ERROR_CALL_TO_BILLING_APPLICATION_FAILED);
                throw new BillingApplicationException(cause.getMessage());
            }
        }
    }

}
