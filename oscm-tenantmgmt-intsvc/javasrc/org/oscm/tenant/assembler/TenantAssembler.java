/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.assembler;

import org.oscm.domobjects.Tenant;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.vo.BaseAssembler;

public class TenantAssembler extends BaseAssembler {

    public static VOTenant toVOTenant(Tenant tenant) {
        if (tenant == null) {
            return null;
        }
        VOTenant voTenant = new VOTenant();
        voTenant.setKey(tenant.getKey());
        voTenant.setVersion(tenant.getVersion());
        voTenant.setTenantId(tenant.getDataContainer().getTenantId());
        voTenant.setDescription(tenant.getDataContainer().getDescription());
        voTenant.setName(tenant.getDataContainer().getName());
        return voTenant;
    }

    public static Tenant updateTenantData(VOTenant voTenant, Tenant tenant) throws ConcurrentModificationException {
        verifyVersionAndKey(tenant, voTenant);
        fillTenantData(tenant, voTenant);
        return tenant;
    }

    public static Tenant toTenant(VOTenant voTenant) {
        Tenant tenant = new Tenant();
        fillTenantData(tenant, voTenant);
        tenant.setKey(voTenant.getKey());
        return tenant;
    }

    private static void fillTenantData(Tenant tenant, VOTenant voTenant) {
        tenant.getDataContainer().setTenantId(voTenant.getTenantId());
        tenant.getDataContainer().setName(voTenant.getName());
        tenant.getDataContainer().setDescription(voTenant.getDescription());
    }
}
