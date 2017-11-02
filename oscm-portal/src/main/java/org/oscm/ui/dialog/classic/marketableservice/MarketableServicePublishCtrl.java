/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.marketableservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.security.auth.login.LoginException;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.pricing.POMarketplacePriceModel;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.POOrganization;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.pricing.POServiceForPricing;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.resalepermissions.POServiceDetails;
import org.oscm.internal.resalepermissions.ResaleService;
import org.oscm.internal.service.POPartner;
import org.oscm.internal.service.POServiceForPublish;
import org.oscm.internal.service.PublishService;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.beans.UserBean;
import org.oscm.ui.common.ADMStringUtils;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.SelectItemBuilder;
import org.oscm.ui.model.CategoryRow;
import org.oscm.ui.model.User;

@ManagedBean
@ViewScoped
public class MarketableServicePublishCtrl extends BaseBean
        implements Serializable {

    private static final long serialVersionUID = 725185082688691574L;

    @ManagedProperty(value = "#{marketableServicePublishModel}")
    private MarketableServicePublishModel model;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    @EJB
    PublishService publishService;
    @EJB
    ResaleService resaleService;
    @EJB
    PricingService pricingService;
    @EJB
    CategorizationService categorizationService;
    @EJB
    MarketplaceService mplService;
    @EJB
    ServiceProvisioningService serviceProvisioning;

    /**
     * initializer method called by <adm:initialize />
     * 
     * @return empty string (due to value jsf binding )
     * 
     *         workaround: to be refactored under jsf 2.0
     * 
     */
    public String getInitializePublish() {
        if (!model.isInitialized()) {
            final Long key = sessionBean.getSelectedServiceKeyForSupplier();
            // if save failed, show the error
            // if auto-selection of service failed, do not show the error
            // (#9481)
            initializeModel(key == null ? 0L : key.longValue(), null,
                    ui.hasErrors());
            model.setInitialized(true);
        }
        return "";
    }

    public void setInitializePartnerServiceView() {
    }

    PublishService getPublishService() {
        return publishService;
    }

    ResaleService getResaleService() {
        return resaleService;
    }

    PricingService getPricingService() {
        return pricingService;
    }

    void initializeModel(long serviceKey, String marketplaceId,
            boolean handleExceptions) {
        POServiceForPublish service = null;
        POOperatorPriceModel operatorPriceModel = null;
        POPartnerPriceModel servicePartnerPriceModel = null;
        if (serviceKey > 0) {
            Response response;
            try {
                response = getPublishService().getServiceDetails(serviceKey);
                service = response.getResult(POServiceForPublish.class);
                operatorPriceModel = response
                        .getResult(POOperatorPriceModel.class);
                servicePartnerPriceModel = response
                        .getResult(POPartnerPriceModel.class);
            } catch (OperationNotPermittedException e) {
                // happens for customer specific products
            } catch (SaaSApplicationException e) {
                if (handleExceptions) {
                    ui.handleException(e);
                }
            }
        }
        model.setServiceDetails(
                service == null ? new POServiceForPublish() : service);
        model.setInitialMarketplaceId(getMarketplaceId(service));
        model.setOperatorPriceModel(operatorPriceModel == null
                ? new POOperatorPriceModel() : operatorPriceModel);
        model.setServicePartnerPriceModel(servicePartnerPriceModel == null
                ? new POPartnerPriceModel() : servicePartnerPriceModel);
        model.setSelectedServiceKey(
                model.getServiceDetails().getService().getKey());
        User sessionUser = userBean.getUserFromSession();
        model.setSupplier(sessionUser.isSupplier());
        model.setOperatorShareVisible(
                sessionUser.isSupplier() && !isExternalPMUsed());
        model.setBrokerShareVisible(
                (sessionUser.isBroker() || sessionUser.isSupplier())
                        && !isExternalPMUsed());
        model.setResellerShareVisible(
                (sessionUser.isReseller() || sessionUser.isSupplier())
                        && !isExternalPMUsed());
        if (marketplaceId == null) {
            marketplaceId = model.getServiceDetails().getMarketplaceId();
        }
        initializeCategories(marketplaceId);
        initializeServiceTemplates();
    }

    void initializeServiceTemplates() {
        Response response = getPublishService().getTemplateServices();
        List<POServiceDetails> templates = response
                .getResultList(POServiceDetails.class);
        List<SelectItem> templateItems = new ArrayList<SelectItem>();
        templateItems
                .add(new SelectItemBuilder(ui).pleaseSelect(Long.valueOf(0L)));
        for (POServiceDetails template : templates) {
            String id = model.isSupplier() ? template.getServiceId()
                    : template.getServiceId() + "  ("
                            + template.getOrganizationId() + ")";
            templateItems
                    .add(new SelectItem(Long.valueOf(template.getKey()), id));
        }
        model.setServiceTemplates(templateItems);
    }

    String getMarketplaceId(POServiceForPublish service) {
        if (service == null) {
            return "";
        }
        String marketplaceId = service.getMarketplaceId();
        if (marketplaceId == null) {
            return "";
        }
        return marketplaceId;
    }

    private void initializeCategories(String marketplaceId) {
        List<VOCategory> allMarketplaceCategories = Collections.emptyList();
        POMarketplacePriceModel marketplacePriceModel = null;
        POPartnerPriceModel partnerPriceModel = null;
        model.setMarketplacePriceModel(null);
        if (!ADMStringUtils.isBlank(marketplaceId)) {
            Response response;
            try {
                response = getPublishService().getCategoriesAndRvenueShare(
                        marketplaceId, getUserLanguage());
                allMarketplaceCategories = response
                        .getResultList(VOCategory.class);
                marketplacePriceModel = response
                        .getResult(POMarketplacePriceModel.class);
                partnerPriceModel = response
                        .getResult(POPartnerPriceModel.class);
            } catch (ObjectNotFoundException e) {
                ui.handleException(e);
            }
        }
        model.initializeMarketplaceCategories(allMarketplaceCategories);
        model.setMarketplacePriceModel(marketplacePriceModel == null
                ? new POMarketplacePriceModel() : marketplacePriceModel);
        model.setMarketplacePartnerPriceModel(partnerPriceModel == null
                ? new POPartnerPriceModel() : partnerPriceModel);
        initializeLists(marketplaceId, partnerPriceModel);
    }

    private void initializeLists(String marketplaceId,
            POPartnerPriceModel partnerPriceModel) {
        model.assignedPermissions.clear();
        model.setResellers(null);
        model.setBrokers(null);
        if (!ADMStringUtils.isBlank(marketplaceId) && model.isSupplier()) {
            Response response = getPublishService().getBrokers(
                    model.getServiceDetails().getService().getKey());
            model.setBrokers(response.getResultList(POPartner.class));
            response = getPublishService().getResellers(
                    model.getServiceDetails().getService().getKey());
            model.setResellers(response.getResultList(POPartner.class));
        }
        setDefaultRevenueShares(model.getBrokers(),
                model.getServicePartnerPriceModel()
                        .getRevenueShareBrokerModel(),
                partnerPriceModel == null ? null
                        : partnerPriceModel.getRevenueShareBrokerModel());

        setDefaultRevenueShares(model.getResellers(),
                model.getServicePartnerPriceModel()
                        .getRevenueShareResellerModel(),
                partnerPriceModel == null ? null
                        : partnerPriceModel.getRevenueShareResellerModel());

        if (!model.isSupplier()) {
            if (model.getServicePartnerPriceModel()
                    .getRevenueShareBrokerModel() != null) {
                model.getMarketplacePartnerPriceModel()
                        .setRevenueShareBrokerModel(
                                model.getServicePartnerPriceModel()
                                        .getRevenueShareBrokerModel());
            }
            if (model.getServicePartnerPriceModel()
                    .getRevenueShareResellerModel() != null) {
                model.getMarketplacePartnerPriceModel()
                        .setRevenueShareResellerModel(
                                model.getServicePartnerPriceModel()
                                        .getRevenueShareResellerModel());
            }
        }
    }

    private void setDefaultRevenueShares(List<POPartner> list,
            PORevenueShare rsPartner, PORevenueShare rsMarketplace) {
        if (list != null) {
            final PORevenueShare rsDefault = rsPartner == null
                    || rsPartner.getKey() <= 0 ? rsMarketplace : rsPartner;
            for (POPartner partner : list) {
                if (partner.getRevenueShare() == null) {
                    partner.setRevenueShare(rsDefault);
                }
                model.assignedPermissions.put(Long.valueOf(partner.getKey()),
                        Boolean.valueOf(partner.isSelected()));
            }
        }
    }

    public void marketplaceChanged(ValueChangeEvent event) {
        initializeModel(model.getSelectedServiceKey(),
                event.getNewValue() == null ? "" : (String) event.getNewValue(),
                true);
    }

    public void serviceChanged(ValueChangeEvent event) {
        final long selectedServiceKey = ((Long) event.getNewValue())
                .longValue();
        if (selectedServiceKey != model.getSelectedServiceKey()) {
            initializeModel(selectedServiceKey, null, true);
            sessionBean.setSelectedServiceKeyForSupplier(
                    Long.valueOf(model.getSelectedServiceKey()));
        }
    }

    public void synchronizeUIWithObjects() {
        model.getChangedCategoriess().clear();
        if (model.getServiceDetails() != null
                && model.getCategorySelection() != null) {
            final List<VOCategory> categories = new ArrayList<VOCategory>();
            for (CategoryRow cat : model.getCategorySelection()) {
                if (cat.isSelected()) {
                    categories.add(cat.getCategory());
                }
            }
            List<VOCategory> changedCategories = getChangedAndSelectedCategories(
                    model.getServiceDetails().getCatalogEntry().getCategories(),
                    categories);
            model.setChangedCategories(changedCategories);
            model.getServiceDetails().getCatalogEntry()
                    .setCategories(categories);
        }
    }

    List<VOCategory> getChangedAndSelectedCategories(
            List<VOCategory> originalCategories,
            List<VOCategory> newCategories) {
        List<VOCategory> changedCategories = new ArrayList<VOCategory>();
        for (VOCategory category : originalCategories) {
            if (!isCategoryExistInList(newCategories, category)) {
                changedCategories.add(category);
            }
        }
        changedCategories.addAll(newCategories);
        return changedCategories;
    }

    private boolean isCategoryExistInList(List<VOCategory> categories,
            VOCategory targetCategory) {
        for (VOCategory category : categories) {
            if (category.getCategoryId()
                    .equals(targetCategory.getCategoryId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * action method for save button
     * 
     * @return null: stay on same page
     */
    public String save() {
        synchronizeUIWithObjects();
        final List<POResalePermissionDetails> toGrant = new ArrayList<POResalePermissionDetails>();
        final List<POResalePermissionDetails> toRevoke = new ArrayList<POResalePermissionDetails>();
        if (model.isSupplier()) {
            toGrant.addAll(getPermissionList(model.getBrokers(),
                    OfferingType.BROKER, true));
            toGrant.addAll(getPermissionList(model.getResellers(),
                    OfferingType.RESELLER, true));
            toRevoke.addAll(getPermissionList(model.getBrokers(),
                    OfferingType.BROKER, false));
            toRevoke.addAll(getPermissionList(model.getResellers(),
                    OfferingType.RESELLER, false));
        }
        try {

            String mplId = model.getServiceDetails().getMarketplaceId();
            VOMarketplace marketplace = mplService.getMarketplaceById(mplId);

            if (marketplace.isRestricted()) {
                handleRestrictedMarketplaceAccess(marketplace,
                        model.getServiceDetails().getService());
            }

            getCategorizationService()
                    .verifyCategoriesUpdated(model.getChangedCategoriess());
            Response response = getPublishService().updateAndPublishService(
                    model.getServiceDetails(), toGrant, toRevoke);
            ui.handle(response, "info.service.saved",
                    model.getServiceDetails().getService().getServiceId());

            updateAssignedPermissions(model.getBrokers(), model.getResellers());
            initRevenueShare(model.getSelectedServiceKey(),
                    model.getServiceDetails().getMarketplaceId());
        } catch (SaaSApplicationException e) {

            if (e instanceof ObjectNotFoundException) {
                String[] keys = e.getMessageParams();
                String displayedName = getDisplayedName(keys[0]);
                e.setMessageParams(new String[] { displayedName });
            }

            ui.handleException(e);
            return OUTCOME_ERROR;
        }
        return OUTCOME_SUCCESS;
    }

    private String getDisplayedName(String key) {
        List<VOCategory> changedCategoriess = model.getChangedCategoriess();
        for (VOCategory category : changedCategoriess) {
            if (key.equals(Long.toString(category.getKey()))) {
                return category.getName();
            }
        }
        return null;
    }

    private void initRevenueShare(long serviceKey, String marketPlaceId)
            throws SaaSApplicationException {
        Response response = null;
        POPartnerPriceModel servicePartnerPriceModel = null;
        POPartnerPriceModel marketplacePartnerPriceModel = null;
        POOperatorPriceModel operatorPriceModel = null;
        POMarketplacePriceModel marketplacePriceModel = null;

        response = getPricingService().getPartnerRevenueShareForService(
                new POServiceForPricing(serviceKey, 0));
        servicePartnerPriceModel = response
                .getResult(POPartnerPriceModel.class);
        response = getPricingService().getOperatorRevenueShare(serviceKey);
        operatorPriceModel = response.getResult(POOperatorPriceModel.class);
        response = getPricingService()
                .getMarketplaceRevenueShares(marketPlaceId);
        marketplacePriceModel = response
                .getResult(POMarketplacePriceModel.class);
        response = getPricingService()
                .getPartnerRevenueSharesForMarketplace(marketPlaceId);
        marketplacePartnerPriceModel = response
                .getResult(POPartnerPriceModel.class);

        model.setServicePartnerPriceModel(servicePartnerPriceModel == null
                ? new POPartnerPriceModel() : servicePartnerPriceModel);
        model.setOperatorPriceModel(operatorPriceModel == null
                ? new POOperatorPriceModel() : operatorPriceModel);
        model.setMarketplacePriceModel(marketplacePriceModel == null
                ? new POMarketplacePriceModel() : marketplacePriceModel);
        model.setMarketplacePartnerPriceModel(
                marketplacePartnerPriceModel == null ? new POPartnerPriceModel()
                        : marketplacePartnerPriceModel);
    }

    private void updateAssignedPermissions(List<POPartner> brokers,
            List<POPartner> resellers) {
        List<POPartner> allPartners = new ArrayList<POPartner>();
        if (brokers != null) {
            allPartners.addAll(brokers);
        }
        if (resellers != null) {
            allPartners.addAll(resellers);
        }
        for (POPartner partner : allPartners) {
            if (partner.isSelected() != model.assignedPermissions
                    .get(Long.valueOf(partner.getKey())).booleanValue()) {
                model.assignedPermissions.put(Long.valueOf(partner.getKey()),
                        Boolean.valueOf(partner.isSelected()));
            }
        }
    }

    private List<POResalePermissionDetails> getPermissionList(
            List<POPartner> partners, OfferingType type, boolean grant) {
        List<POResalePermissionDetails> list = new ArrayList<POResalePermissionDetails>();
        if (partners != null) {
            for (POPartner partner : partners) {
                boolean addToList = model.assignedPermissions
                        .get(Long.valueOf(partner.getKey())).booleanValue();
                if (grant) {
                    // grant permission
                    addToList = partner.isSelected() && !addToList;
                } else {
                    // revoke permission
                    addToList = !partner.isSelected() && addToList;
                }

                if (addToList) {
                    POResalePermissionDetails permission = new POResalePermissionDetails();
                    permission.setGrantee(new POOrganization());
                    permission.setGrantor(new POOrganization());
                    permission.setService(new POServiceDetails());
                    permission.getGrantee()
                            .setOrganizationId(partner.getOrganizationId());
                    permission.getGrantor().setOrganizationId(model
                            .getServiceDetails().getService().getSellerId());
                    permission.getService()
                            .setKey(model.getSelectedServiceKey());
                    permission.getService().setServiceId(model
                            .getServiceDetails().getService().getServiceId());
                    permission.setOfferingType(type);
                    list.add(permission);
                }
            }
        }
        return list;
    }

    private boolean isExternalPMUsed() {
        VOPriceModel priceModel = model.getServiceDetails().getService()
                .getPriceModel();

        if (priceModel != null) {
            return priceModel.isExternal();
        }
        return false;
    }

    private void handleRestrictedMarketplaceAccess(VOMarketplace mpl,
            VOService service) throws ObjectNotFoundException,
                    OperationNotPermittedException {

        boolean displayWarnMsg = false;
        String warnedOrgs = "";

        List<VOCustomerService> customerServices = serviceProvisioning
                .getServiceCustomerTemplates(
                        model.getServiceDetails().getService());

        int i = 0;
        for (VOCustomerService customerService : customerServices) {

            String orgName = customerService.getOrganizationName();
            String orgId = customerService.getOrganizationId();

            boolean isAccessible = false;
            try {
                isAccessible = mplService
                        .doesOrganizationHaveAccessMarketplace(
                                mpl.getMarketplaceId(), orgId);
            } catch (LoginException e) {
                e.printStackTrace();
            }

            if (!isAccessible) {
                displayWarnMsg = true;
                if (i != 0) {
                    warnedOrgs += ", ";
                }
                warnedOrgs += orgName + " (" + orgId + ")";
            }
            i++;
        }

        if (displayWarnMsg) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_WARN,
                    BaseBean.WARNING_NO_CUSTOMER_ACCESS_TO_RESTRICTED_MPL,
                    new Object[] { warnedOrgs, mpl.getName() });
        }
    }

    public void setModel(MarketableServicePublishModel model) {
        this.model = model;
    }

    public MarketableServicePublishModel getModel() {
        return model;
    }

    /**
     * @return the userBean
     */
    public UserBean getUserBean() {
        return userBean;
    }

    /**
     * @param userBean
     *            the userBean to set
     */
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    /**
     * @return the sessionBean
     */
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * @param sessionBean
     *            the sessionBean to set
     */
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    @Override
    protected CategorizationService getCategorizationService() {
        if (categorizationService == null) {
            categorizationService = super.getCategorizationService();
        }
        return categorizationService;
    }

}
