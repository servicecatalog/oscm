/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-09-30                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exceptions;

import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Exception thrown when no billingsharesExportData exists for the given period,
 * organization and billingSharesResultType
 * 
 */
public class NoBilingSharesDataAvailableException extends
        SaaSApplicationException {

    private static final long serialVersionUID = -2603050693820893448L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public NoBilingSharesDataAvailableException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public NoBilingSharesDataAvailableException(final String message) {
        super(message);
    }

}
