/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import java.io.Serializable;

import org.oscm.internal.components.POMarketplace;

/**
 * Represents the mapping between a marketplace and the marketplace and partner
 * price models.
 * 
 * @author barzu
 */
public class POMarketplacePricing implements Serializable {

    private static final long serialVersionUID = 7720880687533966156L;

    private POMarketplace marketplace; // required
    private POMarketplacePriceModel marketplacePriceModel; // required
    private POPartnerPriceModel partnerPriceModel; // required

    public POMarketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(POMarketplace marketplace) {
        this.marketplace = marketplace;
    }

    public POMarketplacePriceModel getMarketplacePriceModel() {
        return marketplacePriceModel;
    }

    public void setMarketplacePriceModel(
            POMarketplacePriceModel marketplacePriceModel) {
        this.marketplacePriceModel = marketplacePriceModel;
    }

    public POPartnerPriceModel getPartnerPriceModel() {
        return partnerPriceModel;
    }

    public void setPartnerPriceModel(POPartnerPriceModel partnerPriceModel) {
        this.partnerPriceModel = partnerPriceModel;
    }
}
