/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-03-09                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.RegistrationExceptionBean;

/**
 * Exception thrown when a problem occurs in the registration of an
 * organization.
 * 
 */
public class RegistrationException extends SaaSApplicationException {

    private static final long serialVersionUID = 6819106515067430437L;
    private RegistrationExceptionBean bean = new RegistrationExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public RegistrationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public RegistrationException(String message) {
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
    public RegistrationException(String message, RegistrationExceptionBean bean) {
        super(message, bean);
        this.bean = bean;
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
    public RegistrationException(String message,
            RegistrationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
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
    public RegistrationException(String message, Reason reason) {
        super(message);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Returns the reason for this exception.
     * 
     * @return the reason
     */
    public Reason getReason() {
        return bean.getReason();
    }

    /* javadoc copied from super class */
    @Override
    public RegistrationExceptionBean getFaultInfo() {
        return new RegistrationExceptionBean(super.getFaultInfo(),
                bean.getReason());
    }

    /**
     * Enumeration of possible reasons for a {@link RegistrationException}.
     * 
     */
    public enum Reason {

        /**
         * The organization specified as the seller for the organization to be
         * registered does not have the supplier, broker, or reseller role.
         */
        TARGET_ORG_INVALID,

        /**
         * A user tries to register but is not allowed to do so.
         */
        SELFREGISTRATION_NOT_ALLOWED

    }

}
