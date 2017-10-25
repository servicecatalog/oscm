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
import org.oscm.reportingservice.dao.ProviderSupplierDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;

/**
 * @author kulle
 * 
 */
public class ProviderSupplierReport {

    private final ProviderSupplierDao dao;
    private final SubscriptionDao subscriptionDao;

    public ProviderSupplierReport(ProviderSupplierDao dao,
            SubscriptionDao subscriptionDao) {
        this.dao = dao;
        this.subscriptionDao = subscriptionDao;
    }

    /**
     * Creates the technology provider supplier report listing all registered
     * suppliers and their created products based on which technical product.
     */
    public void buildReport(String organizationId, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());

        List<ReportResultData> reportData = dao
                .retrieveProviderSupplierReportData(organizationId);
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(reportData, result.getData(),
                Collections.<String, String> emptyMap());
    }

}
