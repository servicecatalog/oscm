/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.11.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.local;

import javax.ejb.Local;
import javax.ejb.TransactionAttributeType;

import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentDeregistrationException;
import org.oscm.internal.vo.VOPaymentData;

/**
 * The local interface for the payment processing.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface PaymentServiceLocal {

    /**
     * Determines all currently non-handled BillingResult objects and, if the
     * customer uses a PSP relevant PaymentInfo, invokes the payment process for
     * it.
     * 
     * @return <code>true</code> in case the charging operations succeeded for
     *         all open bills, <code>false</code> otherwise.
     */
    public boolean chargeForOutstandingBills();

    /**
     * Charges the specified customer according to the content of the provided
     * billing result object.
     * 
     * <p>
     * <b>NOTE:</b> The transaction modifier for this method is
     * {@link TransactionAttributeType#REQUIRES_NEW}
     * </p>
     * 
     * @param billingResult
     *            The billing result the debiting is based on. Value must not be
     *            <code>null</code>.
     * @return <code>true</code> if the processing passed, <code>false</code>
     *         otherwise.
     */
    public boolean chargeCustomer(BillingResult billingResult);

    /**
     * Determines all the payment processing attempts that failed but are marked
     * to be retried. For each of them the payment process is re-invoked using
     * the corresponding billing result object.
     * 
     * @return <code>true</code> in case the operation succeeded for all related
     *         payment result object, <code>false</code> otherwise.
     */
    public boolean reinvokePaymentProcessing();

    /**
     * De-Registers the specified payment information data in the PSP system.
     * 
     * @param payment
     *            The payment which related entry in the PSP system has to be
     *            deleted.
     * @throws PaymentDeregistrationException
     *             Thrown in case the de-registration fails.
     * @throws OperationNotPermittedException
     *             Thrown in case the operation is not permitted.
     */
    public void deregisterPaymentInPSPSystem(PaymentInfo payment)
            throws PaymentDeregistrationException,
            OperationNotPermittedException;

    /**
     * FIXME: Duplicated in local and remote interface
     * 
     * @see PaymentService#avePaymentIdentificationForOrganization(VOPaymentData
     *      paymentData)
     */
    public void savePaymentIdentificationForOrganization(
            VOPaymentData paymentData) throws ObjectNotFoundException,
            PaymentDataException;
}
