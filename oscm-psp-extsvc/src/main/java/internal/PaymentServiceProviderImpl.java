/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.10.2011                                                      
 *                                                                              
 *******************************************************************************/

package internal;

import javax.jws.WebService;

import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.RegistrationLink;
import org.oscm.psp.data.RequestData;
import org.oscm.psp.intf.PaymentServiceProvider;

/**
 * @author afschar
 * 
 */
@WebService(serviceName = "PaymentServiceProvider", targetNamespace = "http://oscm.org/xsd", portName = "PaymentServiceProviderPort", endpointInterface = "org.oscm.psp.intf.PaymentServiceProvider")
public class PaymentServiceProviderImpl implements PaymentServiceProvider {

    @Override
    public RegistrationLink determineRegistrationLink(RequestData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegistrationLink determineReregistrationLink(RequestData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deregisterPaymentInformation(RequestData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChargingResult charge(RequestData data, ChargingData chargingData) {
        throw new UnsupportedOperationException();
    }

}
