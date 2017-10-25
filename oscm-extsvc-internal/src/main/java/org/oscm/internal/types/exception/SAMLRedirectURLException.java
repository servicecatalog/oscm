/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.SAMLRedirectURLExceptionBean;

/**
 * @author roderus
 * 
 */
public class SAMLRedirectURLException extends SaaSApplicationException {

    private static final long serialVersionUID = -9006836433751915578L;

    private SAMLRedirectURLExceptionBean bean = new SAMLRedirectURLExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SAMLRedirectURLException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SAMLRedirectURLException(String message) {
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
    public SAMLRedirectURLException(String message, ReasonEnum reason) {
        super(message);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a {@link SAMLRedirectURLException}.
     * 
     */
    public static enum ReasonEnum {

        /**
         * To generate a SAML Redirect URL, a valid Authentication Request must
         * be set.
         */
        MISSING_AUTHNREQUEST,

        /**
         * To generate a SAML Redirect URL, a valid redirect endpoint URL must
         * be set.
         */
        MISSING_ENDPOINTURL;
    }
}
