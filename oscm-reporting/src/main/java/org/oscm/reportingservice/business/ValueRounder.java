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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.oscm.converter.PriceConverter;

/**
 * Rounds rdo price fields.
 */
public class ValueRounder {

    public static final String EMPTY_STRING = "";
    public static final int SCALING_FACTORS = 5;

    /**
     * Rounds subtotal values, uses a scale of 2 and half up rounding. Subtotals
     * with zero cost are always listed.
     * 
     * @param formatter
     *            used to format the big decimal value
     * @param subtotal
     *            value to round
     * @return
     */
    public static String roundSubtotal(PriceConverter formatter,
            BigDecimal subtotal) {
        return formatter.getValueToDisplay(subtotal.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING, RoundingMode.HALF_UP),
                true);
    }

    /**
     * Rounds values by using a scale of 2 and rounding mode half up. If a non
     * rounded value equals zero (i.e. one of the factors is zero) convert it to
     * an empty string.
     * 
     * @param formatter
     * @param value
     * @param factors
     *            used in the price calculation
     * @return
     */
    public static String roundValue(PriceConverter formatter, BigDecimal value,
            BigDecimal... factors) {

        if (BigDecimal.ZERO.compareTo(value) == 0) {
            if (factors.length == 0) {
                return EMPTY_STRING;
            } else if (containsZeroFactor(factors)) {
                return EMPTY_STRING;
            }
        }

        return formatter.getValueToDisplay(value.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING, RoundingMode.HALF_UP),
                true);
    }

    private static boolean containsZeroFactor(BigDecimal... factors) {
        boolean zeroFactorFound = false;

        int i = 0;
        while (i < factors.length && !zeroFactorFound) {
            if (factors[i] != null
                    && BigDecimal.ZERO.compareTo(factors[i]) == 0) {
                zeroFactorFound = true;
            }
            i++;
        }

        return zeroFactorFound;
    }

    /**
     * Rounds a value by using the specified scale with half up rounding and
     * without grouping. Even if the last part of the decimal value is 0 in
     * specified fraction digits, always all decimal places are filled with 0.
     * (e.g. value 1.2, 5 fraction digits, en locale => '1.20000' will be
     * returned)
     * 
     * @param value
     *            Value to be formatted.
     * @param locale
     *            Current user's locale for formatting.
     * @param fractionDigits
     *            Number of decimal places.
     * @return
     */
    public static String roundValue(BigDecimal value, Locale locale,
            int fractionDigits) {
        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(locale));
        df.setGroupingUsed(false);
        df.setMaximumFractionDigits(fractionDigits);
        df.setMinimumFractionDigits(fractionDigits);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(value);
    }

}
