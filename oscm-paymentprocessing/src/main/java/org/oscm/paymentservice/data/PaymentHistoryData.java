/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 14.10.2011                                                      
 *                                                                              
 *  Completion Time: 14.10.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.data;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.PSPAccountHistory;
import org.oscm.domobjects.PSPHistory;
import org.oscm.domobjects.PSPSettingHistory;
import org.oscm.domobjects.PaymentInfoHistory;
import org.oscm.domobjects.PaymentTypeHistory;

/**
 * Contains the payment processing relevant data.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class PaymentHistoryData {

    private PaymentInfoHistory paymentInfoHistory;
    private PaymentTypeHistory paymentTypeHistory;
    private PSPHistory pspHistory;
    private PSPAccountHistory pspAccountHistory;
    private List<PSPSettingHistory> pspSettingsHistory = new ArrayList<PSPSettingHistory>();

    public void setPaymentInfoHistory(PaymentInfoHistory paymentInfoHistory) {
        this.paymentInfoHistory = paymentInfoHistory;
    }

    public PaymentInfoHistory getPaymentInfoHistory() {
        return paymentInfoHistory;
    }

    public void setPaymentTypeHistory(PaymentTypeHistory paymentTypeHistory) {
        this.paymentTypeHistory = paymentTypeHistory;
    }

    public PaymentTypeHistory getPaymentTypeHistory() {
        return paymentTypeHistory;
    }

    public void setPspHistory(PSPHistory pspHistory) {
        this.pspHistory = pspHistory;
    }

    public PSPHistory getPspHistory() {
        return pspHistory;
    }

    public PSPAccountHistory getPspAccountHistory() {
        return pspAccountHistory;
    }

    public void setPspAccountHistory(PSPAccountHistory pspAccountHistory) {
        this.pspAccountHistory = pspAccountHistory;
    }

    public void setPspSettingsHistory(List<PSPSettingHistory> pspSettingsHistory) {
        this.pspSettingsHistory = pspSettingsHistory;
    }

    public List<PSPSettingHistory> getPspSettingsHistory() {
        return pspSettingsHistory;
    }

}
