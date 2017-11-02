/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pravi                                           
 *                                                                              
 *  Creation Date: Jan 8, 2010                                                      
 *                                                                              
 *  Completion Time: Dec 12, 2011                                   
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.validator.GenericValidator;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.common.JSFUtils;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.exception.ValidationException;

/**
 * It validates the user input, while defining the market service from the
 * technical service definition. Checks if the input is between the min and the
 * max value.
 * 
 * @author PRavi
 * 
 */
public class ParameterValueValidator implements Validator {

    private static final String DATA_TYPE = "dataType";
    private static final String REQUIRED = "required";
    private static final String DATATYPE_STRING = "string";
    private static final String DATATYPE_DURATION = "duration";
    private static final String DATATYPE_LONG = "long";
    private static final String DATATYPE_INTEGER = "integer";

    @Override
    public void validate(FacesContext context, UIComponent uiComponent,
            Object input) throws ValidatorException {
        String datatype = getDataType(uiComponent);
        boolean mandatory = isRequired(uiComponent);
        String value = null;
        if (input != null) {
            value = input.toString();
        }
        if (value == null || value.length() == 0) {
            if (mandatory) {
                ValidatorException e = getException(JSFUtils.getText(
                        BaseBean.ERROR_PARAMETER_VALUE_MANDATORY,
                        new String[] { JSFUtils.getLabel(uiComponent) },
                        context));
                throw e;
            } else {
                return;
            }
        }
        if (value.length() > ADMValidator.LENGTH_DESCRIPTION) {
            ValidatorException e = getException(JSFUtils.getText(
                    BaseBean.ERROR_PARAMETER_VALUE_TO_LONG,
                    new String[] { String
                            .valueOf(ADMValidator.LENGTH_DESCRIPTION) },
                    context));
            throw e;
        }
        boolean result = true;
        if (datatype.equals(DATATYPE_INTEGER)) {
            validateInteger(context, uiComponent, value);
            int parseInt = Integer.parseInt(value);
            result = LongValidator.isInRange(uiComponent, parseInt);
        } else if (datatype.equals(DATATYPE_LONG)) {
            long parseLong = LongValidator.parse(context, uiComponent, value);
            result = LongValidator.isInRange(uiComponent, parseLong);
        } else if (datatype.equals(DATATYPE_DURATION)) {
            long val = getDurationInMs(context, uiComponent, value);
            Long min = LongValidator.getMinValue(uiComponent);
            if (min != null) {
                min = Long.valueOf(getDurationInMs(context, uiComponent,
                        String.valueOf(min.longValue())));
            }
            Long max = LongValidator.getMaxValue(uiComponent);
            if (max != null) {
                max = Long.valueOf(getDurationInMs(context, uiComponent,
                        String.valueOf(max.longValue())));
            }
            result = LongValidator.isInRange(val, min, max);
        }
        if (!result) {
            ValidatorException e = getException(JSFUtils.getText(
                    BaseBean.ERROR_PARAMETER_VALUE_OUT_OF_RANGE, new String[] {
                            JSFUtils.getLabel(uiComponent), value }, context));
            throw e;
        }
    }

    /**
     * Validate integer value.
     * 
     * @param context
     *            JSF context.
     * @param uiComponent
     *            UI component.
     * @param value
     *            Value for validation.
     */
    private void validateInteger(FacesContext context, UIComponent uiComponent,
            String value) {
        if (!GenericValidator.isInt(value.toString())) {
            Object[] args = null;
            String label = JSFUtils.getLabel(uiComponent);
            if (label != null) {
                args = new Object[] { label };
            }
            ValidationException e = new ValidationException(
                    ValidationException.ReasonEnum.INTEGER, label, null);
            String text = JSFUtils.getText(e.getMessageKey(), args, context);
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, text, null));
        }
    }

    /**
     * Wraps the given message in a <code>FacesMessage</code> and throws it as
     * <code>ValidatorException</code>.
     * 
     * @param message
     *            the message string
     * @throws ValidatorException
     *             the <code>ValidatorException</code> containing the provided
     *             message
     */
    private ValidatorException getException(String message)
            throws ValidatorException {
        FacesMessage facesMessage = new FacesMessage(
                FacesMessage.SEVERITY_ERROR, message, null);
        return new ValidatorException(facesMessage);
    }

    /**
     * Reads the 'dataType' attribute from the given component. If not set, data
     * type 'string' is returned.
     * 
     * @param uiComponent
     *            the component to get the 'dataType' attribute from
     * @return the data type string
     */
    private String getDataType(UIComponent uiComponent) {
        Object object = uiComponent.getAttributes().get(DATA_TYPE);
        if (object == null) {
            return DATATYPE_STRING;
        } else {
            return object.toString();
        }
    }

    /**
     * Reads the 'required' attribute from the given component. Returns
     * <code>false</code> if the attribute is not set.
     * 
     * @param uiComponent
     *            the component to get the 'required' attribute from
     * @return <code>true</code> if mandatory else <code>false</code>
     */
    private boolean isRequired(UIComponent uiComponent) {
        Object object = uiComponent.getAttributes().get(REQUIRED);
        if (object == null) {
            return false;
        } else {
            return ((Boolean) object).booleanValue();
        }
    }

    private long getDurationInMs(FacesContext context, UIComponent component,
            String value) {
        Long duration = DurationValidation.getDurationInMs(context, value);
        if (duration == null) {
            throw new ValidatorException(getFacesMessage(component, context));
        } else {
            return duration.longValue();
        }
    }

    /**
     * Generates a default faces message.
     * 
     * @param component
     *            The current component.
     * @return The faces message.
     */
    public static FacesMessage getFacesMessage(UIComponent component,
            FacesContext context) {
        String label = JSFUtils.getLabel(component);
        Long maxVal = LongValidator.getMaxValue(component);
        if (maxVal == null) {
            maxVal = DurationValidation.getDurationMaxDaysValue();
        }
        Object[] args = new Object[] { maxVal.toString() };

        ValidationException e = new ValidationException(
                ValidationException.ReasonEnum.DURATION, label, null);
        String text = JSFUtils.getText(e.getMessageKey(), args, context);
        return new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null);
    }
}
