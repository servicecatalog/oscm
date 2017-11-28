/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 12, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.data.Marketplaces;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.DeletionConstraintException;

/**
 * Tests whether a technical product can be cleaned up depending on the types of
 * the marketable products based on that technical product.
 * 
 * @author barzu
 */
public class TechnicalProductCleanerProductTypeTest {

    private TechnicalProductCleaner cleaner;
    private DataService ds;
    private final boolean DELETED = true;
    private final boolean NOT_DELETED = false;

    private static int keyCounter = 0;

    @Before
    public void setup() {
        ds = mock(DataService.class);
        TenantProvisioningServiceBean tenantProvisioning = mock(TenantProvisioningServiceBean.class);
        cleaner = new TechnicalProductCleaner(ds, tenantProvisioning);

        Query query = mock(Query.class);
        doReturn(query).when(ds).createNamedQuery(anyString());
    }

    private TechnicalProduct givenTechnicalProduct(Product... products) {
        TechnicalProduct tp = new TechnicalProduct();
        List<Product> prdList = new ArrayList<Product>();
        for (Product p : products) {
            prdList.add(p);
        }
        tp.setProducts(prdList);
        return tp;
    }

    private Product givenProduct(boolean deleted, ServiceType type) {
        Product p = new Product();
        p.setKey(keyCounter++);
        p.setStatus(deleted ? ServiceStatus.DELETED : ServiceStatus.ACTIVE);
        p.setType(type);

        givenQueryForDeletingLandingPageProduct();

        return p;
    }

    private Product givenProductWithNotCleanedFeatureService(boolean deleted,
            ServiceType type) throws Exception {

        Product p = new Product();
        p.setVendor(new Organization());
        p.setKey(keyCounter++);
        p.setStatus(deleted ? ServiceStatus.DELETED : ServiceStatus.ACTIVE);
        p.setType(type);

        List<Product> plist = new ArrayList<Product>();
        plist.add(p);
        Marketplace marketplace = Marketplaces.createMarketplace(p.getVendor(),
                "TEST", true, ds);
        Marketplaces.createLandingpageProducts(
                marketplace.getPublicLandingpage(), plist, ds);

        CatalogEntry ce = new CatalogEntry();
        ce.setMarketplace(marketplace);
        ce.setProduct(p);
        p.getCatalogEntries().add(ce);

        return p;

    }

    private Subscription givenSubscription(boolean deletable) {
        Subscription s = new Subscription();
        s.setStatus(deletable ? SubscriptionStatus.DEACTIVATED
                : SubscriptionStatus.ACTIVE);
        return s;
    }

    @Test
    public void cleanupTechnicalProduct_Deleted() throws Exception {
        // given
        Product p1 = givenProduct(true, ServiceType.TEMPLATE);
        Product p2 = givenProduct(true, ServiceType.CUSTOMER_SUBSCRIPTION);
        Product p3 = givenProduct(true, ServiceType.CUSTOMER_TEMPLATE);
        Product p4 = givenProduct(true, ServiceType.PARTNER_SUBSCRIPTION);
        Product p5 = givenProduct(true, ServiceType.PARTNER_TEMPLATE);
        Product p6 = givenProduct(true, ServiceType.SUBSCRIPTION);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p1, p2, p3, p4,
                p5, p6));

        // then
        verify(ds, times(1)).remove(eq(p1));
        verify(ds, times(1)).remove(eq(p2));
        verify(ds, times(1)).remove(eq(p3));
        verify(ds, times(1)).remove(eq(p4));
        verify(ds, times(1)).remove(eq(p5));
        verify(ds, times(1)).remove(eq(p6));
    }

    @Test(expected = DeletionConstraintException.class)
    public void cleanupTechnicalProduct_NotDeleted_Template() throws Exception {
        // given
        Product p = givenProduct(false, ServiceType.TEMPLATE);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));
    }

    @Test(expected = DeletionConstraintException.class)
    public void cleanupTechnicalProduct_NotDeleted_PartnerTemplate()
            throws Exception {
        // given
        Product p = givenProduct(NOT_DELETED, ServiceType.PARTNER_TEMPLATE);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));
    }

    @Test(expected = DeletionConstraintException.class)
    public void cleanupTechnicalProduct_NotDeleted_CustomerTemplate()
            throws Exception {
        // given
        Product p = givenProduct(NOT_DELETED, ServiceType.CUSTOMER_TEMPLATE);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));
    }

    @Test(expected = DeletionConstraintException.class)
    public void cleanupTechnicalProduct_NotDeleted_Subscription()
            throws Exception {
        // given
        Product p = givenProduct(NOT_DELETED, ServiceType.SUBSCRIPTION);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));
    }

    @Test(expected = DeletionConstraintException.class)
    public void cleanupTechnicalProduct_NotDeleted_PartnerSubscription()
            throws Exception {
        // given
        Product p = givenProduct(NOT_DELETED, ServiceType.PARTNER_SUBSCRIPTION);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));
    }

    @Test(expected = DeletionConstraintException.class)
    public void cleanupTechnicalProduct_NotDeleted_CustomerSubscriptino()
            throws Exception {
        // given
        Product p = givenProduct(NOT_DELETED, ServiceType.CUSTOMER_SUBSCRIPTION);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));
    }

    @Test
    public void cleanupTechnicalProduct_OwningSubscription_Deletable()
            throws Exception {
        // given
        Product p = givenProduct(DELETED, ServiceType.PARTNER_SUBSCRIPTION);
        Subscription s = givenSubscription(true);
        p.setOwningSubscription(s);

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));

        // then
        verify(ds, times(1)).remove(eq(p));
        verify(ds, times(1)).remove(eq(s));
    }

    @Test(expected = DeletionConstraintException.class)
    public void cleanupTechnicalProduct_OwningSubscription_NotDeletable()
            throws Exception {
        // given
        Product p = givenProduct(true, ServiceType.PARTNER_SUBSCRIPTION);
        p.setOwningSubscription(givenSubscription(false));

        // when
        cleaner.cleanupTechnicalProduct(givenTechnicalProduct(p));
    }

    @Test
    public void cleanupTechnicalProduct_withLandingPageProducts()
            throws Exception {
        // given
        Product p = givenProductWithNotCleanedFeatureService(true,
                ServiceType.CUSTOMER_TEMPLATE);
        TechnicalProduct tp = givenTechnicalProduct(p);
        Query query = givenQueryForDeletingLandingPageProduct();

        // when
        cleaner.cleanupTechnicalProduct(tp);

        // then
        verify(ds, times(1)).createNamedQuery(
                "LandingpageProduct.deleteLandingpageProductForProduct");
        verify(query, times(1)).executeUpdate();

    }

    private Query givenQueryForDeletingLandingPageProduct() {
        Query query = mock(Query.class);
        doReturn(query).when(ds).createNamedQuery(
                "LandingpageProduct.deleteLandingpageProductForProduct");
        doReturn(query).when(query).setParameter(anyString(), anyString());
        doReturn(Integer.valueOf(1)).when(query).executeUpdate();
        return query;
    }

    @Test
    public void moveProductsOfType_noTypeProvided() {
        // given
        Product p1 = givenProduct(DELETED, ServiceType.PARTNER_SUBSCRIPTION);
        List<Product> products = Arrays.asList(p1);

        // when
        List<Product> result = cleaner.moveProductsOfType(products);

        // then
        assertTrue(result.isEmpty());
        assertTrue(!products.isEmpty());
    }

    @Test
    public void moveProductsOfType_nullAsTypeProvided() {
        // given
        Product p1 = givenProduct(DELETED, ServiceType.PARTNER_SUBSCRIPTION);
        List<Product> products = Arrays.asList(p1);
        ServiceType[] nullTypes = null;

        // when
        List<Product> result = cleaner.moveProductsOfType(products, nullTypes);

        // then
        assertTrue(result.isEmpty());
        assertTrue(!products.isEmpty());
    }

    @Test
    public void moveProductsOfType_subscriptionCopies() {
        // given
        Product p1 = givenProduct(DELETED, ServiceType.PARTNER_SUBSCRIPTION);
        Product p2 = givenProduct(DELETED, ServiceType.CUSTOMER_SUBSCRIPTION);
        Product p3 = givenProduct(DELETED, ServiceType.SUBSCRIPTION);
        List<Product> products = new ArrayList<Product>();
        products.add(p1);
        products.add(p2);
        products.add(p3);

        // when
        List<Product> result = cleaner.moveProductsOfType(products,
                ServiceType.PARTNER_SUBSCRIPTION,
                ServiceType.CUSTOMER_SUBSCRIPTION, ServiceType.SUBSCRIPTION);

        // then
        assertEquals(3, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
        assertTrue(result.contains(p3));
        assertTrue(products.isEmpty());
    }

    @Test
    public void moveProductsOfType_subscriptionCopies2() {
        // given
        Product p1 = givenProduct(DELETED, ServiceType.PARTNER_SUBSCRIPTION);
        Product p2 = givenProduct(DELETED, ServiceType.CUSTOMER_SUBSCRIPTION);
        Product p3 = givenProduct(DELETED, ServiceType.SUBSCRIPTION);
        Product p4 = givenProduct(DELETED, ServiceType.TEMPLATE);
        List<Product> products = new ArrayList<Product>();
        products.add(p1);
        products.add(p2);
        products.add(p3);
        products.add(p4);

        // when
        List<Product> result = cleaner.moveProductsOfType(products,
                ServiceType.PARTNER_SUBSCRIPTION,
                ServiceType.CUSTOMER_SUBSCRIPTION, ServiceType.SUBSCRIPTION);

        // then
        assertEquals(3, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
        assertTrue(result.contains(p3));
        assertEquals(1, products.size());
        assertTrue(products.contains(p4));
    }
}
