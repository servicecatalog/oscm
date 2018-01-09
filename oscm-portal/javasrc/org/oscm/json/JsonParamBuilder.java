/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *   Creation Date: 15.05.15 10:18
 *
 *******************************************************************************/

package org.oscm.json;

import java.util.List;

import org.oscm.string.Strings;
import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;

/**
 * Created by FlorekS on 2015-05-12.
 */
public class JsonParamBuilder {

    private final PricedParameterRow pricedParam;
    private VOParameter parameter;
    private VOPricedParameter pricedParameter;
    private VOParameterDefinition parameterDef;
    private JsonParameter jsonParam = new JsonParameter();

    public JsonParamBuilder(PricedParameterRow pricedParam) {
        this.pricedParam = pricedParam;

        if(pricedParam != null) {
            this.parameter = pricedParam.getParameter();
            this.parameterDef = pricedParam.getParameterDefinition();
            this.pricedParameter = pricedParam.getPricedParameter();
        }

    }

    public boolean isValid() {
        return pricedParam != null &&
                pricedParam.getPricedOption() == null &&
                parameterDef != null;
    }

    public JsonParamBuilder setValueError(boolean valueError) {
        jsonParam.setValueError(valueError);

        return this;
    }

    public JsonParamBuilder setReadOnly(boolean readOnly, boolean editableOneTimeParams) {
        boolean result = readOnly || (pricedParam.isOneTimeParameter() && !editableOneTimeParams);

        jsonParam.setReadonly(result);

        return this;
    }

    public JsonParameter build() {
        return jsonParam;
    }


    public JsonParamBuilder setValue() {
        if (parameterDef.getValueType() == ParameterValueType.DURATION
                && parameter.getValue() != null
                && parameter.getValue().length() > 0) {
            long durationValueDays = Long.valueOf(parameter.getValue()).longValue() / (DurationValidation.MILLISECONDS_PER_DAY);
            jsonParam.setValue(EscapeUtils.escapeJSON(durationValueDays + ""));
        } else {
            jsonParam.setValue(Strings.nullToEmpty(EscapeUtils.escapeJSON(parameter.getValue())));
        }

        return this;
    }

    public JsonParamBuilder setMinValue() {
        if (parameterDef.getMinValue() != null) {
            jsonParam.setMinValue(parameterDef.getMinValue().toString());
        } else {
            jsonParam.setMinValue(Strings.nullToEmpty(null));
        }

        return this;
    }

    public JsonParamBuilder setMaxValue() {
        if (parameterDef.getMaxValue() != null) {
            jsonParam.setMaxValue(parameterDef.getMaxValue().toString());
        } else {
            if (parameterDef.getValueType() == ParameterValueType.DURATION) {
                jsonParam.setMaxValue(DurationValidation.DURATION_MAX_DAYS_VALUE + "");
            } else {
                jsonParam.setMaxValue(Strings.nullToEmpty(null));
            }
        }

        return this;
    }

    public JsonParamBuilder setMandatory() {
        jsonParam.setMandatory(parameterDef.isMandatory());

        return this;
    }

    public JsonParamBuilder setDescription() {
        jsonParam.setDescription(Strings.nullToEmpty(EscapeUtils.escapeJSON(parameterDef.getDescription())));

        return this;
    }


    public JsonParamBuilder setId() {
        jsonParam.setId(Strings.nullToEmpty(parameterDef.getParameterId()));

        return this;
    }

    public JsonParamBuilder setValueType() {
        if (parameterDef.getValueType() == null) {
            jsonParam.setValueType(Strings.nullToEmpty(null));
        } else {
            jsonParam.setValueType(Strings.nullToEmpty(parameterDef.getValueType().name()));
        }

        return this;
    }

    public JsonParamBuilder setModificationType() {
        if (parameterDef.getModificationType() == null) {
            jsonParam.setModificationType(Strings.nullToEmpty(null));
        } else {
            jsonParam.setModificationType(Strings.nullToEmpty(parameterDef.getModificationType().name()));
        }

        return this;
    }

    public JsonParamBuilder setOptions() {
        List<VOParameterOption> voParamOpts = parameterDef.getParameterOptions();

        if (voParamOpts != null) {
            for (VOParameterOption voOpt : voParamOpts) {
                JsonParameterOption jsonOpt = new JsonParameterOption();
                jsonOpt.setId(Strings.nullToEmpty(voOpt.getOptionId()));
                jsonOpt.setDescription(Strings.nullToEmpty(EscapeUtils.escapeJSON(voOpt.getOptionDescription())));
                if(pricedParameter != null){
	                for (VOPricedOption voPricedOpt : pricedParameter.getPricedOptions()) {
	                    if(voPricedOpt.getParameterOptionKey() == voOpt.getKey()){
	                        jsonOpt.setPricePerUser(voPricedOpt.getPricePerUser().doubleValue());
	                        jsonOpt.setPricePerSubscription(voPricedOpt.getPricePerSubscription().doubleValue());
	                    }
	                }
	                jsonParam.getOptions().add(jsonOpt);
                }
            }
        }

        return this;
    }

    public JsonParamBuilder setPricePerUser() {
        if (pricedParameter != null) {
            jsonParam.setPricePerUser(pricedParameter.getPricePerUser().doubleValue());
        }

        return this;
    }

    public JsonParamBuilder setPricePerSubscription() {
        if (pricedParameter != null) {
            jsonParam.setPricePerSubscription(pricedParameter.getPricePerSubscription().doubleValue());
        }

        return this;
    }
}
