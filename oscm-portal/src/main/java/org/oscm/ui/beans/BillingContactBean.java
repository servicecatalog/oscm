/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 05.07.2011                                                      
 *                                                                              
 *  Completion Time: 06.07.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOBillingContact;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name="billingContactBean")
public class BillingContactBean implements Serializable {

    private static final long serialVersionUID = 4916000016066276272L;

    private VOBillingContact billingContact;
    private Set<VOBillingContact> billingContacts;
    private VOBillingContact selectedBillingContact;
    private static final String DIALOG_HELP_CREATE = "billingContact_create";

    /**
     * EJB injected through setters.
     */
    private AccountService accountService;
    
	public void prepareBillingContactForNew() {
		billingContact = new VOBillingContact();
	}

    public VOBillingContact getBillingContact() {
        if (billingContact == null) {
            billingContact = new VOBillingContact();
        }
        return billingContact;
    }

    public String saveBillingContact(VOBillingContact billingContact) throws SaaSApplicationException {

        try {
            this.getAccountService().saveBillingContact(billingContact);
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO, BaseBean.INFO_BILLING_CONTACT_SAVED, null);
        } catch (NonUniqueBusinessKeyException nubke) {
            ExceptionHandler.execute(nubke);
            return null;
        } catch (org.oscm.internal.types.exception.ConcurrentModificationException cme) {
            JSFUtils.addMessage(null,
                    FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_BILLING_CONTACT_MODIFIED_OR_DELETED_CONCURRENTLY,
                    null);
            return BaseBean.OUTCOME_ERROR;
        } finally {
            setBillingContacts(null);
        }
        return BaseBean.OUTCOME_SUCCESS;
    }

    public String saveBillingContact() throws SaaSApplicationException {
        return saveBillingContact(billingContact);
    }

    public void setBillingContactId(long billingContactId)
            throws ObjectNotFoundException {
        billingContact = null;
        if (billingContactId < 0) {
            billingContact = new VOBillingContact();
        }
    }

    /**
     * This method add the "save successful" message to the faces context. The
     * method is used in the context of modal window handling. In contrast to
     * the error message, the "save success" message for a billing contact
     * should be shown on the parent page (not in the modal dialog), so it
     * necessary to read the message before executing a submit.
     */
    public String refreshSaveSuccessMessage() {

        JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO, BaseBean.INFO_BILLING_CONTACT_SAVED, null);

        return BaseBean.OUTCOME_SUCCESS;
    }

    public Collection<VOBillingContact> getBillingContacts() {
        if (billingContacts == null) {
            billingContacts = new TreeSet<>(new BillingContactComparator());
            billingContacts.addAll(getAccountService().getBillingContacts());
        }
        return billingContacts;
    }

    public void setBillingContacts(Set<VOBillingContact> billingContacts) {
        this.billingContacts = billingContacts;
    }

    /**
     * Compares the display name of two billing contacts.
     */
    private class BillingContactComparator implements
            Comparator<VOBillingContact> {
        public int compare(VOBillingContact arg0, VOBillingContact arg1) {
            return arg0.getId().compareTo(arg1.getId());
        }
    }

    public VOBillingContact getSelectedBillingContact() {
        return selectedBillingContact;
    }

    public Long getSelectedBillingContactKey() {
        if (selectedBillingContact == null) {
            return null;
        }
        return Long.valueOf(selectedBillingContact.getKey());
    }

    public void setSelectedBillingContactKey(Long key) {
        selectedBillingContact = null;
        if (billingContacts == null || key == null) {
            return;
        }
        for (VOBillingContact bc : billingContacts) {
            if (bc.getKey() == key.longValue()) {
                selectedBillingContact = bc;
                break;
            }
        }
    }

    public Long getSelectedBillingContactKeyReadOnly() {
        return getSelectedBillingContactKey();
    }

    public void setSelectedBillingContactKeyReadOnly(
            @SuppressWarnings("unused") Long key) {

    }

    public void setSelectedBillingContact(VOBillingContact bc) {
        selectedBillingContact = bc;
    }

    public String getDialogHelpId() {
        return DIALOG_HELP_CREATE;
    }

    public void selectedBillingContactChanged(ValueChangeEvent event) {
        Long newBillingContactKey = (Long) event.getNewValue();
        setSelectedBillingContactKey(newBillingContactKey);
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
}
