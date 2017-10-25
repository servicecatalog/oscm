/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-09-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.ejb.ApplicationException;
import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a requested catalog entry has been removed.
 * 
 */
@ApplicationException(rollback = true)
@WebFault(name = "CatalogEntryRemovedException", targetNamespace = "http://oscm.org/xsd")
public class CatalogEntryRemovedException extends SaaSApplicationException {

    private static final long serialVersionUID = 7679039369926944038L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public CatalogEntryRemovedException() {

    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public CatalogEntryRemovedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public CatalogEntryRemovedException(String message) {
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
    public CatalogEntryRemovedException(String message,
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
    public CatalogEntryRemovedException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
