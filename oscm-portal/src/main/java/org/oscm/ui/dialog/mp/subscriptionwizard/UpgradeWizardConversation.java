/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 27.01.15 10:07
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.subscriptionwizard;

import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_EXTERNAL_TOOL_COMMUNICATION;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_INVALID_CONFIGURED_PARAMETERS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_SUBSCRIPTION_NOT_ACCESSIBLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_UPGRADED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_UPGRADED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_ENTER_PAYMENT;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_ERROR;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_PREVIOUS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_PROCESS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SERVICE_UPGRADE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.VALIDATION_ERROR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.triggerprocess.TriggerProcessesService;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOSteppedPrice;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.json.JsonConverter;
import org.oscm.json.JsonParameterValidator;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.BillingContactBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.PaymentAndBillingVisibleBean;
import org.oscm.ui.beans.PaymentInfoBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.RolePriceHandler;
import org.oscm.ui.common.SteppedPriceComparator;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.PriceModel;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.model.User;

/**
 * Created by ChojnackiD on 2015-01-27.
 */
@Named
@ConversationScoped
public class UpgradeWizardConversation implements Serializable {

    private static final long serialVersionUID = 4482833283708562940L;
    private long TIMEOUT = 300000L;
    private final JsonConverter jsonConverter;
    private final JsonParameterValidator jsonValidator;
    private UiDelegate ui;

    @Inject
    private Conversation conversation;

    @Inject
    private UpgradeWizardModel model;

    @Inject
    private UserBean userBean;

    @Inject
    private SessionBean sessionBean;

    @Inject
    private MenuBean menuBean;

    @Inject
    private PaymentAndBillingVisibleBean paymentAndBillingVisibleBean;

    @Inject
    private PaymentInfoBean paymentInfoBean;

    /**
     * EJB
     */
    private SubscriptionService subscriptionService;
    private SubscriptionDetailsService subscriptionDetailsService;
    private TriggerProcessesService triggerProcessService;
    private AccountService accountingService;
    private PartnerService partnerService;

    private static final BaseBean.Vo2ModelMapper<VOService, Service> SERVICE_LISTING_VOSERVICE_MAPPER = new BaseBean.Vo2ModelMapper<VOService, Service>() {
        @Override
        public Service createModel(final VOService vo) {
            return new Service(vo);
        }
    };
    private SubscriptionsHelper subscriptionsHelper;

    public UpgradeWizardConversation() {
        ui = new UiDelegate();
        subscriptionsHelper = new SubscriptionsHelper();
        jsonConverter = new JsonConverter();
        jsonValidator = new JsonParameterValidator(jsonConverter);
    }

    @PostConstruct
    public void postConstruct() {
        paymentAndBillingVisibleBean = ui
                .findBean("paymentAndBillingVisibleBean");
        paymentInfoBean = ui.findBean("paymentInfoBean");
    }

    private void initializeSubscription()
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException {

        model.setSubscriptionExisting(true);
        model.setIsReportIssueAllowed(userBean.isLoggedInAndAdmin()
                || userBean.isLoggedInAndSubscriptionManager());

        POSubscriptionDetails subscriptionDetails = getSubscriptionDetailsService()
                .getSubscriptionDetails(model.getSelectedSubscriptionId(),
                        ui.getViewLocale().getLanguage())
                .getResult(POSubscriptionDetails.class);

        // get the subscription details
        model.setSubscription(subscriptionDetails.getSubscription());
        model.setService(
                new Service(model.getSubscription().getSubscribedService()));
        model.setServiceSupplier(
                new Organization(subscriptionDetails.getSeller()));
        model.setServicePartner(
                new Organization(subscriptionDetails.getPartner()));
        model.setCompatibleServices(SERVICE_LISTING_VOSERVICE_MAPPER
                .map(subscriptionDetails.getUpgradeOptions()));
        model.setDiscount(subscriptionDetails.getDiscount() == null ? null
                : new Discount(subscriptionDetails.getDiscount()));
        model.setServiceRoles(subscriptionDetails.getServiceRoles());
        model.setServiceEvents(SteppedPriceHandler.buildPricedEvents(
                model.getSubscription().getPriceModel().getConsideredEvents()));
        model.setWaitingforApproval(checkTriggerProcessForSubscription(
                subscriptionDetails.getSubscription()));
        model.setReadOnlyParams(model.isCfgTabDisabled());
        boolean showSubscriptionPrices = model.getSubscription()
                .getPriceModel() != null
                && model.getSubscription().getPriceModel().isChargeable()
                && !model.isDirectAccess();
        model.setShowSubscriptionPrices(showSubscriptionPrices);
        boolean showServicePrices = model.getSubscription()
                .getSubscribedService().getPriceModel() != null
                && model.getSubscription().getSubscribedService()
                        .getPriceModel().isChargeable()
                && !model.isDirectAccess();
        model.setShowServicePrices(showServicePrices);

        // set the initial value of payment info,for bug#9921
        List<PricedParameterRow> serviceParameters = PricedParameterRow
                .createPricedParameterRowListForSubscription(
                        model.getService().getVO());
        model.setServiceParameters(serviceParameters);
        model.setSubscriptionParameters(model.getServiceParameters());

        Collections.sort(
                model.getSubscription().getPriceModel().getSteppedPrices(),
                new SteppedPriceComparator());

        // store the usage licenses in an internal map
        subscriptionsHelper.setUsageLicenses(model.getSubscription(),
                model.getUsageLicenseMap());

        // get the users from the organization and select users
        // which already use the subscription
        setUnassignedAndAssignedUsersAsWellSubsOwners(subscriptionDetails);

        // on subscription changing we have to change subscription for
        // showing detailed price model
        // selectedSubscriptionForShowingPriceModel =
        // initSubscriptionDetails(selectedSubscription);
        Integer maxNamedUsers = subscriptionsHelper
                .setMaximumNamedUsers(model.getSubscription());
        model.setMaximumNamedUsers(maxNamedUsers);

        ArrayList<VOUdaDefinition> subUdaDefinitions = new ArrayList<>();
        ArrayList<VOUdaDefinition> orgUdaDefinitions = new ArrayList<>();

        subscriptionsHelper.setUdas(subscriptionDetails, subUdaDefinitions,
                orgUdaDefinitions, model.getSubscription());

        model.setSubscriptionUdaRows(UdaRow.getUdaRows(subUdaDefinitions,
                subscriptionDetails.getUdasSubscription()));
        initParamsAndUdas();
        initializePriceModelForSubscription(model.getSubscription());

        setConfirmationData(subscriptionDetails);
    }

    private void initParamsAndUdas() {
        for (UdaRow udaRow : model.getSubscriptionUdaRows()) {
            udaRow.initPasswordValueToStore();
        }
        for (PricedParameterRow pricedParameterRow : model.getServiceParameters()) {
            pricedParameterRow.initPasswordValueToStore();
        }
    }

    /**
     * Select the service with the given service key and copy the parameter
     * values from the previously selected subscription to the parameter values
     * of the service.
     *
     * @param serviceKey
     *            the key of the service which is selected
     * @throws SaaSApplicationException
     */
    private void updateSelectedServiceForUpgrade(final Long serviceKey)
            throws SaaSApplicationException {
        Service selectedService = getServiceFromServer(serviceKey);
        if (selectedService == null) {
            return;
        }
        if (selectedService.getVO().getStatus() != ServiceStatus.ACTIVE) {
            return;
        }

        model.setService(selectedService);

        Collections.sort(
                selectedService.getPriceModel().getVo().getSteppedPrices(),
                new SteppedPriceComparator());
        updateServiceEvents(selectedService.getPriceModel().getVo());

        List<PricedParameterRow> serviceParameters = PricedParameterRow
                .createPricedParameterRowList(selectedService.getVO(), true,
                        true, true, true, false);
        model.setServiceParameters(serviceParameters);

        mapToCurrentSubscriptionValues(model.getSubscriptionParameters(),
                model.getServiceParameters());
    }

    /**
     * Load service from server
     */
    private Service getServiceFromServer(Long key) {
        Service result = null;
        try {
            VOServiceEntry svc = getPartnerService()
                    .getServiceForMarketplace(key.longValue(),
                            ui.getViewLocale().getLanguage())
                    .getResult(VOServiceEntry.class);
            if (svc == null) {
                // Note: The error handling is based on setErrorAttribute to be
                // able to forward the errorKey when redirecting to the service
                // list in case the service details page was accessed with an
                // invalid or non service key.
                ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                        BaseBean.ERROR_SERVICE_NOT_AVAILABLE_ANYMORE);
            } else {
                result = new Service(svc);
            }
        } catch (ObjectNotFoundException e) {
            ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_SERVICE_INVALID_KEY);
        }
        return result;

    }

    /**
     * When migrating the values of the existing subscription will be mapped to
     * the configurable parameters of the service selected to migrate to.
     *
     * @param currentParams
     *            the parameters and their values of the currently selected
     *            subscription
     * @param newParams
     *            the parameters and values of the service selected to migrate
     *            to
     */
    private static void mapToCurrentSubscriptionValues(
            List<PricedParameterRow> currentParams,
            List<PricedParameterRow> newParams) {
        for (PricedParameterRow current : currentParams) {
            if (current.isOption()) {
                continue;
            }
            for (PricedParameterRow newParam : newParams) {
                // For one time parameters we also get the non-configurable
                // ones. But in this case we must not overtake the current used
                // values ==> preserve the default values from the target
                // service according the requirement of this feature.
                if (newParam.isNonConfigurableOneTimeParameter())
                    continue;
                if (newParam.getParameterDefinition().getKey() == current
                        .getParameterDefinition().getKey()) {
                    newParam.getParameter()
                            .setValue(current.getParameter().getValue());
                }
            }
        }

    }

    /**
     * Create an exploded list of priced events and stepped prices.
     *
     * @param priceModel
     *            the price model with the considered events.
     */
    private void updateServiceEvents(VOPriceModel priceModel) {
        List<PricedEventRow> serviceEvents = new ArrayList<>();
        for (VOPricedEvent pricedEvent : priceModel.getConsideredEvents()) {
            PricedEventRow row;
            if (pricedEvent.getSteppedPrices().isEmpty()) {
                row = new PricedEventRow();
                row.setPricedEvent(pricedEvent);
                serviceEvents.add(row);
            } else {
                Collections.sort(pricedEvent.getSteppedPrices(),
                        new SteppedPriceComparator());
                for (VOSteppedPrice sp : pricedEvent.getSteppedPrices()) {
                    row = new PricedEventRow();
                    row.setPricedEvent(pricedEvent);
                    row.setSteppedPrice(sp);
                    serviceEvents.add(row);
                }
            }
        }
        model.setServiceEvents(serviceEvents);
    }

    public List<VOPaymentInfo> getPaymentInfosForSubscription() {
        return paymentInfoBean.getPaymentInfosForSubscription(
                model.getService().getKey(), getAccountingService());
    }

    private void initializePriceModelForSubscription(
            VOSubscriptionDetails subDetails) {
        VOPriceModel pm = subDetails.getPriceModel();
        if (pm != null) {
            model.setPriceModel(new PriceModel(pm));
            model.setRoleSpecificPrices(RolePriceHandler
                    .determineRolePricesForSubscription(subDetails));
        }
    }

    private boolean checkTriggerProcessForSubscription(
            VOSubscriptionDetails voSubscription) {
        List<VOTriggerProcess> waitingForApprovalTriggerProcesses = getTriggerProcessService()
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId(
                        voSubscription.getSubscriptionId())
                .getResultList(VOTriggerProcess.class);

        return !waitingForApprovalTriggerProcesses.isEmpty();
    }

    private void setConfirmationData(
            final POSubscriptionDetails subscriptionDetails) {
        if (subscriptionDetails.getNumberOfSessions() > 0) {
            model.setConfirmMessage(
                    ui.getText("warning.subscription.delete.stillInUse"));
        } else if (model.getUsageLicenseMap().size() > 0) {
            model.setConfirmMessage(ui.getText("warning.subscription.delete"));
        } else {
            model.setConfirmMessage(ui.getText("confirm.subscription.delete"));
        }
        if (model.getUsageLicenseMap().size() > 0) {
            model.setConfirmTitle(ui.getText("warning.title"));
        } else {
            model.setConfirmTitle(ui.getText("confirm.title"));
        }
    }

    private void setUnassignedAndAssignedUsersAsWellSubsOwners(
            final POSubscriptionDetails subscriptionDetails) {
        model.setUnassignedUsers(new ArrayList<User>());
        model.setAssignedUsers(new ArrayList<User>());
        model.setSubscriptionOwners(new ArrayList<User>());
        if (subscriptionDetails.getUsersForOrganization() == null) {
            return;
        }
        for (VOUserDetails voUserDetails : subscriptionDetails
                .getUsersForOrganization()) {
            User user = new User(voUserDetails);
            VOUsageLicense voUsageLicense = model.getUsageLicenseMap()
                    .get(voUserDetails.getUserId());
            if (voUsageLicense == null) {
                model.getUnassignedUsers().add(user);
            } else {
                VORoleDefinition role = voUsageLicense.getRoleDefinition();
                if (role != null) {
                    user.setRoleKey(role.getKey());
                }
                user.setVoUsageLicense(voUsageLicense);
                user.setSelected(true);
                model.getAssignedUsers().add(user);
            }
            if (user.isSubscriptionManager() || user.isOrganizationAdmin()) {
                if (user.getUserId()
                        .equals(model.getSubscription().getOwnerId())) {
                    model.setSelectedOwner(user);
                    model.setStoredOwner(user);
                }
                model.getSubscriptionOwners().add(user);
            }
        }
    }

    public String selectService() {
        Service svc = model.getService();
        if (svc != null && model.getUseExternalConfigurator()) {
            boolean validationError = false;
            List<VOParameter> voParameters = svc.getVO().getParameters();
            for (VOParameter parameter : voParameters) {
                if (parameter.getParameterDefinition().isMandatory()
                        && (parameter.getValue() == null
                                || parameter.getValue().isEmpty())) {
                    validationError = true;
                    break;
                }
            }
            if (validationError) {
                addMessage(FacesMessage.SEVERITY_ERROR,
                        ERROR_INVALID_CONFIGURED_PARAMETERS);
                return "";
            }
        }
        model.setReadOnlyParams(true);
        if (isPaymentConfigurationHidden()) {
            return OUTCOME_SUCCESS;
        }
        if (svc != null && svc.getPriceModel().isChargeable()) {
            return OUTCOME_ENTER_PAYMENT;
        }
        return OUTCOME_SUCCESS;
    }

    public void addMessage(FacesMessage.Severity severityError, String msgKey) {
        JSFUtils.addMessage(null, severityError, msgKey, null);
    }

    public boolean isPaymentConfigurationHidden() {
        return getSubscriptionService().isPaymentInfoHidden();
    }

    /**
     * Change the price model of the selected subscription
     *
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String upgrade() throws SaaSApplicationException {
        String result = OUTCOME_SUCCESS;
        rewriteParamsAndUdas();
        VOSubscription rc = getSubscriptionService().upgradeSubscription(
                model.getSubscription(), model.getService().getVO(),
                model.getSelectedPaymentInfo(),
                model.getSelectedBillingContact(), new SubscriptionsHelper()
                        .getVoUdaFromUdaRows(model.getSubscriptionUdaRows()));
        model.setDirty(false);
        menuBean.resetMenuVisibility();
        if (rc == null) {
            ui.handleProgress();
            result = OUTCOME_PROCESS;
        } else {
            // reload the subscription
            ui.handle(
                    rc.getStatus().isPendingUpdOrSuspendedUpd()
                            ? INFO_SUBSCRIPTION_ASYNC_UPGRADED
                            : INFO_SUBSCRIPTION_UPGRADED,
                    model.getSubscription().getSubscriptionId());

            // help the navigation to highlight the correct navigation item
            menuBean.setCurrentPageLink(MenuBean.LINK_SUBSCRIPTION_USERS);

            // TODO: fix that piece of code. Without it subs details won't work.
            sessionBean.setSelectedSubscriptionId(rc.getSubscriptionId());
            sessionBean.setSelectedSubscriptionKey(rc.getKey());
        }
        conversation.end();
        return result;
    }

    private void rewriteParamsAndUdas() {
        for (UdaRow udaRow : model.getSubscriptionUdaRows()) {
            udaRow.rewriteEncryptedValues();
        }
        for (PricedParameterRow pricedParameterRow : model.getServiceParameters()) {
            pricedParameterRow.rewriteEncryptedValues();
        }
    }

    /**
     * Tries to read the payments that are enabled (intersection of product and
     * customer). The resulting list is sorted by the payment type id. The
     * function returns null in case the serviceKey is not set.
     *
     * @return the list of enabled payment types
     */
    public Collection<VOPaymentType> getEnabledPaymentTypes() {
        return paymentInfoBean.getEnabledPaymentTypes(
                Long.valueOf(model.getService().getKey()),
                getAccountingService());
    }

    public boolean isPaymentInfoVisible() {
        return paymentAndBillingVisibleBean.isPaymentVisible(
                getEnabledPaymentTypes(), getPaymentInfosForSubscription());
    }

    public boolean isBillingContactVisible() {
        return paymentAndBillingVisibleBean.isBillingContactVisible();
    }

    public String getPaymentMissingText() {
        String key;
        if (userBean.isLoggedInAndAdmin()) {
            key = "organization.payment.noEnabledPayment";
        } else {
            key = "organization.payment.noAvailablePaymentForNonAdmin";
        }
        return JSFUtils.getText(key, null);
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
        return paymentInfoBean.getAvailablePaymentTypesForCreation(
                model.getService().getKey(), getAccountingService());
    }

    public String upgradeSubscription() throws SaaSApplicationException {
        initializeSubscription();
        if (subscriptionsHelper.validateSubscriptionStatus(
                model.getSubscription(), getSubscriptionDetailsService())) {
            ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                    model.getSubscription().getSubscriptionId());
            return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
        }

        updateSelectedServiceForUpgrade(model.getSelectedServiceKey());

        if (conversation.isTransient()) {
            conversation.setTimeout(TIMEOUT);
            conversation.begin();
        }
        return OUTCOME_SERVICE_UPGRADE;
    }

    public String actionLoadIframe() {
        String jsonParameters = jsonConverter.getServiceParametersAsJsonString(
                model.getServiceParameters(), model.isReadOnlyParams(), true);
        if (jsonParameters != null && jsonParameters.length() > 0) {
            model.setServiceParametersAsJSONString(jsonParameters);
            model.setLoadIframe(true);
            model.setShowExternalConfigurator(true);
            return null;
        } else {
            model.setHideExternalConfigurator(true);
            model.setServiceParametersAsJSONString(null);
            addMessage(FacesMessage.SEVERITY_ERROR,
                    ERROR_EXTERNAL_TOOL_COMMUNICATION);
            return OUTCOME_ERROR;
        }
    }

    public String validateConfiguredParameters() {
        String validationResult = jsonValidator
                .validateConfiguredParameters(model);
        switch (validationResult) {
        case OUTCOME_ERROR:
            addMessage(FacesMessage.SEVERITY_ERROR,
                    ERROR_EXTERNAL_TOOL_COMMUNICATION);
            return OUTCOME_ERROR;
        case VALIDATION_ERROR:
            addMessage(FacesMessage.SEVERITY_ERROR,
                    ERROR_INVALID_CONFIGURED_PARAMETERS);
        default:
            return null;
        }
    }

    public void actionFallback() {
        model.setUseFallback(true);
        model.setShowTitle(true);
    }

    /**
     * Navigation section
     */
    public String next() {
        model.setReadOnlyParams(true);
        return BaseBean.OUTCOME_SERVICE_SUBSCRIBE;
    }

    public String previous() {
        model.setReadOnlyParams(false);
        return SubscriptionDetailsCtrlConstants.OUTCOME_PREVIOUS;
    }

    public String previousFromConfirmPage() {

        if (isPaymentConfigurationHidden()) {
            return OUTCOME_PREVIOUS;
        }

        String resultNav = OUTCOME_PREVIOUS;
        if (model.getService().getPriceModel().isChargeable()) {
            resultNav = selectService();
        }
        if (OUTCOME_SUCCESS.equals(resultNav)) {
            conversation.end();
        }

        model.setReadOnlyParams(false);
        return resultNav;
    }

    public String gotoConfiguration() {
        model.setReadOnlyParams(false);
        return OUTCOME_PREVIOUS;
    }

    public String gotoEnterPayment() {
        return OUTCOME_ENTER_PAYMENT;
    }

    /**
     * Getters and setters section
     */
    public SubscriptionDetailsService getSubscriptionDetailsService() {
        return subscriptionDetailsService;
    }

    @EJB
    public void setSubscriptionDetailsService(
            SubscriptionDetailsService subscriptionDetailsService) {
        this.subscriptionDetailsService = subscriptionDetailsService;
    }

    public TriggerProcessesService getTriggerProcessService() {
        return triggerProcessService;
    }

    @EJB
    public void setTriggerProcessService(
            TriggerProcessesService triggerProcessService) {
        this.triggerProcessService = triggerProcessService;
    }

    public AccountService getAccountingService() {
        return accountingService;
    }

    @EJB
    public void setAccountingService(AccountService accountingService) {
        this.accountingService = accountingService;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public UpgradeWizardModel getModel() {
        return model;
    }

    public void setModel(UpgradeWizardModel model) {
        this.model = model;
    }

    public UiDelegate getUi() {
        return ui;
    }

    public void setUi(UiDelegate ui) {
        this.ui = ui;
    }

    public PartnerService getPartnerService() {
        return partnerService;
    }

    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

    @EJB
    public void setSubscriptionService(
            SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    @EJB
    public void setPartnerService(PartnerService partnerService) {

        this.partnerService = partnerService;
    }

    public VOPaymentInfo getSelectedPaymentInfo() {
        VOPaymentInfo selectedPaymentInfo;
        PaymentInfoBean paymentInfoBean = ui.findBean("paymentInfoBean");

        // Should not be null after creating new one
        selectedPaymentInfo = paymentInfoBean
                .getSelectedPaymentInfoForSubscription();
        if (selectedPaymentInfo == null) {
            selectedPaymentInfo = model.getSelectedPaymentInfo();
        } else {
            model.setSelectedPaymentInfo(selectedPaymentInfo);
        }
        return selectedPaymentInfo;
    }

    public void setSelectedPaymentInfo(VOPaymentInfo paymentInfo) {
        model.setSelectedPaymentInfo(paymentInfo);
    }

    public VOBillingContact getSelectedBillingContact() {
        VOBillingContact selectedBillingContact;
        BillingContactBean billingContactBean = ui
                .findBean("billingContactBean");
        selectedBillingContact = billingContactBean.getBillingContact();
        if (selectedBillingContact.getId() == null) {
            selectedBillingContact = model.getSelectedBillingContact();
        } else {
            model.setSelectedBillingContact(selectedBillingContact);
        }

        return selectedBillingContact;
    }

    public void setSelectedBillingContact(VOBillingContact billingContact) {
        model.setSelectedBillingContact(billingContact);
    }

    public PaymentInfoBean getPaymentInfoBean() {
        return paymentInfoBean;
    }

    public void setPaymentInfoBean(PaymentInfoBean paymentInfoBean) {
        this.paymentInfoBean = paymentInfoBean;
    }

    public void actionHideExternalConfigurator() {
        model.setHideExternalConfigurator(true);
    }

    public PaymentAndBillingVisibleBean getPaymentAndBillingVisibleBean() {
        return paymentAndBillingVisibleBean;
    }

    public void setPaymentAndBillingVisibleBean(
            PaymentAndBillingVisibleBean paymentAndBillingVisibleBean) {
        this.paymentAndBillingVisibleBean = paymentAndBillingVisibleBean;
    }

    public void keepAlive() {
        // to reset conversation timeout any conversation bean method has to be
        // called
    }

    public long getTimeout() {
        return TIMEOUT;
    }
}
