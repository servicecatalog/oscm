/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 07.12.2011                                                      
 *                                                                              
 *  Completion Time: 07.12.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for payment type selections
 * 
 * @author cheld
 * 
 */
public abstract class PaymentTypes {

    List<SelectedPaymentType> paymentTypes;

    public void setPaymentTypes(List<SelectedPaymentType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public List<SelectedPaymentType> getPaymentTypes() {
        return paymentTypes;
    }

    /**
     * Clones this object. Useful to calculate changes in the user selection.
     * 
     * @return PaymentTypes
     */
    public PaymentTypes duplicate() {
        PaymentTypes copy = newInstance();
        List<SelectedPaymentType> copyOfSelectedPaymentTypes = new ArrayList<SelectedPaymentType>();
        for (SelectedPaymentType selectedPaymentType : this.getPaymentTypes()) {
            SelectedPaymentType copyOfSelectedPaymentType = new SelectedPaymentType(
                    selectedPaymentType.getPaymentType());
            copyOfSelectedPaymentType.setSelected(selectedPaymentType
                    .isSelected());
            copyOfSelectedPaymentTypes.add(copyOfSelectedPaymentType);
        }
        copy.setPaymentTypes(copyOfSelectedPaymentTypes);
        return copy;
    }

    protected abstract PaymentTypes newInstance();

    /**
     * Returns true if the payment selection of this object is identical to the
     * payment selection of the given object.
     * 
     * @param other
     *            the compared object
     * @return boolean
     */
    public boolean isSelectionIdentical(PaymentTypes other) {
        for (SelectedPaymentType paymentType : paymentTypes) {
            for (SelectedPaymentType otherPaymentType : other.paymentTypes) {
                if (paymentType.getPaymentTypeId().equals(
                        otherPaymentType.getPaymentTypeId())
                        && paymentType.isSelected() != otherPaymentType
                                .isSelected()) {
                    return false;
                }
            }
        }
        return true;
    }
}
