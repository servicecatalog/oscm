/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 23.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.xml.bind.annotation.XmlType;

import org.oscm.internal.types.exception.beans.DigitalSignatureValidationExceptionBean;

/**
 * @author kulle
 * 
 */
public class DigitalSignatureValidationException extends
        SaaSApplicationException {

    private static final long serialVersionUID = -9006836433751915578L;

    private final DigitalSignatureValidationExceptionBean bean = new DigitalSignatureValidationExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public DigitalSignatureValidationException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public DigitalSignatureValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public DigitalSignatureValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and reason.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     */
    public DigitalSignatureValidationException(String message, ReasonEnum reason) {
        super(message);
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
    public DigitalSignatureValidationException(String message,
            ReasonEnum reason, Throwable cause) {
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
     * {@link DigitalSignatureValidationException}.
     * 
     */
    @XmlType(name = "DigitalSignatureValidationException.ReasonEnum")
    public static enum ReasonEnum {

        /**
         * An exception occurred during the validation process.
         * 
         */
        EXCEPTION_OCCURRED,

        /**
         * The digital signature is not valid.
         */
        NOT_VALID,

        /**
         * IdP keystore could not be loaded.
         */
        EXCEPTION_KEYSTORE;
    }

}
