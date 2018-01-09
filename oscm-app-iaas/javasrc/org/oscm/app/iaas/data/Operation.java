/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.iaas.data;

import java.util.EnumSet;

/**
 * The operation describes what kind of basic processing is currently executed
 * for the service instance.
 */
public enum Operation {
    /**
     * A new virtual server instance has been registered.
     */
    VSERVER_CREATION,

    /**
     * virtual server instance modification has been requested.
     */
    VSERVER_MODIFICATION,

    /**
     * virtual server instance modification with Virtual Disk Creation.
     */
    VSERVER_MODIFICATION_VDISK_CREATION,

    /**
     * virtual server instance modification with Virtual Disk Deletion.
     */
    VSERVER_MODIFICATION_VDISK_DELETION,

    /**
     * A virtual server instance shall be deleted.
     */
    VSERVER_DELETION,

    /**
     * Instance activation or deactivation has been requested.
     */
    VSERVER_ACTIVATION,

    /**
     * Stop/Start instance has been requested.
     */
    VSERVER_OPERATION,

    /**
     * Unknown type
     */
    UNKNOWN,

    /**
     * A new virtual system instance has been registered.
     */
    VSYSTEM_CREATION,

    /**
     * A virtual system instance shall be deleted.
     */
    VSYSTEM_DELETION,

    /**
     * Virtual system instance modification has been requested.
     */
    VSYSTEM_MODIFICATION,

    /**
     * Virtual system activation or deactivation has been requested.
     */
    VSYSTEM_ACTIVATION,

    /**
     * An operation on the virtual system instance has been requested.
     */
    VSYSTEM_OPERATION,
    /**
     * A new virtual instance creation has been requested.
     */
    CREATION,

    /**
     * virtual instance modification has been requested.
     */
    MODIFICATION,

    /**
     * virtual instance deletion has been requested.
     */
    DELETION;

    public static EnumSet<Operation> MODIFICATIONS = EnumSet.of(MODIFICATION,
            VSYSTEM_MODIFICATION, VSERVER_MODIFICATION,
            VSERVER_MODIFICATION_VDISK_CREATION,
            VSERVER_MODIFICATION_VDISK_DELETION);

    public static EnumSet<Operation> DELETIONS = EnumSet.of(DELETION,
            VSERVER_DELETION, VSYSTEM_DELETION);

    public boolean isModification() {
        return MODIFICATIONS.contains(this);
    }

    public boolean isDeletion() {
        return DELETIONS.contains(this);
    }
}
