/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ui.stub;

import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PSPCommunicationException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.vo.VOPaymentData;
import org.oscm.internal.vo.VOPaymentInfo;

public class PaymentServiceStub implements PaymentService {

    private VOPaymentData data = null;

    public void savePaymentIdentificationForOrganization(
            VOPaymentData paymentData) throws ObjectNotFoundException {
        if (paymentData.getPaymentInfoKey() == -1) {
            throw new ObjectNotFoundException(ClassEnum.PAYMENT_INFO, "key");
        }
        this.data = paymentData;
    }

    public PaymentInfoType getPaymentOption() {
        if (data.getPaymentInfoKey() == 100) {
            return PaymentInfoType.DIRECT_DEBIT;
        }
        if (data.getPaymentInfoKey() == 200) {
            return PaymentInfoType.CREDIT_CARD;
        }
        return PaymentInfoType.INVOICE;
    }

    public String determineRegistrationLink(VOPaymentInfo paymentInfo) throws PSPCommunicationException,
            ObjectNotFoundException,
            PaymentDataException, OperationNotPermittedException {
        return null;
    }

    public String determineReregistrationLink(VOPaymentInfo paymentInfo) throws PSPCommunicationException,
            ObjectNotFoundException, OperationNotPermittedException,
            PaymentDataException {
        return null;
    }

    public VOPaymentData getData() {
        return data;
    }

}
