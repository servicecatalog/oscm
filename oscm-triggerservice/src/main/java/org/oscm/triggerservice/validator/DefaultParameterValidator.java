/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:56
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;

/**
 *
 */
public abstract class DefaultParameterValidator implements Validator {

    @Override
    public boolean supports(Object obj) {
        return VOParameter.class.isAssignableFrom(obj.getClass());
    }

    @Override
    public void validate(Object obj) throws ValidationException {
        VOParameter parameter = (VOParameter) obj;
        VOParameterDefinition parameterDefinition = parameter
                .getParameterDefinition();

        if (isNull(parameterDefinition)
                || isNull(parameterDefinition.getValueType())
                || isNullAndMandatory(parameter) 
                || isOptionAndEmpty(parameter)
                || isEmptyAndMandatory(parameter)){

            throw new ValidationException(
                    ValidationException.ReasonEnum.EMPTY_VALUE, null,
                    new Object[] { parameterDefinition.getParameterId() });
        }

        if (isTooLong(parameter.getValue())) {
            throw new ValidationException(
                    ValidationException.ReasonEnum.LENGTH, null,
                    new Object[] { parameterDefinition.getParameterId() });
        }

        if(!parameter.isConfigurable()) {
            throw new ValidationException(
                    ValidationException.ReasonEnum.READONLY, null,
                    new Object[] { parameterDefinition.getParameterId() });
        }
    }

    private boolean isNull(Object obj) {
        return obj == null;
    }

    private boolean isTooLong(String paramValue) {
        return paramValue != null
                && paramValue.length() > ADMValidator.LENGTH_DESCRIPTION;
    }

    private boolean isOptionAndEmpty(VOParameter parameter) {
        return ParameterValueType.ENUMERATION.equals(parameter
                .getParameterDefinition().getValueType())
                && !parameter.getParameterDefinition().isMandatory()
                && (parameter.getValue() == null || parameter.getValue().trim()
                        .length() == 0);
    }

    private boolean isNullAndMandatory(VOParameter parameter) {
        return isNull(parameter.getValue())
                && parameter.getParameterDefinition().isMandatory();
    }

    private boolean isEmptyAndMandatory(VOParameter parameter) {
        return parameter.getParameterDefinition().isMandatory()
                && parameter.getValue().isEmpty();
    }
    
    protected boolean isOptionalAndNullOrEmpty(VOParameter parameter) {
        return !parameter.getParameterDefinition().isMandatory() &&
                (parameter.getValue() == null || parameter.getValue().isEmpty());
    }
}
