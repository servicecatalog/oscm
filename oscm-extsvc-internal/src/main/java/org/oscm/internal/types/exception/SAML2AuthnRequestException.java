/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.SAML2AuthnRequestExceptionBean;

/**
 * @author roderus
 * 
 */
public class SAML2AuthnRequestException extends SaaSApplicationException {

    private static final long serialVersionUID = -9006836433751915577L;

    private SAML2AuthnRequestExceptionBean bean = new SAML2AuthnRequestExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SAML2AuthnRequestException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SAML2AuthnRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and reason.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     */
    public SAML2AuthnRequestException(String message, ReasonEnum reason) {
        super(message);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a {@link SAML2AuthnRequestException}.
     * 
     */
    public static enum ReasonEnum {

        /**
         * Error while converting the Authentication Request
         */
        XML_TRANSFORMATION_ERROR,

        /**
         * SSO_ISSUER_ID must be set in the configsettings
         */
        MISSING_ISSUER,

        /**
         * Error while redirecting/forwarding to the IdP
         */
        CANNOT_REDIRECT_OR_FORWARD;
    }
}
