/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                     
 *                                                                              
 *  Creation Date: 22.07.2011                                                      
 *                                                                              
 *  Completion Time: 22.07.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

/**
 * @author cheld
 * 
 */
public class BigDecimalAssertsTest {

    @Test
    public void checkEquals_roundString() {
        BigDecimalAsserts.checkEquals("30.12", "30.123456");
        BigDecimalAsserts.checkEquals("30.1235", "30.123456");
        BigDecimalAsserts.checkEquals("30.1", "30.1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkEquals_expectedMustHaveScale() {
        BigDecimalAsserts.checkEquals("30", "30.123456");
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkEquals_expectedMustHaveScaleOfTwo() {
        BigDecimalAsserts.checkEquals("30.1", "30.123456");
    }

    @Test
    public void checkEquals_fixScale() {
        BigDecimalAsserts.checkEquals("30.1235", "30.123456", 2);
    }

    @Test
    public void checkEquals_oneString() {
        BigDecimalAsserts.checkEquals("30.123456", new BigDecimal("30.123456"));
    }

    @Test
    public void checkEquals_oneDouble() {
        double expected = 1 / 3.0d;
        BigDecimal actual = new BigDecimal(1).divide(new BigDecimal(3), 50,
                RoundingMode.HALF_UP);
        BigDecimalAsserts.checkEquals(expected, actual);
    }

    @Test
    public void checkEquals_oneDoublelessScale() {
        double expected = 33.000001;
        BigDecimal actual = new BigDecimal("33.000001000022");
        BigDecimalAsserts.checkEquals(expected, actual);
    }

    @Test(expected = AssertionError.class)
    public void checkEquals_oneDoubleFail() {
        double expected = 33.000001;
        BigDecimal actual = new BigDecimal("33.0000001000022");
        BigDecimalAsserts.checkEquals(expected, actual);
    }
}
