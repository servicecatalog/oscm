/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.stubs;

import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PSPCommunicationException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.vo.VOPaymentData;
import org.oscm.internal.vo.VOPaymentInfo;

public class PaymentServiceStub implements PaymentService, PaymentServiceLocal {

    @Override
    public boolean chargeCustomer(BillingResult billingResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean reinvokePaymentProcessing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String determineRegistrationLink(VOPaymentInfo paymentInfo)
            throws PSPCommunicationException, ObjectNotFoundException,
            PaymentDataException, OperationNotPermittedException {
        return null;
    }

    @Override
    public void savePaymentIdentificationForOrganization(
            VOPaymentData paymentData) throws ObjectNotFoundException,
            PaymentDataException {
    }

    @Override
    public String determineReregistrationLink(VOPaymentInfo paymentInfo)
            throws PSPCommunicationException, ObjectNotFoundException,
            OperationNotPermittedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deregisterPaymentInPSPSystem(PaymentInfo payment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean chargeForOutstandingBills() {
        throw new UnsupportedOperationException();
    }

}
