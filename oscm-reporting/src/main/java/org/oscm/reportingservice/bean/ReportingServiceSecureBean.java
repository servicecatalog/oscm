/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.reportingservice.business.model.billing.RDOCustomerPaymentPreview;
import org.oscm.reportingservice.business.model.billing.RDODetailedBilling;
import org.oscm.reportingservice.business.model.billing.VOReportResult;
import org.oscm.reportingservice.business.model.externalservices.RDOExternal;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;
import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;
import org.oscm.reportingservice.business.model.supplierrevenue.RDOPlatformRevenue;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReport;
import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReports;

/**
 * ReportingServiceSecureBean is used to support accessing report by https.
 * 
 */
@Stateless
@WebService(serviceName = "ReportSecure", portName = "ReportSecurePort")
public class ReportingServiceSecureBean {

    @EJB
    protected ReportingServiceBeanLocal delegate;

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
