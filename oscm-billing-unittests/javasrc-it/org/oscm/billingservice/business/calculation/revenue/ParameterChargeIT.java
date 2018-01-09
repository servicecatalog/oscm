/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 3, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.ParameterChargeSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.TestBasicSetup;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author baumann
 * 
 */
public class ParameterChargeIT extends BillingIntegrationTestBase {

    private final ParameterChargeSetup setup = new ParameterChargeSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void billingPerUnitMonthParAndRoleChange() throws Exception {
        // given subscription data for scenario
        setup.createMonthScenarioParAndRoleChange();

        // when
        performBillingRun(13, "2013-03-15 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_MONTH_ROLES", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "");
        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", 1.0D, "1", "345.00",
                "351.50");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.5, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.5, "2.50");
        eva.assertTotalRoleCosts(roleCosts, "6.50");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 5);
        eva.assertParametersCosts(priceModel, "853.85");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "30.00", 1.0, "30.00", "36.50", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "6.50");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.5, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.5, "2.50");
        eva.assertParameterCosts(parameter, "39.50");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.MONTH.name(),
                "2.00", 1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.MONTH.name(),
                "1.10", 1.0, "1.10", "7.60", null);
        eva.assertTotalRoleCosts(roleCosts, "6.50");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.5, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.5, "2.50");
        eva.assertOptionCosts(option, "9.60");
        eva.assertParameterCosts(parameter, "9.60");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.375, "11.25", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.375, "112.50", "157.50", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "45.00");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.375, "45.00");
        eva.assertParameterCosts(parameter, "168.75");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                "4", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.375, "3.00", "4.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.375, "30.00", "39.00", "4.0");
        eva.assertTotalRoleCosts(roleCosts, "9.00");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.125, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.25, "5.00");
        eva.assertParameterCosts(parameter, "42.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "88", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.25, "44.00", "88.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.25, "440.00", "550.00", "88.0");
        eva.assertTotalRoleCosts(roleCosts, "110.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.25, "110.00");
        eva.assertParameterCosts(parameter, "594.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "2006.35");
        eva.assertOverallCosts("EUR", "2006.35");
    }

    @Test
    public void billingPerUnitWeekParAndRoleChange() throws Exception {
        // given subscription data for scenario
        setup.createWeekScenarioParAndRoleChange();

        // when
        performBillingRun(21, "2013-04-25 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_WEEK_ROLES", 0);

        // *** billing result for first period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 4.0D, "1", "600.00",
                "628.50");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "4.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 3);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "28.50");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 6);
        eva.assertParametersCosts(priceModel, "1230.90");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.0, "120.00", "148.50", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "28.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertParameterCosts(parameter, "160.50");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 4.0, "4.40", "32.90", null);
        eva.assertTotalRoleCosts(roleCosts, "28.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertOptionCosts(option, "40.90");
        eva.assertParameterCosts(parameter, "40.90");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "300.00", "390.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "90.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "90.00");
        eva.assertParameterCosts(parameter, "420.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "4.00", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "40.00", "54.00", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "14.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "14.00");
        eva.assertParameterCosts(parameter, "58.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                "7", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.25, "17.50", "7.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.25, "175.00", "241.50", "7.0");
        eva.assertTotalRoleCosts(roleCosts, "66.50");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.5, "24.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.75, "42.00");
        eva.assertParameterCosts(parameter, "259.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.75, "19.50", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.75, "195.00", "273.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "78.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.75, "78.00");
        eva.assertParameterCosts(parameter, "292.50");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "2224.40");
        eva.assertOverallCosts("EUR", "2224.40");

        // *** billing result for second period ***
        // last week of February ends in March, thus it must be charged there
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "");

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 1.0D, "1", "150.00",
                "158.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertTotalRoleCosts(roleCosts, "8.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "442.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 1.0, "30.00", "38.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertParameterCosts(parameter, "41.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "1", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 1.0, "1.10", "9.10", null);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertOptionCosts(option, "11.10");
        eva.assertParameterCosts(parameter, "11.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "26.00", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "260.00", "364.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "104.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "104.00");
        eva.assertParameterCosts(parameter, "390.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "685.10");
        eva.assertOverallCosts("EUR", "685.10");
    }

    @Test
    public void billingPerUnitWeekParAndRoleChange2() throws Exception {
        // given subscription data for scenario
        setup.createWeekScenarioParAndRoleChange2();

        // when
        performBillingRun(21, "2013-04-25 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_WEEK_ROLES2", 0);

        // *** billing result for first period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 4.0D, "1", "600.00",
                "628.50");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "4.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 3);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "28.50");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 6);
        eva.assertParametersCosts(priceModel, "1230.90");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.0, "120.00", "148.50", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "28.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertParameterCosts(parameter, "160.50");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 4.0, "4.40", "32.90", null);
        eva.assertTotalRoleCosts(roleCosts, "28.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertOptionCosts(option, "40.90");
        eva.assertParameterCosts(parameter, "40.90");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "300.00", "390.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "90.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "90.00");
        eva.assertParameterCosts(parameter, "420.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "4.00", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "40.00", "54.00", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "14.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "14.00");
        eva.assertParameterCosts(parameter, "58.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                "7", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.25, "17.50", "7.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.25, "175.00", "241.50", "7.0");
        eva.assertTotalRoleCosts(roleCosts, "66.50");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.5, "24.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.75, "42.00");
        eva.assertParameterCosts(parameter, "259.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.75, "19.50", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.75, "195.00", "273.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "78.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.75, "78.00");
        eva.assertParameterCosts(parameter, "292.50");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "2224.40");
        eva.assertOverallCosts("EUR", "2224.40");

        // *** billing result for second period ***
        // last week of February ends in March, thus it must be charged there
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 1.0D, "1", "150.00",
                "158.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertTotalRoleCosts(roleCosts, "8.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "442.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 1.0, "30.00", "38.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertParameterCosts(parameter, "41.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "1", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 1.0, "1.10", "9.10", null);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertOptionCosts(option, "11.10");
        eva.assertParameterCosts(parameter, "11.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "26.00", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "260.00", "364.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "104.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "104.00");
        eva.assertParameterCosts(parameter, "390.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "685.10");
        eva.assertOverallCosts("EUR", "685.10");
    }

    @Test
    public void billingPerUnitMonthParAndUserAssignChange() throws Exception {
        // given subscription data for scenario
        setup.createMonthScenarioParAndUserAssignChange();

        // when
        performBillingRun(3, "2013-03-05 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_MONTH_ASSIGN", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "");
        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", 1.0D, "1", "345.00",
                "351.50");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.5, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.5, "2.50");
        eva.assertTotalRoleCosts(roleCosts, "6.50");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 5);
        eva.assertParametersCosts(priceModel, "853.85");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "30.00", 1.0, "30.00", "36.50", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "6.50");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.5, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.5, "2.50");
        eva.assertParameterCosts(parameter, "39.50");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.MONTH.name(),
                "2.00", 1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.MONTH.name(),
                "1.10", 1.0, "1.10", "7.60", null);
        eva.assertTotalRoleCosts(roleCosts, "6.50");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.5, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.5, "2.50");
        eva.assertOptionCosts(option, "9.60");
        eva.assertParameterCosts(parameter, "9.60");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.375, "11.25", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.375, "112.50", "157.50", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "45.00");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.375, "45.00");
        eva.assertParameterCosts(parameter, "168.75");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                "4", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.375, "3.00", "4.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.375, "30.00", "39.00", "4.0");
        eva.assertTotalRoleCosts(roleCosts, "9.00");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.125, "4.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.25, "5.00");
        eva.assertParameterCosts(parameter, "42.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "88", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.25, "44.00", "88.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.25, "440.00", "550.00", "88.0");
        eva.assertTotalRoleCosts(roleCosts, "110.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.25, "110.00");
        eva.assertParameterCosts(parameter, "594.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "2006.35");
        eva.assertOverallCosts("EUR", "2006.35");
    }

    @Test
    public void billingPerUnitWeekParAndUserAssignChange() throws Exception {
        // given subscription data for scenario
        setup.createWeekScenarioParAndUserAssignChange();

        // when
        performBillingRun(21, "2013-04-25 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_WEEK_ASSIGN", 0);

        // *** billing result for first period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 4.0D, "1", "600.00",
                "628.50");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "4.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 3);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "28.50");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 6);
        eva.assertParametersCosts(priceModel, "1230.90");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.0, "120.00", "148.50", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "28.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertParameterCosts(parameter, "160.50");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 4.0, "4.40", "32.90", null);
        eva.assertTotalRoleCosts(roleCosts, "28.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.5, "10.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.5, "12.00");
        eva.assertOptionCosts(option, "40.90");
        eva.assertParameterCosts(parameter, "40.90");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "300.00", "390.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "90.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "90.00");
        eva.assertParameterCosts(parameter, "420.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "4.00", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "40.00", "54.00", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "14.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "14.00");
        eva.assertParameterCosts(parameter, "58.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                "7", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.25, "17.50", "7.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.25, "175.00", "241.50", "7.0");
        eva.assertTotalRoleCosts(roleCosts, "66.50");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.5, "24.50");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.75, "42.00");
        eva.assertParameterCosts(parameter, "259.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.75, "19.50", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.75, "195.00", "273.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "78.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.75, "78.00");
        eva.assertParameterCosts(parameter, "292.50");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "2224.40");
        eva.assertOverallCosts("EUR", "2224.40");

        // *** billing result for second period ***
        // last week of February ends in March, thus it must be charged there
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "");

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 1.0D, "1", "150.00",
                "158.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertTotalRoleCosts(roleCosts, "8.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "442.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 1.0, "30.00", "38.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertParameterCosts(parameter, "41.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "1", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 1.0, "1.10", "9.10", null);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertOptionCosts(option, "11.10");
        eva.assertParameterCosts(parameter, "11.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "26.00", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "260.00", "364.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "104.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "104.00");
        eva.assertParameterCosts(parameter, "390.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "685.10");
        eva.assertOverallCosts("EUR", "685.10");
    }

    @Test
    public void billingPerUnitMonthParAndUserAssignChange2() throws Exception {
        // given subscription data for scenario
        setup.createMonthScenarioParAndUserAssignChange2();

        // when
        performBillingRun(3, "2013-03-05 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_MONTH_ASSIGN2", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "");
        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", 1.0D, "1", "345.00",
                "352.44");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.8125, "6.50");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.1875, "0.94");
        eva.assertTotalRoleCosts(roleCosts, "7.44");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 5);
        eva.assertParametersCosts(priceModel, "728.23");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "30.00", 1.0, "30.00", "37.44", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "7.44");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.8125, "6.50");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.1875, "0.94");
        eva.assertParameterCosts(parameter, "40.44");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.MONTH.name(),
                "2.00", 1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.MONTH.name(),
                "1.10", 1.0, "1.10", "8.54", null);
        eva.assertTotalRoleCosts(roleCosts, "7.44");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.8125, "6.50");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.1875, "0.94");
        eva.assertOptionCosts(option, "10.54");
        eva.assertParameterCosts(parameter, "10.54");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.375, "11.25", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.375, "112.50", "157.50", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "45.00");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.375, "45.00");
        eva.assertParameterCosts(parameter, "168.75");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                "4", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.375, "3.00", "4.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.4375, "35.00", "49.00", "4.0");
        eva.assertTotalRoleCosts(roleCosts, "14.00");
        eva.assertRoleCost(roleCosts, "USER", "8.00", 0.4375, "14.00");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.0, "0.00");
        eva.assertParameterCosts(parameter, "52.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "88", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "2.00", 0.25, "44.00", "88.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "20.00", 0.1875, "330.00", "412.50", "88.0");
        eva.assertTotalRoleCosts(roleCosts, "82.50");
        eva.assertRoleCost(roleCosts, "GUEST", "5.00", 0.1875, "82.50");
        eva.assertParameterCosts(parameter, "456.50");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "1881.67");
        eva.assertOverallCosts("EUR", "1881.67");
    }

    @Test
    public void billingPerUnitWeekParAndUserAssignChange2() throws Exception {
        // given subscription data for scenario
        setup.createWeekScenarioParAndUserAssignChange2();

        // when
        performBillingRun(2, "2013-04-04 11:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_WEEK_ASSIGN2", 0);

        // *** billing result for first period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 3.0D, "1", "450.00",
                "471.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "3.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 3);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "7.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertTotalRoleCosts(roleCosts, "21.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 7);
        eva.assertParametersCosts(priceModel, "968.30");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 3.0, "90.00", "111.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "21.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "7.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertParameterCosts(parameter, "123.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 3.0, "3.30", "24.30", null);
        eva.assertTotalRoleCosts(roleCosts, "21.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "7.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertOptionCosts(option, "32.30");
        eva.assertParameterCosts(parameter, "32.30");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "300.00", "390.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "90.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "90.00");
        eva.assertParameterCosts(parameter, "420.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "4.00", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "40.00", "54.00", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "14.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "14.00");
        eva.assertParameterCosts(parameter, "58.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                "7", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.25, "17.50", "7.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.0, "0.00", "0.00", "7.0");
        eva.assertParameterCosts(parameter, "17.50");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-23 06:00:00") + "",
                "10", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.5, "10.00", "10.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.75, "150.00", "210.00", "10.0");
        eva.assertTotalRoleCosts(roleCosts, "60.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.75, "60.00");
        eva.assertParameterCosts(parameter, "220.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-23 06:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.25, "6.50", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.25, "65.00", "91.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "26.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 0.25, "26.00");
        eva.assertParameterCosts(parameter, "97.50");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "1804.30");
        eva.assertOverallCosts("EUR", "1804.30");

        // *** billing result for second period ***
        // last week of February ends in March, thus it must be charged there
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "");

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 1.0D, "1", "150.00",
                "158.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "8.00");
        eva.assertTotalRoleCosts(roleCosts, "8.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "442.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 1.0, "30.00", "38.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertParameterCosts(parameter, "41.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "1", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 1.0, "1.10", "9.10", null);
        eva.assertTotalRoleCosts(roleCosts, "8.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1, "8.00");
        eva.assertOptionCosts(option, "11.10");
        eva.assertParameterCosts(parameter, "11.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "13", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "26.00", "13.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "260.00", "364.00", "13.0");
        eva.assertTotalRoleCosts(roleCosts, "104.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 1.0, "104.00");
        eva.assertParameterCosts(parameter, "390.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "685.10");
        eva.assertOverallCosts("EUR", "685.10");
    }

    @Test
    public void billingPerUnitMonthParChangeAndUserDeassign() throws Exception {
        // given subscription data for scenario
        setup.createMonthScenarioParChangeAndUserDeassign();

        // when
        performBillingRun(13, "2013-03-28 13:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_MONTH_DEASSIGN", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "");
        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", 1.0D, "1", "345.00",
                "360.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "15.00", 1.0, "15.00");
        eva.assertTotalRoleCosts(roleCosts, "15.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 4);
        eva.assertParametersCosts(priceModel, "7936.11");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "PERIOD",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "10368000000", "DURATION");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "1.40", 1.0, "168.00", "120.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "3.80", 1.0, "456.00", "2256.00", "120.0");
        eva.assertTotalRoleCosts(roleCosts, "1800.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "15.00", 1.0, "1800.00");
        eva.assertParameterCosts(parameter, "2424.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                "813", "LONG");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "0.80", 0.375, "243.90", "813.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.30", 0.375, "91.46", "4664.59", "813.0");
        eva.assertTotalRoleCosts(roleCosts, "4573.13");
        eva.assertRoleCost(roleCosts, "ADMIN", "15.00", 0.375, "4573.13");
        eva.assertParameterCosts(parameter, "4908.49");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                "29", "LONG");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "0.80", 0.375, "8.70", "29.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.30", 0.625, "5.44", "277.32", "29.0");
        eva.assertTotalRoleCosts(roleCosts, "271.88");
        eva.assertRoleCost(roleCosts, "ADMIN", "15.00", 0.625, "271.88");
        eva.assertParameterCosts(parameter, "286.02");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 12:00:00") + "",
                "1588", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.MONTH.name(),
                "0.80", 0.25, "317.60", "1588.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.MONTH.name(),
                "0.30", 0.0, "0.00", "0.00", "1588.0");
        eva.assertParameterCosts(parameter, "317.60");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "9097.11");
        eva.assertOverallCosts("EUR", "9097.11");
    }

    @Test
    public void billingPerUnitWeekParChangeAndUserDeassign() throws Exception {
        // given subscription data for scenario
        setup.createWeekScenarioParChangeAndUserDeassign();

        // when
        performBillingRun(1, "2013-04-03 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHARGE_PU_WEEK_DEASSIGN", 0);

        // *** billing result for first period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "3.0", "1", "380.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "3.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 3.0, "30.00");
        eva.assertTotalRoleCosts(roleCosts, "30.00");

        // assert stepped prices for user assignments
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "150.00", "0",
                "1", "150.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "150.00", "120.00", "1",
                "2", "120.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "270.00", "110.00", "2",
                "3", "110.00", "1.0");
        eva.assertSteppedPrice(steppedUserAssPrices, "380.00", "100.00", "3",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "380.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 6);
        eva.assertParametersCosts(priceModel, "39307.81");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 3.0, "90.00", "120.00", "1.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "30.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 3.0, "30.00");
        eva.assertParameterCosts(parameter, "132.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel, "PERIOD",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "10368000000", "DURATION");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.40", 4.0, "672.00", "120.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "3.80", 3.0, "1368.00", "4968.00", "120.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "3600.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 3.0, "3600.00");
        eva.assertParameterCosts(parameter, "5640.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                "813", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 1.0,
                "770.40", "813.0");
        Node steppedParPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParPrices, "0.00", "1.20", "0", "100",
                "120.00", "100");
        eva.assertSteppedPrice(steppedParPrices, "120.00", "1.00", "100",
                "500", "400.00", "400");
        eva.assertSteppedPrice(steppedParPrices, "520.00", "0.80", "500",
                "900", "250.40", "313");
        eva.assertSteppedPrice(steppedParPrices, "840.00", "0.50", "900",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedParPrices, "770.40");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.30", 1.0, "243.90", "8373.90", "813.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "8130.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 1.0, "8130.00");
        eva.assertParameterCosts(parameter, "9144.30");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-12 18:00:00") + "",
                "1523", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                1.25, "1439.38", "1523.0");
        steppedParPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParPrices, "0.00", "1.20", "0", "100",
                "120.00", "100");
        eva.assertSteppedPrice(steppedParPrices, "120.00", "1.00", "100",
                "500", "400.00", "400");
        eva.assertSteppedPrice(steppedParPrices, "520.00", "0.80", "500",
                "900", "320.00", "400");
        eva.assertSteppedPrice(steppedParPrices, "840.00", "0.50", "900",
                "null", "311.50", "623");
        eva.assertSteppedPricesAmount(steppedParPrices, "1151.50");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.30", 1.25, "571.13", "19608.63", "1523.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "19037.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 1.25, "19037.50");
        eva.assertParameterCosts(parameter, "21048.01");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-12 18:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-16 06:00:00") + "",
                "400", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 0.5,
                "210.00", "400.0");
        steppedParPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParPrices, "0.00", "1.20", "0", "100",
                "120.00", "100");
        eva.assertSteppedPrice(steppedParPrices, "120.00", "1.00", "100",
                "500", "300.00", "300");
        eva.assertSteppedPrice(steppedParPrices, "520.00", "0.80", "500",
                "900", "0.00", "0");
        eva.assertSteppedPrice(steppedParPrices, "840.00", "0.50", "900",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedParPrices, "420.00");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.30", 0.75, "90.00", "3090.00", "400.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertTotalRoleCosts(roleCosts, "3000.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 0.75, "3000.00");
        eva.assertParameterCosts(parameter, "3300.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-16 06:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "29", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                1.25, "43.50", "29.0");
        steppedParPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParPrices, "0.00", "1.20", "0", "100",
                "34.80", "29");
        eva.assertSteppedPrice(steppedParPrices, "120.00", "1.00", "100",
                "500", "0.00", "0");
        eva.assertSteppedPrice(steppedParPrices, "520.00", "0.80", "500",
                "900", "0.00", "0");
        eva.assertSteppedPrice(steppedParPrices, "840.00", "0.50", "900",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedParPrices, "34.80");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.30", 0.0, "0.00", "0.00", "29.0");
        eva.assertParameterCosts(parameter, "43.50");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "40082.81");
        eva.assertOverallCosts("40082.81", "EUR", "40082.81");

        // *** billing result for second period ***
        // last week of February ends in March, thus it must be charged there
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "");

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.0", "0", "0.00");

        // assert stepped prices for user assignments
        steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "150.00", "0",
                "1", "0.00", "0.0");
        eva.assertSteppedPrice(steppedUserAssPrices, "150.00", "120.00", "1",
                "2", "0.00", "0");
        eva.assertSteppedPrice(steppedUserAssPrices, "270.00", "110.00", "2",
                "3", "0.00", "0");
        eva.assertSteppedPrice(steppedUserAssPrices, "380.00", "100.00", "3",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "0.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "205.80");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "true", "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 0.0, "0.00", "0.00", "1.0");
        eva.assertParameterCosts(parameter, "3.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel, "PERIOD",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "10368000000", "DURATION");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.40", 1.0, "168.00", "120.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "3.80", 0.0, "0.00", "0.00", "120.0");
        eva.assertParameterCosts(parameter, "168.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00") + "",
                "29", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 1.0,
                "34.80", "29.0");
        steppedParPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParPrices, "0.00", "1.20", "0", "100",
                "34.80", "29");
        eva.assertSteppedPrice(steppedParPrices, "120.00", "1.00", "100",
                "500", "0.00", "0");
        eva.assertSteppedPrice(steppedParPrices, "520.00", "0.80", "500",
                "900", "0.00", "0");
        eva.assertSteppedPrice(steppedParPrices, "840.00", "0.50", "900",
                "null", "0.00", "0");
        eva.assertSteppedPricesAmount(steppedParPrices, "34.80");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.30", 0.0, "0.00", "0.00", "29.0");
        eva.assertParameterCosts(parameter, "34.80");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "290.80");
        eva.assertOverallCosts("290.80", "EUR", "290.80");
    }

    @Test
    public void billingPerUnitWeekParChange() throws Exception {
        // given subscription data for scenario
        setup.createWeekScenarioParChange();

        // when
        performBillingRun(0, "2013-04-01 03:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHANGE_PU_WEEK", 0);

        // *** billing result for first period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-28 02:00:00"),
                DateTimeHandling.calculateMillis("2013-01-28 02:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertOverallCosts("EUR", "25.00");

        // *** billing result for second period ***
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-28 02:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");

        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "4.0", "1", "600.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "4.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        // Parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 5);

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-01-28 02:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.0, "120.00", "144.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "24.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertParameterCosts(parameter, "156.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-01-28 02:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "1", "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 4.0, "4.40", "28.40", null);
        eva.assertTotalRoleCosts(roleCosts, "24.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertOptionCosts(option, "36.40");
        eva.assertParameterCosts(parameter, "36.40");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-01-28 02:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.34523809523809523D, "10.36", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.34523809523809523D, "103.57", "134.64", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "31.07");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.34523809523809523D,
                "31.07");
        eva.assertParameterCosts(parameter, "145.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.6547619047619047D, "6.62", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.6547619047619047D, "66.19", "86.05", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "19.86");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.6547619047619047D,
                "19.86");
        eva.assertParameterCosts(parameter, "92.67");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "7", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "28.00", "7.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "280.00", "364.00", "7.0");
        eva.assertTotalRoleCosts(roleCosts, "84.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "84.00");
        eva.assertParameterCosts(parameter, "392.00");

        eva.assertParametersCosts(priceModel, "822.07");

        // total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "1786.07");
        eva.assertOverallCosts("EUR", "1786.07");

        // *** billing result for third period ***
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "");

        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "1.0", "1", "150.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        // Parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.0, "3.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 1.0, "30.00", "36.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertParameterCosts(parameter, "39.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "1", "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                1.0, "2.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 1.0, "1.10", "7.10", null);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertOptionCosts(option, "9.10");
        eva.assertParameterCosts(parameter, "9.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00") + "",
                "7", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "14.00", "7.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "140.00", "182.00", "7.0");
        eva.assertTotalRoleCosts(roleCosts, "42.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "42.00");
        eva.assertParameterCosts(parameter, "196.00");

        eva.assertParametersCosts(priceModel, "244.10");

        // total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "485.10");
        eva.assertOverallCosts("EUR", "485.10");
    }

    @Test
    public void billingPerUnitWeekParChangeUpgrade() throws Exception {
        // given subscription data for scenario
        setup.createWeekScenarioParChangeUpgrade();

        // when
        performBillingRun(0, "2013-04-01 10:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PARCHANGE_UPGRADE_PU_WEEK", 0);
        VOSubscriptionDetails voUpgradedSubDetails = getSubscriptionDetails(
                "PARCHANGE_UPGRADE_PU_WEEK", 1);

        // *** billing result for first period ***
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // --- initial (per unit) price model ---
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-29 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-29 12:00:00") + "");

        eva.assertOneTimeFee(priceModel, "2.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2.00");

        // --- upgraded (pro rata) price model ---
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voUpgradedSubDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-30 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "");

        eva.assertOneTimeFee(priceModel, "2.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "4.00", "0.21428571428571427", "0.86");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "8.00", "0.21428571428571427", "1",
                "1.71");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "0.21428571428571427");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 0.21428571428571427D,
                "2.14");
        eva.assertTotalRoleCosts(roleCosts, "2.14");

        // Parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-01-30 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.21428571428571427D, "0.86", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.21428571428571427D, "8.57", "12.86", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "4.29");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 0.21428571428571427D,
                "4.29");
        eva.assertParameterCosts(parameter, "13.72");

        eva.assertParametersCosts(priceModel, "13.72");
        eva.assertPriceModelCosts(priceModel, "EUR", "20.43");

        // total costs
        eva.assertOverallCosts("EUR", "22.43");

        // *** billing result for second period ***
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        // --- initial (per unit) price model ---
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-29 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-30 12:00:00") + "");

        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "5.00", "1.0", "5.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "10.00", "1.0", "1", "10.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "1.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "3.00");
        eva.assertTotalRoleCosts(roleCosts, "3.00");

        // Parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 2);

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-01-29 12:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-30 00:00:00") + "",
                "15", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.2857142857142857D, "4.29", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "10.00", 0.2857142857142857D, "42.86", "55.72", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "12.86");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 0.2857142857142857D,
                "12.86");
        eva.assertParameterCosts(parameter, "60.01");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-01-30 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-30 12:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "1.00", 0.7142857142857143D, "1.43", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "10.00", 0.7142857142857143D, "14.29", "18.58", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "4.29");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 0.7142857142857143D,
                "4.29");
        eva.assertParameterCosts(parameter, "20.01");

        eva.assertParametersCosts(priceModel, "80.02");

        eva.assertPriceModelCosts(priceModel, "EUR", "98.02");

        // --- upgraded (pro rata) price model ---
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voUpgradedSubDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 08:00:00") + "");

        eva.assertNullOneTimeFee(priceModel);
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "4.00", "3.4761904761904763", "13.90");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "8.00", "3.4761904761904763", "1",
                "27.81");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "3.4761904761904763");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 3.4761904761904763D,
                "34.76");
        eva.assertTotalRoleCosts(roleCosts, "34.76");

        // Parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-02-25 08:00:00") + "",
                "2", "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.4761904761904763D, "13.90", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.4761904761904763D, "139.05", "208.57", "2.0");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 3.4761904761904763D,
                "69.52");
        eva.assertTotalRoleCosts(roleCosts, "69.52");
        eva.assertParameterCosts(parameter, "222.47");
        eva.assertParametersCosts(priceModel, "222.47");

        eva.assertPriceModelCosts(priceModel, "EUR", "298.94");

        // total costs
        eva.assertOverallCosts("EUR", "396.96");

    }

    @Test
    public void billingRataWeekParUserChange() throws Exception {
        // given subscription data for scenario
        setup.createRataWeekScenarioParUserChange();

        // when
        performBillingRun(0, "2013-05-01 10:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "RATA_WEEK_PAR_USER_CHANGE", 0);

        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00") + "");

        eva.assertOneTimeFee(priceModel, "2.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "4.00", "4.0", "16.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "8.00", 6.0, "2", "48.00", "92.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER1_ID, "3.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                TestBasicSetup.CUSTOMER_USER2_ID, "3.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 2);
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 2.0, "20.00");
        eva.assertRoleCost(roleCosts, "GUEST", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "44.00");

        // Parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"), "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "30.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "300.00", "450.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "150.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 1.0, "150.00");
        eva.assertParameterCosts(parameter, "480.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-18 12:00:00"), "2",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.5, "6.00", "2.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.5, "100.00", "138.00", "2.0");
        eva.assertTotalRoleCosts(roleCosts, "38.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "10.00", 1.0, "20.00");
        eva.assertRoleCost(roleCosts, "GUEST", "6.00", 1.5, "18.00");
        eva.assertParameterCosts(parameter, "144.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-18 12:00:00"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"), "3",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.5, "9.00", "3.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.5, "150.00", "195.00", "3.0");
        eva.assertTotalRoleCosts(roleCosts, "45.00");
        eva.assertRoleCost(roleCosts, "GUEST", "6.00", 2.5, "45.00");
        eva.assertParameterCosts(parameter, "204.00");

        eva.assertParametersCosts(priceModel, "828.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "938.00");

        // total costs
        eva.assertOverallCosts("EUR", "938.00");

    }

}
