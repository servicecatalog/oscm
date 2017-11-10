/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                             
 *                                                                              
 *  Creation Date: 12.09.2011                                                      
 *                                                                              
 *  Completion Time: 12.09.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.Test;

import org.oscm.converter.PriceConverter;

public class ValueRounderTest {

    private PriceConverter formatter = new PriceConverter(Locale.ENGLISH);

    @Test
    public void roundValue_ZeroValueZeroScale() {
        // given
        // when
        String result = ValueRounder.roundValue(BigDecimal.ZERO,
                Locale.ENGLISH, 0);

        // then
        assertEquals("0", result);
    }

    @Test
    public void roundValue_RoundingLowerScale() {
        // given
        // when
        String result = ValueRounder.roundValue(BigDecimal.ZERO,
                Locale.ENGLISH, 1);

        // then
        assertEquals("0.0", result);
    }

    @Test
    public void roundValue_RoundingHigherScale() {
        // given
        // when
        String result = ValueRounder.roundValue(new BigDecimal("444.444"),
                Locale.ENGLISH, 2);

        // then
        assertEquals("444.44", result);
    }

    @Test
    public void roundValue_RoundingBigScale() {
        // given
        // when
        String result = ValueRounder.roundValue(new BigDecimal("1.1"),
                Locale.ENGLISH, 30);

        // then
        assertEquals("1.100000000000000000000000000000", result);
    }

    @Test
    public void roundValue_SmallValue1() {
        // given
        // when
        String result = ValueRounder.roundValue(new BigDecimal("-0.000019"),
                Locale.ENGLISH, 5);
        // then
        assertEquals("-0.00002", result);
    }

    @Test
    public void roundValue_SmallValue2() {
        String result = ValueRounder.roundValue(
                new BigDecimal("0.19999999999"), Locale.ENGLISH, 5);
        assertEquals("0.20000", result);
    }

    @Test
    public void roundValue_BigValue() {
        // given
        // when
        String result = ValueRounder.roundValue(new BigDecimal(
                "9999999999999999999999999999999999"), Locale.ENGLISH, 0);

        // then
        assertEquals("9999999999999999999999999999999999", result);
    }

    @Test
    public void roundValue_LocaleGerman() {
        // given
        // when
        String result = ValueRounder.roundValue(BigDecimal.ONE, Locale.GERMAN,
                1);

        // then
        assertEquals("1,0", result);
    }

    @Test
    public void testRoundSubtotalZero() {
        String rounded = ValueRounder.roundSubtotal(formatter, BigDecimal.ZERO);
        assertEquals("0.00", rounded);
    }

    @Test
    public void testRoundSubtotalLowerScale() {
        BigDecimal value = BigDecimal.valueOf(1.1D);
        String rounded = ValueRounder.roundSubtotal(formatter, value);
        assertEquals("1.10", rounded);
    }

    @Test
    public void testRoundSubtotalEqualScale() {
        BigDecimal value = BigDecimal.valueOf(1.12D);
        String rounded = ValueRounder.roundSubtotal(formatter, value);
        assertEquals("1.12", rounded);
    }

    @Test
    public void testRoundSubtotalHigherScale() {
        BigDecimal value = BigDecimal.valueOf(1.123D);
        String rounded = ValueRounder.roundSubtotal(formatter, value);
        assertEquals("1.12", rounded);
    }

    @Test
    public void testRoundValueZeroNoFactors() {
        BigDecimal value = BigDecimal.ZERO;
        String rounded = ValueRounder.roundValue(formatter, value,
                new BigDecimal[0]);
        assertEquals("", rounded);
    }

    @Test
    public void testRoundValueNotRoundedZero() {
        BigDecimal value = BigDecimal.ZERO;
        String rounded = ValueRounder.roundValue(formatter, value,
                BigDecimal.ONE, BigDecimal.ZERO);
        assertEquals("", rounded);
    }

    @Test
    public void testRoundValueRoundedZero() {
        BigDecimal value = BigDecimal.ZERO;
        String rounded = ValueRounder.roundValue(formatter, value,
                BigDecimal.ONE, BigDecimal.TEN);
        assertEquals("0.00", rounded);
    }

    @Test
    public void testRoundValueLowerScale() {
        BigDecimal value = BigDecimal.valueOf(10.1);
        String rounded = ValueRounder.roundValue(formatter, value,
                BigDecimal.ONE, BigDecimal.TEN);
        assertEquals("10.10", rounded);
    }

    @Test
    public void testRoundValueEqualScale() {
        BigDecimal value = BigDecimal.valueOf(10.15);
        String rounded = ValueRounder.roundValue(formatter, value,
                BigDecimal.ONE, BigDecimal.TEN);
        assertEquals("10.15", rounded);
    }

    @Test
    public void testRoundValueHigherScale() {
        BigDecimal value = BigDecimal.valueOf(10.1234);
        String rounded = ValueRounder.roundValue(formatter, value,
                BigDecimal.ONE, BigDecimal.TEN);
        assertEquals("10.12", rounded);
    }

}
