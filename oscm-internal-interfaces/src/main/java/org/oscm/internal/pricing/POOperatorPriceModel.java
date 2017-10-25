/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Feb 22, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import java.io.Serializable;

/**
 * Represents a price model for a platform operator.
 * 
 * @author tokoda
 */
public class POOperatorPriceModel implements Serializable {

    private static final long serialVersionUID = -4556836375015808554L;

    private PORevenueShare revenueShare;
    private PORevenueShare defaultRevenueShare;

    public PORevenueShare getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(PORevenueShare revenueShare) {
        this.revenueShare = revenueShare;
    }

    public PORevenueShare getDefaultRevenueShare() {
        return defaultRevenueShare;
    }

    public void setDefaultRevenueShare(PORevenueShare defaultRevenueShare) {
        this.defaultRevenueShare = defaultRevenueShare;
    }
}
