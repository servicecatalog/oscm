/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.customizelandingpage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.oscm.string.Strings;
import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.ui.common.SelectItemBuilder;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.components.POMarketplace;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.landingpageconfiguration.LandingpageConfigurationService;
import org.oscm.internal.landingpageconfiguration.POPublicLandingpageConfig;
import org.oscm.internal.landingpageconfiguration.POService;
import org.oscm.internal.types.enumtypes.LandingpageType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exceptions.FillinOptionNotSupportedException;

public class CustomizeLandingpageCtrl implements Serializable {

    private static final long serialVersionUID = 7251850826858691574L;

    private static final String INFO_SAVED = "info.landingpage.saved";
    private static final String CONCURRENT_MODIFICATION_ERROR = "concurrentModification";

    private static final String NO_MARKETPLACE_SELECTED = null;
    private static final String NO_LANDINGPAGE_SELECTED = "";

    CustomizeLandingpageModel model;

    UiDelegate ui = new UiDelegate();

    /**
     * initializer method called by <adm:initialize />
     * 
     * @return empty string (due to value jsf binding )
     * 
     *         workaround: to be refactored under jsf 2.0
     * 
     */
    public String getInitializeCustomizeLandingpage() {
        initializeModel();
        return "";
    }

    LandingpageConfigurationService configureLandingpage;

    LandingpageConfigurationService getLandingpageService() {
        if (configureLandingpage == null) {
            configureLandingpage = new ServiceLocator()
                    .findService(LandingpageConfigurationService.class);
        }
        return configureLandingpage;
    }

    void initializeModel() {
        if (model.isInitialized() == false) {
            resetModel();
        }
    }

    private void initLandingpageType() {
        model.setLandingpageType(NO_LANDINGPAGE_SELECTED);
    }

    void initializeLandingpageConfig() {
        model.setLandingpageConfig(new POPublicLandingpageConfig());
    }

    void initializeFillinOptions() {
        loadFillinOptions(NO_MARKETPLACE_SELECTED);
    }

    void initSelectableMarketplaces() {
        List<POMarketplace> marketplaces = getLandingpageService()
                .getMarketplaceSelections();
        List<SelectItem> uiMarketplaces = initMarketplaceSelector(marketplaces);
        model.setMarketplaces(uiMarketplaces);
    }

    void initNumOfServicesRange() {
        List<Integer> range = getLandingpageService().getNumOfServicesRange();
        List<SelectItem> numOfServicesRange = SelectItemBuilder
                .buildSelectItems(range);
        model.setNumOfServicesRange(numOfServicesRange);
    }

    private List<SelectItem> initMarketplaceSelector(List<POMarketplace> mpls) {
        List<SelectItem> uiMarketplaces = new ArrayList<SelectItem>();
        for (POMarketplace mp : mpls) {
            uiMarketplaces.add(new SelectItem(mp.getMarketplaceId(), mp
                    .getDisplayName()));
        }
        return uiMarketplaces;
    }

    /**
     * value change listener for marketplace chooser
     */
    public String marketplaceChanged() {
        String result = "";
        String selectedMarketplaceId = model.getSelectedMarketplace();
        try {
            if (selectedMarketplaceId != null) {
                boolean isOK = false;
                model.setSelectedMarketplace(selectedMarketplaceId);
                if (isPublicLandingpageActivated(selectedMarketplaceId)) {
                    isOK = loadPublicLandingpage(selectedMarketplaceId);
                } else {
                    isOK = loadEnterpriseLandingpage();
                }
                if (!isOK) {
                    concurrentModification();
                }
            } else {
                resetModel();
            }
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            resetModel();
            result = CONCURRENT_MODIFICATION_ERROR;
        }
        return result;
    }

    void resetModel() {
        initSelectableMarketplaces();
        initLandingpageType();
        initNumOfServicesRange();
        initializeFillinOptions();
        model.setAvailableServices(new ArrayList<POService>());
        initializeLandingpageConfig();
        model.setInitialized(true);
        model.setSelectedMarketplace(null);
    }

    boolean loadEnterpriseLandingpage() {
        model.setLandingpageType(LandingpageType.ENTERPRISE.name());
        model.setLandingpageConfig(null);
        model.setAvailableServices(null);
        model.setFillinItems(null);
        return true;
    }

    /**
     * execute navigation rule: go to destination specified for concurrent
     * modification situation
     */
    private void concurrentModification() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getApplication().getNavigationHandler()
                .handleNavigation(ctx, "", CONCURRENT_MODIFICATION_ERROR);
        ctx.responseComplete();
    }

    void loadFillinOptions(String selectedMarketplaceId) {
        List<FillinCriterion> fillinOptions = getLandingpageService()
                .getFillinOptions(selectedMarketplaceId);
        model.setFillinItems(buildFillinItems(fillinOptions));
    }

    List<SelectItem> buildFillinItems(List<FillinCriterion> fillinOptions) {
        return new SelectItemBuilder(ui).buildSelectItems(fillinOptions,
                "FillinCriterion");
    }

    boolean loadPublicLandingpage(String marketplaceId) {
        try {
            Response r = getLandingpageService().loadPublicLandingpageConfig(
                    marketplaceId);
            setPublicLandingpageModel(marketplaceId, r);
            return true;
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            return false;
        }
    }

    /**
     * @param marketplaceId
     * @param r
     * @throws ObjectNotFoundException
     */
    void setPublicLandingpageModel(String marketplaceId, Response r)
            throws ObjectNotFoundException {
        POPublicLandingpageConfig landingpageConfig = r
                .getResult(POPublicLandingpageConfig.class);
        landingpageConfig
                .setFeaturedServices(adaptServiceNames(landingpageConfig
                        .getFeaturedServices()));
        initNumOfServicesRange();
        model.setLandingpageType(getPublicLandingpageType());
        model.setLandingpageConfig(landingpageConfig);
        
        List<POService> availableServices = new ArrayList<POService>(landingpageConfig.getFeaturedServices());
        availableServices.addAll(adaptServiceNames(r
                .getResultList(POService.class)));		

        model.setAvailableServices(availableServices);
        model.setFillinItems(buildFillinItems(r
                .getResultList(FillinCriterion.class)));
    }

    private String getPublicLandingpageType() {
        return "PUBLIC";
    }

    List<POService> adaptServiceNames(List<POService> services) {
        String text = ui.getText("service.name.undefined");
        for (POService s : services) {
            if (Strings.isEmpty(s.getServiceName())
                    || checkServiceName(s.getServiceName())) {
                s.setServiceName(text);
            }
        }
        return services;
    }

    /**
     * Check if the service name contains a control character.(except for blank
     * and tab)
     * 
     * @param serviceName
     * @return true: the service name contains a control character.
     */
    boolean checkServiceName(String serviceName) {
        char[] chars = serviceName.toCharArray();
        for (char c : chars) {
            if (Character.isISOControl(c) && c != '\t' && c != ' ') {
                return true;
            }
        }
        return false;
    }

    /**
     * if no marketplace is selected disable all other input fields and buttons
     */
    public boolean isFieldsDisabled() {
        if (model == null || model.getSelectedMarketplace() == null
                || model.getSelectedMarketplace().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * action method for save button
     * 
     * @return null: stay on same page
     */
    public String save() {
        String outcome = null;
        try {
            if (isPublicLandingpageSelected()) {
                saveDefaultLandingpage();
            } else {
                saveEnterpriseLandingpage();
            }
        } catch (ObjectNotFoundException e) {
            ui.handleException(e);
            outcome = CONCURRENT_MODIFICATION_ERROR;
        } catch (ConcurrentModificationException e) {
            ui.handleException(e);
            outcome = CONCURRENT_MODIFICATION_ERROR;
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return outcome;
    }

    private boolean isPublicLandingpageSelected() {
        return LandingpageType.isDefault(model.getLandingpageType());
    }

    private void saveEnterpriseLandingpage() throws ObjectNotFoundException,
            OperationNotPermittedException, NonUniqueBusinessKeyException,
            ConcurrentModificationException {
        getLandingpageService().saveEnterpriseLandingpageConfig(
                model.getSelectedMarketplace());
        ui.handle(INFO_SAVED);
    }

    private void saveDefaultLandingpage() throws ObjectNotFoundException,
            NonUniqueBusinessKeyException, ValidationException,
            ConcurrentModificationException, OperationNotPermittedException,
            FillinOptionNotSupportedException {
        Response response = getLandingpageService()
                .savePublicLandingpageConfig(model.getLandingpageConfig());
        ui.handle(INFO_SAVED);
        updateModelLandingpageConfig(response
                .getResult(POPublicLandingpageConfig.class));
    }

    boolean isPublicLandingpageActivated(String marketplaceId)
            throws ObjectNotFoundException {
        return getLandingpageService().loadLandingpageType(marketplaceId) == defaultLandingpageType();
    }

    void updateModelAvailableServices(List<POService> result) {
        model.setAvailableServices(adaptServiceNames(result));
    }

    void updateModelLandingpageConfig(POPublicLandingpageConfig result) {
        result.setFeaturedServices(adaptServiceNames(result
                .getFeaturedServices()));
        model.setLandingpageConfig(result);
    }

    /**
     * action method for reset button
     * 
     * @return
     */
    public String resetToDefault() {
        String outcome = null;
        try {
            String marketplaceId = model.getSelectedMarketplace();
            Response response = getLandingpageService().resetLandingPage(
                    marketplaceId);
            ui.handle(INFO_SAVED);
            model.setLandingpageType(defaultLandingpageType().name());
            updateModelLandingpageConfig(response
                    .getResult(POPublicLandingpageConfig.class));
            updateModelAvailableServices(response
                    .getResultList(POService.class));
            loadFillinOptions(marketplaceId);
        } catch (ObjectNotFoundException e) {
            ui.handleException(e);
            outcome = CONCURRENT_MODIFICATION_ERROR;
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return outcome;
    }

    /**
     * @return
     */
    LandingpageType defaultLandingpageType() {
        return LandingpageType.PUBLIC;
    }

    public void setModel(CustomizeLandingpageModel model) {
        this.model = model;
    }

    public CustomizeLandingpageModel getModel() {
        return model;
    }

    public boolean isHideManualChoicePanels() {
        return landingpageTypeNotSelected()
                && !LandingpageType.isDefault(model.getLandingpageType());
    }

    boolean landingpageTypeNotSelected() {
        return !model.getLandingpageType().equals("");
    }

    /**
     * value change listener for landingpage type
     * 
     * @param event
     */
    public void landingpageTypeChanged(ValueChangeEvent event) {
        String marketplaceId = model.getSelectedMarketplace();
        String selectedLandingpageType = (String) event.getNewValue();
        try {
            if (!LandingpageType.contains(selectedLandingpageType)) {
                model.setLandingpageType(""); // nothing selected
            } else if (LandingpageType.isDefault(selectedLandingpageType)) {
                Response r = getLandingpageService()
                        .loadPublicLandingpageConfig(marketplaceId);
                switchtToPublicLandingpage(r);
            } else {
                switchToEnterpriseLandingpage();
            }
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
    }

    void switchToEnterpriseLandingpage() {
        model.setLandingpageConfig(null);
        model.setAvailableServices(null);
        model.setFillinItems(null);
        model.setLandingpageType(LandingpageType.ENTERPRISE.name());
    }

    void switchtToPublicLandingpage(Response r) {
        POPublicLandingpageConfig landingpageConfig = r
                .getResult(POPublicLandingpageConfig.class);
        landingpageConfig
                .setFeaturedServices(adaptServiceNames(landingpageConfig
                        .getFeaturedServices()));
        initNumOfServicesRange();
        model.setLandingpageType(LandingpageType.PUBLIC.name());
        model.setLandingpageConfig(landingpageConfig);
        List<POService> availableServices = adaptServiceNames(r
                .getResultList(POService.class));
        model.setAvailableServices(availableServices);
        model.setFillinItems(buildFillinItems(r
                .getResultList(FillinCriterion.class)));
    }
}
