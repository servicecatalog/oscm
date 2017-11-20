/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 20.01.2010                                                      
 *                                                                              
 *  Completion Time:  20.01.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.bean;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.paymentservice.local.PaymentServiceLocal;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.vo.VOPaymentData;

/**
 * Test stub of payment processing.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Stateless
@Local(PaymentServiceLocal.class)
public class PaymentServiceStub implements PaymentServiceLocal {

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean chargeCustomer(BillingResult billingResult) {
        return false;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean reinvokePaymentProcessing() {
        return false;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void deregisterPaymentInPSPSystem(PaymentInfo payment) {
        throw new UnsupportedOperationException();
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean chargeForOutstandingBills() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void savePaymentIdentificationForOrganization(
            VOPaymentData paymentData) throws ObjectNotFoundException,
            PaymentDataException {
        throw new UnsupportedOperationException();
    }

}
