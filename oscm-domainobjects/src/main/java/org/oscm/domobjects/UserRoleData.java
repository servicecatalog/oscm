/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                   
 *                                                                              
 *  Creation Date: 29.04.2011                                                      
 *                                                                              
 *  Completion Time: 29.04.2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.PVTConverter;
import org.oscm.domobjects.converters.URTConverter;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * Data container for the user role domain object.
 * 
 * @author cheld
 * 
 */
@Embeddable
public class UserRoleData extends DomainDataContainer {

    private static final long serialVersionUID = 1037056324857441604L;

    /**
     * Stores the string representation of the current role.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private UserRoleType roleName;

    /**
     * Returns the role name as enumeration.
     * 
     * @return UserRoleType
     */
    public UserRoleType getRoleName() {
        return roleName;
    }

    /**
     * Sets the role name as enumeration
     * 
     * @param roleName
     *            Enumeration of the role name to be set
     */
    public void setRoleName(UserRoleType roleName) {
        this.roleName = roleName;
    }

}
