/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.BillingResults;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Test cases for the class SharesDataRetrievalServiceBean
 * 
 * @author cheld
 * 
 */
@SuppressWarnings("boxing")
public class SharesDataRetrievalServiceBeanCutOffDayIT extends EJBTestBase {
    private static final int NUM_BILLING_RESULTS = 10;
    private static final long DAY_MILLIS = 86400000L;
    private static final long PERIOD_START = System.currentTimeMillis();
    private static final long PERIOD_END = System.currentTimeMillis()
            + DAY_MILLIS;
    private static final String SUPPLIER_ID = "supplierId";
    private static final String CUSTOMER_ID = "customerId";
    private static final String RESALE_PRODUCT_ID = "resale product id";
    private static Set<Long> EXPECTED_SUBSCRIPTION_KEYS = new HashSet<Long>();
    private static long SUPPLIER_KEY;
    private static final String CURRENCY_EURO = "EUR";
    private static final String CURRENCY_DOLLAR = "USD";

    private DataService ds;
    private SharesDataRetrievalServiceLocal dao;

    Organization seller;
    Organization marketplaceOwner1, marketplaceOwner2;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(new SharesDataRetrievalServiceBean());
        ds = container.get(DataService.class);
        dao = container.get(SharesDataRetrievalServiceLocal.class);
    }

    @Before
    public void clear() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                ds.createQuery("DELETE FROM BillingResult br").executeUpdate();
                return null;
            }
        });
    }

    @Test
    public void loadBillingResultsForBroker() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END,
                NUM_BILLING_RESULTS);

        // when loading billing result
        List<BillingResult> billingResults = dao.loadBillingResultsForBroker(
                seller.getKey(), PERIOD_START, PERIOD_END);

        // then given
        assertThat(billingResults, hasItems(NUM_BILLING_RESULTS));
    }

    @Test
    public void loadBillingResultsForBroker_NoResults() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END,
                NUM_BILLING_RESULTS);

        // when loading billing result
        List<BillingResult> billingResults = dao.loadBillingResultsForBroker(
                seller.getKey(), PERIOD_END, PERIOD_END + 1000);

        // then given
        assertThat(billingResults, hasNoItems());
    }

    @Test
    public void loadBillingResultsForReseller() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END,
                NUM_BILLING_RESULTS);

        // when loading billing result
        List<BillingResult> billingResults = dao.loadBillingResultsForReseller(
                seller.getKey(), PERIOD_START, PERIOD_END);

        // then given
        assertThat(billingResults, hasItems(NUM_BILLING_RESULTS));
    }

    @Test
    public void loadBillingResultsForReseller_NoResults() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END,
                NUM_BILLING_RESULTS);

        // when loading billing result
        List<BillingResult> billingResults = dao.loadBillingResultsForReseller(
                seller.getKey(), PERIOD_END, PERIOD_END + 1000);

        // then given
        assertThat(billingResults, hasNoItems());
    }

    @Test
    public void loadBillingResultsForSupplier() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END,
                NUM_BILLING_RESULTS);

        // when loading billing result
        List<BillingResult> result = dao.loadBillingResultsForSupplier(
                seller.getKey(), PERIOD_START, PERIOD_END);

        // then given
        assertThat(result, hasItems(NUM_BILLING_RESULTS));
    }

    @Test
    public void loadBillingResultsForSupplier_NoResults() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END,
                NUM_BILLING_RESULTS);

        // when loading billing result
        List<BillingResult> result = dao.loadBillingResultsForSupplier(
                seller.getKey(), PERIOD_END, PERIOD_END + 1000);

        // then given
        assertThat(result, hasNoItems());
    }

    private void givenExistingBillingResult(final long periodStart,
            final long periodEnd, final int numberOfResults) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                seller = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                Organization cust = Organizations.createOrganization(ds);
                Product product = Products.createProduct(
                        seller.getOrganizationId(), "prodId", "techProd", ds);
                Subscription sub = Subscriptions.createSubscription(ds,
                        cust.getOrganizationId(), product);
                for (int i = 0; i < numberOfResults; i++) {
                    BillingResults.createBillingResult(ds, sub,
                            periodStart - i, periodEnd - i, BigDecimal.TEN,
                            BigDecimal.ZERO);
                }
                return null;
            }

        });
    }

    @Test
    public void loadBillingResultsForSupplier_getAllSellerProducts()
            throws Exception {
        // given
        setupLoadBillingResultsForSupplier();

        // when
        List<BillingResult> billingResults = dao.loadBillingResultsForSupplier(
                SUPPLIER_KEY, PERIOD_START, PERIOD_END);

        // then
        assertThat(billingResults, hasItems(4));
        for (BillingResult billingResult : billingResults) {
            assertTrue((EXPECTED_SUBSCRIPTION_KEYS.contains(billingResult
                    .getSubscriptionKey())));
        }
    }

    private void setupLoadBillingResultsForSupplier() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create supplier
                Organization supplier = Organizations
                        .createOrganization(SUPPLIER_ID);
                ds.persist(supplier);
                ds.flush();
                SUPPLIER_KEY = supplier.getKey();

                // create product 1
                Product supplierProduct = Products.createProduct(SUPPLIER_ID,
                        "productId", "techProductId", ds);
                ds.flush();
                supplierProduct.setProductId("productId change");
                ds.flush();

                // currency DOLLAR
                SupportedCurrency currencyDollar = SupportedCurrencies
                        .findOrCreate(ds, CURRENCY_DOLLAR);

                // customer subscribes supplier product
                Organizations.createOrganization(ds, CUSTOMER_ID,
                        OrganizationRoleType.CUSTOMER);
                Subscription subscription = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, supplierProduct);
                ds.flush();

                BillingResults.createBillingResult(ds, subscription, 0, 0,
                        PERIOD_START, PERIOD_END, BigDecimal.valueOf(12),
                        BigDecimal.ZERO, currencyDollar);
                EXPECTED_SUBSCRIPTION_KEYS.add(subscription.getKey());

                Subscription subscription2 = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, supplierProduct);
                BillingResults.createBillingResult(ds, subscription2, 0, 0,
                        PERIOD_START, PERIOD_END, BigDecimal.ZERO,
                        BigDecimal.ZERO, currencyDollar);
                EXPECTED_SUBSCRIPTION_KEYS.add(subscription2.getKey());

                // create reseller and reseller product
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER);
                Product resellerProduct = Products.createProductResaleCopy(
                        supplierProduct, reseller, ds);
                ds.flush();
                resellerProduct.setProductId(RESALE_PRODUCT_ID);
                ds.flush();

                // currency EUR
                SupportedCurrency currencyEuro = SupportedCurrencies
                        .findOrCreate(ds, CURRENCY_EURO);

                // customer subscribes reseller product
                Subscription subscription3 = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, resellerProduct);
                subscription3.setSubscriptionId("subscriptionId2");
                ds.flush();
                subscription3.setSubscriptionId("subscriptionId3");
                ds.flush();

                BillingResults.createBillingResult(ds, subscription3, 0, 0,
                        PERIOD_START, PERIOD_END, BigDecimal.valueOf(17),
                        BigDecimal.ZERO, currencyEuro);
                EXPECTED_SUBSCRIPTION_KEYS.add(subscription3.getKey());

                Subscription subscription4 = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, resellerProduct);
                ds.flush();
                BillingResults.createBillingResult(ds, subscription4, 0, 0,
                        PERIOD_START, PERIOD_END, BigDecimal.valueOf(44),
                        BigDecimal.ZERO, currencyEuro);
                EXPECTED_SUBSCRIPTION_KEYS.add(subscription4.getKey());
                return null;
            }
        });
    }

}
