/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operation.data;

import java.io.Serializable;
/**
 * Represents the result of a service operation.
 * 
 */
public class OperationResult implements Serializable {

    private static final long serialVersionUID = 8918299019350506068L;
    
    private boolean asyncExecution = false;
    private String errorMessage;

    /**
     * Returns if a service operation is executed asynchronously.
     * 
     * @return <code>true</code> if the operation is executed asynchronously,
               <code>false</code> otherwise.
     */
    public boolean isAsyncExecution() {
        return asyncExecution;
    }

    /**
     * Defines whether a service operation is executed asynchronously.
     * 
     * @param asyncExecution
     *            <code>true</code> or <code>false</code>
     */
    public void setAsyncExecution(boolean asyncExecution) {
        this.asyncExecution = asyncExecution;
    }

    /**
     * Retrieves the error message text displayed as the status message for
     * an operation.
     * 
     * @return the message text
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the text to be displayed as the error message for an operation.
     * 
     * @param errorMessage
     *            the message text
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
