/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 12.12.14 11:38
 *
 * ******************************************************************************
 */

package org.oscm.ui.beans;

import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOBillingContact;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;

/**
 * @author chojnackid
 * 
 */
@ViewScoped
@ManagedBean
public class BillingContactEditBean implements Serializable {

    private static final long serialVersionUID = 4916000016066276273L;

    @ManagedProperty(value = "#{billingContactBean}")
    private BillingContactBean billingContactBean;

    private BaseBean baseBean = new BaseBean();

    /**
     * EJB injected through setters.
     */
    private AccountService accountService;
    private Long selectedBillingContactId;
    private VOBillingContact billingContact;
    private static final String DIALOG_HELP_EDIT = "billingContact_edit";

    public VOBillingContact getBillingContact() {
        return billingContact;
    }

    public String saveBillingContact() throws SaaSApplicationException {
        return billingContactBean.saveBillingContact(billingContact);
    }
    
	public void prepareBillingContactForEdit() {
		if (selectedBillingContactId == null) {
			return;
		}
		for (VOBillingContact voBillingContact : billingContactBean
				.getBillingContacts()) {
			if (selectedBillingContactId.equals(new Long(voBillingContact
					.getKey()))) {
				billingContact = voBillingContact;
				return;
			}
		}
	}

    public void setBillingContactId(long billingContactId) {
        for (VOBillingContact voBillingContact : billingContactBean.getBillingContacts())
        if (voBillingContact.getKey() == billingContactId) {
            billingContact = voBillingContact;
            break;
        }
    }

    public String deleteBillingContact() throws SaaSApplicationException {

        String msgKey;
        try {
            getAccountService().deleteBillingContact(billingContact);
            msgKey = BaseBean.INFO_BILLING_CONTACT_DELETED;
        } catch (ObjectNotFoundException e) {
            // display different message
            msgKey = BaseBean.INFO_BILLING_CONTACT_DELETED_CONCURRENTLY;
        }

        billingContactBean.setBillingContacts(null);
        baseBean.addMessage(null, FacesMessage.SEVERITY_INFO, msgKey);

        return BaseBean.OUTCOME_SUCCESS;
    }

    public String getDialogHelpId() {
        return DIALOG_HELP_EDIT;
    }

    @EJB
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setBillingContactBean(BillingContactBean billingContactBean) {
        this.billingContactBean = billingContactBean;
    }

    private AccountService getAccountService() {
        if (accountService == null) {
            accountService = new ServiceLocator().findService(AccountService.class);
        }
        return accountService;
    }

    public void setBaseBean(BaseBean baseBean) {
        this.baseBean = baseBean;
    }

	public Long getSelectedBillingContactId() {
		return selectedBillingContactId;
	}

	public void setSelectedBillingContactId(Long selectedBillingContactId) {
		this.selectedBillingContactId = selectedBillingContactId;
	}
}
