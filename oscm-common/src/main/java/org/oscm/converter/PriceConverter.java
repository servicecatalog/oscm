/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pravi                                               
 *                                                                              
 *  Creation Date: Oct 19, 2009                                                      
 *                                                                              
 *  Completion Time: Oct 2,2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.validator.routines.CurrencyValidator;

/**
 * @author pravi
 */
public class PriceConverter {

    /**
     * Number of digits to the left of the period. e.g. 123.45 has three integer
     * places. The sum of integer places and decimal places is used as the
     * precision in BigDecimal
     */
    public static int NUMBER_OF_INTEGER_PLACES = 50;

    /**
     * Number of digits to the right of the period. e.g. 123.45 has two decimal
     * places. This number is used as maximum scale in BigDecimal
     */
    public static int NUMBER_OF_DECIMAL_PLACES = 50;

    /**
     * The minimum number of fraction digits to the right of the decimal point
     * is 2.
     */
    public static final int MINIMUM_FRACTION_DIGIT = 2;

    /**
     * The number of fraction digits to the normalized price is 2.
     */
    public static final int NORMALIZED_PRICE_SCALING = 2;

    /**
     * Rounding mode used in price calculations
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * The format pattern used for the displaying of prices.
     */
    public static final String FORMAT_PATTERN_WITH_GROUPING = "#,##0.00#";

    public static final String FORMAT_PATTERN_WITHOUT_GROUPING = "#0.00#";

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private static final CurrencyValidator VALIDATOR = new CurrencyValidator() {

        private static final long serialVersionUID = -4392273405928369601L;

        @Override
        protected Format getFormat(String pattern, Locale locale) {
            DecimalFormat format = (DecimalFormat) super.getFormat(pattern,
                    locale);
            format.setMaximumIntegerDigits(NUMBER_OF_INTEGER_PLACES);
            format.setMaximumFractionDigits(NUMBER_OF_DECIMAL_PLACES);
            // avoid lost precision due to parsing to double:
            format.setParseBigDecimal(true);
            return format;
        }
    };

    private Locale activeLocale;

    public PriceConverter() {
        this(DEFAULT_LOCALE);
    }

    public PriceConverter(Locale locale) {
        if (locale == null) {
            setActiveLocale(DEFAULT_LOCALE);
        } else {
            setActiveLocale(locale);
        }
    }

    /**
     * Returns the formatted value as a String, which is displayed for a given
     * price of type BigDecimal. The formatting used takes into account the
     * given locale, and optionally a grouping separator based on the locale.
     * 
     * @param price
     *            the price as a BigDecimal to be formatted.
     * @param useGrouping
     *            a flag indicating whether a grouping for the formatting will
     *            be used or not.
     * @param locale
     *            the locale to use for the formatting.
     * @return the displayed price formatted value as a String.
     */
    public String getValueToDisplay(BigDecimal price, boolean useGrouping,
            Locale locale) {

        DecimalFormat nf = new DecimalFormat();
        nf.setDecimalFormatSymbols(new DecimalFormatSymbols(locale));
        nf.setGroupingUsed(useGrouping);
        nf.setMinimumFractionDigits(MINIMUM_FRACTION_DIGIT);
        if (useGrouping) {
            nf.applyPattern(PriceConverter.FORMAT_PATTERN_WITH_GROUPING);
        } else {
            nf.applyPattern(PriceConverter.FORMAT_PATTERN_WITHOUT_GROUPING);
        }

        String formattedPrice;
        if (price == null) {
            formattedPrice = nf.format(BigDecimal.ZERO);

        } else {
            if (price.scale() > MINIMUM_FRACTION_DIGIT) {
                nf.setMaximumFractionDigits(price.scale());
            }
            formattedPrice = nf.format(price);
        }
        return formattedPrice;

    }

    public String getValueToDisplay(BigDecimal price, boolean useGrouping) {
        return getValueToDisplay(price, useGrouping, getActiveLocale());
    }

    /**
     * Parses a price of type <code>String</code> and converts it to a
     * <code>BigDecimal</code>, considering the active locale. It accepts
     * maximum {@link #NUMBER_OF_INTEGER_PLACES} integer digits and
     * {@link #NUMBER_OF_DECIMAL_PLACES} decimal digits. It accepts grouping
     * separators according to the active locale, placed at any position in the
     * integer part, but not in the decimal part. It accepts no negative prices,
     * no <code>+</code> symbol and no currency symbols (like <code>€</code> or
     * <code>$</code>).
     * 
     * @param price
     *            The price to be parsed as <code>String</code>. May be
     *            <code>null</code>, otherwise it will be trimmed.
     * @return The parsed <code>BigDecimal</code> representing the
     *         <code>price</code>. Returns <code>BigDecimal.ZERO</code> if the
     *         given <code>price</code> parameter is <code>null</code> or it
     *         contains no characters other then blanks.
     * @throws ParseException
     *             if the specified <code>price</code> string represents no
     *             valid price as described.
     */
    public BigDecimal parse(String price) throws ParseException {
        return parse(price, false);
    }

    /**
     * Parses a price of type <code>String</code> and converts it to a
     * <code>BigDecimal</code>, considering the active locale. It accepts
     * maximum {@link #NUMBER_OF_INTEGER_PLACES} integer digits and
     * {@link #NUMBER_OF_DECIMAL_PLACES} decimal digits. It accepts grouping
     * separators according to the active locale, placed at any position in the
     * integer part, but not in the decimal part. It accepts no <code>+</code>
     * symbol and no currency symbols (like <code>€</code> or <code>$</code>).
     * Also, If <code>allosNegativePrice</code> is <code>true</code>, negative
     * price is not accepted.
     * 
     * @param price
     *            The price to be parsed as <code>String</code>. May be
     *            <code>null</code>, otherwise it will be trimmed.
     * @param allosNegativePrice
     *            Defined if negative price value is allowed or not. If value is
     *            <code>true</code>, negative price is not accepted.
     * @return The parsed <code>BigDecimal</code> representing the
     *         <code>price</code>. Returns <code>BigDecimal.ZERO</code> if the
     *         given <code>price</code> parameter is <code>null</code> or it
     *         contains no characters other then blanks.
     * @throws ParseException
     *             if the specified <code>price</code> string represents no
     *             valid price as described.
     */
    public BigDecimal parse(String price, boolean allowNegativePrice)
            throws ParseException {
        if (price == null || price.trim().length() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal bigDecimal = VALIDATOR.validate(price,
                FORMAT_PATTERN_WITH_GROUPING, getActiveLocale());
        if (bigDecimal == null) {
            throw new ParseException("Invalid price: " + price, 0);
        } else if (bigDecimal.compareTo(BigDecimal.ZERO) < 0
                && !allowNegativePrice) {
            // used compareTo() to ignore the scale
            throw new ParseException("Negative price: " + price, 0);
        }
        if (bigDecimal.scale() > NUMBER_OF_DECIMAL_PLACES
                || (bigDecimal.precision() - bigDecimal.scale()) > NUMBER_OF_INTEGER_PLACES) {
            throw new ParseException(
                    "ERROR_PRICEMODEL_INVALID_FRACTIONAL_PART", 0);
        }
        return bigDecimal;
    }

    public Locale getActiveLocale() {
        return activeLocale;
    }

    private void setActiveLocale(Locale locale) {
        activeLocale = locale;
    }

}
