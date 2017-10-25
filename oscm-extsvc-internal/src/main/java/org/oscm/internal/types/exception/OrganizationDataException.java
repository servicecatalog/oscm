/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-06-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when given organization details are not sufficient to
 * process payment information.
 * 
 */
public class OrganizationDataException extends SaaSApplicationException {

    private static final long serialVersionUID = -321525301036650696L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public OrganizationDataException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public OrganizationDataException(String message) {
        super(message);
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
    public OrganizationDataException(String message, Reason reason) {
        super(message);
        setMessageKey(getMessageKey() + "." + reason.toString());
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
    public OrganizationDataException(String message,
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
    public OrganizationDataException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Enumeration of possible reasons for an {@link OrganizationDataException}.
     * 
     */
    public enum Reason {

        /**
         * A payment information object cannot be created because the
         * information on the organization is insufficient.
         */
        CREATE_PAYMENT,

        /**
         * Organization data cannot be changed as specified because there is a
         * payment information object for this organization which requires the
         * email, name, and address to be set.
         */
        MODIFY_ORG_DATA;
    }

}
