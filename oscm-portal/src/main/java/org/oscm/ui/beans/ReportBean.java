/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpSession;

import org.oscm.ui.model.Report;
import org.oscm.internal.types.enumtypes.ReportType;
import org.oscm.internal.vo.VOReport;

/**
 * Managed bean to read the reports
 * 
 */
@ManagedBean
@ViewScoped
public class ReportBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = 8659503745222854962L;
    private List<Report> reports;
    private String reportBaseUri = "reportBaseUri";
    private String selectedReportUrl = "";

    private Vo2ModelMapper<VOReport, Report> mapper = new Vo2ModelMapper<VOReport, Report>() {
        public Report createModel(VOReport vo) {
            return new Report(vo, getRequest().getSession().getId());
        }
    };

    /**
     * Defines the subset of reports which are returned by the
     * getFilteredReports() if available.
     */
    private final static List<String> reportWhiteList = Arrays.asList(
            "Subscription", "Event", "Customer_BillingDetails",
            "Customer_PaymentPreview");

    public String getReportBaseUri() {
        return reportBaseUri;
    }

    public void setReportBaseUri(String reportBaseUri) {
        this.reportBaseUri = reportBaseUri;
    }

    public List<Report> getReports() {
        if (reports == null) {
            reports = mapper.map(getReportingService().getAvailableReports(
                    ReportType.ALL));
        }
        updateHttpSessionWithReportInformation();
        return reports;
    }

    public List<Report> getReportsForOrgAdmin() {
        if (reports == null) {
            reports = mapper.map(getReportingService()
                    .getAvailableReportsForOrgAdmin(ReportType.ALL));
        }
        updateHttpSessionWithReportInformation();
        return reports;
    }

    public List<Report> getNonCustomerReports() {
        if (reports == null) {
            reports = mapper.map(getReportingService().getAvailableReports(
                    ReportType.NON_CUSTOMER));
        }
        updateHttpSessionWithReportInformation();
        return reports;
    }

    private void updateHttpSessionWithReportInformation() {
        if (reports != null) {
            HttpSession httpSession = getRequest().getSession();
            for (Report report : reports) {
                httpSession.setAttribute(report.getReportName(), report);
            }
        }
    }

    /**
     * This function returns a subset of the whole list of reports. The subset
     * is defined by a whitelist of reports names.
     * 
     * @return a list which contains a subset of all available reports.
     */
    public List<Report> getFilteredReports() {
        List<Report> availableReports = getReports();
        List<Report> filteredReports = new ArrayList<Report>();
        for (Report report : availableReports) {
            if (reportWhiteList.contains(report.getReportName())) {
                filteredReports.add(report);
            }
        }

        Collections.sort(filteredReports, new Comparator<Report>() {
            public int compare(Report o1, Report o2) {
                return o1.getLocalizedReportName().compareTo(
                        o2.getLocalizedReportName());
            }
        });

        return filteredReports;
    }

    public void setSelectedReportUrl(String selectedReportUrl) {
        this.selectedReportUrl = selectedReportUrl;
    }

    public String getSelectedReportUrl() {
        // The sr (=selected report) parameter is passed from the account index
        // site. If a valid parameter is passed it'll trigger the immediate
        // creation of the corresponding report
        String sInx = getRequestParameter("sr");
        if (sInx != null) {
            int inx;
            try {
                inx = Integer.parseInt(sInx);
            } catch (NumberFormatException e) {
                return selectedReportUrl;
            }
            // The account index site shows the filtered list of reports
            List<Report> rp = getFilteredReports();
            if (inx > -1 && inx < rp.size()) {
                selectedReportUrl = rp.get(inx).getExternalReportURL();
            }
        }

        return selectedReportUrl;
    }

    public String initReportUrl() {
        return OUTCOME_SUCCESS;
    }
}
