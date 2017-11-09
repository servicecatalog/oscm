/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cmin                                                      
 *                                                                              
 *  Creation Date: 13.11.2013                                                      
 *                                                                              
 *  Completion Time: 13.11.2013                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.LocaleUtils;

/**
 * JSF Language ISO code validator.
 * @author cmin
 */
public class LanguageISOCodeValidator implements Validator {

    /**
     * Validates that the given value contains an language ISO code.
     * 
     * @param facesContext
     *            FacesContext for the request we are processing
     * @param component
     *            UIComponent we are checking for correctness
     * @param value
     *            the value to validate
     * @throws ValidatorException
     *             if validation fails
     */
    @Override
    public void validate(FacesContext facesContext, UIComponent component,
            Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        String languageISOCode = value.toString();
        if (languageISOCode.length() == 0) {
            return;
        }
        checkLocale(languageISOCode, facesContext);
    }

    private void checkLocale(String languageISOCode, FacesContext facesContext) {

        if (LocaleUtils.isLocaleSupported(languageISOCode)) {
            return;
        }
        if (LocaleUtils.isLocaleValid(languageISOCode)) {
            addMessage(languageISOCode, facesContext,
                    BaseBean.ERROR_ISOCODE_NOTSUPPORTED);
        }
        addMessage(languageISOCode, facesContext,
                BaseBean.ERROR_ISOCODE_INVALID);

    }

    private void addMessage(String languageISOCode, FacesContext facesContext,
            String key) {
        String text = JSFUtils.getText(key, new String[] { languageISOCode },
                facesContext);
        throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR, text, null));
    }

}
