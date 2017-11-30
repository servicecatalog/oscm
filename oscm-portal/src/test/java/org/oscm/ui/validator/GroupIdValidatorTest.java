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

public class GroupIdValidatorTest {

    private FacesContextStub context;
    private UIComponentStub component;
    private GroupIdValidator validator;

    @Before
    public void setup() {
        validator = new GroupIdValidator();
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    @Test
    public void validate() throws ValidatorException {
        String a256 = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
                + "01234567890123456789012345678901234567890123456789012345";
        validator.validate(context, component, a256);
    }

    @Test(expected = ValidatorException.class)
    public void validateTooLong() throws ValidatorException {
        String a257 = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
                + "012345678901234567890123456789012345678901234567890123456";
        validator.validate(context, component, a257);
    }
}
