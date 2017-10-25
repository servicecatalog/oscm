/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mao                                        
 *                                                                              
 *  Creation Date: Oct 23, 2014                                                      
 *                                                                                                                        
 *                                                                              
 *******************************************************************************/
package org.oscm.propertyimport;

import org.apache.commons.validator.GenericValidator;

import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Configuration setting validator
 */
public class ConfigurationSettingsValidator {

    public static void validate(ConfigurationKey key, Object input) {
        String datatype = key.getType();
        String value = null;
        if (input != null) {
            value = input.toString();
        }

        if (value == null) {
            return;
        }
        if (value.length() == 0) {
            return;
        }

        if (ConfigurationKey.TYPE_LONG.equals(datatype)) {
            validateLength(key, value, ADMValidator.LENGTH_DESCRIPTION);
            validateLong(key, value);
        } else if (ConfigurationKey.TYPE_URL.equals(datatype)) {
            validateLength(key, value, ADMValidator.LENGTH_DESCRIPTION);
            validateUrl(key, value);
        } else if (ConfigurationKey.TYPE_MAIL.equals(datatype)) {
            // the server also allows only 100 chars for email
            validateLength(key, value, ADMValidator.LENGTH_NAME);
            validateMail(key, value);
        } else if (ConfigurationKey.TYPE_BOOLEAN.equals(datatype)) {
            validateBoolean(key, value);
            // no need to validate length
        } else {
            validateLength(key, value, ADMValidator.LENGTH_DESCRIPTION);
        }
    }

    private static void validateUrl(ConfigurationKey key, String value) {
        if (ADMValidator.isUrl(value)) {
            return;
        }
        throw new RuntimeException("The url format of " + key.getKeyName()
                + " must be valid.");
    }

    private static void validateBoolean(ConfigurationKey key, String value) {
        if (!ADMValidator.isBoolean(value)) {
            throw new RuntimeException("The value for " + key.getKeyName()
                    + " must be TRUE or FALSE.");
        }
    }

    private static void validateMail(ConfigurationKey key, String value) {
        if (!ADMValidator.isEmail(value)) {
            throw new RuntimeException("The email format of "
                    + key.getKeyName() + " must be valid.");
        }
    }

    private static void validateLong(ConfigurationKey key, String value) {
        Long minValue = Long.valueOf(Long.MIN_VALUE);
        Long maxValue = Long.valueOf(Long.MAX_VALUE);
        long parsedLong = parse(key, value, minValue, maxValue);
        if (!isInRange(parsedLong, minValue, maxValue)) {
            throw new RuntimeException(
                    "The value for "
                            + key.getKeyName()
                            + " is illegal.Please enter a valid integer value within the range of "
                            + minValue + " and " + maxValue + ".");
        }
    }

    public static boolean isInRange(long value, Long minValue, Long maxValue) {
        return !((minValue != null && value < minValue.longValue()) || (maxValue != null && value > maxValue
                .longValue()));
    }

    private static long parse(ConfigurationKey key, String value,
            Long minValue, Long maxValue) {
        if (!GenericValidator.isLong(value)) {
            minValue = (minValue != null ? minValue : Long
                    .valueOf(Long.MIN_VALUE));
            maxValue = (maxValue != null ? maxValue : Long
                    .valueOf(Long.MAX_VALUE));
            throw new RuntimeException(
                    "The value for "
                            + key.getKeyName()
                            + " is illegal.Please enter a valid integer value within the range of "
                            + minValue + " and " + maxValue + ".");
        }
        return Long.parseLong(value);
    }

    private static void validateLength(ConfigurationKey key, String value,
            int maxLength) {
        if (value.length() > maxLength) {
            throw new RuntimeException("The value " + key.getKeyName()
                    + " must not be longer than" + maxLength + "characters.");
        }
    }
}
