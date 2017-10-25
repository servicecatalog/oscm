/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 02.07.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.setup;

import java.io.Serializable;

/**
 * Simple wrapper representing the information on the organization key, the user
 * key, the user id and a flag indicating whether the user is an administrative
 * user for the organization or not.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class UserData implements Serializable {

    private static final long serialVersionUID = 3121424031180866527L;

    private String organizationKey;
    private String userKey;
    private String userId;
    private boolean isOrganizationAdmin;

    public String getOrganizationKey() {
        return organizationKey;
    }

    public String getUserKey() {
        return userKey;
    }

    public boolean isOrganizationAdmin() {
        return isOrganizationAdmin;
    }

    public void setOrganizationKey(String organizationKey) {
        this.organizationKey = organizationKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public void setOrganizationAdmin(boolean isOrganizationAdmin) {
        this.isOrganizationAdmin = isOrganizationAdmin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
