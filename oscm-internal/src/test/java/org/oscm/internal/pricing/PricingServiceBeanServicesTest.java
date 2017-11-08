/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningPartnerServiceLocalBean;
import org.oscm.internal.components.response.Response;

/**
 * @author tokoda
 * 
 */
public class PricingServiceBeanServicesTest {

    PricingServiceBean pricingService;

    @Before
    public void setup() {
        pricingService = new PricingServiceBean();
        ServiceProvisioningPartnerServiceLocalBean beanMock = mock(ServiceProvisioningPartnerServiceLocalBean.class);
        pricingService.spPartnerServiceLocal = beanMock;
    }

    @Test
    public void getTemplateServices_NoProduct() {
        // given
        List<Product> products = new ArrayList<Product>();
        when(pricingService.spPartnerServiceLocal.getTemplateProducts())
                .thenReturn(products);

        // when
        Response response = pricingService.getTemplateServices();

        // then
        assertEquals(0, response.getResultList(POServiceForPricing.class)
                .size());
    }

    @Test
    public void getTemplateServices_MultipleProducts() {
        // given
        Organization venderOrg1 = createOrganization("orgId1", "name1");
        Product product1 = createProduct(11111, "productId1", venderOrg1);
        Organization venderOrg2 = createOrganization("orgId2", "name2");
        Product product2 = createProduct(22222, "productId2", venderOrg2);

        List<Product> products = new ArrayList<Product>();
        products.add(product1);
        products.add(product2);
        when(pricingService.spPartnerServiceLocal.getTemplateProducts())
                .thenReturn(products);

        // when
        Response response = pricingService.getTemplateServices();

        // then
        List<POServiceForPricing> results = response
                .getResultList(POServiceForPricing.class);
        assertEquals(2, results.size());
        assertEquals(11111, results.get(0).getKey());
        assertEquals(22222, results.get(1).getKey());
    }

    @Test
    public void getPartnerServicesWithRevenueShareForTemplate_NoPartnerServices()
            throws Exception {
        // given
        when(
                pricingService.spPartnerServiceLocal
                        .getPartnerProductsForTemplate(anyLong())).thenReturn(
                new ArrayList<Product>());

        // when
        Response response = pricingService
                .getPartnerServicesWithRevenueShareForTemplate(new POServiceForPricing());

        // then
        assertEquals(0, response.getResultList(POServicePricing.class).size());
    }

    @Test
    public void getPartnerServicesWithRevenueShareForTemplate()
            throws Exception {
        // given
        Organization venderOrg1 = createOrganization("orgId1", "name1");
        Product product1 = createProduct(11111, "productId1", venderOrg1);
        List<CatalogEntry> ces1 = new ArrayList<CatalogEntry>();
        ces1.add(new CatalogEntry());
        product1.setCatalogEntries(ces1);

        Organization venderOrg2 = createOrganization("orgId2", "name2");
        Product product2 = createProduct(22222, "productId2", venderOrg2);
        List<CatalogEntry> ces2 = new ArrayList<CatalogEntry>();
        ces2.add(new CatalogEntry());
        product2.setCatalogEntries(ces2);

        List<Product> partnerProducts = new ArrayList<Product>();
        partnerProducts.add(product1);
        partnerProducts.add(product2);

        when(
                pricingService.spPartnerServiceLocal
                        .getPartnerProductsForTemplate(anyLong())).thenReturn(
                partnerProducts);

        // when
        Response response = pricingService
                .getPartnerServicesWithRevenueShareForTemplate(new POServiceForPricing());

        // then
        List<POServicePricing> results = response
                .getResultList(POServicePricing.class);
        assertEquals(2, results.size());
        assertEquals(11111, results.get(0).getServiceForPricing().getKey());
        assertEquals(22222, results.get(1).getServiceForPricing().getKey());
    }

    @Test
    public void assembleServiceForPricing() {
        // given
        Organization organization = createOrganization("orgId", "name");
        Product product = createProduct(11111, "productId", organization);
        // when
        POServiceForPricing service = PricingServiceBean
                .assembleServiceForPricing(product);
        // then
        assertEquals(11111, service.getKey());
        assertEquals(0, service.getVersion());
        assertEquals("productId", service.getServiceId());
        POOrganization vendor = service.getVendor();
        assertEquals("orgId", vendor.getOrganizationId());
        assertEquals("name", vendor.getOrganizationName());
    }

    @Test
    public void assembleServicePricing() {
        // given
        Organization organization = createOrganization("orgId", "name");
        Product product = createProduct(11111, "productId", organization);

        CatalogEntry catalogEntry = new CatalogEntry();
        catalogEntry.setBrokerPriceModel(createRevenueShareModel(22222,
                BigDecimal.ONE));
        catalogEntry.setResellerPriceModel(createRevenueShareModel(33333,
                BigDecimal.TEN));

        List<CatalogEntry> catalogEntries = new ArrayList<CatalogEntry>();
        catalogEntries.add(catalogEntry);
        product.setCatalogEntries(catalogEntries);

        // when
        POServicePricing servicePricing = PricingServiceBean
                .assembleServicePricing(product);

        // then
        assertEquals(11111, servicePricing.getServiceForPricing().getKey());
        assertEquals("orgId", servicePricing.getServiceForPricing().getVendor()
                .getOrganizationId());

        POPartnerPriceModel partnerPriceModel = servicePricing
                .getPartnerPriceModel();
        assertEquals(22222, partnerPriceModel.getRevenueShareBrokerModel()
                .getKey());
        assertEquals(BigDecimal.ONE, partnerPriceModel
                .getRevenueShareBrokerModel().getRevenueShare());

        assertEquals(33333, partnerPriceModel.getRevenueShareResellerModel()
                .getKey());
        assertEquals(BigDecimal.TEN, partnerPriceModel
                .getRevenueShareResellerModel().getRevenueShare());

    }

    private Product createProduct(long key, String productId,
            Organization organization) {
        Product product = new Product();
        product.setKey(key);
        product.setProductId(productId);
        product.setVendor(organization);
        return product;
    }

    private Organization createOrganization(String organizationId,
            String organizationName) {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization.setName(organizationName);
        return organization;
    }

    private RevenueShareModel createRevenueShareModel(long key,
            BigDecimal revenueShare) {
        RevenueShareModel revenueShareModel = new RevenueShareModel();
        revenueShareModel.setKey(key);
        revenueShareModel.setRevenueShare(revenueShare);
        return revenueShareModel;
    }

}
