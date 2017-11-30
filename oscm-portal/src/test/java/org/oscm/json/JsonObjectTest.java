/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class JsonObjectTest {

    @Test
    public void jsonParameterOption_null() {
        // given
        JsonParameterOption jsonParamOpt = givenJsonParameterOption();

        // then
        assertFalse(jsonParamOpt == null);
    }

    @Test
    public void jsonParameterOption_equal1() {
        // given
        JsonParameterOption jsonParamOpt1 = givenJsonParameterOption();
        JsonParameterOption jsonParamOpt2 = givenJsonParameterOption();

        // then
        assertTrue(jsonParamOpt1.equals(jsonParamOpt2));
    }

    @Test
    public void jsonParameterOption_equal2() {
        // given
        JsonParameterOption jsonParamOpt = givenJsonParameterOption();

        // then
        assertTrue(jsonParamOpt.equals(jsonParamOpt));
    }

    @Test
    public void jsonParameterOption_notEqual_class() {
        // given
        JsonParameterOption jsonParamOpt = givenJsonParameterOption();
        JsonParameter jsonParam = givenJsonParameter();

        // then
        assertFalse(jsonParamOpt.equals(jsonParam));
    }

    @Test
    public void jsonParameterOption_notEqual_descr() {
        // given
        JsonParameterOption jsonParamOpt1 = givenJsonParameterOption();
        JsonParameterOption jsonParamOpt2 = givenJsonParameterOption();
        jsonParamOpt2
                .setDescription(jsonParamOpt1.getDescription() + "changed");

        // then
        assertFalse(jsonParamOpt2.equals(jsonParamOpt1));
    }

    @Test
    public void jsonParameterOption_notEqual_id() {
        // given
        JsonParameterOption jsonParamOpt1 = givenJsonParameterOption();
        JsonParameterOption jsonParamOpt2 = givenJsonParameterOption();
        jsonParamOpt2.setDescription(jsonParamOpt1.getId() + "changed");

        // then
        assertFalse(jsonParamOpt2.equals(jsonParamOpt1));
    }

    @Test
    public void jsonParameter_null() {
        // given
        JsonParameter jsonParam = givenJsonParameter();

        // then
        assertFalse(jsonParam == null);
    }

    @Test
    public void jsonParameter_equal1() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();

        // then
        assertTrue(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_equal2() {
        // given
        JsonParameter jsonParam = givenJsonParameter();

        // then
        assertTrue(jsonParam.equals(jsonParam));
    }

    @Test
    public void jsonParameter_notEqual_id() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setId(jsonParam1.getId() + "changed");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_desc() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setDescription(jsonParam1.getDescription() + "changed");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_min() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setMinValue("100000");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_max() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setMaxValue("100000");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_value() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setValue(jsonParam1.getValue() + "changed");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_valueType() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setValueType(jsonParam1.getValueType() + "changed");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_modType() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setModificationType(jsonParam1.getModificationType()
                + "changed");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_readonly() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setReadonly(!jsonParam1.isReadonly());

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_valueError() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setValueError(!jsonParam1.isValueError());

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_mandatory() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.setMandatory(!jsonParam1.isMandatory());

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_option() {
        // given
        JsonParameter jsonParam1 = givenJsonParameter();
        JsonParameter jsonParam2 = givenJsonParameter();
        jsonParam2.getOptions().get(0)
                .setId(jsonParam2.getOptions().get(0).getId() + "changed");

        // then
        assertFalse(jsonParam1.equals(jsonParam2));
    }

    @Test
    public void jsonParameter_notEqual_class() {
        // given
        JsonParameterOption jsonParamOpt = givenJsonParameterOption();
        JsonParameter jsonParam = givenJsonParameter();

        // then
        assertFalse(jsonParam.equals(jsonParamOpt));
    }

    @Test
    public void jsonObject_null() {
        // given
        JsonObject jsonObject = givenJsonObject();

        // then
        assertFalse(jsonObject == null);
    }

    @Test
    public void jsonObject_equal1() {
        // given
        JsonObject jsonObject1 = givenJsonObject();
        JsonObject jsonObject2 = givenJsonObject();

        // then
        assertTrue(jsonObject1.equals(jsonObject2));
    }

    @Test
    public void jsonObject_equal2() {
        // given
        JsonObject jsonObject = givenJsonObject();

        // then
        assertTrue(jsonObject.equals(jsonObject));
    }

    @Test
    public void jsonObject_notEqual_class() {
        // given
        JsonObject jsonObject = givenJsonObject();
        JsonParameter jsonParam = givenJsonParameter();

        // then
        assertFalse(jsonObject.equals(jsonParam));
    }

    @Test
    public void jsonObject_notEqual_locale() {
        // given
        JsonObject jsonObject1 = givenJsonObject();
        JsonObject jsonObject2 = givenJsonObject();
        jsonObject2.setLocale(jsonObject1.getLocale() + "changed");

        // then
        assertFalse(jsonObject1.equals(jsonObject2));
    }

    @Test
    public void jsonObject_notEqual_messageType() {
        // given
        JsonObject jsonObject1 = givenJsonObject();
        JsonObject jsonObject2 = givenJsonObject();
        jsonObject2.setMessageType(MessageType.INIT_MESSAGE);

        // then
        assertFalse(jsonObject1.equals(jsonObject2));
    }

    @Test
    public void jsonObject_notEqual_responseCode() {
        // given
        JsonObject jsonObject1 = givenJsonObject();
        JsonObject jsonObject2 = givenJsonObject();
        jsonObject2.setResponseCode(ResponseCode.CONFIGURATION_CANCELLED);

        // then
        assertFalse(jsonObject1.equals(jsonObject2));
    }

    @Test
    public void jsonObject_notEqual_param() {
        // given
        JsonObject jsonObject1 = givenJsonObject();
        JsonObject jsonObject2 = givenJsonObject();
        jsonObject2.getParameters().get(0)
                .setId(jsonObject1.getParameters().get(0).getId() + "changed");

        // then
        assertFalse(jsonObject1.equals(jsonObject2));
    }

    private JsonParameterOption givenJsonParameterOption() {
        JsonParameterOption jsonParamOpt = new JsonParameterOption();
        jsonParamOpt.setId("id1");
        jsonParamOpt.setDescription("descr1");
        return jsonParamOpt;
    }

    private JsonParameter givenJsonParameter() {
        JsonParameter jsonParam = new JsonParameter();
        jsonParam.setId("id1");
        jsonParam.setDescription("descr1");
        jsonParam.setMandatory(true);
        jsonParam.setMinValue("200");
        jsonParam.setMaxValue("300");
        jsonParam.setModificationType("modtype1");
        jsonParam.setReadonly(true);
        jsonParam.setValueError(true);
        jsonParam.setValue("value1");
        jsonParam.setValueType("valtype1");
        jsonParam.setOptions(Arrays.asList(givenJsonParameterOption()));
        return jsonParam;
    }

    private JsonObject givenJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.setMessageType(MessageType.CONFIG_RESPONSE);
        jsonObject.setResponseCode(ResponseCode.CONFIGURATION_FINISHED);
        jsonObject.setLocale("locale1");
        jsonObject.setParameters(Arrays.asList(givenJsonParameter()));
        return jsonObject;
    }

}
