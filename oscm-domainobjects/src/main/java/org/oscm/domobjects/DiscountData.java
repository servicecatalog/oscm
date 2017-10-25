/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich                 
 *                                                                              
 *  Creation Date: 19.05.2010                                                     
 *                                                                              
 *  Completion Time: 19.05.2010                                            
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DiscountData extends DomainDataContainer {

    /**
     * ID.
     */
    private static final long serialVersionUID = -5184426268578130912L;

    /**
     * Value of discount in percents.
     */
    @Column(nullable = false)
    private BigDecimal value = BigDecimal.ZERO;

    /**
     * Start time of discount.
     */
    @Column
    private Long startTime;

    /**
     * End time of discount.
     */
    @Column
    private Long endTime;

    /**
     * Setting discount value.
     * 
     * @param value
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Getting discount value.
     * 
     * @return Discount value.
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Setting start date of discount.
     * 
     * @param startTime
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * Getting start time.
     * 
     * @return Start time for the discount,
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Setting end time of discount.
     * 
     * @param endTime
     */
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * Getting end time of discount.
     * 
     * @return End time for the discount,
     */
    public Long getEndTime() {
        return endTime;
    }
}
