/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:55
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import org.apache.commons.validator.GenericValidator;

import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;

/**
 * Created by FlorekS
 */
public class LongParameterValidator extends DefaultParameterValidator {
    @Override
    public boolean supports(Object obj) {
        if (super.supports(obj)) {
            VOParameter parameter = (VOParameter) obj;
            return ParameterValueType.LONG.equals(parameter
                    .getParameterDefinition().getValueType());
        }

        return false;
    }

    @Override
    public void validate(Object obj) throws ValidationException {
        super.validate(obj);
        VOParameter parameter = (VOParameter) obj;
        VOParameterDefinition parameterDefinition = parameter
                .getParameterDefinition();

        if (isOptionalAndNullOrEmpty(parameter)) {
            return;
        }

        if (!GenericValidator.isLong(parameter.getValue())) {
            throw new ValidationException(ValidationException.ReasonEnum.LONG,
                    null, new Object[] { parameter.getParameterDefinition()
                            .getParameterId() });
        }

        if (!isValid(parameter, parameterDefinition)) {
            throw new ValidationException(
                    ValidationException.ReasonEnum.VALUE_NOT_IN_RANGE, null,
                    new Object[] { parameter.getParameterDefinition()
                            .getParameterId() });
        }
    }

    private boolean isValid(VOParameter parameter,
            VOParameterDefinition parameterDefinition) {
        return isInRange(Long.valueOf(parameter.getValue()),
                parameterDefinition.getMinValue(),
                parameterDefinition.getMaxValue());
    }

    private boolean isInRange(Long value, Long min, Long max) {
        return !((min != null && value.longValue() < min.longValue()) || (max != null && value
                .longValue() > max.longValue()));
    }
}
