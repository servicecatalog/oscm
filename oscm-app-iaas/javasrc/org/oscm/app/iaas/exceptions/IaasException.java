/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 11.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.exceptions;

/**
 * @author Dirk Bernsau
 * 
 */
public abstract class IaasException extends Exception {

    private static final long serialVersionUID = 4116823780290974616L;

    public IaasException(String message) {
        super(message);
    }

    /**
     * This flag signals that the exception was thrown because the requested
     * action could not be initiated at the moment due to some kind of busy
     * state. The caller might suspend processing and try again later.
     * 
     * @return <code>true</code> if the target system was busy
     */
    public abstract boolean isBusyMessage();

    /**
     * 
     */
    public abstract boolean isIllegalState();

}
