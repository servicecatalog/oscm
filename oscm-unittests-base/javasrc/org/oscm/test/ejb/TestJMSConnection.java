/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 21.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * JMS connection stub for the test environment.
 * 
 * @author Mike J&auml;ger
 */
public class TestJMSConnection implements Connection {

    public void close() throws JMSException {
    }

    public ConnectionConsumer createConnectionConsumer(Destination arg0,
            String arg1, ServerSessionPool arg2, int arg3) throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String s, String s1,
        ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }

    public ConnectionConsumer createDurableConnectionConsumer(Topic arg0,
            String arg1, String arg2, ServerSessionPool arg3, int arg4)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String s, String s1,
        ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }

    public Session createSession(boolean arg0, int arg1) throws JMSException {
        return new TestJMSSession();
    }

    @Override
    public Session createSession(int i) throws JMSException {
        return null;
    }

    @Override
    public Session createSession() throws JMSException {
        return null;
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

}
