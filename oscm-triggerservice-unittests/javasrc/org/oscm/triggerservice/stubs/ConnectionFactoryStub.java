/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.stubs;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;

public class ConnectionFactoryStub implements ConnectionFactory {

    ConnectionStub connStub = new ConnectionStub();
    boolean throwsJMSException;

    public Connection createConnection() throws JMSException {
        if (throwsJMSException) {
            throw new JMSException("test caused exception");
        }
        return connStub;
    }

    public Connection createConnection(String arg0, String arg1)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setThrowsJMSException(boolean throwsJMSException) {
        this.throwsJMSException = throwsJMSException;
    }

    @Override
    public JMSContext createContext() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JMSContext createContext(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JMSContext createContext(String arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JMSContext createContext(String arg0, String arg1, int arg2) {
        // TODO Auto-generated method stub
        return null;
    }

}
