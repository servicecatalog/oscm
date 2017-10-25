/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                 
 *                                                                              
 *  Creation Date: 25.06.2010                                                     
 *                                                                              
 *  Completion Time: 25.05.2010                                            
 *                                                                              
 *******************************************************************************/
package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RoleDefinitionData extends DomainDataContainer {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = -383159375419596624L;

    /**
     * ID for role.
     */
    @Column(nullable = false)
    private String roleId;

    /**
     * Setter for role ID.
     * 
     * @param roleId
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Getter for role ID.
     * 
     * @return
     */
    public String getRoleId() {
        return roleId;
    }

}
