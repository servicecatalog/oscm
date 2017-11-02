/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.04.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.common.JSFUtils;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.exception.ValidationException;

/**
 * URL validator
 */
public class UrlValidator implements Validator {

    /**
     * Validates that the given value is an URL
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
    public void validate(FacesContext facesContext, UIComponent component,
            Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        String str = value.toString();
        if (str.length() == 0) {
            return;
        }

        if (ADMValidator.isUrl(str)) {
            return;
        }
        Object[] args = null;
        String label = JSFUtils.getLabel(component);
        if (label != null) {
            args = new Object[] { label };
        }
        ValidationException e = new ValidationException(
                ValidationException.ReasonEnum.URL, label, null);
        String text = JSFUtils.getText(e.getMessageKey(), args, facesContext);
        throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR, text, null));
    }
}
