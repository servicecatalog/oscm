/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016/10/06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.exceptions;

/**
 * @author tateiwamext
 *
 */
public class NovaException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 4036102733666995556L;
    private int responseCode = -1;

    /**
     * Creates a new exception with given message.
     * 
     * @param message
     */
    public NovaException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with given message and response code.
     * 
     * @param message
     * @param responseCode
     */
    public NovaException(String message, int responseCode) {
        this(message);
        this.responseCode = responseCode;
    }

    /**
     * Returns the HTTP response code related to the exception (if present).
     * 
     * @return the response code or -1 if not applicable
     */
    public int getResponseCode() {
        return responseCode;
    }
}
