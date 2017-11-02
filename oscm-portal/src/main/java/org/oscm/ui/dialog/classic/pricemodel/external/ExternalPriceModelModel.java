/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import java.nio.charset.StandardCharsets;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.ws.rs.core.MediaType;

import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;

/**
 * @author stavreva
 * 
 */
@ManagedBean
@ViewScoped
public class ExternalPriceModelModel {

    private String billingId;
    private PriceModelContent selectedPriceModelContent;
    private PriceModel selectedPriceModel;
    private String selectedPriceModelId;
    private boolean savedByUser;

    public String getBillingId() {
        return billingId;
    }

    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }

    public PriceModelContent getSelectedPriceModelContent() {
        return selectedPriceModelContent;
    }

    public void setSelectedPriceModelContent(
            PriceModelContent selectedPriceModelContent) {
        this.selectedPriceModelContent = selectedPriceModelContent;
    }

    /**
     * Returns the JSON representation of the price model as a string. The
     * representation is only valid for external price models.
     * 
     * @return
     */
    public String getContentAsJSON() {
        if (selectedPriceModelContent != null
                && MediaType.APPLICATION_JSON.equals(selectedPriceModelContent
                        .getContentType())) {
            return new String(selectedPriceModelContent.getContent(),
                    StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    public String getSelectedPriceModelId() {
        return selectedPriceModelId;
    }

    public void setSelectedPriceModelId(String selectedPriceModelId) {
        this.selectedPriceModelId = selectedPriceModelId;
    }

    public PriceModel getSelectedPriceModel() {
        return selectedPriceModel;
    }

    public void setSelectedPriceModel(PriceModel selectedPriceModel) {
        this.selectedPriceModel = selectedPriceModel;
    }

    public boolean isSavedByUser() {
        return savedByUser;
    }

    public void setSavedByUser(boolean savedByUser) {
        this.savedByUser = savedByUser;
    }
}
