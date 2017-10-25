/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business;

import java.math.BigDecimal;
import java.util.Locale;

import org.oscm.converter.PriceConverter;

public class BigDecimalJaxbCustomBinder {
    private static final PriceConverter converter = new PriceConverter();

    /**
     * Converts a String to BigDecimal
     * 
     * @param price
     *            Price as String
     * @return converted BigDecimal
     */
    public static BigDecimal parseBigDecimal(String price) {
        return new BigDecimal(price);
    }

    /**
     * Converts a BigDecimal instance to String using locale English.
     * 
     * @param price
     *            Price as BigDecimal
     * @return converted String
     */
    public static String printBigDecimal(BigDecimal price) {
        if (price == null) {
            return null;
        }
        BigDecimal roundedPrice = price.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING,
                PriceConverter.ROUNDING_MODE);
        return converter.getValueToDisplay(roundedPrice, false, Locale.ENGLISH);
    }
}
