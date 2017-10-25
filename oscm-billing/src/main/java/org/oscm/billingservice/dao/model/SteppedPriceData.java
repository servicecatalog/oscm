/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 08.09.2011                                                      
 *                                                                              
 *  Completion Time: 08.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import java.math.BigDecimal;

import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.SteppedPriceHistory;

/**
 * Represents the information for one concrete price step.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class SteppedPriceData {

    private BigDecimal additionalPrice;
    private BigDecimal basePrice;
    private Long limit;
    private long freeEntityCount;
    private BigDecimal stepEnityCount = BigDecimal.ZERO;
    private BigDecimal stepAmount = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    public SteppedPriceData(BigDecimal additionalPrice, long freeAmount,
            Long limit, BigDecimal price, BigDecimal stepAmount,
            BigDecimal stepEntityCount) {
        this.additionalPrice = additionalPrice;
        this.basePrice = price;
        this.limit = limit;
        this.freeEntityCount = freeAmount;
        this.stepAmount = stepAmount;
        this.stepEnityCount = stepEntityCount;

    }

    public SteppedPriceData(BigDecimal additionalPrice, long freeAmount,
            Long limit, BigDecimal price) {
        this.additionalPrice = additionalPrice;
        this.basePrice = price;
        this.limit = limit;
        this.freeEntityCount = freeAmount;

    }

    public SteppedPriceData(SteppedPriceHistory sph) {
        this.additionalPrice = sph.getAdditionalPrice();
        this.basePrice = sph.getPrice();
        this.limit = sph.getLimit();
        this.freeEntityCount = sph.getFreeEntityCount();

    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public Long getLimit() {
        return limit;
    }

    public long getFreeEntityCount() {
        return freeEntityCount;
    }

    public BigDecimal getStepEntityCount() {
        return stepEnityCount;
    }

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public void setFreeAmount(long freeAmount) {
        this.freeEntityCount = freeAmount;
    }

    public void setStepEntityCount(BigDecimal stepEntityCount) {
        this.stepEnityCount = stepEntityCount;
    }

    public BigDecimal getStepAmount() {
        return stepAmount;
    }

    public void setStepAmount(BigDecimal stepAmount) {
        this.stepAmount = stepAmount;
    }

}
