/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.converter.PriceConverter;

/**
 * Class to represent the data on the parameter pricing information and the
 * calculated costs.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ParameterCosts {

    private BigDecimal overallCosts = BigDecimal.ZERO;
    private List<BillingItemCosts<XParameterPeriodValue>> billedItems = new ArrayList<BillingItemCosts<XParameterPeriodValue>>();

    public void addBilledItem(BillingItemCosts<XParameterPeriodValue> billedItem) {
        billedItems.add(billedItem);
        overallCosts = overallCosts.add(billedItem.getBillingItemCosts());
    }

    public List<BillingItemCosts<XParameterPeriodValue>> getBilledItems() {
        return billedItems;
    }

    public BigDecimal getOverallCosts() {
        if (overallCosts == null) {
            return BigDecimal.ZERO;
        }
        return overallCosts;
    }

    public BigDecimal getNormalizedOverallCosts() {
        if (overallCosts == null) {
            return BigDecimal.ZERO
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
        }
        return overallCosts.setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                RoundingMode.HALF_UP);
    }

}
