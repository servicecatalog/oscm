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

import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.dao.ProviderSupplierDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;

/**
 * @author kulle
 * 
 */
public class ProviderInstanceReport {

    private final ProviderSupplierDao dao;
    private final SubscriptionDao subscriptionDao;

    public ProviderInstanceReport(ProviderSupplierDao dao,
            SubscriptionDao subscriptionDao) {
        this.dao = dao;
        this.subscriptionDao = subscriptionDao;
    }

    /**
     * Creates the technology provider instance report listing the technical
     * products and their existing product instances with their parameter set.
     */
    public void buildReport(String organizationId, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        List<ReportResultData> reportData = dao
                .retrieveProviderInstanceReportData(organizationId);
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(reportData, result.getData(),
                Collections.<String, String> emptyMap());
    }
}
