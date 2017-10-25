/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.OperationParameterType;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOServiceOperationParameterValues;

/**
 * @author Yuyin
 * 
 */
public class TechServiceOperationParameterAssembler extends BaseAssembler {

    /**
     * Converts the provided list of {@link OperationParameter} to a list of
     * {@link VOServiceOperationParameter} setting name, Id and RequestValues.
     * 
     * @param roleDefinitions
     *            the {@link OperationParameter}s to convert
     * @return the list of {@link VOServiceOperationParameter}s
     */
    public static List<VOServiceOperationParameter> toVOServiceOperationParameters(
            List<OperationParameter> operationParameters,
            LocalizerFacade localizerFacade) {
        if (operationParameters == null) {
            return null;
        }
        List<VOServiceOperationParameter> result = new ArrayList<VOServiceOperationParameter>();
        for (OperationParameter parameter : operationParameters) {
            result.add(toVOServiceOperationParameter(parameter, localizerFacade));
        }
        return result;
    }

    /**
     * Converts a {@link OperationParameter} into a
     * {@link VOServiceOperationParameter}. If <code>null</code> is passed in,
     * <code>null</code> is returned.
     * 
     * @param OperationParameter
     *            the {@link OperationParameter} to convert
     * @param facade
     *            the {@link LocalizerFacade} to get the localizable name.
     * @return the {@link VOServiceOperationParameter}.
     */
    public static VOServiceOperationParameter toVOServiceOperationParameter(
            OperationParameter parameter, LocalizerFacade localizerFacade) {
        if (parameter == null) {
            return null;
        }
        VOServiceOperationParameter voParameter = new VOServiceOperationParameter();
        updateParameter(parameter, localizerFacade, voParameter);
        return voParameter;
    }

    public static VOServiceOperationParameterValues toVOServiceOperationParameterValues(
            OperationParameter parameter, LocalizerFacade localizerFacade,
            List<String> values) {
        if (parameter == null) {
            return null;
        }
        VOServiceOperationParameterValues vo = new VOServiceOperationParameterValues();
        updateParameter(parameter, localizerFacade, vo);
        vo.setValues(values);
        return vo;
    }

    static void updateParameter(OperationParameter parameter,
            LocalizerFacade localizerFacade,
            VOServiceOperationParameter voParameter) {
        updateValueObject(voParameter, parameter);
        voParameter.setParameterId(parameter.getId());
        voParameter.setMandatory(parameter.isMandatory());
        voParameter.setType(OperationParameterType.valueOf(parameter.getType()
                .name()));
        voParameter
                .setParameterName(localizerFacade.getText(
                        parameter.getKey(),
                        LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME));
    }

}
