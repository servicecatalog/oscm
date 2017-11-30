/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.validator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;

public class IdValidatorTest {

    private FacesContextStub context;
    private UIComponentStub component;
    private IdValidator validator;
    private static final int TEST_LENGTH = 40;

    @Before
    public void setup() {

        validator = new IdValidator() {
            @Override
            protected int getMaxLength() {
                return TEST_LENGTH;
            }
        };

        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    @Test
    public void testValidateEmpty() throws ValidatorException {
        validator.validate(context, component, "");
    }

    @Test
    public void testValidateWhitespaces() throws ValidatorException {
        validator.validate(context, component, "   ");
    }

    @Test
    public void testValidateNull() throws ValidatorException {
        validator.validate(context, component, null);
    }

    @Test
    public void testValidate() throws ValidatorException {
        validator.validate(context, component, "some_valid_id");
    }

    @Test
    public void testValidateEmptyBetween() throws ValidatorException {
        validator.validate(context, component, "1 2");
    }

    @Test
    public void testValidateLimits1() throws ValidatorException {
        String string = new String(new int[] { 0xE000 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits2() throws ValidatorException {
        String string = new String(new int[] { 0xFFFD }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits3() throws ValidatorException {
        String string = new String(new int[] { 0x10000 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits4() throws ValidatorException {
        String string = new String(new int[] { 0x10FFFF }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits5() throws ValidatorException {
        String string = new String(new int[] { 0x28 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits6() throws ValidatorException {
        String string = new String(new int[] { 0x29 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits7() throws ValidatorException {
        String string = new String(new int[] { 0x2D }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits8() throws ValidatorException {
        String string = new String(new int[] { 0x2E }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits9() throws ValidatorException {
        String string = new String(new int[] { 0x29 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits10() throws ValidatorException {
        String string = new String(new int[] { 0x30 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits11() throws ValidatorException {
        String string = new String(new int[] { 0x39 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits12() throws ValidatorException {
        String string = new String(new int[] { 0x40 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits13() throws ValidatorException {
        String string = new String(new int[] { 0x5B }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits14() throws ValidatorException {
        String string = new String(new int[] { 0x5D }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits15() throws ValidatorException {
        String string = new String(new int[] { 0x5F }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits16() throws ValidatorException {
        String string = new String(new int[] { 0x61 }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test
    public void testValidateLimits17() throws ValidatorException {
        String string = new String(new int[] { 0xD7FF }, 0, 1);
        validator.validate(context, component, string);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateTooLong() throws ValidatorException {
        validator.validate(context, component,
                "1234567890123456789012345678901234567890_1");
    }
}
