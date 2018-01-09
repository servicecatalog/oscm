/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 3, 2013                                                      
 *                                                                              
 **************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.billingservice.business.calculation.revenue.setup.PerUnitMonthSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.converter.DateConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.test.DateTimeHandling;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PerUnitMonthIT extends BillingIntegrationTestBase {

    private static VOSubscriptionDetails voSubscriptionDetails;

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);

        new PerUnitMonthSetup().createPerUnitMonthOverlapping();
        voSubscriptionDetails = getSubscriptionDetails(
                "PERUNIT_MONTH_OVERLAPPING", 0);
    }

    @Test
    public void perUnitMonthOverlapping() throws Exception {

        // when
        performBillingRun(0, "2015-04-16 00:00:00");

        // then
        Document billingResult = loadBillingResult(
                voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2015-01-15 00:00:00"),
                DateTimeHandling.calculateMillis("2015-02-15 00:00:00"));

        BillingResultEvaluator eva = new BillingResultEvaluator(billingResult);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "10.00");
        eva.assertOverallCosts("EUR", "10.00");

        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2015-02-15 00:00:00"),
                DateTimeHandling.calculateMillis("2015-03-15 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");

        billingResult = loadBillingResult(voSubscriptionDetails.getKey(),
                DateTimeHandling.calculateMillis("2015-03-15 00:00:00"),
                DateTimeHandling.calculateMillis("2015-04-15 00:00:00"));

        eva = new BillingResultEvaluator(billingResult);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(billingResult,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Period = 4.2.2015 - 16.2.2015 
     */
    public void perUnitMonthOverlapping_AnyPeriod1() throws Exception {

        // when
        List<BillingResult> billingResults = generateBillingForAnyPeriod(
                "2015-02-04 00:00:00", "2015-02-16 00:00:00", getBasicSetup()
                        .getCustomer().getKey());
        printBillingResults(billingResults);

        // then
        assertEquals("Wrong number of billing results", 2,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "10.00");
        eva.assertOverallCosts("EUR", "10.00");

        br = XMLConverter.convertToDocument(billingResults.get(1)
                .getResultXML(), false);
        eva = new BillingResultEvaluator(br);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Period = 6.2.2015 - 14.3.2015 23:59:59 
     */
    public void perUnitMonthOverlapping_AnyPeriod2() throws Exception {

        // when
        List<BillingResult> billingResults = generateBillingForAnyPeriod(
                "2015-02-06 00:00:00", "2015-03-14 23:59:59", getBasicSetup()
                        .getCustomer().getKey());
        printBillingResults(billingResults);

        // then
        assertEquals("Wrong number of billing results", 2,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "0.00");
        eva.assertOverallCosts("EUR", "0.00");

        br = XMLConverter.convertToDocument(billingResults.get(1)
                .getResultXML(), false);
        eva = new BillingResultEvaluator(br);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Period = 6.2.2015 - 15.4.2015 00:00:00 
     */
    public void perUnitMonthOverlapping_AnyPeriod3() throws Exception {

        // when
        List<BillingResult> billingResults = generateBillingForAnyPeriod(
                "2015-02-06 00:00:00", "2015-04-15 00:00:00", getBasicSetup()
                        .getCustomer().getKey());
        printBillingResults(billingResults);

        // then
        assertEquals("Wrong number of billing results", 2,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "0.00");
        eva.assertOverallCosts("EUR", "0.00");

        br = XMLConverter.convertToDocument(billingResults.get(1)
                .getResultXML(), false);
        eva = new BillingResultEvaluator(br);
        priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Period = 11.3.2015 - 15.4.2015 00:00:00 
     */
    public void perUnitMonthOverlapping_AnyPeriod4() throws Exception {

        // when
        List<BillingResult> billingResults = generateBillingForAnyPeriod(
                "2015-03-11 00:00:00", "2015-04-15 00:00:00", getBasicSetup()
                        .getCustomer().getKey());
        printBillingResults(billingResults);

        // then
        assertEquals("Wrong number of billing results", 0,
                billingResults.size());
    }

    @Test
    /**
     * Period = 5.2.2015 00:00:00 - 6.2.2015 00:00:00 
     */
    public void perUnitMonthOverlapping_AnyPeriod5() throws Exception {

        // when
        List<BillingResult> billingResults = generateBillingForAnyPeriod(
                "2015-02-05 00:00:00", "2015-02-06 00:00:00", getBasicSetup()
                        .getCustomer().getKey());
        printBillingResults(billingResults);

        // then
        assertEquals("Wrong number of billing results", 1,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "60.00");
        eva.assertOverallCosts("EUR", "60.00");
    }

    @Test
    /**
     * Period = 15.2.2015 00:00:00 - 16.2.2015 00:00:00 
     */
    public void perUnitMonthOverlapping_AnyPeriod6() throws Exception {

        // when
        List<BillingResult> billingResults = generateBillingForAnyPeriod(
                "2015-02-15 00:00:00", "2015-02-16 00:00:00", getBasicSetup()
                        .getCustomer().getKey());
        printBillingResults(billingResults);

        // then
        assertEquals("Wrong number of billing results", 1,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Period = 1.3.2015 00:00:00 - 2.3.2015 00:00:00 
     */
    public void perUnitMonthOverlapping_AnyPeriod7() throws Exception {

        // when
        List<BillingResult> billingResults = generateBillingForAnyPeriod(
                "2015-03-01 00:00:00", "2015-03-02 00:00:00", getBasicSetup()
                        .getCustomer().getKey());
        printBillingResults(billingResults);

        // then
        assertEquals("Wrong number of billing results", 1,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Preview Start: 5.2.2015 08:00:00 
     */
    public void perUnitMonthOverlapping_PaymentPreview1() throws Exception {

        // when
        BillingRun previewResult = generatePaymentPreviewReport(getBasicSetup()
                .getCustomer().getKey(), "2015-02-05 08:00:00");

        printPaymentPreviewResult(previewResult);

        // then
        List<BillingResult> billingResults = previewResult
                .getBillingResultList();
        assertEquals("Wrong number of billing results", 1,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "60.00");
        eva.assertOverallCosts("EUR", "60.00");
    }

    @Test
    /**
     * Preview Start: 20.2.2015 10:00:00 
     */
    public void perUnitMonthOverlapping_PaymentPreview2() throws Exception {

        // when
        BillingRun previewResult = generatePaymentPreviewReport(getBasicSetup()
                .getCustomer().getKey(), "2015-02-20 10:00:00");

        printPaymentPreviewResult(previewResult);

        // then
        List<BillingResult> billingResults = previewResult
                .getBillingResultList();
        assertEquals("Wrong number of billing results", 1,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Preview Start: 8.3.2015 8:55:55 
     */
    public void perUnitMonthOverlapping_PaymentPreview3() throws Exception {

        // when
        BillingRun previewResult = generatePaymentPreviewReport(getBasicSetup()
                .getCustomer().getKey(), "2015-03-08 08:55:55");

        printPaymentPreviewResult(previewResult);

        // then
        List<BillingResult> billingResults = previewResult
                .getBillingResultList();
        assertEquals("Wrong number of billing results", 1,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Preview Start: 11.3.2015 07:00:00
     */
    public void perUnitMonthOverlapping_PaymentPreview4() throws Exception {

        // when
        BillingRun previewResult = generatePaymentPreviewReport(getBasicSetup()
                .getCustomer().getKey(), "2015-03-11 07:00:00");

        printPaymentPreviewResult(previewResult);

        // then
        List<BillingResult> billingResults = previewResult
                .getBillingResultList();
        assertEquals("Wrong number of billing results", 1,
                billingResults.size());

        Document br = XMLConverter.convertToDocument(billingResults.get(0)
                .getResultXML(), false);
        BillingResultEvaluator eva = new BillingResultEvaluator(br);
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(br,
                voSubscriptionDetails.getPriceModel().getKey());
        eva.assertPriceModelCosts(priceModel, "EUR", "50.00");
        eva.assertOverallCosts("EUR", "50.00");
    }

    @Test
    /**
     * Preview Start: 2.4.2015 07:00:00
     */
    public void perUnitMonthOverlapping_PaymentPreview5() throws Exception {

        // when
        BillingRun previewResult = generatePaymentPreviewReport(getBasicSetup()
                .getCustomer().getKey(), "2015-04-02 07:00:00");

        printPaymentPreviewResult(previewResult);

        // then
        List<BillingResult> billingResults = previewResult
                .getBillingResultList();
        assertEquals("Wrong number of billing results", 0,
                billingResults.size());
    }

    private void printPaymentPreviewResult(BillingRun previewResult) {
        System.out.println("Preview start date: "
                + DateConverter.convertLongToIso8601DateTimeFormat(
                        previewResult.getStart(), Calendar.getInstance()
                                .getTimeZone()));
        System.out.println("Preview end date: "
                + DateConverter.convertLongToIso8601DateTimeFormat(
                        previewResult.getEnd(), Calendar.getInstance()
                                .getTimeZone()));
        printBillingResults(previewResult.getBillingResultList());
    }

    private void printBillingResults(List<BillingResult> billingResults) {
        for (BillingResult br : billingResults) {
            System.out.println(br.getResultXML());
        }
    }
}
