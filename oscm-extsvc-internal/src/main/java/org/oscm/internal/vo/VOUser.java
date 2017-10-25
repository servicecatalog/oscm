/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.util.HashSet;
import java.util.Set;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * Represents a user registered in the platform.
 */
public class VOUser extends BaseVO {

    private static final long serialVersionUID = -8698185138206496238L;

    private String organizationId;
    private String organizationName;
    private String userId;
    private UserAccountStatus status;
    private Set<OrganizationRoleType> organizationRoles = new HashSet<OrganizationRoleType>();
    private Set<UserRoleType> userRoles = new HashSet<UserRoleType>();
    private String tenantKey;
    private String tenantId;

    /**
     * Default constructor.
     */
    public VOUser() {

    }

    protected VOUser(long key, int version) {
        super(key, version);
    }

    /**
     * Retrieves the unique identifier of the organization the user belongs to.
     * 
     * @return the organization ID
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the unique identifier of the organization the user belongs to.
     * 
     * @param organizationId
     *            the organization ID
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Retrieves the unique identifier of the user.
     * 
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier of the user.
     * 
     * @param userId
     *            the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the status of the user account.
     * 
     * @param status
     *            the status
     */
    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }

    /**
     * Retrieves the status of the user account.
     * 
     * @return the status
     */
    public UserAccountStatus getStatus() {
        return status;
    }

    /**
     * Retrieves the roles assigned to the user.
     * 
     * @return the user roles
     */
    public Set<UserRoleType> getUserRoles() {
        return userRoles;
    }

    /**
     * Sets the given roles for the user.
     * 
     * @param userRoles
     *            the user roles
     */
    public void setUserRoles(Set<UserRoleType> userRoles) {
        this.userRoles = userRoles;
    }

    /**
     * Adds the specified role to the user's roles.
     * 
     * @param userRole
     *            the user role
     */
    public void addUserRole(UserRoleType userRole) {
        userRoles.add(userRole);
    }

    /**
     * Removes the specified role from the user's roles.
     * 
     * @param userRole
     *            the user role
     */
    public void removeUserRole(UserRoleType userRole) {
        userRoles.remove(userRole);
    }

    /**
     * Retrieves the organization roles of the user's organization.
     * 
     * @return the organization roles
     */
    public Set<OrganizationRoleType> getOrganizationRoles() {
        return organizationRoles;
    }

    /**
     * Sets the organization roles of the user's organization.
     * 
     * @param organizationRoles
     *            the organization roles
     */
    public void setOrganizationRoles(Set<OrganizationRoleType> organizationRoles) {
        this.organizationRoles = organizationRoles;
    }

    /**
     * Retrieves the organization name of the user's organization.
     *
     * @return the organization name
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Sets the organization name of the user's organization.
     *
     * @param organizationName
     *            the organization roles
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * Sets tenant to which user belongs
     * @return
     */
    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    /**
     * Gets tenant to which user belongs
     * @return
     */
    public String getTenantKey() {
        return tenantKey;
    }

    /**
     * Sets tenantID to which user belongs
     * @param tenantId
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Returns tenantID to which user belongs
     */
    public String getTenantId() {
        return tenantId;
    }
}
