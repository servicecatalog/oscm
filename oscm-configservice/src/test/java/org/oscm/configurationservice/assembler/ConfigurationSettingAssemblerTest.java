/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 08.11.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.configurationservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * Tests for the configuration setting assembler.
 * 
 * @author Mike J&auml;ger
 */
public class ConfigurationSettingAssemblerTest {

    /**
     * exactly 255 characters
     */
    private static final String MAX_LENGTH_URL = "http://01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567";

    @Test
    public void constructor() {
        // only for coverage
        ConfigurationSettingAssembler result = new ConfigurationSettingAssembler();
        assertNotNull(result);
    }

    @Test
    public void toValueObject_NullInput() {
        VOConfigurationSetting result = ConfigurationSettingAssembler
                .toValueObject(null);
        assertNull(result);
    }

    @Test
    public void toVOConfigurationSetting() {
        ConfigurationSetting input = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, "contextId", "http://www.google.de");
        input.setKey(658);
        VOConfigurationSetting result = ConfigurationSettingAssembler
                .toValueObject(input);
        assertNotNull(result);
        assertEquals(ConfigurationKey.BASE_URL, result.getInformationId());
        assertEquals("contextId", result.getContextId());
        assertEquals("http://www.google.de", result.getValue());
        assertEquals(input.getKey(), result.getKey());
        assertEquals(input.getVersion(), result.getVersion());
    }

    @Test
    public void toVOConfigurationSettings_EmptyList() {
        List<ConfigurationSetting> inputList = new ArrayList<ConfigurationSetting>();

        List<VOConfigurationSetting> resultList = ConfigurationSettingAssembler
                .toVOConfigurationSettings(inputList);
        assertNotNull(resultList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void toVOConfigurationSettings_OneElement() {
        List<ConfigurationSetting> inputList = new ArrayList<ConfigurationSetting>();
        ConfigurationSetting input = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, "contextId", "value");
        inputList.add(input);
        List<VOConfigurationSetting> resultList = ConfigurationSettingAssembler
                .toVOConfigurationSettings(inputList);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        VOConfigurationSetting entry = resultList.get(0);
        assertEquals(ConfigurationKey.BASE_URL, entry.getInformationId());
        assertEquals("contextId", entry.getContextId());
        assertEquals("value", entry.getValue());
    }

    @Test
    public void toVOConfigurationSettings_TwoElements() {
        List<ConfigurationSetting> inputList = new ArrayList<ConfigurationSetting>();
        ConfigurationSetting input = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, "contextId", "value");
        ConfigurationSetting input2 = new ConfigurationSetting(
                ConfigurationKey.HIDDEN_UI_ELEMENTS, "contextId", "value2");
        inputList.add(input);
        inputList.add(input2);
        List<VOConfigurationSetting> resultList = ConfigurationSettingAssembler
                .toVOConfigurationSettings(inputList);
        assertNotNull(resultList);
        assertEquals(2, resultList.size());
        VOConfigurationSetting entry = resultList.get(0);
        assertEquals(ConfigurationKey.BASE_URL, entry.getInformationId());
        assertEquals("value", entry.getValue());
        entry = resultList.get(1);
        assertEquals(ConfigurationKey.HIDDEN_UI_ELEMENTS,
                entry.getInformationId());
        assertEquals("value2", entry.getValue());
    }

    @Test
    public void validate_NullContext() {
        ConfigurationSetting domObj = givenConfigurationSetting(
                ConfigurationKey.BASE_URL, "http://www.fujitsu.com");
        VOConfigurationSetting input = givenVOConfigurationSetting(
                ConfigurationKey.BASE_URL, "http://www.fujitsu.com");
        try {
            ConfigurationSettingAssembler.validate(domObj, input);
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.REQUIRED, e.getReason());
        }
    }

    @Test
    public void testToConfigurationSetting_NotMandatory()
            throws ValidationException {
        ConfigurationKey[] notMandatoryKeysOfEachType = new ConfigurationKey[] {
                ConfigurationKey.LOG_LEVEL,
                ConfigurationKey.REPORT_ENGINEURL,
                ConfigurationKey.TIMER_INTERVAL_DISCOUNT_END_NOTIFICATION_OFFSET,
                ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT };
        for (int i = 0; i < notMandatoryKeysOfEachType.length; i++) {

            VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                    notMandatoryKeysOfEachType[i], "");

            ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                    notMandatoryKeysOfEachType[i], "");
            ConfigurationSettingAssembler.validate(dbConfigSetting,
                    voConfigSetting);
        }
    }

    @Test
    public void testToConfigurationSetting_Mandatory() throws Exception {
        ConfigurationKey[] mandatoryKeysOfEachType = new ConfigurationKey[] {
                ConfigurationKey.BASE_URL, ConfigurationKey.WS_TIMEOUT,

                ConfigurationKey.PSP_USAGE_ENABLED };
        for (int i = 0; i < mandatoryKeysOfEachType.length; i++) {
            verifyValidationError(mandatoryKeysOfEachType[i], null,
                    ReasonEnum.REQUIRED);
        }
    }

    @Test
    public void validate_String_Length() throws Exception {
        // given
        VOConfigurationSetting dbConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.LOG_LEVEL, MAX_LENGTH_URL);
        ConfigurationSetting configSetting = givenConfigurationSetting(
                ConfigurationKey.LOG_LEVEL, MAX_LENGTH_URL);

        // when
        ConfigurationSettingAssembler.validate(configSetting, dbConfigSetting);
    }

    @Test(expected = ValidationException.class)
    public void validate_String_Length_BadCase() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.LOG_LEVEL, MAX_LENGTH_URL + "a");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.LOG_LEVEL, MAX_LENGTH_URL + "a");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test
    public void validate_Url() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.BASE_URL, "http://www.fujitsu.com");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.BASE_URL, "http://www.fujitsu.com");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);

    }

    @Test(expected = ValidationException.class)
    public void validate_Url_BadCase() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.BASE_URL, "abc");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.BASE_URL, "abc");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test
    public void validate_Url_Length() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.BASE_URL, MAX_LENGTH_URL);
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.BASE_URL, MAX_LENGTH_URL);

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test(expected = ValidationException.class)
    public void validate_Url_Length_BadCase() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.BASE_URL, MAX_LENGTH_URL + "a");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.BASE_URL, MAX_LENGTH_URL + "a");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test
    public void validate_Long() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS, "3");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS, "3");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test
    public void validate_LongTrimBlanks() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS, " 3 \n");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS, "3");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
        // then
        assertEquals("3", voConfigSetting.getValue());
    }

    @Test(expected = ValidationException.class)
    public void validate_Long_BadCase() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS, "abc");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS, "abc");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test
    public void validate_Long_MinValue() throws Exception {

        for (ConfigurationKey configurationKey : ConfigurationKey.values()) {
            if (ConfigurationKey.TYPE_LONG.equals(configurationKey.getType())) {
                // test minValue
                Long minValue = configurationKey.getMinValue();
                if (minValue == null || minValue.longValue() == Long.MIN_VALUE) {
                    // minValue not set or lower limit: test lower limit
                    String value = String.valueOf(Long.MIN_VALUE);

                    VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                            configurationKey, value);
                    ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                            configurationKey, value);
                    ConfigurationSettingAssembler.validate(dbConfigSetting,
                            voConfigSetting);
                } else {
                    // minValue set, greater than lower limit: test exception
                    String value = String.valueOf(minValue.longValue() - 1L);
                    try {

                        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                                configurationKey, value);
                        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                                configurationKey, value);
                        ConfigurationSettingAssembler.validate(dbConfigSetting,
                                voConfigSetting);

                        fail("Expected ValidationException, as the value '"
                                + value + "' of " + configurationKey.name()
                                + " is smaller than minValue '" + minValue
                                + "'");
                    } catch (ValidationException e) {
                        // expected, as smaller than minValue
                        assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE,
                                e.getReason());
                    }
                }
            }
        }
    }

    @Test
    public void testToConfigurationSetting_Long_MaxValue() throws Exception {
        for (ConfigurationKey configurationKey : ConfigurationKey.values()) {
            if (ConfigurationKey.TYPE_LONG.equals(configurationKey.getType())) {
                // test maxValue
                Long maxValue = configurationKey.getMaxValue();
                if (maxValue == null || maxValue.longValue() == Long.MAX_VALUE) {
                    // maxValue not set or upper limit: test upper limit
                    String value = String.valueOf(Long.MAX_VALUE);

                    VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                            configurationKey, value);
                    ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                            configurationKey, value);
                    ConfigurationSettingAssembler.validate(dbConfigSetting,
                            voConfigSetting);
                } else {
                    // maxValue set, smaller than upper limit: test exception
                    String value = String.valueOf(maxValue.longValue() + 1L);
                    try {
                        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                                configurationKey, value);
                        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                                configurationKey, value);
                        ConfigurationSettingAssembler.validate(dbConfigSetting,
                                voConfigSetting);
                        fail("Expected ValidationException, as the value '"
                                + value + "' of " + configurationKey.name()
                                + " is greater than maxValue '" + maxValue
                                + "'");
                    } catch (ValidationException e) {
                        // expected, as greater than maxValue
                        assertEquals(ReasonEnum.VALUE_NOT_IN_RANGE,
                                e.getReason());
                    }
                }
            }
        }
    }

    @Test
    public void validate_Boolean() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.PSP_USAGE_ENABLED, "true");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.PSP_USAGE_ENABLED, "true");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test(expected = ValidationException.class)
    public void validate_Boolean_BadCase() throws Exception {
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.PSP_USAGE_ENABLED, "yes");
        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.PSP_USAGE_ENABLED, "yes");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test
    public void validate_Readonly() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.AUTH_MODE, "SAML_SP");

        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.AUTH_MODE, "SAML_SP");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test
    public void validate_Readonly_NULL() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.SSO_IDP_URL, "");

        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.SSO_IDP_URL, null);

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    @Test(expected = ValidationException.class)
    public void validate_Readonly_Modify() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.AUTH_MODE, "SAML_SP");

        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.AUTH_MODE, "INTERNAL");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);

        // then a ValidationException is expected.
    }

    @Test
    public void validate_NotReadonly_Modify() throws Exception {
        // given
        VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                ConfigurationKey.SSO_IDP_URL, "http://www.fujitsu.com");

        ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                ConfigurationKey.SSO_IDP_URL, "http://www.fujitsu.com");

        // when
        ConfigurationSettingAssembler
                .validate(dbConfigSetting, voConfigSetting);
    }

    private void verifyValidationError(ConfigurationKey configurationKey,
            String value, ReasonEnum reason) {
        try {

            VOConfigurationSetting voConfigSetting = givenVOConfigurationSetting(
                    configurationKey, value);

            ConfigurationSetting dbConfigSetting = givenConfigurationSetting(
                    configurationKey, value);
            ConfigurationSettingAssembler.validate(dbConfigSetting,
                    voConfigSetting);

        } catch (ValidationException e) {
            assertEquals(reason, e.getReason());
        }
    }

    private VOConfigurationSetting givenVOConfigurationSetting(
            ConfigurationKey configurationKey, String value) {
        VOConfigurationSetting voConfigSetting = new VOConfigurationSetting(
                configurationKey, Configuration.GLOBAL_CONTEXT, value);

        return voConfigSetting;

    }

    private ConfigurationSetting givenConfigurationSetting(
            ConfigurationKey configurationKey, String value) {
        ConfigurationSetting configSetting = new ConfigurationSetting(
                configurationKey, Configuration.GLOBAL_CONTEXT, value);
        return configSetting;

    }

}
