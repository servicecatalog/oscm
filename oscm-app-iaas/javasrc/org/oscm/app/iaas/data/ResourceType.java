/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 26.02.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * Enumeration of resources related to IaaS provisioning.
 */
public enum ResourceType {
    /**
     * The resource of type virtual system represents a logical unit combining
     * one or more virtual servers with other aspects such as networking or
     * firewall rules.
     */
    VSYSTEM,

    /**
     * A virtual server represents one virtual machine that might be part of a
     * virtual system.
     */
    VSERVER,

    /**
     * The type of the resource is unknown.
     */
    UNKNOWN;
}
