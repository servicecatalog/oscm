/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * This object defines the relationship between user group, platform user and user unit role.
 * 
 * @author Fang
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "usergroup_tkey",
        "platformuser_tkey" }))
@NamedQueries({ @NamedQuery(name = "UserGroupToUser.findByBusinessKey", query = "SELECT obj FROM UserGroupToUser obj WHERE obj.usergroup_tkey = :usergroup_tkey AND obj.platformuser_tkey = :platformuser_tkey ORDER BY obj.usergroup_tkey ASC") })
@BusinessKey(attributes = { "usergroup_tkey", "platformuser_tkey" })
public class UserGroupToUser extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = 5177310226685853850L;

    @Column(name = "usergroup_tkey", nullable = false, insertable = false, updatable = false)
    private long usergroup_tkey;

    @Column(name = "platformuser_tkey", nullable = false, insertable = false, updatable = false)
    private long platformuser_tkey;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "userGroupToUser", fetch = FetchType.LAZY)
    private List<UnitRoleAssignment> unitRoleAssignments = new ArrayList<UnitRoleAssignment>();

    public UserGroupToUser() {
        super();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usergroup_tkey")
    private UserGroup userGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platformuser_tkey")
    private PlatformUser platformuser;

    public long getUsergroup_tkey() {
        return usergroup_tkey;
    }

    public void setUsergroup_tkey(long usergroup_tkey) {
        this.usergroup_tkey = usergroup_tkey;
    }

    public long getPlatformuser_tkey() {
        return platformuser_tkey;
    }

    public void setPlatformuser_tkey(long platformuser_tkey) {
        this.platformuser_tkey = platformuser_tkey;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
        if (userGroup != null) {
            setUsergroup_tkey(userGroup.getKey());
        }
    }

    public PlatformUser getPlatformuser() {
        return platformuser;
    }

    public void setPlatformuser(PlatformUser platformuser) {
        this.platformuser = platformuser;
        if (platformuser != null) {
            setPlatformuser_tkey(platformuser.getKey());
        }
    }

    public List<UnitRoleAssignment> getUnitRoleAssignments() {
        return unitRoleAssignments;
    }

    public void setUnitRoleAssignments(List<UnitRoleAssignment> unitRoleAssignments) {
        this.unitRoleAssignments = unitRoleAssignments;
    }

}
