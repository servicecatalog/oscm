/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel.external;

import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Exception thrown when the external price model is not available
 * 
 */
public class ExternalPriceModelException extends SaaSApplicationException {

    private static final long serialVersionUID = 7194031787151261199L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ExternalPriceModelException() {
    }

    /**
     * Constructs a new exception with the cause for this exception.
     * 
     * @param cause
     *            the cause for this exception
     */
    public ExternalPriceModelException(Throwable cause) {
        super(cause);
    }
}
