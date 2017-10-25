/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-07-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a call to the provisioning service of the application
 * underlying to a technical service returns an error.
 */
@WebFault(name = "TechnicalServiceOperationException", targetNamespace = "http://oscm.org/xsd")
public class TechnicalServiceOperationException extends
        SaaSApplicationException {

    private static final long serialVersionUID = 8900096510416194608L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public TechnicalServiceOperationException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public TechnicalServiceOperationException(String message) {
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
    public TechnicalServiceOperationException(String message,
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
    public TechnicalServiceOperationException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and message
     * parameters.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     */
    public TechnicalServiceOperationException(String message, Object[] params) {
        super(message, params);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * message parameters.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     * @param t
     *            the cause
     */
    public TechnicalServiceOperationException(String message, Object[] params,
            Throwable t) {
        super(message, t, params);
    }

}
