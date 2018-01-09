/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Aleh Khomich                                                      
 *                                                                              
 *  Creation Date: 23.07.2010                                                      
 *                                                                              
 *  Completion Time: 23.07.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.oscm.test.Numbers.BD100;
import static org.oscm.test.Numbers.BD1000;
import static org.oscm.test.Numbers.BD120;
import static org.oscm.test.Numbers.BD300;
import static org.oscm.test.Numbers.BD400;
import static org.oscm.test.Numbers.BD500;
import static org.oscm.test.Numbers.BD80;
import static org.oscm.test.Numbers.BD9;
import static org.oscm.test.Numbers.BD90;
import static org.oscm.test.Numbers.BD900;
import static org.oscm.test.Numbers.L10;
import static org.oscm.test.Numbers.L20;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.communicationservice.bean.CommunicationServiceBean;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.UsageLicense;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.paymentservice.bean.PaymentServiceStub;
import org.oscm.serviceprovisioningservice.bean.SteppedPriceComparator;
import org.oscm.test.EJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.XMLTestValidator;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.types.constants.BillingResultXMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * JUnit test.
 * 
 * @author khomich
 * 
 */
public class BillingServiceBeanSteppedPriceIT extends EJBTestBase {

    private DataService mgr;
    private BillingServiceLocal billingService;
    private Organization supplierAndProvider;
    private Organization customer;
    private TechnicalProduct technicalProduct;
    private Product product;
    private Subscription subscription;
    private Long[] limitArray = new Long[] { L10, L20, null };
    private XMLTestValidator xmlValidator;

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new PaymentServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
                // stub empty implementation
            }
        });
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(mock(SharesDataRetrievalServiceBean.class));
        container.addBean(new RevenueCalculatorBean());
        container.addBean(mock(SharesCalculatorBean.class));
        container.addBean(new CommunicationServiceBean());
        container.addBean(new UserLicenseDao());
        container.addBean(new BillingServiceBean());

        mgr = container.get(DataService.class);
        billingService = container.get(BillingServiceLocal.class);
        xmlValidator = new XMLTestValidator();
        xmlValidator.setup();

        // billing offset set to 0
        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        setBaseDataCreationDate();

        // create commons objects for tests
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createOrganizationRoles(mgr);
                createSupportedCurrencies(mgr);
                createPaymentTypes(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                supplierAndProvider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);

                technicalProduct = TechnicalProducts.createTechnicalProduct(mgr,
                        supplierAndProvider, "techProdId", false,
                        ServiceAccessType.LOGIN);

                product = Products.createProduct(supplierAndProvider,
                        technicalProduct, true, "productId", null, mgr);

                customer = Organizations.createCustomer(mgr,
                        supplierAndProvider);

                return null;
            }
        });
    }

    protected void setBaseDataCreationDate() {
        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, 2008);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    /**
     * Billing test for price model with stepped price. The free periods ends
     * before billing period. Parameter are modified before billing period
     * 
     * @throws Exception
     */
    @Test
    public void WithSteppedPricesForPriceModel_FreePeriodBefore()
            throws Exception {
        int numUser = 4;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY) - 1;
        BigDecimal expectedPrice = new BigDecimal("1701.00");
        final Long[] limitArrayForDifferentAssignmentTime = new Long[] {
                Long.valueOf(2), Long.valueOf(3), null };

        BigDecimal[] priceArray = { BD500, BD400, BD300 };
        BigDecimal[] stepAmountArray = { BD1000, BD400, BD300 };

        BigDecimal pricePerUserassigment = BigDecimal.ZERO;
        BigDecimal oneTimeFee = BigDecimal.TEN;
        testBillingWithSteppedPricesForPriceModel(freePeriod, numUser,
                expectedPrice, true, limitArrayForDifferentAssignmentTime,
                priceArray, subscriptionCreationTime,
                subscriptionActivationTime, billingTime, pricePerUserassigment,
                oneTimeFee, stepAmountArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price. The free periods ends
     * with the first day of the billing period. Parameter are modified before
     * billing period
     * 
     * @throws Exception
     */
    @Test
    public void PriceModel_FreePeriodEnd_Equal_BillingStartTime()
            throws Exception {
        int numUser = 4;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY);
        BigDecimal expectedPrice = new BigDecimal("1711.00");
        final Long[] limitArrayForDifferentAssignmentTime = new Long[] {
                Long.valueOf(2), Long.valueOf(3), null };

        BigDecimal[] priceArray = { BD500, BD400, BD300 };
        BigDecimal[] stepAmountArray = { BD1000, BD400, BD300 };

        BigDecimal pricePerUserassigment = BigDecimal.ZERO;
        BigDecimal oneTimeFee = BigDecimal.TEN;
        testBillingWithSteppedPricesForPriceModel(freePeriod, numUser,
                expectedPrice, true, limitArrayForDifferentAssignmentTime,
                priceArray, subscriptionCreationTime,
                subscriptionActivationTime, billingTime, pricePerUserassigment,
                oneTimeFee, stepAmountArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price. The free periods ends
     * after the first day of the billing period. Parameter are modified before
     * billing period
     * 
     * @throws Exception
     */
    @Test
    public void PriceModel_FreePeriodEnd_After_BillingStartTime()
            throws Exception {
        int numUser = 4;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY) + 10;
        BigDecimal expectedPrice = new BigDecimal("1293.86");
        final Long[] limitArrayForDifferentAssignmentTime = new Long[] {
                Long.valueOf(2), Long.valueOf(3), null };

        BigDecimal[] priceArray = { BD500, BD400, BD300 };
        BigDecimal[] stepAmountArray = { BD1000, new BigDecimal("283.18") };

        BigDecimal pricePerUserassigment = BigDecimal.ZERO;
        BigDecimal oneTimeFee = BigDecimal.TEN;
        testBillingWithSteppedPricesForPriceModel(freePeriod, numUser,
                expectedPrice, true, limitArrayForDifferentAssignmentTime,
                priceArray, subscriptionCreationTime,
                subscriptionActivationTime, billingTime, pricePerUserassigment,
                oneTimeFee, stepAmountArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price. The free periods ends
     * with the first day of the billing period. Parameter are modified before
     * billing period. Activation date is 2 month before
     * 
     * @throws Exception
     */
    @Test
    public void PriceModel_FreePeriodEnd2Month_Equal_BillingStartTime()
            throws Exception {
        int numUser = 4;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.MAY, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY)
                + getDaysOfMonth(testYear, Calendar.MARCH);
        BigDecimal expectedPrice = new BigDecimal("1711.00");
        final Long[] limitArrayForDifferentAssignmentTime = new Long[] {
                Long.valueOf(2), Long.valueOf(3), null };

        BigDecimal[] priceArray = { BD500, BD400, BD300 };
        BigDecimal[] stepAmountArray = { BD1000, BD400, BD300 };

        BigDecimal pricePerUserassigment = BigDecimal.TEN;
        BigDecimal oneTimeFee = BigDecimal.TEN;
        testBillingWithSteppedPricesForPriceModel(freePeriod, numUser,
                expectedPrice, true, limitArrayForDifferentAssignmentTime,
                priceArray, subscriptionCreationTime,
                subscriptionActivationTime, billingTime, pricePerUserassigment,
                oneTimeFee, stepAmountArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForPriceModelStep0()
            throws Exception {
        int numUser = 4;
        BigDecimal expectedPrice = new BigDecimal("1258.34");
        final Long[] limitArrayForDifferentAssignmentTime = new Long[] {
                Long.valueOf(2), Long.valueOf(3), null };

        BigDecimal[] priceArray = { BD500, BD400, BD300 };
        long[] freeAmountArray = { 0, 2, 3 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1400) };
        BigDecimal[] stepAmountArray = { BD1000, new BigDecimal("257.34") };
        testBillingWithSteppedPricesForPriceModel(0, numUser, expectedPrice,
                true, limitArrayForDifferentAssignmentTime, priceArray,
                freeAmountArray, additionalPriceArray, stepAmountArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForPriceModelStep1()
            throws Exception {
        int numUser = 10;
        BigDecimal expectedPrice = BigDecimal.valueOf(901L);

        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        long[] freeAmountArray = { 1, 2, 3 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1900) };

        testBillingWithSteppedPricesForPriceModel(0, numUser, expectedPrice,
                false, limitArray, priceArray, freeAmountArray,
                additionalPriceArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price. The free periods ends
     * with the first day of the billing period. Parameter are modified before
     * billing period
     * 
     * @throws Exception
     */
    @Test
    public void PriceModel_FreePeriodEnd_Equal_BillingStartTime_Bug8149()
            throws Exception {
        int numUser = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY);
        BigDecimal expectedPrice = new BigDecimal("11.00");
        final Long[] limitArrayForDifferentAssignmentTime = new Long[] {
                Long.valueOf(2), Long.valueOf(3), null };

        BigDecimal[] priceArray = { BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO };
        BigDecimal[] stepAmountArray = { BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO };

        BigDecimal pricePerUserassigment = BigDecimal.ZERO;
        BigDecimal oneTimeFee = BigDecimal.TEN;
        testBillingWithSteppedPricesForPriceModel(freePeriod, numUser,
                expectedPrice, true, limitArrayForDifferentAssignmentTime,
                priceArray, subscriptionCreationTime,
                subscriptionActivationTime, billingTime, pricePerUserassigment,
                oneTimeFee, stepAmountArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForPriceModelStep2()
            throws Exception {
        int numUser = 20;
        BigDecimal expectedPrice = BigDecimal.valueOf(2621L);

        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        long[] freeAmountArray = { 1, 2, 3 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1900) };

        testBillingWithSteppedPricesForPriceModel(0, numUser, expectedPrice,
                false, limitArray, priceArray, freeAmountArray,
                additionalPriceArray);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForPriceModelStep3()
            throws Exception {
        int numUser = 30;
        BigDecimal expectedPrice = BigDecimal.valueOf(4061L);

        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        long[] freeAmountArray = { 1, 2, 3 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1900) };

        testBillingWithSteppedPricesForPriceModel(0, numUser, expectedPrice,
                false, limitArray, priceArray, freeAmountArray,
                additionalPriceArray);
        xmlValidator.validateBillingResultXML();
    }

    private void testBillingWithSteppedPricesForPriceModel(int freePeriod,
            int numUser, BigDecimal expectedPrice,
            final boolean differentAssignmentTime, final Long[] limitArray,
            final BigDecimal[] priceArray, final long[] freeAmountArray,
            final BigDecimal[] additionalPriceArray,
            final BigDecimal... stepAmountArray) throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                testMonth - 1, testDay);
        long subscriptionActivationTime = getTimeInMillisForBilling(testYear,
                testMonth - 1, testDay);

        testBillingWithSteppedPricesForPriceModel(freePeriod, numUser,
                expectedPrice, differentAssignmentTime, limitArray, priceArray,
                freeAmountArray, additionalPriceArray, subscriptionCreationTime,
                subscriptionActivationTime, BigDecimal.ZERO, BigDecimal.ZERO,
                billingTime, stepAmountArray);

    }

    private void testBillingWithSteppedPricesForPriceModel(int freePeriod,
            int numUser, BigDecimal expectedPrice,
            final boolean differentAssignmentTime, final Long[] limitArray,
            final BigDecimal[] priceArray, final long[] freeAmountArray,
            final BigDecimal[] additionalPriceArray,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime,
            final BigDecimal pricePerUserAssigment, final BigDecimal oneTimeFee,
            final long billingTime, final BigDecimal... stepAmountArray)
            throws Exception {
        int userNumber = numUser;
        int stepNum = 3;

        initDataPriceModel(userNumber, subscriptionCreationTime,
                subscriptionActivationTime, stepNum, limitArray, priceArray,
                freeAmountArray, additionalPriceArray, differentAssignmentTime,
                freePeriod, pricePerUserAssigment, oneTimeFee);

        ValidateBillingUserAssingmentCostsNode(expectedPrice, billingTime,
                stepAmountArray);
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @param pricePerUserssigment
     * @param oneTimeFee
     * 
     * @throws Exception
     */
    private void testBillingWithSteppedPricesForPriceModel(int freePeriod,
            int numUser, BigDecimal expectedPrice,
            final boolean differentAssignmentTime, final Long[] limitArray,
            final BigDecimal[] priceArray, final long subscriptionCreationTime,
            final long subscriptionActivationTime, final long billingTime,
            BigDecimal pricePerUserAssigment, BigDecimal oneTimeFee,
            final BigDecimal... stepAmountArray) throws Exception {

        int stepNum = 3;

        initDataPriceModel(numUser, subscriptionCreationTime,
                subscriptionActivationTime, stepNum, limitArray, priceArray,
                differentAssignmentTime, freePeriod, pricePerUserAssigment,
                oneTimeFee);

        ValidateBillingUserAssingmentCostsNode(expectedPrice, billingTime,
                stepAmountArray);

    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void SteppedPricesForEventsStep1_freePeriodEndsBefore()
            throws Exception {
        int numEvents = 10;
        BigDecimal expectedCosts = new BigDecimal("903.00");
        BigDecimal[] stepCosts = { BD900 };
        BigDecimal[] septEntityCounts = { BD9 };
        testBillingWithSteppedPricesForEvents(numEvents, expectedCosts,
                septEntityCounts, stepCosts, 28);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test(expected = NoResultException.class)
    public void SteppedPricesForEventsStep1_freePeriodEndsAfter()
            throws Exception {
        int numEvents = 10;
        BigDecimal expectedCosts = new BigDecimal("0.00");
        BigDecimal[] stepCosts = { BD900 };
        BigDecimal[] septEntityCounts = { BD9 };

        testBillingWithSteppedPricesForEvents(numEvents, expectedCosts,
                septEntityCounts, stepCosts, 60);
    }

    /**
     * Billing test for events with stepped price. Events occurs after free
     * period
     * 
     * @throws Exception
     */
    @Test
    public void SteppedPricesForEventsStep1_freePeriod5Days_WithEventInFreePeriod()
            throws Exception {
        int numEvents = 10;
        // 28 day are base value because activation date is 1. Feb
        // and billing month is March
        final int testYear = 2010;
        BigDecimal expectedCosts = new BigDecimal("2.52");
        BigDecimal[] stepCosts = { BD900 };
        BigDecimal[] septEntityCounts = { BD9 };

        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);
        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY) + 5;
        long eventOccurTime = getTimeInMillisForBilling(testYear,
                Calendar.MARCH, 1);
        // long eventOccurTime = subscriptionActivationTime + (freePeriod - 2)
        // * Numbers.DAY_MILLISECONDS;

        testBillingWithSteppedPricesForEvents(numEvents, expectedCosts,
                septEntityCounts, stepCosts, freePeriod, billingTime,
                subscriptionCreationTime, subscriptionActivationTime,
                eventOccurTime);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price. Events occurs after free
     * period
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForEventsStep1_freePeriod5Days()
            throws Exception {
        int numEvents = 10;
        // 28 day are base value because activation date is 1. Feb
        // and billing month is March
        int freePeriod = 28 + 5;
        BigDecimal expectedCosts = new BigDecimal("902.52");
        BigDecimal[] stepCosts = { BD900 };
        BigDecimal[] septEntityCounts = { BD9 };
        testBillingWithSteppedPricesForEvents(numEvents, expectedCosts,
                septEntityCounts, stepCosts, freePeriod);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForEventsStep1() throws Exception {
        int numEvents = 10;
        BigDecimal expectedCosts = new BigDecimal("903.00");
        BigDecimal[] stepCosts = { BD900 };
        BigDecimal[] septEntityCounts = { BD9 };
        testBillingWithSteppedPricesForEvents(numEvents, expectedCosts,
                septEntityCounts, stepCosts, 0);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForEventsStep2() throws Exception {
        int numEvents = 20;
        BigDecimal expectedCosts = new BigDecimal("2623.00");
        BigDecimal[] stepCosts = { BD900, new BigDecimal("1620.00") };
        BigDecimal[] septEntityCounts = { BD9, new BigDecimal(18) };
        // BigDecimal.ZERO };
        testBillingWithSteppedPricesForEvents(numEvents, expectedCosts,
                septEntityCounts, stepCosts, 0);
        xmlValidator.validateBillingResultXML();

    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForEventsStep3() throws Exception {
        int numEvents = 100;
        BigDecimal expectedCosts = new BigDecimal("9663.00");
        BigDecimal[] stepCosts = { BD900, BigDecimal.valueOf(1620),
                BigDecimal.valueOf(7760) };
        BigDecimal[] stepEntityCounts = { BD9, BigDecimal.valueOf(18),
                BigDecimal.valueOf(97) };
        testBillingWithSteppedPricesForEvents(numEvents, expectedCosts,
                stepEntityCounts, stepCosts, 0);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @param eventsNumber
     * @param expectedCosts
     * @param stepLimit
     * @param stepCosts
     * @param freePeriod
     * @throws Exception
     */
    private void testBillingWithSteppedPricesForEvents(int eventsNumber,
            BigDecimal expectedCosts, BigDecimal[] stepLimit,
            BigDecimal[] stepCosts, int freePeriod) throws Exception {
        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);
        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);
        long subscriptionActivationTime = subscriptionCreationTime;
        long eventOccurTime = billingTime - 1;

        testBillingWithSteppedPricesForEvents(eventsNumber, expectedCosts,
                stepLimit, stepCosts, freePeriod, billingTime,
                subscriptionCreationTime, subscriptionActivationTime,
                eventOccurTime);
    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @param eventsNumber
     * @param expectedCosts
     * @param stepLimit
     * @param stepCosts
     * @param freePeriod
     * @param eventOccurrenceTime
     * @throws Exception
     */
    private void testBillingWithSteppedPricesForEvents(int eventsNumber,
            BigDecimal expectedCosts, BigDecimal[] stepLimit,
            BigDecimal[] stepCosts, int freePeriod, final long billingTime,
            long subscriptionCreationTime, long subscriptionActivationTime,
            long eventOccurTime) throws Exception {

        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        long[] freeAmountArray = { 1, 2, 3 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1900) };
        int stepNum = 3;

        initDataEvents(billingTime, eventsNumber, subscriptionActivationTime,
                subscriptionCreationTime, stepNum, limitArray, priceArray,
                freeAmountArray, additionalPriceArray, freePeriod,
                eventOccurTime);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        // check overall costs net Amount
        Document doc = getBillingDocument();

        String netCosts = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@netAmount");
        checkEquals("Wrong net costs found", expectedCosts.toPlainString(),
                netCosts);

        // check single step costs
        NodeList nodeList = XMLConverter.getNodeListByXPath(doc,
                "//" + BillingResultXMLTags.STEPPED_PRICE_NODE_NAME);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node paramNode = nodeList.item(i);
            assertNotNull(paramNode);
            // check stepCost
            String amount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_AMOUNT_ATTRIBUTE_NAME)
                    .getTextContent();
            String entityCount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_ENTITY_COUNT_ATTRIBUTE_NAME)
                    .getTextContent();
            if (i < stepCosts.length) {
                checkEquals(stepCosts[i].toPlainString(), amount,
                        PriceConverter.NORMALIZED_PRICE_SCALING);
                checkEquals(stepLimit[i].toPlainString(), entityCount,
                        PriceConverter.NORMALIZED_PRICE_SCALING);
            }
        }

    }

    /**
     * Billing test for events with stepped price with free periods ends after
     * billing period
     * 
     * @throws Exception
     */
    @Test
    public void WithSteppedPricesForParametersStep1_FreePeriodAfter()
            throws Exception {
        int paramValue = 10;
        BigDecimal expectedOverAllCosts = new BigDecimal("913.00");
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("900.00");
        BigDecimal[] stepAmountArray = { BD900 };
        BigDecimal[] stepEventCounts = { BD9 };
        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepEventCounts, 60);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price with free periods ends before
     * billing period. Parameter are modified before billing period
     * 
     * @throws Exception
     */
    @Test
    public void WithSteppedPricesForParametersStep1_FreePeriodBefore()
            throws Exception {
        int paramValue = 10;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY);
        BigDecimal expectedOverAllCosts = new BigDecimal("913.00");
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("900.00");
        BigDecimal[] stepAmountArray = { BD900 };
        BigDecimal[] stepEventCounts = { BD9 };
        long eventOccurTime = subscriptionActivationTime;
        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepEventCounts, freePeriod, billingTime,
                subscriptionCreationTime, subscriptionActivationTime,
                eventOccurTime);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price with free periods ends before
     * billing period. Parameter are modified during billing period
     * 
     * @throws Exception
     */
    @Test
    public void WithSteppedPricesForParametersStep1_1_FreePeriodBefore_Bug8149()
            throws Exception {
        int paramValue = 10;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        long eventOccurTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY);

        BigDecimal expectedOverAllCosts = new BigDecimal("13.00");
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("0.00");
        BigDecimal[] stepAmountArray = { BigDecimal.ZERO };
        BigDecimal[] stepEventCounts = { BD9 };
        Long[] limitArray = { L10, L20, null };
        BigDecimal[] priceArray = { BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO };
        long[] freeAmountArray = { 1, 2, 3 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO };

        int stepNum = 3;

        initDataParameters(paramValue, subscriptionCreationTime,
                subscriptionActivationTime, stepNum, limitArray, priceArray,
                freeAmountArray, additionalPriceArray, PricingPeriod.MONTH,
                freePeriod, eventOccurTime);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        String SteppedPricesAmount = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/Parameters/Parameter/PeriodFee/SteppedPrices/@amount");
        checkEquals("Wrong net costs found",
                expectedSteppedPricesAmount.toPlainString(),
                SteppedPricesAmount);

        String netCosts = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@netAmount");
        checkEquals("Wrong net costs found",
                expectedOverAllCosts.toPlainString(), netCosts);

        // check step costs and step counts
        // compare step counts
        // check single step costs
        NodeList nodeList = XMLConverter.getNodeListByXPath(doc,
                "//" + BillingResultXMLTags.STEPPED_PRICE_NODE_NAME);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node paramNode = nodeList.item(i);
            assertNotNull(paramNode);

            String amount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_AMOUNT_ATTRIBUTE_NAME)
                    .getTextContent();

            String entityCount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_ENTITY_COUNT_ATTRIBUTE_NAME)
                    .getTextContent();

            if (i < stepAmountArray.length) {
                checkEquals(stepAmountArray[i].toPlainString(), amount,
                        PriceConverter.NORMALIZED_PRICE_SCALING);
                checkEquals(stepEventCounts[i].toPlainString(), entityCount);
            }
        }

        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price with free periods ends before
     * billing period. Parameter are modified during billing period
     * 
     * @throws Exception
     */
    @Test
    public void WithSteppedPricesForParametersStep1_1_FreePeriodBefore()
            throws Exception {
        int paramValue = 10;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        long eventOccurTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY);

        BigDecimal expectedOverAllCosts = new BigDecimal("913.00");
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("900.00");
        BigDecimal[] stepAmountArray = { BD900 };
        BigDecimal[] stepEventCounts = { BD9 };
        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepEventCounts, freePeriod, billingTime,
                subscriptionCreationTime, subscriptionActivationTime,
                eventOccurTime);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for parameters with stepped price with free periods ends
     * before billing period. Parameter are modified during billing period
     * 
     * @throws Exception
     */
    @Test
    public void WithSteppedPricesForParametersStep1_2_FreePeriodBefore()
            throws Exception {
        int paramValue = 123;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        long paramModTime = getTimeInMillisForBilling(testYear, Calendar.MARCH,
                10);
        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY);

        BigDecimal expectedOverAllCosts = new BigDecimal("8247.04");
        // value 123 -> 120 (3 are free, see stepCounts) * 80 + 1900 (additional
        // price) = 11500
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("11500.00");
        BigDecimal[] stepAmountArray = { BD900, BigDecimal.valueOf(1620),
                new BigDecimal("9600.00") };
        BigDecimal[] stepCounts = { BD9, BigDecimal.valueOf(18), BD120 };
        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepCounts, freePeriod, billingTime,
                subscriptionCreationTime, subscriptionActivationTime,
                paramModTime);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price. Events occurs after free
     * period
     * 
     * @throws Exception
     */
    @Test
    public void WithSteppedPricesForParametersStep1_freePeriod5Days_WithEventInFreePeriod()
            throws Exception {
        int paramValue = 10;
        // 28 day are base value because activation date is 1. Feb
        // and billing month is March
        final int testYear = 2010;
        BigDecimal expectedCosts = new BigDecimal("765.54");
        BigDecimal[] stepCosts = { BD900 };
        BigDecimal[] stepEntityCounts = { BD9 };

        final long billingTime = getTimeInMillisForBilling(testYear,
                Calendar.APRIL, 1);
        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                Calendar.FEBRUARY, 1);
        long subscriptionActivationTime = subscriptionCreationTime;

        int freePeriod = getDaysOfMonth(testYear, Calendar.FEBRUARY) + 5;
        long paramModificationTimeOccurTime = getTimeInMillisForBilling(
                testYear, Calendar.FEBRUARY, 1);

        testBillingWithSteppedPricesForParameters(paramValue, expectedCosts,
                stepCosts[0], stepCosts, stepEntityCounts, freePeriod,
                billingTime, subscriptionCreationTime,
                subscriptionActivationTime, paramModificationTimeOccurTime);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForParametersStep1()
            throws Exception {
        int paramValue = 10;
        BigDecimal expectedOverAllCosts = new BigDecimal("913.00");
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("900.00");
        BigDecimal[] stepAmountArray = { BD900 };
        BigDecimal[] stepEventCounts = { BD9 };
        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepEventCounts, 0);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForParametersStep2()
            throws Exception {
        int paramValue = 20;
        BigDecimal expectedOverAllCosts = new BigDecimal("2643.00");
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("2620.00");
        BigDecimal[] stepAmountArray = { BD900, new BigDecimal("1620.00") };
        BigDecimal[] stepEventCountArray = { BD9, new BigDecimal("18") };
        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepEventCountArray, 0);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Billing test for events with stepped price.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForParametersStep3()
            throws Exception {
        int paramValue = 30;
        BigDecimal expectedOverAllCosts = new BigDecimal("4093.00");
        BigDecimal expectedSteppedPricesAmount = new BigDecimal("4060.00");
        BigDecimal[] stepAmountArray = { BD900, BigDecimal.valueOf(1620),
                new BigDecimal("2160.00") };
        BigDecimal[] stepEventCountArray = { BD9, BigDecimal.valueOf(18),
                new BigDecimal("27") };
        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepEventCountArray, 0);
        xmlValidator.validateBillingResultXML();
    }

    private void testBillingWithSteppedPricesForParameters(int paramValue,
            BigDecimal expectedOverAllCosts,
            BigDecimal expectedSteppedPricesAmount,
            final BigDecimal[] stepAmountArray,
            final BigDecimal[] stepEventCountArray, long paramModificationTime)
            throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);
        long subscriptionActivationTime = subscriptionCreationTime;

        testBillingWithSteppedPricesForParameters(paramValue,
                expectedOverAllCosts, expectedSteppedPricesAmount,
                stepAmountArray, stepEventCountArray, 0, billingTime,
                subscriptionCreationTime, subscriptionActivationTime,
                paramModificationTime);

    }

    /**
     * Billing test for price model with stepped price.
     * 
     * @throws Exception
     */
    private void testBillingWithSteppedPricesForParameters(int paramValue,
            BigDecimal expectedOverAllCosts,
            BigDecimal expectedSteppedPricesAmount,
            final BigDecimal[] stepAmountArray,
            final BigDecimal[] stepEventCountArray, int freePeriod,
            final long billingTime, long subscriptionCreationTime,
            long subscriptionActivationTime, long paramModificationTime)
            throws Exception {

        Long[] limitArray = { L10, L20, null };
        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        long[] freeAmountArray = { 1, 2, 3 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1900) };

        int stepNum = 3;

        initDataParameters(paramValue, subscriptionCreationTime,
                subscriptionActivationTime, stepNum, limitArray, priceArray,
                freeAmountArray, additionalPriceArray, PricingPeriod.MONTH,
                freePeriod, paramModificationTime);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        String steppedPricesAmount = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/Parameters/Parameter/PeriodFee/SteppedPrices/@amount");
        checkEquals("Wrong net costs found",
                expectedSteppedPricesAmount.toPlainString(),
                steppedPricesAmount);

        String netCosts = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@netAmount");
        checkEquals("Wrong net costs found",
                expectedOverAllCosts.toPlainString(), netCosts);

        // check step costs and step counts
        // compare step counts
        // check single step costs
        NodeList nodeList = XMLConverter.getNodeListByXPath(doc,
                "//" + BillingResultXMLTags.STEPPED_PRICE_NODE_NAME);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node paramNode = nodeList.item(i);
            assertNotNull(paramNode);

            String amount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_AMOUNT_ATTRIBUTE_NAME)
                    .getTextContent();

            String entityCount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_ENTITY_COUNT_ATTRIBUTE_NAME)
                    .getTextContent();

            if (i < stepAmountArray.length) {
                checkEquals(stepAmountArray[i].toPlainString(), amount,
                        PriceConverter.NORMALIZED_PRICE_SCALING);
                checkEquals(stepEventCountArray[i].toPlainString(),
                        entityCount);
            }
        }
    }

    /**
     * Test for price model, events, priced parameter with stepped prices.
     * 
     * @throws Exception
     */
    @Test
    public void testBillingWithSteppedPricesForAll() throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);
        long subscriptionActivationTime = subscriptionCreationTime;

        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        long[] freeAmountArray = { 0, 10, 20 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1900) };

        initData(billingTime, subscriptionCreationTime,
                subscriptionActivationTime, limitArray, priceArray,
                freeAmountArray, additionalPriceArray);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        String netCosts = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@netAmount");
        checkEquals("BugWrong net costs found", "230064.00", netCosts);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Test for billing xml structure. PricedParameter and PriceModel tags
     * position.
     * 
     * @throws Exception
     */
    @Test
    public void testPriceModelParameterTagsStructure() throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final int testYear = 2010;
        final long billingTime = getTimeInMillisForBilling(testYear, testMonth,
                testDay);

        long subscriptionCreationTime = getTimeInMillisForBilling(testYear,
                testMonth - 2, testDay);

        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        long[] freeAmountArray = { 0, 0, 0 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                BigDecimal.valueOf(1900) };

        initData(billingTime, subscriptionCreationTime,
                subscriptionCreationTime, limitArray, priceArray,
                freeAmountArray, additionalPriceArray);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        String parameterId = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/Parameters/Parameter/@id");
        Assert.assertEquals("Wrong structure of billin.xml", "integerParam",
                parameterId);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Creates price model, events and priced parameter with stepped prices.
     * 
     * @param billingTime
     *            Time of billing start.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param subscriptionActivationTime
     *            Subscription activation time.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps. * @throws Exception
     */
    private void initData(final long billingTime,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime, final Long[] limitArray,
            final BigDecimal[] priceArray, final long[] freeAmountArray,
            final BigDecimal[] additionalPriceArray) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                ParameterDefinition paramDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.INTEGER,
                                "integerParam", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);

                Parameter param = Products.createParameter(paramDef, product,
                        mgr);
                param.setValue("11");

                initSubAndProd(subscriptionCreationTime,
                        subscriptionActivationTime);

                Date creationDate = new Date(subscriptionCreationTime);

                createUserAndLicense("user_1", subscriptionCreationTime);

                PriceModel pm = subscription.getPriceModel();

                pm.setPricePerUserAssignment(new BigDecimal(2L));
                pm.setHistoryModificationTime(
                        Long.valueOf(creationDate.getTime()));

                mgr.flush();

                Event event = TechnicalProducts.addEvent("event1",
                        EventType.PLATFORM_EVENT, technicalProduct, mgr);
                PricedEvent pricedEvent = new PricedEvent();
                pricedEvent.setEvent(event);
                pricedEvent.setPriceModel(pm);
                pricedEvent.setEventPrice(new BigDecimal(100));
                pricedEvent.setHistoryModificationTime(
                        Long.valueOf(creationDate.getTime()));

                mgr.persist(pricedEvent);
                mgr.flush();

                GatheredEvent gatheredEvent = new GatheredEvent();
                gatheredEvent.setActor("Anonymous");
                gatheredEvent.setEventId("event1");
                gatheredEvent.setOccurrenceTime(billingTime - 1);
                gatheredEvent.setSubscriptionTKey(subscription.getKey());
                gatheredEvent.setType(EventType.PLATFORM_EVENT);
                gatheredEvent.setMultiplier(100);

                mgr.persist(gatheredEvent);
                mgr.flush();

                int stepNum = 3;

                createSteppedPrices(stepNum, creationDate, limitArray,
                        priceArray, freeAmountArray, additionalPriceArray, null,
                        pricedEvent, null);

                createSteppedPrices(stepNum, creationDate, limitArray,
                        priceArray, freeAmountArray, additionalPriceArray, pm,
                        null, null);

                PricedParameter pricedParam = new PricedParameter();
                pricedParam.setParameter(param);
                pricedParam.setPriceModel(pm);
                pricedParam.setPricePerSubscription(new BigDecimal(555));
                pricedParam.setPricePerUser(new BigDecimal(543));
                pricedParam.setHistoryModificationTime(
                        Long.valueOf(creationDate.getTime()));

                pm.setSelectedParameters(new ArrayList<PricedParameter>());
                pm.getSelectedParameters().add(pricedParam);
                mgr.persist(pricedParam);

                createSteppedPrices(stepNum, creationDate, limitArray,
                        priceArray, freeAmountArray, additionalPriceArray, null,
                        null, pricedParam);

                updateParameterHistoryEntries(
                        new Date(subscriptionActivationTime));

                return null;
            }
        });
    }

    /**
     * Returns the time matching the specified parameters for year, month and
     * day.
     * 
     * @param paramTestYear
     *            The year.
     * @param paramTestMonth
     *            The month.
     * @param paramTestDay
     *            The day.
     * @return The time matching the specified parameters in milliseconds.
     */
    private long getTimeInMillisForBilling(final int paramTestYear,
            final int paramTestMonth, final int paramTestDay) {
        final Calendar billingCalendar = Calendar.getInstance();
        billingCalendar.set(Calendar.YEAR, paramTestYear);
        billingCalendar.set(Calendar.MONTH, paramTestMonth);
        billingCalendar.set(Calendar.DAY_OF_MONTH, paramTestDay);
        billingCalendar.set(Calendar.HOUR_OF_DAY, 0);
        billingCalendar.set(Calendar.MINUTE, 0);
        billingCalendar.set(Calendar.SECOND, 0);
        billingCalendar.set(Calendar.MILLISECOND, 0);

        return billingCalendar.getTimeInMillis();
    }

    /**
     * Returns the number of days matching the specified month.
     * 
     * @param paramTestYear
     *            The year.
     * @param paramTestMonth
     *            The month.
     * @return The time matching the specified parameters in milliseconds.
     */
    private int getDaysOfMonth(final int paramTestYear,
            final int paramTestMonth) {
        final Calendar billingCalendar = Calendar.getInstance();
        billingCalendar.set(Calendar.YEAR, paramTestYear);
        billingCalendar.set(Calendar.MONTH, paramTestMonth);
        // it seems that due to the time change, not setting a day leads to a -1
        // in the hours, what causes the calendar to jump back one month. For
        // february, this leads to 31 days instead of 28 and breaks the tests.
        // So set the day to 2, what avoids this problem.
        billingCalendar.set(Calendar.DAY_OF_MONTH, 2);

        return billingCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Helper method for getting xml billing result.
     * 
     * @return Billing xml document.
     * 
     * @throws Exception
     *             On error.
     */
    private Document getBillingDocument() throws Exception {
        return runTX(new Callable<Document>() {
            @Override
            public Document call() throws Exception {
                Query query = mgr.createQuery(
                        "SELECT br FROM BillingResult br WHERE br.dataContainer.organizationTKey = :organizationTKey ORDER BY br.dataContainer.periodEndTime DESC");
                query.setParameter("organizationTKey",
                        Long.valueOf(customer.getKey()));
                query.setMaxResults(1);
                BillingResult billingResult = (BillingResult) query
                        .getSingleResult();

                System.out.println(billingResult.getResultXML());

                Document doc = XMLConverter
                        .convertToDocument(billingResult.getResultXML(), true);

                return doc;
            }
        });
    }

    private void initDataPriceModel(final int userNumber,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime, final int stepNum,
            final Long[] limitArray, final BigDecimal[] priceArray,
            final long[] freeAmountArray,
            final BigDecimal[] additionalPriceArray,
            final boolean differentAssignmentTime, final int freePeriod,
            final BigDecimal priceperUserAssigment, final BigDecimal oneTimeFee)
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                initSubAndProd(subscriptionCreationTime,
                        subscriptionActivationTime);

                Date creationDate = new Date(subscriptionCreationTime);

                for (int i = 0; i < userNumber; i++) {
                    String userName = "userName" + String.valueOf(i);
                    if (differentAssignmentTime) {
                        createUserAndLicense(userName, subscriptionCreationTime
                                + i * (7 * 24 * 60 * 60 * 1000L));
                    } else {
                        createUserAndLicense(userName,
                                subscriptionCreationTime);

                    }
                }

                PriceModel pm = subscription.getPriceModel();
                pm.setPeriod(PricingPeriod.MONTH);
                pm.setPricePerUserAssignment(priceperUserAssigment);
                pm.setOneTimeFee(oneTimeFee);
                pm.setFreePeriod(freePeriod);
                pm.setHistoryModificationTime(
                        Long.valueOf(creationDate.getTime()));
                mgr.flush();

                createSteppedPrices(stepNum, creationDate, limitArray,
                        priceArray, freeAmountArray, additionalPriceArray, pm,
                        null, null);

                return null;
            }

        });
    }

    /**
     * Creates stepped prices for price model.
     * 
     * @param userNumber
     *            Number of users.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param subscriptionActivationTime
     *            Subscription activation time.
     * @param stepNum
     *            Number of steps.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps. * @throws Exception
     */
    private void initDataPriceModel(final int userNumber,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime, final int stepNum,
            final Long[] limitArray, final BigDecimal[] priceArray,
            final boolean differentAssignmentTime, final int freePeriod,
            final BigDecimal priceperUserAssigment, final BigDecimal oneTimeFee)
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                initSubAndProd(subscriptionCreationTime,
                        subscriptionActivationTime);

                Date creationDate = new Date(subscriptionCreationTime);

                for (int i = 0; i < userNumber; i++) {
                    String userName = "userName" + String.valueOf(i);
                    if (differentAssignmentTime) {
                        createUserAndLicense(userName, subscriptionCreationTime
                                + i * (7 * 24 * 60 * 60 * 1000L));
                    } else {
                        createUserAndLicense(userName,
                                subscriptionCreationTime);

                    }
                }

                PriceModel pm = subscription.getPriceModel();
                pm.setPeriod(PricingPeriod.MONTH);
                pm.setPricePerUserAssignment(priceperUserAssigment);
                pm.setOneTimeFee(oneTimeFee);
                pm.setFreePeriod(freePeriod);
                pm.setHistoryModificationTime(
                        Long.valueOf(creationDate.getTime()));
                mgr.flush();

                createSteppedPrices(stepNum, creationDate, limitArray,
                        priceArray, pm, null, null);

                return null;
            }

        });
    }

    /**
     * Initializes a subscription and the corresponding product with the
     * specified creation time.
     * 
     * @param subscriptionCreationTime
     *            The time the subscription and product were created at.
     * @param subscriptionActivationTime
     *            The time the subscription was activated at.
     * @throws NonUniqueBusinessKeyException
     */
    private void initSubAndProd(final long subscriptionCreationTime,
            final long subscriptionActivationTime)
            throws NonUniqueBusinessKeyException {
        subscription = Subscriptions.createSubscription(mgr,
                customer.getOrganizationId(), product.getProductId(),
                "subscriptionId", subscriptionCreationTime,
                subscriptionActivationTime, product.getVendor(), 1);

        subscription.getProduct().setHistoryModificationTime(
                Long.valueOf(subscriptionCreationTime));
    }

    /**
     * Creates events with stepped prices.
     * 
     * @param billingTime
     *            Time of billing start.
     * @param numEvents
     *            Number of events.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param subscriptionActivationTime
     *            Subscription activation time.
     * @param stepNum
     *            Number of steps.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps.
     * @throws Exception
     */
    private void initDataEvents(final long billingTime, final int numEvents,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime, final int stepNum,
            final Long[] limitArray, final BigDecimal[] priceArray,
            final long[] freeAmountArray,
            final BigDecimal[] additionalPriceArray, final int freePeriod,
            final long occurTime) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                initSubAndProd(subscriptionCreationTime,
                        subscriptionActivationTime);

                Date creationDate = new Date(subscriptionCreationTime);

                String userName = "userName";
                createUserAndLicense(userName, subscriptionCreationTime);

                PriceModel pm = subscription.getPriceModel();
                pm.setFreePeriod(freePeriod);
                pm.setPeriod(PricingPeriod.MONTH);
                pm.setPricePerUserAssignment(new BigDecimal(2l));
                pm.setHistoryModificationTime(
                        Long.valueOf(creationDate.getTime()));
                mgr.flush();

                Event event = TechnicalProducts.addEvent("event1",
                        EventType.PLATFORM_EVENT, technicalProduct, mgr);
                PricedEvent pricedEvent = new PricedEvent();
                pricedEvent.setEvent(event);
                pricedEvent.setPriceModel(pm);
                pricedEvent.setEventPrice(new BigDecimal(100));
                pricedEvent.setHistoryModificationTime(
                        Long.valueOf(creationDate.getTime()));

                mgr.persist(pricedEvent);
                mgr.flush();

                GatheredEvent gatheredEvent = new GatheredEvent();
                gatheredEvent.setActor("Anonymous");
                gatheredEvent.setEventId("event1");
                gatheredEvent.setOccurrenceTime(occurTime);
                gatheredEvent.setSubscriptionTKey(subscription.getKey());
                gatheredEvent.setType(EventType.PLATFORM_EVENT);
                gatheredEvent.setMultiplier(numEvents);

                mgr.persist(gatheredEvent);
                mgr.flush();

                createSteppedPrices(stepNum, creationDate, limitArray,
                        priceArray, freeAmountArray, additionalPriceArray, null,
                        pricedEvent, null);

                return null;
            }
        });
    }

    /**
     * Creates parameter with stepped prices.
     * 
     * @param paramValue
     *            Parameter value. Used for finding needed step.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * @param subscriptionActivationTime
     *            Subscription activation time.
     * @param stepNum
     *            Number of steps.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps.
     * @param pricingPeriod
     *            The period defined for the current price model.
     * @throws Exception
     *             On error.
     */
    private void initDataParameters(final int paramValue,
            final long subscriptionCreationTime,
            final long subscriptionActivationTime, final int stepNum,
            final Long[] limitArray, final BigDecimal[] priceArray,
            final long[] freeAmountArray,
            final BigDecimal[] additionalPriceArray,
            final PricingPeriod pricingPeriod, final int freePeriod,
            final long paramModificationTime) throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                ParameterDefinition paramDef = TechnicalProducts
                        .addParameterDefinition(ParameterValueType.INTEGER,
                                "integerParam", ParameterType.SERVICE_PARAMETER,
                                technicalProduct, mgr, null, null, true);
                Date activationDate = null;
                if (paramModificationTime != 0) {
                    activationDate = new Date(paramModificationTime);
                } else {
                    activationDate = new Date(subscriptionActivationTime);

                }

                Parameter param = Products.createParameter(paramDef, product,
                        mgr, String.valueOf(paramValue));

                initSubAndProd(subscriptionCreationTime,
                        subscriptionActivationTime);

                Date modificationDate = null;
                if (paramModificationTime != 0) {
                    modificationDate = new Date(paramModificationTime);
                } else {
                    modificationDate = new Date(subscriptionCreationTime);

                }

                createUserAndLicense("userName", subscriptionCreationTime);

                PriceModel pm = subscription.getPriceModel();
                pm.setPeriod(pricingPeriod);
                pm.setFreePeriod(freePeriod);
                pm.setPricePerUserAssignment(new BigDecimal(2L));
                pm.setHistoryModificationTime(
                        Long.valueOf(modificationDate.getTime()));
                mgr.flush();

                PricedParameter pricedParam = new PricedParameter();
                pricedParam.setParameter(param);
                pricedParam.setPriceModel(pm);
                pricedParam.setPricePerSubscription(new BigDecimal(555));
                pricedParam.setPricePerUser(new BigDecimal(1));
                pricedParam.setHistoryModificationTime(
                        Long.valueOf(modificationDate.getTime()));

                pm.setSelectedParameters(new ArrayList<PricedParameter>());
                pm.getSelectedParameters().add(pricedParam);
                mgr.persist(pricedParam);

                createSteppedPrices(stepNum, modificationDate, limitArray,
                        priceArray, freeAmountArray, additionalPriceArray, null,
                        null, pricedParam);

                updateParameterHistoryEntries(activationDate);

                return null;
            }
        });
    }

    private void updateParameterHistoryEntries(final Date modDate) {
        Query query = mgr.createQuery(
                "UPDATE ParameterDefinitionHistory pdh SET pdh.modDate = :modDate");
        query.setParameter("modDate", modDate);
        query.executeUpdate();
        query = mgr.createQuery(
                "UPDATE ParameterHistory ph SET ph.modDate = :modDate");
        query.setParameter("modDate", modDate);
        query.executeUpdate();
    }

    /**
     * Create user and license for him.
     * 
     * @param userName
     *            User name.
     * @param subscriptionCreationTime
     *            Subscription creation time.
     * 
     * @throws Exception
     *             On error.
     */
    private void createUserAndLicense(String userName,
            final long subscriptionCreationTime) throws Exception {
        PlatformUser user = new PlatformUser();
        user.setUserId(userName);
        user.setOrganization(supplierAndProvider);
        user.setEmail("user_1@user_1.com");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setLocale("en");
        mgr.persist(user);
        user = mgr.find(user);

        UsageLicense license = new UsageLicense();
        license.setAssignmentDate(subscriptionCreationTime);
        license.setSubscription(subscription);
        license.setUser(user);
        license.setHistoryModificationTime(
                Long.valueOf(subscriptionCreationTime));

        mgr.flush();
        mgr.persist(license);
    }

    /**
     * Create stepped prices.
     * 
     * @param stepNum
     *            Number of steps.
     * @param modificationDate
     *            Date of creation.
     * @param limitArray
     *            Input data for step values.
     * @param priceArray
     *            Input data for step prices.
     * @param freeAmountArray
     *            Input data for free amount. Limit will be reduced for this
     *            value.
     * @param additionalPriceArray
     *            Input data for additional price for steps.
     * @param pm
     *            Price model for which stepped price are created. If not null,
     *            other reference object have to be null.
     * @param pricedEvent
     *            Event for which stepped price are created. If not null, other
     *            reference object have to be null.
     * @param pricedParameter
     *            Parameter for which stepped price are created. If not null,
     *            other reference object have to be null.
     * @throws NonUniqueBusinessKeyException
     *             On error.
     */
    private void createSteppedPrices(int stepNum, Date modificationDate,
            Long[] limitArray, BigDecimal[] priceArray, PriceModel pm,
            PricedEvent pricedEvent, PricedParameter pricedParameter)
            throws NonUniqueBusinessKeyException {
        List<SteppedPrice> steppList = new ArrayList<>();
        for (int i = 0; i < stepNum; i++) {
            SteppedPrice steppedPrice = new SteppedPrice();
            steppedPrice.setPriceModel(pm);
            steppedPrice.setPricedEvent(pricedEvent);
            steppedPrice.setPricedParameter(pricedParameter);
            steppedPrice.setLimit(limitArray[i]);
            steppedPrice.setPrice(priceArray[i]);
            steppedPrice.setHistoryModificationTime(
                    Long.valueOf(modificationDate.getTime()));
            steppList.add(steppedPrice);
        }
        updateFreeAmountAndAdditionalPrice(steppList);
        Iterator<SteppedPrice> iterator = steppList.iterator();
        while (iterator.hasNext()) {
            SteppedPrice step = iterator.next();
            mgr.persist(step);
            mgr.flush();
        }

    }

    private void createSteppedPrices(int stepNum, Date modificationDate,
            Long[] limitArray, BigDecimal[] priceArray, long[] freeAmountArray,
            BigDecimal[] additionalPriceArray, PriceModel pm,
            PricedEvent pricedEvent, PricedParameter pricedParameter)
            throws NonUniqueBusinessKeyException {

        for (int i = 0; i < stepNum; i++) {
            SteppedPrice steppedPrice = new SteppedPrice();

            steppedPrice.setPriceModel(pm);
            steppedPrice.setPricedEvent(pricedEvent);
            steppedPrice.setPricedParameter(pricedParameter);
            steppedPrice.setLimit(limitArray[i]);
            steppedPrice.setPrice(priceArray[i]);
            steppedPrice.setFreeEntityCount(freeAmountArray[i]);
            steppedPrice.setAdditionalPrice(additionalPriceArray[i]);
            steppedPrice.setHistoryModificationTime(
                    Long.valueOf(modificationDate.getTime()));

            mgr.persist(steppedPrice);
            mgr.flush();

        }

    }

    static void updateFreeAmountAndAdditionalPrice(List<SteppedPrice> list) {
        Collections.sort(list, new SteppedPriceComparator());
        int size = list.size();
        for (int i = 1; i < size; i++) {
            SteppedPrice prevStep = list.get(i - 1);
            if (prevStep.getLimit() == null) {
                list.get(i).setFreeEntityCount(0);
                list.get(i).setAdditionalPrice(BigDecimal.ZERO
                        .setScale(PriceConverter.NORMALIZED_PRICE_SCALING));
            } else {
                list.get(i).setFreeEntityCount(prevStep.getLimit().longValue());
                list.get(i)
                        .setAdditionalPrice((BigDecimal
                                .valueOf(prevStep.getLimit().longValue())
                                .subtract(BigDecimal.valueOf(
                                        prevStep.getFreeEntityCount())))
                                                .multiply(prevStep.getPrice())
                                                .add(prevStep
                                                        .getAdditionalPrice())
                                                .setScale(
                                                        PriceConverter.NORMALIZED_PRICE_SCALING,
                                                        RoundingMode.HALF_UP));
            }
        }
        if (size > 0) {
            list.get(0).setFreeEntityCount(0);
            list.get(0).setAdditionalPrice(BigDecimal.ZERO
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING));
            list.get(size - 1).setLimit(null);
        }
    }

    private void ValidateBillingUserAssingmentCostsNode(
            BigDecimal expectedPrice, final long billingTime,
            final BigDecimal... stepAmountArray)
            throws Exception, XPathExpressionException {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                billingService.startBillingRun(billingTime);
                return null;
            }
        });
        Document doc = getBillingDocument();

        System.out.println(XMLConverter.convertToString(doc, false));

        String netCosts = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@netAmount");
        checkEquals("Wrong net costs found", expectedPrice.toPlainString(),
                netCosts, 2);

        // compare step counts

        Node node = XMLConverter.getNodeByXPath(doc,
                "//" + BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);

        BigDecimal factor = new BigDecimal(node.getAttributes()
                .getNamedItem(BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME)
                .getTextContent());
        // check single step costs
        NodeList nodeList = XMLConverter.getNodeListByXPath(doc,
                "//" + BillingResultXMLTags.STEPPED_PRICE_NODE_NAME);

        BigDecimal stepLimit;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node paramNode = nodeList.item(i);
            assertNotNull(paramNode);

            String amount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_AMOUNT_ATTRIBUTE_NAME)
                    .getTextContent();

            String entityCount = paramNode.getAttributes()
                    .getNamedItem(
                            BillingResultXMLTags.STEP_ENTITY_COUNT_ATTRIBUTE_NAME)
                    .getTextContent();
            BigDecimal currentStepCount = new BigDecimal(entityCount);
            if (factor.compareTo(currentStepCount) == 1) {
                stepLimit = currentStepCount;
                factor = factor.subtract(currentStepCount);
            } else {
                stepLimit = factor;
            }
            if (i < stepAmountArray.length) {
                checkEquals(stepAmountArray[i].toPlainString(), amount,
                        PriceConverter.NORMALIZED_PRICE_SCALING);
                checkEquals(stepLimit.toPlainString(), entityCount,
                        PriceConverter.NORMALIZED_PRICE_SCALING);
            }
        }
    }

}
