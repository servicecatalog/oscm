/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 3, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.oscm.billingservice.business.calculation.revenue.setup.PeriodMonthSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author malhotra
 * 
 */
public class PeriodMonthIT extends BillingIntegrationTestBase {

    private static PeriodMonthSetup setup;

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);

        // because testcase14 expects all data from testcase1 to 15 we need to
        // setup it here!
        setup = new PeriodMonthSetup();
        setup.createSubUsageScenario01();
        setup.createSubUsageScenario02();
        setup.createSubUsageScenario03();
        setup.createSubUsageScenario04();
        setup.createSubUsageScenario05();
        setup.createSubUsageScenario06();
        setup.createSubUsageScenario07();
        setup.createSubUsageScenario08();
        setup.createSubUsageScenario09();
        setup.createSubUsageScenario10();
        setup.createSubUsageScenario12();
        setup.createSubUsageScenario13();
        setup.createSubUsageScenario14();
        setup.createSubUsageScenario15();
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario01()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO01_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        eva.assertPriceModelCosts(priceModel, voSubscriptionDetails
                .getPriceModel().getCurrencyISOCode(), "1029.00");
        eva.assertOverallCosts("1029.00", "EUR", "1029.00");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario02()
            throws Exception {
        // given subscription data for scenario

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO02_PERUNIT_MONTH", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario03()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO03_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");
        eva.assertOverallCosts("1029.00", "EUR", "1029.00");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario04()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO04_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "1.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "1152.00");
        eva.assertOverallCosts("1152.00", "EUR", "1152.00");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario05()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO05_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOneTimeFee(priceModel, "123.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "1152.00");
        eva.assertOverallCosts("1152.00", "EUR", "1152.00");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario06()
            throws Exception {
        // given subscription data for scenario

        // when
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO06_PERUNIT_MONTH", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2012-12-01 00:00:00", "2013-01-01 00:00:00"));
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario07()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO07_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "1.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");
        eva.assertTotalRoleCosts(
                BillingXMLNodeSearch.getRoleCostsNode(priceModel), "6.00");
        eva.assertOverallCosts("1029.00", "EUR", "1029.00");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario08()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO08_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertOverallCosts("678.00", "EUR", "678.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "0.0", "0", "0.00");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario09()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO09_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");

        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.12903225, "0.77");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 0.87096774, "6.10");
        eva.assertTotalRoleCosts(roleCosts, "6.87");

        eva.assertPriceModelCosts(priceModel, "EUR", "1029.87");
        eva.assertOverallCosts("1029.87", "EUR", "1029.87");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario10()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertNullOneTimeFee(voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        // Upgraded price model
        voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO10_PERUNIT_MONTH", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", "1.0", "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");

        // Overall costs
        eva.assertOverallCosts("EUR", "2457.00");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario12()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO12_PRORATA_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertNullOneTimeFee(voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1029.00");

        // Upgraded to pro rata price model
        voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO12_PRORATA_MONTH", 1);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", 0.6774193548387096, "497.23");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", 0.6774193548387096, "1",
                "294.00");
        eva.assertUserAssignmentCostsByUser(priceModel,
                "GreenPeaceCustomerUser1", 0.6774193548387096);
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 0.6774, "4.06");
        eva.assertTotalRoleCosts(roleCosts, "4.06");
        eva.assertPriceModelCosts(priceModel, "EUR", "1049.29");

        // Overall costs
        eva.assertOverallCosts("EUR", "2078.29");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario13()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO13_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);

        // Initial price model
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "778.00", "1.0", "778.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "445.00", "2.0", "2", "890.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser2", "1.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");
        eva.assertRoleCost(roleCosts, "USER", "7.00", 1.0, "7.00");
        eva.assertTotalRoleCosts(roleCosts, "13.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1681.00");
        eva.assertOverallCosts("1681.00", "EUR", "1681.00");
    }

    /**
     * Calculates the operator's "Billing data preview" XML and verifies it for
     * scenarios 1-15.
     */
    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario14()
            throws Exception {
        // given

        // when
        final String periodStart = "2012-02-15 06:05:00";
        final String periodEnd = "2013-01-21 02:59:00";

        List<BillingResult> r = generateBillingForAnyPeriod(periodStart,
                periodEnd, getBasicSetup().getCustomer().getKey());

        // then
        // remove all tests which do not belong to scenario 1 to 15
        for (int i = r.size() - 1; i >= 0; i--) {
            Document doc = XMLConverter.convertToDocument(r.get(i)
                    .getResultXML(), false);
            String s = XMLConverter.getNodeByXPath(doc, "//Subscription/@id")
                    .getNodeValue();
            if (s == null || !s.startsWith("SCENARIO")
                    || Integer.valueOf(s.substring(8, 10)).intValue() > 15) {
                r.remove(i);
            }
        }

        assertEquals(30, r.size());

        assertBillingResult(r.get(0), "SCENARIO01_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(1), "SCENARIO01_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.00");
        assertBillingResult(r.get(2), "SCENARIO01_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.00");
        assertBillingResult(r.get(3), "SCENARIO02_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(4), "SCENARIO03_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(5), "SCENARIO03_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.00");
        assertBillingResult(r.get(6), "SCENARIO04_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(7), "SCENARIO05_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(8), "SCENARIO05_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.00");
        assertBillingResult(r.get(9), "SCENARIO06_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(10), "SCENARIO07_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(11), "SCENARIO07_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.00");
        assertBillingResult(r.get(12), "SCENARIO08_PERUNIT_MONTH", periodStart,
                periodEnd, "801.00");
        assertBillingResult(r.get(13), "SCENARIO08_PERUNIT_MONTH", periodStart,
                periodEnd, "678.00");
        assertBillingResult(r.get(14), "SCENARIO09_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(15), "SCENARIO09_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.87");
        assertBillingResult(r.get(16), "SCENARIO09_PERUNIT_MONTH", periodStart,
                periodEnd, "1030.00");
        assertBillingResult(r.get(17), "SCENARIO10_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(18), "SCENARIO10_PERUNIT_MONTH", periodStart,
                periodEnd, "2457.00");
        assertBillingResult(r.get(19), "SCENARIO10_PERUNIT_MONTH", periodStart,
                periodEnd, "1174.00");
        assertBillingResult(r.get(20), "SCENARIO12_PRORATA_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(21), "SCENARIO12_PRORATA_MONTH", periodStart,
                periodEnd, "2078.29");
        assertBillingResult(r.get(22), "SCENARIO12_PRORATA_MONTH", periodStart,
                periodEnd, "208.29");
        assertBillingResult(r.get(23), "SCENARIO13_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(24), "SCENARIO13_PERUNIT_MONTH", periodStart,
                periodEnd, "1681.00");
        assertBillingResult(r.get(25), "SCENARIO13_PERUNIT_MONTH", periodStart,
                periodEnd, "1681.00");
        assertBillingResult(r.get(26), "SCENARIO14_PERUNIT_MONTH", periodStart,
                periodEnd, "1152.00");
        assertBillingResult(r.get(27), "SCENARIO14_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.00");
        assertBillingResult(r.get(28), "SCENARIO14_PERUNIT_MONTH", periodStart,
                periodEnd, "1029.00");
        assertBillingResult(r.get(29), "SCENARIO15_PERUNIT_MONTH_2",
                periodStart, periodEnd, "1620.10");
    }

    private void assertBillingResult(BillingResult b, String subscriptionId,
            String start, String end, String gross)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        final BillingResultEvaluator eva = new BillingResultEvaluator(
                XMLConverter.convertToDocument(b.getResultXML(), true));
        eva.assertOverallCosts(gross, "EUR", gross);
        eva.assertPeriod(start, end);
        eva.assertSubscriptionId(subscriptionId);
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario15()
            throws Exception {
        // given subscription data for scenario

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO15_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        assertNull(
                "Subscription is completely free, no billing result expected",
                billingResult);

        voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO15_PERUNIT_MONTH_2", 0);
        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        assertNotNull(billingResult);

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        eva.assertOverallCosts("1620.10", "EUR", "1620.10");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario18()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario18();

        // when billing run performed
        performBillingRun(6, "2013-03-12 01:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO18_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2013-02-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-05 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        eva.assertPriceModelCosts(priceModel, "EUR", "1497.10");
        eva.assertOverallCosts("1497.10", "EUR", "1497.10");
    }

    @Test
    public void billingPerUnitMonthSubscriptionUsageScenario19()
            throws Exception {
        // given subscription data for scenario
        setup.createSubUsageScenario19();

        // when billing run performed
        performBillingRun(0, "2013-01-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "SCENARIO19_PERUNIT_MONTH", 0);
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());

        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "678.00", "1.0", "678.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "345.00", "1.0", "1", "345.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "1.0");
        // event costs
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "2", "20.00");
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "40.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "1537.10");
        eva.assertOverallCosts("1537.10", "EUR", "1537.10");
    }

    @Test
    public void billingPUMonthScenario01CustomerPriceModel() throws Exception {
        // given
        setup.createMonthScenario01CustomerPriceModel();

        // when
        // two billing runs are needed to get the results for 3 billing periods.
        performBillingRun(0, "2013-01-02 00:00:00");
        performBillingRun(0, "2013-02-02 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO01_PU_MONTH_CUST_PM", 0);

        TestData testData = getTestData("SCENARIO01_PU_MONTH_CUST_PM");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);

        // November 2012
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2012-11-01 00:00:00", "2012-12-01 00:00:00");
        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(), sub
                .getPaymentInfo().getId());

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), sub.getServiceId(), sub.getPriceModel()
                .getKey(), sub.getPriceModel().getType().name());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2012-11-15 12:00:00") + "",
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00") + "");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", "1.0", "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
        eva.assertNullOverallDiscount();

        // December 2012
        eva = getEvaluator(sub.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00");
        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(), sub
                .getPaymentInfo().getId());

        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), sub.getServiceId(), sub.getPriceModel()
                .getKey(), sub.getPriceModel().getType().name());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00") + "");
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", "1.0", "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");
        eva.assertOverallCosts("880.50", "EUR", "880.50");
        eva.assertOverallDiscount("25.0", "293.50", "880.50", "1174.00");

        // January 2013
        eva = getEvaluator(sub.getKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");
        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(), sub
                .getPaymentInfo().getId());

        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), sub.getServiceId(), sub.getPriceModel()
                .getKey(), sub.getPriceModel().getType().name());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-16 12:00:00") + "");
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", "1.0", "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");
        eva.assertOverallCosts("880.50", "EUR", "880.50");
        eva.assertOverallDiscount("25.0", "293.50", "880.50", "1174.00");
    }

    @Test
    public void billingPUMonthScenario01CustomerPriceModel_changeServiceId()
            throws Exception {
        // given
        setup.createMonthScenario01CustomerPriceModel_changeServiceId();

        // when
        // two billing runs are needed to get the results for 3 billing periods.
        performBillingRun(0, "2013-01-02 00:00:00");
        performBillingRun(0, "2013-02-02 00:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "SCENARIO01_PU_MONTH_CUST_PM2", 0);

        TestData testData = getTestData("SCENARIO01_PU_MONTH_CUST_PM2");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails serviceWithChangedId = supplier.getService(1);

        // November 2012
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2012-11-01 00:00:00", "2012-12-01 00:00:00");
        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(), sub
                .getPaymentInfo().getId());

        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), sub.getServiceId(), sub.getPriceModel()
                .getKey(), sub.getPriceModel().getType().name());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2012-11-15 12:00:00") + "",
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00") + "");
        eva.assertOneTimeFee(priceModel, "254.00");
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", "1.0", "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1428.00");
        eva.assertOverallCosts("1428.00", "EUR", "1428.00");
        eva.assertNullOverallDiscount();

        // December 2012
        eva = getEvaluator(sub.getKey(), "2012-12-01 00:00:00",
                "2013-01-01 00:00:00");
        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(), sub
                .getPaymentInfo().getId());

        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), serviceWithChangedId.getServiceId(),
                sub.getPriceModel().getKey(), sub.getPriceModel().getType()
                        .name());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00") + "");
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", "1.0", "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");
        eva.assertOverallCosts("880.50", "EUR", "880.50");
        eva.assertOverallDiscount("25.0", "293.50", "880.50", "1174.00");

        // January 2013
        eva = getEvaluator(sub.getKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");
        eva.assertOrganizationDetails(customer.getOrganizationId(), customer
                .getOrganization().getName(), customer.getOrganization()
                .getAddress(), customer.getOrganization().getEmail(), sub
                .getPaymentInfo().getId());

        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), serviceWithChangedId.getServiceId(),
                sub.getPriceModel().getKey(), sub.getPriceModel().getType()
                        .name());
        eva.assertPriceModelUsagePeriod(priceModel,
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00") + "",
                DateTimeHandling.calculateMillis("2013-01-16 12:00:00") + "");
        eva.assertNullOneTimeFee();
        eva.assertPriceModelPeriodFee(priceModel, PricingPeriod.MONTH.name(),
                "734.00", "1.0", "734.00");

        eva.assertPriceModelUserAssignmentCosts(priceModel,
                PricingPeriod.MONTH.name(), "434.00", "1.0", "1", "434.00");
        eva.assertUserAssignmentCostsFactor(priceModel, customer.getAdminUser()
                .getUserId(), "1.0");
        roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertTotalRoleCosts(roleCosts, "6.00");
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 1.0, "6.00");

        eva.assertPriceModelCosts(priceModel, "EUR", "1174.00");
        eva.assertOverallCosts("880.50", "EUR", "880.50");
        eva.assertOverallDiscount("25.0", "293.50", "880.50", "1174.00");
    }

}
