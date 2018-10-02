/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

/**
 * Enumeration of the possible internal statuses of provisioning operations that
 * may be set by the controller and the status dispatcher.
 */
public enum FlowState {

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
     * The start of an application instance was requested.
     */
    START_REQUESTED,

    /**
     * The stop of an application instance was requested.
     */
    STOP_REQUESTED,

    /**
     * The application instance is currently being executed - waiting for OK
     * state.
     */
    CREATING,

    /**
     * The application instance is currently being deleted.
     */
    DELETING,

    /**
     * The application instance is currently being modified.
     */
    UPDATING,

    /**
     * The application instance is currently being started.
     */
    STARTING,

    /**
     * The application instance is currently being stopped.
     */
    STOPPING,

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
     * The application instance has been destroyed.
     */
    DESTROYED;
}
