/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:56
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;

/**
 * Created by FlorekS
 */
public class EnumerationParameterValidator extends DefaultParameterValidator {

    @Override
    public boolean supports(Object obj) {
        if (super.supports(obj)) {
            VOParameter parameter = (VOParameter) obj;
            return ParameterValueType.ENUMERATION.equals(parameter
                    .getParameterDefinition().getValueType());
        }

        return false;
    }

    @Override
    public void validate(Object obj) throws ValidationException {
        super.validate(obj);
        VOParameter parameter = (VOParameter) obj;

        if (!isEnumValid(parameter, parameter.getParameterDefinition())) {
            throw new ValidationException(
                    ValidationException.ReasonEnum.ENUMERATION, null,
                    new Object[] { parameter.getParameterDefinition()
                            .getParameterId() });
        }
    }

    private boolean isEnumValid(VOParameter parameter,
            VOParameterDefinition parameterDefinition) {
        for (VOParameterOption option : parameterDefinition
                .getParameterOptions()) {
            if (option.getOptionId().equals(parameter.getValue())) {
                return true;
            }
        }
        return false;
    }
}
