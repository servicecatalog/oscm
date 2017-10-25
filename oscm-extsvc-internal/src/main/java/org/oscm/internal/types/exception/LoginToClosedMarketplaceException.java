/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-05-27
 *
 *******************************************************************************/
package org.oscm.internal.types.exception;

/**
 * Created by BadziakP on 2016-05-27.
 */
public class LoginToClosedMarketplaceException extends SaaSApplicationException {

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public LoginToClosedMarketplaceException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     *
     * @param message
     *            the detail message
     */
    public LoginToClosedMarketplaceException(String message) {
        super(message);
    }
}
