/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 11.10.16 12:26
 *
 ******************************************************************************/

package org.oscm.internal.types.exception;

/**
 * @author dchojnacki
 *
 */
public class IssuerNotMatchException extends SaaSApplicationException {

    private static final long serialVersionUID = -1093186892330080980L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public IssuerNotMatchException() {

    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public IssuerNotMatchException(String message) {
        super(message);
    }

}