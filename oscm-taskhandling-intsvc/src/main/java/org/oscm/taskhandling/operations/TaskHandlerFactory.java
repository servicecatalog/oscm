/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 8, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 8, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import org.oscm.taskhandling.facade.ServiceFacade;
import org.oscm.taskhandling.local.TaskMessage;

/**
 * @author tokoda
 */
public class TaskHandlerFactory {

    /**
     * Generate the instance of factory for generating handler class.
     * 
     * @return The instance of factory
     */
    public static final TaskHandlerFactory getInstance() {

        return new TaskHandlerFactory();
    }

    /**
     * Generate the instance of the handler class specified in the task message.
     * 
     * @param message
     * @param serviceFacade
     * @return The instance of the specified handler class in the message
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public TaskHandler getTaskHandler(TaskMessage message,
            ServiceFacade serviceFacade) throws InstantiationException,
            IllegalAccessException {
        TaskHandler handler = message.getInstance();
        handler.setServiceFacade(serviceFacade);
        handler.setPayload(message.getPayload());
        return handler;

    }

}
