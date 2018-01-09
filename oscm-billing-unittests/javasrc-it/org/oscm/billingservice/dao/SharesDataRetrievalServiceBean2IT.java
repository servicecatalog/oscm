/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.oscm.test.matchers.JavaMatchers.hasOneItem;
import static org.oscm.test.matchers.JavaMatchers.isNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.BillingResults;
import org.oscm.test.data.BillingSharesResults;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Test cases for the class SharesDataRetrievalServiceBean
 * 
 * @author cheld
 * 
 */
@SuppressWarnings("boxing")
public class SharesDataRetrievalServiceBean2IT extends EJBTestBase {

    // In order to avoid fiddling with dates in productive code (like
    // creation-time), we simple use the current time and add and subtract
    // something
    private static final int OFFSET = 1000 * 6000;
    private static long TIME_OBJECT_CREATION = System.currentTimeMillis();
    private static long PERIOD_END = TIME_OBJECT_CREATION + OFFSET;
    private static long TIME_BEFORE_PERIOD_END = TIME_OBJECT_CREATION - OFFSET;
    private static final long TIME_AFTER_OBJECT_CREATION = TIME_OBJECT_CREATION
            + OFFSET;
    /**
     * 
     */
    private static final String SUBSCRIPTION_ID = "new subscriptionId";
    private static final long PERIOD_START = 1000;
    private static final String MARKETPLACE_ID1 = "marketplaceId1";
    private static final String MARKETPLACE_ID2 = "marketplaceId2";
    private static final String MARKETPLACE_ID3 = "marketplaceId3";
    private static final String SUPPLIER_ID = "supplierId";
    private static final String RESELLER_ID = "resellerId";
    private static final String ANY_URL = "aUrl";
    private static final String CUSTOMER_ID = "customerId";
    private static final String RESALE_PRODUCT_ID = "resale product id";
    private static final String ORGANIZATION_NAME = "organizationName";
    private static long SUBSCRIPTION_KEY;
    private static Set<Long> EXPECTED_SUBSCRIPTION_KEYS = new HashSet<Long>();
    private static long SERVICE_KEY;
    private static long SUPPLIER_KEY;
    private static final String CURRENCY_EURO = "EUR";
    private static final String CURRENCY_DOLLAR = "USD";
    private static long MARKETPLACE_KEY1;
    private static long MARKETPLACE_KEY2;

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

    /**
     * Load non existing organization history. null is returned instead of
     * exception
     * 
     * @throws Exception
     */
    @Test
    public void loadLastOrganizationHistory_nonExisting() {
        // given
        final long nonExistingKey = 1L;

        // when loading
        OrganizationHistory organization = dao
                .loadLastOrganizationHistory(nonExistingKey);

        // then result is null
        assertNull(organization);
    }

    /**
     * Load organization history.
     * 
     * @throws Exception
     */
    @Test
    public void loadOrganizationHistory() throws Exception {
        // given supplier from setup
        Organization org = setupLoadOrganizationHistory();

        // when loading
        OrganizationHistory organization = dao.loadLastOrganizationHistory(org
                .getKey());

        // than latest information is loaded
        assertEquals(org.getOrganizationId(), organization.getOrganizationId());
        assertEquals(org.getName(), organization.getOrganizationName());
        assertEquals(org.getAddress(), organization.getAddress());
        assertEquals(org.getEmail(), organization.getEmail());
        assertEquals(org.getCutOffDay(), 1);
    }

    private Organization setupLoadOrganizationHistory() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(ds);
                org.setOrganizationId("orgId");
                org.setName("aName");
                org.setAddress("anAdress");
                org.setEmail("email@server.com");
                org.setCutOffDay(1);
                return org;
            }
        });
    }

    /**
     * Load organization history. The latest data (e.g. the current email) must
     * be used. Also, a delete of the organization must not matter (so it is not
     * possible to load the organization directly).
     * 
     * @throws Exception
     */
    @Test
    public void loadOrganizationHistory_latestData() throws Exception {

        // given a new organization. Update and delete it
        final Organization org = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.createOrganization(ds);
                org.setEmail("email@server.com");
                ds.persist(org);
                org.setEmail("newEmail@server.com");
                ds.persist(org);
                Organizations.removeOrganization(ds, org.getOrganizationId());
                return org;
            }
        });

        // when loading
        OrganizationHistory organization = dao.loadLastOrganizationHistory(org
                .getKey());

        // then latest information is still available
        assertEquals("newEmail@server.com", organization.getEmail());
    }

    @Test
    public void loadAllBrokerKeysWithinPeriod_afterRoleAssignment()
            throws Exception {

        // given an organization that is assigned with role broker
        Organization org = givenOrganizationWithRoleBroker();

        // when loading all brokers after role assignment
        List<Long> brokerKeys = dao
                .loadAllBrokerKeysWithinPeriod(TIME_AFTER_OBJECT_CREATION);

        // then broker is found
        assertEquals(Long.valueOf(org.getKey()),
                Long.valueOf(brokerKeys.get(0)));
    }

    @Test
    public void loadAllBrokerKeysWithinPeriod_beforeRoleAssignment()
            throws Exception {

        // given an organization that is assigned with role broker
        givenOrganizationWithRoleBroker();

        // when loading all brokers before role assignment
        List<Long> brokerKeys = dao
                .loadAllBrokerKeysWithinPeriod(TIME_OBJECT_CREATION);

        // then
        assertTrue(brokerKeys.isEmpty());
    }

    private Organization givenOrganizationWithRoleBroker() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations
                        .createOrganization("orgWithTimestamps");
                org.setHistoryModificationTime(TIME_OBJECT_CREATION);
                ds.persist(org);
                ds.flush();
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(OrganizationRoleType.BROKER);
                ds.persist(role);
                OrganizationToRole orgToRole = new OrganizationToRole();
                orgToRole.setOrganization(org);
                orgToRole.setOrganizationRole(role);
                orgToRole.setHistoryModificationTime(TIME_OBJECT_CREATION);
                ds.persist(orgToRole);
                ds.flush();
                org.setHistoryModificationTime(TIME_OBJECT_CREATION);
                ds.persist(org);
                return org;
            }
        });
    }

    @Test
    public void loadAllBrokerKeysWithinPeriod_multipleOrganizations()
            throws Exception {

        // given three organization. two have role broker
        givenOrganizationsWithRoles(OrganizationRoleType.BROKER,
                OrganizationRoleType.RESELLER, OrganizationRoleType.BROKER);

        // when loading all brokers
        List<Long> brokerKeys = dao.loadAllBrokerKeysWithinPeriod(System
                .currentTimeMillis());

        // then
        assertEquals(2, brokerKeys.size());
    }

    private void givenOrganizationsWithRoles(
            final OrganizationRoleType... roles) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (OrganizationRoleType role : roles) {
                    Organizations.createOrganization(ds, role);
                }
                return null;
            }
        });
    }

    @Test
    public void loadAllResellerKeysWithinPeriod_multipleOrganizations()
            throws Exception {

        // given three organization. two have role broker
        givenOrganizationsWithRoles(OrganizationRoleType.BROKER,
                OrganizationRoleType.RESELLER, OrganizationRoleType.BROKER);

        // when loading all brokers
        List<Long> resellerKeys = dao.loadAllResellerKeysWithinPeriod(System
                .currentTimeMillis());

        // then
        assertEquals(1, resellerKeys.size());
    }

    @Test
    public void loadAllSupplierKeysWithinPeriod_multipleOrganizations()
            throws Exception {

        // given three organization. two have role broker
        givenOrganizationsWithRoles(OrganizationRoleType.BROKER,
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.BROKER);

        // when loading all brokers
        List<Long> supplierKeys = dao.loadAllSupplierKeysWithinPeriod(System
                .currentTimeMillis());

        // then
        assertEquals(1, supplierKeys.size());
    }

    @Test
    public void loadBillingResultsForBroker() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END);

        // when loading billing result
        List<BillingResult> billingResults = dao.loadBillingResultsForBroker(
                seller.getKey(), PERIOD_START, PERIOD_END);

        // then given
        assertThat(billingResults, hasOneItem());
        assertEquals(PERIOD_START, billingResults.get(0).getPeriodStartTime());
        assertEquals(PERIOD_END, billingResults.get(0).getPeriodEndTime());
    }

    @Test
    public void loadBillingResultsForReseller_negativ() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END);

        // when loading billing result
        List<BillingResult> billingResults = dao.loadBillingResultsForReseller(
                seller.getKey(), PERIOD_START, PERIOD_END);

        // then given
        assertThat(billingResults, hasOneItem());
    }

    @Test
    public void loadBillingResultsForSupplier() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END);

        // when loading billing result
        List<BillingResult> result = dao.loadBillingResultsForSupplier(
                seller.getKey(), PERIOD_START, PERIOD_END);

        // then given
        assertThat(result, hasOneItem());
    }

    @Test
    public void loadBillingResultsForSupplier_wrongOrgKey() throws Exception {
        // given
        givenExistingBillingResult(PERIOD_START, PERIOD_END);
        long wrongOrgKey = 1L;

        // when loading billing result with wrong supplier key
        List<BillingResult> billingResults = dao.loadBillingResultsForSupplier(
                wrongOrgKey, PERIOD_START, PERIOD_END);

        // then
        assertThat(billingResults, hasNoItems());
    }

    @Test
    public void loadBillingResultsForSupplier_wrongPeriod() throws Exception {

        givenExistingBillingResult(PERIOD_START, PERIOD_END);
        long wrongStartPeriod = PERIOD_END + 1;

        // when loading billing result with wrong start period
        List<BillingResult> result = dao.loadBillingResultsForSupplier(
                seller.getKey(), wrongStartPeriod, PERIOD_END);

        // then
        assertThat(result, hasNoItems());
    }

    private void givenExistingBillingResult(final long periodStart,
            final long periodEnd) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                seller = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                Organization cust = Organizations.createOrganization(ds);
                Product product = Products.createProduct(seller, "techProd",
                        false, "prodId", "priceModelId", ds);
                Subscription sub = Subscriptions.createSubscription(ds,
                        cust.getOrganizationId(), product);
                BillingResults.createBillingResult(ds, sub, periodStart,
                        periodEnd, BigDecimal.TEN, BigDecimal.ZERO);
                return null;
            }

        });
    }

    @Test
    public void loadBillingResultsForSupplier_multipleBillingResults()
            throws Exception {
        // given
        givenTwoBillingResults();

        // when loading billing result
        List<BillingResult> billingResults = dao.loadBillingResultsForSupplier(
                seller.getKey(), PERIOD_START, PERIOD_END);

        // then
        assertThat(billingResults, hasItems(2));
    }

    private void givenTwoBillingResults() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                seller = Organizations.createOrganization(ds,
                        OrganizationRoleType.BROKER);
                Organization cust = Organizations.createOrganization(ds);
                Product product = Products.createProduct(
                        seller.getOrganizationId(), "prodId", "techProd", ds);
                Product product2 = Products.createProduct(
                        seller.getOrganizationId(), "prodId2", "techProd", ds);
                Subscription sub = Subscriptions.createSubscription(ds,
                        cust.getOrganizationId(), product);
                Subscription sub2 = Subscriptions.createSubscription(ds,
                        cust.getOrganizationId(), product2);
                BillingResults.createBillingResult(ds, sub, PERIOD_START,
                        PERIOD_END, BigDecimal.TEN, BigDecimal.ZERO);
                BillingResults.createBillingResult(ds, sub2, PERIOD_START,
                        PERIOD_END, BigDecimal.TEN, BigDecimal.ZERO);
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

    @Test
    public void loadOrganizationHistoryRoles_before() throws Exception {
        // given
        Organization organization = givenOrganizationWithRoleBroker();

        // when
        List<OrganizationRole> roles = dao.loadOrganizationHistoryRoles(
                organization.getKey(), TIME_OBJECT_CREATION);

        // then
        assertNotNull(roles);
        assertEquals(0, roles.size());
    }

    @Test
    public void loadOrganizationHistoryRoles_after() throws Exception {
        // given
        Organization organization = givenOrganizationWithRoleBroker();

        // when
        List<OrganizationRole> roles = dao.loadOrganizationHistoryRoles(
                organization.getKey(), TIME_AFTER_OBJECT_CREATION);

        // then
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals(OrganizationRoleType.BROKER, roles.get(0).getRoleName());
    }

    @Test
    public void loadMarketplaceHistory_beforeSubscription() throws Exception {
        // given
        setupLoadMarketplaceHistory();

        // when
        MarketplaceHistory marketplace = dao
                .loadMarketplaceHistoryBySubscriptionKey(SUBSCRIPTION_KEY,
                        TIME_OBJECT_CREATION);

        // then
        assertNull(marketplace);
    }

    @Test
    public void loadMarketplaceHistory_afterSubscription() throws Exception {
        // given
        setupLoadMarketplaceHistory();

        // when
        MarketplaceHistory marketplace = dao
                .loadMarketplaceHistoryBySubscriptionKey(SUBSCRIPTION_KEY,
                        TIME_AFTER_OBJECT_CREATION);

        // then
        assertNotNull(marketplace);
        assertEquals(MARKETPLACE_ID1, marketplace.getDataContainer()
                .getMarketplaceId());
    }

    private void setupLoadMarketplaceHistory() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create supplier
                Organization supplier = Organizations
                        .createOrganization(SUPPLIER_ID);
                ds.persist(supplier);
                ds.flush();

                // create marketplace 1
                Marketplace marketplace = Marketplaces.createGlobalMarketplace(
                        supplier, MARKETPLACE_ID1, ds);
                ds.flush();
                marketplace.setBrandingUrl(ANY_URL);
                ds.flush();

                // create two products
                // product 1
                Product product1 = Products.createProduct(SUPPLIER_ID,
                        "productId", "techProductId", ds);
                ds.flush();
                product1.setProductId("productId change");
                ds.flush();

                // create a subscription for product 1
                Organizations.createOrganization(ds, CUSTOMER_ID,
                        OrganizationRoleType.CUSTOMER);
                Subscription subscription = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, product1.getProductId(), "SUB1",
                        TIME_OBJECT_CREATION, System.currentTimeMillis(),
                        supplier, 1);
                subscription.setMarketplace(marketplace);
                ds.flush();
                subscription.setSubscriptionId("subscriptionId2");
                ds.flush();
                subscription.setSubscriptionId("subscriptionId3");
                ds.flush();
                SUBSCRIPTION_KEY = subscription.getKey();

                // create marketplace 2
                Marketplace marketplace2 = Marketplaces
                        .createGlobalMarketplace(supplier, MARKETPLACE_ID2, ds);
                ds.flush();

                // product 2
                Product product2 = Products.createProduct(SUPPLIER_ID,
                        "productId2", "techProductId", ds);
                ds.flush();

                // create again new subscription for product 2
                Subscription subscription2 = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, product2.getProductId(), "SUB1",
                        TIME_OBJECT_CREATION, System.currentTimeMillis(),
                        supplier, 1);
                subscription2.setMarketplace(marketplace2);
                ds.flush();
                return null;
            }
        });
    }

    /**
     * Query the product of the supplier. The model is as follows:<br>
     * Subscription -> Product (subscription copy) -> Product (supplier).<br>
     * In this case the top level product must be retrieved.
     */
    @Test
    public void loadProductOfVendor_supplier() throws Exception {
        // given
        Subscription createdSubscription = setupLoadProductOfVendor();

        // when loading corresponding reseller product for this subscription
        ProductHistory loadedVendorProduct = dao.loadProductOfVendor(
                createdSubscription.getKey(), createdSubscription
                        .getPriceModel().getKey(), PERIOD_END);

        // then
        assertEquals(createdSubscription.getProduct().getTemplate().getKey(),
                loadedVendorProduct.getObjKey());
    }

    /**
     * Create a subscription of a supplier product with a couple of additional
     * history entries.
     */
    private Subscription setupLoadProductOfVendor() throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Organization supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                Product product = Products.createProduct(supplier,
                        "techProductId", true, "productId", "priceModelId", ds);
                ds.flush();
                product.setProductId("new prodId for additional history entry");
                ds.flush();
                Organization customer = Organizations.createOrganization(ds);
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), product);
                return sub;
            }
        });
    }

    /**
     * Try to load a product of a subscription. However, the end period is
     * before the subscription was created.
     */
    @Test
    public void loadProductOfVendor_endPeriodBeforeSubscriptionIsCreated()
            throws Exception {
        // given
        Subscription createdSubscription = setupLoadProductOfVendor();

        // when loading corresponding reseller product for this subscription
        ProductHistory loadedVendorProduct = dao.loadProductOfVendor(
                createdSubscription.getKey(), createdSubscription
                        .getPriceModel().getKey(), TIME_BEFORE_PERIOD_END);

        // then
        assertThat(loadedVendorProduct, isNullValue());
    }

    /**
     * Query the product of the supplier. The model is as follows:<br>
     * Subscription -> Product (subscription copy) -> Product (reseller copy) ->
     * Product (supplier).<br>
     * In this case the product in the middle must be retrieved. Not the top
     * level product.
     */
    @Test
    public void loadProductOfVendor_reseller() throws Exception {
        // given
        Subscription createdSubscription = setupLoadProductOfVendorForReseller();

        // when loading corresponding reseller product for this subscription
        ProductHistory loadedVendorProduct = dao.loadProductOfVendor(
                createdSubscription.getKey(), createdSubscription
                        .getPriceModel().getKey(), PERIOD_END);

        // then
        assertEquals(createdSubscription.getProduct().getTemplate().getKey(),
                loadedVendorProduct.getObjKey());
    }

    /**
     * Create a subscription of a reseller product with a couple of additional
     * history entries.
     */
    private Subscription setupLoadProductOfVendorForReseller() throws Exception {
        final Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {

                Organization supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                Product product = Products.createProduct(supplier,
                        "techProductId", true, "productId", "priceModelId", ds);
                ds.flush();
                product.setProductId("new prodId");
                ds.flush();

                // product resale copy with one additional history entry
                Organization reseller = Organizations.createOrganization(ds,
                        OrganizationRoleType.RESELLER);
                Product resaleCopy = Products.createProductResaleCopy(product,
                        reseller, ds);
                ds.flush();
                resaleCopy.setProductId(RESALE_PRODUCT_ID);
                ds.flush();

                // subscription
                Organization customer = Organizations.createOrganization(ds);
                Subscription sub = Subscriptions.createSubscription(ds,
                        customer.getOrganizationId(), resaleCopy);
                return sub;
            }
        });
        return sub;
    }

    @Test
    public void loadSupportedCurrencies_NoCurrency() {
        // when
        List<String> currencies = dao.loadSupportedCurrencies();

        // then
        assertNotNull(currencies);
        assertEquals(0, currencies.size());
    }

    @Test
    public void loadSupportedCurrencies() throws Exception {
        // given
        setupLoadSupportedCurrencies();

        // when
        List<String> currencies = dao.loadSupportedCurrencies();

        // then
        assertNotNull(currencies);
        assertEquals(2, currencies.size());
        assertEquals(CURRENCY_EURO, currencies.get(0));
        assertEquals(CURRENCY_DOLLAR, currencies.get(1));
    }

    private void setupLoadSupportedCurrencies() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency euro = new SupportedCurrency(CURRENCY_EURO);
                ds.persist(euro);
                SupportedCurrency dollar = new SupportedCurrency(
                        CURRENCY_DOLLAR);
                ds.persist(dollar);
                return null;
            }
        });
    }

    @Test
    public void loadMarketplaceIds_NoOwnMarketplaceFound() throws Exception {
        // given
        Organization mpOwner = setupMarketplaces();
        long otherMpOwnerOrgKey = mpOwner.getKey() + 9999;
        // when
        List<Long> marketplaceKeys = dao.loadMarketplaceKeys(
                otherMpOwnerOrgKey, TIME_OBJECT_CREATION);

        // then
        assertNotNull(marketplaceKeys);
        assertEquals(0, marketplaceKeys.size());
    }

    @Test
    public void loadMarketplaceIds_forOwnOrganization() throws Exception {
        // given
        Organization mpOwner = setupMarketplaces();

        // when
        List<Long> marketplaceKeys = dao.loadMarketplaceKeys(mpOwner.getKey(),
                TIME_AFTER_OBJECT_CREATION);

        // then
        assertNotNull(marketplaceKeys);
        assertEquals(2, marketplaceKeys.size());
        assertTrue(marketplaceKeys.contains(MARKETPLACE_KEY1));
        assertTrue(marketplaceKeys.contains(MARKETPLACE_KEY2));
    }

    /**
     * Create two organizations and threemarketplaces.
     * 
     * @return The supplier organization. It is the marketplace owner of two
     *         marketplaces.
     */
    private Organization setupMarketplaces() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplierOrg = Organizations
                        .createOrganization(SUPPLIER_ID);
                ds.persist(supplierOrg);
                ds.flush();

                Marketplace marketplace = Marketplaces.createGlobalMarketplace(
                        supplierOrg, MARKETPLACE_ID1, ds);
                MARKETPLACE_KEY1 = marketplace.getKey();
                marketplace.setHistoryModificationTime(TIME_OBJECT_CREATION);

                Marketplace marketplace2 = Marketplaces
                        .createGlobalMarketplace(supplierOrg, MARKETPLACE_ID2,
                                ds);
                MARKETPLACE_KEY2 = marketplace2.getKey();
                marketplace2.setHistoryModificationTime(TIME_OBJECT_CREATION);

                Organization resellerOrg = Organizations
                        .createOrganization(RESELLER_ID);
                ds.persist(resellerOrg);
                ds.flush();

                Marketplace marketplace3 = Marketplaces
                        .createGlobalMarketplace(resellerOrg, MARKETPLACE_ID3,
                                ds);
                marketplace3.setHistoryModificationTime(TIME_OBJECT_CREATION);

                return supplierOrg;
            }
        });
    }

    @Test
    public void loadSubscriptionHistoryWithinPeriod_before() throws Exception {
        // given
        setupLoadSubscriptionHistoryWithinPeriod();

        // when
        SubscriptionHistory subscription = dao
                .loadSubscriptionHistoryWithinPeriod(SUBSCRIPTION_KEY,
                        PERIOD_START);

        // then
        assertNull(subscription);
    }

    @Test
    public void loadSubscriptionHistoryWithinPeriod_after() throws Exception {
        // given
        setupLoadSubscriptionHistoryWithinPeriod();

        // when
        SubscriptionHistory subscription = dao
                .loadSubscriptionHistoryWithinPeriod(SUBSCRIPTION_KEY,
                        PERIOD_END);

        // then
        assertEquals(SUBSCRIPTION_KEY, subscription.getObjKey());
        assertEquals(SUBSCRIPTION_ID, subscription.getDataContainer()
                .getSubscriptionId());
    }

    @Test
    public void loadSubscriptionHistoryWithinPeriod_afterTerminate_B10933() throws Exception {
        // given
        setupTerminateSubscriptionHistoryWithinPeriod();

        // when
        SubscriptionHistory subscription = dao
                .loadSubscriptionHistoryWithinPeriod(SUBSCRIPTION_KEY,
                        PERIOD_END);

        // then
        assertEquals(SUBSCRIPTION_KEY, subscription.getObjKey());
        assertEquals(SUBSCRIPTION_ID, subscription.getDataContainer()
                .getSubscriptionId());
    }

    private void setupLoadSubscriptionHistoryWithinPeriod() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create product
                Product product1 = Products.createProduct(SUPPLIER_ID,
                        "productId", "techProductId", ds);
                ds.flush();
                product1.setProductId("productId change");
                ds.flush();

                // create subscription
                Organizations.createOrganization(ds, CUSTOMER_ID,
                        OrganizationRoleType.CUSTOMER);
                Subscription subscription = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, product1);
                ds.flush();
                subscription.setSubscriptionId(SUBSCRIPTION_ID);
                ds.flush();
                SUBSCRIPTION_KEY = subscription.getKey();
                return null;
            }
        });
    }
    
    private void setupTerminateSubscriptionHistoryWithinPeriod() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create product
                Product product1 = Products.createProduct(SUPPLIER_ID,
                        "productId", "techProductId", ds);
                ds.flush();
                product1.setProductId("productId change");
                ds.flush();

                // create subscription
                Organizations.createOrganization(ds, CUSTOMER_ID,
                        OrganizationRoleType.CUSTOMER);
                Subscription subscription = Subscriptions.createSubscription(
                        ds, CUSTOMER_ID, product1);
                ds.flush();
                subscription.setSubscriptionId(SUBSCRIPTION_ID);
                ds.flush();
                Subscriptions.teminateSubscription(ds, subscription);
                ds.flush();
                SUBSCRIPTION_KEY = subscription.getKey();
                return null;
            }
        });
    }

    @Test
    public void loadSupplierHistoryOfProduct_wrongSupplierKey() {
        // given
        long wrongSupplierKey = -1;

        // when
        OrganizationHistory organization = dao
                .loadSupplierHistoryOfProduct(wrongSupplierKey);

        // then
        assertNull(organization);
    }

    @Test
    public void loadSupplierHistoryOfProduct_supplier() throws Exception {
        // given
        setupLoadSupplierHistoryOfProduct(false);

        // when
        OrganizationHistory organization = dao
                .loadSupplierHistoryOfProduct(SERVICE_KEY);

        // then
        assertNotNull(organization);
        assertEquals(SUPPLIER_ID, organization.getOrganizationId());
        assertEquals(ORGANIZATION_NAME, organization.getOrganizationName());
    }

    @Test
    public void loadSupplierHistoryOfProduct_seller() throws Exception {
        // given
        setupLoadSupplierHistoryOfProduct(true);

        // when
        OrganizationHistory organization = dao
                .loadSupplierHistoryOfProduct(SERVICE_KEY);

        // then
        assertNotNull(organization);
        assertEquals(SUPPLIER_ID, organization.getOrganizationId());
        assertEquals(ORGANIZATION_NAME, organization.getOrganizationName());
    }

    private void setupLoadSupplierHistoryOfProduct(final boolean resaleCopy)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create supplier
                Organization supplier = Organizations
                        .createOrganization(SUPPLIER_ID);
                ds.persist(supplier);
                ds.flush();

                // create role SUPPLIER
                OrganizationRole supplierRole = new OrganizationRole(
                        OrganizationRoleType.SUPPLIER);
                ds.persist(supplierRole);

                // grant organization SUPPLIER role
                Set<OrganizationToRole> roles = new HashSet<OrganizationToRole>();
                OrganizationToRole otr = new OrganizationToRole();
                otr.setOrganization(supplier);
                otr.setOrganizationRole(supplierRole);
                ds.persist(otr);
                roles.add(otr);
                supplier.setGrantedRoles(roles);
                supplier.setName(ORGANIZATION_NAME);
                ds.flush();

                // create product
                Product product1 = Products.createProduct(SUPPLIER_ID,
                        "productId", "techProductId", ds);
                ds.flush();
                product1.setProductId("productId change");
                ds.flush();
                SERVICE_KEY = product1.getKey();

                if (resaleCopy) {
                    Organization reseller = Organizations.createOrganization(
                            ds, RESELLER_ID, OrganizationRoleType.RESELLER);
                    ds.persist(reseller);
                    ds.flush();
                    reseller.setName("reseller");
                    ds.flush();

                    Product resellerProduct = Products.createProductResaleCopy(
                            product1, reseller, ds);
                    ds.persist(resellerProduct);
                    ds.flush();
                    resellerProduct.setProductId(RESALE_PRODUCT_ID);
                    ds.flush();
                    SERVICE_KEY = resellerProduct.getKey();
                }
                return null;
            }
        });
    }

    @Test
    public void loadAllMpOwnerKeysWithinPeriod_after() throws Exception {
        // given
        setupLoadAllMpOwnerKeysWithinPeriod();

        // when
        List<Long> mpOwnerKeys = dao
                .loadAllMpOwnerKeysWithinPeriod(TIME_AFTER_OBJECT_CREATION);

        // then
        // assertEquals(2, mpOwnerKeys.size());
        assertTrue(mpOwnerKeys
                .contains(Long.valueOf(marketplaceOwner1.getKey())));
        assertTrue(mpOwnerKeys
                .contains(Long.valueOf(marketplaceOwner2.getKey())));
    }

    private void setupLoadAllMpOwnerKeysWithinPeriod() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create marketplace 1
                marketplaceOwner1 = Organizations.createOrganization(ds,
                        SUPPLIER_ID, OrganizationRoleType.SUPPLIER);
                Marketplace mp1 = Marketplaces.createGlobalMarketplace(
                        marketplaceOwner1, MARKETPLACE_ID1, ds);
                mp1.setHistoryModificationTime(TIME_OBJECT_CREATION);
                ds.flush();

                // create marketplace 2
                Marketplace mp2 = Marketplaces.createGlobalMarketplace(
                        marketplaceOwner1, MARKETPLACE_ID3, ds);
                mp2.setHistoryModificationTime(TIME_OBJECT_CREATION);
                ds.flush();

                // create marketplace 3
                marketplaceOwner2 = Organizations.createOrganization(ds,
                        "new supplier", OrganizationRoleType.SUPPLIER);
                Marketplace mp3 = Marketplaces.createGlobalMarketplace(
                        marketplaceOwner2, MARKETPLACE_ID2, ds);
                mp3.setHistoryModificationTime(TIME_OBJECT_CREATION);
                ds.flush();
                return null;
            }
        });
    }

    @Test
    public void loadAllMpOwnerKeysWithinPeriod_before() throws Exception {
        // given
        setupLoadAllMpOwnerKeysWithinPeriod();

        // when
        List<Long> mpOwnerKeys = dao
                .loadAllMpOwnerKeysWithinPeriod(TIME_OBJECT_CREATION);

        // then
        assertEquals(0, mpOwnerKeys.size());
    }

    @Test
    public void loadAllMpOwnerKeysWithinPeriod_ownerChanged() throws Exception {

        // given
        givenMarketplaceWithOwnerChanged();

        // when
        List<Long> mpOwnerKeys = dao.loadAllMpOwnerKeysWithinPeriod(PERIOD_END);

        // then
        assertEquals(1, mpOwnerKeys.size());
    }

    private void givenMarketplaceWithOwnerChanged() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                // assign owner1 to mp
                Organization mpOwner1 = Organizations.createOrganization(ds,
                        SUPPLIER_ID, OrganizationRoleType.SUPPLIER);
                Marketplace mp1 = Marketplaces.createGlobalMarketplace(
                        mpOwner1, MARKETPLACE_ID1, ds);

                // switch mp-owner from owner1 to owner2
                Organization mpOwner2 = Organizations.createOrganization(ds,
                        "new supplier", OrganizationRoleType.SUPPLIER);
                mp1.setOrganization(mpOwner2);

                return null;
            }
        });
    }

    @Test
    public void loadBillingSharesResults_periodCheck() throws Exception {
        // given
        givenTwoBillingSharesResults();

        // when loading billing result
        List<BillingSharesResult> billingSharesResults = dao
                .loadBillingSharesResultForOrganization(seller.getKey(),
                        BillingSharesResultType.MARKETPLACE_OWNER,
                        PERIOD_START, PERIOD_END + 1);

        // then
        assertThat(billingSharesResults, hasItems(1));
    }

    private void givenTwoBillingSharesResults() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                seller = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                BillingSharesResults.createBillingSharesResult(ds,
                        BillingSharesResultType.MARKETPLACE_OWNER,
                        seller.getKey(), PERIOD_START, PERIOD_END);
                BillingSharesResults.createBillingSharesResult(ds,
                        BillingSharesResultType.MARKETPLACE_OWNER,
                        seller.getKey(), PERIOD_END + 1, PERIOD_END + 2);
                return null;
            }
        });
    }

    @Test
    public void loadMarketplaceHistoryWithinPeriod() throws Exception {

        // given a marketplace
        createMarketplaceHistoryEntries();

        // when loading the marketplace history after it was created
        MarketplaceHistory mp = dao.loadMarketplaceHistoryWithinPeriod(
                MARKETPLACE_KEY1, PERIOD_END);

        // then it must be found
        assertEquals(MARKETPLACE_KEY1, mp.getObjKey());
    }

    @Test
    public void loadMarketplaceHistoryWithinPeriod_beforeMpCreation()
            throws Exception {

        // given a marketplace
        createMarketplaceHistoryEntries();

        // when loading the marketplace history before it was created
        MarketplaceHistory mp = dao.loadMarketplaceHistoryWithinPeriod(
                MARKETPLACE_KEY1, TIME_BEFORE_PERIOD_END);

        // then nothing is found
        assertNull(mp);
    }

    private void createMarketplaceHistoryEntries() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                // create marketplace 1
                marketplaceOwner1 = Organizations.createOrganization(ds,
                        SUPPLIER_ID + "1", OrganizationRoleType.SUPPLIER);
                Marketplace mp1 = Marketplaces.createGlobalMarketplace(
                        marketplaceOwner1, MARKETPLACE_ID1, ds);
                ds.flush();
                MARKETPLACE_KEY1 = mp1.getKey();

                // create marketplace 2
                marketplaceOwner2 = Organizations.createOrganization(ds,
                        SUPPLIER_ID + "2", OrganizationRoleType.SUPPLIER);
                Marketplace mp2 = Marketplaces.createGlobalMarketplace(
                        marketplaceOwner2, MARKETPLACE_ID2, ds);
                ds.flush();
                MARKETPLACE_KEY2 = mp2.getKey();

                return null;
            }
        });
    }

    @Test
    public void loadBillingSharesResults_verifyPlatformOperator()
            throws Exception {
        // given
        setupLoadBillingSharesResults_verifyPlatformOperator();

        // when loading billing result
        List<BillingSharesResult> billingSharesResults = dao
                .loadBillingSharesResult(BillingSharesResultType.SUPPLIER,
                        PERIOD_START, PERIOD_END + 1);

        // then
        assertThat(billingSharesResults, hasItems(1));
    }

    private void setupLoadBillingSharesResults_verifyPlatformOperator()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // setup supplier data
                Organization supplier = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                BillingSharesResults.createBillingSharesResult(ds,
                        BillingSharesResultType.SUPPLIER, supplier.getKey(),
                        PERIOD_START, PERIOD_END);
                return null;
            }
        });
    }
}
