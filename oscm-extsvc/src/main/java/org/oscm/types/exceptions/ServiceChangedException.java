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
 * Exception thrown when a service cannot be updated because it was changed by
 * someone else in the time between reading and writing it.
 * 
 */
@WebFault(name = "ServiceChangedException", targetNamespace = "http://oscm.org/xsd")
public class ServiceChangedException extends SaaSApplicationException {

    private static final long serialVersionUID = 496457664617022016L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ServiceChangedException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ServiceChangedException(String message) {
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
    public ServiceChangedException(String message, ApplicationExceptionBean bean) {
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
    public ServiceChangedException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with a pre-defined detail message, and appends
     * the specified reason to the message key.
     * 
     * @param reason
     *            the reason
     */
    public ServiceChangedException(Reason reason) {
        super("Service changed");
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a {@link ServiceChangedException}.
     * 
     */
    public enum Reason {

        /**
         * The service properties have been changed.
         */
        SERVICE_MODIFIED,
        /**
         * The service is not accessible.
         */
        SERVICE_INACCESSIBLE;
    }

}
