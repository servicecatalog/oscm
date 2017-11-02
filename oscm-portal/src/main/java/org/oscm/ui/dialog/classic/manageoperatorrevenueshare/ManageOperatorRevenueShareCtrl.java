/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageoperatorrevenueshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.components.POService;
import org.oscm.internal.components.ServiceSelector;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * @author barzu
 */
public class ManageOperatorRevenueShareCtrl implements Serializable {

    private static final long serialVersionUID = -4998013853927319372L;

    private static final String INFO_SAVED = "info.revenueshare.saved";

    private ManageOperatorRevenueShareModel model;

    UiDelegate ui = new UiDelegate();
    ServiceLocator sl = new ServiceLocator();

    /**
     * initializer method called by <adm:initialize />
     * 
     * @return empty string (due to value jsf binding )
     * 
     *         workaround: to be refactored under jsf 2.0
     * 
     */
    public String getInitializePage() {
        initializeModel();
        return "";
    }

    void initializeModel() {
        long serviceKey = model.getSelectedTemplateKey();
        if (serviceKey == 0) {
            resetModel();
        } else {
            intializeRevenueShares(serviceKey);
        }
        initTemplateServiceSelector();
    }

    void initTemplateServiceSelector() {
        List<SelectItem> uiTemplateServices = new ArrayList<SelectItem>();
        Response response = sl.findService(ServiceSelector.class)
                .getTemplateServices();
        List<POService> templates = response.getResultList(POService.class);
        for (POService templateService : templates) {
            uiTemplateServices.add(new SelectItem(Long.valueOf(templateService
                    .getKey()), templateService.getServiceId() + "("
                    + templateService.getVendorOrganizationId() + ")"));
        }
        model.setTemplates(uiTemplateServices);
    }

    void intializeRevenueShares(long serviceKey) {
        try {
            Response response = sl.findService(PricingService.class)
                    .getOperatorRevenueShare(serviceKey);
            POOperatorPriceModel operatorPriceModel = response
                    .getResult(POOperatorPriceModel.class);
            model.setOperatorRevenueShare(operatorPriceModel.getRevenueShare());
            model.setDefaultOperatorRevenueShare(operatorPriceModel
                    .getDefaultRevenueShare());
        } catch (ObjectNotFoundException e) {
            model.setSelectedTemplateKey(0L);
            resetModel();
            ui.handleException(e);
        } catch (OperationNotPermittedException e) {
            ui.handleException(e);
        }
    }

    void resetModel() {
        model.setOperatorRevenueShare(null);
        model.setDefaultOperatorRevenueShare(null);
        ui.resetDirty();
    }

    /**
     * Action method for changing the template select box
     */
    public void templateChanged(ValueChangeEvent event) {
        final long selectedServiceKey = ((Long) event.getNewValue())
                .longValue();
        if (selectedServiceKey != model.getSelectedTemplateKey()) {
            model.setSelectedTemplateKey(selectedServiceKey);
        }
    }

    /**
     * Action method for saving the operator revenue share for a service
     * template.
     */
    public void save() {
        try {
            Response response = sl.findService(PricingService.class)
                    .saveOperatorRevenueShare(model.getSelectedTemplateKey(),
                            model.getOperatorRevenueShare());
            ui.handle(response, INFO_SAVED);
        } catch (ObjectNotFoundException e) {
            model.setSelectedTemplateKey(0L);
            ui.handleException(e);
        } catch (ValidationException | ServiceOperationException
                | ConcurrentModificationException e) {
            ui.handleException(e);
        }
    }

    public ManageOperatorRevenueShareModel getModel() {
        return model;
    }

    public void setModel(ManageOperatorRevenueShareModel model) {
        this.model = model;
    }

}
