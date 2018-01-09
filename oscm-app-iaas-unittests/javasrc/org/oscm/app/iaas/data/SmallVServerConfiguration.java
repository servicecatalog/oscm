/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-01-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic data object for unit testing.
 */
public class SmallVServerConfiguration implements VServerConfiguration {

    private String serverType;
    private String diskImageId;
    private String serverId;
    private String serverName;
    private String numCPU;
    private String hostName;
    private Set<VNIC> virtualNICS;
    private boolean isEFM;
    private String networkId;

    /**
     * @param diskImageId
     *            the diskImageId to set
     */
    public void setDiskImageId(String diskImageId) {
        this.diskImageId = diskImageId;
    }

    /**
     * @param serverId
     *            the serverId to set
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * @param serverName
     *            the serverName to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @param serverType
     *            the serverType to set
     */
    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    /**
     * @param numCPU
     *            the numCPU to set
     */
    public void setNumCPU(String numCPU) {
        this.numCPU = numCPU;
    }

    /**
     * @param hostName
     *            the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @param virtualNICS
     *            the virtualNICS to set
     */
    public void setVirtualNICS(Set<VNIC> virtualNICS) {
        this.virtualNICS = virtualNICS;
    }

    @Override
    public String getDiskImageId() {
        return diskImageId;
    }

    @Override
    public String getServerId() {
        return serverId;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public String getNumOfCPU() {
        return numCPU;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    public Set<VNIC> getVirtualNICs() {
        return virtualNICS == null ? new HashSet<VNIC>() : virtualNICS;
    }

    /**
     * @param nic
     */
    public void addVirtualNic(VNIC nic) {
        if (virtualNICS == null) {
            virtualNICS = new HashSet<VNIC>();
        }
        virtualNICS.add(nic);
    }

    public boolean isEFM() {
        return isEFM;
    }

    /**
     * @param isEFM
     *            the isEFM to set
     */
    public void setEFM(boolean isEFM) {
        this.isEFM = isEFM;
    }

    @Override
    public String getServerType() {
        return serverType;
    }

    @Override
    public String getNetworkId() {
        return networkId;
    }

    @Override
    public String getMemorySize() {
        return null;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

}
