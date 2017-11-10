/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.TenantSetting;
import org.oscm.internal.types.enumtypes.IdpSettingType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TenantDeletionConstraintException;
import org.oscm.internal.vo.VOTenant;
import org.oscm.internal.vo.VOTenantSetting;
import org.oscm.tenant.assembler.TenantAssembler;
import org.oscm.tenant.local.TenantServiceLocal;

public class TenantServiceBeanTest {

    private TenantServiceLocal tenantServiceLocal;
    private TenantServiceBean tenantServiceBean;

    @Before
    public void setup() {
        tenantServiceLocal = mock(TenantServiceLocal.class);
        tenantServiceBean = spy(new TenantServiceBean());
        tenantServiceBean.setTenantServiceLocal(tenantServiceLocal);
    }

    @Test
    public void testGetTenants() {
        //given
        List<Tenant> tenants = new ArrayList<>();
        tenants.add(prepareTenant());
        when(tenantServiceLocal.getAllTenants()).thenReturn(tenants);

        //when
        List<VOTenant> voTenants = tenantServiceBean.getTenants();

        //then
        assertEquals(voTenants.size(), 1);
        assertEquals(voTenants.get(0).getTenantId(), tenants.get(0).getTenantId());
    }

    @Test
    public void testGetTenantByTenantId() throws ObjectNotFoundException {
        //given
        when(tenantServiceLocal.getTenantByTenantId(anyString())).thenReturn(prepareTenant());

        //when
        VOTenant voTenant = tenantServiceBean.getTenantByTenantId("tenant Id");

        //then
        assertEquals(voTenant.getTenantId(), "tenant Id");
    }

    @Test
    public void testAddTenant() throws NonUniqueBusinessKeyException {
        //given
        VOTenant voTenant = TenantAssembler.toVOTenant(prepareTenant());
        doNothing().when(tenantServiceLocal).saveTenant(any(Tenant.class));

        //when
        tenantServiceBean.addTenant(voTenant);

        //then
        verify(tenantServiceLocal, times(1)).saveTenant(any(Tenant.class));
    }

    @Test
    public void testUpdateTenant()
        throws NonUniqueBusinessKeyException, ConcurrentModificationException, ObjectNotFoundException {
        //given
        VOTenant voTenant = TenantAssembler.toVOTenant(prepareTenant());
        doNothing().when(tenantServiceLocal).saveTenant(any(Tenant.class));
        when(tenantServiceLocal.getTenantByKey(anyLong())).thenReturn(prepareTenant());

        //when
        tenantServiceBean.updateTenant(voTenant);

        //then
        verify(tenantServiceLocal, times(1)).saveTenant(any(Tenant.class));
    }

    @Test
    public void testRemoveTenant() throws ObjectNotFoundException, TenantDeletionConstraintException {
        //given
        VOTenant voTenant = TenantAssembler.toVOTenant(prepareTenant());
        when(tenantServiceLocal.getTenantByKey(anyLong())).thenReturn(prepareTenant());
        doNothing().when(tenantServiceLocal).removeTenant(any(Tenant.class));

        //when
        tenantServiceBean.removeTenant(voTenant);

        //then
        verify(tenantServiceLocal, times(1)).removeTenant(any(Tenant.class));
    }

    @Test(expected = TenantDeletionConstraintException.class)
    public void testRemoveTenant_exceptionExpected() throws ObjectNotFoundException, TenantDeletionConstraintException {
        //given
        VOTenant voTenant = TenantAssembler.toVOTenant(prepareTenant());
        when(tenantServiceLocal.getTenantByKey(anyLong())).thenReturn(prepareTenant());
        doNothing().when(tenantServiceLocal).removeTenant(any(Tenant.class));
        doReturn(true).when(tenantServiceLocal).doesOrganizationAssignedToTenantExist(any(Tenant.class));
        //when
        tenantServiceBean.removeTenant(voTenant);
    }

    @Test(expected = TenantDeletionConstraintException.class)
    public void testRemoveTenant_exceptionExpectedMarketplace() throws ObjectNotFoundException,
        TenantDeletionConstraintException {
        //given
        VOTenant voTenant = TenantAssembler.toVOTenant(prepareTenant());
        when(tenantServiceLocal.getTenantByKey(anyLong())).thenReturn(prepareTenant());
        doNothing().when(tenantServiceLocal).removeTenant(any(Tenant.class));
        doReturn(false).when(tenantServiceLocal).doesOrganizationAssignedToTenantExist(any(Tenant.class));
        doReturn(true).when(tenantServiceLocal).doesMarketplaceAssignedToTenantExist(any(Tenant.class));
        //when
        tenantServiceBean.removeTenant(voTenant);
    }

    @Test
    public void testAddTenantSettings() throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        //given
        VOTenant voTenant = TenantAssembler.toVOTenant(prepareTenant());
        doNothing().when(tenantServiceBean).removeTenantSettings(anyLong());
        doNothing().when(tenantServiceLocal).saveTenantSetting(any(TenantSetting.class));

        //when
        tenantServiceBean.addTenantSettings(prepareVOTenantSettings(), voTenant);

        //then
        verify(tenantServiceLocal, times(1)).saveTenantSetting(any(TenantSetting.class));
    }

    @Test
    public void testRemoveTenantIdpProperties() throws ObjectNotFoundException {
        //given
        List<TenantSetting> tenantSettings = prepareTenantSettings();
        when(tenantServiceLocal.getAllTenantSettingsForTenant(any(Tenant.class))).thenReturn(tenantSettings);
        doNothing().when(tenantServiceLocal).removeTenantSetting(any(TenantSetting.class));

        //when
        tenantServiceBean.removeTenantSettings(1L);

        //then
        verify(tenantServiceLocal, times(1)).removeTenantSetting(any(TenantSetting.class));
    }

    @Test
    public void testRemoveTenantIdpProperties_noProperties() throws ObjectNotFoundException {
        //given
        when(tenantServiceLocal.getAllTenantSettingsForTenant(any(Tenant.class))).thenReturn(new ArrayList<TenantSetting>());
        doNothing().when(tenantServiceLocal).removeTenantSetting(any(TenantSetting.class));

        //when
        tenantServiceBean.removeTenantSettings(1L);

        //then
        verify(tenantServiceLocal, times(0)).removeTenantSetting(any(TenantSetting.class));
    }

    @Test
    public void testGetSettingsForTenant() {
        //given
        when(tenantServiceLocal.getAllTenantSettingsForTenant(any(Tenant.class))).thenReturn(prepareTenantSettings());

        //when
        List<VOTenantSetting> voTenantsettings = tenantServiceBean.getSettingsForTenant(1L);

        //then
        assertEquals(voTenantsettings.size(), 1);
    }
    
    @Test
    public void testTenantByIdPattern() {
        
        //given
        ArrayList<Tenant> tenants = new ArrayList<Tenant>();
        tenants.add(prepareTenant());
        when(tenantServiceLocal.getTenantsByIdPattern(anyString())).thenReturn(tenants);

        //when
        List<VOTenant> voTenants = tenantServiceBean.getTenantsByIdPattern("tenant Id");

        //then
        assertEquals(voTenants.size(), 1);
        assertEquals("tenant Id", tenants.get(0).getTenantId());
    }

    private Tenant prepareTenant() {
        Tenant tenant = new Tenant();
        tenant.setKey(1L);
        tenant.setTenantId("tenant Id");
        tenant.getDataContainer().setDescription("description");
        tenant.setTenantSettings(new ArrayList<TenantSetting>());
        return tenant;
    }

    private List<VOTenantSetting> prepareVOTenantSettings() {
        List<VOTenantSetting> settings = new ArrayList<>();
        VOTenantSetting voTenantSetting = new VOTenantSetting();
        voTenantSetting.setKey(1L);
        voTenantSetting.setName(IdpSettingType.SSO_IDP_URL);
        voTenantSetting.setValue("value");
        voTenantSetting.setVoTenant(TenantAssembler.toVOTenant(prepareTenant()));
        settings.add(voTenantSetting);
        return settings;
    }

    private List<TenantSetting> prepareTenantSettings() {
        List<TenantSetting> tenantSettings = new ArrayList<>();
        TenantSetting tenantSetting = new TenantSetting();
        tenantSetting.setKey(1L);
        tenantSetting.setTenant(prepareTenant());
        tenantSetting.getDataContainer().setName(IdpSettingType.SSO_IDP_URL);
        tenantSetting.getDataContainer().setValue("value");
        tenantSettings.add(tenantSetting);
        return tenantSettings;
    }
}
