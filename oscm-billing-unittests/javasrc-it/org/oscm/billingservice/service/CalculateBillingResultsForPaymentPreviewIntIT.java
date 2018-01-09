/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.accountservice.bean.MarketingPermissionServiceBean;
import org.oscm.app.control.ApplicationServiceBaseStub;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorLocal;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.interceptor.DateFactory;
import org.oscm.paymentservice.bean.PaymentServiceStub;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.LdapAccessServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;

/**
 * Integration test for payment preview calculation.
 * 
 * @author muenz
 * 
 */
public class CalculateBillingResultsForPaymentPreviewIntIT extends EJBTestBase {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd hh:mm:ss";

    // use big amounts
    private static final Double ONE_TIME_FEE = new Double("100.00");

    // use big amounts
    private static final Double PRICE_PER_UNIT = new Double("1000.00");
    private DataService dm;
    private PriceModel priceModel;
    private Product product;
    private Organization supplier;
    private TechnicalProduct technicalProduct;
    private List<Subscription> subscriptions;

    private SharesCalculatorLocal sharesCalculator;
    private BillingServiceLocal service;

    /**
     * <ul>
     * Tests the payment preview calculation with following scenario:
     * <li>Cut-off day is before (lower than) invocation day</li>
     * <li>The subscription starts before the calculation period -> one time fee
     * already invoiced</li>
     * <li>Price model of subscription is: Price per hour 1000,00 EUR, one time
     * fee 100,00 EUR</li>
     * </ul>
     */
    @Test
    public void calculateBillingResultsForPaymentPreview_CutoffDayBeforeInvocation_SubscriptionStartBeforePeriod()
            throws Exception {

        // given -> cutoff day before invocation day
        final int cutOffDay = 3;
        final long invocationTime = defineInvocationTime("2012-12-06 14:00:00");

        // create subscription with start date before cutoff day calculation
        // range -> one time fee already invoiced
        createSubscription(calculateMillis("2012-11-15 08:00:00"), cutOffDay,
                PricingPeriod.HOUR, supplier, product);

        // when
        BillingRun billingRun = service.generatePaymentPreviewReport(supplier
                .getKey());

        // then - 86 hours should be invoiced without one time fee 86 *
        // PRICE_PER_HOUR
        assertEquals(BigDecimal.valueOf(86000).setScale(2),
                overallCosts(billingRun));
        assertEquals(calculateMillis("2012-12-01 00:00:00"),
                billingRun.getStart());
        assertEquals(invocationTime, billingRun.getEnd());
    }

    /**
     * <ul>
     * Tests the payment preview calculation with following scenario:
     * <li>Cut-off day is after (greater than) invocation day, but this test
     * doesn't proof the correct</li>
     * <li>The subscription starts IN the calculation period -> one time fee is
     * to be calculated</li>
     * <li>Price model of subscription is: Price per hour 1000,00 EUR, one time
     * fee 100,00 EUR</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void calculateBillingResultsForPaymentPreview_CutoffDayAfterInvocationDay_SubscriptionStartInPeriod()
            throws Exception {

        // given
        final int cutOffDay = 28;
        final long invocationTime = defineInvocationTime("2012-12-06 14:00:00");

        // cutoff day after invocation time - expected = 30 hours
        createSubscription(calculateMillis("2012-12-05 08:00:00"), cutOffDay,
                PricingPeriod.HOUR, supplier, product);

        // when
        BillingRun billingRun = service.generatePaymentPreviewReport(supplier
                .getKey());

        // then - 30 hours should be invoiced plus one time fee
        // 30 * PRICE_PER_HOUR + ONE_TIME_FEE
        assertEquals(BigDecimal.valueOf(30100).setScale(2),
                overallCosts(billingRun));
        assertEquals(calculateMillis("2012-12-01 00:00:00"),
                billingRun.getStart());
        assertEquals(invocationTime, billingRun.getEnd());
    }

    /**
     * <ul>
     * Tests the payment preview calculation with following scenario:
     * <li>Cut-off day is after (greater than) invocation day</li>
     * <li>The subscription starts BEFORE the calculation period -> no one time
     * fee is to be calculated</li>
     * <li>Price model of subscription is: Price per hour 1000,00 EUR, one time
     * fee 100,00 EUR</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void calculateBillingResultsForPaymentPreview_CutoffDayAfterInvocationDay_SubscriptionStartBeforePeriod()
            throws Exception {

        // given
        final int cutOffDay = 28;
        final long invocationTime = defineInvocationTime("2012-12-06 14:00:00");

        // cutoff day after invocation time - expected = 206 hours
        createSubscription(calculateMillis("2012-05-05 08:00:00"), cutOffDay,
                PricingPeriod.HOUR, supplier, product);

        // when
        BillingRun billingRun = service.generatePaymentPreviewReport(supplier
                .getKey());

        // then - period time of 206 hours * PRICE_PER_HOUR
        assertEquals(BigDecimal.valueOf(134000).setScale(2),
                overallCosts(billingRun));
        assertEquals(calculateMillis("2012-12-01 00:00:00"),
                billingRun.getStart());
        assertEquals(invocationTime, billingRun.getEnd());
    }

    /**
     * <ul>
     * Tests the payment preview calculation with following scenario:
     * <li>Cut-off day is equal invocation day</li>
     * <li>The subscription starts before the calculation period -> no one time
     * fee</li>
     * <li>Price model of subscription is: Price per hour 1000,00 EUR, one time
     * fee 100,00 EUR</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void calculateBillingResultsForPaymentPreview_CutoffEqualInvocationDay_SubscriptionStartBeforePeriod()
            throws Exception {

        // given
        final int cutOffDay = 6;
        final long invocationTime = defineInvocationTime("2012-12-06 14:00:00");

        // cutoff day equal invocation day - expected = 14 hours
        createSubscription(calculateMillis("2012-03-11 08:00:00"), cutOffDay,
                PricingPeriod.HOUR, supplier, product);

        // when
        BillingRun billingRun = service.generatePaymentPreviewReport(supplier
                .getKey());

        // then - expected are just the hours = 14 hour
        assertEquals(BigDecimal.valueOf(14000).setScale(2),
                overallCosts(billingRun));
        assertEquals(calculateMillis("2012-12-01 00:00:00"),
                billingRun.getStart());
        assertEquals(invocationTime, billingRun.getEnd());
    }

    /**
     * Test two subscriptions. One is started and terminated in the calculation
     * period, the other starts before calculation period. The 29th of February
     * should be considered also.
     * 
     * @throws Exception
     */
    @Test
    public void calculateBillingResultsForPaymentPreview_TwoSubscriptions()
            throws Exception {

        // given
        final int cutOffDaySubscription1 = 4;
        final int cutOffDaySubscription2 = 28;
        final long invocationTime = defineInvocationTime("2012-03-13 14:00:00");

        // 72 hours till invocation time
        createSubscription(calculateMillis("2012-03-10 14:00:00"),
                cutOffDaySubscription1, PricingPeriod.HOUR, supplier, product);

        // 350 hour should be invoiced - because cutoff day of 28th
        createSubscription(calculateMillis("2012-02-27 15:00:00"),
                cutOffDaySubscription2, PricingPeriod.HOUR, supplier, product);

        // when
        BillingRun billingRun = service.generatePaymentPreviewReport(supplier
                .getKey());

        // then - expected costs -> 72100 + 86000
        assertEquals(BigDecimal.valueOf(374100).setScale(2),
                overallCosts(billingRun));
        assertEquals(calculateMillis("2012-03-01 00:00:00"),
                billingRun.getStart());
        assertEquals(invocationTime, billingRun.getEnd());

    }

    /**
     * Tests, wheter the daylight saving hour will be considered in case of
     * {@link PricingPeriod#HOUR}. Between 2012-03-24 and 2012-03-25 it is
     * daylight saving where one hour is added. Therefore one hour less should
     * be invoiced!
     */
    @Test
    public void calculateBillingResultsForPaymentPreview_DaylightSavingHoursShouldBeConsidered()
            throws Exception {

        // given
        defineInvocationTime("2012-03-26 14:00:00");

        createSubscription(calculateMillis("2012-03-24 14:00:00"), 4,
                PricingPeriod.HOUR, supplier, product);
        // when
        BillingRun billingRun = service.generatePaymentPreviewReport(supplier
                .getKey());

        // then - expected costs 47100 -> 48 hours - 1 hour day light saving
        assertEquals(BigDecimal.valueOf(47100).setScale(2),
                overallCosts(billingRun));
    }

    /**
     * 
     * @param dateFormat
     * @return
     */
    private long calculateMillis(String dateSource) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(DATE_FORMAT_PATTERN);
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(dateSource));
            c.set(Calendar.MILLISECOND, 0);
            return c.getTimeInMillis();
        } catch (ParseException e) {
            fail("Unable to parse date.");
        }
        return 0;
    }

    /**
     * Returns the defined invocation time in millis.
     * 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @return invocation time in millis.
     */
    private long defineInvocationTime(String dateSource) {
        final long invocationTime = calculateMillis(dateSource);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(invocationTime);
        DateFactory.setInstance(new TestDateFactory(calendar.getTime()));
        return invocationTime;
    }

    private Subscription createSubscription(final long subscriptionStart,
            final int cutoffDay, PricingPeriod neededPricingPeriod,
            final Organization org, final Product p) {
        try {
            updatePricingPeriod(neededPricingPeriod, p);
            return runTX(new Callable<Subscription>() {
                @Override
                public Subscription call() throws Exception {
                    Subscription subscription = Subscriptions
                            .createSubscription(dm, org.getOrganizationId(),
                                    p.getProductId(), "sub-"
                                            + UUID.randomUUID().toString(),
                                    subscriptionStart, subscriptionStart,
                                    supplier, cutoffDay);
                    subscriptions.add(subscription);
                    return subscription;
                }
            });
        } catch (Exception e) {
            fail("Failed to create subscription for test setup.");
        }

        return null;
    }

    private BigDecimal overallCosts(BillingRun billingRun) {
        assertNotNull(billingRun);
        BigDecimal overallCosts = BigDecimal.valueOf(0).setScale(2);
        for (BillingResult br : billingRun.getBillingResultList()) {
            overallCosts = overallCosts.add(br.getGrossAmount());
        }
        return overallCosts;
    }

    private void updatePricingPeriod(final PricingPeriod newPricingPeriod,
            final Product p) {

        // if desired pricemodel is already set, do nothing
        if (newPricingPeriod.equals(p.getPriceModel().getPeriod())) {
            return;
        }

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Product pr = Products.findProduct(dm, supplier,
                            p.getProductId());
                    assertNotNull(
                            "Cannot find product in order to update pricing period!",
                            pr);
                    pr.getPriceModel().setPeriod(newPricingPeriod);
                    return null;
                }
            });
            product.getPriceModel().setPeriod(newPricingPeriod);
        } catch (Exception e) {
            fail("Unable to update pricing period!");
        }
    }

    @Override
    protected void setup(TestContainer container) throws Exception {
        subscriptions = new ArrayList<Subscription>();

        setupContainer(container);

        dm = container.get(DataService.class);
        service = container.get(BillingServiceLocal.class);

        setBaseDataCreationDate();

        runTX(new Callable<SupportedCurrency>() {
            @Override
            public SupportedCurrency call() throws Exception {
                return SupportedCurrencies.findOrCreate(dm, "EUR");
            }
        });

        supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                createPaymentTypes(dm);
                createOrganizationRoles(dm);
                SupportedCountries.createSomeSupportedCountries(dm);
                Organization org = Organizations.createOrganization(dm,
                        OrganizationRoleType.SUPPLIER);
                return org;
            }
        });

        technicalProduct = runTX(new Callable<TechnicalProduct>() {

            @Override
            public TechnicalProduct call() throws Exception {
                return TechnicalProducts.createTechnicalProduct(dm, supplier,
                        "techProduct1", false, ServiceAccessType.LOGIN);
            }
        });

        product = runTX(new Callable<Product>() {

            @SuppressWarnings("boxing")
            @Override
            public Product call() throws Exception {
                Product p = Products.createProduct(supplier, technicalProduct,
                        true, "product1", null, dm);
                priceModel = p.getPriceModel();
                priceModel.setPeriod(PricingPeriod.HOUR);
                SupportedCurrency template = new SupportedCurrency();
                template.setCurrency(Currency.getInstance("EUR"));
                template = (SupportedCurrency) dm
                        .getReferenceByBusinessKey(template);
                priceModel.setCurrency(template);
                priceModel.setOneTimeFee(new BigDecimal(ONE_TIME_FEE));
                priceModel.setPricePerPeriod(new BigDecimal(PRICE_PER_UNIT));
                return p;
            }
        });

    }

    /**
     * Setup the container.
     * 
     * @param container
     * @throws Exception
     */
    @SuppressWarnings("boxing")
    private void setupContainer(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new PaymentServiceStub());
        container.addBean(new ApplicationServiceBaseStub());
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new CommunicationServiceStub());
        container.addBean(new SessionServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new ImageResourceServiceStub());
        sharesCalculator = mock(SharesCalculatorLocal.class);
        when(
                sharesCalculator.performBrokerSharesCalculationRun(anyLong(),
                        anyLong())).thenReturn(Boolean.TRUE);
        when(
                sharesCalculator.performMarketplacesSharesCalculationRun(
                        anyLong(), anyLong())).thenReturn(Boolean.TRUE);
        when(
                sharesCalculator.performResellerSharesCalculationRun(anyLong(),
                        anyLong())).thenReturn(Boolean.TRUE);
        when(
                sharesCalculator.performSupplierSharesCalculationRun(anyLong(),
                        anyLong())).thenReturn(Boolean.TRUE);
        container.addBean(sharesCalculator);
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new TagServiceBean());
        container.addBean(new MarketingPermissionServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new RevenueCalculatorBean());
        container.addBean(new BillingServiceBean());
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

}
