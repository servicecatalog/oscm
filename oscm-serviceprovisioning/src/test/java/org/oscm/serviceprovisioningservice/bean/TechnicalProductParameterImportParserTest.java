/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-8-2                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.xml.sax.Attributes;

import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.ImportException;
import org.oscm.internal.types.exception.UpdateConstraintException;

/**
 * @author yuyin
 * 
 */
public class TechnicalProductParameterImportParserTest {

    private TechnicalProductParameterImportParser parameterParserSpy;
    private TechnicalProduct techProduct;
    private List<ParameterDefinition> obsoleteParameterDefs = null;
    private Attributes att;
    private ParameterDefinition parameterDef;
    private List<Product> products;
    private Parameter param;
    private Product productMock;
    private List<Parameter> parameters;
    @Captor
    ArgumentCaptor<String> strCaptor;
    private final Long maxValue = Long.valueOf(100);
    private final Long minValue = Long.valueOf(1);
    private final String attributeID1 = "AttributeID_1";
    private final String defaultValue = "12";
    private final String strMaxValue = maxValue.toString();
    private final String strMinValue = minValue.toString();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        parameters = new ArrayList<Parameter>();
        param = new Parameter();
        products = new ArrayList<Product>();
        productMock = new Product() {
            private static final long serialVersionUID = 1L;

            public boolean isDeleted() {
                return false;
            }
        };
        techProduct = new TechnicalProduct();
        techProduct.setKey(1234);
        techProduct.setTechnicalProductId("TS-1");
        parameterDef = new ParameterDefinition();
        parameterDef.setKey(1111);
        att = mock(Attributes.class);
        doReturn("true").when(att).getValue("mandatory");
        when(att.getValue("default")).thenReturn("10");
        when(att.getValue("minValue")).thenReturn(minValue.toString());
        when(att.getValue("maxValue")).thenReturn(maxValue.toString());
        when(att.getValue("configurable")).thenReturn("true");
        when(att.getValue("modificationType")).thenReturn("ONE_TIME");

        parameterParserSpy = spy(new TechnicalProductParameterImportParser());
        parameterParserSpy.setTechProduct(techProduct);
        parameterParserSpy.setParamDef(parameterDef);
    }

    @Test
    public void parameterDefParser_OK() throws Exception {
        doReturn(parameterDef).when(parameterParserSpy)
                .createOrUpdateProductParameterDefinition(anyString(),
                        any(ParameterValueType.class), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        anyString());

        ParameterDefinition result = parameterParserSpy.parseParameterDef(
                attributeID1, "STRING", att, techProduct, parameterDef);

        verify(parameterParserSpy, times(1))
                .createOrUpdateProductParameterDefinition(anyString(),
                        any(ParameterValueType.class), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        strCaptor.capture());
        assertEquals(parameterDef.getKey(), result.getKey());
        assertEquals("ONE_TIME", strCaptor.getValue());
    }

    @Test
    public void getTempDefaultValueForEnumeration_OK() throws Exception {
        // given
        parameterParserSpy.setTempDefaultValueForEnumeration("success");
        // when
        String result = parameterParserSpy.getTempDefaultValueForEnumeration();
        // then
        assertEquals("success", result);
    }

    @Test
    public void initProductParameterDefinition_CreateOneTimeModificationType()
            throws Exception {
        // given
        parameterParserSpy.setParamDef(null);
        // when
        ParameterDefinition result = parameterParserSpy
                .createOrUpdateProductParameterDefinition(attributeID1,
                        ParameterValueType.ENUMERATION, "", defaultValue, "1",
                        strMaxValue, "", "ONE_TIME");
        // then
        assertEquals(ParameterModificationType.ONE_TIME,
                result.getModificationType());
        assertEquals(techProduct, result.getTechnicalProduct());
    }

    @Test(expected = UpdateConstraintException.class)
    public void initProductParameterDefinition_UpdateParameterCheck()
            throws Exception {
        // given
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.ONE_TIME);
        products.add(new Product());
        techProduct.setProducts(products);
        // when
        parameterParserSpy.createOrUpdateProductParameterDefinition(
                attributeID1, ParameterValueType.STRING, "false", defaultValue,
                "1", strMaxValue, "true", "STANDARD");
    }

    @Test
    public void initProductParameterDefinition_UpdateModificationTypeOK()
            throws Exception {
        // given
        obsoleteParameterDefs = new ArrayList<ParameterDefinition>();
        obsoleteParameterDefs.add(parameterDef);
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.ONE_TIME);
        techProduct.setProducts(null);
        parameterParserSpy.setTechProduct(null);
        // when
        parameterParserSpy.createOrUpdateProductParameterDefinition(
                attributeID1, ParameterValueType.STRING, "", defaultValue, "1",
                strMaxValue, "", "STANDARD");
        // then
        ParameterDefinition result = parameterParserSpy.getParamDef();
        boolean blResult = parameterParserSpy.isCreateAction();
        assertEquals(ParameterModificationType.STANDARD,
                result.getModificationType());
        assertFalse(blResult);
    }

    @Test
    public void createParameterDefinition_CreateNoModificationType()
            throws Exception {
        // when
        parameterParserSpy.createParameterDefinition(attributeID1,
                ParameterValueType.LONG, "false", defaultValue, "1",
                strMaxValue, "false", null);
        // then
        ParameterDefinition result = parameterParserSpy.getParamDef();
        assertEquals(ParameterModificationType.STANDARD,
                result.getModificationType());
        assertEquals(techProduct, result.getTechnicalProduct());
    }

    @Test
    public void createParameterDefinition_CreateStandardModificationType()
            throws Exception {
        // when
        parameterParserSpy.createParameterDefinition(attributeID1,
                ParameterValueType.DURATION, "true", defaultValue, "1",
                strMaxValue, "true", "STANDARD");
        // then
        ParameterDefinition result = parameterParserSpy.getParamDef();
        boolean blResult = parameterParserSpy.isCreateAction();
        assertEquals(ParameterModificationType.STANDARD,
                result.getModificationType());
        assertEquals(techProduct, result.getTechnicalProduct());
        assertTrue(blResult);
    }

    @Test
    public void updateParameterDefinition_SetDefaultValueNUll()
            throws Exception {
        // given
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.ONE_TIME);
        techProduct.setProducts(null);
        // when
        parameterParserSpy.updateParameterDefinition(attributeID1,
                ParameterValueType.LONG, "true", "", "1", strMaxValue, "true",
                "STANDARD");
        // then
        ParameterDefinition result = parameterParserSpy.getParamDef();
        boolean blResult = parameterParserSpy.isCreateAction();
        assertEquals(ParameterModificationType.STANDARD,
                result.getModificationType());
        assertFalse(blResult);
        assertNull(result.getDefaultValue());
    }

    @Test
    public void updateParameterDefinition_SetDefaultValueDouble()
            throws Exception {
        // given
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.ONE_TIME);
        techProduct.setProducts(null);
        // when
        parameterParserSpy.updateParameterDefinition(attributeID1,
                ParameterValueType.LONG, "true", defaultValue, "", "", "true",
                "STANDARD");
        // then
        ParameterDefinition result = parameterParserSpy.getParamDef();
        boolean blResult = parameterParserSpy.isCreateAction();
        assertEquals(ParameterModificationType.STANDARD,
                result.getModificationType());
        assertFalse(blResult);
        assertEquals(defaultValue, result.getDefaultValue());
    }

    @Test(expected = ImportException.class)
    public void updateParameterDefinition_SetWrongDefaultValue()
            throws Exception {
        // given
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.ONE_TIME);
        techProduct.setProducts(null);
        // when
        parameterParserSpy.updateParameterDefinition(attributeID1,
                ParameterValueType.LONG, "true", "0", "1", strMaxValue, "true",
                "STANDARD");
        // then
        ParameterDefinition result = parameterParserSpy.getParamDef();
        boolean blResult = parameterParserSpy.isCreateAction();
        assertEquals(ParameterModificationType.STANDARD,
                result.getModificationType());
        assertFalse(blResult);
        assertNull(result.getDefaultValue());
    }

    @Test
    public void updateParameterDefinition_SetDefaultValueOK() throws Exception {
        // given
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.ONE_TIME);
        techProduct.setProducts(null);
        // when
        parameterParserSpy.updateParameterDefinition(attributeID1,
                ParameterValueType.LONG, "true", "", "2", "124", "true", null);
        // then
        ParameterDefinition result = parameterParserSpy.getParamDef();
        boolean blResult = parameterParserSpy.isCreateAction();
        assertEquals(ParameterModificationType.STANDARD,
                result.getModificationType());
        assertFalse(blResult);
    }

    @Test
    public void updateParameterDefinition_SetSameDefaultValueForEnumeration()
            throws Exception {
        // given
        initParameterDef(false, true, defaultValue, null, null,
                ParameterModificationType.ONE_TIME);
        techProduct.setProducts(null);
        // when
        parameterParserSpy.updateParameterDefinition(attributeID1,
                ParameterValueType.ENUMERATION, "true", defaultValue, null,
                null, "true", null);
        // then
        String result = parameterParserSpy.getTempDefaultValueForEnumeration();
        assertEquals(defaultValue, result);
    }

    @Test(expected = ImportException.class)
    public void updateParameterDefinition_SetDefaultValueFail()
            throws Exception {
        // given
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.STANDARD);
        products.add(new Product());
        techProduct.setProducts(products);
        // when
        parameterParserSpy.updateParameterDefinition(attributeID1,
                ParameterValueType.STRING, "true", null, "1", strMaxValue,
                "false", "STANDARD");
    }

    @Test(expected = UpdateConstraintException.class)
    public void updateParameterDefinition_ParameterListCheck() throws Exception {
        // given
        initParameterDef(false, true, defaultValue, maxValue, minValue,
                ParameterModificationType.ONE_TIME);
        ParameterSet parameterSet = new ParameterSet();
        parameterSet.setProduct(productMock);
        param.setParameterSet(parameterSet);

        parameters.add(param);
        parameterDef.setParameters(parameters);

        parameterParserSpy.setParamDef(parameterDef);
        techProduct.setProducts(null);
        // when
        parameterParserSpy.updateParameterDefinition(attributeID1,
                ParameterValueType.STRING, "true", defaultValue, "2",
                strMaxValue, "true", "STANDARD");
    }

    @Test
    public void isNewValue_oldValLongNull() {
        // when
        boolean result = parameterParserSpy.isNewValue(null, minValue);
        // then
        assertTrue(result);
    }

    @Test
    public void isNewValue_newValLongValue() {
        // when
        boolean result = parameterParserSpy.isNewValue(strMaxValue, maxValue);
        // then
        assertTrue(result);
    }

    @Test
    public void isNewValue_newValStringValue() {
        // when
        boolean result = parameterParserSpy
                .isNewValue(strMaxValue, strMaxValue);
        // then
        assertFalse(result);
    }

    @Test
    public void isNewValue_nullStringValue() {
        // when
        boolean result = parameterParserSpy.isNewValue(null, strMaxValue);
        // then
        assertTrue(result);
    }

    @Test
    public void getTechProductBusinessKey_null() {
        // given
        parameterParserSpy.setTechProduct(null);

        // when
        String result = parameterParserSpy.getTechProductBusinessKey();

        // then
        assertEquals("", result);
    }

    @Test
    public void getTechProductBusinessKey() {
        // given
        parameterParserSpy.setTechProduct(techProduct);

        // when
        String result = parameterParserSpy.getTechProductBusinessKey();

        // then
        assertEquals("TS-1", result);
    }

    @Test
    public void setDefaultValue_TypeENUMERATION() throws Exception {
        // when
        parameterParserSpy.setDefaultValue(defaultValue, strMinValue,
                strMaxValue, ParameterValueType.ENUMERATION);
        verify(parameterParserSpy, times(1)).setDefaultValueForNonDigitalTpye(
                strCaptor.capture(), any(ParameterValueType.class));
        assertEquals(defaultValue, strCaptor.getValue());
    }

    @Test
    public void setDefaultValue_TypeBOOLEAN() throws Exception {
        // when
        parameterParserSpy.setDefaultValue(defaultValue, strMinValue,
                strMaxValue, ParameterValueType.BOOLEAN);
        // then
        verify(parameterParserSpy, times(1)).setDefaultValueForNonDigitalTpye(
                strCaptor.capture(), any(ParameterValueType.class));
        assertEquals(defaultValue, strCaptor.getValue());
    }

    @Test
    public void setDefaultValue_TypeDURATION() throws Exception {
        // when
        parameterParserSpy.setDefaultValue(defaultValue, strMinValue,
                strMaxValue, ParameterValueType.DURATION);
        // then
        verify(parameterParserSpy, times(1)).setDefaultValueForNonDigitalTpye(
                strCaptor.capture(), any(ParameterValueType.class));
        assertEquals(defaultValue, strCaptor.getValue());
    }

    @Test
    public void setDefaultValue_TypeSTRING() throws Exception {
        // when
        parameterParserSpy.setDefaultValue(defaultValue, strMinValue,
                strMaxValue, ParameterValueType.STRING);
        // then
        verify(parameterParserSpy, times(1)).setDefaultValueForNonDigitalTpye(
                strCaptor.capture(), any(ParameterValueType.class));
        assertEquals(defaultValue, strCaptor.getValue());
    }

    @Test
    public void setDefaultValue_TypeLONG() throws Exception {
        // when
        parameterParserSpy.setDefaultValue(defaultValue, strMinValue,
                strMaxValue, ParameterValueType.LONG);
        // then
        verify(parameterParserSpy, times(1)).setDefaultValueForDigitalTpye(
                strCaptor.capture(), anyString(), anyString());
        assertEquals(defaultValue, strCaptor.getValue());
    }

    @Test(expected = ImportException.class)
    public void isDefaultValueRequired_ImportException() throws Exception {
        // given
        parameterDef.setConfigurable(false);
        // when
        parameterParserSpy.isDefaultValueRequired("");
    }

    @Test
    public void isDefaultValueRequired_OK() throws Exception {
        // when
        parameterParserSpy.isDefaultValueRequired("");
    }

    @Test
    public void setDefaultValueForNonDigitalTpye_ForNullValueOfENUMERATION()
            throws Exception {
        // when
        parameterParserSpy.setDefaultValueForNonDigitalTpye("",
                ParameterValueType.ENUMERATION);
        // then
        assertNull(parameterParserSpy.getTempDefaultValueForEnumeration());
    }

    @Test
    public void setDefaultValueForNonDigitalTpye_ForENUMERATION()
            throws Exception {
        // when
        parameterParserSpy.setDefaultValueForNonDigitalTpye(defaultValue,
                ParameterValueType.ENUMERATION);
        // then
        assertEquals(defaultValue,
                parameterParserSpy.getTempDefaultValueForEnumeration());
    }

    @Test
    public void setDefaultValueForNonDigitalTpye_ForNullValueOfSTRING()
            throws Exception {
        // when
        parameterParserSpy.setDefaultValueForNonDigitalTpye("",
                ParameterValueType.STRING);
        // then
        assertNull(parameterParserSpy.getParamDef().getDefaultValue());
    }

    @Test
    public void setDefaultValueForNonDigitalTpye_ForSTRING() throws Exception {
        // when
        parameterParserSpy.setDefaultValueForNonDigitalTpye(defaultValue,
                ParameterValueType.STRING);
        // then
        assertEquals(defaultValue, parameterParserSpy.getParamDef()
                .getDefaultValue());
    }

    @Test
    public void setDefaultValueForDigitalTpye_ForBlankValue() throws Exception {
        // when
        parameterParserSpy.setDefaultValueForDigitalTpye("", strMinValue,
                strMaxValue);
        // then
        assertNull(parameterParserSpy.getParamDef().getDefaultValue());
    }

    @Test
    public void setDefaultValueForDigitalTpye_ForBlankMinAndMaxValue()
            throws Exception {
        // when
        parameterParserSpy.setDefaultValueForDigitalTpye(defaultValue, null,
                null);
        // then
        assertEquals(defaultValue, parameterParserSpy.getParamDef()
                .getDefaultValue());
    }

    @Test(expected = ImportException.class)
    public void setDefaultValueForDigitalTpye_OverThanMax() throws Exception {
        // when
        parameterParserSpy.setDefaultValueForDigitalTpye(defaultValue,
                strMinValue, "11");
    }

    @Test(expected = ImportException.class)
    public void setDefaultValueForDigitalTpye_LessThanMax() throws Exception {
        // when
        parameterParserSpy.setDefaultValueForDigitalTpye(defaultValue, "14",
                strMaxValue);
    }

    @Test
    public void setDefaultValueForDigitalTpye_OK() throws Exception {
        // when
        parameterParserSpy.setDefaultValueForDigitalTpye(defaultValue,
                strMinValue, strMaxValue);
        // then
        assertEquals(defaultValue, parameterParserSpy.getParamDef()
                .getDefaultValue());
    }

    private void initParameterDef(boolean config, boolean mandatory,
            String defaultValue, Long max, Long min,
            ParameterModificationType type) {
        parameterDef.setConfigurable(config);
        parameterDef.setMandatory(mandatory);
        parameterDef.setDefaultValue(defaultValue);
        parameterDef.setMaximumValue(max);
        parameterDef.setMinimumValue(min);
        parameterDef.setModificationType(type);
        parameterDef.getDataContainer().setConfigurable(false);
        parameterDef.isConfigurable();
        parameterParserSpy.setParamDef(parameterDef);
    }

}
