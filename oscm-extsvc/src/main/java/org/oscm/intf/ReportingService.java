/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-05-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.enumtypes.ReportType;
import org.oscm.vo.VOReport;

/**
 * Remote interface for retrieving report data.
 * 
 */

@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
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
    @WebMethod
    public List<VOReport> getAvailableReports(
            @WebParam(name = "filter") ReportType filter);

}
