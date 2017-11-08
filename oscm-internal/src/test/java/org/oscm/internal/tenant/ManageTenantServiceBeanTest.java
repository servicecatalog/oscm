/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 07.09.2016
 *
 *******************************************************************************/

package org.oscm.internal.tenant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.vo.VOTenant;
import org.oscm.tenant.bean.TenantServiceBean;

public class ManageTenantServiceBeanTest {

    private TenantServiceBean tenantService;
    private ManageTenantServiceBean manageTenantService;

    @Before
    public void setup() {

        manageTenantService = spy(new ManageTenantServiceBean());
        tenantService = mock(TenantServiceBean.class);
        manageTenantService.tenantService = tenantService;
    }

    @Test
    public void testGetTenantsByIdPattern() {

        // given
        String tenantIdPattern = "tenantId";
        when(tenantService.getTenantsByIdPattern(tenantIdPattern))
                .thenReturn(prepareTenants());

        // when
        List<POTenant> tenants = manageTenantService
                .getTenantsByIdPattern(tenantIdPattern);

        // then
        verify(tenantService, times(1)).getTenantsByIdPattern(tenantIdPattern);
        assertEquals(1000, tenants.get(0).getKey());
        assertEquals("tenantId", tenants.get(0).getTenantId());
        assertEquals(2000, tenants.get(1).getKey());
        assertEquals("tenantId2", tenants.get(1).getTenantId());
    }

    @Test
    public void testGetAllTenants() {

        // given
        when(tenantService.getTenants()).thenReturn(prepareTenants());

        // when
        List<POTenant> tenants = manageTenantService.getAllTenants();

        // then
        verify(tenantService, times(1)).getTenants();
        assertEquals(1000, tenants.get(0).getKey());
        assertEquals("tenantId", tenants.get(0).getTenantId());
        assertEquals(2000, tenants.get(1).getKey());
        assertEquals("tenantId2", tenants.get(1).getTenantId());
    }

    private List<VOTenant> prepareTenants() {

        ArrayList<VOTenant> tenants = new ArrayList<VOTenant>();

        VOTenant tenant = new VOTenant();
        tenant.setKey(1000);
        tenant.setTenantId("tenantId");

        VOTenant tenant2 = new VOTenant();
        tenant2.setKey(2000);
        tenant2.setTenantId("tenantId2");

        tenants.add(tenant);
        tenants.add(tenant2);

        return tenants;
    }
}
