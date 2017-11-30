/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.OperationParameterType;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
public class OperationParameterRowTest {

    private OperationParameterRow row;

    private VOServiceOperationParameter parameter;
    private VOTechnicalServiceOperation operation;

    @Before
    public void setup() {
        operation = new VOTechnicalServiceOperation();
        operation.setOperationId("operationId");
        operation.setOperationDescription("operationDescription");
        operation.setOperationName("operationName");

        parameter = new VOServiceOperationParameter();
        parameter.setMandatory(true);
        parameter.setParameterId("parameterId");
        parameter.setParameterName("parameterName");
        parameter.setType(OperationParameterType.REQUEST_SELECT);

        operation.setOperationParameters(Arrays.asList(parameter));

        row = new OperationParameterRow(operation, parameter);
    }

    @Test
    public void getOperationId() {
        String id = row.getOperationId();

        assertNull(id);
    }

    @Test
    public void getParameterId() {
        String id = row.getParameterId();

        assertEquals(parameter.getParameterId(), id);
    }

    @Test(expected = NullPointerException.class)
    public void constructOperationRow() {
        new OperationParameterRow(operation, null);
    }

    @Test
    public void getDisplayName() {
        String name = row.getDisplayName();

        assertEquals(parameter.getParameterName(), name);
    }

    @Test
    public void getOperationDescription() {
        String desc = row.getOperationDescription();

        assertNull(desc);
    }
}
