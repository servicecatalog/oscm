/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import java.io.Serializable;

/**
 * Represents the mapping between a service and its supplier and partner price
 * models.
 * 
 * @author barzu
 */
public class POServicePricing implements Serializable {

    private static final long serialVersionUID = 4157509758896325257L;

    private POServiceForPricing serviceForPricing; // required
    private POPartnerPriceModel partnerPriceModel; // required

    public POServiceForPricing getServiceForPricing() {
        return serviceForPricing;
    }

    public void setServiceForPricing(POServiceForPricing serviceForPricing) {
        this.serviceForPricing = serviceForPricing;
    }

    public POPartnerPriceModel getPartnerPriceModel() {
        return partnerPriceModel;
    }

    public void setPartnerPriceModel(POPartnerPriceModel partnerPriceModel) {
        this.partnerPriceModel = partnerPriceModel;
    }

}
