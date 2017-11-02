/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOPaymentType;

/**
 * @author weiser
 * 
 */
public class SelectedPaymentType {

    private final VOPaymentType paymentType;
    private boolean selected;

    public SelectedPaymentType(VOPaymentType type) {
        paymentType = type;
    }

    public String getPaymentTypeId() {
        return getPaymentType().getPaymentTypeId();
    }

    public String getPaymentTypeIdWithoutBlanks() {
        String s = getPaymentTypeId();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')
                    && (c < '0' || c > '9') && c != '_') {
                s = s.replace(c, '_');
            }
        }
        return s;
    }

    public void setPaymentTypeId(String paymentTypeId) {
        getPaymentType().setPaymentTypeId(paymentTypeId);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public VOPaymentType getPaymentType() {
        return paymentType;
    }

}
