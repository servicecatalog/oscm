/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-06-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown to indicate that an organization has been removed because
 * its account was not confirmed.
 * 
 */
@WebFault(name = "OrganizationRemovedException", targetNamespace = "http://oscm.org/xsd")
public class OrganizationRemovedException extends SaaSApplicationException {

    private static final long serialVersionUID = 9042533346012802859L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public OrganizationRemovedException() {

    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * message parameters.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     * @param cause
     *            the cause
     */
    public OrganizationRemovedException(String message, Object[] params,
            Throwable cause) {
        super(message, cause, params);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public OrganizationRemovedException(String message) {
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
    public OrganizationRemovedException(String message,
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
    public OrganizationRemovedException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
