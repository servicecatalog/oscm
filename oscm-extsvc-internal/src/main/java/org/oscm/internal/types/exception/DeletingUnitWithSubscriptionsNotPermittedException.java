/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 08.07.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.ejb.ApplicationException;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown to indicate problems due to deleting a unit with a
 * subscription assigned.
 * 
 */
@ApplicationException(rollback = true)
public class DeletingUnitWithSubscriptionsNotPermittedException extends
        SaaSApplicationException {

    private static final long serialVersionUID = 1676882639833885057L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public DeletingUnitWithSubscriptionsNotPermittedException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public DeletingUnitWithSubscriptionsNotPermittedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     */
    public DeletingUnitWithSubscriptionsNotPermittedException(String message,
            Object[] params) {
        super(message, params);
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
    public DeletingUnitWithSubscriptionsNotPermittedException(String message,
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
    public DeletingUnitWithSubscriptionsNotPermittedException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
