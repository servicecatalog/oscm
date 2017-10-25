/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-10-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when authorities to be granted to or revoked from an
 * organization are invalid.
 */
@WebFault(name = "OrganizationAuthorityException", targetNamespace = "http://oscm.org/xsd")
public class OrganizationAuthorityException extends SaaSApplicationException {

    private static final long serialVersionUID = -3045508301115769802L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public OrganizationAuthorityException() {

    }

    /**
     * Constructs a new exception with the specified detail message and message
     * parameters. The cause is not initialized.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     */
    public OrganizationAuthorityException(String message, Object[] params) {
        super(message, params);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public OrganizationAuthorityException(String message) {
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
    public OrganizationAuthorityException(String message,
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
    public OrganizationAuthorityException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
