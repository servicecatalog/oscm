/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.math.BigDecimal;

/**
 * Template to store billing items and the determined costs for it.
 * 
 * @author Mike J&auml;ger
 * @param <T>
 *            The type of the concrete billing item.
 * 
 */
public class BillingItemCosts<T> {

    private T billingItem;
    private BigDecimal billingItemCosts;

    public T getBillingItem() {
        return billingItem;
    }

    public BigDecimal getBillingItemCosts() {
        if (billingItemCosts == null) {
            return BigDecimal.ZERO;
        }
        return billingItemCosts;
    }

    public void setBillingItem(T billingItem) {
        this.billingItem = billingItem;
    }

    public void setBillingItemCosts(BigDecimal billingItemCosts) {
        this.billingItemCosts = billingItemCosts;
    }

}
