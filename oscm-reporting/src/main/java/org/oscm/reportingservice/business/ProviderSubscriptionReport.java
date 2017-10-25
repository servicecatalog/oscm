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
import org.oscm.reportingservice.dao.ProviderSupplierDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author kulle
 * 
 */
public class ProviderSubscriptionReport {

    private final ProviderSupplierDao dao;
    private final SubscriptionDao subscriptionDao;

    public ProviderSubscriptionReport(ProviderSupplierDao dao,
            SubscriptionDao subscriptionDao) {
        this.dao = dao;
        this.subscriptionDao = subscriptionDao;
    }

    /**
     * Creates the technology provider subscription report listing all
     * suppliers, their products and the technical products they are based on
     * and the number of subscriptions.
     */
    public void buildReport(String organizationId, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());

        List<ReportResultData> reportData = dao
                .retrieveProviderSubscriptionReportData(organizationId,
                        SubscriptionStatus.ACTIVE.name());
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(reportData, result.getData(),
                Collections.<String, String> emptyMap());
    }
}
