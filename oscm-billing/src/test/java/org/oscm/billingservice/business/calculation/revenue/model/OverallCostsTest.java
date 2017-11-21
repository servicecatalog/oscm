/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                   
 *                                                                              
 *  Creation Date: 15.09.2011                                                      
 *                                                                              
 *  Completion Time: 15.09.2011                                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.OverallCosts;

/**
 * Test cases for OverallCosts
 * 
 * @author cheld
 */
public class OverallCostsTest {

    @Test
    public void set_newValue() {

        // given empty costs
        OverallCosts costs = OverallCosts.newInstance();

        // when
        costs = costs.set("EUR", BigDecimal.TEN);

        // then
        assertEquals(BigDecimal.TEN, costs.get("EUR"));
    }

    @Test
    public void set_replaceValue() {

        // given costs with value
        OverallCosts costs = OverallCosts.newInstance();
        costs = costs.add("EUR", BigDecimal.ONE);

        // when replacing existing value
        costs = costs.set("EUR", BigDecimal.TEN);

        // then
        assertEquals(BigDecimal.TEN, costs.get("EUR"));
    }

    @Test
    public void add_emptyBefore() {

        // given empty costs
        OverallCosts costs = OverallCosts.newInstance();

        // when adding
        costs = costs.add("EUR", BigDecimal.TEN);

        // then no exception is thrown
        assertEquals(BigDecimal.TEN, costs.get("EUR"));
    }

    @Test
    public void add_twoValues() {

        // given costs with value
        OverallCosts costs = OverallCosts.newInstance();
        costs = costs.add("EUR", BigDecimal.TEN);

        // when adding a value
        costs = costs.add("EUR", BigDecimal.TEN);

        // then both values have been added
        assertEquals(new BigDecimal("20"), costs.get("EUR"));
    }

    @Test
    public void add_differentCurrency() {

        // given costs with value
        OverallCosts costs = OverallCosts.newInstance();
        costs = costs.add("EUR", BigDecimal.TEN);

        // when adding a value with different currency
        costs = costs.add("USD", BigDecimal.TEN);

        // then the values have not been added
        assertEquals(BigDecimal.TEN, costs.get("EUR"));
        assertEquals(BigDecimal.TEN, costs.get("USD"));
    }

    @Test
    public void get() {

        // given costs with value
        OverallCosts costs = OverallCosts.newInstance();
        costs = costs.add("EUR", BigDecimal.TEN);

        // when retrieving
        BigDecimal amount = costs.get("EUR");

        // then
        assertEquals(BigDecimal.TEN, amount);
    }


}
