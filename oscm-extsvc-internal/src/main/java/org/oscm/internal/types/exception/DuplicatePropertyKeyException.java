/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.02.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

/**
 * @author farmaki
 * 
 */
public class DuplicatePropertyKeyException extends SaaSApplicationException {

    private static final long serialVersionUID = -4688357772093619837L;

    /**
     * Constructs a new exception with a pre-defined detail message. The cause
     * is not initialized.
     */
    public DuplicatePropertyKeyException() {
        super("Connection Property has at least one duplicate key.");
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public DuplicatePropertyKeyException(String message) {
        super(message);
    }

}
