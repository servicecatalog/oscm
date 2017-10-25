/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 13.07.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.oscm.converter.PriceConverter;

/**
 * The data object for a price step.
 * 
 * @author weiser
 * 
 */
@Embeddable
public class SteppedPriceData extends DomainDataContainer {

    private static final long serialVersionUID = 1394397816918162089L;

    /**
     * The amount limit for which the specified price has to be used for
     * calculation.
     */
    @Column(name = "upperlimit")
    private Long limit;

    /**
     * The price for amounts in this step
     */
    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * An optional price that can be added
     */
    @Column(nullable = false)
    private BigDecimal additionalPrice = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    /**
     * A fraction of the number of entities to charge for which will not be
     * considered in the price calculation.
     */
    @Column(nullable = false)
    private long freeEntityCount;

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public long getFreeEntityCount() {
        return freeEntityCount;
    }

    public void setFreeEntityCount(long freeAmount) {
        this.freeEntityCount = freeAmount;
    }
}
