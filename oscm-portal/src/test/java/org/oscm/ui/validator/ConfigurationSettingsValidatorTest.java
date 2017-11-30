/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: barzu                                        
 *                                                                              
 *  Creation Date: Dec 14, 2011                                                      
 *                                                                              
 *  Completion Time: Dec 14, 2011                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Tests the ConfigurationSettingsValidator.
 * 
 * @author barzu
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
    /**
     * Email value with exactly 100 characters
     */
    private static final String MAX_LENGTH_EMAIL = "a123456789@a123456789012345678901234567890123456789012345678901234567890123456789012345678901234.com";

    private static final String[] TYPES = new String[] {
            ConfigurationKey.TYPE_STRING, ConfigurationKey.TYPE_LONG,
            ConfigurationKey.TYPE_URL, ConfigurationKey.TYPE_MAIL,
            ConfigurationKey.TYPE_BOOLEAN };

    private ConfigurationSettingsValidator validator;
    private FacesContext context;

    private static final UIComponentStub getComponent(String datatype,
            boolean mandatory, Long min, Long max) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dataType", datatype);
        map.put("required", Boolean.valueOf(mandatory));
        map.put("minValue", min);
        map.put("maxValue", max);
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        return new UIComponentStub(map);
    }

    @Before
    public void setup() {
        validator = new ConfigurationSettingsValidator();
        context = new FacesContextStub(Locale.ENGLISH);
    }

    @Test
    public void testValidate_String_NotMandatory() {
        for (int i = 0; i < TYPES.length; i++) {
            UIComponentStub stub = getComponent(TYPES[i], false, null, null);
            validator.validate(context, stub, "");
            validator.validate(context, stub, null);
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeString_Empty() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_STRING, true,
                null, null);

        // when
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeString_Null() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_STRING, true,
                null, null);

        // when
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeLong_Empty() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG, true,
                null, null);

        // when
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeLong_Null() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG, true,
                null, null);

        // when
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeUrl_Empty() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_URL, true,
                null, null);

        // when
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeUrl_Null() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_URL, true,
                null, null);

        // when
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeMail_Empty() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_MAIL, true,
                null, null);

        // when
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeMail_Null() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_MAIL, true,
                null, null);

        // when
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeBoolean_Empty() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_BOOLEAN,
                true, null, null);

        // when
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Mandatory_TypeBoolean_Null() {
        // given
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_BOOLEAN,
                true, null, null);

        // when
        validator.validate(context, stub, null);
    }

    @Test
    public void testValidate_String_Mandatory() {
        for (int i = 0; i < TYPES.length; i++) {
            UIComponentStub stub = getComponent(TYPES[i], true, null, null);
            try {
                validator.validate(context, stub, "");
                fail("ValidatorException expected for empty configuration setting of type "
                        + TYPES[i]);
            } catch (ValidatorException e) {
            }
            try {
                validator.validate(context, stub, null);
                fail("ValidatorException expected for null configuration setting of type "
                        + TYPES[i]);
            } catch (ValidatorException e) {
            }
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_EmptyMandatory() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_STRING, true,
                null, null);
        validator.validate(context, stub, "");
    }

    @Test
    public void testValidate_String() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_STRING,
                false, null, null);
        validator.validate(context, stub, "abc");
    }

    @Test
    public void testValidate_String_Length() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_STRING,
                false, null, null);
        validator.validate(context, stub, MAX_LENGTH_URL);
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_String_Length_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_STRING,
                false, null, null);
        validator.validate(context, stub, MAX_LENGTH_URL + "a");
    }

    @Test
    public void testValidate_Long() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG, false,
                null, null);
        validator.validate(context, stub, "123");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_Long_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG, false,
                null, null);
        validator.validate(context, stub, "abc");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_Long_Length_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG, false,
                null, null);
        validator.validate(context, stub, MAX_LENGTH_LONG + "5");
    }

    @Test
    public void testValidate_Long_MinValue() throws Exception {
        for (ConfigurationKey configurationKey : ConfigurationKey.values()) {
            if (ConfigurationKey.TYPE_LONG.equals(configurationKey.getType())) {
                // test minValue
                Long minValue = configurationKey.getMinValue();
                UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG,
                        false, minValue, null);
                if (minValue == null || minValue.longValue() == Long.MIN_VALUE) {
                    // minValue not set or lower limit: test lower limit
                    String value = String.valueOf(Long.MIN_VALUE);
                    validator.validate(context, stub, value);
                } else {
                    // minValue set, greater than lower limit: test exception
                    String value = String.valueOf(minValue.longValue() - 1L);
                    try {
                        validator.validate(context, stub, value);
                        fail("Expected ValidatorException, as the value '"
                                + value + "' of " + configurationKey.name()
                                + " is smaller than minValue '" + minValue
                                + "'");
                    } catch (ValidatorException e) {
                        // expected, as smaller than minValue
                    }
                }
            }
        }
    }

    @Test
    public void testValidate_Long_MaxValue() throws Exception {
        for (ConfigurationKey configurationKey : ConfigurationKey.values()) {
            if (ConfigurationKey.TYPE_LONG.equals(configurationKey.getType())) {
                // test maxValue
                Long maxValue = configurationKey.getMaxValue();
                UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG,
                        false, null, maxValue);
                if (maxValue == null || maxValue.longValue() == Long.MAX_VALUE) {
                    // maxValue not set or upper limit: test upper limit
                    String value = String.valueOf(Long.MAX_VALUE);
                    validator.validate(context, stub, value);
                } else {
                    // maxValue set, smaller than upper limit: test exception
                    String value = String.valueOf(maxValue.longValue() + 1L);
                    try {
                        validator.validate(context, stub, value);
                        fail("Expected ValidatorException, as the value '"
                                + value + "' of " + configurationKey.name()
                                + " is greater than maxValue '" + maxValue
                                + "'");
                    } catch (ValidatorException e) {
                        // expected, as greater than maxValue
                    }
                }
            }
        }
    }

    @Test
    public void testValidate_Url() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_URL, false,
                null, null);
        validator.validate(context, stub, "http://www.fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_Url_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_URL, false,
                null, null);
        validator.validate(context, stub, "abc");
    }

    @Test
    public void testValidate_Url_Length() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_URL, false,
                null, null);
        validator.validate(context, stub, MAX_LENGTH_URL);
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_Url_Length_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_URL, false,
                null, null);
        validator.validate(context, stub, MAX_LENGTH_URL + "a");
    }

    @Test
    public void testValidate_Email() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_MAIL, false,
                null, null);
        validator.validate(context, stub, "info@est.fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_Email_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_MAIL, false,
                null, null);
        validator.validate(context, stub, "abc");
    }

    @Test
    public void testValidate_Email_Length() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_MAIL, false,
                null, null);
        validator.validate(context, stub, MAX_LENGTH_EMAIL);
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_Email_Length_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_MAIL, false,
                null, null);
        validator.validate(context, stub, MAX_LENGTH_EMAIL + "a");
    }

    @Test
    public void testValidate_Boolean() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_BOOLEAN,
                false, null, null);
        validator.validate(context, stub, "true");
        validator.validate(context, stub, "TRUE");
        validator.validate(context, stub, "false");
        validator.validate(context, stub, "FALSE");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_Boolean_BadCase() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_BOOLEAN,
                false, null, null);
        validator.validate(context, stub, "yes");
    }

    @Test
    public void testValidate_EmailTrimBlanks() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_MAIL, false,
                null, null);
        validator.validate(context, stub, " info@est.fujitsu.com  ");
    }

    @Test
    public void testValidate_LongTrimBlanks() {
        UIComponentStub stub = getComponent(ConfigurationKey.TYPE_LONG, false,
                null, null);
        validator.validate(context, stub, " 123 ");
    }
}
