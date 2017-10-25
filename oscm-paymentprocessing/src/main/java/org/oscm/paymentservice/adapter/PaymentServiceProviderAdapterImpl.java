/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年2月5日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.adapter;

import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.RegistrationLink;
import org.oscm.psp.data.RequestData;
import org.oscm.psp.intf.PaymentServiceProvider;
import org.oscm.types.exceptions.PSPCommunicationException;
import org.oscm.types.exceptions.PSPProcessingException;
import org.oscm.types.exceptions.PaymentDeregistrationException;

/**
 * @author gaowenxin
 * 
 */
public class PaymentServiceProviderAdapterImpl implements
        PaymentServiceProviderAdapter {

    PaymentServiceProvider delegate;

    @Override
    public RegistrationLink determineRegistrationLink(RequestData data)
            throws PSPCommunicationException {
        return delegate.determineRegistrationLink(data);
    }

    @Override
    public RegistrationLink determineReregistrationLink(RequestData data)
            throws PSPCommunicationException {
        return delegate.determineReregistrationLink(data);
    }

    @Override
    public void deregisterPaymentInformation(RequestData data)
            throws PSPCommunicationException, PaymentDeregistrationException {
        delegate.deregisterPaymentInformation(data);
    }

    @Override
    public ChargingResult charge(RequestData data, ChargingData chargingData)
            throws PSPCommunicationException, PSPProcessingException {
        return delegate.charge(data, chargingData);
    }

    @Override
    public void setPaymentServiceProviderService(Object pspInterface) {
        delegate = PaymentServiceProvider.class.cast(pspInterface);
    }

}
