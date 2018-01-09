/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-10-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;

/**
 * Provides information on a user.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 4593705746979139509L;

    /**
     * The key generated and used by the platform to identify the user.
     * 
     */
    private long userKey;

    /**
     * The ID generated and used by the platform to identify the user.
     * 
     */
    private String userId;

    /**
     * The user's last name.
     * 
     */
    private String userLastName;

    /**
     * The user's first name.
     * 
     */
    private String userFirstName;

    /**
     * The user's email address.
     * 
     */
    private String email;

    /**
     * The language used for interacting with the user.
     */
    private String locale;

    /**
     * Retrieves the key generated and used by the platform to identify the
     * user.
     * 
     * @return the platform user key
     */
    public long getUserKey() {
        return userKey;
    }

    /**
     * Retrieves the ID generated and used by the platform to identify the user.
     * 
     * @return the platform user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the key generated and used by the platform to identify the user.
     * 
     * @param userKey
     *            the platform user key
     */
    public void setUserKey(long userKey) {
        this.userKey = userKey;
    }

    /**
     * Sets the ID generated and used by the platform to identify the user.
     * <p>
     * The platform user ID may include the following characters: <br>
     * <code>( ) - . 0 - 9 @ A - Z [ ] _ <br>
     * a-#xD7FF<br>
     * #xE000-xFFFD <br>
     * x10000-#x10FFFF</code>
     * <p>
     * Blanks are not allowed. The maximum length is 40 characters.
     * 
     * @param userId
     *            the platform user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the user's last name.
     * 
     * @return the last name
     */
    public String getLastName() {
        return userLastName;
    }

    /**
     * Sets the user's first name.
     * 
     * @param firstName
     *            the first name
     */
    public void setFirstName(String firstName) {
        this.userFirstName = firstName;
    }

    /**
     * Retrieves the user's first name.
     * 
     * @return the first name
     */
    public String getFirstName() {
        return userFirstName;
    }

    /**
     * Sets the user's last name.
     * 
     * @param lastName
     *            the last name
     */
    public void setLastName(String lastName) {
        this.userLastName = lastName;
    }

    /**
     * Retrieves the user's email address.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * 
     * @param email
     *            the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the language used for interacting with the user.
     * 
     * @return the language code
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the language used for interacting with the user.
     * 
     * @param locale
     *            the language. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
