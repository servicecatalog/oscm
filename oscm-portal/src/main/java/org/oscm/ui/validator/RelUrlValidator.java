/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: brandstetter                                                      
 *                                                                              
 *  Creation Date: 20.10.2011                                                     
 *                                                                              
 *  Completion Time: 20.10.2011                                              
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
 * relative URL validator
 */
public class RelUrlValidator implements Validator {

    /**
     * Validates that the given value is a relative URL
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

        // a relative URL must start with a "/"
        if (str.startsWith("/")) {
            // create a absolute URL to check all the other URL rules
            str = "http://xy".concat(str);
            if (ADMValidator.isUrl(str)) {
                return;
            }
        }

        Object[] args = null;
        String label = JSFUtils.getLabel(component);
        if (label != null) {
            args = new Object[] { label };
        }
        ValidationException e = new ValidationException(
                ValidationException.ReasonEnum.RELATIVE_URL, label, null);
        String text = JSFUtils.getText(e.getMessageKey(), args, facesContext);
        throw new ValidatorException(new FacesMessage(
                FacesMessage.SEVERITY_ERROR, text, null));
    }
}
