/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-13                                                     
 *                                                                             
 *******************************************************************************/
package org.oscm.internal.vo;

import java.math.BigDecimal;

import org.oscm.internal.vo.BaseVO;

/**
 * Represents a step (range) of the stepped pricing defined for a price model, a
 * parameter, or an event.
 * 
 */
public class VOSteppedPrice extends BaseVO {

    /** UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the parameter reference for the field used for stepped
     * parameter prices.
     */
    public static final String FIELD_NAME_PRICED_PARAMETER_VALUE = "pricedParameter";

    /**
     * The upper limit of the range for which the specified price factor is to
     * be applied.
     */
    private Long limit;

    /**
     * The price factor for the step.
     */
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * Sets the upper limit of the range for which the specified price factor is
     * to be applied.
     * 
     * @param limit
     *            the upper limit of the range
     */
    public void setLimit(Long limit) {
        this.limit = limit;
    }

    /**
     * Returns the upper limit of the range for with the specified price factor
     * is applied.
     * 
     * @return the upper limit of the range
     */
    public Long getLimit() {
        return limit;
    }

    /**
     * Sets the price factor for the range.
     * 
     * @param price
     *            the price factor
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the price factor of the range.
     * 
     * @return Price the price factor
     */
    public BigDecimal getPrice() {
        return price;
    }

}
