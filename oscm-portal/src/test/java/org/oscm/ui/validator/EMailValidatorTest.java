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

public class EMailValidatorTest {

    private FacesContextStub context;
    private UIComponentStub component;
    private EmailValidator validator;

    @Before
    public void setup() {
        validator = new EmailValidator();
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    // Many test are missing see RFC 2822
    // and e.g. the display name is not supported with this validator!

    @Test
    public void testValidateNull() throws ValidatorException {
        validator.validate(context, component, null);
    }

    @Test
    public void testValidateEmpty() throws ValidatorException {
        validator.validate(context, component, "");
    }

    @Test
    public void testValidate() throws ValidatorException {
        validator.validate(context, component, "info@fujitsu.com");
    }

    @Test
    public void testValidateTwoDots() throws ValidatorException {
        validator.validate(context, component, "info@est.fujitsu.com");
    }

    @Test
    public void testValidateNameWithDots() throws ValidatorException {
        validator.validate(context, component, "hans.wurst@est.fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateMissingAt() throws ValidatorException {
        validator.validate(context, component, "info_fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateMissingDot() throws ValidatorException {
        validator.validate(context, component, "info@fujitsu_com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateTwoAts() throws ValidatorException {
        validator.validate(context, component, "info@est@fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWhitespaces() throws ValidatorException {
        validator.validate(context, component, "   ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateNotEscapedWhitespaceInside()
            throws ValidatorException {
        validator.validate(context, component, "in fo@fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWhitespaceLeading() throws ValidatorException {
        validator.validate(context, component, " hans.wurst@est.fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWhitespaceTrailing() throws ValidatorException {
        validator.validate(context, component, "hans.wurst@est.fujitsu.com ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWhitespaceLeadingAndTrailing()
            throws ValidatorException {
        validator.validate(context, component, " hans.wurst@est.fujitsu.com ");
    }

    @Test
    public void testValidateWhitespaceLocalPart() throws ValidatorException {
        validator.validate(context, component,
                "mr.\"hans\\ wurst\"@est.fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWhitespaceDomain() throws ValidatorException {
        validator.validate(context, component, "hans.wurst@est\\ fujitsu.com");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateDisplayName() throws ValidatorException {
        validator.validate(context, component,
                "Hans Wurst <hans.wurst@est.fujitsu.com>");
    }
}
