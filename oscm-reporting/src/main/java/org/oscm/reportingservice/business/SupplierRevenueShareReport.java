/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReport;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReports;
import org.oscm.reportingservice.dao.PartnerRevenueDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author tokoda
 * 
 */
public class SupplierRevenueShareReport {

    private final DataService ds;

    public SupplierRevenueShareReport(DataService ds) {
        this.ds = ds;
    }

    public RDOSupplierRevenueShareReport buildReport(PlatformUser user,
            int month, int year) throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException, ParseException {
        if (!hasRole(OrganizationRoleType.SUPPLIER, user)) {
            return new RDOSupplierRevenueShareReport();
        }

        Calendar c = initializeCalendar(month, year);
        long periodStart = c.getTimeInMillis();
        c.add(Calendar.MONTH, 1);
        long periodEnd = c.getTimeInMillis();

        PartnerRevenueDao sqlDao = new PartnerRevenueDao(ds);
        sqlDao.executeSinglePartnerQuery(periodStart, periodEnd, user
                .getOrganization().getKey(), OrganizationRoleType.SUPPLIER
                .name().toUpperCase());

        Map<String, String> serviceIdMap = sqlDao
                .readTemplateServiceIdsForSupplier(user.getOrganization()
                        .getOrganizationId(), periodEnd);
        Map<String, String> marketplaceNameMap = sqlDao.readMarketplaceNames(
                new Locale(user.getLocale()), periodEnd);
        Map<String, String> serviceNameMap = sqlDao.readServiceNames(
                new Locale(user.getLocale()), periodEnd);

        SupplierRevenueShareBuilder builder;
        builder = new SupplierRevenueShareBuilder(new Locale(user.getLocale()),
                sqlDao.getReportData(), serviceIdMap, marketplaceNameMap,
                serviceNameMap);
        RDOSupplierRevenueShareReport result = builder.buildSingleReport();
        return result;
    }

    boolean hasRole(OrganizationRoleType role, PlatformUser user) {
        return user == null ? false : user.getOrganization().hasRole(role);
    }

    private Calendar initializeCalendar(int month, int year) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public RDOSupplierRevenueShareReports buildReports(PlatformUser user,
            int month, int year) throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException,
            ParseException {
        if (!hasRole(OrganizationRoleType.PLATFORM_OPERATOR, user)) {
            return new RDOSupplierRevenueShareReports();
        }

        Calendar c = initializeCalendar(month, year);
        long periodStart = c.getTimeInMillis();
        c.add(Calendar.MONTH, 1);
        long periodEnd = c.getTimeInMillis();

        PartnerRevenueDao sqlDao = new PartnerRevenueDao(ds);
        sqlDao.executeSupplierQuery(periodStart, periodEnd);

        Map<String, String> serviceIdMap = sqlDao
                .readTemplateServiceIds(periodEnd);
        Map<String, String> marketplaceNameMap = sqlDao.readMarketplaceNames(
                new Locale(user.getLocale()), periodEnd);
        Map<String, String> serviceNameMap = sqlDao.readServiceNames(
                new Locale(user.getLocale()), periodEnd);
        SupplierRevenueShareBuilder builder;
        builder = new SupplierRevenueShareBuilder(new Locale(user.getLocale()),
                sqlDao.getReportData(), serviceIdMap, marketplaceNameMap,
                serviceNameMap);
        RDOSupplierRevenueShareReports result = builder.buildReports();
        return result;
    }
}
