/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.partnerservice;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.PriceModelBean;
import org.oscm.ui.beans.ServiceBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.RolePriceHandler;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.dialog.classic.pricemodel.external.ExternalPriceModelDisplayHandler;
import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.partnerservice.POPartnerServiceDetails;
import org.oscm.internal.partnerservice.PartnerService;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOPriceModel;

@ViewScoped
@ManagedBean(name = "partnerServiceViewCtrl")
public class PartnerServiceViewCtrl implements Serializable {

    private static final long serialVersionUID = 725185082688691574L;

    @ManagedProperty(value = "#{partnerServiceViewModel}")
    PartnerServiceViewModel model;

    PartnerService partnerServices;
    UiDelegate ui = new UiDelegate();

    @PostConstruct
    public void initializePartnerServiceView() {
        initializeModel();
        initializePriceModel();
    }

    private PartnerService getPartnerService() {
        if (partnerServices == null) {
            partnerServices = new ServiceLocator()
                    .findService(PartnerService.class);
        }
        return partnerServices;
    }

    private void initializePriceModel() {
        final PriceModelBean pm = (PriceModelBean) ui
                .findBean("priceModelBean");
        pm.setSelectedServiceKey(Long.valueOf(model.getSelectedServiceKey()));
        if (!ui.hasErrors()) {
            pm.updatePriceModel();
            VOPriceModel priceModel = pm.getPriceModel();
            List<RoleSpecificPrice> rolePrices = RolePriceHandler
                    .determineRolePricesForPriceModel(priceModel);
            model.setRolePrices(rolePrices);
        }
        pm.setEditDisabled(true);
    }

    private void initializeModel() {
        if (model.getPartnerServiceDetails() == null) {
            model.setPartnerServiceDetails(new POPartnerServiceDetails());
            model.setDisabled(true);
            model.setRolePrices(new ArrayList<RoleSpecificPrice>());
        }
    }

    private void initializeServices() {
        final ServiceBean serviceBean = (ServiceBean) ui
                .findBean("serviceBean");
        serviceBean.setSelectedServiceKey(0L);
    }

    public void serviceChanged(ValueChangeEvent event) {
        long selectedServiceKey = ((Long) event.getNewValue()).longValue();
        if (selectedServiceKey != model.getSelectedServiceKey()) {
            POPartnerServiceDetails service = new POPartnerServiceDetails();
            boolean isError = false;
            if (selectedServiceKey > 0) {
                try {
                    Response response = getPartnerService()
                            .getServiceDetails(selectedServiceKey);
                    service = response.getResult(POPartnerServiceDetails.class);
                } catch (SaaSApplicationException e) {
                    ui.handleException(e);
                    isError = true;
                    selectedServiceKey = 0L;
                }
            }
            model.setSelectedServiceKey(selectedServiceKey);
            model.setPartnerServiceDetails(service);
            model.setDisabled(isDisabled());
            model.setRolePrices(new ArrayList<RoleSpecificPrice>());
            initializePriceModel();
            if (isError) {
                initializeServices();
            }
        }
    }

    public String display() throws IOException {

        VOPriceModel priceModel = model.getPartnerServiceDetails()
                .getPriceModel();

        if (priceModel.getPresentation() == null) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    BaseBean.ERROR_EXTERNAL_PRICEMODEL_NOT_AVAILABLE, null);
        }
        
        ExternalPriceModelDisplayHandler displayHandler = new ExternalPriceModelDisplayHandler();
        displayHandler.setContent(priceModel.getPresentation());
        displayHandler.setContentType(priceModel.getPresentationDataType());
        //TODO: filename generation
        displayHandler.setFilename("ext_price_model.pdf");

        displayHandler.display();

        return null;
    }

    /**
     * action method for save button
     * 
     * @return null: stay on same page
     */
    public String save() {
        try {
            Response response = getPartnerService().updatePartnerServiceDetails(
                    model.getPartnerServiceDetails());
            ui.handle(response, "info.service.saved",
                    model.getPartnerServiceDetails().getServiceName());
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return null;
    }

    public void setModel(PartnerServiceViewModel model) {
        this.model = model;
    }

    public PartnerServiceViewModel getModel() {
        return model;
    }

    boolean isDisabled() {
        return model.getSelectedServiceKey() <= 0
                || model.getPartnerServiceDetails() != null
                        && ServiceStatus.ACTIVE.equals(
                                model.getPartnerServiceDetails().getStatus())
                || !ui.findUserBean().getUserFromSessionWithoutException()
                        .getUserRoles().contains(UserRoleType.RESELLER_MANAGER);
    }

    public boolean isRolesRendered() {
        List<RoleSpecificPrice> rp = model.getRolePrices();
        return rp != null && !rp.isEmpty();
    }
}
