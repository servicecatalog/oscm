/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;
import org.oscm.reportingservice.dao.PartnerRevenueDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author kulle
 * 
 */
public class PartnerReport {

    private final DataService ds;

    public PartnerReport(DataService ds) {
        this.ds = ds;
    }

    public RDOPartnerReport buildBrokerReport(PlatformUser user, int month,
            int year) throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException, ParseException {
        return runPartnerReport(user, OrganizationRoleType.BROKER, month, year);
    }

    public RDOPartnerReport buildResellerReport(PlatformUser user, int month,
            int year) throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException, ParseException {
        return runPartnerReport(user, OrganizationRoleType.RESELLER, month,
                year);
    }

    private RDOPartnerReport runPartnerReport(PlatformUser user,
            OrganizationRoleType roleType, int month, int year)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException, ParseException {
        if (!hasRole(roleType, user)) {
            return new RDOPartnerReport();
        }

        Calendar c = initializeCalendar(month, year);
        long periodStart = c.getTimeInMillis();
        c.add(Calendar.MONTH, 1);
        long periodEnd = c.getTimeInMillis();

        PartnerRevenueDao sqlDao = new PartnerRevenueDao(ds);
        sqlDao.executeSinglePartnerQuery(periodStart, periodEnd, user
                .getOrganization().getKey(), roleType.name().toUpperCase());

        PartnerRevenueBuilder builder;
        builder = new PartnerRevenueBuilder(new Locale(user.getLocale()),
                sqlDao.getReportData());
        RDOPartnerReport result = builder.buildSingleReport();
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

    public RDOPartnerReports buildPartnerReport(PlatformUser user, int month,
            int year) throws XPathExpressionException,
            ParserConfigurationException, SAXException, IOException,
            ParseException {
        if (!hasRole(OrganizationRoleType.PLATFORM_OPERATOR, user)) {
            return new RDOPartnerReports();
        }

        Calendar c = initializeCalendar(month, year);
        long periodStart = c.getTimeInMillis();
        c.add(Calendar.MONTH, 1);
        long periodEnd = c.getTimeInMillis();

        PartnerRevenueDao sqlDao = new PartnerRevenueDao(ds);
        sqlDao.executePartnerQuery(periodStart, periodEnd);

        PartnerRevenueBuilder builder;
        builder = new PartnerRevenueBuilder(new Locale(user.getLocale()),
                sqlDao.getReportData());
        RDOPartnerReports result = builder.buildReports();
        return result;
    }
}
