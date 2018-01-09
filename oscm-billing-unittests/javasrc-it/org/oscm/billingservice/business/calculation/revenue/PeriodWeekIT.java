/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 3, 2013                                                      
 *                                                                              
 **************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.PeriodWeekSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.UserGroup;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author malhotra
 * 
 */
public class PeriodWeekIT extends BillingIntegrationTestBase {

    private final PeriodWeekSetup testSetup = new PeriodWeekSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario01()
            throws Exception {
        // given
        testSetup.createSubUsageScenario01();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO01_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2012-11-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"));

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "5.0", "425.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", 5.0D, "1", "750.00",
                "780.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "5.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 5.0, "30.00");
        eva.assertTotalRoleCosts(roleCosts, "30.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "2340.50");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2012-11-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 5.0, "15.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 5.0, "150.00", "180.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "30.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 5.0, "30.00");
        eva.assertParameterCosts(parameter, "195.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2012-11-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 5.0, "150.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 5.0, "1500.00", "1950.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "450.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 5.0, "450.00");
        eva.assertParameterCosts(parameter, "2100.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2012-11-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                5.0, "10.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 5.0, "5.50", "35.50", null);
        eva.assertTotalRoleCosts(roleCosts, "30.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 5.0, "30.00");
        eva.assertOptionCosts(option, "45.50");
        eva.assertParameterCosts(parameter, "45.50");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "3545.50");
        eva.assertOverallCosts("EUR", "3545.50");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario02()
            throws Exception {
        // given
        testSetup.createSubUsageScenario02();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO02_PERUNIT_WEEK", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario03()
            throws Exception {
        // given
        testSetup.createSubUsageScenario03();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO03_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "4.0", "1", "600.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "4.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0D, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "1872.40");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354014000000", "1355828400000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.0, "120.00", "144.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "24.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0D, "24.00");
        eva.assertParameterCosts(parameter, "156.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354014000000", "1355828400000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 4.0, "120.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 4.0, "1200.00", "1560.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "360.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0D, "360.00");
        eva.assertParameterCosts(parameter, "1680.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354014000000", "1355828400000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 4.0, "4.40", "28.40", null);
        eva.assertTotalRoleCosts(roleCosts, "24.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0D, "24.00");
        eva.assertOptionCosts(option, "36.40");
        eva.assertParameterCosts(parameter, "36.40");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "2836.40");
        eva.assertOverallCosts("EUR", "2836.40");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario04()
            throws Exception {
        // given
        testSetup.createSubUsageScenario04();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO04_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "2152.30");
        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "3.0", "255.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "3.0", "1", "450.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "3.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "18.00");
        eva.assertTotalRoleCosts(roleCosts, "18.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "1404.30");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354497840000", "1355724720000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 3.0, "9.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 3.0, "90.00", "108.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "18.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "18.00");
        eva.assertParameterCosts(parameter, "117.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354497840000", "1355724720000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.0, "90.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.0, "900.00", "1170.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "270.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "270.00");
        eva.assertParameterCosts(parameter, "1260.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354497840000", "1355724720000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                3.0, "6.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 3.0, "3.30", "21.30", null);
        eva.assertTotalRoleCosts(roleCosts, "18.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "18.00");
        eva.assertOptionCosts(option, "27.30");
        eva.assertParameterCosts(parameter, "27.30");

        eva.assertOverallCosts("EUR", "2152.30");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario05()
            throws Exception {
        // given
        testSetup.createSubUsageScenario05();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO05_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2012-12-21 12:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "2.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2012-12-21 12:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2012-12-21 12:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2012-12-21 12:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "1443.20");
        eva.assertOverallCosts("EUR", "1443.20");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario06()
            throws Exception {
        // given
        testSetup.createSubUsageScenario06();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO06_PERUNIT_WEEK", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario07()
            throws Exception {
        // given
        testSetup.createSubUsageScenario07();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO07_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "4.0", "1", "600.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "4.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 6);
        eva.assertParametersCosts(priceModel, "4028.01");

        // BOOLEAN_PARAMETER (whole period not changed)
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1353884400000", "1356208560000", "true",
                "BOOLEAN");
        Node roleCostsForParam = BillingXMLNodeSearch
                .getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "12.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.0, "120.00", "144.00", "1.0");
        eva.assertTotalRoleCosts(roleCostsForParam, "24.00");
        eva.assertRoleCost(roleCostsForParam, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertParameterCosts(parameter, "156.00");

        // HAS_OPTIONS (whole period not changed)
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1353884400000", "1356208560000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCostsForParam = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 4.0, "4.40", "28.40", null);
        eva.assertTotalRoleCosts(roleCostsForParam, "24.00");
        eva.assertRoleCost(roleCostsForParam, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertOptionCosts(option, "36.40");
        eva.assertParameterCosts(parameter, "36.40");

        // MAX_FOLDER_NUMBER value change: 12
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1353884400000", "1354134960000", "12",
                "INTEGER");
        roleCostsForParam = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.414285714285714, "9.94", "12.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.414285714285714, "99.43", "129.26", "12.0");
        eva.assertTotalRoleCosts(roleCostsForParam, "29.83");
        eva.assertRoleCost(roleCostsForParam, "ADMIN", "6.00", 0.414285,
                "29.83");
        eva.assertParameterCosts(parameter, "139.20");

        // MAX_FOLDER_NUMBER value change: 21
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354134960000", "1354497840000", "21",
                "INTEGER");
        roleCostsForParam = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.6, "25.20", "21.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.6, "252.00", "327.60", "21.0");
        eva.assertTotalRoleCosts(roleCostsForParam, "75.60");
        eva.assertRoleCost(roleCostsForParam, "ADMIN", "6.00", 0.6, "75.60");
        eva.assertParameterCosts(parameter, "352.80");

        // MAX_FOLDER_NUMBER value change: 31
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354497840000", "1354679280000", "31",
                "INTEGER");
        roleCostsForParam = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 0.3, "18.60", "31.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 0.3, "186.00", "241.80", "31.0");
        eva.assertTotalRoleCosts(roleCostsForParam, "55.80");
        eva.assertRoleCost(roleCostsForParam, "ADMIN", "6.00", 0.3, "55.80");
        eva.assertParameterCosts(parameter, "260.40");

        // MAX_FOLDER_NUMBER value change: 41
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354679280000", "1356208560000", "41",
                "INTEGER");
        roleCostsForParam = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.68571428571429, "220.23", "41.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.68571428571429, "2202.29", "2862.98", "41.0");
        eva.assertTotalRoleCosts(roleCostsForParam, "660.69");
        eva.assertRoleCost(roleCostsForParam, "ADMIN", "6.00", 2.685714,
                "660.69");
        eva.assertParameterCosts(parameter, "3083.21");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "4992.01");
        eva.assertOverallCosts("EUR", "4992.01");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario08()
            throws Exception {
        // given
        testSetup.createSubUsageScenario08();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO08_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "0.0", "0", "0.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 4);
        eva.assertParametersCosts(priceModel, "139.99");

        // BOOLEAN_PARAMETER initial value
        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1353884400000", "1354497840000", "true",
                "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.01428571428571, "3.04", "1.0");
        eva.assertParameterCosts(parameter, "3.04");

        // BOOLEAN_PARAMETER changed value
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354498340000", "1356208560000", "true",
                "BOOLEAN");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.98488756613757, "8.95", "1.0");
        eva.assertParameterCosts(parameter, "8.95");

        // HAS_OPTIONS
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1353884400000", "1356208560000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                4.0, "8.00", null);
        eva.assertOptionCosts(option, "8.00");
        eva.assertParameterCosts(parameter, "8.00");

        // MAX_FOLDER_NUMBER
        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1353884400000", "1356208560000", "15",
                "INTEGER");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 4.0, "120.00", "15.0");
        eva.assertParameterCosts(parameter, "120.00");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "479.99");
        eva.assertOverallCosts("479.99", "EUR", "479.99");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario09()
            throws Exception {
        // given
        testSetup.createSubUsageScenario09();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO09_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "5.0", "425.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "5.0", "2", "750.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser2", "4.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 3.58571428, "28.69");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.41428571, "2.90");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "37.59");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "2469.47");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354376880000", "1356873840000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 5.0, "15.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 5.0, "150.00", "187.59", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "37.59");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.4142857, "2.90");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1D, "6.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 3.58571428, "28.69");
        eva.assertParameterCosts(parameter, "202.59");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354376880000", "1356873840000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 5.0, "150.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 5.0, "1500.00", "2063.79", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "563.79");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.4142857, "43.50");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1D, "90.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 3.58571428, "430.29");
        eva.assertParameterCosts(parameter, "2213.79");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354376880000", "1356873840000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                5.0, "10.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 5.0, "5.50", "43.09", null);
        eva.assertTotalRoleCosts(roleCosts, "37.59");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.4142857, "2.90");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1D, "6.00");
        eva.assertRoleCost(roleCosts, "GUEST", "8.00", 3.58571428, "28.69");
        eva.assertOptionCosts(option, "53.09");
        eva.assertParameterCosts(parameter, "53.09");

        // assert total costs
        eva.assertPriceModelCosts(priceModel, "EUR", "3707.06");
        eva.assertOverallCosts("EUR", "3707.06");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario10()
            throws Exception {
        // given
        testSetup.createSubUsageScenario10();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK", 0);
        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK", 1);

        TestData testData = getTestData("SCENARIO10_PERUNIT_WEEK");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);

        Document billingResult = loadBillingResult(
                subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        // no unit assignment
        assertNull(BillingXMLNodeSearch
                .getOrganizationalUnitNode(billingResult));

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                subscriptionDetails.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354134960000L,
                1354497840000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "1418.20");
        eva.assertNullOneTimeFee(subscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "2.0");

        // roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        // parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354134960000", "1354497840000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354134960000", "1354497840000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354134960000", "1354497840000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        // Upgraded price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubscriptionDetails.getServiceId(),
                upgradedSubscriptionDetails.getPriceModel().getKey(),
                upgradedSubscriptionDetails.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354497840000L,
                1356908400000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "4950.00");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "4.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertOverallCosts("EUR", "6368.20");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario10_changeServiceIdAndUnitInBP()
            throws Exception {
        // given
        testSetup.createSubUsageScenario10_changeServiceIdAndUnitInBP();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP", 0);
        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP", 1);
        VOSubscriptionDetails modifiedSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP", 2);

        TestData testData = getTestData("SCENARIO10_PERUNIT_WEEK_CHSRVINBP");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails serviceWithChangedId = supplier.getService(2);
        UserGroup unit2 = customer.getUserGroups().get(1);

        Document billingResult = loadBillingResult(
                subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        // Organizational unit
        Node orgUnit = BillingXMLNodeSearch
                .getOrganizationalUnitNodeForSubscription(billingResult,
                        subscriptionDetails.getSubscriptionId(),
                        modifiedSubscriptionDetails.getUnitName(),
                        unit2.getReferenceId());
        assertNotNull("Organizational unit not found", orgUnit);

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                serviceWithChangedId.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354134960000L,
                1354497840000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "1418.20");
        eva.assertNullOneTimeFee(subscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "2.0");

        // roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        // parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354134960000", "1354497840000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354134960000", "1354497840000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354134960000", "1354497840000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        // Upgraded price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubscriptionDetails.getServiceId(),
                upgradedSubscriptionDetails.getPriceModel().getKey(),
                upgradedSubscriptionDetails.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354497840000L,
                1356908400000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "4950.00");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "4.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertOverallCosts("EUR", "6368.20");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario10_changeServiceIdAndUnitInBP_2()
            throws Exception {
        // given
        testSetup.createSubUsageScenario10_changeServiceIdAndUnitInBP_2();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP_2", 0);
        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP_2", 1);
        VOSubscriptionDetails modifiedSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP_2", 2);

        TestData testData = getTestData("SCENARIO10_PERUNIT_WEEK_CHSRVINBP_2");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails service = supplier.getService(0);
        VOServiceDetails serviceWithChangedId = supplier.getService(2);
        UserGroup unit1 = customer.getUserGroups().get(0);
        UserGroup unit2 = customer.getUserGroups().get(1);

        // *** First billing period ***
        Document billingResult = loadBillingResult(
                subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-11-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        // Subscription starts in overlapping week -> Only one time fee is
        // charged
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                service.getServiceId(), subscriptionDetails.getPriceModel()
                        .getKey(), subscriptionDetails.getPriceModel()
                        .getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354134960000L,
                1354134960000L);
        eva.assertOneTimeFee(subscriptionDetails.getPriceModel().getKey(),
                "25.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "25.00");
        eva.assertOverallCosts("EUR", "25.00");

        // Organizational unit
        Node orgUnit = BillingXMLNodeSearch
                .getOrganizationalUnitNodeForSubscription(billingResult,
                        subscriptionDetails.getSubscriptionId(),
                        subscriptionDetails.getUnitName(),
                        unit1.getReferenceId());
        assertNotNull("Organizational unit not found", orgUnit);

        // *** Second billing period ***
        billingResult = loadBillingResult(subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        // Organizational unit
        orgUnit = BillingXMLNodeSearch
                .getOrganizationalUnitNodeForSubscription(billingResult,
                        subscriptionDetails.getSubscriptionId(),
                        modifiedSubscriptionDetails.getUnitName(),
                        unit2.getReferenceId());
        assertNotNull("Organizational unit not found", orgUnit);

        // Initial price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                serviceWithChangedId.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354134960000L,
                1354497840000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "1418.20");
        eva.assertNullOneTimeFee(subscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "2.0");

        // roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        // parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354134960000", "1354497840000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354134960000", "1354497840000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354134960000", "1354497840000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        // Upgraded price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubscriptionDetails.getServiceId(),
                upgradedSubscriptionDetails.getPriceModel().getKey(),
                upgradedSubscriptionDetails.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354497840000L,
                1356908400000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "4950.00");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "4.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertOverallCosts("EUR", "6368.20");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario10_changeServiceIdAndRemoveUnitInBP()
            throws Exception {
        // given
        testSetup.createSubUsageScenario10_changeServiceIdAndRemoveUnitInBP();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_RUCSINBP", 0);
        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_RUCSINBP", 1);

        TestData testData = getTestData("SCENARIO10_PERUNIT_WEEK_RUCSINBP");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails serviceWithChangedId = supplier.getService(2);

        Document billingResult = loadBillingResult(
                subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        assertNull("Organization unit shouldn't exist",
                BillingXMLNodeSearch.getOrganizationalUnitNode(billingResult));

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                serviceWithChangedId.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354134960000L,
                1354497840000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "1418.20");
        eva.assertNullOneTimeFee(subscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "2.0");

        // roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        // parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354134960000", "1354497840000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354134960000", "1354497840000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354134960000", "1354497840000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        // Upgraded price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubscriptionDetails.getServiceId(),
                upgradedSubscriptionDetails.getPriceModel().getKey(),
                upgradedSubscriptionDetails.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354497840000L,
                1356908400000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "4950.00");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "4.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertOverallCosts("EUR", "6368.20");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario10_changeServiceIdAndUnitAfterBP()
            throws Exception {
        // given
        testSetup.createSubUsageScenario10_changeServiceIdAndUnitAfterBP();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails subscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVAFBP", 0);
        VOSubscriptionDetails upgradedSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_WEEK_CHSRVAFBP", 1);

        TestData testData = getTestData("SCENARIO10_PERUNIT_WEEK_CHSRVAFBP");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        UserGroup unit1 = customer.getUserGroups().get(0);

        Document billingResult = loadBillingResult(
                subscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(),
                subscriptionDetails.getPaymentInfo().getId());

        // Organizational unit
        Node orgUnit = BillingXMLNodeSearch
                .getOrganizationalUnitNodeForSubscription(billingResult,
                        subscriptionDetails.getSubscriptionId(),
                        subscriptionDetails.getUnitName(),
                        unit1.getReferenceId());
        assertNotNull("Organizational unit not found", orgUnit);

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                subscriptionDetails.getServiceId(), subscriptionDetails
                        .getPriceModel().getKey(), subscriptionDetails
                        .getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354134960000L,
                1354497840000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "1418.20");
        eva.assertNullOneTimeFee(subscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "2.0");

        // roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        // parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER", "1354134960000", "1354497840000", "true",
                "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER", "1354134960000", "1354497840000", "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS", "1354134960000", "1354497840000", "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        // Upgraded price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                upgradedSubscriptionDetails.getServiceId(),
                upgradedSubscriptionDetails.getPriceModel().getKey(),
                upgradedSubscriptionDetails.getPriceModel().getType().name());
        assertNotNull("Price Model doesn't exist", priceModel);
        eva.assertPriceModelUsagePeriod(priceModel, 1354497840000L,
                1356908400000L);

        eva.assertPriceModelCosts(priceModel, "EUR", "4950.00");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "4.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertOverallCosts("EUR", "6368.20");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario11()
            throws Exception {
        // given
        testSetup.createSubUsageScenario11();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO11_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertNullOneTimeFee();

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"));
        eva.assertPriceModelCosts(priceModel, "EUR", "2127.30");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "3.0", "255.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "3.0", "1", "450.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Scenario011PUWeekCustomerAdmin", "3.0");

        // roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "18.00");
        eva.assertTotalRoleCosts(roleCosts, "18.00");

        // parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "1404.30");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 3.0, "9.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 3.0, "90.00", "108.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "18.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3, "18.00");
        eva.assertParameterCosts(parameter, "117.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"), "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.0, "90.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.0, "900.00", "1170.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "270.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "270.00");
        eva.assertParameterCosts(parameter, "1260.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"), "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                3.0, "6.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 3.0, "3.30", "21.30", null);
        eva.assertTotalRoleCosts(roleCosts, "18.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "18.00");
        eva.assertOptionCosts(option, "27.30");
        eva.assertParameterCosts(parameter, "27.30");

        // Resumed price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "1418.20");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Scenario011PUWeekCustomerAdmin", "2.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "1",
                "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        eva.assertOverallCosts("EUR", "3545.50");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario11a()
            throws Exception {
        // given
        testSetup.createSubUsageScenario11a();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO11a_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertNullOneTimeFee();

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"));
        eva.assertPriceModelCosts(priceModel, "EUR", "2127.30");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "3.0", "255.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "3.0", "1", "450.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Scenario011aPUWeekCustomerAdmin", "3.0");

        // roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "18.00");
        eva.assertTotalRoleCosts(roleCosts, "18.00");

        // parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "1404.30");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 3.0, "9.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 3.0, "90.00", "108.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "18.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3, "18.00");
        eva.assertParameterCosts(parameter, "117.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"), "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 3.0, "90.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 3.0, "900.00", "1170.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "270.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "270.00");
        eva.assertParameterCosts(parameter, "1260.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2012-11-28 21:36:00"),
                DateTimeHandling.calculateMillis("2012-12-11 02:52:48"), "1",
                "ENUMERATION");
        Node option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                3.0, "6.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 3.0, "3.30", "21.30", null);
        eva.assertTotalRoleCosts(roleCosts, "18.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 3.0, "18.00");
        eva.assertOptionCosts(option, "27.30");
        eva.assertParameterCosts(parameter, "27.30");

        // Resumed price model
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"));
        eva.assertPriceModelCosts(priceModel, "EUR", "1418.20");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "2.0", "170.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "2.0", "1", "300.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "Scenario011aPUWeekCustomerAdmin", "2.0");

        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertTotalRoleCosts(roleCosts, "12.00");

        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "936.20");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"),
                "true", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 2.0, "6.00", "1.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 2.0, "60.00", "72.00", "1.0");
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertParameterCosts(parameter, "78.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "MAX_FOLDER_NUMBER",
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "15",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 2.0, "60.00", "15.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 2.0, "600.00", "780.00", "15.0");
        eva.assertTotalRoleCosts(roleCosts, "180.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "180.00");
        eva.assertParameterCosts(parameter, "840.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2012-12-22 02:52:48"),
                DateTimeHandling.calculateMillis("2012-12-31 00:00:00"), "1",
                "ENUMERATION");
        option = BillingXMLNodeSearch.getParameterOptionNode(parameter);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(option);
        eva.assertParameterPeriodFee(option, PricingPeriod.WEEK.name(), "2.00",
                2.0, "4.00", null);
        eva.assertUserAssignmentCosts(option, PricingPeriod.WEEK.name(),
                "1.10", 2.0, "2.20", "14.20", null);
        eva.assertTotalRoleCosts(roleCosts, "12.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 2.0, "12.00");
        eva.assertOptionCosts(option, "18.20");
        eva.assertParameterCosts(parameter, "18.20");

        eva.assertOverallCosts("EUR", "3545.50");
    }

    @Test
    public void billingPerUnitWeekSubscriptionUsageScenario16()
            throws Exception {
        // given
        testSetup.createSubUsageScenario16();

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO16_PERUNIT_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        assertNotNull(billingResult);

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("EUR", "3181.92");
        assertEquals(
                "1355180400000",
                XMLConverter
                        .getNodeTextContentByXPath(billingResult,
                                "//Parameter[@id='BOOLEAN_PARAMETER']/ParameterUsagePeriod/@startDate"));
        assertEquals(
                "1354575600000",
                XMLConverter
                        .getNodeTextContentByXPath(billingResult,
                                "//Parameter[@id='HAS_OPTIONS']/ParameterUsagePeriod/@startDate"));
        assertEquals(
                "1353884400000",
                XMLConverter
                        .getNodeTextContentByXPath(billingResult,
                                "//Parameter[@id='MAX_FOLDER_NUMBER']/ParameterUsagePeriod/@startDate"));
    }

    @Test
    public void billingPUWeekServiceDeletion() throws Exception {
        // given
        testSetup.createWeekScenarioServiceDeletion();

        // when
        performBillingRun(3, "2013-04-10 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SERVICEDEL_PU_WEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-02 05:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "4.0", "340.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "4.0", "1", "600.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "4.0");

        // assert roles
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "3956.40");

        Node parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-02 05:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                "false", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 4.0, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 4.0, "0.00", "0.00", "0.0");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-02 05:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"), "2",
                "ENUMERATION");
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
                DateTimeHandling.calculateMillis("2013-02-02 05:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"), "35",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 4.0, "280.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 4.0, "2800.00", "3640.00", "35.0");
        eva.assertTotalRoleCosts(roleCosts, "840.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "840.00");
        eva.assertParameterCosts(parameter, "3920.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4945.40");
        eva.assertOverallCosts("EUR", "4945.40");

        // Second billing result
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 15:00:00"));

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "85.00", "1.0", "85.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "150.00", "1.0", "1", "150.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "1.0");

        // assert roles
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");

        // assert parameters
        eva.assertNodeCount(priceModel, "Parameters/Parameter", 3);
        eva.assertParametersCosts(priceModel, "989.10");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "BOOLEAN_PARAMETER",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 15:00:00"),
                "false", "BOOLEAN");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "3.00", 1.0, "0.00", "0.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "30.00", 1.0, "0.00", "0.00", "0.0");
        eva.assertTotalRoleCosts(roleCosts, "0.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "0.00");
        eva.assertParameterCosts(parameter, "0.00");

        parameter = BillingXMLNodeSearch.getParameterNode(priceModel,
                "HAS_OPTIONS",
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 15:00:00"), "2",
                "ENUMERATION");
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
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-25 15:00:00"), "35",
                "INTEGER");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(parameter);
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(),
                "2.00", 1.0, "70.00", "35.0");
        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "20.00", 1.0, "700.00", "910.00", "35.0");
        eva.assertTotalRoleCosts(roleCosts, "210.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "210.00");
        eva.assertParameterCosts(parameter, "980.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1230.10");
        eva.assertOverallCosts("EUR", "1230.10");
    }

    @Test
    public void billingPUWeekLongUsage() throws Exception {
        // given
        testSetup.createWeekScenarioLongUsage();

        // when
        performBillingRun(0, "2013-09-02 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "LONG_USAGE_PUWEEK", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-07-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-26 00:00:00"));

        eva.assertNullOneTimeFee();

        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
                "734.00", "4.0", "2936.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.WEEK.name(), "434.00", "4.0", "1", "1736.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceSecondCustomerUser1", "4.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertNodeCount(roleCosts, "RoleCost", 1);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 4.0, "24.00");
        eva.assertTotalRoleCosts(roleCosts, "24.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "4696.00");
        eva.assertOverallCosts("EUR", "4696.00");
    }

    @Test
    public void billingPUWeekStepped() throws Exception {
        // given
        testSetup.createPerUnitWeekSteppedScenario();

        // when
        performBillingRun(0, "2013-05-01 10:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PER_UNIT_WEEK_STEPPED", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
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
                PricingPeriod.WEEK.name(), 4.0, "1", "490.00");
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
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00") + "",
                "813", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 4.0,
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

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.30", 4.0, "975.60", "975.60", "813.0");

        eva.assertParameterCosts(parameter, "9427.60");

        eva.assertPriceModelCosts(priceModel, "EUR", "10632.60");
        eva.assertOverallCosts("EUR", "10632.60");
    }

    @Test
    public void billingProRataWeekStepped() throws Exception {
        // given
        testSetup.createProRataWeekSteppedScenario();

        // when
        performBillingRun(0, "2013-05-01 10:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "PRO_RATA_WEEK_STEPPED", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"));

        eva.assertOneTimeFee(priceModel, "25.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.WEEK.name(),
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
                PricingPeriod.WEEK.name(), 4.0, "1", "490.00");
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
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00") + "",
                "813", "LONG");
        eva.assertParameterPeriodFee(parameter, PricingPeriod.WEEK.name(), 4.0,
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

        eva.assertUserAssignmentCosts(parameter, PricingPeriod.WEEK.name(),
                "0.30", 4.0, "975.60", "975.60", "813.0");

        eva.assertParameterCosts(parameter, "9427.60");

        eva.assertPriceModelCosts(priceModel, "EUR", "10632.60");
        eva.assertOverallCosts("EUR", "10632.60");
    }
}
