/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 21.01.15 16:47
 *
 * ******************************************************************************
 */

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;

/**
 * Created by ChojnackiD on 2015-01-21.
 */
@Named
public class BooleanValidator {


    /**
     * Add a validation error to the context if the accept license flag is not
     * set.
     *
     * @param context
     *            the context
     * @param component
     *            check box which is validated
     * @param value
     *            the value to validate
     * @param condition
     */
    private void validateBoolean(final FacesContext context,
                                final UIComponent component, final Object value, Boolean condition) {
        if (!value.equals(condition)) {
            String requiredMessage = ((UIInput) component).getRequiredMessage();

            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, requiredMessage, requiredMessage));
        }
    }

    public void validateTrue(final FacesContext context,
                             final UIComponent component, final Object value){
        validateBoolean(context, component, value, Boolean.TRUE);
    }

    public void validateFalse(final FacesContext context,
                              final UIComponent component, final Object value) {
        validateBoolean(context, component, value, Boolean.FALSE);
    }
}
