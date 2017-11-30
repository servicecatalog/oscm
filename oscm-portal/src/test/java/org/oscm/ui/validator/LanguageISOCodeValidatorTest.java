/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.11.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.stubs.ApplicationStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.ResourceBundleStub;
import org.oscm.ui.stubs.UIComponentStub;

/**
 * Unit test for LanguageISOCodeValidator
 * 
 * @author cmin
 * 
 */
public class LanguageISOCodeValidatorTest {

    private LanguageISOCodeValidator validator;
    private FacesContextStub context;
    private UIComponentStub component;
    private ResourceBundleStub resource;
    private ApplicationStub application;

    @Before
    public void setUp() {
        validator = new LanguageISOCodeValidator();
        resource = new ResourceBundleStub();
        resource.addResource(BaseBean.ERROR_ISOCODE_NOTSUPPORTED,
                BaseBean.ERROR_ISOCODE_NOTSUPPORTED);
        resource.addResource(BaseBean.ERROR_ISOCODE_INVALID,
                BaseBean.ERROR_ISOCODE_INVALID);
        context = new FacesContextStub(Locale.ENGLISH);
        application = (ApplicationStub) context.getApplication();
        application.setResourceBundleStub(resource);
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(Locale.ENGLISH);
        locales.add(Locale.GERMAN);
        locales.add(Locale.JAPANESE);
        locales.add(new Locale("in"));
        application.setSupportedLocales(locales);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    @Test
    public void validate_Empty() throws ValidatorException {
        validator.validate(context, component, "");
    }

    @Test(expected = ValidatorException.class)
    public void validate_Whitespaces() throws ValidatorException {
        validator.validate(context, component, "  ");
    }

    @Test
    public void validate_InvalidISOCode() throws ValidatorException {
        try {
            validator.validate(context, component, "tta");
            fail("ValidationException expected");
        } catch (ValidatorException e) {
            assertEquals(BaseBean.ERROR_ISOCODE_INVALID, e.getFacesMessage()
                    .getSummary());
        }
    }

    @Test
    public void validate_ISOCodeNotSuppport() throws ValidatorException {
        try {
            validator.validate(context, component, "aa");
            fail("ValidationException expected");
        } catch (ValidatorException e) {
            assertEquals(BaseBean.ERROR_ISOCODE_NOTSUPPORTED, e
                    .getFacesMessage().getSummary());
        }
    }

    @Test
    public void validate_Null() throws ValidatorException {
        validator.validate(context, component, null);
    }

    @Test
    public void validate() throws ValidatorException {
        validator.validate(context, component, "en");
    }

}
