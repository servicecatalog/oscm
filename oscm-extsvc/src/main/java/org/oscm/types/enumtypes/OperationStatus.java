/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2014-09-17                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the available status types for service operations.
 */
public enum OperationStatus {

    /**
     * The service operation is running.
     */
    RUNNING,

    /**
     * The service operation has been completed successfully.
     */
    COMPLETED,

    /**
     * An error occurred during the execution of the service operation.
     */
    ERROR

}
