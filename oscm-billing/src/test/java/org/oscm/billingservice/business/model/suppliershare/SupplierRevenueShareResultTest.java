/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 31, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.XmlSearch;
import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.MarketplaceData;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionData;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

public class SupplierRevenueShareResultTest {

    private static final BigDecimal BD_1000 = new BigDecimal(1000)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_800 = new BigDecimal(800)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_750 = new BigDecimal(750)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_700 = new BigDecimal(700)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_675 = new BigDecimal(675)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_560 = new BigDecimal(560)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_500 = new BigDecimal(500)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_400 = new BigDecimal(400)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_375 = new BigDecimal(375)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_350 = new BigDecimal(350)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_325 = new BigDecimal(325)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_300 = new BigDecimal(300)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_280 = new BigDecimal(280)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_260 = new BigDecimal(260)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_150 = new BigDecimal(150)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_120 = new BigDecimal(120)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_100 = new BigDecimal(100)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_80 = new BigDecimal(80)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_75 = new BigDecimal(75)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_60 = new BigDecimal(60)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_50 = new BigDecimal(50)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_40 = new BigDecimal(40)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_25 = new BigDecimal(25)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_20 = new BigDecimal(20)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_15 = new BigDecimal(15)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_10 = new BigDecimal(10)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private static final BigDecimal BD_5 = new BigDecimal(5)
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    private static final long MP1_KEY = 1L;
    private static final String MP1_ID = "mp1";
    private static final long MP2_KEY = 2L;

    private SupplierRevenueShareResult supplierResult;
    private SupplierShareResultAssembler assembler;
    private SharesDataRetrievalServiceLocal dao;
    private XmlSearch xmlSearch;
    private List<BillingResult> billingResults = new ArrayList<BillingResult>();

    @Before
    public void setup() {
        dao = mock(SharesDataRetrievalServiceLocal.class);
        assembler = spy(new SupplierShareResultAssembler(dao));
        supplierResult = new SupplierRevenueShareResult();
        mockSupplierOrganization();
        mockBillingResults();
        mockXmlSearch();
    }

    private void mockSupplierOrganization() {
        OrganizationHistory org = spy(new OrganizationHistory());
        org.setKey(1111);
        when(org.getOrganizationId()).thenReturn("111");
        when(org.getOrganizationName()).thenReturn("name_111");
        when(dao.loadLastOrganizationHistory(any(Long.class))).thenReturn(org);
    }

    private void mockBillingResults() {
        when(
                dao.loadBillingResultsForSupplier(any(Long.class), anyLong(),
                        anyLong())).thenReturn(billingResults);
    }

    private void mockXmlSearch() {
        xmlSearch = mock(XmlSearch.class);
        doReturn(xmlSearch).when(assembler).newXmlSearch(
                any(BillingResult.class));
        Set<Long> pmKeys = new HashSet<Long>();
        pmKeys.add(Long.valueOf(0));
        doReturn(pmKeys).when(xmlSearch).findPriceModelKeys();
    }

    private RevenueShareDetails getRevenueShareDetails(String currencyCode,
            String mpId, long srvKey) {
        if (mpId.equals(MP1_ID)) {
            return supplierResult.getCurrencyByCode(currencyCode)
                    .getMarketplace(MP1_KEY).getServiceByKey(srvKey)
                    .getRevenueShareDetails();
        } else {
            return supplierResult.getCurrencyByCode(currencyCode)
                    .getMarketplace(MP2_KEY).getServiceByKey(srvKey)
                    .getRevenueShareDetails();
        }

    }

    private SupplierRevenue getSupplierRevenueForCurrency(String currencyCode) {
        return supplierResult.getCurrencyByCode(currencyCode)
                .getSupplierRevenue();
    }

    private void givenSupplierShareResult() throws Exception {
        supplierResult = assembler.build(Long.valueOf(1L), 0L, 10L);
    }


    private void givenSubscription(String currency, String mpId, long srvKey,
            long subscriptionKey, BigDecimal netAmount, BigDecimal discount,
            OrganizationRoleType vendorType, BigDecimal partnerServiceShare) {

        newBillingResult(currency, netAmount, Long.valueOf(subscriptionKey));
        mockMarketplace(mpId, Long.valueOf(subscriptionKey));
        mockProduct(srvKey, Long.valueOf(subscriptionKey));
        mockProductShare(srvKey, partnerServiceShare);
        mockOrganizationRole(srvKey, vendorType);
        mockSubscriptionHistory(Long.valueOf(subscriptionKey));
        mockXmlSearchAmounts(netAmount, discount);
    }

    @SuppressWarnings("boxing")
    private void mockXmlSearchAmounts(BigDecimal netAmount, BigDecimal discount) {
        List<BigDecimal> mockNetAmounts = new ArrayList<BigDecimal>();
        mockNetAmounts.add(netAmount);
        doReturn(mockNetAmounts).when(xmlSearch).retrieveNetAmounts(anyLong());
        doReturn(discount).when(xmlSearch).retrieveDiscountPercent();
    }

    private void newBillingResult(String currency, BigDecimal netAmount,
            Long subscriptionKey) {

        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(java.util.Currency.getInstance(currency));
        BillingResult billingResult = new BillingResult();
        billingResult.setKey(System.currentTimeMillis());
        billingResult.setSubscriptionKey(subscriptionKey);
        billingResult.setCurrency(sc);
        billingResult.setNetAmount(netAmount);
        billingResults.add(billingResult);
    }

    @SuppressWarnings("boxing")
    private void mockProduct(long srvKey, Long subscriptionKey) {
        ProductHistory productHistory = new ProductHistory();
        productHistory.setObjKey(srvKey);
        productHistory.setVendorObjKey(srvKey);
        productHistory.setTemplateObjKey(Long.valueOf(11L));
        when(
                dao.loadProductOfVendor(eq(subscriptionKey.longValue()),
                        anyLong(), anyLong())).thenReturn(productHistory);
    }

    private void mockProductShare(long srvKey, BigDecimal srvShare) {
        when(dao.loadBrokerRevenueSharePercentage(eq(srvKey), anyLong()))
                .thenReturn(srvShare);
        when(dao.loadResellerRevenueSharePercentage(eq(srvKey), anyLong()))
                .thenReturn(srvShare);
    }

    private void mockOrganizationRole(long srvKey,
            OrganizationRoleType vendorType) {
        OrganizationRole role = new OrganizationRole(vendorType);
        List<OrganizationRole> orgRoles = Arrays.asList(role);
        when(dao.loadOrganizationHistoryRoles(eq(srvKey), anyLong()))
                .thenReturn(orgRoles);
    }

    private void mockSubscriptionHistory(Long subscriptionKey) {
        SubscriptionHistory subscriptionHistory = new SubscriptionHistory();
        SubscriptionData data = new SubscriptionData();
        data.setSubscriptionId("subscriptionId");
        subscriptionHistory.setDataContainer(data);
        when(
                dao.loadSubscriptionHistoryWithinPeriod(
                        eq(subscriptionKey.longValue()), anyLong()))
                .thenReturn(subscriptionHistory);
    }

    private void mockMarketplace(String mpId, Long subscriptionKey) {
        MarketplaceData mpData = new MarketplaceData();
        mpData.setMarketplaceId(mpId);
        MarketplaceHistory marketplaceHistory = new MarketplaceHistory();
        marketplaceHistory.setKey(1L);
        if (mpId.equals(MP1_ID)) {
            marketplaceHistory.setObjKey(MP1_KEY);
        } else {
            marketplaceHistory.setObjKey(MP2_KEY);
        }
        marketplaceHistory.setDataContainer(mpData);
        when(
                dao.loadMarketplaceHistoryBySubscriptionKey(
                        eq(subscriptionKey.longValue()), anyLong()))
                .thenReturn(marketplaceHistory);
    }

    private void givenMarketplaceShare(long mpKey, BigDecimal mpShare) {
        when(dao.loadMarketplaceRevenueSharePercentage(eq(mpKey), anyLong()))
                .thenReturn(mpShare);
    }

    private void givenOperatorShare(BigDecimal operatorShare) {
        when(dao.loadOperatorRevenueSharePercentage(anyLong(), anyLong()))
                .thenReturn(operatorShare);
    }

    @Test
    public void calculateAllShares_singleMpSingleBrokerSubscription()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.BROKER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_500, BD_350, BD_75,
                BD_50, BD_25, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_350, supplierRevenue.getAmount());
        assertBrokerRevenue(supplierRevenue, BD_500, BD_75, BD_50, BD_25);

    }

    @Test
    public void calculateAllShares_singleMpSingleBrokerSubscriptionWithDiscount()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_20,
                OrganizationRoleType.BROKER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_400, BD_280, BD_60,
                BD_40, BD_20, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_280, supplierRevenue.getAmount());
        assertBrokerRevenue(supplierRevenue, BD_400, BD_60, BD_40, BD_20);
    }

    @Test
    public void calculateAllShares_brokerSubscription_decimalAmount()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L,
                BigDecimals.normalize(4086.42), BigDecimal.ZERO,
                OrganizationRoleType.BROKER, BigDecimals.normalize(20));
        givenMarketplaceShare(MP1_KEY, BigDecimals.normalize(1));
        givenOperatorShare(BigDecimals.normalize(3));
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails,
                BigDecimals.normalize(4086.42), BigDecimals.normalize(3105.69),
                BigDecimals.normalize(40.86), BigDecimals.normalize(122.59),
                BigDecimals.normalize(817.28), null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BigDecimals.normalize(3105.69),
                supplierRevenue.getAmount());
        assertBrokerRevenue(supplierRevenue, BigDecimals.normalize(4086.42),
                BigDecimals.normalize(40.86), BigDecimals.normalize(122.59),
                BigDecimals.normalize(817.28));
    }

    @Test
    public void calculateAllShares_brokerSubscription_decimalAmountWithDiscount()
            throws Exception {
        // given a broker subscription with 20% discount and decimal amount
        givenSubscription("EUR", MP1_ID, 1L, 10L,
                BigDecimals.normalize(4086.42), BD_20,
                OrganizationRoleType.BROKER, BigDecimals.normalize(20));
        givenMarketplaceShare(MP1_KEY, BigDecimals.normalize(1));
        givenOperatorShare(BigDecimals.normalize(3));
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails,
                BigDecimals.normalize(3269.14), BigDecimals.normalize(2484.55),
                BigDecimals.normalize(32.69), BigDecimals.normalize(98.07),
                BigDecimals.normalize(653.83), null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BigDecimals.normalize(2484.55),
                supplierRevenue.getAmount());
        assertBrokerRevenue(supplierRevenue, BigDecimals.normalize(3269.14),
                BigDecimals.normalize(32.69), BigDecimals.normalize(98.07),
                BigDecimals.normalize(653.83));

    }

    @Test
    public void calculateAllShares_singleMpSingleResellerSubscriptionWithDiscount()
            throws Exception {
        // given a reseller subscription with 20% discount
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_20,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_400, BD_280, BD_60,
                BD_40, null, BD_20);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_280, supplierRevenue.getAmount());
        assertResellerRevenue(supplierRevenue, BD_400, BD_280, BD_60, BD_40,
                BD_20);
    }

    @Test
    public void calculateAllShares_resellerSubscription_decimalAmount()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L,
                BigDecimals.normalize(4086.42), BigDecimal.ZERO,
                OrganizationRoleType.RESELLER, BigDecimals.normalize(20));
        givenMarketplaceShare(MP1_KEY, BigDecimals.normalize(1));
        givenOperatorShare(BigDecimals.normalize(3));
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails,
                BigDecimals.normalize(4086.42), BigDecimals.normalize(3105.69),
                BigDecimals.normalize(40.86), BigDecimals.normalize(122.59),
                null, BigDecimals.normalize(817.28));

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BigDecimals.normalize(3105.69),
                supplierRevenue.getAmount());
        assertResellerRevenue(supplierRevenue, BigDecimals.normalize(4086.42),
                BigDecimals.normalize(3105.69), BigDecimals.normalize(40.86),
                BigDecimals.normalize(122.59), BigDecimals.normalize(817.28));
    }

    @Test
    public void calculateAllShares_resellerSubscription_decimalAmountWithDiscount()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L,
                BigDecimals.normalize(4086.42), BD_20,
                OrganizationRoleType.RESELLER, BigDecimals.normalize(20));
        givenMarketplaceShare(MP1_KEY, BigDecimals.normalize(1));
        givenOperatorShare(BigDecimals.normalize(3));
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);

        assertRevenueShareDetails(revenueShareDetails,
                BigDecimals.normalize(3269.14), BigDecimals.normalize(2484.55),
                BigDecimals.normalize(32.69), BigDecimals.normalize(98.07),
                null, BigDecimals.normalize(653.83));

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BigDecimals.normalize(2484.55),
                supplierRevenue.getAmount());
        assertResellerRevenue(supplierRevenue, BigDecimals.normalize(3269.14),
                BigDecimals.normalize(2484.55), BigDecimals.normalize(32.69),
                BigDecimals.normalize(98.07), BigDecimals.normalize(653.83));
    }

    @Test
    public void calculateAllShares_singleMpBrokerSubscriptions()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.BROKER, BD_5);
        givenSubscription("EUR", MP1_ID, 1L, 20L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.BROKER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_1000, BD_700, BD_150,
                BD_100, BD_50, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_700, supplierRevenue.getAmount());
        assertBrokerRevenue(supplierRevenue, BD_1000, BD_150, BD_100, BD_50);
    }

    @Test
    public void calculateAllShares_singleMpBrokerSubscriptionsWithDiscount()
            throws Exception {
        // given two broker subscriptions with discounts of 50 % to the same
        // service
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_50,
                OrganizationRoleType.BROKER, BD_5);
        givenSubscription("EUR", MP1_ID, 1L, 20L, BD_500, BD_50,
                OrganizationRoleType.BROKER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_500, BD_350, BD_75,
                BD_50, BD_25, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_350, supplierRevenue.getAmount());
        assertBrokerRevenue(supplierRevenue, BD_500, BD_75, BD_50, BD_25);
    }

    @Test
    public void calculateAllShares_singleMpResellerSubscriptions()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.RESELLER, BD_5);
        givenSubscription("EUR", MP1_ID, 1L, 20L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_1000, BD_700, BD_150,
                BD_100, null, BD_50);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_700, supplierRevenue.getAmount());
        assertResellerRevenue(supplierRevenue, BD_1000, BD_700, BD_150, BD_100,
                BD_50);
    }

    @Test
    public void calculateAllShares_singleMpResellerSubscriptionsWithDiscount()
            throws Exception {
        // given two reseller subscription with discount 50% to the same service
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_50,
                OrganizationRoleType.RESELLER, BD_5);
        givenSubscription("EUR", MP1_ID, 1L, 20L, BD_500, BD_50,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_500, BD_350, BD_75,
                BD_50, null, BD_25);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_350, supplierRevenue.getAmount());
        assertResellerRevenue(supplierRevenue, BD_500, BD_350, BD_75, BD_50,
                BD_25);
    }

    @Test
    public void calculateAllShares_singleMpSingleSupplierSubscription()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.SUPPLIER, null);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_500, BD_375, BD_75,
                BD_50, null, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_375, supplierRevenue.getAmount());
        assertDirectRevenue(supplierRevenue, BD_500, BD_75, BD_50);
    }

    @Test
    public void calculateAllShares_singleMpSingleSupplierSubscriptionWithDiscount()
            throws Exception {
        // given a suplier subscription with a 20% discount
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_20,
                OrganizationRoleType.SUPPLIER, null);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);

        assertRevenueShareDetails(revenueShareDetails, BD_400, BD_300, BD_60,
                BD_40, null, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_300, supplierRevenue.getAmount());
        assertDirectRevenue(supplierRevenue, BD_400, BD_60, BD_40);
    }

    @Test
    public void build_singleMpSingleSupplierSubscription() throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.SUPPLIER, null);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);

        // when
        SupplierRevenueShareResult result = assembler.build(Long.valueOf(1L),
                0L, 10L);

        // then
        assertNotNull(result);

    }

    @Test
    public void calculateAllShares_singleMpSupplierSubscriptions()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.SUPPLIER, null);
        givenSubscription("EUR", MP1_ID, 1L, 20L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.SUPPLIER, null);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_1000, BD_750, BD_150,
                BD_100, null, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_750, supplierRevenue.getAmount());
        assertDirectRevenue(supplierRevenue, BD_1000, BD_150, BD_100);
    }

    @Test
    public void calculateAllShares_singleMpSupplierSubscriptionsWithDiscount()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_50,
                OrganizationRoleType.SUPPLIER, null);
        givenSubscription("EUR", MP1_ID, 1L, 20L, BD_500, BD_50,
                OrganizationRoleType.SUPPLIER, null);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        // then
        RevenueShareDetails revenueShareDetails = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        assertRevenueShareDetails(revenueShareDetails, BD_500, BD_375, BD_75,
                BD_50, null, null);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_375, supplierRevenue.getAmount());
        assertDirectRevenue(supplierRevenue, BD_500, BD_75, BD_50);
    }

    @Test
    public void calculateAllShares_twoCurrenciesBrokerReseller()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.BROKER, BD_5);
        givenSubscription("USD", MP1_ID, 2L, 20L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.RESELLER, BD_5);
        givenSubscription("USD", MP1_ID, 2L, 30L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareEUR = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        RevenueShareDetails revenueShareUSD = getRevenueShareDetails("USD",
                MP1_ID, 2L);

        assertRevenueShareDetails(revenueShareEUR, BD_500, BD_350, BD_75,
                BD_50, BD_25, null);

        assertRevenueShareDetails(revenueShareUSD, BD_1000, BD_700, BD_150,
                BD_100, null, BD_50);

        SupplierRevenue supplierRevenueEUR = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_350, supplierRevenueEUR.getAmount());
        assertBrokerRevenue(supplierRevenueEUR, BD_500, BD_75, BD_50, BD_25);

        SupplierRevenue supplierRevenueUSD = getSupplierRevenueForCurrency("USD");
        assertEquals(BD_700, supplierRevenueUSD.getAmount());
        assertResellerRevenue(supplierRevenueUSD, BD_1000, BD_700, BD_150,
                BD_100, BD_50);

    }

    @Test
    public void calculateAllShares_twoCurrenciesBrokerResellerWithDiscount()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_20,
                OrganizationRoleType.BROKER, BD_5);
        givenSubscription("USD", MP1_ID, 2L, 20L, BD_500, BD_20,
                OrganizationRoleType.RESELLER, BD_5);
        givenSubscription("USD", MP1_ID, 2L, 30L, BD_500, BD_20,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareEUR = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        RevenueShareDetails revenueShareUSD = getRevenueShareDetails("USD",
                MP1_ID, 2L);

        assertRevenueShareDetails(revenueShareEUR, BD_400, BD_280, BD_60,
                BD_40, BD_20, null);
        assertRevenueShareDetails(revenueShareUSD, BD_800, BD_560, BD_120,
                BD_80, null, BD_40);

        SupplierRevenue supplierRevenueEUR = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_280, supplierRevenueEUR.getAmount());
        assertBrokerRevenue(supplierRevenueEUR, BD_400, BD_60, BD_40, BD_20);

        SupplierRevenue supplierRevenueUSD = getSupplierRevenueForCurrency("USD");
        assertEquals(BD_560, supplierRevenueUSD.getAmount());
        assertResellerRevenue(supplierRevenueUSD, BD_800, BD_560, BD_120,
                BD_80, BD_40);

    }

    @Test
    public void calculateAllShares_oneCurrencyPerMarketplace_BrokerReseller()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.BROKER, BD_5);
        givenSubscription("USD", "mp2", 2L, 20L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenMarketplaceShare(MP2_KEY, BD_20);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareEUR = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        RevenueShareDetails revenueShareUSD = getRevenueShareDetails("USD",
                "mp2", 2L);

        assertRevenueShareDetails(revenueShareEUR, BD_500, BD_350, BD_75,
                BD_50, BD_25, null);

        assertRevenueShareDetails(revenueShareUSD, BD_500, BD_325, BD_100,
                BD_50, null, BD_25);

        SupplierRevenue supplierRevenueEUR = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_350, supplierRevenueEUR.getAmount());
        assertBrokerRevenue(supplierRevenueEUR, BD_500, BD_75, BD_50, BD_25);

        SupplierRevenue supplierRevenueUSD = getSupplierRevenueForCurrency("USD");
        assertEquals(BD_325, supplierRevenueUSD.getAmount());
        assertResellerRevenue(supplierRevenueUSD, BD_500, BD_325, BD_100,
                BD_50, BD_25);
    }

    @Test
    public void calculateAllShares_oneCurrencyPerMarketplace_BrokerResellerWithDiscount()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BD_20,
                OrganizationRoleType.BROKER, BD_5);
        givenSubscription("USD", "mp2", 2L, 20L, BD_500, BD_20,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenMarketplaceShare(MP2_KEY, BD_20);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareEUR = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        RevenueShareDetails revenueShareUSD = getRevenueShareDetails("USD",
                "mp2", 2L);

        assertRevenueShareDetails(revenueShareEUR, BD_400, BD_280, BD_60,
                BD_40, BD_20, null);

        assertRevenueShareDetails(revenueShareUSD, BD_400, BD_260, BD_80,
                BD_40, null, BD_20);

        SupplierRevenue supplierRevenueEUR = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_280, supplierRevenueEUR.getAmount());
        assertBrokerRevenue(supplierRevenueEUR, BD_400, BD_60, BD_40, BD_20);

        SupplierRevenue supplierRevenueUSD = getSupplierRevenueForCurrency("USD");
        assertEquals(BD_260, supplierRevenueUSD.getAmount());
        assertResellerRevenue(supplierRevenueUSD, BD_400, BD_260, BD_80, BD_40,
                BD_20);
    }

    @Test
    public void calculateAllShares_OneCurrencyWithTwoMarketplaces_BrokerReseller()
            throws Exception {
        // given
        givenSubscription("EUR", MP1_ID, 1L, 10L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.BROKER, BD_5);
        givenSubscription("EUR", "mp2", 2L, 20L, BD_500, BigDecimal.ZERO,
                OrganizationRoleType.RESELLER, BD_5);
        givenMarketplaceShare(MP1_KEY, BD_15);
        givenMarketplaceShare(MP2_KEY, BD_20);
        givenOperatorShare(BD_10);
        givenSupplierShareResult();

        // when
        supplierResult.calculateAllShares();

        // then
        RevenueShareDetails revenueShareMp1 = getRevenueShareDetails("EUR",
                MP1_ID, 1L);
        RevenueShareDetails revenueShareMp2 = getRevenueShareDetails("EUR",
                "mp2", 2L);

        assertRevenueShareDetails(revenueShareMp1, BD_500, BD_350, BD_75,
                BD_50, BD_25, null);
        assertRevenueShareDetails(revenueShareMp2, BD_500, BD_325, BD_100,
                BD_50, null, BD_25);

        SupplierRevenue supplierRevenue = getSupplierRevenueForCurrency("EUR");
        assertEquals(BD_675, supplierRevenue.getAmount());

        assertBrokerRevenue(supplierRevenue, BD_500, BD_75, BD_50, BD_25);
        assertResellerRevenue(supplierRevenue, BD_500, BD_325, BD_100, BD_50,
                BD_25);

    }

    private void assertRevenueShareDetails(
            RevenueShareDetails revenueShareDetails,
            BigDecimal expectedServiceRevenue,
            BigDecimal expectedAmountForSupplier,
            BigDecimal expectedMarketplaceRevenue,
            BigDecimal expectedOperatorRevenue,
            BigDecimal expectedBrokerRevenue, BigDecimal expectedResellerRevenue) {
        assertEquals(expectedServiceRevenue,
                revenueShareDetails.getServiceRevenue());
        assertEquals(expectedAmountForSupplier,
                revenueShareDetails.getAmountForSupplier());
        assertEquals(expectedMarketplaceRevenue,
                revenueShareDetails.getMarketplaceRevenue());
        assertEquals(expectedOperatorRevenue,
                revenueShareDetails.getOperatorRevenue());
        assertEquals(expectedBrokerRevenue,
                revenueShareDetails.getBrokerRevenue());
        assertEquals(expectedResellerRevenue,
                revenueShareDetails.getResellerRevenue());

    }

    private void assertDirectRevenue(SupplierRevenue supplierRevenue,
            BigDecimal expectedServiceRevenue,
            BigDecimal expectedMarketplaceRevenue,
            BigDecimal expectedOperatorRevenue) {
        assertEquals(expectedServiceRevenue, supplierRevenue.getDirectRevenue()
                .getServiceRevenue());
        assertEquals(expectedMarketplaceRevenue, supplierRevenue
                .getDirectRevenue().getMarketplaceRevenue());
        assertEquals(expectedOperatorRevenue, supplierRevenue
                .getDirectRevenue().getOperatorRevenue());

    }

    private void assertBrokerRevenue(SupplierRevenue supplierRevenue,
            BigDecimal expectedServiceRevenue,
            BigDecimal expectedMarketplaceRevenue,
            BigDecimal expectedOperatorRevenue, BigDecimal expectedBrokerRevenue) {
        assertEquals(expectedServiceRevenue, supplierRevenue.getBrokerRevenue()
                .getServiceRevenue());
        assertEquals(expectedMarketplaceRevenue, supplierRevenue
                .getBrokerRevenue().getMarketplaceRevenue());
        assertEquals(expectedOperatorRevenue, supplierRevenue
                .getBrokerRevenue().getOperatorRevenue());
        assertEquals(expectedBrokerRevenue, supplierRevenue.getBrokerRevenue()
                .getBrokerRevenue());
    }

    private void assertResellerRevenue(SupplierRevenue supplierRevenue,
            BigDecimal expectedServiceRevenue,
            BigDecimal expectedOverallRevenue,
            BigDecimal expectedMarketplaceRevenue,
            BigDecimal expectedOperatorRevenue,
            BigDecimal expectedResellerRevenue) {
        assertEquals(expectedServiceRevenue, supplierRevenue
                .getResellerRevenue().getServiceRevenue());
        assertEquals(expectedOverallRevenue, supplierRevenue
                .getResellerRevenue().getOverallRevenue());
        assertEquals(expectedMarketplaceRevenue, supplierRevenue
                .getResellerRevenue().getMarketplaceRevenue());
        assertEquals(expectedOperatorRevenue, supplierRevenue
                .getResellerRevenue().getOperatorRevenue());
        assertEquals(expectedResellerRevenue, supplierRevenue
                .getResellerRevenue().getResellerRevenue());
    }

}
