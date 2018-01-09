/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2013-12-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

public interface PublicIPStatus {

    public static final String DEPLOYING = "DEPLOYING";
    public static final String UNDEPLOYING = "UNDEPLOYING";
    public static final String DETACHED = "DETACHED";
    public static final String DETACHING = "DETACHING";
    public static final String ATTACHED = "ATTACHED";
    public static final String ATTACHING = "ATTACHING";
    public static final String ERROR = "ERROR";
}
