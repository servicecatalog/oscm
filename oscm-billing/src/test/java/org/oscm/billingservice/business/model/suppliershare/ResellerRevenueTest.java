/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.model.suppliershare.ResellerRevenue;

/**
 * @author kulle
 * 
 */
public class ResellerRevenueTest {

    @Test
    public void calculate() {
        // given
        ResellerRevenue revenue = new ResellerRevenue();
        revenue.setServiceRevenue(new BigDecimal(100));
        revenue.setMarketplaceRevenue(new BigDecimal("10"));
        revenue.setOperatorRevenue(new BigDecimal(5));
        revenue.setResellerRevenue(new BigDecimal("20"));

        // when
        revenue.calculate();

        // then
        assertEquals(65, revenue.getOverallRevenue().intValue());
    }

}
