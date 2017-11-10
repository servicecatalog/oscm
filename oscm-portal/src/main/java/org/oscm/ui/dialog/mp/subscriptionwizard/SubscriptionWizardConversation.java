/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 18.12.14 09:52
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.subscriptionwizard;

import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_EXTERNAL_TOOL_COMMUNICATION;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_INVALID_CONFIGURED_PARAMETERS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_SERVICE_CHANGED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_SERVICE_INACCESSIBLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_TO_PROCEED_SELECT_UNIT;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_CREATED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_CREATED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_ENTER_PAYMENT;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_ENTER_SERVICE_CONFIGURATION;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_ERROR;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_PREVIOUS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_PROCESS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.SUBSCRIPTION_CONFIRMATION_PAGE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.SUBSCRIPTION_NAME_ALREADY_EXISTS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.VALIDATION_ERROR;

import java.io.IOException;
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
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.subscriptiondetails.POServiceForSubscription;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.json.JsonConverter;
import org.oscm.json.JsonParameterValidator;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.BillingContactBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.PaymentAndBillingVisibleBean;
import org.oscm.ui.beans.PaymentInfoBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.RolePriceHandler;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalPriceModelDisplayHandler;
import org.oscm.ui.dialog.mp.serviceDetails.ServiceDetailsModel;
import org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants;
import org.oscm.ui.dialog.mp.userGroups.SubscriptionUnitCtrl;
import org.oscm.ui.generator.IdGenerator;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.PriceModel;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.UdaRow;

/**
 * all service detail information
 */
@Named
@ConversationScoped
public class SubscriptionWizardConversation implements Serializable {

    private static final long serialVersionUID = -3000938669923416419L;

    private long TIMEOUT = 300000L;

    private JsonConverter jsonConverter;
    private JsonParameterValidator jsonValidator;
    private SubscriptionsHelper subscriptionsHelper;

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(SubscriptionWizardConversation.class);

    @Inject
    private Conversation conversation;
    @Inject
    private SubscriptionWizardConversationModel model;
    @Inject
    private UserBean userBean;
    @Inject
    private MenuBean menuBean;
    @Inject
    private SubscriptionUnitCtrl subscriptionUnitCtrl;
    @Inject
    private PaymentAndBillingVisibleBean paymentAndBillingVisibleBean;
    @EJB
    private TriggerService triggerService;

    /**
     * Has to be found through JSF context, rather than injected through CDI
     */
    private SessionBean sessionBean;

    /**
     * EJB injected through setters.
     */
    private SubscriptionDetailsService subscriptionDetailsService;
    private SubscriptionServiceInternal subscriptionServiceInternal;
    private SubscriptionService subscriptionService;
    private UserGroupService userGroupService;
    private AccountService accountingService;

    private UiDelegate ui;

    /**
     * Just utility class.
     */
    private PaymentInfoBean paymentInfoBean;

    public SubscriptionWizardConversation() {
        ui = new UiDelegate();
        jsonConverter = new JsonConverter();
        jsonValidator = new JsonParameterValidator(jsonConverter);
        paymentInfoBean = new PaymentInfoBean();
        subscriptionsHelper = new SubscriptionsHelper();
    }

    @PostConstruct
    public void startSubscription() {
        paymentAndBillingVisibleBean = ui
                .findBean("paymentAndBillingVisibleBean");
        paymentInfoBean = ui.findBean("paymentInfoBean");
        try {
            initializeService(
                    getServiceDetailsModel().getSelectedService());
            if (conversation.isTransient()) {
                conversation.setTimeout(TIMEOUT);
                conversation.begin();
            }
            model.setAnyPaymentAvailable(paymentAndBillingVisibleBean
                    .isPaymentVisible(getEnabledPaymentTypes(),
                            getPaymentInfosForSubscription()));
        } catch (ObjectNotFoundException e) {
            redirectToServiceList();
        } catch (ServiceStateException | OperationNotPermittedException
                | OrganizationAuthoritiesException | ValidationException ex) {
            ui.handleException(ex);
        }
        model.setReadOnlyParams(false);
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
        VOUserDetails voUserDetails = ui.getUserFromSessionWithoutException();
        if (!canUserSubscribeWithoutUnitSelection(voUserDetails)
                && subscriptionUnitCtrl.getModel().getSelectedUnitId() == 0L) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    ERROR_TO_PROCEED_SELECT_UNIT);
            return "";
        }
        updateSelectedUnit();

        if (isPaymentConfigurationHidden()) {
            return OUTCOME_SUCCESS;
        }
        if (svc != null && svc.getPriceModel().isChargeable()) {
            return OUTCOME_ENTER_PAYMENT;
        }
        return OUTCOME_SUCCESS;
    }

    private boolean canUserSubscribeWithoutUnitSelection(
            VOUserDetails voUserDetails) {
        return voUserDetails.hasAdminRole()
                || voUserDetails.hasSubscriptionManagerRole();
    }

    public void addMessage(FacesMessage.Severity severityError, String msgKey) {
        JSFUtils.addMessage(null, severityError, msgKey, null);
    }

    public boolean isServiceReadyForSubscription(Service service) {
        return service.isSubscribable()
                && userBean.isLoggedInAndAllowedToSubscribe();
    }

    /**
     * Indirectly redirects to the service list in case the service details page
     * was accessed with an invalid service key. The actual redirection will be
     * executed be a navigation rule.
     *
     * @return the corresponding outcome to so the navigation rule redirects to
     *         the service list page.
     */
    public String redirectToServiceList() {
        String errorKey = (String) ui.getRequest()
                .getAttribute(Constants.REQ_ATTR_ERROR_KEY);
        if (errorKey != null && errorKey.length() > 0) {
            // The only way that the errorKey is not set at this oint is that
            // it was not possible to parse the passed key => add the
            // corresponding error message
            ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_SERVICE_INVALID_KEY);
        }
        return BaseBean.OUTCOME_SHOW_SERVICE_LIST;
    }

    protected String initializeService(Service selectedService)
            throws ServiceStateException, ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException {

        String result = SubscriptionDetailsCtrlConstants.OUTCOME_SHOW_DETAILS_4_CREATION;
        long serviceKey = getKeyOf(selectedService);
        if (serviceKey != 0) {
            final POServiceForSubscription service = subscriptionDetailsService
                    .getServiceForSubscription(serviceKey,
                            ui.getViewLocale().getLanguage())
                    .getResult(POServiceForSubscription.class);
            model.setService(new Service(service.getService()));

            if (!isServiceReadyForSubscription(model.getService())) {
                result = redirectToServiceList();
            } else {

                initializePriceModelForService(service);
                model.setShowServicePrices(
                        model.getService().getPriceModel() != null
                                && model.getService().getPriceModel()
                                        .isChargeable()
                                && !model.isDirectAccess());

                model.setSubscription(new VOSubscriptionDetails());
                List<VOSubscription> existingSubscriptions = new ArrayList<>();
                existingSubscriptions.addAll(service.getSubscriptions());
                existingSubscriptions.addAll(getTriggeredSubscriptionsIds());
                model.getSubscription().setSubscriptionId(
                        new IdGenerator("", model.getService(),
                                existingSubscriptions).generateNewId());
                List<PricedParameterRow> serviceParameters = PricedParameterRow
                        .createPricedParameterRowListForService(
                                model.getService().getVO());
                model.setServiceParameters(serviceParameters);

                model.setDiscount(service.getDiscount() == null ? null
                        : new Discount(service.getDiscount()));

                model.setServiceEvents(SteppedPriceHandler
                        .buildPricedEvents(service.getService().getPriceModel()
                                .getConsideredEvents()));

                final List<VOUdaDefinition> subUdaDefinitions = new ArrayList<>();
                final List<VOUdaDefinition> orgUdaDefinitions = new ArrayList<>();
                for (VOUdaDefinition def : service.getDefinitions()) {
                    if (def.getTargetType()
                            .equals(UdaBean.CUSTOMER_SUBSCRIPTION)) {
                        subUdaDefinitions.add(def);
                    } else if (def.getTargetType().equals(UdaBean.CUSTOMER)) {
                        orgUdaDefinitions.add(def);
                    }
                }
                model.setSubscriptionUdaRows(UdaRow
                        .getUdaRows(subUdaDefinitions, new ArrayList<VOUda>()));
                initPasswordFields(model.getSubscriptionUdaRows(), model.getServiceParameters());
            }
        }
        getSubscriptionUnitCtrl().initializeUnitListForCreateSubscription();
        return result;
    }

    private void initPasswordFields(List<UdaRow> udas, List<PricedParameterRow> serviceParameters) {
        for (UdaRow udaRow : udas) {
            udaRow.initPasswordValueToStore();
        }
        for (PricedParameterRow serviceParameter : serviceParameters) {
            serviceParameter.initPasswordValueToStore();
        }
    }

    private List<VOSubscription> getTriggeredSubscriptionsIds() {
        List<VOSubscription> triggeredSubscriptions = new ArrayList<>();
        List<VOTriggerProcess> triggers = triggerService
                .getAllActionsForOrganizationRelatedSubscription();
        for (VOTriggerProcess voTriggerProcess : triggers) {
            if (voTriggerProcess.getService() == null) {
                continue;
            }
            if (voTriggerProcess.getService().getServiceId().equals(model.getService().getServiceId())) {
                triggeredSubscriptions.add(voTriggerProcess.getSubscription());
            }
        }
        return triggeredSubscriptions;
    }

    private long getKeyOf(Service selectedService) {
        long serviceKey;
        if (selectedService == null) {
            serviceKey = ui.findSessionBean()
                    .determineSelectedServiceKeyForCustomer();
        } else {
            serviceKey = selectedService.getKey();
        }
        return serviceKey;
    }

    public ServiceDetailsModel getServiceDetailsModel() {
        return ui.findBean("serviceDetailsModel");
    }

    private void initializePriceModelForService(
            POServiceForSubscription service) {
        VOPriceModel pm = service.getService().getPriceModel();
        PriceModel priceModel = null;
        if (pm != null) {
            priceModel = new PriceModel(pm);
            model.setRoleSpecificPrices(
                    RolePriceHandler.determineRolePricesForPriceModel(pm));
        }
        model.setPriceModel(priceModel);
    }

    /*
     * validating method for subscriptionId. NOT suitable for subscription
     * details!!
     */
    public void validateSubscriptionId(FacesContext context,
            UIComponent toValidate, Object value) {
        model.setShowExternalConfigurator(false);
        String givenSubscriptionId = (String) value;
        boolean subscriptionIdAlreadyExists = getSubscriptionServiceInternal()
                .validateSubscriptionIdForOrganization(givenSubscriptionId);
        if (subscriptionIdAlreadyExists) {
            ((UIInput) toValidate).setValid(false);
            String clientFieldId = toValidate.getClientId(context);
            ui.handleError(clientFieldId, SUBSCRIPTION_NAME_ALREADY_EXISTS,
                    new Object[] { givenSubscriptionId });
        }
    }

    public boolean isPaymentInfoVisible() {
        return model.isAnyPaymentAvailable();
    }

    /**
     * Persist the previously created new subscription.
     *
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public String subscribe() throws SaaSApplicationException,
            IllegalArgumentException, IOException {

        String subscriptionId = model.getSubscription().getSubscriptionId();
        SubscriptionStatus status;
        String outcome = null;
        if (!isServiceAccessible(model.getService().getKey())) {
            redirectToAccessDeniedPage();
            return BaseBean.MARKETPLACE_ACCESS_DENY_PAGE;
        }
        try {
            rewriteParametersAndUdas();
            VOSubscription rc = getSubscriptionService().subscribeToService(
                    model.getSubscription(), model.getService().getVO(),
                    Collections.emptyList(),
                    model.getSelectedPaymentInfo(),
                    model.getSelectedBillingContact(),
                    subscriptionsHelper.getVoUdaFromUdaRows(
                            getModel().getSubscriptionUdaRows()));
            model.setDirty(false);
            menuBean.resetMenuVisibility();
            if (rc == null) {
                ui.handleProgress();
                outcome = OUTCOME_PROCESS;
            } else {
                status = rc.getStatus();
                getSessionBean()
                        .setSelectedSubscriptionId(rc.getSubscriptionId());
                getSessionBean().setSelectedSubscriptionKey(rc.getKey());

                ui.handle(
                        status.isPending() ? INFO_SUBSCRIPTION_ASYNC_CREATED
                                : INFO_SUBSCRIPTION_CREATED,
                        subscriptionId, rc.getSuccessInfo());

                // help the navigation to highlight the correct navigation item
                menuBean.setCurrentPageLink(MenuBean.LINK_SUBSCRIPTION_USERS);

                outcome = OUTCOME_SUCCESS;
            }

            conversation.end();

        } catch (NonUniqueBusinessKeyException e) {
            // if subscription name already existed redirect to page
            // confirmation with error message
            ui.handleError(null, SUBSCRIPTION_NAME_ALREADY_EXISTS,
                    new Object[] { subscriptionId });
            outcome = SUBSCRIPTION_CONFIRMATION_PAGE;
        } catch (ObjectNotFoundException e) {
            // if service has been deleted in the meantime, give the
            // inaccessible error message
            if (e.getDomainObjectClassEnum()
                    .equals(DomainObjectException.ClassEnum.SERVICE)) {
                ui.handleError(null, ERROR_SERVICE_INACCESSIBLE);
            } else {
                ConcurrentModificationException ex = new ConcurrentModificationException();
                ex.setMessageKey(ERROR_SERVICE_CHANGED);
                ExceptionHandler.execute(ex);
            }
        }

        return outcome;
    }

    private void rewriteParametersAndUdas() {
        for (UdaRow udaRow : model.getSubscriptionUdaRows()) {
            udaRow.rewriteEncryptedValues();
        }
        for (PricedParameterRow pricedParameterRow : model.getServiceParameters()) {
            pricedParameterRow.rewriteEncryptedValues();
        }
    }

    private boolean isServiceAccessible(long serviceKey)
            throws ObjectNotFoundException {
        boolean retVal = true;
        VOUserDetails voUserDetails = ui.getUserFromSessionWithoutException();
        if (null != voUserDetails && !voUserDetails.hasAdminRole()) {
            List<Long> invisibleProductKeys = getUserGroupService()
                    .getInvisibleProductKeysForUser(voUserDetails.getKey());
            if (invisibleProductKeys.contains(serviceKey)) {
                retVal = false;
            }
        }
        return retVal;
    }

    public List<VOPaymentInfo> getPaymentInfosForSubscription() {
        List<VOPaymentInfo> payments = paymentInfoBean
                .getPaymentInfosForSubscription(model.getService().getKey(),
                        getAccountingService());
        model.setAnyPaymentAvailable(!payments.isEmpty());
        return payments;
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
            model.getService().getKey(),
                getAccountingService());
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

    public String actionLoadIframe() {
        String jsonParameters = jsonConverter.getServiceParametersAsJsonString(
                model.getServiceParameters(), model.isReadOnlyParams(),
                model.isSubscriptionExisting());
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
        switch (validationResult == null ? "" : validationResult) {
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

    public void actionHideExternalConfigurator() {
        model.setHideExternalConfigurator(true);
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
        BillingContactBean billingContactBean = getBillingContactBean();
        selectedBillingContact = billingContactBean.getBillingContact();
        if (selectedBillingContact.getId() == null) {
            selectedBillingContact = model.getSelectedBillingContact();
        } else {
            model.setSelectedBillingContact(selectedBillingContact);
        }

        return selectedBillingContact;
    }

    private BillingContactBean getBillingContactBean() {
        return ui.findBean("billingContactBean");
    }

    public void setSelectedBillingContact(VOBillingContact billingContact) {
        model.setSelectedBillingContact(billingContact);
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

    public void actionFallback() {
        model.setUseFallback(true);
        model.setShowTitle(true);
    }

    /**
     * Navigation part. Can't be done through faces-config.xml using "outcome"
     * because conversation will be lost.
     * 
     * @return
     */
    public String next() {
        model.setReadOnlyParams(true);
        return BaseBean.OUTCOME_SERVICE_SUBSCRIBE;
    }

    public String previous() {
        conversation.end();
        return SubscriptionDetailsCtrlConstants.OUTCOME_PREVIOUS;
    }

    public String previousFromPayment() {
        model.setReadOnlyParams(false);
        return SubscriptionDetailsCtrlConstants.OUTCOME_PREVIOUS;
    }

    // TODO: redirection doesn't work. Nicht funktionieren:)
    private void redirectToAccessDeniedPage()
            throws IllegalArgumentException, IOException {
        HttpServletRequest request = JSFUtils.getRequest();
        HttpServletResponse response = JSFUtils.getResponse();
        String relativePath = BaseBean.MARKETPLACE_ACCESS_DENY_PAGE;
        JSFUtils.sendRedirect(response,
                request.getContextPath() + relativePath);
    }

    public String previousFromConfirmPage() {

        if (isPaymentConfigurationHidden()) {
            return OUTCOME_ENTER_SERVICE_CONFIGURATION;
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
        return OUTCOME_ENTER_SERVICE_CONFIGURATION;
    }

    public String gotoEnterPayment() {
        return OUTCOME_ENTER_PAYMENT;
    }

    /**
     * Method updates subscription model with selected unit details.
     */
    public void updateSelectedUnit() {
        if (getSubscriptionUnitCtrl().isUnitSelected()) {
            model.getSubscription().setUnitKey(
                    getSubscriptionUnitCtrl().getModel().getSelectedUnitId());
            model.getSubscription().setUnitName(
                    getSubscriptionUnitCtrl().getModel().getSelectedUnitName());
            return;
        }
        model.getSubscription().setUnitKey(0);
        model.getSubscription().setUnitName("");
    }

    /**
     * Method is used in UI to show external price model details.
     */
    public void display() throws IOException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            OrganizationAuthoritiesException {

        VOPriceModel priceModel = model.getService().getPriceModel().getVo();

        ExternalPriceModelDisplayHandler displayHandler = new ExternalPriceModelDisplayHandler();

        displayHandler.setContent(priceModel.getPresentation());
        displayHandler.setContentType(priceModel.getPresentationDataType());
        displayHandler.display();
    }

    /**
     * End of navigation part.
     */

    /**
     * Getters and setters part
     */

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    @EJB
    public void setSubscriptionDetailsService(
            SubscriptionDetailsService subscriptionDetailsService) {
        this.subscriptionDetailsService = subscriptionDetailsService;
    }

    public void setUi(UiDelegate ui) {
        this.ui = ui;
    }

    public SubscriptionWizardConversationModel getModel() {
        return model;
    }

    public void setModel(SubscriptionWizardConversationModel model) {
        this.model = model;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SubscriptionServiceInternal getSubscriptionServiceInternal() {
        return subscriptionServiceInternal;
    }

    @EJB
    public void setSubscriptionServiceInternal(
            SubscriptionServiceInternal subscriptionServiceInternal) {
        this.subscriptionServiceInternal = subscriptionServiceInternal;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

    @EJB
    public void setSubscriptionService(
            SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public UserGroupService getUserGroupService() {
        return userGroupService;
    }

    @EJB
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    public AccountService getAccountingService() {
        return accountingService;
    }

    @EJB
    public void setAccountingService(AccountService accountingService) {
        this.accountingService = accountingService;
    }

    public SessionBean getSessionBean() {
        if (sessionBean == null) {
            sessionBean = ui.findSessionBean();
        }
        return sessionBean;
    }

    public SubscriptionsHelper getSubscriptionsHelper() {
        return subscriptionsHelper;
    }

    public void setSubscriptionsHelper(
            SubscriptionsHelper subscriptionsHelper) {
        this.subscriptionsHelper = subscriptionsHelper;
    }

    public JsonConverter getJsonConverter() {
        return jsonConverter;
    }

    public void setJsonConverter(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    public JsonParameterValidator getJsonValidator() {
        return jsonValidator;
    }

    public void setJsonValidator(JsonParameterValidator jsonValidator) {
        this.jsonValidator = jsonValidator;
    }

    public SubscriptionUnitCtrl getSubscriptionUnitCtrl() {
        return subscriptionUnitCtrl;
    }

    public PaymentAndBillingVisibleBean getPaymentAndBillingVisibleBean() {
        return paymentAndBillingVisibleBean;
    }

    public void setPaymentAndBillingVisibleBean(
            PaymentAndBillingVisibleBean paymentAndBillingVisibleBean) {
        this.paymentAndBillingVisibleBean = paymentAndBillingVisibleBean;
    }

    public void setSubscriptionUnitCtrl(
            SubscriptionUnitCtrl subscriptionUnitCtrl) {
        this.subscriptionUnitCtrl = subscriptionUnitCtrl;
    }

    public void keepAlive() {
        LOGGER.logDebug(
                "Keep alive for conversation ID = " + conversation.getId());
    }

    public long getTimeout() {
        return TIMEOUT;
    }

    public boolean isPaymentConfigurationHidden() {
        return getSubscriptionService().isPaymentInfoHidden();
    }
}
