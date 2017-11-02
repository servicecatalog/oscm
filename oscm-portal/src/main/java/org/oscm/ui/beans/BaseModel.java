/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 12.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;

/**
 * Base model bean class.
 * 
 * @author weiser
 * 
 */
public abstract class BaseModel implements Serializable {

    private static final long serialVersionUID = -9105291444335491263L;

    private String token;
    private String tokenIntern;

    protected BaseModel() {
        resetToken();
    }

    public String getToken() {
        return tokenIntern;
    }

    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Checks if the internal token matches the one submitted from the form. If
     * the tokens don't match, the action should not be processed.
     * 
     * @return <code>true</code> if internal and submitted token match otherwise
     *         <code>false</code>
     */
    public boolean isTokenValid() {
        return tokenIntern.equals(token);
    }

    /**
     * Fill the internal token with a new random value after the successful
     * action execution.
     */
    public void resetToken() {
        tokenIntern = String.valueOf(Math.random());
    }

}
