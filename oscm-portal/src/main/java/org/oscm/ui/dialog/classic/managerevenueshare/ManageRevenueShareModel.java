/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 6, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.managerevenueshare;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.internal.pricing.POMarketplacePricing;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.POServicePricing;

/**
 * @author tokoda
 * 
 */
@ViewScoped
@ManagedBean(name="manageRevenueShareModel")
public class ManageRevenueShareModel {

    private List<SelectItem> templates = new ArrayList<SelectItem>();

    private long selectedTemplateKey;

    private POPartnerPriceModel partnerPriceModelForSelectedTemplate;

    private POMarketplacePricing pricingOfMarketplaceForSelectedTemplate;

    private List<POServicePricing> brokerServicePricings = new ArrayList<POServicePricing>();

    private List<POServicePricing> resellerServicePricings = new ArrayList<POServicePricing>();

    public List<SelectItem> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SelectItem> templates) {
        this.templates = templates;
    }

    public long getSelectedTemplateKey() {
        return selectedTemplateKey;
    }

    public void setSelectedTemplateKey(long selectedTemplateKey) {
        this.selectedTemplateKey = selectedTemplateKey;
    }

    public POMarketplacePricing getPricingOfMarketplaceForSelectedTemplate() {
        return pricingOfMarketplaceForSelectedTemplate;
    }

    public void setPricingOfMarketplaceForSelectedTemplate(
            POMarketplacePricing pricingOfMarketplaceForSelectedTemplate) {
        this.pricingOfMarketplaceForSelectedTemplate = pricingOfMarketplaceForSelectedTemplate;
    }

    public POPartnerPriceModel getPartnerPriceModelForSelectedTemplate() {
        return partnerPriceModelForSelectedTemplate;
    }

    public void setPartnerPriceModelForSelectedTemplate(
            POPartnerPriceModel partnerPriceModelForSelectedTemplate) {
        this.partnerPriceModelForSelectedTemplate = partnerPriceModelForSelectedTemplate;
    }

    public List<POServicePricing> getBrokerServicePricings() {
        return brokerServicePricings;
    }

    public void setBrokerServicePricings(
            List<POServicePricing> brokerServicePricings) {
        this.brokerServicePricings = brokerServicePricings;
    }

    public void addBrokerServicePricing(POServicePricing servicePricing) {
        this.brokerServicePricings.add(servicePricing);
    }

    public List<POServicePricing> getResellerServicePricings() {
        return resellerServicePricings;
    }

    public void setResellerServicePricings(
            List<POServicePricing> resellerServicePricings) {
        this.resellerServicePricings = resellerServicePricings;
    }

    public void addResellerServicePricing(POServicePricing servicePricing) {
        this.resellerServicePricings.add(servicePricing);
    }

    public boolean isServiceSelected() {
        return selectedTemplateKey != 0;
    }

    public boolean isServicePublished() {
        return isServiceSelected()
                && pricingOfMarketplaceForSelectedTemplate != null;
    }

    public boolean isServiceNotPublished() {
        return isServiceSelected()
                && pricingOfMarketplaceForSelectedTemplate == null;
    }

    public boolean isBrokerExisting() {
        return brokerServicePricings.size() > 0;
    }

    public boolean isResellerExisting() {
        return resellerServicePricings.size() > 0;
    }

    public boolean isBrokerSaveDisabled() {
        return !isServiceSelected()
                || (!isServicePublished() && !isBrokerExisting());
    }

    public boolean isResellerSaveDisabled() {
        return !isServiceSelected()
                || (!isServicePublished() && !isResellerExisting());
    }
}
