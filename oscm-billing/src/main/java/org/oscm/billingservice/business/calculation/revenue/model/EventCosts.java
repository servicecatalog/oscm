/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 12.08.2010.                                                     
 *                                                                              
 *  Completion Time: 12.08.2010.                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.business.calculation.revenue.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.oscm.billingservice.dao.model.EventCount;
import org.oscm.converter.PriceConverter;

/**
 * Represent total events costs and event statistics.
 * 
 * @author Aleh Khomich
 */
public class EventCosts {
    /** Total events costs. */
    private BigDecimal totalCosts;

    /** List of information on which billing events occurred how often */
    private List<EventCount> eventCountList;

    /**
     * @param totalCosts
     *            the totalCosts to set
     */
    public void setTotalCosts(BigDecimal totalCosts) {
        this.totalCosts = totalCosts;
    }

    /**
     * @return the totalCosts
     */
    public BigDecimal getTotalCosts() {
        if (totalCosts == null) {
            return BigDecimal.ZERO;
        }
        return totalCosts;
    }

    /**
     * @return the totalCosts scaled by to decimal places round halt up
     */
    public BigDecimal getNormalizedTotalCosts() {
        if (totalCosts == null) {
            return BigDecimal.ZERO
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
        }
        return totalCosts.setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                RoundingMode.HALF_UP);
    }

    /**
     * @param eventCountList
     *            the eventCountList to set
     */
    public void setEventCountList(List<EventCount> eventCountList) {
        this.eventCountList = eventCountList;
    }

    /**
     * @return the eventCountList
     */
    public List<EventCount> getEventCountList() {
        return eventCountList;
    }

}
