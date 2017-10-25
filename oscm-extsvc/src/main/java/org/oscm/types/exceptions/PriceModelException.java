/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-09-30                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the modification of a price model fails.
 * 
 */
@WebFault(name = "PriceModelException", targetNamespace = "http://oscm.org/xsd")
public class PriceModelException extends SaaSApplicationException {

    private static final long serialVersionUID = -5634737007588314715L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public PriceModelException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public PriceModelException(final String message) {
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
    public PriceModelException(String message, ApplicationExceptionBean bean) {
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
    public PriceModelException(String message, ApplicationExceptionBean bean,
            Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with the specified reason set as its detail
     * message and appended to the message key.
     * 
     * @param reason
     *            the reason
     */
    public PriceModelException(Reason reason) {
        super(reason.toString());
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a {@link PriceModelException}.
     */
    public enum Reason {
        /**
         * No price model has been defined for the service.
         */
        NOT_DEFINED,

        /**
         * The "free of charge" setting of the price model cannot be changed.
         */
        UNMODIFIABLE_CHARGEABLE,
        /**
         * The currency of the price model cannot be changed.
         */
        UNMODIFIABLE_CURRENCY;
    }

}
