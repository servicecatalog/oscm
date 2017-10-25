/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: Jun 14, 2013                                                      
 *
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.xml.bind.annotation.XmlType;

import org.oscm.internal.types.exception.beans.SessionIndexNotFoundExceptionBean;

/**
 * @author dchojnacki
 *
 */
public class SessionIndexNotFoundException extends SaaSApplicationException {

    private static final long serialVersionUID = -1093186892330080980L;

    private final SessionIndexNotFoundExceptionBean bean = new SessionIndexNotFoundExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SessionIndexNotFoundException() {

    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public SessionIndexNotFoundException(String message) {
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
    public SessionIndexNotFoundException(String message, ReasonEnum reason,
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
    public SessionIndexNotFoundException(String message, ReasonEnum reason,
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
    public SessionIndexNotFoundException(String message, ReasonEnum reason,
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
     * Enumeration of possible reasons for a {@link SessionIndexNotFoundException}.
     *
     */
    @XmlType(name = "SessionNotFoundException.ReasonEnum")
    public static enum ReasonEnum {
        EXCEPTION_OCCURRED, SESSION_ATTRIBUTE_NOT_FOUND;
    }

}