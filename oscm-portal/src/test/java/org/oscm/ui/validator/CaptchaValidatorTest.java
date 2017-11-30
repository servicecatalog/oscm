/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Locale;

import javax.faces.component.html.HtmlInputText;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.Constants;
import org.oscm.ui.stubs.ExternalContextStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpSessionStub;

/**
 * @author Qiu
 * 
 */
@SuppressWarnings("boxing")
public class CaptchaValidatorTest {

    private CaptchaValidator validator;
    private FacesContextStub context;
    private HttpSessionStub sessionMock;
    private HtmlInputText inputField;

    @Before
    public void setup() {
        validator = new CaptchaValidator();
        sessionMock = new HttpSessionStub(Locale.ENGLISH);
        context = new FacesContextStub(Locale.ENGLISH) {
            @Override
            public ExternalContext getExternalContext() {
                ExternalContext exContext = spy(new ExternalContextStub(
                        Locale.ENGLISH));
                doReturn(sessionMock).when(exContext).getSession(false);
                return exContext;
            }
        };

        inputField = new HtmlInputText() {
            @Override
            public String getClientId(FacesContext ctx) {
                return "";
            }
        };
    }

    @Test
    public void validateCaptcha_correctInput() {
        // given
        setCaptcha("0815");
        inputField.setValue("0815");
        // when
        validator.validate(context, inputField, inputField.getValue());
        // then
        assertEquals(true, getCaptchaInputStatus());
        assertEquals(false, isInputCleared());
    }

    @Test(expected = ValidatorException.class)
    public void validateCaptcha_wrongInput() {
        // given
        setCaptcha("0815");
        inputField.setValue("0817");
        // when
        try {
            validator.validate(context, inputField, inputField.getValue());
        } finally {
            // then
            assertEquals(false, getCaptchaInputStatus());
            assertEquals(true, isInputCleared());
        }
    }

    @Test(expected = ValidatorException.class)
    public void validateCaptcha_emptyInput() {
        // given
        setCaptcha("0815");
        inputField.setValue("");
        // when
        try {
            validator.validate(context, inputField, inputField.getValue());
        } finally {
            // then
            assertEquals(false, getCaptchaInputStatus());
            assertEquals(true, isInputCleared());
        }
    }

    @Test
    public void validateCaptcha_doubleCheck() {
        // given
        setCaptcha("0815");
        // when
        try {
            inputField.setValue("asdf");
            validator.validate(context, inputField, inputField.getValue());
        } catch (ValidatorException e) {
            // then
            assertEquals(false, getCaptchaInputStatus());
            assertEquals(true, isInputCleared());
            // when
            inputField.setValue("0815");
            validator.validate(context, inputField, inputField.getValue());
            // then
            assertEquals(true, getCaptchaInputStatus());
            assertEquals(false, isInputCleared());
        }
    }

    /**
     * set value of captcha
     * 
     * @param captcha
     */
    private void setCaptcha(String captcha) {
        sessionMock.setAttribute(Constants.CAPTCHA_KEY, captcha);
    }

    /**
     * get attribute value in session
     * 
     * @return
     */
    private boolean getCaptchaInputStatus() {
        return ((Boolean) sessionMock
                .getAttribute(Constants.CAPTCHA_INPUT_STATUS)).booleanValue();
    }

    /**
     * verify the input has been cleared
     * 
     * @return
     */
    private boolean isInputCleared() {
        return inputField.getValue().equals("");
    }

}
