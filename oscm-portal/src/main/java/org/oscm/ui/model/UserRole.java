/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * A UI model row object for register/manage user role.
 * 
 */
public class UserRole {
    private boolean selected;
    private UserRoleType userRoleType;
    private String displayName;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public UserRoleType getUserRoleType() {
        return userRoleType;
    }

    public void setUserRoleType(UserRoleType userRoleType) {
        this.userRoleType = userRoleType;
        localizeRoleType();
    }

    public String getDisplayName() {
        return displayName;
    }

    private void localizeRoleType() {
        displayName = JSFUtils.getText(
                "UserRoleType." + userRoleType + ".enum", null);
    }
}
