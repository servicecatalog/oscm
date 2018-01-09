/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 19, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.PartnerAsyncSetup;
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
public class PartnerAsyncIT extends BillingIntegrationTestBase {

    private final PartnerAsyncSetup testSetup = new PartnerAsyncSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void subscribeToAsyncResellerService() throws Exception {
        // given
        testSetup.subscribeToAsyncResellerService();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("resellerSubscr", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
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
    public void subscribeToAsyncBrokerService() throws Exception {
        // given
        testSetup.subscribeToAsyncBrokerService();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("brokerSubscr", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
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
    public void testcase1() throws Exception {
        // given
        testSetup.createBrokerScenario1();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("Scenario1", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-18 10:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00",
                "440.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0D, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
    }

    @Test
    public void testcase2() throws Exception {
        // given
        testSetup.createBrokerScenario2();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("Scenario2", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"));
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
    public void testcase3() throws Exception {
        // given
        testSetup.createBrokerScenario3();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("Scenario3", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-19 20:00:00"),
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00",
                "440.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0D, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
    }

    @Test
    public void testcase4() throws Exception {
        // given
        testSetup.createBrokerScenario4();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("Scenario4", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-19 20:00:00"),
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00",
                "440.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0D, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
    }

    @Test
    public void testcaseR_1() throws Exception {
        // given
        testSetup.createResellerScenario1();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("ResellerScenario1",
                0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-18 10:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00",
                "440.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0D, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
    }

    @Test
    public void testcaseR_2() throws Exception {
        // given
        testSetup.createResellerScenario2();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("ResellerScenario2",
                0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"));
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
    public void testcaseR_3() throws Exception {
        // given
        testSetup.createResellerScenario3();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("ResellerScenario3",
                0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-19 20:00:00"),
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00",
                "440.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0D, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
    }

    @Test
    public void testcaseR_4() throws Exception {
        // given
        testSetup.createResellerScenario4();

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("ResellerScenario4",
                0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after the
        // async subscription completion date.
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-08-19 20:00:00"),
                DateTimeHandling.calculateMillis("2013-08-20 10:00:00"));
        eva.assertOneTimeFee(priceModel, "254.00");

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", 1.0D, "1", "434.00",
                "440.00");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0D, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
    }

}
