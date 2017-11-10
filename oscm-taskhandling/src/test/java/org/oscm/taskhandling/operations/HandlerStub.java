/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: Nov 10, 2011                                                      
 *                                                                              
 *  Completion Time: Creation Date: Nov 10, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import org.oscm.taskhandling.payloads.PayloadStub;
import org.oscm.taskhandling.payloads.TaskPayload;

public class HandlerStub extends TaskHandler {

    private PayloadStub payload;

    public void execute() throws Exception {
        payload.setExecuted(true);
        if (!payload.isExecuteCauseException()) {
            payload.setExecutedSuccessfully(true);
        } else {
            throw new Exception();
        }
    }

    public void handleError(Exception cause) throws Exception {
        payload.setErrorHandled(true);
        if (!payload.isHandleErrorCauseException()) {
            payload.setHandledErrorSuccessfully(true);
        } else {
            throw new Exception();
        }
    }

    @Override
    void setPayload(TaskPayload payload) {
        this.payload = (PayloadStub) payload;
    }

    public PayloadStub getPayload() {
        return payload;
    }
}
