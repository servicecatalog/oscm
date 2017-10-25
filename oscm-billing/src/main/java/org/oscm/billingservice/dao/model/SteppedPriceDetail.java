/*******************************************************************************
 *                                                                              
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 22.07.2010.                                                      
 *                                                                              
 *  Completion Time: 22.07.2010.                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.oscm.converter.PriceConverter;

/**
 * Transfer object. Contains calculated step cost and stepped price data.
 * 
 * @author Aleh Khomich.
 * 
 */
public class SteppedPriceDetail {

    /** Calculated cost of the step. */
    private BigDecimal cost = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
    private List<SteppedPriceData> priceData;
    private XParameterPeriodValue parent;

    public SteppedPriceDetail() {
    }

    public SteppedPriceDetail(BigDecimal cost) {
        addCosts(cost);
    }

    public void setParent(XParameterPeriodValue parent) {
        this.parent = parent;
    }

    /**
     * Getter for calculated cost of the step.
     * 
     * @return Calculated cost of the step.
     */
    public BigDecimal getNormalizedCost() {
        return cost.setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                RoundingMode.HALF_UP);
    }

    /**
     * Getter for calculated cost of the step.
     * 
     * @return Calculated cost of the step.
     */
    public BigDecimal getCost() {
        return cost;
    }

    public void setPriceData(List<SteppedPriceData> priceData) {
        this.priceData = priceData;
    }

    public List<SteppedPriceData> getPriceData() {
        return priceData;
    }

    public boolean areSteppedPricesDefined() {
        return priceData != null && !priceData.isEmpty();
    }

    public void addCosts(BigDecimal costs) {
        this.cost = this.cost.add(costs);
        if (parent != null) {
            parent.addTotalCostsForSteppedPrices(costs);
        }
    }
}
