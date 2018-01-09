/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 7, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.AsyncSetup2;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author farmaki
 * 
 */
public class AsyncIT2 extends BillingIntegrationTestBase {

    private AsyncSetup2 testSetup = new AsyncSetup2();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void testCase28() throws Exception {
        // given
        testSetup.createAsyncScenario28();

        // when
        performBillingRun(0, "2013-10-10 00:00:00");

        // then
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SERVICE_SCENARIO_28_subscr", 3);

        BillingResultEvaluator eva = getEvaluator(upgradedSub.getKey(),
                "2013-09-01 00:00:00", "2013-10-01 00:00:00");

        // only one subscription, since upgrade is successful.
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 2);

        // Check price model of subscription after upgrade
        Node upgradedPriceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", upgradedPriceModel);

        eva.assertPriceModelUsagePeriod(upgradedPriceModel,
                DateTimeHandling.calculateMillis("2013-09-14 12:00:00"),
                DateTimeHandling.calculateMillis("2013-09-30 00:00:00"));
        eva.assertOneTimeFee(upgradedPriceModel, "254.00");

        eva.assertPriceModelPeriodFee(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "734.00", "3.0", "2202.00");
        eva.assertPriceModelUserAssignmentCosts(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "434.00", 3.0D, "1", "1302.00",
                "1320.00");

        Node roleCosts = BillingXMLNodeSearch
                .getRoleCostsNode(upgradedPriceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0D, "18.00");
        eva.assertTotalRoleCosts(roleCosts, "18.00");

        eva.assertPriceModelCosts(upgradedPriceModel, "EUR", "3776.00");

        // Check price model of subscription before upgrade
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_28_subscr", 1);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-09-02 12:00:00"),
                DateTimeHandling.calculateMillis("2013-09-14 12:00:00"));
        eva.assertOneTimeFee(priceModel, "20.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "12.0", "1440.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", 12.0D, "1", "720.00",
                "792.00");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 12.0D, "72.00");
        eva.assertTotalRoleCosts(roleCosts, "72.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2252.00");

        // Check overall subscription costs
        eva.assertOverallCosts("6028.00", "EUR", "6028.00");

    }

    @Test
    public void testCase41() throws Exception {
        // given
        testSetup.createAsyncScenario41();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SERVICE_SCENARIO_41_subscr", 3);

        BillingResultEvaluator eva = getEvaluator(upgradedSub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // only one subscription, since upgrade is successful.
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 2);

        // Check price model of subscription after upgrade
        Node upgradedPriceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", upgradedPriceModel);

        eva.assertPriceModelUsagePeriod(upgradedPriceModel,
                DateTimeHandling.calculateMillis("2013-08-15 12:00:00"),
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"));
        eva.assertOneTimeFee(upgradedPriceModel, "25.00");

        eva.assertPriceModelPeriodFee(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "150.00", 2.0D, "1", "300.00",
                "300.00");
        eva.assertPriceModelCosts(upgradedPriceModel, "EUR", "495.00");

        // Check price model of subscription before upgrade
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_41_subscr", 1);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-06 13:00:00"),
                DateTimeHandling.calculateMillis("2013-08-10 12:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "0.5654761904761905", "48.07");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 0.5654761904761905D, "1",
                "84.82", "88.21");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.5654761904761905D,
                "3.39");
        eva.assertTotalRoleCosts(roleCosts, "3.39");
        eva.assertPriceModelCosts(priceModel, "EUR", "161.28");

        // Check overall subscription costs
        eva.assertOverallCosts("656.28", "EUR", "656.28");
    }

    @Test
    public void testCase41_1() throws Exception {
        // given
        testSetup.createAsyncScenario41_1();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SERVICE_SCENARIO_41_1_subscr", 3);

        BillingResultEvaluator eva = getEvaluator(upgradedSub.getKey(),
                "2013-08-02 00:00:00", "2013-09-02 00:00:00");

        // only one subscription, since upgrade is successful.
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 2);

        // Check price model of subscription after upgrade
        Node upgradedPriceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", upgradedPriceModel);

        eva.assertPriceModelUsagePeriod(upgradedPriceModel,
                DateTimeHandling.calculateMillis("2013-08-15 12:00:00"),
                DateTimeHandling.calculateMillis("2013-09-02 00:00:00"));
        eva.assertOneTimeFee(upgradedPriceModel, "25.00");

        eva.assertPriceModelPeriodFee(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "85.00", "3.0", "255.00");
        eva.assertPriceModelUserAssignmentCosts(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "150.00", 3.0D, "1", "450.00",
                "450.00");
        eva.assertPriceModelCosts(upgradedPriceModel, "EUR", "730.00");

        // Check price model of subscription before upgrade
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_41_1_subscr", 1);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-06 13:00:00"),
                DateTimeHandling.calculateMillis("2013-08-10 12:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "0.5654761904761905", "48.07");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 0.5654761904761905D, "1",
                "84.82", "88.21");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.5654761904761905D,
                "3.39");
        eva.assertTotalRoleCosts(roleCosts, "3.39");
        eva.assertPriceModelCosts(priceModel, "EUR", "161.28");

        // Check overall subscription costs
        eva.assertOverallCosts("891.28", "EUR", "891.28");
    }

    @Test
    public void testCase42() throws Exception {
        // given
        testSetup.createAsyncScenario42();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_42_subscr", 2);

        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // only one subscription, since upgrade fails.
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-06 13:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "3.636904761904762", "309.14");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 3.636904761904762D, "1",
                "545.54", "567.36");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.636904761904762D,
                "21.82");
        eva.assertTotalRoleCosts(roleCosts, "21.82");
        eva.assertPriceModelCosts(priceModel, "EUR", "901.50");

        // Check overall subscription costs
        eva.assertOverallCosts("901.50", "EUR", "901.50");

    }

    @Test
    public void testCase43() throws Exception {
        // given
        testSetup.createAsyncScenario43();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SERVICE_SCENARIO_43_subscr", 3);

        BillingResultEvaluator eva = getEvaluator(upgradedSub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Two subscription nodes, since first subscription was suspended.
        eva.assertNodeCount("//Subscriptions/Subscription", 2);
        eva.assertNodeCount("//PriceModels/PriceModel", 2);

        // Check price model of subscription after upgrade
        Node upgradedPriceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", upgradedPriceModel);

        eva.assertPriceModelUsagePeriod(upgradedPriceModel,
                DateTimeHandling.calculateMillis("2013-08-15 12:00:00"),
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"));
        eva.assertOneTimeFee(upgradedPriceModel, "25.00");

        eva.assertPriceModelPeriodFee(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "150.00", 2.0D, "1", "300.00",
                "300.00");
        eva.assertPriceModelCosts(upgradedPriceModel, "EUR", "495.00");

        // Check price model of subscription before upgrade
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_43_subscr", 1);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-06 13:00:00"),
                DateTimeHandling.calculateMillis("2013-08-08 12:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "0.27976190476190477", "23.78");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 0.27976190476190477D, "1",
                "41.96", "43.64");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.27976190476190477D,
                "1.68");
        eva.assertTotalRoleCosts(roleCosts, "1.68");
        eva.assertPriceModelCosts(priceModel, "EUR", "92.42");

        // Check overall subscription costs
        eva.assertOverallCosts("587.42", "EUR", "587.42");

    }

    @Test
    public void testCase43_1() throws Exception {
        // given
        testSetup.createAsyncScenario43_1();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SERVICE_SCENARIO_43_1_subscr", 3);

        BillingResultEvaluator eva = getEvaluator(upgradedSub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // only one subscription, since upgrade is successful and only one price
        // model, since subscription is suspended inside the free period.
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        // Check price model of subscription after upgrade
        Node upgradedPriceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", upgradedPriceModel);

        eva.assertPriceModelUsagePeriod(upgradedPriceModel,
                DateTimeHandling.calculateMillis("2013-08-15 12:00:00"),
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"));
        eva.assertOneTimeFee(upgradedPriceModel, "25.00");

        eva.assertPriceModelPeriodFee(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(upgradedPriceModel,
                PricingPeriod.WEEK.name(), "150.00", 2.0D, "1", "300.00",
                "300.00");
        eva.assertPriceModelCosts(upgradedPriceModel, "EUR", "495.00");

        // Check price model of subscription before upgrade
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_43_1_subscr", 1);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNull(priceModel);

        // Check overall subscription costs
        eva.assertOverallCosts("495.00", "EUR", "495.00");

    }

    @Test
    public void testCase44() throws Exception {
        // given
        testSetup.createAsyncScenario44();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_44_subscr", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-17 12:00:00"),
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "2.0", "1468.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 2.0D, "1", "868.00",
                "880.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0D, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2602.00");
        eva.assertOverallCosts("2602.00", "EUR", "2602.00");
    }

    @Test
    public void testCase45() throws Exception {
        // given
        testSetup.createAsyncScenario45();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_45_subscr", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged 5 days(free period duration) after the
        // async subscription is completed.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 2.0D, "1", "300.00",
                "312.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0D, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "507.00");
        eva.assertOverallCosts("507.00", "EUR", "507.00");
    }

    @Test
    public void testCase45_1() throws Exception {
        // given
        testSetup.createAsyncScenario45_1();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SERVICE_SCENARIO_45_1_subscr", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the resume date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-20 12:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva.assertOneTimeFee(priceModel, "25.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.6428571428571428", "139.64");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 1.6428571428571428D, "1",
                "246.43", "256.29");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.6428571428571428D,
                "9.86");
        eva.assertTotalRoleCosts(roleCosts, "9.86");
        eva.assertPriceModelCosts(priceModel, "EUR", "420.93");
        eva.assertOverallCosts("420.93", "EUR", "420.93");
    }

}
