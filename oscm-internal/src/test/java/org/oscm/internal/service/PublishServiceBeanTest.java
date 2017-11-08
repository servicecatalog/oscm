/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.service;

/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) 2012 FUJITSU Limited - ALL RIGHTS RESERVED.                  
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.pricing.POServiceForPricing;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.resalepermissions.POServiceDetails;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;

public class PublishServiceBeanTest {

    private PublishServiceBean bean;
    private PlatformUser user;

    @Before
    public void setup() throws Exception {
        bean = spy(new PublishServiceBean());
        bean.cs = mock(CategorizationService.class);
        bean.ms = mock(MarketplaceService.class);
        bean.sps = mock(ServiceProvisioningService.class);
        bean.ps = mock(PricingService.class);
        bean.spsl = mock(ServiceProvisioningServiceLocal.class);
        bean.ds = mock(DataService.class);
        bean.partnerSrvProv = mock(ServiceProvisioningPartnerServiceLocal.class);
        bean.msl = mock(MarketplaceServiceLocal.class);
        bean.sessionCtx = mock(SessionContext.class);

        when(bean.sps.getServiceDetails(any(VOService.class))).thenReturn(
                new VOServiceDetails());
        when(bean.ms.getMarketplacesForService(any(VOService.class)))
                .thenReturn(new ArrayList<VOCatalogEntry>());
        when(
                bean.ms.publishService(any(VOService.class),
                        anyListOf(VOCatalogEntry.class))).thenReturn(
                new VOServiceDetails());
        when(bean.cs.getCategories(anyString(), anyString())).thenReturn(
                new ArrayList<VOCategory>());
        when(
                bean.ps.getPartnerRevenueShareForService(any(POServiceForPricing.class)))
                .thenReturn(new Response(new POPartnerPriceModel()));
        when(bean.ps.getMarketplaceRevenueShares(anyString())).thenReturn(
                new Response(new POMarketplacePriceModel()));
        when(bean.ps.getPartnerRevenueSharesForMarketplace(anyString()))
                .thenReturn(new Response(new POPartnerPriceModel()));
        when(bean.ps.getOperatorRevenueShare(anyLong())).thenReturn(
                new Response(new POOperatorPriceModel()));
        Query mock = mock(Query.class);
        when(bean.ds.createNativeQuery(anyString())).thenReturn(mock);
        OrganizationDao orgMock = new OrganizationDao(bean.ds);
        bean.setOrganizationDao(orgMock);
        when(mock.getResultList()).thenReturn(generateOrganizations());
        when(bean.ds.getReference(Product.class, 0)).thenReturn(new Product());

        user = new PlatformUser();
        user.setAssignedRoles(givenRoleAssignment(UserRoleType.SERVICE_MANAGER));
        doReturn(user).when(bean.ds).getCurrentUser();
    }

    private Set<RoleAssignment> givenRoleAssignment(UserRoleType... roles) {
        Set<RoleAssignment> ras = new HashSet<RoleAssignment>();
        for (UserRoleType role : roles) {
            RoleAssignment ra = new RoleAssignment();
            UserRole ur = new UserRole();
            ur.setRoleName(role);
            ra.setRole(ur);
            ras.add(ra);
        }
        return ras;
    }

    @Test
    public void getServiceDetails() throws Exception {
        // when
        Response response = bean.getServiceDetails(3);

        // then
        assertNotNull(response.getResult(POServiceForPublish.class));
        assertNotNull(response.getResult(POPartnerPriceModel.class));
        assertFalse(response.getResult(POServiceForPublish.class)
                .isPartOfUpgradePath());
    }

    @Test
    public void getServiceDetails_PartOfUpgradePath() throws Exception {
        // given
        when(Boolean.valueOf(bean.spsl.isPartOfUpgradePath(anyLong())))
                .thenReturn(Boolean.TRUE);

        // when
        Response response = bean.getServiceDetails(3);

        // then
        assertNotNull(response.getResult(POServiceForPublish.class));
        assertNotNull(response.getResult(POPartnerPriceModel.class));
        assertTrue(response.getResult(POServiceForPublish.class)
                .isPartOfUpgradePath());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getServiceDetails_Error() throws Exception {
        // given
        when(bean.sps.getServiceDetails(any(VOService.class))).thenThrow(
                new ObjectNotFoundException());
        // when
        bean.getServiceDetails(3);
    }

    @Test
    public void getServiceDetails_operatorPriceModel() throws Exception {
        // given
        POOperatorPriceModel expectedModel = new POOperatorPriceModel();
        expectedModel.setRevenueShare(new PORevenueShare());
        expectedModel.setDefaultRevenueShare(new PORevenueShare());
        doReturn(new Response(expectedModel)).when(bean.ps)
                .getOperatorRevenueShare(eq(101L));

        // when
        Response response = bean.getServiceDetails(101L);

        // then
        POOperatorPriceModel actualModel = response
                .getResult(POOperatorPriceModel.class);
        assertNotNull(actualModel);
        assertEquals(expectedModel.getRevenueShare(),
                actualModel.getRevenueShare());
        assertEquals(expectedModel.getDefaultRevenueShare(),
                actualModel.getDefaultRevenueShare());
    }

    @Test
    public void updateAndPublishService() throws Exception {
        // given
        POServiceForPublish s = givenPOServiceForPublish();
        // when
        Response response = bean.updateAndPublishService(s,
                new ArrayList<POResalePermissionDetails>(),
                new ArrayList<POResalePermissionDetails>());

        // then
        assertEquals(0, response.getResults().size());
    }

    @Test
    public void updateAndPublishService_ValidationException() throws Exception {
        // given
        doThrow(new ValidationException()).when(bean.msl)
                .publishServiceWithPermissions(anyLong(), any(CatalogEntry.class),
                        anyListOf(VOCategory.class), anyListOf(POResalePermissionDetails.class),
                        anyListOf(POResalePermissionDetails.class));
        try {
            // when
            bean.updateAndPublishService(givenPOServiceForPublish(),
                    new ArrayList<POResalePermissionDetails>(),
                    new ArrayList<POResalePermissionDetails>());
            fail();
        } catch (ValidationException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updateAndPublishService_ObjectNotFoundException()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.msl)
                .publishServiceWithPermissions(anyLong(), any(CatalogEntry.class),
                        anyListOf(VOCategory.class), anyListOf(POResalePermissionDetails.class),
                        anyListOf(POResalePermissionDetails.class));
        try {
            // when
            bean.updateAndPublishService(givenPOServiceForPublish(),
                    new ArrayList<POResalePermissionDetails>(),
                    new ArrayList<POResalePermissionDetails>());
            fail();
        } catch (ObjectNotFoundException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updateAndPublishService_NonUniqueBusinessKeyException()
            throws Exception {
        // given
        doThrow(new NonUniqueBusinessKeyException()).when(bean.msl)
                .publishServiceWithPermissions(anyLong(), any(CatalogEntry.class),
                        anyListOf(VOCategory.class), anyListOf(POResalePermissionDetails.class),
                        anyListOf(POResalePermissionDetails.class));
        try {
            // when
            bean.updateAndPublishService(givenPOServiceForPublish(),
                    new ArrayList<POResalePermissionDetails>(),
                    new ArrayList<POResalePermissionDetails>());
            fail();
        } catch (NonUniqueBusinessKeyException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updateAndPublishService_ServiceOperationException()
            throws Exception {
        // given
        doThrow(new ServiceOperationException()).when(bean.msl)
                .publishServiceWithPermissions(anyLong(), any(CatalogEntry.class),
                        anyListOf(VOCategory.class), anyListOf(POResalePermissionDetails.class),
                        anyListOf(POResalePermissionDetails.class));
        try {
            // when
            bean.updateAndPublishService(givenPOServiceForPublish(),
                    new ArrayList<POResalePermissionDetails>(),
                    new ArrayList<POResalePermissionDetails>());
            fail();
        } catch (ServiceOperationException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updateAndPublishService_OrganizationAuthorityException()
            throws Exception {
        // given
        doThrow(new OrganizationAuthorityException()).when(bean.msl)
                .publishServiceWithPermissions(anyLong(), any(CatalogEntry.class),
                        anyListOf(VOCategory.class), anyListOf(POResalePermissionDetails.class),
                        anyListOf(POResalePermissionDetails.class));
        try {
            // when
            bean.updateAndPublishService(givenPOServiceForPublish(),
                    new ArrayList<POResalePermissionDetails>(),
                    new ArrayList<POResalePermissionDetails>());
            fail();
        } catch (OrganizationAuthorityException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updateAndPublishService_ServiceStateException()
            throws Exception {
        // given
        doThrow(new ServiceStateException()).when(bean.msl)
                .publishServiceWithPermissions(anyLong(), any(CatalogEntry.class),
                        anyListOf(VOCategory.class), anyListOf(POResalePermissionDetails.class),
                        anyListOf(POResalePermissionDetails.class));
        try {
            // when
            bean.updateAndPublishService(givenPOServiceForPublish(),
                    new ArrayList<POResalePermissionDetails>(),
                    new ArrayList<POResalePermissionDetails>());
            fail();
        } catch (ServiceStateException e) {
            // then
            verify(bean.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updateAndPublishService_ResalePermissions_BrokerManager()
            throws Exception {
        // given
        POServiceForPublish s = givenPOServiceForPublish();
        user.setAssignedRoles(givenRoleAssignment(UserRoleType.BROKER_MANAGER));
        // when
        bean.updateAndPublishService(s,
                new ArrayList<POResalePermissionDetails>(),
                new ArrayList<POResalePermissionDetails>());
    }

    @Test
    public void updateAndPublishService_ResalePermissions_ResellerManager()
            throws Exception {
        // given
        POServiceForPublish s = givenPOServiceForPublish();
        user.setAssignedRoles(givenRoleAssignment(UserRoleType.RESELLER_MANAGER));
        // when
        bean.updateAndPublishService(s,
                new ArrayList<POResalePermissionDetails>(),
                new ArrayList<POResalePermissionDetails>());
    }

    @Test
    public void updateAndPublishService_ResalePermissions_ServiceManager()
            throws Exception {
        // given
        POServiceForPublish s = givenPOServiceForPublish();
        user.setAssignedRoles(givenRoleAssignment(UserRoleType.SERVICE_MANAGER));
        // when
        bean.updateAndPublishService(s,
                new ArrayList<POResalePermissionDetails>(),
                new ArrayList<POResalePermissionDetails>());
    }

    @Test
    public void getCategoriesAndRvenueShare() throws ObjectNotFoundException {
        // when
        Response response = bean.getCategoriesAndRvenueShare("2345rtf", "en");

        // then
        assertNotNull(response.getResult(List.class));
        assertNotNull(response.getResult(POMarketplacePriceModel.class));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getCategoriesAndRvenueShare_Error() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.ps)
                .getMarketplaceRevenueShares(anyString());

        // when
        bean.getCategoriesAndRvenueShare("2345rtf", "en");
    }

    private POServiceForPublish givenPOServiceForPublish() {
        POServiceForPublish poService = new POServiceForPublish();
        VOCatalogEntry voCatalogEntry = new VOCatalogEntry();
        VOMarketplace voMarketplace = new VOMarketplace();
        voMarketplace.setMarketplaceId("1");
        voCatalogEntry.setMarketplace(voMarketplace);
        poService.setCatalogEntry(voCatalogEntry);
        return poService;
    }

    @Test
    public void getBrokers() {
        // when
        Response brokers = bean.getBrokers(0L);

        // then
        assertEquals(1, brokers.getResults().size());
        List<POPartner> list = ParameterizedTypes.list(
                brokers.getResult(List.class), POPartner.class);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getKey());
        assertEquals("organizationId", list.get(0).getOrganizationId());
        assertEquals("name", list.get(0).getName());
        assertTrue(list.get(0).isSelected());
    }

    private List<Object[]> generateOrganizations() {
        List<Object[]> orgs = new ArrayList<Object[]>();
        orgs.add(new Object[]{Long.valueOf(1L), "organizationId", "name",
                Double.valueOf(5.0), ""});
        return orgs;
    }

    @Test
    public void getReseller() {
        // when
        Response reseller = bean.getResellers(0L);

        // then
        assertEquals(1, reseller.getResults().size());
        List<POPartner> list = ParameterizedTypes.list(
                reseller.getResult(List.class), POPartner.class);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getKey());
        assertEquals("organizationId", list.get(0).getOrganizationId());
        assertEquals("name", list.get(0).getName());
        assertTrue(list.get(0).isSelected());
    }

    @Test
    public void getTemplateServices() {
        // given
        List<Product> products = new ArrayList<Product>();
        Organization org = new Organization();
        org.setOrganizationId("orgId");
        Product p1 = new Product();
        p1.setKey(100L);
        p1.setProductId("productId");
        p1.setVendor(org);
        p1.setTemplate(null);
        products.add(p1);
        Product p2 = new Product();
        p2.setTemplate(p1);
        products.add(p2);
        when(bean.partnerSrvProv.loadSuppliedTemplateServices()).thenReturn(
                products);

        // when
        Response templateServices = bean.getTemplateServices();

        // then
        assertEquals(2, templateServices.getResultList(POServiceDetails.class)
                .size());
        assertEquals(100L,
                templateServices.getResultList(POServiceDetails.class).get(0)
                        .getKey());
        assertEquals("productId",
                templateServices.getResultList(POServiceDetails.class).get(0)
                        .getServiceId());
        assertEquals(null,
                templateServices.getResultList(POServiceDetails.class).get(0)
                        .getOrganizationId());
        assertEquals("orgId",
                templateServices.getResultList(POServiceDetails.class).get(1)
                        .getOrganizationId());
    }
}
