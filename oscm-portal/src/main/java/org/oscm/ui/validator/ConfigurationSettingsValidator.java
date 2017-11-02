/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: falkenhahn                                                      
 *                                                                              
 *  Creation Date: Nov 22, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 22, 2011                                              
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
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Configuration setting validator
 */
public class ConfigurationSettingsValidator implements Validator {

    private static final String DATA_TYPE = "dataType";
    private static final String REQUIRED = "required";

    /**
     * Validates that the given value corresponding to the configuration key
     * type.
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
            Object input) throws ValidatorException {
        String datatype = getDataType(uiComponent);
        boolean mandatory = isRequired(uiComponent);
        String value = null;
        if (input != null) {
            value = input.toString().trim();
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
        if (ConfigurationKey.TYPE_LONG.equals(datatype)) {
            validateLength(context, value, ADMValidator.LENGTH_DESCRIPTION);
            validateLong(context, uiComponent, value);
        } else if (ConfigurationKey.TYPE_URL.equals(datatype)) {
            validateLength(context, value, ADMValidator.LENGTH_DESCRIPTION);
            validateUrl(context, uiComponent, value);
        } else if (ConfigurationKey.TYPE_MAIL.equals(datatype)) {
            // the server also allows only 100 chars for email
            validateLength(context, value, ADMValidator.LENGTH_NAME);
            validateMail(context, uiComponent, value);
        } else if (ConfigurationKey.TYPE_BOOLEAN.equals(datatype)) {
            validateBoolean(context, uiComponent, value);
            // no need to validate length
        } else {
            validateLength(context, value, ADMValidator.LENGTH_DESCRIPTION);
        }
    }

    /**
     * Validate url value.
     * 
     * @param context
     *            JSF context.
     * @param uiComponent
     *            UI component.
     * @param value
     *            Value for validation.
     */
    private void validateUrl(FacesContext context, UIComponent uiComponent,
            String value) {
        UrlValidator toValidate = new UrlValidator();
        toValidate.validate(context, uiComponent, value);
    }

    /**
     * Validates a boolean value.
     * 
     * @param context
     *            the JSF context.
     * @param uiComponent
     *            the UI component bing validated.
     * @param value
     *            the value to be validated.
     */
    private void validateBoolean(FacesContext context, UIComponent uiComponent,
            String value) {
        if (!ADMValidator.isBoolean(value)) {
            Object[] args = null;
            String label = JSFUtils.getLabel(uiComponent);
            if (label != null) {
                args = new Object[] { label };
            }
            ValidationException e = new ValidationException(
                    ValidationException.ReasonEnum.BOOLEAN, label, null);
            String message = JSFUtils.getText(e.getMessageKey(), args, context);
            throw getException(message);
        }
    }

    /**
     * Validate mail value.
     * 
     * @param context
     *            JSF context.
     * @param uiComponent
     *            UI component.
     * @param value
     *            Value for validation.
     */
    private void validateMail(FacesContext context, UIComponent uiComponent,
            String value) {
        EmailValidator toValidate = new EmailValidator();
        toValidate.validate(context, uiComponent, value);
    }

    /**
     * Validate long value.
     * 
     * @param context
     *            JSF context.
     * @param uiComponent
     *            UI component.
     * @param value
     *            Value for validation.
     */
    private void validateLong(FacesContext context, UIComponent uiComponent,
            String value) {
        LongValidator toValidate = new LongValidator();
        toValidate.validate(context, uiComponent, value);
    }

    /**
     * Validates that the length of the specified <code>value</code> is not
     * greater than the specified <code>maxLength</code>.
     * 
     * @param context
     *            the JSF validation context
     * @param value
     *            the value to be validated
     * @param maxLength
     *            the maximum allowed length
     * @throws ValidatorException
     *             if the length of <code>value</code> is greater than
     *             <code>maxLength</code>
     */
    private void validateLength(FacesContext context, String value,
            int maxLength) {
        if (value.length() > maxLength) {
            ValidatorException e = getException(JSFUtils.getText(
                    BaseBean.ERROR_PARAMETER_VALUE_TO_LONG,
                    new String[] { String.valueOf(maxLength) }, context));
            throw e;
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
            return ConfigurationKey.TYPE_STRING;
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
     * @return <code>true</code> if required, else <code>false</code>
     */
    private boolean isRequired(UIComponent uiComponent) {
        Object object = uiComponent.getAttributes().get(REQUIRED);
        if (object == null) {
            return false;
        } else {
            return ((Boolean) object).booleanValue();
        }
    }

}
