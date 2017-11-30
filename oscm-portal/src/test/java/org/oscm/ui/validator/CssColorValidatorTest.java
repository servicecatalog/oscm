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

public class CssColorValidatorTest {

    private static final String COLORS[] = { "black", "gray", "maroon", "red",
            "green", "lime", "olive", "yellow", "navy", "blue", "purple",
            "fuchsia", "teal", "aqua", "silver", "white" };

    private FacesContextStub context;
    private UIComponentStub component;
    private CssColorValidator validator;

    @Before
    public void setup() {
        validator = new CssColorValidator();
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    @Test
    public void testValidateNull() {
        validator.validate(context, component, null);
    }

    @Test
    public void testValidateEmpty() {
        validator.validate(context, component, "");
    }

    @Test
    public void testValidateHex000() {
        validator.validate(context, component, "#000");
    }

    @Test
    public void testValidateHexfff() {
        validator.validate(context, component, "#fff");
    }

    @Test
    public void testValidateHexFFF() {
        validator.validate(context, component, "#FFF");
    }

    @Test
    public void testValidateHex000000() {
        validator.validate(context, component, "#000000");
    }

    @Test
    public void testValidateHexffffff() {
        validator.validate(context, component, "#ffffff");
    }

    @Test
    public void testValidateHexFFFFFF() {
        validator.validate(context, component, "#FFFFFF");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateHexFF() {
        validator.validate(context, component, "#FF");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateHexFFFFFF0() {
        validator.validate(context, component, "#FFFFFF0");
    }

    @Test
    public void testValidateRGB1() {
        validator.validate(context, component, "rgb(0,0,0)");
    }

    @Test
    public void testValidateRGB2() {
        validator.validate(context, component, "Rgb(255, 0, 0)");
    }

    @Test
    public void testValidateRGB3() {
        validator.validate(context, component, "rGb (0,255,0)");
    }

    @Test
    public void testValidateRGB4() {
        validator.validate(context, component, "rgB( 0,0,255 )");
    }

    @Test
    public void testValidateRGB5() {
        validator.validate(context, component,
                " R G B (2 5 5 , 2 5 5 , 2 5 5 ) ");
    }

    @Test
    public void testValidateRGBPercent1() {
        validator.validate(context, component, "rgb(0%,0%,0%)");
    }

    @Test
    public void testValidateRGBPercent2() {
        validator.validate(context, component, "Rgb(100%, 0%, 0%)");
    }

    @Test
    public void testValidateRGBPercent3() {
        validator.validate(context, component, "rGb (0%,100%,0%)");
    }

    @Test
    public void testValidateRGBPercent4() {
        validator.validate(context, component, "rgB( 0%,0%,100% )");
    }

    @Test
    public void testValidateRGBPercent5() {
        validator.validate(context, component,
                " R G B (1 0 0 % , 1 0 0 % , 1 0 0 % ) ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateRGBInvalid1() {
        validator.validate(context, component, "rgb(-20,0,0");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateRGBInvalid2() {
        validator.validate(context, component, "rgb(123,234,345)");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateRGBInvalid3() {
        validator.validate(context, component, "rgb(abvc,def,hui)");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateRGBPercentInvalid() {
        validator.validate(context, component, "rgb(50%,100%,150%");
    }

    @Test
    public void testValidateColorNames() {
        for (String color : COLORS) {
            validator.validate(context, component, color);
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWhitespaces() {
        validator.validate(context, component, "   ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateInvalid() {
        validator.validate(context, component, "iu�olzikjhgf");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateInvalidColor() {
        validator.validate(context, component, "pink");
    }
}
