/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 20.02.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

public interface VSystemStatus {
    public static final String NORMAL = "NORMAL";
    public static final String RECONFIG_ING = "RECONFIG_ING";
    public static final String DEPLOYING = "DEPLOYING";
    public static final String ERROR = "ERROR";
    public static final String STOPPED = "STOPPED";
    public static final String RUNNING = "RUNNING";
}
