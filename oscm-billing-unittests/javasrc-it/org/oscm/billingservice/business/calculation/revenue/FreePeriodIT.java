/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: June 14, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.FreePeriodSetup;
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
public class FreePeriodIT extends BillingIntegrationTestBase {

    private FreePeriodSetup testSetup = new FreePeriodSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void fp01_FreePeriodEndsBeforeBillingPeriod() throws Exception {
        // given subscription data for scenario
        testSetup.createFP01_FreePeriodEndsBeforeBillingPeriod();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_01", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");
        eva.assertNullOneTimeFee();

        // subscription entry before suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-07 00:00:00"), DateTimeHandling
                .calculateMillis("2013-06-09 00:00:00"));
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
                DateTimeHandling.calculateMillis("2013-06-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00") + "",
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
        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-11 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"));
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
                DateTimeHandling.calculateMillis("2013-06-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
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
    public void fp02_FreePeriodEndsBeforeSuspend() throws Exception {
        // given subscription data for scenario
        testSetup.createFP02_FreePeriodEndsBeforeSuspend();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_02", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        // subscription entry before suspend
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-08 00:00:00"), DateTimeHandling
                .calculateMillis("2013-06-09 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "421.00");
        eva.assertOneTimeFee(priceModel, "3.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "2.0", "12.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "2.0", "1", "10.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 2.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-08 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 2.0, "60.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 2.0, "240.00", "330.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "90.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 2.0, "90.00");
        eva.assertParameterCosts(parameter, "390.00");

        // subscription entry after resume
        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-11 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "5554.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "26.0", "156.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "26.0", "1", "130.00");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 26.0, "78.00");
        eva.assertTotalRoleCosts(roleCosts, "78.00");

        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(gatheredEvents, "FOLDER_NEW", "30.00", "4",
                "120.00");
        eva.assertGatheredEventsCosts(priceModel, "120.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 26.0, "780.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 26.0, "3120.00", "4290.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "1170.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 26.0, "1170.00");
        eva.assertParameterCosts(parameter, "5070.00");

        eva.assertOverallCosts("EUR", "5975.00");
    }

    @Test
    public void fp03_FreePeriodEndsBetweenSusRes() throws Exception {
        // given subscription data for scenario
        testSetup.createFP03_FreePeriodEndsBetweenSusRes();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails("FREEP_03", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-06-07 00:00:00", "2013-07-07 00:00:00");

        // subscription entry before suspend
        eva.assertNullPriceModel(sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));

        // subscription entry after resume
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00"));
        eva.assertOneTimeFee(priceModel, "3.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "26.0", "156.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "26.0", "1", "130.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 26.0, "78.00");
        eva.assertTotalRoleCosts(roleCosts, "78.00");
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-11 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 26.0, "780.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 26.0, "3120.00", "4290.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "1170.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 26.0, "1170.00");
        eva.assertParameterCosts(parameter, "5070.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "5437.00");
        eva.assertOverallCosts("EUR", "5437.00");
    }

    @Test
    public void fp04_FreePeriodEndsAfterRes() throws Exception {
        // given subscription data for scenario
        testSetup.createFP04_FreePeriodEndsAfterRes();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_04", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        // subscription entry before suspend
        eva.assertNullPriceModel(
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));

        // subscription entry after resume
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-16 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"));

        eva.assertOneTimeFee(priceModel, "3.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "21.0", "126.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "21.0", "1", "105.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 21.0, "63.00");
        eva.assertTotalRoleCosts(roleCosts, "63.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-16 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 21.0, "630.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 21.0, "2520.00", "3465.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "945.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 21.0, "945.00");
        eva.assertParameterCosts(parameter, "4095.00");

        eva.assertOverallCosts("EUR", "4392.00");
    }

    @Test
    public void fp05_FreePeriodEndsAfterBillingPeriod() throws Exception {
        // given subscription data for scenario
        testSetup.createFP05_FreePeriodEndsAfterBillingPeriod();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_05", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        assertNull("There should be no billing result in this billing period",
                eva);
    }

    @Test
    public void fp06_SubscriptionTerminatesInFreePeriod() throws Exception {
        // given subscription data for scenario
        testSetup.createFP06_SubscriptionTerminatesInFreePeriod();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_06", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        assertNull(
                "There should be no billing result because subscription is completely free",
                eva);
    }

    @Test
    public void fp07_UpgradeServiceWithFreePeriod() throws Exception {
        // given subscription data for scenario
        testSetup.createFP07_UpgradeServiceWithFreePeriod();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_07", 0);
        VOSubscriptionDetails upgradedSubDetails = getSubscriptionDetails(
                "FREEP_07", 1);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        // subscription entry before suspend
        eva.assertNullPriceModel(
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));

        // subscription entry after resume - original price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-18 00:00:00"), DateTimeHandling
                .calculateMillis("2013-06-21 00:00:00"));

        eva.assertOneTimeFee(priceModel, "3.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "4.0", "24.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "4.0", "1", "20.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 4.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-18 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-06-21 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 4.0, "120.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 4.0, "480.00", "660.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 4.0, "180.00");
        eva.assertParameterCosts(parameter, "780.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "839.00");

        // upgraded price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-24 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "2241.00");
        eva.assertOneTimeFee(priceModel, "1.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "2.00", "13.0", "26.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "3.00", "13.0", "1", "39.00");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 13.0, "65.00");
        eva.assertTotalRoleCosts(roleCosts, "65.00");

        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(gatheredEvents, "FOLDER_NEW", "20.00", "8",
                "160.00");
        eva.assertGatheredEventsCosts(priceModel, "160.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-24 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.00", 13.0, "195.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "4.00", 13.0, "780.00", "1755.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "975.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 13.0, "975.00");
        eva.assertParameterCosts(parameter, "1950.00");

        eva.assertOverallCosts("EUR", "3080.00");
    }

    @Test
    public void fp08_UpgradeInFreePeriod() throws Exception {
        // given subscription data for scenario
        testSetup.createFP08_UpgradeInFreePeriod();

        // when billing run performed
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_08", 0);
        VOSubscriptionDetails upgradedSubDetails = getSubscriptionDetails(
                "FREEP_08", 1);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        // subscription entry before suspend
        eva.assertNullPriceModel(
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));

        // subscription entry after resume, original price model
        eva.assertNullPriceModel(
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-21 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-21 00:00:00"));

        // upgraded price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-24 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "2081.00");
        eva.assertOneTimeFee(priceModel, "1.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "2.00", "13.0", "26.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "3.00", "13.0", "1", "39.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 13.0, "65.00");
        eva.assertTotalRoleCosts(roleCosts, "65.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-24 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.00", 13.0, "195.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "4.00", 13.0, "780.00", "1755.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "975.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 13.0, "975.00");
        eva.assertParameterCosts(parameter, "1950.00");

        eva.assertOverallCosts("EUR", "2081.00");
    }

    @Test
    public void fp09_FreePeriodEndsAtBeginOfBillingPeriod() throws Exception {
        // given subscription data for scenario
        testSetup.createFP09_FreePeriodEndsAtBeginOfBillingPeriod();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_09", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");
        assertNull("There should be no billing result in this billing period",
                eva);

        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-07-07 00:00:00", "2013-08-07 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"), DateTimeHandling
                .calculateMillis("2013-08-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "6482.00");
        eva.assertOneTimeFee(priceModel, "3.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "31.0", "186.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "31.0", "1", "155.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 31.0, "93.00");
        eva.assertTotalRoleCosts(roleCosts, "93.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-08-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 31.0, "930.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 31.0, "3720.00", "5115.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "1395.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 31.0, "1395.00");
        eva.assertParameterCosts(parameter, "6045.00");

        eva.assertOverallCosts("EUR", "6482.00");
    }

    @Test
    public void fp10_FreePeriodEndsAtTerminationTime() throws Exception {
        // given subscription data for scenario
        testSetup.createFP10_FreePeriodEndsAtTerminationTime();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_10", 0);

        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");
        assertNull("There should be no billing result in this billing period",
                eva);

        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-07-07 00:00:00", "2013-08-07 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "212.00");
        eva.assertOneTimeFee(priceModel, "3.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "1.0", "6.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "1.0", "1", "5.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "3.00");
        eva.assertTotalRoleCosts(roleCosts, "3.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 1.0, "30.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 1.0, "120.00", "165.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "45.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "45.00");
        eva.assertParameterCosts(parameter, "195.00");

        eva.assertOverallCosts("EUR", "212.00");
    }

    @Test
    public void fp11_FreePeriodEndsAtUpgradeTime() throws Exception {
        // given subscription data for scenario
        testSetup.createFP11_FreePeriodEndsAtUpgradeTime();

        // when
        performBillingRun(1, "2013-08-10 23:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "FREEP_11", 0);
        VOSubscriptionDetails upgradedSubDetails = getSubscriptionDetails(
                "FREEP_11", 1);

        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-06-07 00:00:00",
                "2013-07-07 00:00:00");

        // subscription entry before suspend
        eva.assertNullPriceModel(
                voSubscriptionDetails.getPriceModel().getKey(),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-09 00:00:00"));

        // subscription entry after resume - original price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-21 00:00:00"), DateTimeHandling
                .calculateMillis("2013-06-21 00:00:00"));

        eva.assertOneTimeFee(priceModel, "3.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "6.00", "1.0", "6.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "5.00", "1.0", "1", "5.00");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "3.00");
        eva.assertTotalRoleCosts(roleCosts, "3.00");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-21 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-06-21 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "2.00", 1.0, "30.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "8.00", 1.0, "120.00", "165.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "45.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "3.00", 1.0, "45.00");
        eva.assertParameterCosts(parameter, "195.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "212.00");

        // upgraded price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), upgradedSubDetails.getPriceModel()
                .getKey(), DateTimeHandling
                .calculateMillis("2013-06-24 00:00:00"), DateTimeHandling
                .calculateMillis("2013-07-07 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "2241.00");
        eva.assertOneTimeFee(priceModel, "1.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.DAY.name(),
                "2.00", "13.0", "26.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.DAY.name(), "3.00", "13.0", "1", "39.00");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 13.0, "65.00");
        eva.assertTotalRoleCosts(roleCosts, "65.00");

        Node gatheredEvents = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(gatheredEvents, "FOLDER_NEW", "20.00", "8",
                "160.00");
        eva.assertGatheredEventsCosts(priceModel, "160.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2013-06-24 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-07-07 00:00:00") + "",
                "15", "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.DAY.name(),
                "1.00", 13.0, "195.00", "15.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.DAY.name(),
                "4.00", 13.0, "780.00", "1755.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "975.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "5.00", 13.0, "975.00");
        eva.assertParameterCosts(parameter, "1950.00");

        eva.assertOverallCosts("EUR", "2453.00");
    }

}
