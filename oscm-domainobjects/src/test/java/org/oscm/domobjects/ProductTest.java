/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.11.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * @author barzu
 */
public class ProductTest {

    private final String CONF_URL = "http://www.conf.com/app.js";
    private final long HOUR = 1000 * 60 * 60;

    @Test
    public void getProductTemplate_Template() {
        // given
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setTemplate(null);

        // when
        Product prodTemplate = product.getProductTemplate();

        // then
        assertSame("Product template must be the product itself", product,
                prodTemplate);
    }

    @Test
    public void getProductTemplate_PartnerTemplate() {
        // given
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setTemplate(null);
        Product partnerTemplate = new Product();
        partnerTemplate.setType(ServiceType.PARTNER_TEMPLATE);
        partnerTemplate.setTemplate(product);

        // when
        Product prodTemplate = partnerTemplate.getProductTemplate();

        // then
        assertSame("Delivered product is not the product template", product,
                prodTemplate);
    }

    @Test
    public void getProductTemplate_PartnerSubscription() {
        // given
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setTemplate(null);
        Product partnerTemplate = new Product();
        partnerTemplate.setType(ServiceType.PARTNER_TEMPLATE);
        partnerTemplate.setTemplate(product);
        Product partnerSubscriptionCopy = new Product();
        partnerSubscriptionCopy.setType(ServiceType.PARTNER_SUBSCRIPTION);
        partnerSubscriptionCopy.setTemplate(partnerTemplate);

        // when
        Product prodTemplate = partnerSubscriptionCopy.getProductTemplate();

        // then
        assertSame("Delivered product is not the product template", product,
                prodTemplate);
    }

    @Test
    public void isAllowedToCreateReview_externalAccess() {
        // given
        PlatformUser user = createUser(false);
        Product product = createProductTemplate(ServiceAccessType.EXTERNAL);

        // when
        boolean result = product.isAllowedToCreateReview(user);

        // then
        assertTrue("Review must be allowed", result);
    }

    @Test
    public void isAllowedToCreateReview_partnerSubscription_UserIsAdmin() {
        // given
        PlatformUser user = createUser(true);
        Product product = createProductTemplate(ServiceAccessType.LOGIN);
        Product partnerTemplate = product.copyForResale(new Organization());
        createPartnerSubscription(user, partnerTemplate, false);

        // when
        boolean result = product.isAllowedToCreateReview(user);

        // then
        assertTrue("Review must be allowed", result);
    }

    @Test
    public void isAllowedToCreateReview_partnerSubscription_UserHasLicense() {
        // given
        PlatformUser user = createUser(false);
        Product product = createProductTemplate(ServiceAccessType.LOGIN);
        Product partnerTemplate = product.copyForResale(new Organization());
        createPartnerSubscription(user, partnerTemplate, true);

        // when
        boolean result = product.isAllowedToCreateReview(user);

        // then
        assertTrue("Review must be allowed", result);
    }

    @Test
    public void getCleanProductId() {
        // given
        Product product = new Product();
        product.dataContainer = mock(ProductData.class);

        // when
        product.getCleanProductId();

        // then
        verify(product.dataContainer, times(1)).getCleanProductId();
    }

    @Test
    public void setDatacontainerValues() {
        // given
        long now = System.currentTimeMillis();
        long inOneHour = now + HOUR;
        Product template = createProduct(inOneHour, now, ServiceType.TEMPLATE);
        Product copy = new Product();

        // when
        template.setDatacontainerValues(copy, ServiceType.TEMPLATE);

        // then
        assertNull(copy.getDeprovisioningDate());
        assertEquals(inOneHour, copy.getProvisioningDate());
        assertTrue(copy.getProductId().startsWith("abc#"));
    }

    public void setDatacontainerValues_configuratorURL_TEMPLATE() {
        // given
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);
        Product copy = new Product();

        // when
        prod.setDatacontainerValues(copy, ServiceType.TEMPLATE);

        // then
        assertEquals(CONF_URL, copy.getConfiguratorUrl());
    }

    public void setDatacontainerValues_configuratorURL_CUSTOMER_TEMPLATE() {
        // given
        Product prod = createProduct(1, 1, ServiceType.CUSTOMER_TEMPLATE);
        Product copy = new Product();

        // when
        prod.setDatacontainerValues(copy, ServiceType.CUSTOMER_TEMPLATE);

        // then
        assertEquals(null, copy.getConfiguratorUrl());
    }

    @Test
    public void setDatacontainerValues_configuratorURL_CUSTOMER_SUBSCRIPTION() {
        // given
        Product prod = createProduct(1, 1, ServiceType.CUSTOMER_SUBSCRIPTION);
        Product copy = new Product();

        // when
        prod.setDatacontainerValues(copy, ServiceType.CUSTOMER_SUBSCRIPTION);

        // then
        assertEquals(null, copy.getConfiguratorUrl());
    }

    @Test
    public void setDatacontainerValues_configuratorURL_SUBSCRIPTION() {
        // given
        Product prod = createProduct(1, 1, ServiceType.SUBSCRIPTION);
        Product copy = new Product();

        // when
        prod.setDatacontainerValues(copy, ServiceType.SUBSCRIPTION);

        // then
        assertEquals(null, copy.getConfiguratorUrl());
    }

    @Test
    public void setDatacontainerValues_configuratorURL_PARTNER_TEMPLATE() {
        // given
        Product prod = createProduct(1, 1, ServiceType.PARTNER_TEMPLATE);
        Product template = createProduct(1, 1, ServiceType.TEMPLATE);
        prod.setTemplate(template);
        Product copy = new Product();

        // when
        prod.setDatacontainerValues(copy, ServiceType.PARTNER_TEMPLATE);

        // then
        assertEquals(null, copy.getConfiguratorUrl());
    }

    @Test
    public void setDatacontainerValues_configuratorURL_PARTNER_SUBSCRIPTION() {
        // given
        Product prod = createProduct(1, 1, ServiceType.PARTNER_SUBSCRIPTION);
        Product copy = new Product();

        // when
        prod.setDatacontainerValues(copy, ServiceType.PARTNER_SUBSCRIPTION);

        // then
        assertEquals(null, copy.getConfiguratorUrl());
    }

    @Test
    public void setDatacontainerValues_uniqueProductId() {
        Product template = createProduct(0L, 1L, ServiceType.TEMPLATE);
        Product copy1 = new Product();
        Product copy2 = new Product();

        // when
        template.setDatacontainerValues(copy1, ServiceType.TEMPLATE);
        template.setDatacontainerValues(copy2, ServiceType.TEMPLATE);

        // then
        assertFalse(copy1.getProductId().equals(copy2.getProductId()));
    }

    @Test
    public void setDatacontainerValues_autoAssignUserEnabled() {
        Product template = createProduct(0L, 1L, ServiceType.TEMPLATE);
        Product copy1 = new Product();
        Product copy2 = new Product();

        // when
        template.setAutoAssignUserEnabled(Boolean.TRUE);
        template.setDatacontainerValues(copy1, ServiceType.TEMPLATE);
        template.setAutoAssignUserEnabled(Boolean.FALSE);
        template.setDatacontainerValues(copy2, ServiceType.TEMPLATE);

        // then
        assertEquals(Boolean.TRUE, copy1.isAutoAssignUserEnabled());
        assertEquals(Boolean.FALSE, copy2.isAutoAssignUserEnabled());
    }

    @Test
    public void setConfiguratorUrl() {
        Product p = new Product();
        p.setConfiguratorUrl(CONF_URL);
        assertEquals(CONF_URL, p.getConfiguratorUrl());
    }

    @Test
    public void setSubscriptionProductType_TEMPLATE() {
        // given
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);
        Product copy = new Product();

        // when
        prod.setSubscriptionProductType(copy);

        // then
        assertEquals(ServiceType.SUBSCRIPTION, copy.getType());
    }

    @Test
    public void setSubscriptionProductType_CUSTOMER_TEMPLATE() {
        // given
        Product prod = createProduct(1, 1, ServiceType.CUSTOMER_TEMPLATE);
        Product copy = new Product();

        // when
        prod.setSubscriptionProductType(copy);

        // then
        assertEquals(ServiceType.CUSTOMER_SUBSCRIPTION, copy.getType());
    }

    @Test
    public void setSubscriptionProductType_PARTNER_TEMPLATE() {
        // given
        Product prod = createProduct(1, 1, ServiceType.PARTNER_TEMPLATE);
        Product copy = new Product();

        // when
        prod.setSubscriptionProductType(copy);

        // then
        assertEquals(ServiceType.PARTNER_SUBSCRIPTION, copy.getType());
    }

    @Test
    public void setSubscriptionProductType_SUBSCRIPTION() {
        // given
        Product prod = createProduct(1, 1, ServiceType.SUBSCRIPTION);
        Product copy = new Product();

        // when
        prod.setSubscriptionProductType(copy);

        // then
        assertEquals(ServiceType.SUBSCRIPTION, copy.getType());
    }

    

    @Test
    public void setSubscriptionProductType_CUSTOMER_SUBSCRIPTION_B10970() {
        // given
        Product prod = createProduct(1, 1, ServiceType.CUSTOMER_SUBSCRIPTION);
        Product copy = new Product();

        // when
        prod.setSubscriptionProductType(copy);

        // then
        assertEquals(ServiceType.CUSTOMER_SUBSCRIPTION, copy.getType());
    }
    
    @Test
    public void setSubscriptionProductType_PARTNER_SUBSCRIPTION_B10970() {
        // given
        Product prod = createProduct(1, 1, ServiceType.PARTNER_SUBSCRIPTION);
        Product copy = new Product();

        // when
        prod.setSubscriptionProductType(copy);

        // then
        assertEquals(ServiceType.PARTNER_SUBSCRIPTION, copy.getType());
    }

    @Test
    public void copyTemplate() {
        // given
        final String COPY_ID = "copy_id";
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);

        // when
        Product copy = prod.copyTemplate(COPY_ID);

        // then
        assertEquals(ServiceType.TEMPLATE, copy.getType());
        assertEquals(COPY_ID, copy.getProductId());
        assertEquals(ServiceStatus.INACTIVE, copy.getStatus());
        assertEquals(prod.getTechnicalProduct(), copy.getTechnicalProduct());
        assertEquals(prod.getVendor(), copy.getVendor());
        assertNull(copy.getTemplate());
        assertNull(copy.getTargetCustomer());
        assertNull(copy.getOwningSubscription());
        assertNull(copy.getProductFeedback());
        assertEquals(prod.getConfiguratorUrl(), copy.getConfiguratorUrl());
    }

    @Test
    public void copyForCustomer() {
        // given
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);

        // when
        Organization customer = new Organization();
        Product copy = prod.copyForCustomer(customer);

        // then
        assertEquals(ServiceType.CUSTOMER_TEMPLATE, copy.getType());
        assertTrue(copy.getProductId().startsWith(prod.getProductId() + "#"));
        assertEquals(ServiceStatus.INACTIVE, copy.getStatus());
        assertEquals(prod.getTechnicalProduct(), copy.getTechnicalProduct());
        assertEquals(prod.getVendor(), copy.getVendor());
        assertEquals(prod, copy.getTemplate());
        assertEquals(customer, copy.getTargetCustomer());
        assertNull(copy.getOwningSubscription());
        assertNull(copy.getProductFeedback());
        assertNull(copy.getConfiguratorUrl());
    }

    @Test
    public void copyForSubscription() {
        // given
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);

        // when
        Organization customer = new Organization();
        Subscription sub = new Subscription();
        Product copy = prod.copyForSubscription(customer, sub);

        // then
        assertEquals(ServiceType.SUBSCRIPTION, copy.getType());
        assertTrue(copy.getProductId().startsWith(prod.getProductId() + "#"));
        assertEquals(prod.getStatus(), copy.getStatus());
        assertEquals(prod.getTechnicalProduct(), copy.getTechnicalProduct());
        assertEquals(prod.getVendor(), copy.getVendor());
        assertEquals(prod, copy.getTemplate());
        assertEquals(customer, copy.getTargetCustomer());
        assertEquals(sub, copy.getOwningSubscription());
        assertNull(copy.getProductFeedback());
        assertNull(copy.getConfiguratorUrl());
    }

    @Test
    public void copyForResale() {
        // given
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);

        // when
        Organization vendor = new Organization();
        Product copy = prod.copyForResale(vendor);

        // then
        assertEquals(ServiceType.PARTNER_TEMPLATE, copy.getType());
        assertTrue(copy.getProductId().startsWith(prod.getProductId() + "#"));
        assertEquals(prod.getStatus(), copy.getStatus());
        assertEquals(prod.getTechnicalProduct(), copy.getTechnicalProduct());
        assertEquals(vendor, copy.getVendor());
        assertEquals(prod, copy.getTemplate());
        assertNull(copy.getTargetCustomer());
        assertNull(copy.getOwningSubscription());
        assertNull(copy.getProductFeedback());
        assertNull(copy.getPriceModel());
        assertNull(copy.getParameterSet());
        assertNull(copy.getConfiguratorUrl());
    }

    @Test
    public void setParamatersAndPriceModel_null() {
        // given
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);
        prod.setParameterSet(null);
        prod.setPriceModel(null);
        Product copy = new Product();

        // when
        prod.setParamatersAndPriceModel(copy);

        // then
        assertNull(copy.getParameterSet());
        assertNull(copy.getPriceModel());
    }

    @Test
    public void setParamatersAndPriceModel_not_null() {
        // given
        Product prod = createProduct(1, 1, ServiceType.TEMPLATE);
        ParameterSet ps = new ParameterSet();
        prod.setParameterSet(ps);
        PriceModel pm = new PriceModel();
        prod.setPriceModel(pm);
        Product copy = new Product();

        // when
        prod.setParamatersAndPriceModel(copy);

        // then
        assertNotNull(copy.getParameterSet());
        assertNotNull(copy.getPriceModel());
    }

    @Test
    public void testGetTemplateOrSelfForReview() {
        //given
        Product prod = createProduct(1, 1, ServiceType.PARTNER_TEMPLATE);

        //when
        Product result = prod.getTemplateOrSelfForReview();

        //then
        assertEquals(result, prod);
    }

    private Product createProduct(long provisioningDate,
            long deprovisioningDate, ServiceType type) {
        Product template = new Product();
        template.setDeprovisioningDate(Long.valueOf(deprovisioningDate));
        template.setProvisioningDate(provisioningDate);
        template.setProductId("abc");
        template.setStatus(ServiceStatus.ACTIVE);
        template.setType(type);
        template.setConfiguratorUrl(CONF_URL);
        template.setAutoAssignUserEnabled(Boolean.FALSE);
        return template;
    }

    private PlatformUser createUser(boolean userIsAdmin) {
        Organization org = new Organization();
        PlatformUser user = new PlatformUser();
        user.setOrganization(org);
        user.setKey(8138);
        if (userIsAdmin) {
            user.setAssignedRoles(createRoleAssignment(user,
                    UserRoleType.ORGANIZATION_ADMIN));
        }
        return user;
    }

    private Set<RoleAssignment> createRoleAssignment(PlatformUser user,
            UserRoleType roleType) {
        Set<RoleAssignment> roles = new HashSet<RoleAssignment>();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setUser(user);
        ra.setRole(new UserRole(roleType));
        roles.add(ra);
        return roles;
    }

    private Product createProductTemplate(ServiceAccessType accessType) {
        TechnicalProduct techProduct = new TechnicalProduct();
        techProduct.setAccessType(accessType);
        Product product = new Product();
        product.setType(ServiceType.TEMPLATE);
        product.setTechnicalProduct(techProduct);
        product.setTemplate(null);
        return product;
    }

    private void createPartnerSubscription(PlatformUser user,
            Product partnerTemplate, boolean userHasLicense) {
        Subscription partnerSubscription = new Subscription();
        Product partnerSubscriptionCopy = partnerTemplate.copyForSubscription(
                user.getOrganization(), partnerSubscription);
        partnerSubscriptionCopy.setType(ServiceType.PARTNER_SUBSCRIPTION);

        partnerSubscription.setProduct(partnerSubscriptionCopy);
        partnerSubscription.setStatus(SubscriptionStatus.ACTIVE);

        user.getOrganization().setSubscriptions(
                Arrays.asList(new Subscription[] { partnerSubscription }));

        if (userHasLicense) {
            createUsageLicencse(user, partnerSubscription);
        }
    }

    private void createUsageLicencse(PlatformUser user, Subscription sub) {
        List<UsageLicense> usageLicenseList = new ArrayList<UsageLicense>();
        UsageLicense usageLicense = new UsageLicense();
        usageLicense.setUser(user);
        usageLicenseList.add(usageLicense);
        sub.setUsageLicenses(usageLicenseList);
    }
}
