/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 28.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

import java.util.Set;

/**
 * @author Dirk Bernsau
 * 
 */
public interface VServerConfiguration {

    public String getDiskImageId();

    public String getServerId();

    public String getServerName();

    public String getNumOfCPU();

    public String getMemorySize();

    public String getHostName();

    public String getServerType();

    public String getNetworkId();

    public Set<VNIC> getVirtualNICs();

    /**
     * Returns whether the virtual server represents an 'enhanced function
     * module' (e.g. load balancer or firewall) that is subject to specific
     * conditions.
     * 
     * @return <code>true</code> if the virtual server is an EFM
     */
    public boolean isEFM();
}
