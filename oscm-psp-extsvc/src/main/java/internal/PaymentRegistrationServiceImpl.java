/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *******************************************************************************/

package internal;

import javax.jws.WebService;

import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.intf.PaymentRegistrationService;

/**
 * @author afschar
 * 
 */
@WebService(serviceName = "PaymentRegistrationService", targetNamespace = "http://oscm.org/xsd", portName = "PaymentRegistrationServicePort", endpointInterface = "org.oscm.psp.intf.PaymentRegistrationService")
public class PaymentRegistrationServiceImpl implements
        PaymentRegistrationService {

    @Override
    public String register(RegistrationData result) {
        throw new UnsupportedOperationException();
    }

}
