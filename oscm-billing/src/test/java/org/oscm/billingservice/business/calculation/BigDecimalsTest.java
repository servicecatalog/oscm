/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Oct 15, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.test.Numbers;

/**
 * @author kulle
 * 
 */
public class BigDecimalsTest {

    @Test
    public void calculatePercent() {
        // when
        BigDecimal percent = BigDecimals.calculatePercent(Numbers.BD10,
                new BigDecimal("100.04"));

        // then
        assertEquals(Numbers.BD10.setScale(2), percent);
    }

    @Test
    public void calculatePercent_zeroValue() {
        // when
        BigDecimal percent = BigDecimals.calculatePercent(Numbers.BD10,
                BigDecimal.ZERO);

        // then
        assertEquals(BigDecimal.ZERO.setScale(2), percent);
    }

    @Test
    public void calculatePercent_zeroPercent() {
        // when
        BigDecimal percent = BigDecimals.calculatePercent(BigDecimal.ZERO,
                Numbers.BD100);

        // then
        assertEquals(BigDecimal.ZERO.setScale(2), percent);
    }

    @Test
    public void calculatePercent_zero() {
        // when
        BigDecimal percent = BigDecimals.calculatePercent(BigDecimal.ZERO,
                BigDecimal.ZERO);

        // then
        assertEquals(BigDecimal.ZERO.setScale(2), percent);
    }

    @Test
    public void calculatePercent_scaling() {
        // given
        BigDecimal value = new BigDecimal("100.192");

        // when
        BigDecimal percent = BigDecimals.calculatePercent(Numbers.BD10, value);

        // then
        assertEquals(new BigDecimal("10.02").setScale(2), percent);
    }

    @Test
    public void calculatePercent_rounding() {
        // given
        BigDecimal value = new BigDecimal("100.152");

        // when
        BigDecimal percent = BigDecimals.calculatePercent(Numbers.BD10, value);

        // then
        assertEquals(new BigDecimal("10.02").setScale(2), percent);
    }

    @Test
    public void normalize() {
        // given
        double d = 1.004;

        // when
        BigDecimal normalizedValue = BigDecimals.normalize(d);

        // then
        assertEquals(new BigDecimal("1").setScale(2), normalizedValue);
    }

    @Test
    public void normalize_zero() {
        // given
        double d = 0.00;

        // when
        BigDecimal normalizedValue = BigDecimals.normalize(d);

        // then
        assertEquals(BigDecimal.ZERO.setScale(2), normalizedValue);
    }

    @Test
    public void normalize_scaling() {
        // given
        double d = 1.239;

        // when
        BigDecimal normalizedValue = BigDecimals.normalize(d);

        // then
        assertEquals(new BigDecimal("1.24").setScale(2), normalizedValue);
    }

    @Test
    public void normalize_rounding() {
        // given
        double d = 1.235;

        // when
        BigDecimal normalizedValue = BigDecimals.normalize(d);

        // then
        assertEquals(new BigDecimal("1.24").setScale(2), normalizedValue);
    }

}
