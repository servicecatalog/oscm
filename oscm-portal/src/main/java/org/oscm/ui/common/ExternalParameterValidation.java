/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 23.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import javax.faces.context.FacesContext;

import org.apache.commons.validator.GenericValidator;

import org.oscm.ui.validator.LongValidator;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;

/**
 * Validates parameter values, that have been configured by an external tool.
 * 
 * @author Thomas Baumann
 */
public class ExternalParameterValidation {

    public static boolean parameterIsValid(VOParameterDefinition parDefinition,
            String parValue, FacesContext context) {
        if (parDefinition == null) {
            return false;
        }
        ParameterValueType parValueType = parDefinition.getValueType();
        if (parValueType == null) {
            return false;
        }

        if (parValue == null
                || parValue.length() > ADMValidator.LENGTH_DESCRIPTION) {
            return false;
        } else if (parValue.length() == 0) {
            if (parDefinition.isMandatory()) {
                return false;
            } else {
                if (parValueType != ParameterValueType.ENUMERATION) {
                    return true;
                } else {
                    // A configured ENUM parameter must always have a value,
                    // even if it's optional...
                    return false;
                }
            }
        }

        switch (parValueType) {
        case BOOLEAN:
            return ADMValidator.isBoolean(parValue);
        case STRING:
            return true;
        case DURATION:
            return (DurationValidation.getDurationInMs(context, parValue) != null);
        case INTEGER:
            return integerIsValid(parDefinition, parValue);
        case LONG:
            return longIsValid(parDefinition, parValue);
        case ENUMERATION:
            return enumIsValid(parDefinition, parValue);
        default:
            return false;
        }
    }

    private static boolean enumIsValid(VOParameterDefinition parDefinition,
            String parValue) {
        for (VOParameterOption option : parDefinition.getParameterOptions()) {
            if (option.getOptionId().equals(parValue)) {
                return true;
            }
        }
        return false;
    }

    private static boolean integerIsValid(VOParameterDefinition parDefinition,
            String parValue) {
        if (GenericValidator.isInt(parValue.toString())) {
            return LongValidator.isInRange(Integer.parseInt(parValue),
                    parDefinition.getMinValue(), parDefinition.getMaxValue());
        } else {
            return false;
        }
    }

    private static boolean longIsValid(VOParameterDefinition parDefinition,
            String parValue) {
        if (GenericValidator.isLong(parValue.toString())) {
            return LongValidator.isInRange(Long.parseLong(parValue),
                    parDefinition.getMinValue(), parDefinition.getMaxValue());
        } else {
            return false;
        }
    }

}
