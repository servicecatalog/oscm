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
public class ServiceUser implements Serializable {

    private static final long serialVersionUID = -8702261062012232094L;

    private String roleIdentifier;

    private String applicationUserId;

    private String userId;

    private String userLastName;

    private String userFirstName;

    private String email;

    private String locale;

    /**
     * Retrieves the ID generated and used by the application to identify the
     * user.
     * 
     * @return the application user ID
     */
    public String getApplicationUserId() {
        return applicationUserId;
    }

    /**
     * Sets the ID generated and used by the application to identify the user.
     * 
     * @param applicationUserId
     *            the application user ID
     */
    public void setApplicationUserId(String applicationUserId) {
        this.applicationUserId = applicationUserId;
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

    /**
     * Retrieves the identifier of the service role assigned to the user.
     * 
     * @return the role ID
     */
    public String getRoleIdentifier() {
        return roleIdentifier;
    }

    /**
     * Sets the identifier of the service role assigned to the user.
     * 
     * @param roleIdentifier
     *            the role ID
     */
    public void setRoleIdentifier(String roleIdentifier) {
        this.roleIdentifier = roleIdentifier;
    }

}
