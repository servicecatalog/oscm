/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * An OrganizationRole represents a role that is supported by the system and
 * that an organization can have.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@BusinessKey(attributes = "roleName")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "roleName" }))
@NamedQueries({
        @NamedQuery(name = "OrganizationRole.findByBusinessKey", query = "SELECT orgRole FROM OrganizationRole orgRole WHERE orgRole.dataContainer.roleName = :roleName"),
        @NamedQuery(name = "OrganizationRole.findByOrganizationHistory", query = "SELECT orgRole FROM OrganizationToRoleHistory orh, OrganizationRole orgRole WHERE orh.organizationTKey = :organizationKey AND orh.modDate < :modDate AND orh.organizationRoleTKey = orgRole.key ORDER BY orh.objVersion DESC") })
public class OrganizationRole extends
        DomainObjectWithVersioning<OrganizationRoleData> {

    private static final long serialVersionUID = -4618365461702713052L;

    public OrganizationRole() {
        super();
        dataContainer = new OrganizationRoleData();
    }

    public OrganizationRole(OrganizationRoleType roleName) {
        this();
        setRoleName(roleName);
    }

    /**
     * Refer to {@link OrganizationRoleData#roleName}
     */
    public OrganizationRoleType getRoleName() {
        return dataContainer.getRoleName();
    }

    /**
     * Refer to {@link OrganizationRoleData#roleName}
     */
    public void setRoleName(OrganizationRoleType roleName) {
        dataContainer.setRoleName(roleName);
    }
}
