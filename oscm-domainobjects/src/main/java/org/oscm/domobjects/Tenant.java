/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: badziakp
 *
 *  Creation Date: 29.08.2016
 *
 *  Completion Time:
 *
 *******************************************************************************/
package org.oscm.domobjects;

import org.oscm.domobjects.annotations.BusinessKey;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "tenantId" }))
@NamedQueries({

    @NamedQuery(name = "Tenant.getAll", query = "SELECT t FROM Tenant t"),
    @NamedQuery(name = "Tenant.findByBusinessKey", query = "SELECT t FROM Tenant t WHERE t.dataContainer.tenantId = "
        + ":tenantId"),
    @NamedQuery(name = "Tenant.getTenantsByIdPattern", query = "SELECT t FROM Tenant t WHERE t.dataContainer.tenantId"
        + " LIKE :tenantIdPattern"),
    @NamedQuery(name = "Tenant.checkOrganization", query = "SELECT count(o) FROM Organization o WHERE o.tenant = "
        + ":tenant"),
    @NamedQuery(name = "Tenant.checkMarketplace", query = "SELECT count(o) FROM Marketplace o WHERE o.tenant = "
        + ":tenant")})
@BusinessKey(attributes = { "tenantId" })
public class Tenant extends DomainObjectWithVersioning<TenantData> {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = 7294477200910856344L;

    public Tenant() {
        super();
        dataContainer = new TenantData();
    }

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private Collection<Organization> organizations;

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    private Collection<Marketplace> marketplaces;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Collection<TenantSetting> tenantSettings;

    public Collection<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Collection<Organization> organizations) {
        this.organizations = organizations;
    }

    public Collection<Marketplace> getMarketplaces() {
        return marketplaces;
    }

    public void setMarketplaces(Collection<Marketplace> marketplaces) {
        this.marketplaces = marketplaces;
    }

    public Collection<TenantSetting> getTenantSettings() {
        return tenantSettings;
    }

    public void setTenantSettings(Collection<TenantSetting> tenantSettings) {
        this.tenantSettings = tenantSettings;
    }
    
    public void setTenantId(String tenantId) {
        dataContainer.setTenantId(tenantId);
    }
    
    public String getTenantId() {
        return dataContainer.getTenantId();
    }
}
