/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import java.util.ArrayList;
import java.util.List;

import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.vo.BaseAssembler;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;

/**
 * Assembler for parameter definitions.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ParameterDefinitionAssembler extends BaseAssembler {

    /**
     * Returns all parameter definitions in their value object representation as
     * specified.
     * 
     * @param platformParameterDefinitions
     *            The platform parameter definitions, that will be added to the
     *            list of service related parameter definitions.
     * @param parameterDefinitions
     *            The parameter definitions defined for the service.
     * @param excludeNonConfigurableDefs
     *            Indicates whether the non-configurable parameter definitions
     *            have to be assembled as well. This setting affects elements of
     *            both lists provided as input parameters.
     * @param facade
     *            The localizer facade object used to retrieve the localized
     *            data.
     * @return The list of parameter definitions.
     */
    public static List<VOParameterDefinition> toVOParameterDefinitions(
            List<ParameterDefinition> platformParameterDefinitions,
            List<ParameterDefinition> parameterDefinitions,
            boolean excludeNonConfigurableDefs, LocalizerFacade facade) {
        List<VOParameterDefinition> result = new ArrayList<VOParameterDefinition>();
        if (platformParameterDefinitions != null) {
            for (ParameterDefinition paramDef : platformParameterDefinitions) {
                if (needToInclude(excludeNonConfigurableDefs, paramDef)) {
                    VOParameterDefinition voParam = toVOParameterDefinition(
                            paramDef, facade);
                    result.add(voParam);
                }
            }
        }
        if (parameterDefinitions != null) {
            for (ParameterDefinition paramDef : parameterDefinitions) {
                if (needToInclude(excludeNonConfigurableDefs, paramDef)) {
                    VOParameterDefinition voParam = toVOParameterDefinition(
                            paramDef, facade);
                    result.add(voParam);
                }
            }
        }
        return result;
    }

    /**
     * Returns whether a certain parameter definition has to be assembled or
     * not.
     * 
     * @param excludeOfNonConfigurable
     *            If set to <code>true</code>, the method will return false if
     *            the parameter definition is not configurable.
     * @param paramDef
     *            The parameter definition to be considered.
     * @return <code>true</code> in case the parameter definition has to be
     *         assembled, <code>false</code> otherwise.
     */
    private static boolean needToInclude(boolean excludeOfNonConfigurable,
            ParameterDefinition paramDef) {
        if (excludeOfNonConfigurable && !paramDef.isConfigurable()) {
            return false;
        }
        return true;
    }

    /**
     * Converts a parameter definition domain object to a corresponding value
     * object.
     * 
     * @param paramDef
     *            The parameter definition to be converted.
     * @param facade
     *            The localizer facade.
     * @return The value object representation of the parameter definition.
     */
    public static VOParameterDefinition toVOParameterDefinition(
            ParameterDefinition paramDef, LocalizerFacade facade) {
        if (paramDef == null) {
            return null;
        }
        ParameterType paramType = paramDef.getParameterType();
        String paramId = paramDef.getParameterId();
        String description = facade.getText(paramDef.getKey(),
                LocalizedObjectTypes.PARAMETER_DEF_DESC);
        ParameterValueType valueType = paramDef.getValueType();
        ParameterModificationType modificationType = paramDef
                .getModificationType();
        String defaultValue = paramDef.getDefaultValue();
        Long minValue = paramDef.getMinimumValue();
        Long maxValue = paramDef.getMaximumValue();
        boolean mandatory = paramDef.isMandatory();
        boolean configurable = paramDef.isConfigurable();
        List<ParameterOption> options = paramDef.getOptionList();

        List<VOParameterOption> voOptions = ParameterOptionAssembler
                .toVOParameterOptions(options, facade,
                        paramDef.getParameterId());
        VOParameterDefinition voParam = new VOParameterDefinition(paramType,
                paramId, description, valueType, defaultValue, minValue,
                maxValue, mandatory, configurable, modificationType, voOptions);

        updateValueObject(voParam, paramDef);
        return voParam;
    }

}
