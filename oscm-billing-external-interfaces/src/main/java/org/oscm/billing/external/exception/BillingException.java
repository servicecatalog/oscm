/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *  
 *  Creation Date: 2014-08-26
 *                                                                                                                                                                                                      
 *******************************************************************************/

package org.oscm.billing.external.exception;

import javax.ejb.ApplicationException;

/**
 * Exception thrown if the connection to an external billing system cannot be
 * established or a price model cannot be retrieved from such a system.
 *
 */
@ApplicationException(rollback = true)
public class BillingException extends Exception {

    private static final long serialVersionUID = 3776478529289292986L;
    private String id;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public BillingException() {
        super();
        init();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public BillingException(String message) {
        super(message);
        init();
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public BillingException(String message, Throwable cause) {
        super(message, cause);
        init();
    }

    private void init() {
        id = Long.valueOf(System.currentTimeMillis() % Long.MAX_VALUE)
                .toString();
    }

    /**
     * Returns the detail message of this exception, preceded by the exception
     * ID.
     * 
     * @return the detail message
     */
    @Override
    public String getMessage() {
        return "EXCEPTIONID " + id + ": " + super.getMessage();
    }

}
