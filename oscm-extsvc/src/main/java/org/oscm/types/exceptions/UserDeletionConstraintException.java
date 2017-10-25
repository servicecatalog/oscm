/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the deletion of a user fails due to constraint
 * violations.
 * 
 */
@WebFault(name = "UserDeletionConstraintException", targetNamespace = "http://oscm.org/xsd")
public class UserDeletionConstraintException extends SaaSApplicationException {

    private static final long serialVersionUID = -6822194102878539365L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public UserDeletionConstraintException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public UserDeletionConstraintException(String message) {
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
    public UserDeletionConstraintException(String message,
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
    public UserDeletionConstraintException(String message,
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
    public UserDeletionConstraintException(String message, Reason reason) {
        super(message);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a
     * {@link UserDeletionConstraintException}.
     */
    public enum Reason {
        /**
         * The deletion failed because the user is the last administrator of the
         * organization.
         */
        LAST_ADMIN,
        /**
         * The deletion failed because the user tried to delete his own account.
         */
        FORBIDDEN_SELF_DELETION,
        /**
         * The deletion failed because the user still has at least one active
         * subscription.
         */
        HAS_ACTIVE_SUBSCRIPTIONS;
    }

}
