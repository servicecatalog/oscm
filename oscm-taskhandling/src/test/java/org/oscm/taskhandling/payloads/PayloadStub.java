/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Nov 10, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 10, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.taskhandling.payloads;

public class PayloadStub implements TaskPayload {

    private static final long serialVersionUID = -7488865353102367726L;

    private boolean executed = false;

    private boolean executedSuccessfully = false;

    private boolean errorHandled = false;

    private boolean handledErrorSuccessfully = false;

    private boolean executeCauseException = false;

    private boolean handleErrorCauseException = false;

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public boolean isExecutedSuccessfully() {
        return executedSuccessfully;
    }

    public void setExecutedSuccessfully(boolean executedSuccessfully) {
        this.executedSuccessfully = executedSuccessfully;
    }

    public boolean isErrorHandled() {
        return errorHandled;
    }

    public void setErrorHandled(boolean errorHandled) {
        this.errorHandled = errorHandled;
    }

    public boolean isHandledErrorSuccessfully() {
        return handledErrorSuccessfully;
    }

    public void setHandledErrorSuccessfully(boolean handledErrorSuccessfully) {
        this.handledErrorSuccessfully = handledErrorSuccessfully;
    }

    public boolean isExecuteCauseException() {
        return executeCauseException;
    }

    public void setExecuteCauseException(boolean executeCauseException) {
        this.executeCauseException = executeCauseException;
    }

    public boolean isHandleErrorCauseException() {
        return handleErrorCauseException;
    }

    public void setHandleErrorCauseException(boolean handleErrorCauseException) {
        this.handleErrorCauseException = handleErrorCauseException;
    }

    @Override
    public String getInfo() {
        return null;
    }

}
