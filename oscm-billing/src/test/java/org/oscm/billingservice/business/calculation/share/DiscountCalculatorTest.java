/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 3, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import static org.oscm.test.Numbers.BD100;
import static org.oscm.test.Numbers.BD150;
import static org.oscm.test.Numbers.BD200;
import static org.oscm.test.Numbers.BD25;
import static org.oscm.test.Numbers.BD300;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.XmlSearch;

/**
 * @author farmaki
 * 
 */
public class DiscountCalculatorTest {

    private XmlSearch xmlSearch;

    @Before
    public void setup() {
        xmlSearch = mock(XmlSearch.class);
    }

    @Test
    public void calculateServiceRevenue() throws Exception {
        // given no customer discount
        List<BigDecimal> netAmounts = new ArrayList<BigDecimal>();
        netAmounts.add(BD200);
        netAmounts.add(BD100);
        doReturn(netAmounts).when(xmlSearch).retrieveNetAmounts(
                eq(Long.valueOf(1)));

        // when
        BigDecimal result = DiscountCalculator.calculateServiceRevenue(
                xmlSearch, Long.valueOf(1));

        // then
        assertEquals(BD300, result);
    }

    @Test
    public void calculateServiceRevenue_discount() throws Exception {
        // given a 25% percent discount
        List<BigDecimal> netAmounts = new ArrayList<BigDecimal>();
        netAmounts.add(BD100);
        netAmounts.add(BD100);
        doReturn(netAmounts).when(xmlSearch).retrieveNetAmounts(
                eq(Long.valueOf(1)));

        doReturn(BD25).when(xmlSearch).retrieveDiscountPercent();

        // when
        BigDecimal result = DiscountCalculator.calculateServiceRevenue(
                xmlSearch, Long.valueOf(1));

        // then
        assertEquals(BD150, result);
    }
}
