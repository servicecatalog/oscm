/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pock                                                      
 *                                                                              
 *  Creation Date: 10.09.2009                                                      
 *                                                                              
 *  Completion Time: <date>                                     
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang3.StringUtils;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.PublishingToMarketplaceNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOCompatibleService;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceActivation;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceLocalization;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.ImageUploader;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.LocaleUtils;
import org.oscm.ui.generator.IdGenerator;
import org.oscm.ui.model.Category;
import org.oscm.ui.model.CategoryRow;
import org.oscm.ui.model.CustomerService;
import org.oscm.ui.model.ParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.model.ServiceDetails;

/**
 * Backing bean for service related actions
 * 
 */
@ViewScoped
@ManagedBean(name="serviceBean")
public class ServiceBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -8876550461022162039L;

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ServiceBean.class);

    private static final String SERVICE_NO_LONGER_EXISTS = "error.service.noLonger.exists";
    private static final String SERVICE_DELETED = "error.service.deleted";
    private static final String CANNOT_DELETE_ACTIVE_SERVICE = "error.service.cannot.delete.active";

    private List<VOTechnicalService> availableTechServices;
    private VOTechnicalService selectedTechService;
    private List<VOService> services;
    private ServiceDetails selectedService;
    private ServiceDetails serviceForCreation;
    private List<Service> possibleCompatibleServices;
    private VOServiceLocalization localization;
    private List<ParameterRow> parameterRows;
    private boolean saveLocalizedValues = false;
    private List<Service> servicesForDeActivation;
    private final ImageUploader imageUploader = new ImageUploader(
            ImageType.SERVICE_IMAGE);
    private boolean dirty;
    private Boolean partOfUpgradePath;
    private String newServiceId;

    @ManagedProperty(value="#{menuBean}")
    private MenuBean menuBean;

    @ManagedProperty(value="#{sessionBean}")
    private SessionBean sessionBean;

    @ManagedProperty(value="#{marketplaceBean}")
    private MarketplaceBean marketplaceBean;

    private List<CategoryRow> categorySelection;
    private VOCatalogEntry ce = null;
    boolean marketplaceChanged = false;
    transient ApplicationBean appBean;
    public static final String APPLICATION_BEAN = "appBean";
    
    private boolean localizeVisible = false;
    
    ApplicationBean getApplicationBean() {
        if (appBean == null) {
            appBean = ui.findBean(APPLICATION_BEAN);
        }
        return appBean;
    }

    /**
     * Define a new service.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     * @throws ImageException
     *             Thrown in case the access to the uploaded file failed.
     */
    public String create() throws SaaSApplicationException {
        if (serviceForCreation == null || selectedTechService == null) {
            return OUTCOME_ERROR;
        }

        // read public flag from service beforehand since create method will
        // always return false
        boolean isPublicService = serviceForCreation.isPublicService();

        rewritePasswordsValues();
        serviceForCreation = new ServiceDetails(
                cleanupParameter(serviceForCreation.getVoServiceDetails()));

        selectedService = new ServiceDetails(getProvisioningService()
                .createService(selectedTechService,
                        serviceForCreation.getVoServiceDetails(),
                        getImageUploader().getVOImageResource()));
        selectedService.setPublicService(isPublicService);

        boolean addedMsg = false;
        // gather selected categories
        List<VOCategory> categories = selectedCategoryToVOUnwrapper(this.categorySelection);

        try {
            marketplaceBean.publishService(selectedService, categories);
        } catch (ObjectNotFoundException e) {
            if (ClassEnum.MARKETPLACE == e.getDomainObjectClassEnum()) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_SERVICE_SAVED_MARKETPLACE_DELETED, new String[] {
                                selectedService.getServiceId(),
                                marketplaceBean.getMarketplace().getName(),
                                marketplaceBean.getMarketplace()
                                        .getMarketplaceId() });
                addedMsg = true;
            } else {
                throw e;
            }
        } catch (PublishingToMarketplaceNotPermittedException e) {
            addMessage(
                    null,
                    FacesMessage.SEVERITY_ERROR,
                    ERROR_SERVICE_SAVED_PUBLISHING_NOT_PERMITTED,
                    new String[] { selectedService.getServiceId(),
                            marketplaceBean.getMarketplace().getName(),
                            marketplaceBean.getMarketplace().getMarketplaceId() });
            addedMsg = true;
        }

        if (!addedMsg) {
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_SERVICE_CREATED,
                    selectedService.getServiceId());
        }

        sessionBean.setSelectedServiceKeyForSupplier(Long
                .valueOf(selectedService.getKey()));
        prepareParameters();
        services = null;

        // help the navigation to highlight the correct navigation item
        menuBean.setCurrentPageLink(MenuBean.LINK_SERVICE_EDIT);

        if (logger.isDebugLoggingEnabled()) {

        }
        return OUTCOME_SUCCESS;
    }

    private void rewritePasswordsValues() {
        for (ParameterRow parameterRow : parameterRows) {
            if (!parameterRow.getParameterDefinition().isValueTypePWD() & !parameterRow.isPasswordType()) {
                continue;
            }
            if (parameterRow.getPasswordValueToStore() == null || !parameterRow
                .getPasswordValueToStore().trim().equals(HIDDEN_PWD)) {
                parameterRow.getParameter().setValue(parameterRow.getPasswordValueToStore());
            }
        }
    }

    /**
     * Delete the selected service.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String delete() throws SaaSApplicationException {
        if (selectedService == null) {
            return OUTCOME_ERROR;
        }
        if (logger.isDebugLoggingEnabled()) {

        }

        try {
            getProvisioningService().deleteService(
                    selectedService.getVoServiceDetails());
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_SERVICE_DELETED,
                    selectedService.getServiceId());
            sessionBean.setSelectedServiceKeyForSupplier(null);
            selectedService = null;
            services = null;
        } catch (ServiceStateException sse) {
            Object[] params = null;
            String localizedStatus = JSFUtils.getText(
                    "ServiceStatus." + sse.getMessageParams()[1], params);
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    CANNOT_DELETE_ACTIVE_SERVICE, localizedStatus);
            return OUTCOME_ERROR;
        }

        if (logger.isDebugLoggingEnabled()) {

        }
        return OUTCOME_SUCCESS;
    }

    public boolean isDeleteDisabled() {
        boolean deleteAllowed = false;
        try {
            if (getSelectedService() != null
                    && getSelectedService().getVoServiceDetails() != null) {
                deleteAllowed = (getProvisioningService()
                        .statusAllowsDeletion(selectedService
                                .getVoServiceDetails()));
            }
        } catch (OperationNotPermittedException e) {
            deleteAllowed = false;
        } catch (ObjectNotFoundException e) {
            deleteAllowed = false;
        } catch (ConcurrentModificationException e) {
            deleteAllowed = false;
        }

        return getSelectedService() == null
                || getSelectedService().getVoServiceDetails() == null
                || getSelectedService().getStatus() == ServiceStatus.ACTIVE
                // check status for customer specific service
                || !deleteAllowed;
    }

    /**
     * Internal helper method to find a parameter value object in a given list.
     * 
     * @param list
     *            the list to be searched
     * @param parameterType
     *            the parameter type of the searched parameter
     * @param parameterId
     *            the parameter id of the searched parameter
     * @return the found parameter value object or null if no parameter value
     *         object is found.
     */
    private VOParameter findParameter(final List<VOParameter> list,
            final ParameterType parameterType, final String parameterId) {
        if (list != null) {
            for (VOParameter e : list) {
                VOParameterDefinition paramDef = e.getParameterDefinition();
                if (paramDef.getParameterType() == parameterType
                        && paramDef.getParameterId().equals(parameterId)) {
                    return e;
                }
            }
        }
        return null;
    }

    public List<VOTechnicalService> getAvailableTechServices() {
        if (availableTechServices == null) {
            try {
                availableTechServices = getProvisioningService()
                        .getTechnicalServices(OrganizationRoleType.SUPPLIER);
            } catch (OrganizationAuthoritiesException e) {
                ExceptionHandler.execute(e);
            }
        }
        return availableTechServices;
    }

    public VOServiceLocalization getLocalization()
            throws OperationNotPermittedException, ObjectNotFoundException {
        if (getSelectedService() == null
                || getSelectedService().getVoServiceDetails() == null) {
            localization = null;
        } else if (localization == null) {
            localization = getProvisioningService().getServiceLocalization(
                    getSelectedService().getVoServiceDetails());
            List<Locale> supportedLocales = getApplicationBean()
                    .getSupportedLocaleList();
            localization.setNames(LocaleUtils.trim(localization.getNames(),
                    supportedLocales.iterator()));
            localization
                    .setDescriptions(LocaleUtils.trim(
                            localization.getDescriptions(),
                            supportedLocales.iterator()));
            localization.setShortDescriptions(LocaleUtils.trim(
                    localization.getShortDescriptions(),
                    supportedLocales.iterator()));
            localization.setCustomTabNames(
                    LocaleUtils.trim(localization.getCustomTabNames(),
                            supportedLocales.iterator()));
        }
        return localization;
    }

    public void setLocalization(VOServiceLocalization localization) {
        this.localization = localization;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(final MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public List<Service> getPossibleCompatibleServices() {
        final ServiceDetails service = getSelectedService();
        if (service == null || service.getVoServiceDetails() == null) {
            return null;
        }
        if (possibleCompatibleServices == null) {
            try {
                final String mpName = getMarketplaceBean()
                        .getSelectedMarketplaceName();
                List<VOCompatibleService> list = getProvisioningService()
                        .getPotentialCompatibleServices(
                                service.getVoServiceDetails());
                Vo2ModelMapper<VOCompatibleService, Service> mapper = new Vo2ModelMapper<VOCompatibleService, Service>() {
                    @Override
                    public Service createModel(final VOCompatibleService vo) {
                        Service p = new Service(vo);
                        p.setSelected(vo.isCompatible());
                        p.setMarketplaceName(mpName);
                        return p;
                    }
                };
                possibleCompatibleServices = mapper.map(list);
            } catch (SaaSApplicationException e) {
                ExceptionHandler.execute(e);
            }
        }
        return possibleCompatibleServices;
    }

    public List<VOService> getServices() {
        return getServices(PerformanceHint.ALL_FIELDS);
    }

    public List<VOService> getServiceNames() {
        return getServices(PerformanceHint.ONLY_IDENTIFYING_FIELDS);
    }

    private List<VOService> getServices(PerformanceHint performanceHint) {
        if (services == null) {
            services = getProvisioningServiceInternal().getSuppliedServices(
                    performanceHint);
        }
        return services;
    }

    public ServiceDetails getSelectedService() {
        if (selectedService == null) {
            Long key = sessionBean.getSelectedServiceKeyForSupplier();
            if (key != null && inList(key.longValue(), getServiceNames())) {
                setSelectedServiceKey(key.longValue());
            } else {
                sessionBean.setSelectedServiceKeyForSupplier(null);
            }
        }
        return selectedService;
    }

    boolean inList(long key, List<VOService> list) {
        for (VOService svc : list) {
            if (key == svc.getKey()) {
                return true;
            }
        }
        return false;
    }

    public long getSelectedServiceKey() {
        ServiceDetails service = getSelectedService();
        if (service == null) {
            return 0;
        }
        return service.getKey();
    }

    public long getSelectedServiceKeyReadOnly() {
        return getSelectedServiceKey();
    }

    public VOTechnicalService getSelectedTechService() {
        if (selectedTechService == null) {
            long key = sessionBean.getSelectedTechnicalServiceKey();
            if (key > 0) {
                setSelectedTechServiceKey(key);
            } else {
                parameterRows = null;
            }
        }
        return selectedTechService;
    }

    public long getSelectedTechServiceKey() {
        VOTechnicalService techService = getSelectedTechService();
        if (techService == null) {
            return 0;
        }
        return techService.getKey();
    }

    public long getSelectedTechServiceKeyReadOnly() {
        return getSelectedTechServiceKey();
    }

    /**
     * Merge the set parameter of the selected service with the possible
     * parameter definitions and initialize the paramtersRow array.
     */
    private void prepareParameters() {
        // merge the set parameter with the possible parameter definitions
        List<VOParameter> parameters = new ArrayList<VOParameter>();
        if (selectedService.getTechnicalService().getParameterDefinitions() != null) {
            for (VOParameterDefinition paramDef : selectedService
                    .getTechnicalService().getParameterDefinitions()) {
                VOParameter parameter = findParameter(
                        selectedService.getParameters(),
                        paramDef.getParameterType(), paramDef.getParameterId());
                if (parameter == null) {
                    parameter = new VOParameter(paramDef);
                }
                parameters.add(parameter);
            }
            selectedService.setParameters(parameters);
        }
        initParameterRows(parameters, false);
    }

    /**
     * Initialize the paramtersRows array.
     * 
     * @param parameters
     *            parameters for the array.
     */
    private void initParameterRows(List<VOParameter> parameters,
            boolean initDefault) {
        parameterRows = new ArrayList<ParameterRow>();
        for (VOParameter voParameter : parameters) {
            VOParameterDefinition parameterDefinition = voParameter
                    .getParameterDefinition();
            ParameterRow row = new ParameterRow(voParameter, null, initDefault);

            if (row.getParameterDefinition().isValueTypeSecret()) {
                if (StringUtils.isNotBlank(row.getParameter().getValue())) {
                    row.setPasswordValueToStore(HIDDEN_PWD);
                } else {
                    row.setPasswordValueToStore("");
                }
            }
            parameterRows.add(row);
            if (parameterDefinition.getValueType() == ParameterValueType.ENUMERATION) {
                int optionIndex = 0;
                for (VOParameterOption option : parameterDefinition
                        .getParameterOptions()) {
                    ParameterRow optionRow = new ParameterRow(voParameter,
                            option, initDefault);
                    optionRow.setOptionIndex(optionIndex);
                    optionIndex++;
                    parameterRows.add(optionRow);
                }
            }
        }
    }

    /**
     * Save the localized values for the selected service in the database.
     * 
     * @return the logical outcome.
     */
    public String saveLocalizedValues() {
    	setLocalizeVisible(false);
    	
    	if (selectedService == null) {
            return OUTCOME_ERROR;
        }

        saveLocalizedValues = true;
        if (selectedService != null) {
            String locale = getUserFromSession().getLocale();
            selectedService.setName(LocaleUtils.get(localization.getNames(),
                    locale));
            selectedService.setShortDescription(LocaleUtils.get(
                    localization.getShortDescriptions(), locale));
            selectedService.setDescription(LocaleUtils.get(
                    localization.getDescriptions(), locale));
            selectedService.setCustomTabName(LocaleUtils.get(
                    localization.getCustomTabNames(), locale));
        }
        dirty = true;
        menuBean.setCurrentPageLink(MenuBean.LINK_SERVICE_EDIT);

        return null;
    }

    /**
     * Empty action.
     * 
     * @return the logical outcome OUTCOME_SUCCESS.
     */
    public String cancelLocalize() {
    	setLocalizeVisible(false);
        menuBean.setCurrentPageLink(MenuBean.LINK_SERVICE_EDIT);
        return null;
    }

    /**
     * Get localization an set the values the details page.
     * 
     * @return the logical outcome OUTCOME_SERVICE_LOCALIZE.
     */
    public String localize() throws OperationNotPermittedException,
            ObjectNotFoundException {
    	setLocalizeVisible(true);
        menuBean.setCurrentPageLink(MenuBean.LINK_SERVICE_EDIT);
        localization = null;
        setServiceAttributesToLocalization();
        return null;
    }

    /**
     * Transfers the name and description currently set on the service to the
     * corresponding localized texts.
     * 
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     */
    private void setServiceAttributesToLocalization()
            throws OperationNotPermittedException, ObjectNotFoundException {
        if (selectedService != null) {
            String locale = getUserFromSession().getLocale();
            LocaleUtils.set(getLocalization().getNames(), locale,
                    selectedService.getName());
            LocaleUtils.set(getLocalization().getDescriptions(), locale,
                    selectedService.getDescription());
            LocaleUtils.set(getLocalization().getShortDescriptions(), locale,
                    selectedService.getShortDescription());
            LocaleUtils.set(getLocalization().getCustomTabNames(), locale,
                    selectedService.getCustomTabName());
        }
    }

    /**
     * Save the upgrade options for the selected service.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     */
    public String saveUpgradeOptions() throws SaaSApplicationException {
        if (selectedService == null) {
            return OUTCOME_ERROR;
        }
        if (logger.isDebugLoggingEnabled()) {

        }

        List<VOService> compatibleServices = new ArrayList<VOService>();
        if (getPossibleCompatibleServices() != null) {
            for (Service service : getPossibleCompatibleServices()) {
                if (service.isSelected()) {
                    compatibleServices.add(service.getVO());
                }
            }
        }
        getProvisioningService().setCompatibleServices(
                selectedService.getVoServiceDetails(), compatibleServices);
        // cause compatible services to be reloaded (concurrency)
        possibleCompatibleServices = null;
        // reload selected service (concurrency)
        setSelectedServiceKey(getSelectedServiceKey());
        addMessage(null, FacesMessage.SEVERITY_INFO,
                INFO_SERVICE_UPGRADEOPTIONS_SAVED,
                selectedService.getServiceId());

        if (logger.isDebugLoggingEnabled()) {

        }
        return OUTCOME_SUCCESS;
    }

    public void setSelectedServiceKey(final long key) {
        selectedService = null;
        localization = null;
        possibleCompatibleServices = null;
        parameterRows = null;
        newServiceId = null;
        partOfUpgradePath = null;
        sessionBean.setSelectedServiceKeyForSupplier(null);
        if (key == 0) {
            services = null;
            resetUIInputChildren();
            marketplaceBean.setMarketplaceId(null);
            return;
        }
        try {
            VOService service = new VOService();
            service.setKey(key);

            VOServiceDetails serviceDetails = getProvisioningService()
                    .getServiceDetails(service);
            if (serviceDetails == null) {
                services = null;
                resetUIInputChildren(); // Bug#7589
                marketplaceBean.setMarketplaceId(null);
                return;
            }

            // get the service details value object which contains all possible
            // parameter definitions
            selectedService = new ServiceDetails(serviceDetails);
            updateMarketplacePublishedTo();
            sessionBean.setSelectedServiceKeyForSupplier(Long.valueOf(key));
            prepareParameters();
        } catch (SaaSApplicationException e) {
            ExceptionHandler.execute(e);
        }
        dirty = false;
        resetUIInputChildren(); // Bug#7589
    }

    private void updateMarketplacePublishedTo() throws SaaSApplicationException {
        List<VOCatalogEntry> list = marketplaceBean
                .getMarketplacesForService(selectedService
                        .getVoServiceDetails());

        // use first retrieved marketplace from list
        if (!list.isEmpty()) {
            ce = list.get(0);
            selectedService.setPublicService(list.get(0).isAnonymousVisible());
            if (list.get(0).getMarketplace() != null) {
                marketplaceBean.setMarketplaceId(ce.getMarketplace()
                        .getMarketplaceId());
                setMarketplaceCategories(marketplaceBean.getMarketplaceId());
            } else {
                marketplaceBean.setMarketplaceId(null);
            }
        } else {
            marketplaceBean.setMarketplaceId(null);
        }

        if (ce != null && ce.getCategories() != null) {
            checkCategorySelection(ce.getCategories());
        }
    }

    /*
     * set those categoryRows checked that are already assigned to the
     * catalogEntry
     */
    private void checkCategorySelection(List<VOCategory> assignedCategories) {

        for (VOCategory assignedCat : assignedCategories) {
            for (CategoryRow cat : categorySelection) {
                if (cat.getCategory().getKey() == assignedCat.getKey()) {
                    cat.setSelected(true);
                    break;
                }
            }
        }
    }

    // avoid JSF error during submit
    public void setSelectedServiceKeyReadOnly(
            @SuppressWarnings("unused") final long key) {
    }

    /*
     * method is being called when technical service is selected in creation
     * mode.
     */
    public void setSelectedTechServiceKey(final long key) {
        selectedTechService = null;
        serviceForCreation = null;
        ce = null;
        localization = null;
        possibleCompatibleServices = null;
        parameterRows = null;
        sessionBean.setSelectedTechnicalServiceKey(0);
        if (getAvailableTechServices() == null) {
            return;
        }
        for (VOTechnicalService techService : getAvailableTechServices()) {
            if (techService.getKey() == key) {
                selectedTechService = techService;
                sessionBean.setSelectedTechnicalServiceKey(key);
                serviceForCreation = new ServiceDetails(new VOServiceDetails());
                serviceForCreation.setDescription(selectedTechService
                        .getTechnicalServiceDescription());
                serviceForCreation.getVoServiceDetails().setTechnicalId(
                        selectedTechService.getTechnicalServiceId());
                serviceForCreation.getVoServiceDetails().setTechnicalService(
                        selectedTechService);
                List<VOParameter> parameters = new ArrayList<VOParameter>();
                if (selectedTechService.getParameterDefinitions() != null) {
                    for (VOParameterDefinition paramDef : selectedTechService
                            .getParameterDefinitions()) {
                        parameters.add(new VOParameter(paramDef));
                    }
                }
                serviceForCreation.getVoServiceDetails().setParameters(
                        parameters);
                initParameterRows(parameters, true);
                break;
            }
        }
        resetUIInputChildren(); // Bug#7589
    }

    // avoid JSF error during submit
    public void setSelectedTechServiceKeyReadOnly(
            @SuppressWarnings("unused") final long techServiceKey) {
    }

    /**
     * Update the selected service.
     * 
     * @return the logical outcome.
     * @throws SaaSApplicationException
     *             Thrown from the business logic.
     * @throws ImageException
     *             Thrown in case the access to the uploaded file failed.
     */
    public String update() throws SaaSApplicationException {
        if (selectedService == null) {
            return OUTCOME_ERROR;
        }
        if (logger.isDebugLoggingEnabled()) {

        }

        services = null; // refresh the list

        // read public flag from service beforehand since create method will
        // always return false
        boolean isPublicService = selectedService.isPublicService();
        rewritePasswordsValues();
        selectedService = new ServiceDetails(
                cleanupParameter(selectedService.getVoServiceDetails()));

        selectedService = new ServiceDetails(getProvisioningService()
                .updateService(selectedService.getVoServiceDetails(),
                        getImageUploader().getVOImageResource()));
        selectedService.setPublicService(isPublicService);

        boolean addedMsg = false;
        // gather selected categories
        List<VOCategory> categories = selectedCategoryToVOUnwrapper(this.categorySelection);

        try {
            selectedService = new ServiceDetails(
                    marketplaceBean.publishService(selectedService, categories));
            selectedService.setPublicService(isPublicService);
            setMarketplaceCategories(marketplaceBean.getMarketplaceId());
            updateMarketplacePublishedTo();
        } catch (ObjectNotFoundException e) {
            if (ClassEnum.MARKETPLACE == e.getDomainObjectClassEnum()) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        ERROR_SERVICE_SAVED_MARKETPLACE_DELETED, new String[] {
                                selectedService.getServiceId(),
                                marketplaceBean.getMarketplace().getName(),
                                marketplaceBean.getMarketplace()
                                        .getMarketplaceId() });
                addedMsg = true;
            } else {
                throw e;
            }
        } catch (PublishingToMarketplaceNotPermittedException e) {
            addMessage(
                    null,
                    FacesMessage.SEVERITY_ERROR,
                    ERROR_SERVICE_SAVED_PUBLISHING_NOT_PERMITTED,
                    new String[] { selectedService.getServiceId(),
                            marketplaceBean.getMarketplace().getName(),
                            marketplaceBean.getMarketplace().getMarketplaceId() });
            addedMsg = true;
        }

        if (!addedMsg) {
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_SERVICE_SAVED,
                    selectedService.getServiceId());
        }

        prepareParameters();
        if (saveLocalizedValues) {
            setServiceAttributesToLocalization();
            getProvisioningService().saveServiceLocalization(
                    selectedService.getVoServiceDetails(), localization);
            saveLocalizedValues = false;
        }

        dirty = false;

        if (logger.isDebugLoggingEnabled()) {

        }
        return OUTCOME_SUCCESS;
    }

    public List<Service> getServicesForDeActivation()
            throws OperationNotPermittedException, ObjectNotFoundException {
        if (servicesForDeActivation == null) {
            servicesForDeActivation = new ArrayList<Service>();
            try {
                // Add all services
                List<VOService> services = getServices(PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
                for (VOService service : services) {
                    Service prod = new Service(service);
                    prod.setSelected(service.getStatus() == ServiceStatus.ACTIVE);

                    // for every service we request all catalog entries as well,
                    // to determine the visiblity within the catalog
                    List<VOCatalogEntry> catEntries = getMarketplaceServiceInternal()
                            .getMarketplacesForService(service,
                                    PerformanceHint.ONLY_FIELDS_FOR_LISTINGS);
                    prod.setCatalogEntries(catEntries);

                    servicesForDeActivation.add(prod);
                }

                // Sort service list by MARKETPLACE, TECHNICAL SERVICE ID
                Collections.sort(servicesForDeActivation,
                        new DefaultSortingOfDeActivations());

                // Append customer specific services (at correct position)
                List<VOCustomerService> custServices = getProvisioningService()
                        .getAllCustomerSpecificServices();
                ListIterator<Service> iterList = servicesForDeActivation
                        .listIterator();
                while (iterList.hasNext()) {
                    Service prod = iterList.next();
                    for (VOCustomerService voCustomerService : custServices) {
                        if (!voCustomerService.getServiceId().equals(
                                prod.getServiceId())) {
                            continue;
                        }
                        CustomerService custProd = new CustomerService(
                                voCustomerService, prod);
                        custProd.setSelected(voCustomerService.getStatus() == ServiceStatus.ACTIVE);
                        iterList.add(custProd);
                    }
                }

            } catch (OrganizationAuthoritiesException e) {
                ExceptionHandler.execute(e);
            }

        }
        return servicesForDeActivation;
    }

    /**
     * Tries to activate or deactivate a list of services.
     * 
     * @return the outcome
     */
    public String doDeActivate() throws SaaSApplicationException {

        // Collect all modified services
        List<VOServiceActivation> updateList = new ArrayList<VOServiceActivation>();
        for (Service prod : servicesForDeActivation) {
            VOServiceActivation activation = null;
            VOService service = prod.getVO();
            if (prod.isModified()) {
                // Service has been modified
                activation = new VOServiceActivation();
                activation.setService(service);

                // Get new activation state
                activation.setActive(prod.isSelected());

                // and all catalog entries which define the catalog visibility
                activation.setCatalogEntries(prod.getCatalogEntries());

                // and add it to list
                updateList.add(activation);
            }
        }

        try {
            // invoke update
            List<VOService> result = getProvisioningService()
                    .setActivationStates(updateList);
            boolean operationCompleted = (result.size() == updateList.size());
            // display success
            addInfoOrProgressMessage(operationCompleted,
                    INFO_SERVICE_STATE_SAVED, null);
        } catch (ServiceStateException e) {

            String causedStatus = e.getMessageParams()[1];
            if (causedStatus.equals(ServiceStatus.DELETED.name())) {
                addMessage(null, FacesMessage.SEVERITY_ERROR,
                        SERVICE_NO_LONGER_EXISTS);
                return null;
            } else
                throw e;

        } finally {
            // clear caches (enforce reread next time)
            servicesForDeActivation = null;
            services = null;

        }
        return OUTCOME_SUCCESS;
    }

    public boolean isEditDisabled() {
        return getSelectedService() == null
                || getSelectedService().getVoServiceDetails() == null
                || getSelectedService().getStatus() == ServiceStatus.ACTIVE;
    }

    public boolean isEditDisabledForCreation() {
        getSelectedTechService();
        return serviceForCreation == null;
    }

    /**
     * Remove the parameters which are not configurable and which have no value
     * from the parameter list of the service
     * 
     * @param service
     *            the service to modify
     * @return the service with the modified parameter list
     */
    private static VOServiceDetails cleanupParameter(VOServiceDetails service) {
        if (service != null) {
            for (Iterator<VOParameter> it = service.getParameters().iterator(); it
                    .hasNext();) {
                VOParameter param = it.next();
                if (!param.isConfigurable() && isBlank(param.getValue())) {
                    it.remove();
                }
            }
        }
        return service;
    }

    public List<ParameterRow> getParameterRows() {
        return parameterRows;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public String copy() throws SaaSApplicationException {

        ServiceDetails service = getSelectedService();
        if (service == null || service.getVoServiceDetails() == null) {
            return OUTCOME_ERROR;
        }
        try {
            selectedService = new ServiceDetails(getProvisioningService()
                    .copyService(service.getVoServiceDetails(), newServiceId));
            sessionBean.setSelectedServiceKeyForSupplier(Long
                    .valueOf(selectedService.getKey()));
            updateMarketplacePublishedTo();
            prepareParameters();
            addMessage(null, FacesMessage.SEVERITY_INFO, INFO_SERVICE_CREATED,
                    selectedService.getServiceId());

        } catch (ServiceStateException e) {
            String causedStatus = e.getMessageParams()[1];
            if (causedStatus.equals(ServiceStatus.DELETED.name())) {
                addMessage(null, FacesMessage.SEVERITY_ERROR, SERVICE_DELETED);
                return OUTCOME_ERROR;
            } else
                throw e;
        } finally {
            services = null;
            newServiceId = null;
            // help the navigation to highlight the correct navigation item
            menuBean.setCurrentPageLink(MenuBean.LINK_SERVICE_EDIT);

        }
        return OUTCOME_SUCCESS;
    }

    public String getNewServiceId() {
        ServiceDetails service = getSelectedService();
        if (newServiceId == null && service != null
                && service.getVoServiceDetails() != null) {
            newServiceId = new IdGenerator("Copy of ", service, getServices())
                    .generateNewId();
        }
        return newServiceId;
    }

    public void setNewServiceId(String newServiceId) {
        this.newServiceId = newServiceId;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public ServiceDetails getServiceForCreation() {
        return serviceForCreation;
    }

    public void setMarketplaceBean(MarketplaceBean marketplaceBean) {
        this.marketplaceBean = marketplaceBean;
    }

    public MarketplaceBean getMarketplaceBean() {
        return marketplaceBean;
    }

    /**
     * Sort by marketplace and technical service
     */
    private class DefaultSortingOfDeActivations implements Comparator<Service> {
        Collator collator = Collator.getInstance();

        @Override
        public int compare(Service svc1, Service svc2) {
            int rc = collator.compare(svc1.getMarketplace(),
                    svc2.getMarketplace());
            if (rc == 0)
                rc = collator.compare(svc1.getTechnicalId(),
                        svc2.getTechnicalId());
            return rc;
        }
    }

    public ImageUploader getImageUploader() {
        return imageUploader;
    }

    public boolean isPartOfUpgradePath() {
        if (partOfUpgradePath == null) {
            ServiceDetails s = getSelectedService();
            if (s != null) {
                try {
                    partOfUpgradePath = Boolean
                            .valueOf(getProvisioningService()
                                    .isPartOfUpgradePath(
                                            s.getVoServiceDetails()));
                } catch (SaaSApplicationException e) {
                    ExceptionHandler.execute(e);
                    partOfUpgradePath = Boolean.FALSE;
                }
            } else {
                partOfUpgradePath = Boolean.FALSE;
            }
        }
        return partOfUpgradePath.booleanValue();
    }

    public void marketplaceChanged(ValueChangeEvent event) {
        marketplaceChanged = true;
        String selectedMarketplaceId = (String) event.getNewValue();
        setMarketplaceCategories(selectedMarketplaceId);

        if (ce != null
                && ce.getCategories() != null
                && ce.getMarketplace() != null
                && ce.getMarketplace().getMarketplaceId()
                        .equals(selectedMarketplaceId)) {
            checkCategorySelection(ce.getCategories());
        }
    }

    private void setMarketplaceCategories(String selectedMarketplaceId) {

        List<VOCategory> allMarketplaceCategories = Collections.emptyList();
        if (selectedMarketplaceId != null) {
            allMarketplaceCategories = this.getCategorizationService()
                    .getCategories(selectedMarketplaceId, getUserLanguage());
        }
        categorySelection = getWrappedCategory(allMarketplaceCategories);
    }

    private List<CategoryRow> getWrappedCategory(List<VOCategory> categories) {
        List<CategoryRow> categoriesWrapped = new ArrayList<CategoryRow>();
        for (VOCategory cat : categories) {
            CategoryRow categoryRow = new CategoryRow(cat);
            categoryRow.setSelected(false);
            categoriesWrapped.add(categoryRow);
        }
        return categoriesWrapped;
    }

    public void setCategorySelection(List<CategoryRow> categories) {
        this.categorySelection = categories;
    }

    public List<CategoryRow> getCategorySelection() {
        if (categorySelection == null) {
            categorySelection = Collections.emptyList();
        }
        if (categorySelection.size() > 1) {
            Collections.sort(categorySelection, categoriesComparator);
        }
        return categorySelection;
    }

    /**
     * Compares the display name of two categories.
     */
    private final Comparator<CategoryRow> categoriesComparator = new Comparator<CategoryRow>() {

        @Override
        public int compare(CategoryRow arg0, CategoryRow arg1) {
            return Category.getDisplayName(arg0.getCategory()).compareTo(
                    Category.getDisplayName(arg1.getCategory()));
        }
    };

    private List<VOCategory> selectedCategoryToVOUnwrapper(
            List<CategoryRow> selectedCategories) {
        List<VOCategory> voCategories = new ArrayList<VOCategory>();
        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            for (CategoryRow catRow : selectedCategories) {
                if (catRow.isSelected()) {
                    voCategories.add(catRow.getCategory());
                }
            }
        }
        return voCategories;
    }

    public boolean getMarketplaceChanged() {
        return marketplaceChanged;
    }

    public boolean isOneTimeParameterExist() {
        boolean isOneTimeParameterExist = false;
        if (parameterRows != null) {
            for (ParameterRow row : parameterRows) {
                if (row.isOneTimeParameter()) {
                    isOneTimeParameterExist = true;
                }
            }
        }
        return isOneTimeParameterExist;
    }

    public boolean isShowConfirm() {
        if (selectedService == null) {
            return false;
        }

        List<VOService> compatibleServices = new ArrayList<VOService>();
        if (getPossibleCompatibleServices() != null) {
            for (Service service : getPossibleCompatibleServices()) {
                if (service.isSelected()) {
                    compatibleServices.add(service.getVO());
                }
            }
        }

        for (VOService serviceToUpDowngrade : compatibleServices) {
            VOPriceModel priceModel = selectedService.getPriceModel();
            if (priceModel == null)
                continue;
            PriceModelType servicePMType = priceModel.getType();

            priceModel = serviceToUpDowngrade.getPriceModel();
            if (priceModel == null)
                continue;
            PriceModelType serviceToUpDowngradePMType = priceModel.getType();

            if ((servicePMType.equals(PriceModelType.PER_UNIT) && !serviceToUpDowngradePMType
                    .equals(PriceModelType.FREE_OF_CHARGE))
                    || (serviceToUpDowngradePMType
                            .equals(PriceModelType.PER_UNIT) && !servicePMType
                            .equals(PriceModelType.FREE_OF_CHARGE))) {

                return true;
            }
        }

        return false;
    }

	public boolean isLocalizeVisible() {
		return localizeVisible;
	}

	public void setLocalizeVisible(boolean localizeVisible) {
		this.localizeVisible = localizeVisible;
	}
}
