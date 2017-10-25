/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-03-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOUser;

/**
 * Represents the assignment of a user with a specific service role to a
 * subscription whose underlying technical service defines the role.
 * 
 */
public class VOUsageLicense extends BaseVO {

    private static final long serialVersionUID = 1L;

    private VOUser user;
    private String applicationUserId;
    private VORoleDefinition roleDefinition;

    /**
     * Retrieves the user set in the usage license.
     * 
     * @return the user
     */
    public VOUser getUser() {
        return user;
    }

    /**
     * Sets the user for the usage license.
     * 
     * @param user
     *            the user
     */
    public void setUser(VOUser user) {
        this.user = user;
    }

    /**
     * Sets the user ID to be used by the subscription's underlying application
     * for the usage license. This is possible if the application has its own
     * integrated user management.
     * 
     * @param applicationUserId
     *            the user ID to be used by the application
     */
    public void setApplicationUserId(String applicationUserId) {
        this.applicationUserId = applicationUserId;
    }

    /**
     * Retrieves the user ID which is used by the subscription's underlying
     * application for the usage license. This is possible if the application
     * has its own integrated user management.
     * 
     * @return the user ID used by the application
     */
    public String getApplicationUserId() {
        return applicationUserId;
    }

    /**
     * Sets the service role assigned to the user for the subscription.
     * 
     * @param roleDefinition
     *            the role definition
     */
    public void setRoleDefinition(VORoleDefinition roleDefinition) {
        this.roleDefinition = roleDefinition;
    }

    /**
     * Retrieves the service role assigned to the user for the subscription.
     * 
     * @return the role definition
     */
    public VORoleDefinition getRoleDefinition() {
        return roleDefinition;
    }

}
