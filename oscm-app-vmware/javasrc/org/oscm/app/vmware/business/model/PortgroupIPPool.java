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

public class PortgroupIPPool {

  private int tkey;
  private int portgroup_fk;
  private String ip_adress;
  private boolean in_use;

  public int getTkey() {
    return tkey;
  }

  public void setTkey(int tkey) {
    this.tkey = tkey;
  }

  public int getVlan_tkey() {
    return portgroup_fk;
  }

  public void setVlan_tkey(int vlan_tkey) {
    this.portgroup_fk = vlan_tkey;
  }

  public String getIp_adress() {
    return ip_adress;
  }

  public void setIp_adress(String ip_adress) {
    this.ip_adress = ip_adress;
  }

  public boolean isIn_use() {
    return in_use;
  }

  public void setIn_use(boolean in_use) {
    this.in_use = in_use;
  }
}
