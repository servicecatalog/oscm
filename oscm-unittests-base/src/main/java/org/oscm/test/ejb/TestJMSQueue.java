/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 21.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * Test stub implementation for a JMS queue.
 * 
 * @author Mike J&auml;ger
 */
public class TestJMSQueue implements Queue {

    private static TestJMSQueue queue;
    private TestJMSObjectMessage objectMessage;

    private TestJMSQueue() {

    }

    public static TestJMSQueue getInstance() {
        if (queue == null) {
            queue = new TestJMSQueue();
        }
        return queue;
    }

    public String getQueueName() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void setObjectMessage(TestJMSObjectMessage objectMessage) {
        this.objectMessage = objectMessage;
    }

    public TestJMSObjectMessage getObjectMessage() {
        return objectMessage;
    }
}
