/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.UserModificationConstraintExceptionBean;

/**
 * Exception thrown when the modification of a user fails due to constraint
 * violations.
 * 
 */
@WebFault(name = "UserModificationConstraintException", targetNamespace = "http://oscm.org/xsd")
public class UserModificationConstraintException extends
        SaaSApplicationException {

    private static final long serialVersionUID = 4341405264677452397L;
    private UserModificationConstraintExceptionBean bean = new UserModificationConstraintExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public UserModificationConstraintException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public UserModificationConstraintException(String message) {
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
    public UserModificationConstraintException(String message,
            UserModificationConstraintExceptionBean bean) {
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
    public UserModificationConstraintException(String message,
            UserModificationConstraintExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    /**
     * Constructs a new exception and appends the specified reason to the
     * message key.
     * 
     * @param reason
     *            the reason
     */
    public UserModificationConstraintException(Reason reason) {
        super();
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

    /**
     * Sets the reason for this exception.
     * 
     * @param reason
     *            the reason
     */
    public void setReason(Reason reason) {
        bean.setReason(reason);
    }

    /* javadoc copied from super class */
    @Override
    public UserModificationConstraintExceptionBean getFaultInfo() {
        return new UserModificationConstraintExceptionBean(
                super.getFaultInfo(), bean.getReason());
    }

    /**
     * Enumeration of possible reasons for a
     * {@link UserModificationConstraintException}.
     */
    public enum Reason {
        /**
         * The modification failed because the user is the last administrator of
         * the organization and the status must not change.
         */
        LAST_ADMIN;
    }

}
