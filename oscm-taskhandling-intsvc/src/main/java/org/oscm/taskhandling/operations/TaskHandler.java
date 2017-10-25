/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 11, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 11, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import org.oscm.taskhandling.facade.ServiceFacade;
import org.oscm.taskhandling.payloads.TaskPayload;

/**
 * Abstract class of implementation of task handling operation.
 * 
 * @author tokoda
 * 
 */
public abstract class TaskHandler implements TaskHandlerInterface {

    ServiceFacade serviceFacade;

    final void setServiceFacade(ServiceFacade serviceFacade) {
        this.serviceFacade = serviceFacade;
    }

    /**
     * Sets payload object to handler with casting payload type for each handler
     * 
     * @param payload
     */
    abstract void setPayload(TaskPayload payload);

}
