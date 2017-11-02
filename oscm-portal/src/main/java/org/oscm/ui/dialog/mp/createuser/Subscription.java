/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 25.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.createuser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

/**
 * @author weiser
 * 
 */
public class Subscription implements Serializable {

    private static final long serialVersionUID = 7099123835074846199L;

    private boolean selected;
    private String id;
    private String selectedRole;
    private boolean rolesRendered;
    private long licKey;
    private int licVersion;

    private List<SelectItem> roles = new ArrayList<SelectItem>();

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }

    public boolean isRolesRendered() {
        return rolesRendered;
    }

    public void setRolesRendered(boolean renderRoles) {
        this.rolesRendered = renderRoles;
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void setRoles(List<SelectItem> roles) {
        this.roles = roles;
    }

    public long getLicKey() {
        return licKey;
    }

    public void setLicKey(long licKey) {
        this.licKey = licKey;
    }

    public int getLicVersion() {
        return licVersion;
    }

    public void setLicVersion(int licVersion) {
        this.licVersion = licVersion;
    }
    
    @Override
    public String toString() {
        return "Subscription [selected=" + selected + ", id=" + id
                + ", selectedRole=" + selectedRole + ", rolesRendered="
                + rolesRendered + "]";
    }
}
