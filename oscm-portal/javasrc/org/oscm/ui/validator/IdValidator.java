/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 15.04.2011                                                      
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
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.oscm.validator.ADMValidator.INVALID_ID_CHARS;

/**
 * Abstract base class for all id validators. The general validation is similar
 * for all ID fields. The specific attributes of the validation are defined by
 * the subclasses (e.g. the maximum allowd length).
 *
 * @author Florian Walker
 *
 */
public abstract class IdValidator implements Validator {

    /**
     * Abstract method which must be overwritten by the subclasses to indicate
     * the maximum length of the individual ID char field.
     *
     * @return the maximum length of the ID.
     */
    protected abstract int getMaxLength();

    /**
     * Validates that the given value contains only valid characters @see
     * org.oscm.validator#containsOnlyValidIdChars(String).
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
    public void validate(FacesContext facesContext, UIComponent component,
            Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        String id = value.toString();
        if (id.length() == 0) {
            return;
        }
        if (id.length() > getMaxLength()) {
            String label = JSFUtils.getLabel(component);
            Object[] args = new Object[] { label,
                    String.valueOf(getMaxLength()) };
            ValidationException e = new ValidationException(ReasonEnum.LENGTH,
                    label, null);
            String text = JSFUtils.getText(e.getMessageKey(), args,
                    facesContext);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, text, null));
        }
        if (!ADMValidator.containsOnlyValidIdChars(id)) {
            String label = JSFUtils.getLabel(component);
            Set<String> badCharsSet = new HashSet<>();
            for (int i=0; i<id.length(); i++){
                String testedChar = String.valueOf(id.charAt(i));
                if (!ADMValidator.containsOnlyValidIdChars(testedChar)) {
                    badCharsSet.add(testedChar);
                }
            }
            String badCharsString = "";
            Iterator<String> it = badCharsSet.iterator();
            while(it.hasNext()){
                badCharsString = badCharsString + it.next();
            }
            Object[] args = new Object[] { badCharsString };
            ValidationException e = new ValidationException(ReasonEnum.ID_CHAR,
                    label, null);
            String text = JSFUtils.getText(e.getMessageKey(), args,
                    facesContext);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, text, null));
        }
    }

}
