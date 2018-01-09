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

import org.oscm.billingservice.business.calculation.revenue.setup.AsyncSetup3;
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

public class AsyncIT3 extends BillingIntegrationTestBase {

    private final AsyncSetup3 testSetup = new AsyncSetup3();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void testcase31() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario31();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_31", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_31", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period

        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_31", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-05-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

    }

    @Test
    public void testcase31_V2() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario31_V2();

        // when
        performBillingRun(0, "2013-04-30 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_31_V2", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-15 00:00:00", "2013-04-15 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_31_V2", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-10 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-10 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "434.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");

        eva.assertOverallCosts("1174.00", "EUR", "1174.00");

        // next billing period

        eva = getEvaluator(sub.getKey(), "2013-02-15 00:00:00",
                "2013-03-15 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_31_V2", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-10 10:20:00"), DateTimeHandling
                .calculateMillis("2013-03-10 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-10 10:20:00"),
                DateTimeHandling.calculateMillis("2013-03-10 10:20:00"));

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("254.00", "EUR", "254.00");

    }

    @Test
    public void testcase30() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario30();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_30", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_30", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-15 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-15 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "3.0", "2202.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "3.0", "1", "1302.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.00, "18.00");
        eva.assertTotalRoleCosts(roleCosts, "18.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 3.00);
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "3776.00");

        eva.assertOverallCosts("3776.00", "EUR", "3776.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_30", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_30", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-10 10:20:00"), DateTimeHandling
                .calculateMillis("2013-05-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-10 10:20:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "2.9384920634920633", "2156.85");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.9384920634920633", "1", "1275.31");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.9384920634920633,
                "17.63");
        eva.assertTotalRoleCosts(roleCosts, "17.63");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 2.9384920634920633);

        eva.assertPriceModelCosts(priceModel, "EUR", "3703.79");

        // next pricemodel

        sub = getSubscriptionDetails("SCENARIO_30", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_30", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-10 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "2.0", "1468.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "1", "868.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "2348.00");

        eva.assertOverallCosts("6051.79", "EUR", "6051.79");

    }

    @Test
    public void testcase29() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario29();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_29", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_29", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-15 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-31 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-15 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-31 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "2.2857142857142856", "1677.71");

        eva.assertPriceModelCosts(priceModel, "EUR", "2937.42");

        // next price model
        sub = getSubscriptionDetails("SCENARIO_29", 1);
        eva = getEvaluator(sub.getKey(), "2014-03-01 00:00:00",
                "2014-04-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_29", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-31 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-31 10:20:00"));

        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("3191.42", "EUR", "3191.42");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_29", 1);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_29", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:20:00"), DateTimeHandling
                .calculateMillis("2014-04-28 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:20:00"),
                DateTimeHandling.calculateMillis("2014-04-28 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "4.0", "1", "1736.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4696.00");

        eva.assertOverallCosts("4696.00", "EUR", "4696.00");

    }

    @Test
    public void testcase32() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario32();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_32", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_32", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period

        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_32", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-02 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-02 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

    }

    @Test
    public void testcase33() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario33();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_33", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_33", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_33", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_33", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.5", "367.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.5", "1", "217.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.5);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.00");

        // next subscription

        sub = getSubscriptionDetails("SCENARIO_33", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_33", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-01 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1613.00", "EUR", "1613.00");

    }

    @Test
    public void testcase34() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");
        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

    }

    @Test
    public void testcase34_1() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_1();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_1", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_1", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "434.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");

        eva.assertOverallCosts("1428.00", "EUR", "1428.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_1", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_1", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "434.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");

        // next price model
        sub = getSubscriptionDetails("SCENARIO_34_1", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_1", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-20 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-20 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.3333333333333333", "244.67");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.3333333333333333", "1", "144.67");
        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "399.34");

        eva.assertOverallCosts("1573.34", "EUR", "1573.34");

    }

    @Test
    public void testcase34_2() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_2();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_2", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_2", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-31 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-31 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");

        eva.assertOverallCosts("1428.00", "EUR", "1428.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_2", 0);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_2", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 00:00:00"), DateTimeHandling
                .calculateMillis("2014-04-15 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 00:00:00"),
                DateTimeHandling.calculateMillis("2014-04-15 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "3.0", "2202.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "3.0", "1", "1302.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 3.0);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "18.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "3522.00");

        // next price model
        sub = getSubscriptionDetails("SCENARIO_34_2", 1);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_2", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-04-20 10:20:00"), DateTimeHandling
                .calculateMillis("2014-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-04-20 10:20:00"),
                DateTimeHandling.calculateMillis("2014-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.4285714285714284", "1048.57");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.4285714285714284", "1", "620.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "8.57");
        eva.assertPriceModelCosts(priceModel, "EUR", "1931.14");

        eva.assertOverallCosts("5453.14", "EUR", "5453.14");

    }

    @Test
    public void testcase34_8() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_8();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_8", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_8", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-29 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-31 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-29 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-31 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");

        eva.assertOverallCosts("1428.00", "EUR", "1428.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_8", 0);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_8", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 00:00:00"), DateTimeHandling
                .calculateMillis("2014-04-28 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 00:00:00"),
                DateTimeHandling.calculateMillis("2014-04-28 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "4.0", "1", "1736.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 4.0);

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4696.00");

    }

    @Test
    public void testcase34_9() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_9();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_9", 0);

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_9", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-04-01 00:00:00", "2014-05-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_9", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-04-01 10:20:00"), DateTimeHandling
                .calculateMillis("2014-04-28 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-04-01 10:20:00"),
                DateTimeHandling.calculateMillis("2014-04-28 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "4.0", "1", "1736.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 4.0);
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4950.00");

    }

    @Test
    public void testcase34_7() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_7();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_7", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_7", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-31 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-31 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");

        eva.assertOverallCosts("1428.00", "EUR", "1428.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_7", 0);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_7", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 00:00:00"), DateTimeHandling
                .calculateMillis("2014-04-15 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 00:00:00"),
                DateTimeHandling.calculateMillis("2014-04-15 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "3.0", "2202.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "3.0", "1", "1302.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 3.0);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "18.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "3522.00");

        // next price model
        sub = getSubscriptionDetails("SCENARIO_34_7", 1);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_7", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-04-25 10:24:00"), DateTimeHandling
                .calculateMillis("2014-04-28 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-04-25 10:24:00"),
                DateTimeHandling.calculateMillis("2014-04-28 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");

        eva.assertOverallCosts("4950.00", "EUR", "4950.00");

    }

    @Test
    public void testcase34_3() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_3();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_3", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_3", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-30 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-30 10:22:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.2816367265469062", "206.72");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.2816367265469062", "1", "122.23");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.69");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.2816367265469062);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.64");

        // next price model

        sub = getSubscriptionDetails("SCENARIO_34_3", 1);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_3", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.0", "0.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("838.64", "EUR", "838.64");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_3", 1);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_3", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-04-04 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-04-04 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");

        eva.assertOverallCosts("1174.00", "EUR", "1174.00");

    }

    @Test
    public void testcase34_4() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_4();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_4", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_4", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-30 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-30 10:22:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.2816367265469062", "206.72");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.2816367265469062", "1", "122.23");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.69");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.2816367265469062);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.64");

        // next price model

        sub = getSubscriptionDetails("SCENARIO_34_4", 1);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_4", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.0", "0.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("838.64", "EUR", "838.64");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_4", 1);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_4", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-04-04 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-04-04 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");

        eva.assertOverallCosts("1174.00", "EUR", "1174.00");

    }

    @Test
    public void testcase34_5() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_5();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_5", 0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SCENARIO_34_5", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_5", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-30 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-30 10:22:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.2816367265469062", "206.72");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.2816367265469062", "1", "122.23");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.69");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.2816367265469062);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.64");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_5", upgradedSub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"));

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("838.64", "EUR", "838.64");

        // next billing period
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_5", upgradedSub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-04-28 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-04-28 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 4.0);

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4696.00");

        eva.assertOverallCosts("4696.00", "EUR", "4696.00");

    }

    @Test
    public void testcase34_11() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_11();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_11", 0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SCENARIO_34_11", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_11", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-30 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-30 10:22:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.2816367265469062", "206.72");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.2816367265469062", "1", "122.23");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.69");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.2816367265469062);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.64");

        // next price model

        eva.assertOverallCosts("584.64", "EUR", "584.64");

        // next billing period
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_11", upgradedSub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2014-04-15 10:22:00"), DateTimeHandling
                .calculateMillis("2014-04-28 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-04-15 10:22:00"),
                DateTimeHandling.calculateMillis("2014-04-28 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "2.0", "1468.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "2.0", "1", "868.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 2.0);

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2602.00");

        eva.assertOverallCosts("2602.00", "EUR", "2602.00");

    }

    @Test
    public void testcase34_10() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_10();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_10", 0);
        VOSubscriptionDetails upgradedSub = getSubscriptionDetails(
                "SCENARIO_34_10", 1);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");

        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_10", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-30 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-30 10:22:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.2816367265469062", "206.72");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.2816367265469062", "1", "122.23");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.69");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.2816367265469062);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.64");

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_10", upgradedSub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"));

        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("838.64", "EUR", "838.64");

        // next billing period
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_10", upgradedSub
                .getPriceModel().getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-04-28 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                upgradedSub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-04-28 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 4.0);

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4696.00");

        eva.assertOverallCosts("4696.00", "EUR", "4696.00");

    }

    @Test
    public void testcase34_6() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario34_6();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_34_6", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_6", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-30 10:21:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-30 10:21:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_34_6", 1);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_34_6", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:22:00"), DateTimeHandling
                .calculateMillis("2014-04-04 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:22:00"),
                DateTimeHandling.calculateMillis("2014-04-04 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");

        eva.assertOverallCosts("1174.00", "EUR", "1174.00");

    }

    @Test
    public void testcase35() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario35();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_35", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_35", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-03-30 20:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-03-30 20:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_35", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_35", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.5", "367.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.5", "1", "217.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.5);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.00");

        // next subscription

        sub = getSubscriptionDetails("SCENARIO_35", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_35", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");
        eva.assertOverallCosts("1613.00", "EUR", "1613.00");

    }

    @Test
    public void testcase36() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario36();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_36", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_36", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-03-30 20:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-03-30 20:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_36", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_36", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.5", "367.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.5", "1", "217.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.5);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.00");

        eva.assertOverallCosts("584.00", "EUR", "584.00");

    }

    @Test
    public void testcase36a() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario36a();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_36a", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_36a", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-03-30 20:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-03-30 20:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_36a", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_36a", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.5", "367.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.5", "1", "217.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.5);

        eva.assertPriceModelCosts(priceModel, "EUR", "584.00");

        eva.assertOverallCosts("584.00", "EUR", "584.00");

    }

    @Test
    public void testcase37() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario37();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_37", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_37", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_37", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_37", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

    }

    @Test
    public void testcase38() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario38();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_38", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_38", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-03-30 09:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-03-30 09:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.063257065948856", "46.43");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.063257065948856", "1", "27.45");

        eva.assertPriceModelCosts(priceModel, "EUR", "73.88");

        eva.assertOverallCosts("73.88", "EUR", "73.88");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_38", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_38", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

    }

    @Test
    public void testcase39() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario39();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_39", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_39", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-03-30 09:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-03-30 09:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.00, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.00);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1029.00", "EUR", "1029.00");

    }

    @Test
    public void testcase40() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario40();

        // when
        performBillingRun(0, "2013-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_40", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-03-01 00:00:00", "2013-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_40", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2013-03-30 09:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2013-03-30 09:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.063257065948856", "46.43");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.063257065948856", "1", "27.45");

        eva.assertPriceModelCosts(priceModel, "EUR", "73.88");

        eva.assertOverallCosts("73.88", "EUR", "73.88");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_40", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_40", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-10 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-10 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "0.16666666666666666", "122.33");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "0.16666666666666666", "1", "72.33");

        eva.assertPriceModelCosts(priceModel, "EUR", "194.66");

        // next price model

        sub = getSubscriptionDetails("SCENARIO_40", 1);
        eva = getEvaluator(sub.getKey(), "2013-04-01 00:00:00",
                "2013-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_40", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-04-15 10:20:00"), DateTimeHandling
                .calculateMillis("2013-04-30 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-15 10:20:00"),
                DateTimeHandling.calculateMillis("2013-04-30 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "1.0", "1", "345.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        eva.assertOverallCosts("1223.66", "EUR", "1223.66");

    }

    @Test
    public void testcase40_1() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario40_1();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_40_1", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");
        Node subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_40_1", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-28 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-30 09:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription,
                sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-28 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-30 09:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.2754491017964072", "202.18");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "0.2754491017964072", "1", "119.54");
        eva.assertOneTimeFee(priceModel, "254.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);

        eva.assertTotalRoleCosts(roleCosts, "1.65");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.2754491017964072);

        eva.assertPriceModelCosts(priceModel, "EUR", "577.37");

        // next price model

        sub = getSubscriptionDetails("SCENARIO_40_1", 1);

        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_40_1", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:20:00"), DateTimeHandling
                .calculateMillis("2014-03-31 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:20:00"),
                DateTimeHandling.calculateMillis("2014-03-31 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "0.0", "0.00");
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "254.00");

        eva.assertOverallCosts("831.37", "EUR", "831.37");

        // next billing period
        sub = getSubscriptionDetails("SCENARIO_40_1", 1);
        eva = getEvaluator(sub.getKey(), "2014-04-01 00:00:00",
                "2014-05-01 00:00:00");
        subscription = BillingXMLNodeSearch.getSubscriptionNode(eva
                .getBillingResult(), "SCENARIO_40_1", sub.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2014-03-31 10:20:00"), DateTimeHandling
                .calculateMillis("2014-04-04 10:20:00"));
        assertNotNull("Subscription doesn't exist", subscription);

        priceModel = BillingXMLNodeSearch.getPriceModelNode(subscription, sub
                .getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2014-03-31 10:20:00"),
                DateTimeHandling.calculateMillis("2014-04-04 10:20:00"));

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "1.0", "1", "434.00");

        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 1.0);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");

        eva.assertOverallCosts("1174.00", "EUR", "1174.00");

    }

    @Test
    public void testcase40_2V() throws Exception {
        // given subscription data for scenario
        testSetup.createAsyncScenario40_2V();

        // when
        performBillingRun(0, "2014-05-02 10:20:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("SCENARIO_40_2V", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2014-03-01 00:00:00", "2014-04-01 00:00:00");

        assertNull("no result.xml should exist", eva);

    }
}
