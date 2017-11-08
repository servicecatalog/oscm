/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.components;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
import org.oscm.internal.components.response.Response;

/**
 * Unit tests for ServiceSelectorBean
 * 
 * @author barzu
 */
public class ServiceSelectorBeanTest {

    private ServiceSelectorBean bean;

    @Before
    public void setup() {
        bean = new ServiceSelectorBean();
        bean.spPartnerServiceLocal = mock(ServiceProvisioningPartnerServiceLocal.class);
    }

    @Test
    public void getTemplateServices() {
        // given
        List<Product> products = asList(givenProduct(101L), givenProduct(102L));
        doReturn(products).when(bean.spPartnerServiceLocal)
                .getTemplateProducts();

        // when
        Response response = bean.getTemplateServices();

        // then
        List<POService> services = response.getResultList(POService.class);
        assertEquals(2, services.size());

        assertEquals(101L, services.get(0).getKey());
        assertEquals("productId_101", services.get(0).getServiceId());
        assertEquals("oId_101", services.get(0).getVendorOrganizationId());

        assertEquals(102L, services.get(1).getKey());
        assertEquals("productId_102", services.get(1).getServiceId());
        assertEquals("oId_102", services.get(1).getVendorOrganizationId());
    }

    @Test
    public void getTemplateServices_emptyList() {
        // given
        List<Product> products = new ArrayList<Product>();
        doReturn(products).when(bean.spPartnerServiceLocal)
                .getTemplateProducts();

        // when
        Response response = bean.getTemplateServices();

        // then
        List<POService> services = response.getResultList(POService.class);
        assertEquals(0, services.size());
    }

    private Product givenProduct(long key) {
        Product product = new Product();
        product.setKey(key);
        product.setProductId("productId_" + key);
        Organization vendor = new Organization();
        vendor.setOrganizationId("oId_" + key);
        product.setVendor(vendor);
        return product;
    }

}
