/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 18.11.15 16:44
 *
 *******************************************************************************/
package org.oscm.paginator;

import java.util.HashMap;
import java.util.Map;

import org.oscm.internal.types.enumtypes.UnitRoleType;

public class PaginationUsersInUnit extends Pagination {

    private static final long serialVersionUID = 5100265970623358507L;

    private Map<UnitRoleType, String> localizedRolesMap = new HashMap<UnitRoleType, String>();

    private Map<String, String> changedRoles = new HashMap<String, String>();
    
    private Map<String, Boolean> selectedUsersIds = new HashMap<String, Boolean>();

    public PaginationUsersInUnit() {
    }

    public PaginationUsersInUnit(int offset, int limit) {
        super(offset, limit);
    }

    public Map<UnitRoleType, String> getLocalizedRolesMap() {
        return localizedRolesMap;
    }

    public void setLocalizedRolesMap(Map<UnitRoleType, String> localizedRolesMap) {
        this.localizedRolesMap = localizedRolesMap;
    }

    public Map<String, String> getChangedRoles() {
        return changedRoles;
    }

    public void setChangedRoles(Map<String, String> changedRoles) {
        this.changedRoles = changedRoles;
    }

    public Map<String, Boolean> getSelectedUsersIds() {
        return selectedUsersIds;
    }

    public void setSelectedUsersIds(Map<String, Boolean> selectedUsersIds) {
        this.selectedUsersIds = selectedUsersIds;
    }

}
