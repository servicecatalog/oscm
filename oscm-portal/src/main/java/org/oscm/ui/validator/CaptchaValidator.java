/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;

/**
 * JSF validator for Captcha
 * 
 * @author Qiu
 * 
 */
public class CaptchaValidator implements Validator {

    /**
     * Validates if the user input and captchaInputField contain the same value
     * 
     * @param context
     *            FacesContext for the request we are processing
     * @param component
     *            UIComponent we are checking for correctness
     * @param value
     *            the value to validate
     * @throws ValidatorException
     *             if validation fails
     */
    @Override
    public void validate(final FacesContext context,
            final UIComponent captchaInputField, final Object value)
            throws ValidatorException {
        String userInput = (String) value;
        String captchaKey = (String) JSFUtils
                .getSessionAttribute(Constants.CAPTCHA_KEY);
        if (userInput != null && userInput.equals(captchaKey)) {
            JSFUtils.setSessionAttribute(Constants.CAPTCHA_INPUT_STATUS,
                    Boolean.TRUE);
        } else {
            HtmlInputText htmlInputText = (HtmlInputText) captchaInputField;
            htmlInputText.setValid(false);
            JSFUtils.setSessionAttribute(Constants.CAPTCHA_INPUT_STATUS,
                    Boolean.FALSE);
            htmlInputText.setValue("");
            String text = JSFUtils.getText(BaseBean.ERROR_CAPTCHA,
                    (Object[]) null);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, text, null));
        }
    }

}
