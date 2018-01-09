/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 3, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.BugSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestBasicSetup;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author baumann
 * 
 */
public class BugIT extends BillingIntegrationTestBase {

    private final BugSetup testSetup = new BugSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void bug10339() throws Exception {
        // given
        testSetup.createBug10339();

        // when
        performBillingRun(0, "2013-08-1 12:00:00");

        // then
        VOSubscriptionDetails subscr = getSubscriptionDetails("Bug10339", 0);
        BillingResultEvaluator eva = getEvaluator(subscr.getKey(),
                "2013-07-1 00:00:00", "2013-08-1 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), subscr.getPriceModel().getKey());

        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNotNull(parameter);
        eva.assertParameterValue(parameter, "2", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, "MONTH", "2.00", 1.0, "2.00");
        eva.assertUserAssignmentCosts(option, "MONTH", "1.10", 2.0, "2.20",
                "15.20");
        eva.assertRoleCost(roleCosts, "ADMIN", "15.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 1.0, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 1.0, "5.00");
        eva.assertOptionCosts(option, "17.20");
        eva.assertParameterCosts(parameter, "17.20");
    }

    @Test
    public void bug10267_free() throws Exception {
        // given
        testSetup.createBug10267_free();

        // when
        performBillingRun(0, "2013-04-10 07:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("Bug10267_free", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        assertNull(eva);
    }

    @Test
    public void bug10267() throws Exception {
        // given
        testSetup.createBug10267();

        // when
        performBillingRun(0, "2013-04-10 07:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("Bug10267", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");
        assertNull(eva);
    }

    @Test
    public void billingPerUnitMonthBug10091() throws Exception {
        // given subscription data for scenario
        testSetup.createMonthScenarioBug10091();

        // when billing run performed
        performBillingRun(0, "2013-03-20 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10091_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-10 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "123.00");
    }

    @Test
    public void billingPerUnitWeekBug10091() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10091();

        // when billing run performed
        performBillingRun(0, "2013-05-16 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10091_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-10 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertNullOneTimeFee();
        eva.assertOverallCosts("EUR", "709.10");

        voSubscriptionDetails = getSubscriptionDetails("BUG10091_PERUNIT_WEEK",
                0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva = new BillingResultEvaluator(billingResult);
        eva.assertOneTimeFee(priceModel, "25.00");
    }

    @Test
    public void billingPerUnitWeekBug10091_FreeP() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10091_Freep();

        // when billing run performed
        performBillingRun(0, "2013-05-16 07:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10091_PU_WEEK_FREEP", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-10 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertNullOneTimeFee();
        eva.assertOverallCosts("EUR", "709.10");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10091_PU_WEEK_FREEP", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva = new BillingResultEvaluator(billingResult);
        eva.assertOneTimeFee(priceModel, "25.00");
    }

    @Test
    public void billingPerUnitWeekBug10269_FreeP() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10269_Freep();

        // when billing run performed
        performBillingRun(0, "2013-05-16 07:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10269_PERUNIT_WEEK_FREEP", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-11 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertNullOneTimeFee();
        eva.assertOverallCosts("2348.00", "EUR", "2348.00");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10269_PERUNIT_WEEK_FREEP", 1);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-11 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "254.00");
    }

    @Test
    public void billingPerRataWeekBug10269_FreeP() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10269_Rata_Freep();

        // when billing run performed
        performBillingRun(0, "2013-05-16 07:00:00");

        // then assert billing period 1
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10269_RATA_WEEK_FREEP", 1);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-11 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertNullOneTimeFee();
        eva.assertOverallCosts("EUR", "740.74");

        // THEN assert billing period 2
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-11 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        // price model 1
        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10269_RATA_WEEK_FREEP", 1);
        eva.assertOneTimeFee(voSubscriptionDetails.getPriceModel().getKey(),
                "254.00");

        // price model 0
        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10269_RATA_WEEK_FREEP", 0);
        eva.assertNullOneTimeFee(voSubscriptionDetails.getPriceModel().getKey());

    }

    @Test
    public void billingPerRataWeekBug10269_FreeP_2() throws Exception {
        // GIVEN subscription data for scenario
        testSetup.createWeekScenarioBug10269_2_Rata_Freep();

        // WHEN billing run performed
        performBillingRun(0, "2013-05-16 07:00:00");

        // THEN
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10269_2_RATA_WEEK_FREEP", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "1589.08");

        // price model 1
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");

        // price model 2
        VOSubscriptionDetails upgradedSubDetails = getSubscriptionDetails(
                "BUG10269_2_RATA_WEEK_FREEP", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 0.5119047619047619, "375.74");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10269_2_RATA_WEEK_FREEP", 0);
        assertNull(loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00")));
    }

    @Test
    public void billingRataWeekBug10091() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10091_Rata();

        // when
        performBillingRun(0, "2013-05-16 07:00:00");

        // then - assert period 1
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10091_RATA_WEEK", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-04-10 00:00:00",
                "2013-05-10 00:00:00");
        assertNull(eva);

        // then - assert period 2
        voSubscriptionDetails = getSubscriptionDetails("BUG10091_RATA_WEEK", 0);
        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-03-10 00:00:00", "2013-04-10 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, 0.14285714285714285);
    }

    @Test
    public void billingPerUnitWeekBug10133() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10133();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10133_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertOverallCosts("EUR", "25.00");

        voSubscriptionDetails = getSubscriptionDetails("BUG10133_PERUNIT_WEEK",
                0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertNullOneTimeFee();
        eva.assertOverallCosts("EUR", "709.10");
    }

    @Test
    public void billingProRataWeekBug10133() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10133_Rata();

        // when
        performBillingRun(0, "2013-05-06 07:00:00");

        // then - assert period 1
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "BUG10133_RATA_WEEK", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-04-03 00:00:00", "2013-05-03 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        eva.assertOneTimeFee(sub.getPriceModel().getKey(), "25.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, 0.16071428571428573);

        // then - assert period 2
        sub = getSubscriptionDetails("BUG10133_RATA_WEEK", 0);
        assertNull(getEvaluator(sub.getKey(), "2013-05-03 00:00:00",
                "2013-06-03 00:00:00"));
    }

    @Test
    public void billingPerUnitWeekBug10133_2() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10133_2();

        // when billing run performed
        performBillingRun(0, "2013-05-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10133_2_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-28 07:00:00"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-04-28 07:00:00"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"),
                "true", "BOOLEAN");
        eva.assertParameterCosts(parameter, "39.00");
        eva.assertOverallCosts("EUR", "734.10");
    }

    @Test
    public void billingPerUnitWeekBug10221_with_free_period() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10221_with_free_period();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PERIOD_UNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PERIOD_UNIT_WEEK", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-03 00:00:00", "2013-05-03 00:00:00"));
    }

    @Test
    public void billingPerUnitWeekBug10221_free_period_Event() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekBug10221_free_period_and_event();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_UNIT_WEEK_EVENT", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_UNIT_WEEK_EVENT", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-03 00:00:00", "2013-05-03 00:00:00"));
    }

    @Test
    public void billingRataWeekBug10221_with_free_period() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10221_with_free_period_Rata();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PER_RATA_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PER_RATA_WEEK", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-03 00:00:00", "2013-05-03 00:00:00"));
    }

    @Test
    public void billingPerUnitWeekBug10221_with_free_period_2()
            throws Exception {

        // given subscription data for scenario
        testSetup.createWeekScenarioBug10221_with_free_period_2();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_2", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-04 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertNullOneTimeFee();

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_2", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-04 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
    }

    @Test
    public void billingPerUnitWeekBug10221_with_free_period_3()
            throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10221_with_free_period_3();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_3", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-04 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_3", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-04 00:00:00", "2013-05-04 00:00:00"));
    }

    @Test
    public void billingPerUnitWeekBug10235_with_free_period() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10235_with_free_period();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10235_FREE_PERIOD_UNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-04 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertOverallCosts("1230.00", "EUR", "1230.00");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10235_FREE_PERIOD_UNIT_WEEK", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-04 00:00:00", "2013-05-04 00:00:00"));
    }

    @Test
    public void billingPerUnitWeekBug10235_with_free_period_2()
            throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10235_with_free_period_2();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10235_FREE_PERIOD_UNIT_WEEK_2", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-04 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "7.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 5.0, "30.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "5.0");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");
        eva.assertOverallCosts("1362.00", "EUR", "1362.00");
        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10235_FREE_PERIOD_UNIT_WEEK_2", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-04 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertOverallCosts("25.00", "EUR", "25.00");
    }

    @Test
    public void billingRataWeekBug10235_with_free_period() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10235_with_free_period_Rata();

        // when billing run performed
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10235_FREE_PERIOD_RATA_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-04 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.386904761904762,
                "26.32");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 4.386904761904762);
        eva.assertOverallCosts("1082.25", "EUR", "1082.25");

        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10235_FREE_PERIOD_RATA_WEEK", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-04 00:00:00", "2013-05-04 00:00:00"));
    }

    @Test
    public void billingWeek_free_period_stepped_user() throws Exception {
        // given subscription data for scenario
        testSetup.createWeek_free_period_stepPriceUser();

        // when billing run performed
        performBillingRun(0, "2013-07-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREE_STEPPED_USER_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOverallCosts("703.00", "EUR", "703.00");

        voSubscriptionDetails = getSubscriptionDetails(
                "FREE_STEPPED_USER_WEEK", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertOverallCosts("25.00", "EUR", "25.00");
    }

    @Test
    public void testBUG10301() throws Exception {
        // given subscription data for scenario
        testSetup.createBug10301();

        // when
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10301", 1);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-05-01 00:00:00",
                "2013-06-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "BUG10301" + "_SubID2");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1, "7.00");
    }

    @Test
    public void testBUG10303() throws Exception {
        // given subscription data for scenario
        testSetup.createBug10303();

        // when
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("BUG10303", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-05-01 00:00:00", "2013-06-01 00:00:00");
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
    }

    @Test
    public void billingPerUnitMonthBug10249_SteppedEvents() throws Exception {
        // given subscription data for scenario
        testSetup.createMonthScenarioBug10249_perUnit_steppedEvents();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10249_PER_UNIT_MONTH_EVENTS", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "1444.25");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "1444.25");
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");

        // event costs
        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(gatheredEvents, "FILE_UPLOAD", "9.00", "13",
                "117.00");
        eva.assertEventCosts(gatheredEvents, "FOLDER_NEW", "110.25", "1",
                "110.25");
        Node event = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_DOWNLOAD");
        Node steppedEventPrices = BillingXMLNodeSearch
                .getSteppedEventPricesNode(event);
        eva.assertSteppedPrice(steppedEventPrices, "0.00", "10.00", "0", "1",
                "10.00", "1");
        eva.assertSteppedPrice(steppedEventPrices, "10.00", "5.00", "1", "13",
                "60.00", "12");
        eva.assertSteppedPrice(steppedEventPrices, "70.00", "2.00", "13", "80",
                "124.00", "62");
        eva.assertSteppedPrice(steppedEventPrices, "204.00", "1.00", "80",
                "200", "0.00", "0");
        eva.assertSteppedPrice(steppedEventPrices, "324.00", "0.50", "200",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedEventPrices, "194.00");
        eva.assertEventCosts(event, "75", "194.00");
        eva.assertGatheredEventsCosts(priceModel, "421.25");
    }

    @Test
    public void billingPerUnitWeekBug10264_RoleChangeFreeP() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioRolChangeWithFreeP();

        // when billing run performed
        performBillingRun(0, "2013-04-3 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "ROLCHANGE_WEEK_FREEP", 0);

        // no billing result for first period, free period
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-02-01 00:00:00", "2013-03-01 00:00:00"));

        // billing result for second period
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "267.00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "1.0", "1", "150.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "7.00");
        eva.assertTotalRoleCosts(roleCosts, "7.00");
    }

    @Test
    public void billingPerUnitWeekBug10265_ParChangeFreeP() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10265_ParChangeWithFreeP();

        // when billing run performed
        performBillingRun(0,
                DateTimeHandling.calculateMillis("2013-04-03 03:00:00"));

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "RARCHANGE_WEEK_FREEP", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-02-01 00:00:00", "2013-03-01 00:00:00"));

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "1.0", "1", "150.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-02 07:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-03 07:00:00") + "",
                "3", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1, "6.00", "3.0");
    }

    @Test
    public void billingPerUnitWeekBug10265_UpgradeAndParChange()
            throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10265_UpgradeAndParChange();

        // when billing run performed
        performBillingRun(0, "2013-04-03 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10265_UPG_PARCHG", 0);
        VOSubscriptionDetails upgradedSubDetails = getSubscriptionDetails(
                "BUG10265_UPG_PARCHG", 1);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "2.00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00"));
        eva.assertOneTimeFee(priceModel, "2.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2.00");

        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "277.99");

        // price model of original service
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00"));
        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "5.00", "1.0", "5.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "10.00", "1.0", "1", "10.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "3.00");
        eva.assertTotalRoleCosts(roleCosts, "3.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00") + "",
                "3", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 1.0, "3.00", "3.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "10.00", 1.0, "30.00", "39.00", "3.0");
        eva.assertTotalRoleCosts(roleCosts, "9.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "9.00");
        eva.assertParameterCosts(parameter, "42.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "60.00");

        // price model of upgraded service
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-03-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-03 07:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "217.99");
        eva.assertOneTimeFee(priceModel, "4.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.0", "10.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "20.00", "1.0", "1", "20.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-02 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-02 12:00:00") + "",
                "7", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.7857142857, "11.00", "7.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.7857142857, "110.00", "143.00", "7.0");
        eva.assertTotalRoleCosts(roleCosts, "33.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.7857142857, "33.00");
        eva.assertParameterCosts(parameter, "154.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-02 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-03 07:00:00") + "",
                "4", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.2142857143, "1.71", "4.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.2142857143, "17.14", "22.28", "4.0");
        eva.assertTotalRoleCosts(roleCosts, "5.14");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.2142857143, "5.14");
        eva.assertParameterCosts(parameter, "23.99");
    }

    @Test
    public void billingPerUnitWeekBug10265_UpgradeAndParChange2()
            throws Exception {
        // given subscription data for scenario
        testSetup.createWeekScenarioBug10265_UpgradeAndParChange2();

        // when billing run performed
        performBillingRun(0, "2013-04-03 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10265_UPG_PARCHG2", 0);
        VOSubscriptionDetails upgradedSubDetails = getSubscriptionDetails(
                "BUG10265_UPG_PARCHG2", 1);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "6.00");

        // price model of original service
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00"));
        eva.assertOneTimeFee(priceModel, "2.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2.00");

        // price model of upgraded service
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00"));

        eva.assertOneTimeFee(priceModel, "4.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "4.00");

        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "207.68");

        // price model of original service
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00"));
        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelCosts(priceModel, "EUR", "60.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "5.00", "1.0", "5.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "10.00", "1.0", "1", "10.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "3.00");
        eva.assertTotalRoleCosts(roleCosts, "3.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-26 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00") + "",
                "3", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 1.0, "3.00", "3.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "10.00", 1.0, "30.00", "39.00", "3.0");
        eva.assertTotalRoleCosts(roleCosts, "9.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "9.00");
        eva.assertParameterCosts(parameter, "42.00");

        // price model of upgraded service
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-03 07:00:00"));

        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelCosts(priceModel, "EUR", "147.68");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "20.00", "1.0", "1", "20.00");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-28 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "3", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.5714285714, "3.43", "3.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.5714285714, "34.29", "44.58", "3.0");
        eva.assertTotalRoleCosts(roleCosts, "10.29");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.5714285714, "10.29");
        eva.assertParameterCosts(parameter, "48.01");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 07:00:00") + "",
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.0416666667, "0.42", "5.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.0416666667, "4.17", "5.42", "5.0");
        eva.assertTotalRoleCosts(roleCosts, "1.25");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.0416666667, "1.25");
        eva.assertParameterCosts(parameter, "5.84");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-01 07:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-02 12:00:00") + "",
                "7", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.1726190476, "2.42", "7.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.1726190476, "24.17", "31.42", "7.0");
        eva.assertTotalRoleCosts(roleCosts, "7.25");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.1726190476, "7.25");
        eva.assertParameterCosts(parameter, "33.84");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-02 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-03 07:00:00") + "",
                "4", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.2142857143, "1.71", "4.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.2142857143, "17.14", "22.28", "4.0");
        eva.assertTotalRoleCosts(roleCosts, "5.14");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.2142857143, "5.14");
        eva.assertParameterCosts(parameter, "23.99");
    }

    @Test
    public void billingPerUnitWeekSuspend() throws Exception {
        // given subscription data for scenario
        testSetup.createWeekSuspend();

        // when
        performBillingRun(0, "2013-07-06 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO_WEEK_SUSP", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-06-01 00:00:00", "2013-07-01 00:00:00");
        eva.assertNodeCount("//Subscriptions/Subscription", 2);
        eva.assertOverallCosts("1534.00", "EUR", "1534.00");
    }

    @Test
    public void billingPerUnitDayBug10302() throws Exception {
        // given subscription data for scenario
        testSetup.createPerUnitDayBug10302();

        // when
        performBillingRun(28, "2013-05-08 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10302_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-03-07 00:00:00",
                "2013-04-07 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "BUG10302_PERUNIT_DAY");
        assertNotNull("Subscription doesn't exist", subscription);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00") + "");
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "2.00", "5.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "3.00", "5.0", "1", "15.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Bug10302PUnitDayCustomer", "5.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 5.0, "15.00");
        eva.assertTotalRoleCosts(roleCosts, "15.00");
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        eva.assertParametersCosts(priceModel, "3900.00");
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00") + "",
                "10368000000", "DURATION");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.20", 5.0, "720.00", "120.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "3.80", 5.0, "2280.00", "3180.00", "120.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "900.00");
        eva.assertRoleCost(roleCosts, "GUEST", "1.50", 5.0, "900.00");
        eva.assertParameterCosts(parameter, "3900.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "3940.00");
        eva.assertOverallCosts("EUR", "3940.00");
    }

    @Test
    public void testPUDayBug10361DaylSav() throws Exception {
        // given subscription data for scenario
        testSetup.createDayBug10361_DaylightSavingTime();

        // when billing run performed
        performBillingRun(1,
                DateTimeHandling.calculateMillis("2013-04-10 23:00:00"));

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10361_DAYLSAV", 0);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "17.0", "102.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "17.0", "1", "85.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 17.0, "51.00");
        eva.assertTotalRoleCosts(roleCosts, "51.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-15 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 17.0, "2040.00", "2805.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "765.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 17.0, "765.00");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 17.0, "510.00", "15.0");
        eva.assertParameterCosts(parameter, "3315.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "3556.00");
        eva.assertOverallCosts("EUR", "3556.00");
    }

    @Test
    public void testPUDayBug10361DLSParChange() throws Exception {
        // given subscription data for scenario
        testSetup.createDayBug10361_DaylSav_ParChange();

        // when billing run performed
        performBillingRun(1, "2013-04-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10361_DLS_PAR_CHANGE", 0);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "17.0", "102.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "17.0", "1", "85.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 17.0, "51.00");
        eva.assertTotalRoleCosts(roleCosts, "51.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-16 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 15.0434782608769566, "1805.22", "2482.18", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "676.96");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 15.0434782608769566,
                "676.96");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 15.0434782608769566, "451.30", "15.0");
        eva.assertParameterCosts(parameter, "2933.48");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                "4", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 1.9565217391304348, "62.61", "86.09", "4.0");
        eva.assertTotalRoleCosts(roleCosts, "23.48");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.9565217391304348,
                "23.48");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 1.9565217391304348, "15.65", "4.0");
        eva.assertParameterCosts(parameter, "101.74");

        eva.assertPriceModelCosts(priceModel, "EUR", "3276.22");
        eva.assertOverallCosts("EUR", "3276.22");
    }

    @Test
    public void bug10404_Upgrade_Suspended_ProRata_Service() throws Exception {
        // given
        testSetup.createBug10404_suspendUpgradedProRataService();

        // when
        performBillingRun(0, "2013-09-15 12:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("BUG10404_UPGR_SUS",
                0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "BUG10404_UPGR_SUS", 1);

        // First billing period
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-10 00:00:00", "2013-08-10 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "BUG10404_UPGR_SUS");
        assertNotNull("Subscription doesn't exist", subscription);

        // initial service (per time unit price model)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00") + "");

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        // upgraded service (pro rata price model)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-08 13:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-09 18:00:00") + "");

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 0.1726190476190476, "126.70");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 0.1726190476190476, "1",
                "74.92");
        eva.assertUserAssignmentCostsFactor(priceModel, "Bug10404Customer",
                "0.17261904761904762");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "1.16");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.0654761904761905,
                "0.52");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.1071428571428571,
                "0.64");

        eva.assertPriceModelCosts(priceModel, "EUR", "456.78");

        eva.assertOverallCosts("710.78", "EUR", "710.78");

        // Second billing period
        eva = getEvaluator(sub.getKey(), "2013-08-10 00:00:00",
                "2013-09-10 00:00:00");

        // initial service (per time unit price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_UPGR_SUS", sub.getPriceModel()
                .getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-08 13:00:00") + "");

        eva.assertNullOneTimeFee(priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 1.0, "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0, "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Bug10404Customer",
                "1.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1176.00");

        // upgraded service (pro rata price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_UPGR_SUS", upgradedSub
                .getPriceModel().getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-14 20:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00") + "");

        eva.assertNullOneTimeFee(priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 3.738095238095238, "2743.76");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 3.738095238095238, "1",
                "1622.33");
        eva.assertUserAssignmentCostsFactor(priceModel, "Bug10404Customer",
                "3.738095238095238");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "22.43");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.738095238095238,
                "22.43");

        eva.assertPriceModelCosts(priceModel, "EUR", "4388.52");

        eva.assertOverallCosts("EUR", "5564.52");
    }

    @Test
    public void bug10404_Upgrade_Expired_Subscription() throws Exception {
        // given
        testSetup.createBug10404_upgradeExpiredSubscription();

        // when
        performBillingRun(0, "2013-09-15 12:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "BUG10404_UPGR_EXP_SUB", 0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "BUG10404_UPGR_EXP_SUB", 1);

        // First billing period
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-10 00:00:00", "2013-08-10 00:00:00");

        // initial service (per time unit price model)
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_UPGR_EXP_SUB", sub
                .getPriceModel().getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"));

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");
        eva.assertOverallCosts("EUR", "254.00");

        // Second billing period
        eva = getEvaluator(sub.getKey(), "2013-08-10 00:00:00",
                "2013-09-10 00:00:00");

        // initial service (per time unit price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_UPGR_EXP_SUB", sub
                .getPriceModel().getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-12 13:00:00"));

        eva.assertNullOneTimeFee(priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 2.0, "1468.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 2.0, "1", "868.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "2.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "2348.00");

        // upgraded service (pro rata price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_UPGR_EXP_SUB", upgradedSub
                .getPriceModel().getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-30 15:20:00") + "");

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 1.4603174603174602D, "1071.87");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.4603174603174602D, "1",
                "633.78");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.4603174603174602D);

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "8.76");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.4603174603174602D,
                "8.76");

        eva.assertPriceModelCosts(priceModel, "EUR", "1968.41");

        eva.assertOverallCosts("EUR", "4316.41");
    }

    @Test
    public void bug10404_expireSuspendedSubscription() throws Exception {
        // given
        testSetup.createBug10404_expireSuspendedSubscription();

        // when
        performBillingRun(0, "2013-09-15 12:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "BUG10404_EXP_SUS_SUB", 0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "BUG10404_EXP_SUS_SUB", 1);

        // First billing period
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-10 00:00:00", "2013-08-10 00:00:00");

        // initial service (per time unit price model)
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_EXP_SUS_SUB", sub
                .getPriceModel().getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"));

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");
        eva.assertOverallCosts("EUR", "254.00");

        // Second billing period
        eva = getEvaluator(sub.getKey(), "2013-08-10 00:00:00",
                "2013-09-10 00:00:00");

        // initial service (per time unit price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_EXP_SUS_SUB", sub
                .getPriceModel().getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-07 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-12 13:00:00"));

        eva.assertNullOneTimeFee(priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 2.0, "1468.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 2.0, "1", "868.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Bug10404ExpSusCustomer", 2.0);

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "2348.00");

        // upgraded service (pro rata price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10404_EXP_SUS_SUB", upgradedSub
                .getPriceModel().getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-30 15:20:00"));

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 1.4603174603174602D, "1071.87");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.4603174603174602D, "1",
                "633.78");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Bug10404ExpSusCustomer", 1.4603174603174602D);

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "8.76");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.4603174603174602D,
                "8.76");

        eva.assertPriceModelCosts(priceModel, "EUR", "1968.41");

        eva.assertOverallCosts("EUR", "4316.41");
    }

    @Test
    public void bug10476_upgradeSuspendedSubscription() throws Exception {
        // given subscription data for scenario
        testSetup.createBug10476_upgradeSuspendedSubscription();

        // when billing run performed
        performBillingRun(15, "2013-09-26 13:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("BUG10476_UPGRSUS",
                0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "BUG10476_UPGRSUS", 1);

        // First billing period
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-03 00:00:00", "2013-08-03 00:00:00");

        // initial service (per time unit price model)
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10476_UPGRSUS", sub.getPriceModel()
                .getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"));

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("EUR", "254.00");

        // Second billing period
        eva = getEvaluator(sub.getKey(), "2013-08-03 00:00:00",
                "2013-09-03 00:00:00");

        // initial service (per time unit price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10476_UPGRSUS", sub.getPriceModel()
                .getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-03 00:00:00"));

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 1.0D, "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Bug10476UpgrSusCustomerAdmin", "1.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0D, "8.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1176.00");

        // upgraded service (free)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10476_UPGRSUS", upgradedSub
                .getPriceModel().getKey());
        assertNull("Subscription with free price model should not exist",
                subscription);

        eva.assertOverallCosts("EUR", "1176.00");
    }

    @Test
    public void bug10476_upgradeSuspendedSubscription2() throws Exception {
        // given subscription data for scenario
        testSetup.createBug10476_upgradeSuspendedSubscription2();

        // when billing run performed
        performBillingRun(15, "2013-09-26 13:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("BUG10476_UPGRSUS2",
                0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "BUG10476_UPGRSUS2", 1);

        // first billing period
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-03 00:00:00", "2013-08-03 00:00:00");

        // initial service (per time unit price model)
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10476_UPGRSUS2", sub.getPriceModel()
                .getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"));

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("EUR", "254.00");

        // second billing period
        eva = getEvaluator(sub.getKey(), "2013-08-03 00:00:00",
                "2013-09-03 00:00:00");

        // initial service (per time unit price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10476_UPGRSUS2", sub.getPriceModel()
                .getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-03 00:00:00"));

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 1.0D, "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Bug10476UpgrSus2CustomerAdmin", "1.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0D, "8.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1176.00");

        // upgraded service (free)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG10476_UPGRSUS2", upgradedSub
                .getPriceModel().getKey());
        assertNull("Subscription with free price model should not exist",
                subscription);

        eva.assertOverallCosts("EUR", "1176.00");
    }

    @Test
    public void bug11021_upgradeSuspendedSubscription() throws Exception {
        // given subscription data for scenario
        testSetup.createBug11021_upgradeSuspendedSubscription();

        // when billing run performed
        performBillingRun(15, "2013-09-26 13:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("BUG11021_UPGRSUS",
                0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "BUG11021_UPGRSUS", 1);

        // First billing period
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-03 00:00:00", "2013-08-03 00:00:00");

        // initial service (per time unit price model)
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG11021_UPGRSUS", sub.getPriceModel()
                .getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"));

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("EUR", "254.00");

        // Second billing period
        eva = getEvaluator(sub.getKey(), "2013-08-03 00:00:00",
                "2013-09-03 00:00:00");

        // initial service (per time unit price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG11021_UPGRSUS", sub.getPriceModel()
                .getKey());
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-02 10:00:00"),
                DateTimeHandling.calculateMillis("2013-08-03 00:00:00"));

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 1.0D, "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Bug11021UpgrSusCustomerAdmin", "1.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0D, "8.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1176.00");

        // upgraded service (free)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "BUG11021_UPGRSUS", upgradedSub
                .getPriceModel().getKey());
        assertNull("Subscription with free price model should not exist",
                subscription);

        eva.assertOverallCosts("EUR", "1176.00");
    }

    @Test
    public void bug11822_changePurchaseOrderNumber() throws Exception {
        // given
        testSetup.bug11822_changePurchaseOrderNumber();

        // when
        performBillingRun(0, "2015-07-01 00:00:00");
        performBillingRun(0, "2015-08-01 00:00:00");

        // then
        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "BUG11822_CHANGE_PON", 0);
        VOSubscriptionDetails modifiedSubscriptionDetails = getSubscriptionDetails(
                "BUG11822_CHANGE_PON", 1);

        TestData testData = getTestData("BUG11822_CHANGE_PON");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);

        // First billing period
        Document billingResult = loadBillingResult(
                subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-07-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, subscriptionDetails.getSubscriptionId(),
                modifiedSubscriptionDetails.getPurchaseOrderNumber());
        assertNotNull("Subscription node not found", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                subscriptionDetails.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-07-01 00:00:00"));

        eva.assertOneTimeFee(subscriptionDetails.getPriceModel().getKey(),
                "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1152.00");
        eva.assertOverallCosts("EUR", "1152.00");

        // Second billing period
        billingResult = loadBillingResult(subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2015-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-08-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);
        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                subscriptionDetails.getSubscriptionId(),
                modifiedSubscriptionDetails.getPurchaseOrderNumber());
        assertNotNull("Subscription node not found", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                subscriptionDetails.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2015-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-07-01 00:00:00"));

        eva.assertNullOneTimeFee(subscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");
        eva.assertOverallCosts("EUR", "1029.00");
    }

    @Test
    public void bug11822_changePurchaseOrderNumber_afterUpgrade()
            throws Exception {
        // given
        testSetup.bug11822_changePurchaseOrderNumber_afterUpgrade();

        // when
        performBillingRun(0, "2015-07-01 00:00:00");

        // then
        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "BUG11822_CHANGE_PON_AFTER_UPGRADE", 0);
        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "BUG11822_CHANGE_PON_AFTER_UPGRADE", 1);
        VOSubscriptionDetails modifiedSubscriptionDetails = getSubscriptionDetails(
                "BUG11822_CHANGE_PON_AFTER_UPGRADE", 2);

        TestData testData = getTestData("BUG11822_CHANGE_PON_AFTER_UPGRADE");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);

        Document billingResult = loadBillingResult(
                subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-07-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, subscriptionDetails.getSubscriptionId(),
                modifiedSubscriptionDetails.getPurchaseOrderNumber());
        assertNotNull("Subscription node not found", subscription);

        // Original service
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                subscriptionDetails.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-16 00:00:00"));

        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1152.00");

        // Upgraded service
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSubscriptionDetails.getServiceId(),
                upgradedSubscriptionDetails.getPriceModel().getKey(),
                upgradedSubscriptionDetails.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2015-06-16 00:00:00"),
                DateTimeHandling.calculateMillis("2015-07-01 00:00:00"));

        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1152.00");

        eva.assertOverallCosts("EUR", "2304.00");
    }

}
