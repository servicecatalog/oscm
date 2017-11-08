/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: July 19, 2011                                                      
 *                                                                              
 *  Completion Time: July 20, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.test.ejb;

import javax.jms.JMSException;
import javax.jms.Queue;

import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

/**
 * Simple FIFO queue implementation for test purposes.
 * 
 * @author Dirk Bernsau
 * 
 */
public class FifoJMSQueue implements Queue {

    private UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();

    public String getQueueName() throws JMSException {
        throw new UnsupportedOperationException();
    }

    public void add(Object object) {
        buffer.add(object);
    }

    public Object remove() {
        return buffer.remove();
    }

    public void clear() {
        buffer.clear();
    }
}
