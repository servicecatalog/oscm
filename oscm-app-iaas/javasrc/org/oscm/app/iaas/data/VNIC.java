/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-01-08                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * Represents a virtual network interface card.
 */
public interface VNIC {

    /**
     * @return the network ID
     */
    public String getNetworkId();

    /**
     * @return the private IP
     */
    public String getPrivateIP();

    /**
     * @return the number of the virtual NIC
     */
    public String getNicNumber();

}
