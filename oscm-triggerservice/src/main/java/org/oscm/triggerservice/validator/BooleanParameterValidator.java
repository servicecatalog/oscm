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

/**
 * Created by FlorekS
 */
public class BooleanParameterValidator extends DefaultParameterValidator {

    @Override
    public boolean supports(Object obj) {
        if (super.supports(obj)) {
            VOParameter parameter = (VOParameter) obj;
            return ParameterValueType.BOOLEAN.equals(parameter
                    .getParameterDefinition().getValueType());
        }

        return false;
    }

    @Override
    public void validate(Object obj) throws ValidationException {
        super.validate(obj);
        VOParameter parameter = (VOParameter) obj;

        if(isOptionalAndNullOrEmpty(parameter)) {
            return;
        }
        
        if (!ADMValidator.isBoolean(parameter.getValue())) {
            throw new ValidationException(
                    ValidationException.ReasonEnum.BOOLEAN, null,
                    new Object[] { parameter.getParameterDefinition()
                            .getParameterId() });
        }
    }
}
