/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 29.08.2016
 *
 *******************************************************************************/
package org.oscm.internal.tenant;

import org.oscm.internal.base.BasePO;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.vo.VOTenant;

public class POTenant extends BasePO {
    
    private String name;
    private String description;
    private String tenantId;
    private String idp;

    public POTenant() {

    }

    public POTenant(VOTenant voTenant) {
        this.tenantId = voTenant.getTenantId();
        this.name = voTenant.getName();
        this.description = voTenant.getDescription();
        this.idp = voTenant.getTenantSettings().get(IdpSettingType.SSO_IDP_URL);
        this.setKey(voTenant.getKey());
        this.setVersion(voTenant.getVersion());
    }
    public VOTenant toVOTenanat() {
        VOTenant voTenant = new VOTenant();
        voTenant.setKey(this.getKey());
        voTenant.setVersion(this.getVersion());
        voTenant.setTenantId(this.getTenantId());
        voTenant.setName(this.getName());
        voTenant.setDescription(this.getDescription());
        return voTenant;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdp() {
        return idp;
    }

    public void setIdp(String idp) {
        this.idp = idp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
