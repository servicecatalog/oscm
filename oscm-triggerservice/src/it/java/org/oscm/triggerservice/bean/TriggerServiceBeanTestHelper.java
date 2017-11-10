/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:59
 *
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import java.util.List;
import java.util.Random;

import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import com.google.common.collect.Lists;

public class TriggerServiceBeanTestHelper {

    public static final TriggerType VALID_TRIGGER_TYPE = TriggerType.SUBSCRIBE_TO_SERVICE;
    public static final TriggerProcessStatus VALID_TRIGGER_STATUS = TriggerProcessStatus.WAITING_FOR_APPROVAL;

    public static TriggerProcess getTriggerProcess(TriggerProcessStatus state,
            TriggerType type) {
        TriggerProcess triggerProcess = new TriggerProcess();

        TriggerDefinition triggerDefinition = new TriggerDefinition();
        triggerDefinition.setType(type);

        triggerProcess.setTriggerDefinition(triggerDefinition);
        triggerProcess.setState(state);

        return triggerProcess;
    }

    public static TriggerProcess getTriggerProcess(TriggerType type) {
        return getTriggerProcess(VALID_TRIGGER_STATUS, type);
    }

    public static TriggerProcess getTriggerProcess() {
        return getTriggerProcess(VALID_TRIGGER_TYPE);
    }

    public static <T extends Enum<?>> T randomEnum(Class<T> cls, T exceptValue) {
        int x = new Random().nextInt(cls.getEnumConstants().length);
        T t = cls.getEnumConstants()[x];
        return t.equals(exceptValue) ? randomEnum(cls, exceptValue) : t;
    }

    public static <T extends Enum<?>> T randomEnum(Class<T> cls) {
        return randomEnum(cls, null);
    }
    
    public static List<VOParameter> getVOParameters(
            ParameterValueType valueType, String paramValue, boolean isConfigurable) {
        List<VOParameter> parameters = getVOParameters(valueType, paramValue);
        
        parameters.get(0).setConfigurable(isConfigurable);
        
        return parameters;
    }

    public static List<VOParameter> getVOParameters(
            ParameterValueType valueType, String paramValue) {
        VOParameter parameter = new VOParameter();
        VOParameterDefinition parameterDefinition = new VOParameterDefinition();

        parameterDefinition.setValueType(valueType);
        parameter.setValue(paramValue);
        parameter.setConfigurable(true);

        parameter.setParameterDefinition(parameterDefinition);

        return Lists.newArrayList(parameter);
    }

    public static List<VOParameter> getVOParameters(
            ParameterValueType valueType, String paramValue, Long minRange,
            Long maxRange) {
        List<VOParameter> result = getVOParameters(valueType, paramValue);

        VOParameter parameter = result.get(0);
        parameter.setConfigurable(true);
        parameter.getParameterDefinition().setMinValue(minRange);
        parameter.getParameterDefinition().setMaxValue(maxRange);

        return result;
    }

    public static List<VOParameter> getVOParameters(
            ParameterValueType valueType, String paramValue, String optionId) {
        List<VOParameter> result = getVOParameters(valueType, paramValue);

        VOParameterOption option = new VOParameterOption();
        option.setOptionId(optionId);

        result.get(0).getParameterDefinition()
                .setParameterOptions(Lists.newArrayList(option));
        result.get(0).setConfigurable(true);

        return result;
    }
}
