/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 28.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

import java.util.List;

/**
 * @author Dirk Bernsau
 * 
 */
public interface VSystemConfiguration {

    public String getVSystemId();

    public String getVSystemName();

    public List<Network> getNetworks();
    
    public List<String> getNetworkIds(boolean fullID);

    public String getNetworkPrefix();

    public List<String> getServerStatus();

    public List<? extends VServerConfiguration> getVServers();

}
