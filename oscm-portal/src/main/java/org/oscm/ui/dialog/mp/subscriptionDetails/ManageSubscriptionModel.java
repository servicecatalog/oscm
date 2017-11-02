/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.mp.subscriptionDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.dialog.mp.interfaces.ConfigParamValidateable;
import org.oscm.ui.model.*;

@ManagedBean
@ViewScoped
public class ManageSubscriptionModel
        implements Serializable, ConfigParamValidateable {

    /**
     * 
     */
    private static final long serialVersionUID = 206265795857573137L;
    private boolean initialized;
    private boolean subscriptionExisting;
    private VOSubscriptionDetails subscription;
    private String currentSubscriptionID;
    private boolean isReportIssueAllowed;
    private List<PricedParameterRow> subscriptionParameters = new ArrayList<>();
    private final Map<String, VOUsageLicense> userId2UsageLicenseMap = new HashMap<>();
    private Service service;
    private Organization serviceSupplier;
    private Organization servicePartner;
    private List<Service> compatibleServices;
    private Discount discount;
    private List<VORoleDefinition> serviceRoles;
    private List<PricedEventRow> serviceEvents;
    private boolean isWaitingforApproval;
    private boolean showStateWarning;
    private String stateWarning;
    private boolean usersTabDisabled;
    private boolean cfgTabDisabled;
    private boolean payTabDisabled;
    private boolean upgTabDisabled;
    private boolean unsubscribeButtonDisabled;
    private boolean readOnlyParams;
    private boolean directAccess;
    private boolean showServicePrices;
    private boolean showSubscriptionPrices;
    private List<PricedParameterRow> serviceParameters;
    private List<User> unassignedUsers;
    private List<User> assignedUsers;
    private List<User> subscriptionOwners;
    private User selectedOwner;
    private User storedOwner;
    private boolean noSubscriptionOwner;
    private String selectedOwnerName;
    private Integer maximumNamedUsers;
    private List<UdaRow> subscriptionUdaRows;
    private PriceModel priceModel;
    private List<RoleSpecificPrice> roleSpecificPrices;
    private String confirmTitle;
    private String confirmMessage;
    private String deassignMessage;
    private String modalTitle;
    private VOUserDetails userToDeassign;
    private boolean isOwnerSelected;
    private boolean hideExternalConfigurator = false;
    private boolean showTitle = false;
    private boolean useFallback = false;
    private boolean loadIframe = false;
    private boolean showExternalConfigurator = false;
    private String parameterConfigResponse;
    private ParameterValidationResult parameterValidationResult;
    private boolean configurationChanged = false;
    private String serviceParametersAsJSONString;
    private boolean configDirty = false;
    private boolean isAsyncModified;
    private boolean dirty;
    private Long selectedServiceKeyForUpgrade;
    private String selectedTab = "tabUser";
    private boolean notTerminable;
    private String assignNoOwner;
    private boolean showOwnerWarning;
    private String ownerWarningText;
    private long currentSubscriptionKey;
    private boolean paymentTabAvailable;

    /**
     * @return the showExternalConfigurator
     */
    public boolean getShowExternalConfigurator() {
        return showExternalConfigurator;
    }

    /**
     * @param showExternalConfigurator
     *            the showExternalConfigurator to set
     */
    public void setShowExternalConfigurator(boolean showExternalConfigurator) {
        this.showExternalConfigurator = showExternalConfigurator;
    }

    @Override
    public boolean isSubscriptionExisting() {
        return subscriptionExisting;
    }

    @Override
    public void setSubscriptionExisting(boolean subscriptionExisting) {
        this.subscriptionExisting = subscriptionExisting;
    }

    public VOSubscriptionDetails getSubscription() {
        return subscription;
    }

    public void setSubscription(VOSubscriptionDetails subscription) {
        if (subscription != null && subscription.getSubscriptionId() != null) {
            this.currentSubscriptionID = subscription.getSubscriptionId();
            this.currentSubscriptionKey = subscription.getKey();
        }
        this.subscription = subscription;
    }

    public long getCurrentSubscriptionKey() {
        return currentSubscriptionKey;
    }

    /**
     * @return the current Subscription ID
     */
    public String getCurrentSubscriptionID() {
        return currentSubscriptionID;
    }

    /**
     * read only
     */
    public void setCurrentSubscriptionID() {
        // do nothing
    }

    public void setIsReportIssueAllowed(boolean permission) {
        isReportIssueAllowed = permission;
    }

    public boolean getIsReportIssueAllowed() {
        return isReportIssueAllowed;
    }

    public List<PricedParameterRow> getSubscriptionParameters() {
        return subscriptionParameters;
    }

    public void setSubscriptionParameters(
            List<PricedParameterRow> subscriptionParameters) {
        this.subscriptionParameters = subscriptionParameters;
    }

    void resetUsageLicenseMap() {
        userId2UsageLicenseMap.clear();
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public void setService(Service service) {
        this.service = service;
    }

    public Organization getServiceSupplier() {
        return serviceSupplier;
    }

    public void setServiceSupplier(Organization serviceSupplier) {
        this.serviceSupplier = serviceSupplier;
    }

    public Organization getServicePartner() {
        return servicePartner;
    }

    public void setServicePartner(Organization servicePartner) {
        this.servicePartner = servicePartner;
    }

    public List<Service> getCompatibleServices() {
        return compatibleServices;
    }

    public void setCompatibleServices(List<Service> compatibleServices) {
        this.compatibleServices = compatibleServices;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public List<VORoleDefinition> getServiceRoles() {
        return serviceRoles;
    }

    public void setServiceRoles(List<VORoleDefinition> serviceRoles) {
        this.serviceRoles = serviceRoles;
    }

    public List<PricedEventRow> getServiceEvents() {
        return serviceEvents;
    }

    public void setServiceEvents(List<PricedEventRow> serviceEvents) {
        this.serviceEvents = serviceEvents;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isShowStateWarning() {
        return showStateWarning;
    }

    public void setShowStateWarning(boolean showStateWarning) {
        this.showStateWarning = showStateWarning;
    }

    public String getStateWarning() {
        return stateWarning;
    }

    public void setStateWarning(String stateWarning) {
        this.stateWarning = stateWarning;
    }

    public boolean isUsersTabDisabled() {
        return usersTabDisabled;
    }

    public void setUsersTabDisabled(boolean usersTabDisabled) {
        this.usersTabDisabled = usersTabDisabled;
    }

    public boolean isCfgTabDisabled() {
        return cfgTabDisabled;
    }

    public void setCfgTabDisabled(boolean cfgTabDisabled) {
        this.cfgTabDisabled = cfgTabDisabled;
    }

    public boolean isPayTabDisabled() {
        return payTabDisabled;
    }

    public void setPayTabDisabled(boolean payTabDisabled) {
        this.payTabDisabled = payTabDisabled;
    }

    public boolean isUpgTabDisabled() {
        return upgTabDisabled;
    }

    public void setUpgTabDisabled(boolean upgTabDisabled) {
        this.upgTabDisabled = upgTabDisabled;
    }

    public boolean isWaitingforApproval() {
        return isWaitingforApproval;
    }

    public void setWaitingforApproval(boolean isWaitingforApproval) {
        this.isWaitingforApproval = isWaitingforApproval;
    }

    public boolean isUnsubscribeButtonDisabled() {
        return unsubscribeButtonDisabled;
    }

    public void setUnsubscribeButtonDisabled(
            boolean unsubscribeButtonDisabled) {
        this.unsubscribeButtonDisabled = unsubscribeButtonDisabled;
    }

    @Override
    public boolean isReadOnlyParams() {
        return readOnlyParams;
    }

    @Override
    public void setReadOnlyParams(boolean readOnlyParams) {
        this.readOnlyParams = readOnlyParams;
    }

    public boolean isDirectAccess() {
        return directAccess;
    }

    public void setDirectAccess(boolean directAccess) {
        this.directAccess = directAccess;
    }

    public boolean isShowServicePrices() {
        return showServicePrices;
    }

    public void setShowServicePrices(boolean showServicePrices) {
        this.showServicePrices = showServicePrices;
    }

    public boolean isShowSubscriptionPrices() {
        return showSubscriptionPrices;
    }

    public void setShowSubscriptionPrices(boolean showSubscriptionPrices) {
        this.showSubscriptionPrices = showSubscriptionPrices;
    }

    @Override
    public List<PricedParameterRow> getServiceParameters() {
        return serviceParameters;
    }

    @Override
    public void setServiceParameters(
            List<PricedParameterRow> serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    public Map<String, VOUsageLicense> getUsageLicenseMap() {
        return userId2UsageLicenseMap;
    }

    public List<User> getUnassignedUsers() {
        return unassignedUsers;
    }

    public void setUnassignedUsers(List<User> unassignedUsers) {
        this.unassignedUsers = unassignedUsers;
    }

    public List<User> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(List<User> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public List<User> getSubscriptionOwners() {
        return subscriptionOwners;
    }

    public void setSubscriptionOwners(List<User> subscriptionOwners) {
        this.subscriptionOwners = subscriptionOwners;
    }

    public User getSelectedOwner() {
        return selectedOwner;
    }

    public void setSelectedOwner(User selectedOwner) {
        this.selectedOwner = selectedOwner;
    }

    public User getStoredOwner() {
        return storedOwner;
    }

    public void setStoredOwner(User storedOwner) {
        this.storedOwner = storedOwner;
    }

    public boolean isNoSubscriptionOwner() {
        return noSubscriptionOwner;
    }

    public void setNoSubscriptionOwner(boolean noSubscriptionOwner) {
        this.noSubscriptionOwner = noSubscriptionOwner;
    }

    public String getSelectedOwnerName() {
        return selectedOwnerName;
    }

    public void setSelectedOwnerName(String selectedOwnerName) {
        this.selectedOwnerName = selectedOwnerName;
    }

    public Integer getMaximumNamedUsers() {
        return maximumNamedUsers;
    }

    public void setMaximumNamedUsers(Integer maximumNamedUsers) {
        this.maximumNamedUsers = maximumNamedUsers;
    }

    public List<UdaRow> getSubscriptionUdaRows() {
        return subscriptionUdaRows;
    }

    public void setSubscriptionUdaRows(List<UdaRow> subscriptionUdaRows) {
        this.subscriptionUdaRows = subscriptionUdaRows;
    }

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
    }

    public List<RoleSpecificPrice> getRoleSpecificPrices() {
        return roleSpecificPrices;
    }

    public void setRoleSpecificPrices(
            List<RoleSpecificPrice> roleSpecificPrices) {
        this.roleSpecificPrices = roleSpecificPrices;
    }

    public String getConfirmTitle() {
        return confirmTitle;
    }

    public void setConfirmTitle(String confirmTitle) {
        this.confirmTitle = confirmTitle;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    public void setConfirmMessage(String confirmMessage) {
        this.confirmMessage = confirmMessage;
    }

    public String getModalTitle() {
        return modalTitle;
    }

    public void setModalTitle(String modalTitle) {
        this.modalTitle = modalTitle;
    }

    public String getDeassignMessage() {
        return deassignMessage;
    }

    public void setDeassignMessage(String deassignMessage) {
        this.deassignMessage = deassignMessage;
    }

    public VOUserDetails getUserToDeassign() {
        return userToDeassign;
    }

    public void setUserToDeassign(VOUserDetails userToDeassign) {
        this.userToDeassign = userToDeassign;
    }

    public boolean isOwnerSelected() {
        return isOwnerSelected;
    }

    public void setOwnerSelected(boolean isOwnerSelected) {
        this.isOwnerSelected = isOwnerSelected;
    }

    /**
     * Indicates if the default parameter table or the external parameter
     * configuration tool should be shown within the subscription process,
     * subscription upgrade and subscription modification case.<br />
     * 
     * 
     * @return false, if the default table should be shown, otherwise true.
     */
    public boolean getUseExternalConfigurator() {
        return serviceParameters != null && serviceParameters.size() > 0
                && service.useExternalConfigurator()
                && !hideExternalConfigurator;
    }

    public boolean getHideExternalConfigurator() {
        return hideExternalConfigurator;
    }

    @Override
    public void setHideExternalConfigurator(boolean hideExternalConfigurator) {
        this.hideExternalConfigurator = hideExternalConfigurator;
    }

    public boolean isUseFallback() {
        return useFallback;
    }

    public void setUseFallback(boolean useFallback) {
        this.useFallback = useFallback;
    }

    public boolean isLoadIframe() {
        return loadIframe;
    }

    public void setLoadIframe(boolean loadIframe) {
        this.loadIframe = loadIframe;
    }

    @Override
    public String getParameterConfigResponse() {
        return parameterConfigResponse;
    }

    @Override
    public void setParameterConfigResponse(String parameterConfigResponse) {
        this.parameterConfigResponse = parameterConfigResponse;
    }

    @Override
    public ParameterValidationResult getParameterValidationResult() {
        return parameterValidationResult;
    }

    @Override
    public void setParameterValidationResult(
            ParameterValidationResult parameterValidationResult) {
        this.parameterValidationResult = parameterValidationResult;
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    public void setConfigurationChanged(boolean configurationChanged) {
        /**
         * Logic was changed because there was a bug with configuration warning
         * display. Once parameters were changed and dialog opened again
         * clicking configure button resulted in warning message being hidden.
         * As the model is ViewScoped then this parameter should not be changed
         * once it is set to true. After reload new object will be created with
         * default value of false.
         */
        this.configurationChanged = this.configurationChanged
                || configurationChanged;
    }

    public String getServiceParametersAsJSONString() {
        return serviceParametersAsJSONString;
    }

    public void setServiceParametersAsJSONString(
            String serviceParametersAsJSONString) {
        this.serviceParametersAsJSONString = serviceParametersAsJSONString;
    }

    public boolean getUseInternalConfigurator() {
        return (serviceParameters != null && serviceParameters.size() > 0
                && !service.useExternalConfigurator())
                || hideExternalConfigurator;
    }

    public boolean isConfigDirty() {
        return configDirty;
    }

    public void setConfigDirty(boolean configDirty) {
        this.configDirty = configDirty;
    }

    public boolean isAsyncModified() {
        return isAsyncModified;
    }

    public void setAsyncModified(boolean isAsyncModified) {
        this.isAsyncModified = isAsyncModified;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Long getSelectedServiceKeyForUpgrade() {
        return selectedServiceKeyForUpgrade;
    }

    public void setSelectedServiceKeyForUpgrade(
            Long selectedServiceKeyForUpgrade) {
        this.selectedServiceKeyForUpgrade = selectedServiceKeyForUpgrade;
    }

    /*
     * assignment only possible if there are more unassigned users and if the
     * maximum number of assignable users of the service is not yet reached.
     */
    public boolean isAssignAllowed() {
        return getUnassignedUsers() != null && getUnassignedUsers().size() > 0
                && (maximumNamedUsers == null || maximumNamedUsers
                        .intValue() > getUsageLicenseMap().size());
    }

    /**
     * @return true if any priced event row of the price model contains any
     *         stepped price.
     */
    public boolean isPricedEventsWithSteppedPrices() {
        if (serviceEvents == null) {
            return false;
        }
        for (PricedEventRow row : serviceEvents) {
            if (row.getSteppedPrice() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if any priced parameter row of the price model contains any
     *         stepped price.
     */
    public boolean isParametersWithSteppedPrices() {
        return SteppedPriceHandler
                .isParametersWithSteppedPrices(serviceParameters);
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    public boolean getShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isNotTerminable() {
        return notTerminable;
    }

    public void setNotTerminable(boolean notTerminable) {
        this.notTerminable = notTerminable;
    }

    public String getAssignNoOwner() {
        if (this.selectedOwner == null) {
            assignNoOwner = Constants.RADIO_SELECTED;
        }
        return assignNoOwner;
    }

    public void setAssignNoOwner(String assignNoOwner) {
        this.assignNoOwner = assignNoOwner;
    }

    public boolean isShowOwnerWarning() {
        return showOwnerWarning;
    }

    public void setShowOwnerWarning(boolean showOwnerWarning) {
        this.showOwnerWarning = showOwnerWarning;
    }

    public String getOwnerWarningText() {
        return ownerWarningText;
    }

    public void setOwnerWarningText(String ownerWarningText) {
        this.ownerWarningText = ownerWarningText;
    }

    public String getUnitNameToDisplay() {
        String unitName = subscription.getUnitName();
        if (StringUtils.isEmpty(unitName)) {
            return JSFUtils.getText("unit.notAssigned", new Object[] { "" });
        }
        return unitName;
    }

    public boolean isPaymentTabAvailable() {
        return paymentTabAvailable;
    }

    public void setPaymentTabAvailable(boolean paymentTabAvailable) {
        this.paymentTabAvailable = paymentTabAvailable;
    }

}
