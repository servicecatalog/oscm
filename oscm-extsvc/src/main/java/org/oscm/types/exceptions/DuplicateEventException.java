/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-08-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the ID of an event is not unique for a subscription.
 * 
 */
@WebFault(name = "DuplicateEventException", targetNamespace = "http://oscm.org/xsd")
public class DuplicateEventException extends SaaSApplicationException {

    private static final long serialVersionUID = -3425916231618323219L;

    /**
     * Constructs a new exception with a pre-defined detail message. The cause
     * is not initialized.
     */
    public DuplicateEventException() {
        super("Event has no unique ID for the subscription.");
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public DuplicateEventException(String message) {
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
    public DuplicateEventException(String message, ApplicationExceptionBean bean) {
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
    public DuplicateEventException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
