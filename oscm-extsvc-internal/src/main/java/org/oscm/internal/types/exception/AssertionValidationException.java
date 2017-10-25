/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.AssertionValidationExceptionBean;

/**
 * @author kulle
 * 
 */
public class AssertionValidationException extends SaaSApplicationException {

    private static final long serialVersionUID = -7462629123637765889L;

    AssertionValidationExceptionBean bean = new AssertionValidationExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public AssertionValidationException() {

    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            the detail message
     */
    public AssertionValidationException(String message) {
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
    public AssertionValidationException(String message, ReasonEnum reason,
            Object[] params) {
        super(message, params);
        bean.setReason(reason);
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
     * @param params
     *            the message parameters
     */
    public AssertionValidationException(String message, ReasonEnum reason,
            Throwable cause) {
        super(message, cause);
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
     * Enumeration of possible reasons for a
     * {@link AssertionValidationException}.
     * 
     */
    public enum ReasonEnum {

        INVALID_RECIPIENT, ASSERTION_EXPIRED, EXCEPTION_OCCURRED, WRONG_REQUEST, WRONG_TENANT, MISSING_TENANT
    }

}
