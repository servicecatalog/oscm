/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2018
 *
 * <p>Creation Date: 2019-04-16
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.vmware.business;

import java.util.List;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.vmware.business.model.Cluster;
import org.oscm.app.vmware.business.model.DistributedVirtualSwitch;
import org.oscm.app.vmware.business.model.Portgroup;
import org.oscm.app.vmware.business.model.PortgroupIPPool;
import org.oscm.app.vmware.persistence.DataAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortgroupIpSettings {

  private static final Logger logger = LoggerFactory.getLogger(PortgroupIpSettings.class);

  private Cluster cluster;
  private DistributedVirtualSwitch dvs;
  private Portgroup portgroup;
  private List<PortgroupIPPool> portgroupIPPool;
  private DataAccessService das;

  PortgroupIpSettings(VMPropertyHandler ph, int nic) {
    this.das = ph.getDataAccessService();
    cluster = getCluster(ph.getTargetCluster(), ph.getClusters());
    dvs =
        getDistributedVirtualSwitch(ph.getSwitchUUID(nic), ph.getDistributedVirtualSwitch(cluster));
    portgroup = getPortgroup(ph.getPortGroup(nic), ph.getPortgroup(dvs));
    portgroupIPPool = ph.getPortgroupIPPool(portgroup);
  }

  private Cluster getCluster(String targetCluster, List<Cluster> clusters) {
    Cluster cluster = null;
    for (int i = 0; clusters.size() > i; i++) {
      if (clusters.get(i).getName().equals(targetCluster)) {
        cluster = clusters.get(i);
      }
      ;
    }
    return cluster;
  }

  private DistributedVirtualSwitch getDistributedVirtualSwitch(
      String dvsName, List<DistributedVirtualSwitch> distributedVirtualSwitches) {
    DistributedVirtualSwitch DistributedVirtualSwitch = null;
    for (int i = 0; distributedVirtualSwitches.size() > i; i++) {
      logger.debug(
          "Available DistributedVIrtualSwitch: "
              + distributedVirtualSwitches.get(i).getName()
              + " UUID "
              + distributedVirtualSwitches.get(i).getUuid());
      if (distributedVirtualSwitches.get(i).getName().equals(dvsName)) {
        DistributedVirtualSwitch = distributedVirtualSwitches.get(i);
      }
      ;
    }
    return DistributedVirtualSwitch;
  }

  private Portgroup getPortgroup(String portgroupName, List<Portgroup> portgroups) {
    Portgroup portgroup = null;
    for (int i = 0; portgroups.size() > i; i++) {
      logger.debug(
          "Available portgroups: "
              + portgroups.get(i).getName()
              + " UUID "
              + portgroups.get(i).getUuid());
      if (portgroups.get(i).getName().equals(portgroupName)) {
        portgroup = portgroups.get(i);
      }
      ;
    }
    return portgroup;
  }

  public String getIpAdressFromIpPool() throws APPlatformException {
    String ip = "";
    for (int i = 0; portgroupIPPool.size() > i; i++) {
      if (!portgroupIPPool.get(i).isIn_use()) {
        try {
          das.reservePortgroupIPAddress((portgroupIPPool.get(i).getTkey()));
          portgroupIPPool.get(i).setIn_use(true);
          logger.debug("Reserve IP: " + portgroupIPPool.get(i).getIp_adress());
          ip = portgroupIPPool.get(i).getIp_adress().trim();
          return ip;
        } catch (Exception e) {
          throw new APPlatformException(
              "No IP address found Check whether you have specified an IP pool for the port group in the Technical Service. If so, there may not be IPs left in this pool",
              e);
        }
      }
    }
    return ip;
  }

  public void returnIpAdressToIpPool(String ip) throws IllegalArgumentException {
    for (int i = 0; portgroupIPPool.size() > i; i++) {
      if (portgroupIPPool.get(i).isIn_use()) {
        if (ip.equals(portgroupIPPool.get(i).getIp_adress())) {
          try {
            das.unReservePortgroupIPAddress(portgroupIPPool.get(i).getTkey());
          } catch (Exception e) {
            throw new IllegalArgumentException(
                "The ip adress " + ip + " can not be returned. The Ip does not exist");
          }
        }
      }
    }
  }

  public DistributedVirtualSwitch getDvs() {
    return dvs;
  }

  public void setDvs(DistributedVirtualSwitch dvs) {
    this.dvs = dvs;
  }

  public Portgroup getPortgroup() {
    return portgroup;
  }

  public void setPortgroup(Portgroup portgroup) {
    this.portgroup = portgroup;
  }
}
