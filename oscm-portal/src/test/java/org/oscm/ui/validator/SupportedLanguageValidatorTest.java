/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-12-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIComponentStub;

/**
 * @author Mao
 * 
 */
public class SupportedLanguageValidatorTest {
    private static final String APPLICATION_BEAN = "appBean";
    private FacesContextStub context;
    private UIComponentStub component;
    private SupportedLanguageValidator validator;
    private List<String> supportedLanguages;
    private ApplicationBean appBean;

    @Before
    public void setup() {
        validator = spy(new SupportedLanguageValidator());
        validator.ui = mock(UiDelegate.class);
        appBean = mock(ApplicationBean.class);
        supportedLanguages = new ArrayList<String>();
        supportedLanguages.add("en");
        when(validator.ui.findBean(eq(APPLICATION_BEAN))).thenReturn(appBean);
        when(validator.ui.getViewLocale()).thenReturn(new Locale("en"));
        when(appBean.getActiveLocales()).thenReturn(supportedLanguages);
        context = new FacesContextStub(Locale.ENGLISH);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.UI_COMPONENT_ATTRIBUTE_LABEL, "componentStub");
        component = new UIComponentStub(map);
    }

    @Test
    public void validate() throws ValidatorException {
        supportedLanguages.add("de");
        String selectedLanguage = "de";
        validator.validate(context, component, selectedLanguage);
    }

    @Test
    public void tvalidate_Null() throws ValidatorException {
        validator.validate(context, component, null);
    }

    @Test
    public void validate_Empty() throws ValidatorException {
        validator.validate(context, component, "");
    }

    @Test(expected = ValidatorException.class)
    public void validate_localeNotSupported() {
        supportedLanguages.add("de");
        String selectedLanguage = "zh";
        validator.validate(context, component, selectedLanguage);
    }
}
