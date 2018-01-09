/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.stubs;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

public class ConnectionStub implements Connection {

    SessionStub session = new SessionStub();

    public void close() throws JMSException {
    }

    public ConnectionConsumer createConnectionConsumer(Destination arg0,
            String arg1, ServerSessionPool arg2, int arg3) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public ConnectionConsumer createDurableConnectionConsumer(Topic arg0,
            String arg1, String arg2, ServerSessionPool arg3, int arg4)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public Session createSession(boolean arg0, int arg1) throws JMSException {
        return session;
    }

    public String getClientID() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public ExceptionListener getExceptionListener() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public ConnectionMetaData getMetaData() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setClientID(String arg0) throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setExceptionListener(ExceptionListener arg0)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void start() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void stop() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public SessionStub getSession() {
        return session;
    }

    @Override
    public Session createSession() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Session createSession(int arg0) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic arg0,
            String arg1, String arg2, ServerSessionPool arg3, int arg4)
                    throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic arg0,
            String arg1, String arg2, ServerSessionPool arg3, int arg4)
                    throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

}
