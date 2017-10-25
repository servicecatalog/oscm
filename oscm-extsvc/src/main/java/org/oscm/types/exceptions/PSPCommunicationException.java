/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-11-09                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a problem occurs in the communication with a payment
 * service provider (PSP).
 * 
 */
@WebFault(name = "PSPCommunicationException", targetNamespace = "http://oscm.org/xsd")
public class PSPCommunicationException extends SaaSApplicationException {

    private static final long serialVersionUID = -6346682565309938047L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public PSPCommunicationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public PSPCommunicationException(String message) {
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
    public PSPCommunicationException(String message,
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
    public PSPCommunicationException(String message,
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
    public PSPCommunicationException(String message, Reason reason) {
        super(message);
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
    public PSPCommunicationException(String message, Reason reason,
            Throwable cause) {
        super(message, cause);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a {@link PSPCommunicationException}.
     * 
     */
    public enum Reason {

        /**
         * Calling the PSP interface implementation failed due to communication
         * problems (<code>IOException</code>).
         * <p>
         * The related message must provide a parameter for the response code.
         */
        WEB_SERVICE_CALL_FAILED,

        /**
         * The response from the PSP does not contain a valid response URL.
         * <p>
         * The related message must provide a parameter for the response code.
         */
        MISSING_RESPONSE_URL,

        /**
         * The debit operation did not succeed due to a communication problem.
         * 
         * <p>
         * <b>NOTE:</b> As this exception is not displayed to customers, it is
         * not included in the message files of the bundle.
         * </p>
         */
        DEBIT_INVOCATION_FAILED;

    }

}
