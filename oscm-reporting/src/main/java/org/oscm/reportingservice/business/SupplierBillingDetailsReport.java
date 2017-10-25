/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.RDOSubscription;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.BillingDao.ReportBillingData;

/**
 * @author kulle
 * 
 */
public class SupplierBillingDetailsReport {

    private final BillingDao billingDao;

    public SupplierBillingDetailsReport(BillingDao billingDao) {
        this.billingDao = billingDao;
    }

    public RDODetailedBilling buildReport(PlatformUser user,
            long billingResultTkey) throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException, SQLException {
        RDODetailedBilling result = new RDODetailedBilling();

        result.setSummaries(new ArrayList<RDOSummary>());
        result.setSubscriptions(new ArrayList<RDOSubscription>());

        Map<RDOSummary, Document> mapSummaryToResultXml = getBillingData(billingResultTkey);

        List<RDOSummary> summaries = evaluateBillingResult(user,
                mapSummaryToResultXml);

        if (summaries != null && !summaries.isEmpty()) {
            RDOSummary summary = summaries.get(0);
            RDOSubscription rdoSubscription = new RDOSubscription();
            rdoSubscription.setCurrency(summary.getCurrency());
            rdoSubscription.setDiscount(summary.getDiscount());
            rdoSubscription.setDiscountAmount(summary.getDiscountAmount());
            rdoSubscription.setGrossAmount(summary.getGrossAmount());
            rdoSubscription.setAmount(summary.getAmount());
            rdoSubscription.setPon(summary.getPurchaseOrderNumber());
            rdoSubscription.setVat(summary.getVat());
            rdoSubscription.setVatAmount(summary.getVatAmount());
            rdoSubscription.setNetAmountBeforeDiscount(summary
                    .getNetAmountBeforeDiscount());
            rdoSubscription.setSubscriptionId(summary.getSubscriptionId());
            result.getSubscriptions().add(rdoSubscription);
        }
        result.getSummaries().addAll(summaries);

        return result;
    }

    Map<RDOSummary, Document> getBillingData(long billingResultTkey)
            throws ParserConfigurationException, SAXException, IOException {
        List<ReportBillingData> billingDetails = billingDao
                .retrieveBillingDetailsByKey(billingResultTkey);
        Map<RDOSummary, Document> mapSummaryToResultXml = new HashMap<RDOSummary, Document>();
        for (ReportBillingData bd : billingDetails) {
            RDOSummary summaryTemplate = new RDOSummary();
            summaryTemplate.setBillingDate(DateConverter
                    .convertLongToDateTimeFormat(bd.getDate(),
                            TimeZone.getDefault(),
                            DateConverter.DTP_WITHOUT_MILLIS));
            summaryTemplate.setSupplierName(bd.getSupplierName());
            summaryTemplate.setSupplierAddress(bd.getSupplierAddress());
            bd.setBillingResult(encryptBillingResult(bd));
            mapSummaryToResultXml.put(summaryTemplate, XMLConverter
                    .convertToDocument(bd.getBillingResult(), false));
        }
        return mapSummaryToResultXml;
    }

    List<RDOSummary> evaluateBillingResult(PlatformUser user,
            Map<RDOSummary, Document> mapSummaryToResultXml)
            throws XPathExpressionException, SQLException {
        List<RDOSummary> result = new ArrayList<RDOSummary>();
        BillingResultParser brParser = new BillingResultParser(billingDao);
        PriceConverter formatter = new PriceConverter(new Locale(
                user.getLocale()));
        for (RDOSummary summaryTemplate : mapSummaryToResultXml.keySet()) {
            Document details = mapSummaryToResultXml.get(summaryTemplate);
            result.addAll(brParser.evaluateBillingResultForBillingDetails(
                    summaryTemplate, details, user, formatter));
        }
        return result;
    }

    String encryptBillingResult(ReportBillingData bd) {
        String billingResult = bd.getBillingResult();

        String encryptedBillingResult = billingResult.replaceAll(
                "<Email>.*</Email>", "<Email>XXXX</Email>");
        encryptedBillingResult = encryptedBillingResult.replaceAll(
                "<Name>.*</Name>", "<Name>XXXX</Name>");
        encryptedBillingResult = encryptedBillingResult.replaceAll(
                "<Address>.*</Address>", "<Address>XXXX</Address>");

        return encryptedBillingResult;
    }
}
