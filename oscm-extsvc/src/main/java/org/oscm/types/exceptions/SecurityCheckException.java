/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-03-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a security check fails.
 * 
 */
@WebFault(name = "SecurityCheckException", targetNamespace = "http://oscm.org/xsd")
public class SecurityCheckException extends SaaSApplicationException {

    private static final long serialVersionUID = 4180058573768142877L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SecurityCheckException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SecurityCheckException(String message) {
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
    public SecurityCheckException(String message, ApplicationExceptionBean bean) {
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
    public SecurityCheckException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
