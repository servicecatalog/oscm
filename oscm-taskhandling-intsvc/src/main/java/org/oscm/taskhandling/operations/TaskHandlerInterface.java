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

package org.oscm.taskhandling.operations;

/**
 * Task interface allowing to execute the operation of the task message
 * 
 * @author tokoda
 */
public interface TaskHandlerInterface {

    /**
     * Executes the operation of the task message
     * 
     * @throws Exception
     */
    public void execute() throws Exception;

    /**
     * Handling error for the failing execution
     * 
     * @param cause
     *            Reason why the execution failed
     * @throws Exception
     */
    public void handleError(Exception cause) throws Exception;

}
