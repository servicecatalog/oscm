/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.oscm.converter.DateConverter;
import org.oscm.domobjects.PlatformUser;
import org.oscm.reportingservice.business.model.externalservices.RDOExternal;
import org.oscm.reportingservice.business.model.externalservices.RDOExternalService;
import org.oscm.reportingservice.business.model.externalservices.RDOExternalSupplier;
import org.oscm.reportingservice.dao.ExternalServicesDao;
import org.oscm.reportingservice.dao.ExternalServicesDao.ReportData;
import org.oscm.string.Strings;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;

/**
 * @author kulle
 * 
 */
public class ExternalServicesReport {

    ExternalServicesDao dao;

    public ExternalServicesReport(ExternalServicesDao dao) {
        this.dao = dao;
    }

    public RDOExternal buildReport(PlatformUser user) {
        if (!hasRole(OrganizationRoleType.PLATFORM_OPERATOR, user)) {
            return new RDOExternal();
        }

        dao.executeQuery();
        List<ReportData> reportData = dao.getReportData();

        final RDOExternal container = new RDOExternal();
        container.setServerTimeZone(DateConverter
                .getCurrentTimeZoneAsUTCString());
        final TimeZone timeZoneServer = TimeZone.getDefault();
        int i = 0;
        Object currentServiceId = null;
        container.setEntryNr(i++);
        RDOExternalService currentService = null;
        RDOExternalSupplier currentSupplier = null;
        for (ReportData rd : reportData) {
            if (currentSupplier == null
                    || !currentSupplier.getName().equals(rd.getName())) {
                currentSupplier = new RDOExternalSupplier();
                currentSupplier.setEntryNr(i++);
                currentSupplier.setParentEntryNr(container.getEntryNr());
                currentSupplier.setName(rd.getName());
                currentSupplier.setAddress(rd.getAddress());
                currentSupplier.setCountry(new Locale(user.getLocale(), rd
                        .getCountry()).getDisplayCountry());
                currentSupplier.setPhone(rd.getPhone());
                currentSupplier.setEmail(rd.getEmail());
                container.getExternalSuppliers().add(currentSupplier);
            }
            if (currentService == null || currentServiceId == null
                    || currentService.getEndDate() != null
                    || !currentServiceId.equals(rd.getProductKey())) {
                currentServiceId = rd.getProductKey();
                currentService = new RDOExternalService();
                currentService.setParentEntryNr(currentSupplier.getEntryNr());
                currentService.setEntryNr(i++);
                String productId = rd.getProductId();
                if (!Strings.isEmpty(productId)) {
                    productId = productId.split("#")[0];
                }
                currentService.setServiceName(productId);
                currentSupplier.getExternalServices().add(currentService);
            }
            String date = DateConverter.convertLongToDateTimeFormat(rd
                    .getModdate().getTime(), timeZoneServer,
                    DateConverter.DTP_WITHOUT_MILLIS);
            if (ServiceStatus.ACTIVE.name().equals(rd.getProductStatus())) {
                currentService.setStartDate(date);
            } else if (currentService.getStartDate() != null) {
                currentService.setEndDate(date);
            }

        }
        return container;
    }

    boolean hasRole(OrganizationRoleType role, PlatformUser user) {
        return user == null ? false : user.getOrganization().hasRole(role);
    }

}
