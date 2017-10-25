/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;

/**
 * Represents a report.
 * 
 */
public class VOReport implements Serializable {

    private static final long serialVersionUID = -5412324206918451338L;

    /**
     * Retrieves the name of the report.
     * 
     * @return the report name
     */
    public String getReportName() {
        return reportName;
    }

    /**
     * Sets the name of the report.
     * 
     * @param reportName
     *            the report name
     */
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    /**
     * Retrieves the URL template that points to the report in the reporting
     * engine. The template contains a placeholder, <code>${sessionid}</code>,
     * which has to be replaced by the platform session ID of the user who is
     * logged in.
     * 
     * @return the URL template
     */
    public String getReportUrlTemplate() {
        return reportUrlTemplate;
    }

    /**
     * Sets the URL template that points to the report in the reporting engine.
     * The template contains a placeholder, <code>${sessionid}</code>, which has
     * to be replaced by the platform session ID of the user who is logged in.
     * 
     * @param reportURLTemplate
     *            the URL template
     */
    public void setReportURLTemplate(String reportURLTemplate) {
        this.reportUrlTemplate = reportURLTemplate;
    }

    /**
     * The name of the report.
     */
    private String reportName;

    /**
     * The localized name of the report.
     */
    private String localizedReportName;

    /**
     * Retrieves the name of the report in the language set for the calling
     * user.
     * 
     * @return the localized report name
     */
    public String getLocalizedReportName() {
        return localizedReportName;
    }

    /**
     * Specifies the name of the report in the language set for the calling
     * user.
     * 
     * @param localizedReportName
     *            the localized report name
     */
    public void setLocalizedReportName(String localizedReportName) {
        this.localizedReportName = localizedReportName;
    }

    /**
     * The URL template pointing to the report in the reporting engine.
     */
    private String reportUrlTemplate;

}
