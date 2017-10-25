/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 24.07.2010                                                     
 *                                                                              
 *******************************************************************************/
package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.UnitRoleType;

/**
 * Entity represents the unituserrole table
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "roleName" }))
@NamedQueries({ 
    @NamedQuery(name = "UnitUserRole.findByBusinessKey", query = "select c from UnitUserRole c where c.dataContainer.roleName=:roleName"),
    @NamedQuery(name = "UnitUserRole.findAllRoles", query = "select c from UnitUserRole c ORDER BY c.dataContainer.roleName")})
@BusinessKey(attributes = { "roleName" })
public class UnitUserRole extends DomainObjectWithVersioning<UnitUserRoleData> {

    private static final long serialVersionUID = 5802708119472761472L;

    public UnitUserRole() {
        super();
        dataContainer = new UnitUserRoleData();
    }

    public UnitRoleType getRoleName() {
        return dataContainer.getRoleName();
    }

    public void setRoleName(UnitRoleType roleName) {
        dataContainer.setRoleName(roleName);
    }
}
