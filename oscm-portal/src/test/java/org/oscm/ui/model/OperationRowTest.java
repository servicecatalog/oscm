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

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.vo.VOTechnicalServiceOperation;

/**
 * @author weiser
 * 
 */
public class OperationRowTest {

    private OperationRow row;

    private VOTechnicalServiceOperation operation;

    @Before
    public void setup() {
        operation = new VOTechnicalServiceOperation();
        operation.setOperationId("operationId");
        operation.setOperationDescription("operationDescription");
        operation.setOperationName("operationName");

        row = new OperationRow(operation);
    }

    @Test
    public void getOperationId() {
        String id = row.getOperationId();

        assertEquals(operation.getOperationId(), id);
    }

    @Test
    public void getParameterId() {
        String id = row.getParameterId();

        assertNull(id);
    }

    @Test(expected = NullPointerException.class)
    public void constructOperationRow() {
        new OperationRow(null);
    }

    @Test
    public void getDisplayName() {
        String name = row.getDisplayName();

        assertEquals(operation.getOperationName(), name);
    }

    @Test
    public void getOperationDescription() {
        String desc = row.getOperationDescription();

        assertEquals(operation.getOperationDescription(), desc);
    }
}
