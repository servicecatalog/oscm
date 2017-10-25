/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.intf;

import javax.ejb.Remote;

import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PSPCommunicationException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.vo.VOPaymentData;
import org.oscm.internal.vo.VOPaymentInfo;

/**
 * Remote interface for the payment processing service.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Remote
public interface PaymentService {

    /**
     * Handles all required calls to the payment service provider (PSP) in order
     * to return a URL the customer will be redirected to (or shown in a frame)
     * where the customer can register his payment information with the PSP
     * without communicating through the BES-related sites.
     * 
     * @param paymentInfo
     *            the requested payment information
     * @return the link of the PSP to be used for registration
     * @throws PSPCommunicationException
     *             Thrown in case the communication with the PSP failed.
     * @throws ObjectNotFoundException
     *             Thrown in case the payment type is not found in the system.
     * @throws PaymentDataException
     *             Thrown in case the specified payment type is not valid in the
     *             given context.
     * @throws OperationNotPermittedException
     *             Thrown in case the billing contact does not belong to the
     *             calling organization.
     */
    public String determineRegistrationLink(VOPaymentInfo paymentInfo)
            throws PSPCommunicationException, ObjectNotFoundException,
            PaymentDataException, OperationNotPermittedException;

    /**
     * Handles all required calls to the payment service provider (PSP) in order
     * to return a URL the customer will be redirected to (or shown in a frame)
     * where the customer can re-register his payment information with the PSP
     * without communicating through the BES related sites.
     * 
     * @param paymentInfo
     *            payment info object
     * @return the link of the PSP to be used for re-registration
     * @throws PSPCommunicationException
     *             Thrown in case the communication with the PSP failed.
     * @throws ObjectNotFoundException
     *             Thrown in case there is no payment type object in the system
     *             matching the specified parameter.
     * @throws OperationNotPermittedException
     *             Thrown in case the user is not permitted to set the payment
     *             type.
     * @throws PaymentDataException
     *             Thrown in case the specified payment type is not valid in the
     *             given context.
     */
    public String determineReregistrationLink(VOPaymentInfo paymentInfo)
            throws PSPCommunicationException, ObjectNotFoundException,
            OperationNotPermittedException, PaymentDataException;

    /**
     * 
     * Stores the PSP-related identification ID for the organization
     * 
     * @param paymentData
     *            Holds information about the payment info key, the PSP
     *            identifier, Bank/Credit card name and account number.
     * 
     * @throws ObjectNotFoundException
     *             Thrown if an organization with the given key does not exist.
     * @throws PaymentDataException
     *             Thrown if the requested payment type cannot be created for
     *             the specified organization.
     */
    public void savePaymentIdentificationForOrganization(
            VOPaymentData paymentData) throws ObjectNotFoundException,
            PaymentDataException;
}
