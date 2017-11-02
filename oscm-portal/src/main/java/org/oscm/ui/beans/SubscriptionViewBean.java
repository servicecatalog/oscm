/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 18.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.common.RolePriceHandler;
import org.oscm.ui.common.SteppedPriceComparator;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalSubscriptionPriceModelCtrl;
import org.oscm.ui.model.BPLazyDataModel;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.ui.model.UdaRow;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;

/**
 * Backing bean for subscription view page
 * 
 */
@ViewScoped
@ManagedBean(name = "subscriptionViewBean")
public class SubscriptionViewBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -7186612015142245566L;


    private List<PricedParameterRow> pricedParameterRows;
    private List<PricedEventRow> pricedEvents;

    @ManagedProperty(value = "#{udaBean}")
    private UdaBean udaBean;

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value = "#{discountBean}")
    private DiscountBean discountBean;

    @ManagedProperty(value = "#{bPLazyDataModel}")
    private BPLazyDataModel model;

    @EJB
    private SubscriptionService subscriptionService;
    @EJB
    private AccountService accountingService;
    @EJB
    private ServiceProvisioningService provisioningService;

    private List<UdaRow> defaultSubscriptionUdaRows;
    private String terminationReason;

    @ManagedProperty(value = "#{externalSubscriptionPriceModelCtrl}")
    private ExternalSubscriptionPriceModelCtrl extSubBean;

    @PostConstruct
    public void getInitialize() {
        initialDefaultUdaRows();
        if (model.getSelectedSubscriptionAndCustomer() != null) {
            loadSelectedSubscriptionDetails();
        }
    }

    public String selectSubscriptionIdAndCustomerId() {
        return loadSelectedSubscription();
    }

    private String loadSelectedSubscription() {
        model.setSelectedSubscriptionAndCustomer(searchPoObjFromList());
        return loadSelectedSubscriptionDetails();
    }

    private POSubscriptionAndCustomer searchPoObjFromList() {
        for (POSubscriptionAndCustomer subscriptionAndCustomer : getSubscriptions()) {
            if (subscriptionAndCustomer.getCustomerId().equals(model.getCustomerId())
                    && subscriptionAndCustomer.getSubscriptionId().equals(
                            model.getSubscriptionId())) {
                return subscriptionAndCustomer;
            }
        }
        return null;
    }

    private List<POSubscriptionAndCustomer> getSubscriptions() {
        return model.getSubscriptions();
    }

    public String loadSelectedSubscriptionDetails() {

        try {
            model.setSelectedSubscription(getSubscriptionService()
                    .getSubscriptionForCustomer(model.getCustomerId(), model.getSubscriptionId()));
            validateSubscriptionState();
            fetchSubcriptionPriceModel();
            sessionBean.setSelectedCustomerId(model.getCustomerId());
            sessionBean.setSelectedSubscriptionId(model.getSubscriptionId());
            loadSelectedSubcriptionUdasRows();
            if (model.getSelectedSubscription() != null) {
                VOService voService = model.getSelectedSubscription().getSubscribedService();
                if (voService.getPriceModel().isExternal()) {
                    String url = getRequest().getServletPath();
                    if (url.endsWith("subscriptionPriceModel.jsf")) {
                        getExtSubBean().reloadPriceModel(voService);
                    } else {
                        getExtSubBean().reloadPriceModelForViewSubscription(voService);
                    }
                }
            }
            return BaseBean.OUTCOME_SUCCESS;
        } catch (SaaSApplicationException e) {
            return errorSubscriptionNotAccessible();
        }
    }

    private void validateSubscriptionState() throws SubscriptionStateException {
        // TODO Implementation should be moved to service layer
        if (model.getSelectedSubscription().getStatus() == SubscriptionStatus.DEACTIVATED
                || model.getSelectedSubscription().getStatus() == SubscriptionStatus.INVALID) {
            throw new SubscriptionStateException();
        }
    }

    private void fetchSubcriptionPriceModel() throws ObjectNotFoundException,
            OperationNotPermittedException {
        Collections.sort(model.getSelectedSubscription().getPriceModel()
                .getSteppedPrices(), new SteppedPriceComparator());
        pricedParameterRows = PricedParameterRow.createPricedParameterRowList(
                model.getSelectedSubscription().getSubscribedService(), false, true, true,
                false, true);
        if (model.getSelectedSubscription().getPriceModel().isChargeable()) {
            VOServiceDetails service = getProvisioningService()
                    .getServiceDetails(
                            model.getSelectedSubscription().getSubscribedService());
            pricedEvents = PricedEventRow.createPricedEventRowList(service);
        }
    }

    private String errorSubscriptionNotAccessible() {
        new UiDelegate().handleError(null, BaseBean.ERROR_SUBSCRIPTION_NOT_ACCESSIBLE, model.getSubscriptionId());
        removePoObjFromList(model.getSelectedSubscriptionAndCustomer());
        return BaseBean.OUTCOME_SUBSCRIPTION_LIST;
    }

    /**
     * Terminate subscription by supplier.
     */
    public String terminateSubscription() {

        VOSubscription selectedSub = getSelectedSubscription();
        if (selectedSub != null) {
            UiDelegate uiDelegate = new UiDelegate();
            try {
                getSubscriptionService().terminateSubscription(selectedSub,
                        terminationReason);
                uiDelegate.showInfoMessage(null, BaseBean.INFO_SUBSCRIPTION_TERMINATED,
                        selectedSub.getSubscriptionId());
            } catch (SaaSApplicationException e) {
                uiDelegate.handleException(e);
                return null;
            }
        }
        terminationReason = null;
        removePoObjFromList(model.getSelectedSubscriptionAndCustomer());
        model.setSelectedSubscription(null);
        return BaseBean.OUTCOME_SUCCESS;
    }

    public String updateSubscription() throws SaaSApplicationException {
        if (model.getSelectedSubscription() != null) {
            List<VOUda> toSave = getEditableSupplierAttributes();
            getAccountingService().saveUdas(toSave);
            new UiDelegate().showInfoMessage(null, BaseBean.INFO_SUBSCRIPTION_SAVED,
                    model.getSelectedSubscription().getSubscriptionId());
        }
        loadSelectedSubcriptionUdasRows();
        return BaseBean.OUTCOME_SUCCESS;
    }

    private List<VOUda> getEditableSupplierAttributes() {
        List<VOUda> toSave = new ArrayList<VOUda>();
        for (UdaRow row : model.getSubscriptionUdaRows()) {
            VOUda uda = row.getUda();
            uda.setTargetObjectKey(model.getSelectedSubscription().getKey());
            // filter the list of UDAs so that only editable UDAs
            // (UdaConfigurationType.SUPPLIER) will passed to the service
            if (uda.getUdaDefinition().getConfigurationType()
                    .equals(UdaConfigurationType.SUPPLIER)) {
                toSave.add(uda);
            }
        }
        return toSave;
    }

    private void loadSelectedSubcriptionUdasRows() {
        if (model.getSelectedSubscription() != null) {
            long key = model.getSelectedSubscription().getKey();
            try {
                model.setSubscriptionUdaRows(udaBean.getSubscriptionUdas(key));
            } catch (SaaSApplicationException e) {
                model.setSubscriptionUdaRows(null);
                new UiDelegate().handleException(e);
            }
        } else {
            model.setSubscriptionUdaRows(defaultSubscriptionUdaRows);
        }
    }

    private void initialDefaultUdaRows() {
        List<VOUdaDefinition> customerUdaDefinitions = udaBean
                .getForType(UdaBean.CUSTOMER_SUBSCRIPTION);
        defaultSubscriptionUdaRows = UdaRow.getUdaRows(customerUdaDefinitions,
                new ArrayList<VOUda>());
    }

    public boolean isAnyUdasEditable() {
        if (model.getSubscriptionUdaRows() != null) {
            for (UdaRow row : model.getSubscriptionUdaRows()) {
                if (row.isInputRendered()) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private void removePoObjFromList(
            POSubscriptionAndCustomer selectedSubscriptionAndCustomer) {
        getSubscriptions().remove(model.getSelectedSubscriptionAndCustomer());
        model.setSelectedSubscriptionAndCustomer(null);
    }

    public List<UdaRow> getSubscriptionUdas() {
        return model.getSubscriptionUdaRows();
    }

    public List<RoleSpecificPrice> getRoleSpecificPrices() {
        return RolePriceHandler
                .determineRolePricesForSubscription(model.getSelectedSubscription());
    }

    public boolean isDirectAccess() {
        if (model.getSelectedSubscription() != null) {
            return model.getSelectedSubscription().getServiceAccessType() == ServiceAccessType.DIRECT;
        }
        return false;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public boolean isParametersWithSteppedPrices() {
        if (model.getSelectedSubscription() == null) {
            return false;
        }
        return SteppedPriceBean
                .isParametersWithSteppedPrices(pricedParameterRows);
    }

    public boolean isPricedEventsWithSteppedPrices() {
        if (model.getSelectedSubscription() == null) {
            return false;
        }
        return SteppedPriceBean.isPricedEventsWithSteppedPrices(pricedEvents);
    }

    public String getSelectedCustomerIdAndName() {
        if (model.getSelectedSubscriptionAndCustomer() != null
                && model.getSelectedSubscriptionAndCustomer().getCustomerName() != null)
            return model.getSelectedSubscriptionAndCustomer().getCustomerName();
        else
            return "";

    }

    public List<PricedParameterRow> getPricedParameterRows() {
        return pricedParameterRows;
    }

    public List<PricedEventRow> getPricedEvents() {
        return pricedEvents;
    }

    public Discount getDiscountForSelectedSubscription() {
        if (model.getSelectedSubscription() == null)
            return null;
        return discountBean.getDiscountForCustomer(model.getCustomerId());
    }

    public int getSubscriptionsListSize() {
        return model.getSubscriptionsListSize();
    }

    public String getSubscriptionId() {
        return model.getSubscriptionId();
    }

    public void setSubscriptionId(String subscriptionId) {
        model.setSubscriptionId(subscriptionId);
    }

    public String getCustomerId() {
        return model.getCustomerId();
    }

    public void setCustomerId(String customerId) {
        model.setCustomerId(customerId);
    }

    public POSubscriptionAndCustomer getSelectedSubscriptionAndCustomer() {
        return model.getSelectedSubscriptionAndCustomer();
    }

    public void setSelectedSubscriptionAndCustomer(
            POSubscriptionAndCustomer selectedSubscriptionAndCustomer) {
        model.setSelectedSubscriptionAndCustomer(selectedSubscriptionAndCustomer);
    }

    public VOSubscriptionDetails getSelectedSubscription() {
        return model.getSelectedSubscription();
    }

    public void setUdaBean(UdaBean udaBean) {
        this.udaBean = udaBean;
    }

    public UdaBean getUdaBean() {
        return udaBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public BPLazyDataModel getModel() {
        return model;
    }

    public void setModel(BPLazyDataModel model) {
        this.model = model;
    }

    public void setDiscountBean(DiscountBean discountBean) {
        this.discountBean = discountBean;
    }


    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

    public void setSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public AccountService getAccountingService() {
        return accountingService;
    }

    public void setAccountingService(AccountService accountingService) {
        this.accountingService = accountingService;
    }

    public ServiceProvisioningService getProvisioningService() {
        return provisioningService;
    }

    public void setProvisioningService(ServiceProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    public Integer getNumberOfAssignedUsers() {
        if(getSelectedSubscriptionAndCustomer() == null) return null;
        return Integer.valueOf(getSelectedSubscription().getUsageLicenses().size());
    }

    /**
     * @return the extSubBean
     */
    public ExternalSubscriptionPriceModelCtrl getExtSubBean() {
        return extSubBean;
    }

    /**
     * @param extSubBean the extSubBean to set
     */
    public void setExtSubBean(ExternalSubscriptionPriceModelCtrl extSubBean) {
        this.extSubBean = extSubBean;
    }
    
}
