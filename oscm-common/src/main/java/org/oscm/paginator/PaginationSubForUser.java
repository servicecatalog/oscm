/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paginator;

import java.util.HashMap;
import java.util.Map;

public class PaginationSubForUser extends Pagination {

    /**
     * 
     */
    private static final long serialVersionUID = 4319531062720949097L;

    private Map<String, String> changedRoles = new HashMap<String, String>();

    private Map<String, Boolean> selectedUsersIds = new HashMap<String, Boolean>();

    public PaginationSubForUser() {
    }

    public PaginationSubForUser(int offset, int limit) {
        super(offset, limit);
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
