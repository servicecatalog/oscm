/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 22.02.2012                                                      
 *                                                                              
 *  Completion Time: 22.02.2012                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.interceptor;

import java.sql.SQLException;

import javax.ejb.EJBException;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityExistsException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.exceptions.ConnectException;
import org.oscm.internal.types.exception.ConcurrentModificationException;

/**
 * EJB interceptor that maps EJB exceptions to BES. Some exceptions are thrown
 * when the transaction is committed. Typically, this is after our own code is
 * executed. So, we cannot react on with a try catch block. This interceptor
 * handles EJB exception before they are returned to the client.
 * 
 * @author cheld
 * 
 */
public class ExceptionMapper {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ExceptionMapper.class);
    private Throwable connectException = null;

    @AroundInvoke
    public Object mapEJBExceptions(InvocationContext context) throws Exception {

        try {
            return context.proceed();
        } catch (Exception e) {
            throw mapToBesException(e);
        }
    }

    Exception mapToBesException(Exception e) {
        PersistenceException pe = findPersistenceException(e);
        if (canMapToConcurrentModificationException(pe)) {
            return new ConcurrentModificationException(
                    "Database constraint violation occurred");
        }
        if (isCausedByConnectionException(e)) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_CANNOT_CONNECT_TO_DATABASE);
            return new ConnectException("Cannot connect to database",
                    connectException);
        }
        return e;
    }

    PersistenceException findPersistenceException(Exception e) {
        Throwable cause = e;
        while (cause != null && !(cause instanceof PersistenceException)) {
            cause = findCause(cause);
        }
        return (PersistenceException) cause;
    }

    Throwable findCause(Throwable e) {
        Throwable cause = null;
        if (e instanceof EJBException && e.getCause() instanceof Exception) {
            cause = ((EJBException) e).getCausedByException();
        }
        if (cause == null) {
            cause = e.getCause();
        }
        return cause;
    }

    private boolean canMapToConcurrentModificationException(
            PersistenceException pe) {
        if (pe instanceof EntityExistsException
                || pe instanceof OptimisticLockException) {
            return true;
        }
        return false;
    }

    boolean isCausedByConnectionException(Throwable th) {
        if (th != null) {
            connectException = th.getCause();
            boolean isCausedByDBConnection = false;
            while (connectException != null) {
                if (connectException instanceof SQLException
                        || connectException instanceof ConnectException) {
                    isCausedByDBConnection = true;
                }
                if ((connectException instanceof java.net.ConnectException)) {
                    return isCausedByDBConnection;
                }
                connectException = findCause(connectException);
            }
        }
        return false;
    }

}
