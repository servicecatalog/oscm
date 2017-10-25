/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-09-09
 *
 *******************************************************************************/
package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

public class TenantDeletionConstraintException extends SaaSApplicationException {

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public TenantDeletionConstraintException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     *
     * @param message
     *            the detail message
     */
    public TenantDeletionConstraintException(String message) {
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
    public TenantDeletionConstraintException(String message,
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
    public TenantDeletionConstraintException(String message,
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
    public TenantDeletionConstraintException(String message, TenantDeletionConstraintException.Reason reason) {
        super(message);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a
     * {@link TenantDeletionConstraintException}.
     */
    public enum Reason {
        /**
         * The deletion failed because the tenant is associated with the
         * organization.
         */
        RELATED_ORGANIZATION_EXISTS,
        /**
         * The deletion failed because the tenant is associated with the
         * marketplace.
         */
        RELATED_MARKETPLACE_EXISTS;
    }
}
