/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Oct 24, 2014                                                      
 *                                                                                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.app.setup;

import org.junit.Test;

import org.oscm.app.domain.PlatformConfigurationKey;

/**
 * Tests the ConfigurationSettingsValidator.
 * 
 * @author Mao
 */
public class ConfigurationSettingsValidatorTest {

    @Test
    public void validateString() {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_MAIL_RESOURCE, "abc");
    }

    @Test
    public void validateLong() {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_TIMER_INTERVAL, "123");
    }

    @Test(expected = RuntimeException.class)
    public void validateLong_BadCase() {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_TIMER_INTERVAL, "abc");
    }

    @Test
    public void validateLong_MinValue() throws Exception {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_TIMER_INTERVAL,
                Long.valueOf(Long.MIN_VALUE));
    }

    @Test
    public void validateLong_MaxValue() throws Exception {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_TIMER_INTERVAL,
                Long.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void validateUrl() {
        ConfigurationSettingsValidator
                .validate(PlatformConfigurationKey.APP_BASE_URL,
                        "http://www.fujitsu.com");
    }

    @Test(expected = RuntimeException.class)
    public void validateUrl_BadCase() {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_BASE_URL, "abc");
    }

    @Test
    public void validateMail() {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS,
                "test@fnst.com");
    }

    @Test(expected = RuntimeException.class)
    public void validateMail_BadCase() {
        ConfigurationSettingsValidator.validate(
                PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS,
                "http://www.fujitsu.com");
    }
}
