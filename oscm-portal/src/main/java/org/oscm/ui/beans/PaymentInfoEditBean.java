/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 10.12.14 13:17
 *
 * ******************************************************************************
 */

package org.oscm.ui.beans;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;

import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;

/**
 * @author chojnackid
 * 
 */
@ViewScoped
@ManagedBean
public class PaymentInfoEditBean implements Serializable {

    private static final long serialVersionUID = 7132905490326211605L;
    /**
     * EJB injected through setters.
     */
    private AccountService accountService;

    @ManagedProperty(value = "#{paymentInfoBean}")
    private PaymentInfoBean paymentInfoBean;

    private VOPaymentInfo paymentInfo;

    /**
     * Store the updated payment info of the provided type without handling
     * billing contacts.
     *
     * @return the outcome
     * @throws org.oscm.internal.types.exception.SaaSApplicationException
     */
    public String updatePaymentInfo() {
        String result;
        try {
            paymentInfo = getAccountService().savePaymentInfo(paymentInfo);
            addMessage(BaseBean.INFO_PAYMENT_INFO_SAVED);
            result = BaseBean.OUTCOME_SUCCESS;
        } catch (Exception exc) {
            result = BaseBean.OUTCOME_ERROR;
        } finally {
            paymentInfoBean.resetCachedPaymentInfo();
        }
        return result;
    }

    /**
     * Find and store payment info with given id.
     */
    public void setPaymentInfoId(Long key) {
        for (VOPaymentInfo voPaymentInfo : getAccountService().getPaymentInfos()) {
            if (voPaymentInfo.getKey() == key.longValue()) {
                paymentInfo = voPaymentInfo;
                break;
            }
        }
    }

    public VOPaymentInfo getPaymentInfo() {
        return paymentInfo;

    }
    public String getDialogTitle() {
        return generateHeaderText("", ".title");
    }

    /**
     * Return the dialog header description.
     */
    public String getDialogDescription() {
        String prefix = "";
        if (StringUtils.isNotBlank(BaseBean.getMarketplaceIdStatic())) {
            prefix = "marketplace.";
        }
        return generateHeaderText(prefix, ".description");
    }

    private String generateHeaderText(String msgPrefix, String msgSuffix) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(msgPrefix).append("payment.edit.PaymentOption").append(msgSuffix);
        return JSFUtils.getText(buffer.toString(),
                new Object[]{getPaymentInfoTypeName()});
    }

    private String getPaymentInfoTypeName() {
        String retVal = "";
        if (paymentInfo != null) {
            VOPaymentType paymentType = paymentInfo.getPaymentType();
            retVal = paymentType == null ? ""
                    : paymentType.getName();
        }
        return retVal;
    }

    /**
     * Delete current payment info.
     */
    public String deletePaymentInfo() throws SaaSApplicationException {

        String msgKey = BaseBean.INFO_PAYMENT_INFO_DELETED;
        try {
            getAccountService().deletePaymentInfo(paymentInfo);
        } catch (ObjectNotFoundException e) {
            // display different message
            msgKey = BaseBean.INFO_PAYMENT_INFO_DELETED_CONCURRENTLY;
        } finally {
            paymentInfoBean.resetCachedPaymentInfo();
        }
        paymentInfo = null;
        addMessage(msgKey);

        return BaseBean.OUTCOME_SUCCESS;
    }

    public void addMessage(String msgKey) {
        JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO,
                msgKey, null);
    }

    private AccountService getAccountService() {
        if (accountService == null) {
            accountService = new ServiceLocator().findService(AccountService.class);
        }
        return accountService;
    }

    @EJB
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setPaymentInfoBean(PaymentInfoBean paymentInfoBean) {
        this.paymentInfoBean = paymentInfoBean;
    }
}
