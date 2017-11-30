/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 14.01.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.subscriptionDetails;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.common.ExternalParameterValidation;
import org.oscm.ui.model.PricedParameterRow;
import org.oscm.ui.model.Service;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOParameterOption;
import org.oscm.internal.vo.VOService;

/**
 * @author kulle
 * 
 */
public class ValidateExternalParameterTest {

    private ManageSubscriptionCtrl  ctrl;
    private ManageSubscriptionModel model;
    private FacesContext context;

    @Before
    public void setup() {
        VOService voService = new VOService();
        Service srv = new Service(voService);
        model = spy(new ManageSubscriptionModel());
        model.setService(srv);
        model.setServiceParameters(new ArrayList<PricedParameterRow>());
        ctrl = spy(new ManageSubscriptionCtrl());
        ctrl.setModel(model);
        context = new FacesContextStub(Locale.ENGLISH);
    }

    private VOParameterDefinition createParDefinition(String id,
            ParameterValueType valueType, boolean mandatory, Long minValue,
            Long maxValue, String... optionIds) {
        VOParameterDefinition parDefinition = new VOParameterDefinition();
        parDefinition.setParameterId(id);
        parDefinition.setMandatory(mandatory);
        parDefinition.setValueType(valueType);

        parDefinition.setMinValue(minValue);
        parDefinition.setMaxValue(maxValue);

        if (optionIds.length > 0) {
            List<VOParameterOption> options = new ArrayList<VOParameterOption>();
            for (String optionId : optionIds) {
                options.add(new VOParameterOption(optionId, optionId
                        + "Description", id));
            }
            parDefinition.setParameterOptions(options);
        }

        return parDefinition;
    }

    @Test
    public void parameterIsValid_Boolean_True() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("ABoolean",
                ParameterValueType.BOOLEAN, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "TrUe", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Boolean_False() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("ABoolean",
                ParameterValueType.BOOLEAN, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "FALSE", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Boolean_Mandatory_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("ABoolean",
                ParameterValueType.BOOLEAN, true, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertFalse("Mandatory parameter must have a value", parIsValid);
    }

    @Test
    public void parameterIsValid_Boolean_Optional_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("ABoolean",
                ParameterValueType.BOOLEAN, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertTrue("An optional parameter may be empty", parIsValid);
    }

    @Test
    public void parameterIsValid_Wrong_Boolean() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("ABoolean",
                ParameterValueType.BOOLEAN, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "FAALSE", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Duration() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("PERIOD",
                ParameterValueType.DURATION, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "23", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Duration_too_large() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("PERIOD",
                ParameterValueType.DURATION, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "106751991177", context);

        // then
        assertFalse("Duration value was too large", parIsValid);
    }

    @Test
    public void parameterIsValid_Duration_invalid() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("PERIOD",
                ParameterValueType.DURATION, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "12.5", context);

        // then
        assertFalse("Duration value must be a valid long", parIsValid);
    }

    @Test
    public void parameterIsValid_Duration_invalid2() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("PERIOD",
                ParameterValueType.DURATION, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "12ABEF", context);

        // then
        assertFalse("Duration value must be a valid long", parIsValid);
    }

    @Test
    public void parameterIsValid_Duration_null() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("PERIOD",
                ParameterValueType.DURATION, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, null, context);

        // then
        assertFalse("Parameter value may not be null", parIsValid);
    }

    @Test
    public void parameterIsValid_String_tooLong() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("MyString",
                ParameterValueType.STRING, false, null, null);
        StringBuffer stringParameter = new StringBuffer();
        for (int i = 0; i < ADMValidator.LENGTH_DESCRIPTION + 4; i++) {
            stringParameter.append("C");
        }

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, stringParameter.toString(), context);

        // then
        assertFalse("Parameter value was too long", parIsValid);
    }

    @Test
    public void parameterIsValid_String() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("MyString",
                ParameterValueType.STRING, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "abcd", context);

        // then
        assertTrue("Parameter value was valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Enum() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("DiskSpace",
                ParameterValueType.ENUMERATION, true, null, null, "Minimum",
                "Medium", "Maximum");

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "Medium", context);

        // then
        assertTrue("Parameter has a valid value", parIsValid);
    }

    @Test
    public void parameterIsValid_Enum_Mandatory_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("DiskSpace",
                ParameterValueType.ENUMERATION, true, null, null, "Minimum",
                "Medium", "Maximum");

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertFalse("Mandatory parameter must have a value", parIsValid);
    }

    @Test
    public void parameterIsValid_Enum_Optional_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("DiskSpace",
                ParameterValueType.ENUMERATION, false, null, null, "Minimum",
                "Medium", "Maximum");

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertFalse("An optional enum parameter must also have a value",
                parIsValid);
    }

    @Test
    public void parameterIsValid_Enum_WrongValue() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("DiskSpace",
                ParameterValueType.ENUMERATION, true, null, null, "Minimum",
                "Medium", "Maximum");

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "Maxxximum", context);

        // then
        assertFalse("Value of Enum parameter must be a defined option",
                parIsValid);
    }

    @Test
    public void parameterIsValid_Integer() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(10),
                Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "15", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer2() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "15", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer3() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(-10),
                Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "-10", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer4() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(-10),
                Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "20", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_Mandatory_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, true, Long.valueOf(10),
                Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertFalse("Mandatory Parameter must have a value", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_Optional_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(10),
                Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertTrue("Optional Integer Parameter may be empty", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_WrongValue() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(10),
                Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "abcd", context);

        // then
        assertFalse("Integer Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_TooLarge() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, null, Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "25", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_TooLarge2() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(10),
                Long.valueOf(20));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "25", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_TooLarge3() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation
                .parameterIsValid(parDefinition,
                        String.valueOf(Integer.MAX_VALUE) + "0", context);

        // then
        assertFalse("Parameter was larger than Integer.MAX_VALUE", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_TooSmall() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(20), null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "10", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_TooSmall2() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, Long.valueOf(20),
                Long.valueOf(40));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "10", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Integer_TooSmall3() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.INTEGER, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation
                .parameterIsValid(parDefinition,
                        String.valueOf(Integer.MIN_VALUE) + "0", context);

        // then
        assertFalse("Parameter was smaller than Integer.MIN_VALUE", parIsValid);
    }

    @Test
    public void parameterIsValid_Long() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(10),
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "10000000000", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long2() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "15000000000", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long3() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(-10000000000L),
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "-10000000000", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long4() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(-10000000000L),
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "20000000000", context);

        // then
        assertTrue("Parameter should be valid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_Mandatory_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, true, Long.valueOf(10),
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertFalse("Mandatory Parameter must have a value", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_Optional_Empty() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(10),
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "", context);

        // then
        assertTrue("Optional Long Parameter may be empty", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_WrongValue() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(10),
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "abcd", context);

        // then
        assertFalse("Long Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_TooLarge() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, null,
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "30000000000", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_TooLarge2() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(10),
                Long.valueOf(20000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "25000000000", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_TooLarge3() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, String.valueOf(Long.MAX_VALUE) + "0", context);

        // then
        assertFalse("Parameter was larger than Long.MAX_VALUE", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_TooSmall() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(20000000000L),
                null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "10000000000", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_TooSmall2() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, Long.valueOf(20000000000L),
                Long.valueOf(40000000000L));

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "10000000000", context);

        // then
        assertFalse("Parameter was invalid", parIsValid);
    }

    @Test
    public void parameterIsValid_Long_TooSmall3() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("Number1",
                ParameterValueType.LONG, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, String.valueOf(Long.MIN_VALUE) + "0", context);

        // then
        assertFalse("Parameter was smaller than Long.MIN_VALUE", parIsValid);
    }

    @Test
    public void parameterIsValid_NoValueType() throws Exception {
        // given
        VOParameterDefinition parDefinition = createParDefinition("AParameter",
                null, false, null, null);

        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(
                parDefinition, "4711", context);

        // then
        assertFalse("Parameter must have a value type", parIsValid);
    }

    @Test
    public void parameterIsValid_NoParDefinition() throws Exception {
        // when
        boolean parIsValid = ExternalParameterValidation.parameterIsValid(null,
                "4711", context);

        // then
        assertFalse("Parameter must have a value type", parIsValid);
    }

}
