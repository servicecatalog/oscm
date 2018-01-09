/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: stavreva                                                      
 *                                                                              
 *  Creation Date: 18.12.2013                                                    
 *                                                                              
 *******************************************************************************/

package org.oscm.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.oscm.ui.common.DurationValidation;
import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionWizardConversation;
import org.oscm.ui.dialog.mp.subscriptionwizard.SubscriptionWizardConversationModel;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOService;

/**
 * Test converter between parameter PricedParameterRow(VOs) and JSON
 * representation.
 * 
 */
public class JsonConverterTest {
    
    private JsonConverter jsonConverter;
    private JsonParameterValidator jsonValidator;

    private static final String EMPTY_STRING = "";
    private static final String PARAM_ID = "paramId";
    private static final String PARAM_DESC = "paramDesc\n";
    private static final String PARAM_DESC_ESC = "paramDesc\\\\n";
    private static final ParameterValueType PARAM_VALUETYPE = ParameterValueType.ENUMERATION;
    private static final ParameterValueType PARAM_DURATION_VALUETYPE = ParameterValueType.DURATION;
    private static final String PARAM_VALUE = "paramValue\f";
    private static final Long PARAM_DURATION_VALUE = new Long(
            DurationValidation.MILLISECONDS_PER_DAY * 2L);
    private static final Long PARAM_DURATION_EXPECTED_VALUE = new Long(2L);
    private static final String PARAM_VALUE_ESC = "paramValue\\\\f";
    private static final Long PARAM_MIN = Long.valueOf(100L);
    private static final Long PARAM_MAX = Long.valueOf(200L);
    private static final Double PARAM_NO_PRICE = Double.valueOf(0.0);
    private static final Double PARAM_PRICE_ONE = Double.valueOf(1.0);
    private static final Double PARAM_PRICE_TEN = Double.valueOf(10.0);
    private static final ParameterModificationType PARAM_MODTYPE = ParameterModificationType.STANDARD;
    private static final ParameterModificationType PARAM_MODTYPE_ONE_TIME = ParameterModificationType.ONE_TIME;
    private static final boolean PARAM_MANDATORY = true;
    private static final boolean PARAM_CONFIGURABLE = true;
    private static final boolean PARAM_READONLY = false;
    private static final boolean PARAM_VAL_ERROR = false;
    private static final String PARAM_OPT_ID = "optId";
    private static final String PARAM_OPT_DESC = "optDesc\b";
    private static final String PARAM_OPT_DESC_ESC = "optDesc\\\\b";
    private static final String LOCALE = "en";
    private static final MessageType MESSAGE_TYPE = MessageType.CONFIG_REQUEST;
    private static final ResponseCode RESPONSE_CODE = ResponseCode.CONFIGURATION_CANCELLED;

    private static final String PARAM_OPT_OBJ = "{\"id\":\"" + PARAM_OPT_ID
            + "\",\"description\":\"" + PARAM_OPT_DESC_ESC + "\""
            + ",\"pricePerUser\":" + PARAM_NO_PRICE +",\"pricePerSubscription\":"
            + PARAM_NO_PRICE + "}";

    private static final String PARAM_OBJ_NO_OPT = "{\"id\":\"" + PARAM_ID
            + "\",\"valueType\":\"" + PARAM_VALUETYPE + "\",\"minValue\":\""
            + PARAM_MIN + "\",\"maxValue\":\"" + PARAM_MAX
            + "\",\"mandatory\":" + PARAM_MANDATORY + ",\"description\":\""
            + PARAM_DESC_ESC + "\",\"value\":\"" + PARAM_VALUE_ESC
            + "\",\"readonly\":" + PARAM_READONLY + ",\"modificationType\":\""
            + PARAM_MODTYPE + "\",\"valueError\":" + PARAM_VAL_ERROR
            + ",\"pricePerUser\":" + PARAM_NO_PRICE +",\"pricePerSubscription\":"
            + PARAM_NO_PRICE + "}";

    private static final String PARAM_OBJ_NO_OPT_ONETIME = PARAM_OBJ_NO_OPT
            .replace("\"readonly\":false", "\"readonly\":true").replace(
                    "\"modificationType\":\"" + PARAM_MODTYPE + "\"",
                    "\"modificationType\":\"" + PARAM_MODTYPE_ONE_TIME + "\"");

    private static final String PARAM_OBJ_WITH_OPT = "{\"id\":\"" + PARAM_ID
            + "\",\"valueType\":\"" + PARAM_VALUETYPE + "\",\"minValue\":\""
            + PARAM_MIN + "\",\"maxValue\":\"" + PARAM_MAX
            + "\",\"mandatory\":" + PARAM_MANDATORY + ",\"description\":\""
            + PARAM_DESC_ESC + "\",\"value\":\"" + PARAM_VALUE_ESC
            + "\",\"readonly\":" + PARAM_READONLY + ",\"modificationType\":\""
            + PARAM_MODTYPE + "\",\"valueError\":" + PARAM_VAL_ERROR
            + ",\"pricePerUser\":" + PARAM_NO_PRICE +",\"pricePerSubscription\":"
            + PARAM_NO_PRICE
            + ",\"options\":[" + PARAM_OPT_OBJ + "," + PARAM_OPT_OBJ + "]}";

    private static final String PARAM_DURATION = "{\"id\":\"" + PARAM_ID
            + "\",\"valueType\":\"" + PARAM_DURATION_VALUETYPE
            + "\",\"minValue\":\"" + PARAM_MIN + "\",\"maxValue\":\""
            + DurationValidation.DURATION_MAX_DAYS_VALUE + "\",\"mandatory\":"
            + PARAM_MANDATORY + ",\"description\":\"" + PARAM_DESC_ESC
            + "\",\"value\":\"" + PARAM_DURATION_EXPECTED_VALUE
            + "\",\"readonly\":" + PARAM_READONLY + ",\"modificationType\":\""
            + PARAM_MODTYPE + "\",\"valueError\":" + PARAM_VAL_ERROR
            + ",\"pricePerUser\":" + PARAM_NO_PRICE +",\"pricePerSubscription\":"
            + PARAM_NO_PRICE + "}";

    private static final String PARAM_OBJ_WITH_OPT_ONETIME = PARAM_OBJ_WITH_OPT
            .replace("\"readonly\":false", "\"readonly\":true").replace(
                    "\"modificationType\":\"" + PARAM_MODTYPE + "\"",
                    "\"modificationType\":\"" + PARAM_MODTYPE_ONE_TIME + "\"");

    private static final String PARAM_OBJ_ID_VALUE = "{\"id\":\"" + PARAM_ID
            + "\",\"value\":\"" + PARAM_VALUE_ESC + "\"}";

    private static final String PARAM_OBJ_ID = "{\"id\":\"" + PARAM_ID + "\"}";

    private static final String JSON_STRING_NO_OPT_VALID = "{\"messageType\":\""
            + MESSAGE_TYPE
            + "\",\"responseCode\":\""
            + RESPONSE_CODE
            + "\",\"locale\":\""
            + LOCALE
            + "\",\"parameters\":["
            + PARAM_OBJ_NO_OPT
            + ","
            + PARAM_OBJ_NO_OPT_ONETIME
            + ","
            + PARAM_DURATION + "]}";

    private static final String JSON_STRING_VALID = "{\"messageType\":\""
            + MESSAGE_TYPE + "\",\"responseCode\":\"" + RESPONSE_CODE
            + "\",\"locale\":\"" + LOCALE + "\",\"parameters\":["
            + PARAM_OBJ_WITH_OPT + "," + PARAM_OBJ_WITH_OPT_ONETIME + ","
            + PARAM_DURATION + "]}";

    private static final String JSON_STRING_INVALID = "{\"locale\":\"" + LOCALE
            + "\",\"parameters\":" + PARAM_OBJ_WITH_OPT + ","
            + PARAM_OBJ_WITH_OPT + "}";

    private static final String JSON_STRING_VALID_NO_LOCALE = "{\"messageType\":\""
            + MESSAGE_TYPE
            + "\",\"responseCode\":\""
            + RESPONSE_CODE
            + "\",\"parameters\":["
            + PARAM_OBJ_WITH_OPT
            + ","
            + PARAM_OBJ_WITH_OPT_ONETIME + "," + PARAM_DURATION + "]}";

    private static final String JSON_STRING_VALID_PARAMS_ID_VALUE = "{\"messageType\":\""
            + MESSAGE_TYPE
            + "\",\"responseCode\":\""
            + RESPONSE_CODE
            + "\",\"parameters\":["
            + PARAM_OBJ_ID_VALUE
            + ","
            + PARAM_OBJ_ID_VALUE + "]}";

    private static final String JSON_STRING_PARAMS_ID_NULLVALUE = "{\"messageType\":\""
            + MESSAGE_TYPE
            + "\",\"responseCode\":\""
            + RESPONSE_CODE
            + "\",\"parameters\":[" + PARAM_OBJ_ID + "," + PARAM_OBJ_ID + "]}";

    private static final String PARAM_OPT_OBJ_PRICES = "{\"id\":\"" + PARAM_OPT_ID
            + "\",\"description\":\"" + PARAM_OPT_DESC_ESC + "\""
            + ",\"pricePerUser\":" + PARAM_PRICE_ONE +",\"pricePerSubscription\":"
            + PARAM_PRICE_TEN + "}";

    private static final String PARAM_OBJ_WITH_OPT_PRICES = "{\"id\":\"" + PARAM_ID
            + "\",\"valueType\":\"" + PARAM_VALUETYPE + "\",\"minValue\":\""
            + PARAM_MIN + "\",\"maxValue\":\"" + PARAM_MAX
            + "\",\"mandatory\":" + PARAM_MANDATORY + ",\"description\":\""
            + PARAM_DESC_ESC + "\",\"value\":\"" + PARAM_VALUE_ESC
            + "\",\"readonly\":" + PARAM_READONLY + ",\"modificationType\":\""
            + PARAM_MODTYPE + "\",\"valueError\":" + PARAM_VAL_ERROR
            + ",\"pricePerUser\":" + PARAM_PRICE_TEN +",\"pricePerSubscription\":"
            + PARAM_PRICE_ONE
            + ",\"options\":[" + PARAM_OPT_OBJ_PRICES + "," + PARAM_OPT_OBJ_PRICES + "]}";

    private static final String PARAM_DURATION_PRICES = "{\"id\":\"" + PARAM_ID
            + "\",\"valueType\":\"" + PARAM_DURATION_VALUETYPE
            + "\",\"minValue\":\"" + PARAM_MIN + "\",\"maxValue\":\""
            + DurationValidation.DURATION_MAX_DAYS_VALUE + "\",\"mandatory\":"
            + PARAM_MANDATORY + ",\"description\":\"" + PARAM_DESC_ESC
            + "\",\"value\":\"" + PARAM_DURATION_EXPECTED_VALUE
            + "\",\"readonly\":" + PARAM_READONLY + ",\"modificationType\":\""
            + PARAM_MODTYPE + "\",\"valueError\":" + PARAM_VAL_ERROR
            + ",\"pricePerUser\":" + PARAM_PRICE_TEN +",\"pricePerSubscription\":"
            + PARAM_PRICE_ONE + "}";

    private static final String PARAM_OBJ_WITH_OPT_ONETIME_PRICES = PARAM_OBJ_WITH_OPT_PRICES
            .replace("\"readonly\":false", "\"readonly\":true").replace(
                    "\"modificationType\":\"" + PARAM_MODTYPE + "\"",
                    "\"modificationType\":\"" + PARAM_MODTYPE_ONE_TIME + "\"");

    private static final String JSON_STRING_PRICES = "{\"messageType\":\""
            + MESSAGE_TYPE + "\",\"responseCode\":\"" + RESPONSE_CODE
            + "\",\"locale\":\"" + LOCALE + "\",\"parameters\":["
            + PARAM_OBJ_WITH_OPT_PRICES + "," + PARAM_OBJ_WITH_OPT_ONETIME_PRICES + ","
            + PARAM_DURATION_PRICES + "]}";

    @Before
    public void setUp() throws Exception {
        jsonConverter = new JsonConverter(){
            @Override
            public boolean updateValueObjects(JsonObject responseParameters,
                    Service service) {
                return true;
            }
        };

        jsonValidator = new JsonParameterValidator(jsonConverter) {
            @Override
            public boolean validateParameters(JsonObject jsonResponse, FacesContext context, Collection<PricedParameterRow> serviceParameters) {
                return false;
            }
        };

    }

    @Test
    public void createJsonParametersNull() throws Exception {
        // when
        String json = jsonConverter.createJsonFromPricedParameterRows(null,
                null, null, null, false, false);
        // then
        assertEquals("Empty JSON Array expected", "{}", json);
    }

    @Test
    public void createJsonParametersEmpty() throws Exception {
        // when
        String json = jsonConverter.createJsonFromPricedParameterRows(null,
                null, new ArrayList<PricedParameterRow>(), "", false, false);
        // then
        assertEquals("Empty JSON Array expected", "{}", json);
    }

    @Test
    public void createJsonFromPricedParameterRows_ParamWithOptions()
            throws JsonProcessingException {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList(true, false);

        // when
        String json = jsonConverter.createJsonFromPricedParameterRows(
                MESSAGE_TYPE, RESPONSE_CODE, list, LOCALE, false, false);

        // then
        assertEquals(JSON_STRING_VALID, json);
    }

    @Test
    public void createJsonFromPricedParameterRows_ParamWithOptionsAndPrices()
            throws JsonProcessingException {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList(true, true);

        // when
        String json = jsonConverter.createJsonFromPricedParameterRows(
                MESSAGE_TYPE, RESPONSE_CODE, list, LOCALE, false, false);

        // then
        assertEquals(JSON_STRING_PRICES, json);
    }

    @Test
    public void createJsonFromPricedParameterRows_ParamNoOptions()
            throws JsonProcessingException {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList(false, false);

        // when
        String json = jsonConverter.createJsonFromPricedParameterRows(
                MESSAGE_TYPE, RESPONSE_CODE, list, LOCALE, false, false);

        // then
        assertEquals(JSON_STRING_NO_OPT_VALID, json);
    }

    @Test
    public void parseJsonString_Valid() throws IOException {
        // when
        JsonObject jsonObject = jsonConverter
                .parseJsonString(JSON_STRING_VALID);

        // then
        assertEquals(givenJsonObject(LOCALE), jsonObject);
    }

    @Test
    public void parseJsonString_Valid_NoLocale() throws IOException {
        // when
        JsonObject jsonObject = jsonConverter
                .parseJsonString(JSON_STRING_VALID_NO_LOCALE);

        // then
        assertEquals(givenJsonObject(null), jsonObject);
    }

    @Test
    public void parseJsonString_Valid_OnlyParamIdValue() throws IOException {
        // when
        JsonObject jsonObject = jsonConverter
                .parseJsonString(JSON_STRING_VALID_PARAMS_ID_VALUE);

        // then
        assertEquals(givenJsonObjectOnlyIdValue(null), jsonObject);
    }

    @Test
    public void parseJsonString_ValidEmpty() throws IOException {
        // when
        JsonObject jsonObject = jsonConverter.parseJsonString("{}");

        // then
        assertEquals(new JsonObject(), jsonObject);
    }

    @Test
    public void parseJsonString_ValidEmptyParamArray() throws IOException {
        // when
        JsonObject jsonObject = jsonConverter
                .parseJsonString("{\"parameters\":[]}");

        // then
        assertEquals(new JsonObject(), jsonObject);
    }

    @Test(expected = IOException.class)
    public void parseJsonString_Invalid() throws IOException {
        // when
        jsonConverter.parseJsonString(JSON_STRING_INVALID);
    }

    @Test
    public void parseJsonString_NoValues() throws IOException {
        // when
        JsonObject jsonObj = jsonConverter
                .parseJsonString(JSON_STRING_PARAMS_ID_NULLVALUE);

        // then
        List<JsonParameter> params = jsonObj.getParameters();
        for (JsonParameter p : params) {
            assertNull(p.getValue());
        }
    }

    @Test
    public void convertToJsonObject_Readonly_OneTimeEditable() {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList(true, false);

        // when
        JsonObject jsonObj = jsonConverter.convertToJsonObject(MESSAGE_TYPE,
                RESPONSE_CODE, list, LOCALE, true, true);

        // then
        List<JsonParameter> params = jsonObj.getParameters();
        for (JsonParameter p : params) {
            assertTrue(p.isReadonly());
        }
    }

    @Test
    public void convertToJsonObject_NotReadonly_OneTimeEditable() {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList(true, false);

        // when
        JsonObject jsonObj = jsonConverter.convertToJsonObject(MESSAGE_TYPE,
                RESPONSE_CODE, list, LOCALE, false, true);

        // then
        List<JsonParameter> params = jsonObj.getParameters();
        for (JsonParameter p : params) {
            assertFalse(p.isReadonly());
        }
    }

    @Test
    public void convertToJsonObject_Readonly_OneTimeNotEditable() {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList(true, false);

        // when
        JsonObject jsonObj = jsonConverter.convertToJsonObject(MESSAGE_TYPE,
                RESPONSE_CODE, list, LOCALE, true, false);

        // then
        List<JsonParameter> params = jsonObj.getParameters();
        for (JsonParameter p : params) {
            assertTrue(p.isReadonly());
        }
    }

    @Test
    public void convertToJsonObject_NotReadonly_OneTimeNotEditable() {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList(true, false);

        // when
        JsonObject jsonObj = jsonConverter.convertToJsonObject(MESSAGE_TYPE,
                RESPONSE_CODE, list, LOCALE, false, false);

        // then
        List<JsonParameter> params = jsonObj.getParameters();
        for (JsonParameter p : params) {
            if (ParameterModificationType.ONE_TIME.name().equals(
                    p.getModificationType())) {
                assertTrue(p.isReadonly());
            } else {
                assertFalse(p.isReadonly());
            }
        }
    }

    @Test
    public void convertToJsonObject_nullToEmpty() {
        // given
        List<PricedParameterRow> list = givenPricedParameterRowList_nullValues();

        // when
        JsonObject jsonObj = jsonConverter.convertToJsonObject(null, null,
                list, null, false, false);

        // then

        assertEquals(EMPTY_STRING, jsonObj.getLocale());
        List<JsonParameter> params = jsonObj.getParameters();
        for (JsonParameter p : params) {
            assertEquals(EMPTY_STRING, p.getId());
            assertEquals(EMPTY_STRING, p.getValue());
            assertEquals(EMPTY_STRING, p.getMinValue());
            assertEquals(EMPTY_STRING, p.getMaxValue());
            assertEquals(EMPTY_STRING, p.getModificationType());
            assertEquals(EMPTY_STRING, p.getDescription());
            assertEquals(EMPTY_STRING, p.getValueType());
            List<JsonParameterOption> opts = p.getOptions();
            for (JsonParameterOption o : opts) {
                assertEquals(EMPTY_STRING, o.getId());
                assertEquals(EMPTY_STRING, o.getDescription());
            }
        }
    }

    @Test
    public void convertToJsonObject_nullPricedParamRow() {
        // given
        List<PricedParameterRow> list = new ArrayList<>();
        list.add(null);

        // when
        JsonObject jsonObject = jsonConverter.convertToJsonObject(null, null, list, null, false, false);

        //then
        assertTrue(jsonObject.getParameters().isEmpty());
        assertTrue(jsonObject.getLocale().isEmpty());
    }

    @Test
    public void validateConfiguredParameters_configChanged() {
        //given
        SubscriptionWizardConversation swc = new SubscriptionWizardConversation();
        swc.setModel(new SubscriptionWizardConversationModel());
        swc.getModel().setParameterConfigResponse(JSON_STRING_VALID);

        //when
        jsonValidator.validateConfiguredParameters(swc.getModel());

        //then
        Assert.assertEquals(Boolean.TRUE,
                Boolean.valueOf(swc.getModel().isConfigurationChanged()));

    }

    private List<PricedParameterRow> givenPricedParameterRowList(
            boolean withOptions, boolean withPrices) {
        VOService service = new VOService();
        VOParameter voParam = new VOParameter();
        voParam.setValue(PARAM_VALUE);
        voParam.setConfigurable(PARAM_CONFIGURABLE);
        VOParameterDefinition voParamDef = new VOParameterDefinition();
        voParam.setParameterDefinition(voParamDef);
        voParamDef.setConfigurable(true);
        voParamDef.setValueType(PARAM_VALUETYPE);
        voParamDef.setParameterId(PARAM_ID);
        voParamDef.setMinValue(PARAM_MIN);
        voParamDef.setMaxValue(PARAM_MAX);
        voParamDef.setMandatory(PARAM_MANDATORY);
        voParamDef.setDescription(PARAM_DESC);
        voParamDef.setModificationType(PARAM_MODTYPE);
        if (withOptions) {
            VOParameterOption voOpt = new VOParameterOption();
            voOpt.setOptionId(PARAM_OPT_ID);
            voOpt.setOptionDescription(PARAM_OPT_DESC);
            voParamDef.setParameterOptions(Arrays.asList(voOpt, voOpt));
        }

        VOParameter voParamOneTime = new VOParameter();
        voParamOneTime.setValue(PARAM_VALUE);
        voParamOneTime.setConfigurable(PARAM_CONFIGURABLE);
        VOParameterDefinition voParamDefOneTime = new VOParameterDefinition();
        voParamOneTime.setParameterDefinition(voParamDefOneTime);
        voParamDefOneTime.setConfigurable(true);
        voParamDefOneTime.setValueType(PARAM_VALUETYPE);
        voParamDefOneTime.setParameterId(PARAM_ID);
        voParamDefOneTime.setMinValue(PARAM_MIN);
        voParamDefOneTime.setMaxValue(PARAM_MAX);
        voParamDefOneTime.setMandatory(PARAM_MANDATORY);
        voParamDefOneTime.setDescription(PARAM_DESC);
        voParamDefOneTime.setModificationType(PARAM_MODTYPE);
        if (withOptions) {
            VOParameterOption voOpt = new VOParameterOption();
            voOpt.setOptionId(PARAM_OPT_ID);
            voOpt.setOptionDescription(PARAM_OPT_DESC);
            voParamDefOneTime.setParameterOptions(Arrays.asList(voOpt, voOpt));
        }

        voParamOneTime.getParameterDefinition().setModificationType(
                PARAM_MODTYPE_ONE_TIME);

        VOParameter voParamDuration = new VOParameter();
        voParamDuration.setValue(PARAM_DURATION_VALUE.toString());
        voParamDuration.setConfigurable(PARAM_CONFIGURABLE);
        VOParameterDefinition voParamDefDuration = new VOParameterDefinition();
        voParamDuration.setParameterDefinition(voParamDefDuration);
        voParamDefDuration.setConfigurable(true);
        voParamDefDuration.setValueType(PARAM_DURATION_VALUETYPE);
        voParamDefDuration.setParameterId(PARAM_ID);
        voParamDefDuration.setMinValue(PARAM_MIN);
        voParamDefDuration.setMaxValue(null);
        voParamDefDuration.setMandatory(PARAM_MANDATORY);
        voParamDefDuration.setDescription(PARAM_DESC);
        voParamDefDuration.setModificationType(PARAM_MODTYPE);

        service.setParameters(Arrays.asList(voParam, voParamOneTime,
                voParamDuration));

        if(withPrices){
	        VOPriceModel priceModel = new VOPriceModel();

	        List<VOPricedParameter> selectedParameters = new ArrayList<VOPricedParameter>();

	        VOPricedParameter pricedParameter = new VOPricedParameter();

	        pricedParameter.setParameterKey(voParam.getKey());
	        pricedParameter.setPricePerUser(BigDecimal.TEN);
	        pricedParameter.setPricePerSubscription(BigDecimal.ONE);

	        VOPricedOption pricedOption = new VOPricedOption();
	        pricedOption.setPricePerUser(BigDecimal.ONE);
	        pricedOption.setPricePerSubscription(BigDecimal.TEN);
	        pricedOption.setParameterOptionKey(voParam.getParameterDefinition().getParameterOptions().get(0).getKey());

	        List<VOPricedOption> pricedOptions = new ArrayList<VOPricedOption>();

	        pricedOptions.add(pricedOption);

			pricedParameter.setPricedOptions(pricedOptions);

	        selectedParameters.add(pricedParameter);

			priceModel.setSelectedParameters(selectedParameters);

			service.setPriceModel(priceModel);
        }

        List<PricedParameterRow> listPPRow = PricedParameterRow
                .createPricedParameterRowList(service, true, false, true, true,
                        true);
        return listPPRow;
    }

    private List<PricedParameterRow> givenPricedParameterRowList_nullValues() {
        VOService service = new VOService();
        VOParameter voParam = new VOParameter();
        VOParameterDefinition voParamDef = new VOParameterDefinition();
        voParam.setParameterDefinition(voParamDef);
        VOParameterOption voOpt = new VOParameterOption();
        voParamDef.setParameterOptions(Arrays.asList(voOpt, voOpt));

        VOParameter voParam1 = new VOParameter();
        voParam1.setParameterDefinition(voParamDef);
        voOpt = new VOParameterOption();

        service.setParameters(Arrays.asList(voParam, voParam1));

        List<PricedParameterRow> listPPRow = PricedParameterRow
                .createPricedParameterRowList(service, false, false, false,
                        true, true);
        return listPPRow;
    }

    private JsonObject givenJsonObject(String locale) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.setLocale(locale);
        JsonParameter jsonParam = new JsonParameter();
        jsonParam.setMandatory(PARAM_MANDATORY);
        jsonParam.setMinValue(PARAM_MIN.toString());
        jsonParam.setMaxValue(PARAM_MAX.toString());
        jsonParam.setDescription(PARAM_DESC);
        jsonParam.setId(PARAM_ID);
        jsonParam.setModificationType(PARAM_MODTYPE.name());
        jsonParam.setValue(PARAM_VALUE);
        jsonParam.setValueType(PARAM_VALUETYPE.name());
        jsonParam.setReadonly(PARAM_READONLY);
        jsonParam.setValueError(PARAM_VAL_ERROR);
        JsonParameterOption jsonOpt = new JsonParameterOption();
        jsonOpt.setId(PARAM_OPT_ID);
        jsonOpt.setDescription(PARAM_OPT_DESC);
        jsonParam.setOptions(Arrays.asList(jsonOpt, jsonOpt));

        JsonParameter jsonParamOneTime = new JsonParameter();
        jsonParamOneTime.setMandatory(PARAM_MANDATORY);
        jsonParamOneTime.setMinValue(PARAM_MIN.toString());
        jsonParamOneTime.setMaxValue(PARAM_MAX.toString());
        jsonParamOneTime.setDescription(PARAM_DESC);
        jsonParamOneTime.setId(PARAM_ID);
        jsonParamOneTime.setModificationType(PARAM_MODTYPE_ONE_TIME.name());
        jsonParamOneTime.setValue(PARAM_VALUE);
        jsonParamOneTime.setValueType(PARAM_VALUETYPE.name());
        jsonParamOneTime.setReadonly(true);
        jsonParamOneTime.setValueError(PARAM_VAL_ERROR);
        jsonParamOneTime.setOptions(Arrays.asList(jsonOpt, jsonOpt));

        JsonParameter jsonParamDuration = new JsonParameter();
        jsonParamDuration.setValue(PARAM_DURATION_EXPECTED_VALUE.toString());
        jsonParamDuration.setValueType(PARAM_DURATION_VALUETYPE.name());
        jsonParamDuration.setMinValue(PARAM_MIN.toString());
        jsonParamDuration
                .setMaxValue(DurationValidation.DURATION_MAX_DAYS_VALUE + "");
        jsonParamDuration.setMandatory(PARAM_MANDATORY);
        jsonParamDuration.setDescription(PARAM_DESC);
        jsonParamDuration.setModificationType(PARAM_MODTYPE.name());
        jsonParamDuration.setId(PARAM_ID);

        jsonObj.setParameters(Arrays.asList(jsonParam, jsonParamOneTime,
                jsonParamDuration));
        jsonObj.setMessageType(MESSAGE_TYPE);
        jsonObj.setResponseCode(RESPONSE_CODE);
        return jsonObj;
    }

    private JsonObject givenJsonObjectOnlyIdValue(String locale) {
        JsonObject jsonObj = new JsonObject();
        jsonObj.setLocale(locale);
        jsonObj.setMessageType(MESSAGE_TYPE);
        jsonObj.setResponseCode(RESPONSE_CODE);
        JsonParameter jsonParam = new JsonParameter();
        jsonParam.setId(PARAM_ID);
        jsonParam.setValue(PARAM_VALUE);
        jsonObj.setParameters(Arrays.asList(jsonParam, jsonParam));
        return jsonObj;
    }

}
