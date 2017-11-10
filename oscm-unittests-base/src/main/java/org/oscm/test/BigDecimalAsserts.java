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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Assert class to be used in JUnit tests. The class makes handling of
 * BigDecimal more convenient.
 * 
 * @author cheld
 * 
 */
public class BigDecimalAsserts {

    /**
     * Compares the expected value with the actual value. In case the actual
     * value has a fraction, then the expected value must defined with also a
     * fractional part. The actual value will be rounded, in order to make
     * writing test more convenient. The fractional part of the expected value
     * must have at least two digits (in case the actual value as a fractional
     * part with more than 2 digits.)<br>
     * e.g. <br>
     * checkEquals(30 , 30) == true <br>
     * checkEquals(30.12 , 30.1234) == true<br>
     * checkEquals(30.123 , 30.1234) == true<br>
     * checkEquals(30 , 30.1234) with throw IllegalArgumentException<br>
     * checkEquals(30.1 , 30.1234) with throw IllegalArgumentException<br>
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     */
    public static void checkEquals(String expected, String actual) {
        checkEqualsWithScaleOfExpected("", new BigDecimal(expected),
                new BigDecimal(actual));
    }

    /**
     * Compares the expected value with the actual value. In case the actual
     * value has a fraction, then the expected value must defined with also a
     * fractional part. The actual value will be rounded, in order to make
     * writing test more convenient. The fractional part of the expected value
     * must have at least two digits (in case the actual value as a fractional
     * part with more than 2 digits.)<br>
     * e.g. <br>
     * checkEquals(30 , 30) == true <br>
     * checkEquals(30.12 , 30.1234) == true<br>
     * checkEquals(30 , 30.1234) with throw IllegalArgumentException<br>
     * checkEquals(30.1 , 30.1234) with throw IllegalArgumentException<br>
     * 
     * @param message
     *            the message to be shown in the test report
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     */
    public static void checkEquals(String message, String expected,
            String actual) {
        checkEqualsWithScaleOfExpected(message, new BigDecimal(expected),
                new BigDecimal(actual));
    }

    /**
     * Compares the expected value with the actual value. Both values are
     * rounded to the given scale.
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     * @param scale
     *            the numbers of digits in the fraction
     */
    public static void checkEquals(String expected, String actual, int scale) {
        checkEqualsWithGivenScale("", new BigDecimal(expected), new BigDecimal(
                actual), scale);
    }

    /**
     * Compares the expected value with the actual value. Both values are
     * rounded to the given scale.
     * 
     * @param message
     *            the message to be shown in the test report
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     * @param scale
     *            the numbers of digits in the fraction
     */
    public static void checkEquals(String message, String expected,
            String actual, int scale) {
        checkEqualsWithGivenScale(message, new BigDecimal(expected),
                new BigDecimal(actual), scale);
    }

    /**
     * Compares the given expected value as string with the actual value. The
     * expected value is parsed into a BigDecimal.
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     */
    public static void checkEquals(String expected, BigDecimal actual) {
        checkEqualsWithScaleOfExpected("", new BigDecimal(expected), actual);
    }

    /**
     * Compares the given expected value as long with the actual value. The
     * expected value is converted into a BigDecimal.
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     */
    public static void checkEquals(long expected, BigDecimal actual) {
        checkEqualsWithScaleOfTen("", BigDecimal.valueOf(expected), actual);
    }

    /**
     * Compares the expected value with the actual value. Both values are
     * rounded to the given scale.
     * 
     * @param message
     *            the message to be shown in the test report
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     * @param scale
     *            numbers of digits in the fraction
     */
    public static void checkEquals(String message, BigDecimal expected,
            BigDecimal actual, int scale) {
        checkEqualsWithGivenScale(message, expected, actual, scale);

    }

    /**
     * Compares expected value as double with the actual value. The expected
     * value is converted into a BigDecimal.The fractional part of both values
     * will rounded with a precision of ten digits.<br>
     * e.g. <br>
     * checkEquals(30.00000000001 , 30.0) == true <br>
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     */
    public static void checkEquals(double expected, BigDecimal actual) {
        checkEqualsWithScaleOfTen("", new BigDecimal(expected), actual);
    }

    /**
     * Compares expected value as double with the actual value. The fractional
     * part of both values will rounded with a precision of ten digits.<br>
     * e.g. <br>
     * checkEquals(30.00000000001 , 30.0) == true <br>
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     */
    public static void checkEquals(BigDecimal expected, BigDecimal actual) {
        checkEqualsWithScaleOfTen("", expected, actual);
    }

    /**
     * Compares the expected value with the actual value. Both values are
     * rounded to the given scale.
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     * @param scale
     *            numbers of digits in the fraction
     */
    public static void checkEquals(BigDecimal expected, BigDecimal actual,
            int scale) {
        checkEqualsWithGivenScale("", expected, actual, scale);
    }

    /**
     * Compares the expected value with the actual value. Both values are
     * rounded to the given scale.
     * 
     * @param message
     *            the message to be shown in the test report
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     * @param the
     *            numbers of digits in the fraction
     */
    public static void checkEquals(String message, BigDecimal expected,
            BigDecimal actual) {
        checkEqualsWithScaleOfTen(message, expected, actual);

    }

    private static void checkEqualsWithScaleOfExpected(String message,
            BigDecimal expected, BigDecimal actual) {
        if (expected.scale() == 0 && actual.scale() > 0) {
            throw new IllegalArgumentException(
                    "Expected value must be given with more scale. Actual value is: "
                            + actual.toPlainString());
        }
        if (actual.scale() > 2 && expected.scale() < 2) {
            throw new IllegalArgumentException(
                    "Expected value must have at least scale of 2");
        }
        actual = actual.setScale(expected.scale(), RoundingMode.HALF_UP);
        assertEquals(message, expected, actual);
    }

    private static void checkEqualsWithGivenScale(String message,
            BigDecimal expected, BigDecimal actual, int scale) {
        BigDecimal adjustedValue = actual;
        if (actual != null) {
            adjustedValue = actual.setScale(scale, RoundingMode.HALF_UP);
        }
        assertEquals(message, expected.setScale(scale, RoundingMode.HALF_UP),
                adjustedValue);
    }

    private static void checkEqualsWithScaleOfTen(String message,
            BigDecimal expected, BigDecimal actual) {
        BigDecimal adjustedValue = actual;
        if (actual != null) {
            adjustedValue = actual.setScale(10, RoundingMode.HALF_UP);
        }
        assertEquals(message, expected.setScale(10, RoundingMode.HALF_UP),
                adjustedValue);
    }

}
