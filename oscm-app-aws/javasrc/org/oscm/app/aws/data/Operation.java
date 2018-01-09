/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.aws.data;

/**
 * The operation describes what kind of basic processing is currently executed
 * for the service instance.
 */
public enum Operation {

    /**
     * A new EC2 instance has been requested.
     */
    EC2_CREATION,

    /**
     * EC2 instance modification has been requested.
     */
    EC2_MODIFICATION,

    /**
     * EC2 instance deletion has been requested.
     */
    EC2_DELETION,

    /**
     * Activation or deactivation of EC2 instance has been requested.
     */
    EC2_ACTIVATION,

    /**
     * An operation related to an EC2 instance has been requested.
     */
    EC2_OPERATION,

    /**
     * Unknown type
     */
    UNKNOWN
}
