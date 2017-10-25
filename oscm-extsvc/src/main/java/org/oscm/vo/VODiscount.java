/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-11-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.math.BigDecimal;

/**
 * Represents a discount granted to an organization.
 */
public class VODiscount extends BaseVO {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4479757863394747330L;

    /**
     * The discount value in percent.
     */
    private BigDecimal value;

    /**
     * The start of the discount period.
     */
    private Long startTime;

    /**
     * The end of the discount period.
     */
    private Long endTime;

    /**
     * Retrieves the discount value.
     * 
     * @return the discount in percent
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Sets the discount value.
     * 
     * @param value
     *            the discount in percent
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Retrieves the date and time as of which the discount is valid.
     * 
     * @return the start of the discount period
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets the date and time as of which the discount is to be valid.
     * 
     * @param begin
     *            the start of the discount period
     */
    public void setStartTime(Long begin) {
        this.startTime = begin;
    }

    /**
     * Retrieves the date and time when the discount expires.
     * 
     * @return the end of the discount period
     */
    public Long getEndTime() {
        return endTime;
    }

    /**
     * Sets the date and time when the discount is to expire.
     * 
     * @param end
     *            the end of the discount period
     */
    public void setEndTime(Long end) {
        this.endTime = end;
    }
}
