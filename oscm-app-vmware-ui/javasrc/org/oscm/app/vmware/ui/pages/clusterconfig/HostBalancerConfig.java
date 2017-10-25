/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.pages.clusterconfig;

import org.apache.commons.configuration2.HierarchicalConfiguration;

/**
 * Reflects the host balancer configuration.
 *
 * @author soehnges
 */
public class HostBalancerConfig {

    private HierarchicalConfiguration xmlConfig;

    public HostBalancerConfig(HierarchicalConfiguration xmlConfig) {
        this.xmlConfig = xmlConfig;
    }

    public String getBalancer() {
        return xmlConfig.getString("[@class]");
    }

    public void setBalancer(String value) {
        xmlConfig.setProperty("[@class]", value);
    }

    public String getMemoryWeight() {
        return xmlConfig.getString("[@memoryWeight]");
    }

    public void setMemoryWeight(String value) {
        xmlConfig.setProperty("[@memoryWeight]", value);
    }

    public String getCpuWeight() {
        return xmlConfig.getString("[@cpuWeight]");
    }

    public void setCpuWeight(String value) {
        xmlConfig.setProperty("[@cpuWeight]", value);
    }

    public String getVmWeight() {
        return xmlConfig.getString("[@vmWeight]");
    }

    public void setVmWeight(String value) {
        xmlConfig.setProperty("[@vmWeight]", value);
    }

}
