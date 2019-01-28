/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.balancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.business.VMwareDatacenterInventory;
import org.oscm.app.vmware.business.model.VMwareHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Common superclass for all host balancers.
 *
 * @author Dirk Bernsau
 *
 */
public abstract class HostBalancer implements VMwareBalancer<VMwareHost> {

    private static final Logger logger = LoggerFactory
            .getLogger(HostBalancer.class);

    protected VMwareDatacenterInventory inventory;
    protected List<String> hostNames = new ArrayList<String>();

    @Override
    public void setConfiguration(Node xmlConfig) {
        if (xmlConfig != null) {
            NodeList hosts = xmlConfig.getOwnerDocument()
                    .getElementsByTagName("host");

            String str = XMLHelper.getAttributeValue(xmlConfig, "hosts", "");
            List<String> hostList = Arrays.asList(str.split(","));
            if (hostList.size() > 0) {
                for (int i = 1; i < hosts.getLength(); i++) {
                    Node h = hosts.item(i);
                    String hostName = XMLHelper.getAttributeValue(h, "name")
                            .trim();
                    if (hostList.contains(hostName)) {
                        hostNames.add(hostName);
                    }

                }
            }
        }
    }

    @Override
    public void setInventory(VMwareDatacenterInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public List<VMwareHost> getElements() {
        List<VMwareHost> result = new ArrayList<VMwareHost>();
        if (inventory != null) {
            if (hostNames.size() > 0) {
                for (String hostName : hostNames) {
                    VMwareHost host = inventory.getHost(hostName);
                    if (host != null) {
                        result.add(host);
                    }
                }
            } else {
                result.addAll(inventory.getHosts());
            }
        }
        return result;
    }

    /**
     * Checks whether the given host system is capable of hosting the requested
     * VM configuration within its configured limits.
     *
     * @param host
     *            the host system in question
     * @param properties
     *            the properties defining the requested instance
     * @return <code>true</code> when all conditions and limits are met
     */
    public boolean isValid(VMwareHost host, VMPropertyHandler properties) {
        boolean result = host != null && host.isEnabled() && host.checkVmLimit()
                && host.checkCpuLimit(properties.getConfigCPUs())
                && host.checkMemoryLimit(properties.getConfigMemoryMB());
        if (logger.isDebugEnabled() && host != null) {
            logger.debug("Checking isValid() for "
                    + (host.isEnabled() ? "enabled" : "disabled") + " host "
                    + host.getName() + " with limits "
                    + host.getLimitsAsString() + " and allocation "
                    + host.getAllocationAsString());
        }
        return result;
    }
}