/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 25, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.userGroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usermanagement.POUserInUnit;
import org.oscm.ui.beans.BaseModel;

/**
 * @author mao
 * 
 */
@ViewScoped
@ManagedBean(name = "manageGroupModel")
public class ManageGroupModel extends BaseModel {

    private static final long serialVersionUID = 7803451754585858737L;

    static final String ATTRIBUTE_GROUP_ID = "groupId";

    private boolean dirty;

    private String selectedGroupId = "";

    private POUserGroup selectedGroup;

    private List<ServiceRow> serviceRows = new ArrayList<ServiceRow>();

    private String modalTitle = "";

    private String deassignMessage = "";

    private boolean isUserGroupNotFoundException = false;

    private boolean allServicesSelected = false;

    private List<POUserGroup> groups = new ArrayList<>();

    private List<POUserInUnit> usersToAssign = new ArrayList<POUserInUnit>();

    private List<POUserInUnit> usersToUnassign = new ArrayList<POUserInUnit>();

    private List<POUserInUnit> usersToUpdate = new ArrayList<POUserInUnit>();

    private List<POUserInUnit> currentResultUsers = new ArrayList<POUserInUnit>();

    private Map<String, Boolean> selectedUsersIds = new HashMap<String, Boolean>();

    private List<SelectItem> roles = new ArrayList<SelectItem>();

    private Map<String, String> userAndRole = new HashMap<String, String>();

    private String changedUserId;

    private String changedRoleName;
    
    private boolean selection;
    
    private boolean selectAll;
    
    private Map<String, POUserInUnit> usersAssignedToUnit = new HashMap<String, POUserInUnit>();

    public List<ServiceRow> getServiceRows() {
        return serviceRows;
    }

    public void setServiceRows(List<ServiceRow> serviceRows) {
        this.serviceRows = serviceRows;
    }

    public POUserGroup getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(POUserGroup selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public String getSelectedGroupId() {
        return selectedGroupId;
    }

    public void setSelectedGroupId(String selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    public String getModalTitle() {
        return modalTitle;
    }

    public void setModalTitle(String modalTitle) {
        this.modalTitle = modalTitle;
    }

    public String getDeassignMessage() {
        return deassignMessage;
    }

    public void setDeassignMessage(String deassignMessage) {
        this.deassignMessage = deassignMessage;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isUserGroupNotFoundException() {
        return isUserGroupNotFoundException;
    }

    public void setUserGroupNotFoundException(
            boolean isUserGroupNotFoundException) {
        this.isUserGroupNotFoundException = isUserGroupNotFoundException;
    }

    public boolean isAllServicesSelected() {
        return allServicesSelected;
    }

    public void setAllServicesSelected(boolean allServicesSelected) {
        this.allServicesSelected = allServicesSelected;
    }

    public int getServicesNumber() {
        return serviceRows.size();
    }

    public void setGroups(List<POUserGroup> groups) {
        this.groups = groups;
    }

    public List<POUserGroup> getGroups() {
        return groups;
    }

    public List<POUserInUnit> getUsersToAssign() {
        return usersToAssign;
    }

    public void setUsersToAssign(List<POUserInUnit> usersToAssign) {
        this.usersToAssign = usersToAssign;
    }

    public List<POUserInUnit> getUsersToUnassign() {
        return usersToUnassign;
    }

    public void setUsersToUnassign(List<POUserInUnit> usersToUnassign) {
        this.usersToUnassign = usersToUnassign;
    }

    public List<POUserInUnit> getCurrentResultUsers() {
        return currentResultUsers;
    }

    public void setCurrentResultUsers(List<POUserInUnit> currentResultUsers) {
        this.currentResultUsers = currentResultUsers;
    }

    public Map<String, Boolean> getSelectedUsersIds() {
        return selectedUsersIds;
    }

    public void setSelectedUsersIds(Map<String, Boolean> selectedUsersIds) {
        this.selectedUsersIds = selectedUsersIds;
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void setRoles(List<SelectItem> roles) {
        this.roles = roles;
    }

    public List<POUserInUnit> getUsersToUpdate() {
        return usersToUpdate;
    }

    public void setUsersToUpdate(List<POUserInUnit> usersToUpdate) {
        this.usersToUpdate = usersToUpdate;
    }

    public Map<String, String> getUserAndRole() {
        return userAndRole;
    }

    public void setUserAndRole(Map<String, String> userAndRole) {
        this.userAndRole = userAndRole;
    }

    public String getChangedUserId() {
        return changedUserId;
    }

    public void setChangedUserId(String changedUserId) {
        this.changedUserId = changedUserId;
    }

    public String getChangedRoleName() {
        return changedRoleName;
    }

    public void setChangedRoleName(String changedRoleName) {
        this.changedRoleName = changedRoleName;
    }

    public boolean isSelection() {
        return selection;
    }

    public void setSelection(boolean selection) {
        this.selection = selection;
    }

    public Map<String, POUserInUnit> getUsersAssignedToUnit() {
        return usersAssignedToUnit;
    }

    public void setUsersAssignedToUnit(Map<String, POUserInUnit> usersAssignedToUnit) {
        this.usersAssignedToUnit = usersAssignedToUnit;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

}
