/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.oscm.converter.DateConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenue;
import org.oscm.reportingservice.dao.PlatformRevenueDao;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * @author kulle
 * 
 */
public class PlatformRevenueReport {

    private final DataService ds;

    public PlatformRevenueReport(DataService ds) {
        this.ds = ds;
    }

    public RDOPlatformRevenue buildReport(PlatformUser user, Date fromTime,
            Date toTime) {
        if (!hasRole(OrganizationRoleType.PLATFORM_OPERATOR, user)) {
            return new RDOPlatformRevenue();
        }
        if (toTime.before(fromTime)) {
            fromTime = new Date();
            toTime = new Date();
        }
        final PlatformRevenueDao sqlResult = new PlatformRevenueDao(ds,
                new Locale(user.getLocale()).getLanguage());
        sqlResult.executeQuery(fromTime, toTime);
        final PlatformRevenueBuilder builder = new PlatformRevenueBuilder(
                sqlResult, new Locale(user.getLocale()));
        final RDOPlatformRevenue result = builder.build();
        result.setAddress(user.getOrganization().getAddress());
        if (user.getOrganization().getDomicileCountryCode() != null) {
            result.setCountry(new Locale(user.getLocale(), user
                    .getOrganization().getDomicileCountryCode())
                    .getDisplayCountry());
        }
        result.setFrom(DateConverter.convertLongToDateTimeFormat(
                fromTime.getTime(), TimeZone.getDefault(),
                DateConverter.DTP_WITHOUT_MILLIS));
        result.setTo(DateConverter.convertLongToDateTimeFormat(
                toTime.getTime(), TimeZone.getDefault(),
                DateConverter.DTP_WITHOUT_MILLIS));
        result.setName(user.getOrganization().getName());
        return result;
    }

    boolean hasRole(OrganizationRoleType role, PlatformUser user) {
        if (user == null) {
            return false;
        }
        Organization org = user.getOrganization();
        return org.hasRole(role);
    }

}
