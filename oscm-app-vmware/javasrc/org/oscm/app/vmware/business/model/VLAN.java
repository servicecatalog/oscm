/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.business.model;

import java.util.List;

/**
 * Reflects a single VLAN configuration.
 */
public class VLAN {

    private boolean isEnabled;
    private String name;
    private List<IPAddress> ipList;
    private int tkey;
    private int cluster_tkey;

    public VLAN() {
        this.name = "";
    }

    public List<IPAddress> getIPAddresses() {
        return ipList;
    }

    public void setIPAddresses(List<IPAddress> ipList) {
        this.ipList = ipList;
    }

    public int getTkey() {
        return tkey;
    }

    public void setTkey(int tkey) {
        this.tkey = tkey;
    }

    public int getClusterTKey() {
        return cluster_tkey;
    }

    public void setClusterTKey(int tkey) {
        this.cluster_tkey = tkey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
