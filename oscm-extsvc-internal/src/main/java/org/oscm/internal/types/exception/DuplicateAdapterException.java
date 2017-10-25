/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

/**
 * @author farmaki
 * 
 */

/**
 * Exception thrown when the identifier of a billing adapter if not unique.
 * 
 */
public class DuplicateAdapterException extends SaaSApplicationException {

    private static final long serialVersionUID = -3726073100444042962L;

    /**
     * Constructs a new exception with a pre-defined detail message. The cause
     * is not initialized.
     */
    public DuplicateAdapterException() {
        super("Billing adapter violates the unique ID constraint for the billing identifier.");
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public DuplicateAdapterException(String message) {
        super(message);
    }

}
