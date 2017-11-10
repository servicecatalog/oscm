/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 20.04.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.pricemodel.external;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.internal.pricemodel.external.ExternalPriceModelException;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author iversen
 * 
 */
@ManagedBean
@ViewScoped
public class ExternalServicePriceModelCtrl extends ExternalPriceModelCtrl {

    public void initBean(VOServiceDetails selectedService) {
        initPersistedPriceModel(ServiceType.TEMPLATE, selectedService);
    }

    public void upload(VOServiceDetails service) {
        try {
            PriceModel priceModel = getExternalPriceModelService()
                    .getExternalPriceModelForService(service);
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

    public void reloadPriceModel(VOServiceDetails selectedService) {
        reloadPriceModel(ServiceType.TEMPLATE, selectedService);
    }

}
