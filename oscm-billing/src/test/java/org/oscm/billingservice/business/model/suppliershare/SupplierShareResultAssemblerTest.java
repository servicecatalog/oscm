/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.oscm.test.matchers.JavaMatchers.hasNoItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.billingservice.business.XmlSearch;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionData;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

@SuppressWarnings("boxing")
public class SupplierShareResultAssemblerTest {

    private static final double SELLER_REVENUE_PERCENTAGE = 3.4;
    private static final double MARKETPLACE_REVENUE_PERCENTAGE = 2.5;
    private static final double OPERATOR_REVENUE_PERCENTAGE = 4.1;
    private static final int NUMBER_SUBSCRIPTIONS = 7;
    private static final int CUSTOMER = 7;
    private static final Long RESELLER_KEY = 12345L;
    private static final Long BROKER_KEY = 678910L;
    private static final Long SUPPLIER_KEY = 3135423785L;
    private static final Long MP_OWNER_KEY1 = 1000L;
    private static final Long MP_OWNER_KEY2 = 1001L;
    private static final String MP_ID1 = "MP_ID1";
    private static final String MP_ID2 = "MP_ID2";
    private static final long MP_KEY1 = 1000L;
    private static final long MP_KEY2 = 1001L;
    private static final String MARKETPLACE_OWNER = "marketplace";
    private static final String ADDRESS = "_Address_";
    private static final String NAME = "_Name_";
    private static final String EMAIL = "_Email_";
    private static final String ID = "_Id_";
    private static final long PERIOD_START_TIME = 78678647823L;
    private static final long PERIOD_END_TIME = 78678997823L;
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_USD = "USD";
    private static final String PRODUCT_ID1 = "productId_1";
    private static final String PRODUCT_ID2 = "productId_2";
    private static final String PRODUCT_ID3 = "productId_3";
    private static final String PRODUCT_ID5 = "productId_5";
    private static final String SUBSCRIPTION_ID = "subscriptionId_";
    private static final boolean PRINT_TEST_DATA = true;

    private static List<BillingResult> billingResults;
    private static SupplierShareResultAssembler supplierShareAssembler;
    private static DatatypeFactory datatypeFactory;
    private static XmlSearch xmlSearch;

    // subscriptionKey -> testdata
    private static Map<Long, BillingResultDataMock> billingResultDataMock = new LinkedHashMap<Long, BillingResultDataMock>();

    private SharesDataRetrievalServiceLocal dao = mock(SharesDataRetrievalServiceLocal.class);
    private SupplierShareResultAssembler assembler = new SupplierShareResultAssembler(
            dao);

    static class BillingResultDataMock {
        Long billingKey;
        String subscriptionId;
        Long subscriptionKey;
        String marketplaceId;
        String currencyCode;
        BigDecimal netAmount;
        ProductHistory product;

        public BillingResultDataMock(Long subscriptionKey,
                String marketplaceId, String currencyCode,
                BigDecimal netAmount, ProductHistory product) {
            this.marketplaceId = marketplaceId;
            this.billingKey = getBillingKey(subscriptionKey);
            this.subscriptionId = getSubscriptionId(subscriptionKey);
            this.subscriptionKey = subscriptionKey;
            this.currencyCode = currencyCode;
            this.netAmount = netAmount;
            this.product = product;
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        SharesDataRetrievalServiceLocal billingRetrievalService = mock(SharesDataRetrievalServiceLocal.class);
        when(
                billingRetrievalService.loadOrganizationHistoryRoles(anyLong(),
                        anyLong())).thenReturn(
                Arrays.asList(new OrganizationRole(
                        OrganizationRoleType.SUPPLIER)));
        supplierShareAssembler = spy(new SupplierShareResultAssembler(
                billingRetrievalService));

        createBillingResults();

        mockOrganizationData();
        mockOrganizationHistoryRoles();
        mockGetMarketplaceRevenueSharePercentage();
        mockGetOperatorRevenueSharePercentage();
        mockGetSellerRevenueSharePercentage();
        mockGetProductHistoryData();
        mockGetSubscriptionHistoryData();
        mockGetBillingResults();
        mockFindMarketplaceHistory();
        mockGetOrgData(CUSTOMER);
        mockXmlSearch();
        datatypeFactory = DatatypeFactory.newInstance();
    }

    private static void mockXmlSearch() {
        xmlSearch = mock(XmlSearch.class);
        doReturn(xmlSearch).when(supplierShareAssembler).newXmlSearch(
                any(BillingResult.class));
        Set<Long> pmKeys = new HashSet<Long>();
        pmKeys.add(Long.valueOf(0));
        doReturn(pmKeys).when(xmlSearch).findPriceModelKeys();
        List<BigDecimal> mockNetAmounts = new ArrayList<BigDecimal>();
        mockNetAmounts.add(BigDecimal.ONE);
        doReturn(mockNetAmounts).when(xmlSearch).retrieveNetAmounts(anyLong());
    }

    /**
     * Output of the first run
     */
    private static List<BillingResult> createBillingResults() {
        createBillingResultDataMock();

        // create Billing Result domain objects
        billingResults = new LinkedList<BillingResult>();
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            BillingResult billingResult = new BillingResult();
            billingResult.setKey(getBillingKey(subscriptionKey));
            billingResult.setSubscriptionKey(subscriptionKey);
            billingResult.setCurrency(new SupportedCurrency(
                    billingResultDataMock.get(subscriptionKey).currencyCode));
            billingResult.setNetAmount(billingResultDataMock
                    .get(subscriptionKey).netAmount);
            billingResults.add(billingResult);
        }
        return billingResults;
    }

    private static long getBillingKey(long counter) {
        return 200 + counter;
    }

    /**
     * BillingKey;SubscriptionKey;SubscriptionId;ProductId;ProductKey;Currency;
     * NetAmount<br/>
     * 201; 1; subscriptionId_1; mp1; productId_1; 1; EUR; 120 <br/>
     * 202; 2; subscriptionId_2; mp1; productId_1; 1; EUR; 210.15 <br/>
     * 203; 3; subscriptionId_3; mp1; productId_2; 2; EUR; 187.99 <br/>
     * 204; 4; subscriptionId_4; mp2; productId_5; 5; EUR; 87.19 <br/>
     * 205; 5; subscriptionId_5; mp1; productId_3; 3; EUR; 26 <br/>
     * 206; 6; subscriptionId_6; mp1; productId_3; 3; USD; 523.56 <br/>
     * 207; 7; subscriptionId_7; mp1; productId_1; 1; EUR; 222.22 <br/>
     */
    private static void createBillingResultDataMock() {
        Long subscriptionKey = 1L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                subscriptionKey, MP_ID1, CURRENCY_EUR, BigDecimal.valueOf(120),
                getProductHistory(subscriptionKey)));

        subscriptionKey = 2L;
        billingResultDataMock.put(subscriptionKey,
                new BillingResultDataMock(subscriptionKey, MP_ID1,
                        CURRENCY_EUR, BigDecimal.valueOf(210.15),
                        getProductHistory(subscriptionKey)));

        subscriptionKey = 3L;
        billingResultDataMock.put(subscriptionKey,
                new BillingResultDataMock(subscriptionKey, MP_ID1,
                        CURRENCY_EUR, BigDecimal.valueOf(187.99),
                        getProductHistory(subscriptionKey)));

        subscriptionKey = 4L;
        billingResultDataMock.put(subscriptionKey,
                new BillingResultDataMock(subscriptionKey, MP_ID2,
                        CURRENCY_EUR, BigDecimal.valueOf(87.19),
                        getProductHistory(subscriptionKey)));

        subscriptionKey = 5L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                subscriptionKey, MP_ID1, CURRENCY_EUR, BigDecimal.valueOf(26),
                getProductHistory(subscriptionKey)));

        subscriptionKey = 6L;
        billingResultDataMock.put(subscriptionKey,
                new BillingResultDataMock(subscriptionKey, MP_ID1,
                        CURRENCY_USD, BigDecimal.valueOf(523.56),
                        getProductHistory(subscriptionKey)));
        subscriptionKey = 7L;
        billingResultDataMock.put(subscriptionKey,
                new BillingResultDataMock(subscriptionKey, MP_ID1,
                        CURRENCY_EUR, BigDecimal.valueOf(222.22),
                        getProductHistory(subscriptionKey)));

        printTestData();
    }

    private static String getSubscriptionId(Long counter) {
        return SUBSCRIPTION_ID + counter.toString();
    }

    private static ProductHistory getProductHistory(long subscriptionKey) {
        ProductHistory product = new ProductHistory();

        String productId;
        long productKey;
        long templateKey;
        long vendorObjKey;

        if (subscriptionKey == 1 || subscriptionKey == 2
                || subscriptionKey == 7) {
            productId = PRODUCT_ID1;
            productKey = 1;
            templateKey = 3000;
            vendorObjKey = BROKER_KEY;
        } else if (subscriptionKey == 3) {
            productId = PRODUCT_ID2;
            productKey = 2;
            templateKey = 3001;
            vendorObjKey = SUPPLIER_KEY;
        } else if (subscriptionKey == 4) {
            productId = PRODUCT_ID5;
            productKey = 5;
            templateKey = 4001;
            vendorObjKey = BROKER_KEY;
        } else if (subscriptionKey == 5 || subscriptionKey == 6) {
            productId = PRODUCT_ID3;
            productKey = 3;
            templateKey = 3002;
            vendorObjKey = RESELLER_KEY;
        } else {
            throw new IllegalArgumentException("SubscriptionKey "
                    + subscriptionKey + " is invalid");
        }

        product.setObjKey(productKey);
        product.getDataContainer().setProductId(productId);
        product.setTemplateObjKey(templateKey);
        product.setVendorObjKey(vendorObjKey);
        return product;
    }

    private static void printTestData() {
        if (PRINT_TEST_DATA) {
            for (Long key : billingResultDataMock.keySet()) {
                BillingResultDataMock data = billingResultDataMock.get(key);
                System.out.println("billingKey : " + data.billingKey
                        + "\tsubscriptionKey : " + key + "\tsubscriptionId : "
                        + data.subscriptionId + "\tmarketplaceId : "
                        + data.marketplaceId + "\tproductId : "
                        + data.product.getDataContainer().getProductId()
                        + "\t\tproductKey : " + data.product.getObjKey()
                        + "\t\tcurrency : " + data.currencyCode
                        + "\t\tnetAmount : " + data.netAmount);
            }
        }
    }

    private static void mockOrganizationData() {
        when(
                supplierShareAssembler.billingRetrievalService
                        .loadLastOrganizationHistory(eq(SUPPLIER_KEY)))
                .thenReturn(
                        getSellerOrganizationData(SUPPLIER_KEY,
                                OfferingType.DIRECT.name()));

        when(
                supplierShareAssembler.billingRetrievalService
                        .loadLastOrganizationHistory(eq(RESELLER_KEY)))
                .thenReturn(
                        getSellerOrganizationData(RESELLER_KEY,
                                OfferingType.RESELLER.name()));

        when(
                supplierShareAssembler.billingRetrievalService
                        .loadLastOrganizationHistory(eq(BROKER_KEY)))
                .thenReturn(
                        getSellerOrganizationData(BROKER_KEY,
                                OfferingType.BROKER.name()));

        when(
                supplierShareAssembler.billingRetrievalService
                        .loadLastOrganizationHistory(eq(MP_OWNER_KEY1)))
                .thenReturn(
                        getSellerOrganizationData(MP_OWNER_KEY1,
                                MARKETPLACE_OWNER));

        when(
                supplierShareAssembler.billingRetrievalService
                        .loadLastOrganizationHistory(eq(MP_OWNER_KEY2)))
                .thenReturn(
                        getSellerOrganizationData(MP_OWNER_KEY2,
                                MARKETPLACE_OWNER));
        when(
                supplierShareAssembler.billingRetrievalService
                        .getSupportedCountryCode(anyLong())).thenReturn("US");
    }

    private static void mockGetOrgData(int number) {
        for (long orgKey = 1; orgKey <= number; orgKey++) {
            when(
                    supplierShareAssembler.billingRetrievalService
                            .loadLastOrganizationHistory(eq(orgKey)))
                    .thenReturn(getOrganizationData(orgKey));
            when(
                    supplierShareAssembler.billingRetrievalService
                            .getSupportedCountryCode(anyLong())).thenReturn(
                    "US");
        }
    }

    private static OrganizationHistory getOrganizationData(long orgKey) {

        OrganizationHistory organization = new OrganizationHistory();
        org.oscm.domobjects.OrganizationData organizationData = organization
                .getDataContainer();
        organizationData.setAddress(ADDRESS + orgKey);
        organizationData.setName(NAME + orgKey);
        organizationData.setEmail(EMAIL + orgKey);
        organizationData.setOrganizationId(ID + orgKey);
        organizationData.setLocale("GB");
        return organization;
    }

    private static OrganizationHistory getSellerOrganizationData(long key,
            String sellerType) {
        OrganizationHistory organization = new OrganizationHistory();
        organization.setObjKey(key);
        org.oscm.domobjects.OrganizationData organizationData = organization
                .getDataContainer();
        organizationData.setAddress(sellerType + ADDRESS + key);
        organizationData.setName(sellerType + NAME + key);
        organizationData.setEmail(sellerType + EMAIL + key);
        organizationData.setOrganizationId(sellerType + ID + key);
        return organization;
    }

    private static void mockOrganizationHistoryRoles() {
        when(
                supplierShareAssembler.billingRetrievalService
                        .loadOrganizationHistoryRoles(RESELLER_KEY,
                                PERIOD_END_TIME)).thenReturn(
                organizationRoles(OrganizationRoleType.RESELLER));

        when(
                supplierShareAssembler.billingRetrievalService
                        .loadOrganizationHistoryRoles(BROKER_KEY,
                                PERIOD_END_TIME)).thenReturn(
                organizationRoles(OrganizationRoleType.BROKER));

        when(
                supplierShareAssembler.billingRetrievalService
                        .loadOrganizationHistoryRoles(SUPPLIER_KEY,
                                PERIOD_END_TIME)).thenReturn(
                organizationRoles(OrganizationRoleType.SUPPLIER));

    }

    private static List<OrganizationRole> organizationRoles(
            OrganizationRoleType type) {
        List<OrganizationRole> roles = new ArrayList<OrganizationRole>();
        roles.add(new OrganizationRole(type));
        return roles;
    }

    private static void mockGetProductHistoryData() {
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            when(
                    supplierShareAssembler.billingRetrievalService
                            .loadProductOfVendor(eq(subscriptionKey),
                                    anyLong(), eq(PERIOD_END_TIME)))
                    .thenReturn(
                            billingResultDataMock.get(subscriptionKey).product);
        }
    }

    private static void mockGetSubscriptionHistoryData() {
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            when(
                    supplierShareAssembler.billingRetrievalService
                            .loadSubscriptionHistoryWithinPeriod(
                                    eq(subscriptionKey), eq(PERIOD_END_TIME)))
                    .thenReturn(subscriptionHistory(subscriptionKey));
        }
    }

    private static SubscriptionHistory subscriptionHistory(long subscriptionKey) {
        SubscriptionHistory subscription = new SubscriptionHistory();
        subscription.setKey(subscriptionKey);

        if (subscriptionKey == 7) {
            subscription.setOrganizationObjKey(1);
        } else {
            subscription.setOrganizationObjKey(subscriptionKey);
        }
        SubscriptionData dataContainer = new SubscriptionData();
        dataContainer.setSubscriptionId(Long.valueOf(subscriptionKey)
                .toString());
        subscription.setDataContainer(dataContainer);
        return subscription;
    }

    private static void mockGetBillingResults() {
        when(
                supplierShareAssembler.billingRetrievalService
                        .loadBillingResultsForSupplier(eq(SUPPLIER_KEY),
                                anyLong(), anyLong())).thenReturn(
                billingResults);
    }

    private static void mockFindMarketplaceHistory() {
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            when(
                    supplierShareAssembler.billingRetrievalService
                            .loadMarketplaceHistoryBySubscriptionKey(
                                    eq(subscriptionKey), eq(PERIOD_END_TIME)))
                    .thenReturn(marketplaceHistory(subscriptionKey));
        }
    }

    private static MarketplaceHistory marketplaceHistory(long subscriptionKey) {
        MarketplaceHistory marketplace = new MarketplaceHistory();
        if (subscriptionKey < 8 && subscriptionKey != 4 && subscriptionKey != 6) {
            marketplace.getDataContainer().setMarketplaceId(MP_ID1);
            marketplace.setOrganizationObjKey(MP_OWNER_KEY1);
            marketplace.setObjKey(MP_KEY1);
        } else {
            marketplace.getDataContainer().setMarketplaceId(MP_ID2);
            marketplace.setOrganizationObjKey(MP_OWNER_KEY2);
            marketplace.setObjKey(MP_KEY2);
        }
        return marketplace;
    }

    private static void mockGetMarketplaceRevenueSharePercentage() {
        when(
                supplierShareAssembler.billingRetrievalService
                        .loadMarketplaceRevenueSharePercentage(eq(MP_KEY1),
                                eq(PERIOD_END_TIME))).thenReturn(
                BigDecimal.valueOf(MARKETPLACE_REVENUE_PERCENTAGE));
        when(
                supplierShareAssembler.billingRetrievalService
                        .loadMarketplaceRevenueSharePercentage(eq(MP_KEY2),
                                eq(PERIOD_END_TIME))).thenReturn(
                BigDecimal.valueOf(MARKETPLACE_REVENUE_PERCENTAGE));
    }

    private static void mockGetOperatorRevenueSharePercentage() {
        when(
                supplierShareAssembler.billingRetrievalService
                        .loadOperatorRevenueSharePercentage(anyLong(),
                                eq(PERIOD_END_TIME))).thenReturn(
                BigDecimal.valueOf(OPERATOR_REVENUE_PERCENTAGE));
    }

    private static void mockGetSellerRevenueSharePercentage() {
        when(
                supplierShareAssembler.billingRetrievalService
                        .loadResellerRevenueSharePercentage(anyLong(),
                                eq(PERIOD_END_TIME))).thenReturn(
                BigDecimal.valueOf(SELLER_REVENUE_PERCENTAGE));

        when(
                supplierShareAssembler.billingRetrievalService
                        .loadBrokerRevenueSharePercentage(anyLong(),
                                eq(PERIOD_END_TIME))).thenReturn(
                BigDecimal.valueOf(SELLER_REVENUE_PERCENTAGE));
    }

    private void mockServiceNetAmounts() {
        doAnswer(new Answer<List<BigDecimal>>() {
            @Override
            public List<BigDecimal> answer(InvocationOnMock invocation)
                    throws Throwable {
                Long subscrKey = supplierShareAssembler.currentBillingResult
                        .getSubscriptionKey();
                for (BillingResultDataMock testdata : billingResultDataMock
                        .values()) {
                    if (testdata.subscriptionId.equals(SUBSCRIPTION_ID
                            + subscrKey)) {
                        List<BigDecimal> mockNetAmounts = new ArrayList<BigDecimal>();
                        mockNetAmounts.add(testdata.netAmount);
                        return mockNetAmounts;
                    }
                }
                return null;
            }
        }).when(xmlSearch).retrieveNetAmounts(anyLong());
    }

    private void verifySubscriptionData(Subscription subscriptionToVerify,
            String expectedSubscriptionId) {

        boolean verified = false;
        for (BillingResultDataMock testdata : billingResultDataMock.values()) {
            if (testdata.subscriptionId.equals(expectedSubscriptionId)) {
                compareSubscriptionData(subscriptionToVerify,
                        testdata.subscriptionKey, testdata.billingKey,
                        testdata.subscriptionId, testdata.netAmount);
                verified = true;
                break;
            }
        }
        assertTrue(verified);
    }

    private void compareSubscriptionData(Subscription subscriptionToVerify,
            Long subscriptionKey, Long billingKey, String subscriptionId,
            BigDecimal netAmount) {
        assertNotNull(subscriptionToVerify);
        assertNotNull(subscriptionToVerify.getKey());
        assertEquals(subscriptionKey.longValue(), subscriptionToVerify.getKey()
                .longValue());
        assertNotNull(subscriptionToVerify.getId());
        assertEquals(subscriptionId,
                SUBSCRIPTION_ID + subscriptionToVerify.getId());
        assertNotNull(subscriptionToVerify.getBillingKey());
        assertEquals(billingKey.longValue(), subscriptionToVerify
                .getBillingKey().longValue());
        assertNotNull(subscriptionToVerify.getRevenue());
        assertEquals(netAmount, subscriptionToVerify.getRevenue());
    }

    private XMLGregorianCalendar getXMLGregorianCalendar(long time) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(time);
        return datatypeFactory.newXMLGregorianCalendar(gc);
    }

    private Set<String> getBillingResultCurrencies() {
        Set<String> result = new HashSet<String>();
        for (BillingResult billingResult : billingResults) {
            result.add(billingResult.getCurrencyCode());
        }
        return result;
    }

    private void verifyServiceData(Service serviceToVerify,
            String expectedProductId) {
        boolean verified = false;
        for (BillingResultDataMock testdata : billingResultDataMock.values()) {
            if (testdata.product.getDataContainer().getProductId()
                    .equals(expectedProductId)) {
                compareServiceData(serviceToVerify, testdata.product);
                verified = true;
                break;
            }
        }
        if (serviceToVerify.getModel().equals(OfferingType.BROKER.name())) {
            verifyOrganizationData(serviceToVerify.getBroker()
                    .getOrganizationData(), "BROKER");
        }
        if (serviceToVerify.getModel().equals(OfferingType.RESELLER.name())) {
            verifyOrganizationData(serviceToVerify.getReseller()
                    .getOrganizationData(), "RESELLER");
        }

        assertTrue(verified);
    }

    private void verifyOrganizationData(OrganizationData orgDataToVerify,
            String sellerType) {
        assertNotNull(orgDataToVerify);
        assertNotNull(orgDataToVerify.getKey());

        OrganizationHistory orgTestdata = getSellerOrganizationData(
                orgDataToVerify.getKey().longValue(), sellerType);

        assertNotNull(orgDataToVerify.getId());
        assertEquals(orgTestdata.getOrganizationId(), orgDataToVerify.getId());
        assertNotNull(orgDataToVerify.getEmail());
        assertEquals(orgTestdata.getEmail(), orgDataToVerify.getEmail());
        assertNotNull(orgDataToVerify.getName());
        assertEquals(orgTestdata.getOrganizationName(),
                orgDataToVerify.getName());
        assertNotNull(orgDataToVerify.getAddress());
        assertEquals(orgTestdata.getAddress(), orgDataToVerify.getAddress());
    }

    private void compareServiceData(Service serviceToVerify,
            ProductHistory product) {
        assertNotNull(serviceToVerify);
        assertNotNull(serviceToVerify.getId());
        assertEquals(product.getDataContainer().getProductId(),
                serviceToVerify.getId());
        assertNotNull(serviceToVerify.getKey());
        assertEquals(product.getObjKey(), serviceToVerify.getKey().longValue());
        assertNotNull(serviceToVerify.getModel());

        if (product.getVendorObjKey() == SUPPLIER_KEY) {
            assertEquals(OfferingType.DIRECT.name(), serviceToVerify.getModel());
        } else if (product.getVendorObjKey() == BROKER_KEY) {
            assertEquals(OfferingType.BROKER.name(), serviceToVerify.getModel());
        } else if (product.getVendorObjKey() == RESELLER_KEY) {
            assertEquals(OfferingType.RESELLER.name(),
                    serviceToVerify.getModel());
            assertThat(serviceToVerify.getSubscription(), hasNoItems());
            assertEquals("26", serviceToVerify.getSubscriptionsRevenue()
                    .getAmount().toString());
        }
        if (!serviceToVerify.getModel().equals(OfferingType.DIRECT.name())) {
            assertNotNull(serviceToVerify.getTemplateKey());
            assertEquals(product.getTemplateObjKey().longValue(),
                    serviceToVerify.getTemplateKey().longValue());
        }
    }

    private void printXml(ByteArrayOutputStream bos) {
        if (PRINT_TEST_DATA) {
            System.out.println(new String(bos.toByteArray()));
        }
    }

    @Test(expected = NullPointerException.class)
    public void build_BrokerIdIsNull() throws Exception {
        // when
        supplierShareAssembler.build(null, PERIOD_START_TIME, PERIOD_END_TIME);
    }

    @Test
    public void build_verifySupplierData() throws Exception {
        // when
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        assertNotNull(result);
        assertNotNull(result.getOrganizationKey());
        assertEquals(SUPPLIER_KEY.longValue(), result.getOrganizationKey()
                .longValue());
        assertNotNull(result.getOrganizationId());
        assertEquals(OfferingType.DIRECT + ID + SUPPLIER_KEY,
                result.getOrganizationId());
    }

    @Test
    public void build_period() throws Exception {
        // when
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        Period period = result.getPeriod();
        assertNotNull(period);

        // start date
        assertNotNull(period.getStartDate());
        assertEquals(PERIOD_START_TIME, period.getStartDate().longValue());
        assertNotNull(period.getStartDateIsoFormat());
        assertEquals(getXMLGregorianCalendar(PERIOD_START_TIME),
                period.getStartDateIsoFormat());

        // end date
        assertNotNull(period.getEndDate());
        assertEquals(PERIOD_END_TIME, period.getEndDate().longValue());
        assertNotNull(period.getEndDateIsoFormat());
        assertEquals(getXMLGregorianCalendar(PERIOD_END_TIME),
                period.getEndDateIsoFormat());
    }

    @Test
    public void build_currency() throws Exception {
        // when
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        assertNotNull(result.getCurrency());
        Set<String> currencies = getBillingResultCurrencies();
        assertEquals(currencies.size(), result.getCurrency().size());
        for (Currency currency : result.getCurrency()) {
            assertTrue(currencies.contains(currency.getId()));
        }
    }

    @Test
    public void build_marketplace() throws Exception {
        // when
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        // verify marketplace existence
        for (Currency currency : result.getCurrency()) {
            if (currency.getId().equals(CURRENCY_EUR)) {
                assertEquals(2, currency.getMarketplace().size());

            } else if (currency.getId().equals(CURRENCY_USD)) {
                assertEquals(1, currency.getMarketplace().size());
            }

            for (Marketplace marketplace : currency.getMarketplace()) {
                verifyOrganizationData(marketplace.getMarketplaceOwner()
                        .getOrganizationData(), MARKETPLACE_OWNER);
            }
        }
    }

    @Test
    public void buildMarketplace2() {
        // given
        MarketplaceHistory marketplaceHistory = new MarketplaceHistory();
        marketplaceHistory.setObjKey(MP_KEY1);
        marketplaceHistory.getDataContainer().setMarketplaceId(MP_ID1);
        marketplaceHistory.setOrganizationObjKey(MP_OWNER_KEY1);

        // when
        Marketplace mp = supplierShareAssembler
                .buildMarketplace(marketplaceHistory);

        // then
        assertEquals(mp.getKey().longValue(), MP_KEY1);
        assertEquals(mp.getId(), MP_ID1);
    }

    @Test
    public void build_service() throws Exception {
        // when
        mockServiceNetAmounts();
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        for (Currency currency : result.getCurrency()) {
            for (Marketplace marketplace : currency.getMarketplace()) {
                if (currency.getId().equals(CURRENCY_EUR)) {
                    if (marketplace.getId().equals(MP_ID1)) {
                        assertEquals(3, marketplace.getService().size());
                        verifyServiceData(marketplace.getService().get(0),
                                PRODUCT_ID1);
                        verifyServiceData(marketplace.getService().get(1),
                                PRODUCT_ID2);
                        verifyServiceData(marketplace.getService().get(2),
                                PRODUCT_ID3);

                    } else if (marketplace.getId().equals(MP_ID2)) {
                        assertEquals(1, marketplace.getService().size());
                        verifyServiceData(marketplace.getService().get(0),
                                PRODUCT_ID5);

                    } else {
                        Assert.fail("Invalid marketplace id "
                                + marketplace.getId());
                    }

                } else if (currency.getId().equals(CURRENCY_USD)) {
                    if (marketplace.getId().equals(MP_ID1)) {
                        assertEquals(1, marketplace.getService().size());
                        verifyServiceData(marketplace.getService().get(0),
                                PRODUCT_ID3);
                    }
                } else {
                    Assert.fail("Invalid currency " + currency.getId());
                }
            }
        }
    }

    @Test
    public void build_subscription() throws Exception {
        // when
        mockServiceNetAmounts();
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        for (Currency currency : result.getCurrency()) {
            for (Marketplace marketplace : currency.getMarketplace()) {
                for (Service service : marketplace.getService()) {
                    int serviceKey = service.getKey().intValue();
                    if (serviceKey == 1) {
                        assertEquals(3, service.getSubscription().size());
                        verifySubscriptionData(
                                service.getSubscription().get(0),
                                SUBSCRIPTION_ID + "1");
                        verifySubscriptionData(
                                service.getSubscription().get(1),
                                SUBSCRIPTION_ID + "2");

                    } else if (serviceKey == 2) {
                        assertEquals(1, service.getSubscription().size());
                        verifySubscriptionData(
                                service.getSubscription().get(0),
                                SUBSCRIPTION_ID + "3");

                    } else if (serviceKey == 3) {
                        assertEquals(0, service.getSubscription().size());
                        assertNotNull(service.getSubscriptionsRevenue()
                                .getAmount());
                    } else if (serviceKey == 5) {
                        assertEquals(1, service.getSubscription().size());
                        verifySubscriptionData(
                                service.getSubscription().get(0),
                                SUBSCRIPTION_ID + "4");
                    }
                }
            }
        }
    }

    @Test
    public void build_RevenueShareDetails() throws Exception {
        // when
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        result.calculateAllShares();

        // then
        for (Currency currency : result.getCurrency()) {
            for (Marketplace marketplace : currency.getMarketplace()) {
                for (Service service : marketplace.getService()) {
                    RevenueShareDetails revenueDetails = service
                            .getRevenueShareDetails();

                    assertNotNull(revenueDetails);
                    assertNotNull(revenueDetails.getServiceRevenue());
                    assertNotNull(revenueDetails
                            .getMarketplaceRevenueSharePercentage());
                    assertNotNull(revenueDetails
                            .getOperatorRevenueSharePercentage());
                    assertNotNull(revenueDetails.getMarketplaceRevenue());
                    assertNotNull(revenueDetails
                            .getOperatorRevenueSharePercentage());
                    assertNotNull(revenueDetails.getOperatorRevenue());
                    assertNotNull(revenueDetails.getAmountForSupplier());

                    if (service.getModel().equals(OfferingType.DIRECT.name())) {
                        assertNull(revenueDetails.getResellerRevenue());
                        assertNull(revenueDetails
                                .getResellerRevenueSharePercentage());
                        assertNull(revenueDetails.getBrokerRevenue());
                        assertNull(revenueDetails
                                .getBrokerRevenueSharePercentage());
                    } else if (service.getModel().equals(
                            OfferingType.RESELLER.name())) {
                        assertNotNull(revenueDetails.getResellerRevenue());
                        assertNotNull(revenueDetails
                                .getResellerRevenueSharePercentage());
                    } else if (service.getModel().equals(
                            OfferingType.BROKER.name())) {
                        assertNotNull(revenueDetails.getBrokerRevenue());
                        assertNotNull(revenueDetails
                                .getBrokerRevenueSharePercentage());
                    }
                }
            }
        }
    }

    @Test
    public void build_resellerRevenuePerMarketplace() throws Exception {
        // when
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        result.calculateAllShares();

        // then
        for (Currency currency : result.getCurrency()) {
            for (Marketplace marketplace : currency.getMarketplace()) {
                RevenuePerMarketplace revenue = marketplace
                        .getRevenuePerMarketplace();

                assertNotNull(revenue);
                assertNotNull(revenue.getServiceRevenue());
                assertNotNull(revenue.getMarketplaceRevenue());
                assertNotNull(revenue.getOperatorRevenue());
                assertNotNull(revenue.getResellerRevenue());
                assertNotNull(revenue.getBrokerRevenue());
                assertNotNull(revenue.getOverallRevenue());
            }
        }
    }

    @Test
    public void serialize() throws Exception {
        // given
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        result.calculateAllShares();

        // when
        JAXBContext jc = JAXBContext
                .newInstance(SupplierRevenueShareResult.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        marshaller.marshal(result, bos);
        assertNotNull(bos.toByteArray());

        final List<String> fragments = new ArrayList<String>();
        fragments.add(new String(bos.toByteArray(), "UTF-8"));

        byte[] xmlBytes = XMLConverter.combine("RevenueSharesResults",
                fragments, SupplierRevenueShareResult.SCHEMA);
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        bos1.write(xmlBytes);
        printXml(bos1);

        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL schemaUrl = BillingServiceBean.class.getResource("/"
                + "SupplierRevenueShareResult.xsd");
        Schema schema = schemaFactory.newSchema(schemaUrl);

        Source xmlFile = new StreamSource(new ByteArrayInputStream(xmlBytes));
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

    @Test
    public void buildService_cleanProductId() {

        // given
        Marketplace mp = new Marketplace();
        mp.setRevenueSharePercentage(BigDecimal.TEN);
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setObjKey(7);
        givenProduct.setTemplateObjKey(Long.valueOf(8));
        givenProduct.getDataContainer().setProductId("prodId#896896");
        givenProduct.setVendorObjKey(SUPPLIER_KEY);

        // when
        Service constructedSerive = supplierShareAssembler.buildService(mp,
                givenProduct);

        // then
        assertEquals("prodId", constructedSerive.getId());
    }

    /**
     * The period start and end times are UTC times
     * 
     * @throws Exception
     */
    @Test
    public void setPeriod() throws Exception {

        // given
        long givenStartPeriod = 0;
        long givenEndPeriod = 1347867425534L;

        // when
        SupplierRevenueShareResult result = new SupplierRevenueShareResult();
        assembler.result = result;
        assembler.setPeriod(givenStartPeriod, givenEndPeriod);

        // then
        Period period = assembler.result.getPeriod();
        assertEquals(givenStartPeriod, period.getStartDate().longValue());
        assertEquals("1970-01-01T00:00:00.000Z", period.getStartDateIsoFormat()
                .toString());
        assertEquals(givenEndPeriod, period.getEndDate().longValue());
        assertEquals("2012-09-17T07:37:05.534Z", period.getEndDateIsoFormat()
                .toString());
    }

    @Ignore
    // FIXME Test case failing on estbesdev1
    @Test
    public void subscribeTwice_ForOneCus() throws Exception {
        // given
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        // when
        result.calculateAllShares();
        // then
        Currency currency = result.getCurrency().get(0);
        assertEquals(CURRENCY_EUR, currency.getId());
        Marketplace mp = currency.getMarketplace(MP_KEY1);
        assertEquals(MP_ID1, mp.getId());
        Service service = mp.getServiceByKey(1);
        List<CustomerRevenueShareDetails> customerRSDs = service
                .getRevenueShareDetails().getCustomerRevenueShareDetails();
        for (CustomerRevenueShareDetails cus : customerRSDs) {
            if (cus.getCustomerId().equals("_Id_1")) {
                assertEquals(342.22, cus.getServiceRevenue().doubleValue(),
                        0.00);
            }
        }
    }

    @Ignore
    // FIXME Test case failing on estbesdev1
    @Test
    public void verify_supplierRevenue() throws Exception {
        // given
        SupplierRevenueShareResult result = supplierShareAssembler.build(
                SUPPLIER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        // when
        result.calculateAllShares();

        // then
        Currency currencyEUR = result.getCurrency().get(0);
        assertEquals(CURRENCY_EUR, currencyEUR.getId());
        Currency currencyUSD = result.getCurrency().get(1);
        assertEquals(CURRENCY_USD, currencyUSD.getId());
        Service product1 = currencyEUR.getMarketplace(MP_KEY1).getServiceByKey(
                1);

        // subscriptionId_1 + subscriptionId_2 + subscriptionId_7
        BigDecimal serviceRevenueForDetail = product1.getRevenueShareDetails()
                .getServiceRevenue();
        assertEquals(BigDecimal.valueOf(552.37), serviceRevenueForDetail);

        BigDecimal marketplaceRevenuePercentageForDetail = product1
                .getRevenueShareDetails()
                .getMarketplaceRevenueSharePercentage();
        assertEquals(MARKETPLACE_REVENUE_PERCENTAGE,
                marketplaceRevenuePercentageForDetail.doubleValue(), 0.00);
        BigDecimal marketplaceRevenueForDetail = product1
                .getRevenueShareDetails().getMarketplaceRevenue();
        assertEquals(BigDecimal.valueOf(13.81), marketplaceRevenueForDetail);

        BigDecimal operatorRevenueParcentageForDetail = product1
                .getRevenueShareDetails().getOperatorRevenueSharePercentage();
        assertEquals(OPERATOR_REVENUE_PERCENTAGE,
                operatorRevenueParcentageForDetail.doubleValue(), 0.00);
        BigDecimal operatorRevenueForDetail = product1.getRevenueShareDetails()
                .getOperatorRevenue();
        assertEquals(BigDecimal.valueOf(22.65), operatorRevenueForDetail);

        // subscriptionId_2
        BigDecimal serviceRevenueForCustomerRS = product1
                .getRevenueShareDetails().getCustomerRevenueShareDetails()
                .get(0).getServiceRevenue();
        assertEquals(BigDecimal.valueOf(210.15), serviceRevenueForCustomerRS);

        BigDecimal marketplaceRevenueForCustomerRS = product1
                .getRevenueShareDetails().getCustomerRevenueShareDetails()
                .get(0).getMarketplaceRevenue();
        assertEquals(BigDecimal.valueOf(5.25), marketplaceRevenueForCustomerRS);

        BigDecimal operatorRevenueForCustomerRS = product1
                .getRevenueShareDetails().getCustomerRevenueShareDetails()
                .get(0).getOperatorRevenue();
        assertEquals(BigDecimal.valueOf(8.62), operatorRevenueForCustomerRS);

        // For currency
        BigDecimal supplierRevenueEURAmount = currencyEUR.getSupplierRevenue()
                .getAmount();
        BigDecimal supplierRevenueUSDAmount = currencyUSD.getSupplierRevenue()
                .getAmount();
        double total = 120 + 210.15 + 187.99 + 87.19 + 26 + 523.56 + 222.22;
        assertEquals(total,
                supplierRevenueEURAmount.add(supplierRevenueUSDAmount)
                        .doubleValue(), 0.00);
        double direct = currencyEUR.getSupplierRevenue().getDirectRevenue()
                .getServiceRevenue().doubleValue();
        assertEquals(187.99, direct, 0.00);
        double broker = 120 + 222.22 + 210.15 + 87.19;
        assertEquals(broker, currencyEUR.getSupplierRevenue()
                .getBrokerRevenue().getServiceRevenue().doubleValue(), 0.00);
        assertEquals(26, currencyEUR.getSupplierRevenue().getResellerRevenue()
                .getServiceRevenue().doubleValue(), 0.00);
    }
}
