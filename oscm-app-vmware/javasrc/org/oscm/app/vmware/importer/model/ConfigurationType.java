/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-06-27
 *
 *******************************************************************************/

package org.oscm.app.vmware.importer.model;

public enum ConfigurationType {
    VCENTER("vCenter", "vcenter"),
    DATACENTER("Datacenter", "datacenter"),
    CLUSTER("Cluster", "cluster"),
    VLAN("VLAN", "vlan"),
    IPPOOL("IP Pool", "ippool"),
    DISTRIBUTEDVIRTUALSWITCH("Distributed Virtual Switch", "distributedvirtualswitch"),
    PORTGROUP("Portgroup", "portgroup"),
    PORTGROUPIPPOOL("Portgroup IP Pool", "portgroupippool");
    

    // Ordinal number.
    private int id;

    // Name of table that will be updated after import.
    private String tableName;

    // Name of configuration type that will be displayed in the UI.
    private String displayName;

    ConfigurationType(String displayName, String tableName) {
        this.id = ordinal();
        this.displayName = displayName;
        this.tableName = tableName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTableName() {
        return tableName;
    }
}