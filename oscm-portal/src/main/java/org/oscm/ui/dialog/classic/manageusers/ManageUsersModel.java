/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageusers;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseModel;
import org.oscm.ui.profile.FieldData;
import org.oscm.internal.usermanagement.POUser;

/**
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name="manageUsersModel")
public class ManageUsersModel extends BaseModel {

    private static final long serialVersionUID = 8679164543116335674L;

    private boolean initialized;
    private boolean locked;
    private String selectedUserId;
    private long key;
    private int version;
    private boolean resetPasswordButtonVisible;

    private List<POUser> users = new ArrayList<POUser>();
    private List<UserRole> roles = new ArrayList<UserRole>();

    private FieldData<String> salutation;
    private FieldData<String> locale;
    private FieldData<String> userId;
    private FieldData<String> firstName;
    private FieldData<String> lastName;
    private FieldData<String> email;

    public ManageUsersModel() {
        super();
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public List<POUser> getUsers() {
        return users;
    }

    public void setUsers(List<POUser> users) {
        this.users = users;
    }

    public FieldData<String> getSalutation() {
        return salutation;
    }

    public void setSalutation(FieldData<String> salutation) {
        this.salutation = salutation;
    }

    public FieldData<String> getLocale() {
        return locale;
    }

    public void setLocale(FieldData<String> locale) {
        this.locale = locale;
    }

    public FieldData<String> getUserId() {
        return userId;
    }

    public void setUserId(FieldData<String> userId) {
        this.userId = userId;
    }

    public FieldData<String> getFirstName() {
        return firstName;
    }

    public void setFirstName(FieldData<String> firstName) {
        this.firstName = firstName;
    }

    public FieldData<String> getLastName() {
        return lastName;
    }

    public void setLastName(FieldData<String> lastName) {
        this.lastName = lastName;
    }

    public FieldData<String> getEmail() {
        return email;
    }

    public void setEmail(FieldData<String> email) {
        this.email = email;
    }

    public void setSelectedUserId(String selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public String getSelectedUserId() {
        return selectedUserId;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
    }

    public List<UserRole> getRoles() {
        return roles;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setResetPasswordButtonVisible(boolean passwordButtonVisible) {
        this.resetPasswordButtonVisible = passwordButtonVisible;
    }

    public boolean isResetPasswordButtonVisible() {
        return resetPasswordButtonVisible;
    }

}
