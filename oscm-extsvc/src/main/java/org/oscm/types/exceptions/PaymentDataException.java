/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-04-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the provided payment information is not valid.
 * 
 */
@WebFault(name = "PaymentDataException", targetNamespace = "http://oscm.org/xsd")
public class PaymentDataException extends SaaSApplicationException {

    private static final long serialVersionUID = 1235415082913123966L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public PaymentDataException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public PaymentDataException(String message) {
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
    public PaymentDataException(String message, ApplicationExceptionBean bean) {
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
    public PaymentDataException(String message, ApplicationExceptionBean bean,
            Throwable cause) {
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
    public PaymentDataException(String message, Reason reason) {
        super(message);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons a {@link PaymentDataException}.
     * 
     */
    public enum Reason {

        /**
         * The payment service provider does not support the specified payment
         * type.
         */
        PAYMENT_TYPE_UNSUPPORTED_BY_PSP,

        /**
         * The customer's supplier or reseller does not support the specified
         * payment type.
         */
        PAYMENT_TYPE_UNSUPPORTED_FOR_ORGANIZATION,

        /**
         * The payment type is not being handled by an organization.
         */
        PAYMENT_TYPE_NO_ORGANIZATION_HANDLING,

        /**
         * The payment type is unknown. This means that an object with a
         * matching payment type ID does not exist in the database.
         */
        UNKNOWN_PAYMENT_TYPE,

        /**
         * No payment information is required because the service is free of
         * charge.
         */
        NO_PAYMENT_REQUIRED,

        /**
         * Invalid payment information. The payment type and the payment type ID
         * must be specified.
         */
        INVALID_PAYMENT_DATA;
    }

}
