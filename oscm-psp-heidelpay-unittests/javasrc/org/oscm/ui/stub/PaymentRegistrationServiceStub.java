/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: kulle                                                     
 *                                                                              
 *  Creation Date: 20.10.2011                                                      
 *                                                                              
 *  Completion Time: 20.10.2011                                                
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.stub;

import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.psp.data.RegistrationData;
import org.oscm.psp.intf.PaymentRegistrationService;
import org.oscm.types.exceptions.DomainObjectException.ClassEnum;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.PaymentDataException;

/**
 * @author kulle
 * 
 */
public class PaymentRegistrationServiceStub implements
        PaymentRegistrationService {

    RegistrationData registrationData;

    public String register(RegistrationData result)
            throws ObjectNotFoundException, PaymentDataException,
            OperationNotPermittedException {

        this.registrationData = result;

        if (result.getPaymentInfoKey() == -1) {
            throw new ObjectNotFoundException(ClassEnum.PAYMENT_INFO, "key");
        }
        return result.getStatus().name();
    }

    public RegistrationData getRegistrationData() {
        return registrationData;
    }

    public PaymentInfoType getPaymentOption() {
        if (registrationData.getPaymentInfoKey() == 100) {
            return PaymentInfoType.DIRECT_DEBIT;
        }
        if (registrationData.getPaymentInfoKey() == 200) {
            return PaymentInfoType.CREDIT_CARD;
        }
        return PaymentInfoType.INVOICE;
    }

}
