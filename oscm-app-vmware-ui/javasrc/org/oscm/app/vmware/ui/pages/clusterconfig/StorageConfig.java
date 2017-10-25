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
 * Reflects a single storage configuration.
 *
 * @author soehnges
 */
public class StorageConfig {

    private HierarchicalConfiguration xmlConfig;

    public StorageConfig(HierarchicalConfiguration xmlConfig) {
        this.xmlConfig = xmlConfig;
    }

    public String getName() {
        return xmlConfig.getString("[@name]");
    }

    public void setName(String name) {
        xmlConfig.setProperty("[@name]", name);
    }

    public boolean isEnabled() {
        return xmlConfig.getBoolean("[@enabled]");
    }

    public void setEnabled(boolean enabled) {
        xmlConfig.setProperty("[@enabled]", Boolean.valueOf(enabled));
    }

    public String getLimit() {
        return xmlConfig.getString("[@limit]");
    }

    public void setLimit(String limit) {
        xmlConfig.setProperty("[@limit]", limit);
    }

}
