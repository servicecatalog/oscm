/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.model;

public class Cluster {

    public String name;
    public String loadbalancer;
    public int tkey;
    public int datacenter_tkey;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLoadbalancer() {
        return loadbalancer;
    }
    public void setLoadbalancer(String loadbalancer) {
        this.loadbalancer = loadbalancer;
    }
    public int getTkey() {
        return tkey;
    }
    public void setTkey(int tkey) {
        this.tkey = tkey;
    }
    public int getDatacenter_tkey() {
        return datacenter_tkey;
    }
    public void setDatacenter_tkey(int datacenter_tkey) {
        this.datacenter_tkey = datacenter_tkey;
    }

}
