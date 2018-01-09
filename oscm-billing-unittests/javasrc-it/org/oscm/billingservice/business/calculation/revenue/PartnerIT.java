/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 21, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.PartnerSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author farmaki
 * 
 */
public class PartnerIT extends BillingIntegrationTestBase {

    private final PartnerSetup testSetup = new PartnerSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void subscribeToSyncResellerService() throws Exception {
        // given
        testSetup.subscribeToSyncResellerService();
        TestData testData = getTestData("subscribeToSyncResellerService");
        VendorData reseller = testData.getVendor(1);
        CustomerData resellerCustomer = reseller.getCustomer(0);
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "resellerSubscription", 0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        eva.assertOrganizationDetails(resellerCustomer.getOrganizationId(),
                resellerCustomer.getOrganization().getName(), resellerCustomer
                        .getOrganization().getAddress(), resellerCustomer
                        .getOrganization().getEmail(), sub.getPaymentInfo()
                        .getId());

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), sub.getServiceId(), sub.getPriceModel()
                .getKey(), sub.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after
        // the subscription date.
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
    public void subscribeToSyncResellerService_changeServiceIdAfterBP()
            throws Exception {
        // given
        testSetup.subscribeToSyncResellerService_changeServiceIdAfterBP();
        TestData testData = getTestData("subscribeToSyncResellerService_changeServiceIdAfterBP");
        VendorData reseller = testData.getVendor(1);
        CustomerData resellerCustomer = reseller.getCustomer(0);
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "resellerSubscription2", 0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        eva.assertOrganizationDetails(resellerCustomer.getOrganizationId(),
                resellerCustomer.getOrganization().getName(), resellerCustomer
                        .getOrganization().getAddress(), resellerCustomer
                        .getOrganization().getEmail(), sub.getPaymentInfo()
                        .getId());

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), sub.getServiceId(), sub.getPriceModel()
                .getKey(), sub.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after
        // the subscription date.
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
    public void subscribeToSyncBrokerService() throws Exception {
        // given
        testSetup.subscribeToSyncBrokerService();
        TestData testData = getTestData("subscribeToSyncBrokerService");
        VendorData broker = testData.getVendor(1);
        CustomerData brokerCustomer = broker.getCustomer(0);
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "brokerSubscription", 0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        eva.assertOrganizationDetails(brokerCustomer.getOrganizationId(),
                brokerCustomer.getOrganization().getName(), brokerCustomer
                        .getOrganization().getAddress(), brokerCustomer
                        .getOrganization().getEmail(), sub.getPaymentInfo()
                        .getId());

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), sub.getServiceId(), sub.getPriceModel()
                .getKey(), sub.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after
        // the subscription date.
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
    public void subscribeToSyncBrokerService_changeServiceIdInBP()
            throws Exception {
        // given
        testSetup.subscribeToSyncBrokerService_changeServiceIdInBP();
        TestData testData = getTestData("subscribeToSyncBrokerService_changeServiceIdInBP");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails serviceWithChangedId = supplier.getService(1);
        VendorData broker = testData.getVendor(1);
        CustomerData brokerCustomer = broker.getCustomer(0);
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "brokerSubscription2", 0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");

        eva.assertOrganizationDetails(brokerCustomer.getOrganizationId(),
                brokerCustomer.getOrganization().getName(), brokerCustomer
                        .getOrganization().getAddress(), brokerCustomer
                        .getOrganization().getEmail(), sub.getPaymentInfo()
                        .getId());

        // Only one subscription node
        eva.assertNodeCount("//Subscriptions/Subscription", 1);
        eva.assertNodeCount("//PriceModels/PriceModel", 1);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), serviceWithChangedId.getServiceId(),
                sub.getPriceModel().getKey(), sub.getPriceModel().getType()
                        .name());
        assertNotNull("Price Model doesn't exist", priceModel);

        // the costs are charged after
        // the subscription date.
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

}
