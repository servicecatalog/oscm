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

public class ValidationPerformer {

    public static void validate(Class<? extends Validator> validatorCls,
            Object validationObject) throws ValidationException {
        Validator validator = ParameterValidatorFactory
                .getValidator(validatorCls);
        if (validator.supports(validationObject)) {
            validator.validate(validationObject);
        }
    }

    public static void validate(ParameterValueType valueType,
            Object validationObject) throws ValidationException {
        Validator validator = ParameterValidatorFactory.getValidator(valueType);
        if (validator.supports(validationObject)) {
            validator.validate(validationObject);
        }
    }
}
