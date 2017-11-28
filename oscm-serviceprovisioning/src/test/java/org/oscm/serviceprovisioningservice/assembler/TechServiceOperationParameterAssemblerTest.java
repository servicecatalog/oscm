/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-1-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.oscm.types.enumtypes.OperationParameterType.INPUT_STRING;
import static org.oscm.types.enumtypes.OperationParameterType.REQUEST_SELECT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOServiceOperationParameterValues;

/**
 * @author Yuyin
 * 
 */
public class TechServiceOperationParameterAssemblerTest {

    private static final String OPERATION_PARAMETER_ID = "operationParameterID";
    private static final String PARAMETER_NAME = "name";

    private OperationParameter parameter1;
    private LocalizerFacade facade;

    @Before
    public void setup() {
        facade = mock(LocalizerFacade.class);
        when(
                facade.getText(
                        anyLong(),
                        eq(LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_PARAMETER_NAME)))
                .thenReturn(PARAMETER_NAME);

        parameter1 = new OperationParameter();
        parameter1.setKey(1);
        parameter1.setMandatory(true);
        parameter1.setType(REQUEST_SELECT);
        parameter1.setId(OPERATION_PARAMETER_ID);
    }

    @Test
    public void toVOServiceOperationParameters_Null() {
        List<VOServiceOperationParameter> parameters = TechServiceOperationParameterAssembler
                .toVOServiceOperationParameters(null, facade);
        assertNull(parameters);
    }

    @Test
    public void toVOServiceOperationParameters_Empty() {
        List<VOServiceOperationParameter> parameters = TechServiceOperationParameterAssembler
                .toVOServiceOperationParameters(
                        new ArrayList<OperationParameter>(), facade);
        assertNotNull(parameters);
        assertTrue(parameters.isEmpty());
    }

    @Test
    public void toVOServiceOperationParameters_Ok() {
        List<OperationParameter> paramList = new ArrayList<OperationParameter>();
        paramList.add(parameter1);

        OperationParameter parameter2 = new OperationParameter();
        parameter2.setKey(2);
        parameter2.setMandatory(false);
        parameter2.setType(INPUT_STRING);
        String id2 = "different id";
        parameter2.setId(id2);
        paramList.add(parameter2);

        List<VOServiceOperationParameter> parameters = TechServiceOperationParameterAssembler
                .toVOServiceOperationParameters(paramList, facade);

        assertNotNull(parameters);
        assertEquals(paramList.size(), parameters.size());

        assertEquals(1, parameters.get(0).getKey());
        assertEquals(OPERATION_PARAMETER_ID, parameters.get(0).getParameterId());
        assertEquals(PARAMETER_NAME, parameters.get(0).getParameterName());
        assertEquals(
                org.oscm.internal.types.enumtypes.OperationParameterType.REQUEST_SELECT,
                parameters.get(0).getType());
        assertTrue(parameters.get(0).isMandatory());

        assertEquals(2, parameters.get(1).getKey());
        assertEquals(id2, parameters.get(1).getParameterId());
        assertEquals(PARAMETER_NAME, parameters.get(1).getParameterName());
        assertFalse(parameters.get(1).isMandatory());
        assertEquals(
                org.oscm.internal.types.enumtypes.OperationParameterType.INPUT_STRING,
                parameters.get(1).getType());
    }

    @Test
    public void toVOServiceOperationParameter_Null() {
        VOServiceOperationParameter result = TechServiceOperationParameterAssembler
                .toVOServiceOperationParameter(null, facade);
        assertNull(result);
    }

    @Test
    public void toVOServiceOperationParameter_OK() {
        VOServiceOperationParameter result = TechServiceOperationParameterAssembler
                .toVOServiceOperationParameter(parameter1, facade);

        assertNotNull(result);
        assertEquals(1, result.getKey());
        assertEquals(OPERATION_PARAMETER_ID, result.getParameterId());
        assertEquals(
                org.oscm.internal.types.enumtypes.OperationParameterType.REQUEST_SELECT,
                result.getType());
        assertTrue(result.isMandatory());
    }

    @Test
    public void toVOServiceOperationParameterValues_Null() {
        VOServiceOperationParameterValues result = TechServiceOperationParameterAssembler
                .toVOServiceOperationParameterValues(null, facade, null);
        assertNull(result);
    }

    @Test
    public void toVOServiceOperationParameterValues_OK() {
        List<String> values = Arrays.asList("val1", "val2");
        VOServiceOperationParameterValues result = TechServiceOperationParameterAssembler
                .toVOServiceOperationParameterValues(parameter1, facade, values);

        assertNotNull(result);
        assertEquals(1, result.getKey());
        assertEquals(OPERATION_PARAMETER_ID, result.getParameterId());
        assertEquals(
                org.oscm.internal.types.enumtypes.OperationParameterType.REQUEST_SELECT,
                result.getType());
        assertTrue(result.isMandatory());
        assertSame(values, result.getValues());
    }

}
