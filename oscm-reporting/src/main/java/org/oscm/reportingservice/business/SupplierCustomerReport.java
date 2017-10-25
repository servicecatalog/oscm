/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oscm.converter.DateConverter;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;

/**
 * @author kulle
 * 
 */
public class SupplierCustomerReport {

    private final SubscriptionDao subscriptionDao;

    public SupplierCustomerReport(SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    /**
     * Creates the supplier customer report listing all customers of a given
     * supplier and their subscriptions.
     */
    public void buildReport(String organizationId, VOReportResult result,
            boolean allSupplierReports) throws XPathExpressionException,
            ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());
        List<ReportResultData> reportData;
        if (allSupplierReports) {
            reportData = subscriptionDao
                    .retrieveSupplierCustomerReportOfASupplierData(organizationId);
        } else {
            reportData = subscriptionDao
                    .retrieveSupplierCustomerReportData(organizationId);
        }
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(reportData, result.getData(),
                Collections.<String, String> emptyMap());
    }

    /**
     * Creates the supplier customer report listing all customers of the
     * supplier and their subscriptions.
     */
    public void buildReport(String organizationId, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        this.buildReport(organizationId, result, false);
    }

}
