/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-11-09                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

/**
 * Exception thrown when the task error handling failed.
 * 
 */
public class TaskErrorHandlingException extends SaaSSystemException {

    private static final long serialVersionUID = -4487876045774918599L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public TaskErrorHandlingException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public TaskErrorHandlingException(String message) {
        super(message);
    }
}
