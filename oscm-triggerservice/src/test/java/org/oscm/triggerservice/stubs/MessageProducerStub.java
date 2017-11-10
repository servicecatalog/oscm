/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.stubs;

import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

public class MessageProducerStub implements MessageProducer {

    boolean isMessageSent = false;

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

    public void send(Message arg0) throws JMSException {
        isMessageSent = true;
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

    public boolean isMessageSent() {
        return isMessageSent;
    }

    @Override
    public long getDeliveryDelay() throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void send(Message arg0, CompletionListener arg1)
            throws JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(Destination arg0, Message arg1, CompletionListener arg2)
            throws JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(Message arg0, int arg1, int arg2, long arg3,
            CompletionListener arg4) throws JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void send(Destination arg0, Message arg1, int arg2, int arg3,
            long arg4, CompletionListener arg5) throws JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDeliveryDelay(long arg0) throws JMSException {
        // TODO Auto-generated method stub
        
    }

}
