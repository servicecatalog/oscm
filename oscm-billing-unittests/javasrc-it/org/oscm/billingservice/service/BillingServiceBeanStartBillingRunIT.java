/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: brandstetter                                                     
 *                                                                              
 *  Creation Date: 12.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorLocal;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.interceptor.DateFactory;
import org.oscm.test.BillingResultReader;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.EJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class BillingServiceBeanStartBillingRunIT extends EJBTestBase {
    private static final BigDecimal ONE_TIME_FEE = new BigDecimal(100.00);
    private static final BigDecimal MONTHLY_FEE = new BigDecimal(20.00);
    private static final BigDecimal PRICE_PER_HOUR = new BigDecimal(1000.00);

    DataService ds;
    BillingServiceLocal billingSvc;
    ConfigurationServiceLocal configurationSvc;
    BillingDataRetrievalServiceLocal billingDataSvc;
    SharesCalculatorLocal sharesCalculatorMock;
    BillingServiceBean billingBeanSpy;

    Organization platformOrg;
    Organization supplierOrg;
    Organization customerOrg;

    Product product;
    Product productWithExtBilling;
    
    @Override
    protected void setup(TestContainer container) throws Exception {
        setupContainer(container);

        setBaseDataCreationDate();
        initializeServices(container);
        createOrganizations();
        createProducts();
    }

    protected void setBaseDataCreationDate() {
        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    @SuppressWarnings("boxing")
    private void setupContainer(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new RevenueCalculatorBean());

        billingBeanSpy = spy(new BillingServiceBean());
        container.addBean(billingBeanSpy);

        sharesCalculatorMock = mock(SharesCalculatorLocal.class);
        when(
                sharesCalculatorMock.performBrokerSharesCalculationRun(
                        anyLong(), anyLong())).thenReturn(Boolean.TRUE);
        when(
                sharesCalculatorMock.performMarketplacesSharesCalculationRun(
                        anyLong(), anyLong())).thenReturn(Boolean.TRUE);
        when(
                sharesCalculatorMock.performResellerSharesCalculationRun(
                        anyLong(), anyLong())).thenReturn(Boolean.TRUE);
        when(
                sharesCalculatorMock.performSupplierSharesCalculationRun(
                        anyLong(), anyLong())).thenReturn(Boolean.TRUE);
        container.addBean(sharesCalculatorMock);
    }

    private void initializeServices(TestContainer container) {
        billingDataSvc = container.get(BillingDataRetrievalServiceLocal.class);
        ds = container.get(DataService.class);
        configurationSvc = container.get(ConfigurationServiceLocal.class);
        billingSvc = container.get(BillingServiceLocal.class);
    }

    private void createOrganizations() throws Exception,
            NonUniqueBusinessKeyException, ObjectNotFoundException {
        platformOrg = Organizations.createPlatformOperator(ds);
        supplierOrg = Organizations.createOrganization(ds, "SupplierOrgId",
                OrganizationRoleType.SUPPLIER, OrganizationRoleType.CUSTOMER);
        customerOrg = Organizations.createCustomer(ds, supplierOrg,
                "CustomerOrgId", false);
    }

    private void createProducts() throws Exception {
        createProduct("prodWithProRata", PricingPeriod.HOUR,
                PriceModelType.PRO_RATA, ONE_TIME_FEE, PRICE_PER_HOUR);
        
        createProductWithExternalBilling("prodWithExt1");
    }

    private Product createProduct(String prodId, PricingPeriod period,
            PriceModelType priceType, BigDecimal oneTimeFee,
            BigDecimal pricePerPeriod) throws NonUniqueBusinessKeyException,
            ObjectNotFoundException, Exception {
        product = Products.createProduct(supplierOrg.getOrganizationId(),
                prodId, "techProductId", ds);

        PriceModel priceModel = new PriceModel();
        priceModel.setPeriod(period);
        priceModel.setOneTimeFee(oneTimeFee);
        priceModel.setPricePerPeriod(pricePerPeriod);
        priceModel.setType(priceType);

        SupportedCurrency currency = createSupportedCurrency();
        priceModel.setCurrency(currency);
        product.setPriceModel(priceModel);

        product = Products.setStatusForProduct(ds, product,
                ServiceStatus.ACTIVE);
        return product;
    }
    
    private Product createProductWithExternalBilling(String prodId) throws NonUniqueBusinessKeyException, ObjectNotFoundException{
        
        productWithExtBilling = Products.createProduct(supplierOrg.getOrganizationId(),
                prodId, "techProductIdWithExtBill", ds);

        PriceModel priceModel = new PriceModel();
        priceModel.setExternal(true);
        priceModel.setType(PriceModelType.UNKNOWN);

        productWithExtBilling.setPriceModel(priceModel);
        productWithExtBilling = Products.setStatusForProduct(ds, productWithExtBilling,
                ServiceStatus.ACTIVE);
        return productWithExtBilling;
    }

    private SupportedCurrency createSupportedCurrency() throws Exception {
        SupportedCurrencies.findOrCreate(ds, "EUR");

        SupportedCurrency currency = new SupportedCurrency();
        currency.setCurrency(Currency.getInstance("EUR"));
        currency = (SupportedCurrency) ds.getReferenceByBusinessKey(currency);
        return currency;
    }

    private Subscription createSubscription(final String subscriptionStart,
            final int cutOffDay, final Product product) throws Exception {
        return runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Subscription subscription = Subscriptions.createSubscription(
                        ds, customerOrg.getOrganizationId(),
                        product.getProductId(), "sub-"
                                + UUID.randomUUID().toString(),
                        calculateMillis(subscriptionStart),
                        calculateMillis(subscriptionStart), supplierOrg,
                        cutOffDay);
                return subscription;
            }
        });
    }

    private void terminateSubscription(final long subscriptionKey,
            final String date) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                long invocationTime = defineInvocationTime(date);
                Subscription subscription = ds.find(Subscription.class,
                        subscriptionKey);
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscription.setDeactivationDate(new Long(invocationTime));
                ds.persist(subscription);
                return null;
            }
        });
    }

    private boolean startBillingRun(final long time) throws Exception {
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return Boolean.valueOf(billingSvc.startBillingRun(time));
            }
        });
        return result.booleanValue();
    }

    private void setBillingRunOffset(int offsetInDays) throws Exception {
        Long offset = new Long(offsetInDays * 24 * 3600 * 1000L);

        ConfigurationSetting config = new ConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                Configuration.GLOBAL_CONTEXT, offset.toString());
        configurationSvc.setConfigurationSetting(config);
    }

    private long defineInvocationTime(String dateSource) {
        return DateTimeHandling.defineInvocationTime(dateSource);
    }

    private long calculateMillis(String dateSource) {
        return DateTimeHandling.calculateMillis(dateSource);
    }

    private void verifyBillingRun(final long invocationTime, final int called) {
        Mockito.verify(billingBeanSpy, Mockito.times(called)).startBillingRun(
                Matchers.eq(invocationTime));
    }

    private void verifyShareCalculation(final long startOfLastMonth,
            final long endOfLastMonth, final int called) {
        Mockito.verify(sharesCalculatorMock, Mockito.times(called))
                .performBrokerSharesCalculationRun(startOfLastMonth,
                        endOfLastMonth);

        Mockito.verify(sharesCalculatorMock, Mockito.times(called))
                .performMarketplacesSharesCalculationRun(startOfLastMonth,
                        endOfLastMonth);

        Mockito.verify(sharesCalculatorMock, Mockito.times(called))
                .performResellerSharesCalculationRun(startOfLastMonth,
                        endOfLastMonth);

        Mockito.verify(sharesCalculatorMock, Mockito.times(called))
                .performSupplierSharesCalculationRun(startOfLastMonth,
                        endOfLastMonth);
    }

    private BillingResult loadExistingBillingResult(final long subscriptionKey,
            final long startOfPeriod, final long endOfPeriod) throws Exception {

        return runTX(new Callable<BillingResult>() {
            @Override
            public BillingResult call() throws Exception {
                return BillingResultReader.loadBillingResult(ds,
                        subscriptionKey, startOfPeriod, endOfPeriod);
            }
        });
    }

    private List<BillingResult> loadExistingBillingResults(
            final long subscriptionKey, final long startOfFirstPeriod,
            final long endOfLastPeriod) throws Exception {

        return runTX(new Callable<List<BillingResult>>() {
            @Override
            public List<BillingResult> call() throws Exception {
                return BillingResultReader.loadBillingResults(ds,
                        subscriptionKey, startOfFirstPeriod, endOfLastPeriod);
            }
        });
    }

    private void checkBillingResult(final long subscriptionKey,
            final long organizationKey, final long startOfPeriod,
            final long endOfPeriod, final BigDecimal grossAmount)
            throws Exception {
        BillingResult billingResult = loadExistingBillingResult(
                subscriptionKey, startOfPeriod, endOfPeriod);

        assertNotNull(billingResult);
        assertEquals(organizationKey, billingResult.getOrganizationTKey());
        assertEquals(subscriptionKey, billingResult.getSubscriptionKey()
                .longValue());
        assertEquals(startOfPeriod, billingResult.getPeriodStartTime());
        assertEquals(endOfPeriod, billingResult.getPeriodEndTime());
        assertEquals(grossAmount.doubleValue(), billingResult.getGrossAmount()
                .doubleValue(), 0.1);
    }

    private Product createProductWithTimeSlices() throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return createProduct("prodWithTimeSlices", PricingPeriod.MONTH,
                        PriceModelType.PER_UNIT, ONE_TIME_FEE, MONTHLY_FEE);
            }
        });
    }

    @Test
    public void startBillingRun() throws Exception {
        boolean result = startBillingRun(0);
        assertTrue(result);
    }

    /**
     * createProduct("prodWithProRata", PricingPeriod.HOUR,
     * PriceModelType.PRO_RATA, ONE_TIME_FEE, PRICE_PER_HOUR); The billing
     * period corresponds to a calendar month, that means additional to the
     * billing calculation a revenue share calculation is done.
     * 
     * billing period.......: 2012-11-01 00:00:00 - 2012-12-01 00:00:00<br>
     * revenue share period.: 2012-11-01 00:00:00 - 2012-12-01 00:00:00<br>
     */
    @Test
    public void startBillingRun_RevenueShareCalculation() throws Exception {
        // given
        final long invocationTime = defineInvocationTime("2012-12-05 14:00:00");
        final int billingRunOffsetInDays = 4;
        setBillingRunOffset(billingRunOffsetInDays);

        // when
        boolean result = startBillingRun(invocationTime);

        // then
        assertTrue(result);
        verifyBillingRun(invocationTime, 1);

        // revenue share calculation
        long startOfLastMonth = calculateMillis("2012-11-01 00:00:00");
        long endOfLastMonthxx = calculateMillis("2012-12-01 00:00:00");
        verifyShareCalculation(startOfLastMonth, endOfLastMonthxx, 1);
    }

    /**
     * Create two subscriptions with different cutoff days and different
     * activation time. The invocation time to start the billing run leads to
     * bill subscription1 (5th of month - 2 days offset = cutoff day 3).
     * Subscription2 (with cutoff day 1) is also billed (filling the gaps).
     */
    @Test
    public void startBillingRun_FillingGaps() throws Exception {
        // given
        final long invocationTime = defineInvocationTime("2012-12-05 14:00:00");
        final int billingRunOffsetInDays = 2;
        setBillingRunOffset(billingRunOffsetInDays);

        final int cutOffDay1 = 3;// period end
        Subscription subscription1 = createSubscription("2012-10-15 7:00:00",
                cutOffDay1, product);
        final int cutOffDay2 = 1;// period end
        Subscription subscription2 = createSubscription("2012-11-15 9:00:00",
                cutOffDay2, product);
        
        /*final int cutOffDay3 = 1;// period end
        Subscription subscription3 = createSubscription("2012-11-14 8:00:00",
                cutOffDay3, productWithExtBilling);*/

        // when
        boolean result = startBillingRun(invocationTime);

        // then
        assertTrue(result);
        verifyBillingRun(invocationTime, 1);
        long startOfPeriod1 = calculateMillis("2012-11-03 00:00:00");
        long endOfPeriodxx1 = calculateMillis("2012-12-03 00:00:00");
        BigDecimal expectedGrossAmount1 = new BigDecimal(30 * 24)
                .multiply(PRICE_PER_HOUR);
        checkBillingResult(subscription1.getKey(), customerOrg.getKey(),
                startOfPeriod1, endOfPeriodxx1, expectedGrossAmount1);

        long startOfPeriod2 = calculateMillis("2012-11-01 00:00:00");
        long endOfPeriodxx2 = calculateMillis("2012-12-01 00:00:00");
        BigDecimal expectedGrossAmount2 = new BigDecimal(15 * 24 + 15)
                .multiply(PRICE_PER_HOUR);
        expectedGrossAmount2 = expectedGrossAmount2.add(ONE_TIME_FEE);
        checkBillingResult(subscription2.getKey(), customerOrg.getKey(),
                startOfPeriod2, endOfPeriodxx2, expectedGrossAmount2);
    }

    /**
     * Subscription is 5 days active, started and terminated inside the billing
     * period.
     */
    @Test
    public void startBillingRun_TerminatedSubscriptionInsideBillingPeriod()
            throws Exception {
        // given
        final long invocationTime = defineInvocationTime("2012-06-05 14:00:00");
        final int billingRunOffsetInDays = 2;
        setBillingRunOffset(billingRunOffsetInDays);
        final int cutOffDay = 3;// period end
        Subscription subscription = createSubscription("2012-05-10 12:00:00",
                cutOffDay, product);
        terminateSubscription(subscription.getKey(), "2012-05-15 12:00:00");

        // when
        boolean result = startBillingRun(invocationTime);

        // then
        assertTrue(result);
        verifyBillingRun(invocationTime, 1);
        long startOfPeriod = calculateMillis("2012-05-03 00:00:00");
        long endOfPeriodxx = calculateMillis("2012-06-03 00:00:00");
        BigDecimal expectedGrossAmount = new BigDecimal(5 * 24)
                .multiply(PRICE_PER_HOUR);
        expectedGrossAmount = expectedGrossAmount.add(ONE_TIME_FEE);
        checkBillingResult(subscription.getKey(), customerOrg.getKey(),
                startOfPeriod, endOfPeriodxx, expectedGrossAmount);
    }

    /**
     * Subscription is started and terminated outside the billing period.
     */
    @Test
    public void startBillingRun_TerminatedSubscriptionOutsideBillingPeriod()
            throws Exception {
        // given
        final long invocationTime = defineInvocationTime("2011-12-19 14:00:00");
        final int billingRunOffsetInDays = 4;
        setBillingRunOffset(billingRunOffsetInDays);
        final int cutOffDay = 5;// period end
        Subscription subscription = createSubscription("2011-10-10 12:00:00",
                cutOffDay, product);
        terminateSubscription(subscription.getKey(), "2011-12-10 12:00:00");

        // when
        boolean result = startBillingRun(invocationTime);

        // then
        assertTrue(result);
        verifyBillingRun(invocationTime, 1);
        long startOfPeriod = calculateMillis("2011-11-05 00:00:00");
        long endOfPeriodxx = calculateMillis("2011-12-05 00:00:00");
        BigDecimal expectedGrossAmount = new BigDecimal(30 * 24)
                .multiply(PRICE_PER_HOUR);
        checkBillingResult(subscription.getKey(), customerOrg.getKey(),
                startOfPeriod, endOfPeriodxx, expectedGrossAmount);
    }

    /**
     * Bug 10095. A subscription is terminated before the billing period.
     * However, in the given case the billing must be postponed. This is the
     * case because monthly time slices and a cut of day have been defined. This
     * causes the so called 'billing period overlapping' where the period of the
     * time slice and billing period do not match. In this case the billing must
     * be postponed to the month in of the completed time slice.
     */
    @Test
    public void startBillingRun_terminatedSubscriptionWithOverlappingTimeslice()
            throws Exception {
        // given a terminated subscription with the last time slice overlapping
        final int cutOffDay = 20;
        Subscription subscription = createSubscription("2013-03-13 12:00:00",
                cutOffDay, createProductWithTimeSlices());
        terminateSubscription(subscription.getKey(), "2013-03-14 12:00:00");

        // when running billing in the next month
        long invocationTime = defineInvocationTime("2013-05-25 14:00:00");
        boolean result = startBillingRun(invocationTime);

        // then the one time fee is charged in the first month and the
        // monthly fee in the 2nd
        assertTrue(result);
        long startOfFirstPeriod = calculateMillis("2013-02-20 00:00:00");
        long endOfFirstPeriodxx = calculateMillis("2013-03-20 00:00:00");
        checkBillingResult(subscription.getKey(), customerOrg.getKey(),
                startOfFirstPeriod, endOfFirstPeriodxx, ONE_TIME_FEE);
        long startOfSecondPeriod = calculateMillis("2013-03-20 00:00:00");
        long endOfSecondPeriodxx = calculateMillis("2013-04-20 00:00:00");
        checkBillingResult(subscription.getKey(), customerOrg.getKey(),
                startOfSecondPeriod, endOfSecondPeriodxx, MONTHLY_FEE);
    }

    /**
     * Billing run is called 1 second before the first billing period of the
     * subscription ends. In this case no billing results are created.
     * 
     * first billing period..........: 2010-02-15 00:00:00 - 2010-03-15 00:00:00<br>
     * invocation time (with offset).: 2010-03-14 23:59:59<br>
     */
    @Test
    public void startBillingRun_invoked1SecondBeforeFirstBillablePeriodEnd()
            throws Exception {

        // given
        final int cutOffDay = 15;
        Subscription subscription = createSubscription("2010-03-13 12:00:00",
                cutOffDay, product);

        setBillingRunOffset(1);
        long invocationTime = defineInvocationTime("2010-03-15 23:59:59");

        // when
        startBillingRun(invocationTime);

        // then
        // no billing result expected
        long startOfFirstPeriod = calculateMillis("2010-02-15 00:00:00");
        long endOfFirstPeriod = calculateMillis("2010-03-15 00:00:00");

        BillingResult billingResult = loadExistingBillingResult(
                subscription.getKey(), startOfFirstPeriod, endOfFirstPeriod);

        assertEquals(null, billingResult);
    }

    /**
     * Billing run is called directly when first billing period of the
     * subscription ends. In this case single billing result is created.
     * 
     * first billing period..........: 2010-02-15 00:00:00 - 2010-03-15 00:00:00<br>
     * invocation time (with offset).: 2010-03-15 00:00:00<br>
     */
    @Test
    public void startBillingRun_invokedDirectlyOnFirstBillablePeriodEnd()
            throws Exception {

        // given
        final int cutOffDay = 15;
        Subscription subscription = createSubscription("2010-03-13 12:00:00",
                cutOffDay, product);

        setBillingRunOffset(1);
        long invocationTime = defineInvocationTime("2010-03-16 00:00:00");

        // when
        startBillingRun(invocationTime);

        // then
        // expected billing result for period 2010-02-15 00:00:00 - 2010-03-15
        // 00:00:00
        long startOfFirstPeriod = calculateMillis("2010-02-15 00:00:00");
        long endOfFirstPeriod = calculateMillis("2010-03-15 00:00:00");

        BillingResult billingResult = loadExistingBillingResult(
                subscription.getKey(), startOfFirstPeriod, endOfFirstPeriod);

        assertNotNull(billingResult);
        assertEquals(startOfFirstPeriod, billingResult.getPeriodStartTime());
        assertEquals(endOfFirstPeriod, billingResult.getPeriodEndTime());
        assertEquals(subscription.getKey(), billingResult.getSubscriptionKey()
                .longValue());
    }

    /**
     * Billing run is called several months after subscription activation. In
     * this case several billing results are created.
     * 
     * subscription activation .......: 2010-03-13 12:00:00<br>
     * invocation time (with offset).: 2010-10-22 14:00:00<br>
     */
    @Test
    public void startBillingRun_afterSeveralMonths() throws Exception {

        // given
        final int cutOffDay = 20;
        Subscription subscription = createSubscription("2010-03-13 12:00:00",
                cutOffDay, product);

        setBillingRunOffset(1);
        long invocationTime = defineInvocationTime("2010-10-23 14:00:00");

        // when
        startBillingRun(invocationTime);

        // then
        List<BillingResult> billingResults = loadExistingBillingResults(
                subscription.getKey(), calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // expected billing results for periods
        // 2010-02-20 00:00:00 - 2010-03-20 00:00:00
        // 2010-03-20 00:00:00 - 2010-04-20 00:00:00
        // 2010-04-20 00:00:00 - 2010-05-20 00:00:00
        // 2010-05-20 00:00:00 - 2010-06-20 00:00:00
        // 2010-06-20 00:00:00 - 2010-07-20 00:00:00
        // 2010-07-20 00:00:00 - 2010-08-20 00:00:00
        // 2010-08-20 00:00:00 - 2010-09-20 00:00:00
        // 2010-09-20 00:00:00 - 2010-10-20 00:00:00
        assertEquals(8, billingResults.size());

        // checks single billing result
        long startOfLastPeriod = calculateMillis("2010-09-20 00:00:00");
        long endOfLastPeriod = calculateMillis("2010-10-20 00:00:00");

        BillingResult billingResult = loadExistingBillingResult(
                subscription.getKey(), startOfLastPeriod, endOfLastPeriod);

        assertNotNull(billingResult);
        assertEquals(startOfLastPeriod, billingResult.getPeriodStartTime());
        assertEquals(endOfLastPeriod, billingResult.getPeriodEndTime());
        assertEquals(subscription.getKey(), billingResult.getSubscriptionKey()
                .longValue());
    }

    /**
     * Billing run is called after first subscription activation but before
     * second subscription activation. In this case billing results are created
     * only for first subscription.
     * 
     * 1st subscription activation .......: 2010-03-13 12:00:00<br>
     * 2nd subscription activation .......: 2010-06-21 12:00:00<br>
     * invocation time (with offset).: 2010-05-15 14:00:00<br>
     */
    @Test
    public void startBillingRun_beetweenTwoSubscriptionActivation()
            throws Exception {

        // given
        final int cutOffDay = 20;

        Subscription firstSubscription = createSubscription(
                "2010-03-13 12:00:00", cutOffDay, product);
        Subscription secondSubscription = createSubscription(
                "2010-06-21 12:00:00", cutOffDay, product);

        setBillingRunOffset(1);
        long invocationTime = defineInvocationTime("2010-05-16 14:00:00");

        // when
        startBillingRun(invocationTime);

        // then
        List<BillingResult> billingResults = loadExistingBillingResults(
                firstSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // periods of expected billing results for the first subscription
        // 2010-02-20 00:00:00 - 2010-03-20 00:00:00
        // 2010-03-20 00:00:00 - 2010-04-20 00:00:00
        assertEquals(2, billingResults.size());

        // checks single billing result
        long startOfLastPeriod = calculateMillis("2010-03-20 00:00:00");
        long endOfLastPeriod = calculateMillis("2010-04-20 00:00:00");

        BillingResult billingResult = loadExistingBillingResult(
                firstSubscription.getKey(), startOfLastPeriod, endOfLastPeriod);

        assertNotNull(billingResult);
        assertEquals(startOfLastPeriod, billingResult.getPeriodStartTime());
        assertEquals(endOfLastPeriod, billingResult.getPeriodEndTime());
        assertEquals(firstSubscription.getKey(), billingResult
                .getSubscriptionKey().longValue());

        List<BillingResult> results = loadExistingBillingResults(
                secondSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // no billing results expected for the second subscription
        assertTrue(results.isEmpty());
    }

    /**
     * Billing run is called after first and second subscription activation but
     * before third subscription activation. In this case billing results are
     * created only for first and second subscription.
     * 
     * 1st subscription activation .......: 2010-03-13 12:00:00<br>
     * 2nd subscription activation .......: 2010-05-21 12:00:00<br>
     * 3rd subscription activation .......: 2010-08-03 12:00:00<br>
     * invocation time (with offset).: 2010-07-15 14:00:00<br>
     */
    @Test
    public void startBillingRun_beetweenThreeSubscriptionActivation()
            throws Exception {

        // given
        final int cutOffDay = 20;

        Subscription firstSubscription = createSubscription(
                "2010-03-13 12:00:00", cutOffDay, product);
        Subscription secondSubscription = createSubscription(
                "2010-05-21 12:00:00", cutOffDay, product);
        Subscription thirdSubscription = createSubscription(
                "2010-08-03 12:00:00", cutOffDay, product);

        setBillingRunOffset(1);
        long invocationTime = defineInvocationTime("2010-07-16 14:00:00");

        // when
        startBillingRun(invocationTime);

        // then
        List<BillingResult> firstSubscriptionResults = loadExistingBillingResults(
                firstSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // periods of expected billing results for the first subscription
        // 2010-02-20 00:00:00 - 2010-03-20 00:00:00
        // 2010-03-20 00:00:00 - 2010-04-20 00:00:00
        // 2010-04-20 00:00:00 - 2010-05-20 00:00:00
        // 2010-05-20 00:00:00 - 2010-06-20 00:00:00
        assertEquals(4, firstSubscriptionResults.size());

        List<BillingResult> secondSubscriptionResults = loadExistingBillingResults(
                secondSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // periods of expected billing results for the first subscription
        // 2010-05-20 00:00:00 - 2010-06-20 00:00:00
        assertEquals(1, secondSubscriptionResults.size());

        List<BillingResult> thirdSubscriptionResults = loadExistingBillingResults(
                thirdSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // no billing results expected for the third subscription
        assertTrue(thirdSubscriptionResults.isEmpty());
    }
    
    /**
     * Billing run is called after first and second subscription activation but
     * before third subscription activation. In this case billing results are
     * created only for first and second subscription.
     * 
     * 1st subscription activation .......: 2010-03-13 12:00:00<br>
     * 2nd subscription activation .......: 2010-05-21 12:00:00<br>
     * 3rd subscription activation .......: 2010-06-01 12:00:00<br>
     * invocation time (with offset).: 2010-07-15 14:00:00<br>
     */
    @Test
    public void startBillingRun_includesSubscriptionWithExternalBilling()
            throws Exception {

        // given
        final int cutOffDay = 20;

        Subscription firstSubscription = createSubscription(
                "2010-03-13 12:00:00", cutOffDay, product);
        Subscription secondSubscription = createSubscription(
                "2010-05-21 12:00:00", cutOffDay, productWithExtBilling);
        Subscription thirdSubscription = createSubscription(
                "2010-06-01 12:00:00", cutOffDay, productWithExtBilling);

        setBillingRunOffset(1);
        long invocationTime = defineInvocationTime("2010-07-16 14:00:00");

        // when
        startBillingRun(invocationTime);

        // then
        List<BillingResult> firstSubscriptionResults = loadExistingBillingResults(
                firstSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // periods of expected billing results for the first subscription
        // 2010-02-20 00:00:00 - 2010-03-20 00:00:00
        // 2010-03-20 00:00:00 - 2010-04-20 00:00:00
        // 2010-04-20 00:00:00 - 2010-05-20 00:00:00
        // 2010-05-20 00:00:00 - 2010-06-20 00:00:00
        assertEquals(4, firstSubscriptionResults.size());

        List<BillingResult> secondSubscriptionResults = loadExistingBillingResults(
                secondSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // no billing results expected for the second subscription
        assertTrue(secondSubscriptionResults.isEmpty());

        List<BillingResult> thirdSubscriptionResults = loadExistingBillingResults(
                thirdSubscription.getKey(),
                calculateMillis("2010-01-20 00:00:00"),
                calculateMillis("2010-12-20 00:00:00"));

        // no billing results expected for the third subscription
        assertTrue(thirdSubscriptionResults.isEmpty());
    }

}
