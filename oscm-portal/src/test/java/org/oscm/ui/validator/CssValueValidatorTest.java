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

public class CssValueValidatorTest {

    private static final String[] SOME_VALID_CSS = new String[] {
            "margin: 0px;", "padding: 0px;", "color: #333;",
            "font-family: Arial, sans-serif;", "font-size: 9pt;",
            "font-weight: normal;", "line-height: 160%;", };
    private FacesContextStub context;
    private UIComponentStub component;
    private CssValueValidator validator;

    @Before
    public void setup() {
        validator = new CssValueValidator();
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
    public void testValidateWhitespaces() {
        validator.validate(context, component, "   ");
    }

    @Test
    public void testValidate() {
        for (String css : SOME_VALID_CSS) {
            validator.validate(context, component, css);
        }
    }

    @Test(expected = ValidatorException.class)
    public void testValidateOpeningBracket() {
        validator.validate(context, component, "{");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateClosingBracket() {
        validator.validate(context, component, "}");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateComment() {
        validator.validate(context, component, "/*");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWithInlineCssAndComponentAsNull() {
        validator.validate(context, null, "P{font-size: 10pt}");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWithInlineCssAndComponent() {
        validator.validate(context, component, "P{font-size: 10pt}");
    }

}
