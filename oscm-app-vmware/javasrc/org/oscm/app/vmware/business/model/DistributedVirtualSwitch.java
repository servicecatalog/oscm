/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2019
 *
 * <p>Creation Date: 26-04-2019
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.vmware.business.model;

import java.util.List;

public class DistributedVirtualSwitch {

  public List<Portgroup> portgroup;
  public String name;
  public String uuid;
  public int tkey;
  public int clusterTkey;

  public List<Portgroup> getPortgroup() {
    return portgroup;
  }

  public void setPortgroup(List<Portgroup> portgroup) {
    this.portgroup = portgroup;
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

  public int getClusterTkey() {
    return clusterTkey;
  }

  public void setClusterTkey(int cluster_tkey) {
    this.clusterTkey = cluster_tkey;
  }
  
}
