/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 05.11.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.validator.ValidatorException;

import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.validator.ParameterValueValidator;

/**
 * @author Mike J&auml;ger
 * 
 */
public class DurationConverter implements Converter {

    /**
     * Conversion to server representation, so converting days to milliseconds.
     * Prior to the conversion the input value is validated.
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
            String value) {
        try {
            // Checks if mandatory and not empty
            new ParameterValueValidator().validate(context, component, value);
        } catch (ValidatorException e) {
            throw new ConverterException(e.getFacesMessage());
        }

        // Validation passed; so if the value is empty it's not mandatory
        if (value == null || value.trim().length() == 0) {
            return null;
        } else {
            Long durationInMs = DurationValidation.convertDurationToMs(context,
                    value);
            if (durationInMs != null) {
                return durationInMs.toString();
            } else {
                throw new ConverterException(
                        ParameterValueValidator.getFacesMessage(component,
                                context));
            }
        }
    }

    /**
     * Conversion to portal representation as String.
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component,
            Object object) {
        if (object == null) {
            return null;
        }
        String value = object.toString();
        if (value.trim().length() == 0) {
            return null;
        }
        long durationInMs;
        try {
            durationInMs = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ConverterException(
                    ParameterValueValidator.getFacesMessage(component, context));
        }
        long days = durationInMs / DurationValidation.MILLISECONDS_PER_DAY;
        return String.valueOf(days);
    }
}
