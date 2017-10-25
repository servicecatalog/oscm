/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *******************************************************************************/

package org.oscm.converter.strategy.api;

import java.util.List;

import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.vo.VOParameterOption;

public class ToExtParameterOptionStrategy extends AbstractConversionStrategy implements ConversionStrategy<ParameterOption, VOParameterOption> {

    @Override
    public VOParameterOption convert(ParameterOption parameterOption) {
        if (parameterOption == null) {
            return null;
        }

        VOParameterOption voParameterDefinition = new VOParameterOption();

        voParameterDefinition.setKey(parameterOption.getKey());
        voParameterDefinition.setOptionId(parameterOption.getOptionId());
        voParameterDefinition.setVersion(parameterOption.getVersion());

        final List<LocalizedObjectTypes> localizedObjectTypes = parameterOption.getLocalizedObjectTypes();
        final String locale = getDataService().getCurrentUser().getLocale();
        final List<LocalizedResource> localizedResources = getLocalizedResource(localizedObjectTypes, Long.valueOf(parameterOption.getKey()), locale);

        for (LocalizedResource resource : localizedResources) {
            if (resource.getObjectType().equals(LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC)){
                voParameterDefinition.setOptionDescription(resource.getValue());
            }
        }

        return voParameterDefinition;
    }

}
