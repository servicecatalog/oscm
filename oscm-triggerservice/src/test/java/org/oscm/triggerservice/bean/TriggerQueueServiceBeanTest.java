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

package org.oscm.triggerservice.bean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessParameter;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.triggerservice.local.TriggerMessage;

import javax.jms.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TriggerQueueServiceBeanTest {

    private TriggerQueueServiceBean bean;
    private ArrayList<Object> storedObjects;
    private Session sessionMock;
    private ObjectMessage objectMessageMock;
    private Object storedObjectMessage;
    private Connection connectionMock;

    @Before
    public void setUp() throws Exception {
        storedObjects = new ArrayList<Object>();

        Queue queueMock = mock(Queue.class);
        sessionMock = mock(Session.class);

        MessageProducer producerMock = mock(MessageProducer.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                storedObjects.add(args[0]);
                return null;
            }
        }).when(producerMock).send(any(ObjectMessage.class));

        objectMessageMock = mock(ObjectMessage.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                storedObjectMessage = args[0];
                return null;
            }
        }).when(objectMessageMock).setObject(any(Serializable.class));

        when(sessionMock.createProducer(queueMock)).thenReturn(producerMock);
        when(sessionMock.createObjectMessage()).thenReturn(objectMessageMock);

        connectionMock = mock(Connection.class);
        when(connectionMock.createSession(anyBoolean(), anyInt())).thenReturn(
                sessionMock);

        QueueBrowser queueBrowserMock = mock(QueueBrowser.class);
        when(sessionMock.createBrowser(any(Queue.class))).thenReturn(
                queueBrowserMock);

        when(queueBrowserMock.getEnumeration()).thenReturn(new Enumeration() {
            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public Object nextElement() {
                return null;
            }
        });

        ConnectionFactory qFactoryMock = mock(ConnectionFactory.class);
        when(qFactoryMock.createConnection()).thenReturn(connectionMock);

        bean = spy(new TriggerQueueServiceBean());
        bean.qFactory = qFactoryMock;
        bean.queue = queueMock;
        bean.dm = mock(DataService.class);

    }

    @Test
    public void sendMessagesIfRequiredTest() throws JMSException, NonUniqueBusinessKeyException {
        List<TriggerMessage> messages = new ArrayList<>();
        TriggerMessage message = new TriggerMessage();
        message.setReceiverOrgs(Collections.<Organization>emptyList());
        List<TriggerProcessParameter> params = new ArrayList<>();
        TriggerProcessParameter param = new TriggerProcessParameter();
        TriggerProcess tp = new TriggerProcess();
        tp.setState(TriggerProcessStatus.CANCELLED);
        param.setTriggerProcess(tp);
        params.add(param);
        message.setParams(params);
        messages.add(message);
        PlatformUser user = new PlatformUser();

        bean.sendMessagesIfRequired(messages, user);

        assertEquals(1, storedObjects.size());
    }

    @Test
    public void sendMessagesIfRequiredTestBigPortion() throws JMSException, NonUniqueBusinessKeyException {
        List<TriggerMessage> messages = new ArrayList<>();
        TriggerMessage message = new TriggerMessage();
        message.setReceiverOrgs(Collections.<Organization>emptyList());
        List<TriggerProcessParameter> params = new ArrayList<>();
        TriggerProcessParameter param = new TriggerProcessParameter();
        TriggerProcess tp = new TriggerProcess();
        tp.setState(TriggerProcessStatus.CANCELLED);
        param.setTriggerProcess(tp);
        params.add(param);
        message.setParams(params);
        int msgStack = 1001;
        for (int i = 0; i < msgStack; i++) {
            messages.add(message);
        }
        PlatformUser user = new PlatformUser();

        bean.sendMessagesIfRequired(messages, user);

        assertEquals(msgStack, storedObjects.size());
    }
}
