/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-06                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.intf;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.psp.data.RegistrationData;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.PaymentDataException;

/**
 * Remote interface for handling the data passed by a payment service provider
 * (PSP) after registering payment information for a customer. This interface is
 * invoked via the callback component for the relevant PSP after the PSP
 * specific registration actions have been executed at the PSP side.
 */

@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface PaymentRegistrationService {
    /**
     * Handles the data passed by a PSP after registering payment information
     * for a customer. If the registration at the PSP was successful, the
     * payment information with the PSP specific keys is stored in the platform
     * for future transactions.
     * 
     * @param result
     *            a <code>RegistrationData</code> object with the result of the
     *            registration process at the PSP
     * @return the URL of a page which states whether the registration succeeded
     *         or failed. This is sent back to the PSP where it can be used for
     *         redirecting the client to the page.
     * @throws PaymentDataException
     *             if the payment information passed in the
     *             <code>RegistrationData</code> object is invalid
     * @throws ObjectNotFoundException
     *             if the customer organization passed in the
     *             <code>RegistrationData</code> object is not found
     * @throws OperationNotPermittedException
     *             if the caller is not authorized to modify the payment
     *             information in the platform
     */
    @WebMethod
    String register(@WebParam(name = "result") RegistrationData result)
            throws ObjectNotFoundException, PaymentDataException,
            OperationNotPermittedException;
}
