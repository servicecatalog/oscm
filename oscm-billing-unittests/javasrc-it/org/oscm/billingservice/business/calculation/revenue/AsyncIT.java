/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 3, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.AsyncSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author sdehn
 * 
 */

public class AsyncIT extends BillingIntegrationTestBase {

    private final AsyncSetup testSetup = new AsyncSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void testcase_01() throws Exception {
        // given
        testSetup.createAsyncScenario01();

        // when
        performBillingRun(0, "2013-07-01 12:00:00");

        // then
        VOSubscriptionDetails subscr = getSubscriptionDetails("test_01", 0);
        BillingResultEvaluator eva = getEvaluator(subscr.getKey(),
                "2013-06-1 00:00:00", "2013-07-01 00:00:00");

        assertNull(eva);
    }

    @Test
    public void testcase_01_01() throws Exception {
        // given
        testSetup.createAsyncScenario01_01();

        // when
        performBillingRun(0, "2013-07-01 12:00:00");

        // then
        VOSubscriptionDetails subscr = getSubscriptionDetails("test_01_01", 0);
        BillingResultEvaluator eva = getEvaluator(subscr.getKey(),
                "2013-06-1 00:00:00", "2013-07-01 00:00:00");

        assertNull(eva);
    }

    @Test
    public void testcase_04() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario04();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_04", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        // subscription entry before suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "48.58");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "0.14285714285714285", "1.43");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "0.14285714285714285", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-08 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-09 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.14285714285714285, "4.29", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.14285714285714285, "42.86", "42.86", "15.0");
        eva.assertParameterCosts(parameter, "47.15");

        // subscription entry after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "1262.86");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "3.7142857142857144", "37.14");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "3.7142857142857144", "1",
                "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-11 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-07 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.7142857142857144, "111.43", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.7142857142857144, "1114.29", "1114.29", "15.0");
        eva.assertParameterCosts(parameter, "1225.72");
        eva.assertPriceModelCosts(priceModel, "EUR", "1262.86");

        eva.assertOverallCosts("EUR", "1311.44");
    }

    @Test
    public void testcase_05() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario05();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_05", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");
        eva.assertNullOneTimeFee();

        // subscription entry before suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "627.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "3.0", "18.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "3.0", "1", "15.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 3.0, "9.00");
        eva.assertTotalRoleCosts(roleCosts, "9.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-07 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-09 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 3.0, "90.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 3.0, "360.00", "495.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "135.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 3.0, "135.00");
        eva.assertParameterCosts(parameter, "585.00");

        // subscription entry after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "5434.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "26.0", "156.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "26.0", "1", "130.00");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 26.0, "78.00");
        eva.assertTotalRoleCosts(roleCosts, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-11 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-07 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 26.0, "780.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 26.0, "3120.00", "4290.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "1170.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 26.0, "1170.00");
        eva.assertParameterCosts(parameter, "5070.00");

        eva.assertOverallCosts("EUR", "6061.00");
    }

    @Test
    public void testcase_05_01() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario05_01();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_05_01", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        // subscription entry before suspend after free
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "340.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-08 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-09 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1, "300.00", "300.00", "15.0");
        eva.assertParameterCosts(parameter, "330.00");

        // subscription entry after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "1020.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "3.0", "30.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "3.0", "1", "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-11 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.0, "90.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.0, "900.00", "900.00", "15.0");
        eva.assertParameterCosts(parameter, "990.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1020.00");

        eva.assertOverallCosts("EUR", "1360.00");
    }

    @Test
    public void testcase_06() throws Exception {
        // given
        testSetup.createAsyncScenario_06();

        // when
        performBillingRun(1, "2013-07-2 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_06", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "4.285714285714286", "42.86");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "4.285714285714286", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.5714285714285714, "15.71", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.5714285714285714, "157.14", "157.14", "5.0");
        eva.assertParameterCosts(parameter, "172.85");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.714285714285714, "81.43", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.714285714285714, "814.29", "814.29", "15.0");
        eva.assertParameterCosts(parameter, "895.72");
        eva.assertParametersCosts(priceModel, "1068.57");

        eva.assertPriceModelCosts(priceModel, "EUR", "1111.43");
        eva.assertOverallCosts("EUR", "1111.43");
    }

    @Test
    public void testcase_07() throws Exception {
        // given
        testSetup.createAsyncScenario_07();

        // when
        performBillingRun(1, "2013-07-2 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_07", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "5.0", "50.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "5.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-28 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.42857142857142855, "4.29", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.42857142857142855, "42.86", "42.86", "5.0");
        eva.assertParameterCosts(parameter, "47.15");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-28 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 4.571428571428571, "137.14", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 4.571428571428571, "1371.43", "1371.43", "15.0");
        eva.assertParameterCosts(parameter, "1508.57");

        eva.assertPriceModelCosts(priceModel, "EUR", "1605.72");
        eva.assertOverallCosts("EUR", "1605.72");
    }

    @Test
    public void testcase_08() throws Exception {
        // given
        testSetup.createAsyncScenario_08();

        // when
        performBillingRun(1, "2013-07-2 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_08", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "4.285714285714286", "42.86");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "4.285714285714286", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 4.285714285714286, "128.57", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 4.285714285714286, "1285.71", "1285.71", "15.0");
        eva.assertParameterCosts(parameter, "1414.28");

        eva.assertPriceModelCosts(priceModel, "EUR", "1457.14");
        eva.assertOverallCosts("EUR", "1457.14");
    }

    @Test
    public void testcase_09() throws Exception {
        // given
        testSetup.createAsyncScenario_09();

        // when
        performBillingRun(1, "2013-07-2 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_09", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-27 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "3.714285714285714", "37.14");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "3.714285714285714", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-27 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.714285714285714, "111.43", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.714285714285714, "1114.29", "1114.29", "15.0");
        eva.assertParameterCosts(parameter, "1225.72");

        eva.assertPriceModelCosts(priceModel, "EUR", "1262.86");
        eva.assertOverallCosts("EUR", "1262.86");
    }

    @Test
    public void testcase_10() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario10();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_10", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "10.00", "0.5666666666666667", "5.67");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.00", "0.5666666666666667", "1",
                "0.00");

        // parameter fills up unit modify
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-26 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterCosts(parameter, "132.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-26 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterCosts(parameter, "18.34");
        eva.assertParametersCosts(priceModel, "150.34");
        eva.assertPriceModelCosts(priceModel, "EUR", "156.01");

        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-13 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "136.00");
        eva.assertOverallCosts("EUR", "292.01");
    }

    @Test
    public void testcase_11() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario11();

        // when
        performBillingRun(1, "2013-07-12 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_11", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "10.00", "0.5666666666666667", "5.67");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.00", "0.5666666666666667", "1",
                "0.00");

        // parameter fills up unit modify
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterCosts(parameter, "187.00");

        eva.assertParametersCosts(priceModel, "187.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "192.67");

        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-13 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "136.00");

        eva.assertOverallCosts("EUR", "328.67");
    }

    @Test
    public void testcase_12() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario12();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_12", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-27 00:00:00"));

        // parameter fills up unit modify
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-26 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterCosts(parameter, "132.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-26 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-27 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterCosts(parameter, "3.66");
        eva.assertParametersCosts(priceModel, "135.66");
        eva.assertPriceModelCosts(priceModel, "EUR", "139.99");

        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-13 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "136.00");
        eva.assertOverallCosts("EUR", "275.99");
    }

    @Test
    public void testcase_13() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario13();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_13", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "10.00", "1.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.00", "1.0", "1", "0.00");

        // parameter fills up unit modify
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-16 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.5, "15.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.5, "150.00", "150.00", "15.0");
        eva.assertParameterCosts(parameter, "165.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-16 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.5, "5.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.5, "50.00", "50.00", "5.0");
        eva.assertParameterCosts(parameter, "55.00");
        eva.assertParametersCosts(priceModel, "220.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "230.00");
        eva.assertOverallCosts("EUR", "230.00");
    }

    @Test
    public void testcase_14() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario14();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_14", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        // only one subscription, subscription terminates before resume
        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        // billing until suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.857142857142857", "18.57");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.857142857142857", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.857142857142857, "55.71", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.857142857142857, "557.14", "557.14", "15.0");
        eva.assertParameterCosts(parameter, "612.85");

        eva.assertPriceModelCosts(priceModel, "EUR", "631.42");

        // billing is started after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-22 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.2857142857142856", "12.86");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.2857142857142856", "1",
                "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-22 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.2857142857142856, "12.86", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.2857142857142856, "128.57", "128.57", "5.0");
        eva.assertParameterCosts(parameter, "141.43");

        eva.assertPriceModelCosts(priceModel, "EUR", "154.29");

        eva.assertOverallCosts("EUR", "785.71");
    }

    @Test
    public void testcase_15() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario15();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_15", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        // only one subscription, subscription terminates before resume
        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing until suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.857142857142857", "18.57");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.857142857142857", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.857142857142857, "55.71", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.857142857142857, "557.14", "557.14", "15.0");
        eva.assertParameterCosts(parameter, "612.85");

        eva.assertPriceModelCosts(priceModel, "EUR", "631.42");

        eva.assertOverallCosts("EUR", "631.42");
    }

    @Test
    public void testcase_16() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario16();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_16", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "5.0", "50.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "5.0", "1", "0.00");

        // parameter fills up unit unit modify
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.571428571428571, "77.14", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.571428571428571, "771.43", "771.43", "15.0");
        eva.assertParameterCosts(parameter, "848.57");

        // parameter after modify (unit filled up)
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.4285714285714284, "24.29", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.4285714285714284, "242.86", "242.86", "5.0");
        eva.assertParameterCosts(parameter, "267.15");

        eva.assertParametersCosts(priceModel, "1115.72");
        eva.assertPriceModelCosts(priceModel, "EUR", "1165.72");
        eva.assertOverallCosts("EUR", "1165.72");
    }

    @Test
    public void testcase_16_01() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario16_01();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_16_01", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing before suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "5.0", "50.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "5.0", "1", "0.00");

        // parameter fills up unit unit modify
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.571428571428571, "77.14", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.571428571428571, "771.43", "771.43", "15.0");
        eva.assertParameterCosts(parameter, "848.57");

        // parameter after modify (unit filled up)
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.4285714285714284, "24.29", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.4285714285714284, "242.86", "242.86", "5.0");
        eva.assertParameterCosts(parameter, "267.15");

        eva.assertParametersCosts(priceModel, "1115.72");
        eva.assertPriceModelCosts(priceModel, "EUR", "1165.72");
        eva.assertOverallCosts("EUR", "1165.72");
    }

    @Test
    public void testcase_17() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario17();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_17", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        // only one subscription, subscription terminates before resume
        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing until suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.857142857142857", "18.57");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.857142857142857", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.857142857142857, "55.71", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.857142857142857, "557.14", "557.14", "15.0");
        eva.assertParameterCosts(parameter, "612.85");

        eva.assertPriceModelCosts(priceModel, "EUR", "631.42");

        eva.assertOverallCosts("EUR", "631.42");
    }

    @Test
    public void testcase_17_01() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario17_01();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_17_01", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        // billing until suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.857142857142857", "18.57");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.857142857142857", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.857142857142857, "55.71", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.857142857142857, "557.14", "557.14", "15.0");
        eva.assertParameterCosts(parameter, "612.85");

        eva.assertPriceModelCosts(priceModel, "EUR", "631.42");

        // subscription resume (for exactly 1 week) before terminate
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-17 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-24 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.0", "1", "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-17 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-24 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "300.00", "300.00", "15.0");
        eva.assertParameterCosts(parameter, "330.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "340.00");
        eva.assertOverallCosts("EUR", "971.42");
    }

    @Test
    public void testcase_18() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario18();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_18", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        // only one subscription, subscription terminates before resume
        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing until suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "4.285714285714286", "42.86");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "4.285714285714286", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 4.285714285714286, "128.57", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 4.285714285714286, "1285.71", "1285.71", "15.0");
        eva.assertParameterCosts(parameter, "1414.28");

        eva.assertPriceModelCosts(priceModel, "EUR", "1457.14");

        eva.assertOverallCosts("EUR", "1457.14");
    }

    @Test
    public void testcase_19() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario19();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_19", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        // billing until suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-13 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.7142857142857142", "17.14");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.7142857142857142", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-13 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.7142857142857142, "51.43", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.7142857142857142, "514.29", "514.29", "15.0");
        eva.assertParameterCosts(parameter, "565.72");

        eva.assertPriceModelCosts(priceModel, "EUR", "582.86");

        // billing after suspend
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-20 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.5714285714285714", "15.71");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.5714285714285714", "1",
                "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.5714285714285714, "47.14", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.5714285714285714, "471.43", "471.43", "15.0");
        eva.assertParameterCosts(parameter, "518.57");

        eva.assertPriceModelCosts(priceModel, "EUR", "534.28");

        eva.assertOverallCosts("EUR", "1117.14");
    }

    @Test
    public void testcase_20() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario20();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_20", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "5.0", "50.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "5.0", "1", "0.00");

        // parameter fills up unit unit modify
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-01 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 5, "150.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 5, "1500.00", "1500.00", "15.0");
        eva.assertParameterCosts(parameter, "1650.00");

        eva.assertParametersCosts(priceModel, "1650.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1700.00");
        eva.assertOverallCosts("EUR", "1700.00");
    }

    @Test
    public void testcase_21() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario21();

        // when
        performBillingRun(1, "2013-07-2 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_21", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-12 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "485.72");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "3.0", "30.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "3.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.42857142857142845, "24.29", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.42857142857142845, "242.86", "242.86", "5.0");
        eva.assertParameterCosts(parameter, "267.15");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-12 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.5714285714285714, "17.14", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.5714285714285714, "171.43", "171.43", "15.0");
        eva.assertParameterCosts(parameter, "188.57");
        eva.assertParametersCosts(priceModel, "455.72");

        eva.assertOverallCosts("EUR", "485.72");
    }

    @Test
    public void testcase_22() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario22();

        // when
        performBillingRun(1, "2013-07-2 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_22", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-19 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "2.0", "20.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "2.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.5714285714285714, "15.71", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.5714285714285714, "157.14", "157.14", "5.0");
        eva.assertParameterCosts(parameter, "172.85");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-19 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.42857142857142855, "12.86", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.42857142857142855, "128.57", "128.57", "15.0");
        eva.assertParameterCosts(parameter, "141.43");
        eva.assertParametersCosts(priceModel, "314.28");

        eva.assertPriceModelCosts(priceModel, "EUR", "334.28");
        eva.assertOverallCosts("EUR", "334.28");
    }

    @Test
    public void testcase_23() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario23();

        // when
        performBillingRun(1, "2013-07-2 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_23", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");
        // count subscriptions
        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing charged beginning with resume
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-20 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.5714285714285714", "15.71");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.5714285714285714", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.5714285714285714, "15.71", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.5714285714285714, "157.14", "157.14", "5.0");
        eva.assertParameterCosts(parameter, "172.85");

        eva.assertPriceModelCosts(priceModel, "EUR", "188.56");
    }

    @Test
    public void testcase_24() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario24();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_24", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing starts after free period (which is after resume)
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-25 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "10.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "100.00", "100.00", "5.0");
        eva.assertParameterCosts(parameter, "110.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "120.00");
        eva.assertOverallCosts("EUR", "120.00");
    }

    @Test
    public void testcase_24_01() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario24_01();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_24_01", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing starts with resume, free period ends before this
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-20 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "2.0", "20.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "2.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "20.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "200.00", "200.00", "5.0");
        eva.assertParameterCosts(parameter, "220.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "240.00");
        eva.assertOverallCosts("EUR", "240.00");
    }

    @Test
    public void testcase_24_02() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario24_02();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_24_02", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 2);

        // billing is started after free period ends to start of suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-13 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.0", "10.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-13 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "300.00", "300.00", "15.0");
        eva.assertParameterCosts(parameter, "330.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "340.00");

        // billing is charged again after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-20 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "2.0", "20.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "2.0", "1", "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-20 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "20.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "200.00", "200.00", "5.0");
        eva.assertParameterCosts(parameter, "220.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "240.00");

        eva.assertOverallCosts("EUR", "580.00");
    }

    @Test
    public void testcase_25() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario25();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_25", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing is started after resume
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-22 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "1.2857142857142856", "12.86");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "1.2857142857142856", "1",
                "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-22 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.2857142857142856, "12.86", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.2857142857142856, "128.57", "128.57", "5.0");
        eva.assertParameterCosts(parameter, "141.43");

        eva.assertPriceModelCosts(priceModel, "EUR", "154.29");

        eva.assertOverallCosts("EUR", "154.29");
    }

    @Test
    public void testcase_26() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario26();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_26", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        // billing is started after free period ends
        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing starts with resume, free period ends before this
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "2.0", "20.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "2.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-18 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "20.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "200.00", "200.00", "5.0");
        eva.assertParameterCosts(parameter, "220.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "240.00");
        eva.assertOverallCosts("EUR", "240.00");
    }

    @Test
    public void testcase_27() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario27();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_27", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        // billing starts with resume, free period ends before this
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-16 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "3.0", "30.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "3.0", "1", "0.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-16 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.0, "30.00", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.0, "300.00", "300.00", "5.0");
        eva.assertParameterCosts(parameter, "330.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "360.00");
        eva.assertOverallCosts("EUR", "360.00");
    }

    @Test
    public void testcase_27_01() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario27_01();

        // when
        performBillingRun(1, "2013-07-02 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "test_27_01", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        // billing is started after free period ends
        eva.assertNodeCount("//Subscriptions/Subscription", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-14 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 00:00:00"));
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 2);

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "10.00", "3.0", "30.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.00", "3.0", "1", "0.00");

        // after free period, before async update complete
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 00:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 12:00:00")),
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.6428571428571429, "19.29", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.6428571428571429, "192.86", "192.86", "15.0");
        eva.assertParameterCosts(parameter, "212.15");

        // after async update complete
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-06-14 12:00:00")),
                String.valueOf(DateTimeHandling
                        .calculateMillis("2013-07-01 00:00:00")),
                "5", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.357142857142857, "23.57", "5.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.357142857142857, "235.71", "235.71", "5.0");
        eva.assertParameterCosts(parameter, "259.28");

        eva.assertParametersCosts(priceModel, "471.43");

        eva.assertPriceModelCosts(priceModel, "EUR", "501.43");
        eva.assertOverallCosts("EUR", "501.43");
    }

}
