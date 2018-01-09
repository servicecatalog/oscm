/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * The VSERVER flow state enumeration
 */

public enum FlowState {

    /**
     * A new VSERVER instance has been registered.
     */
    VSERVER_CREATION_REQUESTED,

    /**
     * VSERVER instance modification has been requested.
     */
    VSERVER_MODIFICATION_REQUESTED,

    /**
     * A VSERVER instance shall be deleted.
     */
    VSERVER_DELETION_REQUESTED,

    /**
     * A VSERVER instance shall be activated.
     */
    VSERVER_ACTIVATION_REQUESTED,

    /**
     * A VSERVER instance shall be deactivated.
     */
    VSERVER_DEACTIVATION_REQUESTED,

    /**
     * A VSERVER instance shall be stopped.
     */
    VSERVER_STOP_FOR_DEACTIVATION,

    /**
     * modifing the VSERVER instance.
     */
    VSERVER_UPDATING,

    /**
     * modifed the VSERVER instance.
     */
    VSERVER_UPDATED,

    /**
     * The sate representing when creating the server in VSYS.
     */
    VSERVER_CREATING,

    /**
     * The sate representing a created but not running VServer.
     */
    VSERVER_CREATED,

    /**
     * Represents the state when the VSERVER is created and starting up.
     */
    VSERVER_STARTING,

    /**
     * The state representing a created and running VSERVER but without any
     * other resource (VSERVER, IP) or the application.
     */
    VSERVER_STARTED,

    /**
     * Representing the state when starting the VSERVER(s).
     */
    VSERVERS_STARTING,

    /**
     * VSERVER(s) are stopped.
     */
    VSERVER_STOPPED_FOR_MODIFICATION,

    /**
     * VSERVER is currently stopping to be modified.
     */
    VSERVER_STOPPING_FOR_MODIFICATION,

    /**
     * Representing the state when destroying.
     */
    VSERVER_DELETING,

    /**
     * Get guest infos (e.g. default password,host name,IP ...)
     */
    VSERVER_RETRIEVEGUEST,

    /**
     * VSERVER instance is currently stopped for deletion.
     */
    VSERVER_STOPPED_FOR_DELETION,

    /**
     * A new VSDISK instance has been requested.
     */
    VSDISK_CREATION_REQUESTED,

    /**
     * The start of a server within the virtual system has been requested.
     */
    VSERVER_START_REQUESTED,

    /**
     * The stop of a server within the virtual system has been requested.
     */
    VSERVER_STOP_REQUESTED,

    /**
     * Stopping the virtual server instance.
     */
    VSERVER_STOPPING,

    /**
     * Stopped the virtual server instance.
     */
    VSERVER_STOPPED,

    /**
     * A VSDISK instance shall be deleted.
     */
    VSDISK_DELETION_REQUESTED,

    /**
     * VSDISK instance is currently in creating state in virtual system.
     */
    VSDISK_CREATING,

    /**
     * VSDISK instance is deployed to virtual system.
     */
    VSDISK_CREATED,

    /**
     * VSDISK instance is currently in attaching state to virtual server.
     */
    VSDISK_ATTACHING,

    /**
     * VSDISK instance is attached to virtual server.
     */
    VSDISK_ATTACHED,

    /**
     * VSDISK instance is currently in detaching state from virtual server.
     */
    VSDISK_DETACHING,

    /**
     * VSDISK instance is attached from virtual server.
     */
    VSDISK_DETACHED,

    /**
     * VSDISK instance is currently in deleting state from virtual system.
     */
    VSDISK_DELETING,

    /**
     * VSDISK instance is destroyed from virtual system.
     */
    VSDISK_DESTROYED,

    /**
     * A new virtual system instance has been requested.
     */
    VSYSTEM_CREATION_REQUESTED,

    /**
     * The deletion of a virtual system instance has been requested.
     */
    VSYSTEM_DELETION_REQUESTED,

    /**
     * The modification of a virtual system instance has been requested.
     */
    VSYSTEM_MODIFICATION_REQUESTED,

    /**
     * A complete stop of the virtual system has been requested.
     */
    VSYSTEM_STOP_REQUESTED,

    /**
     * The start of all servers within the virtual system has been requested.
     */
    VSYSTEM_START_REQUESTED,

    /**
     * The virtual system shall be activated.
     */
    VSYSTEM_ACTIVATION_REQUESTED,

    /**
     * The virtual system shall be deactivated.
     */
    VSYSTEM_DEACTIVATION_REQUESTED,

    /**
     * The sate representing when creating the VSYS and it is started.
     */
    VSYSTEM_CREATING,

    /**
     * Waiting until information from the guest OS (e.g. default password,
     * system name, IP) become available.
     */
    VSYSTEM_RETRIEVEGUEST,

    /**
     * Representing the state when destroying the virtual system.
     */
    VSYSTEM_DELETING,

    /**
     * Stopping the virtual server instances.
     */
    VSERVERS_STOPPING,

    /**
     * Additional servers are currently processed by a subprocess.
     */
    VSYSTEM_SUBPROCESS_SERVERS,

    /**
     * Scaling up the virtual servers in a virtual system.
     */
    VSYSTEM_SCALE_UP,

    /**
     * Scaling the virtual servers is completed.
     */
    VSYSTEM_SCALING_COMPLETED,

    /**
     * Scale up of virtual system done => waiting for scale up to complete
     * before notifying admin agent.
     */
    VSYSTEM_SCALE_UP_WAIT_BEFORE_NOTIFICATION,

    /**
     * Scale up of virtual system done => notify admin agent about new virtual
     * server.
     */
    VSYSTEM_SCALE_UP_NOTIFY_ADMIN_AGENT,

    /**
     * Scale down of virtual system is required, next operation yet to be
     * defined.
     */
    VSYSTEM_SCALE_DOWN,

    /**
     * Scale down of virtual system is required => stop virtual server.
     **/
    VSYSTEM_SCALE_DOWN_STOP_SERVER,

    /**
     * Scale down of virtual system is required => delete virtual server.
     */
    VSYSTEM_SCALE_DOWN_DESTROY_SERVER,

    /**
     * Check whether resizing of virtual servers is required and issue
     * respective commands.
     */
    VSYSTEM_RESIZE_VSERVERS,

    /**
     * Check whether modification of firewall or public IP addresses is required
     * and issue respective commands.
     */
    VSYSTEM_UPDATE_FW,

    /**
     * Wait for system to be ready again after firewall updates.
     */
    VSYSTEM_UPDATE_FW_WAITING,

    /**
     * VSERVER instance is currently started for delete vserver.
     */
    FW_STARTED_FOR_VSERVER_DELETION,

    /**
     * VSERVER instance is currently starting for delete vserver.
     */
    FW_STARTING_FOR_VSERVER_DELETION,

    /**
     * VSERVER instance is currently started for create vserver.
     */
    FW_STARTED_FOR_VSERVER_CREATION,

    /**
     * VSERVER instance is currently starting for create vserver.
     */
    FW_STARTING_FOR_VSERVER_CREATION,

    /**
     * The instance currently being handled in a manual process.
     */
    MANUAL,

    /**
     * Processing (create/modify) is currently finished.
     */
    FINISHED,

    /**
     * The instance has been destroyed and not accessible anymore.
     */
    DESTROYED,

    /**
     * Instance processing has failed.
     */
    FAILED,

    /**
     * Instance is waiting before the next operation.
     */
    WAITING_BEFORE_STOP,

    /**
     * The virtual networking components are currently being deleted.
     */
    VNET_DELETING;
}
