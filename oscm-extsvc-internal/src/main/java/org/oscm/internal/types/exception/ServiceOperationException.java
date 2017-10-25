/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-07-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a problem occurs in an operation on a marketable
 * service.
 * 
 */
public class ServiceOperationException extends SaaSApplicationException {

    private static final long serialVersionUID = -2426674004399603543L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ServiceOperationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ServiceOperationException(String message) {
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
    public ServiceOperationException(String message,
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
    public ServiceOperationException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with a pre-defined detail message, and appends
     * the specified reason to the message key.
     * 
     * @param reason
     *            the reason
     */
    public ServiceOperationException(Reason reason) {
        super("Service operation failed");
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a {@link ServiceOperationException}.
     * 
     */
    public enum Reason {
        /**
         * The service could not be deleted as it is still in use by a
         * subscription.
         */
        DELETION_FAILED_USED_BY_SUB,

        /**
         * A price model is required but missing.
         */
        MISSING_PRICE_MODEL,
        /**
         * A customer-specific copy of the service already exists.
         */
        CUSTOMER_COPY_ALREADY_EXISTS,
        /**
         * The service status cannot be changed because the service is a
         * subscription-specific copy.
         */
        STATE_CHANGE_FAILED_USED_BY_SUB,

        /**
         * None of the services to be activated is defined as visible.
         */
        NO_VISIBLE_ACTIVE_SERVICE(),

        /**
         * The operation can be executed only for a marketable service as
         * defined by its supplier, or for a copy of such a service for a
         * specific resale permission.
         */
        OPERATION_ALLOWED_ONLY_FOR_TEMPLATE_SERVICE_OR_COPY_FOR_RESALE,

        /**
         * The price model and the marketplace are not specified for the
         * original service defined by the supplier.
         */
        MISSING_PRICE_MODEL_AND_MARKETPLACE_FOR_TEMPLATE,

        /**
         * The price model is not specified for the original service defined by
         * the supplier.
         */
        MISSING_PRICE_MODEL_FOR_TEMPLATE,

        /**
         * The specified service is not a marketable service as defined by its
         * supplier.
         */
        SERVICE_IS_NOT_A_TEMPLATE,

        /**
         * The specified service is not assigned to a marketplace.
         */
        SERVICE_NOT_ASSIGNED_TO_MARKETPLACE,

        /**
         * The specified service cannot be deleted because resale permissions
         * exist.
         */
        DELETION_FAILED_EXISTING_RESALE_PERMISSION;
    }

}
