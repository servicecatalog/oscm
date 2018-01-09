/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.vo.VOServiceEntry;

/**
 * @author barzu
 */
public class ServiceProvisioningServiceBeanGetPartnerServiceIT {

    private static final String MID = "mId";
    private static final String LOCALE = "en";

    private ServiceProvisioningServiceBean sps;

    private Product product;

    @Before
    public void setup() throws Exception {
        sps = spy(new ServiceProvisioningServiceBean());
        sps.dm = mock(DataService.class);
        sps.localizer = mock(LocalizerServiceLocal.class);

        product = new Product();
        product.setTechnicalProduct(new TechnicalProduct());
        product.setAutoAssignUserEnabled(Boolean.FALSE);
        doReturn(product).when(sps.dm).getReference(eq(Product.class),
                anyLong());

        PlatformUser user = new PlatformUser();
        Organization org = new Organization();
        user.setOrganization(org);
        doReturn(user).when(sps.dm).getCurrentUserIfPresent();
        doReturn(user).when(sps.dm).getCurrentUser();

        doReturn(Boolean.TRUE).when(sps).existsCatalogEntryForMarketplace(
                any(Product.class), anyString());
        doReturn(null).when(sps).getCopyForCustomer(eq(product), eq(org));
        doReturn(Boolean.FALSE).when(sps).isSubscriptionLimitReached(
                eq(product));
    }

    @Test
    public void getServiceForMarketplace_NullStatus() throws Exception {
        // when
        VOServiceEntry service = sps.getServiceForMarketplace(
                Long.valueOf(product.getKey()), MID, LOCALE);

        // then
        assertNull(service);
    }

    @Test
    public void getServiceForMarketplace_ACTIVE() throws Exception {
        // given
        product.setStatus(ServiceStatus.ACTIVE);

        // when
        VOServiceEntry service = sps.getServiceForMarketplace(
                Long.valueOf(product.getKey()), MID, LOCALE);

        // then
        assertNotNull(service);
        assertEquals(product.getKey(), service.getKey());
    }

    @Test
    public void getServiceForMarketplace_DenyRevokedPartnerTemplate()
            throws Exception {
        // given
        product.setStatus(ServiceStatus.DELETED);
        product.setType(ServiceType.PARTNER_TEMPLATE);
        Product supplierTemplate = new Product();
        supplierTemplate.setProductId("pId");
        product.setTemplate(supplierTemplate);
        doNothing().when(sps).verifyNoSubscriptionCopy(eq(product));

        // when
        VOServiceEntry service = sps.getServiceForMarketplace(
                Long.valueOf(product.getKey()), MID, LOCALE);

        // then
        assertNull(service);
    }

}
