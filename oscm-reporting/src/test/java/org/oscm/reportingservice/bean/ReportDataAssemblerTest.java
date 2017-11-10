/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.bean;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.Report;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.reportingservice.bean.ReportDataAssembler;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.internal.vo.VOReport;

public class ReportDataAssemblerTest {

    private LocalizerFacade facade;

    @Before
    public void setup() {
        facade = new LocalizerFacade(new LocalizerServiceStub() {
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {
                return "Report-" + objectKey;
            }
        }, "en");
    }

    @Test
    public void testToVOReport() {
        final Report report = createReport(123, "ReportName");
        final VOReport voReport = ReportDataAssembler.toVOReport(report,
                "http://reportengine/", facade);
        assertEquals("ReportName", voReport.getReportName());
        assertEquals("Report-123", voReport.getLocalizedReportName());
        assertEquals("http://reportengine/", voReport.getReportUrlTemplate());
    }

    @Test
    public void testToVOReportList() {
        final Report reportA = createReport(123, "ReportA");
        final Report reportB = createReport(456, "ReportB");
        final List<VOReport> voReports = ReportDataAssembler
                .toVOReportList(Arrays.asList(reportA, reportB),
                        "http://reportengine/", facade);

        final VOReport voReportA = voReports.get(0);
        assertEquals("ReportA", voReportA.getReportName());
        assertEquals("Report-123", voReportA.getLocalizedReportName());
        assertEquals("http://reportengine/", voReportA.getReportUrlTemplate());

        final VOReport voReportB = voReports.get(1);
        assertEquals("ReportB", voReportB.getReportName());
        assertEquals("Report-456", voReportB.getLocalizedReportName());
        assertEquals("http://reportengine/", voReportB.getReportUrlTemplate());
    }

    private Report createReport(long id, String name) {
        final Report report = new Report();
        report.setKey(id);
        report.setReportName(name);
        return report;
    }

}
