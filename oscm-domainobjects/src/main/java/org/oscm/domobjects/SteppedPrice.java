/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 13.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * The domain object for a stepped price.
 * 
 * It can be attached to price models (depending on the users per subscription),
 * priced events (depending on the amount of events), priced parameters
 * (depending on the number of users or the parameters numeric value) and priced
 * options (depending on the number of users).
 * 
 * @author weiser
 * 
 */
@Entity
public class SteppedPrice extends DomainObjectWithHistory<SteppedPriceData> {

    private static final long serialVersionUID = -6554156861562109586L;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PriceModel priceModel;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PricedEvent pricedEvent;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private PricedParameter pricedParameter;

    public SteppedPrice() {
        setDataContainer(new SteppedPriceData());
    }

    public PriceModel getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(PriceModel priceModel) {
        this.priceModel = priceModel;
    }

    public PricedEvent getPricedEvent() {
        return pricedEvent;
    }

    public void setPricedEvent(PricedEvent pricedEvent) {
        this.pricedEvent = pricedEvent;
    }

    public PricedParameter getPricedParameter() {
        return pricedParameter;
    }

    public void setPricedParameter(PricedParameter pricedParameter) {
        this.pricedParameter = pricedParameter;
    }

    public Long getLimit() {
        return dataContainer.getLimit();
    }

    public void setLimit(Long limit) {
        this.dataContainer.setLimit(limit);
    }

    public BigDecimal getPrice() {
        return dataContainer.getPrice();
    }

    public void setPrice(BigDecimal price) {
        this.dataContainer.setPrice(price);
    }

    public BigDecimal getAdditionalPrice() {
        return dataContainer.getAdditionalPrice();
    }

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.dataContainer.setAdditionalPrice(additionalPrice);
    }

    public long getFreeEntityCount() {
        return dataContainer.getFreeEntityCount();
    }

    public void setFreeEntityCount(long freeEntityCount) {
        this.dataContainer.setFreeEntityCount(freeEntityCount);
    }

    /**
     * Copies this stepped price. The reference to the parent element has to be
     * set after copying.
     * 
     * @return the copy of this
     */
    public SteppedPrice copy() {
        SteppedPrice sp = new SteppedPrice();
        sp.setAdditionalPrice(getAdditionalPrice());
        sp.setFreeEntityCount(getFreeEntityCount());
        sp.setLimit(getLimit());
        sp.setPrice(getPrice());
        return sp;
    }
}
