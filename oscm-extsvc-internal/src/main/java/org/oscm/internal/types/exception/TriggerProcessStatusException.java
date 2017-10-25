/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-06-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;

/**
 * Exception thrown when a problem occurs due to the current status of a trigger
 * process.
 * 
 */
public class TriggerProcessStatusException extends SaaSApplicationException {

    private static final long serialVersionUID = -7721815194056865464L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public TriggerProcessStatusException() {

    }

    /**
     * Constructs a new exception with the specified detail message and message
     * parameter.
     * 
     * @param message
     *            the detail message
     * @param status
     *            the status of the trigger process
     */
    public TriggerProcessStatusException(String message,
            TriggerProcessStatus status) {
        super(message, new Object[] { status });
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public TriggerProcessStatusException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public TriggerProcessStatusException(String message,
            ApplicationExceptionBean bean) {
        super(message, bean);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * bean for JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     * @param cause
     *            the cause
     */
    public TriggerProcessStatusException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
