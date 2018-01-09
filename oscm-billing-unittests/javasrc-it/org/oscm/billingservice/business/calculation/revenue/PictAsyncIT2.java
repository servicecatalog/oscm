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
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.PictAsyncSetup2;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author baumann
 * 
 */
public class PictAsyncIT2 extends BillingIntegrationTestBase {

    private final PictAsyncSetup2 testSetup = new PictAsyncSetup2();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void testcase52() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario52();
        // when billing run performed
        performBillingRun(28, "2013-07-30 13:00:00");

        // last upgrade then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_52", 2);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_52");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", 2.880952380952381, "244.88");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 2.880952380952381, "1",
                "432.14");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.880952380952381,
                "17.29");

        // assert parameters

        eva.assertParametersCosts(priceModel, "112.36");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1370887200000", "1372629600000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.880952380952381, "8.64", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.880952380952381, "86.43", "103.72", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "17.29");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.880952380952381,
                "17.29");

        eva.assertParameterCosts(parameter, "112.36");

        eva.assertPriceModelCosts(priceModel, "EUR", "831.67");
        // first upgrade then
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_52", 1);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                "PICT_TEST_52");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 0.14285714285714285, "104.86");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 0.14285714285714285, "1",
                "62.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.14285714285714285,
                "0.86");

        eva.assertPriceModelCosts(priceModel, "EUR", "421.72");
        eva.assertOverallCosts("940.04", "EUR", "940.04");
        eva.assertOverallDiscount("25.0", "313.35", "940.04", "1253.39");
    }

    @Test
    public void testcase52_2() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario52_2();

        // when billing run performed
        performBillingRun(28, "2013-08-30 13:00:00");

        // last upgrade then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_52_2", 2);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_52_2");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", 2.880952380952381, "244.88");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 2.880952380952381, "1",
                "432.14");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.880952380952381,
                "17.29");

        // assert parameters
        eva.assertParametersCosts(priceModel, "112.36");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1370887200000", "1372629600000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.880952380952381, "8.64", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.880952380952381, "86.43", "103.72", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "17.29");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.880952380952381,
                "17.29");

        eva.assertParameterCosts(parameter, "112.36");

        eva.assertPriceModelCosts(priceModel, "EUR", "831.67");
        // first upgrade then
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_52_2", 1);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                "PICT_TEST_52_2");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 0.14285714285714285, "104.86");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 0.14285714285714285, "1",
                "62.00");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.14285714285714285,
                "0.86");

        eva.assertPriceModelCosts(priceModel, "EUR", "421.72");

        eva.assertOverallCosts("EUR", "940.04");
        eva.assertOverallDiscount("25.0", "313.35", "940.04", "1253.39");

        //
        // second billing period;
        //

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_52_2", 2);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                "PICT_TEST_52_2");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", 4.428571428571429, "376.43");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 4.428571428571429, "1",
                "664.29");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.428571428571429,
                "26.57");

        // assert parameters

        eva.assertParametersCosts(priceModel, "172.72");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1372629600000", "1375308000000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.428571428571429, "13.29", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.428571428571429, "132.86", "159.43", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "26.57");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.428571428571429,
                "26.57");

        eva.assertParameterCosts(parameter, "172.72");

        eva.assertOverallCosts("1240.01", "EUR", "1240.01");

    }

    @Test
    public void testcase51() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario51();

        // when billing run performed
        performBillingRun(4, "2013-07-30 13:00:00");

        // 3. price model
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_51", 2);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_51" + "_SubID2");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-06-13 20:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "445.00", "1.0", "445.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "150.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "PICT51Customer", "1.0");

        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "150.00", "0",
                "1", "150.00", "1.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "150.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1, "7.00");

        // assert parameters
        eva.assertParametersCosts(priceModel, "40.00");
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-06-13 20:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "30.00", 1.0, "30.00", "37.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "7.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1, "7.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "667.00");

        // 1. price model
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_51", 0);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-06-08 20:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 20:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "150.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "PICT51Customer", "1.0");

        // assert stepped prices for user assignments
        steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "150.00", "0",
                "1", "150.00", "1.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "150.00");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");

        // assert parameters
        eva.assertParametersCosts(priceModel, "39.00");
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-06-08 20:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 20:00:00"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "30.00", 1.0, "30.00", "36.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "305.00");
        eva.assertOverallCosts("EUR", "729.00");
        eva.assertOverallDiscount("25.0", "243.00", "729.00", "972.00");
    }

    @Test
    public void testcase50() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario50();

        // when billing run performed
        performBillingRun(4, "2013-07-06 13:00:00");

        // price model last upgrade
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_50", 2);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-02 00:00:00",
                "2013-07-02 00:00:00");

        eva.assertNodeCount(eva.getBillingResult(),
                "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_50");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "20.00", "340.0", "6800.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "0.5833333333333334");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict50Customer",
                "340.0");
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "354.58");

        // assert parameters

        eva.assertParametersCosts(priceModel, "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1371492000000", "1372716000000", "15",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 340.0, "0.00", "15.0");

        eva.assertPriceModelCosts(priceModel, "EUR", "7154.58");

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_50", 0);
        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-06-02 00:00:00", "2013-07-02 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_50");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "144.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict50Customer",
                "144.0");

        steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "158.00");

        // event costs
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "15",
                "150.00");

        eva.assertGatheredEventsCosts(priceModel, "150.00");

        // assert parameters

        eva.assertParametersCosts(priceModel, "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1370800800000", "1370973600000", "4",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 48, "0.00", "4.0");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1370455200000", "1370800800000", "15",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 96, "0.00", "15.0");

        eva.assertPriceModelCosts(priceModel, "EUR", "308.00");
        eva.assertOverallVAT("19.0", "1417.89");
        eva.assertOverallCosts("7462.58", "EUR", "8880.47");

    }

    @Test
    public void testcase49() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario49();
        // when billing run performed
        performBillingRun(0, "2013-07-05 13:00:00");

        // price model last upgrade
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_49", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_49");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "73.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "5.0");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict49Customer",
                "73.0");
        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 5, "25.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 73, "219.00");
        eva.assertTotalRoleCosts(roleCosts, "244.00");

        // assert parameters

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1370800800000", "1371060000000", "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "10.00", 73.00, "25550.00", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 5, "875.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 73, "7665.00");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "20.00", 78.0, "54600.00", "63140.00", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "8540.00");
        eva.assertParameterCosts(parameter, "88690.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "88944.00");

        eva.assertOverallCosts("88944.00", "EUR", "105843.36");
        eva.assertOverallVAT("19.0", "16899.36");

    }

    @Test
    public void testcase49_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario49_1();
        // when billing run performed
        performBillingRun(0, "2013-07-05 13:00:00");

        // price model last upgrade
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_49_1", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_49_1");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "73.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "3.0");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict49_1Customer",
                "73.0");
        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 3);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 3, "15.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 24, "72.00");
        eva.assertRoleCost(roleCosts, "USER", "4.00", 49, "196.00");

        // assert parameters
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1370800800000", "1371060000000", "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "10.00", 73, "25550.00", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "20.00", 76.0, "53200.00", "63105.00", "35.0");

        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 3, "525.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 24, "2520.00");
        eva.assertRoleCost(roleCosts, "USER", "4.00", 49, "6860.00");

    }

    @Test
    public void testcase49_2() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario49_2();
        // when billing run performed
        performBillingRun(0, "2013-07-05 13:00:00");

        // price model last upgrade
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_49_2", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_49_2");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "72.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "2.0");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict49_2Customer",
                "72.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 2, "10.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 72, "216.00");
        eva.assertTotalRoleCosts(roleCosts, "226.00");

        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1370800800000", "1371059940000", "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "10.00", 72.0, "25200.00", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 2, "350.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 72, "7560.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "85146.00");
    }

    @Test
    public void testcase49_3() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario49_3();
        // when billing run performed
        performBillingRun(0, "2013-07-05 13:00:00");

        // price model last upgrade
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_49_3", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_49_3");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "72.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "2.0");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict49_3Customer",
                "72.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 3);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 1, "5.00");
        eva.assertRoleCost(roleCosts, "USER", "4.00", 2, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 72, "216.00");
        eva.assertTotalRoleCosts(roleCosts, "229.00");

        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1370800800000", "1371059940000", "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "10.00", 72.0, "25200.00", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 1, "175.00");
        eva.assertRoleCost(roleCosts, "USER", "4.00", 2, "280.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 72, "7560.00");
        eva.assertTotalRoleCosts(roleCosts, "8015.00");
    }

    @Test
    public void testcase49_4() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario49_4();

        // when billing run performed
        performBillingRun(0, "2013-07-05 13:00:00");

        // price model last upgrade
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_49_4", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_49_4");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "72.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "2.0");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict49_4Customer",
                "72.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 2, "10.00");

        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 72, "216.00");
        eva.assertTotalRoleCosts(roleCosts, "226.00");

        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1370800800000", "1371059940000", "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "10.00", 72, "25200.00", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 2, "350.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 72, "7560.00");
        eva.assertTotalRoleCosts(roleCosts, "7910.00");
    }

    @Test
    public void testcase48() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario48();
        // when billing run performed
        performBillingRun(28, "2013-07-29 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_48", 0);

        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-05-01 00:00:00",
                "2013-06-01 00:00:00");

        // count subscriptions
        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_48");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "5.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "4.00", 0.06547619047619048, "0.26");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), 0.06547619047619048, "1", "0.98");
        eva.assertUserAssignmentCostsByUser(priceModel, "Pict48Customer",
                0.06547619047619048);

        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "15.00", "0", "1",
                "0.98", "0.06547619047619048");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "0.98");

        // assert parameters
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1369764000000", "1369803600000", "15",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.06547619047619048, "0.00", "15.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.06547619047619048, "0.98", "0.98", "15.0");

        eva.assertPriceModelCosts(priceModel, "EUR", "7.22");
        eva.assertOverallVAT("19.0", "1.37");
        eva.assertOverallCosts("7.22", "EUR", "8.59");

        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-06-01 00:00:00", "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_48" + "_SubID2");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "4.00", 0.3630952380952381, "1.45");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), 0.3630952380952381, "1", "5.45");
        eva.assertUserAssignmentCostsByUser(priceModel, "Pict48Customer",
                0.3630952380952381);

        // assert stepped prices for user assignments
        steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "15.00", "0", "1",
                "5.45", "0.3630952380952381");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "5.45");

        // assert parameters

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1371013200000", "1371232800000", "15",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.3630952380952381, "0.00", "15.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.3630952380952381, "5.45", "5.45", "15.0");

        eva.assertPriceModelCosts(priceModel, "EUR", "12.35");

        eva.assertOverallVAT("19.0", "2.35");
        eva.assertOverallCosts("12.35", "EUR", "14.70");
    }

    @Test
    public void testcase47() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario47();
        // when billing run performed
        performBillingRun(28, "2013-07-01 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_47", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_47");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertNullOneTimeFee();

        eva.assertGatheredEventsCosts(priceModel, "114.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "5.0", "0.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "12.00", "6.0", "2", "72.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1, "1.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 5, "15.00");
        eva.assertTotalRoleCosts(roleCosts, "16.00");

        // assert parameters

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1367344800000", "1369936800000", "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "7.00", 5, "1225.00", "35.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 6, "0.00", "0.00", "35.0");
        eva.assertParameterCosts(parameter, "1225.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1427.00");
        eva.assertOverallVAT("19.0", "271.13");
        eva.assertOverallCosts("1427.00", "EUR", "1698.13");

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_47", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-03 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                "PICT_TEST_47");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertOneTimeFee(priceModel, "5.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "5.00");
        eva.assertOverallVAT("19.0", "0.95");
        eva.assertOverallCosts("5.00", "EUR", "5.95");

    }

    @Test
    public void testcase46() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario46();
        // when billing run performed
        performBillingRun(28, "2013-06-29 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_46", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva = new BillingResultEvaluator(billingResult);
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_46");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-30 20:00:00"));

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "445.00", "1.0", "445.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict46Customer", "1.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.0", "1", "0.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1, "1.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-30 20:00:00"), "10",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.00", 1, "0.00", "10.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 1.0, "0.00", "0.00", "10.0");

        eva.assertPriceModelCosts(priceModel, "EUR", "446.00");
        eva.assertOverallVAT("5.0", "22.30");
        eva.assertOverallCosts("446.00", "EUR", "468.30");

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_46", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 2);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-04-07 20:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 07:00:00"));
        eva.assertOneTimeFee(priceModel, "5.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "445.00", "2.0", "890.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict46Customer", "2.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "2.0", "1", "0.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 2, "6.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-07 20:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 07:00:00"), "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.00", 2, "0.00", "35.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 2.0, "0.00", "0.00", "35.0");

        // Subscription after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-04-17 07:00:00"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "445.00", "2.0", "890.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict46Customer", "2.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "2.0", "1", "0.00");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 0.34523809523809523,
                "1.04");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.7142857142857143,
                "1.43");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.9404761904761905,
                "0.94");

        // assert parameters 10
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-22 11:00:00"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"), "10",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.9345238095238095, "0.00", "10.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.9345238095238095, "0.00", "0.00", "10.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.9345238095238095,
                "0.00");

        // assert parameters 4
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-17 11:00:00"),
                DateTimeHandling.calculateMillis("2013-04-22 11:00:00"), "4",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.7142857142857143, "0.00", "4.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.7142857142857143, "0.00", "0.00", "4.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.005952380952380952,
                "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.7083333333333334,
                "0.00");

        // assert parameters 35
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-17 07:00:00"),
                DateTimeHandling.calculateMillis("2013-04-17 11:00:00"), "35",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.35119047619047616, "0.00", "35.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.35119047619047616, "0.00", "0.00", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.34523809523809523,
                "0.00");
    }

    @Test
    public void testcase45() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario45();

        // when billing run performed
        performBillingRun(0, "2013-07-17 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_45", 0);

        // *** First billing period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-15 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-15 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // PriceModel after second upgrade (PU/Week)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-05-14 20:00:00"),
                DateTimeHandling.calculateMillis("2013-05-14 20:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        // First price model (PU/Day)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-05-07 20:00:00"),
                DateTimeHandling.calculateMillis("2013-05-13 20:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "7.0", "840.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict45Customer", "7.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "7.0", "1", "420.00");

        eva.assertOneTimeFee(priceModel, "20.00");

        eva.assertOverallCosts("1590.00", "EUR", "1590.00");

        // next BillingPeriod
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_45", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-15 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-15 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_45");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertNullOneTimeFee();

        // PriceModel after second upgrade (PU/Week)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-05-14 20:00:00"),
                DateTimeHandling.calculateMillis("2013-05-15 20:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict45Customer", "1.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "2.0", "2", "868.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1616.00");

        eva.assertOverallCosts("1616.00", "EUR", "1616.00");
    }

    @Test
    public void testcase45_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario45_1();
        // when billing run performed
        performBillingRun(0, "2013-07-17 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_45_1", 2);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-15 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-15 00:00:00"));

        assertNull("no result.xml should exist", billingResult);

    }

    @Test
    public void testcase44() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario44();
        // when billing run performed
        performBillingRun(15, "2013-07-17 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_44", 1);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva = new BillingResultEvaluator(billingResult);
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_44");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee();

        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                1370124000000L, 1372716000000L);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 4.285714285714286, "3145.71");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict44Customer",
                4.285714285714286);

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser2", 4.285714285714286);
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser1", 1.214285714285715);

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 9.785714285714285, "3",
                "4247.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 9.785714285714285,
                "58.71");
        eva.assertPriceModelCosts(priceModel, "EUR", "7451.42");
        eva.assertOverallCosts("7451.42", "EUR", "8420.10");

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_44", 1);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);

        eva = new BillingResultEvaluator(billingResult);
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 2);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                "PICT_TEST_44");

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        // the one time fee must charged from the first service, see next
        // subscription, here factor must be 0
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                1368036000000L, 1368727200000L);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "445.00", "1.0", "445.00");

        eva.assertOneTimeFee(priceModel, "5.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict44Customer", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "1.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "7.00", "2.0", "2", "14.00");
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1368036000000", "1368727200000", "2",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.MONTH.name(),
                "8.00", 1.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.MONTH.name(),
                "0.00", 2.0, "0.00", "0.00", null);
        eva.assertTotalRoleCosts(roleCosts, "0.00");

        eva.assertOptionCosts(option, "8.00");
        eva.assertParameterCosts(parameter, "8.00");

        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                1368727200000L, 1368813600000L);
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 0.14285714285714285, "104.86");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict44Customer",
                0.14285714285714285);
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser1", 0.13690476190476192);

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser2", 0.14285714285714285);

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 0.4226190476190476, "3",
                "183.42");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.14285714285714285,
                "1.14");

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.27976190476190477,
                "1.68");

        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                1368036000000L, 1368727200000L);
        eva.assertGatheredEventsCosts(priceModel, "114.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "445.00", 1.0, "445.00");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict44Customer", 1.0);
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser2", 1.0);

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "7.00", 2.0, "2", "14.00");
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1368036000000", "1368727200000", "2",
                "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.MONTH.name(),
                "8.00", 1.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.MONTH.name(),
                "0.00", 2.0, "0.00", "0.00", null);
        eva.assertTotalRoleCosts(roleCosts, "0.00");

        eva.assertOptionCosts(option, "8.00");
        eva.assertParameterCosts(parameter, "8.00");

        eva.assertOverallCosts("2793.89", "EUR", "3157.10");

    }

    @Test
    public void testcase43() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario43();
        // when billing run performed
        performBillingRun(15, "2013-06-17 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_43", 2);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_43");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict43Customer", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "10.00", "3.0", "3", "30.00");

        // user role
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "160.00");

        // next price model
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_43", 1);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-05-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                "PICT_TEST_43");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", 0.017857142857142856, "13.11");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict43Customer",
                0.017857142857142856);
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser1", 0.011904761904761904);
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser2", 0.017857142857142856);
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 0.047619047619047616, "3",
                "20.67");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.017857142857142856,
                "0.14");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.02976190476190476,
                "0.18");

        eva.assertPriceModelCosts(priceModel, "EUR", "288.10");
        eva.assertOverallCosts("448.10", "EUR", "506.35");
    }

    @Test
    public void testcase42() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario42();

        // when billing run performed
        performBillingRun(0, "2013-07-04 13:00:00");

        // then - period 1
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_42", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-05-02 00:00:00",
                "2013-06-02 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_42");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "85.00", "1.0", "85.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict42Customer", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "1.0");
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "270.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "20.00", 0.0013440860215053765,
                "0.03");
        eva.assertRoleCost(roleCosts, "USER", "10.00", 0.6935483870967742,
                "6.94");
        eva.assertRoleCost(roleCosts, "GUEST", "30.00", 1.3051075268817205,
                "39.15");
        eva.assertTotalRoleCosts(roleCosts, "46.12");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1367431200000", "1368727200000", "35",
                "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                1.0, "42.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.30", 2.0, "21.00", "1635.04", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "20.00", 0.0013440860215053765,
                "0.94");
        eva.assertRoleCost(roleCosts, "USER", "10.00", 0.6935483870967742,
                "242.74");
        eva.assertRoleCost(roleCosts, "GUEST", "30.00", 1.3051075268817205,
                "1370.36");
        eva.assertTotalRoleCosts(roleCosts, "1614.04");
        eva.assertParameterCosts(parameter, "1677.04");

        eva.assertGatheredEventsCosts(priceModel, "114.00");
        eva.assertOverallCosts("2192.16", "EUR", "2301.77");

        // then - period 2
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_42", 0);
        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-02 00:00:00", "2013-05-02 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_42");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertOverallCosts("25.00", "EUR", "26.25");

        // then - period 3
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_42", 1);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-06-02 00:00:00", "2013-07-02 00:00:00"));
    }

    @Test
    public void testcase41() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario41();

        // when billing run performed
        performBillingRun(15, "2013-08-26 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_41", 1);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 2);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_41");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        // <PriceModel id="172003" calculationMode="PRO_RATA">
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-12 22:05:00"),
                DateTimeHandling.calculateMillis("2013-07-12 23:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "10.00", 0.9166666666666666, "9.17");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict41Customer",
                0.9166666666666666);

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "20.00", 0.9166666666666666, "1",
                "18.33");

        // user role
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 0.9166666666666666,
                "1.83");

        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1373659500000", "1373662800000", "500", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                0.9166666666666666, "2291.67", "500.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "10.00", 0.9166666666666666, "4583.33", "5500.00", "500.0");

        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 0.9166666666666666,
                "916.67");
        eva.assertTotalRoleCosts(roleCosts, "916.67");

        eva.assertParameterCosts(parameter, "7791.67");

        eva.assertPriceModelCosts(priceModel, "EUR", "7821.00");

        // next sub
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-10 22:00:00"),
                DateTimeHandling.calculateMillis("2013-07-12 22:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "10.00", "48.0", "480.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict41Customer",
                "48.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "20.00", "48.0", "1", "960.00");

        // user role
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 48, "96.00");

        // parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1373572800000", "1373659200000", "500", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(), 24,
                "60000.00", "500.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "10.00", 24, "120000.00", "144000.00", "500.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 24.00, "24000.00");

        eva.assertParameterCosts(parameter, "204000.00");

        // next parameter
        // parameter
        parameter = BillingXMLNodeSearch
                .getParameterNode(priceModel, "LONG_NUMBER", "1373486400000",
                        "1373572800000", "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(), 24,
                "565320.00", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "10.00", 24, "1130640.00", "1356768.00", "4711.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 24.0, "226128.00");
        eva.assertTotalRoleCosts(roleCosts, "226128.00");

        eva.assertParameterCosts(parameter, "1922088.00");
        eva.assertParametersCosts(priceModel, "2126088.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "2127649.00");

        eva.assertOverallDiscount("25.0", "533867.50", "1601602.50",
                "2135470.00");
        eva.assertOverallVAT("5.0", "80080.13");

        eva.assertOverallCosts("1601602.50", "EUR", "1681682.63");

    }

    @Ignore
    @Test
    public void testcase41_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario41_1();

        // when billing run performed
        performBillingRun(15, "2013-08-26 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_41_1", 1);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 2);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_41_1");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                voSubscriptionDetails.getPriceModel().getKey());

        assertNotNull("Price Model doesn't exist", priceModel);

        // <PriceModel id="172003" calculationMode="PRO_RATA">
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-12 22:05:00"),
                DateTimeHandling.calculateMillis("2013-07-12 23:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "10.00", 0.9166666666666666, "9.17");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict41_1Customer",
                0.9166666666666666);

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "20.00", 0.9166666666666666, "1",
                "18.33");

        // user role
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 0.9166666666666666,
                "1.83");

        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1373659500000", "1373662800000", "500", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                0.9166666666666666, "2291.67", "500.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "10.00", 0.9166666666666666, "4583.33", "5500.00", "500.0");

        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 0.9166666666666666,
                "916.67");
        eva.assertTotalRoleCosts(roleCosts, "916.67");

        eva.assertParameterCosts(parameter, "7791.67");

        eva.assertPriceModelCosts(priceModel, "EUR", "7821.00");

        // next sub
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-10 22:00:00"),
                DateTimeHandling.calculateMillis("2013-07-12 22:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "10.00", "48.0", "480.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict41_1Customer",
                "48.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "20.00", "48.0", "1", "960.00");

        // user role
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 48, "96.00");

        // parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1373572800000", "1373659200000", "500", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(), 24,
                "60000.00", "500.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "10.00", 24, "120000.00", "144000.00", "500.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 24.00, "24000.00");

        eva.assertParameterCosts(parameter, "204000.00");

        // next parameter
        // parameter
        parameter = BillingXMLNodeSearch
                .getParameterNode(priceModel, "LONG_NUMBER", "1373486400000",
                        "1373572800000", "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(), 24,
                "565320.00", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "10.00", 24, "1130640.00", "1356768.00", "4711.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 24.0, "226128.00");
        eva.assertTotalRoleCosts(roleCosts, "226128.00");

        eva.assertParameterCosts(parameter, "1922088.00");
        eva.assertParametersCosts(priceModel, "2126088.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "2127649.00");

        eva.assertOverallDiscount("25.0", "533867.50", "1601602.50",
                "2135470.00");
        eva.assertOverallVAT("5.0", "80080.13");

        eva.assertOverallCosts("1601602.50", "EUR", "1681682.63");

    }

    @Test
    public void testcase40() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario40();

        // when billing run performed
        performBillingRun(15, "2013-09-26 13:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_40", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-03 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_40", 1);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-08-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-03 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                1375862400000L, 1376121600000L);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "445.00", "1.0", "445.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict40Customer", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "3.00");

        // no user role cost

        // parameter
        Node parameter = BillingXMLNodeSearch
                .getParameterNode(priceModel, "LONG_NUMBER", "1375862400000",
                        "1376121600000", "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 1,
                "4711.00", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 2, "0.00", "28266.00", "4711.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 1, "9422.00");
        eva.assertRoleCost(roleCosts, "GUEST", "4.00", 1, "18844.00");
        eva.assertTotalRoleCosts(roleCosts, "28266.00");

        eva.assertNullOneTimeFee();

        eva.assertParameterCosts(parameter, "32977.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "33425.00");

        eva.assertOverallCosts("33425.00", "EUR", "35096.25");
        eva.assertOverallVAT("5.0", "1671.25");
    }

    @Test
    public void testcase39() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario39();

        // when
        performBillingRun(28, "2013-09-29 13:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_39", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00"));
    }

    @Test
    public void testcase38() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario38();

        // when billing run performed
        performBillingRun(0, "2013-11-03 13:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_38", 1);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-10-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 2);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-10-07 10:00:00"),
                DateTimeHandling.calculateMillis("2013-10-10 10:00:00"));
        eva.assertGatheredEventsCosts(priceModel, "150.00");

        // Onetime fee is Zero in price model...
        eva.assertNullOneTimeFee(priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "10.00", "4.0", "40.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict38Customer", "4.0");

        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "10.00");

        // no user role cost for sub period
        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1381132800000", "1381392000000", "2",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);

        eva.assertParameterPeriodFee(option, PricingPeriod.DAY.name(), "1.00",
                4.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.DAY.name(), "0.00",
                4.0, "0.00", "16.00", null);

        eva.assertOptionCosts(option, "20.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);

        // role cost
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "4.00", 4, "16.00");
        eva.assertTotalRoleCosts(roleCosts, "16.00");

        eva.assertParameterCosts(parameter, "20.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "220.00");

        // next price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-10-10 10:00:00"),
                DateTimeHandling.calculateMillis("2013-10-14 23:10:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.02", "109.16666666666667", "2.18");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict38Customer",
                109.16666666666667);
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "0.02", 109.16666666666667, "1",
                "2.18");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        // role cost
        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 109.16666666666667,
                "4.37");

        // parameter
        parameter = BillingXMLNodeSearch
                .getParameterNode(priceModel, "LONG_NUMBER", "1381392000000",
                        "1381785000000", "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.01", 109.16666666666667, "5142.84", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.01", 109.16666666666667, "5142.84", "25714.21", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 109.16666666666667,
                "20571.37");

        eva.assertTotalRoleCosts(roleCosts, "20571.37");
        eva.assertParameterCosts(parameter, "30857.05");
        eva.assertPriceModelCosts(priceModel, "EUR", "30890.78");

        // next subscription
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-10-14 23:15:00"),
                DateTimeHandling.calculateMillis("2013-10-27 20:00:00"));

        // price model update considered for base price
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.02", 309.75, "6.20");

        eva.assertUserAssignmentCostsByUser(priceModel, "Pict38Customer",
                309.75);
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "0.02", 309.75, "1", "6.20");

        // Onetime fee was already charged before suspend
        eva.assertNullOneTimeFee(priceModel);

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        // role cost
        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 309.75, "12.39");

        // parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1382856900000", "1382900400000", "400", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.01", 12.083333333333334, "48.33", "400.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.01", 12.083333333333334, "48.33", "241.66", "400.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 12.083333333333334,
                "193.33");
        eva.assertTotalRoleCosts(roleCosts, "193.33");

        eva.assertParameterCosts(parameter, "289.99");

        // next parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1382829000000", "1382856900000", "500", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.01", 7.75, "38.75", "500.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.01", 7.75, "38.75", "193.75", "500.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 7.75, "155.00");
        eva.assertTotalRoleCosts(roleCosts, "155.00");

        eva.assertParameterCosts(parameter, "232.50");

        // next parameter
        parameter = BillingXMLNodeSearch
                .getParameterNode(priceModel, "LONG_NUMBER", "1381785300000",
                        "1382829000000", "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.01", 289.9166666666667, "13657.97", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.01", 289.9166666666667, "13657.97", "68289.87", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 289.9166666666667,
                "54631.90");
        eva.assertTotalRoleCosts(roleCosts, "54631.90");

        eva.assertParameterCosts(parameter, "81947.84");

        eva.assertParametersCosts(priceModel, "82470.33");

        eva.assertPriceModelCosts(priceModel, "EUR", "82495.12");

        eva.assertOverallDiscount("25.0", "28401.48", "85204.42", "113605.90");
        eva.assertOverallVAT("13.0", "11076.57");
        eva.assertOverallCosts("85204.42", "EUR", "96280.99");
    }

    @Test
    public void testcase37() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario37();

        // when
        performBillingRun(28, "2013-12-28 13:00:00");

        // then - period 1
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_37", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-01 00:00:00", "2013-11-01 00:00:00");
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), 1380700800000L, 1381392000000L);
        eva.assertGatheredEventsCosts(priceModel, "300.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "50.00", "0.25771812080536916", "12.89");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict37Customer",
                "0.25771812080536916");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "0.00022371364653243848");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "2.00", "0.2579418344519016", "2",
                "0.52");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.03355704697986577,
                "0.07");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 0.22438478747203577,
                "0.67");
        eva.assertTotalRoleCosts(roleCosts, "0.74");
        eva.assertOneTimeFee(priceModel, "25.00");
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1380700800000", "1381392000000", "35",
                "INTEGER");
        Node steppedParamPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParamPrices, "0.00", "1.20", "0", "50",
                "42.00", "35");
        eva.assertSteppedPricesAmount(steppedParamPrices, "42.00");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.30", 0.2579418344519016, "2.71", "2.71", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.22438478747203577,
                "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "13.53");
        eva.assertPriceModelCosts(priceModel, "EUR", "352.68");
        eva.assertOverallDiscount("25.0", "88.17", "264.51", "352.68");
        eva.assertOverallCosts("264.51", "EUR", "264.51");

        // then - period 2
        assertNull(getEvaluator(sub.getKey(), "2013-11-01 00:00:00",
                "2013-12-01 00:00:00"));
    }

    @Test
    public void testcase36() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario36();

        // when billing run performed
        performBillingRun(15, "2013-09-16 13:00:00");

        // then - assert billing result 1
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_36", 1);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        // price model 1
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-20 02:00:00"),
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"));
        eva.assertGatheredEventsCosts(priceModel, "150.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "4.00", "2.0", "8.00");

        // price model 1 - user assignments
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict36Customer", "2.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "1.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "1.00", "0", "1",
                "1.00", "1");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "6.00");

        // price model 1 - user roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "5.00", 1.0, "5.00");
        eva.assertRoleCost(roleCosts, "GUEST", "6.00", 1.9942460317460318,
                "11.97");
        eva.assertRoleCost(roleCosts, "ADMIN", "4.00", 0.005753968253968254,
                "0.02");
        eva.assertTotalRoleCosts(roleCosts, "16.99");

        // price model 1 - parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-20 02:00:00"),
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"), "60",
                "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "0.10", 2.0, "12.00", "60.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.20", 3.0, "36.00", "1055.31", "60.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "GUEST", "6.00", 1.9942460317460318,
                "717.93");
        eva.assertRoleCost(roleCosts, "ADMIN", "4.00", 0.005753968253968254,
                "1.38");
        eva.assertRoleCost(roleCosts, "USER", "5.00", 1.0, "300.00");
        eva.assertTotalRoleCosts(roleCosts, "1019.31");
        eva.assertParameterCosts(parameter, "1067.31");

        // price model 2
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-08 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-15 02:00:00"));
        eva.assertGatheredEventsCosts(priceModel, "150.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", 0.9523809523809523, "0.00");
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "175.00");

        // price model 2 - parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-07-08 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-15 02:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.9523809523809523, "0.00", "0.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 0.9523809523809523,
                "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");
        eva.assertParametersCosts(priceModel, "0.00");

        eva.assertOverallCosts("1423.30", "EUR", "1423.30");

        // then - assert billing result 2
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("389.00", "EUR", "389.00");
    }

    @Test
    public void testcase49_5() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario49_5();
        // when billing run performed
        performBillingRun(4, "2013-07-06 13:00:00");

        // price model last upgrade
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_49_5", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-06-05 20:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));

        // assert parameters
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-09 20:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"), "20",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.7055555555555556, "28.22", "20.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "ADMIN", "15.00", 1.7055555555555557,
                "511.67");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.7055555555555556,
                "70.56");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 2.4111111111111114, "964.44", "1546.67", "20.0");

        eva.assertTotalRoleCosts(roleCosts, "582.23");

    }

    @Test
    public void testcase35() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario35();
        // when billing run performed
        performBillingRun(4, "2013-09-16 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_35", 0);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-02 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                1373270400000L, 1375056000000L);

        // event

        eva.assertGatheredEventsCosts(priceModel, "300.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "1.00", 20.666666666666668, "20.67");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict35Customer",
                "20.666666666666668");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.00625");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "0.00", 21.67291666666667, "2",
                "0.00");

        eva.assertOneTimeFee(priceModel, "1.00");

        // 1. parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1374959400000", "1375056000000", "200", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                1.1180555555555556, "559.03", "200.0");
        // assert stepped prices for parameter
        Node steppedParamPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParamPrices, "0.00", "3.00", "0", "100",
                "300.00", "100");
        eva.assertSteppedPrice(steppedParamPrices, "300.00", "2.00", "100",
                "200", "200.00", "100");
        eva.assertSteppedPricesAmount(steppedParamPrices, "500.00");

        // no user and role costs

        eva.assertParameterCosts(parameter, "559.03");

        // 2. parameter

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER", "1373847000000", "1374959400000", "100", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                12.875, "3862.50", "100.0");

        // assert stepped prices for parameter
        steppedParamPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParamPrices, "0.00", "3.00", "0", "100",
                "300.00", "100");

        eva.assertSteppedPricesAmount(steppedParamPrices, "300.00");
        // no user and role costs

        eva.assertParameterCosts(parameter, "3862.50");
        // 3. parameter

        parameter = BillingXMLNodeSearch
                .getParameterNode(priceModel, "LONG_NUMBER", "1373270400000",
                        "1373847000000", "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                6.673611111111111, "18722.82", "4711.0");

        // assert stepped prices for parameter
        steppedParamPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        // last step
        eva.assertSteppedPrice(steppedParamPrices, "600.00", "0.50", "300",
                "null", "2205.50", "4411");

        eva.assertSteppedPricesAmount(steppedParamPrices, "2805.50");
        eva.assertParameterCosts(parameter, "18722.82");

        eva.assertParametersCosts(priceModel, "23144.35");
        eva.assertPriceModelCosts(priceModel, "EUR", "23466.02");
        eva.assertOverallCosts("23466.02", "EUR", "27924.56");
        eva.assertOverallVAT("19.0", "4458.54");

    }

    @Test
    public void testcase34() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario34();

        // when billing run performed
        performBillingRun(15, "2013-09-16 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_34", 0);

        // first billing period

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        // upgraded price model (PU/Week)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-14 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "3.0", "3.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict34Customer", "3.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "3.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "6.0", "2", "18.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "9.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 3.0, "3.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 3.0, "6.00");

        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);

        eva.assertSteppedPricesAmount(steppedUserAssPrices, "18.00");
        eva.assertOneTimeFee(priceModel, "5.00");

        // parameter 100
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-14 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"), "100",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 3.0, "300.00", "100.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "2.00", 6.0, "1200.00", "2100.00", "100.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "900.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 3.0, "300.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 3.0, "600.00");
        eva.assertParameterCosts(parameter, "2400.00");

        // parameter 4711 is not charged, because it's only valid
        // in the free period.

        // next billing period

        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 2);

        // upgraded price model (PU/Week) before suspend
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 21:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "1.0", "1.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict34Customer", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "2", "3.00");

        // parameter 200
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 21:00:00"), "200",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.5952380952380952, "119.05", "200.0");

        // parameter 100
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"), "100",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.40476190476190477, "40.48", "100.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.80952380952380954, "161.90", "283.33", "100.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "121.43");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.40476190476190477,
                "40.48");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.40476190476190477,
                "80.95");

        // next sub: pgraded price model (PU/Week) after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-08-11 21:00:00"),
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "3.0", "3.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict34Customer", "3.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "3.0", "1", "6.00");

        // parameter 200
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-11 21:00:00"),
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"), "200",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 3.0, "600.00", "200.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "600.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 3.0, "600.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2412.00");

        eva.assertOverallCosts("3695.20", "EUR", "4397.29");

    }

    @Test
    public void testcase34_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario34_1();

        // when billing run performed
        performBillingRun(15, "2013-09-16 13:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_34_1", 0);

        // first billing period

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        // Upgraded price model (PU/Week)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-14 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "3.0", "3.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict34_1Customer",
                "3.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "3.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "6.0", "2", "18.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "9.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 3.0, "3.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 3.0, "6.00");

        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);

        eva.assertSteppedPricesAmount(steppedUserAssPrices, "18.00");
        eva.assertOneTimeFee(priceModel, "5.00");

        // parameter 100
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-14 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"), "100",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 3.0, "300.00", "100.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "2.00", 6.0, "1200.00", "2100.00", "100.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "900.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 3.0, "300.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 3.0, "600.00");
        eva.assertParameterCosts(parameter, "2400.00");

        // parameter 200 is not charged, because it starts in the overlapping
        // week, which is charged in the next billing period

        // parameter 4711 is not charged, because it's only valid
        // in the free period.

        // next billing period

        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        // Upgraded price model (PU/Week)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 21:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "1.0", "1.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict34_1Customer",
                "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "2", "3.00");

        // parameter 200
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 21:00:00"), "200",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.5952380952380952, "119.05", "200.0");

        // parameter 100
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"), "100",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.40476190476190477, "40.48", "100.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.80952380952380954, "161.90", "283.33", "100.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "121.43");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.40476190476190477,
                "40.48");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.40476190476190477,
                "80.95");
        eva.assertOverallCosts("1283.20", "EUR", "1527.01");

    }

    @Test
    public void testcase33() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario33();
        // when billing run performed
        performBillingRun(15, "2013-09-16 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_33", 2);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);
        eva.assertNullOneTimeFee();

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-18 20:00:00"),
                DateTimeHandling.calculateMillis("2013-07-28 24:00:00"));
        eva.assertGatheredEventsCosts(priceModel, "30.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "2.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "2.0");
        eva.assertUserAssignmentCostsFactor(priceModel, "PIC33Customer", "2.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "4.0", "2", "4.00");

        // parameter 20

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-23 22:00:00"),
                DateTimeHandling.calculateMillis("2013-07-28 24:00:00"), "20",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.7261904761904762, "17.43", "20.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 1.4523809523809523, "0.00", "0.00", "20.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.6547619047619048,
                "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.7976190476190476,
                "0.00");
        eva.assertParameterCosts(parameter, "17.43");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-18 20:00:00"),
                DateTimeHandling.calculateMillis("2013-07-23 22:00:00"), "15",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                1.2738095238095237, "22.93", "15.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 2.5476190476190474, "0.00", "0.00", "15.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 1.2738095238095237,
                "0.00");

        eva.assertRoleCost(roleCosts, "USER", "0.00", 1.2738095238095237,
                "0.00");
        eva.assertParameterCosts(parameter, "22.93");
        eva.assertPriceModelCosts(priceModel, "EUR", "74.36");

        // next price model

        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                1373097600000L, 1373392800000L);
        eva.assertGatheredEventsCosts(priceModel, "77.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "20.00", "2.0", "40.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "PIC33Customer", "2.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "1", "2.00");

        // parameter 15

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-06 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-09 20:00:00"), "15",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 2.0,
                "36.00", "15.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 2.0, "0.00", "0.00", "15.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 2.0, "0.00");

        eva.assertParameterCosts(parameter, "36.00");

        eva.assertOverallCosts("229.36", "EUR", "229.36");

        // Next billing period
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);
        eva.assertNullOneTimeFee();

        // ///////////////
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-28 24:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 21:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "1.0", "0.00");

        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel, "PIC33Customer", "1.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "2", "2.00");

        // parameter 20

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-28 24:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 21:00:00"), "20",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 1.0,
                "24.00", "20.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 2.0, "0.00", "0.00", "20.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "0.00");

        eva.assertRoleCost(roleCosts, "USER", "0.00", 2.0, "0.00");
        eva.assertParameterCosts(parameter, "24.00");
        eva.assertOverallCosts("26.00", "EUR", "26.00");

    }

    @Test
    // second billing period not ready
    public void testcase32() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario32();
        // when billing run performed
        performBillingRun(28, "2013-09-29 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_32", 2);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2013-07-30 20:00:00"),
                DateTimeHandling.calculateMillis("2013-07-30 20:00:00"));

        // The price model occurs in the billing result because the one time fee
        // must be charged. However, the price model one time fee is 0.00, thus
        // the price model costs are zero....
        eva.assertPriceModelCosts(priceModel, "EUR", "0.00");

        // next price Model
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_32", 1);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-07-29 20:00:00"), DateTimeHandling
                .calculateMillis("2013-07-30 20:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.14285714285714285", "104.86");

        eva.assertUserAssignmentCostsFactor(priceModel, "PIC32Customer",
                "0.14285714285714285");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.14285714285714285,
                "1.14");
        eva.assertTotalRoleCosts(roleCosts, "1.14");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "0.14285714285714285",
                "1", "62.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "168.00");

        // next price Model

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_32", 0);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-07-16 10:00:00"), DateTimeHandling
                .calculateMillis("2013-07-28 24:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "20.00", "2.0", "40.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "PIC32Customer", "2.0");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "1", "2.00");

        eva.assertOneTimeFee(priceModel, "5.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-16 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-28 24:00:00"), "500",
                "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 2.0,
                "1900.00", "500.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 2.0, "1000.00", "2000.00", "500.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "1000.00");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 2.0, "1000.00");
        eva.assertParameterCosts(parameter, "3900.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "3947.00");
        eva.assertOverallCosts("3086.25", "EUR", "3086.25");

        eva.assertOverallDiscount("25.0", "1028.75", "3086.25", "4115.00");

    }

    @Test
    public void testcase31() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario31();

        // when
        performBillingRun(28, "2013-09-29 13:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_31", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00"));
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00"));
    }

    @Test
    public void testcase30() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario30();

        // when billing run performed
        performBillingRun(3, "2013-10-16 13:00:00");

        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_30", 0);

        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-07-13 00:00:00",
                "2013-08-13 00:00:00");

        // count subscriptions
        eva.assertNodeCount(eva.getBillingResult(),
                "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_30" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-07-11 10:00:00"), DateTimeHandling
                .calculateMillis("2013-08-11 24:00:00"));

        assertNotNull("Price Model doesn't exist", priceModel);

        // events
        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(gatheredEvents, "FILE_DOWNLOAD", "8.00", "15",
                "120.00");
        eva.assertEventCosts(gatheredEvents, "FOLDER_NEW", "2.00", "15",
                "30.00");

        eva.assertGatheredEventsCosts(priceModel, "150.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "2.00", "5.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "5.0", "1", "9.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict30Customer", "5.0");
        // no role cost

        // Parameter 100 and second parameter 50 are not charged, because they
        // start in the overlapping week, which is charged in the next billing
        // period
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-14 12:00:00"),
                DateTimeHandling.calculateMillis("2013-08-11 24:00:00"), "50",
                "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                4.071428571428571, "203.57", "50.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 4.071428571428571, "0.00", "0.00", "50.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 4.071428571428571,
                "0.00");
        eva.assertParameterCosts(parameter, "203.57");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-11 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-14 12:00:00") + "",
                "35", "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.9285714285714286, "32.50", "35.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.9285714285714286, "0.00", "0.00", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.011904761904761904,
                "0.00");
        eva.assertParameterCosts(parameter, "32.50");
        eva.assertParametersCosts(priceModel, "236.07");

        eva.assertPriceModelCosts(priceModel, "EUR", "405.07");
        eva.assertOverallCosts("405.07", "EUR", "482.03");

        // next period
        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-08-13 00:00:00", "2013-09-13 00:00:00");

        // count subscriptions
        eva.assertNodeCount(eva.getBillingResult(),
                "//Subscriptions/Subscription", 1);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_30" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-08-12 00:00:00"), DateTimeHandling
                .calculateMillis("2013-09-08 24:00:00"));

        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "2.00", "4.0", "8.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "4.0", "1", "6.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict30Customer", "4.0");
        // no role cost

        // parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-12 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-08 24:00:00") + "",
                "50", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                3.928571428571429, "196.43", "50.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 3.928571428571429, "0.00", "0.00", "50.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 3.928571428571429,
                "0.00");

        eva.assertParameterCosts(parameter, "196.43");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-12 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-12 12:00:00") + "",
                "100", "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.011904761904761904, "1.19", "100.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.011904761904761904, "0.00", "0.00", "100.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.011904761904761904,
                "0.00");

        eva.assertParameterCosts(parameter, "1.19");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-12 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-12 10:00:00") + "",
                "50", "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.05952380952380952, "2.98", "50.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.00", 0.05952380952380952, "0.00", "0.00", "50.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.005952380952380952,
                "0.00");
        eva.assertParameterCosts(parameter, "2.98");

        // events
        gatheredEvents = BillingXMLNodeSearch.getGatheredEventsNode(priceModel);
        assertNull("No events should be charged", gatheredEvents);

        eva.assertPriceModelCosts(priceModel, "EUR", "214.60");
        eva.assertOverallCosts("214.60", "EUR", "255.37");
    }

    @Test
    public void testcase29() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario29();

        // when billing run performed
        performBillingRun(4, "2013-08-11 13:00:00");

        // THEN - assert period 1
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_29", 1);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-07-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-05 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // count subscriptions
        eva.assertNodeCount(billingResult, "//Subscriptions/Subscription", 1);

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                billingResult, "PICT_TEST_29" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-07-09 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-10 12:00:00"));

        assertNotNull("Price Model doesn't exist", priceModel);

        // ////
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.01", "26.0", "0.26");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "0.02", "26.0", "1", "0.52");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict29Customer",
                "26.0");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.04");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.02", 0.25, "0.01");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-09 11:45:01") + "",
                DateTimeHandling.calculateMillis("2013-07-10 12:00:00") + "",
                "200", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.01", 24.24972222222222, "48.50", "200.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.01", 24.24972222222222, "48.50", "242.50", "200.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "194.00");

        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 24.24972222222222,
                "194.00");
        eva.assertParameterCosts(parameter, "291.00");
        // next parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-09 11:30:01") + "",
                DateTimeHandling.calculateMillis("2013-07-09 11:45:01") + "",
                "100", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.01", 0.25, "0.25", "100.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.01", 0.25, "0.25", "0.75", "100.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "0.50");

        eva.assertRoleCost(roleCosts, "ADMIN", "0.02", 0.24972222222222223,
                "0.50");
        eva.assertParameterCosts(parameter, "1.00");

        // next parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-09 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-09 11:30:01") + "",
                "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.01", 1.5002777777777778, "70.68", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.01", 1.5002777777777778, "70.68", "353.37", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "282.69");

        eva.assertRoleCost(roleCosts, "GUEST", "0.04", 1.5, "282.66");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.02", 0.0002777777777777778,
                "0.03");
        eva.assertParameterCosts(parameter, "424.05");

        eva.assertPriceModelCosts(priceModel, "EUR", "742.87");

        // next price model

        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_29", 0);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-07-04 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-09 10:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "2.0", "2.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "2.0", "1", "2.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict29Customer", "2.0");
        // no role cost
        // next parameter

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-04 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-09 10:00:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 2.0, "70.00", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "70.00");
        eva.assertParameterCosts(parameter, "140.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "144.00");
        eva.assertOverallCosts("665.15", "EUR", "751.62");

        // THEN - assert period 2
        voSubscriptionDetails = getSubscriptionDetails("PICT_TEST_29", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-06-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-05 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);
        subscription = BillingXMLNodeSearch.getSubscriptionNode(billingResult,
                "PICT_TEST_29");
        assertNotNull("Subscription doesn't exist", subscription);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-07-04 10:00:00"),
                DateTimeHandling.calculateMillis("2013-07-04 10:00:00"));
        eva.assertGatheredEventsCosts(priceModel, "120.00");
        eva.assertOneTimeFee(priceModel, "1.00");
        eva.assertOverallCosts("121.00", "EUR", "136.73");
    }

    @Test
    public void testcase28() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario28();

        // when
        performBillingRun(15, "2013-09-25 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_28", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-10 00:00:00", "2013-09-10 00:00:00");

        assertNull("no result.xml should exist", eva);

    }

    @Test
    public void testcase28_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario28_1();

        // when
        performBillingRun(15, "2013-09-25 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_28_1", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-10 00:00:00", "2013-08-10 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_28_1");
        assertNotNull("Subscription doesn't exist", subscription);

        // upgrade service (pro rate price model)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertOneTimeFee(priceModel, "5.00");
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-09 13:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-09 24:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "0.06547619047619048", "0.07");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "0.06547619047619048", "1",
                "0.07");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict28_1Customer",
                "0.06547619047619048");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "0.07");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.06547619047619048,
                "0.07");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-09 13:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-09 24:00:00") + "",
                "4711", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.06547619047619048, "200.06", "4711.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.06547619047619048, "308.46", "308.46", "4711.0");

        eva.assertParameterCosts(parameter, "508.52");
        eva.assertParametersCosts(priceModel, "508.52");
        eva.assertPriceModelCosts(priceModel, "EUR", "513.73");

        // first service
        sub = getSubscriptionDetails("PICT_TEST_28_1", 0);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-08 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-08 10:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "0.0", "0.00");
        eva.assertGatheredEventsCosts(priceModel, "77.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "87.00");

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_28_1", 1);
        eva = getEvaluator(sub.getKey(), "2013-08-10 00:00:00",
                "2013-09-10 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_28_1");
        assertNotNull("Subscription doesn't exist", subscription);

        // upgrade service (pro rate price model)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00") + "");

        eva.assertGatheredEventsCosts(priceModel, "30.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "4.428571428571429", "4.43");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "4.428571428571429", "1",
                "4.43");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict28_1Customer",
                "4.428571428571429");
        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "4.43");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 4.428571428571429,
                "4.43");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00") + "",
                "4711", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                4.428571428571429, "13531.50", "4711.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 4.428571428571429, "20863.00", "20863.00", "4711.0");

        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 4.428571428571429,
                "0.00");
    }

    @Test
    public void testcase27() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario27();

        // when
        performBillingRun(04, "2013-09-05 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_27", 2);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_27" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // 2.upgrade service (pro rate price model)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-03 11:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-31 11:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "4.0", "4.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "4.0", "1", "4.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict27Customer", "4.0");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "4.00");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 4.0, "4.00");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-03 11:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-31 11:00:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 4.0,
                "140.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 4.0, "140.00", "280.00", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "140.00");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 4.0, "140.00");

        eva.assertParameterCosts(parameter, "420.00");
        eva.assertParametersCosts(priceModel, "420.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "432.00");

        // next price model
        sub = getSubscriptionDetails("PICT_TEST_27", 1);
        eva = getEvaluator(sub.getKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_27" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // 1.upgrade service (pro rate price model)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-02 11:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-03 11:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "0.14285714285714285", "0.14");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "0.14285714285714285", "1",
                "0.14");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict27Customer",
                "0.14285714285714285");
        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "0.14");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.13095238095238096,
                "0.13");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.011904761904761904,
                "0.01");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-02 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-03 11:00:00") + "",
                "50", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.13690476190476192, "6.85", "50.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.13690476190476192, "6.85", "13.70", "50.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "6.85");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.13095238095238096,
                "6.55");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.005952380952380952,
                "0.30");

        eva.assertParameterCosts(parameter, "20.55");

        // next parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-02 11:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-02 12:00:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.005952380952380952, "0.21", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.005952380952380952, "0.21", "0.42", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "0.21");

        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.005952380952380952,
                "0.21");

        eva.assertParameterCosts(parameter, "0.63");

        eva.assertParametersCosts(priceModel, "21.18");
        eva.assertPriceModelCosts(priceModel, "EUR", "21.60");
        eva.assertOverallCosts("439.99", "EUR", "439.99");
        eva.assertOverallDiscount("3.0", "13.61", "439.99", "453.60");

    }

    @Test
    public void testcase26() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario26();

        // when
        performBillingRun(0, "2013-09-11 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_26", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-10 00:00:00", "2013-08-10 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_26");
        assertNotNull("Subscription doesn't exist", subscription);

        // 1.upgrade service (pro rate price model)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-08 15:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "0.19642857142857142", "0.20");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "0.19642857142857142", "1",
                "0.20");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict26Customer",
                "0.19642857142857142");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "0.20");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.19642857142857142,
                "0.20");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-08 15:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.19642857142857142, "6.87", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.19642857142857142, "6.87", "13.74", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "6.87");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.19642857142857142,
                "6.87");

        eva.assertParameterCosts(parameter, "20.61");
        eva.assertParametersCosts(priceModel, "20.61");
        eva.assertPriceModelCosts(priceModel, "EUR", "31.21");
        // nothing else
        eva.assertOverallCosts("31.21", "EUR", "31.21");

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_26", 1);
        eva = getEvaluator(sub.getKey(), "2013-08-10 00:00:00",
                "2013-09-10 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_26" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee(priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "4.2976190476190474", "4.30");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "4.2976190476190474", "1",
                "4.30");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict26Customer",
                "4.2976190476190474");
        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "4.30");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.0654761904761905,
                "1.07");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 3.2321428571428569,
                "3.23");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-09-01 14:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-09 02:00:00") + "",
                "150", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                1.0714285714285714, "160.71", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 1.0714285714285714, "160.71", "321.42", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "160.71");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.005952380952380952,
                "0.89");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.0654761904761905,
                "159.82");

        eva.assertParameterCosts(parameter, "482.13");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-09 24:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-01 14:00:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                3.2261904761904763, "112.92", "35.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 3.2261904761904761, "112.92", "225.84", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "112.92");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 3.2261904761904761,
                "112.92");

        eva.assertParameterCosts(parameter, "338.76");
        eva.assertParametersCosts(priceModel, "820.89");

        eva.assertPriceModelCosts(priceModel, "EUR", "833.79");

        eva.assertOverallCosts("833.79", "EUR", "833.79");

    }

    @Test
    public void testcase25() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario25();

        // when
        performBillingRun(0, "2013-10-02 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_25", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNull("no result.xml should exist", eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_25", 2);
        eva = getEvaluator(sub.getKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_25");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // 2.upgrade service after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-20 13:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-25 24:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "1.0", "0.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "1.0", "1", "1.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict25Customer", "1.0");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.00");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 1.0, "1.00");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-20 13:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-25 24:00:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 1.0,
                "0.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 1.0, "35.00", "70.00", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "35.00");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 1.0, "35.00");

        eva.assertParameterCosts(parameter, "70.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "72.00");
        eva.assertOverallCosts("69.84", "EUR", "73.33");

        sub = getSubscriptionDetails("PICT_TEST_25", 2);
        eva = getEvaluator(sub.getKey(), "2013-09-01 00:00:00",
                "2013-10-01 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_25");
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-29 24:00:00") + "");

        eva.assertNullOneTimeFee(priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "5.0", "0.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "5.0", "1", "5.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict25Customer", "5.0");
        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "5.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 4.053571428571429,
                "4.05");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.9464285714285714,
                "0.95");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-09-01 14:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-29 24:00:00") + "",
                "150", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                4.059523809523809, "0.00", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 4.059523809523809, "608.93", "1217.86", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "608.93");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.005952380952380952,
                "0.89");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 4.053571428571429,
                "608.04");

        eva.assertParameterCosts(parameter, "1217.86");

        // next parameter
        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-01 14:00:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                0.9404761904761905, "0.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.9404761904761905, "32.92", "65.84", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "32.92");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.9404761904761905,
                "32.92");

        eva.assertParameterCosts(parameter, "65.84");
        eva.assertParametersCosts(priceModel, "1283.70");
        eva.assertPriceModelCosts(priceModel, "EUR", "1293.70");
        eva.assertOverallCosts("1293.70", "EUR", "1358.39");

    }

    @Test
    public void testcase25_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario25_1();

        // when
        performBillingRun(0, "2013-10-02 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_25_1", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");

        assertNull("no result.xml should exist", eva);

        // next billing period

        eva = getEvaluator(sub.getKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        assertNull("no result.xml should exist", eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_25_1", 2);
        eva = getEvaluator(sub.getKey(), "2013-09-01 00:00:00",
                "2013-10-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_25_1");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-09-01 15:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-30 02:00:00") + "");

        eva.assertOneTimeFee(priceModel, "10.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "4.065476190476191", "0.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", "4.065476190476191", "1",
                "4.07");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict25_1Customer",
                "4.065476190476191");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "4.07");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 4.065476190476191,
                "4.07");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-09-01 15:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-30 02:00:00") + "",
                "150", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                4.065476190476191, "609.82", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 4.065476190476191, "609.82", "1219.64", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertTotalRoleCosts(roleCosts, "609.82");

        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 4.065476190476191,
                "609.82");

        eva.assertParameterCosts(parameter, "1829.46");

        eva.assertPriceModelCosts(priceModel, "EUR", "1847.60");
        eva.assertOverallVAT("5.0", "92.38");

    }

    @Test
    public void testcase25_2() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario25_2();

        // when
        performBillingRun(0, "2013-10-02 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_25_2", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");

        assertNull("no result.xml should exist", eva);
        // next billing period

        eva = getEvaluator(sub.getKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        assertNull("no result.xml should exist", eva);
        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_25_2", 1);
        eva = getEvaluator(sub.getKey(), "2013-09-01 00:00:00",
                "2013-10-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_25_2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");

    }

    @Test
    public void testcase25_3() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario25_3();

        // when
        performBillingRun(0, "2013-10-02 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_25_3", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");

        assertNull("no result.xml should exist", eva);

        // next billing period

        eva = getEvaluator(sub.getKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        assertNull("no result.xml should exist", eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_25_3", 0);
        eva = getEvaluator(sub.getKey(), "2013-09-01 00:00:00",
                "2013-10-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_25_3");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");

    }

    @Test
    public void testcase24() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario24();

        // when
        performBillingRun(15, "2013-11-18 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_24", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-09-02 00:00:00", "2013-10-02 00:00:00");

        assertNull("no result.xml should exist", eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_24", 2);
        eva = getEvaluator(sub.getKey(), "2013-10-02 00:00:00",
                "2013-11-02 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_24" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-26 01:15:00") + "",
                DateTimeHandling.calculateMillis("2013-10-29 02:15:00") + "");

        eva.assertOneTimeFee(priceModel, "10.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "2.00", "75.0", "150.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict24Customer",
                "75.0");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "1.00", 75.0, "75.00");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-10-26 01:15:00") + "",
                DateTimeHandling.calculateMillis("2013-10-29 02:15:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                75.0, "2625.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "1.00", 75.0, "2625.00", "2625.00", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "USER", "0.00", 75.0, "0.00");

    }

    @Test
    public void testcase24_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario24_1();

        // when
        performBillingRun(15, "2013-11-18 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_24_1", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-09-02 00:00:00", "2013-10-02 00:00:00");

        assertNull("no result.xml should exist", eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_24_1", 2);
        eva = getEvaluator(sub.getKey(), "2013-10-02 00:00:00",
                "2013-11-02 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_24_1" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-28 01:15:00") + "",
                DateTimeHandling.calculateMillis("2013-10-29 02:15:00") + "");

        eva.assertOneTimeFee(priceModel, "10.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "2.00", "26.0", "52.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict24_1Customer",
                "26.0");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "USER", "1.00", 26.0, "26.00");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-10-28 01:15:00") + "",
                DateTimeHandling.calculateMillis("2013-10-29 02:15:00") + "",
                "35", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                26.0, "910.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "1.00", 26.0, "910.00", "910.00", "35.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);

        eva.assertRoleCost(roleCosts, "USER", "0.00", 26.0, "0.00");

    }

    @Test
    public void testcase23() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario23();

        // when
        performBillingRun(0, "2013-11-18 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_23", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-09-01 00:00:00", "2013-10-01 00:00:00");
        assertNull(eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_23", 2);
        eva = getEvaluator(sub.getKey(), "2013-10-01 00:00:00",
                "2013-11-01 00:00:00");
        assertNull(eva);

    }

    @Test
    public void testcase22() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario22();

        // when
        performBillingRun(0, "2013-11-18 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_22", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-09-15 00:00:00", "2013-10-15 00:00:00");
        assertNull(eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_22", 1);
        eva = getEvaluator(sub.getKey(), "2013-10-15 00:00:00",
                "2013-11-15 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_22" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-27 01:15:11") + "",
                DateTimeHandling.calculateMillis("2013-11-14 24:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "455.74694444444447", "0.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict22Customer",
                "455.74694444444447");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "0.25");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "455.99694444444447", "2", "910.99");
        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 455.74694444444447,
                "1367.24");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.25, "0.25");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-10-27 03:00:01") + "",
                DateTimeHandling.calculateMillis("2013-11-14 24:00:00") + "",
                "1", "ENUMERATION");

        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);

        eva.assertParameterPeriodFee(option, PricingPeriod.HOUR.name(), "0.00",
                452.9997222222222, "0.00", null);

        eva.assertUserAssignmentCosts(option, PricingPeriod.HOUR.name(),
                "0.00", 453.2497222222222, "0.00", "0.00", null);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 452.9997222222222,
                "0.00");

        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.25, "0.00");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-10-27 01:15:11") + "",
                DateTimeHandling.calculateMillis("2013-10-27 03:00:01") + "",
                "2", "ENUMERATION");

        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);

        eva.assertParameterPeriodFee(option, PricingPeriod.HOUR.name(), "0.00",
                2.7472222222222222, "0.00", null);

        eva.assertUserAssignmentCosts(option, PricingPeriod.HOUR.name(),
                "0.00", 2.7472222222222222, "0.00", "0.00", null);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 2.7472222222222222,
                "0.00");

        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.0, "0.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2278.48");

        // next sub
        sub = getSubscriptionDetails("PICT_TEST_22", 1);
        eva = getEvaluator(sub.getKey(), "2013-10-15 00:00:00",
                "2013-11-15 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_22" + "_SubID2", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-10-27 00:00:00"), DateTimeHandling
                .calculateMillis("2013-10-27 01:15:01"));

        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "0.00", "1.2502777777777778", "0.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict22Customer",
                "1.2502777777777778");

        // role cost
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 1.2502777777777778,
                "3.75");

        eva.assertOneTimeFee(priceModel, "5.00");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-10-27 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-27 01:15:01") + "",
                "2", "ENUMERATION");

        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);

        eva.assertParameterPeriodFee(option, PricingPeriod.HOUR.name(), "0.00",
                1.2502777777777778, "0.00", null);

        eva.assertUserAssignmentCosts(option, PricingPeriod.HOUR.name(),
                "0.00", 1.2502777777777778, "0.00", "0.00", null);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 1.2502777777777778,
                "0.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "10.25");
        eva.assertOverallCosts("2288.73", "EUR", "2288.73");

    }

    @Test
    public void testcase21() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario21();

        // when
        performBillingRun(4, "2013-11-18 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_21", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-09-03 00:00:00", "2013-10-03 00:00:00");
        assertNull(eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_21", 0);
        eva = getEvaluator(sub.getKey(), "2013-10-03 00:00:00",
                "2013-11-03 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_21" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-08 08:00:00") + "",
                DateTimeHandling.calculateMillis("2013-11-02 20:00:00") + "");

        eva.assertGatheredEventsCosts(priceModel, "104.00");

        eva.assertOneTimeFee(priceModel, "5.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "2.00", 3.642857142857143, "7.29");

        eva.assertUserAssignmentCostsFactor(priceModel, "PICT21Customer",
                "3.642857142857143");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.2976190476190477");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.00", 4.940476190476191, "2",
                "4.94", "9.88");

        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 4.940476190476191,
                "4.94");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-10-08 08:00:00") + "",
                DateTimeHandling.calculateMillis("2013-11-02 20:00:00") + "",
                "1", "ENUMERATION");

        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);

        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "1.00",
                3.642857142857143, "3.64", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "0.00", 4.940476190476191, "0.00", "4.94", null);
        eva.assertTotalRoleCosts(roleCosts, "4.94");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 4.940476190476191,
                "4.94");
        eva.assertOptionCosts(option, "8.58");

        eva.assertParameterCosts(parameter, "8.58");
        eva.assertPriceModelCosts(priceModel, "EUR", "134.75");
        eva.assertOverallCosts("134.75", "EUR", "160.35");

    }

    @Test
    public void testcase20() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario20();

        // when
        performBillingRun(4, "2013-12-18 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_20", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-02 00:00:00", "2013-11-02 00:00:00");
        assertNull(eva);

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_20", 1);
        eva = getEvaluator(sub.getKey(), "2013-11-02 00:00:00",
                "2013-12-02 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_20" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-11-03 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-11-10 02:15:00") + "");

        eva.assertGatheredEventsCosts(priceModel, "10.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "1.00", "1.0", "1.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict20Customer", "1.0");

        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.0, "1.00");
        eva.assertOneTimeFee(priceModel, "5.00");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-11-03 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-11-10 02:15:00") + "",
                "2", "ENUMERATION");

        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);

        eva.assertParameterPeriodFee(option, PricingPeriod.MONTH.name(),
                "1.00", 1.0, "1.00", null);

        eva.assertUserAssignmentCosts(option, PricingPeriod.MONTH.name(),
                "0.00", 1.0, "0.00", "1.00", null);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.0, "1.00");
        eva.assertParameterCosts(parameter, "2.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "20.00");
        eva.assertOverallCosts("20.00", "EUR", "22.00");

    }

    @Test
    public void testcase19() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario19();

        // when
        performBillingRun(28, "2013-04-29 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_19", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_19" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 03:59:59") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "2.00", "2.0", "4.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict19Customer", "2.0");
        eva.assertOneTimeFee(priceModel, "5.00");

        // role cost
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 2.0, "2.00");

        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 03:59:59") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "1.00", 2.0, "0.00", "0.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 2.0, "0.00");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "1.00", 2.0, "0.00", "0.00", "0.0");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "13.00");
        eva.assertOverallCosts("13.00", "EUR", "13.00");
    }

    @Test
    public void testcase18() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario18();

        // when
        performBillingRun(4, "2013-04-29 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_18", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-15 00:00:00", "2013-04-15 00:00:00");

        assertNull(eva);
    }

    @Test
    public void testcase17() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario17();

        // when
        performBillingRun(15, "2013-09-29 23:00:00");
        // second billing period
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_17", 2);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        assertNull(eva);
        // next billing period

        // next price model
        sub = getSubscriptionDetails("PICT_TEST_17", 0);
        eva = getEvaluator(sub.getKey(), "2013-07-01 00:00:00",
                "2013-08-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_17" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-07-01 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-28 10:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1.00", "3.857142857142857", "3.86");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict17Customer",
                "3.857142857142857");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-01 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-28 10:00:00") + "",
                "100", "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 3.8452380952380953, "384.52", "100.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "1.00", 3.8452380952380953, "384.52", "769.05", "100.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "384.53");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 2.2797619047619047,
                "227.98");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 1.5654761904761905,
                "156.55");

        eva.assertParameterCosts(parameter, "1153.57");

        // next Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-01 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-01 12:00:00") + "",
                "35", "INTEGER");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.011904761904761904, "0.42", "35.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.42");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.005952380952380952,
                "0.21");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.005952380952380952,
                "0.21");

        eva.assertParameterCosts(parameter, "1.26");
        eva.assertParametersCosts(priceModel, "1154.83");

        eva.assertPriceModelCosts(priceModel, "EUR", "1162.55");
        eva.assertOverallCosts("1127.67", "EUR", "1240.44");

    }

    @Test
    public void testcase16() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario16();

        // when
        performBillingRun(0, "2013-05-01 23:00:00");

        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_16", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_16" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 24:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 11:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "2.00", "0.4583333333333333", "0.92");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "0.4583333333333333", "1", "0.46");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-03-31 24:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 11:00:00") + "",
                "false", "BOOLEAN");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.00", 0.4583333333333333, "0.00", "0.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "1.00", 0.4583333333333333, "0.00", "0.00", "0.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");

        eva.assertParameterCosts(parameter, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1.38");
        eva.assertOverallCosts("1.38", "EUR", "1.64");

        // next billing period
        sub = getSubscriptionDetails("PICT_TEST_16", 0);
        eva = getEvaluator(sub.getKey(), "2013-03-01 00:00:00",
                "2013-04-01 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_16");
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-31 24:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "2.00", "1.0", "2.00");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict16Customer", "1.0");

        // next Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 24:00:00") + "",
                "false", "BOOLEAN");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.00", 1.0, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "1.00", 1.0, "0.00", "0.00", "0.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");

        // this day has 23 hour!!!!!
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.478260869565217391,
                "0.00");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 0.521739130434782608,
                "0.00");

        // next price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-03-28 10:00:00"),
                DateTimeHandling.calculateMillis("2013-03-29 11:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "2.00", "1.0416666666666667", "2.08");

        eva.assertUserAssignmentCostsFactor(priceModel, "Pict16Customer",
                "1.0416666666666667");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "0.041666666666666664");

        // next Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-03-28 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-29 11:00:00") + "",
                "false", "BOOLEAN");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.00", 1.0416666666666667, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "1.00", 1.0833333333333335, "0.00", "0.00", "0.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");

    }

    @Test
    public void testcase16_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario16_1();

        // when
        performBillingRun(0, "2013-05-01 23:00:00");
        // second billing period
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_16_1", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_16_1");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "1.00", "1.0", "1.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict16_1Customer",
                "1.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 1.0, "1.00");

    }

    @Test
    public void testcase16_2() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario16_2();

        // when
        performBillingRun(0, "2013-05-01 23:00:00");

        // second billing period
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_16_2", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_16_2");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        // the price model has 1 day free !!!!
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 10:00:00"),
                DateTimeHandling.calculateMillis("2013-03-31 12:00:00"));

        // this day has 23 hour!!!!!
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "1.00", 0.08695652173913043, "0.09");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict16_2Customer",
                "0.08695652173913043");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.08695652173913043,
                "0.09");
    }

    @Test
    public void testcase16_3() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario16_3();

        // when
        performBillingRun(0, "2013-11-01 23:00:00");

        // second billing period
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_16_3", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-01 00:00:00", "2013-11-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_16_3");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-27 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-28 00:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "1.00", "1.0", "1.00");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict16_3Customer",
                "1.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 1.0, "1.00");
    }

    @Test
    public void testcase16_4() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario16_4();

        // when
        performBillingRun(0, "2013-11-01 23:00:00");
        // second billing period
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_16_4", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-01 00:00:00", "2013-11-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_16_4");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-27 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-27 11:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "1.00", "0.04", "0.04");
        eva.assertUserAssignmentCostsFactor(priceModel, "Pict16_4Customer",
                "0.04");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.04, "0.04");
    }

    @Test
    public void testcase15() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario15();

        // when
        performBillingRun(15, "2013-05-15 23:00:00");

        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_15", 2);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-02 00:00:00", "2013-04-02 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_15" + "_SubID4", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-31 03:00:01"), DateTimeHandling
                .calculateMillis("2013-04-01 24:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 03:00:01") + "",
                DateTimeHandling.calculateMillis("2013-04-01 24:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", "45.0", "45.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "45.0", "1", "45.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "45.00");

        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 45.0, "45.00");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-31 03:00:01") + "",
                DateTimeHandling.calculateMillis("2013-04-01 24:00:00") + "",
                "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                45.0, "37899.00", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.10", 45.0, "21199.50", "233194.50", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "211995.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 45.0, "211995.00");

        eva.assertParameterCosts(parameter, "271093.50");

        eva.assertPriceModelCosts(priceModel, "EUR", "271228.50");

        // next sub

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_15" + "_SubID4", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-31 01:00:00"), DateTimeHandling
                .calculateMillis("2013-03-31 01:59:59"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 01:59:59") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", "1.0", "1.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "1.0", "1", "1.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "1.00");

        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.0, "1.00");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 01:59:59") + "",
                "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(), 1.0,
                "842.20", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.10", 1.0, "471.10", "5182.10", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "4711.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.0, "4711.00");

        eva.assertParameterCosts(parameter, "6024.30");

        eva.assertPriceModelCosts(priceModel, "EUR", "6027.30");

        // next price model
        sub = getSubscriptionDetails("PICT_TEST_15", 1);
        eva = getEvaluator(sub.getKey(), "2013-03-02 00:00:00",
                "2013-04-02 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_15" + "_SubID4", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-31 00:00:00"), DateTimeHandling
                .calculateMillis("2013-03-31 01:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "2.00", "2.0", "4.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "2.0", "1", "2.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "2.00");

        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.5, "0.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.5, "1.50");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-31 00:15:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00") + "",
                "100", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                1.75, "17.50", "100.0");
        // new requirement factor is 1.75
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.10", 1.5, "15.00", "165.00", "100.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "150.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.50, "150.00");
        eva.assertParameterCosts(parameter, "182.50");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 00:15:00") + "",
                "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                0.25, "210.55", "4711.0");
        // new requirement factor is 0.25
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.10", 0.5, "235.55", "2591.05", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "2355.50");

        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 0.5, "2355.50");

        eva.assertParameterCosts(parameter, "2801.60");

        eva.assertPriceModelCosts(priceModel, "EUR", "2992.10");

        sub = getSubscriptionDetails("PICT_TEST_15", 0);
        eva = getEvaluator(sub.getKey(), "2013-03-02 00:00:00",
                "2013-04-02 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_15" + "_SubID4", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:15:00"), DateTimeHandling
                .calculateMillis("2013-03-31 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:15:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", "61.75", "61.75");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "61.75", "1", "61.75");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "61.75");

        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 61.75, "61.75");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-03-28 10:15:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00") + "",
                "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                61.75, "52005.85", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.10", 61.75, "29090.43", "319994.68", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "290904.25");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 61.75, "290904.25");

        eva.assertParameterCosts(parameter, "372000.53");
        eva.assertPriceModelCosts(priceModel, "EUR", "372185.78");
        eva.assertOverallCosts("652433.68", "EUR", "717677.05");

    }

    @Test
    public void testcase14() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario14();

        // when
        performBillingRun(28, "2013-05-30 00:00:00");

        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_14", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_14", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-31 10:15:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-31 10:15:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "2.00", "13.75", "27.50");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "13.75", "1", "13.75");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "13.75");

        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 13.75, "13.75");
        eva.assertOneTimeFee(priceModel, "1.00");

        // Parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-03-31 10:15:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                "2", "ENUMERATION");

        eva.assertParameterCosts(parameter, "27.50");

        eva.assertPriceModelCosts(priceModel, "EUR", "83.50");

        // next billing period

        sub = getSubscriptionDetails("PICT_TEST_14", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_14" + "_SubID2", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 10:15:00"), DateTimeHandling
                .calculateMillis("2013-04-01 11:15:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 10:15:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 11:15:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", "2.0", "2.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "2.0", "1", "2.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "2.00");

        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 2.0, "2.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "2.0", "1", "2.00");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-01 10:15:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 11:15:00") + "",
                "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(), 2.0,
                "1684.40", "4711.0");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.10", 2.0, "942.20", "10364.20", "4711.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "9422.00");
        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 2.0, "9422.00");

        eva.assertParameterCosts(parameter, "12048.60");

        eva.assertPriceModelCosts(priceModel, "EUR", "12054.60");

        // next price model
        sub = getSubscriptionDetails("PICT_TEST_14", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_14" + "_SubID2", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-01 10:15:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 10:15:00") + "");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "2.00", "10.25", "20.50");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "1.00", "10.25", "1", "10.25");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "10.25");

        eva.assertRoleCost(roleCosts, "GUEST", "1.00", 10.25, "10.25");

        // Parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 10:15:00") + "",
                "2", "ENUMERATION");

        eva.assertParameterCosts(parameter, "20.50");

        eva.assertPriceModelCosts(priceModel, "EUR", "61.50");
        eva.assertOverallCosts("12116.10", "EUR", "13327.71");

    }
}
