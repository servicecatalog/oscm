/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningPartnerServiceLocalBean;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ServiceOperationException;

/**
 * @author tokoda
 */
public class PricingServiceBeanMarketplacePricingsTest {

    private PricingServiceBean bean;

    @Before
    public void setup() {
        bean = new PricingServiceBean();
        setDataServiceMock();
        bean.localizer = mock(LocalizerServiceLocal.class);
    }

    private void setDataServiceMock() {
        DataService dsMock = mock(DataService.class);
        PlatformUser user = new PlatformUser();
        user.setLocale("en");
        when(dsMock.getCurrentUser()).thenReturn(user);
        bean.dm = dsMock;
        bean.mpServiceLocal = mock(MarketplaceServiceLocal.class);
        bean.spPartnerServiceLocal = mock(ServiceProvisioningPartnerServiceLocalBean.class);
        bean.localizer = mock(LocalizerServiceLocal.class);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getPricingForMarketplace_ObjectNotFound() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(bean.mpServiceLocal)
                .getMarketplace(eq("mId"));
        // when
        bean.getPricingForMarketplace("mId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMarketplacePricingForService_NullArgument() throws Exception {
        // when
        bean.getMarketplacePricingForService(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getMarketplacePricingForService_ObjectNotFoundException()
            throws Exception {
        // given
        long serviceKey = 11111;
        POServiceForPricing service = new POServiceForPricing();
        service.setKey(serviceKey);

        doThrow(new ObjectNotFoundException()).when(bean.spPartnerServiceLocal)
                .getCatalogEntryForProduct(eq(serviceKey));
        // when
        bean.getMarketplacePricingForService(service);
        // then
    }

    @Test(expected = ServiceOperationException.class)
    public void getMarketplacePricingForService_ServiceOperationException()
            throws Exception {
        // given
        long serviceKey = 11111;
        POServiceForPricing service = new POServiceForPricing();
        service.setKey(serviceKey);

        doThrow(new ServiceOperationException()).when(
                bean.spPartnerServiceLocal).getCatalogEntryForProduct(
                eq(serviceKey));
        // when
        bean.getMarketplacePricingForService(service);
        // then
    }

    @Test
    public void getMarketplacePricingForService_NoCatalogEntry()
            throws Exception {
        // given
        long serviceKey = 11111;
        POServiceForPricing service = new POServiceForPricing();
        service.setKey(serviceKey);

        doReturn(null).when(bean.spPartnerServiceLocal)
                .getCatalogEntryForProduct(eq(serviceKey));
        // when
        Response response = bean.getMarketplacePricingForService(service);
        // then
        assertNotNull(response);
        assertNull(response.getResult(POMarketplacePricing.class));
    }

    @Test
    public void getMarketplacePricingForService_NoMarketplace()
            throws Exception {
        // given
        long serviceKey = 11111;
        POServiceForPricing service = new POServiceForPricing();
        service.setKey(serviceKey);

        CatalogEntry ce = new CatalogEntry();
        doReturn(ce).when(bean.spPartnerServiceLocal)
                .getCatalogEntryForProduct(eq(serviceKey));
        // when
        Response response = bean.getMarketplacePricingForService(service);
        // then
        assertNotNull(response);
        assertNull(response.getResult(POMarketplacePricing.class));
    }

    @Test
    public void getMarketplacePricingForService() throws Exception {
        // given
        long serviceKey = 11111;
        POServiceForPricing service = new POServiceForPricing();
        service.setKey(serviceKey);

        long marketplaceKey = 22222;
        CatalogEntry ce = new CatalogEntry();
        Marketplace marketplace = new Marketplace();
        marketplace.setKey(22222);
        ce.setMarketplace(marketplace);
        marketplace.setPriceModel(new RevenueShareModel());
        marketplace.setBrokerPriceModel(new RevenueShareModel());
        marketplace.setResellerPriceModel(new RevenueShareModel());

        doReturn(ce).when(bean.spPartnerServiceLocal)
                .getCatalogEntryForProduct(eq(serviceKey));

        // when
        Response response = bean.getMarketplacePricingForService(service);
        // then
        assertNotNull(response);
        assertEquals(marketplaceKey,
                response.getResult(POMarketplacePricing.class).getMarketplace()
                        .getKey());
    }
}
