/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Ronny Weiser                                                    
 *                                                                              
 *  Creation Date: 16.08.2010                                                      
 *                                                                              
 *  Completion Time: 16.08.2010                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.types.enumtypes;

/**
 * The possible execution types of a service operation.
 * 
 * @author weiser
 * 
 */
public enum OperationExecutionType {

    /**
     * The call will wait until the operation is finished and the result is
     * available. Not useful for long running operations.
     */
    SYNCHRONOUS;
}
