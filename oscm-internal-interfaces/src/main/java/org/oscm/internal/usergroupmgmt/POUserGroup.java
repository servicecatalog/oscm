/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usergroupmgmt;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.POUserInUnit;

/**
 * @author qiu
 */
public class POUserGroup extends BasePO {

    private static final long serialVersionUID = -4638663067090161797L;
    private static final String RADIO_SELECTED = "true";
    private String groupName;
    private String groupDescription;
    private String groupReferenceId;
    private List<POUserDetails> users = new ArrayList<POUserDetails>();
    private List<POService> invisibleServices = new ArrayList<POService>();
    private List<POService> visibleServices = new ArrayList<POService>();
    private String selectedRole;
    private List<SelectItem> roles = new ArrayList<SelectItem>();
    private long userNum;
    private String unitChecked;
    private boolean unitSelected;
    private List<POUserInUnit> usersAssignedToUnit = new ArrayList<POUserInUnit>();
    private List<POUserGroupToInvisibleProduct> invisibleProducts = new ArrayList<POUserGroupToInvisibleProduct>();

    public List<POService> getVisibleServices() {
        return visibleServices;
    }

    public void setVisibleServices(List<POService> visibleServices) {
        this.visibleServices = visibleServices;
    }

    private boolean isDefault;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupReferenceId() {
        return groupReferenceId;
    }

    public void setGroupReferenceId(String groupReferenceId) {
        this.groupReferenceId = groupReferenceId;
    }

    public List<POUserDetails> getUsers() {
        return users;
    }

    public void setUsers(List<POUserDetails> users) {
        this.users = users;
    }

    public List<POService> getInvisibleServices() {
        return invisibleServices;
    }

    public void setInvisibleServices(List<POService> services) {
        this.invisibleServices = services;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public long getUserNum() {
        return userNum;
    }

    public void setUserNum(long userNum) {
        this.userNum = userNum;
    }

    public String getUnitChecked() {
        unitChecked = null;
        if (this.isUnitSelected()) {
            unitChecked = RADIO_SELECTED;
        }
        return unitChecked;
    }

    public void setUnitChecked(String unitChecked) {
        this.unitChecked = unitChecked;
    }

    public boolean isUnitSelected() {
        return unitSelected;
    }

    public void setUnitSelected(boolean unitSelected) {
        this.unitSelected = unitSelected;
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void setRoles(List<SelectItem> roles) {
        this.roles = roles;
    }

    public List<POUserInUnit> getUsersAssignedToUnit() {
        return usersAssignedToUnit;
    }

    public void setUsersAssignedToUnit(List<POUserInUnit> usersAssignedToUnit) {
        this.usersAssignedToUnit = usersAssignedToUnit;
    }

    /**
     * @return the invisibleProducts
     */
    public List<POUserGroupToInvisibleProduct> getInvisibleProducts() {
        return invisibleProducts;
    }

    /**
     * @param invisibleProducts the invisibleProducts to set
     */
    public void setInvisibleProducts(List<POUserGroupToInvisibleProduct> invisibleProducts) {
        this.invisibleProducts = invisibleProducts;
    }
}
