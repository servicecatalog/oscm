/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.customizelandingpage;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.types.enumtypes.FillinCriterion;
import org.oscm.internal.landingpageconfiguration.POPublicLandingpageConfig;
import org.oscm.internal.landingpageconfiguration.POService;

@ViewScoped
@ManagedBean(name="customizeLandingpageModel")
public class CustomizeLandingpageModel {

    boolean initialized;

    // Select options
    private List<SelectItem> marketplaces = new ArrayList<SelectItem>();
    private List<SelectItem> numOfServicesRange = new ArrayList<SelectItem>();
    private List<POService> availableServices = new ArrayList<POService>();
    private List<SelectItem> fillinItems;

    // chosen options
    private String selectedMarketplace;
    private String landingpageType;

    // model
    POPublicLandingpageConfig landingpageConfig = new POPublicLandingpageConfig();

    // getter and setter
    public List<SelectItem> getMarketplaces() {
        return marketplaces;
    }

    public void setMarketplaces(List<SelectItem> marketplaces) {
        this.marketplaces = marketplaces;
    }

    public void setNumOfServicesRange(List<SelectItem> numOfServicesRange) {
        this.numOfServicesRange = numOfServicesRange;
    }

    public List<SelectItem> getNumOfServicesRange() {
        return numOfServicesRange;
    }

    public List<POService> getAvailableServices() {
        return availableServices;
    }

    public void setAvailableServices(List<POService> availableServices2) {
        this.availableServices = availableServices2;
    }

    public String getSelectedMarketplace() {
        return selectedMarketplace;
    }

    public void setSelectedMarketplace(String selectedMarketplace) {
        this.selectedMarketplace = selectedMarketplace;
    }

    public int getNumberOfServicesOnLp() {
        return landingpageConfig.getNumberOfServicesOnLp();
    }

    public void setNumberOfServicesOnLp(int numberOfServicesOnLp) {
        landingpageConfig.setNumberOfServicesOnLp(numberOfServicesOnLp);
    }

    public String getFillin() {
        return landingpageConfig.getFillinCriterion().name();
    }

    public void setFillin(String fillinCriterion) {
        landingpageConfig.setFillinCriterion(FillinCriterion
                .valueOf(fillinCriterion));
    }

    public List<POService> getFeaturedServices() {
        return landingpageConfig.getFeaturedServices();
    }

    public void setFeaturedServices(List<POService> featuredServices) {
        landingpageConfig.setFeaturedServices(featuredServices);
    }

    public POPublicLandingpageConfig getLandingpageConfig() {
        return landingpageConfig;
    }

    public void setLandingpageConfig(POPublicLandingpageConfig landingpageConfig) {
        this.landingpageConfig = landingpageConfig;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public List<SelectItem> getFillinItems() {
        return fillinItems;
    }

    public void setFillinItems(List<SelectItem> fillinItems) {
        this.fillinItems = fillinItems;
    }

    public String getLandingpageType() {
        return landingpageType;
    }

    public void setLandingpageType(String landingpageType) {
        this.landingpageType = landingpageType;
    }

}
