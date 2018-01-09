/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 11.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.GetOrganizationBillingDataSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.test.DateTimeHandling;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author kulle
 * 
 */
@SuppressWarnings("boxing")
public class GetOrganizationBillingDataIT extends BillingIntegrationTestBase {

    private final static long firstOfMarch2013 = 1362092400000L;
    private final static long firstOfApril2013 = 1364767200000L;
    private GetOrganizationBillingDataSetup setup = new GetOrganizationBillingDataSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    private static List<BillingResult> generateBillingForAnyPeriod_march2013(
            final long organizationKey) throws Exception {
        setOffsetForBillingRun(0);
        return runTX(new Callable<List<BillingResult>>() {
            @Override
            public List<BillingResult> call() throws Exception {
                BillingServiceLocal billingService = container
                        .get(BillingServiceLocal.class);
                return billingService.generateBillingForAnyPeriod(
                        firstOfMarch2013, firstOfApril2013, organizationKey);
            }
        });
    }

    private static void setOffsetForBillingRun(final int offsetInDays)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Long offsetInMs = new Long(offsetInDays * 24 * 3600 * 1000L);
                ConfigurationServiceLocal configurationService = container
                        .get(ConfigurationServiceLocal.class);

                ConfigurationSetting config = new ConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT, offsetInMs.toString());
                configurationService.setConfigurationSetting(config);
                return null;
            }
        });
    }

    private Node getPriceModelNode(Document billingResult, long priceModelKey)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult, "//PriceModel[@id='"
                + priceModelKey + "']");
    }

    @Test
    public void perUnit_hour_scenario17() throws Exception {
        // given test data in before block
        setup.createHourScenario17();

        List<BillingResult> allBillingResults = generateBillingForAnyPeriod_march2013(getBasicSetup()
                .getSecondCustomer().getKey());

        // then
        List<BillingResult> billingResults = getBillingResultsForScenario(
                allBillingResults, "SCENARIO17_PERUNIT_HOUR");
        assertEquals(2, billingResults.size());

        // document 1
        VOSubscriptionDetails subscription = getSubscriptionDetails(
                "SCENARIO17_PERUNIT_HOUR", 0);
        BillingResult billingResult = getBillingResult(billingResults,
                DateTimeHandling.calculateMillis("2013-03-07 07:00:00"),
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));
        Document document = XMLConverter.convertToDocument(
                billingResult.getResultXML(), true);
        Node priceModel = getPriceModelNode(document, subscription
                .getPriceModel().getKey());
        BillingResultEvaluator eva = new BillingResultEvaluator(document);
        eva.assertPeriod("2013-03-01 00:00:00", "2013-04-01 00:00:00");
        eva.assertPriceModelCosts(priceModel, "EUR", "29290.00");
        eva.assertPriceModelPeriodFee(priceModel, "HOUR", "60.00", "305.0",
                "18300.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel, "HOUR", "30.00",
                "305.0", "1", "9150.00");
        eva.assertOneTimeFee(priceModel, "10.00");
        eva.assertOverallCosts("29290.00", "EUR", "29290.00");

        // document 2
        billingResult = getBillingResult(billingResults,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        document = XMLConverter.convertToDocument(billingResult.getResultXML(),
                true);
        priceModel = getPriceModelNode(document, subscription.getPriceModel()
                .getKey());
        eva = new BillingResultEvaluator(document);
        eva.assertNullOneTimeFee();
        eva.assertPeriod("2013-03-01 00:00:00", "2013-04-01 00:00:00");

        // factor 287 because of summer/winter time switch
        eva.assertPriceModelPeriodFee(priceModel, "HOUR", "60.00", "287.0",
                "17220.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel, "HOUR", "30.00",
                "287.0", "1", "8610.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "27552.00");
        eva.assertOverallCosts("EUR", "27552.00");
    }

    private List<BillingResult> getBillingResultsForScenario(
            List<BillingResult> billingResults, String scenarioId) {
        List<BillingResult> result = new ArrayList<BillingResult>();

        List<Long> subscriptionKeys = new ArrayList<Long>();
        for (VOSubscriptionDetails d : getSubscriptionDetails(scenarioId)) {
            subscriptionKeys.add(d.getKey());
        }

        for (BillingResult r : billingResults) {
            if (subscriptionKeys.contains(r.getSubscriptionKey())) {
                result.add(r);
            }
        }
        return result;
    }

    @Test
    public void perUnit_day_scenario17() throws Exception {
        // given test data in before block
        setup.createDayScenario17();

        // when
        List<BillingResult> allBillingResults = generateBillingForAnyPeriod_march2013(getBasicSetup()
                .getSecondCustomer().getKey());

        // then
        List<BillingResult> billingResults = getBillingResultsForScenario(
                allBillingResults, "SCENARIO17_PERUNIT_DAY");
        assertEquals(2, billingResults.size());

        // document 1
        VOSubscriptionDetails subscription = getSubscriptionDetails(
                "SCENARIO17_PERUNIT_DAY", 0);
        BillingResult billingResult = getBillingResult(billingResults,
                DateTimeHandling.calculateMillis("2013-03-07 07:00:00"),
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));
        Document document = XMLConverter.convertToDocument(
                billingResult.getResultXML(), true);
        Node priceModel = getPriceModelNode(document, subscription
                .getPriceModel().getKey());
        BillingResultEvaluator eva = new BillingResultEvaluator(document);
        eva.assertPeriod("2013-03-01 00:00:00", "2013-04-01 00:00:00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2438.00");
        eva.assertPriceModelPeriodFee(priceModel, "DAY", "120.00", "13.0",
                "1560.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel, "DAY", "60.00",
                "13.0", "1", "780.00");
        eva.assertOneTimeFee(priceModel, "20.00");
        eva.assertOverallCosts("EUR", "2438.00");

        // document 2
        billingResult = getBillingResult(billingResults,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));
        document = XMLConverter.convertToDocument(billingResult.getResultXML(),
                true);
        priceModel = getPriceModelNode(document, subscription.getPriceModel()
                .getKey());
        eva = new BillingResultEvaluator(document);
        eva.assertNullOneTimeFee();
        eva.assertPeriod("2013-03-01 00:00:00", "2013-04-01 00:00:00");
        eva.assertPriceModelPeriodFee(priceModel, "DAY", "120.00", "12.0",
                "1440.00");
        eva.assertPriceModelUserAssignmentCosts(priceModel, "DAY", "60.00",
                "12.0", "1", "720.00");
        eva.assertPriceModelCosts(priceModel, "EUR", "2232.00");
        eva.assertOverallCosts("EUR", "2232.00");
    }

    private BillingResult getBillingResult(List<BillingResult> billingResults,
            long periodStart, long periodEnd) {
        for (BillingResult result : billingResults) {
            if (result.getPeriodStartTime() == periodStart
                    && result.getPeriodEndTime() == periodEnd) {
                return result;
            }
        }

        return null;
    }
}
