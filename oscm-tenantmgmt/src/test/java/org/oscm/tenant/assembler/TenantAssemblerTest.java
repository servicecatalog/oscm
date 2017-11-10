/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.assembler;

import org.junit.Test;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.TenantSetting;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOTenantSetting;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import static org.oscm.internal.types.enumtypes.IdpSettingType.SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD;

public class TenantAssemblerTest {

    @Test
    public void testToVOTenant() {
        //given
        Tenant tenant = prepareTenant();
        addTenantSettings(tenant);

        //when
        VOTenant voTenant = TenantAssembler.toVOTenant(tenant);

        //then
        assertEquals(voTenant.getTenantId(), tenant.getTenantId());
        assertEquals(voTenant.getDescription(), tenant.getDataContainer().getDescription());
        assertEquals(voTenant.getKey(), tenant.getKey());
        assertEquals(voTenant.getTenantSettings().size(), 1);
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

    @Test
    public void testToTenantSetting() {
        //given
        VOTenantSetting voTenantSetting = prepareVOTenantSetting();

        //when
        TenantSetting tenantSetting = TenantAssembler.toTenantSetting(voTenantSetting);

        //then
        assertEquals(voTenantSetting.getName(), tenantSetting.getName());
        assertEquals(voTenantSetting.getValue(), tenantSetting.getValue());
        assertEquals(voTenantSetting.getVoTenant().getKey(), tenantSetting.getTenant().getKey());
    }

    @Test
    public void testToVOTenantSetting() {
        //given
        TenantSetting tenantSetting = prepareTenantSetting();

        //when
        VOTenantSetting voTenantSetting = TenantAssembler.toVOTenantSetting(tenantSetting);

        //then
        assertEquals(voTenantSetting.getName(), tenantSetting.getName());
        assertEquals(voTenantSetting.getValue(), tenantSetting.getValue());
    }

    private Tenant prepareTenant() {
        Tenant tenant = new Tenant();
        tenant.setKey(1L);
        tenant.setTenantId("tenantId");
        tenant.getDataContainer().setDescription("desc");
        return tenant;
    }

    private void addTenantSettings(Tenant tenant) {
        List<TenantSetting> settings = new ArrayList<>();
        TenantSetting ts = new TenantSetting();
        ts.setTenant(tenant);
        ts.setKey(1L);
        ts.getDataContainer().setName(SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD);
        ts.getDataContainer().setValue("value");
        settings.add(ts);
        tenant.setTenantSettings(settings);
    }

    private VOTenant prepareVOTenant() {
        VOTenant voTenant = new VOTenant();
        voTenant.setKey(1L);
        voTenant.setVersion(0);
        voTenant.setTenantId("new tenantId");
        voTenant.setDescription("new description");
        return voTenant;
    }

    private VOTenantSetting prepareVOTenantSetting() {
        VOTenantSetting voTenantSetting = new VOTenantSetting();
        voTenantSetting.setName(SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD);
        voTenantSetting.setValue("value");
        voTenantSetting.setKey(1L);
        voTenantSetting.setVersion(0);
        voTenantSetting.setVoTenant(prepareVOTenant());
        return voTenantSetting;
    }

    private TenantSetting prepareTenantSetting() {
        TenantSetting tenantSetting = new TenantSetting();
        tenantSetting.setKey(1L);
        tenantSetting.setTenant(prepareTenant());
        tenantSetting.getDataContainer().setName(SSO_IDP_AUTHENTICATION_REQUEST_HTTP_METHOD);
        tenantSetting.getDataContainer().setValue("value");
        return tenantSetting;
    }
}
