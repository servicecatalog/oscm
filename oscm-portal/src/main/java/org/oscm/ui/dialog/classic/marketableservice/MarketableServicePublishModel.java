/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.marketableservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.string.Strings;
import org.oscm.ui.model.CategoryRow;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.service.POPartner;
import org.oscm.internal.service.POServiceForPublish;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.vo.VOCategory;

@ViewScoped
@ManagedBean(name="marketableServicePublishModel")
public class MarketableServicePublishModel {

    private boolean initialized;
    private long selectedServiceKey;
    private POServiceForPublish service;
    private POPartnerPriceModel servicePartnerPriceModel;
    private POOperatorPriceModel operatorPriceModel;
    private POMarketplacePriceModel marketplacePriceModel;
    private POPartnerPriceModel marketplacePartnerPriceModel;
    private List<CategoryRow> categorySelection;
    private List<POPartner> brokers;
    private List<POPartner> resellers;
    private String initialMarketplaceId;
    private boolean supplier;
    private boolean operatorShareVisible;
    private boolean brokerShareVisible;
    private boolean resellerShareVisible;
    final Map<Long, Boolean> assignedPermissions = new HashMap<Long, Boolean>();
    private List<SelectItem> serviceTemplates = new ArrayList<SelectItem>();
    private List<VOCategory> changedCategories = new ArrayList<VOCategory>();

    public POServiceForPublish getServiceDetails() {
        return service;
    }

    public void setServiceDetails(POServiceForPublish service) {
        this.service = service;
    }

    public long getSelectedServiceKey() {
        return selectedServiceKey;
    }

    public void setSelectedServiceKey(long selectedServiceKey) {
        this.selectedServiceKey = selectedServiceKey;
    }

    public boolean isSaveBtnDisabled() {
        return service == null || service.getService() == null
                || service.getService().getKey() <= 0
                || service.getService().getStatus() == ServiceStatus.DELETED
                || service.getService().getStatus() == ServiceStatus.OBSOLETE
                || service.getService().getStatus() == ServiceStatus.SUSPENDED
                || service.getMarketplaceId() == null;
    }

    public boolean isDisabled() {
        return service == null || service.getService() == null
                || service.getService().getKey() <= 0
                || service.getService().getStatus() == ServiceStatus.DELETED
                || service.getService().getStatus() == ServiceStatus.OBSOLETE
                || service.getService().getStatus() == ServiceStatus.SUSPENDED
                || service.getService().getStatus() == ServiceStatus.ACTIVE;
    }

    public List<CategoryRow> getCategorySelection() {
        return categorySelection;
    }

    public void initializeMarketplaceCategories(
            List<VOCategory> allMarketplaceCategories) {
        final Set<Long> selectedCategories = new HashSet<Long>();
        if (getServiceDetails() != null
                && getServiceDetails().getCatalogEntry() != null
                && getServiceDetails().getCatalogEntry().getCategories() != null) {
            for (VOCategory cat : getServiceDetails().getCatalogEntry()
                    .getCategories()) {
                selectedCategories.add(Long.valueOf(cat.getKey()));
            }
        }
        categorySelection = new ArrayList<CategoryRow>();
        if (allMarketplaceCategories != null) {
            for (VOCategory cat : allMarketplaceCategories) {
                final CategoryRow categoryRow = new CategoryRow(cat);
                categoryRow.setSelected(selectedCategories.contains(Long
                        .valueOf(cat.getKey())));
                categorySelection.add(categoryRow);
            }
        }
    }

    public POOperatorPriceModel getOperatorPriceModel() {
        return operatorPriceModel;
    }

    public void setOperatorPriceModel(POOperatorPriceModel operatorPriceModel) {
        this.operatorPriceModel = operatorPriceModel;
    }

    public POMarketplacePriceModel getMarketplacePriceModel() {
        return marketplacePriceModel;
    }

    public void setMarketplacePriceModel(
            POMarketplacePriceModel marketplacePriceModel) {
        this.marketplacePriceModel = marketplacePriceModel;
    }

    public void setServicePartnerPriceModel(
            POPartnerPriceModel poPartnerPriceModel) {
        this.servicePartnerPriceModel = poPartnerPriceModel;
    }

    public POPartnerPriceModel getServicePartnerPriceModel() {
        return servicePartnerPriceModel;
    }

    public PORevenueShare getServiceSpecificBrokerRevenueShare() {
        if (servicePartnerPriceModel.getRevenueShareBrokerModel() != null) {
            return servicePartnerPriceModel.getRevenueShareBrokerModel();
        } else {
            return marketplacePartnerPriceModel.getRevenueShareBrokerModel();
        }

    }

    public PORevenueShare getServiceSpecificResellerRevenueShare() {
        if (servicePartnerPriceModel.getRevenueShareResellerModel() != null) {
            return servicePartnerPriceModel.getRevenueShareResellerModel();
        } else {
            return marketplacePartnerPriceModel.getRevenueShareResellerModel();
        }
    }

    public boolean isPartOfUpgradePath() {
        POServiceForPublish s = getServiceDetails();
        if (s == null) {
            return false;
        }
        return s.isPartOfUpgradePath();
    }

    public List<POPartner> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<POPartner> brokers) {
        this.brokers = brokers;
    }

    public List<POPartner> getResellers() {
        return resellers;
    }

    public void setResellers(List<POPartner> resellers) {
        this.resellers = resellers;
    }

    public void setInitialMarketplaceId(String initialMarketplaceId) {
        this.initialMarketplaceId = initialMarketplaceId;
    }

    public String getInitialMarketplaceId() {
        return initialMarketplaceId;
    }

    public boolean isSupplier() {
        return supplier;
    }

    public void setSupplier(boolean supplier) {
        this.supplier = supplier;
    }

    public boolean isOperatorShareVisible() {
        return operatorShareVisible;
    }

    public void setOperatorShareVisible(boolean operatorShareVisible) {
        this.operatorShareVisible = operatorShareVisible;
    }

    public boolean isBrokerShareVisible() {
        return brokerShareVisible;
    }

    public void setBrokerShareVisible(boolean brokerShareVisible) {
        this.brokerShareVisible = brokerShareVisible;
    }

    public boolean isResellerShareVisible() {
        return resellerShareVisible;
    }

    public void setResellerShareVisible(boolean resellerShareVisible) {
        this.resellerShareVisible = resellerShareVisible;
    }

    public void setMarketplacePartnerPriceModel(
            POPartnerPriceModel poPartnerPriceModel) {
        marketplacePartnerPriceModel = poPartnerPriceModel;
    }

    public POPartnerPriceModel getMarketplacePartnerPriceModel() {
        return marketplacePartnerPriceModel;
    }

    public List<SelectItem> getServiceTemplates() {
        return serviceTemplates;
    }

    public void setServiceTemplates(List<SelectItem> serviceTemplates) {
        this.serviceTemplates = serviceTemplates;
    }

    public boolean isConfirmationRequired() {
        String initialMpId = getInitialMarketplaceId();
        if (isPartOfUpgradePath() && !Strings.isEmpty(initialMpId)) {
            return !initialMpId.equals(getServiceDetails().getMarketplaceId());
        }
        return false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public List<VOCategory> getChangedCategoriess() {
        return changedCategories;
    }

    public void setChangedCategories(List<VOCategory> changedCategories) {
        this.changedCategories = changedCategories;
    }

}
