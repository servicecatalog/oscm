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
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.ReportResultData;
import org.oscm.reportingservice.dao.SubscriptionDao;

/**
 * @author kulle
 * 
 */
public class SupplierBillingReport {

    private final BillingDao billingDao;
    private final SubscriptionDao subscriptionDao;

    public SupplierBillingReport(SubscriptionDao subscriptionDao,
            BillingDao billingDao) {
        this.subscriptionDao = subscriptionDao;
        this.billingDao = billingDao;
    }

    /**
     * Creates the supplier billing report listing all customers of a given
     * supplier and their requested/created billing data.
     */
    public void buildReport(String organizationId, VOReportResult result,
            boolean allSupplierReports) throws XPathExpressionException,
            ParserConfigurationException {
        result.setServerTimeZone(DateConverter.getCurrentTimeZoneAsUTCString());
        List<ReportResultData> reportData;
        if (allSupplierReports) {
            reportData = billingDao
                    .retrieveSupplierBillingBySupplierId(organizationId);
            encrypt(reportData);
        } else {
            reportData = billingDao.retrieveSupplierBillingData(organizationId);
        }

        ReportDataConverter converter = new ReportDataConverter(subscriptionDao);
        converter.convertToXml(reportData, result.getData(),
                Collections.<String, String> emptyMap());
    }

    private void encrypt(List<ReportResultData> reportData) {
        for (ReportResultData reportResultData : reportData) {
            List<String> columnNames = reportResultData.getColumnName();
            if (columnNames.indexOf("resultxml") != -1) {
                int j = columnNames.indexOf("resultxml");
                List<Object> columnValues = reportResultData.getColumnValue();
                String resultXml = (String) columnValues.get(j);
                String encrypted = resultXml
                        .replaceAll(
                                "(?s)<OrganizationDetails>.*</OrganizationDetails>",
                                "<OrganizationDetails><Email>XXXX</Email><Name>XXXX</Name><Address>XXXX</Address></OrganizationDetails>");
                columnValues.set(j, encrypted);
            }
        }
    }

    /**
     * Creates the supplier billing report listing all customers of the supplier
     * and their requested/created billing data.
     */
    public void buildReport(String organizationId, VOReportResult result)
            throws XPathExpressionException, ParserConfigurationException {
        this.buildReport(organizationId, result, false);

    }
}
