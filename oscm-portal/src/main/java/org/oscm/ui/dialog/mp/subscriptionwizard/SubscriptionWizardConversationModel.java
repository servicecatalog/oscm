/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 15.01.15 16:55
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.subscriptionwizard;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;

import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.ui.common.SteppedPriceHandler;
import org.oscm.ui.dialog.mp.interfaces.ConfigParamValidateable;
import org.oscm.ui.model.Discount;
import org.oscm.ui.model.ParameterValidationResult;
import org.oscm.ui.model.PriceModel;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.UdaRow;

/**
 * all service detail information
 */
@Named
public class SubscriptionWizardConversationModel
        implements Serializable, ConfigParamValidateable {

    private static final long serialVersionUID = -8514493198989595102L;

    private Service service;
    private PriceModel priceModel;
    private List<RoleSpecificPrice> roleSpecificPrices;
    private List<UdaRow> subscriptionUdaRows;
    private boolean showServicePrices;
    private VOSubscriptionDetails subscription;
    private boolean showSubscriptionPrices;
    private List<PricedParameterRow> serviceParameters;
    private Discount discount;
    private List<PricedEventRow> serviceEvents;
    private String serviceParametersAsJSONString;
    private boolean anyPaymentAvailable;

    /**
     * Form data
     */
    private boolean hideExternalConfigurator;
    private boolean showExternalConfigurator;
    private boolean dirty;
    private boolean agreed;
    private VOPaymentInfo selectedPaymentInfo;
    private VOBillingContact selectedBillingContact;
    private boolean useFallback = false;
    private boolean loadIframe = false;
    private boolean showTitle = false;
    private boolean configurationChanged = false;
    private boolean readOnlyParams;
    private boolean subscriptionExisting;
    private String parameterConfigResponse;
    private ParameterValidationResult parameterValidationResult;

    public boolean isDirectAccess() {
        return false;
    }

    @Override
    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public Service getService() {
        return service;
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

    public void setSubscriptionUdaRows(List<UdaRow> subscriptionUdaRows) {
        this.subscriptionUdaRows = subscriptionUdaRows;
    }

    public List<UdaRow> getSubscriptionUdaRows() {
        return subscriptionUdaRows;
    }

    public void setShowServicePrices(boolean showServicePrices) {
        this.showServicePrices = showServicePrices;
    }

    public boolean isShowServicePrices() {
        return showServicePrices;
    }

    public void setSubscription(VOSubscriptionDetails subscription) {
        this.subscription = subscription;
    }

    public VOSubscriptionDetails getSubscription() {
        return subscription;
    }

    public void setShowSubscriptionPrices(boolean showSubscriptionPrices) {
        this.showSubscriptionPrices = showSubscriptionPrices;
    }

    public boolean isShowSubscriptionPrices() {
        return showSubscriptionPrices;
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

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setServiceEvents(List<PricedEventRow> serviceEvents) {
        this.serviceEvents = serviceEvents;
    }

    public List<PricedEventRow> getServiceEvents() {
        return serviceEvents;
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

    public void setShowExternalConfigurator(boolean showExternalConfigurator) {
        this.showExternalConfigurator = showExternalConfigurator;
    }

    public boolean isAgreed() {
        return agreed;
    }

    public void setAgreed(boolean agreed) {
        this.agreed = agreed;
    }

    public boolean isShowExternalConfigurator() {
        return showExternalConfigurator;
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

    public boolean getLoadIframe() {
        return !useFallback && loadIframe;
    }

    public void setLoadIframe(boolean loadIframe) {
        this.loadIframe = loadIframe;
    }

    public boolean getUseFallback() {
        return useFallback;
    }

    public void setUseFallback(boolean useFallback) {
        this.useFallback = useFallback;
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

    public String getServiceParametersAsJSONString() {
        return serviceParametersAsJSONString;
    }

    public void setServiceParametersAsJSONString(
            String serviceParametersAsJSONString) {
        this.serviceParametersAsJSONString = serviceParametersAsJSONString;
    }

    @Override
    public boolean isReadOnlyParams() {
        return readOnlyParams;
    }

    @Override
    public void setReadOnlyParams(boolean readOnlyParams) {
        this.readOnlyParams = readOnlyParams;
    }

    @Override
    public boolean isSubscriptionExisting() {
        return subscriptionExisting;
    }

    @Override
    public void setSubscriptionExisting(boolean subscriptionExisting) {
        this.subscriptionExisting = subscriptionExisting;
    }

    public boolean getShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    public void setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
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

    public boolean isAnyPaymentAvailable() {
        return anyPaymentAvailable;
    }

    public void setAnyPaymentAvailable(boolean anyPaymentAvailable) {
        this.anyPaymentAvailable = anyPaymentAvailable;
    }

}
