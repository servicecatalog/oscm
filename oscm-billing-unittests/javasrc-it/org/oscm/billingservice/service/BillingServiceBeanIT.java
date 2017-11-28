/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: 12.02.2009                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.oscm.test.Numbers.BD10;
import static org.oscm.test.Numbers.BD100;
import static org.oscm.test.Numbers.BD1000;
import static org.oscm.test.Numbers.BD1100;
import static org.oscm.test.Numbers.BD1200;
import static org.oscm.test.Numbers.BD1300;
import static org.oscm.test.Numbers.BD1400;
import static org.oscm.test.Numbers.BD1500;
import static org.oscm.test.Numbers.BD1600;
import static org.oscm.test.Numbers.BD1700;
import static org.oscm.test.Numbers.BD2;
import static org.oscm.test.Numbers.BD20;
import static org.oscm.test.Numbers.BD200;
import static org.oscm.test.Numbers.BD3;
import static org.oscm.test.Numbers.BD30;
import static org.oscm.test.Numbers.BD300;
import static org.oscm.test.Numbers.BD4;
import static org.oscm.test.Numbers.BD400;
import static org.oscm.test.Numbers.BD50;
import static org.oscm.test.Numbers.BD500;
import static org.oscm.test.Numbers.BD600;
import static org.oscm.test.Numbers.BD700;
import static org.oscm.test.Numbers.BD8;
import static org.oscm.test.Numbers.BIGDECIMAL_SCALE;
import static org.oscm.test.Numbers.Long.MAX_VALUE;
import static org.oscm.test.Numbers.Long.MIN_VALUE;
import static org.oscm.test.Numbers.TIMESTAMP;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.bean.MarketingPermissionServiceBean;
import org.oscm.app.control.ApplicationServiceBaseStub;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorLocal;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.BigDecimalComparator;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.Discount;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.intf.BillingService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.paymentservice.bean.PaymentServiceStub;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.XMLTestValidator;
import org.oscm.test.data.BillingAdapters;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.Scenario;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.SupportedCurrencies;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.setup.ProductImportParser;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.LdapAccessServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.SessionServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.types.enumtypes.PlatformEventIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings({ "boxing", "deprecation" })
public class BillingServiceBeanIT extends EJBTestBase {

    protected int baseDataCreationYear = 2008;
    protected int testYear = 2010;

    protected DataService mgr;

    protected BillingServiceLocal serviceBill;
    protected BillingService serviceBillExt;
    protected ServiceProvisioningService serviceProv;
    SharesCalculatorLocal sharesCalculator;

    // Values for product 1
    protected final static String P_1_ID = "EXAMPLE Starter";
    protected final static BigDecimal P_1_PRICE_PER_PERIOD = BD1000;
    protected final static BigDecimal P_1_PRICE_PER_USER = BD100;
    protected final static BigDecimal P_1_PRICE_LOGIN = BD10;
    protected final static BigDecimal P_1_PRICE_LOGOUT = BigDecimal.ZERO;
    protected final static BigDecimal P_1_PRICE_UPLOAD = BD20;
    protected final static BigDecimal P_1_ONE_TIME_FEE = new BigDecimal(111)
            .setScale(BIGDECIMAL_SCALE);

    // Values for product 2
    protected final static String P_2_ID = "EXAMPLE Professional";
    protected final static BigDecimal P_2_PRICE_PER_PERIOD = new BigDecimal(
            20000).setScale(BIGDECIMAL_SCALE);
    protected final static BigDecimal P_2_PRICE_PER_USER = BigDecimal.ZERO;
    protected final static BigDecimal P_2_PRICE_LOGIN = BigDecimal.ZERO;
    protected final static BigDecimal P_2_PRICE_LOGOUT = BigDecimal.ZERO;
    protected final static BigDecimal P_2_PRICE_UPLOAD = BigDecimal.ONE
            .setScale(BIGDECIMAL_SCALE);
    protected final static BigDecimal P_2_ONE_TIME_FEE = new BigDecimal(222)
            .setScale(BIGDECIMAL_SCALE);

    // Values for product 3
    protected final static String P_3_ID = "EXAMPLE Week";
    protected final static BigDecimal P_3_PRICE_PER_PERIOD = BD500;
    protected final static BigDecimal P_3_PRICE_PER_USER = BD4;
    protected final static BigDecimal P_3_PRICE_LOGIN = BD8;
    protected final static BigDecimal P_3_PRICE_LOGOUT = BD2;
    protected final static BigDecimal P_3_PRICE_UPLOAD = new BigDecimal(12)
            .setScale(BIGDECIMAL_SCALE);
    protected final static BigDecimal P_3_ONE_TIME_FEE = new BigDecimal(333)
            .setScale(BIGDECIMAL_SCALE);

    // Values for product 4
    protected final static String P_4_ID = "EXAMPLE 4";
    protected final static BigDecimal P_4_PRICE_PER_PERIOD = BD500;
    protected final static BigDecimal P_4_PRICE_PER_USER = BD4;
    protected final static BigDecimal P_4_PRICE_LOGIN = BD8;
    protected final static BigDecimal P_4_PRICE_LOGOUT = BD2;
    protected final static BigDecimal P_4_PRICE_UPLOAD = new BigDecimal(12)
            .setScale(BIGDECIMAL_SCALE);
    protected final static BigDecimal P_4_ONE_TIME_FEE = new BigDecimal(444)
            .setScale(BIGDECIMAL_SCALE);

    // Values for product 5
    protected final static String P_5_ID = "EXAMPLE Hour";
    protected final static BigDecimal P_5_PRICE_PER_PERIOD = BD50;
    protected final static BigDecimal P_5_PRICE_PER_USER = new BigDecimal(22)
            .setScale(BIGDECIMAL_SCALE);
    protected final static BigDecimal P_5_PRICE_LOGIN = new BigDecimal(9)
            .setScale(BIGDECIMAL_SCALE);
    protected final static BigDecimal P_5_PRICE_LOGOUT = BD3;
    protected final static BigDecimal P_5_PRICE_UPLOAD = new BigDecimal(14)
            .setScale(BIGDECIMAL_SCALE);
    protected final static BigDecimal P_5_ONE_TIME_FEE = new BigDecimal(555)
            .setScale(BIGDECIMAL_SCALE);

    // user IDs
    protected final static String U_1_ID = "anton";
    protected final static String U_2_ID = "bernd";
    protected final static String U_3_ID = "claudia";
    protected final static String U_4_ID = "daniel";
    protected final static String U_5_ID = "emil";
    protected final static String U_RESELLER_ID = "reseller_user";

    // shortcuts
    protected final static PricingPeriod M = PricingPeriod.MONTH;
    protected final static PricingPeriod W = PricingPeriod.WEEK;
    protected final static PricingPeriod D = PricingPeriod.DAY;

    protected final static String SUBSCRIPTION_ID = "sub";

    protected String supplierId;
    protected long supplierTkey;
    protected String resellerId;
    protected long resellerTkey;
    protected String customerId;
    protected String customerName;
    protected long customerTkey;
    protected String customerForResellerId;
    protected String customerForResellerName;
    protected long customerForResellerTkey;

    protected long subscriptionKey;
    protected String platformUserKey;
    protected String platformResellerUserKey;

    protected XMLTestValidator xmlValidator;

    private List<Organization> ntfxReceivingOrgs;

    private static final BigDecimal GROSS_REVENUE = BigDecimal.valueOf(743342);
    private static final BigDecimal NET_REVENUE = BigDecimal.valueOf(43847);

    @Override
    public void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(mock(TenantProvisioningServiceBean.class));
        container.addBean(new ConfigurationServiceStub());
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
        when(sharesCalculator.performBrokerSharesCalculationRun(anyLong(),
                anyLong())).thenReturn(Boolean.TRUE);
        when(sharesCalculator.performMarketplacesSharesCalculationRun(anyLong(),
                anyLong())).thenReturn(Boolean.TRUE);
        when(sharesCalculator.performResellerSharesCalculationRun(anyLong(),
                anyLong())).thenReturn(Boolean.TRUE);
        when(sharesCalculator.performSupplierSharesCalculationRun(anyLong(),
                anyLong())).thenReturn(Boolean.TRUE);
        container.addBean(sharesCalculator);
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messages, PlatformUser currentUser) {
                for (TriggerMessage triggerMessage : messages) {
                    ntfxReceivingOrgs = new ArrayList<>(
                            triggerMessage.getReceiverOrgs());
                }
            }
        });
        container.addBean(new TagServiceBean());
        container.addBean(new MarketingPermissionServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new RevenueCalculatorBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new BillingServiceBean());

        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        mgr = container.get(DataService.class);
        serviceBill = container.get(BillingServiceLocal.class);
        serviceBillExt = container.get(BillingService.class);
        serviceProv = container.get(ServiceProvisioningService.class);
        xmlValidator = new XMLTestValidator();
        xmlValidator.setup();

        setBaseDataCreationDate();

        setUpDirServerStub(cfg);
        BillingAdapters.createBillingAdapter(mgr,
                BillingAdapterIdentifier.NATIVE_BILLING.toString(), true);
        createPaymentTypes(mgr);
        createOrganizationRoles(mgr);
        SupportedCountries.createSomeSupportedCountries(mgr);
        Organization supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        mgr.persist(supplier);

        OrganizationReference orgRef = new OrganizationReference(supplier,
                supplier,
                OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
        mgr.persist(orgRef);
        supplier.getTargets().add(orgRef);
        supplier.getSources().add(orgRef);

        Organization customer = Organizations.createCustomer(mgr, supplier);
        customerId = customer.getOrganizationId();
        customerName = customer.getName();
        customerTkey = customer.getKey();

        supplierId = supplier.getOrganizationId();
        supplierTkey = supplier.getKey();

        Organization reseller = Organizations.createOrganization(mgr,
                OrganizationRoleType.RESELLER);
        mgr.persist(reseller);
        resellerId = reseller.getOrganizationId();
        resellerTkey = reseller.getKey();

        Organization customerForReseller = Organizations.createCustomer(mgr,
                reseller);
        customerForResellerId = customerForReseller.getOrganizationId();
        customerForResellerName = customerForReseller.getName();
        customerForResellerTkey = customerForReseller.getKey();

        PlatformUser user = createUserObject(supplier, U_1_ID);
        mgr.persist(user);
        mgr.persist(createUserObject(supplier, U_2_ID));
        mgr.persist(createUserObject(supplier, U_3_ID));
        mgr.persist(createUserObject(supplier, U_4_ID));
        mgr.persist(createUserObject(supplier, U_5_ID));

        platformUserKey = String.valueOf(user.getKey());

        PlatformUser resellerUser = createUserObject(reseller, U_RESELLER_ID);
        mgr.persist(resellerUser);
        platformResellerUserKey = String.valueOf(resellerUser.getKey());

        createSupportedCurrencies(mgr);
        container.login(platformUserKey, ROLE_TECHNOLOGY_MANAGER);
        serviceProv.importTechnicalServices(
                TECHNICAL_SERVICES_XML.getBytes("UTF-8"));

        final String productXml = "<?xml version='1.0' encoding='UTF-8'?>"
                + "<TechnicalProduct orgId=\"" + supplierId
                + "\" id=\"example\" version=\"1.00\">"

                + String.format(Locale.ENGLISH, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        P_1_ID, M, P_1_PRICE_PER_PERIOD, P_1_PRICE_PER_USER,
                        P_1_ONE_TIME_FEE, P_1_PRICE_LOGIN, P_1_PRICE_LOGOUT,
                        P_1_PRICE_UPLOAD)
                + String.format(Locale.ENGLISH, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        P_2_ID, M, P_2_PRICE_PER_PERIOD, P_2_PRICE_PER_USER,
                        P_2_ONE_TIME_FEE, P_2_PRICE_LOGIN, P_2_PRICE_LOGOUT,
                        P_2_PRICE_UPLOAD)
                + String.format(Locale.ENGLISH, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        P_3_ID, W, P_3_PRICE_PER_PERIOD, P_3_PRICE_PER_USER,
                        P_3_ONE_TIME_FEE, P_3_PRICE_LOGIN, P_3_PRICE_LOGOUT,
                        P_3_PRICE_UPLOAD)
                + String.format(Locale.ENGLISH, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        P_4_ID, D, P_4_PRICE_PER_PERIOD, P_4_PRICE_PER_USER,
                        P_4_ONE_TIME_FEE, P_4_PRICE_LOGIN, P_4_PRICE_LOGOUT,
                        P_4_PRICE_UPLOAD)
                + String.format(Locale.ENGLISH, PRODUCT_CHARGEABLE_XML_TEMPLATE,
                        P_5_ID, PricingPeriod.HOUR, P_5_PRICE_PER_PERIOD,
                        P_5_PRICE_PER_USER, P_5_ONE_TIME_FEE, P_5_PRICE_LOGIN,
                        P_5_PRICE_LOGOUT, P_5_PRICE_UPLOAD)

                + "</TechnicalProduct>";

        importProduct(productXml, mgr);
        container.login(platformUserKey);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // Test section
    /**
     * Test for situation, when in previous month was one active subscription.
     * And this subscription is still active in billing period. Bug 5190.
     * 
     * @throws Exception
     */
    @Test
    public void testSubscriptionFromPreviousMonth() throws Exception {
        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);
        final Date date = getDate(testYear, testMonth - 2, testDay, 0, 0);

        creSub(P_1_ID, SUBSCRIPTION_ID, date, null);
        // all updates are in last month
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        // set for all subscriptions history entry the same modification date
        // this is a situation what happens in real code executing;
        // need additional update in different transaction for changing
        // modification date
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query query = mgr.createQuery(
                        "update SubscriptionHistory h set h.modDate = :modDate");
                query.setParameter("modDate", date);
                query.executeUpdate();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                startBillingRun(billingTime);
                return null;
            }
        });
        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, P_1_PRICE_PER_PERIOD,
                testMonth);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Test for discount 0%.
     */
    @Test
    public void testSubscriptionFromPreviousMonthWithDiscount1()
            throws Exception {
        BigDecimal percent = new BigDecimal("0");
        BigDecimal amount = BigDecimal.ZERO;

        testSubscriptionFromPreviousMonthWithDiscountBase(percent, amount);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Test for discount 1%.
     */
    @Test
    public void testSubscriptionFromPreviousMonthWithDiscount2()
            throws Exception {
        BigDecimal percent = new BigDecimal("1.00");
        BigDecimal amount = BigDecimal.TEN;

        testSubscriptionFromPreviousMonthWithDiscountBase(percent, amount);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Test for discount 100%. No xml document will be created.
     */
    @Test
    public void testSubscriptionFromPreviousMonthWithDiscount3()
            throws Exception {
        BigDecimal percent = new BigDecimal("100.00");
        BigDecimal amount = BD1000;

        testSubscriptionFromPreviousMonthWithDiscountBase(percent, amount);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Helper method for testing subscription with discount.
     * 
     * @throws Exception
     */
    protected void testSubscriptionFromPreviousMonthWithDiscountBase(
            final BigDecimal percent, final BigDecimal amount)
            throws Exception {

        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);
        final Date date = getDate(testYear, testMonth - 2, testDay, 0, 0);

        creSub(P_1_ID, SUBSCRIPTION_ID, date, null);
        // all updates are in last month
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        // set for all subscriptions history entry the same modification date
        // this is a situation what happens in real code executing;
        // need additional update in different transaction for changing
        // modification date
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Query query = mgr.createQuery(
                        "update SubscriptionHistory h set h.modDate = :modDate");
                query.setParameter("modDate", date);
                query.executeUpdate();

                Subscription subscription = getSubscription(SUBSCRIPTION_ID);
                Organization organization = subscription.getOrganization();
                Discount discount = new Discount();
                discount.setOrganizationReference(
                        organization.getSources().get(0));
                discount.setValue(percent);
                Long startTime = Long.MIN_VALUE;
                Long endTime = Long.MAX_VALUE;
                discount.setStartTime(startTime);
                discount.setEndTime(endTime);

                mgr.persist(discount);
                mgr.flush();

                return null;
            }
        });
        BigDecimal discountNetAmount = amount;
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                startBillingRun(billingTime);
                return null;
            }
        });
        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, P_1_PRICE_PER_PERIOD,
                discountNetAmount, testMonth);
    }

    /**
     * Creates a subscription based on period MONTH, creates parameter for it
     * and checks the pricing. All parameter data types are covered.
     * 
     * @throws Exception
     */
    @Test
    public void testSimplePeriod1BeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        // costs for options will be for enumeration parameter, not price for
        // parameter
        final BigDecimal parametersAndOptionsCosts = new BigDecimal(5200)
                .setScale(BIGDECIMAL_SCALE);

        BigDecimal durationParamCosts = getDurationParamCosts(1300, 1,
                BigDecimal.ONE);

        // with all factors and multipliers no error situation
        testSimplePeriod1BeginOfJanuaryBase(testMonth, testDay,
                parametersAndOptionsCosts.add(durationParamCosts).setScale(
                        PriceConverter.NORMALIZED_PRICE_SCALING,
                        RoundingMode.HALF_UP),
                "");
        xmlValidator.validateBillingResultXML();
    }

    /**
     * MONTH Error in number parameter value. No billing result saved.
     * 
     * @throws Exception
     */
    @Test(expected = NoResultException.class)
    public void testSimplePeriod1BeginOfJanuaryWithNumberFormatException()
            throws Exception {
        final int testMonth = Calendar.JANUARY; // Calendar.JANUARY
        final int testDay = 1;
        final BigDecimal parametersAndOptionsCosts = new BigDecimal(10200)
                .setScale(BIGDECIMAL_SCALE);
        // with all factors and multipliers
        testSimplePeriod1BeginOfJanuaryBase(testMonth, testDay,
                parametersAndOptionsCosts, "NumberFormatException");
    }

    /**
     * MONTH Error in ParameterOptionHistory. No billing result saved.
     * 
     * @throws Exception
     */
    @Test(expected = NoResultException.class)
    public void testSimplePeriod1BeginOfJanuaryWithoutParameterOptionHistory()
            throws Exception {
        final int testMonth = Calendar.JANUARY; // Calendar.JANUARY
        final int testDay = 1;
        final BigDecimal parametersAndOptionsCosts = new BigDecimal(10200)
                .setScale(BIGDECIMAL_SCALE);
        // with all factors and multipliers
        testSimplePeriod1BeginOfJanuaryBase(testMonth, testDay,
                parametersAndOptionsCosts, "deleteParameterOptionHistory");
    }

    /**
     * WEEK
     * 
     * @throws Exception
     */
    @Test
    public void testSimplePeriod2BeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);
        BigDecimal exspected = new BigDecimal("25242.87");

        Subscription subscription = creSub(P_3_ID,
                getDate(testYear, testMonth, -2, 8, 0));
        prepareParametersAndOptionsBase(testMonth, subscription, "");
        startBillingRun(billingTime);
        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, exspected, testMonth);
        clearParametersAndOptions();
        xmlValidator.validateBillingResultXML();
    }

    /**
     * WEEK
     * 
     * @throws Exception
     */
    @Test
    public void testSimplePeriod2BeginOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);
        BigDecimal parametersAndOptionsCosts = new BigDecimal(20800)
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
        final BigDecimal subscriptionCosts = new BigDecimal(2000)
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

        Subscription subscription = creSub(P_3_ID,
                getDate(testYear, testMonth, -2, 8, 0));
        prepareParametersAndOptionsBase(testMonth, subscription, "");

        BigDecimal periodFactor = BigDecimal.valueOf(28)
                .divide(BigDecimal.valueOf(7));
        BigDecimal durationParamCosts = getDurationParamCosts(1300, 1,
                periodFactor);
        parametersAndOptionsCosts = parametersAndOptionsCosts
                .add(durationParamCosts);

        startBillingRun(billingTime);
        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } },
                subscriptionCosts.add(parametersAndOptionsCosts), testMonth);
        clearParametersAndOptions();
        xmlValidator.validateBillingResultXML();
    }

    /**
     * DAY
     * 
     * @throws Exception
     */
    @Test
    public void testSimplePeriod3BeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_4_ID, getDate(testYear, testMonth, -2, 8, 0));

        startBillingRun(billingTime);
        double faktor = getFraction(D, getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth), testYear, testMonth);

        BigDecimal price = P_4_PRICE_PER_PERIOD
                .multiply(BigDecimal.valueOf(faktor));

        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, price, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSimplePeriodWeekBeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_3_ID, getDate(testYear, testMonth, -2, 8, 0));

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } };

        verify(periods, new BigDecimal("2214.29"), testMonth);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testSimplePeriodWithUserBeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));
        subAddUser(U_1_ID, getDate(testYear, testMonth, -2, 8, 0));
        subAddUser(U_3_ID, getDate(testYear, testMonth, 12, 14, 55));
        subAddUser(U_2_ID, getDate(testYear, testMonth, 2, 8, 0));
        subRevokeUser(U_2_ID, getDate(testYear, testMonth, 16, 16, 59));
        subAddUser(U_2_ID, getDate(testYear, testMonth, 18, 8, 0));
        subRevokeUser(U_2_ID, getDate(testYear, testMonth, 20, 16, 59));

        startBillingRun(billingTime);

        double fraction = 0.0;
        fraction += getFraction(M, getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth), testYear, testMonth);
        fraction += getFraction(M, getDate(testYear, testMonth, 12, 14, 55),
                getEndDate(testYear, testMonth), testYear, testMonth);
        fraction += getFraction(M, getDate(testYear, testMonth, 2, 8, 0),
                getDate(testYear, testMonth, 16, 16, 59), testYear, testMonth);
        fraction += getFraction(M, getDate(testYear, testMonth, 18, 8, 0),
                getDate(testYear, testMonth, 20, 16, 59), testYear, testMonth);
        BigDecimal value = P_1_PRICE_PER_PERIOD
                .add(new BigDecimal(fraction).multiply(P_1_PRICE_PER_USER));
        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, value, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testSimplePeriodWithOneStartEventBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));
        createGatheredEvent(EventType.PLATFORM_EVENT,
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                getDate(testYear, testMonth, 1, 0, 0));

        startBillingRun(billingTime);

        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } },
                P_1_PRICE_PER_PERIOD.add(P_1_PRICE_LOGIN), testMonth);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testSimplePeriodWithOneEndEventBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));
        Calendar cal = getCal(testYear, testMonth, 1, 0, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.SECOND, -1);
        createGatheredEvent(EventType.PLATFORM_EVENT,
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE, cal.getTime());

        startBillingRun(billingTime);

        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } },
                P_1_PRICE_PER_PERIOD.add(P_1_PRICE_LOGIN), testMonth);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testSimplePeriodWithEventsBeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        // Multipliers for testing event numbers
        final long MULTIPLIER_2 = 20;
        final long MULTIPLIER_3 = 30;

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));

        createGatheredEvent(EventType.PLATFORM_EVENT,
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                getDate(testYear, testMonth, -1, 8, 0));

        createGatheredEvent(EventType.PLATFORM_EVENT,
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                getDate(testYear, testMonth, 1, 8, 0), MULTIPLIER_2);

        createGatheredEvent(EventType.SERVICE_EVENT, SERVICE_EVENT_FILE_UPLOAD,
                getDate(testYear, testMonth, 1, 8, 1), MULTIPLIER_3);

        delSub(getDate(testYear, testMonth, 32, 0, 0));

        startBillingRun(billingTime);
        BigDecimal value = P_1_PRICE_PER_PERIOD
                .add(P_1_PRICE_LOGIN.multiply(BD20));
        value = value.add(P_1_PRICE_UPLOAD.multiply(BD30));
        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, value, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodBeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("591.39");

        testInterruptedPeriod(testMonth, testDay, P_1_ID, false, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodEndOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 31;
        final BigDecimal etalonPrice = new BigDecimal("591.39");

        testInterruptedPeriod(testMonth, testDay, P_1_ID, false, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodBeginOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("654.7619047619");

        testInterruptedPeriod(testMonth, testDay, P_1_ID, false, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodEndOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 31;
        final BigDecimal etalonPrice = new BigDecimal("654.7619047619");

        testInterruptedPeriod(testMonth, testDay, P_1_ID, false, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser1BeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("746.37");

        testInterruptedPeriod(testMonth, testDay, P_1_ID, true, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser1EndOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 31;
        final BigDecimal etalonPrice = new BigDecimal("746.37");
        testInterruptedPeriod(testMonth, testDay, P_1_ID, true, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser1BeginOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("826.3492063492");

        testInterruptedPeriod(testMonth, testDay, P_1_ID, true, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser1EndOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 31;
        final BigDecimal etalonPrice = new BigDecimal("826.3492063492");

        testInterruptedPeriod(testMonth, testDay, P_1_ID, true, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser2BeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("1336.9777777778");

        testInterruptedPeriod(testMonth, testDay, P_3_ID, true, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser2EndOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 31;
        final BigDecimal etalonPrice = new BigDecimal("1336.9777777778");

        testInterruptedPeriod(testMonth, testDay, P_3_ID, true, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser3BeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("9166.6666666667");

        testInterruptedPeriod(testMonth, testDay, P_4_ID, false, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithUser3EndOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 31;
        final BigDecimal etalonPrice = new BigDecimal("9166.6666666667");

        testInterruptedPeriod(testMonth, testDay, P_4_ID, false, etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithManyEventsBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("2094.6012544803");

        testInterruptedPeriodWithManyEventsBase(testMonth, testDay,
                etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testInterruptedPeriodWithManyEventsBeginOfMarch()
            throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal("2147.5942460317");

        testInterruptedPeriodWithManyEventsBase(testMonth, testDay,
                etalonPrice);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMultipleSubscriptionsBeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice1 = new BigDecimal("1892.8075396825");
        final BigDecimal etalonPrice2 = new BigDecimal(9444);

        testMultipleSubscriptionsBase(testMonth, testDay, etalonPrice1,
                etalonPrice2, "1000.00");
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMultipleSubscriptionsBeginOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 1;
        final BigDecimal etalonPrice1 = new BigDecimal("1678.5218253968");
        final BigDecimal etalonPrice2 = new BigDecimal(9444);

        testMultipleSubscriptionsBase(testMonth, testDay, etalonPrice1,
                etalonPrice2, "1000.00");
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationBeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal price = new BigDecimal("11942.0322580645");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationEndOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 31;
        final BigDecimal price = new BigDecimal("11942.0322580645");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationBeginOfFebruary() throws Exception {
        final int testMonth = Calendar.FEBRUARY;
        final int testDay = 1;
        final BigDecimal price = new BigDecimal("11942.0322580645");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationEndOfFebruary() throws Exception {
        final int testMonth = Calendar.FEBRUARY;
        final int testDay = 28;
        final BigDecimal price = new BigDecimal("11942.0322580645");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationBeginOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 1;
        final BigDecimal price = new BigDecimal("12598.72");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationEndOfMarch() throws Exception {
        final int testMonth = Calendar.MARCH;
        final int testDay = 31;
        final BigDecimal price = new BigDecimal("12598.72");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationBeginOfApril() throws Exception {
        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final BigDecimal price = new BigDecimal("11950.2812920592");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testMigrationEndOfApril() throws Exception {
        final int testMonth = Calendar.APRIL;
        final int testDay = 30;
        final BigDecimal price = new BigDecimal("11950.2812920592");

        testMigrationBase(testYear, testMonth, testDay, price);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Creates a subscription and modifies the prices stored in the priced
     * events belonging to the subscription's product's price model.
     * 
     * @throws Exception
     */
    @Test
    public void testChangePricesForUsedPriceModelBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal(13500);

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));

        for (int i = 0; i < 25; i++) {
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                    getDate(testYear, testMonth, 1, 12, 2 * i));
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                    getDate(testYear, testMonth, 1, 14, 2 * i));
        }
        for (int i = 0; i < 47; i++) {
            createGatheredEvent(EventType.SERVICE_EVENT,
                    SERVICE_EVENT_FILE_UPLOAD,
                    getDate(testYear, testMonth, 2, 12, i));
        }

        // change the pricing now
        updSubscriptionPrices(BD20, new BigDecimal(14), BD10,
                getDate(testYear, testMonth, 10, 0, 0), BigDecimal.ZERO,
                BigDecimal.ZERO);
        for (int i = 0; i < 160; i++) {
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                    getDate(testYear, testMonth, 11, 12, 2 * i));
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                    getDate(testYear, testMonth, 11, 14, 2 * i));
        }
        for (int i = 0; i < 63; i++) {
            createGatheredEvent(EventType.SERVICE_EVENT,
                    SERVICE_EVENT_FILE_UPLOAD,
                    getDate(testYear, testMonth, 12, 12, i));
        }

        // change the pricing once again
        updSubscriptionPrices(new BigDecimal(25), BD10, BD10,
                getDate(testYear, testMonth, 20, 0, 0), BigDecimal.ZERO,
                BigDecimal.ZERO);
        for (int i = 0; i < 115; i++) {
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                    getDate(testYear, testMonth, 21, 12, 2 * i));
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                    getDate(testYear, testMonth, 21, 14, 2 * i));
        }
        for (int i = 0; i < 90; i++) {
            createGatheredEvent(EventType.SERVICE_EVENT,
                    SERVICE_EVENT_FILE_UPLOAD,
                    getDate(testYear, testMonth, 22, 12, i));
        }

        // and change it for the last time, but out of the bounds to be
        // considered by the billing run
        updSubscriptionPrices(P_1_PRICE_LOGIN, P_1_PRICE_UPLOAD,
                P_1_PRICE_LOGOUT, getDate(testYear, testMonth, 32, 0, 0),
                BigDecimal.ZERO, BigDecimal.ZERO);

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } };

        verify(periods, etalonPrice, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testChangePeriodPricesForUsedPriceModelBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal(12345);

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));

        // now change the price for the period on the last day of the month in
        // the billing period. Finally, the price must be reflected in the costs
        // - for the entire period.
        updSubscriptionPrices(P_1_PRICE_LOGIN, P_1_PRICE_UPLOAD,
                P_1_PRICE_LOGOUT, getDate(testYear, testMonth, 27, 23, 59),
                new BigDecimal(12345), BigDecimal.ZERO);

        // one update outside the relevant billing period range (should be
        // ignored by billing then).
        updSubscriptionPrices(P_1_PRICE_LOGIN, P_1_PRICE_UPLOAD,
                P_1_PRICE_LOGOUT, getDate(testYear, testMonth, 35, 23, 59),
                BD1000, BigDecimal.ZERO);

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } };

        verify(periods, etalonPrice, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testChangeUserAssignmentPeriodPricesForUsedPriceModelBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        BigDecimal etalonPrice = P_1_PRICE_PER_PERIOD
                .add(new BigDecimal(12345));

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));
        subAddUser(SUBSCRIPTION_ID, "anton",
                getDate(testYear, testMonth, -2, 8, 0));

        // now change the price for the period on the last day of the month in
        // the billing period. Finally, the price must be reflected in the costs
        // - for the entire period.
        updSubscriptionPrices(P_1_PRICE_LOGIN, P_1_PRICE_UPLOAD,
                P_1_PRICE_LOGOUT, getDate(testYear, testMonth, 27, 23, 59),
                P_1_PRICE_PER_PERIOD, new BigDecimal(12345));

        updSubscriptionPrices(P_1_PRICE_LOGIN, P_1_PRICE_UPLOAD,
                P_1_PRICE_LOGOUT, getDate(testYear, testMonth, 35, 23, 59),
                P_1_PRICE_PER_PERIOD, P_1_PRICE_PER_USER);

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } };

        verify(periods, etalonPrice, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Creates a subscription and modifies the prices stored in the priced
     * events belonging to the subscription's product's price model.
     * 
     * @throws Exception
     */
    @Test
    public void testChangePricesForUsedPriceModelRemoveEventBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal(1750);

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));

        for (int i = 0; i < 25; i++) {
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                    getDate(testYear, testMonth, 1, 12, 2 * i));
            createGatheredEvent(EventType.PLATFORM_EVENT,
                    PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                    getDate(testYear, testMonth, 1, 14, 2 * i));
        }
        for (int i = 0; i < 47; i++) {
            createGatheredEvent(EventType.SERVICE_EVENT,
                    SERVICE_EVENT_FILE_UPLOAD,
                    getDate(testYear, testMonth, 2, 12, i));
        }

        // change the pricing now, setting 0 deletes the priced event
        updSubscriptionPrices(BD20, BigDecimal.ZERO, BD10,
                getDate(testYear, testMonth, 20, 0, 0), BigDecimal.ZERO,
                BigDecimal.ZERO);

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } };

        verify(periods, etalonPrice, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Runs the billing for a customer that did not have a valid subscription in
     * the last month. The expectation is to retrieve null as resulting billing
     * result object and it must not be persisted anyway.
     * 
     * @throws Exception
     */
    @Test
    public void testPerformBillingRunForOrganizationSubCreatedAfterPeriodBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;

        creSub(P_1_ID, getDate(testYear, testMonth, 35, 0, 0));

        List<BillingResult> res = serviceBill.generateBillingForAnyPeriod(
                getStartDate(testYear, testMonth).getTime(),
                getEndDate(testYear, testMonth).getTime(), supplierTkey);

        Assert.assertTrue(
                "no result XML must have been generated, as there is no subscription in the current month",
                res.isEmpty());
    }

    /**
     * Declares that the result of the billing run should be persisted. As there
     * is no subscription in the current period, no object must be persisted.
     * 
     * @throws Exception
     */
    @Test
    public void testPerformBillingRunForOrganizationSubCreatedAfterPeriodNoPersistenceBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;

        // assert that no entry is stored in the database
        int currentBillResCount = getNumberOfBillingResults();

        serviceBill.generateBillingForAnyPeriod(
                getStartDate(testYear, testMonth).getTime(),
                getEndDate(testYear, testMonth).getTime(), supplierTkey);

        int numberOfBillResultsAfterInvocation = getNumberOfBillingResults();

        Assert.assertEquals(
                "Billing result object has been stored although it must not have been!",
                currentBillResCount, numberOfBillResultsAfterInvocation);
    }

    /**
     * Test related to bug 4905
     * 
     * @throws Exception
     */
    @Test
    public void testPerformBillingRunForOrganizationChangedOrgNameBeginOfJanuary()
            throws Exception {
        final int testMonth = Calendar.JANUARY;

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        customerTkey);
                organization.setName("new Name of Org 1000");
                organization.setAddress("completely new address");
                return organization;
            }
        });

        creSub(P_1_ID, getDate(testYear, testMonth, 24, 0, 0));

        BillingResult res = serviceBill
                .generateBillingForAnyPeriod(
                        getStartDate(testYear, testMonth).getTime(),
                        getEndDate(testYear, testMonth).getTime(), customerTkey)
                .get(0);

        String resultXML = res.getResultXML();
        Document resultDoc = XMLConverter.convertToDocument(resultXML, true);

        String organzationName = XMLConverter.getNodeTextContentByXPath(
                resultDoc, "/BillingDetails/OrganizationDetails/Name");
        String organizationAddress = XMLConverter.getNodeTextContentByXPath(
                resultDoc, "/BillingDetails/OrganizationDetails/Address");

        Assert.assertEquals("Wrong organization name found in result XML",
                "new Name of Org 1000", organzationName);
        Assert.assertEquals("Wrong organization name found in result XML",
                "completely new address", organizationAddress);
        String validationXML = new String(XMLConverter.combine("Billingdata",
                Collections.singletonList(resultXML)), "UTF-8");
        xmlValidator.validateBillingResultXML(validationXML);
    }

    /**
     * Start the billing run for a deleted organization. This must be the last
     * test.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteOrganizationBeginOfJanuary() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final BigDecimal etalonPrice = new BigDecimal(1030);

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 8, 0));

        createGatheredEvent(EventType.PLATFORM_EVENT,
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                getDate(testYear, testMonth, -1, 8, 0));

        createGatheredEvent(EventType.PLATFORM_EVENT,
                PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                getDate(testYear, testMonth, 1, 8, 0));

        createGatheredEvent(EventType.SERVICE_EVENT, SERVICE_EVENT_FILE_UPLOAD,
                getDate(testYear, testMonth, 1, 8, 1));

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Organization organization = Organizations.findOrganization(mgr,
                        customerId);
                if (organization != null) {
                    List<Subscription> subscriptions = organization
                            .getSubscriptions();
                    if (subscriptions != null) {
                        for (Subscription subscription : subscriptions) {
                            mgr.remove(subscription);
                        }
                    }
                    List<Product> products = organization.getProducts();
                    if (products != null) {
                        for (Product product : products) {
                            mgr.remove(product);
                        }
                    }
                }
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Organizations.removeOrganization(mgr, customerId);
                supplierId = null;
                return null;
            }
        });

        startBillingRun(billingTime);

        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, etalonPrice, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * Just tests whether the timer triggered billing process terminates
     * successfully. Does not perform result validation.
     * 
     * @throws Exception
     */
    @Test
    public void testStartBillingRun() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean
                        .valueOf(serviceBill.startBillingRun(billingTime));
            }
        });

        // as no real test is done, the default return value should be true
        Assert.assertTrue("Wrong result for billing invocation",
                result.booleanValue());
    }

    @Test
    public void testStartBillingRun_returnCode() throws Exception {

        // given an error in shares calculation
        given(sharesCalculator.performBrokerSharesCalculationRun(anyLong(),
                anyLong())).willReturn(Boolean.FALSE);

        // when executing billing
        Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                int testMonth = Calendar.JANUARY;
                int testDay = 1;
                long billingTime = getBillingTime(testYear, testMonth, testDay);
                return Boolean
                        .valueOf(serviceBill.startBillingRun(billingTime));
            }
        });

        // then total result must be false
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testStartBillingRunVerifyReceivingOrgs() throws Exception {
        final int testMonth = Calendar.JANUARY;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, 20, 0, 0));
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                serviceBill.startBillingRun(billingTime);
                return null;
            }
        });
        assertNotNull(ntfxReceivingOrgs);
        assertFalse(ntfxReceivingOrgs.isEmpty());
        assertEquals(2, ntfxReceivingOrgs.size());
        Set<Long> orgKeys = new HashSet<>();
        for (Organization org : ntfxReceivingOrgs) {
            orgKeys.add(Long.valueOf(org.getKey()));
        }
        assertTrue(orgKeys.contains(Long.valueOf(supplierTkey)));
        assertTrue(orgKeys.contains(Long.valueOf(customerTkey)));
    }

    @Test
    public void testSubLastMonthHourBased() throws Exception {
        final int testMonth = Calendar.OCTOBER;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);
        final Date date = getDate(testYear, testMonth - 2, testDay, 0, 0);

        creSub(P_5_ID, SUBSCRIPTION_ID, date, null);

        startBillingRun(billingTime);
        BigDecimal value = P_5_PRICE_PER_PERIOD.multiply(BD30)
                .multiply(new BigDecimal(24));
        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } }, value, testMonth);
        xmlValidator.validateBillingResultXML();
    }

    protected String getExpectedCustomerBillingData(List<String> customerIdList,
            String... months) throws Exception {
        if (customerIdList == null || customerIdList.isEmpty()) {
            customerIdList = runTX(new Callable<List<String>>() {
                @Override
                public List<String> call() {
                    Query query = mgr.createQuery(
                            "SELECT customer FROM Organization customer, OrganizationReference ref WHERE customer.key = ref.targetKey AND ref.dataContainer.referenceType = 'SUPPLIER_TO_CUSTOMER' AND ref.source = :supplier ORDER BY customer.key ASC");
                    query.setParameter("supplier", getOrganization());
                    List<String> customerIdList = new ArrayList<>();
                    for (Organization org : ParameterizedTypes.iterable(
                            query.getResultList(), Organization.class)) {
                        customerIdList.add(org.getOrganizationId());
                    }
                    return customerIdList;
                }
            });
        }
        StringBuffer xml = new StringBuffer(String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<Billingdata>%n"));
        for (String customerId : customerIdList) {
            for (int i = 0; i < months.length; i++) {
                xml.append("<result>").append(months[i]).append(" ")
                        .append(customerId)
                        .append(String.format("</result>%n"));
            }
        }
        xml.append(String.format("</Billingdata>%n"));
        return xml.toString();
    }

    private void doTestGetCustomerBillingData(List<String> customerIdList)
            throws Exception {

        Calendar calFrom = new GregorianCalendar();
        Calendar calTo = new GregorianCalendar();
        String jan = "JAN";
        String feb = "FEB";
        String march = "MARCH";

        // test with time frame (no results)
        // -----B1-------B2-------B3----------
        // FT
        calFrom.clear();
        calFrom.set(2010, 0, 1, 0, 0, 0);
        calTo.clear();
        calTo.set(2010, 0, 1, 0, 0, 0);
        String result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(getExpectedCustomerBillingData(customerIdList),
                result);

        // test without time frame
        result = new String(serviceBillExt.getCustomerBillingData(null, null,
                customerIdList), "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with 'from' border only - all results
        // -----B1-------B2-------B3----------
        // F
        calFrom.clear();
        calFrom.set(2010, 0, 1, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()), null, customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with 'from' border only - only two results
        // -----B1-------B2-------B3----------
        // F-
        calFrom.clear();
        calFrom.set(2010, 1, 1, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()), null, customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, feb, march),
                result);

        // test with 'to' border only - all results
        // -----B1-------B2-------B3----------
        // T-
        calTo.clear();
        calTo.set(2010, 3, 1, 0, 0, 0);
        result = new String(
                serviceBillExt.getCustomerBillingData(null,
                        Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with 'to' border only - only two results
        // -----B1-------B2-------B3----------
        // T-
        calTo.clear();
        calTo.set(2010, 2, 1, 0, 0, 0);
        result = new String(
                serviceBillExt.getCustomerBillingData(null,
                        Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb),
                result);

        // test with time frame (all results)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 0, 1, 0, 0, 0);
        calTo.set(2010, 3, 1, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with time frame (exactly one result)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 0, 1, 0, 0, 0);
        calTo.set(2010, 1, 2, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(getExpectedCustomerBillingData(customerIdList, jan),
                result);

        // test with time frame (exactly one result)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 1, 28, 0, 0, 0);
        calTo.set(2010, 2, 1, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(getExpectedCustomerBillingData(customerIdList, feb),
                result);

        // test with time frame (exactly one result)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 1, 1, 0, 0, 0);
        calTo.set(2010, 2, 31, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(getExpectedCustomerBillingData(customerIdList, feb),
                result);

        // test with 'from' border only - all results
        // -----B1-------B2-------B3-----------
        // F
        calFrom.clear();
        calFrom.set(2009, 11, 31, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()), null, customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with 'from' border only - two results
        // -----B1-------B2-------B3-----------
        // F
        calFrom.clear();
        calFrom.set(2010, 1, 2, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()), null, customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, feb, march),
                result);

        // test with 'to' border only - all results
        // -----B1-------B2-------B3-----------
        // T-
        calTo.clear();
        calTo.set(2010, 3, 12, 0, 0, 0);
        result = new String(
                serviceBillExt.getCustomerBillingData(null,
                        Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with 'to' border only - only two results
        // -----B1-------B2-------B3-----------
        // T-
        calTo.clear();
        calTo.set(2010, 2, 27, 0, 0, 0);
        result = new String(
                serviceBillExt.getCustomerBillingData(null,
                        Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb),
                result);

        // test with time frame (all results)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2009, 11, 31, 0, 0, 0);
        calTo.set(2010, 3, 1, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with time frame (all results)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 0, 1, 0, 0, 0);
        calTo.set(2010, 3, 1, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, jan, feb, march),
                result);

        // test with time frame (only two results)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 1, 1, 0, 0, 0);
        calTo.set(2010, 3, 2, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, feb, march),
                result);

        // test with time frame (only two results)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 1, 1, 0, 0, 0);
        calTo.set(2010, 3, 3, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, feb, march),
                result);

        // test with time frame (only two results)
        // -----B1-------B2-------B3-----------
        // F T
        calFrom.clear();
        calTo.clear();
        calFrom.set(2010, 1, 1, 0, 0, 0);
        calTo.set(2010, 3, 1, 0, 0, 0);
        result = new String(serviceBillExt.getCustomerBillingData(
                Long.valueOf(calFrom.getTimeInMillis()),
                Long.valueOf(calTo.getTimeInMillis()), customerIdList),
                "UTF-8");
        Assert.assertEquals(
                getExpectedCustomerBillingData(customerIdList, feb, march),
                result);
    }

    @Test
    public void testGetCustomerBillingData() throws Exception {
        // testDataSetup
        createBillingResult(customerId);

        // login as supplier
        container.login(platformUserKey, ROLE_SERVICE_MANAGER);

        Calendar calFrom = new GregorianCalendar();
        Calendar calTo = new GregorianCalendar();
        List<String> list = new ArrayList<>();
        list.add(supplierId);

        // supplier get own empty billing result
        String result = new String(
                serviceBillExt.getCustomerBillingData(
                        Long.valueOf(calFrom.getTimeInMillis()),
                        Long.valueOf(calTo.getTimeInMillis()), list),
                "UTF-8");
        xmlValidator.validateBillingResultXML(result);
        Assert.assertFalse("Not empty result", result.contains("<result>"));
    }

    @Test
    public void testGetCustomerBillingData_Reseller() throws Exception {
        // testDataSetup
        createBillingResult(customerForResellerId);

        // login as reseller
        container.login(platformResellerUserKey, ROLE_RESELLER_MANAGER);

        Calendar calFrom = new GregorianCalendar();
        Calendar calTo = new GregorianCalendar();
        List<String> list = new ArrayList<>();
        list.add(supplierId);

        // reseller get own empty billing result
        String result = new String(
                serviceBillExt.getCustomerBillingData(
                        Long.valueOf(calFrom.getTimeInMillis()),
                        Long.valueOf(calTo.getTimeInMillis()), list),
                "UTF-8");
        xmlValidator.validateBillingResultXML(result);
        Assert.assertFalse("Not empty result", result.contains("<result>"));
    }

    @Test
    public void testGetCustomerBillingData_validateOutput() throws Exception {
        BigDecimal percent = new BigDecimal("1.00");
        BigDecimal amount = BD10;

        testSubscriptionFromPreviousMonthWithDiscountBase(percent, amount);

        long startTime = getBillingTime(testYear, Calendar.JANUARY, 1);
        long endTime = getBillingTime(testYear, Calendar.DECEMBER, 1);
        container.login(platformUserKey, ROLE_SERVICE_MANAGER);

        String result = new String(
                serviceBillExt.getCustomerBillingData(Long.valueOf(startTime),
                        Long.valueOf(endTime),
                        Collections.singletonList(customerId)),
                "UTF-8");

        xmlValidator.validateBillingResultXML(result);
    }

    @Test
    public void testGetCustomerBillingData_validateOutput_MultipleSubscriptions()
            throws Exception {
        final int testMonth = Calendar.OCTOBER;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);
        final Date date = getDate(testYear, testMonth - 2, testDay, 0, 0);

        String subId1 = SUBSCRIPTION_ID;
        String subId2 = "sub2";

        // Create two subscriptions and start the billing run.
        creSub(P_1_ID, subId1, date, null);
        creSub(P_1_ID, subId2, date, null);

        startBillingRun(billingTime);

        Date startDate = getStartDate(testYear, testMonth);
        Date endDate = getEndDate(testYear, testMonth);
        long startTime = startDate.getTime();
        long endTime = endDate.getTime();

        // Verify the two subscriptions.
        Date periods[][];
        periods = new Date[][] { { startDate, endDate } };
        verify(subId1, periods, new BigDecimal("1000.00"), testMonth);

        verify(subId2, periods, new BigDecimal("1000.00"), testMonth);

        // Get the customer billing data.
        String result = new String(
                serviceBillExt.getCustomerBillingData(Long.valueOf(startTime),
                        Long.valueOf(endTime),
                        Collections.singletonList(customerId)),
                "UTF-8");

        // Validate XML document!!
        xmlValidator.validateBillingResultXML(result);

        Document resultDoc = XMLConverter.convertToDocument(result, false);

        // Check if stored organization names in result XML are correct.
        String orgName1 = XMLConverter.getNodeTextContentByXPath(resultDoc,
                "/Billingdata/BillingDetails[1]/OrganizationDetails/Name");
        String orgName2 = XMLConverter.getNodeTextContentByXPath(resultDoc,
                "/Billingdata/BillingDetails[2]/OrganizationDetails/Name");

        Assert.assertEquals("Wrong organization name found in result XML",
                customerName, orgName1);

        Assert.assertEquals("Wrong organization name found in result XML",
                customerName, orgName2);

        // Check if stored subscription ids in result XML are correct.
        String subNodeId1 = XMLConverter.getNodeTextContentByXPath(resultDoc,
                "//Billingdata/BillingDetails[1]/Subscriptions/Subscription/@id");

        String subNodeId2 = XMLConverter.getNodeTextContentByXPath(resultDoc,
                "//Billingdata/BillingDetails[2]/Subscriptions/Subscription/@id");

        Assert.assertEquals("Wrong subcription id found in result XML",
                subNodeId1, subId1);
        Assert.assertEquals("Wrong subscription id found in result XML",
                subNodeId2, subId2);
    }

    protected String createCustomer() throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Organization customer;
                customer = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);
                OrganizationReference ref = new OrganizationReference(
                        Organizations.findOrganization(mgr, supplierId),
                        customer,
                        OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
                mgr.persist(ref);
                return customer.getOrganizationId();
            }
        });
    }

    @Test
    public void testGetCustomerBillingData2() throws Exception {
        // testDataSetup
        createBillingResult(customerId);
        String customerId1 = createCustomer();
        String customerId2 = createCustomer();
        String customerId3 = createCustomer();
        createBillingResult(customerId1);
        createBillingResult(customerId2);
        createBillingResult(customerId3);

        // login
        container.login(platformUserKey, ROLE_SERVICE_MANAGER);

        List<String> customerIdList = new ArrayList<>();

        customerIdList.add(customerId1);
        doTestGetCustomerBillingData(customerIdList);

        customerIdList.add(customerId2);
        doTestGetCustomerBillingData(customerIdList);

        customerIdList.add(customerId3);
        doTestGetCustomerBillingData(customerIdList);

        customerIdList.remove(customerId2);
        doTestGetCustomerBillingData(customerIdList);
    }

    @Test
    public void testPerformBillingRunForOrganizationNoCosts() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Scenario.setup(container, false, true);

                PriceModel priceModel = Scenario.getPriceModel();
                priceModel = mgr.getReference(PriceModel.class,
                        priceModel.getKey());

                priceModel.setType(PriceModelType.FREE_OF_CHARGE);
                return null;
            }
        });

        BillingResult res = serviceBill.generateBillingForAnyPeriod(
                System.currentTimeMillis(), System.currentTimeMillis() + 20,
                Scenario.getCustomer().getKey()).get(0);

        Assert.assertNotNull("Result must not be null", res);
        checkEquals("Wrong costs contained", new BigDecimal("0.68"),
                res.getGrossAmount(), PriceConverter.NORMALIZED_PRICE_SCALING);
    }

    /**
     * Tests if the results of the method "performBillingRunOrganization"
     * contain a billing result for each of the two created subscriptions and
     * validates the respective billing XML documents.
     * 
     * @throws Exception
     */

    @Test
    public void testPerformBillingRunForOrganization_MultipleSubscriptions()
            throws Exception {
        testYear = 2010;
        int testMonth = Calendar.MAY;
        long startTime = 1272664800000L;
        long endTime = 1273156744630L;

        Date subscriptionStartDate = getDate(testYear, testMonth, 14, 8, 0);

        // Create two subscriptions
        Subscription subscription1 = creSub(P_1_ID, SUBSCRIPTION_ID,
                subscriptionStartDate, null);

        Subscription subscription2 = creSub(P_1_ID, "sub2",
                subscriptionStartDate, null);
        // Execute
        List<BillingResult> resultsList = serviceBill
                .generateBillingForAnyPeriod(startTime, endTime, customerTkey);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startTime);
        Date startDate = cal.getTime();

        // Result must not be null and must contain 2 elements.
        Assert.assertNotNull("Results must not be null", resultsList);
        Assert.assertEquals(
                "There must be 2 billing results for the 2 subscriptions.", 2,
                resultsList.size());

        // The subscription keys must match those stored in the billing results.
        BillingResult result = resultsList.get(0);
        assertNotNull(result.getSubscriptionKey());
        assertEquals(subscription1.getKey(),
                result.getSubscriptionKey().longValue());

        result = resultsList.get(1);
        assertNotNull(result.getSubscriptionKey());
        assertEquals(subscription2.getKey(),
                result.getSubscriptionKey().longValue());

        // Verify the billing XML document for subscription1 and if the price
        // stored matches
        // the expected price.
        Date periods[][];
        periods = new Date[][] { { startDate, new Date(endTime) } };

        verify(XMLConverter.convertToDocument(resultsList.get(0).getResultXML(),
                true), SUBSCRIPTION_ID, periods, new BigDecimal("183.67"),
                BigDecimal.ZERO);

        // Verify the billing XML document for subscription2.
        verify(XMLConverter.convertToDocument(resultsList.get(1).getResultXML(),
                true), "sub2", periods, new BigDecimal("183.67"),
                BigDecimal.ZERO);

        // Validate the XML structure.
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testStartBillingCurrentPeriod() throws Exception {
        testYear = 2010;
        Date subscriptionStartDate = getDate(testYear, Calendar.MAY, 14, 8, 0);
        creSub(P_1_ID, subscriptionStartDate);

        updSub(P_4_ID, getDate(testYear, Calendar.MAY, 18, 8, 0));

        long startTime = 1272664800000L;
        long endTime = 1273156744630L;

        List<BillingResult> result = serviceBill
                .generateBillingForAnyPeriod(startTime, endTime, customerTkey);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startTime);
        // cal.add(Calendar.MONTH, 1);
        Date startDate = cal.getTime();

        verify(XMLConverter.convertToDocument(result.get(0).getResultXML(),
                true), SUBSCRIPTION_ID,
                new Date[][] { { startDate, new Date(endTime) } },
                new BigDecimal("2846.9017939814815").setScale(BIGDECIMAL_SCALE),
                BigDecimal.ZERO);
        xmlValidator.validateBillingResultXML();
    }

    /**
     * The subscription key must be part of the billing result
     * 
     * @throws Exception
     */
    @Test
    public void testBillingResult_subscriptionKey() throws Exception {

        // given
        testYear = 2010;
        long startTime = 1272664800000L;
        long endTime = 1273156744630L;
        Date subscriptionStartDate = getDate(testYear, Calendar.MAY, 14, 8, 0);
        Subscription subscription = creSub(P_1_ID, subscriptionStartDate);

        // execute
        List<BillingResult> bills = serviceBill
                .generateBillingForAnyPeriod(startTime, endTime, customerTkey);

        // assert
        BillingResult result = bills.get(0);
        assertNotNull(result.getSubscriptionKey());
        assertEquals(subscription.getKey(),
                result.getSubscriptionKey().longValue());
    }

    @Test
    public void test_SubscribeMarchTerminateMayBillForMay() throws Exception {
        creSub(P_2_ID, getDate(testYear, 3, 5, 0, 0));
        updSub(SubscriptionStatus.ACTIVE, getDate(testYear, 3, 22, 0, 1));
        Date deactivatedDate = getDate(testYear, 5, 22, 5, 0);
        updSub(SubscriptionStatus.DEACTIVATED, deactivatedDate);

        long startTime = getBillingTime(testYear, Calendar.MAY, 1);
        long endTime = getBillingTime(testYear, Calendar.JUNE, 1);

        BillingResult res = serviceBill
                .generateBillingForAnyPeriod(startTime, endTime, customerTkey)
                .get(0);

        Assert.assertNotNull("Result must not be null", res);
        Assert.assertTrue("Costs must not be zero",
                res.getGrossAmount().compareTo(BigDecimal.ZERO) == 1);

        String resultXML = res.getResultXML();
        Document resultDoc = XMLConverter.convertToDocument(resultXML, true);
        String endDate = XMLConverter.getNodeTextContentByXPath(resultDoc,
                "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/UsagePeriod/@endDate");
        Assert.assertEquals(String.valueOf(deactivatedDate.getTime()), endDate);
        String priceModelFactor = XMLConverter.getNodeTextContentByXPath(
                resultDoc,
                "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/PeriodFee/@factor");
        Assert.assertTrue(Float.parseFloat(priceModelFactor) < 1.0);
    }

    @Test
    public void test_SubscribeMarchTerminateJuneBillForMay() throws Exception {
        creSub(P_2_ID, getDate(testYear, 3, 5, 0, 0));
        updSub(SubscriptionStatus.ACTIVE, getDate(testYear, 3, 22, 0, 1));
        updSub(SubscriptionStatus.DEACTIVATED, getDate(testYear, 6, 2, 0, 0));

        long startTime = getBillingTime(testYear, Calendar.MAY, 1);
        long endTime = getBillingTime(testYear, Calendar.JUNE, 1);

        BillingResult res = serviceBill
                .generateBillingForAnyPeriod(startTime, endTime, customerTkey)
                .get(0);

        Assert.assertNotNull("Result must not be null", res);
        Assert.assertTrue("Costs must not be zero",
                res.getGrossAmount().compareTo(BigDecimal.ZERO) == 1);

        String resultXML = res.getResultXML();
        Document resultDoc = XMLConverter.convertToDocument(resultXML, true);
        String endDate = XMLConverter.getNodeTextContentByXPath(resultDoc,
                "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/UsagePeriod/@endDate");
        Assert.assertEquals(String.valueOf(endTime), endDate);
        String priceModelFactor = XMLConverter.getNodeTextContentByXPath(
                resultDoc,
                "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/PeriodFee/@factor");
        Assert.assertTrue(Float.parseFloat(priceModelFactor) == 1.0);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // internal helper methods

    /**
     * Returns the number of billing result objects that are currently stored in
     * the database.
     * 
     * @return The number of billing result objects.
     * @throws Exception
     */
    protected int getNumberOfBillingResults() throws Exception {
        Long count = runTX(new Callable<Long>() {
            @Override
            public Long call() {
                Query query = mgr
                        .createQuery("SELECT count(*) FROM BillingResult e");
                Long count = (Long) query.getSingleResult();
                return count;
            }
        });
        return count.intValue();
    }

    /**
     * Read the organization from the database.
     * 
     * @return the read organization domain object.
     */
    protected Organization getOrganization() {
        Organization organization = new Organization();
        organization.setOrganizationId(supplierId);
        organization = (Organization) mgr.find(organization);
        return organization;
    }

    /**
     * Reads the customer organization from the database.
     * 
     * @return the read organization domain object.
     */
    protected Organization getCustomerOrganization() {
        Organization organization = new Organization();
        organization.setOrganizationId(customerId);
        organization = (Organization) mgr.find(organization);
        return organization;
    }

    /**
     * Read the subscription with the given id from the database.
     * 
     * @param subId
     *            the id of the subscription to read.
     * 
     * @return the read subscription domain object.
     */
    protected Subscription getSubscription(String subId) {
        Subscription sub = new Subscription();
        sub.setOrganization(getCustomerOrganization());
        sub.setSubscriptionId(subId);
        sub = (Subscription) mgr.find(sub);
        return sub;
    }

    /**
     * Create a new GatheredEvent object
     * 
     * @param type
     *            the event type
     * @param id
     *            the event id
     * @param date
     *            the event occurrence time
     * @return the created object
     * @throws Exception
     */
    protected GatheredEvent createGatheredEventObject(final EventType type,
            final String id, final Date date) {
        GatheredEvent e = new GatheredEvent();
        e.setActor("Anonymous");
        e.setEventId(id);
        e.setOccurrenceTime(date.getTime());
        e.setSubscriptionTKey(subscriptionKey);
        e.setType(type);
        // initialization of multiplier is not needed because GatheredEventData
        // has already default initialization of multiplier to 1L
        return e;
    }

    /**
     * Create a new GatheredEvent object
     * 
     * @param type
     *            the event type
     * @param id
     *            the event id
     * @param date
     *            the event occurrence time
     * @param multiplier
     *            event multiplier
     * @return the created object
     * @throws Exception
     */
    protected GatheredEvent createGatheredEventObject(final EventType type,
            final String id, final Date date, final long multiplier) {
        GatheredEvent e = createGatheredEventObject(type, id, date);
        e.setMultiplier(multiplier);
        return e;
    }

    /**
     * 
     * Create a new GatheredEvent database record with multiplier value 1.
     * 
     * @param type
     *            the event type
     * @param id
     *            the event id
     * @param date
     *            the event occurrence time
     * @throws Exception
     */
    protected void createGatheredEvent(final EventType type, final String id,
            final Date date) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                GatheredEvent e = createGatheredEventObject(type, id, date);
                mgr.persist(e);
                return null;
            }
        });
    }

    /**
     * Create a new GatheredEvent database record
     * 
     * @param type
     *            the event type
     * @param id
     *            the event id
     * @param date
     *            the event occurrence time
     * @param multiplier
     *            The multiplier to be used for the events.
     * @throws Exception
     */
    protected void createGatheredEvent(final EventType type, final String id,
            final Date date, final long multiplier) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                GatheredEvent e = createGatheredEventObject(type, id, date,
                        multiplier);
                mgr.persist(e);
                return null;
            }
        });
    }

    /**
     * Update the modification date of the last history record for the given
     * subscription.
     * 
     * @param obj
     *            The domain object, the last history entry of which has to be
     *            updated.
     * 
     * @param date
     *            the modification date to set.
     */
    protected void updateHistoryModDate(DomainObject<?> obj, Date date) {
        obj.setHistoryModificationTime(Long.valueOf(date.getTime()));
    }

    protected Subscription creSub(String productId, Date date)
            throws Exception {
        return creSub(productId, SUBSCRIPTION_ID, date, null);
    }

    protected Subscription creSub(final String productId, final String subId,
            final Date date, final Long historyDate) throws Exception {
        final Subscription result = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Query query = mgr.createQuery(
                        "UPDATE ParameterDefinitionHistory pdh SET pdh.modDate = :modDate");
                query.setParameter("modDate", date);
                query.executeUpdate();
                query = mgr.createQuery(
                        "UPDATE ParameterOptionHistory poh SET poh.modDate = :modDate");
                query.setParameter("modDate", date);
                query.executeUpdate();

                Product prod = new Product();
                prod.setVendor(Organizations.findOrganization(mgr, supplierId));
                prod.setProductId(productId);
                prod = (Product) mgr.find(prod);

                Subscription sub = new Subscription();
                Organization customer = getCustomerOrganization();
                sub.setOrganization(customer);
                sub.setSubscriptionId(subId);
                sub.setProductInstanceId(productId);
                sub.setCreationDate(Long.valueOf(date.getTime()));
                sub.setActivationDate(Long.valueOf(date.getTime()));
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setCutOffDay(1);
                sub.setUuid(UUID.randomUUID());

                if (historyDate != null) {
                    sub.setHistoryModificationTime(historyDate);
                }
                updateHistoryModDate(sub, date);

                Product productCopy = prod.copyForSubscription(null, sub);
                productCopy.setOwningSubscription(null);
                mgr.persist(productCopy);

                sub.bindToProduct(productCopy);
                mgr.persist(sub);
                subscriptionKey = sub.getKey();

                productCopy.setOwningSubscription(sub);

                PaymentType pt = new PaymentType();
                pt.setPaymentTypeId("DIRECT_DEBIT");
                pt = (PaymentType) mgr.getReferenceByBusinessKey(pt);

                PaymentInfo paymentInfo = PaymentInfos
                        .createPaymentInfo(customer, mgr, pt);
                sub.setPaymentInfo(paymentInfo);
                BillingContact bc = PaymentInfos.createBillingContact(mgr,
                        customer);
                sub.setBillingContact(bc);
                bc.setOrgAddressUsed(true);

                return sub;
            }
        });
        updateHistoryEntriesForRelatedProduct(date, result);

        return result;
    }

    /**
     * Sets the modification date for the ADD entries of the priced event
     * history to the given date in case it is before the one currently set. Its
     * basic meaning is to set a correct time for the priced events that will be
     * considered by the billing run. It also sets the latest history entry for
     * the product and the price model to the given time.
     * 
     * @param date
     *            The date to be set.
     * @param currentSub
     *            The subscription under consideration.
     * @throws Exception
     */
    protected void updateHistoryEntriesForRelatedProduct(final Date date,
            final Subscription currentSub) throws Exception {
        // and now also set the date for the priced event related history
        // entries to the given date
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = (Subscription) mgr.find(currentSub);
                List<PricedEvent> events = sub.getProduct().getPriceModel()
                        .getConsideredEvents();
                for (PricedEvent evt : events) {
                    List<DomainHistoryObject<?>> hist = mgr.findHistory(evt);
                    for (DomainHistoryObject<?> historyObject : hist) {
                        if (historyObject.getModdate().after(date)) {
                            historyObject.setModdate(date);
                            mgr.persist(historyObject);
                        }
                    }
                }

                Product product = sub.getProduct();
                List<DomainHistoryObject<?>> productHistory = mgr
                        .findHistory(product);
                DomainHistoryObject<?> lastProductHistoryEntry = productHistory
                        .get(productHistory.size() - 1);
                if (lastProductHistoryEntry.getModdate().after(date)) {
                    lastProductHistoryEntry.setModdate(date);
                    mgr.merge(lastProductHistoryEntry);
                }

                PriceModel priceModel = sub.getProduct().getPriceModel();
                List<DomainHistoryObject<?>> priceModelHistory = mgr
                        .findHistory(priceModel);
                DomainHistoryObject<?> lastPriceModelHistoryEntry = priceModelHistory
                        .get(priceModelHistory.size() - 1);
                if (lastPriceModelHistoryEntry.getModdate().after(date)) {
                    lastPriceModelHistoryEntry.setModdate(date);
                    mgr.merge(lastPriceModelHistoryEntry);
                }

                return null;
            }
        });
    }

    protected void createBillingResult(final String orgId) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency currency_EUR = SupportedCurrencies
                        .findOrCreate(mgr, "EUR");
                Organization org = Organizations.findOrganization(mgr, orgId);
                Calendar cal = new GregorianCalendar();
                cal.clear();
                cal.set(2010, 0, 1, 0, 0, 0);
                BillingResult br1 = new BillingResult();
                br1.setCreationTime(TIMESTAMP);
                br1.setOrganizationTKey(org.getKey());
                br1.setPeriodStartTime(cal.getTime().getTime());
                cal.clear();
                cal.set(2010, 1, 1, 0, 0, 0);
                br1.setPeriodEndTime(cal.getTime().getTime());
                br1.setResultXML(
                        "<result>JAN " + org.getOrganizationId() + "</result>");
                br1.setChargingOrgKey(supplierTkey);
                br1.setCurrency(currency_EUR);
                br1.setGrossAmount(GROSS_REVENUE);
                br1.setNetAmount(NET_REVENUE);
                mgr.persist(br1);

                cal.clear();
                cal.set(2010, 1, 1, 0, 0, 0);
                BillingResult br2 = new BillingResult();
                br2.setCreationTime(TIMESTAMP);
                br2.setOrganizationTKey(org.getKey());
                br2.setPeriodStartTime(cal.getTime().getTime());
                cal.clear();
                cal.set(2010, 2, 1, 0, 0, 0);
                br2.setPeriodEndTime(cal.getTime().getTime());
                br2.setResultXML(
                        "<result>FEB " + org.getOrganizationId() + "</result>");
                br2.setChargingOrgKey(supplierTkey);
                br2.setCurrency(currency_EUR);
                br2.setGrossAmount(GROSS_REVENUE);
                br2.setNetAmount(NET_REVENUE);
                mgr.persist(br2);

                cal.clear();
                cal.set(2010, 2, 1, 0, 0, 0);
                BillingResult br3 = new BillingResult();
                br3.setCreationTime(TIMESTAMP);
                br3.setOrganizationTKey(org.getKey());
                br3.setPeriodStartTime(cal.getTime().getTime());
                cal.clear();
                cal.set(2010, 3, 1, 0, 0, 0);
                br3.setPeriodEndTime(cal.getTime().getTime());
                br3.setResultXML("<result>MARCH " + org.getOrganizationId()
                        + "</result>");
                br3.setChargingOrgKey(supplierTkey);
                br3.setCurrency(currency_EUR);
                br3.setGrossAmount(GROSS_REVENUE);
                br3.setNetAmount(NET_REVENUE);
                mgr.persist(br3);

                return null;
            }
        });

    }

    /**
     * Creates 6 priced parameters for the given subscription.
     * 
     * @param paramTestMonth
     *            The month to start the test in.
     * 
     * @param subscription
     *            Subscription object.
     * @param value
     *            Indicates whether to cause a NumberFormatException or not.
     * @return Costs of inserted parameters and options. For final tests all
     *         multiplier and factors have to be considered.
     * @throws Exception
     */
    protected BigDecimal[][] prepareParametersAndOptionsBase(int paramTestMonth,
            final Subscription subscription, String value) throws Exception {
        final BigDecimal[] pricePerUserArray = { BD100, BD200, BD300, BD400,
                BD500, BD600, BD700 };
        final BigDecimal[] pricePerSubscriptionArray = { BD1100, BD1200, BD1300,
                BD1400, BD1500, BD1600, BD1700 };
        final BigDecimal costs[][] = {
                { BD100, BD200, BD300, BD400, BD500, BD600 },
                { BD1100, BD1200, BD1300, BD1400, BD1500, BD1600 } };
        final String[] parametersIdentifiersArray = { "CONCURRENT_USER",
                "MAX_FILE_NUMBER", "PERIOD", "HAS_OPTIONS",
                "BOOLEAN_PARAMETER_TRUE", "BOOLEAN_PARAMETER_FALSE" };
        String PARAMETER_VALUE = "1";

        if (value.equals("NumberFormatException")) {
            PARAMETER_VALUE = "NumberFormatException"; // set not number value
            // for exception
        }

        final PriceModel priceModel = subscription.getPriceModel();

        Date date = getDate(testYear, paramTestMonth, -2, 8, 0);

        for (int i = 0; i < 6; i++) {
            // will be 3 different parameter, for price model
            Parameter parameter = createParameter(parametersIdentifiersArray[i],
                    PARAMETER_VALUE, 0, date);

            // will be 3 different priced parameter, for every
            // parameter (price model)
            PricedParameter pricedParameter = createPricedParameter(
                    pricePerUserArray[i], pricePerSubscriptionArray[i],
                    priceModel, parameter, date);

            // will be 1 priced option for every priced
            // parameter
            createPricedOption(pricePerUserArray[i],
                    pricePerSubscriptionArray[i], pricedParameter, date);
        }

        if (value.equals("deleteParameterOptionHistory")) {
            deleteParameterOptionHistory(); // make an error situation
        }

        if (value.equals("deleteParameterDefinitionHistory")) {
            deleteParameterDefinitionHistory(); // make an error situation
        }

        return costs;
    }

    /**
     * Creates a priced parameter for the given parameter, sets the specified
     * costs for it, adds it to the given price model and returns it.
     * 
     * @param pricePerUser
     *            User price.
     * @param pricePerSubscription
     *            Subscription price.
     * @param priceModel
     *            Price model object.
     * @param parameter
     *            Parameter object.
     * @param date
     * @return Inserted object of priced parameter.
     * @throws Exception
     */
    protected PricedParameter createPricedParameter(
            final BigDecimal pricePerUser,
            final BigDecimal pricePerSubscription, final PriceModel priceModel,
            final Parameter parameter, final Date date) throws Exception {
        final PricedParameter result = runTX(new Callable<PricedParameter>() {
            @Override
            public PricedParameter call() throws Exception {
                PricedParameter pricedParameter = null;

                pricedParameter = new PricedParameter();
                pricedParameter.setPricePerUser(pricePerUser);
                pricedParameter.setPricePerSubscription(pricePerSubscription);
                pricedParameter.setPriceModel(priceModel);
                pricedParameter.setParameter(parameter);
                updateHistoryModDate(pricedParameter, date);

                mgr.persist(pricedParameter);
                mgr.flush();
                for (int i = 0; i < 2; i++) {
                    pricedParameter.setPricePerUser(pricedParameter
                            .getPricePerUser().add(BigDecimal.ONE));
                    mgr.flush();
                }
                pricedParameter.setPricePerUser(pricePerUser);
                mgr.flush();

                return pricedParameter;
            }
        });
        return result;
    }

    /**
     * Creates a parameter based on the parameter definition identified by the
     * parameter platformParameterIdentifier and adds it to the parameter set
     * identified by the version param.
     * 
     * @param platformParameterIdentifier
     *            Parameter identifier.
     * @param parameterValue
     *            Value of parameter.
     * @param parameterSetVersion
     *            Parameter set version.
     * @param date
     * @return Inserted object of parameter.
     * @throws Exception
     */
    protected Parameter createParameter( /*
                                          * final ParameterType parameterType,
                                          */
            final String platformParameterIdentifier,
            final String parameterValue, final int parameterSetVersion,
            final Date date) throws Exception {
        final Parameter result = runTX(new Callable<Parameter>() {
            @Override
            public Parameter call() throws Exception {

                String tmpPlatformParameterIdentifier = platformParameterIdentifier;
                String tmpParameterValue = parameterValue;

                if (tmpPlatformParameterIdentifier
                        .equals("BOOLEAN_PARAMETER_TRUE")) {
                    tmpPlatformParameterIdentifier = "BOOLEAN_PARAMETER";
                    tmpParameterValue = "true";
                }
                if (tmpPlatformParameterIdentifier
                        .equals("BOOLEAN_PARAMETER_FALSE")) {
                    tmpPlatformParameterIdentifier = "BOOLEAN_PARAMETER";
                    tmpParameterValue = "false";
                }

                Query query = mgr.createQuery(
                        "select c from ParameterDefinition c where c.dataContainer.parameterId=:parameterId");
                query.setParameter("parameterId",
                        tmpPlatformParameterIdentifier);
                // -- get parameter definition --
                final List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

                Iterator<ParameterDefinition> parameterDefinitionIterator = ParameterizedTypes
                        .iterator(query.getResultList(),
                                ParameterDefinition.class);

                while (parameterDefinitionIterator.hasNext()) {
                    parameterDefinitions
                            .add(parameterDefinitionIterator.next());
                }
                // -- get parameter definition end --

                Parameter parameter = new Parameter();
                parameter.setParameterDefinition(parameterDefinitions.get(0));
                parameter.setValue(tmpParameterValue);
                // -- get parameter set --
                query = mgr.createQuery(
                        "select c from ParameterSet c where c.version=:version");
                query.setParameter("version",
                        Integer.valueOf(parameterSetVersion));

                final List<ParameterSet> parameterSetArray = new ArrayList<>();
                Iterator<ParameterSet> parameterSetIterator = ParameterizedTypes
                        .iterator(query.getResultList(), ParameterSet.class);
                while (parameterSetIterator.hasNext()) {
                    parameterSetArray.add(parameterSetIterator.next());
                }
                // --
                parameter.setParameterSet(parameterSetArray.get(0));
                updateHistoryModDate(parameter, date);
                mgr.persist(parameter);
                mgr.flush();

                return parameter;
            }
        });
        return result;
    }

    /**
     * Helper method for inserting priced option.
     * 
     * @param pricePerUser
     *            Price per user.
     * @param pricePerSubscription
     *            Price per subscription.
     * @param pricedParameter
     *            Priced parameter object.
     * @param date
     * @return Inserted priced option object.
     * 
     * @throws Exception
     */
    protected PricedOption createPricedOption(final BigDecimal pricePerUser,
            final BigDecimal pricePerSubscription,
            final PricedParameter pricedParameter, final Date date)
            throws Exception {
        final PricedOption result = runTX(new Callable<PricedOption>() {
            @Override
            public PricedOption call() throws Exception {

                PricedOption pricedOption = new PricedOption();

                pricedOption.setPricedParameter(pricedParameter);

                Query query = mgr.createQuery(
                        "select c from ParameterOption c where c.version=:version");
                query.setParameter("version", Integer.valueOf(0));

                final List<ParameterOption> parameterOptionArray = new ArrayList<>();
                Iterator<ParameterOption> parameterOptionIterator = ParameterizedTypes
                        .iterator(query.getResultList(), ParameterOption.class);
                while (parameterOptionIterator.hasNext()) {
                    parameterOptionArray.add(parameterOptionIterator.next());
                }
                final long parameterOptionKey = parameterOptionArray.get(0)
                        .getKey();
                pricedOption.setParameterOptionKey(parameterOptionKey);

                pricedOption.setPricePerUser(pricePerUser);
                pricedOption.setPricePerSubscription(pricePerSubscription);
                updateHistoryModDate(pricedOption, date);
                mgr.persist(pricedOption);
                mgr.flush();
                return pricedOption;
            }
        });
        return result;
    }

    /**
     * Helper method for clearing before inserted data for priced parameter and
     * priced options.
     * 
     * @throws Exception
     */
    protected void clearParametersAndOptions() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query query = mgr.createQuery("delete from PricedOption");
                query.executeUpdate();
                query = mgr.createQuery("delete from PricedParameter");
                query.executeUpdate();
                query = mgr.createQuery("delete from Parameter");
                query.executeUpdate();
                return null;
            }
        });
    }

    /**
     * Helper method for deleting parameter option history.
     * 
     * @throws Exception
     */
    protected void deleteParameterOptionHistory() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query query = mgr
                        .createQuery("delete from ParameterOptionHistory");
                query.executeUpdate();
                return null;
            }
        });
    }

    /**
     * Helper method for deleting parameter definition history.
     * 
     * @throws Exception
     */
    protected void deleteParameterDefinitionHistory() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query query = mgr
                        .createQuery("delete from ParameterDefinitionHistory");
                query.executeUpdate();
                return null;
            }
        });
    }

    protected PlatformUser createUserObject(Organization organization,
            String userId) {
        PlatformUser user = new PlatformUser();
        user.setOrganization(organization);
        user.setUserId(userId);
        user.setEmail(userId + "@" + userId + ".com");
        user.setStatus(UserAccountStatus.ACTIVE);
        user.setLocale("en");
        return user;
    }

    /**
     * Get the first millisecond of the previous month.
     * 
     * @param testYear
     * @param testMonth
     * 
     * @return the first millisecond of the previous month.
     */
    protected Date getStartDate(int testYear, int testMonth) {
        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, testYear);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.MONTH, testMonth);

        cal.add(Calendar.MONTH, -1);

        return cal.getTime();
    }

    protected void setBaseDataCreationDate() {
        final Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, baseDataCreationYear);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.MONTH, 1);

        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    /**
     * Get the first millisecond of the current month.
     * 
     * @param testYear
     * @param testMonth
     * 
     * @return the first millisecond of the current month.
     */
    protected Date getEndDate(int testYear, int testMonth) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, testYear);
        cal.set(Calendar.MONTH, testMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns a calendar object for the given settings, considering the month
     * before the specified one.
     * 
     * @param testYear
     *            The year.
     * @param testMonth
     *            The month.
     * @param day
     *            The day.
     * @param hour
     *            The hour.
     * @param minute
     *            The minute.
     * @return A calendar representing the given date settings in the last
     *         month.
     */
    protected Calendar getCal(int testYear, int testMonth, int day, int hour,
            int minute) {
        // set to calendar to the first day of the previous for tested month
        // 8:00
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, testYear);
        cal.set(Calendar.MONTH, testMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        cal.add(Calendar.MONTH, -1);

        return cal;
    }

    /**
     * Returns a date matching the specified settings in the month before to the
     * specified one.
     * 
     * @param testYear
     *            The year.
     * @param testMonth
     *            The month.
     * @param day
     *            The day.
     * @param hour
     *            The hour.
     * @param minute
     *            The minute.
     * @return The date object representing the specified settings in the
     *         preceding month.
     */
    protected Date getDate(int testYear, int testMonth, int day, int hour,
            int minute) {
        return getCal(testYear, testMonth, day, hour, minute).getTime();
    }

    protected Date getDate(int testYear, int testMonth, int day, int hour,
            int minute, int sec) {
        Calendar cal = getCal(testYear, testMonth, day, hour, minute);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected double getFraction(PricingPeriod pricingPeriod, Date start,
            Date end, int paramTestYear, int paramTestMonth) {
        double dt = end.getTime() - start.getTime();
        double divisor = getEndDate(paramTestYear, paramTestMonth).getTime()
                - getStartDate(paramTestYear, paramTestMonth).getTime();
        if (pricingPeriod == PricingPeriod.WEEK) {
            divisor = 7 * 24 * 3600 * 1000;
        } else if (pricingPeriod == PricingPeriod.DAY) {
            divisor = 24 * 3600 * 1000;
        }
        double fraction = dt / divisor;

        return fraction;
    }

    protected void startBillingRun(final long billingTime) throws Exception {
        long time = System.currentTimeMillis();
        // login
        container.login(platformUserKey, ROLE_SERVICE_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                serviceBill.startBillingRun(billingTime);
                return null;
            }
        });
        double dt = (System.currentTimeMillis() - time) / 1000.0;
        System.out.println("The billing run took " + dt + " seconds.");
    }

    protected Document getBillingDocumentAndVerifyPeriod(final String subId,
            final int paramTestMonth, final Long startTime, final Long endTime)
            throws Exception {
        return runTX(new Callable<Document>() {
            @Override
            public Document call() throws Exception {
                Query query = mgr.createQuery(
                        "SELECT br FROM BillingResult br, SubscriptionHistory sub WHERE br.dataContainer.organizationTKey = :organizationTKey AND sub.dataContainer.subscriptionId = :subscriptionId AND br.dataContainer.subscriptionKey = sub.objKey ORDER BY br.dataContainer.periodEndTime DESC");
                query.setParameter("organizationTKey",
                        Long.valueOf(customerTkey));
                query.setParameter("subscriptionId", subId);
                query.setMaxResults(1);
                BillingResult billingResult = (BillingResult) query
                        .getSingleResult();

                // change the flag to true to print the XML document
                if (true) {
                    System.out.println(billingResult.getResultXML());
                }

                long periodStartTime = getStartDate(testYear, paramTestMonth)
                        .getTime();
                if (startTime != null) {
                    periodStartTime = startTime.longValue();
                }

                long periodEndTime = getEndDate(testYear, paramTestMonth)
                        .getTime();
                if (endTime != null) {
                    periodEndTime = endTime.longValue();
                }

                Assert.assertEquals("wrong start value for period",
                        periodStartTime, billingResult.getPeriodStartTime());
                Assert.assertEquals("Wrong end value for period", periodEndTime,
                        billingResult.getPeriodEndTime());

                Document doc = XMLConverter
                        .convertToDocument(billingResult.getResultXML(), true);
                Assert.assertNotNull("The billing document must not be null",
                        doc);

                return doc;
            }
        });
    }

    /**
     * Verify the XML document for the default subscription.
     * 
     * @param periods
     *            the expected billed periods
     * @param expectedPrice
     *            the expected price
     * @param paramTestMonth
     * @throws Exception
     */
    protected void verify(Date periods[][], BigDecimal expectedPrice,
            BigDecimal discountAmount, int paramTestMonth) throws Exception {
        verify(SUBSCRIPTION_ID, periods, expectedPrice, discountAmount,
                paramTestMonth);
    }

    /**
     * Verify the XML document for the default subscription.
     * 
     * @param periods
     *            the expected billed periods
     * @param expectedPrice
     *            the expected price
     * @param paramTestMonth
     * @throws Exception
     */
    protected void verify(Date periods[][], BigDecimal expectedPrice,
            int paramTestMonth) throws Exception {
        verify(SUBSCRIPTION_ID, periods, expectedPrice, paramTestMonth);
    }

    /**
     * Verify the billing XML document.
     * 
     * @param subId
     *            the id of the subscription which is verified
     * @param periods
     *            the expected billed periods
     * @param expectedPrice
     *            the expected price
     * @param paramTestMonth
     *            The month to test for.
     * @throws Exception
     */
    protected void verify(String subId, Date periods[][],
            BigDecimal expectedPrice, int paramTestMonth) throws Exception {
        verify(subId, periods, expectedPrice, BigDecimal.ZERO, paramTestMonth,
                null, null);
    }

    /**
     * Verify the billing XML document.
     * 
     * @param subId
     *            the id of the subscription which is verified
     * @param periods
     *            the expected billed periods
     * @param expectedPrice
     *            the expected price
     * @param paramTestMonth
     *            The month to test for.
     * @throws Exception
     */
    protected void verify(String subId, Date periods[][],
            BigDecimal expectedPrice, BigDecimal discounAmount,
            int paramTestMonth) throws Exception {
        verify(subId, periods, expectedPrice, discounAmount, paramTestMonth,
                null, null);
    }

    protected void verify(Document doc, String subId, Date periods[][],
            BigDecimal expectedPrice, BigDecimal discountAmount)
            throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        BigDecimal price = BigDecimal.ZERO;
        int periodsIdx = periods.length;

        NodeList subscriptions = doc.getElementsByTagName("Subscription");
        for (int i = 0; i < subscriptions.getLength(); i++) {
            Element sub = (Element) subscriptions.item(i);
            String id = sub.getAttribute("id");
            if (!subId.equals(id)) {
                // we only verify one subscription
                continue;
            }
            NodeList priceModels = sub.getElementsByTagName("PriceModel");
            for (int j = 0; j < priceModels.getLength(); j++) {
                NodeList nodeList = priceModels.item(j).getChildNodes();
                for (int k = 0; k < nodeList.getLength(); k++) {
                    Node node = nodeList.item(k);
                    if (node.getNodeName().equals("UsagePeriod")) {
                        periodsIdx--;
                        Assert.assertTrue("Too many periods found.",
                                periodsIdx >= 0);

                        Element period = (Element) node;
                        Long startDate = Long
                                .valueOf(period.getAttribute("startDate"));
                        Long endDate = Long
                                .valueOf(period.getAttribute("endDate"));
                        String start = sdf.format(startDate);
                        String end = sdf.format(endDate);
                        Assert.assertEquals(
                                "Period " + (periodsIdx + 1)
                                        + " wrong startDate:",
                                sdf.format(periods[periodsIdx][0]), start);
                        Assert.assertEquals(
                                "Period " + (periodsIdx + 1)
                                        + " wrong endDate;",
                                sdf.format(periods[periodsIdx][1]), end);
                    } else if (node.getNodeName().equals("PriceModelCosts")) {
                        price = price.add(new BigDecimal(
                                ((Element) node).getAttribute("amount")));
                    }
                }
            }

        }
        Assert.assertEquals("Too little periods found.", 0, periodsIdx);
        checkEquals("Wrong price:", expectedPrice, price, 2);

        String currency = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@currency");
        Assert.assertEquals("Wrong currency set for biling result", "EUR",
                currency);

        // verify overall cost section in the result xml. If there are more
        // subscriptions, skip the check, as it will be called for every
        // subscription
        BigDecimal expectedPriceWithDiscount = expectedPrice
                .subtract(discountAmount)
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                        RoundingMode.HALF_UP);
        NodeList subs = XMLConverter.getNodeListByXPath(doc,
                "/BillingDetails/Subscriptions/Subscription");
        if (subs != null && subs.getLength() < 2) {
            String overallCosts = XMLConverter.getNodeTextContentByXPath(doc,
                    "/BillingDetails/OverallCosts/@grossAmount");
            checkEquals("Wrong overall costs found",
                    expectedPriceWithDiscount.toPlainString(), overallCosts);
        }

    }

    /**
     * Verify the billing XML document.
     * 
     * @param subId
     *            the id of the subscription which is verified
     * @param periods
     *            the expected billed periods
     * @param expectedPrice
     *            the expected price
     * @param paramTestMonth
     *            The month to test in.
     * @param startTime
     *            Intended start time for the check of the billing period. If
     *            <code>null</code>, the begin of the given month in the test
     *            year is used.
     * @param endTime
     *            Intended end time for the check of the billing period. If
     *            <code>null</code>, the end of the given month in the test year
     *            is used.
     * @throws Exception
     */
    protected void verify(String subId, Date periods[][],
            BigDecimal expectedPrice, BigDecimal discountAmount,
            int paramTestMonth, Long startTime, Long endTime) throws Exception {

        Document doc = getBillingDocumentAndVerifyPeriod(subId, paramTestMonth,
                startTime, endTime);
        verify(doc, subId, periods, expectedPrice, discountAmount);

    }

    protected void subRevokeUser(String userId, Date date) throws Exception {
        subRevokeUser(SUBSCRIPTION_ID, userId, date);
    }

    protected void subRevokeUser(final String subId, final String userId,
            final Date date) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Subscription sub = getSubscription(subId);

                PlatformUser user = new PlatformUser();
                user.setUserId(userId);
                user = mgr.find(user);

                for (UsageLicense license : sub.getUsageLicenses()) {
                    if (license.getUser().getUserId().equals(userId)) {
                        updateHistoryModDate(license, date);

                        sub.revokeUser(user);
                        updateHistoryModDate(sub, date);
                        mgr.remove(license);

                        return null;
                    }
                }

                return null;
            }
        });
    }

    /**
     * Updates the priced events for the given subscription, and also sets the
     * modification date of the corresponding history entries to the given date.
     * 
     * <p>
     * If the price per period or the price per user assignment are 0, the
     * current value will not be changed.
     * </p>
     * 
     * @param priceForLogin
     *            The costs for a login event.
     * @param priceForUpload
     *            The costs for an upload event.
     * @param priceForLogout
     *            The costs for a logout event.
     * @param dateToSet
     *            The date to be set as modification date for the history
     *            entries.
     * @param priceForPeriod
     *            The price for the period
     * @param priceForUserAssignment
     *            The price for a user assignment
     * @throws Exception
     */
    protected void updSubscriptionPrices(final BigDecimal priceForLogin,
            final BigDecimal priceForUpload, final BigDecimal priceForLogout,
            final Date dateToSet, final BigDecimal priceForPeriod,
            final BigDecimal priceForUserAssignment) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                // find the subscription
                Subscription sub = new Subscription();
                sub.setOrganizationKey(Organizations
                        .findOrganization(mgr, customerId).getKey());
                sub.setSubscriptionId(SUBSCRIPTION_ID);
                sub = (Subscription) mgr.find(sub);

                List<PricedEvent> consideredEvents = sub.getProduct()
                        .getPriceModel().getConsideredEvents();
                List<PricedEvent> eventCopy = new ArrayList<>();
                eventCopy.addAll(consideredEvents);
                for (PricedEvent pEvent : eventCopy) {
                    if (pEvent.getEvent().getEventIdentifier().equals(
                            PlatformEventIdentifier.USER_LOGIN_TO_SERVICE)) {
                        pEvent.setEventPrice(priceForLogin);
                    }
                    if (pEvent.getEvent().getEventIdentifier().equals(
                            PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE)) {
                        pEvent.setEventPrice(priceForLogout);
                    }
                    if (pEvent.getEvent().getEventIdentifier()
                            .equals(SERVICE_EVENT_FILE_UPLOAD)) {
                        pEvent.setEventPrice(priceForUpload);
                    }

                    if (BigDecimalComparator.isZero(pEvent.getEventPrice())) {
                        updateHistoryModDate(pEvent, dateToSet);
                        mgr.remove(pEvent);
                        sub.getProduct().getPriceModel().getConsideredEvents()
                                .remove(pEvent);
                    } else {
                        updateHistoryModDate(pEvent, dateToSet);
                    }
                }

                if (priceForPeriod == null && priceForUserAssignment == null) {
                    return null;
                } else {
                    PriceModel priceModel = sub.getProduct().getPriceModel();

                    if (priceForPeriod != null
                            && priceForPeriod.longValue() != 0) {
                        priceModel.setPricePerPeriod(priceForPeriod);
                    }
                    if (priceForUserAssignment != null
                            && priceForUserAssignment.longValue() != 0) {
                        priceModel.setPricePerUserAssignment(
                                priceForUserAssignment);
                    }
                    updateHistoryModDate(priceModel, dateToSet);
                    mgr.flush();
                }
                return null;
            }
        });
    }

    protected Subscription updSub(SubscriptionStatus status, Date date)
            throws Exception {
        return updSub(SUBSCRIPTION_ID, status, date);
    }

    protected Subscription updSub(final String subId,
            final SubscriptionStatus status, final Date date) throws Exception {
        Subscription currentSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                Subscription sub = getSubscription(subId);

                sub.setStatus(status);
                if (SubscriptionStatus.ACTIVE == status) {
                    sub.setActivationDate(Long.valueOf(date.getTime()));
                    sub.setDeactivationDate(null);
                } else {
                    sub.setDeactivationDate(Long.valueOf(date.getTime()));
                }
                updateHistoryModDate(sub, date);

                return sub;
            }
        });

        return currentSub;
    }

    /**
     * Migrates the subscription to the product with the given product id and
     * adapts the history entries for that operation to the given date.
     * 
     * @param productId
     * @param date
     * @return The migrated subscription.
     * @throws Exception
     */
    protected Subscription updSub(final String productId, final Date date)
            throws Exception {
        return updSub(SUBSCRIPTION_ID, productId, date);
    }

    /**
     * Migrates the subscription to the product with the given id. Updates the
     * modification date in the history table using the specified date.
     * 
     * @param subId
     * @param productId
     * @param date
     * @return The migrated subscription.
     * @throws Exception
     */
    protected Subscription updSub(final String subId, final String productId,
            final Date date) throws Exception {
        Subscription currentSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                Subscription sub = getSubscription(subId);

                Product prod = new Product();
                prod.setVendor(Organizations.findOrganization(mgr, supplierId));
                prod.setProductId(productId);
                prod = (Product) mgr.find(prod);

                sub.bindToProduct(prod);
                updateHistoryModDate(sub, date);

                return sub;
            }
        });
        updateHistoryEntriesForRelatedProduct(date, currentSub);
        return currentSub;
    }

    protected void delSub(Date date) throws Exception {
        delSub(SUBSCRIPTION_ID, date);
    }

    protected void delSub(final String subId, final Date date)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Subscription sub = getSubscription(subId);
                updateHistoryModDate(sub, date);

                mgr.remove(sub);

                return null;
            }
        });
    }

    protected void subAddUser(String userId, Date date) throws Exception {
        subAddUser(SUBSCRIPTION_ID, userId, date);
    }

    protected void subAddUser(final String subId, final String userId,
            final Date date) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = getSubscription(subId);

                PlatformUser user = new PlatformUser();
                user.setUserId(userId);
                user.setOrganization(getOrganization());
                user = mgr.find(user);

                UsageLicense license = new UsageLicense();
                license.setAssignmentDate(date.getTime());
                license.setHistoryModificationTime(
                        Long.valueOf(date.getTime()));
                license.setSubscription(sub);
                license.setUser(user);
                updateHistoryModDate(license, date);

                mgr.flush();
                mgr.persist(license);

                return null;
            }
        });
    }

    /**
     * Creates a subscription in the month before the test-month. De-activates
     * and activates it, assigns users if request, which are also revoked again.
     * The subscription is not active at the end of the period. Billing period
     * is defined by the testday.
     * 
     * @param testMonth
     * @param testDay
     * @param productId
     * @param withUser
     * @param paramEtalonPrice
     * @throws Exception
     */
    protected void testInterruptedPeriod(int testMonth, int testDay,
            String productId, boolean withUser, BigDecimal paramEtalonPrice)
            throws Exception {

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(productId, getDate(testYear, testMonth, -2, 8, 0));
        if (withUser) {
            subAddUser(U_1_ID, getDate(testYear, testMonth, -2, 8, 0));
            subAddUser(U_2_ID, getDate(testYear, testMonth, 2, 8, 0));
            subRevokeUser(U_2_ID, getDate(testYear, testMonth, 11, 8, 00));
            subAddUser(U_3_ID, getDate(testYear, testMonth, 4, 14, 55));
            subRevokeUser(U_3_ID, getDate(testYear, testMonth, 5, 7, 59));
            subAddUser(U_3_ID, getDate(testYear, testMonth, 12, 8, 00));
            subAddUser(U_4_ID, getDate(testYear, testMonth, 11, 8, 0));
        }

        updSub(SubscriptionStatus.EXPIRED,
                getDate(testYear, testMonth, 5, 8, 0));
        updSub(SubscriptionStatus.EXPIRED,
                getDate(testYear, testMonth, 8, 8, 0));
        updSub(SubscriptionStatus.ACTIVE,
                getDate(testYear, testMonth, 10, 8, 0));
        updSub(SubscriptionStatus.ACTIVE,
                getDate(testYear, testMonth, 14, 8, 0));

        updSub(SubscriptionStatus.DEACTIVATED,
                getDate(testYear, testMonth, 24, 8, 0));

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] {
                { getStartDate(testYear, testMonth),
                        getDate(testYear, testMonth, 5, 8, 0) },
                { getDate(testYear, testMonth, 10, 8, 0),
                        getDate(testYear, testMonth, 24, 8, 0) } };

        verify(periods, paramEtalonPrice, testMonth);
    }

    /**
     * @param paramTestYear
     * @param paramTestMonth
     * @throws Exception
     */
    protected void createEvents(final int paramTestYear,
            final int paramTestMonth) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                GatheredEvent e;
                for (int i = 0; i < 1000; i++) {
                    e = createGatheredEventObject(EventType.PLATFORM_EVENT,
                            PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                            getDate(paramTestYear, paramTestMonth, 9, 20, i * 2,
                                    0));
                    mgr.persist(e);
                    e = createGatheredEventObject(EventType.SERVICE_EVENT,
                            SERVICE_EVENT_FILE_UPLOAD, getDate(paramTestYear,
                                    paramTestMonth, 9, 20, i * 2, 0));
                    mgr.persist(e);
                    e = createGatheredEventObject(EventType.PLATFORM_EVENT,
                            PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                            getDate(paramTestYear, paramTestMonth, 9, 20,
                                    i * 2 + 1, 0));
                    mgr.persist(e);
                }
                return null;
            }
        });
    }

    /**
     * Returns the time matching the specified params for year, month and day.
     * 
     * @param paramTestYear
     *            The year.
     * @param paramTestMonth
     *            The month.
     * @param paramTestDay
     *            The day.
     * @return The time matching the specified params in milliseconds.
     */
    protected long getBillingTime(final int paramTestYear,
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
     * Creates a subscription in the month prior to the specified one. Migrates
     * the product at the tenth of the month to product 2, creates events for it
     * and migrates it back to product 1 at the 20th of the month.
     */
    protected void testMigrationBase(int paramTestYear, int paramTestMonth,
            int paramTestDay, BigDecimal price) throws Exception {

        final long billingTime = getBillingTime(paramTestYear, paramTestMonth,
                paramTestDay);

        creSub(P_1_ID, getDate(paramTestYear, paramTestMonth, -2, 20, 0));

        // migrate to product 2
        Date tmpDateOld = getDate(paramTestYear, paramTestMonth, 10, 0, 0, 0);
        updSub(P_2_ID, tmpDateOld);

        createEvents(testYear, paramTestMonth);

        // 20:00 - 23:59 -> 120 Login Events
        // 20:00 - 23:59 -> 120 SERVICE_EVENT_FILE_UPLOAD Events

        // migrate back to product 1
        updSub(P_1_ID, getDate(paramTestYear, paramTestMonth, 20, 0, 0));

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] {
                { getStartDate(paramTestYear, paramTestMonth),
                        getDate(paramTestYear, paramTestMonth, 10, 0, 0) },
                { getDate(paramTestYear, paramTestMonth, 10, 0, 0),
                        getDate(paramTestYear, paramTestMonth, 20, 0, 0) },
                { getDate(paramTestYear, paramTestMonth, 20, 0, 0),
                        getEndDate(paramTestYear, paramTestMonth) } };

        verify(periods, price, paramTestMonth);
    }

    /**
     * Creates a subscription in the month prior to the test-month. Moreover
     * events (login and logout) are created for that month. Then expires the
     * subscription, activates and deactivates it.
     * 
     * @param testMonth
     * @param testDay
     * @param etalonPrice
     * @throws Exception
     */
    protected void testInterruptedPeriodWithManyEventsBase(int testMonth,
            int testDay, BigDecimal etalonPrice) throws Exception {

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        creSub(P_1_ID, getDate(testYear, testMonth, -2, 20, 0));

        createTestGatheredEventsObject(testMonth);

        // 20:00 - 23:59 -> 120 Login Events
        // 0:00 - 23:59 -> 720 Login Events
        // --> the 1000 - 840 = 160 Login Event belong to the billed period

        updSub(SubscriptionStatus.EXPIRED,
                getDate(testYear, testMonth, 5, 19, 59));
        updSub(SubscriptionStatus.EXPIRED,
                getDate(testYear, testMonth, 5, 20, 0));

        updSub(SubscriptionStatus.ACTIVE,
                getDate(testYear, testMonth, 10, 8, 0));
        updSub(SubscriptionStatus.DEACTIVATED,
                getDate(testYear, testMonth, 20, 20, 0));

        startBillingRun(billingTime);

        Date periods[][] = new Date[][] {
                { getStartDate(testYear, testMonth),
                        getDate(testYear, testMonth, 5, 19, 59) },
                { getDate(testYear, testMonth, 10, 8, 0),
                        getDate(testYear, testMonth, 20, 20, 0) } };

        verify(periods, etalonPrice, testMonth);
    }

    /**
     * Creates 1000 login and 1000 logout events. Not all of them are in the
     * billing period, some are created for a time before that period.
     * 
     * @param testMonth
     *            The month to create the events for.
     * @throws Exception
     */
    protected void createTestGatheredEventsObject(final int testMonth)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                GatheredEvent e;
                for (int i = 0; i < 1000; i++) {
                    e = createGatheredEventObject(EventType.PLATFORM_EVENT,
                            PlatformEventIdentifier.USER_LOGIN_TO_SERVICE,
                            getDate(testYear, testMonth, -1, 20, i * 2));
                    mgr.persist(e);
                    e = createGatheredEventObject(EventType.PLATFORM_EVENT,
                            PlatformEventIdentifier.USER_LOGOUT_FROM_SERVICE,
                            getDate(testYear, testMonth, -1, 20, i * 2 + 1));
                    mgr.persist(e);
                }
                return null;
            }
        });
    }

    /**
     * Creates two subscriptions, one based on product 1 and one based on
     * product 3. Both of them are started in the month prior to the specified
     * test month. They are also updated with a new status later on (one
     * expired, the other remains active).
     * 
     * <p>
     * A third subscription based on product 4 is created in the month, then
     * updated (to expired state) and finally deleted. So it did not exist the
     * entire period.
     * </p>
     * 
     * @param testMonth
     * @param testDay
     * @param etalonPrice1
     * @param etalonPrice2
     * @throws Exception
     */
    protected void testMultipleSubscriptionsBase(int testMonth, int testDay,
            BigDecimal etalonPrice1, BigDecimal etalonPrice2,
            String overallcosts) throws Exception {
        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        String id1 = SUBSCRIPTION_ID;
        String id2 = "sub2";
        String id3 = "sub3";

        creSub(P_1_ID, id1, getDate(testYear, testMonth, -2, 20, 0), null);

        creSub(P_3_ID, id2, getDate(testYear, testMonth, -2, 20, 0), null);
        updSub(id2, SubscriptionStatus.EXPIRED,
                getDate(testYear, testMonth, 5, 19, 59));
        updSub(id2, SubscriptionStatus.ACTIVE,
                getDate(testYear, testMonth, 10, 8, 0));

        creSub(P_4_ID, id3, getDate(testYear, testMonth, 2, 20, 0), null);
        updSub(id3, SubscriptionStatus.EXPIRED,
                getDate(testYear, testMonth, 20, 20, 00));
        delSub(id3, getDate(testYear, testMonth, 26, 20, 00));

        startBillingRun(billingTime);

        Date periods[][];
        periods = new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } };
        verify(id1, periods, new BigDecimal(overallcosts), testMonth);

        periods = new Date[][] {
                { getStartDate(testYear, testMonth),
                        getDate(testYear, testMonth, 5, 19, 59) },
                { getDate(testYear, testMonth, 10, 8, 0),
                        getEndDate(testYear, testMonth) } };

        verify(id2, periods, etalonPrice1, testMonth);

        periods = new Date[][] { { getDate(testYear, testMonth, 2, 20, 0),
                getDate(testYear, testMonth, 20, 20, 00) } };

        verify(id3, periods, etalonPrice2, testMonth);
    }

    /**
     * Creates a subscription based on product 1 in the month before the
     * specified test month. Then creates parameters for the subscription and
     * initiates the billing.
     * 
     * <p>
     * MONTH Error in ParameterOptionHistory. Exception will be thrown. But to
     * this point will not be transfered. Only problem with xml parsing will be
     * detected.
     * </p>
     * 
     * @param testMonth
     *            The test the month is started in.
     * @param testDay
     *            The day the test is started at.
     * @param parametersAndOptionsCosts
     *            The expected overall costs.
     * @param errorSituation
     * 
     * @throws Exception
     */
    protected void testSimplePeriod1BeginOfJanuaryBase(int testMonth,
            int testDay, BigDecimal parametersAndOptionsCosts,
            String errorSituation) throws Exception {

        final long billingTime = getBillingTime(testYear, testMonth, testDay);

        Subscription subscription = creSub(P_1_ID,
                getDate(testYear, testMonth, -2, 8, 0));

        prepareParametersAndOptionsBase(testMonth, subscription,
                errorSituation);
        startBillingRun(billingTime);

        verify(new Date[][] { { getStartDate(testYear, testMonth),
                getEndDate(testYear, testMonth) } },
                P_1_PRICE_PER_PERIOD.add(parametersAndOptionsCosts), testMonth);

        clearParametersAndOptions();
    }

    protected void importProduct(final String xml, final DataService mgr)
            throws Exception {
        ProductImportParser parser = new ProductImportParser(mgr,
                mgr.getCurrentUser().getOrganization());
        parser.parse(xml.getBytes("UTF-8"));

    }

    private BigDecimal getDurationParamCosts(long price, long value,
            BigDecimal periodFactor) {
        // add duration param costs
        BigDecimal paramPrice = BigDecimal.valueOf(price);
        BigDecimal durationParamCosts = BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(86400000L), BIGDECIMAL_SCALE,
                        RoundingMode.HALF_UP)
                .multiply(paramPrice).multiply(periodFactor);
        return durationParamCosts;
    }

    /**
     * Subscriptions with zero costs and a defined discount value must not
     * result in an exception.
     */
    @Test
    public void bug9404() throws Exception {
        final int testMonth = Calendar.APRIL;
        final int testDay = 1;
        final long billingTime = getBillingTime(testYear, testMonth, testDay);
        final Date date = getDate(testYear, testMonth - 3, testDay, 0, 0);
        final Date deactivationDate = getDate(testYear, testMonth - 3, testDay,
                0, 0);
        creSub(P_1_ID, SUBSCRIPTION_ID, date, null);
        defineDiscount(SUBSCRIPTION_ID, new BigDecimal(40));
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.ACTIVE, date);
        updSub(SUBSCRIPTION_ID, SubscriptionStatus.DEACTIVATED,
                deactivationDate);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                startBillingRun(billingTime);
                return null;
            }
        });
    }

    private void defineDiscount(final String subscriptionId,
            final BigDecimal discnt) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = getSubscription(subscriptionId);
                Organization organization = subscription.getOrganization();
                Discount discount = new Discount();
                discount.setOrganizationReference(
                        organization.getSources().get(0));
                discount.setValue(discnt);
                discount.setStartTime(Long.MIN_VALUE);
                discount.setEndTime(Long.MAX_VALUE);
                mgr.persist(discount);
                mgr.flush();
                return null;
            }
        });
    }

}
