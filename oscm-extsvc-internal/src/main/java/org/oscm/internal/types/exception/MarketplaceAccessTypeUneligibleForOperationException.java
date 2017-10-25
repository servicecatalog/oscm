/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-02-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the operation invoked is not suitable for the given
 * marketplace.
 * <p>
 * Some operations only make sense for an open marketplace, others only for
 * marketplaces which are not open. This exception is thrown when an operation
 * eligible for an open marketplace is invoked for a marketplace which is not
 * open, and vice versa.
 */
public class MarketplaceAccessTypeUneligibleForOperationException extends
        SaaSApplicationException {

    private static final long serialVersionUID = -6032688683751724929L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public MarketplaceAccessTypeUneligibleForOperationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public MarketplaceAccessTypeUneligibleForOperationException(String message) {
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
    public MarketplaceAccessTypeUneligibleForOperationException(String message,
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
    public MarketplaceAccessTypeUneligibleForOperationException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and message
     * parameter.
     * 
     * @param message
     *            the detail message
     * @param marketplaceId
     *            the ID of the marketplace for which the exception is thrown
     *            and which is to be set as a message parameter
     */
    public MarketplaceAccessTypeUneligibleForOperationException(String message,
            String marketplaceId) {
        super(message, new Object[] { marketplaceId });
    }

}
