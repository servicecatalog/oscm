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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.PeriodHourSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author malhotra
 * 
 */
public class PeriodHourIT extends BillingIntegrationTestBase {

    private PeriodHourSetup setup = new PeriodHourSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario01()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario01();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO01_PERUNIT_HOUR", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "60.00", "744.0", "44640.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "30.00", "744.0", "1", "22320.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "744.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "71424.00");
        eva.assertOverallCosts("71424.00", "EUR", "71424.00");
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario02()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario02();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO02_PERUNIT_HOUR", 0);
        assertNull(getEvaluator(sub.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario03()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario03();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO03_PERUNIT_HOUR", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "60.00", "151.0", "9060.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "30.00", "151.0", "1", "4530.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "151.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "14496.00");
        eva.assertOverallCosts("14496.00", "EUR", "14496.00");
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario04()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario04();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO04_PERUNIT_HOUR", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "60.00", "704.0", "42240.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "30.00", "704.0", "1", "21120.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "704.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "67594.00");
        eva.assertOverallCosts("67594.00", "EUR", "67594.00");
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario05()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario05();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO05_PERUNIT_HOUR", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "60.00", "26.0", "1560.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "30.00", "26.0", "1", "780.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "26.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "2506.00");
        eva.assertOverallCosts("2506.00", "EUR", "2506.00");
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario06()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario06();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO06_PERUNIT_HOUR", 0);
        assertNull(getEvaluator(sub.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario07()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario07();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO07_PERUNIT_HOUR", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "60.00", "659.0", "39540.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "30.00", "659.0", "1", "19770.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "659.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "63264.00");
        eva.assertTotalRoleCosts(
                BillingXMLNodeSearch.getRoleCostsNode(priceModel), "3954.00");
        eva.assertOverallCosts("63264.00", "EUR", "63264.00");
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario08()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario08();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO08_PERUNIT_HOUR", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOverallCosts("39540.00", "EUR", "39540.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "60.00", "659.0", "39540.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "30.00", "0.0", "0", "0.00");
    }

    @Test
    public void billingPerUnitHourSubscriptionUsageScenario09()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario09();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO09_PERUNIT_HOUR", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.HOUR.name(),
                "60.00", "744.0", "44640.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.HOUR.name(), "30.00", "744.0", "1", "22320.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "744.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 120.7, "724.20");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 623.3, "4363.10");
        eva.assertTotalRoleCosts(roleCosts, "5087.30");

        eva.assertPriceModelCosts(priceModel, "EUR", "72047.30");
        eva.assertOverallCosts("72047.30", "EUR", "72047.30");
    }

}
