/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 10, 2011                                                      
 *                                                                              
 *  Completion Time: Creation Date: Nov 10, 2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.dataservice.local.DataService;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.operations.HandlerStub;
import org.oscm.taskhandling.operations.SendMailHandler;
import org.oscm.taskhandling.operations.UpdateUserHandler;
import org.oscm.taskhandling.payloads.PayloadStub;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.taskhandling.payloads.UpdateUserPayload;
import org.oscm.types.enumtypes.LogMessageIdentifier;

@SuppressWarnings("rawtypes")
public class TaskQueueServiceBeanTest {

    @Test
    public void dummy() {

    }
//
//    private TaskQueueServiceBean tqs;
//    private List<Object> storedObjects;
//
//    Connection connectionMock;
//    Session sessionMock;
//
//    ObjectMessage objectMessageMock;
//    private Object storedObjectMessage;
//
//    @Before
//    public void setUp() throws Exception {
//
//        storedObjects = new ArrayList<Object>();
//
//        Queue queueMock = mock(Queue.class);
//        sessionMock = mock(Session.class);
//
//        MessageProducer producerMock = mock(MessageProducer.class);
//        doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) {
//                Object[] args = invocation.getArguments();
//                storedObjects.add(args[0]);
//                return null;
//            }
//        }).when(producerMock).send(any(ObjectMessage.class));
//
//        objectMessageMock = mock(ObjectMessage.class);
//        doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) {
//                Object[] args = invocation.getArguments();
//                storedObjectMessage = args[0];
//                return null;
//            }
//        }).when(objectMessageMock).setObject(any(Serializable.class));
//
//        when(sessionMock.createProducer(queueMock)).thenReturn(producerMock);
//        when(sessionMock.createObjectMessage()).thenReturn(objectMessageMock);
//
//        connectionMock = mock(Connection.class);
//        when(connectionMock.createSession(anyBoolean(), anyInt())).thenReturn(
//                sessionMock);
//
//        QueueBrowser queueBrowserMock = mock(QueueBrowser.class);
//        when(sessionMock.createBrowser(any(Queue.class))).thenReturn(
//                queueBrowserMock);
//
//        when(queueBrowserMock.getEnumeration()).thenReturn(new Enumeration() {
//            @Override
//            public boolean hasMoreElements() {
//                return false;
//            }
//
//            @Override
//            public Object nextElement() {
//                return null;
//            }
//        });
//
//        ConnectionFactory qFactoryMock = mock(ConnectionFactory.class);
//        when(qFactoryMock.createConnection()).thenReturn(connectionMock);
//
//        tqs = new TaskQueueServiceBean();
//        tqs.qFactory = qFactoryMock;
//        tqs.queue = queueMock;
//        tqs.dm = mock(DataService.class);
//
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesNoFactoryAndNoQueue() throws Exception {
//        tqs.qFactory = null;
//        tqs.queue = null;
//        tqs.sendAllMessages(null);
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesNoFactory() throws Exception {
//        tqs.qFactory = null;
//        tqs.sendAllMessages(null);
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesNoQueue() throws Exception {
//        tqs.queue = null;
//        tqs.sendAllMessages(null);
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesNullMessages() throws Exception {
//        tqs.sendAllMessages(null);
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesNoHandlerClass() throws Exception {
//        PayloadStub payload = new PayloadStub();
//        TaskMessage message = new TaskMessage(null, payload);
//        List<TaskMessage> messages = new ArrayList<TaskMessage>();
//        messages.add(message);
//        tqs.sendAllMessages(messages);
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesNoPayload() throws Exception {
//        TaskMessage message = new TaskMessage(UpdateUserHandler.class, null);
//        List<TaskMessage> messages = new ArrayList<TaskMessage>();
//        messages.add(message);
//        tqs.sendAllMessages(messages);
//    }
//
//    @Test
//    public void testSendAllMessages() throws Exception {
//
//        PayloadStub payload = new PayloadStub();
//        TaskMessage correctMessage = new TaskMessage(HandlerStub.class, payload);
//
//        List<TaskMessage> messages = new ArrayList<TaskMessage>();
//        messages.add(correctMessage);
//        tqs.sendAllMessages(messages);
//
//        assertEquals(1, storedObjects.size());
//        assertEquals(objectMessageMock, storedObjects.get(0));
//        assertEquals(correctMessage, storedObjectMessage);
//    }
//
//    @Test
//    public void testSendAllMessagesBigPortion() throws Exception {
//
//        PayloadStub payload = new PayloadStub();
//        TaskMessage correctMessage = new TaskMessage(HandlerStub.class, payload);
//
//        List<TaskMessage> messages = new ArrayList<>();
//        int msgStack = 1001;
//        for (int i = 0; i < msgStack; i++) {
//            messages.add(correctMessage);
//        }
//        tqs.sendAllMessages(messages);
//
//        assertEquals(msgStack, storedObjects.size());
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesJMSException() throws Exception {
//        doThrow(new JMSException(null)).when(connectionMock).createSession(
//                anyBoolean(), anyInt());
//
//        PayloadStub payload = new PayloadStub();
//        TaskMessage correctMessage = new TaskMessage(HandlerStub.class, payload);
//
//        List<TaskMessage> messages = new ArrayList<TaskMessage>();
//        messages.add(correctMessage);
//        tqs.sendAllMessages(messages);
//    }
//
//    @Test(expected = SaaSSystemException.class)
//    public void testSendAllMessagesJMSExceptionInterupted() throws Exception {
//
//        Log4jLogger oldLogger = TaskQueueServiceBean.logger;
//        Log4jLogger loggerSpy = spy(LoggerFactory
//                .getLogger(TaskQueueServiceBean.class));
//
//        MessageProducer producerMock = mock(MessageProducer.class);
//        doNothing().doThrow(new JMSException(null)).when(producerMock)
//                .send(any(ObjectMessage.class));
//
//        doReturn(producerMock).when(sessionMock).createProducer(
//                any(Queue.class));
//
//        SendMailPayload smPayload = mock(SendMailPayload.class);
//        UpdateUserPayload uuPayload = mock(UpdateUserPayload.class);
//
//        TaskMessage correctMessage = new TaskMessage(SendMailHandler.class,
//                smPayload);
//        TaskMessage errorMessage = new TaskMessage(UpdateUserHandler.class,
//                uuPayload);
//
//        List<TaskMessage> messages = new ArrayList<TaskMessage>();
//        messages.add(correctMessage);
//        messages.add(errorMessage);
//        try {
//            TaskQueueServiceBean.logger = loggerSpy;
//            tqs.sendAllMessages(messages);
//        } finally {
//            verify(loggerSpy, atLeastOnce())
//                    .logError(
//                            Matchers.eq(LogMessageIdentifier.ERROR_SEND_MESSAGE_TO_JMS_QUEUE_FAILED_DETAILS),
//                            Matchers.eq("1"),
//                            Matchers.eq("2"),
//                            Matchers.matches(".*Successful.*SendMailHandler.*SendMailPayload.*Failed.*UpdateUserHandler.*UpdateUserPayload.*"));
//            TaskQueueServiceBean.logger = oldLogger;
//        }
//    }

}
