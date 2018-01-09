/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                      
 *                                                                              
 *  Creation Date: 29.04.2011                                                      
 *                                                                              
 *  Completion Time: 02.05.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The class <code>RoleAssignment</code> associates a platform user with a role.
 * 
 * @author cheld
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "USER_TKEY",
        "USERROLE_TKEY" }))
@NamedQueries({ @NamedQuery(name = "RoleAssignment.getPlatformOperators", query = "SELECT role.user FROM RoleAssignment role where role.userRole.dataContainer.roleName=org.oscm.internal.types.enumtypes.UserRoleType.PLATFORM_OPERATOR") })
public class RoleAssignment extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = -8066561546691425234L;

    /**
     * The user role this object refers to.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UserRole userRole;

    /**
     * The user this object refers to.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PlatformUser user;

    public UserRole getRole() {
        return userRole;
    }

    public void setRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public PlatformUser getUser() {
        return user;
    }

    public void setUser(PlatformUser user) {
        this.user = user;
    }
}
