/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 16.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.stubs;

import javax.jms.JMSException;
import javax.jms.Queue;

public class QueueStub implements Queue {

    public String getQueueName() throws JMSException {
        return "queueName";
    }

}
