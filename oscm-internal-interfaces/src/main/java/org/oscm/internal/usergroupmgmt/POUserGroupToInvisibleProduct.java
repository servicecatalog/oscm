/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 29 lut 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usergroupmgmt;

import org.oscm.internal.base.BasePO;

/**
 * This class is used to display the accessible services.
 *
 * @author BadziakP
 *
 */
public class POUserGroupToInvisibleProduct extends BasePO {

    private static final long serialVersionUID = 5016720506043698384L;
    private boolean forAllUsers;
    private long serviceKey;

    /**
     * @return the forAllUsers
     */
    public boolean isForAllUsers() {
        return forAllUsers;
    }

    /**
     * @param forAllUsers
     *            the forAllUsers to set
     */
    public void setForAllUsers(boolean forAllUsers) {
        this.forAllUsers = forAllUsers;
    }

    /**
     * @return the serviceKey
     */
    public long getServiceKey() {
        return serviceKey;
    }

    /**
     * @param serviceKey
     *            the serviceKey to set
     */
    public void setServiceKey(long serviceKey) {
        this.serviceKey = serviceKey;
    }
}
