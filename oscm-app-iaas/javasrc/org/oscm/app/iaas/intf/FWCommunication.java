/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-12-18                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.intf;

import java.util.Set;

import javax.ejb.Local;

import org.oscm.app.iaas.PropertyHandler;
import org.oscm.app.iaas.data.FWPolicy;
import org.oscm.app.iaas.data.NATRule;

/**
 * 
 */
@Local
public interface FWCommunication {

    public Set<String> updateFirewallSetting(PropertyHandler properties,
            Set<FWPolicy> policies, Set<String> knownPolicyIds)
            throws Exception;

    public Set<String> updateNATSetting(PropertyHandler properties,
            Set<NATRule> rules) throws Exception;

    public Set<NATRule> getNATSetting(PropertyHandler properties)
            throws Exception;

    public String getFirewallStatus(PropertyHandler properties)
            throws Exception;

    public void startFirewall(PropertyHandler properties) throws Exception;
}
