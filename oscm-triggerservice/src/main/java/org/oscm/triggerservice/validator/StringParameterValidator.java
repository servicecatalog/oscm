/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:56
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;

/**
 * Created by FlorekS
 */
public class StringParameterValidator extends DefaultParameterValidator {
    @Override
    public boolean supports(Object obj) {
        if (super.supports(obj)) {
            VOParameter parameter = (VOParameter) obj;
            return ParameterValueType.STRING.equals(parameter
                    .getParameterDefinition().getValueType());
        }

        return false;
    }
}
