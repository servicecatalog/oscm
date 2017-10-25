/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.util.HashSet;
import java.util.Set;

import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author weiser
 * 
 */
public class POUserDetails extends POUser {

    private static final long serialVersionUID = 8288191341824331262L;

    private Salutation salutation;
    private String locale;
    private UserAccountStatus status;

    private Set<UserRoleType> assignedRoles = new HashSet<UserRoleType>();
    private Set<UserRoleType> availableRoles = new HashSet<UserRoleType>();
    private Set<SettingType> mappedAttributes = new HashSet<SettingType>();

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

    public Set<UserRoleType> getAssignedRoles() {
        return assignedRoles;
    }

    public void setAssignedRoles(Set<UserRoleType> assignedRoles) {
        this.assignedRoles = assignedRoles;
    }

    public void setAvailableRoles(Set<UserRoleType> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public Set<UserRoleType> getAvailableRoles() {
        return availableRoles;
    }

    public void setMappedAttributes(Set<SettingType> mappedAttributes) {
        this.mappedAttributes = mappedAttributes;
    }

    public Set<SettingType> getMappedAttributes() {
        return mappedAttributes;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }

    public UserAccountStatus getStatus() {
        return status;
    }

}
