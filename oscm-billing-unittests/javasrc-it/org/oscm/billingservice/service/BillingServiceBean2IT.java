/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 12.02.2009                                                      
 *                                                                              
 *  Completion Time: 27.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.oscm.test.Numbers.BD10;
import static org.oscm.test.Numbers.BD100;
import static org.oscm.test.Numbers.BD110;
import static org.oscm.test.Numbers.BD12;
import static org.oscm.test.Numbers.BD120;
import static org.oscm.test.Numbers.BD13;
import static org.oscm.test.Numbers.BD130;
import static org.oscm.test.Numbers.BD14;
import static org.oscm.test.Numbers.BD140;
import static org.oscm.test.Numbers.BD15;
import static org.oscm.test.Numbers.BD16;
import static org.oscm.test.Numbers.BD2;
import static org.oscm.test.Numbers.BD20;
import static org.oscm.test.Numbers.BD25;
import static org.oscm.test.Numbers.BD3;
import static org.oscm.test.Numbers.BD30;
import static org.oscm.test.Numbers.BD4;
import static org.oscm.test.Numbers.BD5;
import static org.oscm.test.Numbers.BD6;
import static org.oscm.test.Numbers.BD60;
import static org.oscm.test.Numbers.BD7;
import static org.oscm.test.Numbers.BD70;
import static org.oscm.test.Numbers.BD8;
import static org.oscm.test.Numbers.BD80;
import static org.oscm.test.Numbers.BD9;
import static org.oscm.test.Numbers.BD90;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.Query;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.After;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorLocal;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Discount;
import org.oscm.domobjects.GatheredEvent;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.domobjects.UsageLicense;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.interceptor.DateFactory;
import org.oscm.test.EJBTestBase;
import org.oscm.test.Numbers;
import org.oscm.test.TestDateFactory;
import org.oscm.test.XMLTestValidator;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.Scenario;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.VatRates;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.types.constants.BillingResultXMLTags;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

@SuppressWarnings("boxing")
public class BillingServiceBean2IT extends EJBTestBase {

    private static final int HOUR_IN_MILLIS = 3600000;
    private BillingServiceLocal serviceBill;
    private XMLTestValidator xmlValidator;
    private long startTime;
    private DataService dm;
    private List<List<TriggerProcessParameter>> collectedParams = new ArrayList<List<TriggerProcessParameter>>();
    private List<List<Organization>> receivingOrgs = new ArrayList<List<Organization>>();
    private SupportedCurrency usd;
    private SupportedCurrency jpy;

    protected BillingContact bc;

    @Override
    public void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);

        startTime = System.currentTimeMillis();
        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub() {
            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey informationId, String contextId) {
                if (informationId == ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET) {
                    ConfigurationSetting c = new ConfigurationSetting();
                    c.setValue("0");
                    return c;
                }
                return super.getConfigurationSetting(informationId, contextId);
            }
        });
        container.addBean(new LocalizerServiceBean());
        SharesCalculatorLocal sharesCalculator = mock(SharesCalculatorLocal.class);
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
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messages, PlatformUser currentUser) {
                for (TriggerMessage triggerMessage : messages) {
                    collectedParams.add(triggerMessage.getParams());
                    receivingOrgs.add(triggerMessage.getReceiverOrgs());
                }
            }
        });
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new RevenueCalculatorBean());
        container.addBean(new BillingServiceBean());

        serviceBill = container.get(BillingServiceLocal.class);
        dm = container.get(DataService.class);
        xmlValidator = new XMLTestValidator();
        xmlValidator.setup();

        setBaseDataCreationDate();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCountries.createSomeSupportedCountries(dm);
                Scenario.setup(container, true);

                usd = new SupportedCurrency();
                usd.setCurrency(Currency.getInstance("USD"));
                dm.persist(usd);

                jpy = new SupportedCurrency();
                jpy.setCurrency(Currency.getInstance("JPY"));
                dm.persist(jpy);

                PaymentType type = new PaymentType();
                type.setPaymentTypeId(INVOICE);
                type = (PaymentType) dm.getReferenceByBusinessKey(type);
                PaymentInfo pi = PaymentInfos.createPaymentInfo(
                        Scenario.getCustomer(), dm, type);
                bc = PaymentInfos.createBillingContact(dm,
                        Scenario.getCustomer());
                Subscription sub = Scenario.getSubscription();
                sub.setPaymentInfo(pi);
                sub.setBillingContact(bc);
                return null;
            }
        });

        container.login(Scenario.getSupplierAdminUser().getKey());
    }

    protected void setBaseDataCreationDate() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(1);
        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    @Override
    @After
    public void tearDown() throws Exception {
        // validation of XML
        xmlValidator.validateBillingResultXML();
    }

    @Test
    public void testPerformBillingResult_FreePeriod() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = dm.getReference(Subscription.class, Scenario
                        .getSubscription().getKey());
                sub.getPriceModel().setFreePeriod(10);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = dm.getReference(Subscription.class, Scenario
                        .getSubscription().getKey());
                sub.setStatus(SubscriptionStatus.DEACTIVATED);
                return null;
            }
        });

        // add 15 ms to ensure in Windows different time stamp now comparing to
        // scenario setup (BES queries exclude the end billing period):
        Thread.sleep(15);
        long endDate = System.currentTimeMillis();
        long startDate = getStartDateOfMonth(endDate);

        List<BillingResult> billingResults = serviceBill
                .generateBillingForAnyPeriod(startDate, endDate, Scenario
                        .getCustomer().getKey());
        assertEquals(
                "Subscription is completely free, no billing results expected",
                0, billingResults.size());
    }

    @Test
    public void testPerformBillingResult_CheckBillingResultKeySet()
            throws Exception {

        BillingResult billingResult = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);

        assertTrue(billingResult.getKey() == 0);

        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);

        assertEquals(billingResult.getKey(),
                XMLConverter.getNumberByXPath(doc, "/BillingDetails/@key")
                        .longValue());
        validateOrganizationDetails(PaymentType.INVOICE, bc, doc);
    }

    @Test
    public void testPerformBillingResult_CheckBillingResultKeyNotSet()
            throws Exception {

        BillingResult billingResult = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);

        assertTrue(billingResult.getKey() == 0);

        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);

        assertNull(XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/@key"));
        validateOrganizationDetails(PaymentType.INVOICE, bc, doc);
    }

    @Test
    public void testPerformBillingResult_PaymentInfoChanged() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization org = dm.getReference(Organization.class, Scenario
                        .getCustomer().getKey());
                PaymentType pt = new PaymentType();
                pt.setPaymentTypeId(PaymentType.CREDIT_CARD);
                pt = (PaymentType) dm.getReferenceByBusinessKey(pt);
                PaymentInfo pi = PaymentInfos.createPaymentInfo(org, dm, pt);
                Subscription s = dm.getReference(Subscription.class, Scenario
                        .getSubscription().getKey());
                s.setPaymentInfo(pi);
                return null;
            }
        });
        BillingResult billingResult = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);
        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);
        validateOrganizationDetails(PaymentType.CREDIT_CARD, bc, doc);
    }

    @Test
    public void testPerformBillingResult_BillingContactChanged()
            throws Exception {
        BillingContact bcMod = runTX(new Callable<BillingContact>() {

            @Override
            public BillingContact call() throws Exception {
                Subscription s = dm.getReference(Subscription.class, Scenario
                        .getSubscription().getKey());
                BillingContact bc = s.getBillingContact();
                bc.setAddress("another adress");
                bc.setEmail("billing@company.de");
                dm.flush();
                return bc;
            }
        });
        BillingResult billingResult = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);
        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);
        validateOrganizationDetails(PaymentType.INVOICE, bcMod, doc);
    }

    @Test
    public void testPerformBillingResult_NewBillingContactAndOldChanged()
            throws Exception {
        BillingContact newBc = runTX(new Callable<BillingContact>() {

            @Override
            public BillingContact call() throws Exception {
                Subscription s = dm.getReference(Subscription.class, Scenario
                        .getSubscription().getKey());
                BillingContact bc = s.getBillingContact();
                bc.setAddress("another adress");
                bc.setEmail("billing@company.de");
                dm.flush();

                Thread.sleep(1);
                BillingContact newBc = PaymentInfos.createBillingContact(dm,
                        s.getOrganization());
                s.setBillingContact(newBc);
                return newBc;
            }
        });
        BillingResult billingResult = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);
        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);
        validateOrganizationDetails(PaymentType.INVOICE, newBc, doc);
    }

    @Test
    public void testPerformBillingResult_PaymentTypeChanged() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                PaymentType pt = new PaymentType();
                pt.setPaymentTypeId(PaymentType.DIRECT_DEBIT);
                pt = (PaymentType) dm.getReferenceByBusinessKey(pt);
                Subscription s = dm.getReference(Subscription.class, Scenario
                        .getSubscription().getKey());
                s.getPaymentInfo().setPaymentType(pt);
                return null;
            }
        });

        BillingResult billingResult = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);
        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);
        validateOrganizationDetails(PaymentType.DIRECT_DEBIT, bc, doc);
    }

    @Test
    public void testPerformBillingResultForCustomer_CheckUDAContent()
            throws Exception {
        BillingResult result = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);

        // assert UDAs are contained - for customer
        Document doc = XMLConverter.convertToDocument(result.getResultXML(),
                false);
        validateOrganizationDetails(PaymentType.INVOICE, bc, doc);
        assertEquals(
                1,
                XMLConverter.getNumberByXPath(doc,
                        "count(/BillingDetails/OrganizationDetails/Udas)")
                        .intValue());
        assertEquals(
                2,
                XMLConverter.getNumberByXPath(doc,
                        "count(/BillingDetails/OrganizationDetails/Udas/Uda)")
                        .intValue());
        assertEquals("UdaDefCust1", XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OrganizationDetails/Udas/Uda[1]/@id"));
        assertEquals("UdaCust1_Value", XMLConverter.getNodeTextContentByXPath(
                doc, "/BillingDetails/OrganizationDetails/Udas/Uda[1]/@value"));
        assertEquals("UdaDefCust2", XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OrganizationDetails/Udas/Uda[2]/@id"));
        assertEquals("UdaCust2_Value", XMLConverter.getNodeTextContentByXPath(
                doc, "/BillingDetails/OrganizationDetails/Udas/Uda[2]/@value"));

        // assert UDAs are contained - for subscription
        assertEquals(
                1,
                XMLConverter.getNumberByXPath(doc,
                        "count(/BillingDetails/Subscriptions)").intValue());
        assertEquals(
                1,
                XMLConverter.getNumberByXPath(doc,
                        "count(/BillingDetails/Subscriptions/Subscription)")
                        .intValue());
        assertEquals(
                1,
                XMLConverter
                        .getNumberByXPath(doc,
                                "count(/BillingDetails/Subscriptions/Subscription/Udas)")
                        .intValue());
        assertEquals(
                2,
                XMLConverter
                        .getNumberByXPath(doc,
                                "count(/BillingDetails/Subscriptions/Subscription/Udas/Uda)")
                        .intValue());
        assertEquals("UdaDefSub1", XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/Subscriptions/Subscription/Udas/Uda[1]/@id"));
        assertEquals(
                "UdaSub_Value1",
                XMLConverter
                        .getNodeTextContentByXPath(doc,
                                "/BillingDetails/Subscriptions/Subscription/Udas/Uda[1]/@value"));
        assertEquals("UdaDefSub2", XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/Subscriptions/Subscription/Udas/Uda[2]/@id"));
        assertEquals(
                "UdaSub_Value2",
                XMLConverter
                        .getNodeTextContentByXPath(doc,
                                "/BillingDetails/Subscriptions/Subscription/Udas/Uda[2]/@value"));
    }

    /**
     * Test the price model costs. Define a vat rate. The net and gross amount
     * must be calculated correctly. This test cases uses a vat of 100% to
     * simplify calculations (unfortunately the values change for each run).
     * 
     * @throws Exception
     */
    @Test
    public void testPriceModelCosts_vat100Percent() throws Exception {
        // set default VAT for supplier
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier = Scenario.getSupplier();
                supplier = (Organization) dm
                        .getReferenceByBusinessKey(supplier);
                Organization customer = Scenario.getCustomer();
                customer = (Organization) dm
                        .getReferenceByBusinessKey(customer);

                // vat
                VatRates.createVatRate(dm, supplier, BigDecimal.valueOf(100),
                        null, null);

                dm.flush();
                return null;
            }
        });

        BillingResult result = serviceBill.generateBillingForAnyPeriod(
                startTime, System.currentTimeMillis() + 1,
                Scenario.getCustomer().getKey()).get(0);
        System.out.println(result.getResultXML());

        Document doc = XMLConverter.convertToDocument(result.getResultXML(),
                false);

        Double netAmount = Double.valueOf(XMLConverter
                .getNodeTextContentByXPath(doc,
                        "/BillingDetails/OverallCosts/@netAmount"));

        assertEquals(Double.valueOf(netAmount.doubleValue() * 2),
                Double.valueOf(XMLConverter.getNodeTextContentByXPath(doc,
                        "/BillingDetails/OverallCosts/@grossAmount")));
    }

    /**
     * 50% discount and 19% VAT, Bug 9251
     * 
     * @throws Exception
     */
    @Test
    public void testPriceModelCosts_discount50PercentVAT19Percent()
            throws Exception {
        // set default VAT for supplier
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization supplier = Scenario.getSupplier();
                supplier = (Organization) dm
                        .getReferenceByBusinessKey(supplier);
                Organization customer = Scenario.getCustomer();
                customer = (Organization) dm
                        .getReferenceByBusinessKey(customer);

                // vat
                VatRates.createVatRate(dm, supplier, BigDecimal.valueOf(19),
                        null, null);

                // discount
                Discount discount = new Discount();
                discount.setOrganizationReference(customer.getSources().get(0));
                discount.setValue(new BigDecimal("50"));
                Long startTime = Numbers.Long.MIN_VALUE;
                Long endTime = Numbers.Long.MAX_VALUE;
                discount.setStartTime(startTime);
                discount.setEndTime(endTime);
                dm.persist(discount);

                dm.flush();
                return null;
            }
        });

        startTime = System.currentTimeMillis();
        BillingResult result = serviceBill.generateBillingForAnyPeriod(
                startTime, startTime + 10000, Scenario.getCustomer().getKey())
                .get(0);
        System.out.println(result.getResultXML());

        Document doc = XMLConverter.convertToDocument(result.getResultXML(),
                false);

        Double actual = (Double) XMLConverter.getNumberByXPath(doc,
                "/BillingDetails/OverallCosts/@netAmount");
        Double expected = (Double) XMLConverter
                .getNumberByXPath(doc,
                        "/BillingDetails/OverallCosts/Discount/@netAmountBeforeDiscount");
        assertEquals(expected.doubleValue(), actual.doubleValue() * 2, 0.1);

        actual = (Double) XMLConverter
                .getNumberByXPath(
                        doc,
                        "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/PriceModelCosts/@amount");
        expected = (Double) XMLConverter.getNumberByXPath(doc,
                "/BillingDetails/OverallCosts/Discount/@discountNetAmount");
        assertEquals(expected.doubleValue(), actual.doubleValue() / 2, 0.1);

        expected = (Double) XMLConverter
                .getNumberByXPath(
                        doc,
                        "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/PriceModelCosts/@amount");
        actual = (Double) XMLConverter
                .getNumberByXPath(doc,
                        "/BillingDetails/OverallCosts/Discount/@netAmountAfterDiscount");
        assertEquals(expected / 2, actual, 0.1);

        assert2Digits(doc, "OverallCosts/Discount/@percent", 50.0);

        actual = (Double) XMLConverter
                .getNumberByXPath(doc,
                        "/BillingDetails/OverallCosts/Discount/@netAmountBeforeDiscount");
        expected = (Double) XMLConverter
                .getNumberByXPath(
                        doc,
                        "/BillingDetails/Subscriptions/Subscription/PriceModels/PriceModel/PriceModelCosts/@amount");
        assertEquals(expected, actual, 0.1);

        actual = (Double) XMLConverter.getNumberByXPath(doc,
                "/BillingDetails/OverallCosts/VAT/@amount");
        expected = (Double) XMLConverter
                .getNumberByXPath(doc,
                        "/BillingDetails/OverallCosts/Discount/@netAmountAfterDiscount");
        assertEquals(expected * 0.19, actual, 0.1);

        assert2Digits(doc, "OverallCosts/VAT/@percent", 19.00);
    }

    private Double assert2Digits(Document doc, String field, double expect)
            throws XPathExpressionException {
        Double actual = (Double) XMLConverter.getNumberByXPath(doc,
                "/BillingDetails/" + field);
        long exp = (long) (expect * 100.0);
        long act = (long) (actual.doubleValue() * 100.0);
        // expected might be 1 Cent lower or higher, correct the one cent
        if (exp + 1 == act) {
            exp++;
        } else if (exp - 1 == act) {
            exp--;
        }
        assertEquals(exp, act);
        return actual;
    }

    @Test
    public void testPerformBillingRunForOrganization_twoCurrencies()
            throws Exception {

        long now = System.currentTimeMillis();
        final long start = now - HOUR_IN_MILLIS;
        final long usdStart = start - HOUR_IN_MILLIS;

        initData(start, usdStart);

        List<BillingResult> result = serviceBill.generateBillingForAnyPeriod(
                usdStart, now, Scenario.getSecondCustomer().getKey());
        for (int i = result.size() - 1; i >= 0; i--) {
            Document doc = XMLConverter.convertToDocument(result.get(i)
                    .getResultXML(), false);
            String id = XMLConverter.getNodeTextContentByXPath(doc,
                    "//Subscriptions/Subscription/@id");
            if (!"newEURSub".equals(id) && !"newUSDSub".equals(id)) {
                result.remove(i);
            }
        }
        assertEquals(2, result.size());
        Document doc = XMLConverter.convertToDocument(result.get(0)
                .getResultXML(), false);
        String currency0 = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@currency");

        if ("EUR".equals(currency0)) {
            validateBillingResult(result.get(0), "EUR",
                    new BigDecimal(81346).setScale(Numbers.BIGDECIMAL_SCALE));
            validateBillingResult(result.get(1), "USD",
                    new BigDecimal(152569).setScale(Numbers.BIGDECIMAL_SCALE));
        } else {
            validateBillingResult(result.get(1), "EUR",
                    new BigDecimal(81346).setScale(Numbers.BIGDECIMAL_SCALE));
            validateBillingResult(result.get(0), "USD",
                    new BigDecimal(152569).setScale(Numbers.BIGDECIMAL_SCALE));
        }

        // and validate that all gathered events are linked to a billing result
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Query query = dm
                        .createQuery("SELECT count(ge) FROM GatheredEvent ge WHERE ge.billingResult IS NULL");
                List<Long> list = ParameterizedTypes.list(
                        query.getResultList(), Long.class);
                assertEquals(1, list.size());
                assertEquals(2, list.get(0).longValue());
                return null;
            }
        });
    }

    @Test
    public void testStartBillingRun_twoCountriesValidateNtfxs()
            throws Exception {

        // GIVEN
        collectedParams.clear();
        final long now = System.currentTimeMillis();
        final long hour = 3600000;
        final long start = now - hour;
        final long usdStart = start - hour;
        initData(start, usdStart);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.add(Calendar.MONTH, 1);

        // WHEN
        final Boolean result = runTX(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return Boolean.valueOf(serviceBill.startBillingRun(cal
                        .getTimeInMillis()));
            }
        });

        // THEN
        assertTrue(result.booleanValue());

        // assert that notifications have been sent
        Map<Long, Set<Organization>> customerToReceivers = new HashMap<Long, Set<Organization>>();
        for (List<Organization> receivers : receivingOrgs) {
            Long customerKey = Long.valueOf(receivers.get(0).getKey());
            if (!customerToReceivers.containsKey(customerKey)) {
                customerToReceivers.put(customerKey,
                        new HashSet<Organization>());
            }
            Set<Organization> receiverBag = customerToReceivers
                    .get(customerKey);
            receiverBag.add(receivers.get(0));
            receiverBag.add(receivers.get(1));
        }
        Long cust1Key = Long.valueOf(Scenario.getCustomer().getKey());
        assertTrue(customerToReceivers.containsKey(cust1Key));
        assertEquals(2, customerToReceivers.get(cust1Key).size());
        assertTrue(customerToReceivers.get(cust1Key).contains(
                Scenario.getCustomer()));
        assertTrue(customerToReceivers.get(cust1Key).contains(
                Scenario.getSupplier()));
        Long cust2Key = Long.valueOf(Scenario.getSecondCustomer().getKey());
        assertTrue(customerToReceivers.containsKey(cust2Key));
        assertEquals(2, customerToReceivers.get(cust2Key).size());
        assertTrue(customerToReceivers.get(cust2Key).contains(
                Scenario.getSecondCustomer()));
        assertTrue(customerToReceivers.get(cust2Key).contains(
                Scenario.getSupplier()));

        // parameters
        assertEquals(3, collectedParams.size());
        assertEquals(1, collectedParams.get(0).size());
        assertEquals(1, collectedParams.get(1).size());
        assertEquals(1, collectedParams.get(2).size());
        Set<String> usedCurrencies = new HashSet<String>();
        for (List<TriggerProcessParameter> params : collectedParams) {
            String currencyCode = getCurrencyCode(params.get(0).getValue(
                    String.class));
            usedCurrencies.add(currencyCode);
        }
        assertEquals(2, usedCurrencies.size());
        assertTrue(usedCurrencies.contains("USD"));
        assertTrue(usedCurrencies.contains("EUR"));
    }

    public String getCurrencyCode(String resultXml)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        String result = null;
        Document doc = XMLConverter.convertToDocument(resultXml, true);
        Node currencyAttribue = XMLConverter.getNodeByXPath(doc,
                "/BillingDetails/OverallCosts/@currency");

        if (currencyAttribue != null) {
            result = currencyAttribue.getTextContent();
        }

        return result;
    }

    @Test
    public void testPerformBillingRunForOrganization_MultipleCurrenciesComplex()
            throws Exception {
        Organization cust = initComplexScenario();
        assertNotNull(cust);

        // period start must not be 0 due to invariants check
        List<BillingResult> results = serviceBill.generateBillingForAnyPeriod(
                1, 2800000, cust.getKey());
        for (BillingResult entry : results) {
            String subscriptionId = extractSubscriptionId(entry);
            if ("sub1".equals(subscriptionId)) {
                validateBillingResult(entry, "EUR", new BigDecimal("1144.12"));
            } else if ("sub2".equals(subscriptionId)) {
                validateBillingResult(entry, "EUR", new BigDecimal("82.51"));
            } else if ("sub3".equals(subscriptionId)) {
                validateBillingResult(entry, "EUR", new BigDecimal("133.00"));
            } else if ("sub4".equals(subscriptionId)) {
                validateBillingResult(entry, "USD", new BigDecimal("37.08"));
            } else if ("sub5".equals(subscriptionId)) {
                validateBillingResult(entry, "USD", new BigDecimal("1001.12"));
            } else if ("sub6".equals(subscriptionId)) {
                validateBillingResult(entry, "JPY", new BigDecimal("164.33"));
            } else if ("sub7".equals(subscriptionId)) {
                validateBillingResult(entry, "JPY", new BigDecimal("74.12"));
            } else if ("sub8".equals(subscriptionId)) {
                validateBillingResult(entry, "JPY", new BigDecimal("14972.44"));
            }
        }
    }

    private String extractSubscriptionId(BillingResult entry) throws Exception {
        Document doc = XMLConverter.convertToDocument(entry.getResultXML(),
                false);
        String id = XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/Subscriptions/Subscription/@id");
        return id;
    }

    @Test
    public void testPerformBillingRunForOrganization_Bug6890() throws Exception {
        // if the period prices are 0, the period fee element should contain all
        // attributes nevertheless. Those are marked as mandatory in the xsd, so
        // the validation should pass.
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization cust = (Organization) dm
                        .getReferenceByBusinessKey(Scenario.getSecondCustomer());
                Subscription sub = Subscriptions.createSubscription(dm, cust
                        .getOrganizationId(), Scenario.getProduct()
                        .getProductId(), "Bug6890", 0, 0, Scenario
                        .getSupplier(), 1);
                PriceModel priceModel = sub.getPriceModel();
                priceModel.setPricePerPeriod(new BigDecimal(0));
                priceModel.setHistoryModificationTime(Long.valueOf(0));
                return null;
            }
        });
        // period start must not be 0 due to invariants check
        serviceBill.generateBillingForAnyPeriod(1, 2800000, Scenario
                .getSecondCustomer().getKey());
    }

    @Test
    public void testPerformBillingRunForOrganization_CostFreeEvents()
            throws Exception {
        // if an event is not priced but appears in the gathered event table,
        // the reference to the billing result will be stored. If the overall
        // billing costs are 0, the billing result object will be removed =>
        // this causes a constraint violation. Test behavior in this case, how
        // to avoid? reset prices
        final long currentTimeMillis = System.currentTimeMillis();
        Organization cust2 = initComplexScenario();
        initData(currentTimeMillis - 1, currentTimeMillis);
        final PriceModel priceModel = Scenario.getSubscription().getProduct()
                .getPriceModel();
        runTX(new Callable<PriceModel>() {
            @Override
            public PriceModel call() throws Exception {
                PriceModel pm = dm.getReference(PriceModel.class,
                        priceModel.getKey());
                pm.setOneTimeFee(new BigDecimal(0));
                pm.setPricePerPeriod(new BigDecimal(0));
                pm.setPricePerUserAssignment(new BigDecimal(0));
                List<PricedEvent> events = pm.getConsideredEvents();
                for (PricedEvent pe : events) {
                    pe.setEventPrice(BigDecimal.ZERO);
                    pe.setHistoryModificationTime(Long
                            .valueOf(currentTimeMillis - 1));
                }
                pm.setType(PriceModelType.PRO_RATA);
                return pm;
            }
        });

        // create gathered event
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                GatheredEvent ge = new GatheredEvent();
                ge.setActor("1000");
                ge.setEventId(Scenario.getEvent1().getEventIdentifier());
                ge.setMultiplier(1);
                ge.setOccurrenceTime(currentTimeMillis - 1);
                ge.setSubscriptionTKey(Scenario.getSubscription().getKey());
                ge.setType(Scenario.getEvent1().getEventType());
                dm.persist(ge);
                return null;
            }
        });

        // trigger billing run
        // period start must not be 0 due to invariants check
        List<BillingResult> result1 = serviceBill.generateBillingForAnyPeriod(
                1, 2800000, cust2.getKey());
        List<BillingResult> result2 = serviceBill.generateBillingForAnyPeriod(
                currentTimeMillis - 1, currentTimeMillis, Scenario
                        .getCustomer().getKey());
        List<BillingResult> result3 = serviceBill.generateBillingForAnyPeriod(
                currentTimeMillis - 1, currentTimeMillis, Scenario
                        .getSecondCustomer().getKey());

        // check results
        assertEquals(8, result1.size());
        assertEquals(1, result2.size());
        assertEquals(0, result2.get(0).getGrossAmount().longValue());
        assertEquals(1, result3.size());
        List<BillingResult> allBillingResults = getAllPersistedObjectsOfType(BillingResult.class);
        assertEquals(0, allBillingResults.size());
    }

    /**
     * Refers to bug 8887, a one time fee is defined, also a free period. The
     * billing is performed at a time greater than the sum of subscription
     * activation time and free period. The one time fee must not be considered,
     * as the subscription was deactivated in the free period.
     */
    @Test
    public void performBillingRun_OneTimeFeeAndFreePeriod() throws Exception {
        Organization customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization cust = Organizations.createCustomer(dm,
                        Scenario.getSupplier());

                final String custId = cust.getOrganizationId();
                final Product product = Scenario.getProduct();
                PriceModel priceModel = dm.getReference(PriceModel.class,
                        product.getPriceModel().getKey());
                priceModel.setOneTimeFee(BigDecimal.valueOf(50000));
                priceModel.setFreePeriod(1); // 86400000 ms
                final Organization supplier = Scenario.getSupplier();

                // subscribe
                Subscription sub = Subscriptions.createSubscription(dm, custId,
                        product.getProductId(), "sub1", 100000, 100000,
                        supplier, 1);

                // deactivate sub
                sub.setHistoryModificationTime(Long.valueOf(3600000));
                sub.setStatus(SubscriptionStatus.DEACTIVATED);
                dm.flush();

                return cust;
            }
        });

        // perform billing
        // period start must not be 0 due to invariants check
        List<BillingResult> result = serviceBill.generateBillingForAnyPeriod(1,
                86600000, customer.getKey());
        assertEquals(
                "Subscription is completely free, no billing result expected",
                0, result.size());

        List<BillingResult> allBillingResults = getAllPersistedObjectsOfType(BillingResult.class);
        assertEquals(0, allBillingResults.size());
    }

    @Test
    public void performBillingRun_UsageData() throws Exception {

        final String subscriptionId = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return Subscriptions.createSubscription(dm,
                        Scenario.getCustomer().getOrganizationId(),
                        Scenario.getProduct().getProductId(), "mySubId",
                        System.currentTimeMillis(), System.currentTimeMillis(),
                        Scenario.getSupplier(), 1).getSubscriptionId();

            }
        });

        final String custOrgId = Scenario.getCustomer().getOrganizationId();
        final String supplierOrgId = Scenario.getSupplier().getOrganizationId();

        final String userIdA = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                PlatformUser org = Organizations.createUserForOrg(dm,
                        Scenario.getCustomer(), true, "UserA");
                return org.getUserId();
            }
        });
        final String userIdB = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                PlatformUser org = Organizations.createUserForOrg(dm,
                        Scenario.getCustomer(), true, "UserB");
                return org.getUserId();
            }
        });

        final long before = System.currentTimeMillis();
        createUsageLicenseUserA(subscriptionId, custOrgId, supplierOrgId,
                before, userIdA);

        Thread.sleep(1000);
        final long in = System.currentTimeMillis();
        createUsageLicenseUserB(subscriptionId, custOrgId, supplierOrgId, in,
                userIdB);

        BillingResult billingResult = serviceBill.generateBillingForAnyPeriod(
                before + 200, System.currentTimeMillis(),
                Scenario.getCustomer().getKey()).get(0);
        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);
        NodeList nodeList = XMLConverter
                .getNodeListByXPath(
                        doc,
                        "//Subscription[@id='mySubId']/PriceModels/PriceModel/UserAssignmentCosts/@factor");
        for (int i = 0; i < nodeList.getLength(); i++) {
            assertTrue(!nodeList.item(i).getTextContent().startsWith("-"));
        }

    }

    private void createUsageLicenseUserA(final String subscriptionId,
            final String custOrgId, final String supplierOrgId,
            final long before, final String userIdA) throws Exception {
        final UsageLicense ul = runTX(new Callable<UsageLicense>() {
            @Override
            public UsageLicense call() throws Exception {
                Subscription sub = getSubscription(subscriptionId, custOrgId);
                PlatformUser user = getPlatformUser(userIdA, supplierOrgId);
                UsageLicense license = new UsageLicense();
                license.setAssignmentDate(before);
                license.setHistoryModificationTime(Long.valueOf(before));
                license.setSubscription(sub);
                license.setUser(user);
                dm.flush();
                dm.persist(license);
                return license;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UsageLicense license = dm.getReference(UsageLicense.class,
                        ul.getKey());
                license.setAssignmentDate(before);
                license.setHistoryModificationTime(Long.valueOf(before));
                license.setApplicationUserId("appuserid");
                dm.flush();
                dm.persist(license);
                return null;
            }
        });
    }

    private void createUsageLicenseUserB(final String subscriptionId,
            final String custOrgId, final String supplierOrgId, final long in,
            final String userIdB) throws Exception {

        final UsageLicense license = runTX(new Callable<UsageLicense>() {
            @Override
            public UsageLicense call() throws Exception {
                Subscription sub = getSubscription(subscriptionId, custOrgId);
                PlatformUser user = getPlatformUser(userIdB, supplierOrgId);
                UsageLicense license = new UsageLicense();
                license.setAssignmentDate(in);
                license.setHistoryModificationTime(Long.valueOf(in));
                license.setSubscription(sub);
                license.setUser(user);
                dm.flush();
                dm.persist(license);
                return license;
            }
        });
        Thread.sleep(1000);
        runTX(new Callable<UsageLicense>() {
            @Override
            public UsageLicense call() throws Exception {
                UsageLicense l = dm.getReference(UsageLicense.class,
                        license.getKey());
                dm.remove(l);
                return license;
            }
        });
    }

    // --------------------------------------------------------------------
    // internal methods

    /**
     * Initializes a complex scenario using three currencies for the customer
     * identified by the parameter customerId.
     */
    private Organization initComplexScenario() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization cust = Organizations.createCustomer(dm,
                        Scenario.getSupplier());

                final String custId = cust.getOrganizationId();
                final String productId1 = Scenario.getProduct().getProductId();
                final Organization supplier = Scenario.getSupplier();

                Subscription sub1 = Subscriptions
                        .createSubscription(dm, custId, productId1, "sub1",
                                100000, 100000, supplier, 1);
                updateSubscription(sub1, SubscriptionStatus.ACTIVE, 100000,
                        BD10, PricingPeriod.HOUR, BD20, BD15, BD5, BD25, dm);
                updateSubscription(sub1, SubscriptionStatus.SUSPENDED, 200000,
                        BD10, PricingPeriod.HOUR, BD20, BD15, BD5, BD25, dm);
                updateSubscription(sub1, SubscriptionStatus.ACTIVE, 500000,
                        BD10, PricingPeriod.HOUR, BD20, BD30, BD10, BD5, dm);
                updateSubscription(sub1, SubscriptionStatus.DEACTIVATED,
                        600000, BD10, PricingPeriod.HOUR, BD20, BD30, BD10,
                        BD5, dm);
                createGatheredEvent("Event1", 150000, sub1.getKey(), 20);
                Subscription sub2 = Subscriptions.createSubscription(dm,
                        custId, productId1, "sub2", 1000000, 1000000, supplier,
                        1);
                updateSubscription(sub2, SubscriptionStatus.ACTIVE, 1000000,
                        BigDecimal.ZERO, PricingPeriod.DAY, BD2, BD3, BD4, BD5,
                        dm);
                updateSubscription(sub2, SubscriptionStatus.DEACTIVATED,
                        1500000, BigDecimal.ZERO, PricingPeriod.DAY, BD2, BD3,
                        BD4, BD5, dm);
                Subscription sub3 = Subscriptions.createSubscription(dm,
                        custId, productId1, "sub3", 2000000, 2000000, supplier,
                        1);
                updateSubscription(sub3, SubscriptionStatus.ACTIVE, 2000000,
                        BD100, PricingPeriod.DAY, BD2, BD3, BD4, BD5, dm);
                updateSubscription(sub3, SubscriptionStatus.DEACTIVATED,
                        2200000, BD100, PricingPeriod.DAY, BD2, BD3, BD4, BD5,
                        dm);

                Subscription sub4 = Subscriptions.createSubscription(dm,
                        custId, productId1, "sub4", 1000000, 1000000, supplier,
                        1);
                sub4.getPriceModel().setCurrency(usd);
                sub4.getPriceModel().setHistoryModificationTime(
                        Long.valueOf(1000000));
                updateSubscription(sub4, SubscriptionStatus.ACTIVE, 1000000,
                        BD20, PricingPeriod.DAY, BD6, BD7, BD8, BD9, dm);
                updateSubscription(sub4, SubscriptionStatus.DEACTIVATED,
                        1100000, BD20, PricingPeriod.DAY, BD6, BD7, BD8, BD9,
                        dm);
                Subscription sub5 = Subscriptions.createSubscription(dm,
                        custId, productId1, "sub5", 1700000, 1700000, supplier,
                        1);
                sub5.getPriceModel().setCurrency(usd);
                sub5.getPriceModel().setHistoryModificationTime(
                        Long.valueOf(1700000));
                updateSubscription(sub5, SubscriptionStatus.ACTIVE, 1700000,
                        BigDecimal.ZERO, PricingPeriod.DAY, BD13, BD14, BD15,
                        BD16, dm);
                updateSubscription(sub5, SubscriptionStatus.SUSPENDED, 1900000,
                        BigDecimal.ZERO, PricingPeriod.DAY, BD13, BD14, BD15,
                        BD16, dm);
                updateSubscription(sub5, SubscriptionStatus.ACTIVE, 2100000,
                        BigDecimal.ZERO, PricingPeriod.DAY, BD12, BD13, BD16,
                        BD15, dm);
                updateSubscription(sub5, SubscriptionStatus.DEACTIVATED,
                        2400000, BigDecimal.ZERO, PricingPeriod.DAY, BD12,
                        BD13, BD16, BD15, dm);
                createGatheredEvent("Event1", 2220000, sub5.getKey(), 70);

                Subscription sub6 = Subscriptions
                        .createSubscription(dm, custId, productId1, "sub6",
                                200000, 200000, supplier, 1);
                sub6.getPriceModel().setCurrency(jpy);
                sub6.getPriceModel()
                        .setHistoryModificationTime(Long.valueOf(2));
                updateSubscription(sub6, SubscriptionStatus.ACTIVE, 200000,
                        BigDecimal.ZERO, PricingPeriod.DAY, BD60, BD70, BD80,
                        BD90, dm);
                updateSubscription(sub6, SubscriptionStatus.DEACTIVATED,
                        800000, BigDecimal.ZERO, PricingPeriod.DAY, BD60, BD70,
                        BD80, BD90, dm);
                Subscription sub7 = Subscriptions
                        .createSubscription(dm, custId, productId1, "sub7",
                                500000, 500000, supplier, 1);
                sub7.getPriceModel().setCurrency(jpy);
                sub7.getPriceModel().setHistoryModificationTime(
                        Long.valueOf(500000));
                updateSubscription(sub7, SubscriptionStatus.ACTIVE, 500000,
                        BD5, PricingPeriod.DAY, BD110, BD120, BD130, BD140, dm);
                updateSubscription(sub7, SubscriptionStatus.DEACTIVATED,
                        700000, BD5, PricingPeriod.DAY, BD110, BD120, BD130,
                        BD140, dm);
                Subscription sub8 = Subscriptions.createSubscription(dm,
                        custId, productId1, "sub8", 1500000, 1500000, supplier,
                        1);
                sub8.getPriceModel().setCurrency(jpy);
                sub8.getPriceModel().setHistoryModificationTime(
                        Long.valueOf(1500000));
                updateSubscription(sub8, SubscriptionStatus.ACTIVE, 1500000,
                        BD20, PricingPeriod.HOUR, BD80, BD90, BD100, BD110, dm);
                updateSubscription(sub8, SubscriptionStatus.DEACTIVATED,
                        1700000, BD20, PricingPeriod.HOUR, BD80, BD90, BD100,
                        BD110, dm);
                createGatheredEvent("Event1", 1600000, sub8.getKey(), 150);

                return cust;
            }
        });
    }

    /**
     * Updates the given subscription. Must be called from within a transaction.
     * 
     * @param sub
     *            The subscription to modify.
     * @param targetState
     *            The state to be set for the subscription.
     * @param modTime
     *            The time to be stored as modification time.
     * @param oneTimeFee
     *            The one time fee to be set.
     * @param period
     *            The period to be set.
     * @param pricePerUser
     *            The price per user to be set.
     * @param evtPrice
     *            The price per event to be set.
     * @param param1Price
     *            The price for the first param.
     * @param param2Price
     *            The price for the second param.
     * @param dm
     *            The data manager.
     */
    private void updateSubscription(Subscription sub,
            SubscriptionStatus targetState, long modTime,
            BigDecimal oneTimeFee, PricingPeriod period,
            BigDecimal pricePerUser, BigDecimal evtPrice,
            BigDecimal param1Price, BigDecimal param2Price, DataService dm) {
        sub.setStatus(targetState);
        sub.setHistoryModificationTime(Long.valueOf(modTime));
        PriceModel priceModel = sub.getPriceModel();
        if (priceModel != null) {
            priceModel.setOneTimeFee(oneTimeFee);
            priceModel.setPeriod(period);
            priceModel.setPricePerPeriod(pricePerUser);
            priceModel.setPricePerUserAssignment(pricePerUser);
            List<PricedEvent> consideredEvents = priceModel
                    .getConsideredEvents();
            if (!consideredEvents.isEmpty()) {
                consideredEvents.get(0).setEventPrice(evtPrice);
            }
            List<PricedParameter> params = priceModel.getSelectedParameters();
            if (!params.isEmpty()) {
                params.get(0).setPricePerSubscription(param1Price);
                if (params.size() > 1) {
                    params.get(1).setPricePerSubscription(param2Price);
                }
            }
        }
        Subscriptions.setHistoryCreationTime(modTime, sub.getProduct());
        dm.flush();
    }

    /**
     * Initializes a subscription using the given times.
     * 
     * @param modDateForEuro
     *            The history modification time to be used for the EURO based
     *            subscription.
     * @param modDateForUsd
     *            The history modification time to be used for the USD based
     *            subscription.
     * @throws Exception
     */
    private void initData(final long modDateForEuro, final long modDateForUsd)
            throws Exception {
        // create test data with the given modification dates
        setTestDateFactory();
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    // add a second currency
                    SupportedCurrency sc = (SupportedCurrency) dm
                            .getReferenceByBusinessKey(usd);

                    SupportedCurrency eur = new SupportedCurrency();
                    eur.setCurrency(Currency.getInstance("EUR"));
                    eur = (SupportedCurrency) dm.getReferenceByBusinessKey(eur);

                    // create a subscription using USD
                    Organization customer = Scenario.getSecondCustomer();
                    customer = (Organization) dm
                            .getReferenceByBusinessKey(customer);

                    Organization supplier = Scenario.getSupplier();
                    supplier = dm.getReference(Organization.class,
                            supplier.getKey());
                    Subscription newSubEur = Subscriptions.createSubscription(
                            dm, customer.getOrganizationId(), Scenario
                                    .getProduct().getProductId(), "newEURSub",
                            modDateForEuro, modDateForEuro, supplier, 1);

                    Subscription newSubUsd = Subscriptions.createSubscription(
                            dm, customer.getOrganizationId(), Scenario
                                    .getProduct().getProductId(), "newUSDSub",
                            modDateForUsd, modDateForUsd, supplier, 1);
                    PriceModel priceModelEURO = newSubEur.getPriceModel();
                    priceModelEURO.setPeriod(PricingPeriod.HOUR);
                    priceModelEURO.setCurrency(eur);
                    priceModelEURO.setHistoryModificationTime(Long
                            .valueOf(modDateForEuro));
                    PriceModel priceModelUSD = newSubUsd.getPriceModel();
                    priceModelUSD.setPeriod(PricingPeriod.HOUR);
                    priceModelUSD.setCurrency(sc);
                    priceModelUSD.setHistoryModificationTime(Long
                            .valueOf(modDateForUsd));

                    // create one event for the euro and two for the USD
                    // subscription
                    createGatheredEvent("Event1", modDateForEuro + 1,
                            newSubEur.getKey(), 1);

                    createGatheredEvent("Event2", modDateForUsd + 1,
                            newSubUsd.getKey(), 2);

                    return null;
                }
            });
        } finally {
            restoreDateFactory();
        }
    }

    /**
     * The period of the test data is start until now. The modification time of
     * the relevant test data is set manually. In addition the the relevant test
     * data, some additional data is created with the transaction time. In most
     * cases the transaction time is shortly after the end period (now), because
     * the start of a transaction needs some time. However, sometimes the TX
     * time and now fall together. In this case some non relevant test data
     * appears in the queries of the productive code. To avoid this behavior,
     * this method moves the transaction time way out of the testing period.
     */
    private void setTestDateFactory() {
        DateFactory.setInstance(new TestDateFactory(new Date(System
                .currentTimeMillis() + HOUR_IN_MILLIS)));
    }

    private void restoreDateFactory() {
        DateFactory.setInstance(new DateFactory());
    }

    /**
     * Creates a gathered event.
     * 
     * @param eventId
     *            The event identifier.
     * @param creationTime
     *            The time the event was created.
     * @param subKey
     *            The technical key of the subscription to store the event for.
     * @param multiplier
     *            The multiplier for the event.
     * @throws NonUniqueBusinessKeyException
     */
    private void createGatheredEvent(String eventId, long creationTime,
            long subKey, long multiplier) throws NonUniqueBusinessKeyException {
        GatheredEvent evt = new GatheredEvent();
        evt.setEventId(eventId);
        evt.setOccurrenceTime(creationTime);
        evt.setType(EventType.SERVICE_EVENT);
        evt.setSubscriptionTKey(subKey);
        evt.setMultiplier(multiplier);
        evt.setActor("actor");
        dm.persist(evt);
    }

    /**
     * Validates the content of the specified billing result structure.
     * 
     * @param billingResult
     *            The billing result to be validated.
     * @param currency
     *            The currency to be contained in the result.
     * @param amount
     *            The amount to be expected in the result.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    private void validateBillingResult(BillingResult billingResult,
            String currency, BigDecimal amount)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        System.out.println(billingResult.getResultXML());
        Document doc = XMLConverter.convertToDocument(
                billingResult.getResultXML(), false);
        assertEquals(currency, XMLConverter.getNodeTextContentByXPath(doc,
                "/BillingDetails/OverallCosts/@currency"));
        if (amount != null) {
            checkEquals(amount.toPlainString(),
                    XMLConverter.getNodeTextContentByXPath(doc,
                            "/BillingDetails/OverallCosts/@grossAmount"), 2);
        }
    }

    private void validateOrganizationDetails(String ptId, BillingContact bc,
            Document doc) throws Exception {
        String basepath = "/" + BillingResultXMLTags.BILLING_DETAILS_NODE_NAME
                + "/" + BillingResultXMLTags.ORGANIZATION_DETAILS_NODE_NAME
                + "/";
        assertEquals(
                ptId,
                XMLConverter.getNodeTextContentByXPath(doc, basepath
                        + BillingResultXMLTags.PAYMENTTYPE_NODE_NAME));
        if (bc != null) {
            assertEquals(
                    bc.getEmail(),
                    XMLConverter.getNodeTextContentByXPath(doc, basepath
                            + BillingResultXMLTags.EMAIL_NODE_NAME));
            assertEquals(
                    bc.getCompanyName(),
                    XMLConverter.getNodeTextContentByXPath(doc, basepath
                            + BillingResultXMLTags.NAME_NODE_NAME));
            assertEquals(
                    bc.getAddress(),
                    XMLConverter.getNodeTextContentByXPath(doc, basepath
                            + BillingResultXMLTags.ADDRESS_NODE_NAME));

        }
    }

    private long getStartDateOfMonth(long baseTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(baseTime);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Read platform user.
     * 
     * @param userId
     * @param supplierOrgId
     * @return
     */
    private PlatformUser getPlatformUser(final String userId,
            final String supplierOrgId) {
        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        user.setOrganization(getOrganization(supplierOrgId));
        user = (PlatformUser) dm.find(user);
        return user;
    }

    /**
     * Read the subscription with the given id from the database.
     * 
     * @param subId
     *            the id of the subscription to read.
     * @param customerId
     *            the customer organization id
     * 
     * @return the read subscription domain object.
     */
    private Subscription getSubscription(String subId, String customerId) {
        Subscription sub = new Subscription();
        sub.setOrganization(getOrganization(customerId));
        sub.setSubscriptionId(subId);
        sub = (Subscription) dm.find(sub);
        return sub;
    }

    /**
     * Read the organization from the database.
     * 
     * @return the read organization domain object.
     */
    private Organization getOrganization(String organizationId) {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization = (Organization) dm.find(organization);
        return organization;
    }

}
