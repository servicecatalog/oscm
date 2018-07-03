/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  OpenStack controller implementation for the 
 *  Asynchronous Provisioning Platform (APP)
 *       
 *  Creation Date: 2013-11-29                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.openstack.data;

/**
 * Enumeration of the possible internal statuses of provisioning operations that
 * may be set by the controller and the status dispatcher.
 */
public enum FlowState {

    /**
     * Request to create a new project tenant.
     */
    CREATE_PROJECT, 
    
    /**
     * Request to delete project tenant.
     */
    DELETE_PROJECT, 
    
    /**
     * Request to update quota of project tenant.
     */
    UPDATE_PROJECT,

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
     * Activation of stack instance is in progress
     */
    ACTIVATING,

    /**
     * The deactivation of an application instance was requested.
     */
    DEACTIVATION_REQUESTED,

    /**
     * Deactivation of stack instance is in progress
     */
    DEACTIVATING,

    /**
     * The start of an application instance was requested.
     */
    START_REQUESTED,

    /**
     * Start of stack instance is in progress
     */
    STARTING,

    /**
     * The stop of an application instance was requested.
     */
    STOP_REQUESTED,

    /**
     * Stop of stack instance is in progress
     */
    STOPPING,

    /**
     * The application instance is currently being executed - waiting for OK
     * state.
     */
    CREATING_STACK,

    /**
     * The application instance is currently being modified.
     */
    UPDATING,

    /**
     * The creation or modification of an application instance failed.
     */
    FAILED,

    /**
     * The instance currently being handled in a manual process.
     */
    MANUAL,

    /**
     * The creation or modification of an application instance has been
     * completed successfully.
     */
    FINISHED,

    /**
     * The application instance is currently being deleted.
     */
    DELETING_STACK,

    /**
     * The application instance has been destroyed.
     */
    DESTROYED;
}
