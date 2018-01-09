/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-03-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;

/**
 * Provides information for authenticating a user. The information consists of a
 * user ID and a password.
 */
public class PasswordAuthentication implements Serializable {

    private static final long serialVersionUID = 2981435217841741067L;

    private String userName;
    private String password;

    /**
     * Creates a new <code>PasswordAuthentication</code> object.
     * 
     * @param userName
     *            the user ID
     * @param password
     *            the user's password
     */
    public PasswordAuthentication(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Returns the user ID.
     * 
     * @return the user ID
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the user's password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PasswordAuthentication other = (PasswordAuthentication) obj;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }
}
