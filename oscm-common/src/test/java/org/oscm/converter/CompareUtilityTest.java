/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Aleh Khomich.                                                      
 *                                                                              
 *  Creation Date: 25.01.2010                                                      
 *                                                                              
 *  Completion Time:                                               
 *                                                                              
 *******************************************************************************/
package org.oscm.converter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.logging.LoggerFactory;

/**
 * Unit tests for {@link BigDecimalComparator} class.
 * 
 * @author falkenhahn.
 * 
 */
public class CompareUtilityTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LoggerFactory.activateRollingFileAppender("./logs", null, "DEBUG");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // for future using.
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void ctors() {
        new BigDecimalComparator();
    }

    @Test
    public void nullValues() {
        assertFalse(BigDecimalComparator.isEqual(null, BigDecimal.ZERO));
        assertFalse(BigDecimalComparator.isEqual(null, null));
        assertFalse(BigDecimalComparator.isEqual(BigDecimal.ZERO, null));
    }

    @Test
    public void isEqual() {
        assertTrue(BigDecimalComparator.isEqual(new BigDecimal("0.0000"),
                BigDecimal.ZERO));
        assertTrue(BigDecimalComparator.isEqual(new BigDecimal("0.00001"),
                new BigDecimal(new BigInteger("1"), 5)));
        assertTrue(BigDecimalComparator.isEqual(new BigDecimal("0.00001"),
                new BigDecimal(new BigInteger("1"), 5).setScale(500)));
        assertFalse(BigDecimalComparator.isEqual(new BigDecimal("0.00001"),
                new BigDecimal(new BigInteger("1"), 6)));
    }

    @Test
    public void isZero() {
        assertTrue(BigDecimalComparator.isZero(new BigDecimal("0.0000")));
        assertTrue(BigDecimalComparator.isZero(new BigDecimal(new BigInteger(
                "0"), 4)));
        assertTrue(BigDecimalComparator.isZero(new BigDecimal(new BigInteger(
                "0"), 2)));
        assertTrue(BigDecimalComparator.isZero(new BigDecimal(new BigInteger(
                "0"), 50)));
        assertTrue(BigDecimalComparator.isZero(new BigDecimal(new BigInteger(
                "0"), 1000)));
        assertTrue(BigDecimalComparator.isZero(new BigDecimal(new BigInteger(
                "0"), 0)));
        assertTrue(BigDecimalComparator.isZero(BigDecimal.ZERO));
        assertFalse(BigDecimalComparator.isZero(BigDecimal.ONE));
        assertFalse(BigDecimalComparator.isZero(new BigDecimal("0.00001")));
        assertFalse(BigDecimalComparator.isZero(new BigDecimal(new BigInteger(
                "1"), 4)));
        assertTrue(BigDecimalComparator.isZero(new BigDecimal("0.00001")
                .setScale(4, RoundingMode.HALF_UP)));
        assertFalse(BigDecimalComparator.isZero(new BigDecimal("0.005")
                .setScale(4, RoundingMode.HALF_UP)));
    }
}
