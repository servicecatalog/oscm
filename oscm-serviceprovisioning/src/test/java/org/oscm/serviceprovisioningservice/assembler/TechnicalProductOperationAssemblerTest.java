/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.test.stubs.LocalizerServiceStub;
import org.oscm.types.enumtypes.OperationParameterType;
import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
public class TechnicalProductOperationAssemblerTest {

    private static final int OPERATION_KEY = 1234;
    private static final String OPERATION_IDENTIFIER = "operationIdentifier";
    private static final String OPERATION_PARAMETER_ID = "operationParameterID";

    private TechnicalProductOperation technicalProductOperation;
    private OperationParameter parameter;

    private LocalizerFacade facade = new LocalizerFacade(
            new LocalizerServiceStub() {

                @Override
                public String getLocalizedTextFromDatabase(String localeString,
                        long objectKey, LocalizedObjectTypes objectType) {
                    return objectType.name();
                }
            }, "en");

    @Before
    public void setup() {
        technicalProductOperation = new TechnicalProductOperation();
        technicalProductOperation.setKey(OPERATION_KEY);
        technicalProductOperation.setOperationId(OPERATION_IDENTIFIER);

        parameter = new OperationParameter();
        parameter.setKey(1);
        parameter.setId(OPERATION_PARAMETER_ID);
        parameter.setMandatory(true);
        parameter.setType(OperationParameterType.REQUEST_SELECT);
        technicalProductOperation.getParameters().add(parameter);
    }

    @Test
    public void testToVOTechnicalServiceOperation_Null() {
        VOTechnicalServiceOperation operation = TechnicalProductOperationAssembler
                .toVOTechnicalServiceOperation(null, facade);
        assertNull(operation);
    }

    @Test
    public void testToVOTechnicalServiceOperation() {
        VOTechnicalServiceOperation operation = TechnicalProductOperationAssembler
                .toVOTechnicalServiceOperation(technicalProductOperation,
                        facade);
        validateVOOperation(operation);
    }

    @Test
    public void testToVOTechnicalServiceOperations_Null() {
        List<VOTechnicalServiceOperation> operations = TechnicalProductOperationAssembler
                .toVOTechnicalServiceOperations(null, facade);
        assertNull(operations);
    }

    @Test
    public void testToVOTechnicalServiceOperations_Empty() {
        List<VOTechnicalServiceOperation> operations = TechnicalProductOperationAssembler
                .toVOTechnicalServiceOperations(
                        new ArrayList<TechnicalProductOperation>(), facade);
        assertNotNull(operations);
        assertTrue(operations.isEmpty());
    }

    @Test
    public void testToVOTechnicalServiceOperations() {
        List<TechnicalProductOperation> list = new ArrayList<TechnicalProductOperation>();
        list.add(technicalProductOperation);
        list.add(technicalProductOperation);
        List<VOTechnicalServiceOperation> operations = TechnicalProductOperationAssembler
                .toVOTechnicalServiceOperations(list, facade);
        assertNotNull(operations);
        assertEquals(list.size(), operations.size());
        for (VOTechnicalServiceOperation voTechnicalServiceOperation : operations) {
            validateVOOperation(voTechnicalServiceOperation);
        }
    }

    private static final void validateVOOperation(
            VOTechnicalServiceOperation operation) {
        assertEquals(1, operation.getOperationParameters().size());
        assertEquals(OPERATION_PARAMETER_ID, operation.getOperationParameters()
                .get(0).getParameterId());
        assertEquals(OPERATION_KEY, operation.getKey());
        assertEquals(OPERATION_IDENTIFIER, operation.getOperationId());
        assertEquals(
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_DESCRIPTION
                        .name(),
                operation.getOperationDescription());
        assertEquals(
                LocalizedObjectTypes.TECHNICAL_PRODUCT_OPERATION_NAME.name(),
                operation.getOperationName());
    }
}
