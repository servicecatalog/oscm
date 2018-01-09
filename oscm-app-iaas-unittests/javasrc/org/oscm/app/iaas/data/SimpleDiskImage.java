/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 14, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

public class SimpleDiskImage implements DiskImage {

    private String id;
    private String name;
    private String maxCPUCount;

    public SimpleDiskImage(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getDiskImageId() {
        return id;
    }

    @Override
    public String getDiskImageName() {
        return name;
    }

    @Override
    public String getMaxCpuCount() {
        return maxCPUCount;
    }

    @Override
    public String getMaxCpuPerf() {
        return null;
    }

    @Override
    public String getMaxMemorySize() {
        return null;
    }

    public void setMaxCpuCount(String value) {
        maxCPUCount = value;
    }
}
