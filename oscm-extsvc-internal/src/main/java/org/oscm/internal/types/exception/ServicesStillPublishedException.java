/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-09-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a problem occurs because a service is still active on a
 * marketplace.
 * 
 */
public class ServicesStillPublishedException extends SaaSApplicationException {

    private static final long serialVersionUID = -5412526142258919992L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ServicesStillPublishedException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ServicesStillPublishedException(String message) {
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
    public ServicesStillPublishedException(String message,
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
    public ServicesStillPublishedException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with the given detail message, and appends the
     * specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     */
    public ServicesStillPublishedException(String message, Reason reason) {
        super(message);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Constructs a new exception with the given detail message and message
     * parameters, and appends the specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     * @param organizationIds
     *            the IDs of supplier, broker, or reseller organizations which
     *            have active services on a marketplace
     */
    public ServicesStillPublishedException(String message, Reason reason,
            Object[] organizationIds) {
        super(message, organizationIds);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a
     * {@link ServicesStillPublishedException}.
     * 
     */
    public enum Reason {

        /**
         * There are still active services published on the marketplace.
         */
        ACTIVE_SERVICES_ON_MARKETPLACE,

        /**
         * A specific supplier, broker, or reseller organization still has
         * active services published on the marketplace.
         */
        ACTIVE_SERVICES_OF_SELLER;
    }

}
