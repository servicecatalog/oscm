/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 19.08.15 11:04
 *
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

public class BillingAdapterValidatorTest {

    private FacesContextStub context;
    private UIComponentStub component;
    private BillingAdapterNameValidator validator;

    @Before
    public void setup() {
        validator = new BillingAdapterNameValidator();
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    @Test
    public void testValidate() throws ValidatorException {
        String a40 = "0123456789012345678901234567890123456789";
        validator.validate(context, component, a40);
    }

    @Test(expected = ValidatorException.class)
    public void testValidateTooLong() throws ValidatorException {
        String a41 = "A01234567890123456789012345678901234567890123456789012345678901234567890123456789" +
                "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567" +
                "89012345678901234567890123456789";
        validator.validate(context, component, a41);
    }
}
