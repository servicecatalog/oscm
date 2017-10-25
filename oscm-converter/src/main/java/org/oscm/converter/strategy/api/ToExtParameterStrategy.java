/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *******************************************************************************/
package org.oscm.converter.strategy.api;

import org.oscm.converter.api.Converter;
import org.oscm.converter.strategy.ConversionStrategy;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOParameterDefinition;

public class ToExtParameterStrategy extends AbstractConversionStrategy implements ConversionStrategy<Parameter, VOParameter> {
    @Override
    public VOParameter convert(Parameter parameter) {
        if (parameter == null) {
            return null;
        }
        VOParameter voParameter = new VOParameter();

        voParameter.setKey(parameter.getKey());
        voParameter.setVersion(parameter.getVersion());
        voParameter.setConfigurable(parameter.isConfigurable());
        voParameter.setValue(parameter.getValue());
        voParameter.setParameterDefinition(Converter.convert(parameter.getParameterDefinition(), ParameterDefinition.class, VOParameterDefinition.class));

        return voParameter;
    }
}
