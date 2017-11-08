/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.partnerservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocal;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningServiceLocalizationLocal;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.DiscountService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningServiceInternal;
import org.oscm.internal.review.POServiceFeedback;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VODiscount;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceFeedback;
import org.oscm.internal.vo.VOServiceLocalization;

@SuppressWarnings("boxing")
public class PartnerServiceBeanTest {
    private Query query;
    private ServiceProvisioningServiceBean sps;
    private PartnerServiceBean service;

    private Product product;

    @Before
    public void setup() throws Exception {
        query = mock(Query.class);
        service = spy(new PartnerServiceBean());
        service.localizer = mock(LocalizerServiceLocal.class);
        sps = mock(ServiceProvisioningServiceBean.class);
        service.sps = sps;
        service.spsLocal = mock(ServiceProvisioningServiceLocal.class);
        service.ds = mock(DataService.class);
        doReturn(query).when(service.ds).createNamedQuery(
                "Product.getSpecificCustomerProduct");
        service.spsLocalizer = mock(ServiceProvisioningServiceLocalizationLocal.class);
        service.sessionCtx = mock(SessionContext.class);
        service.slService = mock(SubscriptionListServiceLocal.class);
        when(service.ds.getCurrentUser()).thenReturn(new PlatformUser());
        product = new Product();
        product.setStatus(ServiceStatus.INACTIVE);
        doReturn(product).when(service.ds).getReference(eq(Product.class),
                anyLong());

        PlatformUser u = new PlatformUser();
        u.setLocale("locale");
        doReturn(u).when(service.ds).getCurrentUser();
        doReturn(Boolean.FALSE).when(service.slService)
                .isUsableSubscriptionExistForTemplate(any(PlatformUser.class),
                        eq(Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS),
                        any(Product.class));
    }

    VOServiceLocalization generateServiceLocalization(
            List<VOLocalizedText> texts) {
        VOServiceLocalization r = new VOServiceLocalization();
        r.setDescriptions(texts);
        return r;
    }

    List<VOLocalizedText> generateLocalizedText() {
        List<VOLocalizedText> result = new ArrayList<VOLocalizedText>();
        VOLocalizedText t = new VOLocalizedText("locale", "text");
        result.add(t);
        return result;
    }

    VOPriceModelLocalization generatePriceModelLocalization(
            List<VOLocalizedText> text) {
        VOPriceModelLocalization r = new VOPriceModelLocalization();
        r.setLicenses(text);
        return r;
    }

    VOServiceDetails generateVOServiceDetails() {
        VOServiceDetails r = new VOServiceDetails();
        r.setPriceModel(new VOPriceModel());
        r.setShortDescription("shortDescription");
        r.setAutoAssignUserEnabled(true);
        return r;
    }

    @Test
    public void getServiceDetails() throws Exception {
        // given
        VOServiceDetails expect = generateVOServiceDetails();
        when(sps.getServiceDetails(any(VOService.class))).thenReturn(expect);
        doReturn(generateServiceLocalization(generateLocalizedText()))
                .when(sps).getServiceLocalization(any(VOService.class));
        doReturn(generatePriceModelLocalization(generateLocalizedText())).when(
                service.spsLocalizer).getPriceModelLocalization(anyLong());

        // when
        POPartnerServiceDetails actual = service.getServiceDetails(1L)
                .getResult(POPartnerServiceDetails.class);

        // then
        verify(sps).getServiceDetails(any(VOServiceDetails.class));
        assertEquals(expect.getShortDescription(),
                actual.getServiceShortDescription());
        assertEquals(expect.isAutoAssignUserEnabled(),
                actual.isAutoAssignUserEnabled());
    }

    @Test
    public void getServiceDetails_noLocalizedText() throws Exception {
        // given
        when(sps.getServiceDetails(any(VOService.class))).thenReturn(
                new VOServiceDetails());
        doReturn(generateServiceLocalization(new ArrayList<VOLocalizedText>()))
                .when(sps).getServiceLocalization(any(VOService.class));
        doReturn(new VOPriceModel()).when(service).swapPriceModelLicense(
                any(VOPriceModel.class), anyLong());

        // when
        service.getServiceDetails(1L);

        // then
        verify(sps).getServiceDetails(any(VOServiceDetails.class));
    }

    @Test
    public void updatePartnerServiceDetails() throws Exception {
        // given
        POPartnerServiceDetails details = new POPartnerServiceDetails();
        details.setPriceModel(new VOPriceModel());

        // when
        Response resp = service.updatePartnerServiceDetails(details);

        // then
        assertNotNull(resp);
    }

    @Test(expected = NullPointerException.class)
    public void updatePartnerServiceDetails_missingPriceModel()
            throws Exception {
        // given
        POPartnerServiceDetails details = new POPartnerServiceDetails();

        // when
        service.updatePartnerServiceDetails(details);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updatePartnerServiceDetails_ServiceNotFound() throws Exception {
        // given
        POPartnerServiceDetails details = new POPartnerServiceDetails();
        details.setPriceModel(new VOPriceModel());
        doThrow(new ObjectNotFoundException()).when(service.ds).getReference(
                eq(Product.class), anyLong());

        // when
        service.updatePartnerServiceDetails(details);
    }

    @Test(expected = ServiceStateException.class)
    public void updatePartnerServiceDetails_ServiceActive() throws Exception {
        // given
        POPartnerServiceDetails details = new POPartnerServiceDetails();
        details.setPriceModel(new VOPriceModel());
        product.setStatus(ServiceStatus.ACTIVE);

        // when
        service.updatePartnerServiceDetails(details);
    }

    @Test
    public void updatePartnerServiceDetails_Rollback_ObjectNotFound()
            throws Exception {
        // given
        POPartnerServiceDetails details = new POPartnerServiceDetails();
        details.setPriceModel(new VOPriceModel());
        doThrow(new ObjectNotFoundException()).when(service.spsLocalizer)
                .savePriceModelLocalizationForReseller(anyLong(), anyBoolean(),
                        any(VOPriceModelLocalization.class));
        // when
        try {
            service.updatePartnerServiceDetails(details);
            fail();
        } catch (ObjectNotFoundException e) {
            // then
            verify(service.sessionCtx).setRollbackOnly();
        }
    }

    @Test
    public void updatePartnerServiceDetails_verifyNoUpdateOfLocalizedResources()
            throws Exception {
        // given
        POPartnerServiceDetails details = new POPartnerServiceDetails();
        details.setPriceModel(new VOPriceModel());
        // when
        service.updatePartnerServiceDetails(details);
        // then
        verify(service.spsLocalizer, times(0)).saveServiceLocalization(
                anyLong(), any(VOServiceLocalization.class));
        verify(service.spsLocalizer, times(1))
                .savePriceModelLocalizationForReseller(anyLong(), anyBoolean(),
                        any(VOPriceModelLocalization.class));

    }

    @Test(expected = ObjectNotFoundException.class)
    public void getServiceDetails_ServiceNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(service.ds).getReference(
                eq(Product.class), anyLong());

        // when
        service.getServiceDetails(1L);
    }

    @Test(expected = ServiceStateException.class)
    public void getServiceDetails_ResalePermissionRemoved() throws Exception {
        // given
        POPartnerServiceDetails details = new POPartnerServiceDetails();
        details.setPriceModel(new VOPriceModel());
        product.setStatus(ServiceStatus.DELETED);

        // when
        service.getServiceDetails(1L);
    }

    @Test
    public void getServiceForMarketplace() throws Exception {
        // given
        Product expected = new Product();
        expected.setKey(10L);
        expected.setAutoAssignUserEnabled(false);
        expected.setStatus(ServiceStatus.ACTIVE);
        expected.setTechnicalProduct(new TechnicalProduct());
        CatalogEntry c = new CatalogEntry();
        c.setAnonymousVisible(true);
        expected.setCatalogEntries(Arrays.asList(c));
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(1L));

        // when
        VOService actual = service.getServiceForMarketplace(1L, "en")
                .getResult(VOService.class);

        // then
        verify(service.ds, times(1)).getReference(eq(Product.class), eq(1L));
        assertEquals(10L, actual.getKey());
    }

    @Test
    public void getServiceForMarketplace_NotVisibleForAnaonymous()
            throws Exception {
        // given
        Product expected = new Product();
        expected.setKey(10L);
        expected.setStatus(ServiceStatus.ACTIVE);
        expected.setTechnicalProduct(new TechnicalProduct());
        CatalogEntry c = new CatalogEntry();
        expected.setCatalogEntries(Arrays.asList(c));
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(1L));

        // when
        VOService actual = service.getServiceForMarketplace(1L, "en")
                .getResult(VOService.class);

        // then
        verify(service.ds, times(1)).getReference(eq(Product.class), eq(1L));
        assertNull(actual);
    }

    @Test
    public void getServiceForMarketplace_Inactive() throws Exception {
        // given
        Product expected = new Product();
        expected.setKey(10L);
        expected.setStatus(ServiceStatus.INACTIVE);
        expected.setTechnicalProduct(new TechnicalProduct());
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(1L));

        // when
        VOService actual = service.getServiceForMarketplace(1L, "en")
                .getResult(VOService.class);

        // then
        verify(service.ds, times(1)).getReference(eq(Product.class), eq(1L));
        assertNull(actual);
    }

    @Test
    public void getServiceForMarketplace_CustomerTemplate_Anonymous()
            throws Exception {
        // given
        Product expected = new Product();
        expected.setType(ServiceType.CUSTOMER_TEMPLATE);
        expected.setTemplate(new Product());
        expected.getTemplate().setStatus(ServiceStatus.ACTIVE);
        expected.getTemplate().setKey(666L);
        expected.getTemplate().setAutoAssignUserEnabled(false);
        CatalogEntry c = new CatalogEntry();
        c.setAnonymousVisible(true);
        expected.getTemplate().setCatalogEntries(Arrays.asList(c));
        expected.getTemplate().setTechnicalProduct(new TechnicalProduct());
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(1L));

        // when
        VOService actual = service.getServiceForMarketplace(1L, "en")
                .getResult(VOService.class);

        // then
        verify(service.ds, times(1)).getReference(eq(Product.class), eq(1L));
        assertEquals(666L, actual.getKey());
    }

    @Test
    public void getServiceForMarketplace_CallTemplate_ReturnCustomerTemplate()
            throws Exception {
        // given
        Product expected = new Product();
        Query q = mock(Query.class);
        expected.setType(ServiceType.CUSTOMER_TEMPLATE);
        expected.setTemplate(new Product());
        expected.setKey(666L);
        expected.setAutoAssignUserEnabled(false);
        expected.setStatus(ServiceStatus.ACTIVE);
        expected.setTechnicalProduct(new TechnicalProduct());
        expected.getTemplate().setType(ServiceType.TEMPLATE);
        doReturn(expected.getTemplate()).when(service.ds).getReference(
                eq(Product.class), eq(1L));
        doReturn(q).when(service.ds).createNamedQuery(anyString());
        doReturn(Arrays.asList(expected)).when(q).getResultList();
        doReturn(new PlatformUser()).when(service.ds).getCurrentUserIfPresent();

        // when
        VOService actual = service.getServiceForMarketplace(1L, "en")
                .getResult(VOService.class);

        // then
        verify(service.ds, times(1)).getReference(eq(Product.class), eq(1L));
        assertEquals(666L, actual.getKey());
    }

    @Test
    public void getServiceForMarketplace_CustomerTemplate() throws Exception {
        // given
        Organization o = new Organization();
        Product expected = new Product();
        PlatformUser u = new PlatformUser();
        u.setOrganization(o);
        expected.setKey(10L);
        expected.setTemplate(new Product());
        expected.setStatus(ServiceStatus.ACTIVE);
        expected.setTechnicalProduct(new TechnicalProduct());
        expected.setTargetCustomer(o);
        expected.setType(ServiceType.CUSTOMER_TEMPLATE);
        expected.setAutoAssignUserEnabled(false);
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(1L));
        doReturn(u).when(service.ds).getCurrentUserIfPresent();

        // when
        VOService actual = service.getServiceForMarketplace(1L, "en")
                .getResult(VOService.class);

        // then
        verify(service.ds, times(1)).getReference(eq(Product.class), eq(1L));
        assertEquals(10L, actual.getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getServiceForMarketplace_ObjectNotFoundException()
            throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(service.ds).getReference(
                eq(Product.class), eq(1L));

        // when
        service.getServiceForMarketplace(1L, "en");
    }

    @Test
    public void getAllServiceDetailsForMarketplace_found() throws Exception {
        // given

        Product product = createProduct(ServiceStatus.ACTIVE, 10L);

        mockDelegates();
        doReturn(product).when(service.ds).getReference(eq(Product.class),
                eq(10L));

        VODiscount activeDiscount = new VODiscount();
        Date d = new Date();
        Long timeStamp = d.getTime();
        activeDiscount.setEndTime(timeStamp + 990000000);
        activeDiscount.setStartTime(1L);

        doReturn(activeDiscount).when(service.discountService)
                .getDiscountForService(10L);

        // when
        VOService actual = null;
        Response r = null;
        r = service.getAllServiceDetailsForMarketplace(10L, "en", "mpl123");
        actual = r.getResult(VOService.class);

        // then
        verify(service, times(1)).getServiceForMarketplace(product.getKey(),
                "en");

        verify(service.discountService, times(1)).getDiscountForService(
                product.getKey());

        verify(service.spsi, times(1)).getPartnerForService(product.getKey(),
                "en");
        verify(service.sps, times(1)).getServiceSeller(product.getKey(), "en");
        verify(service.sps, times(1)).getRelatedServicesForMarketplace(actual,
                "mpl123", "en");

        assertNotNull(actual);
        assertNotNull(r.getResult(POServiceFeedback.class));
        assertNotNull(r.getResult(VODiscount.class));
    }

    @Test
    public void getAllServiceDetailsForMarketplace_not_found() throws Exception {
        // given
        mockDelegates();
        long serviceKey = 10L;
        doThrow(new ObjectNotFoundException()).when(service.ds).getReference(
                eq(Product.class), anyLong());
        // when
        try {
            service.getAllServiceDetailsForMarketplace(10L, "en", "abcdef");
            fail();
        } catch (ObjectNotFoundException e) {

            // then
            verify(service, times(1))
                    .getServiceForMarketplace(serviceKey, "en");

            verify(service.discountService, times(0)).getDiscountForService(
                    serviceKey);

            verify(service.spsi, times(0)).getPartnerForService(serviceKey,
                    "en");
            verify(service.sps, times(0)).getServiceSeller(serviceKey, "en");
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getAllServiceDetailsForMarketplace_notActive_notMpo()
            throws Exception {
        // given
        String marketplaceId = "abcdef";
        Product product = createProduct(ServiceStatus.SUSPENDED, 10L);

        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setOwningOrganizationId("org");

        mockDelegates();
        PlatformUser user = new PlatformUser();
        Organization organization = new Organization();
        organization.setOrganizationId("123456");
        user.setOrganization(organization);

        when(service.ds.getCurrentUserIfPresent()).thenReturn(user);
        doReturn(marketplace).when(service.marketplaceService)
                .getMarketplaceById(marketplaceId);
        doReturn(product).when(service.ds).getReference(eq(Product.class),
                eq(10L));

        // when
        VOService actual = null;
        try {
            Response result = service.getAllServiceDetailsForMarketplace(10L,
                    "en", marketplaceId);
            actual = result.getResult(VOService.class);
            assertNotNull(result.getReturnCodes());

            assertNull(actual);
        } catch (ObjectNotFoundException e) {
            fail();
        }

    }

    @Test
    public void getAllServiceDetailsForMarketplace_notActive_Mpo()
            throws Exception {
        // given
        String marketplaceId = "mpl123";
        Product product = createProduct(ServiceStatus.SUSPENDED, 10L);

        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setOwningOrganizationId("org");

        mockDelegates();
        PlatformUser user = new PlatformUser();
        Organization organization = new Organization();
        organization.setOrganizationId("org");
        user.setOrganization(organization);

        when(service.ds.getCurrentUserIfPresent()).thenReturn(user);
        doReturn(marketplace).when(service.marketplaceService)
                .getMarketplaceById(marketplaceId);
        doReturn(product).when(service.ds).getReference(eq(Product.class),
                eq(10L));

        // when
        VOService actual = null;
        try {
            Response result = service.getAllServiceDetailsForMarketplace(10L,
                    "en", marketplaceId);
            actual = result.getResult(VOService.class);
            assertNotNull(actual);

            assertTrue(result.getReturnCodes().isEmpty());
        } catch (ObjectNotFoundException e) {
            fail();
        }

    }

    @Test(expected = OperationNotPermittedException.class)
    public void getAllServiceDetailsForMarketplace_serviceNotBelongsToMarketplace()
            throws Exception {
        // given
        Product expected = createProduct(ServiceStatus.ACTIVE, 10L);

        mockDelegates();
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(10L));

        // then
        service.getAllServiceDetailsForMarketplace(10L, "en", "newMP");
    }

    @Test
    public void getAllServiceDetailsForMarketplace_ratingsNotFound()
            throws Exception {
        // given
        Product expected = createProduct(ServiceStatus.ACTIVE, 10L);

        mockDelegates();
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(10L));

        // then
        Response result = service.getAllServiceDetailsForMarketplace(10L, "en",
                "mpl123");
        // then
        assertNotNull(result.getResult(VOService.class));
        assertNull(result.getResult(VOServiceFeedback.class));
    }

    @Test
    public void getAllServiceDetailsForMarketplace_discount_notFound()
            throws Exception {
        // given
        Product expected = createProduct(ServiceStatus.ACTIVE, 10L);

        mockDelegates();
        doReturn(expected).when(service.ds).getReference(eq(Product.class),
                eq(10L));

        doThrow(new ObjectNotFoundException()).when(service.discountService)
                .getDiscountForService(10L);
        // then
        Response result = service.getAllServiceDetailsForMarketplace(10L, "en",
                "mpl123");
        // then
        assertNotNull(result.getResult(VOService.class));
        assertNull(result.getResult(VODiscount.class));
    }

    private Product createProduct(ServiceStatus status, Long givenKey) {
        Product product = new Product();
        product.setKey(givenKey);
        product.setStatus(status);
        product.setType(ServiceType.TEMPLATE);
        TechnicalProduct tp = new TechnicalProduct();
        tp.setAccessType(ServiceAccessType.EXTERNAL);
        product.setTechnicalProduct(tp);
        product.setAutoAssignUserEnabled(false);

        Marketplace mp = new Marketplace();
        mp.setMarketplaceId("mpl123");
        CatalogEntry c = new CatalogEntry();
        c.setAnonymousVisible(true);
        c.setMarketplace(mp);
        product.setCatalogEntries(Arrays.asList(c));
        return product;
    }

    private void mockDelegates() {
        service.discountService = mock(DiscountService.class);
        service.spsi = mock(ServiceProvisioningServiceInternal.class);
        service.marketplaceService = mock(MarketplaceService.class);
    }
}
