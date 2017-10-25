/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.ejb.ApplicationException;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown to indicate problems due to unauthorized calls or calls with
 * an invalid context.
 * 
 */
@ApplicationException(rollback = true)
public class OperationNotPermittedException extends SaaSApplicationException {

    private static final long serialVersionUID = 8004011147324989091L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public OperationNotPermittedException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public OperationNotPermittedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public OperationNotPermittedException(String message,
            ApplicationExceptionBean bean) {
        super(message, bean);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * bean for JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     * @param cause
     *            the cause
     */
    public OperationNotPermittedException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
