/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-05-31                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when trying to change the
 * <code>allowingOnBehalfActing</code> setting for a technical service which
 * already has subscriptions.
 * 
 */
public class UnchangeableAllowingOnBehalfActingException extends
        SaaSApplicationException {

    private static final long serialVersionUID = 3987205984129126724L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public UnchangeableAllowingOnBehalfActingException() {

    }

    /**
     * Constructs a new exception with the specified message parameters.
     * 
     * @param params
     *            the message parameters
     */
    public UnchangeableAllowingOnBehalfActingException(Object[] params) {
        super(params);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public UnchangeableAllowingOnBehalfActingException(String message) {
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
    public UnchangeableAllowingOnBehalfActingException(String message,
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
    public UnchangeableAllowingOnBehalfActingException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
