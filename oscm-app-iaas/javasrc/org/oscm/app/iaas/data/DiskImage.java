/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-12-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * Represents an disk image available for instance creation.
 */
public interface DiskImage {

    public String getDiskImageId();

    public String getDiskImageName();

    public String getMaxCpuCount();

    public String getMaxCpuPerf();

    public String getMaxMemorySize();
}
