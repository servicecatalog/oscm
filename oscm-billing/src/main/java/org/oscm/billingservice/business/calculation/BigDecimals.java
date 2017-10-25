/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation;

import java.math.BigDecimal;
import java.math.MathContext;

import org.oscm.converter.PriceConverter;

public class BigDecimals {

    public static final BigDecimal BD_100 = new BigDecimal(100);

    /**
     * Multiplies the big decimal value with the double factor, using BigDecimal
     * values to not loose precision when internal calculation with double
     * precision takes place.
     * 
     * @param x
     *            The value to be multiplied.
     * @param factor
     *            The factor of type double.
     * @return The rounded multiplication result.
     */
    public static BigDecimal multiply(final BigDecimal x, double factor) {
        BigDecimal multiplicand = new BigDecimal(factor, MathContext.DECIMAL64);
        BigDecimal multiplicationResult = x.multiply(multiplicand);
        return multiplicationResult;
    }

    /**
     * Calculates the given percent of the given value. A scaling of two and
     * rounding mode half up is used for calculation.
     * 
     * @param percent
     *            the given percentage
     * @param value
     *            the given value
     * @return calculated percentage
     */
    public static BigDecimal calculatePercent(final BigDecimal percent,
            final BigDecimal value) {
        return percent.multiply(value).divide(BD_100,
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
    }

    /**
     * Normalizes the given double value to a new big decimal value, using a
     * rounding mode half up and scaling of two.
     * 
     * @param value
     *            given double value
     * @return normalized value
     */
    public static BigDecimal normalize(double value) {
        return new BigDecimal(value).setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
    }
}
