/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.data;

public enum PowerState {

    /**
     * VM deallocated.
     */
    DEALLOCATED,

    /**
     * VM deallocating.
     */
    DEALLOCATING,

    /**
     * VM running.
     */
    RUNNING,

    /**
     * VM starting.
     */
    STARTING,

    /**
     * VM stopping.
     */
    STOPPING,

    /**
     * VM stopped.
     */
    STOPPED,

    /**
     * Unknown power state.
     */
    UNKNOWN;

    public static PowerState valueOfIgnoreCase(String name) {
        for (PowerState powerState : PowerState.values()) {
            if (powerState.name().equalsIgnoreCase(name)) {
                return powerState;
            }
        }
        throw new IllegalArgumentException(String.format(
                "There is no value with name '%s' in PowerState enum", name));
    }
}
