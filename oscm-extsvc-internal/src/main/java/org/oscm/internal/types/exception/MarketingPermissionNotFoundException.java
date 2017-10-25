/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-12-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the permission to offer marketable services based on a
 * specific technical service is not found.
 * 
 */
public class MarketingPermissionNotFoundException extends
        SaaSApplicationException {

    private static final long serialVersionUID = -2387696784628103992L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public MarketingPermissionNotFoundException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public MarketingPermissionNotFoundException(String message) {
        super(message);
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
    public MarketingPermissionNotFoundException(String message, Object[] params) {
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
    public MarketingPermissionNotFoundException(String message,
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
    public MarketingPermissionNotFoundException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
