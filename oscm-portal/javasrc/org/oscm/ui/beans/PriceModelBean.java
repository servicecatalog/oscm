/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Sep 8, 2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.pricemodel.external.ExternalPriceModelException;
import org.oscm.internal.subscriptions.POSubscriptionAndCustomer;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOPricedEvent;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSteppedPrice;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.common.DataTableHandler;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.LocaleUtils;
import org.oscm.ui.common.SteppedPriceComparator;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalCustomerPriceModelCtrl;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalPriceModelCtrl;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalPriceModelModel;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalServicePriceModelCtrl;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalSubscriptionPriceModelCtrl;
import org.oscm.ui.model.BPLazyDataModel;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.PricedEventRow;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceDetails;

/**
 * @author pravi
 * 
 */
@ViewScoped
@ManagedBean(name = "priceModelBean")
public class PriceModelBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -2732059516783724537L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(PriceModelBean.class);

    private Long selectedServiceKey;
    private List<Service> services;

    private String subscriptionID;

    private String customerID;
    private List<Organization> customers;

    private List<String> supportedCurrencies;
    private List<String> pricingPeriods;

    private VOPriceModel priceModel;
    private VOPriceModel templatePriceModel;
    private ServiceDetails selectedService;
    private List<PricedParameterRow> parameters;
    private List<PricedEventRow> pricedEvents;
    private List<VOSteppedPrice> steppedPrices;

    private VOPriceModelLocalization localization;

    private boolean publishedInGlobalMarketPlace;

    @ManagedProperty(value = "#{menuBean}")
    private MenuBean menuBean;

    @ManagedProperty(value = "#{subscriptionViewBean}")
    private SubscriptionViewBean subscriptionViewBean;

    @ManagedProperty(value = "#{appBean}")
    private ApplicationBean appBean;

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value = "#{bPLazyDataModel}")
    private BPLazyDataModel model;
    
    @ManagedProperty(value = "#{externalCustomerPriceModelCtrl}")
    private ExternalCustomerPriceModelCtrl extCustBean;

    @ManagedProperty(value = "#{externalServicePriceModelCtrl}")
    private ExternalServicePriceModelCtrl extServiceBean;
    
    @ManagedProperty(value = "#{externalSubscriptionPriceModelCtrl}")
    private ExternalSubscriptionPriceModelCtrl extSubBean;

    private String initUrl;
    private boolean dirty;
    private boolean isSaved;
    final static int PRICEMODEL_FOR_SERVICE = 1;
    final static int PRICEMODEL_FOR_CUSTOMER = 2;
    final static int PRICEMODEL_FOR_SUBSCRIPTION = 3;

    private int currentPageType = 0;
    private List<String> dataTableHeaders = new ArrayList<>();

    private boolean customerPricemodelCreation;

    /** List of parameters and options, no stepped price rows. */
    private List<PricedParameterRow> parametersRoles;

    /** Service roles */
    private List<VORoleDefinition> roles;

    /** Selected role */
    private VORoleDefinition selectedRole;

    /** Price for user for selected role. Dimension is roles. */
    private BigDecimal pricePerUserSelectedRole = BigDecimal.ZERO;

    /** List of priced roles for the price model. */
    private VOPricedRole[] priceModelPricedRoles;

    /**
     * Specific prices of parameters and options for one selected role. These
     * values are displayed. Dimension is roles.
     */
    private PricedParameterRow[] pricedParametersOfSelectedRole;

    /**
     * Specific prices of parameters and options for all roles. This is a cache
     * for saving prices for all roles before saving.
     * [Dimension_y][Dimension_x]: Dimension_y - roles, Dimension_x - parameters
     * and option.
     */
    private PricedParameterRow[][] pricedParametersOfAllRoles;

    private boolean editDisabled = false;

    private boolean editDisabledInSubscriptionPage = false;

    private boolean localizeVisible = false;

    private String storedServiceId = null;

    private boolean isExternalPriceModelUploaded;
   
    /**
     * Sort NameWithOrganizationId in locale-sensitive alphabetical order.
     */
    private class NameWithOrgIdComparator
            implements Comparator<Organization> {
        Collator collator = Collator.getInstance();

        @Override
        public int compare(Organization o1, Organization o2) {
            return collator.compare(o1.getNameWithOrganizationId(),
                    o2.getNameWithOrganizationId());
            }
    }
    /**
     * Sort service Ids in locale-sensitive alphabetical order.
     */
    private class ServiceIdsComparator implements Comparator<Service> {
        Collator collator = Collator.getInstance();

        @Override
        public int compare(Service svc1, Service svc2) {
            if (svc1 != null && svc2 != null && svc1.getServiceId() != null
                    && svc2.getServiceId() != null) {
                return collator.compare(svc1.getServiceId(),
                        svc2.getServiceId());
            }
            return 0;
        }
    }

    @PostConstruct
    protected void init() {
        String url = getRequest().getServletPath();
        if (url.endsWith("servicePriceModel.jsf")) {
            setCurrentPMPage(PRICEMODEL_FOR_SERVICE);
        } else if (url.endsWith("customerPriceModel.jsf")) {
            setCurrentPMPage(PRICEMODEL_FOR_CUSTOMER);
        } else if (url.endsWith("subscriptionPriceModel.jsf")) {
            setCurrentPMPage(PRICEMODEL_FOR_SUBSCRIPTION);
        } else if (url.endsWith("view.jsf")) {
            setCurrentPMPage(PRICEMODEL_FOR_SERVICE);
        } else if (url.endsWith("manage.jsf")) {
            setCurrentPMPage(PRICEMODEL_FOR_SERVICE);
        }
        initUrl = url;

        if (getCurrentPMPage() == PRICEMODEL_FOR_SUBSCRIPTION) {
            initSubscriptions();
        } else {
            initServices();
        }

        if (getCurrentPMPage() == PRICEMODEL_FOR_SERVICE) {
            extServiceBean.initBean(getSelectedService());
        } else if (getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER) {
            extCustBean.initBean(getSelectedService());
        } else if (getCurrentPMPage() == PRICEMODEL_FOR_SUBSCRIPTION) {
            extSubBean.initBean(getSelectedService());
        }
    }

    public List<String> getDataTableHeaders() {
        if (dataTableHeaders == null || dataTableHeaders.isEmpty()) {
            try {
                dataTableHeaders = DataTableHandler.getTableHeaders(
                        POSubscriptionAndCustomer.class.getName());
            } catch (Exception ex) {
                throw new SaaSSystemException(ex);
            }
        }
        return dataTableHeaders;
    }

    public void setLicense(String license) {
        String locale = getUserFromSession().getLocale();
        VOPriceModelLocalization localizationTmp = null;
        localizationTmp = getLocalization();
        if (localizationTmp == null) {
            localizationTmp = new VOPriceModelLocalization();
        }
        List<VOLocalizedText> licenses = localizationTmp.getLicenses();
        if (licenses == null) {
            licenses = new ArrayList<>();
        }
        if (!LocaleUtils.set(localizationTmp.getLicenses(), locale, license)) {
            licenses.add(new VOLocalizedText(locale, license));
        }
        if (priceModel != null) {
            priceModel.setLicense(license);
        }
    }

    public String getLicense() {
        String locale = getUserFromSession().getLocale();
        getLocalization();
        if (localization != null) {
            return LocaleUtils.get(localization.getLicenses(), locale);
        }
        return "";
    }

    public void setDescription(String description) {
        String locale = getUserFromSession().getLocale();
        VOPriceModelLocalization localizationTmp = null;
        localizationTmp = getLocalization();
        if (localizationTmp == null) {
            localizationTmp = new VOPriceModelLocalization();
        }
        List<VOLocalizedText> descr = localizationTmp.getDescriptions();
        if (descr == null) {
            descr = new ArrayList<>();
        }
        if (!LocaleUtils.set(localizationTmp.getDescriptions(), locale,
                description)) {
            descr.add(new VOLocalizedText(locale, description));
        }
        priceModel.setDescription(description);
    }

    public String getDescription() {
        String locale = getUserFromSession().getLocale();
        getLocalization();
        if (localization != null) {
            return LocaleUtils.get(localization.getDescriptions(), locale);
        }
        return "";
    }

    /**
     * populate list with the services.
     */
    void initServices() {
        Vo2ModelMapper<VOService, Service> mapper = new Vo2ModelMapper<VOService, Service>() {
            @Override
            public Service createModel(final VOService vo) {
                return new Service(vo);
            }
        };
        switch (getCurrentPMPage()) {
        case PRICEMODEL_FOR_SERVICE:
        case PRICEMODEL_FOR_CUSTOMER:
            setServices(mapper
                    .map(getProvisioningServiceInternal().getSuppliedServices(
                            PerformanceHint.ONLY_FIELDS_FOR_LISTINGS)));
            
            if (this.services != null && this.services.size() > 1) {
                Collections.sort(this.services, new ServiceIdsComparator());
            }
     
            break;
        }
    }

    /**
     * populate list with the subscriptions.
     * 
     */
    void initSubscriptions() {
        setSubscriptionID(null);
        setSelectedSubscription(model.getSelectedSubscriptionAndCustomer());
        updatePriceModel();
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    /**
     * @return the currentPriceModel
     */
    public int getCurrentPMPage() {
        return currentPageType;
    }

    /**
     * @return the value object for the current customerID
     */
    public Organization getCustomer() {
        for (Organization organization : getCustomers()) {
            if (organization.getOrganizationId().equals(getCustomerID())) {
                return organization;
            }
        }
        return null;
    }

    /**
     * @return selected subscription
     */
    public VOSubscriptionDetails getSelectedSubscription() {
        return model.getSelectedSubscription();
    }

    /**
     * @return the customerID
     */
    public String getCustomerID() {
        String id = sessionBean.getSelectedCustomerId();
        if (customerID == null && id != null) {
            setCustomerID(id);
        }
        return customerID;
    }

    /**
     * @param customerID
     *            the customerID to set
     */
    public void setCustomerID(final String customerID) {
        this.customerID = customerID;
        sessionBean.setSelectedCustomerId(customerID);
    }

    /**
     * Get a list of all customer of the current supplier.
     * 
     * @return a list with all customer of the current supplier.
     */
    public List<Organization> getCustomers() {
        if (customers == null) {
            Vo2ModelMapper<VOOrganization, Organization> mapper = new Vo2ModelMapper<VOOrganization, Organization>() {
                @Override
                public Organization createModel(final VOOrganization vo) {
                    return new Organization(vo);
                }
            };
            try {
                customers = mapper.map(
                        getAccountingService().getMyCustomersOptimization());
            } catch (OrganizationAuthoritiesException e) {
                ExceptionHandler.execute(e);
            }
            if (getCustomerID() == null && customers != null
                    && customers.size() > 0) {
                setCustomerID(customers.get(0).getOrganizationId());
            }
        }
        Collections.sort(customers, new NameWithOrgIdComparator());
        return customers;
    }

    public VOPriceModelLocalization getLocalization() {
        try {
            if (selectedService == null) {
                localization = null;
            } else if (localization == null) {
                boolean error = false;
                final VOPriceModel pm = getPriceModel();
                if (pm.getKey() == 0) {
                    // The price model is not yet created
                    // read and fill template licenses from technical service
                    localization = new VOPriceModelLocalization();
                    List<VOLocalizedText> templLicenses = getProvisioningService()
                            .getPriceModelLicenseTemplateLocalization(
                                    selectedService.getVoServiceDetails());
                    localization
                            .setDescriptions(new ArrayList<VOLocalizedText>());
                    if (templLicenses != null) {
                        localization.setLicenses(templLicenses);
                    } else {
                        localization
                                .setLicenses(new ArrayList<VOLocalizedText>());
                    }
                } else {
                    PartnerService partnerService = getParterService();
                    try {
                        Response r = partnerService.getPriceModelLocalization(
                                getSelectedService());
                        localization = r
                                .getResult(VOPriceModelLocalization.class);
                    } catch (SaaSApplicationException e) {
                        error = true;
                        ui.handleException(e);
                    }
                }
                if (!error) {
                    List<Locale> supportedLocales = appBean
                            .getSupportedLocaleList();
                    localization.setDescriptions(
                            LocaleUtils.trim(localization.getDescriptions(),
                                    supportedLocales.iterator()));
                    localization.setLicenses(
                            LocaleUtils.trim(localization.getLicenses(),
                                    supportedLocales.iterator()));
                }
            }
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return localization;
    }

    public void setLocalization(VOPriceModelLocalization localization) {
        this.localization = localization;
    }

    /**
     * @return the parameters
     */
    public List<PricedParameterRow> getParameters() {
        return parameters;
    }

    /**
     * @return the parametersRoles
     */
    public List<PricedParameterRow> getParametersRoles() {
        return parametersRoles;
    }

    public List<PricedEventRow> getPricedEvents() {
        return pricedEvents;
    }

    public List<VOSteppedPrice> getSteppedPrices() {
        return steppedPrices;
    }

    public VOPriceModel getPriceModel() {
        if (priceModel == null) {
            updatePriceModel();
        }
        return priceModel;
    }

    public List<String> getPricingPeriods() {
        if (pricingPeriods == null) {
            pricingPeriods = new ArrayList<>();
            for (PricingPeriod element : PricingPeriod.values()) {
                pricingPeriods.add(element.toString());
            }
        }
        return pricingPeriods;
    }

    /**
     * @return the service for the selected serviceId
     */
    private Service getService() {
        Long key = getSelectedServiceKey();
        if (key == null) {
            return null;
        }
        for (Service s : getServices()) {
            if (s.getKey() == key.longValue()) {
                return s;
            }
        }

        return null;
    }

    public List<Service> getServices() {
        return services;
    }

    /**
     * @return the selectedService
     */
    public Long getSelectedServiceKey() {
        if (selectedServiceKey == null) {
            Long key = sessionBean.getSelectedServiceKeyForSupplier();
            List<Service> svcs = getServices();
            if (key != null && svcs != null) {
                for (Service service : svcs) {
                    if (service.getKey() == key.longValue()) {
                        setSelectedServiceKey(key);
                        updateVOServiceDetails();
                    }
                }
            }
        }
        return selectedServiceKey;
    }

    /**
     * @return the subscriptionID
     */
    public String getSubscriptionID() {
        return subscriptionID;
    }

    /**
     * @return the supportedCurrencies
     */
    public List<String> getSupportedCurrencies() {
        if (supportedCurrencies == null) {
            supportedCurrencies = getProvisioningService()
                    .getSupportedCurrencies();
        }
        return supportedCurrencies;
    }

    /**
     * @return the canLocalize
     */
    public boolean isDisableLocalize() {
        return isDisableSave();
    }

    /**
     * @return the canSave
     */
    public boolean isDisableSave() {
        switch (getCurrentPMPage()) {
        case PRICEMODEL_FOR_SERVICE:
            return getServices() == null || getServices().isEmpty()
                    || isDisableSaveForExternalPriceModel(extServiceBean);

        case PRICEMODEL_FOR_CUSTOMER:
            return getServices() == null || getServices().isEmpty()
                    || getCustomers() == null || getCustomers().isEmpty()
                    || isDisableSaveForExternalPriceModel(extCustBean);

        case PRICEMODEL_FOR_SUBSCRIPTION:
            return model.getCachedList() == null
                    || model.getCachedList().isEmpty() || isDisableSaveForExternalPriceModel(extSubBean);
        }
        return false;
    }

    private boolean isDisableSaveForExternalPriceModel(ExternalPriceModelCtrl externalPriceModelCtrl) {
        if (selectedService == null || selectedService.getPriceModel() == null) {
            return false;
        }
        if (!selectedService.getPriceModel().isExternal() && !selectedService.getTechnicalService().isExternalBilling()) {
            return false;
        }
        return externalPriceModelCtrl.getModel().getSelectedPriceModelContent() == null;
    }


    public void setEditDisabled(boolean value) {
        editDisabled = value;
    }

    public boolean isEditDisabled() {
        return editDisabled;
    }

    public void setEditDisabledInSubscriptionPage(boolean value) {
        editDisabledInSubscriptionPage = value;
    }

    public boolean isEditDisabledInSubscriptionPage() {
        if (getCurrentPMPage() == PRICEMODEL_FOR_SUBSCRIPTION) {
            return true;
        }
        return editDisabledInSubscriptionPage;
    }

    /**
     * Returns true if the price model has been saved successfully and false
     * otherwise.
     */
    public boolean isPressedSave() {
        boolean result = isSaved;
        isSaved = false;
        return result;
    }

    /**
     * Disable One-time fee field for page Define price model for subscription.
     * 
     * @return true for age Define price model for subscription.
     */
    public boolean isOneTimeFeeDisabled() {
        boolean flag = false;
        int page = getCurrentPMPage();
        if (page == PRICEMODEL_FOR_SUBSCRIPTION) {
            flag = true;
        }
        return flag;
    }

    /**
     * @param currentPriceModel
     *            the currentPriceModel to set
     */
    public void setCurrentPMPage(final int currentPriceModel) {
        currentPageType = currentPriceModel;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * @param customers
     *            the customers to set
     */
    public void setCustomers(final List<Organization> customers) {
        this.customers = customers;
    }

    public void setLocalizedDescriptions(
            final List<VOLocalizedText> localizedDescriptions) {
        if (localization == null) {
            localization = new VOPriceModelLocalization();
        }
        localization.setDescriptions(localizedDescriptions);
    }

    public void setServices(final List<Service> services) {
        this.services = services;
    }

    /**
     * @param key
     *            the selectedService to set
     */
    public void setSelectedServiceKey(final Long key) {
        /*
         * If an other service is selected the localization data of the "old"
         * service must be deleted. But e.g. the OK button on the
         * customerPriceModel page (Localize price model description and license
         * agreement) triggers this method and the inserted data are deleted
         * before they can be stored in the DB. (Bug 8591)
         */
        if ((selectedServiceKey != null) && (localization != null)) {
            if (!selectedServiceKey.equals(key)) {
                localization = null;
            }
        }
        selectedServiceKey = key;
        sessionBean.setSelectedServiceKeyForSupplier(key);
    }

    /**
     * @param subscriptionID
     *            the subscriptionID to set
     */
    public void setSubscriptionID(final String subscriptionID) {
        if (subscriptionID != null && subscriptionID.length() == 0) {
            this.subscriptionID = null;
        } else {
            this.subscriptionID = subscriptionID;
        }
    }

    /**
     * @return the selectedService
     */
    public VOServiceDetails getSelectedService() {
        if (selectedService == null) {
            return null;
        }
        return selectedService.getVoServiceDetails();
    }

    /**
     * @return the selectedService
     */
    public String getSelectedServiceNameToDisplay() {
        if (selectedService == null) {
            return null;
        }
        return selectedService.getNameToDisplay();
    }

    /**
     * @param selectedService
     *            the selectedService to set
     */
    public void setSelectedService(VOServiceDetails selectedService) {
        if (selectedService == null) {
            this.selectedService = null;
            return;
        }
        this.selectedService = new ServiceDetails(selectedService);
        if (getCurrentPMPage() == PRICEMODEL_FOR_SERVICE) {
            extServiceBean.initBean(selectedService);
        } else if (getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER) {
            extCustBean.initBean(selectedService);
        }
    }

    public String selectSubscriptionIdAndCustomerId() {
        String subscriptionId = getSubscriptionId();
        String customerId = getCustomerId();
        String result = setSelectedSubscription(subscriptionId, customerId,
                false);
        updatePriceModel();
        extSubBean.reloadPriceModel(getSelectedService());
        setDirty(false);
        return result;
    }

    String setSelectedSubscription(String subscriptionId, String customerId,
            boolean isInitPage) {
        if (subscriptionId == null || customerId == null) {
            return OUTCOME_SUBSCRIPTION_LIST;
        }
        isExternalPriceModelUploaded = false;
        POSubscriptionAndCustomer subscriptionAndCustomer = getSubscriptionAndCustomer(
                subscriptionId, customerId);
        if (subscriptionAndCustomer != null) {
            return setSelectedSubscription(subscriptionAndCustomer);
        } else {
            model.setSelectedSubscriptionAndCustomer(null);
            sessionBean.setSelectedCustomerId(null);
            sessionBean.setSelectedSubscriptionId(null);
            if (!isInitPage) {
                JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                        new String[] { subscriptionId });
            }
            return OUTCOME_SUBSCRIPTION_LIST;
        }

    }

    private POSubscriptionAndCustomer getSubscriptionAndCustomer(
            String subscriptionId, String customerId) {
        for (POSubscriptionAndCustomer subscriptionAndCustomer : model
                .getCachedList()) {
            if (subscriptionAndCustomer.getCustomerId().equals(customerId)
                    && subscriptionAndCustomer.getSubscriptionId()
                            .equals(subscriptionId)) {
                return subscriptionAndCustomer;
            }
        }
        return null;
    }

    public String setSelectedSubscription(
            POSubscriptionAndCustomer subscriptionAndCustomer) {
        String result = OUTCOME_SUBSCRIPTION_LIST;
        setSubscriptionID(null);
        setCustomerID(null);
        sessionBean.setSelectedCustomerId(null);
        sessionBean.setSelectedSubscriptionId(null);
        model.setSelectedSubscriptionAndCustomer(subscriptionAndCustomer);

        if (model.getSelectedSubscriptionAndCustomer() == null) {
            return result;
        }
        try {
            getSubscriptionService().getSubscriptionForCustomer(
                    model.getSelectedSubscriptionAndCustomer().getCustomerId(),
                    model.getSelectedSubscriptionAndCustomer()
                            .getSubscriptionId());
            setSubscriptionID(model.getSelectedSubscriptionAndCustomer()
                    .getSubscriptionId());
            setCustomerID(
                    model.getSelectedSubscriptionAndCustomer().getCustomerId());
            sessionBean.setSelectedCustomerId(
                    model.getSelectedSubscriptionAndCustomer().getCustomerId());
            sessionBean.setSelectedSubscriptionId(model
                    .getSelectedSubscriptionAndCustomer().getSubscriptionId());
            model.setSelectedSubscription(getSubscriptionService()
                    .getSubscriptionForCustomer(model.getCustomerId(), model.getSubscriptionId()));
            result = OUTCOME_SUCCESS;
        } catch (ObjectNotFoundException | OperationNotPermittedException e) {
            model.setSelectedSubscriptionAndCustomer(null);
            ui.handleException(e);
        }
        return result;
    }

    public int getSubscriptionsListSize() {
        return model.getSubscriptionsListSize();
    }

    public boolean isExternalServiceSelected() {
        Service service = getService();
        if (service == null) {
            return false;
        }
        try {
            return getProvisioningService().getServiceDetails(service.getVO())
                    .getTechnicalService().isExternalBilling();
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return false;
    }

    public void updatePriceModel() {
        steppedPrices = null;
        pricedEvents = null;
        parameters = null;
        parametersRoles = null;
        localization = null;
        priceModel = null;
        publishedInGlobalMarketPlace = false;
        templatePriceModel = null;

        updateVOServiceDetails();

        // clear selected role
        selectedRole = null;
        roles = null;

        if (selectedService == null) {
            sessionBean.setSelectedServiceKeyForSupplier(null);
            return;
        }
        sessionBean.setSelectedServiceKeyForSupplier(
                Long.valueOf(selectedService.getKey()));
        if (templatePriceModel != null) {
            priceModel = templatePriceModel;
        } else {
            priceModel = selectedService.getPriceModel();
        }
        // if the service doesn't have a price model we must create a new one
        boolean newPriceModel = false;
        if (priceModel == null) {
            priceModel = new VOPriceModel();
        }
        if (priceModel.getKey() == 0) {
            newPriceModel = true;
        }
        steppedPrices = priceModel.getSteppedPrices();
        Collections.sort(steppedPrices, new SteppedPriceComparator());
        pricedEvents = PricedEventRow.createPricedEventRowList(
                selectedService.getVoServiceDetails());
        parameters = PricedParameterRow
                .createPricedParameterRowListForPriceModel(
                        selectedService.getVoServiceDetails());
        parametersRoles = PricedParameterRow
                .createPricedParameterRowListForPriceModelRoles(
                        selectedService.getVoServiceDetails());

        // bug fix 5928
        if (newPriceModel) {
            // in case of the first time price model saving, parameters and
            // options objects are different for members "parameters" and
            // "parametersRoles", but for saving, they have to be the same.
            // parametersRoles = parameters;
            parametersRoles.clear();
            for (PricedParameterRow row : parameters) {
                parametersRoles.add(row);
            }
            // for already existed price model have to be different lists as
            // reason, "parameters" can have stepped price rows, what has to be
            // blocked for service roles prices
        } else {
            initParametersRolesForNotExistingParameter();
        }
    }

    /**
     * When some parameters are not initialized in existing price model, the
     * parameter in member"parameters" should be the same in member
     * "parametersRoles" as it is initialized for first time price model saving.
     */
    void initParametersRolesForNotExistingParameter() {
        for (PricedParameterRow row : getParameters()) {
            if (row.getPricedParameter().getKey() == 0) {
                for (PricedParameterRow roleRow : parametersRoles) {
                    if (roleRow.getParameterDefinition().getKey() == row
                            .getParameterDefinition().getKey()) {
                        parametersRoles.remove(roleRow);
                        parametersRoles.add(row);
                        break;
                    }
                }
            }
        }
    }

    void updateVOServiceDetails() {
        VOServiceDetails vopd = null;
        customerPricemodelCreation = false;
        try {
            Service service = getService();
            if (service != null) {
                storedServiceId = service.getServiceId();
                if (getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER) {
                    if (getCustomerID() != null) {
                        vopd = getProvisioningService().getServiceForCustomer(
                                getCustomer().getVOOrganization(),
                                service.getVO());
                    }
                    if (vopd == null) {
                        customerPricemodelCreation = true;
                        // there is no customer specific one yet so use the
                        // global template
                        vopd = getProvisioningService()
                                .getServiceDetails(service.getVO());
                        if (vopd != null) {
                            templatePriceModel = vopd.getPriceModel();
                        }
                    }
                } else if (getCurrentPMPage() == PRICEMODEL_FOR_SERVICE) {
                    vopd = getProvisioningService()
                            .getServiceDetails(service.getVO());
                }
                if (vopd == null) {
                    initServices();
                    ObjectNotFoundException ex = new ObjectNotFoundException(
                            ClassEnum.SERVICE, storedServiceId);
                    throw ex;
                }
                List<VOCatalogEntry> catalogEntries = getMarketplaceService()
                        .getMarketplacesForService(vopd);
                if (catalogEntries != null && !catalogEntries.isEmpty()) {
                    final VOMarketplace mp = catalogEntries.get(0)
                            .getMarketplace();
                    if (mp != null) {
                        publishedInGlobalMarketPlace = true;
                    }
                }
            } else if (getCurrentPMPage() == PRICEMODEL_FOR_SUBSCRIPTION
                    && getCustomerID() != null && getSubscriptionID() != null) {
                vopd = getProvisioningService().getServiceForSubscription(
                        getCustomer().getVOOrganization(), getSubscriptionID());
            }
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        setSelectedService(vopd);
        editDisabled = true;
        if (selectedServiceKey != null) {
            if (customerPricemodelCreation
                    && (getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER)) {
                // we create a new price model so edit is enabled
                editDisabled = getSelectedService() != null && getSelectedService()
                        .getAccessType() == ServiceAccessType.EXTERNAL;
            } else if (getSelectedService() != null) {
                editDisabled = getSelectedService()
                        .getStatus() == ServiceStatus.ACTIVE
                        || getSelectedService()
                                .getAccessType() == ServiceAccessType.EXTERNAL;
            }
        } else if (getSubscriptionID() != null && vopd != null) {
            editDisabled = false;
        }
    }

    /**
     * Empty action.
     * 
     */
    public String cancelLocalize() {
        setLocalizeVisible(false);
        menuBean.setCurrentPageLink(initUrl);
        return null;
    }

    /**
     * Get localization an set the values the details page.
     * 
     */
    public String localize() {
        setLocalizeVisible(true);
        menuBean.setCurrentPageLink(initUrl);
        if (priceModel != null) {
            String locale = getUserFromSession().getLocale();
            LocaleUtils.set(getLocalization().getDescriptions(), locale,
                    priceModel.getDescription());
        }
        return null;
    }

    /**
     * Save the price model.
     * 
     * @return the logical outcome.
     */
    public String save() throws SaaSApplicationException {

        VOServiceDetails voServiceDetails = getSelectedService();
        if (voServiceDetails == null) {

            logger.logDebug("save() selectedService is null and "
                    + "hence returning without saving");

            dirty = false;

            if (getCurrentPMPage() == PRICEMODEL_FOR_SUBSCRIPTION) {
                throw new ObjectNotFoundException(ClassEnum.SUBSCRIPTION,
                        model.getSubscriptionId());
            } else {
                throw new ObjectNotFoundException(ClassEnum.SERVICE,
                        storedServiceId);
            }

        } else {
            if (voServiceDetails.getTechnicalService().isExternalBilling()) {
                return saveExternalPriceModel(voServiceDetails,
                        getPriceModel());
            } else {
                return saveNativePriceModel(voServiceDetails, getPriceModel());
            }
        }

    }

    public String saveNativePriceModel(VOServiceDetails voServiceDetails,
            VOPriceModel voPriceModel) throws SaaSApplicationException {

        if (this.getSupportedCurrencies().isEmpty() && !isPriceModelFree()) {
            return handleErrorMessage(ERROR_NO_CURRENCIES, null);
        }

        // for Price Model for Subscription no One-time fee is allowed
        int page = getCurrentPMPage();
        if (page == PRICEMODEL_FOR_SUBSCRIPTION) {
            voPriceModel.setOneTimeFee(BigDecimal.ZERO);
        }
        if (!voPriceModel.getSteppedPrices().isEmpty()) {
            voPriceModel.setPricePerUserAssignment(BigDecimal.ZERO);
        }
        // set priced events
        List<VOPricedEvent> consideredEvents = new ArrayList<>();
        for (PricedEventRow row : pricedEvents) {
            if (row.getSteppedPrice() == null
                    && row.getEventPrice().compareTo(BigDecimal.ZERO) != 0
                    || row.isFirstSteppedPrice()) {
                VOPricedEvent pe = row.getPricedEvent();
                if (!pe.getSteppedPrices().isEmpty()) {
                    pe.setEventPrice(BigDecimal.ZERO);
                }
                consideredEvents.add(pe);
            }
        }
        voPriceModel.setConsideredEvents(consideredEvents);

        // -- part for saving role specific prices --
        // prepare and set user-role specific prices
        voPriceModel.setRoleSpecificUserPrices(
                getRoleSpecificUserPricesForSaving());
                // ------------------------------------------

        // set priced parameters
        List<VOPricedParameter> pricedParameters = new ArrayList<>();
        for (PricedParameterRow row : parameters) {
            if (!row.isOption() && row.isEmptyOrFirstSteppedPrice()) {
                VOPricedParameter pp = row.getPricedParameter();
                if (!pp.getSteppedPrices().isEmpty()) {
                    pp.setPricePerSubscription(BigDecimal.ZERO);
                }
                pricedParameters.add(pp);
            }
        }
        voPriceModel.setSelectedParameters(pricedParameters);
        // -- part for saving role specific prices for parameters --
        // add price parameter and option prices for roles
        addRoleSpecificPriceToParameters();
        // ------------------------------------------

        try {
            switch (getCurrentPMPage()) {

            case PRICEMODEL_FOR_SERVICE:
                voServiceDetails = getProvisioningService().savePriceModel(
                        voServiceDetails, voPriceModel);
                saveLocalization(voServiceDetails);
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_PRICEMODEL_SAVED, voServiceDetails.getServiceId());
                break;

            case PRICEMODEL_FOR_CUSTOMER:
                if (customerPricemodelCreation) {
                    voPriceModel.setKey(0);
                    voPriceModel.setVersion(0);
                }
                customerPricemodelCreation = false;
                voServiceDetails = getProvisioningService()
                        .savePriceModelForCustomer(voServiceDetails,
                                voPriceModel,
                                getCustomer().getVOOrganization());
                saveLocalization(voServiceDetails);
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_PRICEMODEL_FOR_CUSTOMER_SAVED,
                        new String[] {
                                getCustomer().getNameWithOrganizationId(),
                                voServiceDetails.getServiceId() });
                break;

            case PRICEMODEL_FOR_SUBSCRIPTION:
                voServiceDetails = getProvisioningService()
                        .savePriceModelForSubscription(voServiceDetails,
                                voPriceModel);

                saveLocalization(voServiceDetails);
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_PRICEMODEL_FOR_SUBSCRIPTION_SAVED,
                        new String[] { getSubscriptionID(),
                                getCustomer().getNameWithOrganizationId() });
                break;

            default:
                throw new IllegalStateException(
                        "PRICEMODEL OF UNKNOWN TYPE" + getCurrentPMPage());
            }
        } catch (SaaSApplicationException ex) {
            if ((ex instanceof ObjectNotFoundException)
                    && ((ObjectNotFoundException) ex)
                            .getDomainObjectClassEnum() == ClassEnum.SERVICE) {
                ex.setMessageParams(new String[] { storedServiceId });
            }
            throw ex;
        }
        setSelectedService(voServiceDetails);
        updatePriceModel();
        getLocalization();
        dirty = false;
        // If the price model is saved successfully, set this boolean to true.
        isSaved = true;
        return OUTCOME_SUCCESS;
    }

    public VOSubscriptionDetails validateSubscription(VOService service)
            throws SaaSApplicationException {
        return getProvisioningService().validateSubscription(service);
    }

    String saveExternalPriceModel(VOServiceDetails voServiceDetails,
            VOPriceModel voPriceModel) throws SaaSApplicationException {

        ExternalPriceModelModel externalPriceModelModel = null;
        if (getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER) {
            externalPriceModelModel = extCustBean.getModel();
        } else {
            externalPriceModelModel = extServiceBean.getModel();
        }

        PriceModel priceModel  = externalPriceModelModel.getSelectedPriceModel();
        if (priceModel == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXTERNAL_PRICEMODEL_NOT_AVAILABLE);
            return OUTCOME_ERROR;
        }

        try {
            getExternalPriceModelService().updateCache(priceModel);
        } catch (ExternalPriceModelException e) {
            return OUTCOME_ERROR;
        }

        try {

            voPriceModel.setExternal(true);
            voPriceModel.setUuid(priceModel.getId());

            switch (getCurrentPMPage()) {

            case PRICEMODEL_FOR_SERVICE:
                voServiceDetails = getProvisioningService()
                        .savePriceModel(voServiceDetails, voPriceModel);
                saveLocalization(voServiceDetails);
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_PRICEMODEL_SAVED, voServiceDetails.getServiceId());
                break;

            case PRICEMODEL_FOR_CUSTOMER:
                if (customerPricemodelCreation) {
                    voPriceModel.setKey(0);
                    voPriceModel.setVersion(0);
                }
                customerPricemodelCreation = false;

                voServiceDetails = getProvisioningService()
                        .savePriceModelForCustomer(voServiceDetails,
                                voPriceModel,
                                getCustomer().getVOOrganization());
                saveLocalization(voServiceDetails);
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_PRICEMODEL_FOR_CUSTOMER_SAVED,
                        new String[] {
                                getCustomer().getNameWithOrganizationId(),
                                voServiceDetails.getServiceId() });
                break;
            case PRICEMODEL_FOR_SUBSCRIPTION:
                voServiceDetails = getProvisioningService().savePriceModelForSubscription(voServiceDetails, voPriceModel);
                saveLocalization(voServiceDetails);
                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_PRICEMODEL_FOR_SUBSCRIPTION_SAVED,
                        new String[] { getSubscriptionID(),
                                getCustomer().getNameWithOrganizationId() });
                break;
            default:
                throw new IllegalStateException(
                        "PRICEMODEL OF UNKNOWN TYPE" + getCurrentPMPage());
            }
        } catch (SaaSApplicationException ex) {
            if ((ex instanceof ObjectNotFoundException)
                    && ((ObjectNotFoundException) ex)
                            .getDomainObjectClassEnum() == ClassEnum.SERVICE) {
                ex.setMessageParams(new String[] { storedServiceId });
            }
            throw ex;
        }

        externalPriceModelModel.setSavedByUser(true);
        setSelectedService(voServiceDetails);
        updatePriceModel();
        getLocalization();
        dirty = false;

        // If the price model is saved successfully, set this boolean to true.
        isSaved = true;

        return OUTCOME_SUCCESS;
    }

    /**
     * Saves the description and short description for the price model of the
     * given service.
     * 
     * @param voServiceDetails
     *            the service with the price model for which the save is
     *            performed
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     * @throws ConcurrentModificationException
     */
    private void saveLocalization(VOServiceDetails voServiceDetails)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ConcurrentModificationException {
        if (localization != null) {
            // We need to remove current localization from the list as it has been saved already during price model
            // saving in {@link #save() save()}
            VOPriceModelLocalization newLocalization = new VOPriceModelLocalization();
            List<VOLocalizedText> licenses = localization.getLicenses();
            List<VOLocalizedText> descriptions = localization.getDescriptions();
            newLocalization.setLicenses(removeCurrentLocalizationFromList(licenses));
            newLocalization.setDescriptions(removeCurrentLocalizationFromList(descriptions));
            getProvisioningService().savePriceModelLocalization(
                    voServiceDetails.getPriceModel(), newLocalization);
        }
    }


    private List<VOLocalizedText> removeCurrentLocalizationFromList(List<VOLocalizedText> textsToFilter) {
        List<VOLocalizedText> localizatedTextTostore = new ArrayList<>();
        String currentUserLocale = appBean.getUserFromSessionWithoutException().getLocale();
        for (VOLocalizedText license : textsToFilter) {
            if (!license.getLocale().equals(currentUserLocale)) {
                localizatedTextTostore.add(license);
            }
        }
        return localizatedTextTostore;
    }

    /**
     * Sets the save localized values flag to true and redirects to initial
     * price model URL.
     * 
     * @return the logical outcome.
     */
    public String saveLocalizedValues() {
        setLocalizeVisible(false);
        dirty = true;
        menuBean.setCurrentPageLink(initUrl);

        return null;
    }

    // -- part for specific priced roles --
    /**
     * Getting name of selected role for showing in GUI.
     * 
     * @return String Name of selected role.
     */
    public String getSelectedRoleName() {
        return getSelectedRole().getName();
    }

    /**
     * Setter for service roles.
     * 
     * @param roles
     *            Roles list.
     */
    public void setRoles(List<VORoleDefinition> roles) {
        this.roles = roles;
    }

    /**
     * Getter for service roles. Initialize roles and specific parameter prices.
     * 
     * @return List of service roles.
     * 
     */
    public List<VORoleDefinition> getRoles() {
        if (roles == null) {
            VOServiceDetails vopd = getSelectedService();
            if (vopd != null) {
                VOTechnicalService technicalService = vopd
                        .getTechnicalService();
                roles = technicalService.getRoleDefinitions();

                initRoleSpecificUserPrice();

                initParametersForRole();
            }
        }
        return roles;
    }

    /**
     * Setter for selected role. This roles is displayed in combobox.
     * 
     * @param selectedRole
     *            Selected role.
     */
    public void setSelectedRole(VORoleDefinition selectedRole) {
        this.selectedRole = selectedRole;
    }

    /**
     * Getter for selected role.
     * 
     * @return Selected role.
     */
    public VORoleDefinition getSelectedRole() {
        return selectedRole;
    }

    /**
     * Get the id of the selected role.
     * 
     * @return the id of the selected role or null if roles is not selected.
     */
    public String getSelectedRoleId() {
        if (selectedRole == null) {
            return null;
        }
        return selectedRole.getRoleId();
    }

    /**
     * Set selected role id.
     * 
     * @param roleId
     *            id of the selected role
     */
    public void setSelectedRoleId(String roleId) {
        if ((roleId == null) || (roleId.equals(""))) {
            selectedRole = null;
        } else {
            for (VORoleDefinition curRole : getRoles()) {
                if (curRole.getRoleId().equals(roleId)) {
                    selectedRole = curRole;

                    updateRole();

                    break;
                }
            }
        }
    }

    /**
     * Update role and prices for displaying in GUI.
     * 
     */
    public void updateRole() {

        // clear displayed values
        clearParametersSelectedRole();
        pricePerUserSelectedRole = BigDecimal.ZERO;

        if (selectedRole != null) {
            // initialize values for current role
            int index = getSelectedRoleIndex();
            if (index != -1) {
                // initialize parameter specific role prices for all
                // parametersRoles
                // and options for selected role from internal cache
                int paramNum = pricedParametersOfAllRoles[index].length;
                for (int i = 0; i < paramNum; i++) {
                    if ((pricedParametersOfSelectedRole[i]
                            .getPricedParameter() != null)
                            && (pricedParametersOfAllRoles[index][i]
                                    .getPricedParameter() != null)) {
                        pricedParametersOfSelectedRole[i].getPricedParameter()
                                .setPricePerUser(
                                        pricedParametersOfAllRoles[index][i]
                                                .getPricedParameter()
                                                .getPricePerUser());
                    }
                    if ((pricedParametersOfSelectedRole[i]
                            .getPricedOption() != null)
                            && (pricedParametersOfAllRoles[index][i]
                                    .getPricedOption() != null)) {
                        pricedParametersOfSelectedRole[i].getPricedOption()
                                .setPricePerUser(
                                        pricedParametersOfAllRoles[index][i]
                                                .getPricedOption()
                                                .getPricePerUser());
                    }
                }
                // initialize user role price
                pricePerUserSelectedRole = priceModelPricedRoles[index]
                        .getPricePerUser();
            }
        }

    }

    /**
     * Setting priced parameters for selected role.
     * 
     * @param inputRow
     *            parameters for selected role Value to set.
     */
    public void setParametersSelectedRole(PricedParameterRow[] inputRow) {
        this.pricedParametersOfSelectedRole = inputRow;
    }

    /**
     * Getting priced parameters for selected role.
     * 
     * @return List of parameters for selected role.
     */
    public PricedParameterRow[] getParametersSelectedRole() {
        return pricedParametersOfSelectedRole;
    }

    /**
     * Setter of user price for selected role.
     * 
     * @param pricePerUserCurrentRole
     *            Current role price per user
     */
    public void setPricePerUserCurrentRole(BigDecimal pricePerUserCurrentRole) {
        this.pricePerUserSelectedRole = pricePerUserCurrentRole;
    }

    /**
     * Getter for current user price for role.
     * 
     * @return User price for selected role.
     */
    public BigDecimal getPricePerUserCurrentRole() {
        int index = getSelectedRoleIndex();
        if (index != -1) {
            pricePerUserSelectedRole = priceModelPricedRoles[index]
                    .getPricePerUser();
        }
        return pricePerUserSelectedRole;
    }

    /**
     * Write specific parameter-role prices from displayed arrays to cache array
     * for further using.
     * 
     */
    public void saveRoles() {

        int index = getSelectedRoleIndex();
        if (index != -1) {
            // save user specific price for role
            priceModelPricedRoles[index]
                    .setPricePerUser(pricePerUserSelectedRole);
            // save parameter prices for role
            int paramNum = pricedParametersOfSelectedRole.length;
            for (int i = 0; i < paramNum; i++) {
                if ((pricedParametersOfSelectedRole[i]
                        .getPricedParameter() != null)
                        && (pricedParametersOfAllRoles[index][i]
                                .getPricedParameter() != null)) {
                    pricedParametersOfAllRoles[index][i].getPricedParameter()
                            .setPricePerUser(pricedParametersOfSelectedRole[i]
                                    .getPricedParameter().getPricePerUser());
                }
                if ((pricedParametersOfSelectedRole[i]
                        .getPricedOption() != null)
                        && (pricedParametersOfAllRoles[index][i]
                                .getPricedOption() != null)) {
                    pricedParametersOfAllRoles[index][i].getPricedOption()
                            .setPricePerUser(pricedParametersOfSelectedRole[i]
                                    .getPricedOption().getPricePerUser());
                }
            }

        }

    }

    /**
     * Predicate for checking is role selected.
     * 
     * @return Confirmation about no selected role.
     */
    public boolean isNoSelectedRole() {
        if (selectedRole == null) {
            return true;
        }
        return false;
    }

    /**
     * Predicate for checking are there roles. Needed for enabling a button on
     * GUI.
     * 
     * @return Confirmation about no selected role.
     */
    public boolean isRoleExists() {
        List<VORoleDefinition> rolesTmp = getRoles();
        if (rolesTmp != null && rolesTmp.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Return index of selected role.
     * 
     * @return Index of selected role, zero based. -1 if error.
     */
    private int getSelectedRoleIndex() {
        List<VORoleDefinition> rolesLocal = getRoles();

        int result = -1;
        if (rolesLocal != null) {
            for (int i = 0; i < rolesLocal.size(); i++) {
                VORoleDefinition curRole = rolesLocal.get(i);
                if (curRole.equals(selectedRole)) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Allocate and initialize role specific user price.
     */
    private void initRoleSpecificUserPrice() {

        // number of roles
        int roleCount = roles.size();

        // allocate array for user prices for role.
        priceModelPricedRoles = new VOPricedRole[roleCount];

        // initialize user prices for role to 0
        // it is needed for situation when there are no saved prices yet.
        // if no price will be inputed, the value 0 will be saved
        for (int i = 0; i < roleCount; i++) {
            priceModelPricedRoles[i] = new VOPricedRole();
        }

        // initialize user prices for role to actual values
        if (priceModel != null) {
            List<VOPricedRole> roleSpecificUserPrice = priceModel
                    .getRoleSpecificUserPrices();
            if (roleSpecificUserPrice != null) {
                for (int i = 0; i < roleCount; i++) {
                    VORoleDefinition curRole = roles.get(i);
                    for (int j = 0; j < roleSpecificUserPrice.size(); j++) {
                        VOPricedRole curUserPrice = roleSpecificUserPrice
                                .get(j);
                        if (curRole.getRoleId()
                                .equals(curUserPrice.getRole().getRoleId())) {
                            priceModelPricedRoles[i] = curUserPrice;
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize specific parameters price for roles.
     */
    private void initParametersForRole() {

        // number of roles
        int numRoles = 0;
        if (roles != null) {
            numRoles = roles.size();
        }

        // number of rows in parameters and option GUI display table
        int numParam = 0;
        if (parametersRoles != null) {
            numParam = parametersRoles.size();
        }

        // specific prices of parameters and options for one selected role
        // these values are displayed
        pricedParametersOfSelectedRole = new PricedParameterRow[numParam];

        // specific prices of parameters and options for all roles. This is a
        // cache for saving prices for all roles before saving.
        pricedParametersOfAllRoles = new PricedParameterRow[numRoles][numParam];

        // initialize parameters for selected role for GUI
        List<PricedParameterRow> paramForSelectedRole = PricedParameterRow
                .createPricedParameterRowListForPriceModelRoles(
                        selectedService.getVoServiceDetails());
        for (int i = 0; i < numParam; i++) {
            pricedParametersOfSelectedRole[i] = copyParameterRow(
                    paramForSelectedRole.get(i));
        }

        // for all roles the same initial values, but different object list
        // create for all parametersRoles and options values with price == 0
        for (int i = 0; i < numRoles; i++) {
            List<PricedParameterRow> paramForRoleInCash = PricedParameterRow
                    .createPricedParameterRowListForPriceModelRoles(
                            selectedService.getVoServiceDetails());
            for (int j = 0; j < numParam; j++) {
                pricedParametersOfAllRoles[i][j] = copyParameterRow(
                        paramForRoleInCash.get(j));
            }
        }

        // initialize with real prices
        for (int indexForParameters = 0; indexForParameters < numParam; indexForParameters++) {
            PricedParameterRow curParameter = parametersRoles
                    .get(indexForParameters);
            if (!curParameter.isOption()) {
                // looking for only for parameter entry, not for option
                if (curParameter.getParameterDefinition()
                        .getValueType() != ParameterValueType.ENUMERATION) {
                    initNotEnumerationParameter(indexForParameters,
                            curParameter);

                } else {
                    initEnumerationParameter(curParameter);
                }
            }
        }
    }

    /**
     * Initialize specific user prices for roles of parameters with options -
     * enumeration type.
     * 
     * @param curParameter
     *            Parameter for initialization.
     * @TODO Optimize this method as method for adding options role prices
     *       before price model saving. Initialization method uses compare of
     *       keys, it will work as reason keys are initialized or equals 0, in
     *       this case prices are also 0 - the first saving of price model.
     * 
     */
    private void initEnumerationParameter(PricedParameterRow curParameter) {
        // this is parameter with options
        // get all priced option for the parameter
        List<VOPricedOption> pricedOptionList = curParameter
                .getPricedParameter().getPricedOptions();
        // for every option looking for role price
        for (int optionIndex = 0; optionIndex < pricedOptionList
                .size(); optionIndex++) {
            VOPricedOption pricedOption = pricedOptionList.get(optionIndex);
            // list of role prices for one current option
            List<VOPricedRole> oldRoleSpecificUserPrices = pricedOption
                    .getRoleSpecificUserPrices();
            // looking for position in displayed parameter-option
            // list for the option
            for (int indexInCache = 0; indexInCache < parametersRoles
                    .size(); indexInCache++) {
                VOPricedOption curPricedOption = parametersRoles
                        .get(indexInCache).getPricedOption();
                if (curPricedOption != null) {
                    // this is option
                    if (pricedOption.getKey() == curPricedOption.getKey()) {
                        // this is needed position for option in
                        // cache
                        // looking through cache for every role
                        for (int j = 0; j < roles.size(); j++) {
                            VORoleDefinition role = roles.get(j);

                            for (int roleIndex = 0; roleIndex < oldRoleSpecificUserPrices
                                    .size(); roleIndex++) {
                                VORoleDefinition oldCurRole = oldRoleSpecificUserPrices
                                        .get(roleIndex).getRole();
                                if (oldCurRole.getRoleId()
                                        .equals(role.getRoleId())) {
                                    // role is already has price
                                    // just change old price to new
                                    // price
                                    BigDecimal pricePerUser = oldRoleSpecificUserPrices
                                            .get(roleIndex).getPricePerUser();
                                    VOPricedOption po = pricedParametersOfAllRoles[j][indexInCache]
                                            .getPricedOption();
                                    po.setPricePerUser(pricePerUser);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize specific user prices for roles of parameters without options -
     * not enumeration type.
     * 
     * @param indexForParameters
     *            Number in parameter-option list.
     * @param curParameter
     *            Parameter for initialization.
     */
    private void initNotEnumerationParameter(int indexForParameters,
            PricedParameterRow curParameter) {
        // specific prices for all roles
        List<VOPricedRole> roleSpecificUserPrices = curParameter
                .getPricedParameter().getRoleSpecificUserPrices();
        if (roleSpecificUserPrices != null) {
            for (VOPricedRole rolePrice : roleSpecificUserPrices) {
                // define, which role is it
                VORoleDefinition curParamRole = rolePrice.getRole();
                for (int roleIndex = 0; roleIndex < roles.size(); roleIndex++) {
                    if (curParamRole.getRoleId()
                            .equals(roles.get(roleIndex).getRoleId())) {
                        pricedParametersOfAllRoles[roleIndex][indexForParameters]
                                .getPricedParameter()
                                .setPricePerUser(rolePrice.getPricePerUser());
                    }
                }
            }
        }
    }

    /**
     * Prepare parameterRow with new internal member pricedParameter and
     * pricedOption. We need copy of objects, because we use the same structure
     * for another functionality. Without copy we get problems with object
     * references.
     * 
     * @param pricedParameterRow
     *            Original parameterRow.
     * @return parameterRow with new internal member pricedParameter and
     *         pricedOption.
     */
    private PricedParameterRow copyParameterRow(
            PricedParameterRow pricedParameterRow) {

        PricedParameterRow paramRow = pricedParameterRow;
        if (paramRow.getPricedParameter() != null) {
            VOPricedParameter paramOld = paramRow.getPricedParameter();
            VOPricedParameter paramNew = copyParameter(paramOld);

            paramNew.setPricePerUser(BigDecimal.ZERO);
            paramRow.setPricedParameter(paramNew);
        }
        if (paramRow.getPricedOption() != null) {
            VOPricedOption optionOld = paramRow.getPricedOption();
            VOPricedOption optionNew = copyOption(optionOld);

            optionNew.setPricePerUser(BigDecimal.ZERO);
            paramRow.setPricedOption(optionNew);
        }
        return paramRow;
    }

    /**
     * Initialize prices for parameter roles prices.
     */
    private void clearParametersSelectedRole() {
        int paramNum = pricedParametersOfSelectedRole.length;
        for (int i = 0; i < paramNum; i++) {
            if (pricedParametersOfSelectedRole[i]
                    .getPricedParameter() != null) {
                pricedParametersOfSelectedRole[i].getPricedParameter()
                        .setPricePerUser(BigDecimal.ZERO);
            }
            if (pricedParametersOfSelectedRole[i].getPricedOption() != null) {
                pricedParametersOfSelectedRole[i].getPricedOption()
                        .setPricePerUser(BigDecimal.ZERO);
            }
        }
    }

    /**
     * Copy parameter. We need copy of objects, because we use the same
     * structure for another functionality. Without copy we get problems with
     * object references.
     * 
     * @param paramOld
     * @return Copied parameter.
     */
    private VOPricedParameter copyParameter(VOPricedParameter paramOld) {

        VOPricedParameter paramNew = new VOPricedParameter();

        paramNew.setKey(paramOld.getKey());
        paramNew.setParameterKey(paramOld.getParameterKey());
        paramNew.setPricedOptions(paramOld.getPricedOptions());
        paramNew.setPricePerUser(paramOld.getPricePerUser());
        paramNew.setRoleSpecificUserPrices(
                paramOld.getRoleSpecificUserPrices());
        paramNew.setVersion(paramOld.getVersion());
        paramNew.setVoParameterDef(paramOld.getVoParameterDef());

        paramNew.setSteppedPrices(paramOld.getSteppedPrices());

        return paramNew;
    }

    /**
     * Copy option. We need copy of objects, because we use the same structure
     * for another functionality. Without copy we get problems with object
     * references.
     * 
     * @param optionOld
     * @return Copied option.
     */
    private VOPricedOption copyOption(VOPricedOption optionOld) {

        VOPricedOption optionNew = new VOPricedOption();

        optionNew.setKey(optionOld.getKey());
        optionNew.setParameterOptionKey(optionOld.getParameterOptionKey());
        optionNew.setPricePerSubscription(optionOld.getPricePerSubscription());
        optionNew.setPricePerUser(optionOld.getPricePerUser());
        optionNew.setRoleSpecificUserPrices(
                optionOld.getRoleSpecificUserPrices());
        optionNew.setVersion(optionOld.getVersion());

        return optionNew;
    }

    /**
     * Prepare price model for saving: fill list of user-role specific price
     * list.
     * 
     * @return User-role specific price list.
     */
    List<VOPricedRole> getRoleSpecificUserPricesForSaving() {
        // sets defined role specific user prices for the given price model
        final int numRoles = roles.size();
        List<VOPricedRole> roleSpecificUserPrices = new ArrayList<>();
        for (int i = 0; i < numRoles; i++) {
            priceModelPricedRoles[i].setRole(roles.get(i));
            roleSpecificUserPrices.add(priceModelPricedRoles[i]);
        }
        return roleSpecificUserPrices;
    }

    /**
     * Prepare price model for saving: add to parameters and options role
     * specific prices.
     * 
     */
    void addRoleSpecificPriceToParameters() {
        if (parametersRoles != null) {
            for (int paramIndex = 0; paramIndex < parametersRoles
                    .size(); paramIndex++) {
                PricedParameterRow curParam = parametersRoles.get(paramIndex);
                // looking for needed parameter in current cache
                if (!curParam.isOption()) {
                    // looking for only for parameter entry, not for option
                    if (curParam.getParameterDefinition()
                            .getValueType() != ParameterValueType.ENUMERATION) {
                        addForNotEnumeration(curParam, paramIndex);
                    } else {
                        addForEnumeration(curParam, paramIndex);
                    }
                }
            }
        }
    }

    /**
     * Prepare price model for saving: specific role prices of options are added
     * from cache.
     * 
     * @param curParam
     *            Current enumeration parameter.
     * @param paramIndex
     *            index in displayed array.
     */
    private void addForEnumeration(PricedParameterRow curParam,
            int paramIndex) {
        // get all priced option for the parameter
        List<VOPricedOption> pricedOptionList = curParam.getPricedParameter()
                .getPricedOptions();
        for (int optionIndex = 0; optionIndex < pricedOptionList
                .size(); optionIndex++) {
            VOPricedOption pricedOption = pricedOptionList.get(optionIndex);
            List<VOPricedRole> oldRoleSpecificUserPrices = pricedOption
                    .getRoleSpecificUserPrices();
            // looking for position in option list for the option
            int indexInCache = paramIndex + optionIndex + 1;
            // this is needed position for option in cache
            // looking through cache for every role
            for (int j = 0; j < roles.size(); j++) {
                VORoleDefinition role = roles.get(j);
                int numOldRoleWithPrice = oldRoleSpecificUserPrices.size();
                boolean isUpdated = false;
                for (int roleIndex = 0; roleIndex < numOldRoleWithPrice; roleIndex++) {
                    VORoleDefinition oldCurRole = oldRoleSpecificUserPrices
                            .get(roleIndex).getRole();
                    if (oldCurRole.getRoleId().equals(role.getRoleId())) {
                        // role is already has price
                        // just change old price to new price
                        BigDecimal price = pricedParametersOfAllRoles[j][indexInCache]
                                .getPricedOption().getPricePerUser();
                        oldRoleSpecificUserPrices.get(roleIndex)
                                .setPricePerUser(price);
                        isUpdated = true;
                        break;
                    }
                }
                if (!isUpdated) {
                    // if there is no specific price for such option
                    // - create new
                    // take prices for this role and add to list
                    VOPricedRole price = new VOPricedRole();
                    if (pricedParametersOfAllRoles[j][indexInCache]
                            .getPricedOption() != null) {
                        price.setPricePerUser(
                                pricedParametersOfAllRoles[j][indexInCache]
                                        .getPricedOption().getPricePerUser());
                        price.setRole(role);
                    }
                    oldRoleSpecificUserPrices.add(price);
                }
            }
        }
    }

    /**
     * Prepare price model for saving: specific role prices of not enumeration
     * parameters are added from cache.
     * 
     * @param curParam
     *            Current parameter.
     * @param paramIndex
     *            parameter index in displayed array.
     */
    private void addForNotEnumeration(PricedParameterRow curParam,
            int paramIndex) {
        List<VOPricedRole> oldRoleSpecificUserPrices = curParam
                .getPricedParameter().getRoleSpecificUserPrices();
        // for parameter without option looking through cache
        // values for every role
        for (int j = 0; j < roles.size(); j++) {
            VORoleDefinition role = roles.get(j);
            // check if the role already has old price, change
            // only price
            int numOldRoleWithPrice = oldRoleSpecificUserPrices.size();
            boolean isUpdated = false;
            for (int roleIndex = 0; roleIndex < numOldRoleWithPrice; roleIndex++) {
                VORoleDefinition oldCurRole = oldRoleSpecificUserPrices
                        .get(roleIndex).getRole();
                if (oldCurRole.getRoleId().equals(role.getRoleId())) {
                    // role is already has price
                    // just change old price to new price
                    oldRoleSpecificUserPrices.get(roleIndex).setPricePerUser(
                            pricedParametersOfAllRoles[j][paramIndex]
                                    .getPricedParameter().getPricePerUser());
                    isUpdated = true;
                }
            }
            if (!isUpdated) {
                // if there is no specific price for such option - create new
                // take prices for this role and add to list
                VOPricedRole price = new VOPricedRole();
                if (pricedParametersOfAllRoles[j][paramIndex]
                        .getPricedParameter() != null) {
                    price.setPricePerUser(
                            pricedParametersOfAllRoles[j][paramIndex]
                                    .getPricedParameter().getPricePerUser());
                    price.setRole(role);
                }
                oldRoleSpecificUserPrices.add(price);
            }
        }
    }

    public boolean isDirectAccess() {
        VOServiceDetails svc = getSelectedService();
        if (svc != null) {
            return svc.getAccessType() == ServiceAccessType.DIRECT;
        }
        return false;
    }

    /**
     * Return true if no price model is selected or the current price model is
     * not chargeable.
     * 
     * @return true if not price model is selected or the current price model is
     *         not chargeable.
     */
    public boolean isPriceModelFree() {
        VOPriceModel priceModel = getPriceModel();
        if (priceModel == null) {
            return true;
        }
        return !priceModel.isChargeable();
    }

    /**
     * Set the price model type of the currently selected price model.
     * 
     * @param priceModelType
     * 
     */
    public void setSelectedPriceModelType(String priceModelType) {
        VOPriceModel priceModel = getPriceModel();
        if (priceModel != null) {
            if (priceModelType.equals("FREE_OF_CHARGE")) {
                resetPriceModelToFree();
            } else if (priceModelType.equals("PRO_RATA")) {
                priceModel.setType(PriceModelType.PRO_RATA);

            } else if (priceModelType.equals("PER_UNIT")) {
                priceModel.setType(PriceModelType.PER_UNIT);
            }
        }
    }

    public String getSelectedPriceModelType() {
        VOPriceModel priceModel = getPriceModel();
        if (priceModel == null) {
            return "FREE_OF_CHARGE";
        }
        return priceModel.getType().name();
    }

    public void reloadPriceModel(ValueChangeEvent event) {
        this.selectedServiceKey = Long.class.cast(event.getNewValue());
        sessionBean.setSelectedServiceKeyForSupplier(this.selectedServiceKey);
        updatePriceModel();
        if (getCurrentPMPage() == PRICEMODEL_FOR_SERVICE) {
            extServiceBean.reloadPriceModel(getSelectedService());
        } else if(getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER) {
            extCustBean.reloadPriceModel(getSelectedService());
        }
    }

    /**
     * Resets the price model with the values fetched from the server and sets
     * it to non-chargeable.
     */
    private void resetPriceModelToFree() {
        steppedPrices = null;
        pricedEvents = null;
        parameters = null;
        priceModel = null;
        templatePriceModel = null;

        updateVOServiceDetails();
        if (selectedService == null) {
            sessionBean.setSelectedServiceKeyForSupplier(null);
            return;
        }
        sessionBean.setSelectedServiceKeyForSupplier(
                Long.valueOf(selectedService.getKey()));
        if (templatePriceModel != null) {
            priceModel = templatePriceModel;
        } else {
            priceModel = selectedService.getPriceModel();
        }

        // Set the price model to free of charge.
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);

        steppedPrices = priceModel.getSteppedPrices();
        Collections.sort(steppedPrices, new SteppedPriceComparator());

        pricedEvents = PricedEventRow.createPricedEventRowList(
                selectedService.getVoServiceDetails());

        parameters = PricedParameterRow
                .createPricedParameterRowListForPriceModel(
                        selectedService.getVoServiceDetails());

        parametersRoles = PricedParameterRow
                .createPricedParameterRowListForPriceModelRoles(
                        selectedService.getVoServiceDetails());

    }

    /**
     * Predicate for checking if the selected service is published in a global
     * marketplace.
     * 
     * @return true if the selected service is published in a global
     *         marketplace.
     */
    public boolean isPublishedInGlobalMarketPlace() {
        return publishedInGlobalMarketPlace;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public boolean isFreePeriodAvailable() {
        VOPriceModel priceModel = getPriceModel();
        if (priceModel == null) {
            return false;
        }
        return priceModel.getFreePeriod() > 0;
    }

    public void setFreePeriodAvailable(
            @SuppressWarnings("unused") boolean isFreePeriodAvailable) {
        // not persisted, calculated from VOPriceModel.freePeriod
    }

    /**
     * Needed by <code>ParameterValueValidator</code> to validate the minimum
     * value.
     * 
     * @return The minimum value as <code>Long</code>, as expected by the
     *         ParameterValueValidator, in order to reuse it.
     */
    public Long getFreePeriodMinValue() {
        return Long.valueOf(0L);
    }

    /**
     * Needed by <code>ParameterValueValidator</code> to validate the maximum
     * value.
     * 
     * @return The maximum value as <code>Long</code>, as expected by the
     *         ParameterValueValidator, in order to reuse it.
     */
    public Long getFreePeriodMaxValue() {
        return Long.valueOf(Integer.MAX_VALUE);
    }

    protected PartnerService getParterService() {
        return sl.findService(PartnerService.class);
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

    String handleErrorMessage(String messageKey, Object[] params) {
        addMessage(null, FacesMessage.SEVERITY_ERROR, messageKey, params);
        dirty = false;
        ui.resetDirty();
        return OUTCOME_ERROR;
    }

    void throwObjectNotFoundException(ClassEnum objectClass, String objectId)
            throws ObjectNotFoundException {
        ObjectNotFoundException ex = new ObjectNotFoundException(objectClass,
                objectId);
        dirty = false;
        throw ex;
    }

    public boolean isLocalizeVisible() {
        return localizeVisible;
    }

    public void setLocalizeVisible(boolean localizeVisible) {
        this.localizeVisible = localizeVisible;
    }

    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    public BPLazyDataModel getModel() {
        return model;
    }

    public void setModel(BPLazyDataModel model) {
        this.model = model;
    }

    public SubscriptionViewBean getSubscriptionViewBean() {
        return subscriptionViewBean;
    }

    public void setSubscriptionViewBean(
            SubscriptionViewBean subscriptionViewBean) {
        this.subscriptionViewBean = subscriptionViewBean;
    }

    /**
     * @return the isExternalPriceModelUploaded
     */
    public boolean isExternalPriceModelUploaded() {
        return isExternalPriceModelUploaded;
    }

    /**
     * @param isExternalPriceModelUploaded the isExternalPriceModelUploaded to set
     */
    public void setExternalPriceModelUploaded(
            boolean isExternalPriceModelUploaded) {
        this.isExternalPriceModelUploaded = isExternalPriceModelUploaded;
    }

    /**
     * @return the extCustBean
     */
    public ExternalCustomerPriceModelCtrl getExtCustBean() {
        return extCustBean;
    }

    /**
     * @param extCustBean the extCustBean to set
     */
    public void setExtCustBean(ExternalCustomerPriceModelCtrl extCustBean) {
        this.extCustBean = extCustBean;
    }

    /**
     * @return the extServiceBean
     */
    public ExternalServicePriceModelCtrl getExtServiceBean() {
        return extServiceBean;
    }

    /**
     * @param extServiceBean the extServiceBean to set
     */
    public void setExtServiceBean(ExternalServicePriceModelCtrl extServiceBean) {
        this.extServiceBean = extServiceBean;
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

    public void upload() throws SaaSApplicationException {
        if (getCurrentPMPage() == PRICEMODEL_FOR_SERVICE) {
            extServiceBean.upload(getSelectedService());
        } else if (getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER) {
            extCustBean.upload(getSelectedService(),
                    getCustomer().getVOOrganization());
        } else if (getCurrentPMPage() == PRICEMODEL_FOR_SUBSCRIPTION) {
            extSubBean.upload(validateSubscription(getSelectedSubscription()));
            setExternalPriceModelUploaded(true);
        }
        setDirty(true);
    }

    public void display() throws SaaSApplicationException, IOException {
        if (getCurrentPMPage() == PRICEMODEL_FOR_SERVICE) {
            extServiceBean.display();
        } else if (getCurrentPMPage() == PRICEMODEL_FOR_CUSTOMER) {
            extCustBean.display();
        } else if (getCurrentPMPage() == PRICEMODEL_FOR_SUBSCRIPTION) {
            if (getSelectedSubscription() == null) {
                return;
            }
            extSubBean.display(isExternalPriceModelUploaded,
                    getSelectedSubscription().getSubscribedService());
        }
    }

    public VOSubscriptionDetails validateSubscription(
            VOSubscriptionDetails subscription)
                    throws SaaSApplicationException {
        if (subscription == null || subscription.getPriceModel() == null) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXTERNAL_PRICEMODEL_NOT_AVAILABLE);
            return null;
        }
        if (!subscription.getPriceModel().isExternal()) {
            return null;
        }
        try {
            return validateSubscription(subscription.getSubscribedService());
        } catch (SaaSApplicationException e) {
            if (e instanceof SubscriptionStateException) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_SUBSCRIPTION_NOT_ACCESSIBLE,
                        new String[] { subscription.getSubscriptionId() });
                setDirty(false);
                return null;
            }
            throw e;
        }
    }
}
