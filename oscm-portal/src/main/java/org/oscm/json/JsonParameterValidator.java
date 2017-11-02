/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 15.05.15 10:18
 *
 *******************************************************************************/

package org.oscm.json;

import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.OUTCOME_ERROR;
import static org.oscm.ui.dialog.mp.subscriptionDetails.SubscriptionDetailsCtrlConstants.VALIDATION_ERROR;

import java.io.IOException;
import java.util.Collection;

import javax.faces.context.FacesContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.ExternalParameterValidation;
import org.oscm.ui.dialog.mp.interfaces.ConfigParamValidateable;
import org.oscm.ui.dialog.mp.subscriptionDetails.ManageSubscriptionModel;
import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionWizardConversationModel;
import org.oscm.ui.dialog.mp.subscriptionwizard.UpgradeWizardModel;
import org.oscm.ui.model.ParameterValidationResult;
import org.oscm.ui.model.PricedParameterRow;

/**
 * Created on 2015-05-12.
 */
public class JsonParameterValidator {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(JsonParameterValidator.class);

    private final JsonConverter converter;

    public JsonParameterValidator(JsonConverter converter) {
        this.converter = converter;
    }

    public boolean validateParameters(JsonObject jsonResponse, FacesContext context, Collection<PricedParameterRow> serviceParameters) {
        boolean validationError = false;

        for (JsonParameter responseParameter : jsonResponse.getParameters()) {
            PricedParameterRow pricedParRow = JsonUtils.findPricedParameterRowById(responseParameter.getId(), serviceParameters);
            if (pricedParRow != null) {
                boolean parIsValid = ExternalParameterValidation
                        .parameterIsValid(
                                pricedParRow.getParameterDefinition(),
                                responseParameter.getValue(), context);
                responseParameter.setValueError(!parIsValid);
                validationError = validationError || !parIsValid;
            } else {
                // The external tool has sent a parameter, which it didn't get
                // from BES. This may be a parameter without an ID, an unknown
                // parameter or a parameter, which is not configurable and was
                // thus not sent to the tool.
                validationError = true;
            }
        }

        return validationError;
    }

    /**
     * Action for validation of parameters configured by an external tool
     *
     * @param swc
     * @return the logical outcome success.
     */
    public String validateConfiguredParameters(SubscriptionWizardConversationModel model) {
        JsonObject jsonObject = getJsonResponse(model);
        if (!hasValidationError(jsonObject, model)) {
            model.setConfigurationChanged(isConfigurationChanged(model, jsonObject));
            model.setParameterValidationResult(new ParameterValidationResult(false, null));
            return null;
        } else {
            return validateParametersError(model, jsonObject);
        }
    }

    /**
     * Action for validation of parameters configured by an external tool
     *
     * @param msc
     * @return the logical outcome success.
     */
    public String validateConfiguredParameters(ManageSubscriptionModel model) {
        JsonObject jsonObject = getJsonResponse(model);
        if (!hasValidationError(jsonObject, model)) {
            model.setConfigurationChanged(isConfigurationChanged(model, jsonObject));
            model.setParameterValidationResult(new ParameterValidationResult(false, null));
            return null;
        } else {
            return validateParametersError(model, jsonObject);
        }
    }

    /**
     * Action for validation of parameters configured by an external tool
     *
     * @param model
     * @return the logical outcome success.
     */
    public String validateConfiguredParameters(UpgradeWizardModel model) {
        JsonObject jsonObject = getJsonResponse(model);
        if (!hasValidationError(jsonObject, model)) {
            model.setParameterValidationResult(new ParameterValidationResult(false, null));
            return null;
        } else {
            return validateParametersError(model, jsonObject);
        }
    }

    private JsonObject getJsonResponse(ConfigParamValidateable model) {
        String configResponse = model.getParameterConfigResponse();

        JsonObject jsonResponse = null;
        try {
            jsonResponse = converter.parseJsonString(configResponse);
        } catch (IOException ioe) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, ioe, LogMessageIdentifier.ERROR);
        }

        return jsonResponse;
    }

    private boolean hasValidationError(JsonObject jsonResponse, ConfigParamValidateable model) {
        boolean validationError = true;
        if (jsonResponse != null) {
            validationError = validateParameters(
                    jsonResponse,
                    FacesContext.getCurrentInstance(),
                    model.getServiceParameters());
        }

        return validationError;
    }

    private boolean isConfigurationChanged(ConfigParamValidateable model, JsonObject jsonResponse) {
        return converter.updateValueObjects(jsonResponse, model.getService());
    }

    private String validateParametersError(ConfigParamValidateable model, JsonObject jsonResponse) {

        ParameterValidationResult validationResult = new ParameterValidationResult(true, null);
        model.setParameterValidationResult(validationResult);

        try {
            JsonObject jsonRequest = converter.getServiceParametersAsJsonObject(
                    model.getServiceParameters(),
                    model.isReadOnlyParams(),
                    model.isSubscriptionExisting());

            JsonUtils.copyResponseParameters(jsonRequest, jsonResponse);
            validationResult.setConfigRequest(converter.createJsonString(jsonRequest));
            return VALIDATION_ERROR;
        } catch (JsonProcessingException e) {
            model.setHideExternalConfigurator(true);
            return OUTCOME_ERROR;
        }

    }
}
