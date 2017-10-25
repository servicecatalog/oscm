/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 17.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import java.math.BigDecimal;

import org.oscm.internal.base.BasePO;

/**
 * Represents the share of the generated revenue for a product, that will be
 * received by an organization involved in selling that product.
 * 
 * @author barzu
 */
public class PORevenueShare extends BasePO {

    private static final long serialVersionUID = 6782666510065936925L;
    private BigDecimal revenueShare;

    public PORevenueShare() {

    }

    public PORevenueShare(BigDecimal revenueShare) {
        this.revenueShare = revenueShare;
    }

    public BigDecimal getRevenueShare() {
        return revenueShare;
    }

    public void setRevenueShare(BigDecimal revenueShare) {
        this.revenueShare = revenueShare;
    }

}
