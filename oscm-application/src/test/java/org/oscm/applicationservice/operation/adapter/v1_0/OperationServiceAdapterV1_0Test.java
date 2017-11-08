/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.09.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.applicationservice.operation.adapter.v1_0;

import static junit.framework.Assert.assertSame;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.oscm.applicationservice.operation.adapter.OperationServiceAdapterV1_0;
import org.oscm.operation.data.OperationParameter;
import org.oscm.operation.data.OperationResult;
import org.oscm.operation.intf.OperationService;

public class OperationServiceAdapterV1_0Test {
    private static final String USERID = "user";
    private static final String INSTANCEID = "instance";
    private static final String TRANSACTIONID = "transaction";
    private static final String OPERATIONID = "operation";

    private OperationServiceAdapterV1_0 adapter;
    private OperationService port;
    private OperationResult result;

    @Mock
    List<OperationParameter> params;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        adapter = new OperationServiceAdapterV1_0();
        port = mock(OperationService.class);
        result = new OperationResult();
        adapter.setOperationService(port);
    }

    @Test
    public void executeServiceOperation() {
        // given
        String message = "some return message";
        result.setErrorMessage(message);
        when(
                port.executeServiceOperation(anyString(), anyString(),
                        anyString(), anyString(),
                        anyListOf(OperationParameter.class)))
                .thenReturn(result);

        // when
        OperationResult operationResult = adapter.executeServiceOperation(
                USERID, INSTANCEID, TRANSACTIONID, OPERATIONID, params);

        // then
        assertSame(result, operationResult);

        verify(port).executeServiceOperation(same(USERID), same(INSTANCEID),
                same(TRANSACTIONID), same(OPERATIONID), same(params));
        verifyNoMoreInteractions(port);
        verifyZeroInteractions(params);
    }

    @Test
    public void getParameterValues() {
        // given
        when(port.getParameterValues(anyString(), anyString(), anyString()))
                .thenReturn(params);

        // when
        List<OperationParameter> list = adapter.getParameterValues(USERID,
                INSTANCEID, OPERATIONID);

        assertSame(params, list);
        verify(port).getParameterValues(same(USERID), same(INSTANCEID),
                same(OPERATIONID));
        verifyNoMoreInteractions(port);
        verifyZeroInteractions(params);
    }
}
