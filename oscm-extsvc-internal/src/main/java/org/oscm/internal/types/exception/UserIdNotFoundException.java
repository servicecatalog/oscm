/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 14, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.xml.bind.annotation.XmlType;

import org.oscm.internal.types.exception.beans.UserIdNotFoundExceptionBean;

/**
 * @author farmaki
 * 
 */
public class UserIdNotFoundException extends SaaSApplicationException {

    private static final long serialVersionUID = -1093186892330080980L;

    private final UserIdNotFoundExceptionBean bean = new UserIdNotFoundExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public UserIdNotFoundException() {

    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            the detail message
     */
    public UserIdNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given detail message and message
     * parameters, and appends the specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     * @param params
     *            the message parameters
     */
    public UserIdNotFoundException(String message, ReasonEnum reason,
            Object[] params) {
        super(message, params);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Constructs a new exception with the given detail message and cause, and
     * appends the specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     * @param cause
     *            the cause
     */
    public UserIdNotFoundException(String message, ReasonEnum reason,
            Throwable cause) {
        super(message, cause);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Constructs a new exception with the given detail message, message
     * parameters and cause, and appends the specified reason to the message
     * key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     * @param cause
     *            the cause
     * @param params
     *            the message parameters
     */
    public UserIdNotFoundException(String message, ReasonEnum reason,
            Throwable cause, Object[] params) {
        super(message, cause, params);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Returns the reason for this exception.
     * 
     * @return the reason
     */
    public ReasonEnum getReason() {
        return bean.getReason();
    }

    /**
     * Enumeration of possible reasons for a {@link UserIdNotFoundException}.
     * 
     */
    @XmlType(name = "UserIdNotFoundException.ReasonEnum")
    public static enum ReasonEnum {
        EXCEPTION_OCCURRED, USERID_ATTRIBUTE_NOT_FOUND;
    }

}
