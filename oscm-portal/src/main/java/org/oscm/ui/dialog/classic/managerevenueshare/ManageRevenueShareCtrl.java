/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.managerevenueshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricing.POMarketplacePricing;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.POServiceForPricing;
import org.oscm.internal.pricing.POServicePricing;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;

/**
 * @author tokoda
 * 
 */
public class ManageRevenueShareCtrl implements Serializable {

    private static final long serialVersionUID = 7054369213367858762L;

    static final String INFO_SAVED = "info.revenueshares.saved";
    static final String CONCURRENT_MODIFICATION_ERROR = "concurrentModification";

    ManageRevenueShareModel model;

    PricingService pricingService;
    UiDelegate ui = new UiDelegate();

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
            intializePricingForSelectedTemplate(serviceKey);
        }
        initTemplateServiceSelector();
    }

    void intializePricingForSelectedTemplate(long serviceKey) {
        try {
            boolean marketplaceExists = initPricingOfMarketplaceForSelectedTemplate(serviceKey);
            if (marketplaceExists) {
                initPartnerPriceModelForSelectedTemplate(serviceKey);
            }
            initPartnerServicePricings(serviceKey);
        } catch (ObjectNotFoundException oex) {
            model.setSelectedTemplateKey(0);
            resetModel();
            ui.handleException(oex);
        } catch (ServiceStateException sex) {
            model.setSelectedTemplateKey(0);
            resetModel();
            ui.handleException(sex);
        } catch (SaaSApplicationException ex) {
            ui.handleException(ex);
        }
    }

    private void resetModel() {
        model.setPartnerPriceModelForSelectedTemplate(null);
        model.setPricingOfMarketplaceForSelectedTemplate(null);
        model.setBrokerServicePricings(new ArrayList<POServicePricing>());
        model.setResellerServicePricings(new ArrayList<POServicePricing>());
        ui.resetDirty();
    }

    boolean initPricingOfMarketplaceForSelectedTemplate(long serviceKey)
            throws ObjectNotFoundException, ServiceOperationException,
            ServiceStateException {
        POServiceForPricing service = new POServiceForPricing();
        service.setKey(serviceKey);
        Response response = getPricingService()
                .getMarketplacePricingForService(service);
        model.setPricingOfMarketplaceForSelectedTemplate(response
                .getResult(POMarketplacePricing.class));
        return model.getPricingOfMarketplaceForSelectedTemplate() != null;
    }

    void initPartnerPriceModelForSelectedTemplate(long serviceKey)
            throws SaaSApplicationException {
        POServiceForPricing service = new POServiceForPricing(serviceKey, 0);
        Response response = getPricingService()
                .getPartnerRevenueShareForAllStatesService(service);
        model.setPartnerPriceModelForSelectedTemplate(response
                .getResult(POPartnerPriceModel.class));
    }

    void initPartnerServicePricings(long serviceKey)
            throws ObjectNotFoundException, ServiceOperationException {
        POServiceForPricing service = new POServiceForPricing(serviceKey, 0);
        Response response = pricingService
                .getPartnerServicesWithRevenueShareForTemplate(service);
        List<POServicePricing> servicePricings = response
                .getResultList(POServicePricing.class);
        model.setBrokerServicePricings(new ArrayList<POServicePricing>());
        model.setResellerServicePricings(new ArrayList<POServicePricing>());
        for (POServicePricing servicePricing : servicePricings) {
            if (servicePricing.getPartnerPriceModel()
                    .getRevenueShareBrokerModel() != null) {
                model.addBrokerServicePricing(servicePricing);
            }
            if (servicePricing.getPartnerPriceModel()
                    .getRevenueShareResellerModel() != null) {
                model.addResellerServicePricing(servicePricing);
            }
        }
    }

    private void initTemplateServiceSelector() {
        List<SelectItem> uiTemplateServices = new ArrayList<SelectItem>();
        Response response = getPricingService().getTemplateServices();
        List<POServiceForPricing> templates = response
                .getResultList(POServiceForPricing.class);
        for (POServiceForPricing templateService : templates) {
            uiTemplateServices.add(new SelectItem(Long.valueOf(templateService
                    .getKey()), templateService.getServiceId() + "("
                    + templateService.getVendor().getOrganizationId() + ")"));
        }
        model.setTemplates(uiTemplateServices);
    }

    /**
     * action method for changing the template select box
     */
    public void templateChanged(ValueChangeEvent event) {
        final long selectedServiceKey = ((Long) event.getNewValue())
                .longValue();
        if (selectedServiceKey != model.getSelectedTemplateKey()) {
            model.setSelectedTemplateKey(selectedServiceKey);
        }
    }

    /**
     * action method for save button for manage broker revenue share page
     * 
     * @return null: stay on same page
     */
    public String saveForBroker() {
        return save(model.getBrokerServicePricings());
    }

    /**
     * action method for save button for manage reseller revenue share page
     * 
     * @return null: stay on same page
     */
    public String saveForReseller() {
        return save(model.getResellerServicePricings());
    }

    String save(List<POServicePricing> partnerServicePricings) {
        String outcome = null;
        List<POServicePricing> pricings = new ArrayList<POServicePricing>();

        if (model.isServicePublished()) {
            POServicePricing pricingForTemplate = new POServicePricing();
            POServiceForPricing service = new POServiceForPricing();
            service.setKey(model.getSelectedTemplateKey());
            pricingForTemplate.setServiceForPricing(service);
            pricingForTemplate.setPartnerPriceModel(model
                    .getPartnerPriceModelForSelectedTemplate());
            pricings.add(pricingForTemplate);
        }

        pricings.addAll(partnerServicePricings);

        try {
            Response response = getPricingService()
                    .savePartnerRevenueSharesForServices(pricings);
            ui.handle(response, INFO_SAVED);
        } catch (ObjectNotFoundException oex) {
            model.setSelectedTemplateKey(0);
            ui.handleException(oex);
        } catch (ServiceStateException sex) {
            model.setSelectedTemplateKey(0);
            ui.handleException(sex);
        } catch (ConcurrentModificationException cex) {
            outcome = CONCURRENT_MODIFICATION_ERROR;
            ui.handleException(cex);
        } catch (SaaSApplicationException ex) {
            ui.handleException(ex);
        }

        return outcome;
    }

    PricingService getPricingService() {
        if (pricingService == null) {
            pricingService = new ServiceLocator()
                    .findService(PricingService.class);
        }
        return pricingService;
    }

    public void setModel(ManageRevenueShareModel model) {
        this.model = model;
    }

    public ManageRevenueShareModel getModel() {
        return model;
    }

}
