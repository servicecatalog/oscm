/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.validator;

import static org.oscm.test.Numbers.L0;
import static org.oscm.test.Numbers.L100;
import static org.oscm.test.Numbers.L5;
import static org.oscm.test.Numbers.L50;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;

public class ParameterValueValidatorTest {

    private static final String DATA_TYPE = "dataType";
    private static final String REQUIRED = "required";
    private static final String MIN_VALUE = "minValue";
    private static final String MAX_VALUE = "maxValue";
    private static final String DATATYPE_STRING = "string";
    private static final String DATATYPE_DURATION = "duration";
    private static final String DATATYPE_LONG = "long";
    private static final String DATATYPE_INTEGER = "integer";

    private ParameterValueValidator validator;
    private FacesContext context;

    private static final UIComponentStub getComponent(String datatype,
            boolean mandatory, Long min, Long max) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DATA_TYPE, datatype);
        map.put(REQUIRED, Boolean.valueOf(mandatory));
        map.put(MIN_VALUE, min);
        map.put(MAX_VALUE, max);
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        return new UIComponentStub(map);
    }

    @Before
    public void setup() {
        validator = new ParameterValueValidator();
        context = new FacesContextStub(Locale.ENGLISH);
    }

    @Test
    public void testValidateString() {
        UIComponentStub stub = getComponent(DATATYPE_STRING, false, null, null);
        validator.validate(context, stub, "abc");
    }

    @Test
    public void testValidateStringEmpty() {
        // passing null as data type will be handled as string
        UIComponentStub stub = getComponent(null, false, null, null);
        validator.validate(context, stub, "   ");
    }

    @Test
    public void testValidateStringNull() {
        // passing null as mandatory will be handled as 'false'
        UIComponentStub stub = getComponent(DATATYPE_STRING, false, null, null);
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateStringTooLong() {
        UIComponentStub stub = getComponent(DATATYPE_STRING, false, null, null);
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            value.append("a");
        }
        validator.validate(context, stub, value.toString());
    }

    @Test
    public void testValidateStringMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_STRING, true, null, null);
        validator.validate(context, stub, "abc");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateStringEmptyMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_STRING, true, null, null);
        validator.validate(context, stub, "");
    }

    @Test
    public void testValidateStringWhitespacesMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_STRING, true, null, null);
        validator.validate(context, stub, "   ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateStringNullMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_STRING, true, null, null);
        validator.validate(context, stub, null);
    }

    @Test
    public void testValidateLong() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, null, null);
        validator.validate(context, stub, "1000");
    }

    @Test
    public void testValidateLongMinValue() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, null, null);
        validator.validate(context, stub, String.valueOf(Long.MIN_VALUE));
    }

    @Test
    public void testValidateLongMaxValue() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, null, null);
        validator.validate(context, stub, String.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void testValidateLongEmpty() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, null, null);
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongWhitespaces() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, null, null);
        validator.validate(context, stub, "   ");
    }

    @Test
    public void testValidateLongNull() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, null, null);
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongInvalid() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, null, null);
        validator.validate(context, stub, "xyz");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongTooLong() {
        UIComponentStub stub = getComponent(DATATYPE_STRING, false, null, null);
        StringBuilder value = new StringBuilder();
        value.append("1");
        for (int i = 0; i < 500; i++) {
            value.append("0");
        }
        validator.validate(context, stub, value.toString());
    }

    @Test
    public void testValidateLongRange() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, L0, L50);
        validator.validate(context, stub, "25");
    }

    @Test
    public void testValidateLongRangeMin() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, L0, L50);
        validator.validate(context, stub, "0");
    }

    @Test
    public void testValidateLongRangeMax() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, L0, L50);
        validator.validate(context, stub, "50");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongRangeBelow() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, L0, L50);
        validator.validate(context, stub, "-50");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongRangeAbove() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, false, L0, L50);
        validator.validate(context, stub, "100");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongEmptyMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, null, null);
        validator.validate(context, stub, "  ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongNullMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, null, null);
        validator.validate(context, stub, null);
    }

    @Test
    public void testValidateLongUpperLimitMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, null, L100);
        validator.validate(context, stub, "50");
    }

    @Test
    public void testValidateLongUpperLimitEqualsValueMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, null, L100);
        validator.validate(context, stub, "100");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongUpperLimitAboveMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, null, L100);
        validator.validate(context, stub, "150");
    }

    @Test
    public void testValidateLongLowerLimitMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, L100, null);
        validator.validate(context, stub, "150");
    }

    @Test
    public void testValidateLongLowerLimitEqualsValueMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, L100, null);
        validator.validate(context, stub, "100");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateLongLowerLimitBelowMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_LONG, true, L100, null);
        validator.validate(context, stub, "50");
    }

    @Test
    public void testValidateInteger() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, "50");
    }

    @Test
    public void testValidateIntegerMinValue() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, String.valueOf(Integer.MIN_VALUE));
    }

    @Test
    public void testValidateIntegerMaxValue() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, String.valueOf(Integer.MAX_VALUE));
    }

    @Test
    public void testValidateIntegerEmpty() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerWhitespaces() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, "   ");
    }

    @Test
    public void testValidateIntegerNull() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerInvalid() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, "abc");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerEmptyMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, null, null);
        validator.validate(context, stub, "  ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerNullMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, null, null);
        validator.validate(context, stub, null);
    }

    @Test
    public void testValidateIntegerLowerLimitMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, L100, null);
        validator.validate(context, stub, "150");
    }

    @Test
    public void testValidateIntegerLowerLimitEqualsValueMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, L100, null);
        validator.validate(context, stub, "100");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerLowerLimitBelowMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, L100, null);
        validator.validate(context, stub, "50");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerLongMinValue() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, String.valueOf(Long.MIN_VALUE));
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerLongMaxValue() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        validator.validate(context, stub, String.valueOf(Long.MAX_VALUE));
    }

    @Test
    public void testValidateIntegerRange() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, L0, L50);
        validator.validate(context, stub, "25");
    }

    @Test
    public void testValidateIntegerRangeMin() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, L0, L50);
        validator.validate(context, stub, "0");
    }

    @Test
    public void testValidateIntegerRangeMax() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, L0, L50);
        validator.validate(context, stub, "50");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerRangeBelow() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, L0, L50);
        validator.validate(context, stub, "-50");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerRangeAbove() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, L0, L50);
        validator.validate(context, stub, "100");
    }

    @Test
    public void testValidateIntegerUpperLimitMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, null, L100);
        validator.validate(context, stub, "50");
    }

    @Test
    public void testValidateIntegerUpperLimitEqualsValueMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, null, L100);
        validator.validate(context, stub, "100");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerUpperLimitAboveMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, true, null, L100);
        validator.validate(context, stub, "150");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerMinValueMinusOne() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        long value = ((long) Integer.MIN_VALUE) - 1;
        validator.validate(context, stub, String.valueOf(value));
    }

    @Test(expected = ValidatorException.class)
    public void testValidateIntegerMaxValuePlusOne() {
        UIComponentStub stub = getComponent(DATATYPE_INTEGER, false, null, null);
        long value = ((long) Integer.MAX_VALUE) + 1;
        validator.validate(context, stub, String.valueOf(value));
    }

    @Test
    public void testValidateDuration() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "1232");
    }

    @Test
    public void testValidateDurationEmpty() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationWhitespaces() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "   ");
    }

    @Test
    public void testValidateDurationLongNumber() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "9999999999");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationTooLongNumber() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        char[] nines = new char[256];
        Arrays.fill(nines, '9');
        validator.validate(context, stub, new String(nines));
    }

    @Test
    public void testValidateDurationNull() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationInvalid() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "abc");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationEmptyMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        validator.validate(context, stub, "  ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationNullMandatory() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        validator.validate(context, stub, null);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationNegative() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        validator.validate(context, stub, "-0.5");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationNegative2() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        validator.validate(context, stub, "-25");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationInvalidFormat1() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        validator.validate(context, stub, "123.000");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationInvalidFormat2() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        validator.validate(context, stub, "123.0E12");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationInvalidFormat3() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        // different locale format
        validator.validate(context, stub, "1.234,56");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationInvalidFormat4() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, true, null, null);
        validator.validate(context, stub, "123.00.0");
    }

    @Test
    public void testValidateDurationLowerLimit() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, L5, null);
        validator.validate(context, stub, "7");
    }

    @Test
    public void testValidateDurationLowerLimitEqualsValue() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, L5, null);
        validator.validate(context, stub, "5");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationLowerLimitBelow() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, L5, null);
        validator.validate(context, stub, "2");
    }

    @Test
    public void testValidateDurationUpperLimit() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null, L5);
        validator.validate(context, stub, "3");
    }

    @Test
    public void testValidateDurationUpperLimitEqualsValue() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null, L5);
        validator.validate(context, stub, "5");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationUpperLimitAbove() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null, L5);
        validator.validate(context, stub, "7");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDurationUpperLimitAbove2() {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, L0, null);
        validator.validate(context, stub,
                String.valueOf(DurationValidation.DURATION_MAX_DAYS_VALUE + 1));
    }

    @Test
    public void testGetParsedDuration_IntegerInput() throws Exception {
        Number result = DurationValidation.getParsedDuration(context, "1");
        assertEquals(1, result.longValue());
    }

    @Test
    public void testGetParsedDuration_FloatInput() throws Exception {
        Number result = DurationValidation.getParsedDuration(context, "1.1");
        assertEquals(1, result.longValue());
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_DurationFloatInput() throws Exception {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "1.1");
    }

    @Test(expected = ValidatorException.class)
    public void testValidate_DurationFloatInputGerman() throws Exception {
        context = new FacesContextStub(Locale.GERMAN);
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "1,1");
    }

    @Test
    public void testValidate_DurationLongInputWithSeparatorEnglish()
            throws Exception {
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "1,1");
    }

    @Test
    public void testValidate_DurationLongInputWithSeparatorGerman()
            throws Exception {
        context = new FacesContextStub(Locale.GERMAN);
        UIComponentStub stub = getComponent(DATATYPE_DURATION, false, null,
                null);
        validator.validate(context, stub, "1.1");
    }
}
