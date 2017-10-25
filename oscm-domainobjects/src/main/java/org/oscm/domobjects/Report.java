/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: schmid                                 
 *                                                                              
 *  Creation Date: 21.01.2009                                                      
 *                                                                              
 *  Completion Time:                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.domobjects.annotations.BusinessKey;
import org.oscm.domobjects.enums.LocalizedObjectTypes;

/**
 * Report holds a name and the URL to a report configured for the current
 * platform instance.
 * 
 * @author menk
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Report.getAllReports", query = "SELECT report FROM Report report ORDER BY report.key ASC"),
        @NamedQuery(name = "Report.getAllReportsForRole", query = "SELECT report FROM Report report WHERE report.organizationRole = :role ORDER BY report.key ASC"),
        @NamedQuery(name = "Report.findByBusinessKey", query = "SELECT report FROM Report report WHERE report.dataContainer.reportName = :reportName") })
@BusinessKey(attributes = { "reportName" })
public class Report extends DomainObjectWithVersioning<ReportData> {

    private static final long serialVersionUID = 1L;

    private static final List<LocalizedObjectTypes> LOCALIZATION_TYPES = Collections
            .unmodifiableList(Arrays.asList(LocalizedObjectTypes.REPORT_DESC));

    public Report() {
        super();
        dataContainer = new ReportData();
    }

    /**
     * the role of the organization this report is visible for
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationRole organizationRole;

    public void setReportName(String reportName) {
        dataContainer.setReportName(reportName);
    }

    public String getReportName() {
        return dataContainer.getReportName();
    }

    public void setOrganizationRole(OrganizationRole organizationRole) {
        this.organizationRole = organizationRole;
    }

    public OrganizationRole getOrganizationRole() {
        return organizationRole;
    }

    @Override
    public List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        return LOCALIZATION_TYPES;
    }

}
