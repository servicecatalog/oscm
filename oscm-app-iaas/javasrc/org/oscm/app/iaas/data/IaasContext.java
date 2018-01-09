/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-02-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

import java.util.List;

/**
 * Container for status and configuration objects of the virtual system and
 * server context.
 */
public class IaasContext {

    private VSystemConfiguration vSystemConfiguration;
    private String vSystemStatus;

    private VServerConfiguration vServerConfiguration;
    private String vServerStatus;

    private List<DiskImage> diskImages;

    public VSystemConfiguration getVSystemConfiguration() {
        return vSystemConfiguration;
    }

    public String getVSystemStatus() {
        return vSystemStatus;
    }

    public VServerConfiguration getVServerConfiguration() {
        return vServerConfiguration;
    }

    public String getVServerStatus() {
        return vServerStatus;
    }

    public void add(VSystemConfiguration configuration) {
        vSystemConfiguration = configuration;
    }

    public void setVSystemStatus(String status) {
        vSystemStatus = status;
    }

    public void add(VServerConfiguration configuration) {
        vServerConfiguration = configuration;
    }

    public void setVServerStatus(String status) {
        vServerStatus = status;
    }

    public List<DiskImage> getDiskImages() {
        return diskImages;
    }

    public void setDiskImages(List<DiskImage> diskImages) {
        this.diskImages = diskImages;
    }

    public void clear() {
        clearVSystemContext();
        clearVServerContext();
        diskImages = null;
    }

    public void clearVServerContext() {
        vServerConfiguration = null;
        vServerStatus = null;
    }

    public void clearVSystemContext() {
        vSystemConfiguration = null;
        vSystemStatus = null;
    }
}
