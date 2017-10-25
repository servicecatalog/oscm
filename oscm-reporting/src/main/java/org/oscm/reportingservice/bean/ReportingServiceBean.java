/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.persistence.Query;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.ReportEngineUrl;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Report;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.reportingservice.business.model.billing.RDOCustomerPaymentPreview;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.business.model.externalservices.RDOExternal;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenue;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReport;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReports;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.intf.ReportingService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ReportType;
import org.oscm.internal.vo.VOReport;

/**
 * Session Bean implementation class ReportingServiceBean
 * 
 */
// Note: For every @WebMethod of this bean needs 'action' attribute have to be
// implemented for SOAP handling of BIRT. (See Bug9994)
@Stateless
@Remote(ReportingService.class)
@WebService(serviceName = "Report", portName = "ReportPort")
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ReportingServiceBean implements ReportingService {

    @EJB
    protected ReportingServiceBeanLocal delegate;

    @EJB(beanInterface = DataService.class)
    protected DataService dataService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizerService;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal configurationService;

    @Override
    public List<VOReport> getAvailableReports(ReportType reportFilterType) {
        return getAvailableReportsInt(reportFilterType);
    }

    @Override
    @RolesAllowed({ "ORGANIZATION_ADMIN", "UNIT_ADMINISTRATOR" })
    public List<VOReport> getAvailableReportsForOrgAdmin(
            ReportType reportFilterType) {
        return getAvailableReportsInt(reportFilterType);
    }

    private List<VOReport> getAvailableReportsInt(ReportType reportFilterType) {
        PlatformUser currentUser = dataService.getCurrentUser();
        Set<OrganizationToRole> orgToRoles = mapReportTypeToRoles(
                currentUser.getOrganization(), reportFilterType);
        List<Report> reports = getReportsByRoles(orgToRoles);
        return convertToVOReports(currentUser, reports);
    }

    Set<OrganizationToRole> mapReportTypeToRoles(Organization organization,
            ReportType reportType) {
        Set<OrganizationToRole> result = new HashSet<>();
        switch (reportType) {
        case ALL:
            result = organization.getGrantedRoles();
            break;
        case NON_CUSTOMER:
            result = new HashSet<>(
                    organization.getGrantedRoles());
            removeOrgToRole(OrganizationRoleType.CUSTOMER, result);
            break;
        }

        return result;
    }

    private void removeOrgToRole(OrganizationRoleType orl,
            Set<OrganizationToRole> result) {
        for (Iterator<OrganizationToRole> i = result.iterator(); i.hasNext();) {
            OrganizationToRole current = i.next();
            if (current.getOrganizationRole().getRoleName() == orl) {
                i.remove();
                break;
            }
        }
    }

    List<Report> getReportsByRoles(Set<OrganizationToRole> roles) {
        List<Report> reports = new ArrayList<>();
        for (OrganizationToRole orgToRole : roles) {
            OrganizationRole role = orgToRole.getOrganizationRole();
            Query query = dataService
                    .createNamedQuery("Report.getAllReportsForRole");
            query.setParameter("role", role);
            ParameterizedTypes.addAll(query.getResultList(), reports,
                    Report.class);
        }
        return reports;
    }

    List<VOReport> convertToVOReports(PlatformUser currentUser,
            List<Report> reports) {
        LocalizerFacade facade = new LocalizerFacade(localizerService,
                currentUser.getLocale());
        return ReportDataAssembler.toVOReportList(reports, getEngineUrl(),
                facade);
    }

    private String getEngineUrl() {
        String url = configurationService
                .getConfigurationSetting(ConfigurationKey.REPORT_ENGINEURL,
                        Configuration.GLOBAL_CONTEXT).getValue();
        url = ReportEngineUrl.replace(
                url,
                ReportEngineUrl.KEY_SOAPENDPOINT,
                configurationService.getConfigurationSetting(
                        ConfigurationKey.REPORT_SOAP_ENDPOINT,
                        Configuration.GLOBAL_CONTEXT).getValue());
        url = ReportEngineUrl.replace(
                url,
                ReportEngineUrl.KEY_WSDLURL,
                configurationService.getConfigurationSetting(
                        ConfigurationKey.REPORT_WSDLURL,
                        Configuration.GLOBAL_CONTEXT).getValue());
        return url;
    }

    @WebMethod(action = "\"\"")
    public VOReportResult getReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "reportId") String reportId) {
        return delegate.getReport(sessionId, reportId);
    }

    @WebMethod(action = "\"\"")
    public RDOExternal getExternalServicesReport(
            @WebParam(name = "sessionId") String sessionId) {
        return delegate.getExternalServicesReport(sessionId);
    }

    @WebMethod(action = "\"\"")
    public RDOCustomerPaymentPreview getCustomerPaymentPreview(
            @WebParam(name = "sessionId") String sessionId) {
        return delegate.getCustomerPaymentPreview(sessionId);
    }

    @WebMethod(action = "\"\"")
    public RDODetailedBilling getBillingDetailsReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "billingKey") long billingKey) {
        return delegate.getBillingDetailsReport(sessionId, billingKey);
    }

    @WebMethod(action = "\"\"")
    public RDOPlatformRevenue getPlatformRevenueReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "from") Date fromTime,
            @WebParam(name = "to") Date toTime) {
        return delegate.getPlatformRevenueReport(sessionId, fromTime, toTime);
    }

    @WebMethod(action = "\"\"")
    public RDOPartnerReport getBrokerRevenueShareReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "month") int month,
            @WebParam(name = "year") int year) {
        return delegate.getBrokerRevenueShareReport(sessionId, month, year);
    }

    @WebMethod(action = "\"\"")
    public RDOPartnerReport getResellerRevenueShareReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "month") int month,
            @WebParam(name = "year") int year) {
        return delegate.getResellerRevenueShareReport(sessionId, month, year);
    }

    @WebMethod(action = "\"\"")
    public RDOPartnerReports getPartnerRevenueShareReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "month") int month,
            @WebParam(name = "year") int year) {
        return delegate.getPartnerRevenueShareReport(sessionId, month, year);
    }

    @WebMethod(action = "\"\"")
    public RDOSupplierRevenueShareReport getSupplierRevenueShareReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "month") int month,
            @WebParam(name = "year") int year) {
        return delegate.getSupplierRevenueShareReport(sessionId, month, year);
    }

    @WebMethod(action = "\"\"")
    public RDOSupplierRevenueShareReports getSuppliersRevenueShareReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "month") int month,
            @WebParam(name = "year") int year) {
        return delegate.getSuppliersRevenueShareReport(sessionId, month, year);
    }

    @WebMethod(action = "\"\"")
    public RDODetailedBilling getBillingDetailsOfASupplierReport(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "billingKey") long billingKey) {
        return delegate.getBillingDetailsOfASupplierReport(sessionId,
                billingKey);
    }

    @WebMethod(action = "\"\"")
    public VOReportResult getReportOfASupplier(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "supplierOrgId") String supplierOrgId,
            @WebParam(name = "reportId") String reportId) {
        return delegate
                .getReportOfASupplier(sessionId, supplierOrgId, reportId);

    }
}
