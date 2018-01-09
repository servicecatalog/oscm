/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: Feb 11, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * @author Dirk Bernsau
 * 
 */
public interface VServerStatus {

    public static final String DEPLOYING = "DEPLOYING";
    public static final String UNDEPLOYING = "UNDEPLOYING";
    public static final String RUNNING = "RUNNING";
    public static final String STOPPING = "STOPPING";
    public static final String STOPPED = "STOPPED";
    public static final String STARTING = "STARTING";
    public static final String FAILOVER = "FAILOVER";
    public static final String UNEXPECTED_STOP = "UNEXPECTED_STOP";
    public static final String RESTORING = "RESTORING";
    public static final String BACKUP_ING = "BACKUP_ING";
    public static final String ERROR = "ERROR";
    public static final String START_ERROR = "START_ERROR";
    public static final String STOP_ERROR = "STOP_ERROR";
    public static final String CHANGE_TYPE = "CHANGE_TYPE";
    public static final String REGISTERING = "REGISTERING";
}
