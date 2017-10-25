/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.io.Serializable;
import java.util.List;

import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserRoleType;

public class POUserInUnit implements Serializable {

    private static final long serialVersionUID = 6037969626330082505L;
    private boolean selected;
    private POUser poUser;
    private String roleInUnit;
    private Salutation salutation;
    private String locale;
    private List<UserRoleType> assignedRoles;
    private String tenantId;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public POUser getPoUser() {
        return poUser;
    }

    public void setPoUser(POUser poUser) {
        this.poUser = poUser;
    }

    public String getUserId() {
        return poUser.getUserId();
    }

    public String getFirstName() {
        return poUser.getFirstName();
    }

    public String getLastName() {
        return poUser.getLastName();
    }

    public String getRoleInUnit() {
        return roleInUnit;
    }

    public void setRoleInUnit(String roleInUnit) {
        this.roleInUnit = roleInUnit;
    }

    public Salutation getSalutation() {
        return salutation;
    }

    public void setSalutation(Salutation salutation) {
        this.salutation = salutation;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<UserRoleType> getAssignedRoles() {
        return assignedRoles;
    }

    public void setAssignedRoles(List<UserRoleType> assignedRoles) {
        this.assignedRoles = assignedRoles;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
