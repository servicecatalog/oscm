/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-08-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;


/**
 * Exception thrown when a method call is not supported in the given context.
 * 
 */
public class UnsupportedOperationException extends SaaSSystemException {

    private static final long serialVersionUID = 9044422434383029323L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public UnsupportedOperationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public UnsupportedOperationException(String message) {
        super(message);
    }
}
