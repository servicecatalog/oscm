/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 25.09.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOParameterDefinition;
import org.oscm.internal.vo.VOPricedOption;
import org.oscm.internal.vo.VOPricedParameter;
import org.oscm.internal.vo.VOPricedRole;
import org.oscm.test.stubs.LocalizerServiceStub;

/**
 * Tests to ensure correct behaviour of the parameter assembler.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class ParameterAssemblerTest {
    public static final Long L1 = 1L;
    public static final Long L150 = 150L;

    private LocalizerFacade facade = new LocalizerFacade(
            new LocalizerServiceStub() {

                @Override
                public String getLocalizedTextFromDatabase(String localeString,
                        long objectKey, LocalizedObjectTypes objectType) {
                    return "";
                }

                @Override
                public String getLocalizedTextFromBundle(
                        LocalizedObjectTypes objectType, Marketplace shop,
                        String localeString, String key) {
                    return "";
                }
            }, "en");

    @Test
    public void testIsInt() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        paramDef.setMinimumValue(L1);
        paramDef.setMaximumValue(L150);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("123");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsIntMinInt() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue(String.valueOf(Integer.MIN_VALUE));
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsIntMaxInt() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue(String.valueOf(Integer.MAX_VALUE));
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsIntTooLessValue() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue(String.valueOf((long) Integer.MIN_VALUE - 1));
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.INTEGER", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "-2147483649",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsIntTooGreatValue() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue(String.valueOf((long) Integer.MAX_VALUE + 1));
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.INTEGER", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "2147483648",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsIntNoNumericValue() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("no valid number");
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.INTEGER", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "no valid number",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsIntNullValue() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.INTEGER", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertNull("Wrong params", e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsIntNullValueNotMandatory() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsIntEmptyValueNotMandatory() throws Exception {
        ParameterDefinition paramDef = initIntegerParamDef();
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("   ");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsLong() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("123");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsLongMinValue() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue(String.valueOf(Long.MIN_VALUE));
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsLongMaxValue() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue(String.valueOf(Long.MAX_VALUE));
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsLongTooLessValue() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        BigInteger bigInt = BigInteger.valueOf(Long.MIN_VALUE);
        BigInteger subtract = BigInteger.valueOf(1);
        bigInt = bigInt.subtract(subtract);
        param.setValue(String.valueOf(bigInt));
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.LONG", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "-9223372036854775809",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsLongTooGreatValue() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        BigInteger bigInt = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger addValue = BigInteger.valueOf(1);
        bigInt = bigInt.add(addValue);
        param.setValue(String.valueOf(bigInt));
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.LONG", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "9223372036854775808",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsLongNoNumericValue() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("no valid number");
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.LONG", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "no valid number",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsLongNullValue() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.LONG", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", null, e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsLongNullValueNotMandatory() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsLongEmptyValueNotMandatory() throws Exception {
        ParameterDefinition paramDef = initLongParamDef();
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("   ");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsBoolean() throws Exception {
        ParameterDefinition paramDef = initBooleanParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("false");
        ParameterAssembler.validateParameter(param, paramDef);

        param.setValue("TRUE");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsBooleanInvalidValue() throws Exception {
        ParameterDefinition paramDef = initBooleanParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("fasld");
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.BOOLEAN", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "fasld",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsBooleanNullValue() throws Exception {
        ParameterDefinition paramDef = initBooleanParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.BOOLEAN", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", null, e.getMessageParams()[0]);
        }
    }

    @Test
    public void testIsBooleanNullValueNotMandatory() throws Exception {
        ParameterDefinition paramDef = initBooleanParamDef();
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsBooleanEmptyValueNotMandatory() throws Exception {
        ParameterDefinition paramDef = initBooleanParamDef();
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("   ");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsEnumeration() throws Exception {
        ParameterDefinition paramDef = initEnumerationParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("1");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test(expected = ValidationException.class)
    public void testIsEnumerationEmptyValue() throws Exception {
        ParameterDefinition paramDef = initEnumerationParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test(expected = ValidationException.class)
    public void testIsEnumerationEmptyOptions() throws Exception {
        List<ParameterOption> empty = Collections.emptyList();
        ParameterDefinition paramDef = initEnumerationParamDef();
        paramDef.setOptionList(empty);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("not_existing");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test(expected = ValidationException.class)
    public void testIsEnumerationInvalidParameterValue() throws Exception {
        ParameterDefinition paramDef = initEnumerationParamDef();
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("iujgtf");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsEnumerationNullValueNotMandatory() throws Exception {
        List<ParameterOption> empty = Collections.emptyList();
        ParameterDefinition paramDef = initEnumerationParamDef();
        paramDef.setOptionList(empty);
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testIsEnumerationEmptzyValueNotMandatory() throws Exception {
        ParameterDefinition paramDef = initEnumerationParamDef();
        paramDef.setMandatory(false);
        VOParameterDefinition voParamDef = ParameterDefinitionAssembler
                .toVOParameterDefinition(paramDef, facade);
        VOParameter param = new VOParameter(voParamDef);
        param.setValue("   ");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testPricedParameterWithRolesAssembling() throws Exception {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("roleId");

        ParameterDefinition paramDef = new ParameterDefinition();
        Parameter param = new Parameter();
        param.setParameterDefinition(paramDef);

        PricedProductRole ppr = new PricedProductRole();
        ppr.setRoleDefinition(rd);
        ppr.setPricePerUser(BigDecimal.valueOf(123L));

        PricedParameter pricedParam = new PricedParameter();
        pricedParam.setParameter(param);
        pricedParam.setRoleSpecificUserPrices(Collections.singletonList(ppr));

        List<VOPricedParameter> voPricedParameters = ParameterAssembler
                .toVOPricedParameters(Collections.singletonList(pricedParam),
                        facade);

        Assert.assertEquals(1, voPricedParameters.size());
        VOPricedParameter voPricedParameter = voPricedParameters.get(0);
        VOPricedRole voPricedProductRole = voPricedParameter
                .getRoleSpecificUserPrices().get(0);
        Assert.assertEquals(BigDecimal.valueOf(123L),
                voPricedProductRole.getPricePerUser());
        Assert.assertEquals("roleId", voPricedProductRole.getRole().getRoleId());
    }

    @Test
    public void testPricedParameterOptionWithRolesAssembling() throws Exception {
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("roleId");

        ParameterDefinition paramDef = new ParameterDefinition();
        Parameter param = new Parameter();
        param.setParameterDefinition(paramDef);

        PricedProductRole ppr = new PricedProductRole();
        ppr.setRoleDefinition(rd);
        ppr.setPricePerUser(BigDecimal.valueOf(123L));

        PricedParameter pricedParam = new PricedParameter();
        pricedParam.setParameter(param);

        PricedOption option = new PricedOption();
        option.setRoleSpecificUserPrices(Collections.singletonList(ppr));
        pricedParam.setPricedOptionList(Collections.singletonList(option));

        List<VOPricedOption> pricedOptions = ParameterAssembler
                .toVOPricedParameters(Collections.singletonList(pricedParam),
                        facade).get(0).getPricedOptions();

        Assert.assertEquals(1, pricedOptions.size());
        VOPricedOption voPricedOption = pricedOptions.get(0);
        VOPricedRole voPricedProductRole = voPricedOption
                .getRoleSpecificUserPrices().get(0);
        Assert.assertEquals(BigDecimal.valueOf(123L),
                voPricedProductRole.getPricePerUser());
        Assert.assertEquals("roleId", voPricedProductRole.getRole().getRoleId());
    }

    @Test
    public void testvalidateParameters_DurationNull() throws Exception {
        ParameterDefinition paramDef = initDurationParam("1");
        VOParameter param = new VOParameter(
                ParameterDefinitionAssembler.toVOParameterDefinition(paramDef,
                        facade));
        param.setValue("0");
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testvalidateParameters_DurationTwoDays() throws Exception {
        ParameterDefinition paramDef = initDurationParam("1");
        VOParameter param = new VOParameter(
                ParameterDefinitionAssembler.toVOParameterDefinition(paramDef,
                        facade));
        String value = String.valueOf(2 * 24 * 3600000);
        param.setValue(value);
        ParameterAssembler.validateParameter(param, paramDef);
    }

    @Test
    public void testvalidateParameters_DurationNegative() throws Exception {
        ParameterDefinition paramDef = initDurationParam("1");
        VOParameter param = new VOParameter(
                ParameterDefinitionAssembler.toVOParameterDefinition(paramDef,
                        facade));
        param.setValue("-10");
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            fail("Wrong input value must cause exception");
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.POSITIVE_NUMBER, e.getReason());
        }
    }

    @Test
    public void testvalidateParameters_DurationFraction() throws Exception {
        ParameterDefinition paramDef = initDurationParam("1");
        VOParameter param = new VOParameter(
                ParameterDefinitionAssembler.toVOParameterDefinition(paramDef,
                        facade));
        param.setValue("5.6");
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            fail("Wrong input value must cause exception");
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.LONG, e.getReason());
        }
    }

    @Test
    public void testvalidateParameters_DurationTooLargeValue() throws Exception {
        ParameterDefinition paramDef = initDurationParam("1");
        VOParameter param = new VOParameter(
                ParameterDefinitionAssembler.toVOParameterDefinition(paramDef,
                        facade));
        BigInteger bigInt = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger subtract = BigInteger.valueOf(1);
        bigInt = bigInt.add(subtract);
        param.setValue(String.valueOf(bigInt));
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            Assert.fail("Operation must fail, as the passed value is not a valid integer");
        } catch (ValidationException e) {
            Assert.assertEquals("Wrong message key",
                    "ex.ValidationException.LONG", e.getMessageKey());
            Assert.assertEquals("Wrong param number", 1,
                    e.getMessageParams().length);
            Assert.assertEquals("Wrong params", "9223372036854775808",
                    e.getMessageParams()[0]);
        }
    }

    @Test
    public void testvalidateParameters_DurationNoFullDay() throws Exception {
        ParameterDefinition paramDef = initDurationParam("1");
        VOParameter param = new VOParameter(
                ParameterDefinitionAssembler.toVOParameterDefinition(paramDef,
                        facade));
        String value = String.valueOf(24 * 3600000 - 1);
        param.setValue(value);
        try {
            ParameterAssembler.validateParameter(param, paramDef);
            fail("Wrong input value must cause exception");
        } catch (ValidationException e) {
            assertEquals(ReasonEnum.DURATION, e.getReason());
        }
    }

    @Test(expected = ValidationException.class)
    public void testToParameterDefinition_Null_ModificationType()
            throws Exception {
        VOParameterDefinition voParamDef = new VOParameterDefinition();
        voParamDef.setModificationType(null);
        ParameterAssembler.toParameterDefinition(voParamDef);
    }

    @Test(expected = ValidationException.class)
    public void validatePricedParameter_subscriptionPriceNotInScale()
            throws Exception {
        // given
        VOPricedParameter param = new VOPricedParameter();
        param.setPricePerSubscription(BigDecimal.TEN
                .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1));
        param.setPricePerUser(BigDecimal.TEN);

        // when
        ParameterAssembler.validatePricedParameter(param);
    }

    @Test(expected = ValidationException.class)
    public void validatePricedParameter_pricePerUserNotInScale()
            throws Exception {
        // given
        VOPricedParameter param = new VOPricedParameter();
        param.setPricePerSubscription(BigDecimal.TEN);
        param.setPricePerUser(BigDecimal.TEN
                .setScale(PriceConverter.NUMBER_OF_DECIMAL_PLACES + 1));

        // when
        ParameterAssembler.validatePricedParameter(param);
    }

    private ParameterDefinition initDurationParam(String value) {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId("testParamDuration");
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setValueType(ParameterValueType.DURATION);
        paramDef.setDefaultValue(value);
        paramDef.setConfigurable(false);
        paramDef.setMandatory(false);
        paramDef.setModificationType(ParameterModificationType.STANDARD);
        paramDef.setOptionList(new ArrayList<ParameterOption>());
        return paramDef;
    }

    private ParameterDefinition initIntegerParamDef() {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId("testParamInteger");
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setValueType(ParameterValueType.INTEGER);
        paramDef.setDefaultValue("");
        paramDef.setMinimumValue(Long.MIN_VALUE);
        paramDef.setMaximumValue(Long.MAX_VALUE);
        paramDef.setConfigurable(true);
        paramDef.setMandatory(true);
        paramDef.setModificationType(ParameterModificationType.STANDARD);
        paramDef.setOptionList(new ArrayList<ParameterOption>());
        return paramDef;
    }

    private ParameterDefinition initLongParamDef() {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId("testParamLong");
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setValueType(ParameterValueType.LONG);
        paramDef.setDefaultValue("");
        paramDef.setMinimumValue(Long.MIN_VALUE);
        paramDef.setMaximumValue(Long.MAX_VALUE);
        paramDef.setConfigurable(true);
        paramDef.setMandatory(true);
        paramDef.setModificationType(ParameterModificationType.STANDARD);
        paramDef.setOptionList(new ArrayList<ParameterOption>());
        return paramDef;
    }

    private ParameterDefinition initBooleanParamDef() {
        ParameterDefinition paramDef = new ParameterDefinition();
        paramDef.setParameterId("testParamBoolean");
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setValueType(ParameterValueType.BOOLEAN);
        paramDef.setDefaultValue("");
        paramDef.setConfigurable(true);
        paramDef.setMandatory(true);
        paramDef.setModificationType(ParameterModificationType.STANDARD);
        paramDef.setOptionList(new ArrayList<ParameterOption>());
        return paramDef;
    }

    private ParameterDefinition initEnumerationParamDef() {
        String parameterId = "testParamEnumeration";
        ParameterDefinition paramDef = new ParameterDefinition();
        ParameterOption paramOption = new ParameterOption();
        paramOption.setOptionId("1");
        paramOption.setParameterDefinition(paramDef);
        paramDef.setParameterId(parameterId);
        paramDef.setParameterType(ParameterType.SERVICE_PARAMETER);
        paramDef.setValueType(ParameterValueType.ENUMERATION);
        paramDef.setDefaultValue("");
        paramDef.setConfigurable(true);
        paramDef.setMandatory(true);
        paramDef.setModificationType(ParameterModificationType.STANDARD);
        paramDef.setOptionList(Collections.singletonList(paramOption));
        return paramDef;
    }
}
