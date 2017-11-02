/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: barzu                                        
 *                                                                              
 *  Creation Date: Dec 9, 2011                                                      
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
import org.oscm.ui.common.JSFUtils;
import org.oscm.internal.types.exception.ValidationException;

/**
 * JSF validator for long integers.
 * 
 * @author barzu
 */
public class LongValidator implements Validator {

    private static final String MIN_VALUE = "minValue";
    private static final String MAX_VALUE = "maxValue";

    /**
     * Validates that the given value contains a long integer.
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
    public void validate(FacesContext context, UIComponent uiComponent,
            Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        String stringValue = value.toString();
        if (stringValue.length() == 0) {
            return;
        }
        validate(context, uiComponent, stringValue);
    }

    private static void validate(FacesContext context, UIComponent uiComponent,
            String value) throws ValidatorException {
        Long minValue = getMinValue(uiComponent);
        Long maxValue = getMaxValue(uiComponent);
        long parsedLong = parse(context, value, minValue, maxValue);
        if (!isInRange(parsedLong, minValue, maxValue)) {
            minValue = (minValue != null ? minValue : Long
                    .valueOf(Long.MIN_VALUE));
            maxValue = (maxValue != null ? maxValue : Long
                    .valueOf(Long.MAX_VALUE));
            String message = JSFUtils.getText(
                    BaseBean.ERROR_LONG_VALUE_OUT_OF_RANGE,
                    new String[] { String.valueOf(minValue),
                            String.valueOf(maxValue) }, context);
            throw getException(message);
        }
    }

    /**
     * Parses the specified string into a long integer.
     * 
     * @param context
     *            FacesContext for the request we are processing
     * @param component
     *            UIComponent we are checking for correctness
     * @param value
     *            the value to parse
     * @throws ValidatorException
     *             if the specified string could not be parsed into a valid long
     *             integer.
     */
    public static long parse(FacesContext context, UIComponent uiComponent,
            String value) throws ValidatorException {
        if (!GenericValidator.isLong(value)) {
            Object[] args = null;
            String label = JSFUtils.getLabel(uiComponent);
            if (label != null) {
                args = new Object[] { label };
            }
            ValidationException e = new ValidationException(
                    ValidationException.ReasonEnum.LONG, label, null);
            String message = JSFUtils.getText(e.getMessageKey(), args, context);
            throw getException(message);
        }
        return Long.parseLong(value);
    }

    /**
     * Parses the specified string into a long integer between the specified
     * minimum and maximum.
     * 
     * @param context
     *            FacesContext for the request we are processing
     * @param minValue
     *            the minimum allowed value
     * @param maxValue
     *            the maximum allowed value
     * @param value
     *            the value to parse
     * @throws ValidatorException
     *             if the specified string could not be parsed into a valid long
     *             integer or if it exceeds the specified minimum and maximum.
     */
    private static long parse(FacesContext context, String value,
            Long minValue, Long maxValue) throws ValidatorException {
        if (!GenericValidator.isLong(value)) {
            minValue = (minValue != null ? minValue : Long
                    .valueOf(Long.MIN_VALUE));
            maxValue = (maxValue != null ? maxValue : Long
                    .valueOf(Long.MAX_VALUE));
            String message = JSFUtils.getText(
                    BaseBean.ERROR_LONG_VALUE_OUT_OF_RANGE,
                    new String[] { String.valueOf(minValue),
                            String.valueOf(maxValue) }, context);
            throw getException(message);
        }
        return Long.parseLong(value);
    }

    /**
     * Checks if the given value is in the range defined on the given component
     * 
     * @param component
     *            the component to get minimum and maximum values from
     * @param value
     *            the value to check
     * @return <code>true</code> if in the range else <code>false</code>
     */
    public static boolean isInRange(UIComponent component, long value) {
        Long minValue = getMinValue(component);
        Long maxValue = getMaxValue(component);
        return isInRange(value, minValue, maxValue);
    }

    /**
     * Checks if the given value is in the given range. Maximum and minimum are
     * only checked if they are not <code>null</code>.
     * 
     * @param value
     *            the value to check
     * @param minValue
     *            the minimum value or <code>null</code>
     * @param maxValue
     *            the maximum value or <code>null</code>
     * @return <code>true</code> if the range is not violated
     */
    public static boolean isInRange(long value, Long minValue, Long maxValue) {
        return !((minValue != null && value < minValue.longValue()) || (maxValue != null && value > maxValue
                .longValue()));
    }

    /**
     * Reads the 'minValue' attribute from the given component and returns it.
     * 
     * @param component
     *            the component to read the 'minValue' attribute from
     * @return a <code>Long</code> representing the minimum value or
     *         <code>null</code> if not set
     */
    public static Long getMinValue(UIComponent component) {
        Object object = component.getAttributes().get(MIN_VALUE);
        if (object == null) {
            return null;
        } else {
            return ((Long) object);
        }
    }

    /**
     * Reads the 'maxValue' attribute from the given component and returns it.
     * 
     * @param component
     *            the component to read the 'maxValue' attribute from
     * @return a <code>Long</code> representing the maximum value or
     *         <code>null</code> if not set
     */
    public static Long getMaxValue(UIComponent component) {
        Object object = component.getAttributes().get(MAX_VALUE);
        if (object == null) {
            return null;
        } else {
            return ((Long) object);
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
    private static ValidatorException getException(String message)
            throws ValidatorException {
        FacesMessage facesMessage = new FacesMessage(
                FacesMessage.SEVERITY_ERROR, message, null);
        return new ValidatorException(facesMessage);
    }

}
