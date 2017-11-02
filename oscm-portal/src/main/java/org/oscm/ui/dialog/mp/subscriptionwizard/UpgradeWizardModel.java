/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 27.01.15 10:13
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.subscriptionwizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.dialog.mp.interfaces.ConfigParamValidateable;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.ParameterValidationResult;
import org.oscm.ui.model.PriceModel;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.UdaRow;
import org.oscm.ui.model.User;

/**
 * Created by ChojnackiD on 2015-01-27.
 */
@Named
public class UpgradeWizardModel
        implements Serializable, ConfigParamValidateable {
    private static final long serialVersionUID = -8824353344411915678L;

    private VOSubscriptionDetails subscription;
    private boolean isReportIssueAllowed;
    private Service service;
    private Organization serviceSupplier;
    private Organization servicePartner;
    private List<Service> compatibleServices;
    private Discount discount;
    private List<VORoleDefinition> serviceRoles;
    private List<PricedEventRow> serviceEvents;
    private boolean showSubscriptionPrices;
    private boolean showServicePrices;
    private PriceModel priceModel;
    private List<RoleSpecificPrice> roleSpecificPrices;
    private List<PricedParameterRow> serviceParameters;
    private List<PricedParameterRow> subscriptionParameters = new ArrayList<>();
    private boolean waitingforApproval;
    private boolean cfgTabDisabled;
    private boolean readOnlyParams;
    private boolean directAccess;
    private List<UdaRow> subscriptionUdaRows;
    private Integer maximumNamedUsers;
    private String confirmMessage;
    private Map<String, VOUsageLicense> usageLicenseMap = new HashMap<>();
    private String confirmTitle;
    private List<User> unassignedUsers;
    private List<User> assignedUsers;
    private List<User> subscriptionOwners;
    private User selectedOwner;
    private User storedOwner;
    private boolean dirty;
    private boolean hideExternalConfigurator;
    private String selectedSubscriptionId;
    private Long selectedServiceKey;
    private VOPaymentInfo selectedPaymentInfo;
    private VOBillingContact selectedBillingContact;
    private boolean agreed;
    private String serviceParametersAsJSONString;
    private boolean loadIframe;
    private boolean showExternalConfigurator;
    private boolean showTitle = false;
    private boolean useFallback;
    private String parameterConfigResponse;
    private ParameterValidationResult parameterValidationResult;
    private boolean subscriptionExisting;

    public VOSubscriptionDetails getSubscription() {
        return subscription;
    }

    public void setSubscription(VOSubscriptionDetails subscription) {
        this.subscription = subscription;
    }

    public void setIsReportIssueAllowed(boolean isReportIssueAllowed) {
        this.isReportIssueAllowed = isReportIssueAllowed;
    }

    public boolean isReportIssueAllowed() {
        return isReportIssueAllowed;
    }

    public void setReportIssueAllowed(boolean isReportIssueAllowed) {
        this.isReportIssueAllowed = isReportIssueAllowed;
    }

    @Override
    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public Service getService() {
        return service;
    }

    public void setServiceSupplier(Organization serviceSupplier) {
        this.serviceSupplier = serviceSupplier;
    }

    public Organization getServiceSupplier() {
        return serviceSupplier;
    }

    public void setServicePartner(Organization servicePartner) {
        this.servicePartner = servicePartner;
    }

    public Organization getServicePartner() {
        return servicePartner;
    }

    public void setCompatibleServices(List<Service> compatibleServices) {
        this.compatibleServices = compatibleServices;
    }

    public List<Service> getCompatibleServices() {
        return compatibleServices;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setServiceRoles(List<VORoleDefinition> serviceRoles) {
        this.serviceRoles = serviceRoles;
    }

    public List<VORoleDefinition> getServiceRoles() {
        return serviceRoles;
    }

    public void setServiceEvents(List<PricedEventRow> serviceEvents) {
        this.serviceEvents = serviceEvents;
    }

    public List<PricedEventRow> getServiceEvents() {
        return serviceEvents;
    }

    public void setShowSubscriptionPrices(boolean showSubscriptionPrices) {
        this.showSubscriptionPrices = showSubscriptionPrices;
    }

    public boolean isShowSubscriptionPrices() {
        return showSubscriptionPrices;
    }

    public void setShowServicePrices(boolean showServicePrices) {
        this.showServicePrices = showServicePrices;
    }

    public boolean isShowServicePrices() {
        return showServicePrices;
    }

    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
    }

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public void setRoleSpecificPrices(
            List<RoleSpecificPrice> roleSpecificPrices) {
        this.roleSpecificPrices = roleSpecificPrices;
    }

    public List<RoleSpecificPrice> getRoleSpecificPrices() {
        return roleSpecificPrices;
    }

    @Override
    public void setServiceParameters(
            List<PricedParameterRow> serviceParameters) {
        this.serviceParameters = serviceParameters;
    }

    @Override
    public List<PricedParameterRow> getServiceParameters() {
        return serviceParameters;
    }

    public void setSubscriptionParameters(
            List<PricedParameterRow> subscriptionParameters) {
        this.subscriptionParameters = subscriptionParameters;
    }

    public List<PricedParameterRow> getSubscriptionParameters() {
        return subscriptionParameters;
    }

    public void setWaitingforApproval(boolean waitingforApproval) {
        this.waitingforApproval = waitingforApproval;
    }

    public boolean isWaitingforApproval() {
        return waitingforApproval;
    }

    public boolean isCfgTabDisabled() {
        return cfgTabDisabled;
    }

    public void setCfgTabDisabled(boolean cfgTabDisabled) {
        this.cfgTabDisabled = cfgTabDisabled;
    }

    @Override
    public void setReadOnlyParams(boolean readOnlyParams) {
        this.readOnlyParams = readOnlyParams;
    }

    @Override
    public boolean isReadOnlyParams() {
        return readOnlyParams;
    }

    public boolean isDirectAccess() {
        return directAccess;
    }

    public void setDirectAccess(boolean directAccess) {
        this.directAccess = directAccess;
    }

    public void setSubscriptionUdaRows(List<UdaRow> subscriptionUdaRows) {
        this.subscriptionUdaRows = subscriptionUdaRows;
    }

    public List<UdaRow> getSubscriptionUdaRows() {
        return subscriptionUdaRows;
    }

    public void setMaximumNamedUsers(Integer maximumNamedUsers) {
        this.maximumNamedUsers = maximumNamedUsers;
    }

    public Integer getMaximumNamedUsers() {
        return maximumNamedUsers;
    }

    public void setConfirmMessage(String confirmMessage) {
        this.confirmMessage = confirmMessage;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    public Map<String, VOUsageLicense> getUsageLicenseMap() {
        return usageLicenseMap;
    }

    public void setUsageLicenseMap(
            Map<String, VOUsageLicense> usageLicenseMap) {
        this.usageLicenseMap = usageLicenseMap;
    }

    public void setConfirmTitle(String confirmTitle) {
        this.confirmTitle = confirmTitle;
    }

    public String getConfirmTitle() {
        return confirmTitle;
    }

    public List<User> getUnassignedUsers() {
        return unassignedUsers;
    }

    public void setUnassignedUsers(List<User> unassignedUsers) {
        this.unassignedUsers = unassignedUsers;
    }

    public void setAssignedUsers(List<User> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public List<User> getAssignedUsers() {
        return assignedUsers;
    }

    public void setSubscriptionOwners(List<User> subscriptionOwners) {
        this.subscriptionOwners = subscriptionOwners;
    }

    public List<User> getSubscriptionOwners() {
        return subscriptionOwners;
    }

    public void setSelectedOwner(User selectedOwner) {
        this.selectedOwner = selectedOwner;
    }

    public User getSelectedOwner() {
        return selectedOwner;
    }

    public void setStoredOwner(User storedOwner) {
        this.storedOwner = storedOwner;
    }

    public User getStoredOwner() {
        return storedOwner;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
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

    public boolean getUseInternalConfigurator() {
        return (serviceParameters != null && serviceParameters.size() > 0
                && !service.useExternalConfigurator())
                || hideExternalConfigurator;
    }

    public boolean isHideExternalConfigurator() {
        return hideExternalConfigurator;
    }

    @Override
    public void setHideExternalConfigurator(boolean hideExternalConfigurator) {
        this.hideExternalConfigurator = hideExternalConfigurator;
    }

    public String getSelectedSubscriptionId() {
        return selectedSubscriptionId;
    }

    public void setSelectedSubscriptionId(String selectedSubscriptionId) {
        this.selectedSubscriptionId = selectedSubscriptionId;
    }

    public Long getSelectedServiceKey() {
        return selectedServiceKey;
    }

    public void setSelectedServiceKey(Long selectedServiceKey) {
        this.selectedServiceKey = selectedServiceKey;
    }

    /**
     * @return true if any priced parameter row of the price model contains any
     *         stepped price.
     */
    public boolean isParametersWithSteppedPrices() {
        return SteppedPriceHandler
                .isParametersWithSteppedPrices(serviceParameters);
    }

    /**
     * @return true if any priced event row of the price model contains any
     *         stepped price.
     */
    public boolean isPricedEventsWithSteppedPrices() {
        return SteppedPriceHandler
                .isPricedEventsWithSteppedPrices(serviceEvents);
    }

    public VOPaymentInfo getSelectedPaymentInfo() {
        return selectedPaymentInfo;
    }

    public void setSelectedPaymentInfo(VOPaymentInfo selectedPaymentInfo) {
        this.selectedPaymentInfo = selectedPaymentInfo;
    }

    public VOBillingContact getSelectedBillingContact() {
        return selectedBillingContact;
    }

    public void setSelectedBillingContact(
            VOBillingContact selectedBillingContact) {
        this.selectedBillingContact = selectedBillingContact;
    }

    public boolean isAgreed() {
        return agreed;
    }

    public void setAgreed(boolean agreed) {
        this.agreed = agreed;
    }

    public void setServiceParametersAsJSONString(
            String serviceParametersAsJSONString) {
        this.serviceParametersAsJSONString = serviceParametersAsJSONString;
    }

    public String getServiceParametersAsJSONString() {
        return serviceParametersAsJSONString;
    }

    public void setLoadIframe(boolean loadIframe) {
        this.loadIframe = loadIframe;
    }

    public boolean isLoadIframe() {
        return loadIframe;
    }

    public void setShowExternalConfigurator(boolean showExternalConfigurator) {
        this.showExternalConfigurator = showExternalConfigurator;
    }

    public boolean isShowExternalConfigurator() {
        return showExternalConfigurator;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isUseFallback() {
        return useFallback;
    }

    public void setUseFallback(boolean useFallback) {
        this.useFallback = useFallback;
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

    @Override
    public boolean isSubscriptionExisting() {
        return subscriptionExisting;
    }

    @Override
    public void setSubscriptionExisting(boolean subscriptionExisting) {
        this.subscriptionExisting = subscriptionExisting;
    }
}
