/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: June 14, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.PictAsyncSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.converter.DateConverter;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author baumann
 * 
 */

public class PictAsyncIT extends BillingIntegrationTestBase {

    private final PictAsyncSetup testSetup = new PictAsyncSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void testcase01() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario01();

        // when
        performBillingRun(4, "2013-07-20 13:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_01", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-05-05 00:00:00", "2013-06-05 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_01");
        assertNotNull("Subscription doesn't exist", subscription);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-05-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-21 13:00:00"));
        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "0.00", 16.541666666666667, "0.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "0.00", 16.541666666666667, "1",
                "0.00", "0.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Pict01DiscountedCustomer", 16.541666666666667);
        eva.assertPriceModelCosts(priceModel, "EUR", "0.00");

        // second service (per time unit price model)
        sub = getSubscriptionDetails("PICT_TEST_01", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-05-21 13:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "2.0", "1468.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 2.0D, "1", "868.00",
                "880.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Pict01DiscountedCustomer", "2.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0D, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2602.00");
        eva.assertOverallCosts("2211.70", "EUR", "2499.22");
        eva.assertOverallDiscount("15.0", "390.30", "2211.70", "2602.00");
        eva.assertOverallVAT("13.0", "287.52");

        // second period
        eva = getEvaluator(sub.getKey(), "2013-06-05 00:00:00",
                "2013-07-05 00:00:00");
        eva.assertNullOneTimeFee();
        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_01");
        assertNotNull("Subscription doesn't exist", subscription);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-06-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 4.0D, "1", "1736.00",
                "1760.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Pict01DiscountedCustomer", "4.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0D, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4696.00");

        eva.assertOverallCosts("4696.00", "EUR", "5306.48");
        eva.assertNullOverallDiscount();
        eva.assertOverallVAT("13.0", "610.48");
    }

    @Test
    public void testcase02() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario02();

        // when
        performBillingRun(0, "2013-08-02 13:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_02", 0);
        // free period
        assertNull(getEvaluator(sub.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00"));

        // then - period 2
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_02" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // ---- First Pro Rata price model after upgrade ----
        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_02", 1);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-07-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-22 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "100.00", 3, "300.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "100.00", 2, "1", "200.00");

        eva.assertUserAssignmentCostsByUser(priceModel, "PIC02Customer", 2);

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 2);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-15 00:00:00") + "",
                "15", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "4.00", 1.0, "60.00", "60.00", "15.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 1.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "120.00");

        // value changed, role changed
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-15 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-22 00:00:00") + "",
                "5", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "10.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "4.00", 1.0, "20.00", "20.00", "5.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 1.0, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "30.00");
        eva.assertParametersCosts(priceModel, "150.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "650.00");

        // ---- Second Pro Rata price model after upgrade ---- before suspend
        upgradedSubscriptionDetails = getSubscriptionDetails("PICT_TEST_02", 2);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-07-22 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-23 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "200.00", 0.14285714285714285, "28.57");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "200.00", 0.14285714285714285, "1",
                "28.57");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC02Customer",
                0.14285714285714285);

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-22 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-23 00:00:00") + "",
                "15", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "4.00", 0.14285714285714285, "8.57", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "8.00", 0.14285714285714285, "17.14", "17.14", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.14285714285714285,
                "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "25.71");
        eva.assertParametersCosts(priceModel, "25.71");

        eva.assertPriceModelCosts(priceModel, "EUR", "82.85");

        // ---- Second Pro Rata price model after upgrade ---- after suspend
        upgradedSubscriptionDetails = getSubscriptionDetails("PICT_TEST_02", 2);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-07-24 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-31 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "200.00", 1.0, "200.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "200.00", 1, "1", "200.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC02Customer", 1);

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-24 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-31 00:00:00") + "",
                "15", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "4.00", 1, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "8.00", 1, "120.00", "120.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 1, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "180.00");
        eva.assertParametersCosts(priceModel, "180.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "580.00");

        eva.assertOverallCosts("1312.85", "EUR", "1575.42");
        eva.assertOverallVAT("20.0", "262.57");
    }

    @Test
    public void testcase03() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario03();

        // when
        performBillingRun(15, "2013-08-17 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03", 0);

        // Billing Period per time-unit hour
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-07-01 00:00:00",
                "2013-08-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_03");
        assertNotNull("Subscription doesn't exist", subscription);

        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03", 0);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), subscriptionDetails.getPriceModel()
                .getKey(), "2013-07-21 00:00:00", "2013-08-01 00:00:00");
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", 264, "264.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), 264, "1", "267.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC03Customer", 264);

        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "1.00", "2",
                "null", "262.00", "262.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "267.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 229.83333333333334,
                "459.67");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 34.166666666666664,
                "34.17");
        eva.assertTotalRoleCosts(roleCosts, "493.84");

        // assert parameter
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        String msOf150Days = "12960000000";
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD", String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-21 00:00:00")), String
                        .valueOf(DateTimeHandling
                                .calculateMillis("2013-08-01 00:00:00")),
                msOf150Days, "DURATION");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 264.0, "0.00", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.00", 264.0, "0.00", "0.00", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 34.166666666666664,
                "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 229.83333333333334,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "0.00");
        eva.assertParametersCosts(priceModel, "0.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1034.84");
    }

    @Test
    public void testcase03_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario03_1();

        // when
        performBillingRun(15, "2013-08-17 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_03_1", 0);

        // Period per time-unit hour
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_03_1");
        assertNotNull("Subscription doesn't exist", subscription);

        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03_1", 0);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-07-21 00:00:00"), DateTimeHandling
                .calculateMillis("2013-08-01 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", 264, "264.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), 264, "1", "267.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC03_1Customer", 264);
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "1.00", "2",
                "null", "262.00", "262.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "267.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 230.0, "460.00");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 34.0, "34.00");
        eva.assertTotalRoleCosts(roleCosts, "494.00");

        // assert parameter
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-07-21 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00") + "",
                "12960000000", // 150 days -> ms
                "DURATION");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 264.0, "0.00", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.00", 264.0, "0.00", "0.00", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 34.0, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 230.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertParametersCosts(priceModel, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1035.00");
    }

    @Test
    public void testcase03_2() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario03_2();

        // when
        performBillingRun(15, "2013-08-17 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03_2", 0);

        // Billing Period per time-unit hour
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-07-01 00:00:00",
                "2013-08-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_03_2");
        assertNotNull("Subscription doesn't exist", subscription);

        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03_2", 0);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-07-21 00:00:00"), DateTimeHandling
                .calculateMillis("2013-08-01 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", 264, "264.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), 264, "1", "267.00");

        eva.assertUserAssignmentCostsByUser(priceModel, "PIC03_2Customer", 264);
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "1.00", "2",
                "null", "262.00", "262.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "267.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 229.66666666666666,
                "459.33");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 34.33333333333333,
                "34.33");
        eva.assertTotalRoleCosts(roleCosts, "493.66");

        // assert parameter
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-07-21 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00") + "",
                "12960000000", // 150 days -> ms
                "DURATION");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 264.0, "0.00", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.00", 264.0, "0.00", "0.00", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 34.33333333333333, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 229.66666666666666,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertParametersCosts(priceModel, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1034.66");
    }

    /*
     * with daylight savings start in March - simple
     */
    @Test
    public void testcase03_3() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario03_3();

        // when
        performBillingRun(15, "2013-04-17 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_03_3", 0);

        // Period per time-unit hour
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_03_3");
        assertNotNull("Subscription doesn't exist", subscription);

        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03_3", 0);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-21 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", 263, "263.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), 263, "1", "266.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC03_3Customer", 263);
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "1.00", "2",
                "null", "261.00", "261.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "266.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 228.66666666666666,
                "457.33");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 34.33333333333333,
                "34.33");
        eva.assertTotalRoleCosts(roleCosts, "491.66");

        // assert parameter
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-21 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                "12960000000", // 150 hrs -> ms
                "DURATION");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 263.0, "0.00", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.00", 263.0, "0.00", "0.00", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 34.33333333333333, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 228.66666666666666,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertParametersCosts(priceModel, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1030.66");
    }

    @Test
    public void testcase03_4() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario03_4();

        // when
        performBillingRun(15, "2013-11-17 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_03_4", 0);

        // Billing Period per time-unit hour
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-01 00:00:00", "2013-11-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_03_4");
        assertNotNull("Subscription doesn't exist", subscription);

        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03_4", 0);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-10-21 00:00:00"), DateTimeHandling
                .calculateMillis("2013-11-01 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", 265, "265.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), 265, "1", "268.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC03_4Customer", 265);

        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "1.00", "2",
                "null", "263.00", "263.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "268.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 230.66666666666666,
                "461.33");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 34.33333333333333,
                "34.33");
        eva.assertTotalRoleCosts(roleCosts, "495.66");

        // assert parameter
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-10-21 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-11-01 00:00:00") + "",
                (DateConverter.MILLISECONDS_PER_DAY * 150) + "", "DURATION");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 265.0, "0.00", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "0.00", 265.0, "0.00", "0.00", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 34.33333333333333, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 230.66666666666666,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "0.00");
        eva.assertParametersCosts(priceModel, "0.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1038.66");
    }

    /*
     * with daylight savings start in March - like 03_3 but complex
     */
    @Test
    public void testcase03_5() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario03_5();

        // when
        performBillingRun(15, "2013-04-17 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_03_5", 0);

        // *** Billing Period per time-unit hour
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_03_5");
        assertNotNull("Subscription doesn't exist", subscription);

        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "PICT_TEST_03_5", 0);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubscriptionDetails
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-21 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", 263, "263.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), 263, "1", "266.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC03_5Customer", 263);
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "1.00", "2",
                "null", "261.00", "261.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "266.00");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 228.66666666666666,
                "457.33");
        eva.assertRoleCost(roleCosts, "USER", "1.00", 34.33333333333333,
                "34.33");
        eva.assertTotalRoleCosts(roleCosts, "491.66");

        // assert parameter - parameter is 150 days, role USER
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 2);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-21 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-22 10:20:00") + "",
                "12960000000", // 150 days -> ms
                "DURATION");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "1.00", 34.33333333333333, "5150.00", "150.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "1.00", 34.33333333333333, "5150.00", "5150.00", "150.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 34.33333333333333, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "10300.00");

        // assert parameter - parameter is 200 days, role ADMIN
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel, "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-22 10:20:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                "17280000000", // 200 days -> ms
                "DURATION");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "1.00", 228.66666666666666, "45733.33", "200.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "1.00", 228.66666666666666, "45733.33", "45733.33", "200.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 228.66666666666666,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "91466.66");
        eva.assertParametersCosts(priceModel, "101766.66");

        // event costs
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", "10.00", "1", "10.00");
        eva.assertGatheredEventsCosts(priceModel, "10.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "102807.32");
    }

    @Test
    public void testcase04() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario04();

        // when
        performBillingRun(28, "2013-05-08 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_04", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-07 00:00:00", "2013-04-07 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_04" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // first service (per unit price model)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00") + "");
        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(gatheredEvents, "FILE_UPLOAD", "3.10", "3", "9.30");
        Node event = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_DOWNLOAD");
        Node steppedEventPrices = BillingXMLNodeSearch
                .getSteppedEventPricesNode(event);
        eva.assertSteppedPrice(steppedEventPrices, "0.00", "10.00", "0", "1",
                "10.00", "1");
        eva.assertSteppedPrice(steppedEventPrices, "10.00", "5.00", "1", "3",
                "10.00", "2");
        eva.assertSteppedPrice(steppedEventPrices, "20.00", "2.00", "3", "7",
                "8.00", "4");
        eva.assertSteppedPrice(steppedEventPrices, "28.00", "1.00", "7", "10",
                "3.00", "3");
        eva.assertSteppedPrice(steppedEventPrices, "31.00", "0.50", "10",
                "null", "2.50", "5");
        eva.assertSteppedPricesAmount(steppedEventPrices, "33.50");
        eva.assertEventCosts(event, "15", "33.50");
        eva.assertGatheredEventsCosts(priceModel, "42.80");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "7.00", "5.0", "35.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "8.00", "5.0", "1", "40.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Pict04DiscountedCustomer", "5.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "USER", "4.00", 5.0, "20.00");
        eva.assertTotalRoleCosts(roleCosts, "20.00");
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00") + "",
                "8640000000", "DURATION");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.20", 5.0, "600.00", "100.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "3.80", 5.0, "1900.00", "4150.00", "100.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "2250.00");
        eva.assertRoleCost(roleCosts, "USER", "4.50", 5.0, "2250.00");
        eva.assertParameterCosts(parameter, "4750.00");
        eva.assertParametersCosts(priceModel, "4750.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "4887.80");

        // Upgrade to pro rata service
        sub = getSubscriptionDetails("PICT_TEST_04", 2);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", 0.6831763122476446, "501.45");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", 0.6347240915208614, "1",
                "275.47", "279.60");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Pict04DiscountedCustomer", 0.6347240915208614);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.3117092866756393,
                "1.87");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.32301480484522205,
                "2.26");
        eva.assertTotalRoleCosts(roleCosts, "4.13");
        eva.assertPriceModelCosts(priceModel, "EUR", "1035.05");

        // Overall costs
        eva.assertOverallCosts("5330.56", "EUR", "6236.76");
        eva.assertOverallDiscount("10.0", "592.29", "5330.56", "5922.85");
        eva.assertOverallVAT("17.0", "906.20");
    }

    @Test
    public void testcase04_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario04_1();

        // when
        performBillingRun(28, "2013-04-30 22:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_04_1", 0);
        VOSubscriptionDetails upgradedSub2 = getSubscriptionDetails(
                "PICT_TEST_04_1", 2);

        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");

        // ***** Subscription before suspend *****
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_04_1" + "_SubID2", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-05 00:00:00"), DateTimeHandling
                .calculateMillis("2013-03-07 12:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());

        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "7.00", "3.0", "21.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "8.00", "3.0", "1", "24.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Pict04_1DiscountedCustomer", "3.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "USER", "4.00", 3.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        eva.assertNodeCount(priceModel, "Parameters/Parameter", 2);
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-05 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-06 00:00:00") + "",
                "10368000000", "DURATION");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.20", 1.0, "144.00", "120.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "3.80", 1.0, "456.00", "996.00", "120.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "540.00");
        eva.assertRoleCost(roleCosts, "USER", "4.50", 1.0, "540.00");
        eva.assertParameterCosts(parameter, "1140.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel, "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-06 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-07 12:00:00") + "",
                "8640000000", "DURATION");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.20", 2.0, "240.00", "100.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "3.80", 2.0, "760.00", "1660.00", "100.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "900.00");
        eva.assertRoleCost(roleCosts, "USER", "4.50", 2.0, "900.00");
        eva.assertParameterCosts(parameter, "1900.00");

        eva.assertParametersCosts(priceModel, "3040.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "3097.00");

        // ***** Subscription after resume *****
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_04_1" + "_SubID2", sub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2013-03-08 18:00:00"), DateTimeHandling
                .calculateMillis("2013-03-11 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        // first service (per unit price model)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());

        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(gatheredEvents, "FILE_UPLOAD", "3.10", "3", "9.30");
        Node event = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_DOWNLOAD");
        Node steppedEventPrices = BillingXMLNodeSearch
                .getSteppedEventPricesNode(event);
        eva.assertSteppedPrice(steppedEventPrices, "0.00", "10.00", "0", "1",
                "10.00", "1");
        eva.assertSteppedPrice(steppedEventPrices, "10.00", "5.00", "1", "3",
                "10.00", "2");
        eva.assertSteppedPrice(steppedEventPrices, "20.00", "2.00", "3", "7",
                "8.00", "4");
        eva.assertSteppedPrice(steppedEventPrices, "28.00", "1.00", "7", "10",
                "3.00", "3");
        eva.assertSteppedPrice(steppedEventPrices, "31.00", "0.50", "10",
                "null", "2.50", "5");
        eva.assertSteppedPricesAmount(steppedEventPrices, "33.50");
        eva.assertEventCosts(event, "15", "33.50");
        eva.assertGatheredEventsCosts(priceModel, "42.80");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "7.00", "4.0", "28.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "8.00", "4.0", "1", "32.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Pict04_1DiscountedCustomer", "4.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "USER", "4.00", 4.0, "16.00");
        eva.assertTotalRoleCosts(roleCosts, "16.00");

        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel, "PERIOD",
                DateTimeHandling.calculateMillis("2013-03-08 18:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-11 00:00:00") + "",
                "8640000000", "DURATION");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.20", 4.0, "480.00", "100.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "3.80", 4.0, "1520.00", "3320.00", "100.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "1800.00");
        eva.assertRoleCost(roleCosts, "USER", "4.50", 4.0, "1800.00");
        eva.assertParameterCosts(parameter, "3800.00");
        eva.assertParametersCosts(priceModel, "3800.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "3918.80");

        // Upgrade to pro rata service
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub2.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-17 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", 0.4831763122476447, "354.65");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", 0.4347240915208614, "1",
                "188.67");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Pict04_1DiscountedCustomer", 0.4347240915208614);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.1117092866756393,
                "0.67");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.3230148048452221,
                "2.26");
        eva.assertTotalRoleCosts(roleCosts, "2.93");

        eva.assertPriceModelCosts(priceModel, "EUR", "800.25");

        // Overall costs
        eva.assertOverallCosts("7034.44", "EUR", "8230.29");
        eva.assertOverallDiscount("10.0", "781.61", "7034.44", "7816.05");
        eva.assertOverallVAT("17.0", "1195.85");
    }

    @Test
    public void testcase05() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario05();

        // when
        performBillingRun(28, "2013-03-01 10:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_05", 0);
        // result for January should not exist (see offset)
        assertNull(getEvaluator(sub.getKey(), "2013-01-02 00:00:00",
                "2013-02-02 00:00:00"));

        // when
        performBillingRun(28, "2013-03-02 10:00:00");

        // then
        // January Billing Period
        sub = getSubscriptionDetails("PICT_TEST_05", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-01-02 00:00:00", "2013-02-02 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_05" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // before upgrade service (per unit price model)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "0.0", "0.00");
        // the week has not ended in the billing period, so factor is 0
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "1.00", "0", "1",
                "0.00", "0.0");
        eva.assertSteppedPrice(steppedUserAssPrices, "1.00", "2.00", "1",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "0.00");
        eva.assertOneTimeFee(priceModel, "5.00");

        // after upgrade service (pro rata price model)
        sub = getSubscriptionDetails("PICT_TEST_05", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-02 00:00:00") + "");
        // factor is 14 hrs/168hrs per week
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "0.08333333333333333", "0.83");
        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "0.08333333333333333", "1",
                "0.00");
        // parameters
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-01 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-02 00:00:00") + "",
                "2", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "0.00",
                0.08333333333333333, "0.00", null);
        // parameter role costs
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "3.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.08333333333333333,
                "0.17");
        eva.assertTotalRoleCosts(roleCosts, "0.17");

        eva.assertPriceModelCosts(priceModel, "EUR", "1.00");
        eva.assertNullOneTimeFee(priceModel);

        eva.assertOverallDiscount("10.0", "0.60", "5.40", "6.00");
        eva.assertOverallCosts("5.40", "EUR", "5.40");

        // when
        performBillingRun(28, "2013-03-30 10:00:00");

        // then
        // February Billing Period
        sub = getSubscriptionDetails("PICT_TEST_05", 0);
        eva = getEvaluator(sub.getKey(), "2013-02-02 00:00:00",
                "2013-03-02 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_05" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // -- first service (per unit week price model) --
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-01 10:00:00") + "");

        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "1.0", "0.00");

        // user assignment costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.5714285714285714,
                "0.57");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.42857142857142855,
                "0.86");
        steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "1.00", "0", "1",
                "1.00", "1.0");
        eva.assertSteppedPrice(steppedUserAssPrices, "1.00", "2.00", "1",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "1.00");

        // parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-01 10:00:00") + "",
                "2", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "0.00",
                1.0, "0.00", null);
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.5714285714285714,
                "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.42857142857142855,
                "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "0.00", 1.0, "0.00", "0.00", null);
        eva.assertOptionCosts(option, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "2.43");

        // -- second service (pro-rata week price model) --
        sub = getSubscriptionDetails("PICT_TEST_05", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());
        // should be BP start to upgrade time
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-02 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-03 00:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
        // factor is 24hrs/168hrs
                "10.00", "0.14285714285714285", "1.43");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", // base price
                "0.14285714285714285", "1", "0.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Pict05DiscountedCustomer", 0.14285714285714285);
        // parameter
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-02 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-03 00:00:00") + "",
                "2", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "0.00",
                0.14285714285714285, "0.00", null);
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.14285714285714285,
                "0.29");
        eva.assertTotalRoleCosts(roleCosts, "0.29");
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "0.00", 0.14285714285714285, "0.00", "0.29", null);
        eva.assertOptionCosts(option, "0.29");
        eva.assertParameterCosts(parameter, "0.29");

        eva.assertPriceModelCosts(priceModel, "EUR", "1.72");

        // -- third service (pro-rata week price model 2 ) --
        sub = getSubscriptionDetails("PICT_TEST_05", 2);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertOneTimeFee(priceModel, "100.00");
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-03 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-03 10:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "1000.00", "0.05952380952380952", "59.52");
        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", // base price
                "0.05952380952380952", "1", "0.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Pict05DiscountedCustomer", 0.05952380952380952);
        // parameter before change
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-03 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-03 02:00:00") + "",
                "2", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "0.00",
                0.011904761904761904, "0.00", null); // factor is 2hrs
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.011904761904761904,
                "0.02");
        eva.assertTotalRoleCosts(roleCosts, "0.02");
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "0.00", 0.011904761904761904, "0.00", "0.02", null);
        eva.assertOptionCosts(option, "0.02");
        eva.assertParameterCosts(parameter, "0.02");
        // parameter after option change
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-03 02:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-03 10:00:00") + "",
                "1", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "0.00",
                0.047619047619047616, "0.00", null); // factor is 8 hrs
        // parameter role costs after option change
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.047619047619047616,
                "0.10");
        eva.assertTotalRoleCosts(roleCosts, "0.10");
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "0.00", 0.047619047619047616, "0.00", "0.10", null);
        eva.assertOptionCosts(option, "0.10");
        eva.assertParameterCosts(parameter, "0.10");

        eva.assertPriceModelCosts(priceModel, "EUR", "159.64");

        eva.assertOverallDiscount("10.0", "16.38", "147.41", "163.79");
        eva.assertOverallCosts("147.41", "EUR", "147.41");
    }

    @Test
    public void testcase05_1() throws Exception {
        testSetup.createPictScenario05_1();
        // when
        performBillingRun(28, "2013-04-02 10:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_05_1", 0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "PICT_TEST_05_1", 1);

        // *** First billing period ***
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-01-02 00:00:00", "2013-02-02 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_05_1" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // before upgrade service (per unit price model)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "0.0", "0.00");
        // the week has not ended in the billing period, so factor is 0
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "0.0", "0", "0.00");
        eva.assertOneTimeFee(priceModel, "5.00");

        // after upgrade service (pro rata price model)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-02 00:00:00") + "");
        // factor is 14 hrs/168hrs per week
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "0.08333333333333333", "0.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "0.08333333333333333", "1",
                "0.00");
        eva.assertNullOneTimeFee(priceModel);

        // *** Second billing period ***
        eva = getEvaluator(sub.getKey(), "2013-02-02 00:00:00",
                "2013-03-02 00:00:00");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_05_1" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // first service (per unit price model)
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-01 10:00:00") + "");

        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "1.0", "0.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", // base price
                "1.0", // factor for one week
                "1", "0.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "Pict05_1_Customer",
                1.0);
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.0, "1.00");
        eva.assertTotalRoleCosts(roleCosts, "1.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1.00");

        // after upgrade service (pro rata price model)
        sub = getSubscriptionDetails("PICT_TEST_05_1", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertNullOneTimeFee(sub.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-02 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-03 10:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "0.00", "0.20238095238095238", "0.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", // base price
                "0.20238095238095238", "1", "0.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "Pict05_1_Customer",
                0.20238095238095238);
        eva.assertPriceModelCosts(priceModel, "EUR", "0.00");

        eva.assertOverallCosts("1.00", "EUR", "1.00");
    }

    @Test
    /**
     * @throws Exception
     */
    public void testcase06() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario06();

        // when
        performBillingRun(0, "2013-11-01 10:00:00");

        // then
        // October Billing Period

        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_06", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-01 00:00:00", "2013-11-01 00:00:00");

        // before upgrade service (per unit price model)
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_06", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-10-23 10:00:00"), DateTimeHandling
                .calculateMillis("2013-10-26 08:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        // count subscriptions
        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        // period until suspend
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-23 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-26 08:00:00") + "");

        // factor 70 hrs +1 (because 8:00 is part of next unit)
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", "71.0", "71.00");

        // stepped user assignment costs
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "1.00", "0", "1",
                "1.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "1.00", "0.00", "1",
                "null", "0.00", "70.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "1.00");
        eva.assertOneTimeFee(priceModel, "10.00");

        // parameters
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-10-23 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-26 08:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "4.00", 71.0, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "2.00", 71.0, "0.00", "0.00", "0.0");
        // parameter role costs
        // costs and factor should be 0 because parameter is false
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 71.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "82.00");

        // period after resume of subscription
        // before upgrade service (per unit price model)
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_06", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-10-26 09:00:00"), DateTimeHandling
                .calculateMillis("2013-10-26 10:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // period after resume
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-26 09:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-26 10:00:00") + "");

        // factor 1 hrs +1 (because 10:00 is part of next time unit)
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", "2.0", "2.00");

        // stepped user assignment costs:
        // if a subscription is suspended and resumed, then stepped prices are
        // calculated as if a new subscription is started
        steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "1.00", "0", "1",
                "1.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "1.00", "0.00", "1",
                "null", "0.00", "1.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "1.00");
        eva.assertNullOneTimeFee(priceModel);

        // parameters
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-10-26 09:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-26 10:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "4.00", 2.0, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "2.00", 2.0, "0.00", "0.00", "0.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        // costs should be 0 because parameter is false
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 2.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "3.00");

        // period after upgrade of service to pro rata hour
        sub = getSubscriptionDetails("PICT_TEST_06", 1);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_06", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-10-26 10:00:00"), DateTimeHandling
                .calculateMillis("2013-10-29 10:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // period after upgrade until termination
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-10-26 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-29 10:00:00") + "");

        // event costs
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "1", "10.00");
        eva.assertGatheredEventsCosts(priceModel, "10.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1000.00", "73.0", "73000.00");

        // user assignment costs:
        // factor 48: 73 - 24hr -1 hr (dst) (deassigned for 1 day where dst is
        // changed)
        eva.assertUserAssignmentCosts(priceModel, PricingPeriod.HOUR.name(),
                null, 48.0, "1.00", "1.00");
        steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "1.00", "0", "1",
                "1.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "1.00", "0.00", "1",
                "null", "0.00", "47.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "1.00");
        eva.assertOneTimeFee(priceModel, "10.00");

        // assert parameter costs
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-10-26 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-29 10:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "4.00", 73.0, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "2.00", 48.0, "0.00", "0.00", "0.0");

        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 48.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "73021.00");

        eva.assertOverallVAT("20.0", "14621.20");
        eva.assertOverallCosts("73106.00", "EUR", "87727.20");

        // November Billing Period
        performBillingRun(0, "2013-12-01 10:00:00");

        sub = getSubscriptionDetails("PICT_TEST_06", 0);
        // result for November should not exist, subscription is terminated
        assertNull(getEvaluator(sub.getKey(), "2013-11-01 00:00:00",
                "2013-12-01 00:00:00"));

        sub = getSubscriptionDetails("PICT_TEST_06", 1);
        // result for November should not exist, subscription is terminated
        assertNull(getEvaluator(sub.getKey(), "2013-11-01 00:00:00",
                "2013-12-01 00:00:00"));
    }

    @Test
    public void testcase07() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario07();

        // when
        performBillingRun(4, "2013-07-19 10:00:00");

        // then
        // first Billing Period

        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_07", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-06-15 00:00:00", "2013-07-15 00:00:00");

        // for free period, subscription not in billing result
        assertNull(getEvaluator(sub.getKey(), "2013-07-15 00:00:00",
                "2013-07-15 00:00:00"));

        // when
        performBillingRun(4, "2013-08-19 10:00:00");

        // then
        // second Billing Period

        sub = getSubscriptionDetails("PICT_TEST_07", 0);
        eva = getEvaluator(sub.getKey(), "2013-07-15 00:00:00",
                "2013-08-15 00:00:00");
        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        // before upgrade service (pro rata price model)
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_07", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-08-01 12:00:00"), DateTimeHandling
                .calculateMillis("2013-08-03 12:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // charged after free period ends
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-01 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-03 12:00:00") + "");

        // event costs: only one event charged, rest free
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", null, "10", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "0.00", "0.06451612903225806", "0.00");

        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.00", "0.03225806451612903", "1",
                "0.03");

        eva.assertOneTimeFee(priceModel, "10.00");

        // parameter
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-01 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-03 12:00:00") + "",
                "4711", "LONG");

        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                0.06451612903225806, "6.45", "4711.0");
        // assert stepped prices for parameter
        Node steppedParamPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParamPrices, "0.00", "1.00", "0", "100",
                "100.00", "100");
        eva.assertSteppedPrice(steppedParamPrices, "100.00", "0.00", "100",
                "null", "0.00", "4611");
        eva.assertSteppedPricesAmount(steppedParamPrices, "100.00");
        // parameter role costs
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.024193548387096774,
                "113.98");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.008064516129032258,
                "75.98");
        eva.assertTotalRoleCosts(roleCosts, "189.96");
        eva.assertParameterCosts(parameter, "500.35");

        eva.assertPriceModelCosts(priceModel, "EUR", "530.38");

        // subscription after resume
        // until end of billing period
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "PICT_TEST_07", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-08-06 12:00:00"), DateTimeHandling
                .calculateMillis("2013-08-15 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eventCosts = BillingXMLNodeSearch.getGatheredEventsNode(priceModel);
        assertNull("No events should be charged", eventCosts);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "0.00", "0.27419354838709675", "0.00");

        // user was deassigned before suspend
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.00", "0.0", "0", "0.00");

        eva.assertNullOneTimeFee(priceModel);

        // parameter value 500
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-08 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-15 00:00:00") + "",
                "500", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                0.20967741935483872, "20.97", "500.0");
        // assert stepped prices for parameter
        steppedParamPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParamPrices, "0.00", "1.00", "0", "100",
                "100.00", "100");
        eva.assertSteppedPrice(steppedParamPrices, "100.00", "0.00", "100",
                "null", "0.00", "400");
        eva.assertSteppedPricesAmount(steppedParamPrices, "100.00");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 00, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "20.97");

        // parameter value 4711
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-08-06 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-08 12:00:00") + "",
                "4711", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                0.06451612903225806, "6.45", "4711.0");
        // assert stepped prices for parameter
        steppedParamPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParamPrices, "0.00", "1.00", "0", "100",
                "100.00", "100");
        eva.assertSteppedPrice(steppedParamPrices, "100.00", "0.00", "100",
                "null", "0.00", "4611");
        eva.assertSteppedPricesAmount(steppedParamPrices, "100.00");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 00, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 0.0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "6.45");

        eva.assertParametersCosts(priceModel, "27.42");
        eva.assertPriceModelCosts(priceModel, "EUR", "27.42");

        eva.assertOverallVAT("50.0", "278.90");
        eva.assertOverallCosts("557.80", "EUR", "836.70");

        performBillingRun(4, "2013-09-19 10:00:00");

        // third Billing Period

        // expect nothing since upgraded to free service
        // at beginning of this billing period
        sub = getSubscriptionDetails("PICT_TEST_07", 0);
        eva = getEvaluator(sub.getKey(), "2013-08-15 00:00:00",
                "2013-09-15 00:00:00");
        eva.assertOverallCosts("0.00", "EUR", "0.00");

    }

    @Test
    public void testcase08() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario08();

        // when
        performBillingRun(15, "2013-07-17 10:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_08", 0);
        // result should not exist, free
        assertNull(getEvaluator(sub.getKey(), "2013-06-02 00:00:00",
                "2013-07-02 00:00:00"));

        // when
        performBillingRun(15, "2013-08-17 10:00:00");

        // then
        // July Billing Period
        sub = getSubscriptionDetails("PICT_TEST_08", 0);
        // result should not exist, free subscription

        sub = getSubscriptionDetails("PICT_TEST_08", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-02 00:00:00", "2013-08-02 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_08");
        assertNotNull("Subscription doesn't exist", subscription);

        // after upgrade service (to pro-rata service)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-07-14 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-02 00:00:00") + "");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "2.7142857142857144", "27.14");
        // user assignment costs zero, users are deassigned
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "0.0", "0", "0.00");
        eva.assertNullOneTimeFee(priceModel);
        // parameters
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-07-14 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-02 00:00:00") + "",
                "2", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "0.00",
                2.7142857142857144, "0.00", null);
        // parameter role costs
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "27.14");

        eva.assertOverallDiscount("10.0", "2.71", "24.43", "27.14");
        eva.assertOverallCosts("24.43", "EUR", "24.43");

        // when
        performBillingRun(15, "2013-09-17 10:00:00");

        // then
        // August Billing Period (just verify that subscription continues)
        sub = getSubscriptionDetails("PICT_TEST_08", 0);
        // result should not exist, free subscription

        sub = getSubscriptionDetails("PICT_TEST_08", 1);
        eva = getEvaluator(sub.getKey(), "2013-08-02 00:00:00",
                "2013-09-02 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_08");
        assertNotNull("Subscription doesn't exist", subscription);
    }

    @Test
    public void testcase09() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario09();

        // when
        performBillingRun(4, "2013-04-5 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_09", 0);

        // *** Billing Period per time-unit hour
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_09" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        VOSubscriptionDetails subDetails = getSubscriptionDetails(
                "PICT_TEST_09", 0);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), subDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-03-21 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-31 11:59:59"));
        assertNotNull("Price Model doesn't exist", priceModel);

        // event costs: only one event charged, rest free for each event type
        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        Node eventNode = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_DOWNLOAD");
        Node steppedEvents = BillingXMLNodeSearch
                .getSteppedEventPricesNode(eventNode);
        eva.assertSteppedPrice(steppedEvents, "0.00", "10.00", "0", "1",
                "10.00", "1");
        eva.assertSteppedPrice(steppedEvents, "10.00", "0.00", "1", "null",
                "0.00", "99");
        eva.assertSteppedPricesAmount(steppedEvents, "10.00");
        eventNode = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_UPLOAD");
        steppedEvents = BillingXMLNodeSearch
                .getSteppedEventPricesNode(eventNode);
        eva.assertSteppedPrice(steppedEvents, "0.00", "20.00", "0", "1",
                "20.00", "1");
        eva.assertSteppedPrice(steppedEvents, "20.00", "0.00", "1", "null",
                "0.00", "99");
        eva.assertSteppedPricesAmount(steppedEvents, "20.00");
        eva.assertGatheredEventsCosts(priceModel, "30.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", 251, "251.00");

        // user assignment costs (stepped)
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), 251.00, "1", "254.00", "756.00");
        eva.assertUserAssignmentCostsByUser(priceModel, "PIC09Customer", 251);

        // user assignment roles costs
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "2.00", 251.00, "502.00");
        eva.assertTotalRoleCosts(roleCosts, "502.00");

        // parameters costs
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-03-21 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-31 11:59:59") + "",
                "2", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.HOUR.name(), "0.00",
                251, "0.00");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 251, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertOptionCosts(option, "251.00");
        eva.assertParameterCosts(parameter, "251.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1288.00");

        // back to per unit hour price model (but free period)
        sub = getSubscriptionDetails("PICT_TEST_09", 2);
        assertNull(getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-04-01 00:00:00"));

        eva.assertOverallCosts("1288.00", "EUR", "1288.00");

        performBillingRun(4, "2013-05-5 00:00:00");

        sub = getSubscriptionDetails("PICT_TEST_09", 2);

        // *** Billing Period per time-unit hour, again (terminate in free
        // period)
        assertNull(getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00"));
    }

    @Test
    /**
     * @throws Exception
     */
    public void testcase10() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario10();

        // when
        performBillingRun(28, "2013-07-29 10:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_10", 0);
        // result should not exist, free
        assertNull(getEvaluator(sub.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00"));

        // when
        performBillingRun(28, "2013-08-29 10:00:00");

        // then
        // July Billing Period

        // *** Billing Period per time-unit month
        sub = getSubscriptionDetails("PICT_TEST_10", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_10");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-07-14 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01  00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "1.00", "1.0", "1.00");

        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "5.00", "1.0", "1", "5.00");

        eva.assertOneTimeFee(priceModel, "10.00");

        // parameters
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-07-14 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-14 10:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "4.00", 0.4327956989247312, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.4327956989247312, "0.00", "0.00", "0.0");
        // parameter role costs
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.4327956989247312,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-07-14 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "4.00", 0.5672043010752689, "2.27", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.5672043010752689, "1.13", "1.70", "1.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        // costs should be 0 because parameter is false
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 0.5672043010752689,
                "0.57");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.57");
        eva.assertParameterCosts(parameter, "3.97");

        eva.assertParametersCosts(priceModel, "3.97");
        eva.assertPriceModelCosts(priceModel, "EUR", "19.97");

        eva.assertOverallDiscount("10.0", "2.00", "17.97", "19.97");
        eva.assertOverallCosts("17.97", "EUR", "26.96");
        eva.assertOverallVAT("50.0", "8.99");

        // when
        performBillingRun(28, "2013-09-29 10:00:00");

        // then
        // August Billing Period
        sub = getSubscriptionDetails("PICT_TEST_10", 2);
        // result should not exist, free subscription

        eva = getEvaluator(sub.getKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_10");
        assertNotNull("Subscription doesn't exist", subscription);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-08-14 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-20 12:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "10.00", "1.0", "10.00");

        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "50.00", "1.0", "1", "50.00");

        eva.assertOneTimeFee(priceModel, "100.00");

        // parameters -false
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-08-14 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-20 12:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "40.00", 1.0, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 1.0, "0.00", "0.00", "0.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 1.0, "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertParametersCosts(priceModel, "0.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "160.00");

        // price model per unit month #1 before 2nd upgrade
        sub = getSubscriptionDetails("PICT_TEST_10", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-14 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "1.00", "1.0", "1.00");

        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "5.00", "1.0", "1", "5.00");

        // parameter costs - true
        eva.assertNullOneTimeFee(priceModel);
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-14 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "4.00", 1, "4.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "2.00", 1.0, "2.00", "3.00", "1.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.00, "1.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "1.00");
        eva.assertParameterCosts(parameter, "7.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "13.00");

        eva.assertOverallDiscount("10.0", "17.30", "155.70", "173.00");
        eva.assertOverallCosts("155.70", "EUR", "233.55");
        eva.assertOverallVAT("50.0", "77.85");

    }

    @Test
    public void testcase11() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario11();

        // when
        performBillingRun(0, "2013-08-01 10:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_11", 0);
        // result should not exist, week has not ended
        assertNull(getEvaluator(sub.getKey(), "2013-07-01 00:00:00",
                "2013-08-01 00:00:00"));

        // when
        performBillingRun(0, "2013-09-01 10:00:00");

        // then
        // August Billing Period

        // *** Billing Period per time-unit month
        sub = getSubscriptionDetails("PICT_TEST_11", 2);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_11" + "_SubID2");
        assertNotNull("Subscription doesn't exist", subscription);

        // 2nd upgrade price model per unit WEEK in free period 100 days, so no
        // costs in this billing period (no billing output)

        sub = getSubscriptionDetails("PICT_TEST_11", 0);
        // price model per unit WEEK after free period 10 days
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-08-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-15 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "4.00", "2.0", "8.00");

        // event costs
        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        Node event = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_UPLOAD");
        Node steppedEventPrices = BillingXMLNodeSearch
                .getSteppedEventPricesNode(event);
        eva.assertSteppedPrice(steppedEventPrices, "0.00", "20.00", "0", "1",
                "20.00", "1");
        eva.assertSteppedPrice(steppedEventPrices, "20.00", "0.00", "1",
                "null", "0.00", "9");
        eva.assertSteppedPricesAmount(steppedEventPrices, "20.00");
        eva.assertEventCosts(event, "10", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");

        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "1", "5.00");
        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1.0");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "1.00", "2",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "5.00");

        // parameter 2nd true period
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-08-14 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-15 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "4.00", 0.7142857142857143, "2.86", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.7142857142857143, "1.43", "1.43", "1.0");
        // parameter role costs
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.011904761904761904,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.7023809523809523,
                "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "4.29");

        // parameter false period
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-08-13 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-14 00:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "4.00", 0.14285714285714285, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.14285714285714285, "0.00", "0.00", "0.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.14285714285714285,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        // parameter 1st true period
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-08-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-13 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "4.00", 1.1428571428571428, "4.57", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.1428571428571428, "2.29", "2.29", "1.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 1.1428571428571428,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "6.86");

        eva.assertParametersCosts(priceModel, "11.15");
        eva.assertPriceModelCosts(priceModel, "EUR", "44.15");

        eva.assertOverallDiscount("10.0", "4.42", "39.73", "44.15");
        eva.assertOverallCosts("39.73", "EUR", "47.68");
        eva.assertOverallVAT("20.0", "7.95");
    }

    @Test
    /**
     * @throws Exception
     */
    public void testcase12() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario12();

        // when
        performBillingRun(0, "2013-09-03 10:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_12", 0);
        // result should not exist, month has not ended
        assertNull(getEvaluator(sub.getKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00"));

        // when
        performBillingRun(0, "2013-09-06 10:00:00");

        // then
        // August Billing Period

        sub = getSubscriptionDetails("PICT_TEST_12", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-05 00:00:00", "2013-09-05 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_12");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-08-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "0.00", 0.9720430107526882, "0.00");

        // user assignment costs
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser1", 0.03225806451612903);
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Pict12DiscountedCustomer", 0.9720430107526882);
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.00", 1.0043010752688173, "2",
                "1.00", "1.00");

        // parameter true period
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.843010752688172, "0.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.843010752688172, "0.00", "0.00", "1.0");
        // parameter role costs
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.843010752688172,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        // parameter false period
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-08-06 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.12903225806451613, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.16129032258064516, "0.00", "0.00", "0.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.12903225806451613,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.03225806451612903,
                "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertParametersCosts(priceModel, "0.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1.00");

        eva.assertOverallDiscount("10.0", "0.10", "0.90", "1.00");
        eva.assertOverallCosts("0.90", "EUR", "0.99");
        eva.assertOverallVAT("10.0", "0.09");

        // when
        performBillingRun(0, "2013-10-06 10:00:00");

        // then
        // September Billing Period

        sub = getSubscriptionDetails("PICT_TEST_12", 0);
        eva = getEvaluator(sub.getKey(), "2013-09-05 00:00:00",
                "2013-10-05 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_12");
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "0.00", "0.16666666666666666", "0.00");

        // user assignment costs
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser1", 0.06666666666666667);
        eva.assertUserAssignmentCostsByUser(priceModel,
                "Pict12DiscountedCustomer", 0.16666666666666666);
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.00", "0.23333333333333334", "2",
                "0.23");

        // parameter false period
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-09-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00") + "",
                "false", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.1, "0.00", "0.0");
        // TODO: 0.16666666666666667 double bug #10312
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.16666666666666669, "0.00", "0.00", "0.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0.06666666666666667,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.1, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        // parameter true period
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-09-05 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-09-07 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.06666666666666667, "0.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.00", 0.06666666666666667, "0.00", "0.00", "1.0");
        // parameter role costs
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertRoleCost(roleCosts, "ADMIN", "0.00", 0.06666666666666667,
                "0.00");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0, "0.00");
        eva.assertRoleCost(roleCosts, "USER", "0.00", 0, "0.00");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        eva.assertParametersCosts(priceModel, "0.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "0.23");

        eva.assertOverallCosts("0.23", "EUR", "0.25");
        eva.assertOverallVAT("10.0", "0.02");

    }

    @Test
    /**
     * @throws Exception
     */
    public void testcase13() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario13();

        // when
        performBillingRun(15, "2013-11-16 10:00:00");

        // then
        // October Billing Period

        // *** Billing Period per time-unit hour
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_13", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-01 00:00:00", "2013-11-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_13");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-10-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-10-30 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "1.00", "650.0", "650.00");

        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "1253.0", "2", "5.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser1", 603.0);
        eva.assertUserAssignmentCostsByUser(priceModel, "Pict13Customer", 650.0);
        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "0.00", "2",
                "null", "0.00", "1251.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "5.00");

        eva.assertOneTimeFee(priceModel, "10.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 2);
        // initial parameter value 35
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-10-03 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-04 10:20:00") + "",
                "35", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 34.33333333333333, "0.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "4.00", 68.66666666666666, "9613.33", "13218.33", "35.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "3605.00");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 34.33333333333333,
                "2403.33");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 34.33333333333333,
                "1201.67");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "13218.33");

        // parameter after value changed to 5
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-10-04 10:20:00") + "",
                DateTimeHandling.calculateMillis("2013-10-30 00:00:00") + "",
                "5", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.HOUR.name(),
                "0.00", 615.6666666666666, "0.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.HOUR.name(),
                "4.00", 1184.3333333333335, "23686.67", "32450.84", "5.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "8764.17");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 568.5, "5685.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 615.8333333333333,
                "3079.17");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "32450.84");

        eva.assertParametersCosts(priceModel, "45669.17");
        eva.assertPriceModelCosts(priceModel, "EUR", "46334.17");

        eva.assertOverallCosts("46334.17", "EUR", "46334.17");

        // when
        performBillingRun(15, "2013-12-16 10:00:00");
        // then
        // November Billing Period
        // then
        sub = getSubscriptionDetails("PICT_TEST_13", 0);
        // result should not exist, upgraded to free
        assertNull(getEvaluator(sub.getKey(), "2013-11-01 00:00:00",
                "2013-12-01 00:00:00"));

        sub = getSubscriptionDetails("PICT_TEST_13", 1);
        // free sub should not appear in billing
        assertNull(getEvaluator(sub.getKey(), "2013-11-01 00:00:00",
                "2013-12-01 00:00:00"));

    }

    @Test
    /**
     * same as testcase13() but with time unit day price model
     * @throws Exception
     */
    public void testcase13_1() throws Exception {
        // given subscription data for scenario
        testSetup.createPictScenario13_1();

        // when
        performBillingRun(15, "2013-11-16 10:00:00");

        // then
        // October Billing Period

        // *** Billing Period per time-unit hour
        VOSubscriptionDetails sub = getSubscriptionDetails("PICT_TEST_13_1", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-10-01 00:00:00", "2013-11-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(
                eva.getBillingResult(), "PICT_TEST_13_1");
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-10-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-10-30 00:00:00"));
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "1.00", "28.0", "28.00");

        // user assignment costs
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "55.0", "2", "5.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceSecondCustomerUser1", 27.0);
        eva.assertUserAssignmentCostsByUser(priceModel, "Pict13_1Customer",
                28.0);
        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "3.00", "0", "1",
                "3.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "3.00", "2.00", "1", "2",
                "2.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "5.00", "0.00", "2",
                "null", "0.00", "53.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "5.00");

        eva.assertOneTimeFee(priceModel, "10.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 2);
        // initial parameter value 35
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-10-03 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-04 10:00:00") + "",
                "35", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "0.00", 1.4166666666666667, "0.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "4.00", 2.8333333333333335, "396.67", "545.42", "35.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "148.75");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 1.4166666666666667,
                "99.17");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 1.4166666666666667,
                "49.58");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "545.42");

        // parameter after value changed to 5
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-10-04 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-10-30 00:00:00") + "",
                "5", "INTEGER");
        assertNotNull("Parameter doesn't exist", parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "0.00", 26.583333333333332, "0.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "4.00", 52.16666666666667, "1043.33", "1431.88", "5.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "388.55");
        eva.assertRoleCost(roleCosts, "USER", "2.00", 25.541666666666668,
                "255.42");
        eva.assertRoleCost(roleCosts, "ADMIN", "1.00", 26.625, "133.13");
        eva.assertRoleCost(roleCosts, "GUEST", "0.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "1431.88");

        eva.assertParametersCosts(priceModel, "1977.30");
        eva.assertPriceModelCosts(priceModel, "EUR", "2020.30");

        eva.assertOverallCosts("2020.30", "EUR", "2020.30");

        // when
        performBillingRun(15, "2013-12-16 10:00:00");
        // then
        // November Billing Period
        // then
        sub = getSubscriptionDetails("PICT_TEST_13_1", 0);
        // result should not exist, upgraded to free
        assertNull(getEvaluator(sub.getKey(), "2013-11-01 00:00:00",
                "2013-12-01 00:00:00"));

        sub = getSubscriptionDetails("PICT_TEST_13_1", 1);
        // free sub should not appear in billing
        assertNull(getEvaluator(sub.getKey(), "2013-11-01 00:00:00",
                "2013-12-01 00:00:00"));

    }
}
