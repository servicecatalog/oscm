/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 29.07.15 15:01
 *
 *******************************************************************************/
package org.oscm.internal.usergroupmgmt;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.enumtypes.UnitRoleType;

public class POUnitUserRole extends BasePO {

    private static final long serialVersionUID = 1337174480227855194L;

    private UnitRoleType roleName;
    private String roleNameToDisplay;

    public UnitRoleType getRoleName() {
        return roleName;
    }

    public void setRoleName(UnitRoleType roleName) {
        this.roleName = roleName;
    }

    public String getRoleNameToDisplay() {
        return roleNameToDisplay;
    }

    public void setRoleNameToDisplay(String roleNameToDisplay) {
        this.roleNameToDisplay = roleNameToDisplay;
    }

}
