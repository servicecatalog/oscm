/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.oscm.converter.DateConverter;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;
import org.oscm.reportingservice.dao.UnitDao;

/**
 * @author kulle
 * 
 */
public class CustomerSubscriptionReport {

    private final SubscriptionDao subscriptionDao;
    private final UnitDao unitDao;

    public CustomerSubscriptionReport(SubscriptionDao subscriptionDao,
            UnitDao unitDao) {
        this.subscriptionDao = subscriptionDao;
        this.unitDao = unitDao;
    }

    /**
     * Creates the customer subscription report containing all subscriptions of
     * the customer and its assigned users. In case the calling user has only
     * unit administrator role, the list contains the subscriptions currently
     * assigned to the units he is allowed to administrate.
     */
    public void buildReport(PlatformUser user, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());
        List<ReportResultData> subscriptionReportData = new ArrayList<ReportResultData>();
        if (user.isUnitAdmin() && !user.isOrganizationAdmin()) {
            List<Long> unitAdminKeys = unitDao
                    .retrieveUnitKeysForUnitAdmin(user.getKey());
            subscriptionReportData = subscriptionDao
                    .retrieveSubscriptionReportData(user.getOrganization()
                            .getOrganizationId(), unitAdminKeys);
        } else {
            subscriptionReportData = subscriptionDao
                    .retrieveSubscriptionReportData(user.getOrganization()
                            .getOrganizationId());
        }
        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(subscriptionReportData, result.getData(),
                Collections.<String, String> emptyMap());
    }
}
