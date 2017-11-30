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

public class UrlValidatorTest {

    private FacesContextStub context;
    private UIComponentStub component;
    private UrlValidator validator;

    @Before
    public void setup() {
        validator = new UrlValidator();
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    @Test
    public void testValidate1() throws ValidatorException {
        validator.validate(context, component, "http://www.gmx.de");
    }

    @Test
    public void testValidate2() throws ValidatorException {
        validator.validate(context, component, "ftp://123.45.67.80:1234");
    }

    @Test
    public void testValidate3() throws ValidatorException {
        validator.validate(context, component, null);
    }

    @Test
    public void testValidate4() throws ValidatorException {
        // see [5157]
        validator.validate(context, component,
                "http://estbesrh4:8090/smartdocs/crm.html");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateMissingProtocol1() throws ValidatorException {
        validator.validate(context, component, "www.gmx.de");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateMissingProtocol2() throws ValidatorException {
        validator.validate(context, component, "gmx.de");
    }

    @Test
    public void testValidateEmpty() throws ValidatorException {
        validator.validate(context, component, "");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateWhitespaces() throws ValidatorException {
        validator.validate(context, component, "   ");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateObject() throws ValidatorException {
        validator.validate(context, component, new Long(34));
    }

    @Test(expected = ValidatorException.class)
    public void testValidateMissingPort() throws ValidatorException {
        validator.validate(context, component, "https://localhost:");
    }

    @Test(expected = ValidatorException.class)
    public void testValidateUnknownProtocol() throws ValidatorException {
        validator.validate(context, component, "xyz://www.gmx.de");
    }
    
    @Test
    public void validate_MSUrl() throws ValidatorException {
        validator.validate(context, component,
                "http://msdn.microsoft.com/en-us/library/5471dc8s(VS.80).aspx");
    }
    
}
