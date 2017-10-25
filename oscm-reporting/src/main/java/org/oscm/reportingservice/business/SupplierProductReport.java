/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
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
public class SupplierProductReport {

    private final SubscriptionDao subscriptionDao;

    public SupplierProductReport(SubscriptionDao subscriptionDao) {
        this.subscriptionDao = subscriptionDao;
    }

    /**
     * Creates the supplier product report listing all subscriptions created for
     * products of the supplier.
     */
    public void buildReport(String organizationId, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());

        List<ReportResultData> reportData = subscriptionDao
                .retrieveSupplierProductReportData(organizationId);
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(reportData, result.getData(),
                Collections.<String, String> emptyMap());
    }

}
