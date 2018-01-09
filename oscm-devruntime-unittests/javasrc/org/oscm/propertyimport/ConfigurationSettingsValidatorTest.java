/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                        
 *  Creation Date: Oct 24, 2014                                                                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.propertyimport;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Tests the ConfigurationSettingsValidator.
 * 
 * @author Mao
 */
public class ConfigurationSettingsValidatorTest {

    /**
     * Long value with exactly 255 characters
     */
    private static final String MAX_LENGTH_LONG = "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234";

    /**
     * URL value with exactly 255 characters
     */
    private static final String MAX_LENGTH_URL = "http://01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567";

    @Test
    public void testValidate_Long() {
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED, "123");
    }

    @Test(expected = RuntimeException.class)
    public void testValidate_Long_BadCase() {
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED, "abc");
    }

    @Test(expected = RuntimeException.class)
    public void testValidate_Long_Length_BadCase() {
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED,
                MAX_LENGTH_LONG + "5");
    }

    @Test
    public void testValidate_Long_MinValue() throws Exception {
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED,
                Long.valueOf(Long.MIN_VALUE));
    }

    @Test
    public void testValidate_Long_MaxValue() throws Exception {
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED,
                Long.valueOf(Long.MIN_VALUE));
    }

    @Test
    public void testValidate_Url() {
        ConfigurationSettingsValidator.validate(ConfigurationKey.BASE_URL,
                "http://www.fujitsu.com");
    }

    @Test(expected = RuntimeException.class)
    public void testValidate_Url_BadCase() {
        ConfigurationSettingsValidator.validate(ConfigurationKey.BASE_URL,
                "abc");
    }

    @Test
    public void testValidate_Url_Length() {
        ConfigurationSettingsValidator.validate(ConfigurationKey.BASE_URL,
                MAX_LENGTH_URL);
    }

    @Test(expected = RuntimeException.class)
    public void testValidate_Url_Length_BadCase() {
        ConfigurationSettingsValidator.validate(ConfigurationKey.BASE_URL,
                MAX_LENGTH_URL + "a");
    }

    @Test
    public void testValidate_Boolean() {
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_ENABLED, false);
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_ENABLED, true);
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_ENABLED, Boolean.TRUE);
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_ENABLED, Boolean.FALSE);
    }

    @Test(expected = RuntimeException.class)
    public void testValidate_Boolean_BadCase() {
        ConfigurationSettingsValidator.validate(
                ConfigurationKey.AUDIT_LOG_ENABLED, "YES");
    }

}
