/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.oscm.converter.ReportEngineUrl;
import org.oscm.domobjects.Report;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.vo.VOReport;

/**
 * Assembler to convert user related value objects to the according domain
 * objects and vice versa.
 * 
 * @author Christian Menk
 * 
 */
public class ReportDataAssembler extends BaseAssembler {

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @return A value object reflecting the values of the given domain object.
     */
    public static VOReport toVOReport(Report report, String reportUrlTemplate,
            LocalizerFacade facade) {

        VOReport voReport = new VOReport();
        String reportName = report.getReportName();
        voReport.setReportName(reportName);
        reportUrlTemplate = ReportEngineUrl.replace(reportUrlTemplate,
                ReportEngineUrl.KEY_REPORTNAME, reportName);
        reportUrlTemplate = ReportEngineUrl.replace(reportUrlTemplate,
                ReportEngineUrl.KEY_LOCALE, facade.getLocale());
        voReport.setReportURLTemplate(reportUrlTemplate);
        String localizedReportName = facade.getText(report.getKey(),
                LocalizedObjectTypes.REPORT_DESC);
        voReport.setLocalizedReportName(localizedReportName);

        return voReport;
    }

    /**
     * Converts the given domain object to a value object containing the
     * identifying attributes.
     * 
     * @return A value object reflecting the values of the given domain object.
     */
    public static List<VOReport> toVOReportList(List<Report> reports,
            String reportUrlTemplate, LocalizerFacade facade) {
        List<VOReport> rtv = new ArrayList<VOReport>();
        for (Iterator<Report> iterator = reports.iterator(); iterator.hasNext();) {
            Report report = iterator.next();
            rtv.add(toVOReport(report, reportUrlTemplate, facade));
        }

        return rtv;
    }

}
