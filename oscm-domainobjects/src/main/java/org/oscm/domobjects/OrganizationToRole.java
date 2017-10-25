/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.08.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Explicitly models the n:m reference from an organization to the organization
 * roles.
 * 
 * <p>
 * The default JPA managed approach to realize an n:m relation creates a join
 * table, but does not take care of historization of the objects. But
 * historization is essential for our purposes. There is another advantage in
 * doing so: we can enhance the relation by additional information in the
 * future.
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "ORGANIZATION_TKEY", "ORGANIZATIONROLE_TKEY" }))
@NamedQueries({ @NamedQuery(name = "OrganizationToRole.getByOrganizationAndRole", query = "SELECT otr FROM OrganizationToRole otr WHERE otr.organization.key = :orgTKey AND otr.organizationRole.key = :orgRoleTKey") })
public class OrganizationToRole extends

DomainObjectWithHistoryAndEmptyDataContainer {

    private static final long serialVersionUID = -950764851130197353L;

    /**
     * The organization role this object refers to.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationRole organizationRole;

    /**
     * The organization this object refers to.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Organization organization;

    public OrganizationRole getOrganizationRole() {
        return organizationRole;
    }

    public void setOrganizationRole(OrganizationRole organizationRole) {
        this.organizationRole = organizationRole;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

}
