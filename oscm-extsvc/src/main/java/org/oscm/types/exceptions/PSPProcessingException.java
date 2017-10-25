/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-12-22                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when problems occur in the processing of billing data.
 * 
 */
@WebFault(name = "PSPProcessingException", targetNamespace = "http://oscm.org/xsd")
public class PSPProcessingException extends SaaSApplicationException {

    private static final long serialVersionUID = 1170174453783800112L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public PSPProcessingException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public PSPProcessingException(String message) {
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
    public PSPProcessingException(String message, Throwable cause) {
        super(message, cause);
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
    public PSPProcessingException(String message, ApplicationExceptionBean bean) {
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
    public PSPProcessingException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
