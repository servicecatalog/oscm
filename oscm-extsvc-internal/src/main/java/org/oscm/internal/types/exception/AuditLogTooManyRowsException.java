/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2013-05-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;


/**
 * Exception thrown when audit log data to be exported contains too many entries.  
 * <p> 
 * @author goebel
 */
public class AuditLogTooManyRowsException extends SaaSApplicationException {

    private static final long serialVersionUID = 7366660626179100575L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public AuditLogTooManyRowsException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public AuditLogTooManyRowsException(String message) {
        super(message);
    }
}
