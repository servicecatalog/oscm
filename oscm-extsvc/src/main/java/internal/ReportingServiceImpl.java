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
package internal;

import java.util.List;

import javax.jws.WebService;

import org.oscm.intf.ReportingService;
import org.oscm.types.enumtypes.ReportType;
import org.oscm.vo.VOReport;

/**
 * This is a stub implementation of the {@link ReportingService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Aleh Khomich
 */
@WebService(serviceName = "ReportingService", targetNamespace = "http://oscm.org/xsd", portName = "ReportingServicePort", endpointInterface = "org.oscm.intf.ReportingService")
public class ReportingServiceImpl implements ReportingService {

    @Override
    public List<VOReport> getAvailableReports(ReportType reportFilterType) {
        throw new UnsupportedOperationException();
    }

}
