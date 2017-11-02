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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.PaymentService;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.exception.PSPCommunicationException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.delegates.ServiceLocator;

/**
 * @author weiser
 * 
 */
@ViewScoped
@ManagedBean(name="paymentInfoBean")
//TODO: class should be refactored and creation of payment info extracted, like PaymentInfoEditBean.
public class PaymentInfoBean extends BaseBean implements Serializable {

    // must match org.oscm.psp.data.RegistrationData.Status.Canceled
    private static final String CANCELED = "Canceled";
    // must match org.oscm.psp.data.RegistrationData.Status.Success
    private static final String SUCCESS = "Success";
    // must match org.oscm.psp.data.RegistrationData.Status.Failure
    private static final String FAILURE = "Failure";

    private static final long serialVersionUID = -1511948546844968878L;

    private Comparator<VOPaymentType> paymentTypeComparator = new Comparator<VOPaymentType>() {

        public int compare(VOPaymentType o1, VOPaymentType o2) {
            return o1.getPaymentTypeId().compareTo(o2.getPaymentTypeId());
        }

    };

    private static final String DIALOG_HELP_CREATE = "paymentInfo_create";

    private static final String WINDOW_CLOSED = "window_closed";

    private VOPaymentType selectedPaymentType;
    private VOPaymentInfo paymentInfo;
    private VOPaymentInfo selectedPaymentInfoForSubscription;
    private Set<VOPaymentType> enabledPaymentTypes;
    private List<VOPaymentType> availablePaymentTypes;


    // TODO: refactor it. Redirects to next page for registering payment type.
    private String paymentTypeRegisterPage = "paymentOptionInclude";

    // Contains all payment types a costumer can create in the context of
    // subscribing to a service.
    private List<VOPaymentType> availablePaymentTypesForCreation;

    private List<VOPaymentInfo> allPaymentInfos;
    private List<VOPaymentInfo> paymentInfosForSubscription;

    private String paymentRegistrationLink;
    private String pspResult;
    private Long currentServiceKeyForPayment;

    private Long subscribeToServiceKey;

    @ManagedProperty(value="#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value="#{menuBean}")
    private MenuBean menuBean;

    @ManagedProperty(value="#{paymentAndBillingVisibleBean}")
    private PaymentAndBillingVisibleBean paymentAndBillingVisibleBean;

    /**
     * EJB injected through setters
     */
    private PaymentService paymentService;

    private final String CONTEXT_ACCOUNT = "account";
    private final String CONTEXT_SUBSCRIPTION = "subscription";
    private String context = CONTEXT_ACCOUNT;


    /**
     * Set the the selected payment type for a new payment option created by the
     * administrator.
     * 
     * In distinction to the supplier, the administrator selects from all
     * available payment types.
     * 
     * @param id
     *            - the id of the payment type.
     */
    public void setSelectedPaymentTypeId(String id) {
        Collection<VOPaymentType> types = getAvailablePaymentTypes();
        selectedPaymentType = null;
        for (VOPaymentType voPaymentType : types) {
            if (voPaymentType.getPaymentTypeId().equals(id)) {
                selectedPaymentType = voPaymentType;
            }
        }
        if (selectedPaymentType != null) {
            paymentInfo.setPaymentType(selectedPaymentType);
        }
    }

    /**
     * Return the selected payment type for a new payment option created by the
     * administrator.
     */
    public String getSelectedPaymentTypeId() {
        if (selectedPaymentType != null) {
            return selectedPaymentType.getPaymentTypeId();
        }
        // By default the first is selected.
        return (getPaymentTypeChoice().size() > 0) ? getPaymentTypeChoice()
                .iterator().next().getPaymentTypeId() : "";
    }

    /**
     * Set the context for creation of a new payment type.
     * <p>
     * In context of subscriptions only those types are available which are
     * enabled for the supplier.
     */
    public void setCreationContext(String context) {
        this.context = context;
    }

    /**
     * Return the list of payment types which are available in the given choice
     * context.
     * <p>
     * In context of subscriptions only those types are available which are
     * enabled for the supplier.
     * 
     * @return a list of payment type VOSs.
     */
    public Collection<VOPaymentType> getPaymentTypeChoice() {
        if (context.equals(CONTEXT_SUBSCRIPTION))
            return getAvailablePaymentTypesForCreation();
        return getAvailablePaymentTypes();
    }

    /**
     * Return the dialog header title.
     */
    public String getDialogTitle() {
        return generateHeaderText("", ".title");
    }

    /**
     * Return the dialog header description.
     */
    public String getDialogDescription() {
        String prefix = "";
        if (isMarketplaceSet(getRequest())) {
            prefix = "marketplace.";
        }
        return generateHeaderText(prefix, ".description");
    }

    private String generateHeaderText(String msgPrefix, String msgSuffix) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(msgPrefix).append("payment.new").append(msgSuffix);
        return JSFUtils.getText(buffer.toString(),
                new Object[]{""});
    }

    public String getDialogHelpId() {
            return DIALOG_HELP_CREATE;
    }

    public VOPaymentInfo getPaymentInfo() {
        if (paymentInfo == null) {
            paymentInfo = new VOPaymentInfo();
        }
        return paymentInfo;

    }

    /**
     * Call Payment Service to determine the registration link for displaying
     * the registration dialog of the selected payment type and switch to the
     * payment details page.
     * 
     * @return the outcome
     * @throws SaaSApplicationException
     */
    public String switchToPaymentDetails() throws SaaSApplicationException {
        paymentInfo = getPaymentInfo();
        if (paymentInfo.getPaymentType() != null) {
            try {
                getPaymentRegistrationLink();
                paymentTypeRegisterPage = "paymentTypeInclude";
            } catch (Exception ex) {
                PSPCommunicationException exc = new PSPCommunicationException();
                exc.setMessageKey("ex.PSPProcessingException");
                ExceptionHandler.execute(exc);
            } finally {
                resetCachedPaymentInfo();
            }
        }
        return OUTCOME_NEXT;
    }

    public Long getSubscribeToServiceKey() {
        // we store the service key in the this bean, if the user navigates to
        // another page the service key is LOST (this is not a bug but a
        // feature)
        if (sessionBean.getSubscribeToServiceKey() != null) {
            subscribeToServiceKey = sessionBean.getSubscribeToServiceKey();
            sessionBean.setSubscribeToServiceKey(null);
        }
        return subscribeToServiceKey;
    }

    public void setSubscribeToServiceKey(Long subscribeToServiceKey) {
        this.subscribeToServiceKey = subscribeToServiceKey;
    }

    /**
     * Tries to read the payments that are enabled (intersection of product and
     * customer). The resulting list is sorted by the payment type id. The
     * function returns null in case the serviceKey is not set.
     * 
     * @return the list of enabled payment types
     */
    public Collection<VOPaymentType> getEnabledPaymentTypes() {
        Long serviceKeyForPayment = sessionBean.getServiceKeyForPayment();
        if (serviceKeyForPayment == null) {
            serviceKeyForPayment = sessionBean.getSubscribeToServiceKey();
            if (serviceKeyForPayment != null) {
                sessionBean.setServiceKeyForPayment(serviceKeyForPayment);
            }
        }
        if (enabledPaymentTypes == null
                || serviceKeyForPaymentChanged(serviceKeyForPayment)) {
            currentServiceKeyForPayment = serviceKeyForPayment;
            if (serviceKeyForPayment == null) {
                return null;
            }
            enabledPaymentTypes = new HashSet<>();
            enabledPaymentTypes.addAll(getEnabledPaymentTypes(serviceKeyForPayment, getAccountingService()));
        }
        return enabledPaymentTypes;
    }

    public Collection<VOPaymentType> getEnabledPaymentTypes(Long key, AccountService accountService) {
        TreeSet<VOPaymentType> enabledPaymentTypes = null;
        try{
            enabledPaymentTypes =  new TreeSet<>(paymentTypeComparator);
            enabledPaymentTypes.addAll(accountService
                    .getAvailablePaymentTypesFromOrganization(
                            key));
        } catch (SaaSApplicationException e) {
            ExceptionHandler.execute(e);
        }
        return enabledPaymentTypes;
    }

    /**
     * Returns the list of payment types a customer can define payment
     * information for.
     * 
     * @return a list of payment type VOs.
     */
    public Collection<VOPaymentType> getAvailablePaymentTypes() {
        if (availablePaymentTypes == null) {
            availablePaymentTypes = new ArrayList<VOPaymentType>(getAccountingService()
                    .getAvailablePaymentTypes());
        }
        return availablePaymentTypes;
    }

    /**
     * Returns all payment types for which a costumer can create new payment
     * infos in the context of an subscription. The returned collection reflect
     * all payment types the supplier has enabled for this customer, without
     * INVOICE. The function returns null in case the suppliedId is not set.
     * 
     * @return a list of payment type VOSs.
     */
    public List<VOPaymentType> getAvailablePaymentTypesForCreation() {
        return getAvailablePaymentTypesForCreation(getEnabledPaymentTypes());
    }

    private List<VOPaymentType> getAvailablePaymentTypesForCreation(Collection<VOPaymentType> enabledPaymentTypes) {
        if (availablePaymentTypesForCreation == null) {
            availablePaymentTypesForCreation = new ArrayList<>();
            if (enabledPaymentTypes == null) {
                return null;
            }
            for (VOPaymentType pt : enabledPaymentTypes) {
                if (!pt.getPaymentTypeId().equals("INVOICE")) {
                    availablePaymentTypesForCreation.add(pt);
                }
            }
        }
        return availablePaymentTypesForCreation;
    }

    public List<VOPaymentType> getAvailablePaymentTypesForCreation(long key, AccountService accountService) {
        ArrayList<VOPaymentType> availablePaymentTypesForCreation = new ArrayList<>();
        Collection<VOPaymentType> enabledPaymentTypes = getEnabledPaymentTypes(Long.valueOf(key), accountService);
        for (VOPaymentType pt : enabledPaymentTypes) {
            if (!pt.getPaymentTypeId().equals("INVOICE")) {
                availablePaymentTypesForCreation.add(pt);
            }
        }
        return availablePaymentTypesForCreation;
    }

    /**
     * Return a list of all defined payment informations.
     * 
     * @return a list of all defined payment informations.
     */
    public List<VOPaymentInfo> getPaymentInfos() {
        if (allPaymentInfos == null) {
            allPaymentInfos = getAccountingService()
                    .getPaymentInfosForOrgAdmin();
        }
        return allPaymentInfos;
    }

    public String handlePspResult() {

        String outcome = OUTCOME_PSP_SUCCESS;
        pspResult = getPSPResult();
        if (pspResult == null || pspResult.trim().length() == 0) {
            pspResult = FAILURE;
        }
        if (SUCCESS.equals(pspResult)) {
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_PAYMENT_INFO_SAVED);
        } else if (CANCELED.equals(pspResult)) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    "info.pspregistrationresult.cancel");
            outcome = OUTCOME_PSP_ERROR;
        } else if (FAILURE.equals(pspResult)) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    "error.pspregistrationresult.failure");
            outcome = OUTCOME_PSP_ERROR;
        } else if (WINDOW_CLOSED.equals(pspResult)) {
            outcome = WINDOW_CLOSED;
        }
        paymentRegistrationLink = null;
        pspResult = null;

        if (OUTCOME_PSP_SUCCESS.equals(outcome)) {
            // find the newly created payment info and select it.
            selectedPaymentInfoForSubscription = findCreatedPaymentInfoById(paymentInfo
                    .getId());
        } else {
            resetCachedPaymentInfo();
        }
        // reset page to edit
        paymentInfo = null;
        currentServiceKeyForPayment = null;

        return outcome;
    }

    protected String getPSPResult() {
        return pspResult;
    }

    private VOPaymentInfo findCreatedPaymentInfoById(String displayName) {
        // Reload payment info list and find the newly created one.
        allPaymentInfos = getAccountingService().getPaymentInfos();
        VOPaymentInfo lastPi = null;
        long key = -1;
        for (VOPaymentInfo pi : allPaymentInfos) {
            String id = pi.getId();
            if (key == -1 && id.equals(displayName)) {
                lastPi = pi;
            }
            // If given ID already existed at registration, PI will have been
            // stored with a counter suffix, separated with underscore
            int idx = id.lastIndexOf('_');
            if (idx > 0 && id.length() > idx + 1) {
                id = id.substring(0, idx);
                if (id.equals(displayName) && pi.getVersion() == 0) {
                    String rest = pi.getId().substring(idx + 1);
                    try {
                        // Test if has counter suffix
                        Integer.parseInt(rest);
                    } catch (NumberFormatException nfe) {
                        // Continue, there is no counter
                        continue;
                    }
                    // Take the one with highest key.
                    if (pi.getKey() > key) {
                        lastPi = pi;
                        key = pi.getKey();
                    }

                }
            }

        }
        return lastPi;
    }

    public String getPaymentRegistrationLink() {
        if (paymentRegistrationLink == null) {
            try {
                paymentRegistrationLink = getPaymentService()
                        .determineRegistrationLink(getPaymentInfo());
            } catch (SaaSApplicationException e) {
                ExceptionHandler.execute(e);
            }
        }
        return paymentRegistrationLink;
    }

    public String previous() {
        return "previous" + OUTCOME_SERVICE_SUBSCRIBE;
    }

    public void setPspResult(String pspResult) {
        this.pspResult = pspResult;
    }

    public String getPspResult() {
        return pspResult;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public List<VOPaymentInfo> getPaymentInfosForSubscription() {
        Long serviceKeyForPayment = sessionBean.getServiceKeyForPayment();

        if (paymentInfosForSubscription == null
                || serviceKeyForPaymentChanged(serviceKeyForPayment)) {
            currentServiceKeyForPayment = serviceKeyForPayment;
            paymentInfosForSubscription = new ArrayList<>();
            if (currentServiceKeyForPayment != null) {
                paymentInfosForSubscription = getPaymentInfosForSubscription(serviceKeyForPayment.longValue(), getAccountingService());
            }
        }
        Collections.sort(paymentInfosForSubscription,
                new PaymentInfoComparator());
        return paymentInfosForSubscription;
    }

    public List<VOPaymentInfo> getPaymentInfosForSubscription(long key, AccountService accountService) {
        ArrayList<VOPaymentInfo> paymentInfosForSubscription = new ArrayList<>();
        try {
            Set<VOPaymentType> types;
            types = accountService
                    .getAvailablePaymentTypesFromOrganization(
                            Long.valueOf(key));

            List<VOPaymentInfo> infos = accountService
                    .getPaymentInfos();
            for (VOPaymentInfo info : infos) {
                if (types.contains(info.getPaymentType())) {
                    paymentInfosForSubscription.add(info);
                }
            }
        } catch (SaaSApplicationException e) {
            paymentInfosForSubscription = null;
            ExceptionHandler.execute(e);
        }
        return paymentInfosForSubscription;
    }

    public boolean isPaymentVisible(UserBean userBean, long key, AccountService accountingService) {
        if (!userBean.isLoggedInAndAdmin() && userBean.isLoggedInAndSubscriptionManager()) {
            List<VOPaymentInfo> paymentInfosForSubscription = getPaymentInfosForSubscription(key, accountingService);
            return (paymentInfosForSubscription != null && !paymentInfosForSubscription.isEmpty());
        }

        if (userBean.isLoggedInAndAdmin()) {
            Collection<VOPaymentType> enabledPaymentTypes = getEnabledPaymentTypes(Long.valueOf(key), accountingService);
            return (enabledPaymentTypes != null && !enabledPaymentTypes.isEmpty());
        }

        return false;
    }

    /**
     * Compares the display name of two payment infos.
     */
    private class PaymentInfoComparator implements Comparator<VOPaymentInfo> {
        public int compare(VOPaymentInfo arg0, VOPaymentInfo arg1) {
            return arg0.getId().compareTo(arg1.getId());
        }
    }

    private boolean serviceKeyForPaymentChanged(Long serviceKeyForPayment) {
        boolean changed = false;
        if (currentServiceKeyForPayment == null) {
            changed = serviceKeyForPayment != null;
        } else {
            changed = !currentServiceKeyForPayment.equals(serviceKeyForPayment);
        }
        if (changed) {
            // reset everything that relates to the key that has changed now
            paymentInfosForSubscription = null;
            enabledPaymentTypes = null;
        }
        return changed;
    }

    public VOPaymentInfo getSelectedPaymentInfoForSubscription() {
        return selectedPaymentInfoForSubscription;
    }

    public void setSelectedPaymentInfoForSubscriptionKey(Long key) {
        selectedPaymentInfoForSubscription = null;
        if (paymentInfosForSubscription == null || key == null) {
            return;
        }
        for (VOPaymentInfo info : paymentInfosForSubscription) {
            if (info.getKey() == key.longValue()) {
                selectedPaymentInfoForSubscription = info;
                break;
            }
        }
    }

    public Long getSelectedPaymentInfoForSubscriptionKey() {
        if (selectedPaymentInfoForSubscription == null) {
            return null;
        }
        return Long.valueOf(selectedPaymentInfoForSubscription.getKey());
    }

    public void setSelectedPaymentInfoForSubscriptionKeyReadOnly(
            @SuppressWarnings("unused") Long key) {
    }

    public Long getSelectedPaymentInfoForSubscriptionKeyReadOnly() {
        return getSelectedPaymentInfoForSubscriptionKey();
    }

    public void setSelectedPaymentInfoForSubscription(VOPaymentInfo pi) {
        if (pi != null) {
            // (Re-)read payment informations and set given pi as selected
            // only if its payment type is still available.
            getPaymentInfosForSubscription();
            setSelectedPaymentInfoForSubscriptionKey(Long.valueOf(pi.getKey()));
        } else {
            setSelectedPaymentInfoForSubscriptionKey(null);
        }
    }

    /**
     * Convenience method to access the PSP enum type via EL.
     */
    public final PaymentCollectionType getCollectionTypePSP() {
        return PaymentCollectionType.PAYMENT_SERVICE_PROVIDER;
    }

    /**
     * Clears the cached paymentinfos which forces and refresh of the
     * information on the next access.
     */
    protected void resetCachedPaymentInfo() {
        allPaymentInfos = null;
        allPaymentInfos = null;
        paymentInfosForSubscription = null;
    }

    public void selectedPaymentInfoChanged(ValueChangeEvent event) {
        Long newPaymentInfoKey = (Long) event.getNewValue();
        setSelectedPaymentInfoForSubscriptionKey(newPaymentInfoKey);
    }

    private PaymentService getPaymentService() {
        if (paymentService == null) {
            paymentService = new ServiceLocator().findService(PaymentService.class);
        }
        return paymentService;
    }

    @EJB
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public String getPaymentTypeRegisterPage() {
        return paymentTypeRegisterPage;
    }

    public PaymentAndBillingVisibleBean getPaymentAndBillingVisibleBean() {
        return paymentAndBillingVisibleBean;
    }

    public void setPaymentAndBillingVisibleBean(PaymentAndBillingVisibleBean paymentAndBillingVisibleBean) {
        this.paymentAndBillingVisibleBean = paymentAndBillingVisibleBean;
    }
}
