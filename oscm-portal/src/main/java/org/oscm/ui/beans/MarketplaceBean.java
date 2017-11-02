/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: weiser
 *
 *  Creation Date: 07.03.2011
 *
 *  Completion Time: <date>
 *
 *******************************************************************************/

package org.oscm.ui.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ValueChangeEvent;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.string.Strings;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.model.Marketplace;
import org.oscm.ui.model.Organization;
import org.oscm.ui.model.ServiceDetails;

/**
 * Bean for marketplace related operations.
 *
 * @author weiser
 *
 */
@ViewScoped
@ManagedBean(name = "marketplaceBean")
public class MarketplaceBean extends BaseBean implements Serializable {

    private static final long serialVersionUID = -1194088973179673510L;

    private List<Marketplace> marketplaces = null;

    // Holds the ID of the currently selected marketplace.
    private String marketplaceId;

    // Holds the VO of the currently selected marketplace
    Marketplace marketplace;

    private List<Organization> suppliersForMarketplace;
    private String supplierIdToAdd;

    @ManagedProperty(value = "#{menuBean}")
    private MenuBean menuBean;

    @EJB
    private MarketplaceService marketplaceService;

    public MarketplaceBean() {
        super();
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(final MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    private MarketplaceConfiguration getConfig() {
        return marketplaceService.getCachedMarketplaceConfiguration(BaseBean
                .getMarketplaceIdStatic());
    }

    /**
     * Retrieve the list of marketplaces available for the supplier to publish
     * the services on.
     *
     * @return the list of marketplaces
     */
    public List<Marketplace> getMarketplacesForSupplier() {
        if (marketplaces == null) {
            reloadMarketplacesForSupplier();
        }
        return marketplaces;
    }

    @SuppressWarnings("unused")
    public void reloadMarketplacesListener(ComponentSystemEvent event) {
        reloadMarketplacesForSupplier();
    }

    /**
     * Reload the list of marketplaces available for the supplier
     */
    public void reloadMarketplacesForSupplier() {
        marketplaces = new ArrayList<Marketplace>();
        for (VOMarketplace mp : getMarketplaceService()
                .getMarketplacesForOrganization()) {
            marketplaces.add(new Marketplace(mp));
        }
    }

    /**
     * Publishes the provided service to the selected marketplace. In case no
     * marketplace is selected, the service will be unpublished .
     *
     * @param service
     *            the service to (un)publish
     * @param categories
     *            list of assigned categories
     * @return a value object with the updated service definition
     * @throws SaaSApplicationException
     */
    VOServiceDetails publishService(ServiceDetails service,
            List<VOCategory> categories) throws SaaSApplicationException {
        VOMarketplace voMp = null;
        if (!isBlank(marketplaceId)) {
            voMp = new VOMarketplace();
            voMp.setMarketplaceId(marketplaceId);
        }

        VOCatalogEntry voCE = new VOCatalogEntry();
        voCE.setAnonymousVisible(service.isPublicService());
        voCE.setMarketplace(voMp);
        voCE.setCategories(categories);
        try {
            return getMarketplaceService().publishService(
                    service.getVoServiceDetails(), Arrays.asList(voCE));
        } catch (SaaSApplicationException e) {
            checkMarketplaceDropdownAndMenuVisibility(e);
            throw e;
        }
    }

    /**
     * Sets the selected marketplace id.
     *
     * @param marketplaceId
     *            the marketplace id
     */
    @Override
    public void setMarketplaceId(String marketplaceId) {
        this.marketplaceId = marketplaceId;
    }

    /**
     * Value change listener for marketplace combo
     *
     * @param event
     */
    public void processValueChange(final ValueChangeEvent event) {
        this.setMarketplaceId((String) event.getNewValue());
        marketplaceChanged();
    }

    public String marketplaceChangedForManageSeller() {
        boolean result = true;
        suppliersForMarketplace = null;
        supplierIdToAdd = null;

        if (marketplaceId == null || marketplaceId.equals("0")) {
            // 0 is the item value of the "please select one" item. If it was
            // selected the ID will be reset.
            this.marketplaceId = null;
            this.marketplace = null;
        } else {
            // "marketplaces" is not set and not needed during the
            // "createMarketplace()" operation!

            if ((result = this.validateMarketplaceId(marketplaceId))
                    && this.marketplaces != null) {
                this.marketplace = this.findMarketplace(this.marketplaces,
                        this.marketplaceId);
            }

        }

        return result ? BaseBean.OUTCOME_SUCCESS : BaseBean.OUTCOME_ERROR;
    }

    public String marketplaceChanged() {
        String result = marketplaceChangedForManageSeller();
        if (result.equals(OUTCOME_SUCCESS)) {
            return "";
        }
        return result;
    }

    /**
     * validation of the marketplace if it's available
     */
    private boolean validateMarketplaceId(String marketplaceId) {
        try {
            getMarketplaceService().getMarketplaceById(marketplaceId);
            return true;
        } catch (SaaSApplicationException e) {
            this.resetMarketplaces();
            this.ui.handleException(e);
        }
        return false;
    }

    void resetMarketplaces() {
        this.marketplaces = null;
        this.marketplace = null;
        this.marketplaceId = null;
    }

    /**
     * Returns the selected marketplace id
     *
     * @return the marketplace id
     */
    @Override
    public String getMarketplaceId() {
        return marketplaceId;
    }

    /**
     * Defines whether the marketplace is considered open.
     *
     * @param isOpen
     */
    public void setOpen(boolean isOpen) {
        getMarketplace().setOpen(isOpen);
    }

    /**
     * Signals whether the marketplace is considered open.
     *
     * @return true in case the marketplace is open
     */
    public boolean isOpen() {
        return getMarketplace().isOpen();
    }

    /**
     * Returns the message identifiers for message properties files, depending
     * on the marketplace type.
     *
     * @return the message identifier
     */
    public String getMessageId() {
        String messageId = "marketplace.manageSuppliers";
        if (marketplaceId == null) {
            messageId += ".notselected";
        } else {
            if (isOpen()) {
                messageId += ".open";
            }
        }
        return messageId;
    }

    /**
     * Returns the list of catalog entries to all marketplaces the provided
     * service is published on.
     *
     * @param service
     *            the service to get the marketplaces it is published on for
     * @return the list of marketplaces
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     */
    List<VOCatalogEntry> getMarketplacesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException {
        return getMarketplaceService().getMarketplacesForService(service);
    }

    /**
     * Returns a list of all marketplaces.
     *
     * @return a list of all marketplaces
     */
    public List<Marketplace> getMarketplacesForOperator() {
        if (marketplaces == null) {
            marketplaces = new ArrayList<Marketplace>();
            for (VOMarketplace mp : getMarketplaceService()
                    .getMarketplacesForOperator()) {
                marketplaces.add(new Marketplace(mp));
            }

        }
        return marketplaces;
    }

    /**
     * Returns a list of all marketplaces the logged in user is allowed to
     * manage (i.e. that he owns).
     *
     * @return a list of all marketplaces the user is allowed to manage
     */
    public List<Marketplace> getMarketplacesOwned() {
        if (this.marketplaces == null) {
            this.marketplaces = new ArrayList<>();
            for (final VOMarketplace mp : this.getMarketplaceService()
                    .getMarketplacesOwned()) {
                this.marketplaces.add(new Marketplace(mp));
            }
        }
        return marketplaces;
    }

    /**
     * Get the current VOMarketplace object. If it doesn't exist a default
     * VOMarketplace object will be created.
     *
     * @return the current VOMarketplace object.
     */
    public Marketplace getMarketplace() {
        if (marketplace == null) {
            marketplace = new Marketplace();
            marketplace.setOpen(false);
        }
        return marketplace;
    }

    /**
     * Returns the VO for the currently selected marketplace ID. If there is no
     * ID selected <code>null</code> will be returned.
     * <p>
     * Note: This function expects that the select ID belongs to a VO which was
     * previously acquired by the <code>getGlobalMarketplaces</code> or
     * <code>getMarketplaces</code> function. There will be no additional server
     * call to obtain the VO for the selected ID.
     *
     * @return the VO for the currently selected marketplace ID.
     */
    private Marketplace findMarketplace(List<Marketplace> mplList, String mplId) {
        if (mplList != null && mplId != null) {
            for (Marketplace mpl : mplList) {
                if (mplId.equals(mpl.getMarketplaceId())) {
                    return mpl;
                }
            }
        }
        return null;
    }

    /**
     * Returns whether a marketplace id is selected.
     * <p>
     * This function can be used to control the disable state of UI components.
     *
     * @return <code>true</code> in case a marketplace id is selected, otherwise
     *         <code>false</code>
     */
    public boolean isDisabledForEdit() {
        return (marketplaceId == null || marketplaceId.equals("0"));
    }

    /**
     * Deletes a marketplace.
     */
    public String deleteMarketplace() throws SaaSApplicationException {
        try {
            if (isTokenValid()) {
                getMarketplaceService().deleteMarketplace(
                        marketplace.getMarketplaceId());

                addMessage(null, FacesMessage.SEVERITY_INFO,
                        INFO_MARKETPLACE_DELETED,
                        marketplace.getMarketplaceId());
                resetToken();
            }
        } finally {
            checkMarketplaceDropdownAndMenuVisibility(null);
        }
        return null;
    }

    public List<Organization> getSupplierMarketplaceRelation() {

        try {
            if (suppliersForMarketplace == null) {
                suppliersForMarketplace = new ArrayList<Organization>();
                if (marketplaceId != null) {
                    List<VOOrganization> suppliers = new ArrayList<VOOrganization>();
                    if (isOpen()) {
                        suppliers = getMarketplaceService()
                                .getBannedOrganizationsForMarketplace(
                                        marketplaceId);
                    } else {
                        suppliers = getMarketplaceService()
                                .getOrganizationsForMarketplace(marketplaceId);
                    }
                    for (VOOrganization org : suppliers) {
                        suppliersForMarketplace.add(new Organization(org));
                    }
                }
            }

        } catch (SaaSApplicationException e) {
            checkMarketplaceDropdownAndMenuVisibility(e);
            ExceptionHandler.execute(e);
        }
        return suppliersForMarketplace;
    }

    public String addSupplierMarketplaceRelation()
            throws SaaSApplicationException {

        if (!isTokenValid()) {
            return OUTCOME_SUCCESS;
        }
        if (!Strings.isEmpty(supplierIdToAdd)
                && !Strings.isEmpty(marketplaceId)) {
            List<String> ids = new ArrayList<String>();
            ids.add(supplierIdToAdd);
            try {
                if (isOpen()) {
                    getMarketplaceService().banOrganizationsFromMarketplace(
                            ids, marketplaceId);
                    addMessage(null, FacesMessage.SEVERITY_INFO,
                            INFO_SUPPLIER_BANNED, supplierIdToAdd);
                } else {
                    getMarketplaceService().addOrganizationsToMarketplace(ids,
                            marketplaceId);
                    addMessage(null, FacesMessage.SEVERITY_INFO,
                            INFO_SUPPLIER_ADDED, supplierIdToAdd);
                }
            } catch (SaaSApplicationException e) {
                checkMarketplaceDropdownAndMenuVisibility(e);
                throw e;
            }
            suppliersForMarketplace = null;
            supplierIdToAdd = null;
            resetToken();
        }

        return null;
    }

    public String removeSupplierMarketplaceRelation()
            throws SaaSApplicationException {

        supplierIdToAdd = null;
        if (!isTokenValid()) {
            return OUTCOME_SUCCESS;
        }

        List<String> ids = getSelectedOrganizationIds();
        if (ids.size() > 0) {
            try {
                if (isOpen()) {
                    getMarketplaceService()
                            .liftBanOrganizationsFromMarketplace(ids,
                                    marketplaceId);
                    addMessage(null, FacesMessage.SEVERITY_INFO,
                            INFO_SUPPLIER_BANLIFTED);
                } else {
                    getMarketplaceService().removeOrganizationsFromMarketplace(
                            ids, marketplaceId);
                    addMessage(null, FacesMessage.SEVERITY_INFO,
                            INFO_SUPPLIER_REMOVED);
                }
            } catch (SaaSApplicationException e) {
                checkMarketplaceDropdownAndMenuVisibility(e);
                throw e;
            }
            suppliersForMarketplace = null;
            resetToken();
        }

        return null;
    }

    /**
     * @return true, if there is a marketplace with a list of suppliers and one
     *         or more elements of this list are selected
     */
    public boolean isDeleteButtonDisabled() {
        return (isDisabledForEdit() || (getSelectedOrganizationIds().isEmpty()));
    }

    private List<String> getSelectedOrganizationIds() {
        ArrayList<String> list = new ArrayList<String>();
        if (suppliersForMarketplace == null
                || suppliersForMarketplace.isEmpty()) {
            return list;
        }
        for (Organization org : suppliersForMarketplace) {
            if (org.isSelected()) {
                list.add(org.getOrganizationId());
            }
        }
        return list;
    }

    public String getSupplierIdToAdd() {
        return supplierIdToAdd;
    }

    public void setSupplierIdToAdd(String supplierIdToAdd) {
        this.supplierIdToAdd = supplierIdToAdd;
    }

    /**
     * Resets the cached marketplaces and the menu visibility upon certain
     * server-side exceptions. Should be invoked after e.g. concurrent deletion
     * or reassignment of marketplace. Can be enforced by passing
     * <code>null</code> as argument.
     */
    protected void checkMarketplaceDropdownAndMenuVisibility(
            SaaSApplicationException ex) {
        if (ex == null
                || (ex instanceof ObjectNotFoundException && ((ObjectNotFoundException) ex)
                        .getDomainObjectClassEnum() == ClassEnum.MARKETPLACE)
                || (ex instanceof OperationNotPermittedException)) {
            marketplaceId = null;
            marketplaces = null;
            menuBean.resetMenuVisibility();
        }
    }

    public String getSelectedMarketplaceName() {
        List<Marketplace> list = getMarketplacesForSupplier();
        Marketplace mp = findMarketplace(list, marketplaceId);
        if (mp != null) {
            return mp.getName();
        }
        return JSFUtils.getText("marketplace.name.undefined", null);
    }

    public boolean isUiRenderEnabled() {
        return !this.isDisabledForEdit() && this.marketplace != null
                && this.marketplace.getKey() != 0;
    }

    public boolean isRestricted() {

        return getConfig().isRestricted();
    }

}
