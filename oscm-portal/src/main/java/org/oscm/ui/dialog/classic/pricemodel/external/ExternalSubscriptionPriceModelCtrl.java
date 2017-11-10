/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19 lut 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.internal.pricemodel.external.ExternalPriceModelException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * This class is used to handle actions related to the subscription external
 * price model.
 *
 * @author BadziakP
 *
 */
@ManagedBean
@ViewScoped
public class ExternalSubscriptionPriceModelCtrl extends ExternalPriceModelCtrl {

    public void initBean(VOServiceDetails selectedService) {
        if (selectedService == null) {
            return;
        }
        if (selectedService.getPriceModel().isRelatedSubscription()) {
            showPersistedPriceModel(selectedService);
        }
    }

    public void upload(VOSubscriptionDetails subscription) throws SaaSApplicationException {

        if (subscription == null) {
            return;
        }

        try {
            PriceModel priceModel = getExternalPriceModelService()
                    .getExternalPriceModelForSubscription(subscription);
            if (priceModel == null) {
                throw new ExternalPriceModelException();
            }
            loadPriceModelContent(priceModel);
            addMessage(null, FacesMessage.SEVERITY_INFO,
                    INFO_EXTERNAL_PRICE_UPLOADED);
        } catch (ExternalPriceModelException e) {
            addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_EXTERNAL_PRICEMODEL_NOT_AVAILABLE);
        }
    }

    public void display(boolean isExternalPriceModelUploade, VOService selectedService) throws IOException, SaaSApplicationException {
        // if an external price model has not been just uploaded then show the
        // price model stored in the database.
        if (!isExternalPriceModelUploade) {
            showPersistedPriceModel(selectedService);
        }
        super.display();
    }

    public void reloadPriceModel(VOService voService) {
        if (voService.getPriceModel().isRelatedSubscription()) {
            showPersistedPriceModel(voService);
            return;
        }
        resetPriceModel();
    }

    public void reloadPriceModelForViewSubscription(VOService voService) {
        showPersistedPriceModel(voService);
    }

}
