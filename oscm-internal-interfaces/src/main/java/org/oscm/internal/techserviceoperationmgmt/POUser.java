/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Nov 14, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.techserviceoperationmgmt;

import org.oscm.internal.base.BasePO;

/**
 * @author zhaoh.fnst
 * 
 */
public class POUser extends BasePO {

    private static final long serialVersionUID = 7328216512029073428L;

    private String userId;

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

}
