/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Sample controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2012-09-06                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.sample.controller;

/**
 * Enumeration of the possible internal statuses of provisioning operations that
 * may be set by the controller and the status dispatcher.
 */
public enum Status {
    /**
     * The creation of a new application instance was started.
     */
    CREATION_REQUESTED,

    /**
     * A modification of an application instance was started.
     */
    MODIFICATION_REQUESTED,

    /**
     * The deletion of an application instance was started.
     */
    DELETION_REQUESTED,

    /**
     * The activation of an application instance was requested.
     */
    ACTIVATION_REQUESTED,

    /**
     * The deactivation of an application instance was requested.
     */
    DEACTIVATION_REQUESTED,

    /**
     * The application instance is currently being created (step 1).
     */
    CREATION_STEP1,

    /**
     * The application instance is currently being created (step 2).
     */
    CREATION_STEP2,

    /**
     * The application instance is currently being modified.
     */
    UPDATING,

    /**
     * The creation or modification of an application instance failed.
     */
    FAILED,

    /**
     * The creation or modification of an application instance has been
     * completed successfully.
     */
    FINISHED,

    /**
     * The application instance is currently being deleted.
     */
    DELETING,

    /**
     * The application instance has been destroyed.
     */
    DESTROYED;
}
