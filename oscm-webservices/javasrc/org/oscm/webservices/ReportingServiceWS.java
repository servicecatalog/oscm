/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                                                     
 *                                                                              
 *  Creation Date: 22.06.2010                                                      
 *                                                                              
 *  Completion Time: 22.06.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.webservices;

import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.converter.api.EnumConverter;
import org.oscm.converter.api.VOCollectionConverter;
import org.oscm.intf.ReportingService;
import org.oscm.types.enumtypes.ReportType;
import org.oscm.vo.VOReport;

/**
 * End point facade for WS.
 * 
 * @author Aleh Khomich.
 * 
 */
@WebService(name = "ReportingService", serviceName = "ReportingService", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.intf.ReportingService")
public class ReportingServiceWS implements ReportingService {

    WebServiceLogger WS_LOGGER = new WebServiceLogger(
            LoggerFactory.getLogger(ReportingServiceWS.class));

    org.oscm.internal.intf.ReportingService delegate;
    DataService ds;
    WebServiceContext wsContext;

    @Override
    public List<VOReport> getAvailableReports(ReportType filter) {
        WS_LOGGER.logAccess(wsContext, ds);
        return VOCollectionConverter.convertList(
                delegate.getAvailableReports(EnumConverter.convert(filter,
                        org.oscm.internal.types.enumtypes.ReportType.class)),
                org.oscm.vo.VOReport.class);
    }

}
