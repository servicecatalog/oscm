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
import org.oscm.reportingservice.dao.EventDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;

/**
 * @author kulle
 * 
 */
public class ProviderEventReport {

    private final EventDao eventDao;
    private final SubscriptionDao subscriptionDao;

    public ProviderEventReport(SubscriptionDao subscriptionDao,
            EventDao eventDao) {
        this.subscriptionDao = subscriptionDao;
        this.eventDao = eventDao;
    }

    /**
     * Creates the technology provider event report listing the number of events
     * per product instance, product and technical product.
     */
    public void buildReport(String organizationId, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());

        List<ReportResultData> reportData = eventDao
                .retrieveProviderEventReportData(organizationId);
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(reportData, result.getData(),
                Collections.<String, String> emptyMap());
    }

}
