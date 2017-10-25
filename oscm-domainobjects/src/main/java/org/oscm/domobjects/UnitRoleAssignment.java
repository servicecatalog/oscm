/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 24.07.2010                                                     
 *                                                                              
 *******************************************************************************/
package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * The object defines relationship between unit role and platform user.
 */
@Entity
@BusinessKey(attributes = { "usergrouptouser_tkey", "unituserrole_tkey" })
@NamedQueries({ @NamedQuery(name = "UnitRoleAssignment.findByBusinessKey", query = "SELECT obj FROM UnitRoleAssignment obj WHERE obj.usergrouptouser_tkey = :usergrouptouser_tkey AND obj.unituserrole_tkey = :unituserrole_tkey ORDER BY obj.usergrouptouser_tkey ASC"),
    @NamedQuery(name = "UnitRoleAssignment.findByUserAndGroup", query = "SELECT ura FROM UserGroup us, UnitRoleAssignment ura, UserGroupToUser ugtu, PlatformUser pu WHERE ugtu.userGroup = us AND ugtu.usergroup_tkey = :usergroup_tkey AND pu.dataContainer.userId = :userId AND ugtu.platformuser = pu AND ugtu = ura.userGroupToUser")})
public class UnitRoleAssignment extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = -1809586993141091289L;

    @Column(name = "usergrouptouser_tkey", insertable = false, updatable = false, nullable = false)
    private long usergrouptouser_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usergrouptouser_tkey")
    private UserGroupToUser userGroupToUser;

    @Column(name = "unituserrole_tkey", insertable = false, updatable = false, nullable = false)
    private long unituserrole_tkey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unituserrole_tkey")
    private UnitUserRole unitUserRole;

    public long getUnituserrole_tkey() {
        return unituserrole_tkey;
    }

    public void setUnituserrole_tkey(long unituserrole_tkey) {
        this.unituserrole_tkey = unituserrole_tkey;
    }

    public UnitUserRole getUnitUserRole() {
        return unitUserRole;
    }

    public void setUnitUserRole(UnitUserRole unitUserRole) {
        this.unitUserRole = unitUserRole;
        if (unitUserRole != null) {
            setUnituserrole_tkey(unitUserRole.getKey());
        }
    }

    public long getUsergrouptouser_tkey() {
        return usergrouptouser_tkey;
    }

    public void setUsergrouptouser_tkey(long usergrouptouser_tkey) {
        this.usergrouptouser_tkey = usergrouptouser_tkey;
    }

    public UserGroupToUser getUserGroupToUser() {
        return userGroupToUser;
    }

    public void setUserGroupToUser(UserGroupToUser userGroupToUser) {
        this.userGroupToUser = userGroupToUser;
        if (userGroupToUser != null) {
            setUsergrouptouser_tkey(userGroupToUser.getKey());
        }
    }

}
