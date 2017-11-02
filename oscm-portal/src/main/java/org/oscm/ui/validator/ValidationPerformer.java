/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2015-05-28
 *
 *******************************************************************************/

package org.oscm.ui.validator;

public class ValidationPerformer {

    public static boolean validate(Class<? extends Validator> validatorCls, Object validationObject, Object validationParam) {
        Validator validator = ValidatorFactory.getValidator(validatorCls);
        return validator.supports(validationObject.getClass(), validationParam.getClass()) &&
                validator.validate(validationObject, validationParam);

    }
}
