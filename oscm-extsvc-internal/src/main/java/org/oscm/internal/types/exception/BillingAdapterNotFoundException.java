/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 24.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;


/**
 * Exception thrown when a non-existing billing adapter is configured for a
 * technical service.
 */
public class BillingAdapterNotFoundException extends SaaSApplicationException {

    private static final long serialVersionUID = 8644688338033488472L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public BillingAdapterNotFoundException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public BillingAdapterNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message parameters.
     * 
     * @param params
     *            the message parameters
     */
    public BillingAdapterNotFoundException(Object[] params) {
        super(params);
    }

    /**
     * Constructs a new exception with the specified detail message and message
     * parameters.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     */
    public BillingAdapterNotFoundException(String message, Object[] params) {
        super(message, params);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * message parameters.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     * @param t
     *            the cause
     */
    public BillingAdapterNotFoundException(String message, Object[] params,
            Throwable t) {
        super(message, t, params);
    }

}
