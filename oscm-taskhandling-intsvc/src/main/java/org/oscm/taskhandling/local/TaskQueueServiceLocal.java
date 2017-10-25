/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 3, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 3, 2011                                        
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.local;

import java.util.List;

import javax.ejb.Local;

/**
 * Service to provide functionality to post a task message to the queue.
 * 
 * @author tokoda
 * 
 */
@Local
public interface TaskQueueServiceLocal {

    /**
     * Sends operation related messages to the JMS queue.
     * 
     * @param messages
     *            The messages to send.
     */
    public void sendAllMessages(List<TaskMessage> messages);
}
