/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.stubs;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.oscm.test.stubs.ObjectMessageStub;

public class SessionStub implements Session {

    MessageProducerStub messageProducerStub = new MessageProducerStub();
    ObjectMessageStub objectMessage = new ObjectMessageStub();

    public void close() throws JMSException {
    }

    public void commit() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public QueueBrowser createBrowser(Queue arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public QueueBrowser createBrowser(Queue arg0, String arg1)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public BytesMessage createBytesMessage() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public MessageConsumer createConsumer(Destination arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public MessageConsumer createConsumer(Destination arg0, String arg1)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public MessageConsumer createConsumer(Destination arg0, String arg1,
            boolean arg2) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1,
            String arg2, boolean arg3) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public MapMessage createMapMessage() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public Message createMessage() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public ObjectMessage createObjectMessage() throws JMSException {
        return objectMessage;
    }

    public ObjectMessage createObjectMessage(Serializable arg0)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public MessageProducer createProducer(Destination arg0) throws JMSException {
        return messageProducerStub;
    }

    public Queue createQueue(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public StreamMessage createStreamMessage() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public TemporaryQueue createTemporaryQueue() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public TemporaryTopic createTemporaryTopic() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public TextMessage createTextMessage() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public TextMessage createTextMessage(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public Topic createTopic(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public int getAcknowledgeMode() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public MessageListener getMessageListener() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public boolean getTransacted() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void recover() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void rollback() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void run() {
        throw new UnsupportedOperationException();
    }

    public void setMessageListener(MessageListener arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void unsubscribe(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public MessageProducerStub getMessageProducerStub() {
        return messageProducerStub;
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic arg0, String arg1)
            throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic arg0, String arg1,
            String arg2, boolean arg3) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic arg0, String arg1)
            throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic arg0, String arg1,
            String arg2) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic arg0, String arg1)
            throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic arg0, String arg1,
            String arg2) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

}
