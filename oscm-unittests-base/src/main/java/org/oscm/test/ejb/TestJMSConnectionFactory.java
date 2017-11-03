/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 21.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;

/**
 * The stub implementation for a JMS connection factory to be used by the tests.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class TestJMSConnectionFactory implements ConnectionFactory {

    public Connection createConnection() throws JMSException {
        return new TestJMSConnection();
    }

    public Connection createConnection(String arg0, String arg1)
            throws JMSException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMSContext createContext() {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1) {
        return null;
    }

    @Override
    public JMSContext createContext(String s, String s1, int i) {
        return null;
    }

    @Override
    public JMSContext createContext(int i) {
        return null;
    }

}
