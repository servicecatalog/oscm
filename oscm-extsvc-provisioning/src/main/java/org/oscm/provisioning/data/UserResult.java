/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.provisioning.data;

import java.util.List;

/**
 * Provides user data returned to the platform upon calls to the provisioning
 * service of an application.
 * 
 */
public class UserResult extends BaseResult {
    /**
     * The user objects for which the application has set the
     * <code>applicationUserId</code> field.
     * 
     */
    private List<User> users;

    /**
     * Sets the platform users for which the application has filled the
     * <code>applicationUserId</code> field. These are the platform users which
     * are mapped to the application's own users.
     * 
     * @param users
     *            the list of <code>User</code> objects
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Retrieves the platform users for which the application has filled the
     * <code>applicationUserId</code> field. These are the platform users which
     * are mapped to the application's own users.
     * 
     * @return the list of <code>User</code> objects
     */
    public List<User> getUsers() {
        return users;
    }
}
