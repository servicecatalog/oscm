/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 07.09.2016
 *
 *******************************************************************************/

package org.oscm.tenant.dao;

import org.junit.Before;
import org.junit.Test;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Tenant;
import org.oscm.internal.types.exception.ObjectNotFoundException;

import static org.mockito.Mockito.*;

import javax.persistence.Query;

public class TenantDaoTest {
    
    private TenantDao tenantDao;
    private DataService dataManager;
    private Query query;
    
    @Before
    public void setup(){
        tenantDao = spy(new TenantDao());
        dataManager = mock(DataService.class);
        query = mock(Query.class);
        tenantDao.dataManager = dataManager;
        
        when(dataManager.createNamedQuery(anyString())).thenReturn(query);
    }
    
    @Test
    public void testGetAllTenants(){
        
        //given
        String namedQuery = "Tenant.getAll";

        //when
        tenantDao.getAllTenants();
        
        //then
        verify(dataManager, times(1)).createNamedQuery(namedQuery); 
    }
    
    @Test
    public void testGetTenantByTenantId() throws ObjectNotFoundException{
        
        //given
        String tenantId = "tenantId";
        
        //when
        tenantDao.getTenantByTenantId(tenantId);
        
        //then
        verify(dataManager, times(1)).getReferenceByBusinessKey(any(Tenant.class)); 
    }
    
    @Test
    public void testGetAllTenantSettingsForTenant() throws ObjectNotFoundException{
        
        //given
        String namedQuery = "TenantSetting.getAllForTenant";
        
        //when
        tenantDao.getAllTenantSettingsForTenant(new Tenant());
        
        //then
        verify(dataManager, times(1)).createNamedQuery(namedQuery); 
    }
    
    @Test
    public void testGetTenantsByIdPattern() throws ObjectNotFoundException{
        
        //given
        String namedQuery = "Tenant.getTenantsByIdPattern";
        
        //when
        tenantDao.getTenantsByIdPattern("tenantId");
        
        //then
        verify(dataManager, times(1)).createNamedQuery(namedQuery); 
    }
    
    @Test
    public void testFind() {
        
        //given
        long tenantId = 1000L;
        
        //when
        tenantDao.find(tenantId);
        
        //then
        verify(dataManager, times(1)).find(Tenant.class, tenantId); 
    }

    @Test
    public void testdoesOrganizationForTenantExist() {
        //given
        doReturn(1L).when(query).getSingleResult();

        //when
        tenantDao.doesOrganizationForTenantExist(new Tenant());

        //then
        verify(dataManager, times(1)).createNamedQuery("Tenant.checkOrganization");
    }

    @Test
    public void testdoesMarketplaceForTenantExist() {
        //given
        doReturn(1L).when(query).getSingleResult();

        //when
        tenantDao.doesMarketplaceAssignedToTenantExist(new Tenant());

        //then
        verify(dataManager, times(1)).createNamedQuery("Tenant.checkMarketplace");
    }
}
