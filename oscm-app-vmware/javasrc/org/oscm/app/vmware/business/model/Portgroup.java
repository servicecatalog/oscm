package org.oscm.app.vmware.business.model;

import java.util.List;

public class Portgroup {
    
    public List<PortgroupIPPool> portgroupIPPool;
    public String name;
    public String uuid;
    public int tkey;
    public int distributedVirtualSwitch_cluster_fk;
    
    public List<PortgroupIPPool> getPortgroupIPPool() {
        return portgroupIPPool;
    }
    public void setPortgroupIPPool(List<PortgroupIPPool> portgroupIPPool) {
        this.portgroupIPPool = portgroupIPPool;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public int getTkey() {
        return tkey;
    }
    public void setTkey(int tkey) {
        this.tkey = tkey;
    }
    public int getDistributedVirtualSwitch_cluster_fk() {
        return distributedVirtualSwitch_cluster_fk;
    }
    public void setDistributedVirtualSwitch_cluster_fk(
            int distributedVirtualSwitch_cluster_fk) {
        this.distributedVirtualSwitch_cluster_fk = distributedVirtualSwitch_cluster_fk;
    }

}
