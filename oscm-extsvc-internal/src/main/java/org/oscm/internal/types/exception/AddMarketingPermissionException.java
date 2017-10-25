/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-12-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a supplier organization cannot be granted the
 * permission to offer marketable services for a technical service.
 */
public class AddMarketingPermissionException extends SaaSApplicationException {

    private static final long serialVersionUID = -8852853384717050456L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public AddMarketingPermissionException() {
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public AddMarketingPermissionException(String message) {
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
    public AddMarketingPermissionException(String message,
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
    public AddMarketingPermissionException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
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
    public AddMarketingPermissionException(String message, Object[] params) {
        super(message, params);
    }

}
