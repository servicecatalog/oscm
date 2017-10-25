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

import java.io.Serializable;

import org.oscm.taskhandling.operations.TaskHandler;
import org.oscm.taskhandling.payloads.TaskPayload;

/**
 * Message object representing an task request.
 * 
 * @author tokoda
 */
public class TaskMessage implements Serializable {

    private static final long serialVersionUID = -412854486761462425L;

    private final Class<? extends TaskHandler> handlerClass;

    private TaskHandler instance;

    private final TaskPayload payload;

    private long execTime;

    private long currentUserKey;

    private int retry;

    private int numberOfAttempted;

    public TaskMessage(Class<? extends TaskHandler> handlerClass,
            TaskPayload payload) {
        this.handlerClass = handlerClass;
        this.payload = payload;
    }

    public TaskHandler getInstance() throws InstantiationException,
            IllegalAccessException {
        if (instance == null) {
            instance = handlerClass.newInstance();
        }
        return instance;
    }

    public Class<? extends TaskHandler> getHandlerClass() {
        return handlerClass;
    }

    public long getExecTime() {
        return execTime;
    }

    public TaskPayload getPayload() {
        return payload;
    }

    public int getRetry() {
        return retry;
    }

    public int getNumberOfAttempt() {
        return numberOfAttempted;
    }

    public void forwardNumberOfAttempt() {
        numberOfAttempted++;
    }

    public void setCurrentUserKey(long key) {
        currentUserKey = key;
    }

    public long getCurrentUserKey() {
        return currentUserKey;
    }
}
