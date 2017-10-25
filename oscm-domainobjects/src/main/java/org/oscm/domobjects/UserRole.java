/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                               
 *                                                                              
 *  Creation Date: 29.04.2011                                                      
 *                                                                              
 *  Completion Time: 29.04.2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * An UserRole represents a role that is supported by the system and that a
 * platform user can have.
 * 
 * @author cheld
 * 
 */
@Entity
@BusinessKey(attributes = "roleName")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "roleName" }))
@NamedQueries({
        @NamedQuery(name = "UserRole.findByBusinessKey", query = "SELECT userRole FROM UserRole userRole WHERE userRole.dataContainer.roleName = :roleName"),
        @NamedQuery(name = "UserRole.getAllUserRoles", query = "SELECT userRole FROM UserRole userRole") })
public class UserRole extends DomainObjectWithVersioning<UserRoleData> {

    private static final long serialVersionUID = -2179561445314279387L;

    public UserRole() {
        super();
        dataContainer = new UserRoleData();
    }

    public UserRole(UserRoleType roleName) {
        this();
        setRoleName(roleName);
    }

    /**
     * Refer to {@link UserRoleData#roleName}
     */
    public UserRoleType getRoleName() {
        return dataContainer.getRoleName();
    }

    /**
     * Refer to {@link UserRoleData#roleName}
     */
    public void setRoleName(UserRoleType roleName) {
        dataContainer.setRoleName(roleName);
    }

}
