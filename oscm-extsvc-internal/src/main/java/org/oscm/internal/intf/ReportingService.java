/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-05-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.ReportType;
import org.oscm.internal.vo.VOReport;

/**
 * Remote interface for retrieving report data.
 * 
 */

@Remote
public interface ReportingService {

    /**
     * Returns a list of reports that are registered in the platform for the
     * role of the calling user's organization.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param filter
     *            a <code>ReportType</code> object specifying the types of
     *            report which are to be included in the list
     * @return a list of <code>VOReport</code> objects containing the data of
     *         the available reports
     */

    public List<VOReport> getAvailableReports(ReportType filter);
    
    /**
     * Returns a list of reports that are registered in the platform for the
     * role of the calling user's organization.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param filter
     *            a <code>ReportType</code> object specifying the types of
     *            report which are to be included in the list
     * @return a list of <code>VOReport</code> objects containing the data of
     *         the available reports
     */
    public List<VOReport> getAvailableReportsForOrgAdmin(ReportType filter);

}
