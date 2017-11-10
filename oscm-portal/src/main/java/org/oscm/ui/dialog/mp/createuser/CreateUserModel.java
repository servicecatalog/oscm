/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.createuser;

import org.oscm.ui.beans.BaseModel;
import org.oscm.ui.dialog.classic.manageusers.UserRole;
import org.oscm.ui.profile.FieldData;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * @author weiser
 * 
 */
@ManagedBean
@ViewScoped
public class CreateUserModel extends BaseModel {

    private static final long serialVersionUID = -1295546968971087022L;
    public static final String ATTRIBUTE_USER_ID = "userId";

    private FieldData<String> salutation;
    private FieldData<String> locale;
    private FieldData<String> userId;
    private FieldData<String> firstName;
    private FieldData<String> lastName;
    private FieldData<String> email;

    private List<UserRole> roles = new ArrayList<>();
    private List<Subscription> subscriptions = new ArrayList<>();
    private List<UserGroup> userGroups = new ArrayList<>();

    public CreateUserModel() {
        super();
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

    public List<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<UserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

}
