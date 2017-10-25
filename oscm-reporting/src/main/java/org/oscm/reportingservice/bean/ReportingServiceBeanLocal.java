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

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.oscm.billingservice.service.BillingServiceLocal;
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
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ReportType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOReport;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.reportingservice.business.BillingDetailsReport;
import org.oscm.reportingservice.business.CustomerEventReport;
import org.oscm.reportingservice.business.CustomerPaymentPreviewReport;
import org.oscm.reportingservice.business.CustomerSubscriptionReport;
import org.oscm.reportingservice.business.ExternalServicesReport;
import org.oscm.reportingservice.business.PartnerReport;
import org.oscm.reportingservice.business.PlatformRevenueReport;
import org.oscm.reportingservice.business.ProviderEventReport;
import org.oscm.reportingservice.business.ProviderInstanceReport;
import org.oscm.reportingservice.business.ProviderSubscriptionReport;
import org.oscm.reportingservice.business.ProviderSupplierReport;
import org.oscm.reportingservice.business.SupplierBillingDetailsReport;
import org.oscm.reportingservice.business.SupplierBillingReport;
import org.oscm.reportingservice.business.SupplierCustomerReport;
import org.oscm.reportingservice.business.SupplierPaymentReport;
import org.oscm.reportingservice.business.SupplierProductReport;
import org.oscm.reportingservice.business.SupplierRevenueShareReport;
import org.oscm.reportingservice.business.model.RDO;
import org.oscm.reportingservice.business.model.billing.RDOCustomerPaymentPreview;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.RDOPaymentPreviewSummary;
import org.oscm.reportingservice.business.model.billing.RDOSummary;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.business.model.externalservices.RDOExternal;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenue;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReport;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReports;
import org.oscm.reportingservice.dao.BillingDao;
import org.oscm.reportingservice.dao.EventDao;
import org.oscm.reportingservice.dao.ExternalServicesDao;
import org.oscm.reportingservice.dao.PaymentDao;
import org.oscm.reportingservice.dao.ProviderSupplierDao;
import org.oscm.reportingservice.dao.SubscriptionDao;
import org.oscm.reportingservice.dao.UnitDao;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

/**
 * ReportingServiceBeanLocal is used to be injected to
 * ReportingServiceSecureBean.
 * 
 */
@LocalBean
@Stateless
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class ReportingServiceBeanLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ReportingServiceBeanLocal.class);

    @EJB(beanInterface = DataService.class)
    protected DataService dataService;

    @EJB(beanInterface = SessionServiceLocal.class)
    protected SessionServiceLocal sessionService;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizerService;

    @EJB(beanInterface = ConfigurationServiceLocal.class)
    protected ConfigurationServiceLocal configurationService;

    @EJB(beanInterface = BillingServiceLocal.class)
    protected BillingServiceLocal billingService;

    @EJB(beanInterface = UserGroupServiceLocalBean.class)
    protected UserGroupServiceLocalBean userGroupService;
    
    private static final String EMPTY = "";
    
    public VOReportResult getReport(String sessionId, String reportId) {

        PlatformUser platformUser = loadUser(sessionId);
        if (platformUser == null) {
            return new VOReportResult();
        }

        Organization organization = platformUser.getOrganization();
        String organizationID = organization.getOrganizationId();
        if (!isReportAvailableForOrganization(organization, reportId)) {
            return new VOReportResult();
        }
        final String cacheId = sessionId + '#' + reportId;
        VOReportResult cachedResult = getFromCache(cacheId,
                VOReportResult.class);
        if (cachedResult != null) {
            return cachedResult;
        }
        try {
            VOReportResult rtv = new VOReportResult();

            if ("Subscription".equals(reportId)) {
                CustomerSubscriptionReport report = new CustomerSubscriptionReport(
                        new SubscriptionDao(dataService),
                        new UnitDao(dataService));
                report.buildReport(platformUser, rtv);
            } else if ("Event".equals(reportId)) {
                CustomerEventReport report = new CustomerEventReport(
                        new EventDao(dataService),
                        new SubscriptionDao(dataService),
                        new UnitDao(dataService));
                report.buildReport(rtv, platformUser,
                        localizerService.getDefaultLocale().getLanguage());
            } else if ("Supplier_Product".equals(reportId)) {
                SupplierProductReport report = new SupplierProductReport(
                        new SubscriptionDao(dataService));
                report.buildReport(organizationID, rtv);
            } else if ("Supplier_Customer".equals(reportId)) {
                SupplierCustomerReport report = new SupplierCustomerReport(
                        new SubscriptionDao(dataService));
                report.buildReport(organizationID, rtv);
            } else if ("Supplier_Billing".equals(reportId)) {
                SupplierBillingReport report = new SupplierBillingReport(
                        new SubscriptionDao(dataService),
                        new BillingDao(dataService));
                report.buildReport(organizationID, rtv);
            } else if ("Provider_Event".equals(reportId)) {
                ProviderEventReport report = new ProviderEventReport(
                        new SubscriptionDao(dataService),
                        new EventDao(dataService));
                report.buildReport(organizationID, rtv);
            } else if ("Provider_Supplier".equals(reportId)) {
                ProviderSupplierReport report = new ProviderSupplierReport(
                        new ProviderSupplierDao(dataService),
                        new SubscriptionDao(dataService));
                report.buildReport(organizationID, rtv);
            } else if ("Provider_Subscription".equals(reportId)) {
                ProviderSubscriptionReport report = new ProviderSubscriptionReport(
                        new ProviderSupplierDao(dataService),
                        new SubscriptionDao(dataService));
                report.buildReport(organizationID, rtv);
            } else if ("Provider_Instance".equals(reportId)) {
                ProviderInstanceReport report = new ProviderInstanceReport(
                        new ProviderSupplierDao(dataService),
                        new SubscriptionDao(dataService));
                report.buildReport(organizationID, rtv);
            } else if ("Supplier_PaymentResultStatus".equals(reportId)) {
                SupplierPaymentReport report = new SupplierPaymentReport(
                        new PaymentDao(dataService),
                        new SubscriptionDao(dataService));
                report.buildReport(organization.getKey(), rtv);
            }

            putToCache(cacheId, rtv);

            return rtv;
        } catch (Exception e) {
            SaaSSystemException ex = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_GENERATE_REPORT_FAILED);
            throw ex;
        }
    }

    private boolean isReportAvailableForOrganization(Organization organization,
            String reportId) {
        List<Report> reports = getReportsByRoles(
                mapReportTypeToRoles(organization, ReportType.ALL));
        for (Report report : reports) {
            if (report.getReportName().equals(reportId)) {
                return true;
            }
        }
        return false;
    }

    public RDOExternal getExternalServicesReport(String sessionId) {
        try {
            PlatformUser user = loadUser(sessionId);
            ExternalServicesReport report = new ExternalServicesReport(
                    new ExternalServicesDao(dataService));
            return report.buildReport(user);
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "external services");
            throw se;
        }
    }

    PlatformUser loadUser(String sessionId) {
        long userKey = sessionService.getPlatformSessionForSessionId(sessionId)
                .getPlatformUserKey();
        return dataService.find(PlatformUser.class, userKey);
    }

    Set<OrganizationToRole> mapReportTypeToRoles(Organization organization,
            ReportType reportType) {
        Set<OrganizationToRole> result = new HashSet<>();
        switch (reportType) {
        case ALL:
            result = organization.getGrantedRoles();
            break;
        case NON_CUSTOMER:
            result = new HashSet<>(organization.getGrantedRoles());
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
                        Configuration.GLOBAL_CONTEXT)
                .getValue();
        url = ReportEngineUrl.replace(url, ReportEngineUrl.KEY_SOAPENDPOINT,
                configurationService.getConfigurationSetting(
                        ConfigurationKey.REPORT_SOAP_ENDPOINT,
                        Configuration.GLOBAL_CONTEXT).getValue());
        url = ReportEngineUrl.replace(url, ReportEngineUrl.KEY_WSDLURL,
                configurationService.getConfigurationSetting(
                        ConfigurationKey.REPORT_WSDLURL,
                        Configuration.GLOBAL_CONTEXT).getValue());
        return url;
    }

    public RDOCustomerPaymentPreview getCustomerPaymentPreview(
            String sessionId) {

        try {
            RDOCustomerPaymentPreview cachedResult = getFromCache(sessionId,
                    RDOCustomerPaymentPreview.class);
            if (cachedResult != null) {
                return cachedResult;
            }
            CustomerPaymentPreviewReport report = new CustomerPaymentPreviewReport(
                    new BillingDao(dataService), new UnitDao(dataService),
                    billingService, userGroupService);

            RDOCustomerPaymentPreview result = report
                    .buildReport(loadUser(sessionId));

            if (!configurationService.isPaymentInfoAvailable()) {
                hidePaymentInfo(result);
            }

            putToCache(sessionId, result);

            return result;
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "customer payment preview");
            throw se;
        }
    }

    @SuppressWarnings("unchecked")
    <T> T getFromCache(String cacheKey, Class<T> type) {
        return (T) ReportingResultCache.get(dataService,
                cacheKey + type.getName());
    }

    <T> void putToCache(String cacheKey, T result) {
        ReportingResultCache.put(dataService,
                cacheKey + result.getClass().getName(),
                System.currentTimeMillis(), result);
    }

    public RDODetailedBilling getBillingDetailsReport(String sessionId,
            long billingKey) {

        PlatformUser user = loadUser(sessionId);
        if (user == null) {
            return new RDODetailedBilling();
        }

        String cacheKey = sessionId + "#" + billingKey;
        RDODetailedBilling cachedResult = getFromCache(cacheKey,
                RDODetailedBilling.class);
        if (cachedResult != null) {
            return cachedResult;
        }

        try {
            BillingDetailsReport billingReport = new BillingDetailsReport(
                    new BillingDao(dataService), new UnitDao(dataService),
                    userGroupService);
            RDODetailedBilling result = billingReport.buildReport(user,
                    billingKey);
            
            if (!configurationService.isPaymentInfoAvailable()) {
                hidePaymentInfo(result);
            }
            
            putToCache(cacheKey, result);

            return result;
        } catch (Exception e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_GENERATE_BILLING_DETAIL_REPORT_FAILED);
            throw sse;
        }

    }

    /**
     * Collects and aggregates the required information for the platform revenue
     * report. The returned transfer object contains all information required by
     * the reporting engine.
     * 
     * @param sessionId
     * @param fromTime
     *            The start date of the billing results to be included in the
     *            report
     * @param toTime
     *            The end date of the billing results to be included in the
     *            report
     * @return RDOPlatformRevenue transfer object
     */
    public RDOPlatformRevenue getPlatformRevenueReport(String sessionId,
            Date fromTime, Date toTime) {

        try {
            PlatformRevenueReport report = new PlatformRevenueReport(
                    dataService);
            return report.buildReport(loadUser(sessionId), fromTime, toTime);
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "broker revenue share");
            throw se;
        }
    }

    public RDOPartnerReport getBrokerRevenueShareReport(String sessionId,
            int month, int year) {
        try {
            PartnerReport partnerReport = new PartnerReport(dataService);
            return partnerReport.buildBrokerReport(loadUser(sessionId), month,
                    year);
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "broker revenue share");
            throw se;
        }
    }

    public RDOPartnerReport getResellerRevenueShareReport(String sessionId,
            int month, int year) {
        try {
            PartnerReport partnerReport = new PartnerReport(dataService);
            return partnerReport.buildResellerReport(loadUser(sessionId), month,
                    year);
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "reseller revenue share");
            throw se;
        }
    }

    public RDOPartnerReports getPartnerRevenueShareReport(String sessionId,
            int month, int year) {
        try {
            PartnerReport partnerReport = new PartnerReport(dataService);
            return partnerReport.buildPartnerReport(loadUser(sessionId), month,
                    year);
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "partner revenue");
            throw se;
        }
    }

    public RDOSupplierRevenueShareReport getSupplierRevenueShareReport(
            String sessionId, int month, int year) {

        try {
            String cacheKey = sessionId + month + year;
            RDOSupplierRevenueShareReport cachedResult = getFromCache(cacheKey,
                    RDOSupplierRevenueShareReport.class);
            if (cachedResult != null) {
                return cachedResult;
            }
            SupplierRevenueShareReport supplierReport = new SupplierRevenueShareReport(
                    dataService);
            RDOSupplierRevenueShareReport result = supplierReport
                    .buildReport(loadUser(sessionId), month, year);
            putToCache(cacheKey, result);

            return result;
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "supplier revenue share");
            throw se;
        }
    }

    public RDOSupplierRevenueShareReports getSuppliersRevenueShareReport(
            String sessionId, int month, int year) {

        try {
            String cacheKey = sessionId + month + year;
            RDOSupplierRevenueShareReports cachedResult = getFromCache(cacheKey,
                    RDOSupplierRevenueShareReports.class);
            if (cachedResult != null) {
                return cachedResult;
            }
            SupplierRevenueShareReport supplierReport = new SupplierRevenueShareReport(
                    dataService);
            RDOSupplierRevenueShareReports result = supplierReport
                    .buildReports(loadUser(sessionId), month, year);
            putToCache(cacheKey, result);

            return result;
        } catch (Exception e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REPORT_FAILED,
                    "suppliers revenue share");
            throw se;
        }
    }

    public RDODetailedBilling getBillingDetailsOfASupplierReport(
            String sessionId, long billingKey) {

        PlatformUser platformUser = loadUser(sessionId);
        if (platformUser == null) {
            return new RDODetailedBilling();
        }

        String cacheKey = sessionId + "#" + billingKey
                + platformUser.getOrganization().getKey();
        RDODetailedBilling cachedResult = getFromCache(cacheKey,
                RDODetailedBilling.class);
        if (cachedResult != null) {
            return cachedResult;
        }

        try {
            SupplierBillingDetailsReport billingReport = new SupplierBillingDetailsReport(
                    new BillingDao(dataService));
            RDODetailedBilling result = billingReport.buildReport(platformUser,
                    billingKey);
            
            if (!configurationService.isPaymentInfoAvailable()) {
                hidePaymentInfo(result);
            }
            
            putToCache(cacheKey, result);

            return result;
        } catch (Exception e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_GENERATE_BILLING_DETAIL_REPORT_FAILED);
            throw sse;
        }
    }

    public VOReportResult getReportOfASupplier(String sessionId,
            String supplierOrgId, String reportId) {

        PlatformUser platformUser = loadUser(sessionId);
        if (platformUser == null) {
            return new VOReportResult();
        }

        if (supplierOrgId == null) {
            return new VOReportResult();
        }
        supplierOrgId = supplierOrgId.trim();
        Organization supplierOrg = new Organization();
        supplierOrg.setOrganizationId(supplierOrgId);
        try {
            supplierOrg = (Organization) dataService
                    .getReferenceByBusinessKey(supplierOrg);
        } catch (ObjectNotFoundException e) {
            return new VOReportResult();
        }
        if (!supplierOrg.hasRole(OrganizationRoleType.SUPPLIER)) {
            return new VOReportResult();
        }

        Organization organization = platformUser.getOrganization();
        if (!isReportAvailableForOrganization(organization, reportId)) {
            return new VOReportResult();
        }

        try {
            VOReportResult rtv = new VOReportResult();

            if ("Supplier_ProductOfASupplier".equals(reportId)) {
                SupplierProductReport report = new SupplierProductReport(
                        new SubscriptionDao(dataService));
                report.buildReport(supplierOrgId, rtv);
            } else if ("Supplier_CustomerOfASupplier".equals(reportId)) {
                SupplierCustomerReport report = new SupplierCustomerReport(
                        new SubscriptionDao(dataService));
                report.buildReport(supplierOrgId, rtv, true);
            } else if ("Supplier_BillingOfASupplier".equals(reportId)) {
                SupplierBillingReport report = new SupplierBillingReport(
                        new SubscriptionDao(dataService),
                        new BillingDao(dataService));
                report.buildReport(supplierOrgId, rtv, true);
            }

            return rtv;
        } catch (Exception e) {
            SaaSSystemException ex = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.ERROR_GENERATE_REPORT_FAILED);
            throw ex;
        }
    }
    
    void hidePaymentInfo(RDO rdo){
        
        List<RDOSummary> summaries = new ArrayList<>();
        
        if(rdo instanceof RDOCustomerPaymentPreview){
            
            RDOCustomerPaymentPreview rdoPaymentPreview = (RDOCustomerPaymentPreview) rdo;  
            summaries.addAll(rdoPaymentPreview.getSummaries());

        } else if(rdo instanceof RDODetailedBilling){
            
            RDODetailedBilling rdoDetailedBilling = (RDODetailedBilling) rdo;
            summaries.addAll(rdoDetailedBilling.getSummaries());
        }
        
        for(RDOSummary summary : summaries){
            summary.setPaymentType(EMPTY);
            summary.setOrganizationAddress(EMPTY);
        }
        
    }
}
