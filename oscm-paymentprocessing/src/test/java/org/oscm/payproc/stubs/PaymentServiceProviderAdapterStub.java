/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                   
 *                                                                              
 *  Creation Date: 17.10.2011                                                      
 *                                                                              
 *  Completion Time: 17.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.payproc.stubs;

import java.net.URL;

import org.oscm.paymentservice.adapter.PaymentServiceProviderAdapter;
import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.PaymentProcessingStatus;
import org.oscm.psp.data.RegistrationLink;
import org.oscm.psp.data.RequestData;
import org.oscm.types.exceptions.PSPCommunicationException;
import org.oscm.types.exceptions.PSPProcessingException;
import org.oscm.types.exceptions.PaymentDeregistrationException;

/**
 * @author kulle
 * 
 */
public class PaymentServiceProviderAdapterStub implements
        PaymentServiceProviderAdapter {

    private RequestData requestData;
    private ChargingData chargingData;

    public RegistrationLink determineReregistrationLink(RequestData data)
            throws PSPCommunicationException {
        this.requestData = data;
        final RegistrationLink link = new RegistrationLink();
        link.setUrl("http://www.fujitsu.com");
        link.setBrowserTarget("paypal".equalsIgnoreCase(data.getPspIdentifier()) ? "_blank"
                : "");
        return link;
    }

    public RegistrationLink determineRegistrationLink(RequestData data)
            throws PSPCommunicationException {
        this.requestData = data;
        final RegistrationLink link = new RegistrationLink();
        link.setUrl("POST.VALIDATION=ACK&FRONTEND.REDIRECT_URL=https%3A%2F%2Ftest.ctpe.net%2Ffrontend%2FstartFrontend.prc%3Bjsessionid%3D811C209B1A370839FB118EF1EDE5B050.sapp01&P3.VALIDATION=ACK");
        link.setBrowserTarget("paypal".equalsIgnoreCase(data.getPspIdentifier()) ? "_blank"
                : "");
        return link;
    }

    public void deregisterPaymentInformation(RequestData data)
            throws PSPCommunicationException, PaymentDeregistrationException {
        this.requestData = data;
        if (data.getPaymentInfoKey() == null
                || data.getPaymentInfoKey().longValue() < 0) {
            throw new PaymentDeregistrationException();
        }
    }

    public ChargingResult charge(RequestData data, ChargingData chargingData)
            throws PSPCommunicationException, PSPProcessingException {
        this.requestData = data;
        this.chargingData = chargingData;
        final ChargingResult result = new ChargingResult();
        result.setProcessingStatus(PaymentProcessingStatus.SUCCESS);
        return result;
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public ChargingData getChargingData() {
        return chargingData;
    }

    public URL getLocalWSDL() {
        return null;
    }

    public void setPaymentServiceProviderService(Object pspInterface) {
    }

}
