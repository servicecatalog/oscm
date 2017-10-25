/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

/**
 * History object to the organization to role object.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@NamedQuery(name = "OrganizationToRoleHistory.findByObject", query = "select c from OrganizationToRoleHistory c where c.objKey=:objKey order by objversion")
public class OrganizationToRoleHistory extends
        DomainHistoryObjectWithEmptyDataContainer {

    private static final long serialVersionUID = 3926514336331405565L;

    /**
     * Reference to the target organization (key-only).
     */
    private long organizationTKey;

    /**
     * Reference to the target organization role (key-only).
     */
    private long organizationRoleTKey;

    public OrganizationToRoleHistory() {

    }

    public OrganizationToRoleHistory(OrganizationToRole orgToRole) {
        super(orgToRole);
        if (orgToRole.getOrganization() != null) {
            this.organizationTKey = orgToRole.getOrganization().getKey();
        }
        if (orgToRole.getOrganizationRole() != null) {
            this.organizationRoleTKey = orgToRole.getOrganizationRole()
                    .getKey();
        }
    }

    public long getOrganizationTKey() {
        return organizationTKey;
    }

    public long getOrganizationRoleTKey() {
        return organizationRoleTKey;
    }

}
