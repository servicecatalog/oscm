/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-02-01                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

/**
 * Exception thrown when a method fails due to invalid input parameters.
 * 
 */
public class IllegalArgumentException extends SaaSSystemException {

    private static final long serialVersionUID = 9044422434383029323L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public IllegalArgumentException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public IllegalArgumentException(String message) {
        super(message);
    }
}
