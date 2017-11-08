/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 21.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import javax.jms.*;

/**
 * JMS message producer stub for test environment.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class TestJMSMessageProducer implements MessageProducer {
    boolean isMessageSent = false;
    FifoJMSQueue queue;

    public void close() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public int getDeliveryMode() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public Destination getDestination() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public boolean getDisableMessageID() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public boolean getDisableMessageTimestamp() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public int getPriority() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public long getTimeToLive() throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDeliveryDelay(long l) throws JMSException {

    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        return 0;
    }

    public void send(Message arg0) throws JMSException {
        isMessageSent = true;
        if (queue != null) {
            queue.add(arg0);
        }
    }

    public void send(Destination arg0, Message arg1) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void send(Message arg0, int arg1, int arg2, long arg3)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void send(Destination arg0, Message arg1, int arg2, int arg3,
            long arg4) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(Message message, CompletionListener completionListener) throws JMSException {

    }

    @Override
    public void send(Message message, int i, int i1, long l, CompletionListener completionListener)
        throws JMSException {

    }

    @Override
    public void send(Destination destination, Message message, CompletionListener completionListener)
        throws JMSException {

    }

    @Override
    public void send(Destination destination, Message message, int i, int i1, long l,
        CompletionListener completionListener) throws JMSException {

    }

    public void setDeliveryMode(int arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setDisableMessageID(boolean arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setDisableMessageTimestamp(boolean arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setPriority(int arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setTimeToLive(long arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setQueue(FifoJMSQueue queue) {
        this.queue = queue;
    }
}
