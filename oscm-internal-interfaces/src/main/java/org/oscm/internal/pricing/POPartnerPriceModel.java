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
 * Represents the price model for a partner organization.
 * 
 * @author barzu
 */
public class POPartnerPriceModel implements Serializable {

    private static final long serialVersionUID = -2081586230351276035L;

    private PORevenueShare revenueShareBrokerModel;
    private PORevenueShare revenueShareResellerModel;

    public PORevenueShare getRevenueShareBrokerModel() {
        return revenueShareBrokerModel;
    }

    public void setRevenueShareBrokerModel(
            PORevenueShare revenueShareBrokerModel) {
        this.revenueShareBrokerModel = revenueShareBrokerModel;
    }

    public PORevenueShare getRevenueShareResellerModel() {
        return revenueShareResellerModel;
    }

    public void setRevenueShareResellerModel(
            PORevenueShare revenueShareResellerModel) {
        this.revenueShareResellerModel = revenueShareResellerModel;
    }

}
