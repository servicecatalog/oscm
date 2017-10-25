/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: Paulina Badziak
 *
 *  Creation Date: 30.08.2016
 *
 *  Completion Time: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.domobjects;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.internal.types.enumtypes.IdpSettingType;

import javax.persistence.*;


@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
    "tenant_tkey", "name" }))
@NamedQueries({
    @NamedQuery(name = "TenantSetting.getAllForTenant", query = "SELECT ts FROM TenantSetting ts WHERE ts.tenant = "
        + ":tenant"),
    @NamedQuery(name = "TenantSetting.findByBusinessKey", query = "SELECT ts FROM TenantSetting ts WHERE ts.dataContainer"
        + ".name = :name AND ts.tenant = :tenant"),
    @NamedQuery(name = "TenantSetting.getAll", query = "SELECT ts FROM TenantSetting ts")})
@BusinessKey(attributes = { "tenant", "name" })
public class TenantSetting extends DomainObjectWithVersioning<TenantSettingData> {

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = -4272380804853076168L;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_tkey")
    private Tenant tenant;

    public TenantSetting() {
        dataContainer = new TenantSettingData();
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public IdpSettingType getName() {
        return dataContainer.getName();
    }

    public String getValue() {
        return dataContainer.getValue();
    }
}
