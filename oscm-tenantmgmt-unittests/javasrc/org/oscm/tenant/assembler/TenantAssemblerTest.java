/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.assembler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.oscm.domobjects.Tenant;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOTenant;

public class TenantAssemblerTest {

    @Test
    public void testToVOTenant() {
        //given
        Tenant tenant = prepareTenant();
       
        //when
        VOTenant voTenant = TenantAssembler.toVOTenant(tenant);

        //then
        assertEquals(voTenant.getTenantId(), tenant.getTenantId());
        assertEquals(voTenant.getDescription(), tenant.getDataContainer().getDescription());
        assertEquals(voTenant.getKey(), tenant.getKey());
    }

    @Test
    public void testUpdateTenantData() throws ConcurrentModificationException {
        //given
        VOTenant voTenant = prepareVOTenant();
        Tenant tenant = prepareTenant();

        //when
        TenantAssembler.updateTenantData(voTenant, tenant);

        //then
        assertEquals(voTenant.getTenantId(), tenant.getTenantId());
        assertEquals(voTenant.getDescription(), tenant.getDataContainer().getDescription());
    }

    @Test
    public void testToTenant() {
        //given
        VOTenant voTenant = prepareVOTenant();

        //when
        Tenant tenant = TenantAssembler.toTenant(voTenant);

        //then
        assertEquals(voTenant.getTenantId(), tenant.getTenantId());
        assertEquals(voTenant.getDescription(), tenant.getDataContainer().getDescription());
    }
    
    private Tenant prepareTenant() {
        Tenant tenant = new Tenant();
        tenant.setKey(1L);
        tenant.setTenantId("tenantId");
        tenant.getDataContainer().setDescription("desc");
        return tenant;
    }

    private VOTenant prepareVOTenant() {
        VOTenant voTenant = new VOTenant();
        voTenant.setKey(1L);
        voTenant.setVersion(0);
        voTenant.setTenantId("new tenantId");
        voTenant.setDescription("new description");
        return voTenant;
    }
}
