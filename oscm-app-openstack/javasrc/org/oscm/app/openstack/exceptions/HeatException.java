/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 13.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.exceptions;

/**
 * @author Dirk Bernsau
 * 
 */
public class HeatException extends Exception {

    private static final long serialVersionUID = 4463597914514288826L;

    private int responseCode = -1;

    /**
     * Creates a new exception with given message.
     * 
     * @param message
     */
    public HeatException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with given message and response code.
     * 
     * @param message
     * @param responseCode
     */
    public HeatException(String message, int responseCode) {
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
