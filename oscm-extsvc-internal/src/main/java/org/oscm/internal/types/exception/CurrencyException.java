/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when an unsupported currency is passed to the platform.
 */
public class CurrencyException extends SaaSApplicationException {

    private static final long serialVersionUID = -8175173207154691192L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public CurrencyException() {

    }

    /**
     * Constructs a new exception with the specified detail message and message
     * parameters. The cause is not initialized.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     */
    public CurrencyException(String message, Object[] params) {
        super(message, params);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public CurrencyException(String message) {
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
    public CurrencyException(String message, ApplicationExceptionBean bean) {
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
    public CurrencyException(String message, ApplicationExceptionBean bean,
            Throwable cause) {
        super(message, bean, cause);
    }

}
