/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui.pages.clusterconfig;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationFileType {
    public static final Map<Integer, ConfigurationFileType> ALL = initAll();

    private static Map<Integer, ConfigurationFileType> initAll() {
        Map<Integer, ConfigurationFileType> map = new HashMap<>();
        int i = 0;
        map.put(i++, new ConfigurationFileType("vCenter", "vcenter"));
        map.put(i++, new ConfigurationFileType("Datacenter", "datacenter"));
        map.put(i++, new ConfigurationFileType("Cluster", "cluster"));
        map.put(i++, new ConfigurationFileType("VLAN", "vlan"));
        map.put(i, new ConfigurationFileType("IP Pool", "ippool"));
        return map;
    }

    private String displayName;
    private String tableName;

    ConfigurationFileType(String displayName, String tableName) {
        this.displayName = displayName;
        this.tableName = tableName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTableName() {
        return tableName;
    }
}