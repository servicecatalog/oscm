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
 * Represents a price model for a marketplace.
 * 
 * @author barzu
 */
public class POMarketplacePriceModel implements Serializable {

    private static final long serialVersionUID = -9049459935906276305L;

    private PORevenueShare revenueShare;

    public PORevenueShare getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(PORevenueShare revenueShare) {
        this.revenueShare = revenueShare;
    }

}
