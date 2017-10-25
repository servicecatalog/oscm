/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2010-06-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the statuses of trigger processing.
 * 
 */
public enum TriggerProcessStatus {

    /**
     * The trigger processing was just initiated; related tasks are waiting to
     * be handled.
     */
    INITIAL,

    /**
     * A notification was sent; the processing has been suspended until an
     * approval arrives.
     */
    WAITING_FOR_APPROVAL,

    /**
     * The processing was approved by an external authority.
     */
    APPROVED,

    /**
     * The processing was canceled by an external authority.
     */
    CANCELLED,

    /**
     * The external system did not accept the action.
     */
    ERROR,

    /**
     * The processing failed during its approval due to a problem in an internal
     * operation.
     */
    FAILED,

    /**
     * The processing was rejected by an external authority.
     */
    REJECTED,

    /**
     * A non-suspending trigger process was completed successfully. The client
     * was notified, approval is not required.
     */
    NOTIFIED;

}
