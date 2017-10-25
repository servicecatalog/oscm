/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-07-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a service cannot be activated because it has not been
 * published on a marketplace.
 * 
 */
public class ServiceNotPublishedException extends SaaSApplicationException {

    private static final long serialVersionUID = 7337065584628342080L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ServiceNotPublishedException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ServiceNotPublishedException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message parameters.
     * 
     * @param params
     *            the message parameters
     */
    public ServiceNotPublishedException(Object[] params) {
        super(params);
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
    public ServiceNotPublishedException(String message,
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
    public ServiceNotPublishedException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
