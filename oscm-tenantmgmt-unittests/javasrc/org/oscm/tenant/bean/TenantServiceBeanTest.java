/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 30.08.2016
 *
 *******************************************************************************/
package org.oscm.tenant.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.domobjects.Tenant;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TenantDeletionConstraintException;
import org.oscm.internal.vo.VOTenant;
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
        doReturn(Boolean.TRUE).when(tenantServiceLocal).doesOrganizationAssignedToTenantExist(any(Tenant.class));
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
        doReturn(Boolean.FALSE).when(tenantServiceLocal).doesOrganizationAssignedToTenantExist(any(Tenant.class));
        doReturn(Boolean.TRUE).when(tenantServiceLocal).doesMarketplaceAssignedToTenantExist(any(Tenant.class));
        //when
        tenantServiceBean.removeTenant(voTenant);
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
      
        return tenant;
    }
    
}
