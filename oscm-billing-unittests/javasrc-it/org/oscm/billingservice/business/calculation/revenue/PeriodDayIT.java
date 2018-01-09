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

import org.oscm.billingservice.business.calculation.revenue.setup.PeriodDaySetup;
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
public class PeriodDayIT extends BillingIntegrationTestBase {

    private PeriodDaySetup setup = new PeriodDaySetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario01() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario01();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO01_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "31.0", "3720.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "31.0", "1", "1860.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "31.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "5766.00");
        eva.assertOverallCosts("5766.00", "EUR", "5766.00");
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario01_AsyncCreate()
            throws Exception {
        // given subscription data for scenario
        setup.createAsyncSubUsageScenario01();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO01_ASYNC_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "31.0", "3720.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "31.0", "1", "1860.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "31.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "5766.00");
        eva.assertOverallCosts("5766.00", "EUR", "5766.00");
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario02() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario02();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO02_PERUNIT_DAY", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario03() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario03();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO03_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "6.0", "720.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "6.0", "1", "360.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "6.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "1116.00");
        eva.assertOverallCosts("1116.00", "EUR", "1116.00");
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario04() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario04();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO04_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        eva.assertOneTimeFee(priceModel, "20.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "19.0", "2280.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "19.0", "1", "1140.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "19.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "3554.00");
        eva.assertOverallCosts("EUR", "3554.00");
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario05() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario05();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO05_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        eva.assertOneTimeFee(priceModel, "20.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "5.0", "600.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "5.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "5.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "950.00");
        eva.assertOverallCosts("EUR", "950.00");
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario06() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario06();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO06_PERUNIT_DAY", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario07() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario07();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO07_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "27.0", "3240.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "27.0", "1", "1620.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "27.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "5022.00");
        eva.assertNodeCount(priceModel,
                "UserAssignmentCosts/RoleCosts/RoleCost", 1);
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 27.0, "162.00");
        eva.assertTotalRoleCosts(roleCosts, "162.00");
        eva.assertOverallCosts("EUR", "5022.00");
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario08() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario08();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO08_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        eva.assertOverallCosts("3720.00", "EUR", "3720.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "31.0", "3720.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "0.0", "0", "0.00");
    }

    @Test
    public void billingPerUnitDaySubscriptionUsageScenario09() throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario09();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO09_PERUNIT_DAY", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "120.00", "31.0", "3720.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "60.00", "32.0", "2", "1920.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "31.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "1.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 31.0, "186.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "7.00");
        eva.assertTotalRoleCosts(roleCosts, "193.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "5833.00");
        eva.assertOverallCosts("EUR", "5833.00");
    }

    @Test
    public void billingPUDayStepped() throws Exception {
        // given
        setup.createPerUnitDaySteppedScenario();

        // when
        performBillingRun(0, "2013-05-01 10:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PER_UNIT_DAY_STEPPED", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 12:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "85.00", "4.0", "340.00");

        // event costs
        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        Node event = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_DOWNLOAD");
        Node steppedEventPrices = BillingXMLNodeSearch
                .getSteppedEventPricesNode(event);
        eva.assertSteppedPrice(steppedEventPrices, "0.00", "10.00", "0", "10",
                "100.00", "10");
        eva.assertSteppedPrice(steppedEventPrices, "100.00", "5.00", "10",
                "40", "150.00", "30");
        eva.assertSteppedPrice(steppedEventPrices, "250.00", "2.00", "40",
                "80", "80.00", "40");
        eva.assertSteppedPrice(steppedEventPrices, "330.00", "1.00", "80",
                "null", "20.00", "20");
        eva.assertSteppedPricesAmount(steppedEventPrices, "350.00");
        eva.assertEventCosts(event, "100", "350.00");
        eva.assertGatheredEventsCosts(priceModel, "350.00");

        // stepped prices for user assignments
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), 4.0, "1", "490.00");
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "150.00", "0",
                "1", "150.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "150.00", "120.00", "1",
                "3", "240.00", "2");
        eva.assertSteppedPrice(steppedUserAssPrices, "390.00", "100.00", "3",
                "null", "100.00", "1.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "490.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        eva.assertParametersCosts(priceModel, "9427.60");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-04-04 12:00:00") + "",
                "813", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(), 4.0,
                "8452.00", "813.0");
        Node steppedParPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParPrices, "0.00", "5.00", "0", "100",
                "500.00", "100");
        eva.assertSteppedPrice(steppedParPrices, "500.00", "3.00", "100",
                "300", "600.00", "200");
        eva.assertSteppedPrice(steppedParPrices, "1100.00", "2.00", "300",
                "800", "1000.00", "500");
        eva.assertSteppedPrice(steppedParPrices, "2100.00", "1.00", "800",
                "null", "13.00", "13");
        eva.assertSteppedPricesAmount(steppedParPrices, "2113.00");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "0.30", 4.0, "975.60", "975.60", "813.0");

        eva.assertParameterCosts(parameter, "9427.60");

        eva.assertPriceModelCosts(priceModel, "EUR", "10632.60");
        eva.assertOverallCosts("EUR", "10632.60");
    }

    @Test
    public void billingProRataDayStepped() throws Exception {
        // given
        setup.createProRataDaySteppedScenario();

        // when
        performBillingRun(0, "2013-05-01 10:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PRO_RATA_DAY_STEPPED", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "85.00", "4.0", "340.00");

        // event costs
        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        Node event = BillingXMLNodeSearch.getEventNode(gatheredEvents,
                "FILE_DOWNLOAD");
        Node steppedEventPrices = BillingXMLNodeSearch
                .getSteppedEventPricesNode(event);
        eva.assertSteppedPrice(steppedEventPrices, "0.00", "10.00", "0", "10",
                "100.00", "10");
        eva.assertSteppedPrice(steppedEventPrices, "100.00", "5.00", "10",
                "40", "150.00", "30");
        eva.assertSteppedPrice(steppedEventPrices, "250.00", "2.00", "40",
                "80", "80.00", "40");
        eva.assertSteppedPrice(steppedEventPrices, "330.00", "1.00", "80",
                "null", "20.00", "20");
        eva.assertSteppedPricesAmount(steppedEventPrices, "350.00");
        eva.assertEventCosts(event, "100", "350.00");
        eva.assertGatheredEventsCosts(priceModel, "350.00");

        // stepped prices for user assignments
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), 4.0, "1", "490.00");
        Node steppedUserAssPrices = BillingXMLNodeSearch
                .getUserAssignmentSteppedPricesNode(priceModel);
        eva.assertSteppedPrice(steppedUserAssPrices, "0.00", "150.00", "0",
                "1", "150.00", "1");
        eva.assertSteppedPrice(steppedUserAssPrices, "150.00", "120.00", "1",
                "3", "240.00", "2");
        eva.assertSteppedPrice(steppedUserAssPrices, "390.00", "100.00", "3",
                "null", "100.00", "1.0");
        eva.assertSteppedPricesAmount(steppedUserAssPrices, "490.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 1);
        eva.assertParametersCosts(priceModel, "9427.60");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "LONG_NUMBER",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"), "813",
                "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(), 4.0,
                "8452.00", "813.0");
        Node steppedParPrices = BillingXMLNodeSearch
                .getParameterSteppedPricesNode(parameter);
        eva.assertSteppedPrice(steppedParPrices, "0.00", "5.00", "0", "100",
                "500.00", "100");
        eva.assertSteppedPrice(steppedParPrices, "500.00", "3.00", "100",
                "300", "600.00", "200");
        eva.assertSteppedPrice(steppedParPrices, "1100.00", "2.00", "300",
                "800", "1000.00", "500");
        eva.assertSteppedPrice(steppedParPrices, "2100.00", "1.00", "800",
                "null", "13.00", "13");
        eva.assertSteppedPricesAmount(steppedParPrices, "2113.00");

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "0.30", 4.0, "975.60", "975.60", "813.0");

        eva.assertParameterCosts(parameter, "9427.60");

        eva.assertPriceModelCosts(priceModel, "EUR", "10632.60");
        eva.assertOverallCosts("EUR", "10632.60");
    }
}
