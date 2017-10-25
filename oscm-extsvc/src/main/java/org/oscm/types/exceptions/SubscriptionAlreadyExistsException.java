/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-07-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when trying to create several subscriptions for a technical
 * service which allows for one subscription only.
 * 
 */
@WebFault(name = "SubscriptionAlreadyExistsException", targetNamespace = "http://oscm.org/xsd")
public class SubscriptionAlreadyExistsException extends
        SaaSApplicationException {

    private static final long serialVersionUID = 1627764755258290479L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SubscriptionAlreadyExistsException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SubscriptionAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message parameters.
     * 
     * @param params
     *            the message parameters
     */
    public SubscriptionAlreadyExistsException(Object[] params) {
        super(params);
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
    public SubscriptionAlreadyExistsException(String message,
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
    public SubscriptionAlreadyExistsException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
