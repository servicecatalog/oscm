/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Oct 23, 2014                                                      
 *                                                                                                                        
 *                                                                              
 *******************************************************************************/
package org.oscm.app.setup;

import org.apache.commons.validator.GenericValidator;

import org.oscm.validator.ADMValidator;
import org.oscm.app.domain.PlatformConfigurationKey;

/**
 * Configuration setting validator
 * 
 * Author: Mao
 */
public class ConfigurationSettingsValidator {

    private static final UrlValidator DEFAULT_URL_VALIDATOR = new UrlValidator(
            null, null, 0L);

    public static void validate(PlatformConfigurationKey key, Object input) {
        String datatype = key.getType();
        String value = null;
        if (input != null) {
            value = input.toString();
        }

        if (value == null) {
            return;
        }

        String str = value.toString();
        if (str.length() == 0) {
            return;
        }

        if (PlatformConfigurationKey.TYPE_LONG.equals(datatype)) {
            validateLong(key, value);
        } else if (PlatformConfigurationKey.TYPE_URL.equals(datatype)) {
            validateUrl(key, value);
        } else if (PlatformConfigurationKey.TYPE_MAIL.equals(datatype)) {
            validateMail(key, value);
        } else if (PlatformConfigurationKey.TYPE_BOOLEAN.equals(datatype)) {
            validateBoolean(key, value);
        }
    }

    private static void validateUrl(PlatformConfigurationKey key, String value) {
        if (DEFAULT_URL_VALIDATOR.isValid(value)) {
            return;
        }
        throw new RuntimeException("The url format of " + key
                + " must be valid.");
    }

    private static void validateBoolean(PlatformConfigurationKey key,
            String value) {
        if (!ADMValidator.isBoolean(value)) {
            throw new RuntimeException("The value for" + key
                    + " must be TRUE or FALSE.");
        }
    }

    private static void validateMail(PlatformConfigurationKey key, String value) {
        if (!ADMValidator.isEmail(value)) {
            throw new RuntimeException("The email format of " + key
                    + " must be valid.");
        }
    }

    private static void validateLong(PlatformConfigurationKey key, String value) {
        Long minValue = Long.valueOf(Long.MIN_VALUE);
        Long maxValue = Long.valueOf(Long.MAX_VALUE);
        long parsedLong = parse(key, value, minValue, maxValue);
        if (!isInRange(parsedLong, minValue.longValue(), maxValue.longValue())) {
            throw new RuntimeException(
                    "The value for"
                            + key
                            + " is illegal.Please enter a valid integer value within the range of "
                            + minValue + " and " + maxValue + ".");
        }
    }

    public static boolean isInRange(long value, Long minValue, Long maxValue) {
        return !((minValue != null && value < minValue.longValue()) || (maxValue != null && value > maxValue
                .longValue()));
    }

    private static long parse(PlatformConfigurationKey key, String value,
            Long minValue, Long maxValue) {
        if (!GenericValidator.isLong(value)) {
            minValue = (minValue != null ? minValue : Long
                    .valueOf(Long.MIN_VALUE));
            maxValue = (maxValue != null ? maxValue : Long
                    .valueOf(Long.MAX_VALUE));
            throw new RuntimeException(
                    "The value for"
                            + key
                            + " is illegal.Please enter a valid integer value within the range of "
                            + minValue + " and " + maxValue + ".");
        }
        return Long.parseLong(value);
    }

}
