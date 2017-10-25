/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.*;

import org.oscm.domobjects.converters.ORTConverter;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Data container for the organization role domain object.
 * 
 * <p>
 * It stores the role name as string, the value must match one of the elements
 * of the enum {@link OrganizationRoleType}. Please note that, if supporting a
 * more generic role handling in the future, this type must be changed to a
 * plain string and the matching against an enum or a user defined string must
 * be done outside.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@Embeddable
public class OrganizationRoleData extends DomainDataContainer {

    private static final long serialVersionUID = -4250017210106141188L;

    /**
     * Stores the string representation of the current role.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private OrganizationRoleType roleName;

    public OrganizationRoleType getRoleName() {
        return roleName;
    }

    public void setRoleName(OrganizationRoleType roleName) {
        this.roleName = roleName;
    }

}
