/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 10, 2011                                                      
 *                                                                              
 *  Completion Time: Creation Date: Nov 10, 2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.operations.HandlerStub;
import org.oscm.taskhandling.payloads.PayloadStub;
import org.oscm.internal.types.exception.IllegalArgumentException;

public class TaskListenerTest {

    TaskListener listener;

    @Before
    public void setup() {
        listener = new TaskListener();
        listener.ds = mock(DataService.class);
    }

    @Test
    public void onMessage() throws Exception {
        // given
        PayloadStub payload = new PayloadStub();
        TaskMessage taskMessage = new TaskMessage(HandlerStub.class, payload);

        ObjectMessage messageMock = mock(ObjectMessage.class);
        when(messageMock.getObject()).thenReturn(taskMessage);

        // when
        listener.onMessage(messageMock);

        // then
        assertTrue(payload.isExecutedSuccessfully());
    }

    @Test
    public void onMessageErrorHandling() throws Exception {
        // given
        PayloadStub payload = new PayloadStub();
        payload.setExecuteCauseException(true);
        TaskMessage taskMessage = new TaskMessage(HandlerStub.class, payload);

        ObjectMessage messageMock = mock(ObjectMessage.class);
        when(messageMock.getObject()).thenReturn(taskMessage);

        // when
        listener.onMessage(messageMock);

        // then
        assertTrue(payload.isHandledErrorSuccessfully());
    }

    @Test
    public void onMessageErrorHandlingFailed() throws Exception {
        // given
        PayloadStub payload = new PayloadStub();
        payload.setExecuteCauseException(true);
        payload.setHandleErrorCauseException(true);
        TaskMessage taskMessage = new TaskMessage(HandlerStub.class, payload);

        ObjectMessage messageMock = mock(ObjectMessage.class);
        when(messageMock.getObject()).thenReturn(taskMessage);

        // when
        listener.onMessage(messageMock);

        // then
        assertTrue(payload.isExecuted());
        assertFalse(payload.isExecutedSuccessfully());
        assertTrue(payload.isErrorHandled());
        assertFalse(payload.isHandledErrorSuccessfully());
    }

    @Test
    public void onMessageInvalidMessageInstance() throws Exception {
        // given
        listener = spy(listener);
        Message messageMock = mock(TextMessage.class);

        // when
        listener.onMessage(messageMock);

        // then
        verify(listener, times(1)).logTaskMessageInstanceError(eq(messageMock));
    }

    @Test
    public void onMessageInvalidMessageObjectInstance() throws Exception {
        // given
        listener = spy(listener);
        Serializable wrongMessage = mock(Serializable.class);

        ObjectMessage messageMock = mock(ObjectMessage.class);
        when(messageMock.getObject()).thenReturn(wrongMessage);

        // when
        listener.onMessage(messageMock);

        // then
        verify(listener, times(1)).logIllegalArgumentExceptionError(
                any(IllegalArgumentException.class));

    }

}
