/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Author: stavreva
 *
 *  Creation Date: 18.12.2013
 *
 *******************************************************************************/

package org.oscm.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;

/**
 * Converter between parameter VOs and JSON representation.
 *
 */
public class JsonConverter {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(JsonConverter.class);
    private UiDelegate ui = new UiDelegate();

    public String createJsonFromPricedParameterRows(
            MessageType messageType, ResponseCode responseCode,
            List<PricedParameterRow> parameterRows, String locale,
            boolean readonly, boolean editableOneTimeParams)
            throws JsonProcessingException {
        return createJsonString(convertToJsonObject(messageType, responseCode,
                parameterRows, locale, readonly, editableOneTimeParams));
    }

    public String createJsonString(JsonObject jsonObject)
            throws JsonProcessingException {
        try {
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_JSON_PROCESSING_FAILED);
            throw e;
        }
    }

    public JsonObject convertToJsonObject(MessageType messageType,
            ResponseCode responseCode,
            List<PricedParameterRow> pricedParamList, String locale,
            boolean readonly, boolean editableOneTimeParams) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.setMessageType(messageType);
        jsonObject.setResponseCode(responseCode);

        if (pricedParamList == null) {
            return jsonObject;
        }

        jsonObject.setLocale(Strings.nullToEmpty(locale));
        List<JsonParameter> jsonParamList = new ArrayList<>();

        for (PricedParameterRow pricedParam : pricedParamList) {
            JsonParamBuilder paramBuilder = new JsonParamBuilder(pricedParam);

            if(!paramBuilder.isValid()) {
                continue;
            }

            JsonParameter jsonParam = paramBuilder.
                    setValueError(false).
                    setReadOnly(readonly, editableOneTimeParams).
                    setValue().
                    setMinValue().
                    setMaxValue().
                    setMandatory().
                    setDescription().
                    setId().
                    setValueType().
                    setModificationType().
                    setOptions().
                    setPricePerUser().
                    setPricePerSubscription().
                    build();

            jsonParamList.add(jsonParam);

        }

        jsonObject.setParameters(jsonParamList);
        return jsonObject;
    }

    public JsonObject parseJsonString(String jsonString)
            throws IOException {
        ObjectMapper om = new ObjectMapper();
        JsonObject jsonObject;
        try {
            jsonObject = om.readValue(jsonString, JsonObject.class);
            List<JsonParameter> params = jsonObject.getParameters();
            for (JsonParameter p : params) {
                p.setDescription(EscapeUtils.unescapeJSON(p.getDescription()));
                p.setValue(EscapeUtils.unescapeJSON(p.getValue()));
                List<JsonParameterOption> options = p.getOptions();
                for (JsonParameterOption opt : options) {
                    opt.setDescription(EscapeUtils.unescapeJSON(opt
                            .getDescription()));
                }
            }
        } catch (IOException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_JSON_IO_EXCEPTION, jsonString);
            throw e;
        }
        return jsonObject;
    }

    public String getServiceParametersAsJsonString(
            List<PricedParameterRow> serviceParameters, boolean readOnlyParams, boolean subscriptionExisting) {
        String parameterJsonString;
        try {
            parameterJsonString =
                    createJsonFromPricedParameterRows(
                            MessageType.CONFIG_REQUEST, null,
                            serviceParameters,
                            ui.getViewLocale().getLanguage(),
                            readOnlyParams,
                            !subscriptionExisting);
        } catch (JsonProcessingException e) {
            parameterJsonString = null;
        }
        return parameterJsonString;
    }

    public JsonObject getServiceParametersAsJsonObject(
            List<PricedParameterRow> serviceParameters, boolean readOnlyParams, boolean subscriptionExisting) {
        return convertToJsonObject(MessageType.CONFIG_REQUEST,
                null, serviceParameters, ui.getViewLocale().getLanguage(),
                readOnlyParams, !subscriptionExisting);
    }

    public void logParameterNotFound(String parameterId) {
        LOGGER.logWarn(Log4jLogger.SYSTEM_LOG,
                LogMessageIdentifier.WARN_UPDATE_PARAMETER_UNKNOWN_ID,
                parameterId);
    }

    public boolean isValueEqual(VOParameter voParameter, JsonParameter p) {
        String voParam = voParameter.getValue();
        if (voParam == null) {
            voParam = "";
        }

        if (voParameter.getParameterDefinition().getValueType()
                .equals(ParameterValueType.STRING)) {
            return voParam.equals(p.getValue());
        }

        if (!p.getValue().equals("")
                && ParameterValueType.DURATION.equals(voParameter
                        .getParameterDefinition().getValueType())) {
            long durationInMs = Long.valueOf(p.getValue()).longValue()
                    * DurationValidation.MILLISECONDS_PER_DAY;
            return voParam.equalsIgnoreCase(Long.toString(durationInMs));
        }

        return voParam.equalsIgnoreCase(p.getValue());
    }

    public boolean updateValueObjects(JsonObject responseParameters, Service service) {
        boolean configurationChanged = false;
        if (responseParameters == null
                || responseParameters.getResponseCode() != ResponseCode.CONFIGURATION_FINISHED) {
            return configurationChanged;
        }

        for (JsonParameter p : responseParameters.getParameters()) {

            VOParameter voParameter = JsonUtils.findParameterById(p.getId(), service);
            if (voParameter == null) {
                logParameterNotFound(p.getId());
                continue;
            }
            if (!isValueEqual(voParameter, p)) {

                if (p.getValue() != null && p.getValue().length() > 0) {
                    ParameterValueType valueType = voParameter
                            .getParameterDefinition().getValueType();

                    if (ParameterValueType.DURATION.equals(valueType)) {
                        // Internally the duration is stored in milliseconds,
                        // but
                        // in the UI and in the external configuration tool
                        // the duration is shown in days...
                        long durationInMs = Long.valueOf(p.getValue()).longValue()
                                * DurationValidation.MILLISECONDS_PER_DAY;
                        voParameter.setValue(String.valueOf(durationInMs));
                    } else if (ParameterValueType.BOOLEAN.equals(valueType)) {
                        voParameter.setValue(p.getValue().toLowerCase());
                    } else {
                        voParameter.setValue(p.getValue());
                    }
                } else {
                    voParameter.setValue(p.getValue());
                }
                configurationChanged = true;
            }
        }
        return configurationChanged;
    }

    public void setUiDelegate(UiDelegate uiDelegate) {
        this.ui = uiDelegate;
    }
}
