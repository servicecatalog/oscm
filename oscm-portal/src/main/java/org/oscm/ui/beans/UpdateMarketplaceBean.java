/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: Mar 14, 2012
 *
 *******************************************************************************/

package org.oscm.ui.beans;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.marketplace.MarketplaceServiceManagePartner;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POMarketplacePricing;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.string.Strings;
import org.oscm.ui.model.Marketplace;
import org.oscm.ui.model.User;

/**
 * @author tang
 *
 */
@ViewScoped
@ManagedBean(name = "updateMarketplaceBean")
public class UpdateMarketplaceBean extends BaseBean {

    List<SelectItem> selectableMarketplaces;
    Marketplace model;

    @ManagedProperty(value = "#{menuBean}")
    private MenuBean menuBean;

    // TODO: think more about that. It breaks architecture in view layer. Maybe
    // model should be a bean and we should not get access to model through this
    // bean but directly.
    @ManagedProperty(value = "#{selectOrganizationIncludeBean}")
    private SelectOrganizationIncludeBean selectOrganizationIncludeBean;

    PricingService pricingService;
    MarketplaceServiceManagePartner marketplaceManagePartnerService;

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(final MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public Marketplace getModel() {
        if (model == null) {
            // use disabled default if nothing is selected
            model = new Marketplace();
            model.setRevenueSharesReadOnly(!isLoggedInAndPlatformOperator());
        }
        return model;
    }

    VOMarketplace convertToValueObject(Marketplace mp) {
        if (mp == null) {
            return null;
        }
        VOMarketplace vmp = new VOMarketplace();
        vmp.setName(mp.getName());
        vmp.setKey(mp.getKey());
        vmp.setVersion(mp.getVersion());
        vmp.setOpen(!mp.isClosed());
        vmp.setMarketplaceId(mp.getMarketplaceId());
        vmp.setOwningOrganizationId(mp.getOwningOrganizationId());
        vmp.setTaggingEnabled(mp.isTaggingEnabled());
        vmp.setReviewEnabled(mp.isReviewEnabled());
        vmp.setSocialBookmarkEnabled(mp.isSocialBookmarkEnabled());
        vmp.setCategoriesEnabled(mp.isCategoriesEnabled());
        vmp.setTenantId(mp.getTenantId());
        return vmp;
    }

    POMarketplacePriceModel convertToMarketplacePriceModel(Marketplace model) {
        if (model == null) {
            return null;
        }
        POMarketplacePriceModel po = new POMarketplacePriceModel();
        po.setRevenueShare(model.getMarketplaceRevenueShareObject());
        return po;
    }

    POPartnerPriceModel convertToPartnerPriceModel(Marketplace model) {
        if (model == null) {
            return null;
        }
        POPartnerPriceModel po = new POPartnerPriceModel();
        po.setRevenueShareResellerModel(model.getResellerRevenueShareObject());
        po.setRevenueShareBrokerModel(model.getBrokerRevenueShareObject());
        return po;
    }

    /**
     * Calls the marketplace service and persists the changes to the
     * marketplace.
     *
     * @return <code>OUTCOME_SUCCESS</code> if the operation was executed
     *         successfully
     */
    public String updateMarketplace() {
        final boolean assignedOrgChanged = model.assignedOrgChanged();
        final String mId = model.getMarketplaceId();
        // see todo above
        model.setOwningOrganizationId(getSelectOrganizationIncludeBean()
                .getOrganizationId());
        try {
            Response response = getMarketplaceManagePartnerService()
                    .updateMarketplace(convertToValueObject(model),
                            convertToMarketplacePriceModel(model),
                            convertToPartnerPriceModel(model));

            VOMarketplace vmp = response.getResult(VOMarketplace.class);
            model = convertToModel(vmp);
            addToModel(response.getResult(POMarketplacePriceModel.class));
            addToModel(response.getResult(POPartnerPriceModel.class));
            updateSelectionList(vmp, selectableMarketplaces);
        } catch (ObjectNotFoundException e) {
            ui.handleException(e);
            if (e.getDomainObjectClassEnum() == ClassEnum.MARKETPLACE) {
                ui.resetDirty();
                reset();
            }
            return OUTCOME_ERROR;
        } catch (OperationNotPermittedException e) {
            ui.handleException(e, true);
            reset();
            return OUTCOME_ERROR;
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            return OUTCOME_ERROR;
        }
        if (assignedOrgChanged) {
            applyOrgChange(mId);
        }
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_MARKETPLACE_SAVED,
                mId);
        return OUTCOME_SUCCESS;
    }

    /**
     * Updates the label of the {@link SelectItem} matching the passed
     * {@link VOMarketplace}.
     *
     * @param mp
     *            the {@link VOMarketplace} to get the new label from
     * @param list
     *            the list of {@link SelectItem} to update
     */
    void updateSelectionList(VOMarketplace mp, List<SelectItem> list) {
        if (list == null) {
            return;
        }
        for (SelectItem si : list) {
            if (mp.getMarketplaceId().equals(si.getValue())) {
                si.setLabel(getLabel(mp));
                break;
            }
        }
    }

    private void reset() {
        getMenuBean().resetMenuVisibility();
        model = null;
        selectableMarketplaces = null;
    }

    String applyOrgChange(String mId) {
        VOMarketplace mp = null;
        POMarketplacePricing pricing = null;
        try {
            mp = getMarketplaceService().getMarketplaceById(mId);
            Response response = getPricingService().getPricingForMarketplace(
                    mId);
            pricing = response.getResult(POMarketplacePricing.class);
        } catch (ObjectNotFoundException e) {
            ui.handleException(e);
            model = null;
            selectableMarketplaces = null;
            return OUTCOME_ERROR;
        }
        model = convertToModel(mp);

        addToModel(pricing.getMarketplacePriceModel());
        addToModel(pricing.getPartnerPriceModel());

        if (null != mp) {
            this.selectOrganizationIncludeBean.setOrganizationId(mp
                    .getOwningOrganizationId());
        }

        return OUTCOME_SUCCESS;
    }

    public List<SelectItem> getSelectableMarketplaces() {
        if (selectableMarketplaces == null) {
            List<VOMarketplace> marketplaces;

            if (isLoggedInAndPlatformOperator()) {
                marketplaces = getMarketplaceService()
                        .getMarketplacesForOperator();
            } else {
                marketplaces = getMarketplaceService().getMarketplacesOwned();
            }
            List<SelectItem> result = new ArrayList<SelectItem>();
            // create the selection model based on the read data
            for (VOMarketplace vMp : marketplaces) {
                result.add(new SelectItem(vMp.getMarketplaceId(), getLabel(vMp)));
            }
            selectableMarketplaces = result;
        }
        return selectableMarketplaces;
    }

    String getLabel(VOMarketplace vMp) {
        if (vMp == null) {
            return "";
        }
        if (vMp.getName() == null || Strings.isEmpty(vMp.getName())) {
            return vMp.getMarketplaceId();
        }
        return String.format("%s (%s)", vMp.getName(), vMp.getMarketplaceId());
    }

    public void marketplaceChanged() {
        String marketplaceId = model.getMarketplaceId();
        if (marketplaceId == null || marketplaceId.equals("0")) {
            model = null;
        } else {
            applyOrgChange(marketplaceId);
        }
    }

    Marketplace convertToModel(VOMarketplace vmp) {
        if (vmp == null) {
            return null;
        }
        Marketplace mp = new Marketplace();
        mp.setClosed(!vmp.isOpen());
        mp.setKey(vmp.getKey());
        mp.setMarketplaceId(vmp.getMarketplaceId());
        mp.setName(vmp.getName());
        mp.setOwningOrganizationId(vmp.getOwningOrganizationId());
        mp.setOriginalOrgId(vmp.getOwningOrganizationId());
        mp.setReviewEnabled(vmp.isReviewEnabled());
        mp.setSocialBookmarkEnabled(vmp.isSocialBookmarkEnabled());
        mp.setCategoriesEnabled(vmp.isCategoriesEnabled());
        mp.setTaggingEnabled(vmp.isTaggingEnabled());
        mp.setVersion(vmp.getVersion());
        mp.setEditDisabled(false);
        mp.setOrganizationSelectVisible(isLoggedInAndPlatformOperator());
        mp.setTenantSelectVisible(isLoggedInAndPlatformOperator()
                && !menuBean.getApplicationBean().isInternalAuthMode());
        mp.setPropertiesDisabled(!isMpOwner(vmp));
        mp.setRevenueSharesReadOnly(!isLoggedInAndPlatformOperator());
        mp.setTenantId(vmp.getTenantId());
        return mp;
    }

    void addToModel(POMarketplacePriceModel priceModel) {
        if (model != null) {
            model.setMarketplaceRevenueShareObject(priceModel.getRevenueShare());
        }
    }

    void addToModel(POPartnerPriceModel priceModel) {
        if (model != null) {
            model.setResellerRevenueShareObject(priceModel
                    .getRevenueShareResellerModel());
            model.setBrokerRevenueShareObject(priceModel
                    .getRevenueShareBrokerModel());
        }
    }

    boolean isMpOwner(VOMarketplace vmp) {
        User user = getUserFromSession();
        return user.isMarketplaceOwner()
                && user.getOrganizationId().equals(
                        vmp.getOwningOrganizationId());
    }

    PricingService getPricingService() {
        if (pricingService == null) {
            pricingService = getService(PricingService.class, null);
        }
        return pricingService;
    }

    MarketplaceServiceManagePartner getMarketplaceManagePartnerService() {
        if (marketplaceManagePartnerService == null) {
            marketplaceManagePartnerService = getService(
                    MarketplaceServiceManagePartner.class, null);
        }
        return marketplaceManagePartnerService;
    }

    public void setSelectOrganizationIncludeBean(
            SelectOrganizationIncludeBean selectOrganizationIncludeBean) {
        this.selectOrganizationIncludeBean = selectOrganizationIncludeBean;
    }

    public SelectOrganizationIncludeBean getSelectOrganizationIncludeBean() {
        return selectOrganizationIncludeBean;
    }
}
