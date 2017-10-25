/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: Jun 01, 2016
 *
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.SAML2StatusCodeInvalidExceptionBean;

/**
 * @author mgrubski
 *
 */
public class SAML2StatusCodeInvalidException extends SaaSApplicationException {

    private static final long serialVersionUID = -1093186892330080980L;

    private final SAML2StatusCodeInvalidExceptionBean bean = new SAML2StatusCodeInvalidExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SAML2StatusCodeInvalidException() {

    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            the detail message
     */
    public SAML2StatusCodeInvalidException(String message) {
        super(message);
    }

}