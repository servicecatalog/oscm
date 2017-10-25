/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-06                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.intf;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.oscm.psp.data.ChargingData;
import org.oscm.psp.data.ChargingResult;
import org.oscm.psp.data.RegistrationLink;
import org.oscm.psp.data.RequestData;
import org.oscm.types.exceptions.PSPCommunicationException;
import org.oscm.types.exceptions.PSPProcessingException;
import org.oscm.types.exceptions.PaymentDeregistrationException;

/**
 * Interface defining the methods which must be implemented by a PSP integration
 * adapter. A PSP integration adapter is required for every payment service
 * provider (PSP) that is to be connected to the platform.
 * 
 */
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface PaymentServiceProvider {
    /**
     * Returns the link to the registration page of the PSP. This page is opened
     * by the platform for a customer to specify new payment information data.
     * 
     * @param data
     *            a <code>RequestData</code> object with the platform data
     *            required for the registration
     * @return the link to the PSP registration page
     * @throws PSPCommunicationException
     *             if the communication with the PSP fails
     */
    @WebMethod
    RegistrationLink determineRegistrationLink(RequestData data)
            throws PSPCommunicationException;

    /**
     * Returns the link to the re-registration page of the PSP. This page is
     * opened by the platform for a customer to update existing payment
     * information data.
     * 
     * @param data
     *            a <code>RequestData</code> object with the platform data
     *            required for the update
     * @return the link to the PSP re-registration page
     * @throws PSPCommunicationException
     *             if the communication with the PSP fails
     */
    RegistrationLink determineReregistrationLink(RequestData data)
            throws PSPCommunicationException;

    /**
     * Deregisters a customer's payment information at the PSP after it has been
     * removed in the platform.
     * 
     * @param data
     *            a <code>RequestData</code> object with the platform data
     *            required for the deregistration
     * @throws PSPCommunicationException
     *             if the communication with the PSP fails
     * @throws PaymentDeregistrationException
     *             if the removal of the payment information fails
     */
    void deregisterPaymentInformation(RequestData data)
            throws PSPCommunicationException, PaymentDeregistrationException;

    /**
     * Triggers the PSP to charge a customer.
     * 
     * @param data
     *            a <code>RequestData</code> object with the platform's basic
     *            data of the customer
     * @param chargingData
     *            a <code>ChargingData</code> object with the platform's billing
     *            data to be used by the PSP
     * @return the result and status of the payment processing
     * @throws PSPCommunicationException
     *             if the communication with the PSP fails
     * @throws PSPProcessingException
     *             if the payment processing fails
     */
    ChargingResult charge(RequestData data, ChargingData chargingData)
            throws PSPCommunicationException, PSPProcessingException;
}
