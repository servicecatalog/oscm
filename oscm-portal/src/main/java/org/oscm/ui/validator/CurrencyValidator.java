/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: weiser                                                      
 *                                                                              
 *  Creation Date: 13.12.2011                                                      
 *                                                                              
 *  Completion Time: 13.12.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import java.util.Currency;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * JSF currency ISO code validator.
 * 
 */
public class CurrencyValidator implements Validator {

    /**
     * Validates that the given value contains an currency ISO code.
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
        String currencyCode = value.toString();
        if (currencyCode.length() == 0) {
            return;
        }
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            String label = JSFUtils.getLabel(component);
            ValidationException ve = new ValidationException(
                    ReasonEnum.INVALID_CURRENCY, label,
                    new Object[] { currencyCode });
            String text = JSFUtils.getText(ve.getMessageKey(),
                    new Object[] { currencyCode }, facesContext);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, text, null));
        }
    }
}
