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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * DataContainer for domain object PaymentInfo
 * 
 * @see org.oscm.domobjects.PaymentInfo
 * 
 * @author schmid
 */
@Embeddable
public class ReportData extends DomainDataContainer implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6757652243176438166L;

    /**
     * Name of the report
     */
    @Column(nullable = false)
    private String reportName;

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportName() {
        return reportName;
    }

}
