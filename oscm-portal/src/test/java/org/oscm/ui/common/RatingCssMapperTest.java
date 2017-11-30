/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.common;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * 
 * Test for a rating css mapper class
 * 
 * @author ryumshyn
 * 
 */
public class RatingCssMapperTest {

    @Test
    public void testAverageRatingZero() {
        assertEquals("0_0", RatingCssMapper.getRatingClass(BigDecimal.ZERO));
    }

    @Test
    public void testAverageRatingNull() {
        assertEquals("0_0", RatingCssMapper.getRatingClass(null));
    }

    @Test
    public void testAverageRatingMaxFloat() {
        assertEquals("5_0",
                RatingCssMapper.getRatingClass(new BigDecimal(Float.MAX_VALUE)));
    }

    @Test
    public void testAverageRatingMaxInt() {
        assertEquals("5_0", RatingCssMapper.getRatingClass(new BigDecimal(
                Integer.MAX_VALUE)));
    }

    @Test
    public void testAverageRatingMin() {
        assertEquals("0_5",
                RatingCssMapper.getRatingClass(new BigDecimal(Float.MIN_VALUE)));
    }

    @Test
    public void testAverageRatingValue1() {
        assertEquals("0_5",
                RatingCssMapper.getRatingClass(new BigDecimal(0.26456f)));
    }

    @Test
    public void testAverageRatingValue2() {
        assertEquals("1_0",
                RatingCssMapper.getRatingClass(new BigDecimal(0.546456f)));
    }

    @Test
    public void testAverageRatingValue3() {
        assertEquals("0_5",
                RatingCssMapper.getRatingClass(new BigDecimal(0.0000526456f)));
    }

    @Test
    public void testAverageRatingValue4() {
        assertEquals("1_5",
                RatingCssMapper.getRatingClass(new BigDecimal(1.046456f)));
    }

    @Test
    public void testAverageRatingValue5() {
        assertEquals("1_5",
                RatingCssMapper.getRatingClass(new BigDecimal(1.1f)));
    }

    @Test
    public void testAverageRatingValue6() {
        assertEquals("2_0",
                RatingCssMapper.getRatingClass(new BigDecimal(1.789f)));
    }

    @Test
    public void testAverageRatingValue7() {
        assertEquals("5_0",
                RatingCssMapper.getRatingClass(new BigDecimal(4.50002f)));
    }

    @Test
    public void testAverageRatingValue8() {
        assertEquals("4_5",
                RatingCssMapper.getRatingClass(new BigDecimal(4.1002f)));
    }

    @Test
    public void testAverageRatingValue9() {
        assertEquals("4_0",
                RatingCssMapper.getRatingClass(new BigDecimal(4.00f)));
    }

    @Test
    public void testAverageRatingValue10() {
        assertEquals("3_0",
                RatingCssMapper.getRatingClass(new BigDecimal(3.0f)));
    }

    @Test
    public void testAverageRatingValue11() {
        assertEquals("3_5",
                RatingCssMapper.getRatingClass(new BigDecimal(3.50f)));
    }

    @Test
    public void testAverageRatingValue12() {
        assertEquals("4_0",
                RatingCssMapper.getRatingClass(new BigDecimal(3.51f)));
    }
}
