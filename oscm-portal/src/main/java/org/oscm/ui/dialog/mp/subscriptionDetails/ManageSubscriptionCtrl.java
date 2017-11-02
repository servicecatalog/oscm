/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.mp.subscriptionDetails;

import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ASSIGN_USERS_MODAL_TITLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.EDIT_ROLES_MODAL_TITLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_EXTERNAL_TOOL_COMMUNICATION;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_INVALID_CONFIGURED_PARAMETERS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_SUBSCRIPTION_NOT_ACCESSIBLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.ERROR_USER_NOTASSIGNEDTOSUBSCRIPTION;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_MAX_USERS_REACHED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_NO_MORE_USERS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_PAYMENT_INFO_SAVED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ASYNC_SAVED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_DELETED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_ROLE_UPDATED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_SAVED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_USER_ASSIGNED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.INFO_SUBSCRIPTION_USER_DEASSIGNED;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_DEASSIGNED_USER_OR_ERROR;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_ERROR;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_MODIFICATION_ERROR;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SERVICE_UNSUBSCRIBE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SUBSCRIPTION_NEED_APPROVAL;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_SUCCESS;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.REQUEST_PARAM_USER_TO_DEASSIGN;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.SELECT_OWNERS_MODAL_TITLE;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.STATUS_PREFIX;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.SUBSCRIPTION_STATE_WARNING;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.SUBSCRIPTION_USER_DEASSIGN_MSG_KEY;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.VALIDATION_ERROR;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparatorChain;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.intf.SessionService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.intf.SubscriptionServiceInternal;
import org.oscm.internal.subscriptiondetails.POSubscriptionDetails;
import org.oscm.internal.subscriptiondetails.SubscriptionDetailsService;
import org.oscm.internal.triggerprocess.TriggerProcessesService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTriggerProcess;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.json.JsonConverter;
import org.oscm.json.JsonParameterValidator;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.BillingContactBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.beans.PaymentAndBillingVisibleBean;
import org.oscm.ui.beans.PaymentInfoBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UdaBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.RolePriceHandler;
import org.oscm.ui.common.SteppedPriceComparator;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalPriceModelDisplayHandler;
import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionsHelper;
import org.oscm.ui.dialog.mp.userGroups.SubscriptionUnitCtrl;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.PriceModel;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.model.User;

@ManagedBean
@ViewScoped
public class ManageSubscriptionCtrl implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4130706788335062134L;

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(ManageSubscriptionCtrl.class);

    private static final BaseBean.Vo2ModelMapper<VOService, Service> SERVICE_LISTING_VOSERVICE_MAPPER = new BaseBean.Vo2ModelMapper<VOService, Service>() {
        @Override
        public Service createModel(final VOService vo) {
            return new Service(vo);
        }
    };
    public static final String DONT_OPEN_MODAL_DIALOG = "dontOpenModalDialog";

    private final ComparatorChain comparatorChain = new ComparatorChain();
    private JsonConverter jsonConverter;
    private JsonParameterValidator jsonValidator;

    @ManagedProperty(value = "#{manageSubscriptionModel}")
    private ManageSubscriptionModel model;

    @ManagedProperty(value = "#{menuBean}")
    private MenuBean menuBean;

    @ManagedProperty(value = "#{paymentInfoBean}")
    private PaymentInfoBean paymentInfoBean;

    @ManagedProperty(value = "#{billingContactBean}")
    private BillingContactBean billingContactBean;

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    @Inject
    private SubscriptionUnitCtrl subscriptionUnitCtrl;

    @EJB
    private UserGroupService userGroupService;

    @ManagedProperty(value = "#{paymentAndBillingVisibleBean}")
    private PaymentAndBillingVisibleBean paymentAndBillingVisibleBean;

    UiDelegate ui = new UiDelegate();

    private TriggerProcessesService triggerProcessService;
    private SubscriptionDetailsService subscriptionDetailsService;
    private SubscriptionService subscriptionService;
    private SubscriptionServiceInternal subscriptionServiceInternal;
    private SessionService sessionService;
    private OperatorService operatorService;
    private ServiceLocator sl = new ServiceLocator();
    private SubscriptionsHelper subscriptionsHelper;

    public ManageSubscriptionCtrl() {
        LOGGER.logDebug(
                ManageSubscriptionCtrl.class.getName() + "bean created...");
        jsonConverter = new JsonConverter();
        jsonValidator = new JsonParameterValidator(jsonConverter);
        subscriptionsHelper = new SubscriptionsHelper();
    }

    @PostConstruct
    public void initialize() {

        comparatorChain.addComparator(new BeanComparator("roleKey"), true);
        comparatorChain.addComparator(new BeanComparator("userId"));

        model.setSubscriptionExisting(true);

        try {
            if (!model.isInitialized()) {
                final long key = sessionBean.getSelectedSubscriptionKey();
                initializeSubscription(key);
            }
        } catch (ObjectNotFoundException | ValidationException
                | OrganizationAuthoritiesException
                | OperationNotPermittedException e) {

            ui.handleException(e);
        }
    }

    void initializeSubscription(long key)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            OperationNotPermittedException, ValidationException {
        if (model.getSubscription() != null
                && key == model.getSubscription().getKey()) {
            return;
        }

        final POSubscriptionDetails subscriptionDetails = getSubscriptionDetailsService()
                .getSubscriptionDetails(key, ui.getViewLocale().getLanguage())
                .getResult(POSubscriptionDetails.class);

        initializeSubscription(subscriptionDetails);
    }

    void initializeSubscription(String id)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException {

        if (model.getSubscription() != null
                && id.equals(model.getSubscription().getSubscriptionId())) {
            return;
        }

        if (ADMStringUtils.isBlank(id)) {
            return;
        }

        final POSubscriptionDetails subscriptionDetails = getSubscriptionDetailsService()
                .getSubscriptionDetails(id, ui.getViewLocale().getLanguage())
                .getResult(POSubscriptionDetails.class);

        initializeSubscription(subscriptionDetails);

    }

    void initializeSubscription(POSubscriptionDetails subscriptionDetails) {
        model.setPaymentTabAvailable(isPaymentTabAvailable());
        model.setIsReportIssueAllowed(isReportIssueAllowed());
        model.setSubscription(null);
        model.getSubscriptionParameters().clear();
        model.resetUsageLicenseMap();

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
        setStateWarningAndTabDisabled(subscriptionDetails);
        model.setReadOnlyParams(model.isCfgTabDisabled());
        model.setShowSubscriptionPrices(
                model.getSubscription().getPriceModel() != null && model
                        .getSubscription().getPriceModel().isChargeable()
                        && !model.isDirectAccess());
        model.setShowServicePrices(model.getSubscription()
                .getSubscribedService().getPriceModel() != null
                && model.getSubscription().getSubscribedService()
                        .getPriceModel().isChargeable()
                && !model.isDirectAccess());

        // set warning message,for bug#11075
        if (model.isUnsubscribeButtonDisabled()) {
            ui.handleProgress();
        }

        // set the initial value of payment info,for bug#9921

        initPaymentInfo();
        refreshSubscriptionParametersInModel(model.getSubscription());

        Collections.sort(
                model.getSubscription().getPriceModel().getSteppedPrices(),
                new SteppedPriceComparator());

        // List<PricedParameterRow> serviceParameters = PricedParameterRow
        // .createPricedParameterRowList(model.getSubscription()
        // .getSubscribedService(), true, true, true, false, false);
        // setServiceParameters(serviceParameters);

        // store the usage licenses in an internal map
        subscriptionsHelper.setUsageLicenses(model.getSubscription(),
                model.getUsageLicenseMap());

        // get the users from the organization and select users
        // which already use the subscription
        setAssignedUsers(subscriptionDetails);
        setUnassignedUsers(subscriptionDetails);
        setSubscriptionOwners(subscriptionDetails);

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



        initializePriceModelForSubscription(model.getSubscription());

        List<PricedParameterRow> serviceParameters = PricedParameterRow
                .createPricedParameterRowListForSubscription(
                        model.getService().getVO());
        model.setServiceParameters(serviceParameters);
        model.setNotTerminable(model.getSubscription() == null
                || model.isUnsubscribeButtonDisabled());

        setConfirmationData(subscriptionDetails);
        refreshSelectedOwnerName(model.getSelectedOwner());
        initializeUnitAssignment();
        initParamsAndUdas();
    }

    private void initParamsAndUdas() {
        for (UdaRow udaRow : model.getSubscriptionUdaRows()) {
            udaRow.initPasswordValueToStore();
        }
        for (PricedParameterRow pricedParameterRow : model.getSubscriptionParameters()) {
            pricedParameterRow.initPasswordValueToStore();
        }
    }

    private void initializeUnitAssignment() {
        VOSubscriptionDetails voSubscriptionDetails = model.getSubscription();
        if (voSubscriptionDetails == null) {
            return;
        }
        long selectedUnitId = voSubscriptionDetails.getUnitKey();
        String selectedUnitName = voSubscriptionDetails.getUnitName();
        if (selectedUnitId == 0L) {
            selectedUnitId = subscriptionUnitCtrl.getModel()
                    .getSelectedUnitId();
            selectedUnitName = subscriptionUnitCtrl.getModel()
                    .getSelectedUnitName();
        }
        if (userBean.getIsUnitAdmin()
                && voSubscriptionDetails.getUnitKey() == 0) {
            subscriptionUnitCtrl.initializeUnitListForModifySubscription();
        }
        subscriptionUnitCtrl.setSelectedUnitToModel(selectedUnitId,
                selectedUnitName);
    }

    private boolean isReportIssueAllowed() {
        return userBean.isLoggedInAndAdmin()
                || userBean.isLoggedInAndSubscriptionManager();
    }

    private boolean isPaymentTabAvailable() {
        return paymentAndBillingVisibleBean.isPaymentTabVisible();
    }

    void setStateWarningAndTabDisabled(
            final POSubscriptionDetails subscriptionDetails) {
        SubscriptionStatus status = subscriptionDetails.getStatus();
        if (!model.isInitialized()) {
            model.setWaitingforApproval(checkTriggerProcessForSubscription(
                    subscriptionDetails.getSubscription()));
        }
        model.setShowStateWarning(
                status.isPending() || status.isPendingUpdOrSuspendedUpd());
        Object[] params = new Object[] {
                JSFUtils.getText(STATUS_PREFIX + status.name(), null) };
        model.setStateWarning(
                JSFUtils.getText(SUBSCRIPTION_STATE_WARNING, params));
        model.setUsersTabDisabled(
                !status.isActive() || model.isWaitingforApproval());
        model.setCfgTabDisabled(
                !status.isActive() || model.isWaitingforApproval());
        model.setPayTabDisabled(
                status.isExpired() || model.isWaitingforApproval());
        model.setUpgTabDisabled(
                status.isPending() || status.isPendingUpdOrSuspendedUpd()
                        || model.isWaitingforApproval());
        model.setUnsubscribeButtonDisabled(model.isWaitingforApproval());
    }

    boolean checkTriggerProcessForSubscription(
            VOSubscriptionDetails voSubscription) {
        List<VOTriggerProcess> waitingForApprovalTriggerProcesses = getTriggerProcessService()
                .getAllWaitingForApprovalTriggerProcessesBySubscriptionId(
                        voSubscription.getSubscriptionId())
                .getResultList(VOTriggerProcess.class);

        return !waitingForApprovalTriggerProcesses.isEmpty();
    }

    void initPaymentInfo() {
        billingContactBean.getBillingContacts();

        VOBillingContact bc = model.getSubscription().getBillingContact();
        if (bc != null) {
            billingContactBean
                    .setSelectedBillingContactKey(Long.valueOf(bc.getKey()));
        }

        sessionBean.setServiceKeyForPayment(
                Long.valueOf(model.getSubscription().getServiceKey()));
        paymentInfoBean.getPaymentInfosForSubscription();

        VOPaymentInfo pi = model.getSubscription().getPaymentInfo();
        if (pi != null) {
            paymentInfoBean.setSelectedPaymentInfoForSubscriptionKey(
                    Long.valueOf(pi.getKey()));
        }
    }

    void refreshSubscriptionParametersInModel(
            VOSubscriptionDetails subscription) {
        model.setServiceParameters(
                PricedParameterRow.createPricedParameterRowList(
                        subscription.getSubscribedService(), true, true, true,
                        false, true));
        model.setSubscriptionParameters(model.getServiceParameters());
    }

    void setUnassignedUsers(final POSubscriptionDetails subscriptionDetails) {

        if (model.getUnassignedUsers() != null) {
            return;
        }
        model.setUnassignedUsers(new ArrayList<User>());
        if (subscriptionDetails.getUsersForOrganization() == null) {
            return;
        }
        long roleKey = 0;
        if (model.getServiceRoles() != null
                && model.getServiceRoles().size() > 0) {
            VORoleDefinition voRoleDefinition = model.getServiceRoles().get(0);
            roleKey = voRoleDefinition.getKey();
        }
        for (VOUserDetails voUserDetails : subscriptionDetails
                .getUsersForOrganization()) {
            User user = new User(voUserDetails);
            VOUsageLicense voUsageLicense = model.getUsageLicenseMap()
                    .get(voUserDetails.getUserId());
            if (voUsageLicense == null) {
                user.setRoleKey(roleKey);
                model.getUnassignedUsers().add(user);
            }
        }
    }

    @SuppressWarnings("unchecked")
    void setAssignedUsers(final POSubscriptionDetails subscriptionDetails) {

        if (subscriptionDetails.getUsersForOrganization() == null) {
            model.setAssignedUsers(null);
            return;
        }

        final ArrayList<User> users = new ArrayList<>();
        for (VOUserDetails voUserDetails : subscriptionDetails
                .getUsersForOrganization()) {
            User user = new User(voUserDetails);
            VOUsageLicense voUsageLicense = model.getUsageLicenseMap()
                    .get(voUserDetails.getUserId());
            if (voUsageLicense != null) {
                VORoleDefinition role = voUsageLicense.getRoleDefinition();
                if (role != null) {
                    user.setRoleKey(role.getKey());
                }
                user.setVoUsageLicense(voUsageLicense);
                user.setSelected(true);
                users.add(user);
            }
        }
        Collections.sort(users, comparatorChain);
        model.setAssignedUsers(users);
    }

    void setSubscriptionOwners(
            final POSubscriptionDetails subscriptionDetails) {
        if (model.getSubscriptionOwners() != null) {
            return;
        }
        model.setSubscriptionOwners(new ArrayList<User>());
        if (subscriptionDetails.getUsersForOrganization() == null) {
            return;
        }

        String ownerId = model.getSubscription().getOwnerId();

        if (ownerId == null) {
            model.setNoSubscriptionOwner(true);
        }

        for (VOUserDetails voUserDetails : subscriptionDetails
                .getUsersForOrganization()) {
            User user = new User(voUserDetails);
            if (user.isSubscriptionManager() || user.isOrganizationAdmin()
                    || user.isUnitAdministrator()) {
                if (user.getUserId().equals(ownerId)) {
                    model.setNoSubscriptionOwner(false);
                    model.setSelectedOwner(user);
                    model.setStoredOwner(user);
                    refreshSelectedOwnerName(user);
                }
                model.getSubscriptionOwners().add(user);
            }
        }
    }

    void refreshSelectedOwnerName(User user) {
        String selectedOwnerName;
        if (user != null) {
            if (ui.isNameSequenceReversed()) {
                selectedOwnerName = formatOwnerName(user.getLastName(),
                        user.getFirstName(), user.getUserId());
            } else {
                selectedOwnerName = formatOwnerName(user.getFirstName(),
                        user.getLastName(), user.getUserId());
            }
        } else {
            selectedOwnerName = JSFUtils.getText("subscription.noOwner",
                    new Object[] { "" });
        }
        model.setSelectedOwnerName(selectedOwnerName);
    }

    private String formatOwnerName(String firstName, String lastName,
            String userId) {
        return filterName(firstName) + " " + filterName(lastName) + "("
                + filterName(userId) + ")";
    }

    private String filterName(String value) {
        return value == null ? "" : value;
    }

    void initializePriceModelForSubscription(VOSubscriptionDetails subDetails) {
        VOPriceModel pm = subDetails.getPriceModel();
        if (pm == null) {
            model.setPriceModel(null);
        } else {
            model.setPriceModel(new PriceModel(pm));
            model.setRoleSpecificPrices(RolePriceHandler
                    .determineRolePricesForSubscription(subDetails));
        }
    }

    void setConfirmationData(final POSubscriptionDetails subscriptionDetails) {
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

    /**
     * Get the {@link VORoleDefinition} for the service role key set on the
     * provided user.
     * 
     * @param user
     *            the user with the key of the selected role
     * @return the matching {@link VORoleDefinition} or <code>null</code>
     */
    private VORoleDefinition getSelectedRole(User user) {
        List<VORoleDefinition> tmpRoles = model.getServiceRoles();
        long roleKey = user.getRoleKey();
        if (tmpRoles != null && roleKey != 0) {
            for (VORoleDefinition role : tmpRoles) {
                if (roleKey == role.getKey()) {
                    return role;
                }
            }
        }
        return null;
    }

    public String setPopupTargetEditRoles() {
        return null;
    }

    /*
     * set the view id for displaying the assign users modal panel if the modal
     * panel should not be shown (e.g. because maximum number of users is
     * already assigned or no more unassigned users exist an info message is
     * created.
     */
    public String setPopupTargetAssignUsers() {
        initializeUnassignedUsers();
        model.setModalTitle(ui.getText(EDIT_ROLES_MODAL_TITLE,
                model.getSubscription().getSubscriptionId()));
        boolean dontOpenModalDialog = false;
        model.setModalTitle(ui.getText(ASSIGN_USERS_MODAL_TITLE,
                model.getSubscription().getSubscriptionId()));
        if (model.getMaximumNamedUsers() != null && model.getMaximumNamedUsers()
                .intValue() <= model.getUsageLicenseMap().size()) {
            ui.handleError(null, INFO_MAX_USERS_REACHED);
            dontOpenModalDialog = true;
        } else if (model.getUnassignedUsers().size() < 1) {
            ui.handleError(null, INFO_NO_MORE_USERS);
            dontOpenModalDialog = true;
        }

        if (dontOpenModalDialog) {
            // invoke navigation rule so that
            // error message is displayed.
            return DONT_OPEN_MODAL_DIALOG;
        } else {
            // continue modal dialog process.
            return null;
        }
    }

    private String initializeUnassignedUsers() {

        Long orgKey = userBean.getOrganizationBean().getOrganization().getKey();
        Long subKey = model.getCurrentSubscriptionKey();

        List<VOUserDetails> unassignedUsers = getOperatorService()
                .getUnassignedUsersByOrg(subKey, orgKey);
        List<User> list = model.getUnassignedUsers();
        list.clear();
        User user;
        for (VOUserDetails voUserDetails : unassignedUsers) {
            user = new User(voUserDetails);
            list.add(user);
        }
        return null;
    }

    public String initializeSubscriptionOwners() {

        Long orgKey = userBean.getOrganizationBean().getOrganization().getKey();

        List<VOUserDetails> usersAvailableForAssignment = getOperatorService()
                .getSubscriptionOwnersForAssignment(orgKey);
        List<User> list = model.getSubscriptionOwners();
        list.clear();
        User user;
        for (VOUserDetails voUserDetails : usersAvailableForAssignment) {
            user = new User(voUserDetails);
            list.add(user);
        }
        return setPopupTargetSelectOwners();
    }

    /**
     * deassign a user from the subscription
     * 
     * @return the logical outcome.
     */
    public String deassignUser() throws OperationPendingException {

        String subscriptionId = model.getSubscription().getSubscriptionId();

        try {
            if (subscriptionsHelper.validateSubscriptionStatus(
                    model.getSubscription(), getSubscriptionDetailsService())) {
                ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                        model.getSubscription().getSubscriptionId());
                return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
            }
            if (model.getUserToDeassign() != null) {
                List<VOUsageLicense> usersToBeAdded = new ArrayList<>();
                List<VOUser> usersToBeRevoked = new ArrayList<>();
                usersToBeRevoked.add(model.getUserToDeassign());

                boolean rc = getSubscriptionService().addRevokeUser(
                        subscriptionId, usersToBeAdded, usersToBeRevoked);
                if (rc) {
                    ui.handle(INFO_SUBSCRIPTION_USER_DEASSIGNED,
                            subscriptionId);
                } else {
                    ui.handleProgress();
                }
                model.setInitialized(false);
            }
        } catch (ConcurrentModificationException e) {
            ExceptionHandler.execute(e);
            return OUTCOME_MODIFICATION_ERROR;
        } catch (ObjectNotFoundException e) {
            ExceptionHandler.execute(new ConcurrentModificationException());
            return OUTCOME_MODIFICATION_ERROR;
        } catch (SaaSApplicationException e) {
            ExceptionHandler.execute(e);
        }
        menuBean.resetMenuVisibility();

        return OUTCOME_DEASSIGNED_USER_OR_ERROR;
    }

    /**
     * Set selected Subscription Owners to model.selectedOwner invoke when click
     * "Ok" button in subscription owner selection page
     */
    public void updateSelectedOwner() {
        for (User owner : model.getSubscriptionOwners()) {
            if (owner.isOwnerSelected()) {
                model.setSelectedOwner(owner);
                break;
            }
        }
        refreshSelectedOwnerName(model.getSelectedOwner());
        model.setShowOwnerWarning(shouldOwnerWarningBeShown());
    }

    public boolean shouldOwnerWarningBeShown() {
        User subscriptionOwner = model.getSelectedOwner();
        if (subscriptionOwner == null) {
            return false;
        }
        if (subscriptionOwner.isOrganizationAdmin()) {
            return false;
        }
        if (model.getSubscription().getUnitKey() == 0L) {
            if (subscriptionOwner.isUnitAdministrator()) {
                setOwnerWarningMessage(
                        BaseBean.WARNING_UNIT_NOT_SELECTED_UNIT_ADMIN,
                        new Object[] { model.getSelectedOwnerName() });
                return true;
            }
            return false;
        }
        if (subscriptionOwner.isSubscriptionManager()) {
            setOwnerWarningMessage(BaseBean.WARNING_OWNER_IS_SUB_MAN,
                    new Object[] { model.getSubscription().getUnitName(),
                            model.getSelectedOwnerName() });
            return true;
        }
        List<POUserGroup> usergroups = getUserGroupService()
                .getUserGroupsForUserWithRole(subscriptionOwner.getKey(),
                        UnitRoleType.ADMINISTRATOR.getKey());
        for (POUserGroup poUserGroup : usergroups) {
            if (poUserGroup.getKey() == model.getSubscription().getUnitKey()) {
                return false;
            }
        }
        setOwnerWarningMessage(BaseBean.WARNING_OWNER_NOT_A_UNIT_ADMIN,
                new Object[] { model.getSubscription().getUnitName(),
                        model.getSelectedOwnerName() });
        return true;
    }

    public void setOwnerWarningMessage(String warningText, Object[] params) {
        String ownerWarningText = JSFUtils.getText(warningText, params);
        model.setOwnerWarningText(ownerWarningText);
    }

    public boolean isOwnerSelected() {
        return model.isOwnerSelected();
    }

    /**
     * Invoke when click radio button to select subscription owner in
     * subscription owner selection page
     */
    public void selectedOwnerChanged(ValueChangeEvent event) {

        UIComponent uiComponent = event.getComponent();
        if (!(uiComponent instanceof HtmlSelectOneRadio)) {
            return;
        }

        HtmlSelectOneRadio radioBtn = (HtmlSelectOneRadio) uiComponent;
        List<UIComponent> uiComponents = radioBtn.getChildren();

        for (UIComponent component : uiComponents) {

            if (!(component instanceof HtmlInputHidden)) {
                continue;
            }

            HtmlInputHidden hiddenInput = (HtmlInputHidden) component;
            String ownerId = (String) hiddenInput.getValue();
            if (Boolean.valueOf((String) event.getNewValue()).booleanValue()) {
                if (ownerId.trim().isEmpty()) {
                    deassignOwner();
                    return;
                }
                setOwnerSelectedStatus(ownerId);
                return;
            }
            for (User owner : model.getSubscriptionOwners()) {
                if (owner.getUserId().equals(ownerId)) {
                    owner.setOwnerSelected(false);
                }
            }
        }
    }

    private void deassignOwner() {
        model.setSelectedOwner(null);
        model.setAssignNoOwner(Constants.RADIO_SELECTED);
        refreshSelectedOwnerName(null);
        for (User owner : model.getSubscriptionOwners()) {
            owner.setOwnerSelected(false);
        }
    }

    /**
     * Refresh subscription owner when radio is clicked
     * 
     */
    public void refreshOwner() {

        if (model.isNoSubscriptionOwner()) {
            User owner = model.getSelectedOwner();
            if (owner != null) {
                owner.setOwnerSelected(false);
            }
            model.setSelectedOwner(null);
        } else {
            model.setSelectedOwner(model.getStoredOwner());
        }
        refreshSelectedOwnerName(model.getSelectedOwner());

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

    public void addMessage(FacesMessage.Severity severityError, String msgKey) {
        JSFUtils.addMessage(null, severityError, msgKey, null);
    }

    public String actionLoadIframe() {
        String jsonParameters = jsonConverter.getServiceParametersAsJsonString(
                model.getServiceParameters(), model.isReadOnlyParams(),
                model.isSubscriptionExisting());
        if (jsonParameters != null && jsonParameters.length() > 0) {
            if (jsonParameters.contains("'")) {
                jsonParameters = jsonParameters.replaceAll("\'", "\\\\\'");
            }
            model.setServiceParametersAsJSONString(jsonParameters);
            model.setLoadIframe(true);
            model.setShowExternalConfigurator(true);
            return null;
        } else {
            model.setHideExternalConfigurator(true);
            model.setServiceParametersAsJSONString(null);
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXTERNAL_TOOL_COMMUNICATION, null);
            return OUTCOME_ERROR;
        }
    }

    public void actionHideExternalConfigurator() {
        model.setHideExternalConfigurator(true);
    }

    /**
     * Rename the selected subscription
     * 
     * @return the logical outcome.
     */
    public String modify() throws SaaSApplicationException {

        if (subscriptionsHelper.validateSubscriptionStatus(
                model.getSubscription(), getSubscriptionDetailsService())) {
            ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                    model.getSubscription().getSubscriptionId());
            return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
        }
        model.getSubscription().setOwnerId(model.getSelectedOwner() != null
                ? model.getSelectedOwner().getUserId() : null);
        updateSelectedUnit();
        rewriteParamsAndUdas();
        VOSubscriptionDetails changedSubscription = getSubscriptionService()
                .modifySubscription(model.getSubscription(),
                        getSubscriptionParameterForModification(),
                        subscriptionsHelper.getVoUdaFromUdaRows(
                                model.getSubscriptionUdaRows()));
        if (subscriptionNotSuspended(changedSubscription)) {
            SubscriptionStatus status = changedSubscription.getStatus();
            if (status.isPendingUpdOrSuspendedUpd()) {
                model.setShowStateWarning(true);
                model.setUsersTabDisabled(true);
                model.setCfgTabDisabled(true);
                model.setUpgTabDisabled(true);
                Object[] params = new Object[] {
                        JSFUtils.getText(STATUS_PREFIX + status.name(), null) };
                model.setStateWarning(
                        JSFUtils.getText(SUBSCRIPTION_STATE_WARNING, params));
                model.setReadOnlyParams(true);
                model.setAsyncModified(true);
                model.setConfigurationChanged(false);
                model.setConfigDirty(false);
                ui.handle(INFO_SUBSCRIPTION_ASYNC_SAVED,
                        model.getSubscription().getSubscriptionId());
            } else {
                refreshModel(changedSubscription);
                ui.handle(INFO_SUBSCRIPTION_SAVED,
                        model.getSubscription().getSubscriptionId());
            }
        } else {
            ui.handleProgress();
            disableTabsForWaitingApproval(true);
        }

        return OUTCOME_SUCCESS;
    }

    private void rewriteParamsAndUdas() {
        for (UdaRow udaRow : model.getSubscriptionUdaRows()) {
            udaRow.rewriteEncryptedValues();
        }
        for (PricedParameterRow pricedParameterRow : model.getSubscriptionParameters()) {
            pricedParameterRow.rewriteEncryptedValues();
        }
    }

    private boolean subscriptionNotSuspended(
            VOSubscriptionDetails changedSubscription) {
        return changedSubscription != null;
    }

    private void disableTabsForWaitingApproval(boolean isDisabled) {
        model.setUsersTabDisabled(isDisabled);
        model.setCfgTabDisabled(isDisabled);
        model.setPayTabDisabled(isDisabled);
        model.setUpgTabDisabled(isDisabled);
        model.setUnsubscribeButtonDisabled(isDisabled);
        model.setWaitingforApproval(isDisabled);
    }

    private List<VOParameter> getSubscriptionParameterForModification() {
        List<VOParameter> parameterList = new ArrayList<>();
        for (PricedParameterRow row : model.getSubscriptionParameters()) {
            if (!row.isOption()) {
                parameterList.add(row.getParameter());
            }
        }
        return parameterList;
    }

    void refreshModel(VOSubscriptionDetails changedSubscription)
            throws SaaSApplicationException {
        sessionBean.setSelectedSubscriptionId(
                model.getSubscription().getSubscriptionId());

        model.setSubscription(changedSubscription);
        updateVoService(changedSubscription);
        model.setConfigurationChanged(false);
        model.setConfigDirty(false);
        refreshStoredOwner(model.getSelectedOwner());
        refreshOrgAndSubscriptionUdasInModel(
                changedSubscription.getSubscriptionId());
        refreshSubscriptionParametersInModel(changedSubscription);
    }

    private void updateVoService(VOSubscriptionDetails changedSubscription) {
        VOService voService = changedSubscription.getSubscribedService();
        model.setService(new Service(voService));
    }

    void refreshStoredOwner(User selectedOwner) {
        if (model.getStoredOwner() != null) {
            model.getStoredOwner().setOwnerSelected(false);
        }
        model.setStoredOwner(selectedOwner);
        if (selectedOwner == null) {
            model.setNoSubscriptionOwner(true);
        }
    }

    void refreshOrgAndSubscriptionUdasInModel(String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException {

        POSubscriptionDetails subscriptionDetails = getSubscriptionDetailsService()
                .getSubscriptionDetails(subscriptionId,
                        ui.getViewLocale().getLanguage())
                .getResult(POSubscriptionDetails.class);

        List<VOUdaDefinition> subUdaDefinitions = new ArrayList<>();
        List<VOUdaDefinition> orgUdaDefinitions = new ArrayList<>();
        for (VOUdaDefinition def : subscriptionDetails.getUdasDefinitions()) {
            if (def.getTargetType().equals(UdaBean.CUSTOMER_SUBSCRIPTION)) {
                subUdaDefinitions.add(def);
            } else if (def.getTargetType().equals(UdaBean.CUSTOMER)) {
                orgUdaDefinitions.add(def);
            }
        }
        model.setSubscriptionUdaRows(UdaRow.getUdaRows(subUdaDefinitions,
                subscriptionDetails.getUdasSubscription()));
    }

    /**
     * Bug 10053
     */
    public String leavePaymentTab() {
        initPaymentInfo();
        model.setDirty(false);
        return null;
    }

    public String savePayment() throws SaaSApplicationException {
        if (subscriptionsHelper.validateSubscriptionStatus(
                model.getSubscription(), getSubscriptionDetailsService())) {
            ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                    model.getSubscription().getSubscriptionId());
            return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
        }
        final VOSubscriptionDetails details = getSubscriptionService()
                .modifySubscriptionPaymentData(model.getSubscription(),
                        billingContactBean.getSelectedBillingContact(),
                        paymentInfoBean
                                .getSelectedPaymentInfoForSubscription());
        model.setSubscription(details);
        // setDirty(false);
        ui.handle(INFO_PAYMENT_INFO_SAVED);
        return OUTCOME_SUCCESS;
    }

    public boolean isBillingContactVisible() {
        return paymentAndBillingVisibleBean.isBillingContactVisible();
    }

    public boolean isPaymentInfoVisible() {
        return paymentAndBillingVisibleBean.isPaymentVisible(
                paymentInfoBean.getEnabledPaymentTypes(),
                paymentInfoBean.getPaymentInfosForSubscription());
    }

    public String updateRoles() throws SaaSApplicationException {

        if (subscriptionsHelper.validateSubscriptionStatus(
                model.getSubscription(), getSubscriptionDetailsService())) {
            ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                    model.getSubscription().getSubscriptionId());
            return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
        }

        List<VOUsageLicense> usersToBeChanged = new ArrayList<>();
        List<VOUser> usersToBeRevoked = new ArrayList<>();

        int usersChanged = 0;

        for (User user : model.getAssignedUsers()) {
            try {
                boolean isAssigned;
                long userKey = user.getKey();
                long subscriptionKey = model.getSubscription().getKey();

                isAssigned = getSubscriptionDetailsService()
                        .isUserAssignedToTheSubscription(userKey,
                                subscriptionKey);

                if (!isAssigned) {
                    throw new UserRoleAssignmentException();
                }
            } catch (UserRoleAssignmentException e) {
                ui.handleError(null, ERROR_USER_NOTASSIGNEDTOSUBSCRIPTION,
                        user.getUserId(),
                        model.getSubscription().getSubscriptionId());
                return OUTCOME_MODIFICATION_ERROR;
            }
            VOUsageLicense usageLicense = model.getUsageLicenseMap()
                    .get(user.getUserId());
            VORoleDefinition role = getSelectedRole(user);
            if (usageLicense != null) {
                VORoleDefinition role1 = usageLicense.getRoleDefinition();
                if ((role1 == null && user.getRoleKey() != 0) || (role1 != null
                        && role1.getKey() != user.getRoleKey())) {
                    // the service role has changed
                    usageLicense.setRoleDefinition(role);

                    usersToBeChanged.add(usageLicense);
                    usersChanged++;
                }
            }
        }

        if (usersChanged > 0) {
            String id = model.getSubscription().getSubscriptionId();
            try {
                boolean rc = getSubscriptionService().addRevokeUser(id,
                        usersToBeChanged, usersToBeRevoked);
                if (rc) {
                    ui.handle(INFO_SUBSCRIPTION_ROLE_UPDATED);
                }
            } catch (ConcurrentModificationException e) {
                ExceptionHandler.execute(e);
                return OUTCOME_MODIFICATION_ERROR;
            } catch (ObjectNotFoundException e) {
                if (e.getDomainObjectClassEnum()
                        .equals(ClassEnum.SUBSCRIPTION)) {
                    ExceptionHandler
                            .execute(new ConcurrentModificationException());
                    return OUTCOME_MODIFICATION_ERROR;
                } else
                    throw e;
            }
            menuBean.resetMenuVisibility();
        }
        model.setSubscription(null);

        return "back";
    }

    public String assignUsers() throws SaaSApplicationException {

        if (subscriptionsHelper.validateSubscriptionStatus(
                model.getSubscription(), getSubscriptionDetailsService())) {
            ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                    model.getSubscription().getSubscriptionId());
            return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
        }
        List<VOUsageLicense> usersToBeAdded = new ArrayList<>();
        List<VOUser> usersToBeRevoked = new ArrayList<>();
        int usersAdded = 0;

        if (model.getUnassignedUsers() != null)
            for (User user : model.getUnassignedUsers()) {

                if (user.isSelected()) {
                    VOUsageLicense usageLicense = model.getUsageLicenseMap()
                            .get(user.getUserId());
                    if (usageLicense == null) {
                        // user added to subscription
                        final VOUsageLicense lic = new VOUsageLicense();
                        lic.setUser(user.getVOUserDetails());
                        lic.setRoleDefinition(getSelectedRole(user));
                        usersToBeAdded.add(lic);
                        usersAdded++;
                    }
                }
            }

        if (usersAdded > 0) {
            try {
                final boolean rc = getSubscriptionService().addRevokeUser(
                        model.getSubscription().getSubscriptionId(),
                        usersToBeAdded, usersToBeRevoked);
                if (rc) {
                    ui.handle(INFO_SUBSCRIPTION_USER_ASSIGNED,
                            model.getSubscription().getSubscriptionId());
                } else {
                    ui.handleProgress();
                }
            } catch (ConcurrentModificationException e) {
                ExceptionHandler.execute(e);
                return OUTCOME_MODIFICATION_ERROR;
            } catch (ObjectNotFoundException e) {
                if (ClassEnum.SUBSCRIPTION
                        .equals(e.getDomainObjectClassEnum())) {
                    ExceptionHandler
                            .execute(new ConcurrentModificationException());
                    return OUTCOME_MODIFICATION_ERROR;
                } else {
                    throw e;
                }
            }
        }
        menuBean.resetMenuVisibility();
        model.setSubscription(null);

        return "back";
    }

    /**
     * Determine the details of the user that should be deassigned from the
     * current subscription
     * 
     * @return the logical outcome
     */
    public String determineUserToDeassign() {
        String idOfUserToDeassign = ui.getExternalContext()
                .getRequestParameterMap().get(REQUEST_PARAM_USER_TO_DEASSIGN);
        if (idOfUserToDeassign != null) {
            VOUsageLicense voUsageLicense = model.getUsageLicenseMap()
                    .get(idOfUserToDeassign);
            if (voUsageLicense != null) {
                VOUserDetails voUserDetails = new VOUserDetails();
                voUserDetails.setKey(voUsageLicense.getUser().getKey());
                voUserDetails.setUserId(voUsageLicense.getUser().getUserId());
                voUserDetails.setOrganizationId(
                        voUsageLicense.getUser().getOrganizationId());
                model.setUserToDeassign(voUserDetails);
                model.setDeassignMessage(
                        ui.getText(SUBSCRIPTION_USER_DEASSIGN_MSG_KEY,
                                model.getUserToDeassign().getUserId()));
            }
        }
        return null;
    }

    public String setPopupTargetSelectOwners() {
        boolean dontOpenModalDialog = false;
        model.setModalTitle(ui.getText(SELECT_OWNERS_MODAL_TITLE,
                model.getSubscription().getSubscriptionId()));
        List<User> owners = model.getSubscriptionOwners();
        if (owners == null || owners.isEmpty()) {
            ui.handleError(null, INFO_NO_MORE_USERS);
            dontOpenModalDialog = true;
        }
        model.setOwnerSelected(false);
        if (model.getSelectedOwner() != null
                && model.getSelectedOwner().getUserId() != null) {
            setOwnerSelectedStatus(model.getSelectedOwner().getUserId());
        } else {
            for (User owner : model.getSubscriptionOwners()) {
                owner.setOwnerSelected(false);
            }
        }

        if (dontOpenModalDialog) {
            return DONT_OPEN_MODAL_DIALOG;
        } else {
            return null;
        }
    }

    private void setOwnerSelectedStatus(String ownerId) {
        for (User owner : model.getSubscriptionOwners()) {
            if (owner.getUserId().equals(ownerId)) {
                owner.setOwnerSelected(true);
                model.setOwnerSelected(true);
                model.setAssignNoOwner(null);
            } else {
                owner.setOwnerSelected(false);
            }
        }
    }

    /**
     * Unsubscribe from the selected subscription.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String unsubscribe() throws SaaSApplicationException {

        if (model.getSubscription() == null
                || model.getSubscription().getSubscriptionId() == null) {
            ui.getRequest().setAttribute(Constants.REQ_ATTR_ERROR_KEY,
                    BaseBean.ERROR_SUBSCRIPTION_NOT_SET);
            return null;
        }
        if (subscriptionsHelper.validateSubscriptionStatus(
                model.getSubscription(), getSubscriptionDetailsService())) {
            ui.handleError(null, ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                    model.getSubscription().getSubscriptionId());
            return OUTCOME_SUBSCRIPTION_NOT_AVAILABLE;
        }
        try {
            // remove all open sessions
            getSessionService().deleteServiceSessionsForSubscription(
                    model.getSubscription().getKey());

            // unsubscribe
            boolean rc = getSubscriptionService().unsubscribeFromService(
                    model.getSubscription().getSubscriptionId());
            if (rc) {
                // the subscription was terminated without suspending the
                // process
                sessionBean.setSelectedSubscriptionId(null);
                ui.handle(INFO_SUBSCRIPTION_DELETED,
                        model.getSubscription().getSubscriptionId());
            } else {
                ui.handleProgress();
                disableTabsForWaitingApproval(true);
                return OUTCOME_SUBSCRIPTION_NEED_APPROVAL;
            }
        } catch (ObjectNotFoundException e) {
            if (e.getDomainObjectClassEnum().equals(ClassEnum.SUBSCRIPTION)) {
                throw new ConcurrentModificationException();
            } else {
                throw e;
            }
        }

        menuBean.resetMenuVisibility();
        model.setSubscription(null);
        model.resetUsageLicenseMap();

        if (ui.getRequest().getServletPath()
                .startsWith(Marketplace.MARKETPLACE_ROOT)) {
            // MP needs special redirect/outcome
            return OUTCOME_SERVICE_UNSUBSCRIBE;
        }
        return OUTCOME_SUCCESS;
    }

    // for Bug #9958
    public void reload()
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, OrganizationAuthoritiesException {
        model.setSubscription(null);
        model.setSubscriptionOwners(null);
        model.setConfigurationChanged(false);
        model.setConfigDirty(false);
        initializeSubscription(model.getCurrentSubscriptionKey());
        model.setAsyncModified(false);
    }

    public String getNoPaymentTypeAvailableMSG() {
        if (userBean.isLoggedInAndAdmin())
            return SubscriptionDetailsCtrlConstants.MESSAGE_NO_PAYMENT_TYPE_ENABLED;
        else
            return SubscriptionDetailsCtrlConstants.MESSAGE_NO_PAYMENT_TYPE_AVAILABLE;
    }

    public void actionFallback() {
        model.setUseFallback(true);
        model.setShowTitle(true);
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
        } else {
            model.getSubscription().setUnitKey(0);
            model.getSubscription().setUnitName("");
        }
        model.setShowOwnerWarning(shouldOwnerWarningBeShown());
    }

    /**
     * Method is used in UI to show external price model details.
     */
    public void display() throws IOException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            OrganizationAuthoritiesException {

        VOSubscriptionDetails subscriptionDetails = getSubscriptionService()
                .getSubscriptionDetails(
                        model.getSubscription().getSubscriptionId());
        VOPriceModel priceModel = subscriptionDetails.getPriceModel();

        ExternalPriceModelDisplayHandler displayHandler = new ExternalPriceModelDisplayHandler();

        displayHandler.setContent(priceModel.getPresentation());
        displayHandler.setContentType(priceModel.getPresentationDataType());
        displayHandler.display();
    }

    public ManageSubscriptionModel getModel() {
        return model;
    }

    public void setModel(ManageSubscriptionModel model) {
        this.model = model;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public PaymentInfoBean getPaymentInfoBean() {
        return paymentInfoBean;
    }

    public void setPaymentInfoBean(PaymentInfoBean paymentInfoBean) {
        this.paymentInfoBean = paymentInfoBean;
    }

    public BillingContactBean getBillingContactBean() {
        return billingContactBean;
    }

    public void setBillingContactBean(BillingContactBean billingContactBean) {
        this.billingContactBean = billingContactBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public JsonConverter getJsonConverter() {
        return jsonConverter;
    }

    public void setJsonConverter(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    public void setTriggerProcessService(
            TriggerProcessesService triggerProcessService) {
        this.triggerProcessService = triggerProcessService;
    }

    private TriggerProcessesService getTriggerProcessService() {
        if (triggerProcessService == null) {
            triggerProcessService = ui
                    .findService(TriggerProcessesService.class);
        }
        return triggerProcessService;
    }

    public void setSubscriptionServiceInternal(
            SubscriptionServiceInternal subscriptionServiceInternal) {
        this.subscriptionServiceInternal = subscriptionServiceInternal;
    }

    public SubscriptionServiceInternal getSubscriptionServiceInternal() {
        if (subscriptionServiceInternal == null) {
            subscriptionServiceInternal = sl
                    .findService(SubscriptionServiceInternal.class);
        }
        return subscriptionServiceInternal;
    }

    public void setSubscriptionDetailsService(
            SubscriptionDetailsService subscriptionDetailsService) {
        this.subscriptionDetailsService = subscriptionDetailsService;
    }

    public SubscriptionDetailsService getSubscriptionDetailsService() {
        if (subscriptionDetailsService == null) {
            subscriptionDetailsService = sl
                    .findService(SubscriptionDetailsService.class);
        }
        return subscriptionDetailsService;
    }

    public void setSubscriptionService(
            SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public SubscriptionService getSubscriptionService() {
        if (subscriptionService == null) {
            subscriptionService = sl.findService(SubscriptionService.class);
        }
        return subscriptionService;
    }

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public SessionService getSessionService() {
        if (sessionService == null) {
            sessionService = sl.findService(SessionService.class);
        }
        return sessionService;
    }

    public void setOperatorService(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    public OperatorService getOperatorService() {
        if (operatorService == null) {
            operatorService = sl.findService(OperatorService.class);
        }
        return operatorService;
    }

    public SubscriptionsHelper getSubscriptionsHelper() {
        return subscriptionsHelper;
    }

    public void setSubscriptionsHelper(
            SubscriptionsHelper subscriptionsHelper) {
        this.subscriptionsHelper = subscriptionsHelper;
    }

    public SubscriptionUnitCtrl getSubscriptionUnitCtrl() {
        return subscriptionUnitCtrl;
    }

    public void setSubscriptionUnitCtrl(
            SubscriptionUnitCtrl subscriptionUnitCtrl) {
        this.subscriptionUnitCtrl = subscriptionUnitCtrl;
    }

    public UserGroupService getUserGroupService() {
        return userGroupService;
    }

    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }

    public PaymentAndBillingVisibleBean getPaymentAndBillingVisibleBean() {
        return paymentAndBillingVisibleBean;
    }

    public void setPaymentAndBillingVisibleBean(
            PaymentAndBillingVisibleBean paymentAndBillingVisibleBean) {
        this.paymentAndBillingVisibleBean = paymentAndBillingVisibleBean;
    }

    public void selectedBillingContactChanged(ValueChangeEvent event) {
        billingContactBean.selectedBillingContactChanged(event);
        model.setDirty(true);
    }

    public void selectedPaymentInfoChanged(ValueChangeEvent event) {
        paymentInfoBean.selectedPaymentInfoChanged(event);
        model.setDirty(true);
    }

}
