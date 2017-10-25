/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oscm.converter.DateConverter;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.dao.PaymentDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;

/**
 * @author kulle
 * 
 */
public class SupplierPaymentReport {

    private final PaymentDao paymentDao;
    private final SubscriptionDao subscriptionDao;

    public SupplierPaymentReport(PaymentDao paymentDao,
            SubscriptionDao subscriptionDao) {
        this.paymentDao = paymentDao;
        this.subscriptionDao = subscriptionDao;
    }

    /**
     * Creates the supplier payment result report containing the status of the
     * interactions (debiting processes) of BES with the payment service
     * provider.
     */
    public void buildReport(long supplierKey, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());

        List<ReportResultData> reportData = paymentDao
                .retrievePaymentInformationData(supplierKey);
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        Map<String, String> columnXPathMap = new HashMap<String, String>();
        columnXPathMap.put("processingresult",
                "/Response/Transaction/Processing/Return/text()");
        converter.convertToXml(reportData, result.getData(), columnXPathMap);
    }

}
