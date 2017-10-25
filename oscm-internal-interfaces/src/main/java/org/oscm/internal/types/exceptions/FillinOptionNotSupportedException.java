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
 * Exception thrown when the selected fillin option is not supported for the
 * marketplace
 * 
 */
public class FillinOptionNotSupportedException extends SaaSApplicationException {

    private static final long serialVersionUID = 5853600095534181030L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public FillinOptionNotSupportedException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public FillinOptionNotSupportedException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified reason set as its detail
     * message and appended to the message key.
     * 
     * @param reason
     *            the reason
     */
    public FillinOptionNotSupportedException(Reason reason) {
        super(reason.toString());
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /**
     * Enumeration of possible reasons for a
     * {@link FillinOptionNotSupportedException}.
     */
    public enum Reason {
        /**
         * rating is not enabled for that marketplace
         */
        RATING_NOT_ENABLED;
    }

}
