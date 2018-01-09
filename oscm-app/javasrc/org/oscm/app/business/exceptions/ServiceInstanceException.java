/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-02-25                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.business.exceptions;

/**
 * @author goebel
 */
public class ServiceInstanceException extends Exception {

    private static final long serialVersionUID = 6410887335421503457L;

    public ServiceInstanceException(Exception ex) {
        super(ex);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * parameters.
     * 
     * @param message
     *            the detail message
     * @param args
     *            the message parameters
     * 
     */
    public ServiceInstanceException(String message, Object... args) {
        super(String.format(message, args));
    }

    public ServiceInstanceException(Throwable cause, String message,
            Object... args) {
        super(String.format(message, args), cause);
    }

}
