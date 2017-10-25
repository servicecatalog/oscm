/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-22                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the provisioning service of the application underlying
 * to a technical service does not respond correctly.
 * 
 */
@WebFault(name = "TechnicalServiceNotAliveException", targetNamespace = "http://oscm.org/xsd")
public class TechnicalServiceNotAliveException extends SaaSApplicationException {

    private static final long serialVersionUID = -6402384816118813208L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public TechnicalServiceNotAliveException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public TechnicalServiceNotAliveException(String message) {
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
    public TechnicalServiceNotAliveException(String message,
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
    public TechnicalServiceNotAliveException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with the specified reason set as its detail
     * message and appended to the message key.
     * 
     * @param reason
     *            the reason
     */
    public TechnicalServiceNotAliveException(Reason reason) {
        super(String.valueOf(reason));
        setMessageKey(getMessageKey() + "." + reason);
    }

    /**
     * Constructs a new exception with the given cause and with the specified
     * reason set as its detail message and appended to the message key.
     * 
     * @param reason
     *            the reason
     * @param t
     *            the cause
     */
    public TechnicalServiceNotAliveException(Reason reason, Throwable t) {
        super(reason.toString(), t);
        setMessageKey(getMessageKey() + "." + reason);
    }

    /**
     * Constructs a new exception with the given cause and message parameters,
     * and with the specified reason set as its detail message and appended to
     * the message key.
     * 
     * @param reason
     *            the reason
     * @param params
     *            the message parameters
     * @param t
     *            the cause
     */
    public TechnicalServiceNotAliveException(Reason reason, Object[] params,
            Throwable t) {
        super(reason.toString(), t, params);
        setMessageKey(getMessageKey() + "." + reason);
    }

    /**
     * Enumeration of possible reasons for a
     * {@link TechnicalServiceNotAliveException}.
     * 
     */
    public enum Reason {
        /**
         * The connection to the provisinoingUrl could not be established.
         */
        CONNECTION_REFUSED,

        /**
         * A problem occurred in a customer operation.
         */
        CUSTOMER,

        /**
         * The WSDL from the provisioning service does not contain an endpoint.
         */
        ENDPOINT,

        /**
         * A problem occurred in a supplier operation.
         */
        SUPPLIER,

        /**
         * The target namespace is unknown (cannot be mapped to a supported
         * version).
         */
        TARGET_NAMESPACE,

        /**
         * A timeout occurred when executing the Web service call.
         */
        TIMEOUT;
    }

}
